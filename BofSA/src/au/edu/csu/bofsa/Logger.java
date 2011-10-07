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
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author ephphatha
 *
 */
public class Logger implements Runnable {

  public static class Task implements Comparable<Task> {
    protected final String name;
    protected final Long startTime;
    protected final Long duration;
    
    public Task(String name, Long startTime, Long duration) {
      this.name = name;
      this.startTime = startTime;
      this.duration = duration;
    }
    
    public String toString() {
      return "\"" + this.name + "\"," + this.startTime + "," + this.duration + "\n";
    }

    @Override
    public int compareTo(Task o) {
      return (int) (this.startTime - o.startTime);
    }
  }
  
  public static class TaskStats {
    public final AtomicLong executionCount;
    public final AtomicLong retryCount;
    public final AtomicLong waitCount;
    public final AtomicLong totalRuntime;
    public final AtomicReference<Double> meanRuntime;
    public final AtomicReference<Double> sumSquaresRuntime;
    
    public TaskStats() {
      this.executionCount = new AtomicLong(0);
      this.retryCount = new AtomicLong(0);
      this.waitCount = new AtomicLong(0);
      this.totalRuntime = new AtomicLong(0);
      this.meanRuntime = new AtomicReference<Double>(new Double(0));
      this.sumSquaresRuntime = new AtomicReference<Double>(new Double(0));
    }
    
    public void reset() {
      this.executionCount.set(0);
      this.retryCount.set(0);
      this.waitCount.set(0);
      this.totalRuntime.set(0);
    }

    public void merge(TaskStats rhs) {
      double xbar_a = this.meanRuntime.get();
      double xbar_b = rhs.meanRuntime.get();
      
      long n_a = this.executionCount.get();
      long n_b = rhs.executionCount.get();
      
      if (n_b == 0) {
        //use this values;
      } else if (n_a == 0) {
        //use rhs values;
        this.meanRuntime.set(xbar_b);
        this.sumSquaresRuntime.set(rhs.sumSquaresRuntime.get());
      } else {
        double sigma = xbar_b - xbar_a;
        
        // \bar{x}_X = \frac{n_A\bar{x}_A + n_B\bar{x}_B}{n_A+n_B}
        this.meanRuntime.set((n_a * xbar_a + n_b * xbar_b)/(n_a + n_b));
        
        this.sumSquaresRuntime.set(this.sumSquaresRuntime.get() + rhs.sumSquaresRuntime.get() + Math.pow(sigma, 2) * (n_a*n_b)/(n_a+n_b));
      }
      
      this.executionCount.set(n_a + n_b);
      this.retryCount.addAndGet(rhs.retryCount.get());
      this.waitCount.addAndGet(rhs.waitCount.get());
      this.totalRuntime.addAndGet(rhs.totalRuntime.get());
    }
  }
  
  public static enum Mode {
    DETAILED,
    SAMPLE,
    BASIC
  }
  
  private Mode mode;
  private FileWriter detailFile;
  private SimpleDateFormat df;
  private Date startDate;
  private String description;
  
  private Queue<Task> pendingMessages;
  private Map<String, TaskStats> taskStats;
  private int numWorkers;
  private long startTime;
  
  public Logger() {
    this.pendingMessages = new ConcurrentLinkedQueue<Task>();
    this.taskStats = new ConcurrentHashMap<String, TaskStats>();
    
    this.df = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
    
    this.mode = Mode.BASIC;
  }

  public void startLogging(String description) {
    this.startLogging(description, -1);
  }
  
  public void startLogging(String description, int workerThreads) {
    this.pendingMessages.clear();
    
    this.description = description;
    
    this.numWorkers = workerThreads;
    
    for (TaskStats ms : this.taskStats.values()) {
      ms.reset();
    }
    
    this.startDate = Calendar.getInstance().getTime();
    this.startTime = System.currentTimeMillis();
  }
  
  public void setLogMode(Mode level) {
    this.mode = level;
  }
  
  public void merge(Logger rhs) {
    if (rhs == null) {
      return;
    }
    
    List<Task> temp = new LinkedList<Task>();
    
    while (!this.pendingMessages.isEmpty()) {
      temp.add(this.pendingMessages.poll());
    }
    
    while (!rhs.pendingMessages.isEmpty()) {
      temp.add(rhs.pendingMessages.poll());
    }
    
    for (Map.Entry<String, TaskStats> e : rhs.taskStats.entrySet()) {
      if (this.taskStats.containsKey(e.getKey())) {
        this.taskStats.get(e.getKey()).merge(e.getValue());
      } else {
        this.taskStats.put(e.getKey(), e.getValue());
      }
    }
    rhs.taskStats.clear();
    
    Collections.sort(temp);
    
    this.pendingMessages.addAll(temp);
  }
  
  public void stopLogging() {
    this.flush();
    
    float duration = (System.currentTimeMillis() - this.startTime) / 1000.0f;

    long numTasks = 0;
    long totalRuntime = 0;
    long numRetries = 0;
    long numWaits = 0;
    
    try {
      FileWriter file;
      
      if (this.mode == Mode.SAMPLE) {
        file = this.getFile("_STATS.log");
  
        if (file != null) {
          try {
            file.write(
                "Task name," +
                "Times executed," +
                "Times retried," +
                "Times not ready," +
                "Total runtime (ns)," +
                "Average runtime (ns)," +
                "Standard Deviation (estimated)" +
                "\n");

            for (Map.Entry<String, TaskStats> e : this.taskStats.entrySet()) {
              TaskStats ms = e.getValue();
              
              file.write(
                  e.getKey() + "," +
                  ms.executionCount + "," +
                  ms.retryCount + "," +
                  ms.waitCount + "," +
                  ms.totalRuntime + "," +
                  ms.meanRuntime + "," +
                  Double.toString(Math.sqrt(ms.sumSquaresRuntime.get() / ms.executionCount.get())) +
                  "\n");
            }
          } finally {
            file.flush();
            file.close();
          }
        }
      }

      file = this.getFile(".log");
      
      if (file != null) {
        try {

          StringBuilder headings = new StringBuilder();
          StringBuilder totals = new StringBuilder();

          for (Map.Entry<String, TaskStats> e : this.taskStats.entrySet()) {
            TaskStats ms = e.getValue();
            
            headings.append(e.getKey() + ",");
            totals.append(ms.executionCount + ",");
            
            numTasks += ms.executionCount.get();
            totalRuntime += ms.totalRuntime.get();
            numRetries += ms.retryCount.get();
            numWaits += ms.waitCount.get();
          }
          
          file.write(
              "Number of worker threads," +
              "Total user time (seconds)," +
              "Total tasks executed," +
              "Tasks not ready when retrieved," +
              "Tasks not ready for immediate rerun," +
              "Combined runtime (ns)," +
              headings.toString() +
              "\n");
          
          file.write(
              this.numWorkers + "," +
              duration + "," +
              numTasks + "," +
              numRetries + "," +
              numWaits + "," +
              totalRuntime + "," +
              totals.toString() +
              "\n");
        } finally {
          file.flush();
          file.close();
        }
      }
    } catch (IOException e) {
      //Goggles
    }
    
    this.detailFile = null;
  }
  
  public void taskRun(Task m) {
    this.taskRun(m.name);
    
    if (this.mode == Mode.DETAILED || (this.mode == Mode.SAMPLE && Math.random() >= 0.99)) {
      this.pendingMessages.add(m);
    }

    TaskStats ms = this.taskStats.get(m.name);
    
    if (ms == null) {
      synchronized (this.taskStats) {
        if (!this.taskStats.containsKey(m.name)) {
          ms = new TaskStats();
          this.taskStats.put(m.name, ms);
        }
      }
    }
  
    if (this.mode == Mode.SAMPLE) {
      ms.totalRuntime.addAndGet(m.duration);
      
      double xbar_n1 = ms.meanRuntime.get();
      
      double xbar_n = xbar_n1 + (m.duration - xbar_n1) / ms.executionCount.get();
      
      ms.meanRuntime.set(xbar_n);
      ms.sumSquaresRuntime.set(ms.sumSquaresRuntime.get() + (m.duration - xbar_n) * (m.duration - xbar_n1));
    }
  }
  
  public void taskRun(String m) {
    TaskStats ms = this.taskStats.get(m);
    
    if (ms == null) {
      synchronized (this.taskStats) {
        if (!this.taskStats.containsKey(m)) {
          ms = new TaskStats();
          this.taskStats.put(m, ms);
        }
      }
    }
  
    ms.executionCount.incrementAndGet();
  }

  public void taskWaited(String m) {
    TaskStats ms = this.taskStats.get(m);
    
    if (ms == null) {
      synchronized (this.taskStats) {
        if (!this.taskStats.containsKey(m)) {
          ms = new TaskStats();
          this.taskStats.put(m, ms);
        }
      }
    }
  
    ms.waitCount.incrementAndGet();
  }

  public void taskRetried(String m) {
    TaskStats ms = this.taskStats.get(m);
    
    if (ms == null) {
      synchronized (this.taskStats) {
        if (!this.taskStats.containsKey(m)) {
          ms = new TaskStats();
          this.taskStats.put(m, ms);
        }
      }
    }
  
    ms.retryCount.incrementAndGet();
  }
  
  public void flush() {
    while (!this.pendingMessages.isEmpty()) {
      Task m = this.pendingMessages.poll();
      if (m != null) {
        if (this.detailFile == null) {
          this.detailFile = this.getFile(".csv");
          
          if (this.detailFile != null) {
            try {
              this.detailFile.write("Name,Start Time,Runtime\n");
            } catch (IOException e) {
              //Goggles
            }
          }
        }
        
        try {
          synchronized (this.detailFile) {
            this.detailFile.write(m.toString());
          }
        } catch (IOException e) {
          //Goggles
        }
      }
    }
    
    if (this.detailFile != null) {
      try {
        this.detailFile.flush();
      } catch (IOException e) {
        //Goggles
      }
    }
  }
  
  private FileWriter getFile(String extension) {
    try {
      return new FileWriter(
          this.df.format(this.startDate) +
          (this.description != null ? ("_" + this.description) : "" ) +
          extension);
    } catch (IOException e) {
      return null;
    }
  }
  
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      this.flush();
      Thread.yield();
    }
  }
}