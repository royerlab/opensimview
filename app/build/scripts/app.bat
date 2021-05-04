@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  app startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and APP_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\app.jar;%APP_HOME%\lib\clearcontrol.jar;%APP_HOME%\lib\JTransformsLR.jar;%APP_HOME%\lib\libraries.jar;%APP_HOME%\lib\coremem.jar;%APP_HOME%\lib\de.mukis.jama-2.0.0.M1.jar;%APP_HOME%\lib\javacl-1.0.0-RC4.jar;%APP_HOME%\lib\jocl-2.0.1.jar;%APP_HOME%\lib\javacl-core-1.0.0-RC4.jar;%APP_HOME%\lib\opencl4java-1.0.0-RC4.jar;%APP_HOME%\lib\bridj-0.7.0.jar;%APP_HOME%\lib\jna-platform-4.5.1.jar;%APP_HOME%\lib\jblosc-1.0.1.jar;%APP_HOME%\lib\jna-4.5.1.jar;%APP_HOME%\lib\commons-collections4-4.4.jar;%APP_HOME%\lib\commons-io-2.4.jar;%APP_HOME%\lib\commons-lang3-3.4.jar;%APP_HOME%\lib\commons-math3-3.5.jar;%APP_HOME%\lib\log4j-core-2.1.jar;%APP_HOME%\lib\log4j-api-2.1.jar;%APP_HOME%\lib\reflections-0.9.10.jar;%APP_HOME%\lib\guava-18.0.jar;%APP_HOME%\lib\imglib2-algorithm-0.2.1.jar;%APP_HOME%\lib\imglib2-ij-2.0.0-beta-30.jar;%APP_HOME%\lib\imagej-common-0.12.2.jar;%APP_HOME%\lib\imglib2-roi-0.3.0.jar;%APP_HOME%\lib\trove4j-3.0.3.jar;%APP_HOME%\lib\jackson-databind-2.6.1.jar;%APP_HOME%\lib\jscience-4.3.1.jar;%APP_HOME%\lib\ejml-0.25.jar;%APP_HOME%\lib\vecmath-1.3.1.jar;%APP_HOME%\lib\jython-2.7-rc2.jar;%APP_HOME%\lib\rsyntaxtextarea-2.5.6.jar;%APP_HOME%\lib\groovy-all-2.4.3-indy.jar;%APP_HOME%\lib\mail-1.4.7.jar;%APP_HOME%\lib\seaglasslookandfeel-0.1.7.3.jar;%APP_HOME%\lib\looks-2.2.2.jar;%APP_HOME%\lib\Quaqua-7.3.4.jar;%APP_HOME%\lib\Enzo-0.3.6.jar;%APP_HOME%\lib\controlsfx-8.40.11.jar;%APP_HOME%\lib\fontawesomefx-8.0.10.jar;%APP_HOME%\lib\miglayout-3.7.4.jar;%APP_HOME%\lib\JMathPlot-1.0.1.jar;%APP_HOME%\lib\imglib2-ui-2.0.0-beta-28.jar;%APP_HOME%\lib\imglib2-realtransform-2.0.0-beta-28.jar;%APP_HOME%\lib\imglib2-2.2.1.jar;%APP_HOME%\lib\udunits-4.5.5.jar;%APP_HOME%\lib\loci-common-4.4.9.jar;%APP_HOME%\lib\gluegen-rt-2.3.2.jar;%APP_HOME%\lib\gluegen-rt-2.3.2-natives-macosx-universal.jar;%APP_HOME%\lib\gluegen-rt-2.3.2-natives-windows-amd64.jar;%APP_HOME%\lib\gluegen-rt-2.3.2-natives-linux-amd64.jar;%APP_HOME%\lib\jogl-all-2.3.2.jar;%APP_HOME%\lib\jogl-all-2.3.2-natives-macosx-universal.jar;%APP_HOME%\lib\jogl-all-2.3.2-natives-windows-amd64.jar;%APP_HOME%\lib\jogl-all-2.3.2-natives-linux-amd64.jar;%APP_HOME%\lib\jssc-2.8.0.jar;%APP_HOME%\lib\AppleJavaExtensions-1.4.jar;%APP_HOME%\lib\args4j-2.0.29.jar;%APP_HOME%\lib\junit-4.13.jar;%APP_HOME%\lib\dx-1.7.jar;%APP_HOME%\lib\jackson-annotations-2.6.0.jar;%APP_HOME%\lib\jackson-core-2.6.1.jar;%APP_HOME%\lib\javolution-5.2.3.jar;%APP_HOME%\lib\javassist-3.18.2-GA.jar;%APP_HOME%\lib\annotations-2.0.1.jar;%APP_HOME%\lib\activation-1.1.jar;%APP_HOME%\lib\JMathIO-1.0.jar;%APP_HOME%\lib\JMathArray-1.0.jar;%APP_HOME%\lib\ij-1.49p.jar;%APP_HOME%\lib\jama-1.0.3.jar;%APP_HOME%\lib\joda-time-2.2.jar;%APP_HOME%\lib\jcip-annotations-1.0.jar;%APP_HOME%\lib\slf4j-api-1.7.2.jar;%APP_HOME%\lib\hamcrest-core-1.3.jar;%APP_HOME%\lib\nativelibs4java-utils-1.6.jar;%APP_HOME%\lib\scijava-common-2.38.1.jar;%APP_HOME%\lib\gentyref-1.1.0.jar;%APP_HOME%\lib\eventbus-1.4.jar


@rem Execute app
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %APP_OPTS%  -classpath "%CLASSPATH%" dorado.main.DoradoMain %*

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable APP_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%APP_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
