/**
 * 
 */
package com.IDG.mapSimulator;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JPanel;

import com.IDG.controller.Game;
import com.IDG.controller.GameFileManager;
import com.IDG.enemyFactory.EnemyFactory;
import com.IDG.enemyFactory.EnemyType;
import com.IDG.playGame.EnemyPath;

/**
 * 
 * This class is the view for the Game Simulation GUI
 * 
 * @author Pavan Sokke Nagaraj <pavansn8@gmail.com>
 * @version Build 1
 * @since Build 1
 *
 */
public class MapSimulatorView extends JPanel implements Runnable {

	/**
	 * A simple thread intialized to repaint
	 */
	public Thread paintThread = new Thread(this);

	/**
	 * Static variable to hold number of rows in the map grid
	 */
	public static int gridRow;
	/**
	 * Static variable to hold number of columns in the map grid
	 */
	public static int gridColumn;

	/**
	 * variable to hold the game's power.The value is set from
	 * {@code Game.INITIAL_GAME_POWER}
	 */
	public static int power = Game.getInstance().INITIAL_GAME_POWER;

	/**
	 * variable to hold the game's health.The value is set from
	 * {@code Game.INITIAL_GAME_HEALTH}
	 */
	public static int health = Game.getInstance().INITIAL_GAME_HEALTH;

	/**
	 * variable to hold the each grids game value
	 */
	public static char[][] gameValue;

	/**
	 * class Room initialized to hold the grid
	 */
	public static Room room = new Room();

	/**
	 * A point representing a location in (x,y) JPanel MapSimulatorView space.
	 * Default value set to {@code Point(0, 0); }
	 */
	public static Point mse = new Point(0, 0);

	/**
	 * class Arsenal initialization which holds the towers
	 */
	public static Arsenal arsenal;

	/**
	 * Path of enemy that has to be followed
	 */
	public static LinkedList<Point> enemyPath = new LinkedList<Point>();
	/**
	 * Enemy Factory
	 */
	public static EnemyFactory enemyFactory = new EnemyFactory();
	/**
	 * Enemy Type
	 */
	public static String enemyType = "bossenemy";
	/**
	 * List of Enemies on Map
	 */
	public static ArrayList<EnemyType> enemiesOnMap = new ArrayList<EnemyType>();
	/**
	 * boolean to check if enemies are running or not
	 */
	public static boolean moveEnemy = false;
	/**
	 * Map start X pos
	 */
	public static int mapXStart = 0;
	/**
	 * Map start Y pos
	 */
	public static int mapYStart = 0;
	/**
	 * Game level
	 */
	public static int level = 0;
	/**
	 * boolean to define if game is lost
	 */
	public static boolean isGameLost=false;
	/**
	 * boolean to define if game is won
	 */
	public static boolean isGameWon=false;

	/**
	 * Constructs a new object of our map simulator and start the paintThread
	 */
	public MapSimulatorView() {
		super();
		paintThread.start();
	}

	/**
	 * run's the painThread, initialize the arsenal class and repaint
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		addMouseListener(new KeyHandel());
		addMouseMotionListener(new KeyHandel());
		arsenal = new Arsenal();
		long beforeTime, timeDiff, sleep;
		beforeTime = System.currentTimeMillis();
		while (true) {

			repaint();
			timeDiff = System.currentTimeMillis() - beforeTime;
			sleep = 30 - timeDiff;

			if (sleep < 0) {
				sleep = 10;
			}

			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				System.out.println("Interrupted: " + e.getMessage());
			}

			beforeTime = System.currentTimeMillis();
		}

	}

	/**
	 * paint the game simulator GUI components
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(Graphics graphic) {

		// draws a white background
		graphic.setColor(Color.WHITE);
		graphic.fillRect(0, 0, getWidth(), getHeight());
		// System.out.println(getWidth() + "\t\t" + getHeight());

		// draws a black box to enter text
		graphic.setColor(Color.BLACK);
		graphic.drawRect(20, 0, 1200, 30);

		if (level != 0) {
			graphic.setFont(new Font("Courier New", Font.BOLD, 20));
			graphic.drawString("GAME LEVEL : " + level, 25, 20);
		}

		if (isGameLost) {
			graphic.setFont(new Font("Courier New", Font.BOLD, 20));
			graphic.drawString("GAME LOST", 180, 20);
		}
		if (isGameWon) {
			graphic.setFont(new Font("Courier New", Font.BOLD, 20));
			graphic.drawString("GAME WON", 180, 20);
		}

		// draw a yellow background for game grid
		graphic.setColor(Color.ORANGE);
		graphic.fillRect(20, 35, 650, 750);

		graphic.setColor(Color.LIGHT_GRAY);
		graphic.fillRect(700, 35, 500, 250);

		// game Tower board
		graphic.setColor(Color.GRAY);
		graphic.fillRect(700, 300, 500, 250);

		// game life board
		graphic.setColor(Color.DARK_GRAY);
		graphic.fillRect(700, 565, 500, 220);

		if (gridRow != 0 || gridColumn != 0) {
			room = new Room(gridColumn, gridRow, gameValue);
			room.drawGameArena(graphic, gameValue);

			arsenal.draw(graphic);

			long start = System.currentTimeMillis();
			if(MapSimulatorView.health<0){
				Arsenal.resetGame();
				isGameLost=true;

			}
			if(MapSimulatorView.level>10&&MapSimulatorView.health>0){
				Arsenal.resetGame();
				isGameWon=true;
			}
			
			if (moveEnemy) {
				updateEnemies(graphic);
				updateTower(graphic);

			}
			long time = System.currentTimeMillis() - start;
		}
	}
	/**
	 * This method will update all the enemies on the map, eventually reducing health and killing them among others
	 * @param graphic Graphic variable to paint screen
	 */
	public void updateEnemies(Graphics graphic) {
		for (int k = 0; k < enemiesOnMap.size(); k++) {
			if (enemiesOnMap.size() > 0 && enemiesOnMap.get(k) != null) {
				enemiesOnMap.get(k).update(graphic);
			}
			if (enemiesOnMap.size() > 0 && enemiesOnMap.get(k) != null) {
				enemiesOnMap.get(k).draw(graphic);
			}
		}
	}
	/**
	 * This method will update all the towers on the map, eventually finding enemies and killing them among others
	 * @param graphic Graphic variable to paint screen
	 */
	public void updateTower(Graphics graphic) {
		for (int i = 0; i < gridRow; i++) {
			for (int j = 0; j < gridColumn; j++) {
				if (gameValue[i][j] == 'G' || gameValue[i][j] == 'R'
						|| gameValue[i][j] == 'B') {
					Tower tower = GameFileManager.getTowerObject(i, j);
					int towerX = MapSimulatorView.room.block[i][j].x;
					int towerY = MapSimulatorView.room.block[i][j].y;
					if (tower.hasHitOnce
							|| tower.attackDelay > tower.maxAttackDelay) {
						tower.hasHitOnce = false;
						ArrayList<EnemyType> currentEnemyList = tower
								.calculateEnemy(enemiesOnMap, towerX, towerY);
						if (currentEnemyList != null) {
							for (int k = 0; k < currentEnemyList.size(); k++) {
								EnemyType currentEnemy = currentEnemyList
										.get(k);
								if (currentEnemy != null) {
									if (tower.towerAttackType == 2
											|| tower.towerAttackType == 1) {
										int temphealth1;
										temphealth1 = currentEnemy
												.getCurrentHealth();
										temphealth1 = temphealth1
												- tower.damage;
										currentEnemy
										.setCurrentHealth(temphealth1);
									} else if (tower.towerAttackType == 3) {
										int temphealth;
										temphealth = currentEnemy
												.getCurrentHealth();
										temphealth = temphealth - tower.damage;
										currentEnemy
										.setCurrentHealth(temphealth);
										currentEnemy.setEnemyCurrentSpeed(5);
									}
									tower.drawFireEffect(graphic, currentEnemy,
											towerX, towerY);
									tower.attackDelay = 0;
								}
							}
						}
					} else {
						tower.attackDelay += 1;
						GameFileManager.saveTowerObject(tower, i, j);
					}
				}
			}
		}
	}
}
