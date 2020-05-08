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

import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;


public class PrimitivesDataSetArguments implements ArgumentsProvider {

  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
    return Stream.of(
        Arguments.of(new TestData<>(Boolean.TRUE, boolean.class)),
        Arguments.of(new TestData<>(Byte.MAX_VALUE, byte.class)),
        Arguments.of(new TestData<>(Short.MAX_VALUE, short.class)),
        Arguments.of(new TestData<>(Character.MAX_VALUE, char.class)),
        Arguments.of(new TestData<>(Integer.MAX_VALUE, int.class)),
        Arguments.of(new TestData<>(Float.MAX_VALUE, float.class)),
        Arguments.of(new TestData<>(Long.MAX_VALUE, long.class)),
        Arguments.of(new TestData<>(Double.MAX_VALUE, double.class))
    );
  }
}
