package org.filimonov.file.filter.util.io.reader;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.filimonov.file.filter.util.queue.ReaderQueue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

@Slf4j
@Builder
@RequiredArgsConstructor
public class FileToQueueReader implements Runnable {

  private final ReaderQueue readerQueue;
  private final File inputFile;

  @Override
  public void run() {
    int stringCount = 0;
    int longCount = 0;
    int doubleCount = 0;

    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile))) {
      log.info("Started reading file: [{}]", inputFile.getName());
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        try {
          if (line.isBlank()) {
            log.debug("Line skipped, because it was empty");
            continue;
          }
          if (!NumberUtils.isCreatable(line)) {
            readerQueue.getStrings().put(line);
            stringCount++;
            continue;
          }

          switch (NumberUtils.createNumber(line)) {
          case Integer integerFromFile -> {
            readerQueue.getLongs().put(integerFromFile.longValue());
            longCount++;
          }
          case Long longFromFile -> {
            readerQueue.getLongs().put(longFromFile);
            longCount++;
          }
          case Float ignored -> {
            readerQueue.getDoubles().put(Double.parseDouble(line));
            doubleCount++;
          }
          case Double doubleFromFile -> {
            readerQueue.getDoubles().put(doubleFromFile);
            doubleCount++;
          }
          default -> {
            readerQueue.getStrings().put(line);
            stringCount++;
          }
          }

        } catch (InterruptedException exception) {
          Thread.currentThread().interrupt();
          log.error("Reader interrupted for file: [{}], error: ", inputFile.getName(), exception);
        } catch (Exception exception) {
          log.error("Line: [{}] skipped due to error: ", line, exception);
        }
      }

      log.info("File [{}] was successfully read, total: Long - [{}], Double - [{}], String - [{}]",
               inputFile, longCount, doubleCount, stringCount);
    } catch (Exception exception) {
      log.error("Reader error for file: [{}], error: ", inputFile.getName(), exception);
    } finally {
      readerQueue.setIsFinished();
    }
  }
}
