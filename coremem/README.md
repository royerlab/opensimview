[![Build Status](https://travis-ci.org/ClearControl/coremem.svg?branch=master)](https://travis-ci.org/ClearControl/coremem)

# CoreMem #

CoreMem is an library that provided access to off-heap memory, and offers
a high-level API for handling such memory. 
Moreover, Coremem facilitates interoperability with libraries such as BridJ and JNA 
that provide low-level access to native libraries.

### Basic Examples ###

Allocate an off-heap memory regions of 100 floats:

```java
OffHeapMemory.allocateFloats(100)
```

Allocate a large page-aligned memory region:

```java
OffHeapMemory.allocatePageAlignedBytes(1000000)
```

These memory regions get automatically 'freed' once you lose a reference to the object.

You can read and write different primitive types to these memory regions:

```
memory = OffHeapMemory.allocateFloats(100)
memory.setFloat(1, (byte) 255);        // the offset is in /byte/  units
memory.setFloatAligned(1, (byte) 255); // the offset is in /float/ units

memory.getDouble(1);        // the offset is in /byte/  units
memory.getIntAligned(2);    // the offset is in /int/ units (32 bits)
...
```

You can also use a 'buffer' API that keeps track of a current read and write index:

```
ContiguousBuffer buffer = ContiguousBuffer.allocate(124);
buffer.setPosition(100);
buffer.pushPosition();
buffer.writeFloat(1.0f);
buffer.writeFloat(1.0f);
buffer.writeFloat(1.0f);
buffer.writeFloat(1.0f);
buffer.popPosition();
buffer.readFloat();
...
```

There is also support for non-contiguous /fragmented/ memory regions:

```
ContiguousMemoryInterface memory = OffHeapMemory.allocateShorts(3 * 5);
FragmentedMemory lSplit = FragmentedMemory.split(lMemory, 3);
```

### Other features ###

CoreMem also supports:
- Interoperability with BridJ and JNA pointers as well as NIO buffers.
- File memory mapping.
- Recycler pattern: mechanism for reusing ressource-heavy objects like 3D stacks that use off-heap memory).
- Resource garbage collection API: design your own release-just-before-garbage-collection scheme -- inspired by how it is done in NIO.

### Maven Dependency ###

Artifact:
```
compile 'net.coremem:coremem:0.4.5'
```

Add the JCenter repository:
```
repositories {  
   jcenter()  
}
```

If you want to know what is the official latest release, check the contents of this [file](https://github.com/ClearControl/master/blob/master/master.gradle).

### How to build project with Gradle

This project uses the gradle wrapper, which means that you don't need to setup gradle on your machine,
it comes within this repo.

To build and generate an eclipse project, just do:

```
./gradlew build cleanEclipse eclipse
```

### Contributors ###

* Loic Royer (loic.royer -at- czbiohub -point- org)
