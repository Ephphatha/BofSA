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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * @author ephphatha
 *
 */
public class Scheduler implements Executor {

  protected Queue<WorkerThread> idleThreads;
  
  protected Queue<Runnable> tasks;
  
  public Scheduler() {
    this.idleThreads = new ConcurrentLinkedQueue<WorkerThread>();
    this.tasks = new PriorityBlockingQueue<Runnable>();
  }
  
  public void slice(WorkerThread worker) {

    Runnable t = this.tasks.poll();
    while (t != null && !this.idleThreads.isEmpty()) {
      WorkerThread w = this.idleThreads.poll();
      
      if (w != null) {
        w.setPriority(Thread.NORM_PRIORITY + 1);
        w.execute(t);
  
        t = this.tasks.poll();
      }
    }
    
    if (t != null) {
      worker.execute(t);
    } else {
      worker.setPriority(Thread.MIN_PRIORITY);
      this.idleThreads.add(worker);
    }
  }

  @Override
  public void execute(Runnable r) {
    this.tasks.add(r);
  }
}
