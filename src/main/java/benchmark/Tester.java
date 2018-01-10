/*
 * Copyright 2018 Aleksei Balan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.Callable;

public class Tester implements Callable<Void> {

  private static final Logger LOGGER = LoggerFactory.getLogger("");

  private static final Map<String, TestStatistics> STATISTICS = new LinkedHashMap<String, TestStatistics>();

  private static final List<Test> memoryLock = new ArrayList<Test>(); // keep garbage

  //
  private final Set<Class<? extends Test>> tests;

  private static void addResults(final Class<? extends Test> aClass, final List<TestResult> results, final long memory) {
    for (int i = 0; i < results.size(); i++) {
      final String keyStatistics = aClass.getName() + i;
      final TestResult result = results.get(i);
      if (!STATISTICS.containsKey(keyStatistics)) {
        STATISTICS.put(keyStatistics, new TestStatistics(result.getName()));
      }
      TestStatistics testStatistics = STATISTICS.get(keyStatistics);
      testStatistics.getNanoSeconds().addValue((double) result.getNanoSeconds() / (double) result.getMultiplier());
      testStatistics.getBytes().addValue(memory);
    }
  }

  private static void logResults() {
    for (final TestStatistics s : STATISTICS.values()) {
      LOGGER.info(String.format("%-40s | %10.2f ns | %10.2f ns | %10.2f ns",
          s.getName(),
          s.getNanoSeconds().getElement((int)s.getNanoSeconds().getN() - 1),
          s.getNanoSeconds().getMean(),
          (s.getNanoSeconds().getPercentile(49.999) + s.getNanoSeconds().getPercentile(50.001)) / (double) 2
      ));
    }
  }

  public Tester() {
    tests = new LinkedHashSet<Class<? extends Test>>();
    final ServiceLoader<Test> serviceLoader = ServiceLoader.load(Test.class);
    for (final Test test : serviceLoader) {
      tests.add(test.getClass());
    }
  }

  @Override
  public Void call() throws Exception {
    final List<Class<? extends Test>> list = new ArrayList<Class<? extends Test>>(tests);
    for (int i = 0; i < 10; i++) {
      LOGGER.info(String.format("--------------------------- test %2d ---- | result ------ | mean -------- | median ------", i+1));
      for (Class<? extends Test> testClass : list) {
        final long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        final Test test = testClass.newInstance();
        final List<TestResult> results = test.getTestResults();
        addResults(testClass, results, Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - memory);
        memoryLock.add(test);
      }
      logResults();
      Collections.shuffle(list);
    }
    return null;
  }
}
