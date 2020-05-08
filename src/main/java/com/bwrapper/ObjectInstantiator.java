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
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * Class used for creating object's instances by it's
 * default public constructors and an array instances.
 *
 * @author Alexander Podolyak
 * @version 1.0.0
 */
class ObjectInstantiator {

  /**
   * Create new array instance of certain {@code componentType}
   * with requested array's {@code len}.
   *
   * @param componentType
   *     component type of array.
   * @param len
   *     array's length;
   *
   * @return new {@code componentType}'s array of requested {@code len}.
   * @throws java.lang.NullPointerException
   *     if {@code componentType} is {@code null}.
   * @throws java.lang.IllegalArgumentException
   *     if {@code len} is negative.
   */
  static Object newArrayInstance(Class<?> componentType, int len) {
    Restrictions.notNull(componentType, "Component type is null");
    Restrictions.assertTrue(len >= 0, "Negative array length");
    return Array.newInstance(componentType, len);
  }

  /**
   * Create new instance of requested {@code type}.
   *
   * @param type
   *     target object's class.
   * @return instance of requested {@code type}.
   * @throws ReflectiveOperationException
   *     if {@code type} has not declared public default constructor
   *     or any other reflection error has occured during instant creation.
   * @throws java.lang.NullPointerException
   *     if {@code type} is {@code null}.
   */
  static Object newInstance(Class<?> type) throws ReflectiveOperationException {
    Restrictions.notNull(type, "Type is null");
    Constructor<?> con = getDefaultPublicConstructor(type);
    if (con == null) {
      throw new ReflectiveOperationException("No public constructor found");
    }

    return con.newInstance();
  }

  /**
   * Retrieve declared default public constructor from {@code type} class.
   *
   * @param type
   *     target class.
   *
   * @return {@code type}'s default public constructor.
   * @throws NoSuchMethodException
   *     if {@code type} has not default public constructor.
   */
  private static Constructor<?> getDefaultPublicConstructor(Class<?> type)
      throws NoSuchMethodException {
    Constructor<?> con = type.getDeclaredConstructor();
    con.setAccessible(true);
    return Modifier.isPublic(con.getModifiers()) ? con : null;
  }

}
