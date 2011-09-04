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
import java.util.concurrent.CopyOnWriteArraySet;

import au.edu.csu.bofsa.Behaviour;
import au.edu.csu.bofsa.CheckPoint;
import au.edu.csu.bofsa.CopyableBoolean;
import au.edu.csu.bofsa.CopyableFloat;
import au.edu.csu.bofsa.CopyableVector2f;
import au.edu.csu.bofsa.Event;
import au.edu.csu.bofsa.EventSink;
import au.edu.csu.bofsa.EventSource;
import au.edu.csu.bofsa.InputSignal;
import au.edu.csu.bofsa.Signal;

/**
 * @author ephphatha
 *
 */
public class CollisionBehaviour extends Behaviour<CopyableBoolean> implements EventSource<InputSignal<CheckPoint>> {

  protected InputSignal<CopyableVector2f> object;
  protected InputSignal<CopyableFloat> radius;
  protected InputSignal<CheckPoint> collider;
  
  protected Set<EventSink<InputSignal<CheckPoint>>> sinks;
  
  public CollisionBehaviour(Signal<CopyableBoolean> signal, InputSignal<CopyableVector2f> object, InputSignal<CopyableFloat> radius, InputSignal<CheckPoint> collider) {
    super(signal);

    this.addInput(object);
    this.addInput(radius);
    this.addInput(collider);
    
    this.object = object;
    this.radius = radius;
    this.collider = collider;
    
    this.sinks = new CopyOnWriteArraySet<EventSink<InputSignal<CheckPoint>>>();
  }

  @Override
  protected boolean doRun() {
    CopyableVector2f objPos = this.object.read();
    CheckPoint colPos = this.collider.read();
    
    if (objPos.distanceSquared(colPos.position) <= Math.pow(this.radius.read().getValue(), 2)) {
      if (this.signal.read().getValue() == false) {
        this.notifySinks(new Event<InputSignal<CheckPoint>>(this, this.collider, System.nanoTime()));
        this.signal.write(new CopyableBoolean(true));
      }
    } else {
      this.signal.write(new CopyableBoolean(false));
    }
    
    return true;
  }

  @Override
  public void addSink(EventSink<InputSignal<CheckPoint>> sink) {
    this.sinks.add(sink);
  }

  @Override
  public void removeSink(EventSink<InputSignal<CheckPoint>> sink) {
    this.sinks.remove(sink);
  }

  @Override
  public void notifySinks(Event<InputSignal<CheckPoint>> event) {
    for (EventSink<InputSignal<CheckPoint>> s : this.sinks) {
      s.handleEvent(event);
    }
  }
}
