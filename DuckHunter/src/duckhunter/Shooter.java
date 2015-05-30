package duckhunter;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import javax.imageio.ImageIO;
import java.applet.*;

public class Shooter {
	private Random random;
	private Font font;
	private ArrayList<Duck> ducks;
	private int numRunAwayDucks, numKilledDucks, score, numShots;
	private long lastTimeShoot, timeBetweenShots;
	private BufferedImage backgroundImg, grassImg, duckImg, sightImg;
	private int sightImgMidW, sightImgMidH;

	public Shooter(){
		Framework.gs = Framework.gameState.GAME_CONTENT_LOADING;

		Thread threadForInitGame = new Thread() {
			@Override
			public void run(){
				init();
				load();
				Framework.gs = Framework.gameState.PLAYING;
			}
		};
		threadForInitGame.start();
	}


	/**
	 * Set variables and objects for the game.
	 */
	private void init()
	{
		random = new Random();        
		font = new Font("monospaced", Font.BOLD, 18);

		ducks = new ArrayList<Duck>();

		numRunAwayDucks = 0;
		numKilledDucks = 0;
		score = 0;
		numShots = 0;

		lastTimeShoot = 0;
		timeBetweenShots = Framework.secInNanosec / 3;
	}

	/**
	 * Load game files - images, sounds, ...
	 */
	private void load()
	{
		try
		{
			URL backgroundURL = this.getClass().getResource("/shoot_the_duck/resources/images/background.jpg");
			backgroundImg = ImageIO.read(backgroundURL);

			URL grassURL = this.getClass().getResource("/shoot_the_duck/resources/images/grass.png");
			grassImg = ImageIO.read(grassURL);

			URL duckImgUrl = this.getClass().getResource("/shoot_the_duck/resources/images/duck.png");
			duckImg = ImageIO.read(duckImgUrl);

			URL sightURL = this.getClass().getResource("/shoot_the_duck/resources/images/sight.png");
			sightImg = ImageIO.read(sightURL);
			sightImgMidW = sightImg.getWidth() / 2;
			sightImgMidH = sightImg.getHeight() / 2;
		}
		catch (IOException ex) {
			Logger.getLogger(Shooter.class.getName()).log(Level.SEVERE, null, ex);
		}
	}


	/**
	 * Restart game - reset some variables.
	 */
	public void restart()
	{
		// Removes all of the ducks from this list.
		ducks.clear();

		// We set last duckt time to zero.
		Duck.lastDuckTime = 0;

		numRunAwayDucks = 0;
		numKilledDucks = 0;
		score = 0;
		numShots = 0;

		lastTimeShoot = 0;
	}


	/**
	 * Update game logic.
	 * 
	 * @param gameTime gameTime of the game.
	 * @param mousePosition current mouse position.
	 */
	public void update(long gameTime, Point mousePosition) 
	{
		// Creates a new duck, if it's the time, and add it to the array list.
		if(System.nanoTime() - Duck.lastDuckTime >= Duck.timeBetweenDucks)
		{
			// Here we create new duck and add it to the array list.
			ducks.add(new Duck(Duck.duckLines[Duck.nextDuckLines][0] + random.nextInt(200), Duck.duckLines[Duck.nextDuckLines][1], Duck.duckLines[Duck.nextDuckLines][2], Duck.duckLines[Duck.nextDuckLines][3], duckImg));

			// Here we increase nextDuckLines so that next duck will be created in next line.
			Duck.nextDuckLines++;
			if(Duck.nextDuckLines >= Duck.duckLines.length)
				Duck.nextDuckLines = 0;

			Duck.lastDuckTime = System.nanoTime();
		}

		// Update all of the ducks.
		for(int i = 0; i < ducks.size(); i++)
		{
			// Move the duck.
			ducks.get(i).Update();

			// Checks if the duck leaves the screen and remove it if it does.
			if(ducks.get(i).x < 0 - duckImg.getWidth())
			{
				ducks.remove(i);
				numRunAwayDucks++;
			}
		}

		// Does player numShots?
		if(Canvas.mouseButtonState(MouseEvent.BUTTON1))
		{
			// Checks if it can shoot again.
			if(System.nanoTime() - lastTimeShoot >= timeBetweenShots)
			{
				numShots++;

				// We go over all the ducks and we look if any of them was shoot.
				for(int i = 0; i < ducks.size(); i++)
				{
					// We check, if the mouse was over ducks head or body, when player has shot.
					if(new Rectangle(ducks.get(i).x + 18, ducks.get(i).y     , 27, 30).contains(mousePosition) ||
							new Rectangle(ducks.get(i).x + 30, ducks.get(i).y + 30, 88, 25).contains(mousePosition))
					{
						numKilledDucks++;
						score += ducks.get(i).score;

						// Remove the duck from the array list.
						try{
							AudioClip clip = Applet.newAudioClip(new URL("http://cdn.hark.com/swfs/player_fb.swf?pid=gpgvpgyxzd"));
							clip.play();
						} catch(MalformedURLException m){
							m.printStackTrace();
						}
						ducks.remove(i);

						// We found the duck that player shoot so we can leave the for loop.
						break;
					}
				}

				lastTimeShoot = System.nanoTime();
			}
		}

		// Game ends if 10 ducks ran away
		if(numRunAwayDucks >= 10){
			Framework.gs = Framework.gameState.GAMEOVER;
		}
	}

	/**
	 * Draw the game to the screen.
	 * 
	 * @param g Graphics2D
	 * @param mousePosition current mouse position.
	 */
	public void paint(Graphics2D g, Point mousePosition){
		g.drawImage(backgroundImg, 0, 0, Framework.frameWidth, Framework.frameHeight, null);

		// Here we draw all the ducks.
		for(int i = 0; i < ducks.size(); i++)
		{
			ducks.get(i).Draw(g);
		}

		g.drawImage(grassImg, 0, Framework.frameHeight - grassImg.getHeight(), Framework.frameWidth, grassImg.getHeight(), null);

		g.drawImage(sightImg, mousePosition.x - sightImgMidW, mousePosition.y - sightImgMidH, null);

		g.setFont(font);
		g.setColor(Color.darkGray);

		g.drawString("RUNAWAY: " + numRunAwayDucks, 10, 21);
		g.drawString("KILLS: " + numKilledDucks, 160, 21);
		g.drawString("SHOOTS: " + numShots, 299, 21);
		g.drawString("SCORE: " + score, 440, 21);
	}


	/**
	 * Draw the game over screen.
	 * 
	 * @param g Graphics2D
	 * @param mousePosition Current mouse position.
	 */
	public void gameIsOver(Graphics2D g, Point mousePosition){
		paint(g, mousePosition);

		g.setColor(Color.black);
		g.drawString("Game Over", Framework.frameWidth / 2 - 39, (int)(Framework.frameHeight * 0.65) + 1);
		g.drawString("Press space or enter to restart.", Framework.frameWidth / 2 - 149, (int)(Framework.frameHeight * 0.70) + 1);
		g.setColor(Color.red);
		g.drawString("Game Over", Framework.frameWidth / 2 - 40, (int)(Framework.frameHeight * 0.65));
		g.drawString("Press space or enter to restart.", Framework.frameWidth / 2 - 150, (int)(Framework.frameHeight * 0.70));
	}
}
