package controller;

import java.util.logging.Logger;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import voiture.Car;
import voiture.Road;
import voiture.Window;

public class SimulationController {

	@FXML
	private TextField car;
	@FXML
	private TextField accMax;
	@FXML
	private TextField currentSpeed;
	@FXML
	private TextField xPosition;
	@FXML
	private TextField yPosition;
	@FXML
	private CheckBox holdInformation;
	@FXML
	private Button play;
	@FXML
	private Button pause;


	private Window window;

	private double dimension = 64;

	private static Logger logger = Logger.getLogger(SimulationController.class.toString());
	private int MIN, MAX_WIDTH_COORD, MAX_HEIGHT_COORD;
	Road[][] map;

	@FXML
	private Pane simulation;

	@FXML
	private void initialize() {
		double width = simulation.getPrefWidth() - 64;
		double height = simulation.getPrefHeight() - 64;    	
		MIN = 0;
		MAX_WIDTH_COORD = (int) (width / dimension);
		MAX_HEIGHT_COORD = (int) (height / dimension);
		MapGenerator mp = new MapGenerator();
		map = mp.generateMap(simulation, MAX_HEIGHT_COORD, MAX_WIDTH_COORD);
		askInformationSystem();
	}

	public void addCar(Car c) {
		simulation.getChildren().add(c);
	}

	public Road[][] getMap(){
		return map;
	}

	public Pane getPane() {
		return simulation;
	}

	public void printInformation(int numero, double acceleration, double speed, double x, double y) {
		car.setText(Integer.toString(numero));
		accMax.setText(Double.toString(acceleration));
		currentSpeed.setText(Double.toString(speed));
		xPosition.setText(Double.toString(x));
		yPosition.setText(Double.toString(y));
	}
	
	public void askInformationSystem() {
		car.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode().equals(KeyCode.ENTER)) {
					window.askInformation(car.getText());
				}
			}
		});
	}

	public boolean getHoldButtonStatus() {
		return holdInformation.isSelected();
	}
	
	public void setWindow(Window window) {
		this.window = window;
	}

	public String getCarNumber() {
		return car.getText();
	}
	
	public void play() {
		play.setDisable(true);
		pause.setDisable(false);
		window.playSimulation();
	}
	
	public void pause() {
		pause.setDisable(true);
		play.setDisable(false);
		window.pauseSimulation();
	}
}