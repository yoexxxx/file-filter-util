package org.filimonov.file.filter.util.io.writer;

import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.filimonov.file.filter.util.config.AppConfig;
import org.filimonov.file.filter.util.manager.StatsManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@SuperBuilder
@RequiredArgsConstructor
public abstract class QueueToFileWriter<T> implements Runnable {

  private static final int POLL_INTERVAL = 100;

  private final AppConfig appConfig;
  private final CountDownLatch readersFinished;

  protected final BlockingQueue<T> queue;
  protected final File file;
  protected final StatsManager statsManager;

  private boolean fileUsed = false;

  @Override
  public void run() {
    try (var writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, appConfig.isAppend()),
                                                                StandardCharsets.UTF_8))) {
      do {
        T dataFromQueue = queue.poll(POLL_INTERVAL, TimeUnit.MILLISECONDS);
        if (dataFromQueue != null) {
          writer.write(convertToString(dataFromQueue));
          writer.newLine();
          fileUsed = true;
          recordStatistics(dataFromQueue);
        }
      }
      while (readersFinished.getCount() != 0 || !queue.isEmpty());

    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      log.error("Writer interrupted for file: [{}], error: ", file.getName(), exception);
    } catch (Exception exception) {
      log.error("Writer error while writing to file: [{}], error: ", file.getName(), exception);
    }

    if (!fileUsed && !appConfig.isAppend() && file.exists() && file.length() == 0) {
      boolean deleted = file.delete();
      log.info("Deleted empty file [{}]: {}", file.getName(), deleted);
    }

    log.info("File [{}] was written successfully", file);
  }

  protected abstract String convertToString(T dataFromQueue);

  protected abstract void recordStatistics(T dataFromQueue);
}
