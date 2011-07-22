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
package au.edu.csu.bofsa;

import java.util.Queue;

import org.newdawn.slick.geom.Vector2f;

/**
 * @author ephphatha
 *
 */
public class SpawnPoint {
  public final Vector2f position;
  protected Queue<CheckPoint> checkpoints;
  protected Vector2f goal;
  
  protected final float spawnDuration,
                        spawnInterval,
                        lullDuration;
  
  private State state;
  private float elapsedTime;
  private float lastSpawnTime;
  
  protected enum State {
    SPAWNING,
    IDLE
  }

  public SpawnPoint(final Vector2f position, float spawnDuration, float spawnInterval, float lullDuration) {
    this.position = position;
    
    this.spawnDuration = spawnDuration;
    this.spawnInterval = spawnInterval;
    this.lullDuration = lullDuration;
    
    this.checkpoints = null;
    this.goal = null;
    
    this.state = State.IDLE;
    
    this.elapsedTime = 0;
    this.lastSpawnTime = 0;
  }

  public boolean setCheckPoints(Queue<CheckPoint> checkpoints) {
    if (this.checkpoints == null) {
      this.checkpoints = checkpoints;
      return true;
    } else {
      return false;
    }
  }
  
  public boolean setGoal(Vector2f goal) {
    if (this.goal == null) {
      this.goal = goal;
      return true;
    } else {
      return false;
    }
  }
  
  public void update(CreepManager cm, float dt) {
    this.elapsedTime += dt;
    
    boolean repeat = false;
    
    do {
      switch (this.state) {
      case IDLE:
        if (this.elapsedTime >= this.lullDuration) {
          this.state = State.SPAWNING;
          repeat = true;
          
          this.elapsedTime -= this.lullDuration;
          this.lastSpawnTime = this.elapsedTime;
        } else {
          repeat = false;
        }
        break;
        
      case SPAWNING:
        while (this.elapsedTime - this.lastSpawnTime >= this.spawnInterval) {
          this.lastSpawnTime += this.spawnInterval;
          cm.spawnCreep(Creep.Type.CUSTOMER, this.position, this.checkpoints, this.goal);
          
          if (this.lastSpawnTime > this.spawnDuration) {
            break;
          }
        }
        
        if (this.elapsedTime >= this.spawnDuration) {
          this.state = State.IDLE;
          repeat = true;
          
          this.elapsedTime -= this.spawnDuration;
        } else {
          repeat = false;
        }
        break;
      }
    } while (repeat);
  }
}
