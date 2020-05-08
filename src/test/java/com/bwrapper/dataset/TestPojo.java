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

import java.io.Serializable;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestPojo implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final String CONSTANT = "";
  private transient String transientField = "";

  private StandardOpenOption enumVal;
  private LocalDateTime dateTimeVal;

  private final int[] arrayVal;
  private final String stringVal;

  private final NestedPojo nestedObjVal;

  public TestPojo() {
    this.arrayVal = null;
    this.stringVal = null;
    this.nestedObjVal = null;
  }

  private TestPojo(StandardOpenOption enumVal, LocalDateTime dateTimeVal, int[] arrayVal,
                   String stringVal, NestedPojo nestedObjVal) {
    this.enumVal = enumVal;
    this.dateTimeVal = dateTimeVal;
    this.arrayVal = arrayVal;
    this.stringVal = stringVal;
    this.nestedObjVal = nestedObjVal;
  }

  public static TestPojo create() {
    return new TestPojo(StandardOpenOption.APPEND, LocalDateTime.now(),
        new int[] {Integer.MIN_VALUE, Integer.MAX_VALUE},
        UUID.randomUUID().toString(),
        NestedPojo.create());
  }

  public static TestPojo createWithNullAttributes() {
    return new TestPojo(StandardOpenOption.APPEND, LocalDateTime.now(),
        null,
        null,
        NestedPojo.create());
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null || getClass() != object.getClass()) {
      return false;
    }
    TestPojo testPojo = (TestPojo) object;
    return Objects.equals(transientField, testPojo.transientField) &&
        enumVal == testPojo.enumVal &&
        Objects.equals(dateTimeVal, testPojo.dateTimeVal) &&
        Arrays.equals(arrayVal, testPojo.arrayVal) &&
        Objects.equals(stringVal, testPojo.stringVal) &&
        Objects.equals(nestedObjVal, testPojo.nestedObjVal);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(transientField, enumVal, dateTimeVal, stringVal, nestedObjVal);
    result = 31 * result + Arrays.hashCode(arrayVal);
    return result;
  }


  public static class NestedPojo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long longVal;
    private List<String> stringListVal;

    public NestedPojo() {
      longVal = -1;
    }

    private NestedPojo(long longVal, List<String> stringListVal) {
      this.longVal = longVal;
      this.stringListVal = stringListVal;
    }

    private static NestedPojo create() {
      List<String> stringList = Stream.generate(() -> UUID.randomUUID().toString())
          .limit(5)
          .collect(Collectors.toList());
      return new NestedPojo(Long.MIN_VALUE, stringList);
    }

    @Override
    public boolean equals(Object object) {
      if (this == object) {
        return true;
      }
      if (object == null || getClass() != object.getClass()) {
        return false;
      }
      NestedPojo that = (NestedPojo) object;
      return longVal == that.longVal &&
          Objects.equals(stringListVal, that.stringListVal);
    }

    @Override
    public int hashCode() {
      return Objects.hash(longVal, stringListVal);
    }
  }

}
