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

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.GameState;
import org.newdawn.slick.state.StateBasedGame;

import au.edu.csu.bofsa.Behaviours.CreepFactoryBehaviour;
import au.edu.csu.bofsa.Behaviours.TowerFactoryBehaviour;
import au.edu.csu.bofsa.Events.Event;
import au.edu.csu.bofsa.Events.EventSink;
import au.edu.csu.bofsa.Signals.Signal;

/**
 * @author ephphatha
 *
 */
public class InGameStateST implements CreepManager, EventSink, GameState, Runnable {
  private int stateID;
  
  protected GameLevelST map;
  
  protected List<Tower> towers;
  
  protected Queue<Creep> newCreeps;
  protected List<Creep> creeps;
  protected Queue<Creep> deadCreeps;
  
  protected List<Tower> towerBallast;
  protected List<Creep> creepBallast;
  
  protected Logger logger;

  protected Logger.Mode logMode;

  protected int numTowers;

  protected Signal<CopyableDimension> tileSize;

  protected Signal<CopyableList<Pipe<CopyableVector2f>>> creepPositions;

  protected Thread updateThread;
  
  @SuppressWarnings("unused")
  private InGameStateST() {
    this(0, Logger.Mode.BASIC, 0);
  }
  
  public InGameStateST(int id, Logger.Mode logMode, int numTowers) {
    this.stateID = id;
    
    this.numTowers = numTowers;

    this.towers = new LinkedList<Tower>();
    this.towerBallast = new LinkedList<Tower>();
    this.creeps = new LinkedList<Creep>();
    this.creepBallast = new LinkedList<Creep>();
    this.deadCreeps = new LinkedList<Creep>();
    this.newCreeps = new LinkedList<Creep>();

    this.creepPositions = new Signal<CopyableList<Pipe<CopyableVector2f>>>(new CopyableList<Pipe<CopyableVector2f>>());
    
    this.tileSize = new Signal<CopyableDimension>(new CopyableDimension(1,1));
    
    this.logger = new Logger();
    
    this.logMode = logMode;
  }

  @Override
  public int getID() {
    return this.stateID;
  }

  @Override
  public void init(GameContainer container, StateBasedGame game)
      throws SlickException {
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
    
    {
    CopyablePoint dummy = new CopyablePoint(0,0);
    Tower t = null;
    for (int i = 0; i < this.map.getHeight() * this.map.getWidth(); ++i) {
      TowerFactoryBehaviour.createTower(dummy, this.creepPositions, t, this.tileSize, null);
      this.towerBallast.add(t);
    }
    }

    Signal<CopyableList<Pipe<CopyableVector2f>>> tempSignal = new Signal<CopyableList<Pipe<CopyableVector2f>>>(new CopyableList<Pipe<CopyableVector2f>>());
    
    {
    CopyableVector2f dummy = new CopyableVector2f(1,1);
    Creep c = null;
    for (int i = 0; i < 1024; ++i) {
      c = new Creep();
      CreepFactoryBehaviour.spawnCreep(dummy, null, c, c, this.tileSize, this, tempSignal);
      this.creepBallast.add(c);
    }
    }

    {
    CopyablePoint dummy = new CopyablePoint(0,0);
    Tower t = null;
    for (int i = 0; i < this.numTowers; ++i) {
      TowerFactoryBehaviour.createTower(dummy, this.creepPositions, t, this.tileSize, null);
      this.towers.add(t);
    }
    }

    this.logger.setLogMode(this.logMode);
    
    if (this.getClass() == InGameStateST.class) {
      this.logger.startLogging("SINGLETHREAD");
      
      this.updateThread.start();
    }
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

    this.tileSize.write(new CopyableDimension(container.getWidth() / this.map.getWidth(), container.getHeight() / this.map.getHeight()));

    for (int i = 0; i < this.towerBallast.size() - this.towers.size(); ++i) {
      this.towerBallast.get(i).draw(g);
    }
    
    for (int i = 0; i < this.creepBallast.size() - this.creeps.size(); ++i) {
      this.creepBallast.get(i).draw(g);
    }
    
    if (this.map != null) {
      this.map.render(container, g);
      
      for (Creep c : this.creeps) {
        c.draw(g);
      }
    }
    
    //this.logger.taskRun(new Logger.Task("Render", start, System.nanoTime() - start));
  }

  @Override
  public void update(GameContainer container, StateBasedGame game, int delta)
      throws SlickException {
    long start = System.nanoTime();
    
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
    
    // Game logic
    
    for (Creep c : this.newCreeps) {
      this.creeps.add(c);
    }
    
    this.newCreeps.clear();
    
    this.map.update(this, delta / 1000.0f);
    
    for (Tower t : this.towers) {
      t.update(delta / 1000.0f);
    }
    
    for (Creep c : this.creeps) {
      c.update(this, delta / 1000.0f);
    }
    
    for (Creep c : this.deadCreeps) {
      this.creeps.remove(c);
    }
    
    this.deadCreeps.clear();
    
    this.logger.taskRun(new Logger.Task("Update", start, System.nanoTime() - start));
  }

  @Override
  public void onDeath(Creep c) {
    this.deadCreeps.add(c);
  }

  @Override
  public void onSpawn(Creep c) {
    this.newCreeps.add(c);
  }

  @Override
  public void spawnCreep(
      CopyableVector2f position,
      Queue<CheckPoint> checkpoints) {
    Creep c = new Creep();
    
    CreepFactoryBehaviour.spawnCreep(
        position,
        checkpoints,
        c,
        c,
        tileSize,
        this,
        this.creepPositions);
    
    this.onSpawn(c);
  }

  @Override
  public void handleEvent(Event event) {
    // TODO Auto-generated method stub
    
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

  @Override
  public void run() {
    // TODO Auto-generated method stub
    
  }
}
