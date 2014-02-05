[![Build Status](https://travis-ci.org/eHarmony/vw-webservice.png)](https://travis-ci.org/eHarmony/vw-webservice)

# Vowpal Wabbit Webservice

This is a simple web service that wraps [vowpal wabbit](https://github.com/JohnLangford/vowpal_wabbit) daemon. 

## Installation

### Dependencies

* Java 1.7
* Maven 2.2.1 or Maven 3
* Jetty 9.1.10
* Vowpal Wabbit (included as a submodule)

The current web service was developed and tested on Jetty 9.1.0. You will need Maven (either version 2 or 3) to build the web service. Instructions for both versions have been included in this document, so pick the version of Maven you'd like to use and execute the provided instructions.

But first, we need to get the right version of Java...

#### Java 1.7

You will need Java 7 in order to run Jetty 9.1.10. Furthermore, you need the JDK (instead of the JRE) in order to run Maven. 

From the Maven documentation:

```
Make sure that JAVA_HOME is set to the location of your JDK, e.g. export JAVA_HOME=/usr/java/jdk1.7.x and that $JAVA_HOME/bin is in your PATH environment variable. 
```

Once the Java 7 JDK is ready to go (with JAVA_HOME/bin also properly set on your PATH), you can install the version of Maven you'd like to use (pick either 2.2.1 or 3.1.1 from below).

#### Maven 2.2.1

```
wget http://mirror.tcpdiag.net/apache/maven/maven-2/2.2.1/binaries/apache-maven-2.2.1-bin.tar.gz
tar xzvf apache-maven-2.2.1-bin.tar.gz
export M2_HOME=$PWD/apache-maven-2.2.1
export PATH=$M2_HOME/bin:$PATH

# check it worked
mvn -version
```

#### Maven 3.1.1

```
wget http://mirror.tcpdiag.net/apache/maven/maven-3/3.1.1/binaries/apache-maven-3.1.1-bin.tar.gz 
tar xzvf apache-maven-3.1.1-bin.tar.gz 
export M2_HOME=$PWD/apache-maven-3.1.1
export PATH=$M2_HOME/bin:$PATH

# check it worked
mvn -version
```

Now let's install Jetty which we'll use as our web container for the web service. Although technically you should be able to use the web container of your choice (Tomcat/Glassfish/etc) bear in mind that so far we've only
tested the web service using Jetty.

#### Jetty 9.1.10

On the box where you plan on running the web service, install [Jetty 9.1.0](http://eclipse.org/downloads/download.php?file=/jetty/9.1.0.v20131115/dist/jetty-distribution-9.1.0.v20131115.tar.gz&r=1).

On the command line you can use:

```
wget http://mirrors.ibiblio.org/eclipse/jetty/9.1.0.v20131115/dist/jetty-distribution-9.1.0.v20131115.tar.gz
tar xzvf jetty-distribution-9.1.0.v20131115.tar.gz
```

That's it for the prerequisites. Now you can go ahead and set up the VW web service.

### Building and Deploying the VW Web Service

This involves 3 steps:

1. Build vowpal wabbit from source, then launch it in daemon mode.
2. Specify the host and port where vowpal wabbit is running in the vw-webservice.properties file, and build+package the webservice to produce the .war (Web Application Resource) file.
3. Place the .war file into the /webapps folder of Jetty.

Let's get started.

Clone this repo:

```
git clone --recursive git@github.com:eHarmony/vw-webservice.git
cd vw-webservice
```

Note: for the --recursive option to work (it grabs the vowpal wabbit submodule for you), you will need git 1.6.5 or later. Otherwise you can pull the vowpal wabbit submodule in separately using ``git submodule``.

You should now have a vw-webservice directory with some files and 4 directories inside of it:

* vowpal_wabbit
* vw-webservice-common
* vw-webservice-core
* vw-webservice-jersey

#### Building Vowpal Wabbit

Now that you have the webservice, under the vw-webservice/vowpal_wabbit folder, you should find the C++ source for Vowpal Wabbit. Before you can launch the daemon you will have to build it.

If you're using Linux, then make sure you already have gcc and g++ installed on your system. Note also that Vowpal Wabbit depends on boost program options (on a Mac this can be installed via [homebrew](http://brew.sh): ``brew install boost`` and on Linux you can try ``sudo apt-get install -y -m libboost-program-options-dev``).

```
cd vowpal_wabbit
make clean
make

#now launch it in daemon mode (from within the vw-webservice/vowpal_wabbit directory)
./vowpalwabbit/vw --daemon [other options you like]
```

#### Building VW Web Service

Now that we have Vowpal Wabbit up and running, we just need to make sure that the web service knows the host and port where the daemon lives. Edit the config:

```
vim vw-webservice/vw-webservice-jersey/src/main/resources/vw-webservice.properties
``` 

and change if necessary:
```
vw.hostName=localhost
vw.port=26542
```

Now let's build and package up the web service:

```
mvn package
```

In the output, you should see the location where the WAR (Web Application Resource) file has been created:

```
...
...
...
[INFO] Webapp assembled in[172 msecs]
[INFO] Building war: vw-webservice/vw-webservice-jersey/target/vw-webservice-jersey.war
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESSFUL
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 23 seconds
...
...
...
```

Now you can deploy the .war file:

```
# the maven build (assuming you're using the default directories) will have spit out the WAR file to the 'target' subdirectory
# if you're running the Jetty instance on your local machine, copy the WAR over to the 'webapps' directory of Jetty
cp vw-webservice/vw-webservice-jersey/target/vw-webservice-jersey.war /path/to/jetty-9.1.0/webapps/

# alternatively, you can scp the war file to the box where you are running your jetty instance:
# scp vw-webservice/vw-webservice-jersey/target/vw-webservice-jersey.war  box.running.jetty.com:/path/to/jetty-9.1.0/webapps/

# Restart the Jetty instance (wherever you have Jetty running).
cd /path/to/jetty-9.1.0
java -jar start.jar
```

The last command will start spitting out the Jetty logs to the console. You can keep an eye on this as you submit requests to the vw-webservice, which will log to the console. The web service
uses logback for logging, and the logging configuration can be found under vw-webservice-jersey/src/main/resources/logback.xml.

## Using the Web Service

You can hit the webservice from the command line using curl, or code up your own client (in any language) to communicate with the web service. Something to keep in mind is that the client you use should support chunked transfer encoding, as this will allow you to stream massive amounts of data to/from the webservice, without buffering it all in memory to calculate the value of the Content-Length request header. A Java client that supports this is the [AsynHttpClient](http://sonatype.github.io/async-http-client/). You can find a test that uses this client in ``vw-webservice-jersey/src/test/java/AsyncHttpClientTest.java``.

Examples should follow the VW format. For more information on the VW input format, refer to the documentation at: https://github.com/JohnLangford/vowpal_wabbit/wiki/Input-format

However, when examples are submitted to the web service by a client, they can be either in plaintext format, or in a more structured format. In either case, once an example is received by the web service, it will convert the example to the proper VW format before submitting it to the VW daemon.

#### Plaintext examples

This means you will be submitting a stream of examples to the web service, with each example being a string in the accepted VW input format. 

For instance:

```
1 first|user name=Adam gender=male age=34 |movie Snatch
-1 second|user name=Adam gender=male age=34 |movie Titanic
1 third|user name=Adam gender=male age=34 |movie Hangover
```

You can submit such examples to the web service from the command line using curl. Assuming all your plaintext VW examples are sitting in some file called examples.txt, you can do the following:

```
curl    -H "Content-Type:text/plain" -X POST \
        -T examples.txt \
        http://host.running.jetty.com:8080/vw-webservice-jersey/predict/main \
        -v
```

If you happen to have a humongous gzipped file containing millions of plaintext examples (eg, ner.train.gz, included under vw-webservice-jersey/src/test/resources, which has ~272K examples), you can do the following:

```
# assume we are in the vw-webservice directory
gzcat vw-webservice-jersey/src/test/resources/ner.train.gz \
| curl  -H "Content-Type:text/plain" \
        -X POST \
        -T - \
        http://host.running.jetty.com:8080/vw-webservice-jersey/predict/main \
        -v
```

The curl '-T' switch performs a file transfer, without trying to buffer all the data in memory to compute the Content-Length HTTP request header.

Of course, you can also use any HTTP client to submit such a stream of plaintext examples to the web service. Just make sure that each example appears on a line by itself.

#### Structured examples

This means you will build up each VW example in a structured way using some API, and this structure will be reflected in the format of the data being sent to the web service.

Currently, there is a class called StructuredExample.java in the package com.eharmony.matching.vw.webservice.common.example in the vw-webservice-common project, that let's you use the Builder pattern to build up an example from it's component parts (a label, a tag, and a set of namespaces, each of which has some number of features). 

To see code that demonstrates this, check out the "simpleExampleBuildingTest" and "simpleExampleBuildingTestWithTag" tests in StructuredExampleTest.java in that same project. These tests demonstrate how to use the API to build up an example piece by piece.

Once you have an instance of a StructuredExample, you can write that out to some stream. Currently, the web service only supports the json format for submitting structured examples. In json, a stream of structured examples must have the schema described in "vw_example_schema.json" found in the same project (vw-webservice-common) under the src/test/resources folder. Note that this is the schema for the entire stream of structured json examples that will be submitted to the web service.

The serialized stream of json-formatted VW examples would look like this:

```javascript
[
{
    "label": "34",
    "tag": "someTag",
    "namespaces": [{
        "name": "one",
        "features": [{
            "name": "a",
            "value": 12.34
        }, {
            "name": "b",
            "value": 45.1
        }]
    }, {
        "name": "two",
        "scale": 34.3,
        "features": [{
            "name": "bah",
            "value": 0.038293
        }, {
            "name": "another",
            "value": 3.4
        }, {
            "name": "andThis",
            "value": 2.0
        }]
    }]
}
,
{
//the next json example
}
,
```

The first json example in the above chunk would be converted by the web service to the following before submitting to the VW daemon: "34 someTag|one a:12.34 b:45.1 |two:34.3 bah:0.038293 another:3.4 andThis:2".

To see code that shows how to write a single StructuredExample in json format, check out the "writeExample" method in JsonTestUtils.java, which can be found in the vw-webservice-jersey project under src/test/java in the com.eharmony.matching.vw.webservice.messagebodyreader.jsonexamplesmessagebodyreader package. 

To see code that writes an entire stream of StructuredExamples in json format, check out the 'getJsonInputStreamBodyGenerator' method of AsyncHttpClientTest.java in the com.eharmony.matching.vw.webservice.client package under src/test/java in the vw-webservice-jersey project.

## Benchmarks

Some basic benchmarks seems to indicate that, as the number of examples increases and hardware memory improves, the web-service seems to perform comparably to netcat. Note that we did not do any performance tweaking of the web-service. VW was running in daemon mode as "vw -b 10 --daemon", and we performed 10 runs with each setup.

| Setup                                 | # examples | # of features | median time | slowdown |
|:--------------------------------------|-----------:|--------------:|------------:|---------:|
| netcat and vw --daemon on localhost   | 27M        |1.2B           |      239.7s | baseline |
| webservice and vw daemon on localhost | 27M        |1.2B           |      244.4s |       2% |

The percentage hit in terms of median times was only about 2%, which seems acceptable.

## ToDo

* Document application/x-vw-text.
* More tests.
* Pull out integration tests into a separate module and have Maven run them as part of the verify phase. Use the failsafe plugin for this.
* Incorporate maven enforcer, findbugs and checkstyle plugin invocations
* Incorporate suggestions made by others during code review
* Protocol buffer support.
* Java client.
* Javascript client.
* Add compression support.
* Automate setup and installation.
* Move all property configuration outside the .war file. Right now the configuration is packaged inside, effectively making the .war files hard-coded.
* Add codahale metrics gathering.
* Go through all the TODO comments in the source code and make changes where necessary.
* CometD support.
* Speed optimizations.
* Document extension points.
* Re-factor tests to instantiate a web-service instance, perhaps using Grizzly http server?
