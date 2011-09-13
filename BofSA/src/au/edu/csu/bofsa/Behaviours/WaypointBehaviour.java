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

import java.util.LinkedList;
import java.util.Queue;

import au.edu.csu.bofsa.CheckPoint;
import au.edu.csu.bofsa.Events.CollisionEvent;
import au.edu.csu.bofsa.Events.Event;
import au.edu.csu.bofsa.Events.GenericEvent;
import au.edu.csu.bofsa.Events.Stream;
import au.edu.csu.bofsa.Signals.Signal;

/**
 * @author ephphatha
 *
 */
public class WaypointBehaviour extends Behaviour<CheckPoint> {

  protected Queue<CheckPoint> waypoints;
  protected Stream creepStream;

  public WaypointBehaviour(Signal<CheckPoint> goal, Queue<CheckPoint> waypoints, Stream creepStream) {
    super(goal);
    
    if (waypoints.isEmpty()) {
      throw new IllegalArgumentException("Must be at least one waypoint.");
    }
    
    this.waypoints = new LinkedList<CheckPoint>(waypoints);
    
    this.signal.write(this.waypoints.poll());
    
    this.creepStream = creepStream;
    
    this.creepStream.addSink(this);
  }

  @Override
  protected boolean doRun() {
    if (this.events.isEmpty()) {
      return true;
    } else {
      while (!this.events.isEmpty()) {
        Event e = this.events.poll();
        
        if (e instanceof CollisionEvent) {
          if (this.waypoints.isEmpty()) {
            this.creepStream.handleEvent(new GenericEvent(this, GenericEvent.Message.DEATH, Event.Type.BROADCAST, System.nanoTime()));
            return false;
          } else {
            this.signal.write(this.waypoints.poll());
          }
        } else if (e instanceof GenericEvent) {
          if (e.value == GenericEvent.Message.DEATH) {
            return false;
          }
        }
      }
    }
    
    return true;
  }
}
