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
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import au.edu.csu.bofsa.Behaviours.Behaviour;
import au.edu.csu.bofsa.Events.Event;
import au.edu.csu.bofsa.Events.EventSink;
import au.edu.csu.bofsa.Events.GenericEvent;

/**
 * @author ephphatha
 *
 */
public class Scheduler implements Caller<Boolean>, EventSink, Comparable<Object> {

  protected List<Thread> threads;
  protected Queue<WorkerThread> idleThreads;
  
  protected Queue<Callable<Boolean>> tasks;
  protected Queue<Callable<Boolean>> pendingTasks;
  
  private AtomicLong numTasks;
  private AtomicLong totalRunTime;
  
  protected static enum State {
    RUNNING,
    STOPPED
  }
  
  protected State state;
  
  public Scheduler() {
    this.threads = new LinkedList<Thread>();
    this.idleThreads = new ConcurrentLinkedQueue<WorkerThread>();
    this.tasks = new ConcurrentLinkedQueue<Callable<Boolean>>();
    this.pendingTasks = new PriorityBlockingQueue<Callable<Boolean>>();
    
    this.numTasks = new AtomicLong();
    this.totalRunTime = new AtomicLong();
  }
  
  public void start() {
    for (int i = 0; i < Runtime.getRuntime().availableProcessors(); ++i) {
      Thread t = new WorkerThread(this);
      this.threads.add(t);
      t.start();
    }
    
    this.startLogging();
    
    this.state = State.RUNNING;
  }
  
  private void startLogging() {
    this.numTasks.set(0);
    this.totalRunTime.set(0);
  }

  public void stop() {
    this.state = State.STOPPED;
    
    for (Thread t : this.threads) {
      t.interrupt();
    }
    
    System.out.println(this.numTasks + " tasks executed with a combined time of " + this.totalRunTime + " nanoseconds.");
    
    this.threads.clear();
    this.tasks.clear();
    this.pendingTasks.clear();
  }
  
  public void slice(WorkerThread worker) {
    if (this.state == State.STOPPED) {
      return;
    }

    Callable<Boolean> t = this.tasks.poll();
    while (t != null && !this.idleThreads.isEmpty()) {
      WorkerThread w = this.idleThreads.poll();
      
      if (w != null) {
        w.setPriority(Thread.NORM_PRIORITY + 1);
        w.call(t);
  
        t = this.tasks.poll();
      }
    }
    
    if (t != null) {
      worker.call(t);
    } else if (this.idleThreads.size() + 1 < this.threads.size()) {
      worker.setPriority(Thread.MIN_PRIORITY);
      this.idleThreads.add(worker);
    }
  }

  public void call(Callable<Boolean> c) {
    if (this.state == State.RUNNING) {
      this.tasks.add(c);
      
      if (c instanceof Behaviour<?>) {
        this.numTasks.incrementAndGet();
        this.totalRunTime.addAndGet(((Behaviour<?>) c).getLastRunTime());
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void handleEvent(Event event) {
    if (event instanceof GenericEvent) {
      if (event.value == GenericEvent.Message.NEW_BEHAVIOUR) {
        this.call((Callable<Boolean>) event.getSource());
      }
    }
  }

  @Override
  public int compareTo(Object o) {
    return this.hashCode() - o.hashCode();
  }
}
