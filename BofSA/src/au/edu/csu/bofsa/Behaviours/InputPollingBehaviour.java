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
package au.edu.csu.bofsa.Behaviours;

import java.awt.Dimension;

import org.newdawn.slick.Input;
import org.newdawn.slick.InputListener;

import au.edu.csu.bofsa.CopyableBoolean;
import au.edu.csu.bofsa.CopyableDimension;
import au.edu.csu.bofsa.CopyablePoint;
import au.edu.csu.bofsa.Events.Event;
import au.edu.csu.bofsa.Events.EventSink;
import au.edu.csu.bofsa.Events.TowerSpawnEvent;
import au.edu.csu.bofsa.Signals.InputSignal;
import au.edu.csu.bofsa.Signals.Signal;

/**
 * @author ephphatha
 *
 */
public class InputPollingBehaviour extends Behaviour<CopyableBoolean> implements InputListener {
  
  protected EventSink broadcastStream;
  
  protected Input input;
  
  protected InputSignal<CopyableDimension> tileSize;

  private boolean acceptingInput;

  public InputPollingBehaviour(
      Signal<CopyableBoolean> dummy,
      InputSignal<CopyableDimension> tileSize,
      EventSink broadcastStream) {
    super(InputPollingBehaviour.class.getSimpleName(), dummy);
    
    super.addInput(tileSize);
    
    this.tileSize = tileSize;
    
    this.broadcastStream = broadcastStream;
  }

  @Override
  protected boolean doRun() {
    if (this.input != null) {
      if (this.input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
        Dimension d = this.tileSize.read();
        this.broadcastStream.handleEvent(
            new TowerSpawnEvent(
                this,
                new CopyablePoint(this.input.getMouseX() / d.width,
                    this.input.getMouseY() / d.height),
                Event.Type.BROADCAST,
                System.nanoTime()));
      }
    }
    return true;
  }

  @Override
  public void mouseClicked(int button, int x, int y, int clickCount) {
  }

  @Override
  public void mouseDragged(int oldx, int oldy, int newx, int newy) {
  }

  @Override
  public void mouseMoved(int oldx, int oldy, int newx, int newy) {
  }

  @Override
  public void mousePressed(int button, int x, int y) {
  }

  @Override
  public void mouseReleased(int button, int x, int y) {
  }

  @Override
  public void mouseWheelMoved(int change) {
  }

  @Override
  public void inputEnded() {
    this.acceptingInput = false;
  }

  @Override
  public void inputStarted() {
    this.acceptingInput = true;
  }

  @Override
  public boolean isAcceptingInput() {
    return this.acceptingInput;
  }

  @Override
  public void setInput(Input input) {
    this.input = input;
  }

  @Override
  public void keyPressed(int key, char c) {
  }

  @Override
  public void keyReleased(int key, char c) {
  }

  @Override
  public void controllerButtonPressed(int controller, int button) {
  }

  @Override
  public void controllerButtonReleased(int controller, int button) {
  }

  @Override
  public void controllerDownPressed(int controller) {
  }

  @Override
  public void controllerDownReleased(int controller) {
  }

  @Override
  public void controllerLeftPressed(int controller) {
  }

  @Override
  public void controllerLeftReleased(int controller) {
  }

  @Override
  public void controllerRightPressed(int controller) {
  }

  @Override
  public void controllerRightReleased(int controller) {
  }

  @Override
  public void controllerUpPressed(int controller) {
  }

  @Override
  public void controllerUpReleased(int controller) {
  }

}
