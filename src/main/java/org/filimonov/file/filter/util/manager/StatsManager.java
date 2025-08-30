package org.filimonov.file.filter.util.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.filimonov.file.filter.util.config.AppConfig;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

@RequiredArgsConstructor
@Slf4j
public class StatsManager {

  private final AppConfig appConfig;

  private final LongAdder longCount = new LongAdder();
  private final AtomicLong longMinValue = new AtomicLong(Long.MAX_VALUE);
  private final AtomicLong longMaxValue = new AtomicLong(Long.MIN_VALUE);
  private final LongAdder longSum = new LongAdder();

  private final LongAdder doubleCount = new LongAdder();
  private final AtomicReference<Double> doubleMinValue = new AtomicReference<>(Double.MAX_VALUE);
  private final AtomicReference<Double> doubleMaxValue = new AtomicReference<>(Double.NEGATIVE_INFINITY);
  private final DoubleAdder doubleSum = new DoubleAdder();

  private final LongAdder stringCount = new LongAdder();
  private final AtomicLong stringMinLength = new AtomicLong(Long.MAX_VALUE);
  private final AtomicLong stringMaxLength = new AtomicLong(Long.MIN_VALUE);

  public void recordLong(long value) {
    longCount.increment();
    longSum.add(value);

    longMinValue.accumulateAndGet(value, Math::min);
    longMaxValue.accumulateAndGet(value, Math::max);
  }

  public void recordDouble(double value) {
    doubleCount.increment();
    doubleSum.add(value);

    doubleMinValue.getAndUpdate(current -> Math.min(current, value));
    doubleMaxValue.getAndUpdate(current -> Math.max(current, value));
  }

  public void recordString(String value) {
    stringCount.increment();
    int len = value.length();

    stringMinLength.accumulateAndGet(len, Math::min);
    stringMaxLength.accumulateAndGet(len, Math::max);
  }

  public void printStats() {
    if (appConfig.isShortStats()) {
      printShortStats();
    } else if (appConfig.isFullStats()) {
      printFullStats();
    } else
      log.info("You have not specified the option for statistics!");
  }

  public void printShortStats() {
    System.out.println("Long total count -->> " + longCount);
    System.out.println("Double total count -->> " + doubleCount);
    System.out.println("String total count -->> " + stringCount);
  }

  public void printFullStats() {
    if (longCount.sum() > 0) {
      double average = longSum.sum() / (double) longCount.sum();
      System.out.printf("Long full statistic: count=%d, min value=%d, max value=%d, total sum=%d, average value=%.2f%n",
                        longCount.sum(), longMinValue.get(), longMaxValue.get(), longSum.sum(), average);
    } else
      System.out.println("Longs -->> no data");

    if (doubleCount.sum() > 0) {
      double average = doubleSum.sum() / doubleCount.sum();
      System.out.printf("Double full statistics: count=%d, min value=%.2f, max value=%.2f, total sum=%.2f, average value=%.2f%n",
                        doubleCount.sum(), doubleMinValue.get(), doubleMaxValue.get(), doubleSum.sum(), average);
    } else
      System.out.println("Doubles -->> no data");

    if (stringCount.sum() > 0) {
      System.out.printf("String full statistic: count=%d, min length=%d, max length=%d%n",
                        stringCount.sum(), stringMinLength.get(), stringMaxLength.get());
    } else
      System.out.println("Strings -->> no data");
  }
}
