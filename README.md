# Bwrapper

[![Build Status](https://github.com/alexpodolyak/bwrapper/workflows/bwrapper-build/badge.svg)](https://github.com/alexpodolyak/bwrapper/workflows/bwrapper-build/badge.svg)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

This is the home page of the Bwrapper, a lightweight library 
for Java Language objects serializing.

## Table of contents
   * [Overview](#overview)
        * [Serialization](#serialization)
        * [Deserialization](#deserialization)
   * [Get Started](#get-started)
        * [Installation](#installation)
   * [Dependencies](#dependencies)    
   * [Authors](#authors)
   * [License](#license)
   
## Overview
Bwrapper provides efficient serialization of objects with less space usage.
The main goal for which library was built is 'Write only what You need'. 
Next two chapters describes the serialization and deserialization process,
***bwrapper***'s I/O streams and code examples. 

The ***Bwrapper*** is good for serialization of immutable objects with a small lifetime. 
One of the options for using this library is the transferring the object via network sockets.

### Serialization
***Bwrapper*** define ```BwrapperOutput``` as base stream-based interface for objects serializing. 
It extends ```DataOutput``` interface to allow primitive data types writing.
Along with ```DataOutput``` it extends ```AutoClosable``` interface to allow it's certain 
implementation to be closed automatically using try-with-resource block.

```java
package com.bwrapper;

import java.io.DataOutput;
import java.io.IOException;

public interface BWrapperOutput extends DataOutput, AutoCloseable {

  void writeObject(Object obj) throws IOException;
}
``` 
Interface provide only one method ```writeObject``` that is used to write an object. 
The exception thrown reflects errors that can occurs during the writing. 
It can be as writing errors as errors caused by Java Reflection API while accessing 
object's fields e.t.c.

```BwrapperOutputStream``` is the base implementation of ```BwrapperOutput```. 

The way how objects are serialized is pretty straightforward. There are two main mechanisms
defined in ```BwrapperOutputStream``` that are using in serializing:

- Class define internall methods for handle writing of most commonly used objects: primitives, arrays, strings,
collections, maps, enums and the actual classes. If object does not refer to eiher of type described above
then it will take object's fields and try to write it's values. This process will continue recursively until the
end of the object's graph is reached. Note that not all fields can be taken to serialize. 
As in ```ObjectOutputStream``` current implementation is using non-static and non-trancient fields
for writing.

- Most of JDK provided classes has special serializing/deserializing handling that can be used only by
```ObjectOutputStream```. In this case ```BwrapperOutputStream``` will use ```ObjectOutputStream```
for writing. Examples of such objects are ```LocalDateTime```, ```InetAddress``` e.t.c.

Let's write a string using ```BwrapperOutputStream```:
```java
   ByteArrayOutputStream baos = new ByteArrayOutputStream();
   BwrapperOutputStream out = new BwrapperOutputStream(baos);
   
   out.writeObject("Hello Bwrapper!");
   out.close();
```
Here ```ByteArrayOutputStream``` is used as base stream that is requested to receive the bytes.
```BwrapperOutputStream``` is created for writing that bytes. After the string "Hello Bwrapper!" 
is written to the stream. 

### Deserialization
The ```BwrapperInput``` is stream-based interface for objects deserializing.

```java
package com.bwrapper;

import java.io.DataInput;
import java.io.IOException;

public interface BWrapperInput extends DataInput, AutoCloseable {

  <T> T readObject(Class<T> actualType)
      throws IOException, ClassNotFoundException;

}
```
```readObject``` method is used for read object of certain actual type from the stream. ```actualType``` argument 
represent an actual type of object whose data is in the input stream. To read object properly ```actualType```
should refer to an specific implementation and not to an interface or abstract class since an empty instance
will be created from this type for it's futher initialization. 

Also the reading should be in order in which writing was performed. 
Otherwise object will not read correctly.

```BwrapperInputStream``` is the base implementation of ```BwrapeprInput``` interface.
The mechanisms of reading the objects is the same as writing mechanisms in ```BwrapperOutputStream```.

Before we had an example of writing an string into the byte array. Now let's read 
this string from that array.

```java
    byte[] bytes = ...;// bytes of written string from previous example.
   
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    BwrapperInputStream in = new BwrapperInputStream(bais);
    
    String str = in.readObject(String.class);
    assert str.equals("Hello Bwrapper!");

    in.close();
```
Here we takes ```bytes``` array from prevoius example where we was written "Hello Bwrapper!" string.
Now ```ByteArrayInputStream``` was created as source stream from which data will be read. ```BwrapperInputStream```
used for read that data. The line ```assert str.equals("Hello Bwrapper!");``` is used to verify read value.

## Get started

### Installation
To install library clone the repo using the command:

```cmd
git clone https://github.com/alexpodolyak/bwrapper.git
```
Go to the bwrapper's parent dir and execute command to build the project:
```cmd
./gradlew build
```
After successfull build takes the jar file from ``build/libs``
folder and add it to Your's prject classpath.

## Dependencies

Project written on pure Java Language. 
Third-party libraries using only for testing and static code analazing.

 | Dependencies |
 | :----------- |
 | [![Junit](https://img.shields.io/badge/Junit-5.6.2-green.svg)](https://img.shields.io/badge/Junit-5.6.2-green.svg) |
 | [![Mockito](https://img.shields.io/badge/Mockito-3.3.3-green.svg)](https://img.shields.io/badge/Mockito-3.3.3-green.svg) |
 | [![Checkstyle](https://img.shields.io/badge/Checkstyle-8.32-green.svg)](https://img.shields.io/badge/Checkstyle-8.32-green.svg)|

## Authors

* **Alexander Podolyak** - [alexpodolyak](https://github.com/alexpodolyak)

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE.txt) file for details.
