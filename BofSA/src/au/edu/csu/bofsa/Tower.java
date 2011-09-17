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

import java.util.List;

import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;

/**
 * @author ephphatha
 *
 */
public class Tower {
  private static Image defaultImage = null;
  
  public final Vector2f position;
  
  public final Sprite sprite;

  protected float timeElapsed;

  protected Attributes attributes;
  
  public static class Attributes {
    public float secondsPerShot;
  
    public float rangeSquared;
  
    public float damage;

    public Attributes() {
      this(0.0f, 0.0f, 0.0f);
    }
  
    Attributes(float fireRate, float range, float damage) {
      this.setFireRate(fireRate);
      this.setRange(range);
      this.setDamage(damage);
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
  
  protected Tower(Sprite sprite, final Vector2f position, Attributes attributes) {
    this.sprite = sprite;
    
    this.position = position;
    
    this.attributes = attributes;
    
    this.timeElapsed = 0.0f;
  }
  
  public static Tower createTower(Vector2f pos) {
    if (Tower.defaultImage == null) {
      ImageBuffer buffer = new ImageBuffer(16, 16);
      
      for (int x = 0; x < buffer.getWidth(); ++x) {
        for (int y = 0; y < buffer.getHeight(); ++y) {
          int rgb = ((x % 2 == 0) != (y % 2 == 0)) ? 255 : 0; 
          buffer.setRGBA(x, y, rgb, rgb, rgb, 255);
        }
      }
      
      Tower.defaultImage = new Image(buffer);
    }
    
    Sprite s;
    try {
      Image i = new Image("assets/tower.png");
      s = new Sprite(i, i.getWidth() / 4, i.getHeight() / 4);
    } catch (SlickException e) {
      s = new Sprite(Tower.defaultImage);
    } catch (RuntimeException e) {
      s = new Sprite(Tower.defaultImage);
    }

    Attributes attributes = new Attributes(2, 4, 8);
    
    Sprite.SequencePoint[] a = new Sprite.SequencePoint[4];

    for (int j = 0; j < 4; ++j) {
      a[j] = new Sprite.SequencePoint(j, 0.25f);
    }
    s.setFrameSequence(a);
    
    return new Tower(s, pos, attributes);
  }
  
  public void update(float dt, List<Creep> creeps) {
    this.timeElapsed += dt;

    boolean fired = false;
    
    while (this.timeElapsed > this.attributes.secondsPerShot) {
      fired = false;
      for (Creep c : creeps) {
        if (this.attack(c)) {
          fired = true;
          break;
        }
      }
      
      this.timeElapsed -= this.attributes.secondsPerShot;
    }
    
    if (this.timeElapsed > this.attributes.secondsPerShot && fired == false) {
      this.timeElapsed = this.attributes.secondsPerShot;
    }
  }

  private boolean attack(Creep c) {
    if (this.position.distanceSquared(c.getPosition()) <= this.attributes.rangeSquared) {
      c.takeDamage(this.attributes.damage);
      return true;
    } else {
      return false;
    }
  }
}
