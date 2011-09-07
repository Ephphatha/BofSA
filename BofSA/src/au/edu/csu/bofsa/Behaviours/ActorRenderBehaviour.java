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

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

import au.edu.csu.bofsa.Behaviour;
import au.edu.csu.bofsa.CopyableBoolean;
import au.edu.csu.bofsa.CopyableVector2f;
import au.edu.csu.bofsa.Drawable;
import au.edu.csu.bofsa.Event;
import au.edu.csu.bofsa.EventSink;
import au.edu.csu.bofsa.EventSource;
import au.edu.csu.bofsa.InputSignal;
import au.edu.csu.bofsa.Signal;
import au.edu.csu.bofsa.Sprite;
import au.edu.csu.bofsa.Stream;

/**
 * @author ephphatha
 *
 */
public class ActorRenderBehaviour extends Behaviour<CopyableBoolean> implements Drawable, EventSource{
  
  protected InputSignal<CopyableVector2f> position;
  protected InputSignal<CopyableVector2f> velocity;
  
  protected Sprite sprite;

  protected Sprite.SequencePoint[][] sequences;
  
  protected Direction currentDir;
  
  protected long previous;
  
  protected Set<EventSink> sinks;
  
  public static enum Direction {
    SOUTH,
    NORTH,
    WEST,
    EAST
  }
  
  public ActorRenderBehaviour(Signal<CopyableBoolean> signal, InputSignal<CopyableVector2f> position, InputSignal<CopyableVector2f> velocity,
                              Sprite sprite, Sprite.SequencePoint[][] sequences, Stream creepStream, EventSink sink) {
    super(signal);
    
    this.addInput(position);
    this.addInput(velocity);
    
    this.position = position;
    this.velocity = velocity;
    
    this.sprite = sprite;
    
    if (sequences.length < 4) {
      throw new IllegalArgumentException("Must be at least four animation sequences.");
    } else {
      this.sequences = sequences;
    }
    
    this.setAnimationSequence(Direction.NORTH);
    
    this.sinks = new ConcurrentSkipListSet<EventSink>();
    
    this.addSink(sink);
    
    this.handleEvent(new Event(this, Event.Generic.ADD_DRAWABLE, System.nanoTime()));
    
    creepStream.addSink(this);
  }

  @Override
  protected boolean doRun() {
    while (!this.events.isEmpty()) {
      Event e = this.events.poll();
      
      if (e.value instanceof Event.Generic) {
        if ((Event.Generic)e.value == Event.Generic.DEATH) {
          this.notifySinks(new Event(this, Event.Generic.REMOVE_DRAWABLE, System.nanoTime()));
          return false;
        } else if ((Event.Generic)e.value == Event.Generic.ADD_DRAWABLE) {
          this.notifySinks(new Event(this, Event.Generic.ADD_DRAWABLE, System.nanoTime()));
        }
      }
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
    
    long current = System.nanoTime();
    
    this.sprite.update((float) (current - this.previous) / (1000.0f * 1000.0f * 1000.0f));
    
    this.previous = current;
    
    return true;
  }
  
  private void setAnimationSequence(Direction dir) {
    if (this.currentDir != dir) {
      this.currentDir = dir;
      this.sprite.setFrameSequence(this.sequences[this.currentDir.ordinal()]);
    }
  }

  public void draw(Graphics g, Rectangle tile) {
    CopyableVector2f pos = this.position.read();
    Rectangle r = new Rectangle(pos.x * tile.getWidth() - tile.getWidth() / 4.0f, pos.y * tile.getHeight() - tile.getHeight() / 4.0f, tile.getWidth() / 2.0f, tile.getHeight() / 2.0f);
    this.sprite.draw(g, r);
  }

  @Override
  public void addSink(EventSink sink) {
    this.sinks.add(sink);
  }

  @Override
  public void notifySinks(Event event) {
    for (EventSink s : this.sinks) {
      s.handleEvent(event);
    }
  }

  @Override
  public void removeSink(EventSink sink) {
    this.sinks.remove(sink);
  }
}
