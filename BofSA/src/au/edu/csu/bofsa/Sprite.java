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

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Rectangle;

/**
 * @author ephphatha
 *
 */
public class Sprite extends SpriteSheet {
  protected SequencePoint[] sequence;
  protected boolean isCustomSequence;
  
  protected int currentFrameIndex;
  protected int currentFrameSequenceIndex;
  protected float currentFrameTime;
  
  public static class SequencePoint {
    public final int frameIndex;
    public final float duration;
    
    public SequencePoint() {
      this(0);
    }
    
    public SequencePoint(int frameIndex) {
      this(frameIndex, Float.POSITIVE_INFINITY);
    }
    
    public SequencePoint(int frameIndex, float duration) {
      this.frameIndex = frameIndex;
      this.duration = duration;
    }
  }
  
  public Sprite(Sprite s) {
    this((SpriteSheet) s);
    
    this.setFrameSequence(s.sequence);
  }
  
  public Sprite(SpriteSheet s) {
    this((Image) s, s.getWidth() / s.getHorizontalCount(), s.getHeight() / s.getVerticalCount());
  }
  
  public Sprite(Image image) {
    this(image, image.getWidth(), image.getHeight());
  }
  
  public Sprite(Image image, int frameWidth, int frameHeight) {
    this(image, Float.POSITIVE_INFINITY, frameWidth, frameHeight);
  }
  
  public Sprite(Image image, float duration, int frameWidth, int frameHeight) {
    super(image, frameWidth, frameHeight);
    
    this.doSetBasicFrameSequence(duration);
    
    this.resetSequenceIndex();
  }
  
  public void update(float dt) {
    this.currentFrameTime += dt;
    
    while (this.currentFrameTime > this.sequence[this.currentFrameSequenceIndex].duration) {
      this.currentFrameTime -= this.sequence[this.currentFrameSequenceIndex].duration;
      this.doNextFrame();
    }
  }
  
  public void draw(Graphics g, Rectangle tile) {
    Rectangle r = new Rectangle(0, 0,
        this.getWidth() / this.getHorizontalCount(),
        this.getHeight() / this.getVerticalCount());
    
    r.setLocation((this.currentFrameIndex % this.getHorizontalCount()) * r.getWidth(),
                  (this.currentFrameIndex / this.getHorizontalCount()) * r.getHeight());
    super.draw(tile.getX(), tile.getY(), tile.getMaxX(), tile.getMaxY(), r.getX(), r.getY(), r.getMaxX(), r.getMaxY());
  }
 
  public int getFrameIndex() {
    return this.currentFrameIndex;
  }
  
  public boolean setFrameIndex(int i) {
    if (this.numFrames() > i) {
      this.currentFrameIndex = i;
      return true;
    } else {
      return false;
    }
  }
  
  private int numFrames() {
    return this.getHorizontalCount() * this.getVerticalCount();
  }

  public boolean setFrameSequence(SequencePoint[] a) {
    try {
      if (a.length < 1) {
        return false;
      }
      
      for (SequencePoint i : a) {
        if (i.frameIndex >= this.numFrames()) {
          return false;
        }
      }
      
      this.sequence = a;
      this.isCustomSequence = true;
      
      this.resetSequenceIndex();
      
      return true;
    } catch (NullPointerException e) {
      return false;
    }
  }
  
  private void resetSequenceIndex() {
    this.currentFrameTime = 0.0f;
    this.currentFrameSequenceIndex = 0;
    
    if (this.sequence.length > 0) {
      this.currentFrameIndex = this.sequence[this.currentFrameSequenceIndex].frameIndex;
    } else {
      this.currentFrameIndex = 0;
    }
  }

  private void doSetBasicFrameSequence(float duration) {
    this.sequence = new SequencePoint[this.numFrames()];
    
    for (int i = 0; i < this.sequence.length; ++i) {
      this.sequence[i] = new SequencePoint(i, duration);
    }
    
    this.isCustomSequence = false;
    
    this.resetSequenceIndex();
  }
  
  public boolean setFrameSequenceIndex(int i) {
    try {
      this.currentFrameIndex = this.sequence[i].frameIndex;
      this.currentFrameSequenceIndex = i;
      return true;
    } catch (IndexOutOfBoundsException e) {
      return false;
    }
  }
  
  public void nextFrame() {
    this.currentFrameTime = 0;
    
    this.doNextFrame();
  }

  private void doNextFrame() {
    if (++this.currentFrameSequenceIndex >= this.sequence.length) {
      this.currentFrameSequenceIndex = 0;
    }
    
    this.currentFrameIndex = this.sequence[this.currentFrameSequenceIndex].frameIndex;
  }

  public void prevFrame() {
    this.currentFrameTime = 0;
    
    if (--this.currentFrameSequenceIndex <= 0) {
      this.currentFrameSequenceIndex = this.sequence.length - 1;
    }
    
    this.currentFrameIndex = this.sequence[this.currentFrameSequenceIndex].frameIndex;
  }
}
