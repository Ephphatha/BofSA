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

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

/**
 * @author ephphatha
 *
 */
public class BofSA extends StateBasedGame {

  protected enum States {
    MAINMENU,
    SINGLE_THREAD,
    DATA_PARALLEL,
    TASK_BASED
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    //Thread.currentThread().setPriority(Thread.NORM_PRIORITY + 2);
    int maxThreads = Runtime.getRuntime().availableProcessors();
    
    Logger.Mode logMode = Logger.Mode.BASIC;
    
    for (String s : args) {
      if (s.startsWith("-t")) {
        try {
          maxThreads = Integer.parseInt(s.substring(2));
        } catch (NumberFormatException e) {
          //Goggles
        }
      } else if (s.startsWith("-l")) {
        if (s.substring(2).equalsIgnoreCase("Detailed")) {
          logMode = Logger.Mode.DETAILED;
        } else if (s.substring(2).equalsIgnoreCase("Sample")) {
          logMode = Logger.Mode.SAMPLE;
        } else {
          logMode = Logger.Mode.BASIC;
        }
      }
    }
    
    try {
      AppGameContainer app = new AppGameContainer(new BofSA(maxThreads, logMode));
      app.setDisplayMode(800, 600, false);
      app.start();
    } catch (SlickException e) {
      e.printStackTrace();
    }
  }
  
  public BofSA() {
    this(Integer.MAX_VALUE, Logger.Mode.BASIC);
  }
  
  public BofSA(int maxThreads) {
    this(maxThreads, Logger.Mode.BASIC);
  }
  
  public BofSA(Logger.Mode logMode) {
    this(Integer.MAX_VALUE, logMode);
  }
  
  public BofSA(int maxThreads, Logger.Mode logMode) {
    super("Bank of SA");
    
    this.addState(new MainMenuState(States.MAINMENU.ordinal()));
    this.addState(new InGameStateST(States.SINGLE_THREAD.ordinal(), logMode));
    this.addState(new InGameStateDP(States.DATA_PARALLEL.ordinal(), maxThreads, logMode));
    this.addState(new InGameStateTB(States.TASK_BASED.ordinal(), maxThreads, logMode));
    
    this.enterState(States.MAINMENU.ordinal());
  }

  @Override
  public void initStatesList(GameContainer gc) throws SlickException {
    for (int i = 0; i < this.getStateCount(); ++i) {
      this.getState(i).init(gc, this);
    }
  }
}
