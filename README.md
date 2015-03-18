# logstash-forwarder-java

## What is this ?

Logstash-forwarder-java is a log shipper program written in java. This is in fact a java version of [logstash-forwarder](https://github.com/elasticsearch/logstash-forwarder) by jordansissel.
Here are a few features of this program :
  - compatible with Java 5 runtime
  - lightweight : requires only a few dependencies and few system resources
  - configuration compatible with logstash-forwarder
  - lumberjack output (including zlib compression)

## Why ?

Logstash-forwarder is written in go. This programming language is not available on all platforms (for example AIX), that's why a java version is more portable.

Logstash runs on java and provides a lumberjack output, but the file input doesn't run on all plaforms (for example AIX) and logstash requires a recent JVM. Moreover Logstash is heavier : big package and more system resources.

So logstash-forwarder-java is a solution for those who want a portable, lightweight log shipper for their ELK stack.

## How to install it ?

For the moment the only way to install logstash-forwarder-java is to download the maven project and run maven build. Next step is to distribute the logstash-forwarder-java jar and the lib directory located in the maven target directory.
I'll try to provide a tarball in the next releases.

## Differences with logstash-forwarder

### Configuration

The configuration file is the same (json format), but there are a few differences :
  - the ssl ca parameter points to a java keystore containing the root certificate of the server, not a PEM file
  - the program only uses the first server in the network section for the moment
  - comments are C-style comments

### Command-line options

Some options are the same :
  - config (but only for a file, not a directory)
  - quiet
  - idle-timeout (renamed idletimeout)
  - spool-size (renamed spoolsize)
  - help

There are a few more options :
  - debug : turn on debug logging level
  - trace : turn on trace logging level

