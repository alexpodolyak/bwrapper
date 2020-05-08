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
import static com.bwrapper.SerializationUtil.requiredDefaultSerialization;

import com.bwrapper.utils.Restrictions;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * An <tt>BwrapperOutputStream</tt> writes Java objects along with
 * raw primitives into the <tt>OutputStream</tt>. Written data can
 * be deserialized using appropriate {@link com.bwrapper.BwrapperInputStream}.
 *
 * <p>To achieve less space usage in comparison with default serializer
 * <tt>BwrapperOutputStream</tt> does not write any object's information except
 * it's values. In some cases along with values, class names may also be written.
 *
 * <p>Only objects that is assignable from {@link java.io.Serializable} interface
 * may be written into the stream. As there are cases when requested object will be
 * written with {@link java.io.ObjectOutputStream} this constraint may be present.
 *
 * <p>Some of commonly used types can be written directly into the stream.
 * This types are primitive data types, enums, classes, arrays, strings,
 * collections and maps. All other objects can be written by iterating throught
 * the object's non-static and non-transient fields and serialize it's values.
 * This process goes recursively until that value can be written into the stream.
 * The objects must be read back from the corresponding <tt>BwrapperInputStream</tt>
 * with the same types and in the same order as they were written.
 *
 * <p>Classes that provided by JDK has special handling during the serialization
 * and deserialization process that's why they can be written using only
 * {@link java.io.ObjectOutputStream}.
 *
 * <p>For example to write object that can be read by appropriate
 * <tt>BwrapperInputStream</tt>:
 * <pre>
 *    ByteArrayOutputStream baos = new ByteArrayOutputStream();
 *    BwrapperOutputStream bos = new BwrapperOutputStream(baos);
 *
 *    bos.writeObject(new int[] {1,2,3});
 *    bos.writeObject("Hello World!");
 *
 *    bos.close();
 * </pre>
 *
 * @author Alexander Podolyak
 * @version 1.0.0
 * @see java.io.OutputStream
 * @see com.bwrapper.BWrapperOutput
 * @see com.bwrapper.BwrapperInputStream
 */
public class BWrapperOutputStream extends OutputStream implements BWrapperOutput {

  /**
   * Main underlying stream.
   */
  private final DataOutputStream out;
  /**
   * Default object output stream which is responsible
   * to serialize objects that can be serialized by <tt>BwrapperOutputStream</tt>.
   */
  private ObjectOutputStream oos;

  public BWrapperOutputStream(OutputStream os) {
    this.out = new DataOutputStream(os);
  }

  /**
   * Writes an {@code obj} into the stream.
   *
   * <p>Writing process is delegates to the underlying
   * {@link #writeObject(Object, java.lang.reflect.Type, java.lang.reflect.Type[])}
   * method.
   *
   * @param obj
   *     target object.
   * @throws IOException
   *     if any I/O errors has occured during the reading.
   * @throws java.lang.NullPointerException
   *     if {@code obj} is {@code null}.
   * @see #writeObject(Object, java.lang.reflect.Type, java.lang.reflect.Type[])
   */
  @Override
  public void writeObject(Object obj) throws IOException {
    Restrictions.notNull(obj, "Object is null");
    writeObject(obj, obj.getClass(), null);
  }

  /**
   * Writes an object along with raw primities into this stream.
   *
   * <p>The way how object will be written depends on object's {@code type}.
   * There are several methods that can write objects of certain types:
   * <ul>
   *   <li>Primitive data types</li>
   *   <li>Arrays</li>
   *   <li>Enums</li>
   *   <li>Classes</li>
   *   <li>{@link java.lang.String}s</li>
   *   <li>{@link java.util.Collection}s</li>
   *   <li>{@link java.util.Map}s</li>
   * </ul>
   *
   * <p>Other types can be seralized in two ways:
   * <ul>
   *   <li>First way is to takes object's fields and writes
   *   it's value into the stream. Only non-transient and non-static fields
   *   will be allowed to serialize. This process goes recursively for every
   *   field until one of the existing method will be able to serialize it's value.
   *   </li>
   *   <li>Most of classes provided by JDK strictly configured for using
   *   {@link java.io.ObjectOutputStream} for serialization and cannot be
   *   serialized with other tools. Examples of such classes is {@link java.time.LocalDateTime}
   *   or {@link java.net.InetAddress} e.t.c. They has transient fields and ovverides special
   *   methods used in default stream for provide serialization. For this objects was built
   *   {@link #writeDefault(Object)} method that use {@link java.io.ObjectOutputStream} for
   *   serializing objects of such types.
   *   </li>
   * </ul>
   *
   * @param obj
   *     object to write.
   * @param type
   *     object's type.
   * @param targs
   *     object's actual type arguments (optional, may be {@code null}).
   * @throws IOException
   *     if any I/O errors has occured in the underlying methods calls.
   * @throws java.io.NotSerializableException
   *     if {@code type} is not supports serialization.
   * @see com.bwrapper.SerializationUtil#requiredDefaultSerialization(java.lang.reflect.Type)
   * @see #writeNull()
   * @see #writeEnum(Enum)
   * @see #writeClass(Class)
   * @see #writePrimitive(Object) 
   * @see #writeString(String) 
   * @see #writeArray(Object) 
   * @see #writeCollection(java.util.Collection, java.lang.reflect.Type, java.lang.reflect.Type[])
   * @see #writeMap(java.util.Map, java.lang.reflect.Type, java.lang.reflect.Type[])
   * @see #writeFields(Object)
   */
  private void writeObject(Object obj, Type type, Type[] targs)
      throws IOException {
    if (obj == null) {
      writeNull();
      return;
    }

    if (type instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) type;
      writeObject(obj, pt.getRawType(), pt.getActualTypeArguments());
    } else if (type instanceof Class<?>) {
      Class<?> actualType = (Class<?>) type;

      // base cases
      if (actualType.isEnum()) {
        writeEnum((Enum<?>) obj);
      } else if (Class.class.equals(actualType)) {
        writeClass((Class<?>) obj);
      } else if (isPrimitive(actualType)) {
        writePrimitive(obj);
      } else if (String.class.equals(actualType)) {
        writeString((String) obj);
      } else if (actualType.isArray()) {
        writeArray(obj);
      } else if (Collection.class.isAssignableFrom(actualType)) {
        writeCollection((Collection) obj, actualType, targs);
      } else if (Map.class.isAssignableFrom(actualType)) {
        writeMap((Map) obj, actualType, targs);
      } else {
        if (!(obj instanceof Serializable)) {
          throw new NotSerializableException(actualType.getName());
        }
        // remaining cases
        if (requiredDefaultSerialization(actualType)) {
          writeDefault(obj);
        } else {
          writeFields(obj);
        }
      }
    } else {
      throw new IOException("Invalid object's type: " + type.getTypeName());
    }
  }

  /**
   * Writes an class {@code obj} into this stream.
   * Classes are written in the stream using their names.
   *
   * @param obj
   *     class value to be written.
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream.
   */
  private void writeClass(Class<?> obj) throws IOException {
    writeString(obj.getName());
  }

  /**
   * Writes enum {@code obj} into this stream.
   * Enums are written into the stream using it's names.
   *
   * @param obj
   *     enum value to be written.
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream.
   */
  private void writeEnum(Enum<?> obj) throws IOException {
    writeString(obj.name());
  }

  /**
   * Writes a special flag to the stream that represeting
   * a {@code null} value.
   *
   * @throws java.io.IOException
   *     if any I/O errors has occured in the underlying stream.
   */
  private void writeNull() throws IOException {
    out.writeByte(WrapperConstants.NULL_REF);
  }

  /**
   * Writes {@code obj}ect's fields to this stream.
   *
   * <p>Which exactly fields to write decided by
   * {@link com.bwrapper.SerializationUtil#getSerialFields(Class)} method.
   * It checks for all non-static and non-transient fields. In case when
   * {@code obj} has no fields for serialization, then method calls
   * {@link #writeDefault(Object)} method to write {@code obj}.
   *
   * <p>Otherwise for every field's value {@link #writeObject(Object, java.lang.reflect.Type,
   * java.lang.reflect.Type[])} is called.
   *
   *
   * @param obj
   *     whose fields to be written.
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream
   *     or underlying methods calls.
   * @see com.bwrapper.SerializationUtil#getSerialFields(Class)
   * @see #writeDefault(Object)
   * @see #writeObject(Object, java.lang.reflect.Type, java.lang.reflect.Type[])
   */
  private void writeFields(Object obj) throws IOException {
    Field[] fields = SerializationUtil.getSerialFields(obj.getClass());
    if (fields.length == 0) {
      writeDefault(obj);
      return;
    }

    for (Field f : fields) {
      try {
        Object val = f.get(obj);
        Type vtype = f.getGenericType();
        Type[] vargs = TypeUtil.getActualTypeArguments(vtype);
        writeObject(val, vtype, vargs);
      } catch (IllegalAccessException e) {
        throw new IOException(e);
      }
    }
  }

  /**
   * Writes collection object represented by {@code obj} argument
   * to this stream.
   *
   * <p>{@code targs} represents an {@code obj}ect's actual type arguments.
   * If {@code targs} is not {@code null} or empty, then component type is
   * can be determined and it will not be written to the stream, otherwise
   * component type is determined from element themself and writes into
   * the stream.
   *
   * <p>Then 4 bytes of {@code obj}ect's size is written to the stream
   * and for each collection's elements {@link #writeObject(Object, java.lang.reflect.Type,
   * java.lang.reflect.Type[])} is called.
   *
   *
   * @param obj
   *     collection object to be written.
   * @param type
   *    collection's type.
   * @param targs
   *     collection's actual type arguments (optional, may be {@code null}).
   *
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream
   *     or underlying methods calls.
   * @see com.bwrapper.TypeUtil#getComponentType(java.util.Collection, java.lang.reflect.Type[])
   * @see com.bwrapper.SerializationUtil#requiredDefaultSerialization(java.lang.reflect.Type)
   * @see #writeDefault(Object)
   * @see #writeClassInfo(java.lang.reflect.Type)
   * @see #writeObject(Object, java.lang.reflect.Type, java.lang.reflect.Type[])
   */
  private void writeCollection(Collection obj, Type type, Type[] targs)
      throws IOException {
    Type ctype = TypeUtil.getComponentType(obj, targs);
    // move logic with writing meta into other place, after remove this lines!
    if (targs == null || targs.length != 1) {
      writeClassInfo(ctype);
    } else  {
      Class<?> actualType = TypeUtil.getRawType(type);
      if (actualType != null && actualType.isInterface()) {
        writeClassInfo(obj.getClass());
      }
    }

    out.writeInt(obj.size());
    Type[] cargs = TypeUtil.getActualTypeArguments(ctype);
    for (Object o : obj) {
      writeObject(o, ctype, cargs);
    }
  }

  /**
   * Writes map object represented by {@code obj} to this stream.
   *
   * <p>{@code targs} arguments represents an {@code obj}'s actual type
   * arguments i.e. key/value types. If {@code targs} is not {@code null}
   * or empty, then system can determine its actual type and they will not be written
   * into the stream. Otherwise actual key/value types will be taken from
   * themselves and written into the stream.
   *
   * <p>For key/value's types determination used two methods:
   * {@link com.bwrapper.TypeUtil#getMapKeyType(java.util.Map, java.lang.reflect.Type[])}
   * and
   * {@link com.bwrapper.TypeUtil#getMapValType(java.util.Map, java.lang.reflect.Type[])}
   * to get key's and value's actual types accordingly.
   *
   * @param obj
   *     map object to be written.
   * @param type
   *     map's type.
   * @param targs
   *     map's actual type arguments (optional, may be {@code null}).
   *
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream or
   *     underlying methods calls.
   * @see com.bwrapper.TypeUtil#getMapKeyType(java.util.Map, java.lang.reflect.Type[])
   * @see com.bwrapper.TypeUtil#getMapValType(java.util.Map, java.lang.reflect.Type[])
   * @see com.bwrapper.SerializationUtil#requiredDefaultSerialization(java.lang.reflect.Type)
   * @see #writeClassInfo(java.lang.reflect.Type)
   * @see #writeObject(Object, java.lang.reflect.Type, java.lang.reflect.Type[])
   */
  private void writeMap(Map<?, ?> obj, Type type, Type[] targs) throws IOException {
    Type ktype = TypeUtil.getMapKeyType(obj, targs);
    Type vtype = TypeUtil.getMapValType(obj, targs);
    if (targs == null || targs.length != 2) {
      writeClassInfo(ktype);
      writeClassInfo(vtype);
    } else {
      // if targs is null or empty, this means that methods was
      // was called to write map directly, not from field
      // then system need to write map's actual type as it cannot be
      // determined from the field that owns this value.
      Class<?> actualType = TypeUtil.getRawType(type);
      if (actualType != null && actualType.isInterface()) {
        writeClassInfo(obj.getClass());
      }
    }

    out.writeInt(obj.size());
    Type[] kargs = TypeUtil.getActualTypeArguments(ktype);
    Type[] vargs = TypeUtil.getActualTypeArguments(vtype);
    for (Map.Entry<?, ?> e : obj.entrySet()) {
      writeObject(e.getKey(), ktype, kargs);
      writeObject(e.getValue(), vtype, vargs);
    }
  }

  /**
   * Writes an array that represented by {@code obj} to this stream.
   *
   * <p>If {@code obj} represents an array of objects that can be
   * serialized by <tt>BwrapperOutputStream</tt> then writes 4 bytes
   * of array's length, and after goes through array's elements
   * and call {@link #writeObject(Object, java.lang.reflect.Type,
   * java.lang.reflect.Type[])} method for each of them.
   *
   * <p>Otherwise if array's component type cannot be serialized
   * by <tt>BwrapperOutputStream</tt> then {@link #writeDefault(Object)}
   * method is called for {@code obj} and it writes using default
   * {@link java.io.ObjectOutputStream}.
   *
   * <p>Method use {@link com.bwrapper.SerializationUtil#requiredDefaultSerialization(
   * java.lang.reflect.Type)} to checks whether {@code obj}ect's component type
   * is required to be serialized by default stream.
   *
   * @param obj
   *     an array value to write.
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream
   *     or methods calls.
   * @see com.bwrapper.SerializationUtil#requiredDefaultSerialization(java.lang.reflect.Type)
   * @see #writeDefault(Object)
   * @see #writeObject(Object, java.lang.reflect.Type, java.lang.reflect.Type[])
   */
  private void writeArray(Object obj) throws IOException {
    Class<?> ctype = obj.getClass().getComponentType();
    if (requiredDefaultSerialization(ctype)) {
      writeDefault(obj);
      return;
    }

    int len = Array.getLength(obj);
    out.writeInt(len);
    for (int i = 0; i < len; i++) {
      writeObject(Array.get(obj, i), ctype, null);
    }
  }

  /**
   * Writes string {@code obj} to this stream.
   *
   * <p>Firstly writes four bytes of length information to the
   * output stream and then calls underlying
   * {@link java.io.DataOutputStream#writeBytes(String)} method
   * to write actual string's bytes.
   *
   * @param obj
   *     string value to write.
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream.
   * @see java.io.DataOutputStream#writeInt(int)
   * @see java.io.DataOutputStream#writeBytes(String)
   */
  private void writeString(String obj) throws IOException {
    out.writeInt(obj.length());
    out.writeBytes(obj);
  }

  /**
   * Writes primitive value represented by {@code obj} to
   * this stream.
   *
   * <p>If {@code obj} refer to an primitive's wrapper class, then it will
   * be replaced by raw type i.e. {@code Integer.class} will be replaced to the
   * {@code int.class} and then writing will be performed. Replacing is done with
   * with {@link com.bwrapper.PrimitivesUtil#unwrap(Class)} method.
   *
   * <p>Main purposes of type replacing is to make easier to determine the
   * actual {@code obj}'s type.
   *
   * @param obj
   *     primitive value to be written.
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream.
   * @see com.bwrapper.PrimitivesUtil#unwrap(Class) to check how primitive's
   *     types replacing works.
   * @see java.io.DataOutputStream#writeBoolean(boolean)
   * @see java.io.DataOutputStream#writeByte(int)
   * @see java.io.DataOutputStream#writeChar(int)
   * @see java.io.DataOutputStream#writeShort(int)
   * @see java.io.DataOutputStream#writeInt(int)
   * @see java.io.DataOutputStream#writeFloat(float)
   * @see java.io.DataOutputStream#writeLong(long)
   * @see java.io.DataOutputStream#writeDouble(double)
   */
  private void writePrimitive(Object obj)
      throws IOException {
    Class<?> type = unwrap(obj.getClass());
    if (type.equals(boolean.class)) {
      out.writeBoolean((boolean) obj);
    } else if (type.equals(byte.class)) {
      out.writeByte((byte) obj);
    } else if (type.equals(char.class)) {
      out.writeChar((char) obj);
    } else if (type.equals(short.class)) {
      out.writeShort((short) obj);
    } else if (type.equals(int.class)) {
      out.writeInt((int) obj);
    } else if (type.equals(float.class)) {
      out.writeFloat((float) obj);
    } else if (type.equals(long.class)) {
      out.writeLong((long) obj);
    } else if (type.equals(double.class)) {
      out.writeDouble((double) obj);
    }
  }

  /**
   * Writes an {@code obj} to this stream using default
   * {@link java.io.ObjectOutputStream}.
   *
   * <p>If {@code obj} is not {@link java.io.Serializable} or
   * {@link java.io.Externalizable} as is required to the default stream
   * the {@link java.io.NotSerializableException} may be thrown.
   *
   * @param obj
   *     to be written.
   *
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream.
   * @see java.io.ObjectOutputStream#writeObject(Object)
   */
  private void writeDefault(Object obj) throws IOException {
    if (oos == null) {
      oos = new ObjectOutputStream(this);
    }
    oos.writeObject(obj);
  }

  /**
   * Writes an {@code type} name to this stream.
   *
   * <p>If {@code type} is instance of {@link java.lang.Class}
   * then method checks whether this class is either interface
   * or abstract class and then writes the result
   * of {@link Class#getName()} method that called on {@code type} object.
   * Otherwise no data will be writen.
   *
   * @param type
   *     target type whose name to be written.
   *
   * @throws IOException
   *     if any I/O errors has occured in the underlying methods calls.
   * @see #writeString(String)
   */
  private void writeClassInfo(Type type) throws IOException {
    if (!(type instanceof Class<?>)) {
      return;
    }
    Class<?> actualType = (Class<?>) type;
    if (actualType.isInterface()) {
      return;
    }

    // here explicitly checks whether actualType is array
    // because of Java treat array classes as abstract
    if (!Modifier.isAbstract(actualType.getModifiers())
        || actualType.isArray()) {
      writeString(actualType.getName());
    }
  }

  /**
   * Writes a {@code boolean} value, to this output stream.
   * If the argument {@code v} is {@code true},
   * the value{@code (byte) 1} is written; if {@code v}
   * is {@code false}, the value {@code (byte) 0} is written.
   *
   * @param v
   *     value to write.
   *
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream.
   * @see java.io.DataOutputStream#writeBoolean(boolean)
   */
  @Override
  public void writeBoolean(boolean v) throws IOException {
    out.writeBoolean(v);
  }

  /**
   * Writes to the output stream the eight low-order bits
   * of the argument {@code v}. The 24 high-order bits of
   * {@code v} are ignored.
   *
   * @param v
   *     the byte value to write.
   *
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream.
   * @see java.io.DataOutputStream#writeByte(int)
   */
  @Override
  public void writeByte(int v) throws IOException {
    out.writeByte(v);
  }

  /**
   * Writes two bytes to the output
   * stream to represent the value of the argument.
   *
   * @param v
   *     value to write.
   *
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream.
   * @see java.io.DataOutputStream#writeShort(int)
   */
  @Override
  public void writeShort(int v) throws IOException {
    out.writeShort(v);
  }

  /**
   * Writes a {@code char} value,
   * which is comprised of two bytes, to the output stream.
   *
   * @param v
   *     value to write.
   *
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream.
   * @see java.io.DataOutputStream#writeChar(int)
   */
  @Override
  public void writeChar(int v) throws IOException {
    out.writeChar(v);
  }

  /**
   * Writes a {@code int} value,
   * which is comprised of four bytes, to the output stream.
   *
   * @param v
   *     value to write.
   *
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream.
   * @see java.io.DataOutputStream#writeInt(int)
   */
  @Override
  public void writeInt(int v) throws IOException {
    out.writeInt(v);
  }

  /**
   * Writes a {@code long} value,
   * which is comprised of eight bytes, to the output stream.
   *
   * @param v
   *     value to write.
   *
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream.
   * @see java.io.DataOutputStream#writeLong(long)
   */
  @Override
  public void writeLong(long v) throws IOException {
    out.writeLong(v);
  }

  /**
   * Writes a {@code float} value,
   * which is comprised of four bytes, to the output stream.
   *
   * @param v
   *     value to write.
   *
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream.
   * @see java.io.DataOutputStream#writeFloat(float)
   */
  @Override
  public void writeFloat(float v) throws IOException {
    out.writeFloat(v);
  }

  /**
   * Writes a {@code double} value,
   * which is comprised of eight bytes, to the output stream.
   *
   * @param v
   *     value to write.
   *
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream.
   * @see java.io.DataOutputStream#writeDouble(double)
   */
  @Override
  public void writeDouble(double v) throws IOException {
    out.writeDouble(v);
  }

  /**
   * Writes a string to the output stream. For every character
   * in the string {@code s}, taken in order, one byte is
   * written to the output stream.
   *
   * @param s
   *     the string to write.
   *
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream.
   * @see java.io.DataOutputStream#writeBytes(String)
   */
  @Override
  public void writeBytes(String s) throws IOException {
    out.writeBytes(s);
  }

  /**
   * Writes every character in the string {@code s},
   * to the output stream, in order,
   * two bytes per character.
   *
   * @param s
   *     the string to write.
   *
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream.
   * @see java.io.DataOutputStream#writeChars(String)
   */
  @Override
  public void writeChars(String s) throws IOException {
    out.writeChars(s);
  }

  /**
   * Writes string {@code s} to this string followed by
   * <a href="DataInput.html#modified-utf-8">modified UTF-8</a>
   * representation.
   *
   * @param s
   *     string to write.
   *
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream.
   * @see java.io.DataOutputStream#writeUTF(String)
   */
  @Override
  public void writeUTF(String s) throws IOException {
    out.writeUTF(s);
  }

  /**
   * Writes single byte to this ouput stream.
   *
   * @param b
   *     the byte.
   *
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream.
   * @see java.io.DataOutputStream#write(int)
   */
  @Override
  public void write(int b) throws IOException {
    out.write(b);
  }

  /**
   * Writes {@code b.length} bytes from the specified byte array
   * starting at {@code 0} to this output stream.
   *
   * @param b
   *     the data to write.
   *
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream.
   * @see java.io.DataOutputStream#write(byte[])
   */
  @Override
  public void write(byte[] b) throws IOException {
    out.write(b);
  }

  /**
   * Writes {@code len} bytes from the specified byte array
   * starting at offset {@code off} to this output stream.
   *
   * @param b
   *     the data.
   * @param off
   *     the starting position of the data.
   * @param len
   *     the number of bytes to write from {@code b}.
   *
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream.
   * @see java.io.DataOutputStream#write(byte[], int, int)
   */
  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    out.write(b, off, len);
  }

  /**
   * Flushes underlying stream and forces any buffered output bytes
   * to be written out.
   *
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream.
   * @see java.io.DataOutputStream#flush()
   */
  @Override
  public void flush() throws IOException {
    out.flush();
  }

  /**
   * Closes underlying stream and releases all related
   * to it resources.
   *
   * @throws IOException
   *     if any I/O errors has occured in the underlying stream.
   * @see java.io.DataOutputStream#close()
   */
  @Override
  public void close() throws IOException {
    out.close();
  }
}
