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
package au.edu.csu.bofsa.Events;

import java.util.Queue;

import org.newdawn.slick.geom.Vector2f;

import au.edu.csu.bofsa.CheckPoint;

/**
 * @author ephphatha
 *
 */
public class CreepSpawnEvent extends Event {

  private static final long serialVersionUID = 7750426419085883272L;

  public static class SpawnEventParameters {
    public Vector2f position;
    public Queue<CheckPoint> waypoints;
    
    public SpawnEventParameters(Vector2f position, Queue<CheckPoint> waypoints) {
      this.position = position;
      this.waypoints = waypoints;
    }
  }
  
  /**
   * @param source
   * @param params
   * @param time
   */
  public CreepSpawnEvent(Object source, SpawnEventParameters params, Type type, long time) {
    super(source, params, type, time);
  }

}
