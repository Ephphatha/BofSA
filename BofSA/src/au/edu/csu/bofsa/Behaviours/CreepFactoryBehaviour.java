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

import java.util.Queue;

import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;
import org.newdawn.slick.SlickException;

import au.edu.csu.bofsa.CheckPoint;
import au.edu.csu.bofsa.CopyableBoolean;
import au.edu.csu.bofsa.CopyableDimension;
import au.edu.csu.bofsa.CopyableFloat;
import au.edu.csu.bofsa.CopyableList;
import au.edu.csu.bofsa.CopyableVector2f;
import au.edu.csu.bofsa.Pipe;
import au.edu.csu.bofsa.Sprite;
import au.edu.csu.bofsa.Events.CreepSpawnEvent;
import au.edu.csu.bofsa.Events.Event;
import au.edu.csu.bofsa.Events.EventSink;
import au.edu.csu.bofsa.Events.GenericEvent;
import au.edu.csu.bofsa.Events.Stream;
import au.edu.csu.bofsa.Events.CreepSpawnEvent.SpawnEventParameters;
import au.edu.csu.bofsa.Signals.InputSignal;
import au.edu.csu.bofsa.Signals.Signal;

/**
 * @author ephphatha
 *
 */
public class CreepFactoryBehaviour extends Behaviour<CopyableList<Pipe<CopyableVector2f>>> {

  protected static Image errorImage,
                         spriteSheet;
  
  protected InputSignal<CopyableDimension> tileSize;
  
  protected EventSink behaviourWatcher;

  protected EventSink drawWatcher;
  
  public CreepFactoryBehaviour(Signal<CopyableList<Pipe<CopyableVector2f>>> signal, InputSignal<CopyableDimension> tileSize, EventSink behaviourWatcher, EventSink drawWatcher) {
    super(CreepFactoryBehaviour.class.getSimpleName(), signal);
    
    super.addInput(tileSize);
    
    this.tileSize = tileSize;
    
    this.behaviourWatcher = behaviourWatcher;
    this.drawWatcher = drawWatcher;
  }

  
  public static void loadResources() {
    CreepFactoryBehaviour.getErrorImage();
    CreepFactoryBehaviour.getSpriteSheet();
  }


  protected static Image getErrorImage() {
    if (CreepFactoryBehaviour.errorImage == null) {
      ImageBuffer buffer = new ImageBuffer(16, 16);
      
      for (int x = 0; x < buffer.getWidth(); ++x) {
        for (int y = 0; y < buffer.getHeight(); ++y) {
          int rgb = ((x % 2 == 0) != (y % 2 == 0)) ? 255 : 0; 
          buffer.setRGBA(x, y, rgb, rgb, rgb, 255);
        }
      }
      
      CreepFactoryBehaviour.errorImage = new Image(buffer);
    }
    
    return CreepFactoryBehaviour.errorImage;
  }
  
  
  protected static Image getSpriteSheet() {
    if (CreepFactoryBehaviour.spriteSheet == null) {
      try {
        CreepFactoryBehaviour.spriteSheet = new Image(CreepFactoryBehaviour.class.getResource("/assets/creep.png").getRef());
      } catch (NullPointerException n) {
        try {
          CreepFactoryBehaviour.spriteSheet = new Image("/assets/creep.png");
        } catch (SlickException e) {
          CreepFactoryBehaviour.spriteSheet = CreepFactoryBehaviour.getErrorImage();
        }
      } catch (SlickException e) {
        CreepFactoryBehaviour.spriteSheet = CreepFactoryBehaviour.getErrorImage();
      }
    }
    
    return CreepFactoryBehaviour.spriteSheet;
  }

  public Sprite getSprite() {
    Sprite s;
    Image i = CreepFactoryBehaviour.getSpriteSheet();
    try {
      s = new Sprite(i, i.getWidth() / 8, i.getHeight() / 8);
    } catch (RuntimeException e) {
      s = new Sprite(CreepFactoryBehaviour.getErrorImage());
    }

    return s;
  }
  
  public static void spawnCreep(
      Sprite s,
      final CopyableVector2f pos,
      final Queue<CheckPoint> cps,
      EventSink controller,
      EventSink behaviourWatcher,
      InputSignal<CopyableDimension> tileSize,
      EventSink drawWatcher,
      Signal<CopyableList<Pipe<CopyableVector2f>>> signal) {
    Sprite.SequencePoint[][] a = new Sprite.SequencePoint[4][];

    for (int i = 0; i < 4; ++i) {
      a[i] = new Sprite.SequencePoint[4];
      for (int j = 0; j < 4; ++j) {
        a[i][j] = new Sprite.SequencePoint((i * 4) + j, 0.25f);
      }
    }
    
    long birthTime = System.nanoTime();
    
    Stream creepStream = new Stream();
    
    creepStream.addSink(controller);
    
    Signal<CopyableFloat> health = new Signal<CopyableFloat>(new CopyableFloat(64.0f));
    
    HealthBehaviour h = new HealthBehaviour(
        health,
        creepStream,
        controller);
    
    behaviourWatcher.handleEvent(
        new GenericEvent(
            h,
            GenericEvent.Message.NEW_BEHAVIOUR,
            Event.Type.TARGETTED,
            birthTime));
    
    Signal<CopyableVector2f> position = new Signal<CopyableVector2f>(pos);
    
    Signal<CopyableVector2f> velocity = new Signal<CopyableVector2f>(new CopyableVector2f(0, 0));
    
    MoveBehaviour m = new MoveBehaviour(
        position,
        velocity,
        creepStream);
    
    behaviourWatcher.handleEvent(
        new GenericEvent(
            m,
            GenericEvent.Message.NEW_BEHAVIOUR,
            Event.Type.TARGETTED,
            birthTime));
    
    Signal<CheckPoint> cp = new Signal<CheckPoint>(cps.peek());
    
    WaypointBehaviour w = new WaypointBehaviour(
        cp,
        cps,
        creepStream);

    behaviourWatcher.handleEvent(
        new GenericEvent(
            w,
            GenericEvent.Message.NEW_BEHAVIOUR,
            Event.Type.TARGETTED,
            birthTime));
    
    Signal<CopyableFloat> speed = new Signal<CopyableFloat>(new CopyableFloat(1.0f));
    
    VelocityBehaviour v = new VelocityBehaviour(
        velocity,
        position,
        cp,
        speed,
        creepStream);

    behaviourWatcher.handleEvent(
        new GenericEvent(
            v,
            GenericEvent.Message.NEW_BEHAVIOUR,
            Event.Type.TARGETTED,
            birthTime));
    
    CollisionBehaviour c = new CollisionBehaviour(
        new Signal<CopyableBoolean>(new CopyableBoolean(true)),
        position,
        new Signal<CopyableFloat>(new CopyableFloat(0.25f)),
        cp,
        creepStream);

    behaviourWatcher.handleEvent(
        new GenericEvent(
            c,
            GenericEvent.Message.NEW_BEHAVIOUR,
            Event.Type.TARGETTED,
            birthTime));
    
    ActorRenderBehaviour arb = new ActorRenderBehaviour(
        new Signal<CopyableBoolean>(new CopyableBoolean(true)),
        position,
        velocity,
        health,
        new Signal<CopyableFloat>(health.read()),
        tileSize,
        s,
        a,
        creepStream,
        drawWatcher);

    behaviourWatcher.handleEvent(
        new GenericEvent(
            arb,
            GenericEvent.Message.NEW_BEHAVIOUR,
            Event.Type.TARGETTED,
            birthTime));
    
    CopyableList<Pipe<CopyableVector2f>> temp = signal.read().copy();
    
    temp.add(new Pipe<CopyableVector2f>(position, creepStream));
    
    signal.write(temp);
  }

  
  @Override
  protected boolean doRun() {
    while (!this.events.isEmpty()) {
      Event e = this.events.poll();
      
      if (e == null) {
        continue;
      } else if (e instanceof GenericEvent) {
        if (e.value == GenericEvent.Message.DEATH && e.getSource() instanceof Stream) {
          CopyableList<Pipe<CopyableVector2f>> temp = this.signal.read().copy();
          
          for (int i = temp.size() - 1; i >= 0; --i) {
            if (temp.get(i).sink.equals(e.getSource())) {
              temp.remove(i);
            }
          }
          
          this.signal.write(temp);
        } else if (e.value == GenericEvent.Message.FORGET_ALL) {
          CopyableList<Pipe<CopyableVector2f>> c = this.signal.read().copy();
          c.clear();
          this.signal.write(c);
        }
      } else if (e instanceof CreepSpawnEvent) {
        CreepSpawnEvent.SpawnEventParameters params = (SpawnEventParameters) e.value;
        
        CreepFactoryBehaviour.spawnCreep(
            this.getSprite(),
            new CopyableVector2f(params.position),
            params.waypoints,
            this,
            this.behaviourWatcher,
            this.tileSize,
            this.drawWatcher,
            this.signal);
      }
    }
    return true;
  }
  
  public boolean isReady() {
    return !this.events.isEmpty();
  }
}
