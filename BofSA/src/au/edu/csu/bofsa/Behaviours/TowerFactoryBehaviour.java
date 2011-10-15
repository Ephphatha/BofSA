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

import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;
import org.newdawn.slick.SlickException;

import au.edu.csu.bofsa.CopyableBoolean;
import au.edu.csu.bofsa.CopyableDimension;
import au.edu.csu.bofsa.CopyableFloat;
import au.edu.csu.bofsa.CopyableList;
import au.edu.csu.bofsa.CopyablePoint;
import au.edu.csu.bofsa.CopyableVector2f;
import au.edu.csu.bofsa.Pipe;
import au.edu.csu.bofsa.Sprite;
import au.edu.csu.bofsa.Events.BuildAreaModEvent;
import au.edu.csu.bofsa.Events.Event;
import au.edu.csu.bofsa.Events.EventSink;
import au.edu.csu.bofsa.Events.GenericEvent;
import au.edu.csu.bofsa.Events.TowerSpawnEvent;
import au.edu.csu.bofsa.Signals.InputSignal;
import au.edu.csu.bofsa.Signals.Signal;

/**
 * @author ephphatha
 *
 */
public class TowerFactoryBehaviour extends Behaviour<CopyableList<CopyablePoint>> {

  protected static Image errorImage,
                         spriteSheet;
  
  protected InputSignal<CopyableDimension> tileSize;
  protected InputSignal<CopyableList<Pipe<CopyableVector2f>>> creeps;
  
  protected EventSink drawWatcher;

  protected EventSink behaviourWatcher;
  
  public static class Attributes {
    public int maxTargets;
    
    public float secondsPerShot;
  
    public float rangeSquared;
  
    public float damage;

    Attributes(float fireRate, float range, float damage) {
      this.setFireRate(fireRate);
      this.setRange(range);
      this.setDamage(damage);
      
      this.maxTargets = 1;
    }

    private void setDamage(float damage) {
      this.damage = damage;
    }

    public void setFireRate(float fireRate) {
      this.secondsPerShot = 1.0f / fireRate;
    }

    public void setRange(float range) {
      this.rangeSquared = range * range;
    }
  }
  
  public TowerFactoryBehaviour(Signal<CopyableList<CopyablePoint>> signal, InputSignal<CopyableDimension> tileSize, InputSignal<CopyableList<Pipe<CopyableVector2f>>> creeps, EventSink drawWatcher, EventSink behaviourWatcher) {
    super(TowerFactoryBehaviour.class.getSimpleName(), signal);
    
    this.tileSize = tileSize;
    this.creeps = creeps;
    
    this.drawWatcher = drawWatcher;
    
    this.behaviourWatcher = behaviourWatcher;
  }

  public static void loadResources() {
    TowerFactoryBehaviour.getErrorImage();
    TowerFactoryBehaviour.getSpriteSheet();
  }


  protected static Image getErrorImage() {
    if (TowerFactoryBehaviour.errorImage == null) {
      ImageBuffer buffer = new ImageBuffer(16, 16);
      
      for (int x = 0; x < buffer.getWidth(); ++x) {
        for (int y = 0; y < buffer.getHeight(); ++y) {
          int rgb = ((x % 2 == 0) != (y % 2 == 0)) ? 255 : 0; 
          buffer.setRGBA(x, y, rgb, rgb, rgb, 255);
        }
      }
      
      TowerFactoryBehaviour.errorImage = new Image(buffer);
    }
    
    return TowerFactoryBehaviour.errorImage;
  }
  
  
  protected static Image getSpriteSheet() {
    if (TowerFactoryBehaviour.spriteSheet == null) {
      try {
        TowerFactoryBehaviour.spriteSheet = new Image(TowerFactoryBehaviour.class.getResource("/assets/tower.png").getRef());
      } catch (NullPointerException n) {
        try {
          TowerFactoryBehaviour.spriteSheet = new Image("/assets/tower.png");
        } catch (SlickException e) {
          TowerFactoryBehaviour.spriteSheet = TowerFactoryBehaviour.errorImage;
        }
      } catch (SlickException e) {
        TowerFactoryBehaviour.spriteSheet = TowerFactoryBehaviour.errorImage;
      }
    }
    
    return TowerFactoryBehaviour.spriteSheet;
  }

  public static Sprite getSprite() {
    Sprite s;
    Image i = TowerFactoryBehaviour.getSpriteSheet();
    try {
      s = new Sprite(i, i.getWidth() / 4, i.getHeight() / 4);
    } catch (RuntimeException e) {
      s = new Sprite(TowerFactoryBehaviour.errorImage);
    }

    Sprite.SequencePoint[] a = new Sprite.SequencePoint[4];

    for (int j = 0; j < 4; ++j) {
      a[j] = new Sprite.SequencePoint(j, 0.25f);
    }
    s.setFrameSequence(a);
    
    return s;
  }

  public static void createTower(
      final CopyablePoint value,
      InputSignal<CopyableList<Pipe<CopyableVector2f>>> creeps,
      EventSink controller,
      InputSignal<CopyableDimension> tileSize,
      EventSink drawWatcher) {
    Sprite s = TowerFactoryBehaviour.getSprite();
    
    long birthTime = System.nanoTime();
    
    Signal<CopyableVector2f> position = new Signal<CopyableVector2f>(new CopyableVector2f(value.x, value.y));
    
    AttackBehaviour ab = new AttackBehaviour(new Signal<CopyableBoolean>(new CopyableBoolean(true)), creeps, position,
        new Signal<CopyableFloat>(new CopyableFloat(2.0f)),
        new Signal<CopyableFloat>(new CopyableFloat(8.0f)),
        new Signal<CopyableFloat>(new CopyableFloat(4.0f)));

    controller.handleEvent(new GenericEvent(ab, GenericEvent.Message.NEW_BEHAVIOUR, Event.Type.TARGETTED, birthTime));
    
    RenderBehaviour rb = new RenderBehaviour(new Signal<CopyableBoolean>(new CopyableBoolean(true)), position, tileSize, s, drawWatcher);

    controller.handleEvent(new GenericEvent(rb, GenericEvent.Message.NEW_BEHAVIOUR, Event.Type.TARGETTED, birthTime));
  }
  
  @Override
  protected boolean doRun() {
    while (!this.events.isEmpty()) {
      Event e = this.events.poll();
      
      if (e != null) {
        if (e instanceof BuildAreaModEvent) {
          BuildAreaModEvent.Data d = (BuildAreaModEvent.Data) e.value;
          switch (d.type) {
          case ADD_LOCATION:
          {
            CopyableList<CopyablePoint> c = this.signal.read().copy();
            c.add((CopyablePoint) d.position);
            this.signal.write(c);
            break;
          }

          case REMOVE_LOCATION:
          {
            CopyableList<CopyablePoint> c = this.signal.read().copy();
            c.remove((CopyablePoint) d.position);
            this.signal.write(c);
            break;
          }
          }
        } else if (e instanceof GenericEvent) {
          if (e.value == GenericEvent.Message.FORGET_ALL) {
            CopyableList<CopyablePoint> c = this.signal.read().copy();
            c.clear();
            this.signal.write(c);
          }
        } else if (e instanceof TowerSpawnEvent) {
          CopyableList<CopyablePoint> c = this.signal.read().copy();
          if (c.contains(e.value)) {
            TowerFactoryBehaviour.createTower((CopyablePoint) e.value, this.creeps, this.behaviourWatcher, this.tileSize, this.drawWatcher);
            c.remove(e.value);
            this.signal.write(c);
          }
        }
      }
    }
    return true;
  }
  
  public boolean isReady() {
    return !this.events.isEmpty();
  }
}
