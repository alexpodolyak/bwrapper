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

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class ArrayDataSetArguments implements ArgumentsProvider {

  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
    return Stream.of(
        Arguments.of(new TestData<>(new int[] {1, 2, 3, 4, 5, -1}, int[].class)),
        Arguments.of(new TestData<>(new String[] {"1", "2", "3", "4", "5", "-1"}, String[].class)),
        Arguments
            .of(new TestData<>(new LocalDateTime[] {LocalDateTime.now()}, LocalDateTime[].class)),
        Arguments
            .of(new TestData<>(new InetAddress[] {InetAddress.getLocalHost()}, InetAddress[].class))
    );
  }


}
