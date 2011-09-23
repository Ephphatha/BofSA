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

import java.util.HashSet;
import java.util.Set;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Sound;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.gui.ComponentListener;
import org.newdawn.slick.gui.GUIContext;

/**
 * @author ephphatha
 *
 */
public class Button {
  protected Image currentImage,
                  normalImage,
                  mouseOverImage,
                  mouseDownImage;
  
  protected Color colour;
  
  protected Shape area;
  
  protected Sound mouseOverSound,
                  mouseDownSound;
  
  protected State state;
  
  protected enum State {
    NORMAL,
    MOUSE_OVER,
    MOUSE_DOWN
  }

  protected boolean mouseDown,
                    mouseUp,
                    over;
  
  protected Set<ComponentListener> listeners;
  
  Button(Image i, final Shape pos, final Color colour) {
    this.listeners = new HashSet<ComponentListener>();
    
    this.currentImage = i;
    this.normalImage = i;
    this.mouseOverImage = i;
    this.mouseDownImage = i;
    
    this.colour = colour;
    
    this.area = pos;
    
    this.over = false;
    
    this.mouseOverSound = null;
    this.mouseDownSound = null;
  }
  
  public void addListener(ComponentListener listener) {
    this.listeners.add(listener);
  }
  
  public void render(GUIContext container, Graphics g) {
    this.currentImage.draw(
        (float) container.getWidth() * this.area.getX(),
        (float) container.getHeight() * this.area.getY(),
        (float) container.getWidth() * this.area.getWidth(),
        (float) container.getHeight() * this.area.getHeight(),
        this.colour);
  }

  public boolean hit(Vector2f p) {
    return Rectangle.contains(p.x, p.y, this.area.getX(), this.area.getY(), this.area.getWidth(), this.area.getHeight());
  }
  
  public boolean mouseMove(Vector2f pos) {
    if (hit(pos)) {
      if (this.over != true) {
        this.over = true;
        this.currentImage = this.mouseOverImage;
        
        if (this.mouseOverSound != null) {
          this.mouseOverSound.play();
        }
      }
      
      return true;
    } else {
      if (this.over == true) {
        this.over = false;
        
        if (this.mouseOverSound != null) {
          if (this.mouseOverSound.playing()) {
            this.mouseOverSound.stop();
          }
        }
      }

      return false;
    }
  }
  
  public boolean mousePressed(Vector2f pos) {
    if (hit(pos)) {
      this.currentImage = this.mouseDownImage;
      
      if (this.mouseOverSound != null) {
        if (this.mouseOverSound.playing()) {
          this.mouseOverSound.stop();
        }
      }
      
      if (this.mouseDownSound != null) {
        this.mouseDownSound.play();
      }
      
      return true;
    } else {
      return false;
    }
  }
  
  public void setMouseOverSound(Sound s) {
    if (this.mouseOverSound != null) {
      if (this.mouseOverSound.playing()) {
        this.mouseOverSound.stop();
      }
    }
    
    this.mouseOverSound = s;
    
    if (this.over) {
      this.mouseOverSound.play();
    }
  }
  
  public void setMouseDownSound(Sound s) {
    if (this.mouseDownSound != null) {
      if (this.mouseDownSound.playing()) {
        this.mouseDownSound.stop();
      }
    }
    
    this.mouseDownSound = s;
  }
}
