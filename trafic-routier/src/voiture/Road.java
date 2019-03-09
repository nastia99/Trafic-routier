package voiture;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;

public class Road {

	private static Logger logger = Logger.getLogger(Road.class.toString());
	private String name;

	private boolean debug = false;

	// --- Start Image Foreach Road
	private ImageView currentImage;
	private ImageView imgStraight = new ImageView(new Image(getClass().getResourceAsStream("/assets/straightRoad.png")));
	private ImageView imgCurve = new ImageView(new Image(getClass().getResourceAsStream("/assets/curveRoad.png")));
	private ImageView imgThree = new ImageView(new Image(getClass().getResourceAsStream("/assets/threeRoad.png")));
	private ImageView imgIntersection = new ImageView(new Image(getClass().getResourceAsStream("/assets/intersectionRoad.png")));
	// --- End Image Foreach Road

	private int longueur; // longueur de la route
	private int largeur; // largeur de la route

	private Circle c1;
	private Circle c2;

	private List<Point2D> entryPoints = new ArrayList<>();
	private List<Point2D> exitPoints = new ArrayList<>();
	private List<Rectangle> fireGraphics = new ArrayList<>();
	private List<Rectangle> stopPoints = new ArrayList<>();
	private boolean currentFire = true;

	private int orientationInt;
	private Pane pane;
	private RoadType type;

	private double x;
	private double y;
	private double orientation;

	// --- Start Neigbour
	private Road left = null;
	private Road right = null;
	private Road top = null;
	private Road bottom = null;
	// --- End Neighbour


	public double getOrientation() {
		return orientation;
	}

	public void setOrientation(double o) {
		orientation = o;
	}

	public void setNeighbour(String neighbour, Road element) {
		switch(neighbour.toUpperCase()) {
		case "LEFT":
			left = element;		
			break;
		case "RIGHT":
			right = element;
			break;
		case "TOP":
			top = element;
			break;
		case "BOTTOM":
			bottom = element;
			break;
		default: 
			logger.log(Level.SEVERE, "Bad neighbour");
		}
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public void awake(double x, double y, int startFrom, int nb, double rot) {
		pane = new Pane();
		pane.setTranslateX(x + (longueur / 2));
		pane.setTranslateY(y + (largeur / 2));
		currentImage.setLayoutX(- (longueur / 2));
		currentImage.setLayoutY(- (largeur / 2));
		pane.getChildren().add(currentImage);

		Point2D centre = Point2D.ZERO; // Mettre la largeur ici 

		List<Circle> tmpEntries = new ArrayList<>();
		List<Circle> tmpExits = new ArrayList<>();

		for (int i = 1; i <= nb; i++) {
			// TODO Improve le 32 en longueur / 2
			Point2D centerPoint = new Point2D(centre.getX() + Math.sin(Math.toRadians(360 / rot) * i) * 32, centre.getY() + Math.cos(Math.toRadians(360 / rot) * i) * 32);
			Point2D centerPointFire = new Point2D(centre.getX() + Math.sin(Math.toRadians(360 / rot) * i) * 28, centre.getY() + Math.cos(Math.toRadians(360 / rot) * i) * 28);
			// On élargit le point centré pour avoir les deux points de sorties/entrées 
			Point2D entryPoint;
			Point2D exitPoint;

			Point2D fireStop = null;
			// We use rot because Straight and Curve have the same nb
			if (nb != 2 || rot != 2) { // No Straight Road
				if (i%2 != 0) {
					entryPoint = new Point2D(centerPoint.getX() + Math.cos(Math.toRadians(360 / rot) * i) * -(16/2), centerPoint.getY() + Math.sin(Math.toRadians(360 / rot) * i) * -(16/2));
					exitPoint = new Point2D(centerPoint.getX() + Math.cos(Math.toRadians(360 / rot) * i) * 16/2, centerPoint.getY() + Math.sin(Math.toRadians(360 / rot) * i) * 16/2);
					fireStop = new Point2D(centerPointFire.getX() + Math.cos(Math.toRadians(360 / rot) * i) * -10, centerPointFire.getY() + Math.sin(Math.toRadians(360 / rot) * i) * -10);
				}
				else {
					entryPoint = new Point2D(centerPoint.getX() + Math.cos(Math.toRadians(360 / rot) * i) * 16/2, centerPoint.getY() + Math.sin(Math.toRadians(360 / rot) * i) * 16/2);
					exitPoint = new Point2D(centerPoint.getX() + Math.cos(Math.toRadians(360 / rot) * i) * -(16/2), centerPoint.getY() + Math.sin(Math.toRadians(360 / rot) * i) * -(16/2));
					fireStop = new Point2D(centerPointFire.getX() + Math.cos(Math.toRadians(360 / rot) * i) * 10, centerPointFire.getY() + Math.sin(Math.toRadians(360 / rot) * i) * 10);
				}
			}
			else { // Straight Road
				entryPoint = new Point2D(centerPoint.getX() + Math.cos(Math.toRadians(360 / rot) * i) * 16/2, centerPoint.getY() + Math.sin(Math.toRadians(360 / rot) * i) * 16/2);
				exitPoint = new Point2D(centerPoint.getX() + Math.cos(Math.toRadians(360 / rot) * i) * -(16/2), centerPoint.getY() + Math.sin(Math.toRadians(360 / rot) * i) * -(16/2));
			}

			if (debug) {
				c1 = new Circle(entryPoint.getX(), entryPoint.getY(), 2, Color.RED);
				c2 = new Circle(exitPoint.getX(), exitPoint.getY(), 2, Color.BLUE);
				tmpEntries.add(c1);
				tmpExits.add(c2);
				pane.getChildren().addAll(c1, c2);
				// ---
				Line lb = new Line();
				lb.setStartX(centre.getX());	lb.setStartY(centre.getY());
				lb.setEndX(entryPoint.getX());			lb.setEndY(entryPoint.getY());
				Line lbbb = new Line();
				lbbb.setStartX(centre.getX());	lbbb.setStartY(centre.getY());
				lbbb.setEndX(exitPoint.getX());		lbbb.setEndY(exitPoint.getY());
				// ---
				pane.getChildren().addAll(lb, lbbb);
			}
			else {
				c1 = new Circle(entryPoint.getX(), entryPoint.getY(), 1, Color.TRANSPARENT);
				c2 = new Circle(exitPoint.getX(), exitPoint.getY(), 1, Color.TRANSPARENT);
				tmpEntries.add(c1);
				tmpExits.add(c2);
				pane.getChildren().addAll(c1, c2);
			}

			// To create fire system on intersection
			if (type.equals(RoadType.INTERSECTION) && fireStop != null) {
				Rectangle r = new Rectangle(15, 5, Color.RED);
				r.setX(fireStop.getX());
				r.setY(fireStop.getY());
				if (i % 2 == 0) {
					r.setDisable(false);
					r.setFill(Color.TRANSPARENT);
				}
				Rotate rotation = new Rotate((360 / rot) * i, r.getX() + r.getWidth() / 2, r.getY() + r.getHeight() / 2);
				r.getTransforms().add(rotation);
				pane.getChildren().add(r);
			}
		}	

		// We apply the rotation individually at every children of pane
		// Doesn't at all childrens in the same time to apply localToParent
		// For the picture, we translate pivot of the rotation at center
		for (Node n : pane.getChildren()) {
			if (n instanceof ImageView) {
				n.getTransforms().add(new Rotate(getOrientation(), longueur / 2, largeur / 2));
			}
			else {
				Rotate r = new Rotate(0, 0, 0);
				r.setAngle(getOrientation());
				n.getTransforms().add(r);
			}
		}

		// We need to replace point in the global repere
		for (Node n : pane.getChildren()) {
			if (n instanceof Circle) {
				Circle c = (Circle) n;
				Point2D pt = c.localToParent(c.getCenterX(), c.getCenterY());
				if (tmpEntries.contains(c)) {
					entryPoints.add(new Point2D(pane.getTranslateX() +  pt.getX(), pane.getTranslateY() +  pt.getY()));
				}
				else {
					exitPoints.add(new Point2D(pane.getTranslateX() + pt.getX(), pane.getTranslateY() + pt.getY()));
				}
			}
			else if (n instanceof Rectangle) {
				Rectangle r = (Rectangle) n;
				Point2D pt = r.localToParent(r.getX(), r.getY());
				Rectangle rStop = new Rectangle();
				rStop.setX(pane.getTranslateX() + pt.getX());
				rStop.setY(pane.getTranslateY() + pt.getY());
				fireGraphics.add(r);
				stopPoints.add(rStop);
			}
		}
		// To let GC do its work
		tmpEntries = null;
		tmpExits = null;
	}

	public static Road createRoad(RoadType type, int orientation, int x, int y) {
		Road road = null;
		switch (type) {
		case CURVE:
			road = new Road(x, y, orientation, 2, 4, type, "Raod " + x + "*" + y);
			break;
		case STRAIGHT:
			// Orientation in a line don't care about rotation of more 2
			road = new Road(x, y, orientation % 2, 2, 2, type, "Raod " + x + "*" + y);
			break;
		case THREE:
			road = new Road(x, y, orientation, 3, 4, type, "Raod " + x + "*" + y);
			break;
		case INTERSECTION:
			// Intersection don't care the rotation
			road = new Road(x, y, 0, 4, 4, type, "Raod " + x + "*" + y);
			break;
		default:
			logger.log(Level.SEVERE, "Type of road not supported");
		}
		return road;
	}

	public Road getNeighbour(String neighbour) {
		switch (neighbour.toUpperCase()) {
		case "LEFT":
			return left;
		case "RIGHT":
			return right;
		case "TOP":
			return top;
		case "BOTTOM":
			return bottom;
		default:
			return null;
		}
	}

	public List<Road> getNeighbours(){
		List<Road> neigbours = new ArrayList<>();
		neigbours.add(left);
		neigbours.add(right);
		neigbours.add(top);
		neigbours.add(bottom);
		return neigbours;
	}

	private void setType(RoadType type) {
		this.type = type;
	}

	public RoadType getType() {
		return type;
	}

	public Road(int x, int y, int orientation, int nb, int split, RoadType type, String name) {
		setLongueur(longueur);
		setX(x);
		setY(y);
		setType(type);
		setImage(type);
		this.name = name;
		orientationInt = orientation;
		setOrientation(orientation * (360 / 4));
		this.largeur = 64; // largeur de la route choisi arbitrairement pour toutes les routes
		this.longueur = 64;
		awake(x * longueur, y * largeur, orientation * (360 / split), nb, split);
	}

	private void setImage(RoadType type) {
		switch (type) {
		case CURVE:
			currentImage = imgCurve;
			break;
		case STRAIGHT:
			currentImage = imgStraight;
			break;
		case THREE:
			currentImage = imgThree;
			break;
		case INTERSECTION:
			currentImage = imgIntersection;
			break;
		default:
			logger.log(Level.SEVERE, "Incorrect type");
		}
	}
	
	public void updateFire() {
		if (getType().equals(RoadType.INTERSECTION)) {
			for (int i = 1; i <= 4; i++) {
				if (currentFire) {
					if (i % 2 != 0) {
						stopPoints.get(i-1).setDisable(true);
						fireGraphics.get(i-1).setFill(Color.TRANSPARENT);
					}
					else {
						stopPoints.get(i-1).setDisable(false);
						fireGraphics.get(i-1).setFill(Color.RED);
					}
				}
				else {
					if (i % 2 == 0) {
						stopPoints.get(i-1).setDisable(true);
						fireGraphics.get(i-1).setFill(Color.TRANSPARENT);
					}
					else {
						stopPoints.get(i-1).setDisable(false);
						fireGraphics.get(i-1).setFill(Color.RED);
					}
				}
			}
			currentFire = !currentFire;
		}
	}

	public void rotateRoad(double orientation) {
		setOrientation(orientation);		
	}

	public int getLongueur() {
		return longueur;
	}

	public void setLongueur(int longueur) {
		this.longueur = longueur;
	}

	public Node getImageView() {
		return currentImage;
	}

	public int getOrientationInt() {
		return orientationInt;
	}

	public Pane getPane() {
		return pane;
	}

	public List<Point2D> getEntryPoints() {
		return entryPoints;
	}

	public List<Point2D> getExitPoints() {
		return exitPoints;
	}

	public List<Rectangle> getStopPoint(){
		return stopPoints;
	}

	public int getEntryIndex(Point2D entryPoint) {
		for (int i = 0; i < entryPoints.size(); i++) {
			if (entryPoint.equals(entryPoints.get(i))) {
				return i;
			}
		}
		return -1;
	}

	public int getExitIndex(Point2D exitPoint) {
		for (int i = 0; i < exitPoints.size(); i++) {
			if (exitPoint.equals(exitPoints.get(i))) {
				return i;
			}
		}
		return -1;
	}

	public int getRandomIndexEntry() {
		Random r = new Random();
		return r.nextInt(entryPoints.size());
	}

	public int getRandomIndexExit() {
		Random r = new Random();
		return r.nextInt(exitPoints.size());
	}

	public Point2D getEntry(int i) {
		return entryPoints.get(i);
	}

	public Point2D getExit(int i) {
		return exitPoints.get(i);
	}

	public Point2D getRandomEntry() {
		Random r = new Random();
		return entryPoints.get(r.nextInt(entryPoints.size()));
	}

	public Point2D getRandomExit() {
		Random r = new Random();
		return exitPoints.get(r.nextInt(exitPoints.size()));
	}

	/**
	 * 
	 * @param i The axe number of entry
	 * @return A point2D who aren't on the same axe at the entryPoint
	 */
	public Point2D getRandomExitNotParameter(int i) {
		int number;
		do {
			Random r = new Random();
			number = r.nextInt(exitPoints.size());
		} while (number == i);
		return exitPoints.get(number);
	}

	public Road changeRoad(Point2D destinationPoint) {
		if (left != null) {
			for (Point2D pt : left.getEntryPoints()) {
				if (pt.equals(destinationPoint)) {
					return left;
				}
			}
		}
		if (right != null) {
			for (Point2D pt : right.getEntryPoints()) {
				if (pt.equals(destinationPoint)) {
					return right;
				}
			}
		}
		if (top != null) {
			for (Point2D pt : top.getEntryPoints()) {
				if (pt.equals(destinationPoint)) {
					return top;
				}
			}
		}
		if (bottom != null) {
			for (Point2D pt : bottom.getEntryPoints()) {
				if (pt.equals(destinationPoint)) {
					return bottom;
				}
			}
		}
		logger.log(Level.SEVERE, "FAIL reach destination");
		return null;
	}

	@Override
	public String toString() {
		return name;
	}
}