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

  protected Image errorImage,
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
    super(signal);
    
    super.addInput(tileSize);
    super.addInput(creeps);
    
    this.tileSize = tileSize;
    this.creeps = creeps;
    
    this.drawWatcher = drawWatcher;
    
    this.behaviourWatcher = behaviourWatcher;
  }

  public void loadResources() {
    this.getErrorImage();
    this.getSpriteSheet();
  }


  protected Image getErrorImage() {
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
    
    return this.errorImage;
  }
  
  
  protected Image getSpriteSheet() {
    if (this.spriteSheet == null) {
      try {
        this.spriteSheet = new Image("assets/tower.png");
      } catch (SlickException e) {
        this.spriteSheet = this.errorImage;
      }
    }
    
    return this.spriteSheet;
  }

  public void createTower(final CopyablePoint value) {
    Sprite s;
    try {
      s = new Sprite(this.spriteSheet, this.spriteSheet.getWidth() / 4, this.spriteSheet.getHeight() / 4);
    } catch (RuntimeException e) {
      s = new Sprite(this.errorImage);
    }

    Sprite.SequencePoint[] a = new Sprite.SequencePoint[4];

    for (int i = 0; i < 4; ++i) {
      a[i] = new Sprite.SequencePoint(i, 0.25f);
    }
    s.setFrameSequence(a);

    //switch (type) {
    //case CLERK:
    //  attributes.setFireRate(10.0f);
    //  attributes.setRange(4.0f);
    //  attributes.setDamage(2.0f);
    //  break;
    //}

    long birthTime = System.nanoTime();
    
    Signal<CopyableVector2f> position = new Signal<CopyableVector2f>(new CopyableVector2f(value.x, value.y));
    
    AttackBehaviour ab = new AttackBehaviour(new Signal<CopyableBoolean>(new CopyableBoolean(true)), this.creeps, position,
        new Signal<CopyableFloat>(new CopyableFloat(0.1f)),
        new Signal<CopyableFloat>(new CopyableFloat(4.0f)),
        new Signal<CopyableFloat>(new CopyableFloat(2.0f)));

    this.behaviourWatcher.handleEvent(new GenericEvent(ab, GenericEvent.Message.NEW_BEHAVIOUR, Event.Type.TARGETTED, birthTime));
    
    RenderBehaviour rb = new RenderBehaviour(new Signal<CopyableBoolean>(new CopyableBoolean(true)), position, this.tileSize, s, this.drawWatcher);

    this.behaviourWatcher.handleEvent(new GenericEvent(rb, GenericEvent.Message.NEW_BEHAVIOUR, Event.Type.TARGETTED, birthTime));
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
            CopyableList<CopyablePoint> c = this.signal.read();
            c.add((CopyablePoint) d.position);
            this.signal.write(c);
            break;
          }
            
          case REMOVE_LOCATION:
          {
            CopyableList<CopyablePoint> c = this.signal.read();
            c.add((CopyablePoint) d.position);
            this.signal.write(c);
            break;
          }
          
          case REMOVE_ALL:
          {
            CopyableList<CopyablePoint> c = this.signal.read();
            c.clear();
            this.signal.write(c);
          }
          }
        } else if (e instanceof TowerSpawnEvent) {
          CopyableList<CopyablePoint> c = this.signal.read();
          if (c.contains(e.value)) {
            this.createTower((CopyablePoint) e.value);
            c.remove(e.value);
            this.signal.write(c);
          }
        }
      }
    }
    return true;
  }
}
