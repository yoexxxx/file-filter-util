package org.filimonov.file.filter.util.io.writer;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class LongToFileWriter extends QueueToFileWriter<Long> {

  @Override
  protected String convertToString(Long dataFromQueue) {
    return String.valueOf(dataFromQueue);
  }

  @Override
  protected void recordStatistics(Long dataFromQueue) {
    statsManager.recordLong(dataFromQueue);
  }
}
