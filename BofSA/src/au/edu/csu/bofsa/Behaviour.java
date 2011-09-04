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

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import au.edu.csu.bofsa.Event.Generic;

/**
 * @author ephphatha
 *
 */
public abstract class Behaviour<T extends Copyable<T>> implements Callable<Boolean>, EventSink<Event.Generic> {
  
  protected long lastStartTime;
  protected long lastEndTime;
  
  protected Signal<T> signal;
  protected List<InputSignal<?>> inputs;
  protected Queue<Event<Event.Generic>> events;

  @SuppressWarnings("unused")
  private Behaviour() {
    //Goggles
  }
  
  public Behaviour(Signal<T> signal) {
    this.inputs = new LinkedList<InputSignal<?>>();
    this.lastStartTime = System.nanoTime();
    this.signal = signal;
    this.lastEndTime = System.nanoTime();
    
    this.events = new ConcurrentLinkedQueue<Event<Event.Generic>>();
  }
  
  protected void addInput(InputSignal<?> input) {
    this.inputs.add(input);
  }
  
  protected void addInputs(List<? extends InputSignal<?>> inputs) {
    this.inputs.addAll(inputs);
  }
  
  public Signal<T> getSignal() {
    return this.signal;
  }
  
  public Signal<T> setSignal(Signal<T> signal) {
    Signal<T> temp = getSignal();
    this.signal = signal;
    return temp;
  }
  
  @Override
  public Boolean call() {
    while (!this.events.isEmpty()) {
      Event<Event.Generic> e = this.events.poll();
      
      if (e != null) {
        if (e.value == Event.Generic.DEATH) {
          return false;
        }
      }
    }
    
    this.lastStartTime = System.nanoTime();
    
    if (!doRun()) {
      return false;
    }
    
    this.lastEndTime = System.nanoTime();
    
    return true;
  }

  @Override
  public void handleEvent(Event<Generic> event) {
    this.events.offer(event);
  }
  
  abstract protected boolean doRun();

  public long getLastRunTime() {
    return this.lastEndTime;
  }
}
