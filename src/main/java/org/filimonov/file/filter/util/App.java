package org.filimonov.file.filter.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.filimonov.file.filter.util.config.AppConfig;
import org.filimonov.file.filter.util.io.writer.DoubleToFileWriter;
import org.filimonov.file.filter.util.manager.FileManager;
import org.filimonov.file.filter.util.queue.CommonQueue;
import org.filimonov.file.filter.util.manager.StatsManager;
import org.filimonov.file.filter.util.io.reader.FileToQueueReader;
import org.filimonov.file.filter.util.io.writer.LongToFileWriter;
import org.filimonov.file.filter.util.io.writer.StringToFileWriter;
import org.filimonov.file.filter.util.queue.Coordinator;
import org.filimonov.file.filter.util.queue.ReaderQueue;
import picocli.CommandLine;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
public class App {

  public static void main(String[] args) {
    log.info("Program started successfully!");

    var appConfig = new AppConfig();
    var cmdLine = new CommandLine(appConfig);

    int exitCode = cmdLine.execute(args);
    if (exitCode != 0) {
      log.error("Program terminated: [exitCode={}]", exitCode);
      System.exit(exitCode);
    }
    if (CollectionUtils.isEmpty(appConfig.getInputFiles())) {
      log.error("No input files provided. At least one is required");
      System.exit(1);
    }

    var statsManager = new StatsManager(appConfig);
    var fileManager = new FileManager(appConfig);
    var commonQueue = new CommonQueue();

    try (var executorService = Executors.newVirtualThreadPerTaskExecutor()) {
      List<File> inputFiles = appConfig.getValidInputFiles();
      var fileSize = inputFiles.size();
      var readersFinished = new CountDownLatch(1);
      var readerQueues = Stream.generate(ReaderQueue::new)
                               .limit(fileSize)
                               .toList();

      executorService.execute(Coordinator.builder()
                                         .readerQueues(readerQueues)
                                         .commonQueue(commonQueue)
                                         .readersFinished(readersFinished)
                                         .build());

      for (int i = 0; i < fileSize; i++) {
        executorService.execute(FileToQueueReader.builder()
                                                 .readerQueue(readerQueues.get(i))
                                                 .inputFile(inputFiles.get(i))
                                                 .build());
      }

      executeWriters(executorService, appConfig, readersFinished, commonQueue, fileManager, statsManager);

      executorService.shutdown();

      if (!executorService.awaitTermination(2, TimeUnit.MINUTES)) {
        log.warn("Not all tasks finished, forcing shutdown");
        executorService.shutdownNow();
      }
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      log.error("Thread interrupted, error", exception);
    }
    log.debug("All threads terminated");

    statsManager.printStats();
    log.info("Program terminated!");

  }

  private static void executeWriters(ExecutorService executorService,
                                     AppConfig appConfig,
                                     CountDownLatch readersFinished,
                                     CommonQueue commonQueue,
                                     FileManager fileManager,
                                     StatsManager statsManager) {
    executorService.execute(StringToFileWriter.builder()
                                              .appConfig(appConfig)
                                              .readersFinished(readersFinished)
                                              .queue(commonQueue.getCommonStrings())
                                              .file(fileManager.getStringFile())
                                              .statsManager(statsManager)
                                              .build());
    executorService.execute(LongToFileWriter.builder()
                                            .appConfig(appConfig)
                                            .readersFinished(readersFinished)
                                            .queue(commonQueue.getCommonLongs())
                                            .file(fileManager.getLongFile())
                                            .statsManager(statsManager)
                                            .build());
    executorService.execute(DoubleToFileWriter.builder()
                                              .appConfig(appConfig)
                                              .readersFinished(readersFinished)
                                              .queue(commonQueue.getCommonDoubles())
                                              .file(fileManager.getDoubleFile())
                                              .statsManager(statsManager)
                                              .build());
  }
}