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

/**
 * @author Alexander Podolyak
 * @version 1.0.0
 */
public class TestData<T> {

  private final T value;
  private final Class<? extends T> type;

  public TestData(T value, Class<? extends T> type) {
    this.value = value;
    this.type = type;
  }

  public T getValue() {
    return value;
  }

  public Class<? extends T> getType() {
    return type;
  }

  @Override
  public String toString() {
    return "TestData [ value=" + value + ", type=" + type + ']';
  }
}
