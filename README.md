This document contains instructions on how to set up the VW-WebService. 

The current web service was developed against and tested on Jetty 6.2.10, so these instructions are for Jetty. You will also need to have Maven 2.2.1 installed in order to build the source code.

Step 1:

On the box where you wish to run the web service, install Jettty 6.1.20. From your command line, do this:

~> wget http://dist.codehaus.org/jetty/jetty-6.1.20/jetty-6.1.20.zip
~> unzip jetty-6.1.20.zip

In the folder where you executed the above commands, you should now have a "jetty-6.1.20" folder.

Step 2:

On your local machine, grab the code for the VW web service:

~> git clone git@github.corp.eharmony.com:matching/vw-webservice.git

In the folder where you executed the above, you should now see a "vw-webservice" folder.

Step 3:

On your local machine, navigate to the "vw-webservice" folder you set up from Step 2. Then create the WAR (Web Application Resource) package:

~> cd /path/to/vw-webservice
~/path/to/vw-webservice> mvn package

In the maven output, you should see see the location where the WAR (Web Application Resource) file has been created:
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

Step 4:

Copy the generated .war file to the jetty-6.1.20/webapps folder.

If you're running Jetty on the same box where you built the vw-webservice.war file, you can do this manually. Otherwise, you can use 'scp' to copy the WAR file
to the box where Jetty is running (specifically, the /webapps folder under the jetty-6.1.20 folder).

~> scp /Users/vrahimtoola/Desktop/Git/GitHubCorpDotCom/vw-webservice/target/vw-webservice.war  boxthatIsRunningJetty:/path/to/jetty-6.1.20/webapps

Step 5:

Restart the Jetty instance (wherever you will be running Jetty).

~> cd /path/to/jetty-6.1.20
~> ./bin/jetty.sh stop
~> ./bin/jetty.sh start

The output of the last command will tell you which file Jetty is logging to, you can tail that file to read the output messages being produced by the VW web service.

--------------

Testing the web service:

You can try to submit individual examples to the web service using curl. Example:

~> curl -H "Content-Type:text/plain" --data-binary @/path/to/examples.txt http://boxRunningJetty:8080/vw-webservice/predict -v

Note that your examples.txt must have this format:

vwExamples=first Example
secondExample
thirdExample

For instance:

vwExamples=1 | w_2=German pre1_2=g c_0=A_fw=y c_0=A c_2=Aa suf2_2=an pre2_2=ge c_2=Aa_fw=n w_-1=<s> suf3_0=u suf1_0=u suf2_1=ts pre3_1=rej c_1=a w_1=rejects suf2_0=eu pre2_1=re suf3_1=cts suf3_2=man w_0=EU pre1_1=r pre1_0=e c_1=a_fw=n w_-2=<s> pre3_2=ger l_2=german l_0=eu pre3_0=eu pre2_0=eu suf1_1=s l_1=rejects suf1_2=n
2 | pre3_2=cal c_0=a c_2=a_fw=n c_1=Aa suf1_2=l suf1_-1=u pre1_1=g suf3_0=cts pre1_-1=e suf1_1=n c_-1=A_fw=y suf3_-1=u pre3_0=rej suf3_1=man pre3_1=ger suf2_0=ts pre2_-1=eu pre2_2=ca pre1_2=c l_1=german w_-1=EU pre1_0=r pre2_1=ge w_2=call suf2_2=ll c_2=a pre3_-1=eu l_2=call c_1=Aa_fw=n suf1_0=s suf3_2=all w_-2=<s> l_-1=eu suf2_-1=eu w_1=German suf2_1=an c_-1=A l_0=rejects c_0=a_fw=n pre2_0=re w_0=rejects
3 | c_2=a_fw=n suf2_0=an pre1_0=g suf2_-1=ts c_-1=a c_-2=A c_0=Aa pre3_-1=rej pre1_-2=e suf3_-2=u suf3_0=man suf3_1=all l_-1=rejects w_0=German suf3_-1=cts pre1_-1=r suf2_1=ll l_-2=eu pre2_0=ge l_0=german pre3_-2=eu c_-2=A_fw=y c_1=a pre1_2=t l_2=to suf1_0=n pre3_1=cal pre2_2=to pre3_0=ger c_2=a c_-1=a_fw=n c_0=Aa_fw=n suf3_2=o suf2_2=to w_-1=rejects c_1=a_fw=n pre1_1=c suf2_-2=eu suf1_2=o pre3_2=to w_2=to suf1_1=l pre2_1=ca pre2_-1=re w_1=call suf1_-1=s pre2_-2=eu w_-2=EU l_1=call suf1_-2=u

You can look at the file ner.train.withVwExamplesMarkerPrePended.gz under src/test/resources for a big list of examples.

Useful tip:

When you submit examples to the web service, you should tail the Jetty log file (see Step 5) to see what the web service is doing.




