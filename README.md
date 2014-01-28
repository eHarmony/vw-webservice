[![Build Status](https://travis-ci.org/eHarmony/vw-webservice.png)](https://travis-ci.org/eHarmony/vw-webservice)

Vowpal Wabbit Webservice
=============

This is a simple web service that wraps vowpal wabbit daemon.
Below you will find a description and installation instructions.

Dependencies
------------

* Vowpal Wabbit
* Maven 2.2.1
* Jetty 9.1.10
* Java 1.7
* Protobuf 2.4.1

The current web service was developed against and tested on Jetty 9.1.0, so
these instructions are for that version of Jetty. You will also need to have Maven 2.2.1
installed in order to build the source code. Maven 3 seems to have some trouble building the source, so for now it's Maven 2.2.1.
You will also need to have Java 1.7 and the protobuf compiler (protoc) 2.4.1 installed.

Installation
------------

Vowpal wabbit
-------------

```
git clone git@github.com:JohnLangford/vowpal_wabbit.git
cd vowpal_wabbit
make

# launch daemon with the parameters you want
# for example loading a pretrained model
vowpalwabbit/vw --daemon
```

Note that by default, the VW daemon runs on port 26542 but you can specify the port you want it to run on. Refer to the VW documentation for the exact command line argument to use to do this.

Maven 2.2.1
-----------

```
wget http://mirror.tcpdiag.net/apache/maven/maven-2/2.2.1/binaries/apache-maven-2.2.1-bin.tar.gz
tar xzvf apache-maven-2.2.1-bin.tar.gz
export M2_HOME=$PWD/apache-maven-2.2.1
export PATH=$PATH:$M2_HOME/bin

# check it worked
mvn -version
```

Jetty 9.1.10
------------

On the box where you wish to run the web service, install Jettty 9.1.0.

You can download this from the Jetty website. For the rest of this README, it will be assumed that you have it installed at the location:

~/jetty-9.1.0

(but of course you can install it anywhere you like).

Protoc 2.4.1
------------

```
wget https://protobuf.googlecode.com/files/protobuf-2.4.1.tar.gz
tar -xzvf protobuf-2.4.1.tar.gz
cd protobuf-2.4.1
./configure --prefix=/usr
make
make check
sudo make install

#check that it worked - this should print 'libprotoc 2.4.1'
protoc --version
```

Now that you have all the pre-requisites set up, you can go ahead and set up the VW web service.

VW webservice
-------------

```
git clone git@github.com:eHarmony/vw-webservice.git
cd vw-webservice
```

Under src/main/resources you should find a file called 'vw-webservice.properties'. Open up this file and place the proper values for the vw host and port where you started the VW daemon. 

Note that this properties file will shortly be removed to some external location as a matter of best practice.

Now you can build the web service:

```
mvn package
```

In the output, you should see the location where the WAR (Web Application Resource) file has been created:

```
...
...
...
[INFO] Webapp assembled in[172 msecs]
[INFO] Building war: /path/to/vw-webservice/target/vw-webservice.war
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESSFUL
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 23 seconds
...
...
...
```


Now you can deploy the war file:

```
# the maven build (assuming you're using the default directories) will have spit out the WAR file to the 'target' subdirectory
# if you're running the Jetty instance on your local machine, copy the WAR over to the 'webapps' directory of Jetty
cp /path/to/vw-webservice/target/vw-webservice.war /path/to/jetty-9.1.0/webapps/

# alternatively, you can scp the war file to the box where you are running your jetty instance:
# scp /path/to/vw-webservice/target/vw-webservice.war  box.running.jetty.com:/path/to/jetty-9.1.0/webapps/

# Restart the Jetty instance (wherever you have Jetty running).
cd /path/to/jetty-9.1.0
java -jar start.jar

```

The last command will start spitting out the Jetty logs to the console. You can keep an eye on this as you submit requests to the vw-webservice which will log to the console. The web service
uses logback for logging, and the logging configuration can be found under src/main/resources/logback.xml.

Usage
-----

You can submit individual examples to the web service using curl (assuming all your VW examples are sitting in examples.txt in the directory where you're invoking curl from):

```
curl -H "Content-Type:text/plain" \
        --data-binary @examples.txt \
        http://host.running.jetty.com:8080/vw-webservice/predict \
        -v
```

If you happen to have a humongous gzipped file containing millions of examples, you can do the following:

```
gunzip -c /path/to/lotsAndLotsOfVWExamples.txt.gz | curl -H "Content-Type:text/plain" \
--data-binary @- \
http://host.running.jetty.com:8080/vw-webservice/predict \
-v
```

The '@-' tells curl to read from STDIN, while the --data-binary tells it to just leave the data as is (otherwise it will try to url encode it or do some other processing).

Examples must adhere to the documented VW example format. Some sample examples are:

```
1 first| w_2=German pre1_2=g c_0=A_fw=y c_0=A c_2=Aa suf2_2=an pre2_2=ge c_2=Aa_fw=n w_-1=<s> suf3_0=u suf1_0=u suf2_1=ts pre3_1=rej c_1=a w_1=rejects suf2_0=eu pre2_1=re suf3_1=cts suf3_2=man w_0=EU pre1_1=r pre1_0=e c_1=a_fw=n w_-2=<s> pre3_2=ger l_2=german l_0=eu pre3_0=eu pre2_0=eu suf1_1=s l_1=rejects suf1_2=n
2 second| pre3_2=cal c_0=a c_2=a_fw=n c_1=Aa suf1_2=l suf1_-1=u pre1_1=g suf3_0=cts pre1_-1=e suf1_1=n c_-1=A_fw=y suf3_-1=u pre3_0=rej suf3_1=man pre3_1=ger suf2_0=ts pre2_-1=eu pre2_2=ca pre1_2=c l_1=german w_-1=EU pre1_0=r pre2_1=ge w_2=call suf2_2=ll c_2=a pre3_-1=eu l_2=call c_1=Aa_fw=n suf1_0=s suf3_2=all w_-2=<s> l_-1=eu suf2_-1=eu w_1=German suf2_1=an c_-1=A l_0=rejects c_0=a_fw=n pre2_0=re w_0=rejects
3 third| c_2=a_fw=n suf2_0=an pre1_0=g suf2_-1=ts c_-1=a c_-2=A c_0=Aa pre3_-1=rej pre1_-2=e suf3_-2=u suf3_0=man suf3_1=all l_-1=rejects w_0=German suf3_-1=cts pre1_-1=r suf2_1=ll l_-2=eu pre2_0=ge l_0=german pre3_-2=eu c_-2=A_fw=y c_1=a pre1_2=t l_2=to suf1_0=n pre3_1=cal pre2_2=to pre3_0=ger c_2=a c_-1=a_fw=n c_0=Aa_fw=n suf3_2=o suf2_2=to w_-1=rejects c_1=a_fw=n pre1_1=c suf2_-2=eu suf1_2=o pre3_2=to w_2=to suf1_1=l pre2_1=ca pre2_-1=re w_1=call suf1_-1=s pre2_-2=eu w_-2=EU l_1=call suf1_-2=u
```

Comparing different ways of invoking VW to retrieve predictions
----

Some basic benchmarking seems to indicate that as the number of examples increases and hardware memory improves, the web-service seems to perform roughly as well as a simple netcat to VW. Note that in the generation of these numbers, no performance tweaking was done to the web-service. VW was running in daemon mode with the '-b 10' switch, ie, it was brought up in the following manner: "vw -b 10 --daemon".

A total of ~27 million examples was submitted in 10 separate runs (for each setup). 

The LP box has ~1TB (ie, 1 Terabyte) of memory.

| Setup                                 | # examples | # of features | median time | slowdown |
|:--------------------------------------|-----------:|--------------:|------------:|---------:|
| netcat and vw --daemon on localhost   | 27M        |1.2B           |      239.7s | baseline |
| webservice and vw daemon on localhost | 27M        |1.2B           |      244.4s |       2% |

The percentage difference in median times was ~2%, so the web service seemed to be performing quite nicely.


ToDo
----

* document application/x-vw-text
* more tests
* Announce ------------------------------------------
* Benchmarks
* JSON support
* protocol buffer support (JSON may be free)
* Java client
* Javascript client
* add compression support
* mvn test (use examples.txt)
* automate setup and installation
* flesh out the spring application context and get rid of spring annotations from the actual source code
* move all property configuration to some place outside the .war file, right now it's packaged inside it effectively making them hard coded
* add codahale metrics gathering
* go through all the TODO comments in the source code and make changes where necessary
* CometD support
* Speed optimizations
* Document extension points
* Re-organize project structure into client, common and server side projects
* re-factor tests to instantiate a web-service instance, perhaps using Grizzly http server?
