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

import java.io.DataOutput;
import java.io.IOException;

/**
 * <tt>BwrapperOutput</tt> interface is extension of {@link java.io.DataOutput}
 * with ability to write Java Language Objects into the stream using object's
 * serialization processing.
 *
 * <p>Also interface is an extension of {@link java.lang.AutoCloseable} for
 * enable autoclosing certain implementations in try-with-resource blocks.
 *
 * @author Alexander Podolyak
 * @version 1.0.0
 * @see java.io.DataOutput
 * @see java.lang.AutoCloseable
 * @see com.bwrapper.BWrapperOutputStream
 */
public interface BWrapperOutput extends DataOutput, AutoCloseable {

  /**
   * Write requested {@code obj} to the stream.
   *
   * @param obj
   *     target object.
   * @throws IOException
   *     if any I/O errors has occured during the writing.
   */
  void writeObject(Object obj) throws IOException;
}
