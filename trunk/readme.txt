*********************************************

Zambon Jacopo, 574308
12.30.2011

Concurrent and Distributed Programming
Academic Year 2011-2012

-Assigned Project-
CLONE DETECTION

Proposers: 
Mauro Conti, PHD
Eyüp Serdar Canlar

*********************************************

**********************************************************
|							  |
| Instruction on how to start the simulator and the server| 
|							  |	
**********************************************************

1. Open a terminal window

2. Navigate to where the .java files of the project are

3. Compile the java files: type the command 
	"javac *.java"

4. Type the command 
	"java ProjGUI" 
(it'll appear a GUI for the start of the clone detection program)

5. Open another terminal window

6. Navigate to where the compilated files (.class) of the project are

7. Run the RMI interface compiler on your implementation class:
Type the command 
	"rmic txtPrintImpl" 
to create the stub used by RMI

8. Start the RMI registry: type in the same directory
	"start rmiregistry"
if you are using a Windows system, or
	"rmiregistry &"
in a Lunix or Unix-like system

9. Start the RMI Server program: type in the same directory
	"java RMIServer"

10. Go to the GUI opened at the step 4.,insert the data needed and press the
START button to begin the simulation