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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Utility class that provide methods for working with any {@link java.lang.reflect.Type}
 * implementations. TODO: add more javadoc
 *
 * @author Alexander Podolyak
 * @version 1.0.0
 */
class TypeUtil {

  private static final int MAP_KEY_ARG_IDX = 0;
  private static final int MAP_VAL_ARG_IDX = 1;

  /**
   * Retreives actual type arguments from requested {@code parent} type.
   *
   * <p>There is only one possible way to get type arguments when {@code parent}'s
   * implementation is either {@link ParameterizedType} or {@link WildcardType}.
   *
   * @param parent
   *     target type.
   *
   * @return actual type arguments of {@code parent} type,
   *     or {@code null} if {@code parent}'s type is neither
   *     {@link ParameterizedType} or {@link WildcardType}.
   * @throws NullPointerException
   *     if {@code parent} is {@code null}.
   */
  static Type[] getActualTypeArguments(Type parent) {
    Restrictions.notNull(parent, "parent type is null");
    if (parent instanceof ParameterizedType) {
      return ((ParameterizedType) parent).getActualTypeArguments();
    }

    if (parent instanceof WildcardType) {
      WildcardType wt = (WildcardType) parent;
      Type[] ub = wt.getUpperBounds();
      return ub != null && ub.length > 0 ? ub : wt.getLowerBounds();
    }
    return null;
  }

  /**
   * Determine component type of {@link Collection}'s {@code value}.
   *
   * <p>Determination process takes two arguments: first one is target
   * object for which determination is requested, second argument it's {@code value}'s type
   * arguments. {@code vargs} is optional argument, so they can be empty or {@code null}.
   *
   * <p>{@code vargs} argument may be taken from {@code value}'s
   * specific {@link Type} implementation. In can be available only by Java Reflection API using.
   * Example:
   * <pre>
   *      class SomeClass {
   *        private {@code List<String>} list;
   *        //constructors, getters and setters.
   *      }
   *
   *      SomeClass instance = ... ;
   *      Field listField = SomeClass.class.getDeclaredField("list");
   *      // here type refers to an list type,
   *      // which is {@link java.lang.reflect.ParameterizedType}
   *      // as collection interface signature takes one geneic parameter.
   *
   *      Type listType = listField.getGenericType();
   *      ParametrizedType pt = (ParametrizedType) listField; // casting is safe.
   *
   *     // this is one of ways how to propagate instance's type arguments.
   *      Type keyType = getCollectionTypeArg(instance, pt.getActualTypeArguments();
   * </pre>
   *
   * <p>When method is called with {@code value}'s raw type,
   * it simply will take first collection's value and return it's class. Otherwise, if {@code
   * vargs} is not {@code null} and empty, method will try to resolve component type from them.
   *
   * <p>According to {@link Collection} signature it takes one type parameter,
   * so only first item from {@code vargs} is taken for resolving.
   *
   *
   * <p>See {@link #getArgType(Type)} to get more details
   * how resolving from {@code vargs} works.
   *
   * @param value
   *     target object whoose component type should be determined.
   * @param vargs
   *     {@code value}'s component type arguments (Optional argument).
   *
   * @return {@code value}'s component type or {@code null}
   *     if {@code value} is empty or {@code vargs} are emtpy
   *     or component type cannot be determined from them.
   * @throws NullPointerException
   *     if {@code value} and {@code vargs} is {@code null}.
   * @see #getArgType(Type)
   */
  static Type getComponentType(Collection<?> value, Type[] vargs) {
    if (vargs != null && vargs.length == 1) {
      return getArgType(vargs[0]);
    }
    Restrictions.notNull(value, "collection is null");
    Iterator<?> i = value.iterator();
    return i.hasNext() ? i.next().getClass() : null;
  }

  /**
   * Determines parameter type of {@code value}'s key object or {@code value}'s actual type
   * arguments which is represented by {@code vargs} array.
   *
   * <p>There is two ways that used for resolve parameter's type:
   * first one is simply takes first key from {@code value} map and return it's class. This way is
   * used in cases when {@code vargs} is {@code null} or empty.
   *
   * <p>Otherwise, when {@code vargs} is not {@code null} and not empty,
   * it takes first type from {@code vargs} and call {@link #getArgType(Type)} for actual type
   * resolving.
   *
   * <p>{@code vargs} argument may be taken from {@code value}'s
   * specific {@link Type} implementation. In can be available only by using
   * Java Reflection API.
   * Example:
   * <pre>
   *      class SomeClass {
   *        private {@code Map<String, Integer>} map;
   *        // constructors, getters and setters
   *      }
   *
   *
   *      SomeClass instance = ... ;
   *      Field mapField = SomeClass.class.getDeclaredField("map");
   *      // here type refers to an map type,
   *      // which is {@link java.lang.reflect.ParameterizedType}
   *      // as map signature takes two geneic parameters.
   *
   *      Type mapType = mapField.getGenericType();
   *      ParametrizedType pt = (ParametrizedType) mapType; // casting is safe.
   *
   *      // this is one of ways how to propagate instance's type arguments.
   *      Type keyType = getMapKeyType(instance, pt.getActualTypeArguments();
   * </pre>
   *
   * @param value
   *     target value for resolving it's key type.
   * @param vargs
   *     {@code value}'s actual type arguments.
   *
   * @return actual type of {@code value}'s key objects.
   * @throws NullPointerException
   *     if {@code value} is {@code null}.
   */
  static Type getMapKeyType(Map<?, ?> value, Type[] vargs) {
    if (vargs != null && vargs.length > 1) {
      return getArgType(vargs[MAP_KEY_ARG_IDX]);
    }
    Restrictions.notNull(value, "map is null");
    Iterator i = value.keySet().iterator();
    return i.hasNext() ? i.next().getClass() : null;
  }

  /**
   * Determines parameter type of {@code value}'s value object or
   * {@code value}'s actual type arguments which is represented by {@code vargs} array.
   *
   * <p>There is two ways that used for resolve parameter's type:
   * first one is simply takes first value from {@code value} map
   * and return it's class. This way is used in cases when {@code vargs}
   * is {@code null} or empty.
   *
   * <p>Otherwise, when {@code vargs} is not {@code null} and not empty,
   * it takes first type from {@code vargs} and call {@link #getArgType(Type)}
   * for actual type resolving.
   *
   * <p>{@code vargs} argument may be taken from {@code value}'s
   * specific {@link Type} implementation. In can be available only by Java Reflection API using.
   * Example:
   * <pre>
   *      class SomeClass {
   *        private {@code Map<String, Integer>} map;
   *        // constructors, getters and setters
   *      }
   *
   *      SomeClass instance = ... ;
   *      Field mapField = SomeClass.class.getDeclaredField("map");
   *      // here type refers to an map type,
   *      // which is {@link java.lang.reflect.ParameterizedType}
   *      // as map signature takes two geneic parameters.
   *
   *      Type mapType = mapField.getGenericType();
   *      ParametrizedType pt = (ParametrizedType) mapType; // casting is safe.
   *
   *      // this is one of ways how to propagate instance's type arguments.
   *      Type keyType = getMapValType(instance, pt.getActualTypeArguments();
   * </pre>
   *
   * @param value
   *     target value for resolving it's value type.
   * @param vargs
   *     {@code value}'s actual type arguments.
   *
   * @return actual type of {@code value}'s value objects.
   * @throws NullPointerException
   *     if {@code value} and {@code vargs} is {@code null}.
   */
  static Type getMapValType(Map<?, ?> value, Type[] vargs) {
    if (vargs != null && vargs.length > 1) {
      return getArgType(vargs[MAP_VAL_ARG_IDX]);
    }
    Restrictions.notNull(value, "map is null");
    Iterator i = value.values().iterator();
    return i.hasNext() ? i.next().getClass() : null;
  }

  /**
   * Determines map {@code value}'s key-value's types.
   *
   * <p>Current method built to avoid boilerplate code
   * on map's parameters type determination by using multiple
   * methods instead this one.
   * Example:
   * <pre>
   *     {@code Map<Integer,String>} map = ...;
   *      Type mapType = ...;
   *      Type[] params = ((ParametrizedType)mapType).getActualTypeArguments();
   *
   *      Type keyType = TypeUtil.getMapKeyType(map, params);
   *      Type valType = TypeUtil.getMapValType(map, params);
   *
   *      // current method just reduce line of code required to get this values.
   *      Type[] actulKeyValTypes = TypeUtil.getMapKeyValTypes(map, params);
   *      // actualkeyValTypes[0] - always represents a map's key type.
   *      // actualkeyValTypes[1] - always represents a map's value type.
   * </pre>
   *
   * <p>This method just groups two methods described above and return
   * array of types represented key's and value's type accordingly.
   *
   * @param value
   *     target value whose key/value's types should be resolved.
   * @param vargs
   *     represents {@code value}'s actual type arguments.
   *
   * @return {@link Type} array, represents a {@code value}'s
   *     key/value's actual type. Some of array's elements
   *     can be {@code null}, this is depends on unredlying
   *     methods execution.
   * @throws NullPointerException
   *     if {@code value} and {@code vargs} is {@code null}.
   * @see #getMapKeyType(Map, Type[])
   * @see #getMapValType(Map, Type[])
   * @deprecated use {@link #getMapValType(java.util.Map, java.lang.reflect.Type[])}
   *     and {@link #getMapKeyType(java.util.Map, java.lang.reflect.Type[])} instead.
   */
  @Deprecated
  static Type[] getMapKeyValTypes(Map<?, ?> value, Type[] vargs) {
    return new Type[] {
        getMapKeyType(value, vargs),
        getMapValType(value, vargs)
    };
  }

  /**
   * Resolves raw type of specific {@code type} object.
   *
   * <p>Method will return requested {@code type} if
   * it instance of {@link java.lang.Class}.
   *
   * @param type
   *     whose raw type should be resolved.
   *
   * @return raw type of {@code type} if it instance of {@link Class},
   *     {@link java.lang.reflect.ParameterizedType#getRawType()}
   *     if {@code type} is instance of {@link java.lang.reflect.ParameterizedType},
   *     otherwise return {@code null}.
   * @throws java.lang.NullPointerException
   *     if {@code type} is {@code null}.
   */
  static Class<?> getRawType(Type type) {
    Restrictions.notNull(type, "type is null");
    if (type instanceof Class<?>) {
      return (Class<?>) type;
    } else if (type instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) type;
      return getRawType(pt.getRawType());
    }
    return null;
  }

  /**
   * Resolve actual type of requested {@code type}.
   * TODO: add javadoc with describing restrictions of using.
   *
   * <p>In other words, method checks for specific implementation
   * of {@code type} and return it's actual type in case when it's
   * not an {@link Class} or {@link java.lang.reflect.ParameterizedType}.
   *
   * <p>Here more logic come in case when {@code type} refer to the
   * {@link WildcardType} implementation. Then method checks wildcard's
   * {@link WildcardType#getUpperBounds()}, {@link WildcardType#getLowerBounds()}
   * arrays and return first not-null object regardless of it's
   * bound. It checks only for first as there is no cases when one
   * {@link WildcardType} can have multiple types in both upper and
   * lower bound arrays.
   *
   * <p><b>Note: </b>{@code type} supposed to be not null,
   * as this method is only for internal usage.
   *
   * @param type
   *     target type for resolving.
   *
   * @return actual type resolved from {@code type}
   *     or {@code type} if it not an {@link WildcardType} object.
   */
  private static Type getArgType(Type type) {
    if (type instanceof WildcardType) {
      WildcardType wt = (WildcardType) type;
      Type[] ub = wt.getUpperBounds();
      return ub == null || ub.length == 0 ? wt.getLowerBounds()[0] : ub[0];
    }
    return type;
  }
}
