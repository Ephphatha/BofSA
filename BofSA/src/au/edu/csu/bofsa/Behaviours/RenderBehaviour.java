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
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

import au.edu.csu.bofsa.CopyableBoolean;
import au.edu.csu.bofsa.CopyableDimension;
import au.edu.csu.bofsa.CopyableVector2f;
import au.edu.csu.bofsa.Drawable;
import au.edu.csu.bofsa.Sprite;
import au.edu.csu.bofsa.Events.Event;
import au.edu.csu.bofsa.Events.EventSink;
import au.edu.csu.bofsa.Events.EventSource;
import au.edu.csu.bofsa.Events.GenericEvent;
import au.edu.csu.bofsa.Signals.InputSignal;
import au.edu.csu.bofsa.Signals.Signal;

/**
 * @author ephphatha
 *
 */
public class RenderBehaviour extends Behaviour<CopyableBoolean> implements
    Drawable, EventSource {

  protected InputSignal<CopyableVector2f> position;
  protected InputSignal<CopyableDimension> tileSize;

  protected Sprite sprite;

  protected Set<EventSink> sinks;
  protected long previous;
  
  public RenderBehaviour(
      Signal<CopyableBoolean> signal,
      InputSignal<CopyableVector2f> position,
      InputSignal<CopyableDimension> tileSize,
      Sprite sprite,
      EventSink drawWatcher) {
    this(RenderBehaviour.class.getSimpleName(), signal, position, tileSize, sprite, drawWatcher);
  }
  
  protected RenderBehaviour(
      String name,
      Signal<CopyableBoolean> signal,
      InputSignal<CopyableVector2f> position,
      InputSignal<CopyableDimension> tileSize,
      Sprite sprite,
      EventSink drawWatcher) {
    super(name, signal);
    
    this.position = position;
    this.tileSize = tileSize;
    
    this.sprite = sprite;

    this.sinks = new ConcurrentSkipListSet<EventSink>();
    
    this.addSink(drawWatcher);
    
    this.previous = System.nanoTime();
    
    this.handleEvent(
        new GenericEvent(
            this,
            GenericEvent.Message.ADD_DRAWABLE,
            Event.Type.TARGETTED,
            System.nanoTime()));
  }

  /**
   * @see au.edu.csu.bofsa.Drawable#draw(org.newdawn.slick.Graphics)
   */
  @Override
  public void draw(Graphics g) {
    CopyableVector2f pos = this.position.read();
    Dimension tile = this.tileSize.read();
    Rectangle r = new Rectangle(
        pos.x * tile.width,
        pos.y * tile.height,
        tile.width,
        tile.height);
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

  @Override
  protected boolean doRun() {
    long current = System.nanoTime();
    
    while (!this.events.isEmpty()) {
      Event e = this.events.poll();
      
      if (e instanceof GenericEvent) {
        if ((GenericEvent.Message)e.value == GenericEvent.Message.DEATH) {
          this.notifySinks(
              new GenericEvent(
                  this,
                  GenericEvent.Message.REMOVE_DRAWABLE,
                  Event.Type.BROADCAST,
                  current));
          return false;
        } else if ((GenericEvent.Message)e.value == GenericEvent.Message.ADD_DRAWABLE) {
          this.notifySinks(
              new GenericEvent(
                  this,
                  GenericEvent.Message.ADD_DRAWABLE,
                  Event.Type.BROADCAST,
                  current));
        }
      }
    }

    this.sprite.update((float) (current - this.signal.getTimeStamp()) / 1.0E9f);
    
    this.signal.write(new CopyableBoolean(true), current);
    
    return true;
  }
}
