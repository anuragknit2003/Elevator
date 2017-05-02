To Build this project please execute the following maven command:
mvn clean install

This maven command will create a self executable jar with name : elevator-0.0.1-SNAPSHOT-jar-with-dependencies.jar

The jar can be executed by : java -jar elevator-0.0.1-SNAPSHOT-jar-with-dependencies.jar

The number of elevators are hard coded to 2 in MainClass.java. To override this pass number of elevators with the above command
java -jar elevator-0.0.1-SNAPSHOT-jar-with-dependencies.jar 4

To submit an User Request please enter the details in following format from the console
3 6 UP
The first number will have be the floor the user is waiting to be picked from
The Second Number will have be the floor the user wants to go
Third needs to UP or DOWN String indicating the direction

A log file will be created in the current directory from where jar is being executed with name ElevatorLogs.txt
which will log the movements of the elevators.

To terminate the program please enter SHUTDOWN from the console