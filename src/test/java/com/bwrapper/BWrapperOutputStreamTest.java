package com.bwrapper;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.bwrapper.dataset.ArrayDataSetArguments;
import com.bwrapper.dataset.ClassDataSetArguments;
import com.bwrapper.dataset.CollectionDataSetArguments;
import com.bwrapper.dataset.CustomObjectDataSetArguments;
import com.bwrapper.dataset.DefaultSerializedObjectDataSetArguments;
import com.bwrapper.dataset.EnumDataSetArguments;
import com.bwrapper.dataset.MapDataSetArguments;
import com.bwrapper.dataset.PrimitivesDataSetArguments;
import com.bwrapper.dataset.StringDataSetArguments;
import com.bwrapper.dataset.TestData;
import com.bwrapper.dataset.TestPojo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ArgumentsSources;
import org.junit.jupiter.params.provider.NullSource;

class BWrapperOutputStreamTest {

  @ParameterizedTest
  @ArgumentsSources(value = {
      @ArgumentsSource(PrimitivesDataSetArguments.class),
      @ArgumentsSource(ArrayDataSetArguments.class),
      @ArgumentsSource(EnumDataSetArguments.class),
      @ArgumentsSource(ClassDataSetArguments.class),
      @ArgumentsSource(StringDataSetArguments.class),
      @ArgumentsSource(CollectionDataSetArguments.class),
      @ArgumentsSource(MapDataSetArguments.class),
      @ArgumentsSource(CustomObjectDataSetArguments.class),
      @ArgumentsSource(DefaultSerializedObjectDataSetArguments.class)
  })
  void givenObjects_ShouldBeWritten(TestData testData) {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
         BWrapperOutputStream bos = new BWrapperOutputStream(baos)) {
      bos.writeObject(testData.getValue());
      byte[] bytes = baos.toByteArray();

      assertAll(
          () -> assertNotNull(bytes),
          () -> assertNotEquals(0, bytes.length)
      );
    } catch (IOException e) {
      fail(e);
    }
  }

  @ParameterizedTest
  @NullSource
  void givenNullObject_ShouldThrownException(TestPojo value) {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
         BWrapperOutputStream bos = new BWrapperOutputStream(baos)) {
      assertThrows(NullPointerException.class, () -> bos.writeObject(value));
    } catch (IOException e) {
      fail(e);
    }
  }

  @Test
  void givenNotSerializableObject_ShouldThrowException() {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
         BWrapperOutputStream bos = new BWrapperOutputStream(baos)) {
      assertThrows(NotSerializableException.class,
          () -> bos.writeObject(new NotSerializable("")));
    } catch (IOException e) {
      fail(e);
    }
  }

  private static class NotSerializable  {
    private String stringVal;

    public NotSerializable(String stringVal) {
      this.stringVal = stringVal;
    }

    public String getStringVal() {
      return stringVal;
    }
  }
}