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

```
cd vowpal_wabbit
make clean
make

#now launch it in daemon mode (from within the vw-webservice/vowpal_wabbit directory)
./vowpalwabbit/vw --daemon [other options you like]
```

Note: Vowpal Wabbit depends on boost program options (on a Mac this can be installed via [homebrew](http://brew.sh): ``brew install boost``)

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

You can also submit examples to the web service from the command line using curl. Assuming all your VW examples are sitting in examples.txt, in the directory where you invoke curl:

```
curl    -H "Content-Type:text/plain" -X POST \
        -T examples.txt \
        http://host.running.jetty.com:8080/vw-webservice-jersey/predict/main \
        -v
```

If you happen to have a humongous gzipped file containing millions of examples (eg, ner.train.gz, included under vw-webservice-jersey/src/test/resources, which has ~272K examples), you can do the following:

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

Examples should follow the VW format. For more information on the VW input format, refer to the documentation at: https://github.com/JohnLangford/vowpal_wabbit/wiki/Input-format

For instance:

```
1 first| w_2=German pre1_2=g c_0=A_fw=y c_0=A c_2=Aa suf2_2=an pre2_2=ge c_2=Aa_fw=n w_-1=<s> suf3_0=u suf1_0=u suf2_1=ts pre3_1=rej c_1=a w_1=rejects suf2_0=eu pre2_1=re suf3_1=cts suf3_2=man w_0=EU pre1_1=r pre1_0=e c_1=a_fw=n w_-2=<s> pre3_2=ger l_2=german l_0=eu pre3_0=eu pre2_0=eu suf1_1=s l_1=rejects suf1_2=n
2 second| pre3_2=cal c_0=a c_2=a_fw=n c_1=Aa suf1_2=l suf1_-1=u pre1_1=g suf3_0=cts pre1_-1=e suf1_1=n c_-1=A_fw=y suf3_-1=u pre3_0=rej suf3_1=man pre3_1=ger suf2_0=ts pre2_-1=eu pre2_2=ca pre1_2=c l_1=german w_-1=EU pre1_0=r pre2_1=ge w_2=call suf2_2=ll c_2=a pre3_-1=eu l_2=call c_1=Aa_fw=n suf1_0=s suf3_2=all w_-2=<s> l_-1=eu suf2_-1=eu w_1=German suf2_1=an c_-1=A l_0=rejects c_0=a_fw=n pre2_0=re w_0=rejects
3 third| c_2=a_fw=n suf2_0=an pre1_0=g suf2_-1=ts c_-1=a c_-2=A c_0=Aa pre3_-1=rej pre1_-2=e suf3_-2=u suf3_0=man suf3_1=all l_-1=rejects w_0=German suf3_-1=cts pre1_-1=r suf2_1=ll l_-2=eu pre2_0=ge l_0=german pre3_-2=eu c_-2=A_fw=y c_1=a pre1_2=t l_2=to suf1_0=n pre3_1=cal pre2_2=to pre3_0=ger c_2=a c_-1=a_fw=n c_0=Aa_fw=n suf3_2=o suf2_2=to w_-1=rejects c_1=a_fw=n pre1_1=c suf2_-2=eu suf1_2=o pre3_2=to w_2=to suf1_1=l pre2_1=ca pre2_-1=re w_1=call suf1_-1=s pre2_-2=eu w_-2=EU l_1=call suf1_-2=u
```

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
* Pull out integration tests into a separate module and have Maven run them as part of the integration-test phase. Use the failsafe plugin for this.
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
