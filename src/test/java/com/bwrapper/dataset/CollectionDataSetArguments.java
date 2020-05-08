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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class CollectionDataSetArguments implements ArgumentsProvider {
  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
    return Stream.concat(listTestData(), setTestData());
  }

  private Stream<Arguments> listTestData() {
    Random r = new Random();
    List<Integer> l1 = IntStream.generate(r::nextInt)
        .limit(5)
        .boxed()
        .collect(Collectors.toList());

    List<String> l2 = Stream.generate(() -> UUID.randomUUID().toString())
        .limit(5)
        .collect(Collectors.toCollection(LinkedList::new));

    List<LocalDateTime> l3 = Stream.generate(LocalDateTime::now)
        .limit(5)
        .collect(Collectors.toList());

    List<LocalDateTime> l4 = Stream.generate(LocalDateTime::now)
        .limit(5)
        .collect(Collectors.toCollection(LinkedList::new));
    return Stream.of(
        Arguments.of(new TestData<>(l1, l1.getClass())),
        Arguments.of(new TestData<>(l2, l2.getClass())),
        Arguments.of(new TestData<>(l3, l3.getClass())),
        Arguments.of(new TestData<>(l4, l4.getClass()))
    );
  }

  private Stream<Arguments> setTestData() {
    Random r = new Random();
    Set<Integer> l1 = IntStream.generate(r::nextInt)
        .limit(5)
        .boxed()
        .collect(Collectors.toSet());

    Set<String> l2 = Stream.generate(() -> UUID.randomUUID().toString())
        .limit(5)
        .collect(Collectors.toCollection(HashSet::new));

    Set<String> l3 = Stream.generate(() -> UUID.randomUUID().toString())
        .limit(5)
        .collect(Collectors.toCollection(TreeSet::new));

    Set<LocalDateTime> l4 = Stream.generate(LocalDateTime::now)
        .limit(5)
        .collect(Collectors.toSet());

    Set<LocalDateTime> l5 = Stream.generate(LocalDateTime::now)
        .limit(5)
        .collect(Collectors.toCollection(LinkedHashSet::new));

    return Stream.of(
        Arguments.of(new TestData<>(l1, l1.getClass())),
        Arguments.of(new TestData<>(l2, l2.getClass())),
        Arguments.of(new TestData<>(l3, l3.getClass())),
        Arguments.of(new TestData<>(l4, l4.getClass())),
        Arguments.of(new TestData<>(l5, l5.getClass()))
    );
  }
}
