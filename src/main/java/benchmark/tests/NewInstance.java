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
package benchmark.tests;

import benchmark.Test;
import benchmark.TestResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NewInstance implements Test {

  private static final int WARMUP = 1000;

  private static final int ITERATIONS = 1000000;

  @Override
  public List<TestResult> getTestResults() {
    try {
      final Object[] test1 = new Object[ITERATIONS];
      final Object[] test2 = new Object[ITERATIONS];
      final Object object = new Object();

//      for (int i = 0; i < WARMUP; i++) {
//        new Object(); Object.class.newInstance();
//      }

      final long t0 = System.nanoTime();

      for (int i = 0; i < ITERATIONS; i++) {
        test1[i] = new Object();
      }
      final long t1 = System.nanoTime();

      for (int i = 0; i < ITERATIONS; i++) {
        test2[i] = Object.class.newInstance();
      }
      final long t2 = System.nanoTime();

      for (int i = 0; i < ITERATIONS; i++) {
        test1[i] = new StringBuilder();
      }
      final long t3 = System.nanoTime();

      for (int i = 0; i < ITERATIONS; i++) {
        test2[i] = StringBuilder.class.newInstance();
      }
      final long t4 = System.nanoTime();

      for (int i = 0; i < ITERATIONS; i++) {
        test2[i] = object;
      }
      final long t5 = System.nanoTime();

      final List<TestResult> testResults = new ArrayList<TestResult>();
      testResults.add(new TestResult("new Object()", t1 - t0, ITERATIONS, null));
      testResults.add(new TestResult("Object.class.newInstance()", t2 - t1, ITERATIONS, null));
      testResults.add(new TestResult("new StringBuilder()", t3 - t2, ITERATIONS, null));
      testResults.add(new TestResult("StringBuilder.class.newInstance()", t4 - t3, ITERATIONS, null));
      testResults.add(new TestResult("array[i] = object", t5 - t4, ITERATIONS, null));

      return testResults;
    } catch (final Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
