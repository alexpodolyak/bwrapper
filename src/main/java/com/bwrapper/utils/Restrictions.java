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

package com.bwrapper.utils;

import java.lang.reflect.Array;

/**
 * Utility class that used for validation objects.
 *
 * @author Alexander Podolyak
 * @version 1.0.0
 */
public final class Restrictions {

  /**
   * Checks whether {@code object} is not null
   * or thrown an appropriate exception with {@code errMessage}.
   *
   * @param object
   *     to check.
   * @param errMessage
   *     message that will be outputted in appropriate exception.
   *
   * @throws NullPointerException
   *     if {@code object} is null.
   */
  public static void notNull(Object object, String errMessage) {
    if (object == null) {
      throw new NullPointerException(errMessage);
    }
  }

  /**
   * Checks whether every object from {@code objects} is not null
   * or thrown an appropriate exception with {@code errMessage}.
   *
   * @param errMessage
   *     message that will be outputted in appropriate exception
   * @param objects
   *     to check.
   *
   * @throws NullPointerException
   *     if any from {@code objects} is null.
   */
  public static void notNull(String errMessage, Object... objects) {
    for (Object o : objects) {
      notNull(o, errMessage);
    }
  }

  /**
   * Checks whether {@code string} is not blank, i.e. not null and not empty.
   *
   * <p>If string contains only whiltespace, it also will be an empty string.
   * Method will trim {@code string} and check it's length too.
   * Example:
   * <pre>
   *      String str = " ";
   *      notBlank(str, "String is empty");
   * </pre>
   * This method throw {@link IllegalArgumentException} in this case as {@code string} is empty.
   *
   * @param string
   *     to check.
   * @param errMessage
   *     message that will be outputted in appropriate exception.
   *
   * @throws NullPointerException
   *     if {@code string} is null.
   * @throws IllegalArgumentException
   *     if {@code string} is empty
   */
  public static void notBlank(String string, String errMessage) {
    notNull(string, errMessage);
    if (string.isEmpty() || string.trim().isEmpty()) {
      throw new IllegalArgumentException(errMessage);
    }
  }

  /**
   * Checks whether any string from {@code strings} is not blank
   * i.e. not {@code null} and empty or thrown an appropriate
   * exception with {@code errMessage}.
   *
   * @param errMessage
   *     message that will be outputted in appropriate exception.
   * @param strings
   *     to check.
   *
   * @throws NullPointerException
   *     if any from {@code strings} string is null.
   * @throws IllegalArgumentException
   *     if any from {@code string} string is blank.
   * @see #notBlank(String, String)
   */
  public static void notBlank(String errMessage, String... strings) {
    for (String s : strings) {
      notBlank(s, errMessage);
    }
  }

  /**
   * Checks whether {@code array} is not null or not empty
   * or throwns exception with {@code errMessage}.
   *
   * @param array
   *     to check.
   * @param errMessage
   *     message that will be outputted in appropriate exception.
   *
   * @throws NullPointerException
   *     if {@code array} is null.
   * @throws IllegalArgumentException
   *     if {@code array} has zero length;
   * @see #notEmptyArray0(Object, String) documentation.
   */
  public static void notEmptyArray(byte[] array, String errMessage) {
    notEmptyArray0(array, errMessage);
  }

  /**
   * Checks whether booleans {@code array} is not {@code null}
   * and empty or thrown exception with {@code errMessage}.
   *
   * @param array
   *     to check.
   * @param errMessage
   *     message that will be outputted in appropriate exception.
   *
   * @throws java.lang.NullPointerException
   *     if {@code array} is {@code null}.
   * @throws java.lang.IllegalArgumentException
   *     if {@code array}'s length is zero.
   * @see #notEmptyArray(byte[], String) doc.
   */
  public static void notEmptyArray(boolean[] array, String errMessage) {
    notEmptyArray0(array, errMessage);
  }

  /**
   * Checks whether shorts {@code array} is not {@code null}
   * and empty or thrown exception with {@code errMessage}.
   *
   * @param array
   *     to check.
   * @param errMessage
   *     message that will be outputted in appropriate exception.
   *
   * @throws java.lang.NullPointerException
   *     if {@code array} is {@code null}.
   * @throws java.lang.IllegalArgumentException
   *     if {@code array}'s length is zero.
   * @see #notEmptyArray(byte[], String) doc.
   */
  public static void notEmptyArray(short[] array, String errMessage) {
    notEmptyArray0(array, errMessage);
  }

  /**
   * Checks whether chars {@code array} is not {@code null}
   * and empty or thrown exception with {@code errMessage}.
   *
   * @param array
   *     to check.
   * @param errMessage
   *     message that will be outputted in appropriate exception.
   *
   * @throws java.lang.NullPointerException
   *     if {@code array} is {@code null}.
   * @throws java.lang.IllegalArgumentException
   *     if {@code array}'s length is zero.
   * @see #notEmptyArray(byte[], String) doc.
   */
  public static void notEmptyArray(char[] array, String errMessage) {
    notEmptyArray0(array, errMessage);
  }

  /**
   * Checks whether ints {@code array} is not {@code null}
   * and empty or thrown exception with {@code errMessage}.
   *
   * @param array
   *     to check.
   * @param errMessage
   *     message that will be outputted in appropriate exception.
   *
   * @throws java.lang.NullPointerException
   *     if {@code array} is {@code null}.
   * @throws java.lang.IllegalArgumentException
   *     if {@code array}'s length is zero.
   * @see #notEmptyArray(byte[], String) doc.
   */
  public static void notEmptyArray(int[] array, String errMessage) {
    notEmptyArray0(array, errMessage);
  }

  /**
   * Checks whether longs {@code array} is not {@code null}
   * and empty or thrown exception with {@code errMessage}.
   *
   * @param array
   *     to check.
   * @param errMessage
   *     message that will be outputted in appropriate exception.
   *
   * @throws java.lang.NullPointerException
   *     if {@code array} is {@code null}.
   * @throws java.lang.IllegalArgumentException
   *     if {@code array}'s length is zero.
   * @see #notEmptyArray(byte[], String) doc.
   */
  public static void notEmptyArray(long[] array, String errMessage) {
    notEmptyArray0(array, errMessage);
  }

  /**
   * Checks whether doubles {@code array} is not {@code null}
   * and empty or thrown exception with {@code errMessage}.
   *
   * @param array
   *     to check.
   * @param errMessage
   *     message that will be outputted in appropriate exception.
   *
   * @throws java.lang.NullPointerException
   *     if {@code array} is {@code null}.
   * @throws java.lang.IllegalArgumentException
   *     if {@code array}'s length is zero.
   * @see #notEmptyArray(byte[], String) doc.
   */
  public static void notEmptyArray(double[] array, String errMessage) {
    notEmptyArray0(array, errMessage);
  }

  /**
   * Checks whether floats {@code array} is not {@code null}
   * and empty or thrown exception with {@code errMessage}.
   *
   * @param array
   *     to check.
   * @param errMessage
   *     message that will be outputted in appropriate exception.
   *
   * @throws java.lang.NullPointerException
   *     if {@code array} is {@code null}.
   * @throws java.lang.IllegalArgumentException
   *     if {@code array}'s length is zero.
   * @see #notEmptyArray(byte[], String) doc.
   */
  public static void notEmptyArray(float[] array, String errMessage) {
    notEmptyArray0(array, errMessage);
  }

  /**
   * Checks whether {@code condition} is {@code true}
   * and thrown an exception.
   *
   * @param condition
   *     for checking.
   * @param errMessage
   *     that will be outputted in appropriate exception.
   *
   * @throws IllegalArgumentException
   *     if {@code condition} is {@code true}.
   */
  public static void assertFalse(boolean condition, String errMessage) {
    if (condition) {
      throw new IllegalArgumentException(errMessage);
    }
  }

  /**
   * Checks whether {@code condition} is {@code false}
   * and thrown an exception.
   *
   * @param condition
   *     for checking.
   * @param errMessage
   *     that will be outputted in appropriate exception.
   *
   * @throws IllegalArgumentException
   *     if {@code condition} is {@code false}.
   */
  public static void assertTrue(boolean condition, String errMessage) {
    if (!condition) {
      throw new IllegalArgumentException(errMessage);
    }
  }

  /**
   * Checks whether {@code array} is not null or not empty.
   *
   * <p>This method takes {@link Object} as argument, this add one checking
   * to {@code array} to be sure that is an array object.
   *
   * @param array
   *     to check.
   * @param errMessage
   *     message that will be outputted in appropriate exception.
   *
   * @throws NullPointerException
   *     if {@code array} is null.
   * @throws IllegalArgumentException
   *     if {@code array} is not refer to an array type, i.e. {@link Class#isArray()}
   *     method return {@code false}.
   *     if {@code array} has zero length.
   */
  private static void notEmptyArray0(Object array, String errMessage) {
    notNull(array, errMessage);
    if (!array.getClass().isArray()) {
      throw new IllegalArgumentException("Argument is not an array");
    }
    if (Array.getLength(array) == 0) {
      throw new IllegalArgumentException(errMessage);
    }
  }
}
