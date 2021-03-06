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

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import au.edu.csu.bofsa.Copyable;
import au.edu.csu.bofsa.Events.Event;
import au.edu.csu.bofsa.Events.EventSink;
import au.edu.csu.bofsa.Signals.InputSignal;
import au.edu.csu.bofsa.Signals.Signal;

/**
 * @author ephphatha
 *
 */
public abstract class Behaviour<T extends Copyable<T>> implements Callable<Boolean>, EventSink, Comparable<Object> {
  
  private static final int BALLASTITERATIONS = 1000;
  protected long lastStartTime;
  protected long lastEndTime;
  
  protected Signal<T> signal;
  protected List<InputSignal<?>> inputs;
  protected Queue<Event> events;
  
  protected long deltaThreshold;
  protected final String name;

  public long fibresult;
  
  public Behaviour(String name, Signal<T> signal) {
    this.name = name;
    this.lastStartTime = System.nanoTime();
    this.lastEndTime = System.nanoTime();
    this.signal = signal;
    
    this.inputs = new LinkedList<InputSignal<?>>();
    this.events = new ConcurrentLinkedQueue<Event>();
    
    this.deltaThreshold = 100000;
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
    this.lastStartTime = System.nanoTime();
    
    boolean retVal = this.doRun();
    
    long a = 1;
    long b = 1;
    
    for (int i = 0; i < Behaviour.BALLASTITERATIONS; ++i) {
      long temp = a;
      a += b;
      b = temp;
    }
    
    this.fibresult = a;
    
    this.lastEndTime = System.nanoTime();
    
    return retVal;
  }

  @Override
  public void handleEvent(Event event) {
    this.events.offer(event);
  }
  
  abstract protected boolean doRun();

  public long getLastStartTime() {
    return this.lastStartTime;
  }

  public long getLastRunTime() {
    return this.lastEndTime - this.lastStartTime;
  }
  
  public long getLastCompletionTime() {
    return this.lastEndTime;
  }
  
  public boolean isReady() {
    for (InputSignal<?> i : this.inputs) {
      if (this.signal.getTimeStamp() - i.getTimeStamp() > this.deltaThreshold) {
        return false;
      }
    }
    
    return true;
  }
  
  public int compareTo(Object o) {
    return this.hashCode() - o.hashCode();
  }

  public String getName() {
    return this.name;
  }
}
