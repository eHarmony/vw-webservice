[![Build Status](https://travis-ci.org/eHarmony/vw-webservice.png)](https://travis-ci.org/eHarmony/vw-webservice)

Vowpal Wabbit Webservice
=============

This is a simple web service that wraps vowpal wabbit daemon.
Below you will find a description and installation instructions.

Dependencies
------------

* Maven 2.2.1
* Java
* Jetty (6.2.10)
* vowpal wabbit

The current web service was developed against and tested on Jetty 6.2.10, so
these instructions are for Jetty. You will also need to have Maven 2.2.1
installed in order to build the source code.

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

Jetty
-----

On the box where you wish to run the web service, install Jettty 6.1.20 by running:

```
wget http://dist.codehaus.org/jetty/jetty-6.1.20/jetty-6.1.20.zip 
unzip jetty-6.1.20.zip
```

VW webservice
-------------

```
git clone git@github.com:eHarmony/vw-webservice.git
cd vw-webservice
mvn package
```

In the output, you should see see the location where the WAR (Web Application Resource) file has been created:
```
…
…
…
[INFO] Webapp assembled in[172 msecs]
[INFO] Building war: /Users/vrahimtoola/Desktop/Git/GitHubCorpDotCom/vw-webservice/target/vw-webservice.war
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESSFUL
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 23 seconds
[INFO] Finished at: Fri Dec 13 17:19:17 PST 2013
[INFO] Final Memory: 21M/81M
[INFO] ------------------------------------------------------------------------
```
The output of the last command will tell you which file Jetty is logging to, you can tail that file to read the output messages being produced by the VW web service.

Now you can deploy the war file:

```
# install the war file so that jetty can see it
cp target/vw-webservice.war jetty-6.1.20/webapps/
# alternatively scp war file to a box where you are running your jetty:
# scp target/vw-webservice.war  box.running.jetty.com:/path/to/jetty-6.1.20/webapps/

# Restart the Jetty instance (wherever you have Jetty running).
cd /path/to/jetty-6.1.20
./bin/jetty.sh stop
./bin/jetty.sh start
```

The output of the last command will tell you which file Jetty is logging to.
You can ``tail -f`` that file to read the output messages being produced by the VW web
service.

Usage
-----

You can submit individual examples to the web service using curl:

```
curl -H "Content-Type:text/plain" \
        --data-binary @examples.txt \
        http://host.running.jetty.com:8080/vw-webservice/predict \
        -v
```

Note that examples.txt must have this format:
```
vwExamples=first Example
secondExample
thirdExample
```
For instance:
```
vwExamples=1 | w_2=German pre1_2=g c_0=A_fw=y c_0=A c_2=Aa suf2_2=an pre2_2=ge c_2=Aa_fw=n w_-1=<s> suf3_0=u suf1_0=u suf2_1=ts pre3_1=rej c_1=a w_1=rejects suf2_0=eu pre2_1=re suf3_1=cts suf3_2=man w_0=EU pre1_1=r pre1_0=e c_1=a_fw=n w_-2=<s> pre3_2=ger l_2=german l_0=eu pre3_0=eu pre2_0=eu suf1_1=s l_1=rejects suf1_2=n
2 | pre3_2=cal c_0=a c_2=a_fw=n c_1=Aa suf1_2=l suf1_-1=u pre1_1=g suf3_0=cts pre1_-1=e suf1_1=n c_-1=A_fw=y suf3_-1=u pre3_0=rej suf3_1=man pre3_1=ger suf2_0=ts pre2_-1=eu pre2_2=ca pre1_2=c l_1=german w_-1=EU pre1_0=r pre2_1=ge w_2=call suf2_2=ll c_2=a pre3_-1=eu l_2=call c_1=Aa_fw=n suf1_0=s suf3_2=all w_-2=<s> l_-1=eu suf2_-1=eu w_1=German suf2_1=an c_-1=A l_0=rejects c_0=a_fw=n pre2_0=re w_0=rejects
3 | c_2=a_fw=n suf2_0=an pre1_0=g suf2_-1=ts c_-1=a c_-2=A c_0=Aa pre3_-1=rej pre1_-2=e suf3_-2=u suf3_0=man suf3_1=all l_-1=rejects w_0=German suf3_-1=cts pre1_-1=r suf2_1=ll l_-2=eu pre2_0=ge l_0=german pre3_-2=eu c_-2=A_fw=y c_1=a pre1_2=t l_2=to suf1_0=n pre3_1=cal pre2_2=to pre3_0=ger c_2=a c_-1=a_fw=n c_0=Aa_fw=n suf3_2=o suf2_2=to w_-1=rejects c_1=a_fw=n pre1_1=c suf2_-2=eu suf1_2=o pre3_2=to w_2=to suf1_1=l pre2_1=ca pre2_-1=re w_1=call suf1_-1=s pre2_-2=eu w_-2=EU l_1=call suf1_-2=u
```

ToDo
----

* get rid of "vwExamples=" prefix in body
* pass through empty lines (they have special meaning in vw input)
* text/plain -> text/vw
* include vowpal wabbit as a submodule?
* mvn test (use examples.txt)
* automate setup and installation
* JSON support
* protocol buffer support
