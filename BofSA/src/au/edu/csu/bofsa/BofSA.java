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
    TASK_BASED
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    try {
      AppGameContainer app = new AppGameContainer(new BofSA());
      app.setDisplayMode(800, 600, false);
      app.start();
    } catch (SlickException e) {
      e.printStackTrace();
    }
  }
  
  public BofSA() {
    super("Bank of SA");
    
    this.addState(new MainMenuState(States.MAINMENU.ordinal()));
    this.addState(new InGameStateST(States.SINGLE_THREAD.ordinal()));
    
    this.enterState(States.MAINMENU.ordinal());
  }

  @Override
  public void initStatesList(GameContainer gc) throws SlickException {
    for (int i = 0; i < this.getStateCount(); ++i) {
      this.getState(i).init(gc, this);
    }
  }
}
