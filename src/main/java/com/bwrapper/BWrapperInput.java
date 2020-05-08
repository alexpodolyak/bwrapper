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

import java.io.DataInput;
import java.io.IOException;

/**
 * <tt>BwrapperInput</tt> interface is extension of {@link java.io.DataInput}
 * with ability to read objects of certain types.
 *
 * <p>Also interface extends {@link java.lang.AutoCloseable} interface
 * to provide automatically stream closing in try-with-resouce blocks.
 *
 * @author Alexander Podolyak
 * @version 1.0.0
 * @see java.io.DataInput
 * @see java.lang.AutoCloseable
 * @see com.bwrapper.BwrapperInputStream
 */
public interface BWrapperInput extends DataInput, AutoCloseable {

  /**
   * Read object of certain {@code actualType} from the stream.
   *
   * <p><b>Note: </b> {@code actualType} should not refer to an
   * interface or abstract class as type is used in object
   * instantination process.
   *
   * @param actualType
   *     of object that should be read from the stream.
   * @param <T>
   *     object's type.
   *
   * @return object of certain {@code actualType} read from the stream.
   * @throws IOException
   *     if any I/O errors has occured during the reading.
   * @throws ClassNotFoundException
   *     if any instantination errors has occured during the reading.
   */
  <T> T readObject(Class<T> actualType)
      throws IOException, ClassNotFoundException;

}
