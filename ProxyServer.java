/* Code Reference : 
http://wps.pearsoned.com/ecs_kurose_compnetw_6/216/55463/14198700.cw/index.html
https://abet.soe.ucsc.edu/sites/default/files/ce155­middle.pdf
http://www.jtmelton.com/2007/11/27/a-simple-multi-threaded-java-http-proxy-server/
http://crunchify.com/how-to-create-a-simple-in-memory-cache-in-java-lightweight-cache/
*/

/* Importing Java libraries */
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


/* Main class of Proxy Server */
class ProxyServer{
private static final int DEFAULT_PORT = 8080; //default port is declared to 8080
private ConnectionHandler ch = null;

/* Server waits for client to connect to it (executing the program)*/ 
public ProxyServer(int port){
System.out.println("Server is assigned to port: " +port);
ch = new ConnectionHandler(port); // accepts client request
ch.start();
}

/* Client enters a port number in command prompt or doesn’t enter */
public static void main(String args[]){
int s_port;
try{
s_port = Integer.parseInt(args[0],10); //Server connects to port number entered by client
}
catch(Exception e){
System.out.println("Server is connected to DEFAULT PORT: " + DEFAULT_PORT);
s_port = DEFAULT_PORT;  //Server connects to default port if no port number is entered by client
}
new ProxyServer(s_port); //Proxy Server connects to the port
}
}

/* Accepted Client request is handled in this class */
class ConnectionHandler extends Thread{
private static int port1;
private ServerSocket main_skt = null;
private static Vector thread = new Vector(5,2); //thread size is set to 5 and it increases by 2 each time a new thread is created


public ConnectionHandler(int port){
port1 = port; //assigned port number is passed here 
}


public void run(){
serviceRequests();
}

/* Socket connection is created each and every time a client makes request/connection. Multithreading is performed in this function to serve multiple clients simultaneously. */
private void serviceRequests(){
try{
main_skt = new ServerSocket(port1); //ServerSocket object is created to monitor the port
}

catch (Exception e){
System.err.println(e);
System.exit(1);
}

/* new thread is created every time to process the requests made by client */
while(true){
try{ 
Socket con = main_skt.accept();  
CreateConnection tempSock= new CreateConnection(con); //new socket is created every time connect with client when it makes new request
tempSock.start();
thread.addElement(tempSock);
for(int i=0;i<ConnectionHandler.thread.size();i++)
if(!((CreateConnection)(thread.elementAt(i))).isAlive())
thread.removeElementAt(i);
}
catch(Exception e){
System.err.println("Exception:\n"+e);
}
}
}
}

/* A new thread is created in this class for establishing double connection between , Web Browser, Proxy Server and Web Server */
class CreateConnection extends Thread{
private Socket sock; 
private PrintStream out;
private InputStream in;
static int Port2 = 80; // Default local port of HTTP 
public static String Addr; // Host Address
public static String RequestString; // Stores the parsed content of the request
public static int SIZE_OF_BUFFER=1024;
public static int Method = 0; // GET method initialisation to call later
public PrintWriter pw; // to print text into a file
public static ArrayList<String> ch = new ArrayList<String>(); // static cache to store address of requests made by client


/* new socket connection between Proxy and browser */
public CreateConnection(Socket s){
sock=s; 
}

/* Parsing the request line and headers sent by Client is performed in this function. */

public static int parseRequest(String src){
int index1 = src.indexOf("//");
int index2 = src.indexOf("\n");
String begin;
String mid;
String last;

/* URL part is extracted and parsed as per HTTP Request and Response format and sent to HTTP Server */

if((index1 < 0) || (index2 < 0))
return -2; // indicates error in request

try{
begin = src.substring(0,index1-5);
mid= src.substring(index1+2,index2);
last = src.substring(index2);
}

catch(Exception e){
System.out.println("Exception" + e);
return -1;
}

int index3 = mid.indexOf('/');
if(index3 < 0)
return -1;

try{
String Adr = mid.substring(0,index3);

int index4 = Adr.indexOf(':');
if(index4 > 0){
String Port3 = Adr.substring(index4+1); // Extracted Port number is displayed in Command prompt
Port2 = (Integer.valueOf(Port3)).intValue();
Addr = Adr.substring(0,index4);   // Web address is displayed in Command Prompt as well
}
else{
Addr = Adr;
}
ch.add(Addr); // extracted addresses are appended to the cache

String Remining = mid.substring(index3);
RequestString = (begin.concat(Remining)).concat(last);
if(RequestString.startsWith("GET")) // Checking for GET Request made by client 
Method = 1;
return Method;
}

catch(Exception e){
System.out.println("Exception" + e);
return -1;
}
}

/* Main function of Proxy Server. Connection between proxy server and HTTP server is made here. */
private void MainServer(){
try{
int currCh = 0; // Current read character
int prevCh = 0; // Previously read character
int r =0; // returns the value
int Size = SIZE_OF_BUFFER;

pw = new PrintWriter(new FileWriter("log.txt", true)); //log.txt file is generated
DateFormat TimeDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); //to generate Elapsed time at client request
Calendar caldr = Calendar.getInstance(); // gets content of Calender to generate DateFormat

StringBuffer req = new StringBuffer(Size);
while((currCh = in.read()) != -1){
req.append((char)currCh); // HTTP Request message is generated

if((currCh == 13) && (prevCh == 10)){
int status = parseRequest(req.toString()); //Parsing is done here. After receiving request from client
for(String s:ch)// checking if extracted Web Address exists in cache or not
{
if(!s.equals(Addr)) //if does not exist then request is forwarded to HTTP Server and new socket connection is established
{   
if(status > 0)
{
System.out.println("\n File not found in Cache! \nRequest forwarded to HTTP Server\n");
System.out.println("\n\n");
System.out.print(RequestString); // Client GET request information is processed and HTTP Response message is displayed at Proxy Server (Command Prompt)
pw.println(RequestString);
System.out.println("\n");
System.out.println(TimeDate.format(caldr.getTime()));
System.out.println("\n");
System.out.println("Address:" +Addr);
System.out.println("Local Port:" +Port2+ "\n");
pw.println(TimeDate.format(caldr.getTime()));
pw.println("\n");
pw.println("Host Address:" +Addr);
pw.println("Local Port:" +Port2+ "\n");


r = client(Addr,Port2,pw); // Proxy server connects to HTTP Server and sends information to browser
}

/* if client makes request from browser with port number directly then server throws an error as proxy has been set with the browser*/
else{
out.print("Proxy server cannot process request from WWW browser.");
}}
else  // if exists then proxy server returns the requested web page from cache
{ 
if(status > 0)
{
System.out.println("File found in Cache!");
System.out.println("\n");
System.out.println("Accept: text/html");
System.out.println("\n");
System.out.println(TimeDate.format(caldr.getTime()));
System.out.println("\n");
System.out.println("Address:" +Addr);
System.out.println("Local Port:" +Port2+ "\n");
System.out.println("\n");
pw.println("\n");
pw.println(TimeDate.format(caldr.getTime()));
pw.println("\n");
pw.println("Host Address:" +Addr);
pw.println("Local Port:" +Port2+ "\n");

System.out.println("Read from Cache!");
r = client(Addr,Port2,pw); // Proxy server connects to HTTP Server and sends information to browser
}

/* if client makes request from browser with port number directly then server throws an error as proxy has been set with the browser*/
else{
out.print("Proxy server cannot process request from WWW browser.");
}
}
}
req.setLength(0); // Prepares for next request
break;

}
prevCh = currCh;


}

/* if the requested page doesn’t exist or if the web address specified is non readable, server throws an error */
if(r<0){
out.print("404 Not Found \n");
out.println("Proxy server cannot establish connection with the specified Internet address.");
pw.println("HTTP/1.0 404 Not Found \r\n");
pw.println("Error establishing connection! \n");
}
pw.close();

}
catch(Exception e){
System.out.println("Exception" + e);
}
}

/* Main function of Client. Initiates connection with server at the address and Port specified */
public int client(String Addr, int Port2, PrintWriter pw){
PrintStream outputStr; // Stream to send message to HTTP Server
InputStream inputStr; // Stream to receive data
int c = 0;

try{
Socket http_Sock = new Socket(Addr, Port2); // socket connection established with HTTP Server
outputStr = new PrintStream(http_Sock.getOutputStream());
inputStr = (http_Sock.getInputStream());

/* Sends GET Request to HTTP Server. */
if(Method==1){
outputStr.println(RequestString);
System.out.println("HTTP/1.1 200 OK");
pw.println("HTTP/1.1 200 OK");
}

/* Writing data back to Web Browser */
while((c = inputStr.read()) != -1)
out.write((char)c);


inputStr.close();
outputStr.close();
http_Sock.close(); //Connection with HTTP Server is closed
return 1;
}
/* if the requested page doesn’t exist or if the web address specified is non readable, HTTP Server throws an error */
 
catch(Exception e){
System.out.println("HTTP/1.0 404 Not Found \r\n"); 
System.out.println("Error establishing connection! \n");
return -1;
}

}

/* Thread function */
public void run(){
System.out.print("\nConnection Established Successfully…\n");

try{
out = new PrintStream(sock.getOutputStream());
in = sock.getInputStream();
MainServer();
sock.close();  //Main socket connection with Web Browser is closed 
}

catch(Exception e){
System.err.println("Exception: \n" + e);
}
System.out.print("\n Connection Terminated!\n");
stop(); // Thread is ended. All connections are closed after all client requests are handled
}

}