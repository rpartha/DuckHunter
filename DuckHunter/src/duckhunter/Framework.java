package duckhunter;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.IOException;
import java.net.URL;
import java.util.logging.*;
import javax.imageio.ImageIO;

public class Framework extends Canvas {
    
   
    public static int frameWidth, frameHeight;
    public static final long secInNanosec = 1000000000L, milisecInNanosec = 1000000L;
    private final int GAME_FPS = 60;
    private final long GAME_UPDATE_PERIOD = secInNanosec / GAME_FPS;
    public static enum gameState{STARTING, VISUALIZING, GAME_CONTENT_LOADING, MAIN_MENU, OPTIONS, PLAYING, GAMEOVER, DESTROYED}
    public static gameState gs;
    private long gameTime, lastTime;
    private Shooter game;
    
    
    /**
     * Image for menu.
     */
    private BufferedImage shootTheDuckMenuImg;    
    
    public Framework ()	
    {
        super();
        
        gs = gs.VISUALIZING;
        
        //We start game in new thread.
        Thread gameThread = new Thread() {
            @Override
            public void run(){
                GameLoop();
            }
        };
        gameThread.start();
    }
    
    
   /**
     * Set variables and objects.
     * This method is intended to set the variables and objects for this class, variables and objects for the actual game can be set in Game.java.
     */
    private void init()
    {

    }
    
    /**
     * Load files - images, sounds, ...
     * This method is intended to load files for this class, files for the actual game can be loaded in Game.java.
     */
    private void LoadContent()
    {
        try
        {
            URL shootTheDuckMenuImgURL = this.getClass().getResource("/shoot_the_duck/resources/images/menu.jpg");
            shootTheDuckMenuImg = ImageIO.read(shootTheDuckMenuImgURL);
        }
        catch (IOException ex) {
            Logger.getLogger(Framework.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * In specific intervals of time (GAME_UPDATE_PERIOD) the game/logic is updated and then the game is drawn on the screen.
     */
    private void GameLoop()
    {
        // This two variables are used in VISUALIZING state of the game. We used them to wait some time so that we get correct frame/window resolution.
        long visualizingTime = 0, lastVisualizingTime = System.nanoTime();
        
        // This variables are used for calculating the time that defines for how long we should put threat to sleep to meet the GAME_FPS.
        long beginTime, timeTaken, timeLeft;
        
        while(true)
        {
            beginTime = System.nanoTime();
            
            switch (gs)
            {
                case PLAYING:
                    gameTime += System.nanoTime() - lastTime;
                    
                    game.update(gameTime, mousePosition());
                    
                    lastTime = System.nanoTime();
                break;
                case GAMEOVER:
                    //...
                break;
                case MAIN_MENU:
                    //...
                break;
                case OPTIONS:
                    //...
                break;
                case GAME_CONTENT_LOADING:
                    //...
                break;
                case STARTING:
                    // Sets variables and objects.
                    init();
                    // Load files - images, sounds, ...
                    LoadContent();

                    // When all things that are called above finished, we change game status to main menu.
                    gs = gameState.MAIN_MENU;
                break;
                case VISUALIZING:
                    if(this.getWidth() > 1 && visualizingTime > secInNanosec)
                    {
                        frameWidth = this.getWidth();
                        frameHeight = this.getHeight();

                        // When we get size of frame we change status.
                        gs = gameState.STARTING;
                    }
                    else
                    {
                        visualizingTime += System.nanoTime() - lastVisualizingTime;
                        lastVisualizingTime = System.nanoTime();
                    }
                break;
            }
            
            // Repaint the screen.
            repaint();
            
            // Here we calculate the time that defines for how long we should put threat to sleep to meet the GAME_FPS.
            timeTaken = System.nanoTime() - beginTime;
            timeLeft = (GAME_UPDATE_PERIOD - timeTaken) / milisecInNanosec; // In milliseconds
            // If the time is less than 10 milliseconds, then we will put thread to sleep for 10 millisecond so that some other thread can do some work.
            if (timeLeft < 10) 
                timeLeft = 10; //set a minimum
            try {
                 //Provides the necessary delay and also yields control so that other thread can do work.
                 Thread.sleep(timeLeft);
            } catch (InterruptedException ex) { }
        }
    }
    
    /**
     * Draw the game to the screen. It is called through repaint() method in GameLoop() method.
     */
    @Override
    public void paint(Graphics2D g2d)
    {
        switch (gs)
        {
            case PLAYING:
                game.paint(g2d, mousePosition());
            break;
            case GAMEOVER:
                game.gameIsOver(g2d, mousePosition());
            break;
            case MAIN_MENU:
                g2d.drawImage(shootTheDuckMenuImg, 0, 0, frameWidth, frameHeight, null);
                g2d.drawString("Use left mouse button to shot the duck.", frameWidth / 2 - 83, (int)(frameHeight * 0.65));   
                g2d.drawString("Click with left mouse button to start the game.", frameWidth / 2 - 100, (int)(frameHeight * 0.67));                
                g2d.drawString("Press ESC any time to exit the game.", frameWidth / 2 - 75, (int)(frameHeight * 0.70));
                g2d.setColor(Color.white);
                g2d.drawString("WWW.GAMETUTORIAL.NET", 7, frameHeight - 5);
            break;
            case OPTIONS:
            break;
            case GAME_CONTENT_LOADING:
                g2d.setColor(Color.white);
                g2d.drawString("GAME is LOADING", frameWidth / 2 - 50, frameHeight / 2);
            break;
        }
    }
    
    /**
     * Starts new game.
     */
    private void newGame()
    {
        // We set gameTime to zero and lastTime to current time for later calculations.
        gameTime = 0;
        lastTime = System.nanoTime();
        
        game = new Shooter();
    }
    
    /**
     *  Restart game - reset game time and call RestartGame() method of game object so that reset some variables.
     */
    private void restartGame()
    {
        // We set gameTime to zero and lastTime to current time for later calculations.
        gameTime = 0;
        lastTime = System.nanoTime();
        
        game.restart();
        
        // We change game status so that the game can start.
        gs = gameState.PLAYING;
    }
    
    /**
     * Returns the position of the mouse pointer in game frame/window.
     * If mouse position is null than this method return 0,0 coordinate.
     * 
     * @return Point of mouse coordinates.
     */
    private Point mousePosition()
    {
        try
        {
            Point mp = this.getMousePosition();
            
            if(mp != null)
                return this.getMousePosition();
            else
                return new Point(0, 0);
        }
        catch (Exception e)
        {
            return new Point(0, 0);
        }
    }
    
    /**
     * This method is called when keyboard key is released.
     * 
     * @param event KeyEvent
     */
    @Override
    public void keyReleasedFramework(KeyEvent event)
    {
        switch (gs)
        {
            case GAMEOVER:
                if(event.getKeyCode() == KeyEvent.VK_ESCAPE)
                    System.exit(0);
                else if(event.getKeyCode() == KeyEvent.VK_SPACE || event.getKeyCode() == KeyEvent.VK_ENTER)
                    restartGame();
            break;
            case PLAYING:
            case MAIN_MENU:
                if(event.getKeyCode() == KeyEvent.VK_ESCAPE)
                    System.exit(0);
            break;
        }
    }
    
    /**
     * This method is called when mouse button is clicked.
     * 
     * @param event MouseEvent
     */
    @Override
    public void mouseClicked(MouseEvent event)
    {
        switch (gs)
        {
            case MAIN_MENU:
                if(event.getButton() == MouseEvent.BUTTON1)
                    newGame();
            break;
        }
    }
}
