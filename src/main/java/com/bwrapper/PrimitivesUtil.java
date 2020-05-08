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

package com.bwrapper;

import com.bwrapper.utils.Restrictions;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class that provide useful methods for
 * working with primitive classes.
 *
 * @author Alexander Podolyak
 * @version 1.0.0
 */
public class PrimitivesUtil {

  private static final Map<Class<?>, Class<?>> wrapperToPrimitive =
      new HashMap<Class<?>, Class<?>>() {{
        put(Boolean.class, boolean.class);
        put(Byte.class, byte.class);
        put(Short.class, short.class);
        put(Character.class, char.class);
        put(Integer.class, int.class);
        put(Float.class, float.class);
        put(Long.class, long.class);
        put(Double.class, double.class);
      }};

  private static final List<String> primitiveNames = Arrays.asList(
      "boolean", "byte", "short", "char", "int", "float", "long", "double"
  );

  /**
   * Unwrap {@code wrapper} class to it's primitive class type.
   *
   * @param wrapper
   *     class represents an primitive type.
   *
   * @return unwrapped primitive class.
   * @throws NullPointerException
   *     if {@code wrapper} class if {@code null}.
   * @throws IllegalArgumentException
   *     if {@code wrrapper} is either primitive type
   *     or it's wrapper class.
   */
  public static Class<?> unwrap(Class<?> wrapper) {
    Restrictions.notNull(wrapper, "class is null");
    if (wrapperToPrimitive.containsKey(wrapper)) {
      return wrapperToPrimitive.get(wrapper);
    } else if (wrapperToPrimitive.containsValue(wrapper)) {
      return wrapper;
    }
    throw new IllegalArgumentException("class is either primitive type of it's wrapper class");
  }

  /**
   * Checks whether {@code clazz} refer to an
   * primitive type or it's wrapper class.
   *
   * @param clazz
   *     to check.
   *
   * @return {@code true} if {@code clazz} refers
   *     to an primitive type or it's wrapper class.
   * @throws NullPointerException
   *     if {@code clazz} is {@code null}.
   */
  public static boolean isPrimitive(Class<?> clazz) {
    Restrictions.notNull(clazz, "class is null");
    return wrapperToPrimitive.containsKey(clazz) || wrapperToPrimitive.containsValue(clazz);
  }

  /**
   * Checks whether {@code clazz} refer to an primitive raw class.
   *
   * @param clazz
   *     to check.
   *
   * @return {@code true} if {@code clazz} refers
   *     to a raw primitive class, otherwise return {@code false}.
   * @throws NullPointerException
   *     if {@code clazz} is {@code null}.
   */
  static boolean isRawType(Class<?> clazz) {
    Restrictions.notNull(clazz, "class is null");
    return wrapperToPrimitive.containsValue(clazz);
  }

  /**
   * Checks whether {@code clazz} refer to an primitive's wrapper class.
   *
   * @param clazz
   *     to check.
   *
   * @return {@code true} if {@code clazz} refer to a raw
   *     primitive's wrapper class, otherwise return {@code false}.
   * @throws NullPointerException
   *     if {@code clazz} is {@code null}.
   */
  static boolean isWrapper(Class<?> clazz) {
    Restrictions.notNull(clazz, "class is null");
    return wrapperToPrimitive.containsKey(clazz);
  }

  /**
   * Internal method used for load primitive class
   * by requested {@code name}.
   *
   * <p>In case if {@code name} represents an primitive's wrapper class,
   * method will return it's raw type.
   * Example:
   * <pre>
   *      Class<?> rawInt = readPrimitiveClass("java.lang.Integer");
   * </pre>
   * Here variable {@code rawInt} will be reffered to an {@code int.class}
   * instead of {@link Integer}.
   *
   * @param name
   *     of primitive type or it's wrapper.
   *
   * @return class that represents an primitive
   *     type or it's wrapper class of requested {@code name}.
   * @throws ClassNotFoundException
   *     if system cannot find requested class by {@code name}.
   * @throws NullPointerException
   *     if {@code name} is {@code null}.
   * @throws IllegalArgumentException
   *     if {@code name} is empty.
   *     if class with certain {@code name} does not refer
   *     to an primitive type or it's wrapper class.
   */
  static Class<?> readPrimitiveClass(String name) throws ClassNotFoundException {
    Restrictions.notBlank(name, "class name is blank");
    switch (name) {
      case "boolean":
        return boolean.class;
      case "byte":
        return byte.class;
      case "char":
        return char.class;
      case "short":
        return short.class;
      case "int":
        return int.class;
      case "float":
        return float.class;
      case "long":
        return long.class;
      case "double":
        return double.class;
      default:
        break;
    }
    // it may be an wrapper name
    if (name.startsWith("java.lang")) {
      Class<?> wrapper = Class.forName(name);
      Restrictions.assertTrue(isPrimitive(wrapper),
          "class name does not refer to an primitive type"
      );
      return unwrap(wrapper);
    }

    throw new IllegalArgumentException(
        "class name does not refer to an primitive type or it's wrapepr class"
    );
  }

  /**
   * Checks whether {@code className} represents a
   * raw primitive class name.
   *
   * @param className
   *     to be checked.
   *
   * @return {@code true} if {@code className} refer
   *     to a raw primitive class name, otherwise return {@code false}.
   * @throws NullPointerException
   *     if {@code className} is {@code null}.
   * @throws IllegalArgumentException
   *     if {@code className} is blank.
   */
  static boolean isRawPrimitive(String className) {
    Restrictions.notBlank(className, "class name is blank");
    return primitiveNames.contains(className);
  }
}
