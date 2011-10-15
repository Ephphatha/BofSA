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
import java.util.concurrent.CopyOnWriteArrayList;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.GameState;
import org.newdawn.slick.state.StateBasedGame;

import au.edu.csu.bofsa.Behaviours.ActorRenderBehaviour;
import au.edu.csu.bofsa.Behaviours.CreepFactoryBehaviour;
import au.edu.csu.bofsa.Behaviours.InputPollingBehaviour;
import au.edu.csu.bofsa.Behaviours.RenderBehaviour;
import au.edu.csu.bofsa.Behaviours.TowerFactoryBehaviour;
import au.edu.csu.bofsa.Events.Event;
import au.edu.csu.bofsa.Events.EventSink;
import au.edu.csu.bofsa.Events.GenericEvent;
import au.edu.csu.bofsa.Events.Stream;
import au.edu.csu.bofsa.Signals.Signal;

/**
 * @author ephphatha
 *
 */
public class InGameStateTB implements GameState, EventSink, Comparable<Object> {
  private int stateID;
  
  protected GameLevelTB map;
  
  private CreepFactoryBehaviour creepFactory;
  private TowerFactoryBehaviour towerFactory;
  
  private InputPollingBehaviour input;

  private Scheduler scheduler;
  
  private Stream broadcastStream;

  private List<Drawable> drawables;

  private Signal<CopyableDimension> tileSize;

  private Logger logger;

  private List<Drawable> towerBallast;
  private List<Drawable> creepBallast;

  private int towerCount;

  private Logger.Mode logMode;

  private int maxThreads;

  private int numTowers;

  @SuppressWarnings("unused")
  private InGameStateTB() {
    this(0, Integer.MAX_VALUE, Logger.Mode.BASIC, 0);
  }
  
  public InGameStateTB(int id, int maxThreads, Logger.Mode logMode, int numTowers) {
    this.stateID = id;

    this.numTowers = numTowers;
    this.maxThreads = maxThreads;
    
    this.logMode = logMode;
    
    this.drawables = new CopyOnWriteArrayList<Drawable>();

    this.towerBallast = new LinkedList<Drawable>();
    this.creepBallast = new LinkedList<Drawable>();
    
    this.broadcastStream = new Stream();
    
    this.scheduler = new Scheduler();
    
    this.logger = new Logger();
    this.scheduler.setLogger(this.logger);
    
    this.broadcastStream.addSink(this.scheduler);
    
    this.tileSize = new Signal<CopyableDimension>(new CopyableDimension(1, 1));
    
    Signal<CopyableList<Pipe<CopyableVector2f>>> creeps = new Signal<CopyableList<Pipe<CopyableVector2f>>>(new CopyableList<Pipe<CopyableVector2f>>()); 
    
    this.creepFactory = new CreepFactoryBehaviour(creeps, this.tileSize, this.broadcastStream, this.broadcastStream);
    this.towerFactory = new TowerFactoryBehaviour(new Signal<CopyableList<CopyablePoint>>(new CopyableList<CopyablePoint>()), this.tileSize, creeps, this.broadcastStream, this.broadcastStream);
    
    this.broadcastStream.addSink(this.creepFactory);
    this.broadcastStream.addSink(this.towerFactory);
    
    this.input = new InputPollingBehaviour(new Signal<CopyableBoolean>(new CopyableBoolean()), tileSize, broadcastStream);
  }

  @Override
  public int getID() {
    return this.stateID;
  }

  @Override
  public void enter(GameContainer container, StateBasedGame game)
      throws SlickException {
    CreepFactoryBehaviour.loadResources();
    this.towerFactory.loadResources();
    
    this.logger.setLogMode(logMode);
    
    Stream dummyStream = new Stream();
    Signal<CopyableVector2f> dummy = new Signal<CopyableVector2f>(new CopyableVector2f(1,1));
    Signal<CopyableFloat> health = new Signal<CopyableFloat>(new CopyableFloat(1));
    Sprite.SequencePoint[][] a = new Sprite.SequencePoint[4][];

    for (int i = 0; i < 4; ++i) {
      a[i] = new Sprite.SequencePoint[1];
      for (int j = 0; j < 1; ++j) {
        a[i][j] = new Sprite.SequencePoint((i * 4) + j, 0.25f);
      }
    }
    
    for (int i = 0; i < 1024; ++i) {
      this.creepBallast.add(
          new ActorRenderBehaviour(
              new Signal<CopyableBoolean>(new CopyableBoolean(true)),
              dummy,
              dummy,
              health,
              health,
              this.tileSize,
              CreepFactoryBehaviour.getSprite(),
              a,
              dummyStream,
              dummyStream));
    }
    
    this.scheduler.start(Scheduler.Mode.UNORDERED, this.maxThreads, this.logMode);

    this.logger.startLogging("TASKBASED", this.scheduler.numThreads());

    try {
      this.map = new GameLevelTB("test", this.scheduler, this.creepFactory, this.towerFactory);
    } catch (SlickException e) {
      e.printStackTrace();
    }

    for (int i = 0; i < this.map.getHeight() * this.map.getWidth(); ++i) {
      this.towerBallast.add(
          new RenderBehaviour(
              new Signal<CopyableBoolean>(new CopyableBoolean(true)),
              dummy,
              this.tileSize,
              this.towerFactory.getSprite(),
              dummyStream));
    }
    
    CopyablePoint dummy2 = new CopyablePoint(0,0);
    for (int i = 0; i < this.numTowers; ++i) {
      this.towerFactory.createTower(dummy2);
    }

    this.tileSize.write(
        new CopyableDimension(
            container.getWidth() / this.map.getWidth(),
            container.getHeight() / this.map.getHeight()));

    this.scheduler.call(this.creepFactory);
    this.scheduler.call(this.towerFactory);
    this.scheduler.call(this.input);
    
    container.getInput().addListener(this.input);
    this.input.setInput(container.getInput());
  }

  @Override
  public void init(GameContainer container, StateBasedGame game)
      throws SlickException {
    this.towerCount = 0;
    
    this.broadcastStream.addSink(this);
    
    this.tileSize.write(new CopyableDimension(container.getWidth(), container.getHeight()));
  }

  @Override
  public void leave(GameContainer container, StateBasedGame game)
      throws SlickException {
    this.map = null;
    
    this.towerFactory.handleEvent(new GenericEvent(this, GenericEvent.Message.FORGET_ALL, Event.Type.TARGETTED, System.nanoTime()));
    this.creepFactory.handleEvent(new GenericEvent(this, GenericEvent.Message.FORGET_ALL, Event.Type.TARGETTED, System.nanoTime()));

    this.scheduler.stop();
    
    //this.daemonThread.interrupt();
    
    this.drawables.clear();
    
    this.creepBallast.clear();
    this.towerBallast.clear();
    
    this.logger.stopLogging();
    
    this.towerCount = 0;
  }

  @Override
  public void render(GameContainer container, StateBasedGame game, Graphics g)
      throws SlickException {
    //long start = System.nanoTime();
    
    for (int i = 0; i < this.towerBallast.size() - this.towerCount; ++i) {
      this.towerBallast.get(i).draw(g);
    }
    
    for (int i = 0; i < this.creepBallast.size() - (this.drawables.size() - this.towerCount); ++i) {
      this.creepBallast.get(i).draw(g);
    }
    
    if (this.map != null) {
      this.map.render(container, g);
      
      for (Drawable d : this.drawables) {
        d.draw(g);
      }
    }

    //this.logger.taskRun(new Logger.Task("Render", start, System.nanoTime() - start));
  }

  @Override
  public void update(GameContainer container, StateBasedGame game, int delta)
      throws SlickException {
    Input input = container.getInput();

    if (input.isKeyPressed(Input.KEY_ESCAPE)) {
      game.enterState(BofSA.States.MAINMENU.ordinal());
    }
  }

  @Override
  public void handleEvent(Event event) {
    if (event instanceof GenericEvent) {
      if ((GenericEvent.Message)event.value == GenericEvent.Message.ADD_DRAWABLE) {
        this.drawables.add((Drawable) event.getSource());
        
        if (event.getSource() instanceof RenderBehaviour && !(event.getSource() instanceof ActorRenderBehaviour)) {
          this.towerCount++;
        }
      } else if ((GenericEvent.Message)event.value == GenericEvent.Message.REMOVE_DRAWABLE) {
        this.drawables.remove(event.getSource());
      }
    }
  }

  @Override
  public int compareTo(Object o) {
    return this.hashCode() - o.hashCode();
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