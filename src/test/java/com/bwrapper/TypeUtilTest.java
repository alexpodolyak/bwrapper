package com.bwrapper;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

class TypeUtilTest {

  @ParameterizedTest
  @NullSource
  void givenNullValue_ShouldThrownException_On_GetActualTypeArgumentsCall(Type value) {
    assertThrows(NullPointerException.class, () -> TypeUtil.getActualTypeArguments(value));
  }

  @ParameterizedTest
  @NullSource
  void givenNullValue_ShouldThrowException_On_GetCollectionTypeArg(Collection<?> value) {
    assertThrows(NullPointerException.class, () -> TypeUtil.getComponentType(value, null));
  }

  @ParameterizedTest
  @NullSource
  void givenNullValue_ShouldThrownException_On_GetKeyAndValTypeArg(Map<?, ?> value) {
    assertAll(
        () -> assertThrows(NullPointerException.class, () -> TypeUtil.getMapKeyType(value, null)),
        () -> assertThrows(NullPointerException.class, () -> TypeUtil.getMapValType(value, null))
    );
  }

  @ParameterizedTest
  @EmptySource
  void givenEmptyMap_ShouldReturnNull_On_GetKeyValType(Map<?, ?> value) {
    assertAll(
        () -> assertNull(TypeUtil.getMapKeyType(value, null)),
        () -> assertNull(TypeUtil.getMapValType(value, null))
    );
  }

  @ParameterizedTest
  @EmptySource
  void givenEmptyList_ShouldReturnNull_On_GetCollectionTypeArg(List<?> value) {
    assertNull(TypeUtil.getComponentType(value, null));
  }

  @ParameterizedTest
  @MethodSource("collectionTypeArguments")
  void givenCollectionFields_ShouldResolveComponentTypeAndTypeArgs(Type type,
                                                                   Type expectedComType) {
    assertTrue(type instanceof ParameterizedType);

    ParameterizedType pt = (ParameterizedType) type;
    Type[] args = pt.getActualTypeArguments();
    Type actualType = TypeUtil.getComponentType(new ArrayList<>(), args);

    assertAll(
        () -> assertArrayEquals(args, TypeUtil.getActualTypeArguments(type)),
        () -> assertNotNull(actualType),
        () -> assertEquals(expectedComType, actualType)
    );
  }

  @Test
  void givenCollectionObject_ShouldResolveComponentType() {
    List<Integer> list = IntStream.range(0, 1)
        .boxed()
        .collect(Collectors.toList());
    assertEquals(Integer.class, TypeUtil.getComponentType(list, null));
  }

  @ParameterizedTest
  @MethodSource("mapFieldTypeArguments")
  void givenMapFields_ShouldResolveActualTypeArgumentsAndKeyWithValActualTypes(Type mapType,
                                                                               Type expectedKeyType,
                                                                               Type expectedValType) {
    assertTrue(mapType instanceof ParameterizedType);

    ParameterizedType pt = (ParameterizedType) mapType;
    Type[] args = pt.getActualTypeArguments();

    Type actualKeyType = TypeUtil.getMapKeyType(new HashMap<>(), args);
    Type actualValType = TypeUtil.getMapValType(new HashMap<>(), args);

    assertAll(
        () -> assertArrayEquals(args, TypeUtil.getActualTypeArguments(mapType)),
        () -> assertNotNull(actualKeyType),
        () -> assertNotNull(actualValType),
        () -> assertEquals(expectedKeyType, actualKeyType),
        () -> assertEquals(expectedValType, actualValType)
    );
  }

  @Test
  void givenMapObject_SHouldResolveComponentType() {
    Map<String, Integer> map = IntStream.range(0, 1)
        .boxed()
        .collect(Collectors.toMap(
            i -> UUID.randomUUID().toString(),
            Function.identity()
        ));

    assertAll(
        () -> assertEquals(String.class, TypeUtil.getMapKeyType(map, null)),
        () -> assertEquals(Integer.class, TypeUtil.getMapValType(map, null))
    );
  }

  private static Stream<Arguments> collectionTypeArguments() {
    List<Arguments> arguments = new ArrayList<>();

    for (Field f : ClassWithCollection.class.getDeclaredFields()) {
      Type type = f.getGenericType();
      Type ctype;

      if (type instanceof ParameterizedType) {
        ParameterizedType pt = (ParameterizedType) type;
        Type[] args = pt.getActualTypeArguments();
        if (!(args[0] instanceof WildcardType)) {
          ctype = args[0];
        } else {
          ctype = getFirstNotEmptyType((WildcardType) args[0]);
        }

        arguments.add(Arguments.of(type, ctype));
      }
    }
    return arguments.stream();
  }

  private static Stream<Arguments> mapFieldTypeArguments() {
    List<Arguments> arguments = new ArrayList<>();

    for (Field f : ClassWithMap.class.getDeclaredFields()) {
      Type type = f.getGenericType();
      Type ktype, vtype;

      if (type instanceof ParameterizedType) {
        ParameterizedType pt = (ParameterizedType) type;
        Type[] args = pt.getActualTypeArguments();

        if (!(args[0] instanceof WildcardType)) {
          ktype = args[0];
        } else {
          ktype = getFirstNotEmptyType((WildcardType) args[0]);
        }

        if (!(args[1] instanceof WildcardType)) {
          vtype = args[1];
        } else {
          vtype = getFirstNotEmptyType((WildcardType) args[1]);
        }
        arguments.add(Arguments.of(type, ktype, vtype));
      }
    }

    return arguments.stream();
  }

  private static Type getFirstNotEmptyType(WildcardType type) {
    Type[] ub = type.getUpperBounds();
    return ub == null || ub.length == 0 ? type.getLowerBounds()[0] : ub[0];
  }

  @SuppressWarnings("FieldCanBeLocal")
  private static class ClassWithCollection {

    private final List<Integer> list;
    private final List<? extends Integer> upperBoundList;
    private final List<? super Integer> lowerBoundList;

    public ClassWithCollection() {
      this.list = new ArrayList<>();
      this.upperBoundList = new ArrayList<>();
      this.lowerBoundList = new ArrayList<>();
    }
  }

  @SuppressWarnings("FieldCanBeLocal")
  private static class ClassWithMap {

    private Map<String, Integer> map;
    private Map<? extends String, ? extends Integer> upperBoundMap;
    private Map<? super String, ? super Integer> lowerBoundMap;
    private Map<? extends String, ? super Integer> upperLowerBoundMap;
    private Map<? extends String, List<Integer>> upperParametrizedMap;

    public ClassWithMap() {
      this.map = new HashMap<>();
      this.upperBoundMap = new HashMap<>();
      this.lowerBoundMap = new HashMap<>();
      this.upperLowerBoundMap = new HashMap<>();
      this.upperParametrizedMap = new HashMap<>();
    }
  }
}