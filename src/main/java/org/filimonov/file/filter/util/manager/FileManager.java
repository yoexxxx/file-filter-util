package org.filimonov.file.filter.util.manager;

import lombok.Getter;
import org.filimonov.file.filter.util.config.AppConfig;

import java.io.File;

@Getter
public class FileManager {

  private final AppConfig appConfig;
  private final File stringFile;
  private final File longFile;
  private final File doubleFile;

  public FileManager(AppConfig appConfig) {
    this.appConfig = appConfig;

    File outputDirectory = appConfig.getOutputDirectory();
    String prefix = appConfig.getPrefix();

    this.stringFile = new File(outputDirectory, prefix + "strings.txt");
    this.longFile = new File(outputDirectory, prefix + "integers.txt");
    this.doubleFile = new File(outputDirectory, prefix + "floats.txt");
  }
}
