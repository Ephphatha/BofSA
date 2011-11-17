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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
  protected List<Callable<Boolean>> waitingTasks;
  protected Lock waitingLock;
  protected Queue<Callable<Boolean>> unsortedTasks;
  
  protected AtomicInteger numIdle;
  
  protected static enum State {
    RUNNING,
    STOPPED
  }
  
  protected State state;
  
  protected static enum Mode {
    UNORDERED,
    ORDERED_RETRY,
    ORDERED_PRECOMPUTE
  }
  
  protected Mode mode;
  
  public Scheduler() {
    this.threads = new LinkedList<Thread>();
    this.idleThreads = new ConcurrentLinkedQueue<WorkerThread>();
    
    this.tasks = new ConcurrentLinkedQueue<Callable<Boolean>>();
    this.waitingTasks = new LinkedList<Callable<Boolean>>();
    this.waitingLock = new ReentrantLock();
    this.unsortedTasks = new ConcurrentLinkedQueue<Callable<Boolean>>();
    
    this.numIdle = new AtomicInteger();
    
    this.state = State.STOPPED;
    
    this.mode = Mode.ORDERED_PRECOMPUTE;
  }
  
  public void start(Mode scheduleMode, int workers) {
    this.mode = scheduleMode;
    
    this.numIdle.set(0);
    
    int numWorkers = Math.max(workers, 1);
    
    for (int i = 0; i < numWorkers; ++i) {
      WorkerThread w = new WorkerThread(this);
      this.threads.add(w);
    }
    
    for (Thread t : this.threads) {
      t.start();
    }
    
    this.state = State.RUNNING;
  }
  
  public int numThreads() {
    return this.threads.size();
  }
  
  public void stop() {
    this.state = State.STOPPED;
    
    for (Thread t : this.threads) {
      t.interrupt();
    }

    for (Thread t : this.threads) {
      try {
        t.join(25);
      } catch (InterruptedException e) {
        //Goggles
      }
    }

    this.threads.clear();
    
    this.tasks.clear();
    
    this.numIdle.set(0);
  }
  
  public void slice(WorkerThread worker) {
    if (this.state == State.STOPPED) {
      return;
    }
    
    Callable<Boolean> t = this.getNextTask();

    while (t != null && !this.idleThreads.isEmpty()) {
      WorkerThread w = this.idleThreads.poll();
      
      if (w != null) {
        this.numIdle.decrementAndGet();
        w.setPriority(Thread.NORM_PRIORITY /*+ 1*/);
        w.call(t);
        
        if (w == worker) {
          return;
        }
  
        t = this.getNextTask();
      }
    }

    if (t != null) {
      worker.call(t);
    } else {
      if (worker.getPriority() != Thread.MIN_PRIORITY) {
        worker.setPriority(Thread.MIN_PRIORITY);
        this.idleThreads.add(worker);
        this.numIdle.incrementAndGet();
      }
    }
  }

  public int getActiveCount() {
    return this.threads.size() - this.numIdle.get();
  }
  
  protected Callable<Boolean> getNextTask() {
    Callable<Boolean> t = this.tasks.poll();
    
    switch (this.mode) {
    case UNORDERED:
      break;
      
    case ORDERED_RETRY:
      while (t != null && t instanceof Behaviour<?> && !((Behaviour<?>) t).isReady()) {
        this.tasks.add(t);
        
        t = this.tasks.poll();
      }
      break;
      
    case ORDERED_PRECOMPUTE:
      if (this.waitingLock.tryLock()) {
        try {
          while (!this.unsortedTasks.isEmpty()) {
            Callable<Boolean> b = this.unsortedTasks.poll();
            
            if (b != null) {
              this.waitingTasks.add(b);
            }
          }
          
          for (int i = this.waitingTasks.size() - 1; i >= 0; --i) {
            Callable<Boolean> c = this.waitingTasks.get(i);
            
            if (c instanceof Behaviour<?>) {
              if (((Behaviour<?>) c).isReady()) {
                 this.waitingTasks.remove(i);
                 this.tasks.add(c);
              }
            } else {
              this.tasks.add(c);
            }
          }
        } finally {
          this.waitingLock.unlock();
          
          if (t == null) {
            t = this.tasks.poll();
          }
        }
      }
    }
    
    return t;
  }
  
  public void call(Callable<Boolean> c) {
    if (this.state == State.RUNNING) {
      switch (this.mode) {
      case UNORDERED:
      case ORDERED_RETRY:
        this.tasks.offer(c);
        break;
        
      case ORDERED_PRECOMPUTE:
        if (c instanceof Behaviour<?>) {
          if (((Behaviour<?>) c).isReady()) {
            this.tasks.offer(c);
          } else {
            this.unsortedTasks.offer(c);
          }
        } else {
          this.tasks.offer(c);
        }
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

  public boolean isBusy() {
    if (this.state == State.STOPPED) {
      return false;
    }
    
    if (this.tasks.isEmpty()) {
      if (this.getActiveCount() > 0) {
        return true;
      } else {
        return false;
      }
    }
    
    return true;
  }
}
