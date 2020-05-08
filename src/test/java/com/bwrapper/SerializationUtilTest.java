package com.bwrapper;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

class SerializationUtilTest {

  @ParameterizedTest
  @NullSource
  void givenNull_ShouldThrowException(Class<?> value) {
    assertAll(
        () -> assertThrows(NullPointerException.class,
            () -> SerializationUtil.getSerialFields(value)),
        () -> assertThrows(NullPointerException.class,
            () -> SerializationUtil.requiredDefaultSerialization(value)),
        () -> assertThrows(NullPointerException.class,
            () -> SerializationUtil.hasSerialFields(value))
    );
  }

  @ParameterizedTest
  @MethodSource(value = "classToDefaultSerializationAbilityArguments")
  void givenClass_ShouldDetermineDefaultSerAllowability(Class<?> value, boolean isAllowed) {
    assertEquals(isAllowed, SerializationUtil.requiredDefaultSerialization(value));
  }

  @ParameterizedTest
  @MethodSource(value = "classToHasSerialFieldsIndicator")
  void givenClass_ShouldDetermineIfClassHasSerialFields(Class<?> value, boolean isAllowed) {
    assertEquals(isAllowed, SerializationUtil.hasSerialFields(value));
  }

  @ParameterizedTest
  @MethodSource(value = "classToNumberOfSerialFieldsArguments")
  void givenClass_ShouldRetreiveSerializedFields(Class<?> clazz, int numOfFilteredFields) {
    assertEquals(numOfFilteredFields, SerializationUtil.getSerialFields(clazz).length);
  }

  private static Stream<Arguments> classToNumberOfSerialFieldsArguments() {
    return Stream.of(
        Arguments.of(ClassWithSerialFields.class, 2)
    );
  }

  private static Stream<Arguments> classToHasSerialFieldsIndicator() {
    return Stream.of(
        Arguments.of(ClassWithSerialFields.class, true),
        Arguments.of(ClassWithtoutSerialFields.class, false),
        Arguments.of(InetAddress.class, false)
    );
  }

  private static Stream<Arguments> classToDefaultSerializationAbilityArguments() {
    return Stream.of(
        Arguments.of(InetAddress.class, true),
        Arguments.of(LocalDateTime.class, true),
        Arguments.of(String.class, false),
        Arguments.of(int.class, false),
        Arguments.of(HashMap.class, false)
    );
  }

  private static class ClassWithSerialFields {
    private static final String ignoredString = "";
    private transient int ignoredInt;
    private int nonIgnoredInt;
    private String nonIgnoredString;
  }

  private static class ClassWithtoutSerialFields {
    private static final String ignoredString = "";
    private transient int ignoredInt;
    private transient List<String> ignoredList;
  }

}