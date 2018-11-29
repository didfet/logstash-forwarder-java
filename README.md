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

## Generate SSL Certificates (Logstash side)

Option 1: IP Address :
If you don't have a DNS setup—that would allow your servers, that you will gather logs from, to resolve the IP address of your ELK Server—you will have to add your ELK Server's private IP address to the subjectAltName (SAN) field of the SSL certificate that we are about to generate. To do so, open the OpenSSL configuration file:

    $ "sudo vi /etc/pki/tls/openssl.cnf"
    
Find the [ v3_ca ] section in the file, and add this line under it (substituting in the ELK Server's private IP address):

    
    subjectAltName = IP: ELK_server_private_ip 
    
Save and exit.

Now generate the SSL certificate and private key in the appropriate locations (/etc/pki/tls/), with the following commands:
    
    cd /etc/pki/tls
    sudo openssl req -config /etc/pki/tls/openssl.cnf -x509 -days 3650 -batch -nodes -newkey rsa:2048 -keyout private/logstash-forwarder.key -out certs/logstash-forwarder.crt
    

## Configure Logstash
Logstash configuration files are in the JSON-format, and reside in /etc/logstash/conf.d. The configuration consists of three sections: inputs, filters, and outputs.

Let's create a configuration file called 02-beats-input.conf and set up our "filebeat" input:


    sudo vi /etc/logstash/conf.d/02-beats-input.conf


Insert the following input configuration:


    input {
        beats {
            port => 5044
            ssl => true
            ssl_certificate => "/etc/pki/tls/certs/logstash-forwarder.crt"
            ssl_key => "/etc/pki/tls/private/logstash-forwarder.key"
        }
    }
    
Save and quit. This specifies a beats input that will listen on tcp port 5044, and it will use the SSL certificate and private key that we created earlier.

Test your Logstash configuration with this command:

    sudo service logstash configtest
    
It should display Configuration OK if there are no syntax errors. Otherwise, try and read the error output to see what's wrong with your Logstash configuration.

Restart and enable Logstash to put our configuration changes into effect:

    sudo systemctl restart logstash
    sudo chkconfig logstash on

## Create keystore for client side
If you have your CA certificate stored in a .pem (.crt with x509) text file, run the following command to create the keystore.jks file (don't worry about it password) :

    keytool -importcert -trustcacerts -file cacert.pem -alias ca -keystore keystore.jks
    
To list the contents of the keystore file, run this command (with your password) :
        
    keytool -list -v -keystore keystore.jks

## Example configuration file (config.json)

    {
      "network": {
        "servers": [ "logstash_server:5044" ],
        "ssl ca": "./keystore.jks",
        "timeout": 15
      },
      "files": [
    
        {
          "paths": [
            "/var/log/messages",
            "/var/log/*.log"
          ],
    
          "fields": { "type": "syslog" }
        }, {
          "paths": [ "-" ],
          "fields": { "type": "stdin" }
        }, {
          "paths": [
            "/var/log/apache/httpd-*.log"
          ],
          "fields": { "type": "apache" },
          "dead time": "12h" 
        }, {
          "paths": [
            "/var/log/apache/error-*.log"
          ],
          "fields": { "type": "error" },
          "multiline": { "pattern": "^[0-9]{4}", "negate": "true" },
          "dead time": "8h32m50s" 
        }
      ]
    } 
    
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
  - multiline support with attributes "pattern", "negate" (true/false) and "what" (previous/next) (version 0.2.5)
  - filtering support with attributes "pattern" and "negate" (true/false) (version 0.2.5)

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

