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

import java.util.ArrayList;
import java.util.List;


/**
 * @author ephphatha
 *
 */
public class MoveBehaviour extends Behaviour<CopyableVector2f> {
  
  protected InputSignal<CopyableVector2f> pos;
  protected InputSignal<CopyableVector2f> vel;
  
  public MoveBehaviour(Signal<CopyableVector2f> targetPosition, InputSignal<CopyableVector2f> position, InputSignal<CopyableVector2f> velocity) {
    super(targetPosition);

    List<InputSignal<?>> tempList = new ArrayList<InputSignal<?>>();
    tempList.add(position);
    tempList.add(velocity);
    super.addInputs(tempList);
    
    this.pos = position;
    this.vel = velocity;
  }

  @Override
  protected void doRun() {
    CopyableVector2f vel = this.vel.read();
    CopyableVector2f pos = this.pos.read();
    
    long lastUpdate = this.pos.getTimeStamp();
    long current = System.nanoTime();
    float delta = (float) (current - lastUpdate) / (1000.0f * 1000.0f);
    vel.scale(delta);
    
    pos.add(vel);
    
    this.signal.write(pos, current);
  }

}
