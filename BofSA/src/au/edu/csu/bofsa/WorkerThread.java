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
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author ephphatha
 *
 */
public class WorkerThread extends Thread implements Caller<Boolean> {

  protected Scheduler scheduler;
  
  protected Queue<Callable<Boolean>> tasks;
  
  /**
   * 
   */
  public WorkerThread(Scheduler s) {
    this.scheduler = s;
    
    this.tasks = new ConcurrentLinkedQueue<Callable<Boolean>>();
  }

  public void run() {
    while (!Thread.interrupted()) {
      if (!this.tasks.isEmpty()) {
        do {
          Callable<Boolean> c = this.tasks.poll();
          try {
            if (c.call()) {
              this.scheduler.call(c);
            }
          } catch (InterruptedException e) {
            break;
          } catch (Exception e) {
            e.printStackTrace();
          }
        } while (!this.tasks.isEmpty());
      }
      
      this.scheduler.slice(this);
      
      Thread.yield();
    }
  }

  public void call(Callable<Boolean> r) {
    this.tasks.add(r);
  }
}
