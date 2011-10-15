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

import java.util.concurrent.Callable;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.StateBasedGame;

/**
 * @author ephphatha
 *
 */
public class InGameStateDP extends InGameStateST {
 
  //private Thread daemonThread;
  
  private Logger logger2;

  private int maxThreads;

  private Logger dummyLogger;
  private Scheduler scheduler;

  @SuppressWarnings("unused")
  private InGameStateDP() {
    this(0, Integer.MAX_VALUE, Logger.Mode.BASIC, 0);
  }
  
  public InGameStateDP(int id, int maxThreads, Logger.Mode logMode, int numTowers) {
    super(id, logMode, numTowers);
    
    this.maxThreads = maxThreads;

    this.scheduler = new Scheduler();
    
    this.scheduler.setLogger(this.logger);
  }

  @Override
  public int getID() {
    return super.getID();
  }

  @Override
  public void enter(GameContainer container, StateBasedGame game)
      throws SlickException {
    super.enter(container, game);
    
    int numThreads = Math.max(this.maxThreads - 2, 1);
    
    this.logger.startLogging("DATAPARALLEL", numThreads);
    
    this.logger2 = new Logger();
    this.logger2.setLogMode(this.logMode);

    this.dummyLogger = new Logger();
    this.dummyLogger.setLogMode(this.logMode);

    this.scheduler.start(Scheduler.Mode.UNORDERED, numThreads, this.logMode);

    this.updateThread.start();
  }

  @Override
  public void init(GameContainer container, StateBasedGame game)
      throws SlickException {
  }

  @Override
  public void leave(GameContainer container, StateBasedGame game)
      throws SlickException {
    this.logger.merge(this.logger2);
    
    this.logger2 = null;
    this.dummyLogger = null;
    
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
    super.render(container, game, g);
  }

  @Override
  public void update(GameContainer container, StateBasedGame game, int delta)
      throws SlickException {
    //long start = System.nanoTime();
    
    Input input = container.getInput();

    Vector2f relativeInput = new Vector2f((float) input.getMouseX() / (float) container.getWidth(),
                                          (float) input.getMouseY() / (float) container.getHeight());
    
    if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
      CopyablePoint towerPos = new CopyablePoint((int) Math.floor(relativeInput.x * this.map.getWidth()),
                                                 (int) Math.floor(relativeInput.y * this.map.getHeight()));
      
      Tower t = this.map.spawnTower(towerPos, this.creepPositions, this.tileSize, this);
      
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
    this.dummyLogger.taskRun("Spawn");
    
    for (final Tower t : this.towers) {
      this.scheduler.call(
          new Callable<Boolean>() {
            public Boolean call() {
              t.update(delta);
              dummyLogger.taskRun("Attack");
              dummyLogger.taskRun("Render");
              return true;
            }
          }
      );
    }
    
    this.waitForPendingTasks();
    
    final CreepManager man = this;
    for (final Creep c : this.creeps) {
      this.scheduler.call(
          new Callable<Boolean>() {
            public Boolean call() {
              c.update(man, delta);
              dummyLogger.taskRun("Move");
              dummyLogger.taskRun("Velocity");
              dummyLogger.taskRun("Collision");
              dummyLogger.taskRun("Waypoint");
              dummyLogger.taskRun("Health");
              dummyLogger.taskRun("Render");
              return true;
            }
          }
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
    while (!this.scheduler.isBusy() && !Thread.currentThread().isInterrupted()) {
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
}
