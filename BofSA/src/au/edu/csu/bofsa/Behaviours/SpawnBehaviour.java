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
import au.edu.csu.bofsa.CopyableList;
import au.edu.csu.bofsa.CopyableLong;
import au.edu.csu.bofsa.CopyableVector2f;
import au.edu.csu.bofsa.Events.CreepSpawnEvent;
import au.edu.csu.bofsa.Events.Event;
import au.edu.csu.bofsa.Events.EventSink;
import au.edu.csu.bofsa.Signals.InputSignal;
import au.edu.csu.bofsa.Signals.Signal;


/**
 * @author ephphatha
 *
 */
public class SpawnBehaviour extends Behaviour<CopyableLong> {
  protected InputSignal<CopyableVector2f> position;
  protected InputSignal<CopyableList<CheckPoint>> checkpoints;
  
  protected InputSignal<CopyableFloat> spawnDuration,
                                       spawnInterval,
                                       lullDuration;
  
  protected EventSink creepBuilder;
  
  private State state;
  
  protected enum State {
    SPAWNING,
    IDLE
  }

  public SpawnBehaviour(
      Signal<CopyableLong> lastStateChange,
      InputSignal<CopyableVector2f> position,
      InputSignal<CopyableList<CheckPoint>> checkpoints,
      InputSignal<CopyableFloat> spawnDuration,
      InputSignal<CopyableFloat> spawnInterval,
      InputSignal<CopyableFloat> lullDuration,
      EventSink creepBuilder) {
    super(lastStateChange);
    
    this.position = position;
    this.checkpoints = checkpoints;
    
    this.spawnDuration = spawnDuration;
    this.spawnInterval = spawnInterval;
    this.lullDuration = lullDuration;
    
    this.state = State.SPAWNING;
    
    this.signal.write(new CopyableLong(System.nanoTime()));

    this.creepBuilder = creepBuilder;
  }

  public boolean doRun() {
    boolean repeat = false;
  
    do {
      long current = System.nanoTime();
      
      long stateChangeDelta = current - this.signal.read().getValue();
      
      switch (this.state) {
      case IDLE:
        long lullDuration = (long) (this.lullDuration.read().getValue() * 1.0E9f);
        if (stateChangeDelta >= lullDuration) {
          this.state = State.SPAWNING;
          repeat = true;
          
          this.signal.write(new CopyableLong(this.signal.read().getValue() + lullDuration), this.signal.getTimeStamp() + lullDuration);
        } else {
          repeat = false;
        }
        break;
        
      case SPAWNING:
        long spawnDuration = (long) (this.spawnDuration.read().getValue() * 1.0E9F);
        long spawnInterval = (long) (this.spawnInterval.read().getValue() * 1.0E9F);
        long spawnDelta = current - this.signal.getTimeStamp();
        
        while (spawnDelta >= spawnInterval) {
          this.signal.write(this.signal.read(), this.signal.getTimeStamp() + spawnInterval);
          
          this.creepBuilder.handleEvent(
              new CreepSpawnEvent(
                  this,
                  new CreepSpawnEvent.SpawnEventParameters(
                      this.position.read(),
                      this.checkpoints.read()),
                  Event.Type.BROADCAST,
                  this.signal.getTimeStamp()));
          
          spawnDelta -= spawnInterval;
          if (this.signal.getTimeStamp() - this.signal.read().getValue() >= spawnDuration) {
            break;
          }
        }
        
        if (stateChangeDelta >= spawnDuration) {
          this.state = State.IDLE;
          repeat = true;

          this.signal.write(new CopyableLong(this.signal.read().getValue() + spawnDuration), this.signal.getTimeStamp());
        } else {
          repeat = false;
        }
        break;
      }
    } while (repeat);
    
    return true;
  }
}
