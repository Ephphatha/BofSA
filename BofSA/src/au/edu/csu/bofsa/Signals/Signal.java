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
package au.edu.csu.bofsa.Signals;

import au.edu.csu.bofsa.Copyable;


/**
 * @author ephphatha
 *
 */
public class Signal<T extends Copyable<T>> implements InputSignal<T> {
  protected T buffer;
  protected Long timeStamp;
  
  public Signal(final T value) {
    this.write(value);
  }
  
  public Signal(final T value, long timeStamp) {
    this.write(value, timeStamp);
  }
  
  public void write(final T newValue) {
    write(newValue, System.nanoTime());
  }
  
  public void write(final T newValue, long timeStamp) {
    this.buffer = newValue.copy();
    this.timeStamp = Long.valueOf(timeStamp);
  }
  
  public final T read() {
    return this.buffer.copy();
  }
  
  public long getTimeStamp() {
    return this.timeStamp;
  }
}
