package controller;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.layout.Pane;
import voiture.Road;
import voiture.RoadType;

public class MapGenerator {

	private static Logger logger = Logger.getLogger(MapGenerator.class.toString());
	
	private Road[][] map;
	private Pane simulation;
	private int min, maxHeight, maxWidth;
	
	public Road[][] generateMap(Pane p, int maxHeight, int maxWidth) {
		map = new Road[maxWidth + 1][maxHeight + 1];
		this.simulation = p;
		this.maxHeight = maxHeight;
		this.maxWidth = maxWidth;
		// ---
		for (int i = 0; i <= maxHeight; i++) {
			for (int j = 0; j <= maxWidth; j++) {
			
				// --- Start Coin Case
				if (j == min && i == min) {
					generate(RoadType.CURVE, 1, j, i); // top left
				}
				else if (i == min && j == maxWidth) {
					generate(RoadType.CURVE, 2, j, i); // top right
				}
				else if (i == maxHeight && j == min) {
					generate(RoadType.CURVE, 0, j, i); // bottom left
				}
				else if (i == maxHeight && j == maxWidth) {
					generate(RoadType.CURVE, 3, j, i); // bottom right
				}
				// --- End Coin Case

				// --- Start Border Case
				// --- Start Top Border
				else if (i == min) {
					Random r = new Random();
					if (r.nextInt(10) < 5) {
						generate(RoadType.STRAIGHT, 1, j, i);
					}
					else {
						generate(RoadType.THREE, 2, j, i);
					}
				}
				// --- End Top Border
				// --- Start Left Border
				else if (j == min) {
					Random r = new Random();
					if (r.nextInt(10) < 5) {
						generate(RoadType.STRAIGHT, 0, j, i);
					}
					else {
						generate(RoadType.THREE, 1, j, i);
					}
				}
				// --- End Left Border
				// --- Start Bottom Border
				else if (i == maxHeight) {
					int x = j;
					int y = i;
					Road prevY = map[x][--y];
					if (prevY.getType().equals(RoadType.CURVE) && (prevY.getOrientationInt() == 1 || prevY.getOrientationInt() == 2)) {
						generate(RoadType.THREE, 0, j, i);
					}
					else if (prevY.getType().equals(RoadType.STRAIGHT) && (prevY.getOrientationInt() == 0 || prevY.getOrientationInt() == 2)) {
						generate(RoadType.THREE, 0, j, i);
					}
					else if (prevY.getType().equals(RoadType.THREE) && 
							(prevY.getOrientationInt() == 1 ||
							prevY.getOrientationInt() == 2 ||
							prevY.getOrientationInt() == 3) ) {
						generate(RoadType.THREE, 0, j, i);
					}
					else if (prevY.getType().equals(RoadType.INTERSECTION)) {
						generate(RoadType.THREE, 0, j, i);
					}
					else {
						generate(RoadType.STRAIGHT, 1, j, i);
					}
				}
				// --- End Bottom Border
				// --- Start Right Border
				else if (j == maxWidth) {
					int x = j;
					int y = i;
					Road prevX = map[--x][y];
					if (prevX.getType().equals(RoadType.CURVE) && (prevX.getOrientationInt() == 0 || prevX.getOrientationInt() == 1)) {
						generate(RoadType.THREE, 3, j, i);
					}
					else if (prevX.getType().equals(RoadType.STRAIGHT) && (prevX.getOrientationInt() == 1 || prevX.getOrientationInt() == 3)) {
						generate(RoadType.THREE, 3, j, i);
					}
					else if (prevX.getType().equals(RoadType.THREE) && 
							(prevX.getOrientationInt() == 0 ||
							prevX.getOrientationInt() == 1 ||
							prevX.getOrientationInt() == 2) ) {
						generate(RoadType.THREE, 3, j, i);
					}
					else if (prevX.getType().equals(RoadType.INTERSECTION)) {
						generate(RoadType.THREE, 3, j, i);
					}
					else {
						generate(RoadType.STRAIGHT, 0, j, i);
					}
				}
				// --- End Right Border
				// --- End Border Case

				// --- Start Fill Inside
				else {
					// Save previous
					int x = j;
					int y = i;
					Road prevX = map[--x][y];
					Road prevY = map[++x][--y]; // ++x pour ajouter la valeur soustraite précédement
					y++; // y++ pour ajouter la valeur soustraite précédement
					// ------
					boolean roadCreate = true;
					RoadType futurRoad = null;
					while (roadCreate) {
						Random r = new Random();
						int random = r.nextInt(4);
						switch (random) {
						case 0: 
							futurRoad = RoadType.CURVE;
							break;
						case 1: 
							futurRoad = RoadType.STRAIGHT;
							break;
						case 2: 
							futurRoad = RoadType.THREE;
							break;
						case 3:
							futurRoad = RoadType.INTERSECTION;
							break;
						default:
							logger.log(Level.SEVERE, "Random broken !");
						}
						roadCreate = !createNextRoadByPrevious(prevX, prevY, futurRoad, j, i);
					}
				}
				// --- End Fill Inside
			}
		}
		initializeNeighbour();
		return map;
	}

	public void generate(RoadType type, int orientation, int x, int y) {
		Road road = Road.createRoad(type, orientation, x, y);
		map[x][y] = road;
		simulation.getChildren().add(road.getPane());
	}
	
	private void initializeNeighbour() {
		for (int i = 0; i <= maxWidth; i++) {
			for (int j = 0; j <= maxHeight; j++) {
				if (i > 0) {
					map[i][j].setNeighbour("Left", map[i-1][j]);
				}
				if (i < maxWidth) {
					map[i][j].setNeighbour("Right", map[i+1][j]);
				}
				if (j > 0) {
					map[i][j].setNeighbour("Top", map[i][j-1]);
				}
				if (j < maxHeight) {
					map[i][j].setNeighbour("Bottom", map[i][j+1]);
				}
			}
		}
	}

	private int[] checkCreationLeft(Road previous, RoadType type) {
		int[] orientation = null;
		// --- For each case, we see if previous element have a good rotation
		switch (previous.getType()) {
		case CURVE:
			if (previous.getOrientationInt() == 0 || previous.getOrientationInt() == 1) {
				orientation = orientationPossible(type, "Left");
			}
			break;
		case STRAIGHT:
			if (previous.getOrientationInt() == 1 || previous.getOrientationInt() == 3) {
				orientation = orientationPossible(type, "Left");
			}
			break;
		case THREE:
			if (previous.getOrientationInt() == 0 || previous.getOrientationInt() == 1 || previous.getOrientationInt() == 2) {
				orientation = orientationPossible(type, "Left");
			}
			break;
		case INTERSECTION:
			orientation = orientationPossible(type, "Left");
			break;
		default:
			logger.log(Level.SEVERE, "Previous type not supported");
		}

		if (orientation == null) {
			orientation = orientationNotPossible(type, "Left");
		}
		return orientation;
	}

	private int[] checkCreationTop(Road previous, RoadType type) {
		int[] orientation = null;
		// --- For each case, we see if previous element have a good rotation
		switch (previous.getType()) {
		case CURVE:
			if (previous.getOrientationInt() == 1 || previous.getOrientationInt() == 2) {
				orientation = orientationPossible(type, "Top");
			}
			break;
		case STRAIGHT:
			if (previous.getOrientationInt() == 0 || previous.getOrientationInt() == 2) {
				orientation = orientationPossible(type, "Top");
			}
			break;
		case THREE:
			if (previous.getOrientationInt() == 1 || previous.getOrientationInt() == 2 || previous.getOrientationInt() == 3) {
				orientation = orientationPossible(type, "Top");
			}
			break;
		case INTERSECTION:
			orientation = orientationPossible(type, "Top");
			break;
		default:
			logger.log(Level.SEVERE, "Previous type not supported");
		}
		if (orientation == null) {
			orientation = orientationNotPossible(type, "Top");
		}
		return orientation;
	}

	private int[] orientationPossible(RoadType typeRoad, String orientationType) {
		int[] orientation = null;
		if (orientationType.equalsIgnoreCase("TOP")) {
			// --- 
			if (typeRoad.equals(RoadType.CURVE)) {
				orientation = new int[2];
				orientation[0] = 0;
				orientation[1] = 3;
			}
			else if (typeRoad.equals(RoadType.THREE)) {
				orientation = new int[3];
				orientation[0] = 0;
				orientation[1] = 1;
				orientation[2] = 3;
			}
			else if (typeRoad.equals(RoadType.STRAIGHT)) {
				orientation = new int[2];
				orientation[0] = 0;	
				orientation[1] = 2;	
			}
			else if (typeRoad.equals(RoadType.INTERSECTION)) {
				orientation = new int[4];
				orientation[0] = 0;
				orientation[1] = 1;
				orientation[2] = 2;
				orientation[3] = 3;
			}
		}
		else if (orientationType.equalsIgnoreCase("LEFT")) {
			// ---
			if (typeRoad.equals(RoadType.CURVE)) {
				orientation = new int[2];
				orientation[0] = 2;
				orientation[1] = 3;
			}
			else if (typeRoad.equals(RoadType.THREE)) {
				orientation = new int[3];
				orientation[0] = 0;
				orientation[1] = 2;
				orientation[2] = 3;
			}
			else if (typeRoad.equals(RoadType.STRAIGHT)) {
				orientation = new int[2];
				orientation[0] = 1;
				orientation[1] = 3;	
			}
			else if (typeRoad.equals(RoadType.INTERSECTION)) {
				orientation = new int[4];
				orientation[0] = 0;
				orientation[1] = 1;
				orientation[2] = 2;
				orientation[3] = 3;
			}
		}
		return orientation;
	}

	private int[] orientationNotPossible(RoadType typeRoad, String orientationType) {
		int[] orientation = null;
		if (orientationType.equalsIgnoreCase("TOP")) {
			// --- 
			if (typeRoad.equals(RoadType.CURVE)) {
				orientation = new int[2];
				orientation[0] = 1;
				orientation[1] = 2;
			}
			else if (typeRoad.equals(RoadType.THREE)) {
				orientation = new int[1];
				orientation[0] = 2;
			}
			else if (typeRoad.equals(RoadType.STRAIGHT)) {
				orientation = new int[2];
				orientation[0] = 1;
				orientation[1] = 3;			
			}
			else if (typeRoad.equals(RoadType.INTERSECTION)) {
				// Althings is possible
				orientation = new int[0];
			}
		}
		else if (orientationType.equalsIgnoreCase("LEFT")) {
			// ---
			if (typeRoad.equals(RoadType.CURVE)) {
				orientation = new int[2];
				orientation[0] = 0;
				orientation[1] = 1;
			}
			else if (typeRoad.equals(RoadType.THREE)) {
				orientation = new int[1];
				orientation[0] = 1;
			}
			else if (typeRoad.equals(RoadType.STRAIGHT)) {
				orientation = new int[2];
				orientation[0] = 0;
				orientation[1] = 2;
			}
			else if (typeRoad.equals(RoadType.INTERSECTION)) {
				// Althings is possible
				orientation = new int[0];
			}
		}
		return orientation;
	}

	private boolean createNextRoadByPrevious(Road previousLeft, Road previousTop, RoadType futurRoad, int x, int y) {
		if (futurRoad == null) {
			return false;
		}
		int[] orientationLeft = checkCreationLeft(previousLeft, futurRoad);
		int[] orientationTop = checkCreationTop(previousTop, futurRoad);
		int[] tab = Compute.intersect(orientationLeft, orientationTop);
		return buildRoad(tab, futurRoad, x, y);
	}
	
	public boolean buildRoad(int[] tab, RoadType futurRoad, int x, int y) {
		if (tab.length > 0) {
			Random r = new Random();
			int rdm = r.nextInt(tab.length);
			generate(futurRoad, tab[rdm], x, y);
			return true;
		}
		else {
			return false;
		}
	}
	
}