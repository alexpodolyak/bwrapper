package com.bwrapper;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

class ObjectInstantiatorTest {

  @ParameterizedTest
  @NullSource
  void giveNull_ShouldThrowException(Class<?> type) {
    assertAll(
        () -> assertThrows(NullPointerException.class,
            () -> ObjectInstantiator.newInstance(type)),
        () -> assertThrows(NullPointerException.class,
            () -> ObjectInstantiator.newArrayInstance(type, 1))
    );
  }

  @Test
  void givenNegativeInt_ShouldThrowExceptionOnArrayInstanceCreation() {
    assertThrows(IllegalArgumentException.class,
        () -> ObjectInstantiator.newArrayInstance(int.class, -1));
  }

  @ParameterizedTest
  @MethodSource(value = "arrayInstanceArguments")
  void givenClassAndLenght_ShouldCreateArrayInstance(Class<?> componentType, int length) {
    Object array = ObjectInstantiator.newArrayInstance(componentType, length);

    assertAll(
        () -> assertNotNull(array),
        () -> assertEquals(length, Array.getLength(array))
    );
  }

  @ParameterizedTest
  @MethodSource(value = "objectInstanceArguments")
  void givenClass_SholdCreateInstance(Class<?> actualType)
      throws ReflectiveOperationException {
    Object instance = ObjectInstantiator.newInstance(actualType);
    assertNotNull(instance);
  }

  private static Stream<Arguments> arrayInstanceArguments() {
    return Stream.of(
        Arguments.of(Integer.class, 1),
        Arguments.of(int.class, 1),
        Arguments.of(List.class, 1)
    );
  }

  private static Stream<Arguments> objectInstanceArguments() {
    return Stream.of(
        Arguments.of(ArrayList.class),
        Arguments.of(HashMap.class),
        Arguments.of(ConcurrentHashMap.class),
        Arguments.of(HashSet.class)
    );
  }

}