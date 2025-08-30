package org.filimonov.file.filter.util.io.writer;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class DoubleToFileWriter extends QueueToFileWriter<Double> {

  @Override
  protected String convertToString(Double dataFromQueue) {
    return String.valueOf(dataFromQueue);
  }

  @Override
  protected void recordStatistics(Double dataFromQueue) {
    statsManager.recordDouble(dataFromQueue);
  }
}