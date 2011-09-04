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
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.GameState;
import org.newdawn.slick.state.StateBasedGame;

import au.edu.csu.bofsa.Creep.Type;

/**
 * @author ephphatha
 *
 */
public class InGameState implements GameState, CreepManager {
  private int stateID;
  
  protected GameLevel map;
  
  private float value;

  private CreepFactory creepFactory;

  private List<Thread> threads;

  private Scheduler scheduler;

  @SuppressWarnings("unused")
  private InGameState() {
    this(0);
  }
  
  public InGameState(int id) {
    this.stateID = id;

    this.threads = new LinkedList<Thread>();
  }

  @Override
  public int getID() {
    return this.stateID;
  }

  @Override
  public void enter(GameContainer container, StateBasedGame game)
      throws SlickException {
    try {
      this.map = new GameLevel("test");
    } catch (SlickException e) {
      e.printStackTrace();
    }
    
    for (int i = 0; i < Runtime.getRuntime().availableProcessors(); ++i) {
      Thread t = new WorkerThread(this.scheduler);
      this.threads.add(t);
      t.start();
    }
    
    this.value = 0.0f;
  }

  @Override
  public void init(GameContainer container, StateBasedGame game)
      throws SlickException {
  }

  @Override
  public void leave(GameContainer container, StateBasedGame game)
      throws SlickException {
    this.map = null;

    for (Thread t : this.threads) {
      t.interrupt();
    }
    
    this.threads.clear();
  }

  @Override
  public void render(GameContainer container, StateBasedGame game, Graphics g)
      throws SlickException {
    if (this.map != null) {
      this.map.render(container, g);
      
      Rectangle tile = new Rectangle(0, 0, container.getWidth() / this.map.getWidth(), container.getHeight() / this.map.getHeight());

      //for (Creep c : this.creeps) {
        //c.draw(g, tile);
      //}
    }

    g.drawString("Asset value: " + Float.toString(this.value), 5, 25);
  }

  @Override
  public void update(GameContainer container, StateBasedGame game, int delta)
      throws SlickException {
    Input input = container.getInput();

    Vector2f relativeInput = new Vector2f((float) input.getMouseX() / (float) container.getWidth(),
                                          (float) input.getMouseY() / (float) container.getHeight());
    
    if (input.isKeyPressed(Input.KEY_ESCAPE)) {
      game.enterState(BofSA.States.MAINMENU.ordinal());
    }
    
    if (this.map != null) {
      this.map.update(delta / 1000.0f);
    }
    
    // Game logic
    
    this.map.update(this, delta / 1000.0f);
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
  public void onDeath(Creep c) {
    this.value += c.getValue();
    this.creepPositions.remove(c.getPosition());
  }

  @Override
  public void checkpointReached(Creep c) {
  }

  @Override
  public void goalReached(Creep c) {
    System.out.println("A creep has reached its goal!");
    
    this.value -= c.getValue();
  }

  @Override
  public void onSpawn(Creep c) {
    this.creepPositions.add(c.getPosition());
  }

  @Override
  public void spawnCreep(Type type, Vector2f position,
      Queue<CheckPoint> checkpoints, Vector2f goal) {
    if (this.creepFactory == null) {
      this.creepFactory = new CreepFactory();
    }
    
    this.onSpawn(this.creepFactory.spawnCreep(type, position, checkpoints, goal));
  }
}
