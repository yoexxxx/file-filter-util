package org.filimonov.file.filter.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.apache.commons.io.FileUtils;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

class AppTest {

  private static final String BASE_PATH = "src/test/resources/files";
  private static final List<String> EXPECTED_INTEGERS = Arrays.asList("45", "100500", "1234567890123456789");
  private static final List<String> EXPECTED_FLOATS = Arrays.asList("3.1415", "-0.001", "1.528535047E-25");
  private static final List<String> EXPECTED_STRINGS = Arrays.asList("Lorem ipsum dolor sit amet",
                                                                     "Пример",
                                                                     "consectetur adipiscing",
                                                                     "тестовое задание",
                                                                     "Нормальная форма числа с плавающей запятой",
                                                                     "Long");

  @TempDir
  Path tempDir;

  @Test
  void testFileFilterUtil() throws Exception {
    Path inputFile1 = tempDir.resolve(String.join("/", BASE_PATH, "in1.txt"));
    Path inputFile2 = tempDir.resolve(String.join("/", BASE_PATH, "in2.txt"));
    FileUtils.copyFile(new File(String.join("/", BASE_PATH, "in1.txt")), inputFile1.toFile());
    FileUtils.copyFile(new File(String.join("/", BASE_PATH, "in2.txt")), inputFile2.toFile());

    Path outputDir = tempDir.resolve(String.join("/", BASE_PATH, "results"));
    Files.createDirectory(outputDir);

    App.main(new String[] {
        "-s",
        "-o", outputDir.toString(),
        "-p", "sample-",
        inputFile1.toString(),
        inputFile2.toString()
    });

    Path stringsFile = outputDir.resolve("sample-strings.txt");
    Path integersFile = outputDir.resolve("sample-integers.txt");
    Path floatsFile = outputDir.resolve("sample-floats.txt");
    List<String> stringsOutput = Files.readAllLines(stringsFile, StandardCharsets.UTF_8);
    List<String> integersOutput = Files.readAllLines(integersFile, StandardCharsets.UTF_8);
    List<String> floatsOutput = Files.readAllLines(floatsFile, StandardCharsets.UTF_8);

    assertLinesMatch(EXPECTED_STRINGS, stringsOutput);
    assertLinesMatch(EXPECTED_INTEGERS, integersOutput);
    assertLinesMatch(EXPECTED_FLOATS, floatsOutput);
  }
}
