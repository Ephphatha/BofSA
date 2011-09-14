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

import au.edu.csu.bofsa.CopyableFloat;
import au.edu.csu.bofsa.Events.DamageEvent;
import au.edu.csu.bofsa.Events.Event;
import au.edu.csu.bofsa.Events.GenericEvent;
import au.edu.csu.bofsa.Events.Stream;
import au.edu.csu.bofsa.Signals.Signal;

/**
 * @author ephphatha
 *
 */
public class HealthBehaviour extends Behaviour<CopyableFloat> {
  
  protected Stream creepStream;

  public HealthBehaviour(Signal<CopyableFloat> signal, Stream creepStream) {
    super(signal);
    
    this.creepStream = creepStream;
    
    this.creepStream.addSink(this);
  }

  @Override
  protected boolean doRun() {
    while (!this.events.isEmpty()) {
      Event e = this.events.poll();
      
      if (e != null) {
        if (e instanceof GenericEvent) {
          if (e.value == GenericEvent.Message.DEATH) {
            return false;
          }
        } else if (e instanceof DamageEvent) {
          float hp = this.signal.read().getValue();
          Float damage = (Float) e.value;
          hp -= damage.floatValue();
          this.signal.write(new CopyableFloat(hp));
          
          if (hp <= 0.0f) {
            this.creepStream.handleEvent(
                new GenericEvent(
                    this,
                    GenericEvent.Message.DEATH,
                    Event.Type.BROADCAST,
                    e.time));
          }
        }
      }
    }
    return true;
  }

}
