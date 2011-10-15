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

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

import au.edu.csu.bofsa.Behaviours.ActorRenderBehaviour;
import au.edu.csu.bofsa.Behaviours.CollisionBehaviour;
import au.edu.csu.bofsa.Behaviours.HealthBehaviour;
import au.edu.csu.bofsa.Behaviours.MoveBehaviour;
import au.edu.csu.bofsa.Behaviours.VelocityBehaviour;
import au.edu.csu.bofsa.Behaviours.WaypointBehaviour;
import au.edu.csu.bofsa.Events.DamageEvent;
import au.edu.csu.bofsa.Events.Event;
import au.edu.csu.bofsa.Events.EventSink;
import au.edu.csu.bofsa.Events.GenericEvent;
import au.edu.csu.bofsa.Events.Stream;
import au.edu.csu.bofsa.Signals.InputSignal;
import au.edu.csu.bofsa.Signals.Signal;

/**
 * @author ephphatha
 *
 */
public class Creep implements EventSink {

  private HealthBehaviour h;
  private MoveBehaviour m;
  private WaypointBehaviour w;
  private VelocityBehaviour v;
  private CollisionBehaviour c;
  private ActorRenderBehaviour arb;
  private boolean isDead;
  
  Creep(Sprite sprite, final Sprite.SequencePoint[][] frames, final Vector2f pos, final Queue<CheckPoint> checkpoints, final InputSignal<CopyableDimension> tileSize) {
    
    Stream creepStream = new Stream();
    
    creepStream.addSink(this);
    
    Signal<CopyableFloat> health = new Signal<CopyableFloat>(new CopyableFloat(64.0f));
    
    this.h = new HealthBehaviour(
        health,
        creepStream,
        this);
    
    Signal<CopyableVector2f> position = new Signal<CopyableVector2f>(new CopyableVector2f(pos));
    
    Signal<CopyableVector2f> velocity = new Signal<CopyableVector2f>(new CopyableVector2f(0, 0));
    
    this.m = new MoveBehaviour(
        position,
        velocity,
        creepStream);
    
    Signal<CheckPoint> cp = new Signal<CheckPoint>(checkpoints.peek());
    
    this.w = new WaypointBehaviour(
        cp,
        checkpoints,
        creepStream);

    Signal<CopyableFloat> speed = new Signal<CopyableFloat>(new CopyableFloat(1.0f));
    
    this.v = new VelocityBehaviour(
        velocity,
        position,
        cp,
        speed,
        creepStream);

    this.c = new CollisionBehaviour(
        new Signal<CopyableBoolean>(new CopyableBoolean(true)),
        position,
        new Signal<CopyableFloat>(new CopyableFloat(0.25f)),
        cp,
        creepStream);

    this.arb = new ActorRenderBehaviour(
        new Signal<CopyableBoolean>(new CopyableBoolean(true)),
        position,
        velocity,
        health,
        new Signal<CopyableFloat>(health.read()),
        tileSize,
        sprite,
        frames,
        creepStream,
        this);

    this.isDead = false;
  }
  
  public Rectangle getBounds() {
    CopyableVector2f position = this.m.getSignal().read();
    return new Rectangle(position.x - 0.25f, position.y - 0.25f, 0.5f, 0.5f);
  }
  
  public Vector2f getPosition() {
    return this.m.getSignal().read();
  }
  
  public Vector2f getVelocity() {
    return this.v.getSignal().read();
  }
  
  public void draw(Graphics g) {
    this.arb.draw(g);
  }
  
  public void update(float dt) {
    this.arb.call();
  }
  
  public void update(CreepManager cm, float dt) {
    this.update(dt);
    
    this.h.call();
    
    this.v.call();

    this.m.call();
    
    this.c.call();
    
    this.w.call();
    
    if (this.isDead) {
      cm.onDeath(this);
    }
  }

  public void takeDamage(float damage) {
    this.h.handleEvent(new DamageEvent(this, damage, Event.Type.TARGETTED, System.nanoTime()));
  }

  @Override
  public void handleEvent(Event event) {
    if (event.value == GenericEvent.Message.DEATH) {
      this.isDead = true;
    }
  }
}
