/**
 *   The MIT License
 *
 *  Copyright 2011 Andrew James <ephphatha@thelettereph.com>.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package au.edu.csu.bofsa;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.GameState;
import org.newdawn.slick.state.StateBasedGame;

/**
 * @author ephphatha
 *
 */
public class InGameStateDP implements GameState, CreepManager, Runnable {
  private int stateID;
  
  protected GameLevelST map;
  
  private Thread updateThread;
  private Thread daemonThread;
  private ThreadPoolExecutor pool;
  List<Future<?>> tasks;
  
  private List<Tower> towers;
  private List<Tower> towerBallast;
  private List<Creep> creeps;
  private List<Creep> creepBallast;
  private Queue<Creep> deadCreeps;
  private Queue<Creep> newCreeps;
  
  private CreepFactory creepFactory;
  
  private Logger logger;
  private Logger logger2;

  private Logger.Mode logMode;

  private int maxThreads;

  @SuppressWarnings("unused")
  private InGameStateDP() {
    this(0, Integer.MAX_VALUE, Logger.Mode.BASIC);
  }
  
  public InGameStateDP(int id, int maxThreads, Logger.Mode logMode) {
    this.stateID = id;
    
    this.maxThreads = maxThreads;
    
    this.logMode = logMode;

    this.towers = new LinkedList<Tower>();
    this.towerBallast = new LinkedList<Tower>();
    this.creeps = new CopyOnWriteArrayList<Creep>();
    this.creepBallast = new LinkedList<Creep>();
    this.deadCreeps = new ConcurrentLinkedQueue<Creep>();
    this.newCreeps = new ConcurrentLinkedQueue<Creep>();

    this.creepFactory = new CreepFactory();
    
    this.tasks = new LinkedList<Future<?>>();
    
    this.logger = new Logger();
  }

  @Override
  public int getID() {
    return this.stateID;
  }

  @Override
  public void enter(GameContainer container, StateBasedGame game)
      throws SlickException {
    try {
      this.map = new GameLevelST("test");
    } catch (SlickException e) {
      e.printStackTrace();
    }
    
    this.updateThread = new Thread(this);
    
    int numThreads = Math.max(Math.min(Runtime.getRuntime().availableProcessors() - 2, this.maxThreads), 1);
    
    this.pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);
    
    this.daemonThread = new Thread(new Runnable(){
      public void run() {
        final long sampleInterval = 5000;
        
        long last = System.nanoTime();
        Logger logger = new Logger();
        logger.setLogMode(Logger.Mode.DETAILED);
        logger.startLogging("THREADCOUNT");
        
        while (System.nanoTime() - last < 10E9) {
          try {
            Thread.sleep(50);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            break;
          }
        }
        
        while (!Thread.currentThread().isInterrupted()) {
          
          long current = System.nanoTime();

          if (current - last >= sampleInterval) {
            logger.taskRun(new Logger.Task("ActiveThreadCount", current, Long.valueOf(pool.getActiveCount())));
            last = current;
          } else {
            try {
              Thread.sleep(0, (int) (sampleInterval - (current - last)));
            } catch (InterruptedException e) {
              break;
            }
          }
        }
        
        logger.stopLogging();
      }
    });
    
    this.daemonThread.setDaemon(true);
    
    Vector2f dummy = new Vector2f(1,1);
    for (int i = 0; i < this.map.getHeight() * this.map.getWidth(); ++i) {
      this.towerBallast.add(Tower.createTower(dummy));
    }
    
    for (int i = 0; i < 256; ++i) {
      this.creepBallast.add(this.creepFactory.spawnCreep(dummy, null, dummy));
    }

    this.logger.setLogMode(this.logMode);
    
    this.logger.startLogging("DATAPARALLEL", numThreads);
    
    this.logger2 = new Logger();
    this.logger2.setLogMode(this.logMode);
    
    //this.daemonThread.start();
    this.updateThread.start();
  }

  @Override
  public void init(GameContainer container, StateBasedGame game)
      throws SlickException {
  }

  @Override
  public void leave(GameContainer container, StateBasedGame game)
      throws SlickException {
    this.updateThread.interrupt();
    try {
      this.updateThread.join();
    } catch (InterruptedException e) {
      //Goggles
    }
    
    this.pool.shutdown();
    
    try {
      this.pool.awaitTermination(1, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      //Goggles
    }
    
    if (this.pool.isTerminating()) {
      this.pool.shutdownNow();
    }
    
    //this.daemonThread.interrupt();
    
    this.logger.merge(this.logger2);
    
    this.logger.stopLogging();
    
    this.map = null;
    
    this.towerBallast.clear();
    this.creepBallast.clear();

    this.towers.clear();
    this.creeps.clear();
    this.deadCreeps.clear();
    this.newCreeps.clear();
  }

  @Override
  public void render(GameContainer container, StateBasedGame game, Graphics g)
      throws SlickException {
    //long start = System.nanoTime();

    Rectangle tile = new Rectangle(0, 0, container.getWidth() / this.map.getWidth(), container.getHeight() / this.map.getHeight());

    for (int i = 0; i < this.towerBallast.size() - this.towers.size(); ++i) {
      this.towerBallast.get(i).sprite.draw(g, tile);
    }
    
    for (int i = 0; i < this.creepBallast.size() - this.creeps.size(); ++i) {
      this.creepBallast.get(i).draw(g, tile);
    }
    
    if (this.map != null) {
      this.map.render(container, g);
      
      for (Creep c : this.creeps) {
        c.draw(g, tile);
      }
    }
    
    //this.logger.taskRun(new Logger.Task("Render", start, System.nanoTime() - start));
  }

  @Override
  public void update(GameContainer container, StateBasedGame game, int delta)
      throws SlickException {
    //long start = System.nanoTime();
    
    Input input = container.getInput();

    Vector2f relativeInput = new Vector2f((float) input.getMouseX() / (float) container.getWidth(),
                                          (float) input.getMouseY() / (float) container.getHeight());
    
    if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
      Vector2f towerPos = new Vector2f((float) Math.floor(relativeInput.x * this.map.getWidth()),
                                       (float) Math.floor(relativeInput.y * this.map.getHeight()));
      
      Tower t = this.map.spawnTower(towerPos);
      
      if (t != null) {
        this.towers.add(t);
      }
    }
    
    if (input.isKeyPressed(Input.KEY_ESCAPE)) {
      game.enterState(BofSA.States.MAINMENU.ordinal());
    }
    
    if (this.map != null) {
      this.map.update(delta / 1000.0f);
    }
    
    //this.logger.taskRun(new Logger.Task("Input", start, System.nanoTime() - start));
  }

  public void update(final float delta) {
    long start = System.nanoTime();
    // Game logic
    
    while (!this.newCreeps.isEmpty()) {
      Creep c = this.newCreeps.poll();
      this.creeps.add(c);
    }
    
    this.map.update(this, delta);
    
    for (final Tower t : this.towers) {
      this.tasks.add(
        this.pool.submit(
          new Runnable() {
            public void run() {
              t.update(delta, creeps);
            }
          }
        )
      );
    }
    
    this.waitForPendingTasks();
    
    final CreepManager man = this;
    for (final Creep c : this.creeps) {
      this.tasks.add(
        this.pool.submit(
          new Runnable() {
            public void run() {
              c.update(man, delta);
            }
          }
        )
      );
    }

    this.waitForPendingTasks();
    
    while (!this.deadCreeps.isEmpty()) {
      Creep c = this.deadCreeps.poll();
      this.creeps.remove(c);
    }
    
    this.logger2.taskRun(new Logger.Task("Update", start, System.nanoTime() - start));
  }

  private void waitForPendingTasks() {
    while (!this.tasks.isEmpty() && !Thread.currentThread().isInterrupted()) {
      for (int i = this.tasks.size() - 1; i >= 0; --i) {
        Future<?> t = this.tasks.get(i);
        if (t.isDone()) {
          this.tasks.remove(i);
        }
      }
    }
  }

  @Override
  public void run() {
    long last = System.nanoTime();
    
    while (!Thread.currentThread().isInterrupted()) {
      long current = System.nanoTime();
      this.update((current - last) / 1E9f);
      last = current;
    }
  }

  @Override
  public void onDeath(Creep c) {
    this.deadCreeps.offer(c);
  }

  @Override
  public void checkpointReached(Creep c) {
    c.getNextCheckpoint();
  }

  @Override
  public void goalReached(Creep c) {
    this.deadCreeps.offer(c);
  }

  @Override
  public void onSpawn(Creep c) {
    this.newCreeps.offer(c);
  }

  @Override
  public void spawnCreep(Vector2f position,
      Queue<CheckPoint> checkpoints, Vector2f goal) {
    if (this.creepFactory == null) {
      this.creepFactory = new CreepFactory();
    }
    
    this.onSpawn(this.creepFactory.spawnCreep(position, checkpoints, goal));
  }

  @Override
  public void mouseClicked(int button, int x, int y, int clickCount) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void mouseDragged(int oldx, int oldy, int newx, int newy) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void mouseMoved(int oldx, int oldy, int newx, int newy) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void mousePressed(int button, int x, int y) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void mouseReleased(int button, int x, int y) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void mouseWheelMoved(int change) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void inputEnded() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void inputStarted() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public boolean isAcceptingInput() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void setInput(Input input) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void keyPressed(int key, char c) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void keyReleased(int key, char c) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void controllerButtonPressed(int controller, int button) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void controllerButtonReleased(int controller, int button) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void controllerDownPressed(int controller) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void controllerDownReleased(int controller) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void controllerLeftPressed(int controller) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void controllerLeftReleased(int controller) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void controllerRightPressed(int controller) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void controllerRightReleased(int controller) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void controllerUpPressed(int controller) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void controllerUpReleased(int controller) {
    // TODO Auto-generated method stub
    
  }
}
