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
import au.edu.csu.bofsa.CopyableFloat;
import au.edu.csu.bofsa.CopyableVector2f;
import au.edu.csu.bofsa.Events.Event;
import au.edu.csu.bofsa.Events.GenericEvent;
import au.edu.csu.bofsa.Events.Stream;
import au.edu.csu.bofsa.Signals.InputSignal;
import au.edu.csu.bofsa.Signals.Signal;

/**
 * @author ephphatha
 *
 */
public class VelocityBehaviour extends Behaviour<CopyableVector2f>{

  protected InputSignal<CopyableVector2f> pos;
  protected InputSignal<CheckPoint> goal;
  protected InputSignal<CopyableFloat> speed;
  
  /**
   * @param signal
   */
  public VelocityBehaviour(Signal<CopyableVector2f> velocity, InputSignal<CopyableVector2f> position, InputSignal<CheckPoint> goal, InputSignal<CopyableFloat> maxSpeed, Stream creepStream) {
    super(velocity);

    this.addInput(position);
    this.addInput(goal);
    this.addInput(maxSpeed);
    
    this.pos = position;
    this.goal = goal;
    this.speed = maxSpeed;
    
    creepStream.addSink(this);
  }

  /**
   * @see au.edu.csu.bofsa.Behaviours.Behaviour#doRun()
   */
  @Override
  protected boolean doRun() {
    while (!this.events.isEmpty()) {
      Event e = this.events.poll();
      
      if (e instanceof GenericEvent) {
        if ((GenericEvent.Message) e.value == GenericEvent.Message.DEATH) {
          return false;
        }
      }
    }
    
    CopyableVector2f vel = new CopyableVector2f(this.goal.read().position);
    
    vel.sub(this.pos.read());
    
    float maxSpeed = this.speed.read().getValue();
    if (vel.lengthSquared() > (maxSpeed * maxSpeed)) {
      vel.normalise();
      vel.scale(maxSpeed);
    }
    
    this.signal.write(vel);
    
    return true;
  }
}
