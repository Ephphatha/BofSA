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

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author ephphatha
 *
 */
public class Logger extends Thread {

  public static class Message {
    protected final String name;
    protected final Long startTime;
    protected final Long duration;
    
    public Message(String name, Long startTime, Long duration) {
      this.name = name;
      this.startTime = startTime;
      this.duration = duration;
    }
    
    public String toString() {
      return "\"" + this.name + "\"," + this.startTime + "," + this.duration + "\n";
    }
  }
  
  private FileWriter file;
  private Queue<Message> pendingMessages;
  private boolean running;
  private SimpleDateFormat df;
  
  public Logger() {
    this.pendingMessages = new ConcurrentLinkedQueue<Message>();
    
    this.running = false;
    
    this.df = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
    
    this.setDaemon(true);
  }

  public boolean startLogging() {
    try {
      this.pendingMessages.clear();
      this.file = new FileWriter(this.df.format(Calendar.getInstance().getTime()) + ".csv");
      this.file.write("Section,Start Time,Duration\n");
      this.running = true;
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    return this.running;
  }
  
  public void stopLogging() {
    this.running = false;
    
    try {
      this.file.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    this.file = null;
  }
  
  public void printMessage(Message m) {
    if (this.running) {
      this.pendingMessages.add(m);
    }
  }
  
  public void run() {
    while (!this.isInterrupted()) {
      if (!this.pendingMessages.isEmpty()) {
        Message m = this.pendingMessages.poll();
        if (m != null && this.file != null) {
          try {
            this.file.write(m.toString());
          } catch (IOException e) {
            //Goggles
            System.out.println(m.toString());
          }
        }
      }
    }
  }
}