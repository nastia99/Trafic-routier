package voiture;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Car extends Parent {

	private static Logger logger = Logger.getLogger(Car.class.toString());

	// --- Start Misc
	private Window window;
	private int number;
	private static int inc;
	private double deltaTime; 
	private boolean debug = false;
	// --- End Misc
	
	// --- Start Car Graphic
	private Pane carPane;
	private ImageView imgV = new ImageView(new Image(getClass().getResourceAsStream("/assets/voiture.png")));
	private ImageView imgVSelected = new ImageView(new Image(getClass().getResourceAsStream("/assets/voitureSelected.png")));
	// --- End Car Graphic

	// --- Start Information About Road
	private Road currentRoad;
	private Point2D entryPoint;
	private Point2D destinationPoint;
	private boolean destinationReach;
	// --- End Information About Road
	
	// --- Start Orientation
	private double rotate = 0;
	private static final int TOP = 270;
	private static final int LEFT = 180;
	private static final int RIGHT = 0;
	private static final int BOTTOM = 90;
	// --- End Orientation

	// --- Start Acceleration
	private boolean accelerating = true;
	private double acceleration = 0.1;
	private double speed = 5;
	// --- End Acceleration
	
	// --- Start Deceleration
	private static final double HIGH_SLOW = 15;
	private static final double MAX_CURVE_SPEED = 3.5;
	private static final double CURVE_DECELERATION = 5;
	private boolean slowing = false;
	// --- End Deceleration
	
	// --- Start Security
	private boolean security = false;
	private Rectangle securityZone;
	// --- End Security



	public Car(double acc, double securityZoneLenght, Road r, String position, Window w) {
		acceleration = acc;
		number = inc;
		inc++;
		window = w;
		currentRoad = r;
		entryPoint = r.getRandomEntry();
		boolean destinationSet = setRandomDestination(entryPoint);
		while (!destinationSet) {
			destinationSet = setRandomDestination(entryPoint);
		}
		
		imgVSelected.setVisible(false);
		
		// Place the pane at the entry position
		carPane = new Pane(imgV, imgVSelected);
		carPane.setTranslateX(entryPoint.getX());
		carPane.setTranslateY(entryPoint.getY());
		
		securityZone = new Rectangle(securityZoneLenght, 10, Color.TRANSPARENT);
		securityZone.setX(22 / 2 + 6);
		securityZone.setY(2);

		if (debug) {
			securityZone.setFill(Color.BLUE);
		}
		carPane.getChildren().add(securityZone);
		
		// When we click on the car we have information
		carPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				window.printInformation(number, acceleration, speed, getX(), getY());
			}			
		});
		
		setCarOnRoad(position);
		
		// Center the car 
		// TODO Improve 22/2 et 16/2 par largeur et hauteur de l image de la voiture
		imgV.setX(- 22 / 2);
		imgV.setY(- 16 / 2);		
		imgVSelected.setX(- 22 / 2);
		imgVSelected.setY(- 16 / 2);
		this.getChildren().add(carPane);
	}

	private void setCarOnRoad(String position) {
		switch(position.toUpperCase()) {
			case "TOPRIGHT":
				if (currentRoad.getEntryIndex(entryPoint) == 0) {
					setCarRotation(RIGHT);
				}
				else if (currentRoad.getEntryIndex(entryPoint) == 1) {
					setCarRotation(TOP);
				}
				break;
			case "TOPLEFT":
				if (currentRoad.getEntryIndex(entryPoint) == 0) {
					setCarRotation(TOP);
				}
				else if (currentRoad.getEntryIndex(entryPoint) == 1) {
					setCarRotation(LEFT);
				}
				break;
			case "BOTTOMRIGHT":
				if (currentRoad.getEntryIndex(entryPoint) == 0) {
					setCarRotation(BOTTOM);
				}
				else if (currentRoad.getEntryIndex(entryPoint) == 1) {
					setCarRotation(RIGHT);
				}
				break;
			case "BOTTOMLEFT":
				if (currentRoad.getEntryIndex(entryPoint) == 0) {
					setCarRotation(LEFT);
				}
				else if (currentRoad.getEntryIndex(entryPoint) == 1) {
					setCarRotation(BOTTOM);
				}
				break;
			default:
				logger.log(Level.SEVERE, "Spawn position incorrect");
		}
		
		
	}

	public double getSpeed() {
		return speed;
	}
	
	private boolean compareBorder(Bounds curr, Bounds other) {
		return (curr.contains(other.getMinX(), other.getMinY()) 
				|| curr.contains(other.getMinX(), other.getMaxY())
				|| curr.contains(other.getMaxX(), other.getMinY())
				|| curr.contains(other.getMaxX(), other.getMaxY()) );
	}

	// --- Start Move
	public boolean securityDriving(List<Car> cars) {
		Bounds thisCarSecurityBounds = carPane.localToParent(securityZone.getBoundsInParent());
//		Bounds thisCarBounds = carPane.localToParent(imgV.getBoundsInParent());
		for (Car c : cars) {
			Bounds otherCarBounds = c.getCarPane().localToParent(c.getCar().getBoundsInParent());
//			Bounds otherCarSecurityBounds = c.getCarPane().localToParent(c.getSecurityZone().getBoundsInParent());
			if (compareBorder(thisCarSecurityBounds, otherCarBounds) ) {
				if (debug) {
					securityZone.setFill(Color.RED);
				}
				prioritySlow(c.getSpeed());
				return true;
			}
		}
		if (debug) {
			securityZone.setFill(Color.BLUE);
		}
		security = false;
		accelerating = true;
		slowing = false;
		return false;
	}
	
	private void prioritySlow(double speed) {
		security = true;
		accelerating = false;
		slowing = true;
		slow(speed, HIGH_SLOW);
	}
	
	public boolean fireDriving() {
		Bounds thisCarSecurityBounds = carPane.localToParent(securityZone.getBoundsInParent());
		for (Road r : currentRoad.getNeighbours()) {
			if (r != null) {
				if (r.getType().equals(RoadType.INTERSECTION)) {
					for (Rectangle rt : r.getStopPoint()) {
						Bounds fireBounds = rt.getBoundsInLocal();
						if (compareBorder(thisCarSecurityBounds, fireBounds) && !rt.isDisable()) {
							if (debug) {
								securityZone.setFill(Color.GREEN);
							}
							prioritySlow(0);
							return true; 
						}
					}
				}
			}	
			if (debug) {
				securityZone.setFill(Color.BLUE);
			}
		}
		return false;
	}

	public void moveToDestination() {
		deltaTime = window.getDeltaTime();
		securityDriving(window.getAllCarsExceptThis(this));
		fireDriving();
		if (!destinationReach) {
			switch(currentRoad.getType()) {
			case CURVE:
				moveCurve();
				break;
			case STRAIGHT:
				moveStraight();
				break;
			case THREE:
				moveThree();
				break;
			case INTERSECTION:
				moveIntersection();
				break;
			default:
				logger.log(Level.SEVERE, "Get type of road problem");
			}
		}
		else {
			changeRoadWithDestination();
		}
	}

	private void changeRoadWithDestination() {
		currentRoad = currentRoad.changeRoad(destinationPoint);
		entryPoint = destinationPoint;
		boolean destinationSet = setRandomDestination(entryPoint);
		while (!destinationSet) {
			destinationSet = setRandomDestination(entryPoint);
		}
	}

	private void moveIntersection() {
		// Intersection doesn't have orientation different of 0
		if (currentRoad.getEntryIndex(entryPoint) == 0) {
			if (currentRoad.getExitIndex(destinationPoint) == 1) {
				moveCurveRightToTop();
			}
			else if (currentRoad.getExitIndex(destinationPoint) == 2) {
				moveStraightToLeft();
			}
			else if (currentRoad.getExitIndex(destinationPoint) == 3) {
				moveCurveRightToBottom();
			}
		}
		else if (currentRoad.getEntryIndex(entryPoint) == 1) {
			if (currentRoad.getExitIndex(destinationPoint) == 2) {
				moveCurveTopToLeft();
			}
			else if (currentRoad.getExitIndex(destinationPoint) == 3) {
				moveStraightToBottom();
			}
			else if (currentRoad.getExitIndex(destinationPoint) == 0) {
				moveCurveTopToRight();
			}
		}
		else if (currentRoad.getEntryIndex(entryPoint) == 2) {
			if (currentRoad.getExitIndex(destinationPoint) == 3) {
				moveCurveLeftToBottom();
			}
			else if (currentRoad.getExitIndex(destinationPoint) == 0) {
				moveStraightToRight();
			}
			else if (currentRoad.getExitIndex(destinationPoint) == 1) {
				moveCurveLeftToTop();
			}
		}
		else if (currentRoad.getEntryIndex(entryPoint) == 3) {
			if (currentRoad.getExitIndex(destinationPoint) == 0) {
				moveCurveBottomToRight();
			}
			else if (currentRoad.getExitIndex(destinationPoint) == 1) {
				moveStraightToTop();
			}
			else if (currentRoad.getExitIndex(destinationPoint) == 2) {
				moveCurveBottomToLeft();
			}
		}

	}

	private void moveThree() {
		switch(currentRoad.getOrientationInt()) {
		case 0:
			if (currentRoad.getEntryIndex(entryPoint) == 0) {
				if (currentRoad.getExitIndex(destinationPoint) == 1) {
					moveCurveRightToTop();
				}
				else if (currentRoad.getExitIndex(destinationPoint) == 2) {
					moveStraightToLeft();
				}
			}
			else if (currentRoad.getEntryIndex(entryPoint) == 1) {
				if (currentRoad.getExitIndex(destinationPoint) == 2) {
					moveCurveTopToLeft();
				}
				else if (currentRoad.getExitIndex(destinationPoint) == 0) {
					moveCurveTopToRight();
				}			
			}
			else if (currentRoad.getEntryIndex(entryPoint) == 2) {
				if (currentRoad.getExitIndex(destinationPoint) == 0) {
					moveStraightToRight();
				}
				else if (currentRoad.getExitIndex(destinationPoint) == 1) {
					moveCurveLeftToTop();
				}			
			}
			break;
		case 1:
			if (currentRoad.getEntryIndex(entryPoint) == 0) {
				if (currentRoad.getExitIndex(destinationPoint) == 1) {
					moveCurveBottomToRight();
				}
				else if (currentRoad.getExitIndex(destinationPoint) == 2) {
					moveStraightToTop();
				}
			}
			else if (currentRoad.getEntryIndex(entryPoint) == 1) {
				if (currentRoad.getExitIndex(destinationPoint) == 2) {
					moveCurveRightToTop();
				}
				else if (currentRoad.getExitIndex(destinationPoint) == 0) {
					moveCurveRightToBottom();
				}
			}
			else if (currentRoad.getEntryIndex(entryPoint) == 2) {
				if (currentRoad.getExitIndex(destinationPoint) == 0) {
					moveStraightToBottom();
				}
				else if (currentRoad.getExitIndex(destinationPoint) == 1) {
					moveCurveTopToRight();
				}
			}
			break;
		case 2:
			if (currentRoad.getEntryIndex(entryPoint) == 0) {
				if (currentRoad.getExitIndex(destinationPoint) == 1) {
					moveCurveLeftToBottom();
				}
				else if (currentRoad.getExitIndex(destinationPoint) == 2) {
					moveStraightToRight();
				}
			}
			else if (currentRoad.getEntryIndex(entryPoint) == 1) {
				if (currentRoad.getExitIndex(destinationPoint) == 2) {
					moveCurveBottomToRight();
				}
				else if (currentRoad.getExitIndex(destinationPoint) == 0) {
					moveCurveBottomToLeft();
				}
			}
			else if (currentRoad.getEntryIndex(entryPoint) == 2) {
				if (currentRoad.getExitIndex(destinationPoint) == 0) {
					moveStraightToLeft();
				}
				else if (currentRoad.getExitIndex(destinationPoint) == 1) {
					moveCurveRightToBottom();
				}
			}
			break;
		case 3:
			if (currentRoad.getEntryIndex(entryPoint) == 0) {
				if (currentRoad.getExitIndex(destinationPoint) == 1) {
					moveCurveTopToLeft();
				}
				else if (currentRoad.getExitIndex(destinationPoint) == 2) {
					moveStraightToBottom();
				}
			}
			else if (currentRoad.getEntryIndex(entryPoint) == 1) {
				if (currentRoad.getExitIndex(destinationPoint) == 2) {
					moveCurveLeftToBottom();
				}
				else if (currentRoad.getExitIndex(destinationPoint) == 0) {
					moveCurveLeftToTop();
				}
			}
			else if (currentRoad.getEntryIndex(entryPoint) == 2) {
				if (currentRoad.getExitIndex(destinationPoint) == 0) {
					moveStraightToTop();
				}
				else if (currentRoad.getExitIndex(destinationPoint) == 1) {
					moveCurveBottomToLeft();
				}
			}
			break;
		default:
			logger.log(Level.SEVERE, "Get Int Orientation Problem");
		}
	}

	// --- Start Straight Move
	private void moveStraight() {
		switch(currentRoad.getOrientationInt()) {
		case 0:
			if (currentRoad.getEntryIndex(entryPoint) == 0) {
				moveStraightToBottom();
			}
			else if (currentRoad.getEntryIndex(entryPoint) == 1) {
				moveStraightToTop();
			}
			break;
		case 1:
			if (currentRoad.getEntryIndex(entryPoint) == 0) {
				moveStraightToLeft();
			}
			else if (currentRoad.getEntryIndex(entryPoint) == 1) {
				moveStraightToRight();
			}
			break;
		default:
			logger.log(Level.SEVERE, "Get Int Orientation Problem");
		}
	}

	private void moveStraightToTop() {
		accelerate();
		if (carPane.getTranslateY() > destinationPoint.getY()) {
			carPane.setTranslateY(carPane.getTranslateY()-speed); 
		}	
		else {
			destinationReach = true;
		}
	}

	private void moveStraightToBottom() {
		accelerate();
		if (carPane.getTranslateY() < destinationPoint.getY()) {
			carPane.setTranslateY(carPane.getTranslateY()+speed); 
		}	
		else {
			destinationReach = true;
		}
	}

	private void moveStraightToLeft() {
		accelerate();
		if (carPane.getTranslateX() > destinationPoint.getX()) {
			carPane.setTranslateX(carPane.getTranslateX()-speed); 
		}	
		else {
			destinationReach = true;
		}
	}

	private void moveStraightToRight() {
		accelerate();
		if (carPane.getTranslateX() < destinationPoint.getX()) {
			carPane.setTranslateX(carPane.getTranslateX()+speed); 
		}
		else {
			destinationReach = true;
		}
	}
	// --- End Straight Move
	// --- Start Curve Move
	private void moveCurve() {
		switch(currentRoad.getOrientationInt()) {
		case 0:
			if (currentRoad.getEntryIndex(entryPoint) == 0) {
				moveCurveRightToTop();
			}
			else if (currentRoad.getEntryIndex(entryPoint) == 1) {
				moveCurveTopToRight();
			}
			break;
		case 1:
			if (currentRoad.getEntryIndex(entryPoint) == 0) {
				moveCurveBottomToRight();
			}
			else if (currentRoad.getEntryIndex(entryPoint) == 1) {
				moveCurveRightToBottom();
			}
			break;
		case 2:
			if (currentRoad.getEntryIndex(entryPoint) == 0) {
				moveCurveLeftToBottom();
			}
			else if (currentRoad.getEntryIndex(entryPoint) == 1) {
				moveCurveBottomToLeft();
			}
			break;
		case 3:
			if (currentRoad.getEntryIndex(entryPoint) == 0) {
				moveCurveTopToLeft();
			}
			else if (currentRoad.getEntryIndex(entryPoint) == 1) {
				moveCurveLeftToTop();
			}
			break;
		default:
			logger.log(Level.SEVERE, "Get Int Orientation Problem");
		}
	}
	// --- Start Orientation 0 / Top-Right
	private void moveCurveTopToRight() {
		slow(MAX_CURVE_SPEED, CURVE_DECELERATION);
		if (carPane.getTranslateY() < destinationPoint.getY()) {
			carPane.setTranslateY(carPane.getTranslateY()+speed); 
		}
		else if (rotate != RIGHT) {
			setCarRotation(RIGHT);
		}
		else if (carPane.getTranslateX() < destinationPoint.getX()) {
			carPane.setTranslateX(carPane.getTranslateX()+speed); 
		}
		else {
			destinationReach = true;
		}
	}

	private void moveCurveRightToTop() {
		slow(MAX_CURVE_SPEED, CURVE_DECELERATION);
		if (carPane.getTranslateX() > destinationPoint.getX()) {
			carPane.setTranslateX(carPane.getTranslateX()-speed); 
		}
		else if (rotate != TOP) {
			setCarRotation(TOP);
		}
		else if (carPane.getTranslateY() > destinationPoint.getY()) {
			carPane.setTranslateY(carPane.getTranslateY()-speed); 
		}
		else {
			destinationReach = true;
		}
	}
	// --- End Orientation 0 / Top-Right
	// --- Start Orientation 1 / Bottom-Right
	private void moveCurveBottomToRight() {
		slow(MAX_CURVE_SPEED, CURVE_DECELERATION);
		if (carPane.getTranslateY() > destinationPoint.getY()) {
			carPane.setTranslateY(carPane.getTranslateY()-speed); 
		}
		else if (rotate != RIGHT) {
			setCarRotation(RIGHT);
		}
		else if (carPane.getTranslateX() < destinationPoint.getX()) {
			carPane.setTranslateX(carPane.getTranslateX()+speed);
		}
		else {
			destinationReach = true;
		}
	}

	private void moveCurveRightToBottom() {
		slow(MAX_CURVE_SPEED, CURVE_DECELERATION);
		if (carPane.getTranslateX() > destinationPoint.getX()) {
			carPane.setTranslateX(carPane.getTranslateX()-speed);
		}
		else if (rotate != BOTTOM) {
			setCarRotation(BOTTOM);
		}
		else if (carPane.getTranslateY() < destinationPoint.getY()) {
			carPane.setTranslateY(carPane.getTranslateY()+speed); 
		}
		else {
			destinationReach = true;
		}
	}
	// --- End Orientation 1 / Bottom-Right
	// --- Start Orientation 2 / Left-Bottom
	private void moveCurveLeftToBottom() {
		slow(MAX_CURVE_SPEED, CURVE_DECELERATION);
		if (carPane.getTranslateX() < destinationPoint.getX()) {
			carPane.setTranslateX(carPane.getTranslateX()+speed); 
		}
		else if (rotate != BOTTOM) {
			setCarRotation(BOTTOM);
		}
		else if (carPane.getTranslateY() < destinationPoint.getY()) {
			carPane.setTranslateY(carPane.getTranslateY()+speed); 
		}
		else {
			destinationReach = true;
		}
	}

	private void moveCurveBottomToLeft() {
		slow(MAX_CURVE_SPEED, CURVE_DECELERATION);
		if (carPane.getTranslateY() > destinationPoint.getY()) {
			carPane.setTranslateY(carPane.getTranslateY()-speed); 
		}
		else if (rotate != LEFT) {
			setCarRotation(LEFT);
		}
		else if (carPane.getTranslateX() > destinationPoint.getX()) {
			carPane.setTranslateX(carPane.getTranslateX()-speed); 
		}
		else {
			destinationReach = true;
		}
	}
	// --- End Orientation 2 / Left-Bottom
	// --- Start Orientation 3 / Left-Top
	private void moveCurveTopToLeft() {
		slow(MAX_CURVE_SPEED, CURVE_DECELERATION);
		if (carPane.getTranslateY() < destinationPoint.getY()) {
			carPane.setTranslateY(carPane.getTranslateY()+speed); 
		}
		else if (rotate != LEFT) {
			setCarRotation(LEFT);
		}
		else if (carPane.getTranslateX() > destinationPoint.getX()) {
			carPane.setTranslateX(carPane.getTranslateX()-speed); 
		}
		else {
			destinationReach = true;
		}
	}

	private void moveCurveLeftToTop() {
		slow(MAX_CURVE_SPEED, CURVE_DECELERATION);
		if (carPane.getTranslateX() < destinationPoint.getX()) {
			carPane.setTranslateX(carPane.getTranslateX()+speed);
		}
		else if (rotate != TOP) {
			setCarRotation(TOP);
		}
		else if (carPane.getTranslateY() > destinationPoint.getY()) {
			carPane.setTranslateY(carPane.getTranslateY()-speed);
		}
		else {
			destinationReach = true;
		}
	}
	// --- End Orientation 3 / Left-Top
	// --- End Curve Move
	// --- End Move

	public void setDestination(Point2D destination) {
		destinationReach = false;
		destinationPoint = destination;
	}

	public boolean setRandomDestination(Point2D entryPoint) {
		int entryIndex = currentRoad.getEntryIndex(entryPoint);
		if (entryIndex == -1) {
			return false;
		}
		setDestination(currentRoad.getRandomExitNotParameter(entryIndex));
		return true;
	}

	@Override
	public String toString() {
		return "Voiture " + number;
	}

	public Pane getCarPane() {
		return carPane;
	}
	
	public Node getCar() {
		return imgV;
	}

	public boolean hasDestination() {
		return destinationReach;
	}

	public Road getDestination() {
		return currentRoad;
	}
	
	public int getNumber() {
		return number;
	}
	
	public double getX() {
		return carPane.getTranslateX();
	}
	
	public double getY() {
		return carPane.getTranslateY();
	}
	
	public double getAcceleration() {
		return acceleration;
	}
	
	public Rectangle getSecurityZone() {
		return securityZone;
	}
	
	public void accelerate() {
		if (accelerating && !security) {
			speed += acceleration * deltaTime;
		}
		else if (slowing && !security){
			slowing = false;
			accelerating = true;
		}
	}
	
	private void slow(double maxSpeed, double deceleration) {
		if (speed > maxSpeed) {
			accelerating = false;
			slowing = true;	
			if (speed - deltaTime * deceleration > 0) {	
				speed -= deltaTime * deceleration;
			}
			else {
				speed = 0;
			}
		}
		else {
			accelerate();
		}
	}
	
	public void setCarRotation(double rotate) {
		carPane.getTransforms().clear();
		carPane.getTransforms().add(new Rotate(rotate, 0, 0));
		this.rotate = rotate;
	}

	public void changeColor() {
		imgV.setVisible(false);
		imgVSelected.setVisible(true);
	}

	public void revertColor() {
		imgV.setVisible(true);
		imgVSelected.setVisible(false);
	}
}
