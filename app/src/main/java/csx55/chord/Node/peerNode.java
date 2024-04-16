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
import java.util.List;

import csx55.chord.wireformat.*;
import csx55.chord.transport.*;
import csx55.chord.util.*;
import csx55.chord.datahandler.*;

import java.text.DecimalFormat;

import java.util.concurrent.LinkedBlockingDeque;



public class peerNode extends Node {

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

    fingerUpdater fingerupdater = null;
    Thread fingerUpdaterThread = null;

    String fileToSend = "";


    public peerNode(String ip, int port, String reghost, int regpot){
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
        DataHandlerNode handle = new DataHandlerNode(dataQueue, this, this.messagesRecievedCount, this.messagesRelayed);
        this.hndl = new Thread(handle);
        this.datahandler = handle;
        //System.out.println("My socket is: " + this.mySocket);

        this.regHostName = reghost;
        this.regPortNumber = regpot;
        this.myFingerTable = new fingerTable(this);

        //TCPReceiverThread tcpt = new TCPReceiverThread(this.mySocket, dataQueue, this);
        //Thread tcpThread = new Thread(tcpt);
        //tcpThread.setName("TCPRecieverThread");
        //this.tcpRecThread = tcpThread;
        //this.tcpRec = tcpt;
        //give it some number in ms to wait for, 60000 is a minute
        fingerUpdater updater = new fingerUpdater(2000, this);
        Thread fingerThread = new Thread(updater);
        this.fingerupdater = updater;
        this.fingerUpdaterThread = fingerThread;



        setup();
        setupThreads();
        setupRegisterConnection();
        register();
        this.fingerUpdaterThread.start();

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

        //initialize our blank fingertable
        this.myFingerTable.generateOnlyTable(myHash, this.giveName());

        //we recived a node fromt eh discovery tree, lets contact it for information
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
        this.myFingerTable.printTable();
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
    //peer has asked for help on where to go, we assist
    public void registerPeer(String [][] data, connectionData con){

        System.out.println("Peer has asked for registration: " + data[0][0]);

        TCPSender sender = con.getTcpSender();
        Event helpResponse = EventFactory.createEvent(CONTACTPEERRESPONSE);
        //this meerly returns the locationhash, not the id, be careful
        String nextNode = this.myFingerTable.determineSpot(Double.valueOf(data[0][0]));
        System.out.println(nextNode);
        //loop, only one current node in the ssytem, default setup.
        if (nextNode.equals("0")){
            //default setup, it becomes the next regardless, and we point it to us.
            System.out.println("loop setup thank you for being second");
            String response = 0 + " " + this.myName+ " " + this.myHash;
            helpResponse.setData(response);
            
            this.myForwardCon = con;
            this.myBackwardCon = con;
            this.myFingerTable.loopValidate();
        }
        else if(nextNode.equals("1")){
            //this node is then behind me and my last node, so lets just plop it in.
            System.out.println("we are the new forward for this node.");
            String response = 1 +  " " + this.myName + " " + this.myHash + " "  + this.myBackwardCon.getName() + " " +this.backHash;
            this.myBackwardCon = con;
            this.backHash = Double.valueOf(data[0][0]);
            helpResponse.setData(response);
        }
        else if(nextNode.equals("2")){
            //its in our front range, give it our front node, and our own data just in case
            System.out.println("we are the new forward for this node.");
            String response = 2 +  " " + this.myName + " " + this.myHash + " "  + this.myForwardCon.getName() + " " +this.forwardHash;
            helpResponse.setData(response);
        }
        else {
            //this means its not even in our range, we send it to the max value instead to just get it out of our hari
            System.out.println("didnt find it, try to get it closer");
            nextNode = this.myForwardCon.getName();
            System.out.println("I am : " + this.giveName());
            System.out.println("go here + " + nextNode);
            System.out.println("My hash : " + giveHash());
            String response = 3 + " " + nextNode;
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

        //this.myFingerTable.validateFingerTable();
        
    }


    //help peer will be answered by peerresponserecieved
    public connectionData peerConnection(String hostname, int port){
        String key = hostname+port;
        if (this.connectionsMap.containsKey(key)){
            return connectionsMap.get(key);
        }
        else{
            try {
                Socket outgoingSock = new Socket(hostname, port);
                TCPSender TCPSender;
            
                TCPSender = new TCPSender(outgoingSock);
            
                
                connectionData connection = new connectionData(outgoingSock, TCPSender, hostname, port);

                TCPReceiver temp2 = tcpRec.nodeListen(outgoingSock, myName, connection);
                connection.setReciever(temp2);
                connection.setName(hostname + ":" + port);
                connectionsMap.put(key, connection);
                return connection;
            } catch (IOException e) { 
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;


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
    
    public void peerResponseRecieved(String[][] data, connectionData con ){
        //System.out.println("I got a response!");
        //System.out.println("Code was: " + data[0][0]);
        //System.out.println("extra " + data[0][1]);

        //this one is a default setup of a loop
        if (data[0][0].equals("0")){
            System.out.println("Setup of 0");
            System.out.println("Default setup (loop)");
            this.myBackwardCon = tempCon;
            this.forwardHash = Double.valueOf(data[0][2]);
            this.myForwardCon = tempCon;
            this.backHash = Double.valueOf(data[0][2]);
            communicateChanges();
            this.myFingerTable.validateFingerTable();
        }
        //a one means that we are behind that node, its now our forward, and gave us our backwards
        else if (data[0][0].equals("1")){
            System.out.println("Setup of 1");
            this.myBackwardCon = tempCon;
            String [] split = data[0][3].split(":");
            this.myForwardCon = con;
            this.forwardHash = Double.valueOf(data[0][2]);
            //its now our back
            this.myBackwardCon = makeConnection(split[0], Integer.valueOf(split[1]));
            this.backHash = Double.valueOf(data[0][4]);
            communicateChanges();
        }
        //a 2 means that we are in front of it, and it gave us its forward to now hook between :)
        else if (data[0][0].equals("2")){
            //THIS SHOULD NEVER BE CALLED
            System.out.println("Setup of 2");
            this.myBackwardCon = tempCon;
            String [] split = data[0][3].split(":");
            this.myBackwardCon = con;
            this.backHash = Double.valueOf(data[0][2]);
            //its now our back
            this.myForwardCon = makeConnection(split[0], Integer.valueOf(split[1]));
            this.forwardHash = Double.valueOf(data[0][4]);
            communicateChanges();
        }
        //else we didnt find it, and its telling me on my way
        else{
            //this was not our home, go to the next one
            contactSetup(data[0][1]);
        }
    }

    //this method talks to recieved neighbor
    public void communicateChanges(){
        //hashcode creation
        System.out.println("Hey");
        System.out.println("|" + this.myForwardCon.getName() + "|");

        //determine which one has the better table to grab from
        if (this.backHash == this.forwardHash){
            //only send the important one, and loop it
            notifyNeighbor(this.myForwardCon, 3);
            return;
        }
        notifyNeighbor(this.myBackwardCon, 1);
        notifyNeighbor(this.myForwardCon, 2);
        newPeer(this.myForwardCon);
        System.out.println("Done notifying neighbors");
        this.myFingerTable.validateFingerTable();
        this.myFingerTable.printTable();
    }

    public void newPeer(connectionData con){
        //this will always creep forward notifying of a new peer in the system.
        System.out.println("Creep");
        Event notify = EventFactory.createEvent(NEWPEER); 
        TCPSender sender = con.getTcpSender();
        String arguments = this.myName;
        notify.setData(arguments);
        try {
            sender.sendMessage(notify.getBytes(), NEWPEER);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void newPeerRecieved(connectionData con, String [][]data){
        System.out.println("NEW PEER RECIEVED");
        if (data[0][0].equals(this.myName)){
            //i sent this, woot
        }
        else{
            this.myFingerTable.validateFingerTable();
            Event notify = EventFactory.createEvent(NEWPEER); 
            TCPSender sender = this.myForwardCon.getTcpSender();
            String arguments = data[0][0];
            notify.setData(arguments);
            try {
                sender.sendMessage(notify.getBytes(), NEWPEER);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
        }

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
        con.setName(data[0][1]);
        if (data[0][0].equals("3")){
            //loop, first one added after meeee
            //the node talking to us is behind us! lets give them our data
            System.out.println("They want my table too!");
            con.setName(data[0][1]);
            this.myBackwardCon = con;
            this.myForwardCon = con;
            this.backHash = Double.valueOf(data[0][2]);
            this.forwardHash = Double.valueOf(data[0][2]);
            //sendMyTable();
            this.myFingerTable.updateTableNeighbors();
            //this.myFingerTable.printTable();
            System.out.println("initial communication right");
        }

        if (data[0][0].equals("2")){
            //the node talking to us is behind us! lets give them our data
            System.out.println("They want my table too!");
            this.myBackwardCon = con;
            double oldHash = this.backHash + 0;
            this.backHash = Double.valueOf(data[0][2]);
            //sendMyTable();
            this.myFingerTable.updateTableNeighbors();
            //this.myFingerTable.printTable();
        }
        else{
            System.out.println("its ahead of me");
            //theyre cutting ahead of me, lets recalculate a little
            this.myForwardCon = con;
            this.forwardHash = Double.valueOf(data[0][2]);
            this.myFingerTable.updateTableNeighbors();
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
        
    }

    public void responseTableInfo(String[][] data, connectionData con){
        this.myFingerTable.validateInfo(data);
    }

    

    public void queryForTable(String address, String message){
        //will only be doing the table requests, nothing more.
        System.out.println("I will be messaging: " + address);
        System.out.println("With the string of : " + message);
        String [] broken = address.split(":");
        Socket tempSock;
        try {
            tempSock = new Socket(broken[0] , Integer.valueOf(broken[1]));
            TCPSender newSender = new TCPSender(tempSock);
            connectionData connection = new connectionData(tempSock, newSender, broken[0], Integer.valueOf(broken[1]));
            TCPReceiver temp2 = tcpRec.nodeListen(tempSock, address, connection); // listen for response
            Event requestTableInfo = EventFactory.createEvent(REQUESTTABLEINFO);
            connection.setReciever(temp2);
            this.connectionsMap.put(address, connection);

            requestTableInfo.setData(message);
            newSender.sendMessage(requestTableInfo.getBytes(), REQUESTTABLEINFO);

        } catch (NumberFormatException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //take in 
    }

    public void recievedFingerTable(String [][] data, connectionData con){
        System.out.println("We got a finger table! lets math it up to be ours");
        System.out.println(data);
        ArrayList<chord> tempFingerTable = new ArrayList<chord>();

        this.myFingerTable.generateOnlyTable(this.myHash, this.myIP);


        int note = 1;
        for (int i = 0; i < 15; i += 3) {
            double temp = this.forwardHash + (Math.pow(2, note - 1));
            while (temp > 31) {
                temp = temp - 31;
            }
            ////while (temp > 2147483647) {
           //     temp = temp - 2147483647;
           // }
            System.out.println(data[0][i]);
            System.out.println(data[0][i + 1]);
            System.out.println(data[0][i + 2]);
            
            chord tempChord = new chord(Double.valueOf(data[0][i + 2]), String.valueOf(data[0][i + 1]));
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


    //code to run and validate our forward finger.
    public void validateForward(){
        if (this.myForwardCon == null){
            return;
        }
        //System.out.println("Validating forward");
        Event validateForward = EventFactory.createEvent(VALIDATEFORWARD);
        String arguments = this.myName + " " + this.myHash;
        validateForward.setData(arguments);

        try {
            this.myForwardCon.getTcpSender().sendMessage(validateForward.getBytes(), VALIDATEFORWARD);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void validateForwardReply(String [][]data, connectionData con){
        Event validateForwardResponse = EventFactory.createEvent(VALIDATEFORWARDRESPONSE);
        //this will always come from behind us, lets verify
        System.out.println("Back hash is : " + this.backHash);
        System.out.println();
        if (this.backHash == -1){
            //lets accept him and double check just to make sure
            this.backHash = Double.valueOf(data[0][1]);
            
            this.myBackwardCon = con;
            this.myBackwardCon.setName(data[0][0]);
            String arguments = "0"; //all good
            validateForwardResponse.setData(arguments);
            System.out.println("now ours");
            
        }
        else if (this.myBackwardCon.getName().equals(data[0][0])){;
            String arguments = "0"; //all good
            validateForwardResponse.setData(arguments);
            System.out.println("Still ours");
            this.myFingerTable.printTable();
            //still ours
        }        
        else{
            //we send back our new behind, and then it figures it out
            System.out.println("Nope go somewhere else now");
            String arguments = "1 " + this.myBackwardCon.getName() + " " + this.backHash; 
            validateForwardResponse.setData(arguments);
            this.myFingerTable.printTable();
        }
        try {
            con.getTcpSender().sendMessage(validateForwardResponse.getBytes(), VALIDATEFORWARDRESPONSE);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void validateForwardRespond(String [][]data, connectionData con){
        if (data[0][0].equals("0")){

            //all good
        }
        else{
            System.out.println("WE HAVE CHANGED OUR BACK POINTER NOW");

            System.out.println("Going to go to : " + data[0][1]);
            //we must quest to find our new forward!
            this.myFingerTable.printTable();
            String [] broken  = data[0][1].split(":");
            tempCon = makeConnection(broken[0], Integer.valueOf(broken[1]));
            this.myForwardCon = tempCon;
            this.forwardHash = Double.valueOf(data[0][2]);
            System.out.println("New forward is: " + this.forwardHash);
            validateForward();
        }
    }

    public void printFingerTable(){
        this.myFingerTable.printTable();
    }

    public void doValidation(){
        this.myFingerTable.validateFingerTable();
    }



    public void requestHashLocation(String[][]data, connectionData con){
        System.out.println("Validating position");
        System.out.println(data[0][0] + " is asking about us storing" + data[0][1]);
        
        if (this.myFingerTable.backwardRange(Double.valueOf(data[0][1])) == true){
            Event validateFinger = EventFactory.createEvent(REQUESTHASHRESPONSE);
            //we totally do
            String arguments = data[0][1] + " " + this.giveName();
            validateFinger.setData(arguments);
            System.out.println("We do");
            String [] brokenString = data[0][0].split(":");
            connectionData tempCon = peerConnection(brokenString[0], Integer.valueOf(brokenString[1]));
            try {
                tempCon.getTcpSender().sendMessage(validateFinger.getBytes(), REQUESTHASHRESPONSE);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else{
            Event validateFinger = EventFactory.createEvent(REQUESTHASHlOCATION);

            System.out.println("We do not!");
           // int realspot = this.myFingerTable.findHighestLowest(Double.valueOf(data[0][1]));
            connectionData tempCon = null;

            String [] brokenString= this.myForwardCon.getName().split(":");
            tempCon = peerConnection(brokenString[0], Integer.valueOf(brokenString[1]));
            System.out.println("pass it forward");
            //String [] broken  = randomChord.getAddress().split(":");
            String arguments = data[0][0] + " " + data[0][1];
            validateFinger.setData(arguments);

            try {
                tempCon.getTcpSender().sendMessage(validateFinger.getBytes(), REQUESTHASHlOCATION);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }


   

    //above method we respond to on another node
    public void validateRandomReply(String [][]data, connectionData con){
        //someone has asked us if we currently hold this spot, lets check.
        printFingerTable();
        System.out.println("Validating position");
        System.out.println(data[0][0] + " is asking about us holding" + data[0][1]);
        if (data[0][0].equals(this.giveName())){
            System.out.println("loop prevention");
            return;
        }
         
        
        if (this.myFingerTable.backwardRange(Double.valueOf(data[0][1])) == true){
            Event validateFinger = EventFactory.createEvent(VALIDATEFINGERRESPONSE);
            //we totally do
            String arguments = data[0][1] + " " + this.giveName() + " " + this.giveHash() + " " + data[0][2];
            validateFinger.setData(arguments);
            System.out.println("We do");
            String [] brokenString= data[0][0].split(":");
            connectionData tempCon = peerConnection(brokenString[0], Integer.valueOf(brokenString[1]));
            try {
                tempCon.getTcpSender().sendMessage(validateFinger.getBytes(), VALIDATEFINGERRESPONSE);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else{
            Event validateFinger = EventFactory.createEvent(VALIDATEFINGER);

            System.out.println("We do not!");
           // int realspot = this.myFingerTable.findHighestLowest(Double.valueOf(data[0][1]));
            connectionData tempCon = null;

            String [] brokenString= this.myForwardCon.getName().split(":");
            tempCon = peerConnection(brokenString[0], Integer.valueOf(brokenString[1]));
            System.out.println("pass it forward");
            //String [] broken  = randomChord.getAddress().split(":");
            String arguments = data[0][0] + " " + data[0][1] + " " + data[0][2];
            validateFinger.setData(arguments);

            try {
                tempCon.getTcpSender().sendMessage(validateFinger.getBytes(), VALIDATEFINGER);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            //WE JUST PASS IT AROUND THE RING LIKE A WHISKEY FLASK

/* 
            if (realspot == -2000){
                //backtrack table
                String [] brokenString= this.myBackwardCon.getName().split(":");
                tempCon = peerConnection(brokenString[0], Integer.valueOf(brokenString[1]));
                System.out.println("overshot, backhash holds that spot instead");
                //String [] broken  = randomChord.getAddress().split(":");
                String arguments = data[0][0] + " " + data[0][1] + " " + data[0][2];
                validateFinger.setData(arguments);
            }
            else if (realspot == -2000000){

                chord tempChord = this.myFingerTable.giveEntry(realspot);
                String [] brokenString= tempChord.getAddress().split(":");
                tempCon = peerConnection(brokenString[0], Integer.valueOf(brokenString[1]));
                System.out.println(tempChord.getHash() + " holds that spot instead");
                //String [] broken  = randomChord.getAddress().split(":");
                String arguments = data[0][0] + " " + data[0][1] + " " + data[0][2];
                validateFinger.setData(arguments);
            } */
                
            //this.myFingerTable.
            
        }
       // this.myFingerTable.printTable();

    }

    public void validationReturn(String [][] data, connectionData con){
        System.out.println("Validating the validation");
        if (data[0][0].equals("0")){
            //all good bb
            System.out.println("THIS ENTRY WAS VALID");
        }
        else{

            //we go deeper, we do not meerly accept what it says


            System.out.println(data[0][0]);
            System.out.println("updated my table!");
            System.out.println("Re-fixing: " + data[0][3]);
            chord randomChord = this.myFingerTable.giveEntry(Integer.valueOf(data[0][3]));
            System.out.println("Finger of : " + randomChord.getHash() + " Says had holds : " + randomChord.getPosition());
            System.out.println("changing to be: " );
            randomChord.setAddresss(data[0][1]);
            randomChord.setHash(Double.valueOf(data[0][2]));
            System.out.println("Finger of : " + randomChord.getHash() + " Says had holds : " + randomChord.getPosition());

            
            this.myFingerTable.setEntry(Integer.valueOf(data[0][3]), randomChord);

        }

        this.myFingerTable.printTable();


    }

    public void sendFile(String pathtofile){
        //.hashCode();


    }

    public void searchSpot(double hash){
        //look for this hash
        this.myFingerTable.searchForThisHash(hash);


    }

    public void requestHashResponse(String[][]data, connectionData con){
        System.out.println("This one says its got our node we requested : " + data[0][1]);
        //great lets send it to them then


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
