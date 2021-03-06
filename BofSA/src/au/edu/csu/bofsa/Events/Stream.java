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
package au.edu.csu.bofsa.Events;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;


/**
 * @author ephphatha
 *
 */
public class Stream implements EventSource, EventSink, Comparable<Object> {
  protected Set<EventSink> sinks;
  
  public Stream() {
    this.sinks = new ConcurrentSkipListSet<EventSink>();
  }

  @Override
  public void addSink(EventSink sink) {
    this.sinks.add(sink);
  }

  @Override
  public void removeSink(EventSink sink) {
    this.sinks.remove(sink);
  }

  @Override
  public void handleEvent(Event event) {
    this.notifySinks(event);
  }

  @Override
  public void notifySinks(Event event) {
    for (EventSink s : this.sinks) {
      s.handleEvent(event);
    }
  }

  @Override
  public int compareTo(Object o) {
    return this.hashCode() - o.hashCode();
  }

}
