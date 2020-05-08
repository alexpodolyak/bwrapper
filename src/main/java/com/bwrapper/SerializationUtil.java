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
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Class provide methods for working with objects types,
 * determining its allowability to serializing , retrieving
 * fields for serialization e.t.c.
 *
 * @author Alexander Podolyak
 * @version 1.0.0
 */
class SerializationUtil {

  /**
   * Cheks whether object of certain {@code type} is required
   * to be serialized by default {@link java.io.ObjectOutputStream}
   * instead of <tt>bwrapper</tt> stream.
   *
   * <p>In some cases <tt>bwrapper</tt> streams cannot serialize
   * objects of certain types. This objects for example may have
   * only transient fields and no public default constructors and/or has
   * only static methods for creating it's instances (for example
   * {@link java.time.LocalDateTime}).
   *
   * <p>This classes may ovverides default {@code writeObject(ObjectOutputStream)}
   * or {@code writeReplace} methods to have ability be serialized by
   * {@link java.io.ObjectOutputStream}. Therfore they cannot be
   * serialized by <tt>bwrapper</tt>.
   *
   * @param type
   *     target type to check.
   * @return {@code true} if object's of certain {@code type} is allowed
   *     for serialization only by default {@link java.io.ObjectOutputStream} class,
   *     otherwise return {@code false}.
   * @throws java.lang.NullPointerException
   *     if {@code type} is {@code null}.
   */
  static boolean requiredDefaultSerialization(Type type) {
    Restrictions.notNull(type, "Type is null");
    if (type instanceof Class<?>) {
      Class<?> actualType = (Class<?>) type;
      return !isWrappable(actualType) & Serializable.class.isAssignableFrom(actualType);
    } else if (type instanceof ParameterizedType) {
      return requiredDefaultSerialization(((ParameterizedType) type).getRawType());
    }
    return false;
  }

  /**
   * Cheks whether {@code type} can be serialized with
   * <tt>bwrapper</tt> stream or optimized along with default serializator.
   *
   * @param type
   *     target class to check.
   * @return {@code true} if {@code type} is allowed to be serialized
   *     with <tt>bwrapper</tt> stream of optimized along with default
   *     serializer.
   * @see com.bwrapper.BWrapperOutputStream to know more about serialization
   *     and default serialization optimizations.
   */
  private static boolean isWrappable(Class<?> type) {
    boolean isWrappable = PrimitivesUtil.isPrimitive(type)
        || type.isArray()
        || type.isEnum()
        || Class.class.isAssignableFrom(type)
        || String.class.isAssignableFrom(type)
        || Collection.class.isAssignableFrom(type)
        || Map.class.isAssignableFrom(type);
    if (isWrappable) {
      return true;
    }

    // if type hasn't default public constructor then bwrapper deserializator
    // is not abble to create it's instance. Also if there is no fields to serialize
    // then there are assumptions that certain type ovveride default writeObject
    // or writeReplace method for futher default serializations.
    return hasDefaultPublicConstructor(type) && hasSerialFields(type);
  }

  /**
   * Checks whether {@code clazz} has non-transient
   * and non-static fields for serializing.
   *
   * @param clazz
   *     target class to check.
   *
   * @return {@code true} if {@code clazz} contains fields that
   *     allowed for serializing. Otherwise return {@code false}.
   * @throws java.lang.NullPointerException
   *     if {@code clazz} is {@code null}.
   */
  static boolean hasSerialFields(Class<?> clazz) {
    Restrictions.notNull(clazz, "class is null");
    return Stream.of(clazz.getDeclaredFields())
        .anyMatch(SerializationUtil::isDefaultSerialField);
  }

  /**
   * Return all declared non-static and non-transient
   * {@code clazz}'s fields.
   *
   * @param clazz
   *     target class.
   *
   * @return all non-transient and non-static
   * {@code clazz}'s declared fields.
   * @throws java.lang.NullPointerException
   *     if {@code clazz} is {@code null}.
   */
  static Field[] getSerialFields(Class<?> clazz) {
    Restrictions.notNull(clazz, "class is null");

    return Stream.of(clazz.getDeclaredFields())
        .filter(SerializationUtil::isDefaultSerialField)
        .peek(f -> f.setAccessible(true))
        .toArray(Field[]::new);
  }

  /**
   * Check whether requested {@code field} is either
   * {@code static final} or {@code transient}.
   *
   * @param field
   *     for checking.
   *
   * @return {@code true} if field's modifiers are either
   *     {@code static final} or {@code transient},
   *     otherwise return {@code false}.
   */
  private static boolean isDefaultSerialField(Field field) {
    int mask = Modifier.STATIC | Modifier.TRANSIENT;
    return (field.getModifiers() & mask) == 0;
  }

  /**
   * Cheks whether certain {@code type} class has declared
   * public constructor.
   *
   * @param type
   *     target class to check.
   * @return {@code true} if {@code type} class has default public constructor,
   *     otherwise return {@code false}.
   */
  private static boolean hasDefaultPublicConstructor(Class<?> type) {
    try {
      Constructor<?> con = type.getDeclaredConstructor();
      if (con == null) {
        return false;
      }
      return Modifier.isPublic(con.getModifiers());
    } catch (NoSuchMethodException e) {
      return false;
    }
  }
}
