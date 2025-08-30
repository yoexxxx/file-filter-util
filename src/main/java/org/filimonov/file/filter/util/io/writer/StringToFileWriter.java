package org.filimonov.file.filter.util.io.writer;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class StringToFileWriter extends QueueToFileWriter<String> {

  @Override
  protected String convertToString(String dataFromQueue) {
    return dataFromQueue;
  }

  @Override
  protected void recordStatistics(String dataFromQueue) {
    statsManager.recordString(dataFromQueue);
  }
}
