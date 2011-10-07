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

import au.edu.csu.bofsa.CheckPoint;
import au.edu.csu.bofsa.CopyableBoolean;
import au.edu.csu.bofsa.CopyableFloat;
import au.edu.csu.bofsa.CopyableVector2f;
import au.edu.csu.bofsa.Events.CollisionEvent;
import au.edu.csu.bofsa.Events.Event;
import au.edu.csu.bofsa.Events.EventSink;
import au.edu.csu.bofsa.Events.EventSource;
import au.edu.csu.bofsa.Events.GenericEvent;
import au.edu.csu.bofsa.Events.Stream;
import au.edu.csu.bofsa.Signals.InputSignal;
import au.edu.csu.bofsa.Signals.Signal;

/**
 * @author ephphatha
 *
 */
public class CollisionBehaviour extends Behaviour<CopyableBoolean> implements EventSource {

  protected InputSignal<CopyableVector2f> object;
  protected InputSignal<CopyableFloat> radius;
  protected InputSignal<CheckPoint> collider;
  
  protected EventSink creepStream;
  
  public CollisionBehaviour(
      Signal<CopyableBoolean> signal,
      InputSignal<CopyableVector2f> object,
      InputSignal<CopyableFloat> radius,
      InputSignal<CheckPoint> collider,
      Stream creepStream) {
    super(CollisionBehaviour.class.getSimpleName(), signal);

    this.addInput(object);
    
    this.object = object;
    this.radius = radius;
    this.collider = collider;
    
    this.addSink(creepStream);
    
    creepStream.addSink(this);
  }

  @Override
  protected boolean doRun() {
    while (!this.events.isEmpty()) {
      Event e = this.events.poll();
      
      if (e instanceof GenericEvent) {
        if ((GenericEvent.Message)e.value == GenericEvent.Message.DEATH) {
          return false;
        }
      }
    }
    
    CopyableVector2f objPos = this.object.read();
    CheckPoint colPos = this.collider.read();
    
    if (objPos.distanceSquared(colPos.position) <= Math.pow(this.radius.read().getValue(), 2)) {
      if (this.signal.read().getValue() == false) {
        this.notifySinks(
            new CollisionEvent(
                this,
                this.collider.read(),
                Event.Type.TARGETTED,
                System.nanoTime()));
        
        this.signal.write(new CopyableBoolean(true));
      }
    } else {
      this.signal.write(new CopyableBoolean(false));
    }
    
    return true;
  }

  @Override
  public void addSink(EventSink sink) {
    this.creepStream = sink;
  }

  @Override
  public void removeSink(EventSink sink) {
    this.creepStream = null;
  }

  @Override
  public void notifySinks(Event event) {
    this.creepStream.handleEvent(event);
  }
}
