/*
 * Copyright 2019-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bwrapper.dataset;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class MapDataSetArguments implements ArgumentsProvider {
  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
    return mapArguments();
  }

  private Stream<Arguments> mapArguments() {
    Random r = new Random();
    Map<String, Integer> m1 = IntStream.generate(r::nextInt)
        .limit(5)
        .boxed()
        .collect(Collectors.toMap(
            i -> UUID.randomUUID().toString(),
            Function.identity())
        );

    Map<String, Integer> m2 = IntStream.generate(r::nextInt)
        .limit(5)
        .boxed()
        .collect(Collectors.toMap(
            i -> UUID.randomUUID().toString(),
            Function.identity(),
            (v1, v2) -> v1,
            TreeMap::new));

    Map<LocalDateTime, Integer> m3 = IntStream.generate(r::nextInt)
        .limit(5)
        .boxed()
        .collect(Collectors.toMap(
            i -> LocalDateTime.now(),
            Function.identity(),
            (v1, v2) -> v1,
            LinkedHashMap::new));

    return Stream.of(
        Arguments.of(new TestData<>(m1, m1.getClass())),
        Arguments.of(new TestData<>(m2, m2.getClass())),
        Arguments.of(new TestData<>(m3, m3.getClass()))
    );
  }
}
