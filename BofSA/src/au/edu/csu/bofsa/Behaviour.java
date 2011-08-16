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

/**
 * @author ephphatha
 *
 */
public abstract class Behaviour<T extends Signal<? extends Copyable<?>>> implements Runnable {
  
  protected long lastStartTime;
  protected long lastEndTime;
  protected long currentTimeStamp;
  
  protected T signal;

  public Behaviour() {
    this.lastStartTime = System.nanoTime();
    this.recordCurrentTimeStamp(System.nanoTime());
    this.lastEndTime = System.nanoTime();
  }
  
  @Override
  public void run() {
    this.lastStartTime = System.nanoTime();
    
    doRun();
    
    this.lastEndTime = System.nanoTime();
  }
  
  protected void doRun() {
    this.recordCurrentTimeStamp(System.nanoTime());
  }
  
  protected void recordCurrentTimeStamp(long time) {
    this.currentTimeStamp = time;
  }
  
  public long getCurrentTimeStamp() {
    return this.currentTimeStamp;
  }
}
