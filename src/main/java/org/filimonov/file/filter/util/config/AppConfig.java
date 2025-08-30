package org.filimonov.file.filter.util.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine;

import java.io.File;
import java.util.List;

@Slf4j
@Getter
@Setter
@CommandLine.Command(name = "filter-util", description = "Утилита фильтрации содержимого файлов")
public class AppConfig implements Runnable {

  @CommandLine.Option(names = "-o", description = "Directory for output files")
  private File outputDirectory = new File(".");

  @CommandLine.Option(names = "-p", description = "Prefix for output files names")
  private String prefix = StringUtils.EMPTY;

  @CommandLine.Option(names = "-a", description = "Append mode")
  private boolean append = false;

  @CommandLine.Option(names = "-s", description = "Short stats")
  private boolean shortStats;

  @CommandLine.Option(names = "-f", description = "Full stats")
  private boolean fullStats;

  @CommandLine.Parameters(arity = "1..*", paramLabel = "FILES", description = "Input files")
  private List<File> inputFiles;

  public List<File> getValidInputFiles() {
    return inputFiles.stream()
                     .filter(file -> {
                       if (!file.getName().endsWith(".txt")) {
                         log.warn("File [{}] skipped: not a .txt file", file.getName());
                         return false;
                       }
                       return true;
                     })
                     .toList();

  }

  @Override
  public void run() {

  }
}
