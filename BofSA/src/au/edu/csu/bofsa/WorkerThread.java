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

/**
 * @author ephphatha
 *
 */
public class WorkerThread extends Thread implements Executor {

  protected Scheduler scheduler;
  
  protected Queue<Runnable> tasks;
  
  /**
   * 
   */
  public WorkerThread(Scheduler s) {
    this.scheduler = s;
    
    this.tasks = new ConcurrentLinkedQueue<Runnable>();
  }

  public void run() {
    while (!Thread.interrupted()) {
      if (!this.tasks.isEmpty()) {
        do {
          try {
            Runnable t = this.tasks.poll();
            t.run();
            this.scheduler.execute(t);
          } finally {
          }
        } while (!this.tasks.isEmpty());
        
        this.scheduler.slice(this);
      }
      
      Thread.yield();
    }
  }

  public void execute(Runnable r) {
    this.tasks.add(r);
  }
}
