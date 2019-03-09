package voiture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import controller.SimulationController;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Window extends Application{

	private Road[][] map;
	private SimulationController sc;
	private AnimationTimer timer;

	private double t = 0;
	private double dt;
	private double duration = 20000;

	private double previous;
	private boolean initialize = false;

	private List<Car> cars;
	private List<Car> carsOnRoad;

	private boolean canGenerate = true;
	private int carGenerate = 0;
	private double nbCar = 0;
	private int generationInterval = 2;
	private int fireSwape = 5;
	private boolean canSwipe = true;
	
	// --- 
	private static final int LEFT = 0;
	private static final int RIGHT = 16;
	private static final int TOP = 0;
	private static final int BOTTOM = 14;
	// ---
	private static final String S_TOP = "Top";
	private static final String S_BOTTOM = "Bottom";
	private static final String S_LEFT = "Left";
	private static final String S_RIGHT = "Right";
	// ---

	@Override
	public void start(Stage primaryStage) throws IOException {	
		FXMLLoader fxml;
		fxml = new FXMLLoader(getClass().getClassLoader().getResource("./Simulation.fxml"));
		Scene s = new Scene(fxml.load());
		sc = fxml.getController();
		sc.setWindow(this);
		primaryStage.setScene(s);
		primaryStage.show();
		map = sc.getMap();

		// Car to add at the circuit
		Car c0 = new Car(1, 23, map[LEFT][TOP], S_TOP+S_LEFT, this);
		Car c1 = new Car(3, 23, map[RIGHT][TOP], S_TOP+S_RIGHT, this);
		Car c2 = new Car(1, 23, map[LEFT][BOTTOM], S_BOTTOM+S_LEFT, this);
		Car c3 = new Car(2, 23, map[RIGHT][BOTTOM], S_BOTTOM+S_RIGHT, this);

		Car c4 = new Car(1, 23, map[LEFT][TOP], S_TOP+S_LEFT, this);
		Car c5 = new Car(3, 23, map[RIGHT][TOP], S_TOP+S_RIGHT, this);
		Car c6 = new Car(1, 23, map[LEFT][BOTTOM], S_BOTTOM+S_LEFT, this);
		Car c7 = new Car(2, 23, map[RIGHT][BOTTOM], S_BOTTOM+S_RIGHT, this);

		Car c8 = new Car(0.2, 23, map[LEFT][TOP], S_TOP+S_LEFT, this);
		Car c9 = new Car(0.4, 23, map[RIGHT][TOP], S_TOP+S_RIGHT, this);
		Car c10 = new Car(1.3, 23, map[LEFT][BOTTOM], S_BOTTOM+S_LEFT, this);
		Car c11 = new Car(0.75, 23, map[RIGHT][BOTTOM], S_BOTTOM+S_RIGHT, this);

		Car c12 = new Car(1.1, 23, map[LEFT][TOP], S_TOP+S_LEFT, this);
		Car c13 = new Car(1.3, 23, map[RIGHT][TOP], S_TOP+S_RIGHT, this);
		Car c14 = new Car(1.0, 23, map[LEFT][BOTTOM], S_BOTTOM+S_LEFT, this);
		Car c15 = new Car(0.8, 23, map[RIGHT][BOTTOM], S_BOTTOM+S_RIGHT, this);

		Car c16 = new Car(0.2, 23, map[LEFT][TOP], S_TOP+S_LEFT, this);
		Car c17 = new Car(0.4, 23, map[RIGHT][TOP], S_TOP+S_RIGHT, this);
		Car c18 = new Car(1.0, 23, map[LEFT][BOTTOM], S_BOTTOM+S_LEFT, this);
		Car c19 = new Car(0.8, 23, map[RIGHT][BOTTOM], S_BOTTOM+S_RIGHT, this);

		Car c20 = new Car(0.45, 23, map[LEFT][TOP], S_TOP+S_LEFT, this);
		Car c21 = new Car(0.65, 23, map[RIGHT][TOP], S_TOP+S_RIGHT, this);
		Car c22 = new Car(1.0, 23, map[LEFT][BOTTOM], S_BOTTOM+S_LEFT, this);
		Car c23 = new Car(0.75, 23, map[RIGHT][BOTTOM], S_BOTTOM+S_RIGHT, this);
		cars = new ArrayList<>(Arrays.asList(c0, c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15, c16, c17, c18, c19, c20, c21, c22, c23));
		carsOnRoad = new ArrayList<>();
		nbCar = cars.size();

		timer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				if (!initialize) {
					previous = now;
					initialize = true;
				}
				else {
					update(now);
					previous = now;
				}
			}
		};
		sc.play();
	}

	public void playSimulation() {
		timer.start();
	}
	
	public void pauseSimulation() {
		timer.stop();
	}

	public void update(double now) {
		if (t < duration) {
			
			// Update time
			dt = Math.pow(10, -9) * (now - previous);
			t += dt;

			// Generate new car
			generateCar();
			
			if (sc.getHoldButtonStatus()) {
				Car c = searchCar(sc.getCarNumber());
				if (c != null) {
					askInformation(c);
					c.changeColor();
				}
			}
			else {
				Car c = searchCar(sc.getCarNumber());
				if (c != null) {
					c.revertColor();
				}
			}
			
			updateFireSystem(t);

			// Check mouvement of each car
			for (Car c : carsOnRoad) {
				c.moveToDestination();
			}
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

	public void printInformation(int numero, double acceleration, double speed, double x, double y) {
		sc.printInformation(numero, acceleration, speed, x, y);
	}

	private Car searchCar(String text) {
		try {
			int carNb = Integer.parseInt(text);
			Car car = null;
			for (Car c : carsOnRoad) {
				if (c.getNumber() == carNb) {
					car = c;
				}
			}
			return car;
		}
		catch (Exception e) {
			// We do nothing
			return null;
		}
		
	}
	
	public void askInformation(String text) {
		Car car = searchCar(text);
		if (car != null) {
			sc.printInformation(car.getNumber(), car.getAcceleration(), car.getSpeed(), car.getX(), car.getY());
		}
	}
	
	public void askInformation(Car car) {
		if (car != null) {
			sc.printInformation(car.getNumber(), car.getAcceleration(), car.getSpeed(), car.getX(), car.getY());
		}
	}

	public double getDeltaTime() {
		return dt;
	}

	public List<Car> getAllCarsExceptThis(Car car) {
		List<Car> cars = new ArrayList<>(carsOnRoad);
		cars.remove(car);
		return cars;
	}
	
	private void generateCar() {
		if (carGenerate < nbCar && (int) t % generationInterval == 0) {
			if (canGenerate) {
				carsOnRoad.add(cars.get(carGenerate));
				sc.addCar(cars.get(carGenerate));
				carGenerate++;
				canGenerate = false;
			}
		}
		else {
			canGenerate = true;
		}
	}
	
	private void updateFireSystem(double time) {
		if ((int) time % fireSwape == 0) {
			if (canSwipe) {
				for (int i = 0; i < map.length; i++) {
					for (int j = 0; j < map[0].length; j++) {
						map[i][j].updateFire();
					}
				}
				canSwipe = false;
			}
		}
		else {
			canSwipe = true;
		}
	}
}