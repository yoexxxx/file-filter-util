package org.filimonov.file.filter.util.queue;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
@RequiredArgsConstructor
public class Coordinator implements Runnable {

  private final List<ReaderQueue> readerQueues;
  private final CommonQueue commonQueue;
  private final CountDownLatch readersFinished;

  @Override
  public void run() {
    try {
      for (int i = 0; i < readerQueues.size(); i++) {
        ReaderQueue readerQueue = readerQueues.get(i);

        while (!readerQueue.isFinished()) {
          var longFromFile = readerQueue.getLongs().poll();
          if (longFromFile != null) {
            commonQueue.getCommonLongs().put(longFromFile);
          }

          var doubleFromFile = readerQueue.getDoubles().poll();
          if (doubleFromFile != null) {
            commonQueue.getCommonDoubles().put(doubleFromFile);
          }

          var stringFromFile = readerQueue.getStrings().poll();
          if (stringFromFile != null) {
            commonQueue.getCommonStrings().put(stringFromFile);
          }
        }
        log.debug("Coordinator: reader {} finished", i);
      }
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      log.error("Coordinator interrupted, error: ", exception);
    } finally {
      readersFinished.countDown();
    }
  }
}