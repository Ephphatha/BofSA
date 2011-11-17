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

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

/**
 * @author ephphatha
 *
 */
public class InGameStateDP extends InGameStateST {
 
  private int maxThreads;

  private Scheduler scheduler;

  @SuppressWarnings("unused")
  private InGameStateDP() {
    this(0, Integer.MAX_VALUE);
  }
  
  public InGameStateDP(int id, int maxThreads) {
    super(id);
    
    this.maxThreads = maxThreads;

    this.scheduler = new Scheduler();
  }

  @Override
  public int getID() {
    return super.getID();
  }

  @Override
  public void enter(GameContainer container, StateBasedGame game)
      throws SlickException {
    super.enter(container, game);
    
    this.scheduler.start(Scheduler.Mode.UNORDERED, this.maxThreads - 2);

    this.updateThread.start();
  }
  
  @Override
  public void leave(GameContainer container, StateBasedGame game)
      throws SlickException {
    this.scheduler.stop();
    
    super.leave(container, game);
  }

  @Override
  public void update(final float delta) {
    // Game logic

    this.creeps.addAll(this.newCreeps);
    this.newCreeps.clear();
    
    this.map.update(this, delta);
    
    for (Tower t : this.towers) {
      this.scheduler.call(t);
    }
    
    this.waitForPendingTasks();
    
    for (Creep c : this.creeps) {
      this.scheduler.call(c);
    }

    this.waitForPendingTasks();

    this.creeps.removeAll(this.deadCreeps);
    this.deadCreeps.clear();
  }

  private void waitForPendingTasks() {
    while (this.scheduler.isBusy() && !Thread.currentThread().isInterrupted()) {
      Thread.yield();
    }
  }
}
