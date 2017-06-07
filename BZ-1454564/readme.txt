1.
Add user using add-user.sh script.

2. 
Start EAP 7.0 server

3. 
client connect with the following command.
$ mvn clean compile exec:java -Djmx.service.url=service:jmx:remote+http://127.0.0.1:9990

This client will sleep before closing connection.
$ Sleeping 10 sec...

3. 
During client sleep, disconnect a network between the server and the client. 
(or suspend the server process with kill -SIGSTOP <PID>, you can see similarly result.)

4.
It will hang in close method.


