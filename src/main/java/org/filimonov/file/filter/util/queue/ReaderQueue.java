package org.filimonov.file.filter.util.queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.Getter;

@Getter
public class ReaderQueue {

  private static final int CAPACITY = 1000;

  private final BlockingQueue<String> strings = new ArrayBlockingQueue<>(CAPACITY);
  private final BlockingQueue<Long> longs = new ArrayBlockingQueue<>(CAPACITY);
  private final BlockingQueue<Double> doubles = new ArrayBlockingQueue<>(CAPACITY);
  private final AtomicBoolean isFinished = new AtomicBoolean();

  public void setIsFinished() {
    isFinished.set(true);
  }

  public boolean isFinished() {
    return isFinished.get() && strings.isEmpty() && longs.isEmpty() && doubles.isEmpty();
  }
}
