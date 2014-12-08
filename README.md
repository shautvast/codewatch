codewatch
=========

Small experimental webapp, using java7, javascript  and websockets. 
It listens on changes in the local filesystem (it's meant to run locally), does findbugs analysis and shows the code annotated with the results in the browser automatically.

* Go to the code in nl.jssl.codewatch.Main.java
* In this version the source and binaries locations are still hardcoded, so you'll have to update that. 
* Starting the main, will startup a webserver.
* Browse to http://localhost:8080
* your browser will respond with "Waiting for code changes".
* Change something in some source code that is in the path you specified in Main.
* go back to your browser, and it will show the code you touched.

