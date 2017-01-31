# logstash-forwarder-java

## What is this ?

Logstash-forwarder-java is a log shipper program written in java. This is in fact a java version of [logstash-forwarder](https://github.com/elasticsearch/logstash-forwarder) by jordansissel.
Here are a few features of this program :
  - compatible with Java 5 runtime
  - lightweight : package size is ~2MB and memory footprint ~8MB
  - configuration compatible with logstash-forwarder
  - lumberjack output (including zlib compression)

## Why ?

Logstash-forwarder is written in go. This programming language is not available on all platforms (for example AIX), that's why a java version is more portable.

Logstash runs on java and provides a lumberjack output, but the file input doesn't run on all plaforms (for example AIX) and logstash requires a recent JVM. Moreover Logstash is heavier : it is a big package and uses more system resources.

So logstash-forwarder-java is a solution for those who want a portable, lightweight log shipper for their ELK stack.

## How to install it ?

Download one of the following archives :
  - [logstash-forwarder-java-0.2.4-bin.zip](https://github.com/didfet/logstash-forwarder-java/releases/download/0.2.4/logstash-forwarder-java-0.2.4-bin.zip)
  - [logstash-forwarder-java-0.2.4-bin.tar.gz](https://github.com/didfet/logstash-forwarder-java/releases/download/0.2.4/logstash-forwarder-java-0.2.4-bin.tar.gz)
  - [logstash-forwarder-java-0.2.4-bin.tar.bz2](https://github.com/didfet/logstash-forwarder-java/releases/download/0.2.4/logstash-forwarder-java-0.2.4-bin.tar.bz2)

Or download the maven project and run maven package. Then you can install one of the archives located in the target directory.

## How to run it ?

Just run this command :

    java -jar logstash-forwarder-java-X.Y.Z.jar -config /path/to/config/file.json

For help run :

    java -jar logstash-forwarder-java-X.Y.Z.jar -help

## Differences with logstash-forwarder

### Configuration

The configuration file is the same (json format), but there are a few differences :
  - the ssl ca parameter points to a java [keystore](https://github.com/didfet/logstash-forwarder-java/blob/master/HOWTO-KEYSTORE.md) containing the root certificate of the server, not a PEM file
  - comments are C-style comments
  - multiline support with attributes "pattern", "negate" (true/false) and what (Previous/Next)

### Command-line options

Some options are the same :
  - config (but only for a file, not a directory)
  - quiet
  - idle-timeout (renamed idletimeout)
  - spool-size (renamed spoolsize)
  - tail
  - help

There are a few more options :
  - debug : turn on debug logging level
  - trace : turn on trace logging level
  - signaturelength : size of the block used to compute the checksum
  - logfile : send logs to this file instead of stdout
  - logfilesize : maximum size of each log file (default 10M)
  - logfilenumber : number of rotated log files (default 5)
