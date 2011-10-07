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

import java.util.List;

import au.edu.csu.bofsa.CopyableBoolean;
import au.edu.csu.bofsa.CopyableFloat;
import au.edu.csu.bofsa.CopyableList;
import au.edu.csu.bofsa.CopyableVector2f;
import au.edu.csu.bofsa.Pipe;
import au.edu.csu.bofsa.Events.DamageEvent;
import au.edu.csu.bofsa.Events.Event;
import au.edu.csu.bofsa.Signals.InputSignal;
import au.edu.csu.bofsa.Signals.Signal;

/**
 * @author ephphatha
 *
 */
public class AttackBehaviour extends Behaviour<CopyableBoolean> {

  protected InputSignal<CopyableList<Pipe<CopyableVector2f>>> targets;
  protected InputSignal<CopyableVector2f> position;
  protected InputSignal<CopyableFloat> fireRate;
  protected InputSignal<CopyableFloat> damage;
  protected InputSignal<CopyableFloat> range;
  
  public AttackBehaviour(
      Signal<CopyableBoolean> signal,
      InputSignal<CopyableList<Pipe<CopyableVector2f>>> targets,
      InputSignal<CopyableVector2f> position,
      InputSignal<CopyableFloat> fireRate,
      InputSignal<CopyableFloat> damage,
      InputSignal<CopyableFloat> range) {
    super(AttackBehaviour.class.getSimpleName(), signal);
    
    this.targets = targets;
    this.position = position;
    this.fireRate = fireRate;
    this.damage = damage;
    this.range = range;
    
    this.signal.write(new CopyableBoolean(false), System.nanoTime() - (long) ((1.0f / this.fireRate.read().getValue()) * 1.0E9f));
  }

  @Override
  protected boolean doRun() {
    List<Pipe<CopyableVector2f>> l = this.targets.read();
    
    long current = System.nanoTime();
    
    long nanosPerShot = (long) ((1.0f / this.fireRate.read().getValue()) * 1.0E9f);
    
    boolean fired = true;
    
    while (fired == true && current - this.signal.getTimeStamp() > nanosPerShot) {
      fired = false;
      for (Pipe<CopyableVector2f> p : l) {
        if (p.signal.read().distanceSquared(this.position.read()) < Math.pow(this.range.read().getValue(), 2)) {
          this.signal.write(new CopyableBoolean(true), this.signal.getTimeStamp() + nanosPerShot);
          p.sink.handleEvent(
              new DamageEvent(
                  this,
                  Float.valueOf(this.damage.read().getValue()),
                  Event.Type.TARGETTED,
                  System.nanoTime()));
          
          fired = true;
          break;
        }
      }
    }
    
    if (!fired) {
      this.signal.write(new CopyableBoolean(true), current - nanosPerShot);
    }
    
    return true;
  }
}
