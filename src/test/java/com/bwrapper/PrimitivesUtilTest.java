package com.bwrapper;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;

class PrimitivesUtilTest {

  @ParameterizedTest
  @MethodSource("anyClasses")
  void givenCasse_ShouldDetermineIfItPrimitiveOrWrapper(Class<?> value, boolean isPrimitive) {
    assertEquals(isPrimitive, PrimitivesUtil.isPrimitive(value));
  }

  @ParameterizedTest
  @NullSource
  void givenNull_ShouldThrowExceptionOnIsPrimitiveMethodCall(Class<?> value) {
    assertThrows(NullPointerException.class, () -> PrimitivesUtil.isPrimitive(value));
  }

  @ParameterizedTest
  @MethodSource("wrapperToPrimitive")
  void givenWrapperClass_ShouldUnwrapToRaw(Class<?> wrapper, Class<?> expectedRaw) {
    Class<?> actualRaw = PrimitivesUtil.unwrap(wrapper);
    assertAll(
        () -> assertNotNull(actualRaw),
        () -> assertEquals(expectedRaw, actualRaw)
    );
  }

  @ParameterizedTest
  @NullSource
  void givenNull_ShouldThrowExceptionOnUnwrapMethodCall(Class<?> value) {
    assertThrows(NullPointerException.class, () -> PrimitivesUtil.unwrap(value));
  }

  @ParameterizedTest
  @MethodSource("wrapperToPrimitive")
  void givenPrimitives_ShouldLoadClassByName(Class<?> expectedWrapper, Class<?> expectedRaw)
      throws ClassNotFoundException {
    Class<?> actualWrapper = PrimitivesUtil.readPrimitiveClass(expectedWrapper.getName());
    Class<?> actualRaw = PrimitivesUtil.readPrimitiveClass(expectedRaw.getName());

    assertAll(
        () -> assertNotNull(actualWrapper),
        () -> assertNotNull(actualRaw),
        () -> assertEquals(expectedRaw, actualWrapper),
        () -> assertEquals(expectedRaw, actualRaw)
    );
  }

  @ParameterizedTest
  @MethodSource("nonPrimitives")
  void givenNonPrimitiveClassName_ShouldThrowExceptionOnReadPrmitiveClassMethodCall(
      Class<?> nonPrimitive) {
    assertThrows(IllegalArgumentException.class,
        () -> PrimitivesUtil.readPrimitiveClass(nonPrimitive.getName()));
  }

  @ParameterizedTest
  @NullAndEmptySource
  void givenBlankClassName_ShouldThrowExceptionOnReadPrimitiveClassMethodCall(String value) {
    if (value == null) {
      assertThrows(NullPointerException.class, () -> PrimitivesUtil.readPrimitiveClass(value));
    } else {
      assertThrows(IllegalArgumentException.class, () -> PrimitivesUtil.readPrimitiveClass(value));
    }
  }

  private static Stream<Arguments> wrapperToPrimitive() {
    return Stream.of(
        Arguments.of(Boolean.class, boolean.class),
        Arguments.of(Byte.class, byte.class),
        Arguments.of(Short.class, short.class),
        Arguments.of(Character.class, char.class),
        Arguments.of(Integer.class, int.class),
        Arguments.of(Float.class, float.class),
        Arguments.of(Long.class, long.class),
        Arguments.of(Double.class, double.class)
    );
  }

  private static Stream<Arguments> nonPrimitives() {
    return Stream.of(
        Arguments.of(List.class),
        Arguments.of(String.class),
        Arguments.of(BigDecimal.class),
        Arguments.of(Map.class)
    );
  }

  private static Stream<Arguments> primitives() {
    return Stream.of(
        Arguments.of(boolean.class),
        Arguments.of(byte.class),
        Arguments.of(short.class),
        Arguments.of(char.class),
        Arguments.of(int.class),
        Arguments.of(float.class),
        Arguments.of(long.class),
        Arguments.of(double.class)
    );
  }

  private static Stream<Arguments> wrappers() {
    return Stream.of(
        Arguments.of(Boolean.class),
        Arguments.of(Byte.class),
        Arguments.of(Short.class),
        Arguments.of(Character.class),
        Arguments.of(Integer.class),
        Arguments.of(Float.class),
        Arguments.of(Long.class),
        Arguments.of(Double.class)
    );
  }

  private static Stream<Arguments> anyClasses() {
    return Stream.concat(
        primitives().map(a -> Arguments.of(a.get()[0], true)),
        Stream.concat(
            wrappers().map(a -> Arguments.of(a.get()[0], true)),
            nonPrimitives().map(a -> Arguments.of(a.get()[0], false)
            )
        )
    );
  }
}