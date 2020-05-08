package com.bwrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ArgumentsSources;

class BwrapperInputStreamTest {

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
  void givenObjects_ShouldBeRead(TestData testData) throws IOException {
    byte[] bytes = write(testData.getValue());
    assertNotNull(bytes);

    try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
         BwrapperInputStream bis = new BwrapperInputStream(bais)) {
      //noinspection unchecked
      Object actual = bis.readObject(testData.getType());

      assertNotNull(actual);

      if (testData.getType().isArray()) {
        assertArrayEquals(testData.getValue(), actual);
      } else {
        assertEquals(testData.getValue(), actual);
      }
    } catch (IOException | ClassNotFoundException e) {
      fail(e);
    }
  }

  @Test
  void givenEmptyInputStream_ShouldThrownException() {
    ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
    assertThrows(IllegalArgumentException.class, () -> new BwrapperInputStream(bais));
  }

  private void assertArrayEquals(Object expected, Object actual) {
    int len1 = Array.getLength(expected);
    int len2 = Array.getLength(actual);
    assertEquals(len1, len2);
    for (int i = 0; i < len1; i++) {
      assertEquals(Array.get(expected, i), Array.get(actual, i));
    }
  }

  private byte[] write(Object object) throws IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
         BWrapperOutputStream os = new BWrapperOutputStream(baos)) {
      os.writeObject(object);
      return baos.toByteArray();
    }
  }
}