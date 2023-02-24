https://github.com/Eliyahug66/TFRecordsRW
# TFRreader, TFRwriter
### Java fast & simple serialization of general Tensorflow Records
#### Author: [Eliyahu Greitzer](mailto:egreitzer@gmail.com)
#### Version: 3.2
## Motivation
Having duplicate preprocessing code, one in Python, for training and research, and
the other in Java (for speed, in production), may result in significant human effort due to the slight differences that
occur. Once the preprocessing gained a relative stability, one may want to have a single fast Java version for the preprocessing, and serialize the result straight into Tensorflow records files.
As we weren't able to find a general ready to use classes for serialization of our TFRs, we created these classes.

## Details
* Built on top of org.tensorflow read/write methods in order to facilitate usage.
* Original version used for labeled TFRs, each describes a patient medical history prior to an event.
* Supports both 'context' and 'sequence' features of various types, with optional padding of the latter.
* Infers compression and reads/writes accordingly.

## Dependencies
* commons-io/commons-io
* com.google.protobuf/protobuf-java
* org.tensorflow/proto
* org.tensorflow/tensorflow-hadoop

## Usage
See Test.java for an extra simple example:
1. Generation of Java maps with two hypothetical input histories 
2. Converting them into TensorFlowRecords bytes streams
3. Saving them to a single on-disk tfr file (readable by Tensorflow classes)
4. Reading the tfr file to memory while conversing it back to Java maps

## Known issues
* Error message: "Could not find artifact jdk.tools:jdk.tools:jar:1.6 at specified path */.jdks/openjdk-19.0.2/../lib/tools.jar"
  * If you solve it tell me how. Otherwise - just ignore it.

### License
MIT © [Eliyahu_Greitzer](https://www.facebook.com/eliyahu.greitzer)
