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

/**
 * @author ephphatha
 *
 */
public class CollisionBehaviour extends Behaviour<CopyableBoolean> {

  protected InputSignal<CopyableVector2f> object;
  protected InputSignal<CopyableFloat> radius;
  protected InputSignal<CopyableVector2f> collider;
  
  public CollisionBehaviour(Signal<CopyableBoolean> signal, InputSignal<CopyableVector2f> object, InputSignal<CopyableFloat> radius, InputSignal<CopyableVector2f> collider) {
    super(signal);
    
    this.object = object;
    this.radius = radius;
    this.collider = collider;
  }

  @Override
  protected void doRun() {
    CopyableVector2f objPos = this.object.read();
    CopyableVector2f colPos = this.collider.read();
    
    if (objPos.distanceSquared(colPos) <= Math.pow(this.radius.read().getValue(), 2)) {
      if (this.signal.read().getValue() == false) {
        // TODO Generate collision event;
        this.signal.write(new CopyableBoolean(true));
      }
    } else {
      this.signal.write(new CopyableBoolean(false));
    }
  }
}
