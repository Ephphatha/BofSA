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
  
  protected Queue<Behaviour<?>> tasks;
  protected List<Behaviour<?>> waitingTasks;
  protected Lock waitingLock;
  protected Queue<Behaviour<?>> unsortedTasks;
  
  protected Logger logger;
  
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
    
    this.tasks = new ConcurrentLinkedQueue<Behaviour<?>>();
    this.waitingTasks = new LinkedList<Behaviour<?>>();
    this.waitingLock = new ReentrantLock();
    this.unsortedTasks = new ConcurrentLinkedQueue<Behaviour<?>>();
    
    this.state = State.STOPPED;
    
    this.mode = Mode.ORDERED_PRECOMPUTE;
  }
  
  public void start(Mode scheduleMode, int maxThreads, Logger.Mode logMode) {
    this.mode = scheduleMode;
    
    int numWorkers = Math.max(maxThreads - 1, 1);
    
    for (int i = 0; i < numWorkers; ++i) {
      WorkerThread w = new WorkerThread(this);
      w.getLogger().setLogMode(logMode);
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
    
    Queue<Logger> buffer = new LinkedList<Logger>();
    
    for (Thread t : this.threads) {
      t.interrupt();
    }

    for (Thread t : this.threads) {
      try {
        t.join(25);
      } catch (InterruptedException e) {
        //Goggles
      } finally {
        if (t instanceof WorkerThread) {
          WorkerThread w = (WorkerThread) t;
          buffer.offer(w.getLogger());
        }
      }
    }

    this.threads.clear();
    
    Queue<Logger> offBuffer = new LinkedList<Logger>();
    
    do {
      while (!offBuffer.isEmpty()) {
        buffer.offer(offBuffer.poll());
      }
      
      while (!buffer.isEmpty()) {
        Logger a = buffer.poll();
        Logger b = buffer.poll();
        
        a.merge(b);
        
        offBuffer.offer(a);
      }
    } while (offBuffer.size() > 1);
    
    this.logger.merge(offBuffer.poll());
    
    this.tasks.clear();
  }
  
  public void slice(WorkerThread worker) {
    if (this.state == State.STOPPED) {
      return;
    }
    
    Behaviour<?> t = this.getNextTask();

    while (t != null && !this.idleThreads.isEmpty()) {
      WorkerThread w = this.idleThreads.poll();
      
      if (w != null) {
        w.setPriority(Thread.NORM_PRIORITY /*+ 1*/);
        w.call(t);
  
        t = this.getNextTask();
      }
    }

    if (t != null) {
      worker.call(t);
    } else if (this.idleThreads.size() + 1 < this.threads.size()) {
      worker.setPriority(Thread.MIN_PRIORITY);
      this.idleThreads.add(worker);
    }
  }

  public int getActiveCount() {
    return this.threads.size() - this.idleThreads.size();
  }
  
  protected Behaviour<?> getNextTask() {
    Behaviour<?> t = this.tasks.poll();
    
    switch (this.mode) {
    case UNORDERED:
      break;
      
    case ORDERED_RETRY:
      while (t != null && !t.isReady()) {
        this.tasks.add(t);
        
        if (this.logger != null) {
          this.logger.taskRetried(t.getClass().getSimpleName());
        }
        
        t = this.tasks.poll();
      }
      break;
      
    case ORDERED_PRECOMPUTE:
      if (this.waitingLock.tryLock()) {
        try {
          while (!this.unsortedTasks.isEmpty()) {
            Behaviour<?> b = this.unsortedTasks.poll();
            
            if (b != null) {
              this.waitingTasks.add(b);
            }
          }
          
          for (int i = this.waitingTasks.size() - 1; i >= 0; --i) {
            if (this.waitingTasks.get(i).isReady()) {
              Behaviour<?> b = this.waitingTasks.remove(i);
              
              this.tasks.add(b);
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
      if (c instanceof Behaviour<?>) {
        Behaviour<?> b = (Behaviour<?>) c;
        
        switch (this.mode) {
        case UNORDERED:
        case ORDERED_RETRY:
          this.tasks.add(b);
          break;
          
        case ORDERED_PRECOMPUTE:
          if (b.isReady()) {
            this.tasks.add(b);
          } else {
            if (this.logger != null) {
              this.logger.taskWaited(b.getClass().getSimpleName());
            }
            
            this.unsortedTasks.add(b);
          }
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

  public void setLogger(Logger logger) {
    this.logger = logger;
  }
}
