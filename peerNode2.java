package csx55.chord.Node;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Collections;

import csx55.chord.wireformat.*;
import csx55.chord.transport.*;
import csx55.chord.util.*;
import csx55.chord.datahandler.*;

import java.text.DecimalFormat;

import java.util.concurrent.LinkedBlockingDeque;



public class peerNode2 extends Node {

     //class specific nodes
    private connectionData register;
    protected String regHostName = null;
    protected Integer regPortNumber = null;
    private volatile String myName;
    private double myHash;
    private fingerTable myFingerTable;
    public connectionData myForwardCon = null;
    public connectionData myBackwardCon = null;

    private connectionData tempCon = null;

    public double forwardHash = -1;
    public double backHash = -1;

    private String tempConnection = null;

    private HashMap<String, connectionData> connectionsMap = null;


    public peerNode2(String ip, int port, String reghost, int regpot){
        this.myIP = ip;
        this.myPort = port;
        System.out.println("hi");
        this.myName = "peerNode";

        this.connectionsMap = new HashMap<String, connectionData>();

        //this.myName = ip + ":" + port;
        
        //this.myHash = (this.myName.hashCode());
        //this.myHash = 24;
        
        //System.out.println("I am : " + getMyName());
        
        this.NodesList = new HashMap<String, connectionData>();  
        //DataHandlerNode handle = new DataHandlerNode(dataQueue, this, this.messagesRecievedCount, this.messagesRelayed);
        this.hndl = new Thread(handle);
        this.datahandler = handle;
        //System.out.println("My socket is: " + this.mySocket);

        this.regHostName = reghost;
        this.regPortNumber = regpot;
        //this.myFingerTable = new fingerTable(this);


        setup();
        setupThreads();
        setupRegisterConnection();
        register();

    }

    public double giveHash(){
        return this.myHash;
    }

    public String giveName(){
        return this.myName;
    }


    public void registerRespond(String[][]data, Socket incomingSock){
        //System.out.println("RegRespond");

        synchronized (registerLock) {
        
            //We got a response, change our name, and rejoice we now have a connection to father register
            //System.out.println("Responding");
            //this.register.setReciever(incomingSock);
            //System.out.print("Data" + data[0][0]);
            if (Integer.parseInt(data[0][0]) == 1){
                //We were already registered :D
            }
            else{
                //this.name = data[0][1];
                //System.out.println("We are now named: "+ this.name);
                System.out.println("I will find this: " + data[0][2]);
                if (data[0][2].equals("yay")){
                    System.out.println("NVM I AM FIRST NODE LETS MAKE OUR TABLE JANK");
                    this.myFingerTable.defaultSetup(myHash, myIP);
                }
                else{

                    contactSetup(data[0][2]);
                }
                this.datahandler.myNodesName = this.myIP + ":" + this.myPort;
            }
            registerLock.notify();
        }

    }
    

    public void contactSetup(String fullName){

        String [] broken = fullName.split(":");
        this.tempConnection = fullName;
        
        Event helpMe = EventFactory.createEvent(contactPEERINITIAL);
        Socket tempSock;
        try {
            tempSock = new Socket(broken[0] , Integer.valueOf(broken[1]));
            TCPSender newSender = new TCPSender(tempSock);
            connectionData connection = new connectionData(tempSock, newSender, broken[0], Integer.valueOf(broken[1]));

            TCPReceiver temp2 = tcpRec.nodeListen(tempSock, fullName, connection);
            connection.setReciever(temp2);
            connection.setName(broken[0] + ":" + broken[1]);
            //store it in case we need it
            this.tempCon = connection;

            String arguments = String.valueOf(this.myHash);
            helpMe.setData(arguments);
            newSender.sendMessage(helpMe.getBytes(), contactPEERINITIAL);

        } catch (NumberFormatException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("Contact done");
    }





    public void handleClose(Socket socket){
        //System.out.println("Handle close");
        System.exit(2);
        
        synchronized (registerLock) {


                //node side
            if (socket == this.register.getSocket()){
                //System.out.println("Error, lost connection to register, shutting down");
                System.exit(255);

                //This is a register connection closure, we will attempt to reconnect
            }
            else{
                //System.out.println("Lost connection to node, please check overlay setup");
                //System.exit(256);
            }

            registerLock.notify();
        }

    }

    private synchronized connectionData setupRegisterConnection(){
        synchronized (registerLock) {
            //System.out.println("Register");
            if (this.register != null){

                registerLock.notify();
                return this.register;            
            }
            else{
                Socket clientSock = null;
                TCPSender TCPSender = null;
                try{
                    clientSock = new Socket(this.regHostName, this.regPortNumber);
                    TCPSender = new TCPSender(clientSock);
                    connectionData connectionData = new connectionData(clientSock, TCPSender, this.regHostName, this.regPortNumber);
                    this.register = connectionData;
                    TCPReceiver temp2 = tcpRec.nodeListen(clientSock, myName, connectionData);
                    connectionData.setReciever(temp2);
                    registerLock.notify();
                } catch (ConnectException e){
                    return null;
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return this.register; 
                //sendMessage(commandBroken, mess.connections.get(Integer.parseInt(commandBroken[1])));
            }
        //registerLock.notify();
        }
      
    }

    public void helpPeer(String[][] data, connectionData con){

        System.out.println(data[0][0]);
        String nextNode = this.myFingerTable.determineSpot(Double.valueOf(data[0][0]));
        
        TCPSender sender = con.getTcpSender();
        Event helpResponse = EventFactory.createEvent(CONTACTPEERRESPONSE);
        System.out.println(nextNode);
        if (nextNode.equals("second")){
            //default setup, it becomes the next regardless, and we point it to us.
            System.out.println("try this!");
            String response = 0 + " " + this.myName + this.myHash;
            helpResponse.setData(response);
            this.myForwardCon = con;
            this.myBackwardCon = con;
        }
        else if(nextNode.equals("forward")){
            //signals we are done and our next should be that one
            System.out.println("Forward");
            String response = 1 + " " + this.myForwardCon.getName();
            helpResponse.setData(response);

        }
        else {
            System.out.println("Default");
            System.out.println("I am : " + this.giveName());
            System.out.println("go here + " + nextNode);
            System.out.println("My hash : " + giveHash());
            String response = 2 + " " + nextNode;
            helpResponse.setData(response);
            //default telling us to keep going

        }

        try {
            System.out.println(con.getIP() + " " + con.getPort());
            sender.sendMessage(helpResponse.getBytes(), CONTACTPEERRESPONSE);
            
            System.out.println("Sent message");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    private connectionData makeConnection(String hostname, int port){
        try {
            Socket outgoingSock = new Socket(hostname, port);
            TCPSender TCPSender;
        
            TCPSender = new TCPSender(outgoingSock);
        
            
            connectionData connection = new connectionData(outgoingSock, TCPSender, hostname, port);

            TCPReceiver temp2 = tcpRec.nodeListen(outgoingSock, myName, connection);
            connection.setReciever(temp2);
            connection.setName(hostname + ":" + port);
            return connection;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;


    }
    
    public void progressAssignment(String[][] data, connectionData con ){
        //System.out.println("I got a response!");
        //System.out.println("Code was: " + data[0][0]);
        //System.out.println("extra " + data[0][1]);
        if (data[0][0].equals("0")){
            System.out.println("Default setup (loop)");
            this.myBackwardCon = tempCon;
            this.myForwardCon = tempCon;
            insertionProtocol();
        }
        else if (data[0][0].equals("1")){
            //the node sent back is going to be our successor, the one after us in the loop, talk to him :)
            this.myBackwardCon = tempCon;
            String [] split = data[0][1].split(":");
            this.myForwardCon = makeConnection(split[0], Integer.valueOf(split[1]));
            this.myBackwardCon = tempCon;
            insertionProtocol();
        }
        else{
            //this was not our home, go to the next one
            contactSetup(data[0][1]);
        }
    }

    public void insertionProtocol(){
        //hashcode creation
        System.out.println("Hey");
        System.out.println("|" + this.myForwardCon.getName() + "|");

        double hashedName = this.myForwardCon.getName().hashCode();
        if (hashedName < 0){
            hashedName = -hashedName + 2147483647;
        }
        this.forwardHash = hashedName;

        hashedName = this.myBackwardCon.getName().hashCode();
        if (hashedName < 0){
            hashedName = -hashedName + 2147483647;
        }
        this.backHash = hashedName;
        System.out.println(this.forwardHash);
        //determine which one has the better table to grab from
        if (this.backHash == this.forwardHash){
            //only send the important one, and loop it
            notifyNeighbor(this.myForwardCon, 3);
            return;
        }
        notifyNeighbor(this.myBackwardCon, 1);
        notifyNeighbor(this.myForwardCon, 2);
        System.out.println("Done notifying neighbors");
      
    }

    public void notifyNeighbor(connectionData connection, int flag){
        //flag 1 means that we are in front of them, flag 2 means we are behind them
        //flag of 2 also means we want their table! so we can fill in ours
        Event notify = EventFactory.createEvent(NOTIFYNEIGHBOR); 
        TCPSender sender = connection.getTcpSender();
        String arguments = String.valueOf(flag) + " " + this.myName + " " + this.myHash;
        notify.setData(arguments);
        try {
            sender.sendMessage(notify.getBytes(), NOTIFYNEIGHBOR);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public void recievedNeighbor(String [][] data, connectionData con){
        System.out.println("I have a new neighbor");
        System.out.println(data[0][0]);
        if (data[0][0].equals("3")){
            //loop, first one added after meeee
            //the node talking to us is behind us! lets give them our data
            System.out.println("They want my table too!");
            con.setName(data[0][1]);
            this.myBackwardCon = con;
            this.myForwardCon = con;
            this.backHash = Double.valueOf(data[0][2]);
            this.forwardHash = Double.valueOf(data[0][2]);
            sendMyTable();
            this.myFingerTable.selfCheckFinger();
            this.myFingerTable.printTable();
            System.out.println("neat");
        }
        if (data[0][0].equals("2")){
            //the node talking to us is behind us! lets give them our data
            System.out.println("They want my table too!");
            this.myBackwardCon = con;
            this.backHash = Double.valueOf(data[0][2]);
            sendMyTable();
            this.myFingerTable.printTable();
        }
        else{
            this.myForwardCon = con;
            this.forwardHash = Double.valueOf(data[0][2]);
        }
    }

    public void sendMyTable(){
        //will always be using our back way
        System.out.println("lets send my table");
        String fingerTable = this.myFingerTable.giveFingerTable();
        Event giveFinger = EventFactory.createEvent(GIVEFINGERTABLE);
        giveFinger.setData(fingerTable);
        
        try {
            this.myBackwardCon.getTcpSender().sendMessage(giveFinger.getBytes(), GIVEFINGERTABLE);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.myFingerTable.printTable();
    }

    public void handleTableQuery(String[][]data, connectionData con){
        System.out.println("Someones asking about updates");
        int sizer = data[0].length;
        StringBuilder returnString = new StringBuilder();
        DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(8);
        for (int i = 0; i < sizer/2; i = i + 2){
            //i is position for return
            //i+1 is the number we are checking
            double maxHeight = 2147483647 * 2;
            double findMe = Double.valueOf(data[0][i+1]);

            System.out.println("Looking for " + df.format(findMe));
            System.out.println("I am " + df.format(this.giveHash()));
            System.out.println("My back is: " + df.format(this.backHash));
            //zero to us
            if (findMe == this.giveHash()){
                returnString.append(i).append(" ").append("valid").append(" ");
            }
            //from us to zero
            if (findMe < this.giveHash()){
                System.out.println("here");
                if ((this.backHash > 0 && this.backHash > this.giveHash())){
                    returnString.append(i).append(" ").append("valid");
                }
                if((this.backHash > 0 && this.backHash < this.giveHash())){
                    if (this.backHash < findMe){

                        returnString.append(i).append(" ").append(this.myBackwardCon.getName()).append(" ");
                    }

                }
            }
            //see if beyond zero.
            else if (this.backHash > this.giveHash()){
                //we then become max
                System.out.println("its outta range");
                double tempmyHash = maxHeight;
                if (findMe < maxHeight && findMe > this.backHash){
                    returnString.append(i).append(" ").append(this.myBackwardCon.getName()).append(" ");
                }
                
                //cheat, do the distance to our last hash isntead, see the distance to this one.
               }

            else if (this.backHash > this.giveHash() && findMe < this.giveHash() && findMe > this.backHash ){
                returnString.append(i).append(" ").append("valid");
            }
            else{
                System.out.println("Invalid");
                returnString.append(i).append(" ").append(this.myBackwardCon.getName());
            }

            //between us and our last, past the zero resetter.

        }

        //ship it back
        Event response = EventFactory.createEvent(RESPONSETABLEINFO);
        response.setData(returnString.toString());
        try {
            con.getTcpSender().sendMessage(response.getBytes(), RESPONSETABLEINFO);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void responseTableInfo(String[][] data, connectionData con){
        this.myFingerTable.validateInfo(data);
    }

    

    
    public void recievedFingerTable(String [][] data, connectionData con){
        System.out.println("We got a finger table! lets math it up to be ours");
        System.out.println(data);
        ArrayList<chord> tempFingerTable = new ArrayList<chord>();

        this.myFingerTable.generateOnlyTable(this.myHash, this.myIP);


        int note = 1;
        for (int i = 0; i < 10; i += 2) {
            double temp = this.forwardHash + (Math.pow(2, note - 1));
            while (temp > 2147483647) {
                temp = temp - 2147483647;
            }
            System.out.println(data[0][i]);
            System.out.println(data[0][i + 1]);
            
            chord tempChord = new chord(this.forwardHash, String.valueOf(data[0][i + 1]));
            tempChord.setPosition(Double.valueOf(data[0][i]));
            
            tempFingerTable.add(tempChord);
            note++;
        }

        Collections.sort(tempFingerTable, (a, b) -> Double.compare(a.getPosition(), b.getPosition()));

        int spot = 1;
        
        System.out.println("Temp table as follows");
        for (chord c : tempFingerTable) {

            System.out.println(c.getPosition() + " " + c.getHash());
            spot++;
        }

        this.myFingerTable.updateFingerTable(tempFingerTable);



        

        //for (HashMap.Entry<Integer,chord> entry : tempFingerTable.entrySet()){

            //double temp = this.parent.giveHash()+(Math.pow(2, note-1));
            //System.out.println((Math.pow(2, note-1)));
            //System.out.println(temp);
           // this.myFingerTable.defaultSetup(myHash, myIP);
            
            //formating issues:
            
            
           // System.out.println("Entry: " + entry.getValue().getPosition() +" " + entry.getKey() + " " + " " + entry.getValue().getHash());
    
       //}




    }

    private synchronized void register(){
        //System.out.println("Registration Request");
        //this one could use the registry lock, however it does not access the variable, just starts the process.
        Event Registration = EventFactory.createEvent(REGISTRATION_REQUEST); 
        TCPSender sender = this.register.getTcpSender();
        try{
            InetAddress myAddr = InetAddress.getLocalHost();
            this.myIP = myAddr.toString();
        }
        catch (Exception e){
            //System.out.println("Are you sure youre connected to the internet");
        }
        
        if (this.myIP.contains("/")){
            String [] temp = this.myIP.split("/");
            this.myIP = temp[1];
        }
        this.myName = this.myIP +":" + this.myPort;

        double hashedName = this.myName.hashCode();
        System.out.println("basic: " + hashedName);
        if (hashedName < 0){
            hashedName = (-hashedName) + 2147483647;
        }

        Random random = new Random();
        
        // Generate a random 5-bit number
        int randomNumber = random.nextInt(32);
        this.myHash = randomNumber;
        System.out.println("my hash <3 " + this.myHash);
      
        String arguments = (this.myIP + " " + this.myPort + " " + this.myHash);

        
        //System.out.println("ARguments: " + arguments);
        Registration.setData(arguments);
        
        try {
            sender.sendMessage(Registration.getBytes(), REGISTRATION_REQUEST);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



    
}
