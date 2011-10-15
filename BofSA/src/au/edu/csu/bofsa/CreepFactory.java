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
import org.newdawn.slick.geom.Vector2f;

import au.edu.csu.bofsa.Signals.InputSignal;

/**
 * @author ephphatha
 *
 */
public class CreepFactory {
  protected Image errorImage,
                  spriteSheet;
  

  public Creep spawnCreep(final Vector2f pos,
      final Queue<CheckPoint> cps, final InputSignal<CopyableDimension> tileSize) {
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
    
    return new Creep(s, a, pos, cps, tileSize);
  }
}
