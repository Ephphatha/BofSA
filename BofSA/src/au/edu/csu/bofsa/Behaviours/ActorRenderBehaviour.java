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

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

import au.edu.csu.bofsa.CopyableBoolean;
import au.edu.csu.bofsa.CopyableDimension;
import au.edu.csu.bofsa.CopyableVector2f;
import au.edu.csu.bofsa.Sprite;
import au.edu.csu.bofsa.Events.EventSink;
import au.edu.csu.bofsa.Events.Stream;
import au.edu.csu.bofsa.Signals.InputSignal;
import au.edu.csu.bofsa.Signals.Signal;

/**
 * @author ephphatha
 *
 */
public class ActorRenderBehaviour extends RenderBehaviour{
  
  protected InputSignal<CopyableVector2f> velocity;
  
  protected Sprite.SequencePoint[][] sequences;
  
  protected Direction currentDir;
  
  public static enum Direction {
    SOUTH,
    NORTH,
    WEST,
    EAST
  }
  
  public ActorRenderBehaviour(Signal<CopyableBoolean> signal, InputSignal<CopyableVector2f> position, InputSignal<CopyableVector2f> velocity, InputSignal<CopyableDimension> tileSize,
                              Sprite sprite, Sprite.SequencePoint[][] sequences, Stream creepStream, EventSink drawWatcher) {
    super(signal, position, tileSize, sprite, drawWatcher);
    
    this.addInput(velocity);
    
    this.velocity = velocity;
    
    if (sequences.length < 4) {
      throw new IllegalArgumentException("Must be at least four animation sequences.");
    } else {
      this.sequences = sequences;
    }
    
    this.setAnimationSequence(Direction.NORTH);
    
    creepStream.addSink(this);
  }

  @Override
  protected boolean doRun() {
    if (super.doRun() == false) {
      return false;
    }
    
    Vector2f vel = this.velocity.read();
    
    if (Math.abs(vel.x) > Math.abs(vel.y)) {
      if (vel.x > 0) {
        this.setAnimationSequence(Direction.EAST);
      } else {
        this.setAnimationSequence(Direction.WEST);
      }
    } else {
      if (vel.y > 0) {
        this.setAnimationSequence(Direction.SOUTH);
      } else {
        this.setAnimationSequence(Direction.NORTH);
      }
    }
    
    return true;
  }

  /**
   * @see au.edu.csu.bofsa.Drawable#draw(org.newdawn.slick.Graphics)
   */
  @Override
  public void draw(Graphics g) {
    CopyableVector2f pos = this.position.read();
    Dimension tile = this.tileSize.read();
    Rectangle r = new Rectangle(pos.x * tile.width - tile.width / 4.0f, pos.y * tile.height - tile.height / 4.0f, tile.width / 2.0f, tile.height / 2.0f);
    this.sprite.draw(g, r);
  }

  private void setAnimationSequence(Direction dir) {
    if (this.currentDir != dir) {
      this.currentDir = dir;
      this.sprite.setFrameSequence(this.sequences[this.currentDir.ordinal()]);
    }
  }
}
