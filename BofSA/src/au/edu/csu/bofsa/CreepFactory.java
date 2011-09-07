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

import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;
import org.newdawn.slick.SlickException;

import au.edu.csu.bofsa.Behaviours.ActorRenderBehaviour;
import au.edu.csu.bofsa.Behaviours.CollisionBehaviour;
import au.edu.csu.bofsa.Behaviours.MoveBehaviour;
import au.edu.csu.bofsa.Behaviours.VelocityBehaviour;
import au.edu.csu.bofsa.Behaviours.WaypointBehaviour;

/**
 * @author ephphatha
 *
 */
public class CreepFactory {
  protected Image errorImage,
                  spriteSheet;
  

  public void spawnCreep(Scheduler scheduler, final CopyableVector2f pos,
      final Queue<CheckPoint> cps, EventSink drawWatcher) {
    if (this.errorImage == null) {
      ImageBuffer buffer = new ImageBuffer(16, 16);
      
      for (int x = 0; x < buffer.getWidth(); ++x) {
        for (int y = 0; y < buffer.getHeight(); ++y) {
          int rgb = ((x % 2 == 0) != (y % 2 == 0)) ? 255 : 0; 
          buffer.setRGBA(x, y, rgb, rgb, rgb, 255);
        }
      }
      
      this.errorImage = new Image(buffer);
    }
    
    Sprite s;
    try {
      if (this.spriteSheet == null) {
        this.spriteSheet = new Image("assets/creep.png");
      }
      
      s = new Sprite(this.spriteSheet, this.spriteSheet.getWidth() / 8, this.spriteSheet.getHeight() / 8);
    } catch (SlickException e) {
      s = new Sprite(this.errorImage);
    } catch (RuntimeException e) {
      s = new Sprite(this.errorImage);
    }

    Sprite.SequencePoint[][] a = new Sprite.SequencePoint[4][];

    for (int i = 0; i < 4; ++i) {
      a[i] = new Sprite.SequencePoint[4];
      for (int j = 0; j < 4; ++j) {
        a[i][j] = new Sprite.SequencePoint((i * 4) + j, 0.25f);
      }
    }
    
    Stream creepStream = new Stream();
    
    Signal<CopyableVector2f> position = new Signal<CopyableVector2f>(pos);
    
    Signal<CopyableVector2f> velocity = new Signal<CopyableVector2f>(new CopyableVector2f(0, 0));
    
    MoveBehaviour m = new MoveBehaviour(position, position, velocity, creepStream);
    
    scheduler.call(m);
    
    Signal<CheckPoint> cp = new Signal<CheckPoint>(cps.peek());
    
    WaypointBehaviour w = new WaypointBehaviour(cp, cps, creepStream);
    
    scheduler.call(w);
    
    Signal<CopyableFloat> speed = new Signal<CopyableFloat>(new CopyableFloat(1.0f));
    
    VelocityBehaviour v = new VelocityBehaviour(velocity, position, cp, speed, creepStream);

    scheduler.call(v);
    
    CollisionBehaviour c = new CollisionBehaviour(new Signal<CopyableBoolean>(new CopyableBoolean(true)), position, new Signal<CopyableFloat>(new CopyableFloat(0.25f)), cp, creepStream);

    scheduler.call(c);
    
    ActorRenderBehaviour arb = new ActorRenderBehaviour(new Signal<CopyableBoolean>(new CopyableBoolean(true)), position, velocity, s, a, creepStream, drawWatcher);

    scheduler.call(arb);
  }
}
