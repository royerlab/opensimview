# ClearCL #

[![Join the chat at https://gitter.im/ClearCL/Lobby](https://badges.gitter.im/ClearCL/Lobby.svg)](https://gitter.im/ClearCL/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Multi-backend Java Object Oriented Facade API for OpenCL. 

## Why?

OpenCL libraries come and go in Java, some are great but then one day the lead developper goes on to greener pastures and you are left with code that needs to be rewritten to take advantage of a new up-to-date library with better support. Maybe a particular library has a bug or does not support the function you need? or it does not give you access to the underlying native pointers, making difficult to process large buffers/images or interoperate with hardware? or maybe it just does not support your exotic OS of choice. To protect your code from complete rewrites ClearCL offers a very clean and complete API to write your code against. Changing backend requires just changing one line of code.   

ClearVolume 2.0 GPU code will be built on top of ClearCL to offer flexibility and robsutness against OpenCL library idiosyncrasies and eventual deprecation.

## Features:
1. Implemented backends: JOCL (www.jocl.org/) and JavaCL (github.com/nativelibs4java/JavaCL).
2. Full support of OpenCL 1.2
3. Support for offheap memory (> 2G) via CoreMem (http://github.com/ClearControl/CoreMem)
4. Automatic backend selection (different backends works better on some platforms).
5. Automatic device selection via kernel benchmarking.
6. Supports OpenCL 1.0/1.1 devices by automatically using alternative functions.

## In progress:
1. Full support for events
2. Improve robustness: ClearCL is used in the current ClearVolume ImageJ plugin.

## Planned features:
1. Basic set of OpenCL kernels for image processing: denoising, deconvolution, image quality, correlation, projection...
2. Scatter-gather for processing images and buffers that don't fit in GPU mem.
3. Upload-and-scaledown functionality to load and scale down images into GPU memory
4. Live-coding infrastructire to be able to edit kernel code and immediately see the result.

## Integration with imglib2 and FiJi
Integration with imglib2 and FiJi is done in the [ClearCLIJ](https://github.com/ClearControl/clearclij) project.

## How to add ClearCL as a dependency to your project:

Find the latest version on [BinTray](https://bintray.com/clearcontrol/ClearControl/ClearCL).
You can also find out the latest official version [here](https://github.com/ClearControl/master/blob/45a3e7956f6783eaf833d1e08ed28839f8dc0cb4/master.gradle#L32).

IMPORTANT NOTE: ClearCL has been moved from the ClearVolume org to the ClearControl org.

### With Gradle:
~~~~
     compile 'net.clearcontrol:clearcl:0.6.0'
~~~~

~~~~
repositories {
    maven {
        url  "http://dl.bintray.com/clearcontrol/ClearControl" 
    }
}
~~~~

### With Maven:
~~~~
<dependency>
  <groupId>net.clearcontrol</groupId>
  <artifactId>clearcl</artifactId>
  <version>0.6.0</version>
  <type>pom</type>
</dependency>
~~~~

~~~~
<repository>
     <snapshots>
         <enabled>false</enabled>
     </snapshots>
     <id>bintray-clearvolume-ClearVolume</id>
     <name>bintray</name>
     <url>http://dl.bintray.com/clearcontrol/ClearControl</url>
 </repository>
~~~~

## Getting started:

Just check the tests [here](https://github.com/ClearVolume/ClearCL/blob/master/src/java/clearcl/test/ClearCLTests.java) to learn how to use ClearCL. More tests are coming...


## Example:

Here is an example taken from the tests:

````java

// 
ClearCLBackendInterface lClearCLBackendInterface = ClearCLBackends.getBestBackend();

try (ClearCL lClearCL = new ClearCL(lClearCLBackendInterface))
    {

      ClearCLDevice lClearClDevice =
                                   lClearCL.getBestDevice(DeviceTypeSelector.GPU,
                                                          BadDeviceSelector.NotIntegratedIntel,
                                                          GlobalMemorySelector.MAX);

      // System.out.println(lClearClDevice.getInfoString());

      ClearCLContext lContext = lClearClDevice.createContext();

      ClearCLProgram lProgram =
                              lContext.createProgram(this.getClass(),
                                                     "test.cl");
      lProgram.addDefine("CONSTANT", "10");
      lProgram.addBuildOptionAllMathOpt();

      BuildStatus lBuildStatus = lProgram.buildAndLog();

      assertEquals(lBuildStatus, BuildStatus.Success);
      // assertTrue(lProgram.getBuildLog().isEmpty());

      ClearCLBuffer lBufferA =
                             lContext.createBuffer(HostAccessType.WriteOnly,
                                                   KernelAccessType.ReadOnly,
                                                   NativeTypeEnum.Float,
                                                   cFloatArrayLength);

      ClearCLBuffer lBufferB =
                             lContext.createBuffer(HostAccessType.WriteOnly,
                                                   KernelAccessType.ReadOnly,
                                                   NativeTypeEnum.Float,
                                                   cFloatArrayLength);

      ClearCLBuffer lBufferC =
                             lContext.createBuffer(HostAccessType.ReadOnly,
                                                   KernelAccessType.WriteOnly,
                                                   NativeTypeEnum.Float,
                                                   cFloatArrayLength);

      ClearCLKernel lKernel = lProgram.createKernel("buffersum");
      lKernel.setGlobalSizes(cFloatArrayLength);

      // System.out.println(lKernel.getSourceCode());

      // checking if include is, well , included:
      assertTrue(lKernel.getSourceCode()
                        .contains("inline float4 matrix_mult"));

      // checking if define is, well , defined:
      assertTrue(lKernel.getSourceCode().contains("CONSTANT"));

      // checking if define is, well , defined:
      assertTrue(lKernel.getSourceCode()
                        .contains("WARNING!! Could not resolve include"));

      // different ways to set arguments:
      lKernel.setArguments(11f, lBufferA, lBufferB, lBufferC);
      lKernel.run();

      lKernel.clearArguments();
      lKernel.setArgument(0, 11f);
      lKernel.setArgument(1, lBufferA);
      lKernel.setArgument(2, lBufferB);
      lKernel.setArgument(3, lBufferC);
      lKernel.run();

      //
      lKernel.clearArguments();
      lKernel.setArgument("p", 11f);
      lKernel.setArgument("a", lBufferA);
      lKernel.setArgument("b", lBufferB);
      lKernel.setArgument("c", lBufferC);
      lKernel.run();

      // what if an argument is missing but there is a default value defined?
      try
      {
        lKernel.clearArguments();
        lKernel.setArgument("a", lBufferA);
        lKernel.setArgument("b", lBufferB);
        lKernel.setArgument("c", lBufferB);
        lKernel.run();
        assertTrue(true);
      }
      catch (ClearCLArgumentMissingException e)
      {
        fail();
      }

      boolean lFailed = false;

      // what if an argument is missing?
      try
      {
        lKernel.clearArguments();
        lKernel.setArgument("p", 11f);
        lKernel.setArgument("a", lBufferA);
        lKernel.setArgument("b", lBufferB);
        lKernel.run();
        lFailed = true;
      }
      catch (RuntimeException e)
      {
        System.out.println("ALL GOOD, Caught as expected: " + e);
      }

      // what if an unknown argument is added?
      try
      {
        lKernel.clearArguments();
        lKernel.setArgument("p", 11f);
        lKernel.setArgument("a", lBufferA);
        lKernel.setArgument("b", lBufferB);
        lKernel.setArgument("c", lBufferC);
        lKernel.setArgument("z", 1.3f);
        lKernel.run();
        lFailed = true;
      }
      catch (RuntimeException e)
      {
        System.out.println("ALL GOOD, Caught as expected: " + e);
      }

      assertTrue(!lFailed);

    }

````

The corresponding kernel is here:

````java

// You can include other resources
// Path relative to class OCLlib, the package is found automatically (first in class path if several exist)
#include [OCLlib] "linear/matrix.cl" 

// You can also do absolute includes:
// Note, this is more brittle to refactoring. 
// Ideally you can move code and if the kernels 
// stay at the same location relative to the classes 
// everything is ok.
#include "clearcl/test/testinclude.cl" 

// If you include something that cannot be found, 
// then it fails silently but the final source code gets annotated. 
// (check method: myprogram.getSourceCode())
#include "blu/tada.cl" 

//default buffersum p=0f
__kernel void buffersum(         const float p,
        		            __global const float *a,
        		            __global const float *b,
        		            __global       float *c)
{
	int x = get_global_id(0);
	
	c[x] = a[x] + b[x] + p * CONSTANT;
	
	//if(x%100000==1)
	//  printf("this is a test string c[%d] = %f + %f + %f = %f \n", x, a[x], b[x], p,  c[x]);
 
}

// A kernel just to check if a kernel with a 'substring' name does not confuse ClearCL
__kernel void dummyfillimagexor(int i)
{
}

// A kernel to fill an image with beautiful garbage:
//default fillimagexor dx=0i
//default fillimagexor dy=0i
//default fillimagexor u=0f
__kernel void fillimagexor(__write_only image3d_t image, int dx, int dy, float u )
{
	int x = get_global_id(0); 
	int y = get_global_id(1);
	int z = get_global_id(2);
	
	write_imagef (image, (int4)(x, y, z, 0), u*((x+dx)^((y+dy)+1)^(z+2)));
}

````


## How to build project with Gradle

1. Clone the project
2. run the Gradle Wrapper that comes with the repo:
~~~~ 
     ./gradlew cleanEclipse eclipse build 
~~~~
     
~~~~
     ./gradlew idea build 
~~~~

## Internals & how to implement backends:

Implementing backends simply consists in implementing classes against this [interface](https://github.com/ClearVolume/ClearCL/blob/master/src/java/clearcl/backend/ClearCLBackendInterface.java).

OpenCL binding libraries such as (or wthin) JOCL, JavaCL, Jogamp, and LWJGL encapsulate native pointers/handles
using specific classes. ClearCL backends further encapsulate these within [ClearCLPeerPointers](https://github.com/ClearVolume/ClearCL/blob/master/src/java/clearcl/ClearCLPeerPointer.java). This pointer wrapper class is not exposed by the Object Oriented API but instead is only used from within the backend implementations and within the OO classes.

## Contributors

* Loic Royer ( royer -at- mpi-cbg -point- de )
* Robert Haase ( rhaase -at- mpi-cbg -point- de )
* you?
