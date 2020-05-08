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

import static com.bwrapper.PrimitivesUtil.isPrimitive;
import static com.bwrapper.PrimitivesUtil.unwrap;

import com.bwrapper.utils.Restrictions;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamConstants;
import java.io.PushbackInputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

/**
 * <tt>BwrapperInputStream</tt> performs a deserialization of objects
 * that were written with appropriate to current {@link com.bwrapper.BWrapperOutputStream}
 * stream.
 *
 * <p><tt>BwrapperInputStream</tt> and it's appropiate serializer stream
 * {@link com.bwrapper.BWrapperOutputStream} provide mechanism to serialize
 * and deserialize objects with using minimum space.
 *
 * <p><tt>BwrapperInputStream</tt> ensures that object will be restored
 * correclty from the input stream if and only if deserialization is followed
 * by serialization convension. Note that deserialization should be in the
 * same order as serialization.
 *
 * <p>As <tt>bwrapper</tt> streams is built for minimize the size of
 * serialization's result, <tt>BwrapperOutputStream</tt> does not write any
 * class version or graphs of object's classes as in default's
 * {@link java.io.ObjectOutputStream}, that's why deserialization should be
 * processed preferably on immutable objects the structure of which will
 * not change during some amount of time.
 *
 * <p>One of example of using such way of serialization is passing
 * objects that has small lifetime between hosts using a socket stream.
 *
 * <p>There are cases when <tt>BwrapperInputStream</tt> used default
 * {@link java.io.ObjectInputStream} to deserialize requested object.
 * Many of classes from JDK ovverides methods for default serializing
 * and deserializing according to <b>Java Serialization Specification</b>
 * and make that classes unsupported to serialization in other ways exept default.
 * In this case when such an object is requested to deserialize, <tt>BwrapperInputStream</tt>
 * will delegate this work to the {@link java.io.ObjectOutputStream}.
 *
 * <p>Method {@link #readObject(Class)} used to read object of certain type
 * from the input stream. Note that argument that is passed to the method should
 * refer to an certain implementation and not to an interface or abstract class
 * as it used for create object's instance. This is built only to avoid
 * writing additional data about to the stream. But in some cases runtime object
 * presentation is may be written. See {@link com.bwrapper.BWrapperOutputStream} docs to know
 * more about this cases.
 *
 * <p>{@link #readObject(Class)} Method can read as object as a raw primitive.
 * Also for primitives ans strings, methods from {@link java.io.DataInput} interface
 * can be used as <tt>BwrapperInputStream</tt> implements the last one.
 *
 * <p>Using <tt>BwrapperInputStream</tt> is pretty straightforward and similar to
 * the default Java IO Streams:
 * <pre>
 *   {@code
 *    byte[] bytes = ...; // data written by <tt>BwrapperOuputStream</tt>
 *
 *    try (ByteArrayInputStream baos = new ByteArrayInputStream(bytes);
 *        BwrapperInputStream bis = new BwrapperInputStream(baos)) {
 *          List<String> list = bis.readObject(ArrayList.class);
 *           // furher list using.
 *    }
 *   }
 * </pre>
 *
 * @author Alexander Podolyak
 * @version 1.0.0
 * @see java.io.InputStream
 * @see com.bwrapper.BWrapperInput
 * @see com.bwrapper.BWrapperOutputStream
 */
public class BwrapperInputStream extends InputStream implements BWrapperInput {
  /**
   * Underlying stream used only for 'peeking'
   * operations.
   */
  private final PushbackInputStream buff;
  /**
   * Main underlying stream.
   */
  private final DataInputStream in;
  /**
   * Stream used for reading default object serialized
   * with {@link java.io.ObjectOutputStream}.
   */
  private ObjectInputStream ois;

  public BwrapperInputStream(InputStream in) throws IOException {
    this.buff = new PushbackInputStream(in, in.available());
    this.in = new DataInputStream(this.buff);
  }

  /**
   * Reads an object of requested {@code actualType} from the stream.
   *
   * <p>Actual reading is delegated to the underlying method
   * {@link #readObject(java.lang.reflect.Type, java.lang.reflect.Type[])}
   * which is more extendable because it takes {@link java.lang.reflect.Type}
   * arguments that gives ability to determines object's actual types.
   *
   * @param actualType
   *     of object that should be read from the stream.
   * @param <T>
   *     object's base type.
   * @return object of certain {@code actualType} that is read
   *     from the stream.
   * @throws IOException
   *     if any I/O errors has occured during the reading.
   * @throws ClassNotFoundException
   *     if an error has been occured during the reading.
   * @throws java.lang.NullPointerException
   *     if {@code actualType} is {@code null}.
   * @see #readObject(java.lang.reflect.Type, java.lang.reflect.Type[])
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T> T readObject(Class<T> actualType)
      throws IOException, ClassNotFoundException {
    Restrictions.notNull(actualType, "Actual type is null");
    return (T) readObject(actualType, null);
  }

  /**
   * Base method that performs reading an objects of certain {@code type}
   * from the stream.
   *
   * <p>Basically method delegates reading to other existing methods.
   * Which exactly method should be called depends on {@code type} of object
   * to read.
   *
   * @param type
   *     target object's type that should be read.
   * @param targs
   *     object's actual type arguments (optional, may be {@code null}).
   * @return object of requested {@code type} read from the stream.
   * @throws IOException
   *     if any I/O errors has occured in the underlying methods calls;
   *     if actual object's type cannot be determined from {@code type} argument.
   * @throws ClassNotFoundException
   *     if an error occured during the reading.
   * @see #readDefault()
   * @see #readEnum(Class)
   * @see #readClass()
   * @see #readPrimitive(Class)
   * @see #readString()
   * @see #readArray(Class)
   * @see #readCollection(Class, java.lang.reflect.Type[])
   * @see #readMap(Class, java.lang.reflect.Type[])
   * @see #readFields(Class, java.lang.reflect.Type[])
   */
  private Object readObject(Type type, Type[] targs) throws IOException, ClassNotFoundException {
    if (hasDefaultSerialHeader()) {
      return readDefault();
    } else if (hasNullReference()) {
      // skip null flag
      skipBytes(1);
      return null;
    }

    if (type instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) type;
      return readObject(pt.getRawType(), pt.getActualTypeArguments());
    } else if (type instanceof Class<?>) {
      Class<?> actualType = (Class<?>) type;

      if (actualType.isEnum()) {
        //noinspection unchecked
        return readEnum((Class<? extends Enum>) actualType);
      } else if (Class.class.equals(actualType)) {
        return readClass();
      } else if (isPrimitive(actualType)) {
        return readPrimitive(actualType);
      } else if (String.class.equals(actualType)) {
        return readString();
      } else if (actualType.isArray()) {
        return readArray(actualType);
      } else if (Collection.class.isAssignableFrom(actualType)) {
        return readCollection(actualType, targs);
      } else if (Map.class.isAssignableFrom(actualType)) {
        return readMap(actualType, targs);
      } else {
        if (SerializationUtil.requiredDefaultSerialization(actualType)) {
          return readDefault();
        } else {
          return readFields(actualType, targs);
        }
      }

    } else {
      throw new IOException("Invalid object's type: " + type.getTypeName());
    }
  }

  /**
   * Reads class value from this stream.
   *
   * @return class read from the stream.
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream;
   * @throws java.lang.ClassNotFoundException
   *     if read class name cannot be resolved properly.
   */
  private Class<?> readClass() throws IOException, ClassNotFoundException {
    return Class.forName(readString());
  }

  /**
   * Reads an enum object from this stream.
   *
   * <p>Enum stored in the stream by their name,
   * so method read string and convert result into enum
   * using {@link Enum#valueOf(Class, String)} method.
   *
   * @param actualType
   *     certain enum type.
   * @return enum object read from the stream.
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream.
   * @see #readString()
   * @see Enum#valueOf(Class, String)
   */
  private Enum<?> readEnum(Class<? extends Enum> actualType) throws IOException {
    String val = readString();
    //noinspection unchecked
    return Enum.valueOf(actualType, val);
  }

  /**
   * Reads serializable fields of {@code actualType} class.
   *
   * <p><tt>Serializable</tt> fields means all non-transient and non-static
   * declared fields.
   *
   * <p>In case when {@code actualType} has not declared serializable fields,
   * then object is read with {@link #readDefault()} method. Otherwise
   * method goes through the {@code actualType}'s serialized fields and
   * call {@link #readObject(java.lang.reflect.Type, java.lang.reflect.Type[])}
   * for each field to read it's value from the stream.
   *
   * @param actualType
   *     target object's type.
   * @param targs
   *     object's type arguments parameters (optional, may be {@code null}).
   * @return object of requested {@code actualType} read from the stream.
   * @throws IOException
   *     if any I/O errors has occured during the reading from the underlying stream;
   *     if {@code actualType} refer to an interface or abstract class;
   *     if {@link com.bwrapper.ObjectInstantiator#newInstance(Class)} throw error
   *     during the object's instance creation.
   * @throws ClassNotFoundException
   *     if an error has been occured in the underlying stream.
   * @see #readDefault()
   * @see #readObject(java.lang.reflect.Type, java.lang.reflect.Type[])
   */
  private Object readFields(Class<?> actualType, Type[] targs)
      throws IOException, ClassNotFoundException {
    Field[] fields = SerializationUtil.getSerialFields(actualType);
    if (fields.length == 0) {
      return readDefault();
    }

    if (isIfsOrAbstractClass(actualType)) {
      throw new IOException("Invalid actual type (interface|abstract class): "
          + actualType.getName());
    }

    try {
      Object instance = ObjectInstantiator.newInstance(actualType);
      for (Field f : fields) {
        Type valType = f.getGenericType();
        Type[] valArgs = TypeUtil.getActualTypeArguments(valType);
        Object val = readObject(valType, valArgs);
        f.set(instance, val);
      }
      return instance;
    } catch (ReflectiveOperationException e) {
      throw new IOException(e);
    }
  }

  /**
   * Reads a collection of {@code actualType} from the stream.
   *
   * <p>Firstly collecion's component type is read from the stream
   * or resolved from {@code targs} if it not {@code null} or empty.
   * The result is collection instance of requested {@code actualType}.
   *
   * @param actualType
   *     collection's actual type that will be returned.
   * @param targs
   *     collection's type arguments (optional, may be {@code null}).
   * @return collection of requested {@code actualType} read from the stream.
   * @throws IOException
   *     if any I/O errors has occured during the reading from underlying stream;
   *     if {@code actualType} is interface or abstract class;
   *     if method does not read collection's component type from the stream properly;
   *     if collection instance cannot be created propertly.
   * @throws ClassNotFoundException
   *     if an error has been occured in the underlying stream.
   */
  private Object readCollection(Class<?> actualType, Type[] targs)
      throws IOException, ClassNotFoundException {
    Type ctype;
    if (targs == null || targs.length == 0) {
      ctype = Class.forName(readString());
    } else {
      ctype = TypeUtil.getComponentType(null, targs);
      if (actualType.isInterface()) {
        actualType = Class.forName(readString());
      }
    }

    if (isIfsOrAbstractClass(actualType)) {
      throw new IOException("Invalid actual type (interface|abstract class): "
          + actualType.getName());
    }

    if (ctype == null) {
      throw new IOException("Cannot resolve collection's component type");
    }

    Type[] cargs = TypeUtil.getActualTypeArguments(ctype);
    int len = in.readInt();

    try {
      Collection instance = (Collection) ObjectInstantiator.newInstance(actualType);
      for (int i = 0; i < len; i++) {
        Object val = readObject(ctype, cargs);
        // noinspection unchecked
        instance.add(val);
      }
      return instance;
    } catch (ReflectiveOperationException e) {
      throw new IOException(e);
    }
  }

  /**
   * Reads a {@link java.util.Map} object of certain {@code actualType}
   * from the stream.
   *
   * <p>Argument {@code targs} represents an map's actual type arguments,
   * that can be retrieved using Java Reflection API. If {@code targs} is
   * {@code null} or empty, then appropriate {@link com.bwrapper.BWrapperOutputStream}
   * will writes map's key/value runtime type into the stream.
   *
   * <p>Therefore, firstly method checks whether map's key/val types
   * should be read from the stream or determined from {@code targs}.
   * Method reads 4 bytes value that represents an map's size and
   * start successive reading of key/val pairs from the stream.
   *
   * <p>Result map is the instance of {@code actualType}.
   *
   *
   * @param actualType
   *     of map that should be read.
   * @param targs
   *     map's actual type arguments. (optional, may be {@code null})
   * @return map of {@code actualType} read from the stream.
   * @throws IOException
   *     if any I/O errors has occured during the reading;
   *     if {@code actualType} refer to an interface or abstract class.
   * @throws ClassNotFoundException
   *     if an error has been occured in the underlying method calls.
   */
  private Object readMap(Class<?> actualType, Type[] targs)
      throws IOException, ClassNotFoundException {
    Type ktype;
    Type vtype;
    if (targs == null || targs.length == 0) {
      ktype = Class.forName(readString());
      vtype = Class.forName(readString());
    } else {
      ktype = TypeUtil.getMapKeyType(null, targs);
      vtype = TypeUtil.getMapValType(null, targs);
      if (actualType.isInterface()) {
        actualType = Class.forName(readString());
      }
    }

    if (isIfsOrAbstractClass(actualType)) {
      throw new IOException("Invalid actual type (interface|abstract class): "
          + actualType.getName());
    }

    if (ktype == null) {
      throw new IOException("Cannot resolve map's key type");
    }

    if (vtype == null) {
      throw new IOException("Cannot resolve map's value type");
    }

    Type[] kargs = TypeUtil.getActualTypeArguments(ktype);
    Type[] vargs = TypeUtil.getActualTypeArguments(vtype);

    int len = in.readInt();
    try {
      Map instance = (Map) ObjectInstantiator.newInstance(actualType);
      for (int i = 0; i < len; i++) {
        Object key = readObject(ktype, kargs);
        Object val = readObject(vtype, vargs);
        //noinspection unchecked
        instance.put(key, val);
      }
      return instance;
    } catch (ReflectiveOperationException e) {
      throw new IOException(e);
    }
  }

  /**
   * Reads an array from the stream.
   *
   * <p>The first 4 bytes read represents an array size.
   * After method reads that number of object's from the stream.
   * The result is an array instance of {@code actualType}.
   *
   * @param actualType
   *     represents an array type i.e. {@code int[].class}
   *     or {@code String[].class}
   *
   * @return array of {@code actualType} read from the stream.
   * @throws IOException
   *     if any I/O errors has occured during the reading.
   * @throws ClassNotFoundException
   *     if an error has been occured in the underlying method calls.
   */
  private Object readArray(Class<?> actualType) throws IOException, ClassNotFoundException {
    Class<?> ctype = actualType.getComponentType();
    int len = in.readInt();

    Object instance = ObjectInstantiator.newArrayInstance(ctype, len);
    for (int i = 0; i < len; i++) {
      Array.set(instance, i, readObject(ctype, null));
    }
    return instance;
  }

  /**
   * Reads an {@link java.lang.String} value from the stream.
   *
   * <p>The first 4 bytes read represents the string's length.
   * It reads that length of bytes and then they returned as
   * utf-8 encoded string.
   *
   * @return string value read from the stream.
   * @throws IOException
   *     if any I/O errors has occured during the read.
   */
  private String readString() throws IOException {
    byte[] buffer = new byte[in.readInt()];
    in.readFully(buffer);
    return new String(buffer, StandardCharsets.UTF_8);
  }

  /**
   * Reads any primitive value of {@code actualType} from
   * the input stream.
   *
   * <p>{@link com.bwrapper.BWrapperOutputStream} writes only raw primitive
   * types even if wrapper object is requested to write, so
   * firstly {@code actualType} is unwraps to the raw in case when
   * {@code actualType} refers to an primitive wrapper class.
   *
   * @param actualType
   *     the primitive type (raw or wrapper).
   * @return raw primitive value read from the stream.
   * @throws IOException
   *     if any I/O errors has occured during the reading;
   *     if {@code actualType} does not refer to an primitve type.
   */
  private Object readPrimitive(Class<?> actualType) throws IOException {
    Class<?> raw = unwrap(actualType);
    if (raw.equals(boolean.class)) {
      return in.readBoolean();
    } else if (raw.equals(byte.class)) {
      return in.readByte();
    } else if (raw.equals(char.class)) {
      return in.readChar();
    } else if (raw.equals(short.class)) {
      return in.readShort();
    } else if (raw.equals(int.class)) {
      return in.readInt();
    } else if (raw.equals(float.class)) {
      return in.readFloat();
    } else if (raw.equals(long.class)) {
      return in.readLong();
    } else if (raw.equals(double.class)) {
      return in.readDouble();
    }

    throw new IOException("Invalid primitive type: " + raw.getName());
  }

  /**
   * Reads object from the input stream using default
   * {@link java.io.ObjectInputStream}.
   *
   * @return object read from the stream.
   * @throws IOException
   *     if any I/O errors has occured during the reading.
   * @throws ClassNotFoundException
   *     if an error has been occured in the {@link #ois} stream.
   * @see java.io.ObjectInputStream#readObject()
   */
  private Object readDefault() throws IOException, ClassNotFoundException {
    if (ois == null) {
      ois = new ObjectInputStream(this);
    }
    return ois.readObject();
  }

  /**
   * Reads some bytes from an input stream and
   * stores them into the buffer array {@code b}.
   * The number of bytes read is equal to the
   * length of {@code b}.
   *
   * @param b
   *     the buffer into which the data read.
   * @throws IOException
   *     if any I/O errors has occured.
   * @see java.io.DataInputStream#readFully(byte[])
   */
  @Override
  public void readFully(byte[] b) throws IOException {
    in.readFully(b);
  }

  /**
   * Reads up to {@code len} bytes of data from the contained
   * input stream into an array of bytes.
   *
   * @param b
   *     the buffer into which the data read.
   * @param off
   *     the start offset in the destination array {@code b}.
   * @param len
   *     the maximum number of bytes to be read into {@code b}.
   *
   * @throws IOException
   *     if any I/O errors has occured.
   * @see java.io.DataInputStream#readFully(byte[], int, int)
   */
  @Override
  public void readFully(byte[] b, int off, int len) throws IOException {
    in.readFully(b, off, len);
  }

  /**
   * Makes an attempt to skip over {@code n} bytes
   * of data from the input stream, discarding
   * the skipped bytes.
   *
   * @param n
   *     the number of bytes to skip.
   * @return actual number of bytes skipped.
   * @throws IOException
   *     if any I/O errors has occured.
   * @see java.io.DataInputStream#skipBytes(int)
   */
  @Override
  public int skipBytes(int n) throws IOException {
    return in.skipBytes(n);
  }

  /**
   * Reads one input byte and returns
   * {@code true} if that byte is nonzero,
   * {@code false} if that byte is zero.
   *
   * @return boolean value read from the stream.
   * @throws IOException
   *     if any I/O errors has occured during the reading.
   * @see java.io.DataInputStream#readBoolean()
   */
  @Override
  public boolean readBoolean() throws IOException {
    return in.readBoolean();
  }

  /**
   * Reads and returns one input byte.
   *
   * @return byte value read from the stream.
   * @throws IOException
   *     if any I/O errors has occured during the reading.
   * @see java.io.DataInputStream#readByte()
   */
  @Override
  public byte readByte() throws IOException {
    return in.readByte();
  }

  /**
   * Reads one input byte, zero-extends
   * it to type {@code int}, and returns
   * the result, which is therefore in the range
   * {@code 0}
   * through {@code 255}.
   *
   * @return the unsigned 8-bit value read from the stream.
   * @throws IOException
   *     if any I/O errors has occured during the reading.
   * @see java.io.DataInputStream#readUnsignedByte()
   */
  @Override
  public int readUnsignedByte() throws IOException {
    return in.readUnsignedByte();
  }

  /**
   * Reads short value from the stream.
   *
   * @return the 16-bit value read from the stream.
   * @throws IOException
   *     if any I/O errors has occured during the reading.
   * @see java.io.DataInputStream#readShort()
   */
  @Override
  public short readShort() throws IOException {
    return in.readShort();
  }

  /**
   * Reads two input bytes and returns
   * an {@code int} value in the range {@code 0}
   * through {@code 65535}.
   *
   * @return the unsigned 16-bit value read from the stream.
   * @throws IOException
   *     if any I/O errors has occured during the reading.
   * @see java.io.DataInputStream#readUnsignedShort()
   */
  @Override
  public int readUnsignedShort() throws IOException {
    return in.readUnsignedShort();
  }

  /**
   * Reads characted value from the stream.
   *
   * @return character value read from the stream.
   * @throws IOException
   *     if any I/O errors has occured during the reading.
   * @see java.io.DataInputStream#readChar()
   */
  @Override
  public char readChar() throws IOException {
    return in.readChar();
  }

  /**
   * Reads int value from the stream.
   *
   * @return int value read from the stream.
   * @throws IOException
   *     if any I/O errors has occured during the reading.
   * @see java.io.DataInputStream#readInt()
   */
  @Override
  public int readInt() throws IOException {
    return in.readInt();
  }

  /**
   * Reads long value from the stream.
   *
   * @return long value read from the stream.
   * @throws IOException
   *     if any I/O errors has occured during the reading.
   * @see java.io.DataInputStream#readLong()
   */
  @Override
  public long readLong() throws IOException {
    return in.readLong();
  }

  /**
   * Reads float value from the stream.
   *
   * @return float value read from the stream.
   * @throws IOException
   *     if any I/O errors has occured during the reading.
   * @see java.io.DataInputStream#readFloat()
   */
  @Override
  public float readFloat() throws IOException {
    return in.readFloat();
  }

  /**
   * Reads double value from the stream.
   *
   * @return double value read from the stream.
   * @throws IOException
   *     if any I/O errors has occured during the reading.
   * @see java.io.DataInputStream#readDouble()
   */
  @Override
  public double readDouble() throws IOException {
    return in.readDouble();
  }

  /**
   * Reads the next line of text from the input stream.
   * It reads successive bytes, converting each byte
   * separately into a character, until it encounters
   * a line terminator or end of file; the characters
   * read are then returned as a {@code String}.
   * Note that because this method processes bytes,
   * it does not support input of the full Unicode
   * character set.
   *
   * @return the next line of text from the underlying stream.
   * @throws IOException
   *     if any I/O errors has occured during the reading.
   * @deprecated This method does not properly convert bytes
   *     to characters.
   * @see java.io.DataInputStream#readLine()
   */
  @Override
  @Deprecated
  public String readLine() throws IOException {
    return in.readLine();
  }

  /**
   * Reads in a string that has been encoded using a
   * <a href="#modified-utf-8">modified UTF-8</a>
   * format.
   *
   * <p>This method returns result of
   * {@link java.io.DataInputStream#readUTF()}.
   *
   * @return a Unicode string.
   * @throws IOException
   *     if any I/O errors has occured during the reading.
   * @see java.io.DataInputStream#readUTF()
   */
  @Override
  public String readUTF() throws IOException {
    return in.readUTF();
  }

  /**
   * Reads the next byte of data from this input stream. The value
   * byte is returned as an {@code int} in the range
   * {@code 0} to {@code 255}. If no byte is available
   * because the end of the stream has been reached, the value
   * {@code -1} is returned.
   *
   * @return the next byte of data from the underlying stream,
   *     or {@code -1} if the end of the stream has been reached.
   * @throws IOException
   *     if any I/O errors has occured.
   * @see java.io.DataInputStream#read()
   */
  @Override
  public int read() throws IOException {
    return in.read();
  }

  /**
   * Reads some number of bytes from the contained input stream and
   * stores them into the buffer array {@code b}. The number of
   * bytes actually read is returned as an integer. This method blocks
   * until input data is available, end of file is detected, or an
   * exception is thrown.
   *
   * @param b
   *     the buffer into which the data is read.
   * @return the maximum number of bytes to be read into {@code b}.
   * @throws IOException
   *     if any I/O errors has occured during the reading.
   * @see java.io.DataInputStream#read(byte[])
   */
  @Override
  public int read(byte[] b) throws IOException {
    return in.read(b);
  }

  /**
   * Reads up to {@code len} bytes of data from the contained
   * input stream into an array of bytes. An attempt is made to read
   * as many as {@code len} bytes, but a smaller number may be read,
   * possibly zero. The number of bytes actually read is returned as an
   * integer.
   *
   * @param b
   *     the buffer into which the data is read.
   * @param off
   *     the start offset in the destination array {@code b}.
   * @param len
   *     the maximum number of bytes to be read into {@code b}.
   * @return the actual number of bytes read into buffer, or {@code -1}
   *     if end of the stream has been reached.
   * @throws IOException
   *     if any I/O errors has occured during the reading.
   * @see java.io.DataInputStream#read(byte[], int, int)
   */
  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    return in.read(b, off, len);
  }

  /**
   * Skips over and discards {@code n} bytes of data from this input
   * stream. Current method may, for a variety of reasons, end
   * up skipping over some smaller number of bytes, possibly {@code 0}.
   *
   * @param n
   *     number of bytes to skip.
   * @return the actual number of bytes skipped.
   * @throws IOException
   *     if any I/O errors has occured.
   * @see java.io.DataInputStream#skip(long)
   */
  @Override
  public long skip(long n) throws IOException {
    return in.skip(n);
  }

  /**
   * Returns an estimate of the number of bytes that can be read (or
   * skipped over) from this input stream without blocking by the next
   * caller of a method for this input stream. The next caller might be
   * the same thread or another thread. A single read or skip of this
   * many bytes will not block, but may read or skip fewer bytes.
   *
   * <p>This method returns the result of underlying {@link java.io.DataInputStream#available()}
   * method.
   *
   * @return an estimane of the number of bytes that can be read
   *     from underlying input stream.
   * @throws IOException
   *     if any I/O errors has occured.
   * @see java.io.DataInputStream#available()
   */
  @Override
  public int available() throws IOException {
    return in.available();
  }

  /**
   * Closes and releases any system resources
   * associated with the underlying streams.
   *
   * @throws IOException
   *     if any I/O errors has occured during the closing.
   * @see java.io.PushbackInputStream#close()
   * @see java.io.DataInputStream#close()
   */
  @Override
  public void close() throws IOException {
    buff.close();
    in.close();
  }

  /**
   * Marks the current position in this input stream. A subsequent call to
   * the {@link #reset()} method repositions this stream at the last marked
   * position so that subsequent reads re-read the same bytes.
   *
   * <p>The {@code readLimit} argument tells this input stream to
   * allow that many bytes to be read before the mark position gets
   * invalidated.
   *
   * @param readlimit
   *     tells to the underlying stream to allow
   *     that many bytes to be read before mark
   *     position gets invalidated.
   * @see java.io.DataInputStream#mark(int)
   */
  @Override
  public synchronized void mark(int readlimit) {
    in.mark(readlimit);
  }

  /**
   * Repositions this stream to the position at the time the
   * {@link #mark(int)} method was last called on this input stream.
   *
   * @throws IOException
   *     if this stream has not been marked or if the
   *     mark has been invalidated.
   * @see java.io.DataInputStream#reset()
   */
  @Override
  public synchronized void reset() throws IOException {
    in.reset();
  }

  /**
   * Checks whether underlying buffer supports
   * {@link #mark(int)} and {@link #reset()} methods.
   *
   * @return {@code true} if underlying stream supports
   *     {@link #mark(int)} and {@link #reset()} methods.
   * @see java.io.DataInputStream#markSupported()
   */
  @Override
  public boolean markSupported() {
    return in.markSupported();
  }

  /**
   * Peeks short value from the stream using the underlying pushback {@link #buff}
   * and checks whether that value is represent default serializer's
   * {@link java.io.ObjectStreamConstants#STREAM_MAGIC} header.
   *
   * @return {@code true} if peeked short value from the stream represents
   *     a default serializer's magic number, otherwise return {@code short}.
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream
   *     during the reading and pushing back the short value.
   */
  final boolean hasDefaultSerialHeader() throws IOException {
    if (buff.available() < 2) {
      return false;
    }
    int b1 = buff.read();
    int b2 = buff.read();

    short magic = (short) ((b1 << 8) + b2);
    buff.unread(b2);
    buff.unread(b1);
    return magic == ObjectStreamConstants.STREAM_MAGIC;

  }

  /**
   * Checks whether {@code type} is interface or abstract class.
   *
   * @param type
   *     target class to check.
   * @return {@code true} if {@code type} is interface or abstarct class,
   *     otherwise return {@code false}.
   */
  private boolean isIfsOrAbstractClass(Class<?> type) {
    return type.isInterface() || Modifier.isAbstract(type.getModifiers());
  }

  /**
   * Peeks one byte from the stream and checks whether
   * this byte is special {@link com.bwrapper.WrapperConstants#NULL_REF}.
   * After peeking that byte is pushes back to the stream.
   *
   * @return {@code true} if next byte in the stream refer
   *     to an special {@code null} reference flag. Otherwise
   *     return {@code false}.
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream.
   */
  private boolean hasNullReference() throws IOException {
    byte flag = (byte) buff.read();
    buff.unread(flag);
    return flag == WrapperConstants.NULL_REF;
  }
}
