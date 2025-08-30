package org.filimonov.file.filter.util.queue;

import lombok.Getter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Getter
public class CommonQueue {
  private final BlockingQueue<String> commonStrings = new LinkedBlockingQueue<>();
  private final BlockingQueue<Long> commonLongs = new LinkedBlockingQueue<>();
  private final BlockingQueue<Double> commonDoubles = new LinkedBlockingQueue<>();
}