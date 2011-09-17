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
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
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
public class MainMenuState implements GameState {
  private int stateID;

  Image background;
  Button stButton,
         tbButton,
         exitButton;
  
  @SuppressWarnings("unused")
  private MainMenuState() {
    this(0);
  }

  public MainMenuState(int id) {
    this.stateID = id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.newdawn.slick.state.GameState#getID()
   */
  @Override
  public int getID() {
    return this.stateID;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.newdawn.slick.state.GameState#init(org.newdawn.slick.GameContainer,
   * org.newdawn.slick.state.StateBasedGame)
   */
  @Override
  public void init(GameContainer container, StateBasedGame game)
      throws SlickException {
    this.background = new Image("assets/menubg.jpg");
    
    Image buttons = new Image("assets/menubuttons.png");
    
    this.stButton = new Button(buttons.getSubImage(0, 0, 320, 64), new Rectangle(0.3f, 0.4f, 0.4f, 0.2f));
    this.exitButton = new Button(buttons.getSubImage(0, 64, 320, 64), new Rectangle(0.3f, 0.6f, 0.4f, 0.2f));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.newdawn.slick.state.GameState#render(org.newdawn.slick.GameContainer,
   * org.newdawn.slick.state.StateBasedGame, org.newdawn.slick.Graphics)
   */
  @Override
  public void render(GameContainer container, StateBasedGame game, Graphics g)
      throws SlickException {
    background.draw(0, 0, container.getWidth(), container.getHeight());
    
    this.stButton.render(container, g);
    
    this.exitButton.render(container, g);
  }

  @Override
  public void enter(GameContainer container, StateBasedGame game)
      throws SlickException {
    // Goggles
  }

  @Override
  public void leave(GameContainer container, StateBasedGame game)
      throws SlickException {
    // Goggles
  }

  @Override
  public void update(GameContainer container, StateBasedGame game, int delta)
      throws SlickException {
    Input input = container.getInput();

    Vector2f p = new Vector2f((float) input.getMouseX() / (float) container.getWidth(),
                            (float) input.getMouseY() / (float) container.getHeight());
  
    this.stButton.mouseMove(p);
    this.exitButton.mouseMove(p);
    
    if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
      if (this.stButton.mousePressed(p)) {
        // Transition to game state.
        System.out.println("Start button hit");
        game.enterState(BofSA.States.SINGLE_THREAD.ordinal());
      } else if (this.exitButton.mousePressed(p)) {
        // Exit game.
        System.out.println("Exit button hit");
        container.exit();
      }
    }
  }

  @Override
  public void mouseClicked(int button, int x, int y, int clickCount) {
    // Goggles
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
  public void setInput(Input arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void keyPressed(int arg0, char arg1) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void keyReleased(int arg0, char arg1) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void controllerButtonPressed(int arg0, int arg1) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void controllerButtonReleased(int arg0, int arg1) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void controllerDownPressed(int arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void controllerDownReleased(int arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void controllerLeftPressed(int arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void controllerLeftReleased(int arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void controllerRightPressed(int arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void controllerRightReleased(int arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void controllerUpPressed(int arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void controllerUpReleased(int arg0) {
    // TODO Auto-generated method stub
    
  }

}
