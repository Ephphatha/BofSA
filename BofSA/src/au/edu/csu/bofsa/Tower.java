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

import au.edu.csu.bofsa.Behaviours.AttackBehaviour;
import au.edu.csu.bofsa.Behaviours.RenderBehaviour;
import au.edu.csu.bofsa.Events.Event;
import au.edu.csu.bofsa.Events.EventSink;
import au.edu.csu.bofsa.Events.GenericEvent;

/**
 * @author ephphatha
 *
 */
public class Tower implements EventSink{
  private AttackBehaviour a;
  private RenderBehaviour r;
  
  private CopyablePoint position;
  
  protected Tower(CopyablePoint position) {
    this.position = position;
  }
  
  public void update(float dt) {
    this.a.call();
    
    this.r.call();
  }

  public void draw(Graphics g) {
    this.r.draw(g);
  }
  
  @Override
  public void handleEvent(Event event) {
    if (event.value == GenericEvent.Message.NEW_BEHAVIOUR) {
      Object o = event.getSource();
      if (o instanceof AttackBehaviour) {
        this.a = (AttackBehaviour) o;
      } else if (o instanceof RenderBehaviour) {
        this.r = (RenderBehaviour) o;
      }
    }
  }

  public CopyablePoint getPosition() {
    return this.position;
  }
}
