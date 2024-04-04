package csx55.threads.Node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import csx55.threads.wireformat.*;
import csx55.threads.transport.*;
import csx55.threads.util.*;
import csx55.threads.datahandler.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class RegisterNode extends Node{



    //keeps track of all our nodes, so that when they are done we can print out stuff.
    private HashMap <String, String> nodeStatus = new HashMap<String, String>();

    OverlayCreator overlayCreator = new OverlayCreator();

    //Cute little incrementer so we can keep track of nodes, and name them!
    private int nodesCurr = 0;

    private int threadWorkersNum = 0;

    //please note, mNodes is now NodesList
    //private HashMap <String, connectionData> mNodes = new HashMap<String, connectionData>();


    private ArrayList<String[]> allMap = new ArrayList<String[]>();

    public RegisterNode(int portNumber, String name){
        //Assume only one register ever
        this.myPort = portNumber;

        this.datahandler = new DataHandlerRegister(dataQueue, this);
        this.hndl = new Thread(datahandler);
        try {
            this.mySocket = new ServerSocket(this.myPort);
        } catch (IOException e) {
            // TODO 
            e.printStackTrace();
        }

        setupThreads();
        //setup();
    }



    public void listNodes(){
        System.out.println("Nodes connected to this register:");
        for (Map.Entry<String, connectionData> entry : this.NodesList.entrySet()) {
            //String name = entry.getKey();
            connectionData value = entry.getValue();
            String name = value.getIP() + ":" + value.getPort();
            System.out.println("Name = " + name + " | Host = " + value.getSocket().getInetAddress() + " | Port = " + value.getPort());
        }
        System.out.println("========================");
    }

    public void showWeights(){
        this.overlayCreator.showWeights();
    }


    @Override
    public synchronized void run(){
        
        synchronized (registerLock){
            try {
                
                //await

                registerLock.wait();

            } catch (InterruptedException e) {
                //Failure somewhere, whoops
                e.printStackTrace();
            }
        }
    }

    public void startRounds(int roundNum){
        //System.out.println("Starting rounds: ");
        //send a message to all nodes that has the number of rounds todo
        Event startRounds = EventFactory.createEvent(7);
        String message = (Integer.toString(roundNum));
        //System.out.println("Rounds are: " + message);
        startRounds.setData(message);
        //System.out.println("Setup, now mailing");
        sendAllConnections(startRounds, 7);
    }

    

    public void nodeReport(String [][]data, Socket currSocket){
        //System.out.println("Node report");
        String tempkey = data[0][0] +":" +data[0][1];
        this.nodeStatus.put(tempkey, "done");

        //check all values, then if so we move to a diff method
        int allDone = 0;
        for (Map.Entry<String, String> entry: this.nodeStatus.entrySet()){
            //System.out.println("Key is: " + entry.getKey());
            if (entry.getValue().equals("working")){
                allDone++;
            }
        }
        if (allDone == 0){
            //System.out.println("all nodes done");
            taskSummaryRequest();
        }
        else{
            //System.out.println("There are : " + allDone + " nodes still working");
        }
    }

    public void taskSummaryRequest(){

        Event retrieveTraffic = EventFactory.createEvent(RETRIEVE_TRAFFIC);
        retrieveTraffic.setData("traffic");
        
        sendAllConnections(retrieveTraffic, RETRIEVE_TRAFFIC);
    }

    public void summaryRecieved(String [][] data, Socket currSocket){
        //System.out.println("Summ");
        String tempkey = data[0][0];
        //System.out.println(data[0][0]);
        String statsData = "";
        for (int i = 0; i < data[1].length; i++){ 
            statsData = statsData + " " + data[1][i];
        }

        this.nodeStatus.put(tempkey, statsData);
        int records = 0;

        for (Map.Entry<String, String> entry: this.nodeStatus.entrySet()){
            //String valueAT = entry.getValue();
            //System.out.println(valueAT);
            if (entry.getValue().equals("working")){
                records++;
            }
        }

        if (records == 0){
            //System.out.println("all nodes done 2222");
            finalPrint();
        }
        else{
            //System.out.println("There are : " + records + " nodes still working");
        }

    }

    public void finalPrint(){
        //heres the format per cell
        //String data = tempkey + " " + intSent + " " + intRecieved + " " + MSC + " " + MRC + " " + MRR;
        //System.out.println("Printing out final report now");
        int TotaltasksCreated = 0;
        int TotaltasksCompleted = 0;

        int TotaltasksPulled = 0;
        int TotalTasksPushed = 0;

        

        //loop to get full nubmer of tasks for percentages first
        for (Map.Entry<String, String> entry: this.nodeStatus.entrySet()){
            //System.out.println(entry.getValue());

            String[] fields = entry.getValue().split("\\s+");
            //System.out.println(fields[4]);
            TotaltasksCompleted += Integer.valueOf(fields[4]);

        }

       //System.out.println("our header");
       System.out.println("                          Number   "+"   Number   "+"   Number  "+"   Number    "+"   Percent"); 
       System.out.println("                            of     "+"     of     "+"     of    "+"      of     "+"     of  "); 
       System.out.println("                          tasks    "+"    pulled  "+"   pushed  "+"   completed "+" total tasks");
       System.out.println("                           created "+"    tasks   "+"    tasks  "+"   tasks     "+"   performed" );
       System.out.println("=============================================================================================" );

       for (Map.Entry<String, String> entry: this.nodeStatus.entrySet()){
            
            //System.out.println(entry.getValue());

            String[] fields = entry.getValue().split("\\s+");
            //System.out.println(fields[0]);  
            //System.out.println(fields.length);
            int hold = Integer.valueOf(fields[4]);
            double quickMath = (double) hold/TotaltasksCompleted;
            quickMath = quickMath * 100;
            //flipped em here, am dumb              
            System.out.println("Node:" + entry.getKey()+ ":     " + fields[1] + "        " + fields[2] + "        " + fields[3] + "        " + fields[4] + "        " + quickMath);
            TotaltasksCreated+= Integer.valueOf(fields[1]);
            TotaltasksPulled+=  Integer.valueOf(fields[2]);
            TotalTasksPushed+=  Integer.valueOf(fields[3]);

            // intSentTotal += Integer.valueOf(fields[1]);
            //intRecTotal += Integer.valueOf(fields[2]);

                
            
        }
        System.out.println("=============================================================================================" );
        System.out.println("Summations:               " + TotaltasksCreated + "         " + TotaltasksPulled + "         " + TotalTasksPushed + "         " + TotaltasksCompleted + "        " +  100);

        
    }

    public void setupOverlay(int cr){
        //System.out.println("Settup overlay");
        this.overlayCreator = new OverlayCreator();
        this.threadWorkersNum = cr;

        //System.out.println("Attempting setup of Overlay");

        this.overlayCreator.initialize(this.NodesList);


        //System.out.println("Setup of Node creator...Done");
        this.overlayCreator.boltConnection();

        //System.out.println("Connections have been setup");
        //this.overlayCreator.showMap();
        //this.overlayCreator.showWeights();

        //we just use this to get the threadpool, its crafty aint it?
        //System.out.println("Assigning threadpool num of: " + cr);
        this.overlayCreator.assignWeights(cr);                                                                                      
        this.allMap = this.overlayCreator.getAllMap();
        //showWeights();

        assignNodetoNode();
    }

    public void assignNodetoNode(){
        //method will assign nodes to talk to the othernodes. 
        //System.out.println("Assinging node to node");
        String nodesToMessage = "";
        int counter = 0;
        String currentNodeName = "";

        for (Map.Entry<String, connectionData> entry : this.NodesList.entrySet()){
            currentNodeName = entry.getKey();
            counter = 0;
            nodesToMessage = "";
            for (String[] array : this.allMap) {
                if (array[0].equals(currentNodeName)){
                    //System.out.println(array[0]);
                    //System.out.println(currentNodeName);
                    if (nodesToMessage.equals("")){
                        nodesToMessage = array[1];
                    }
                    else{
                        nodesToMessage = nodesToMessage + " " + array[1];
                    }
                    counter++;
                }
                
                //for (String element : array) { 
                    //System.out.print(element + " "); 
                //}
            }
            this.nodeStatus.put(currentNodeName, "working");
            //System.out.println("This node will message: " + currentNodeName);
            //System.out.println(nodesToMessage);
            nodesToMessage = counter + " " + nodesToMessage + " " + this.threadWorkersNum + " " + this.NodesList.size();

            Event ev = EventFactory.createEvent(MESSAGE_NODES_LIST);

            ev.setData(nodesToMessage);
            
            try {
                Response(entry.getValue(), ev, MESSAGE_NODES_LIST);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }



    }

    public void sendOverlay(){
        //else lets distribute weights to everyone
        //allMap is what we send

        //Protocol is INFORM_NODES_MAP
        //Convert inform nodes map over...
        //Event linkWeights = EventFactory.createEvent(5);
        //String[][] convert = this.overlayCreator.getAllMapReadyShip();
        //linkWeights.setData(convert);
        //sendAllConnections(linkWeights);
       
    }


    public void sendAllConnections(Event messsageToSend, int type){
        
        //itterate through the MessenderData list of mNodes
        //System.out.println("Sending Messages: ");
        for (Map.Entry<String, connectionData> entry : this.NodesList.entrySet()) {
            
            String name = entry.getKey();
            //System.out.println("Dispatching to: " + name);
            connectionData value = entry.getValue();
                        this.nodeStatus.put(name, "working");
            TCPSender tempSender = value.getTcpSender();
            try {
                tempSender.sendMessage(messsageToSend.getBytes(), type);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    public Boolean validateConnection(Socket sock, String nodeName){
        //System.out.print("Validating connection...");

        //this.myIP = this.serverSocket.getInetAddress().getHostAddress();
        //it wont come from our reciever port!
        String[] justIP = nodeName.split(":");
        String tempName = sock.getInetAddress().getHostAddress().toString();

        if (tempName.contains("/")){
            tempName = tempName.replace("/", "");
        }  
        if (sock.getInetAddress().isLoopbackAddress() || sock.getInetAddress().isAnyLocalAddress()){
            //System.out.println("Is localhost:");
            //so that parts good
            //check port
            return true;
        }
        else{
            //System.out.println(justIP[0]);
            ///System.out.println(tempName);

            if (justIP[0].equals(tempName))
                return true;
        }

        return false;
        
    }

    public int registerRequest(String [][]data, connectionData con) throws IOException{
        //System.out.println("Register Request Recieved");
            
        //improvedConnection(newNode);
        String tempkey = data[0][0] +":" +data[0][1];
        Boolean correct = validateConnection(con.getSocket(), tempkey);
        if (correct == false){
            //System.out.println("Rejected, not true ip");
        }
        if (NodesList.containsKey(tempkey)){
            //System.out.println("Rejected");
            //Key is already on stack, reject
            connectionData tempNode = this.NodesList.get(tempkey);
            registerResponse(tempNode, 1);
        }
        else{
            //System.out.println("Accepted, " +tempkey+ " has connected to the register");
            con.setIP(data[0][0]);
            con.setPort(Integer.valueOf(data[0][1]));

            registerResponse(con, 0);

            this.NodesList.put(tempkey, con);
        }
        //Key is just ipstring+node
        //System.out.println("Processed request");


        
                
        return 0;
    }


    public void deregisterRequest(Event currentEvent, Socket incomingSocket) throws IOException{
       // System.out.println("Deregistration Request Recieved");
        String[][] data = currentEvent.giveData();
        //improvedConnection(newNode);
        String tempkey = data[0][0] +":" +data[0][1];

        Boolean correct = validateConnection(incomingSocket, tempkey);
        if (correct == false){
            //System.out.println("Rejected, not true ip");
        }

        if (this.NodesList.containsKey(tempkey)){
            //System.out.println("Accepted");
            connectionData tempNode = this.NodesList.get(tempkey);
            Socket tempSock = tempNode.getSocket();
            this.NodesList.remove(tempkey);
            nodesCurr--;
            Event ev = EventFactory.createEvent(4);
            String arguments = ("0");
            ev.setData(arguments);
            Response(tempNode, ev, 4);
            tempSock.close();
            //remove it
        }
        else{
            //System.out.println("No registration found for this node");
            Event ev = EventFactory.createEvent(4);
            String arguments = ("1");
            ev.setData(arguments);
            TCPSender temp = new TCPSender(incomingSocket);
            connectionData tempNode = new connectionData(incomingSocket, temp, data[0][0] , Integer.valueOf(data[0][1]));
            Response(tempNode, ev, 4);
        }
    
    }

    public void Response(connectionData connect, Event ev, int type) throws IOException{
        TCPSender sender = connect.getTcpSender();
        sender.sendMessage(ev.getBytes(), type);
    }
    
    private void registerResponse(connectionData temp, int status) throws IOException{
        //Successful Registration, lets tell him he did a good job
        //The connection is already open lets use that. 
        
        if (temp == null){
            //System.out.println("Null node, returning");
            return;
        }
        Event RegResponse = EventFactory.createEvent(1);
        //nodesCurr
        //0 is operational
            //1 is alreadyRegistered // TODO
        if (status == 0){
            this.nodesCurr++;
            String newName = "Node" + this.nodesCurr;
            //System.out.println(temp.getIP());
            String arguments = ("0" + " " + newName);
            RegResponse.setData(arguments);
        }
        else{
            String arguments = ("1" + " " + "NoName");
            RegResponse.setData(arguments);
        }

        TCPSender sender = temp.getTcpSender();
        sender.sendMessage(RegResponse.getBytes(), 1);
        
   
    }

    public static connectionData existingConnection(connectionData newNode, Socket incomingSock) throws IOException{
        //Socket clientSock = new Socket(mess.getIP(), mess.getPort());
        //Socket outGoingSock = new Socket(newNode.getIP(), newNode.getPort());
        TCPSender sender2 = new TCPSender(incomingSock); //whoops

        //TCPSender TCPSender = new TCPSender(outGoingSock);
        //mess.connections.put(name, connectionData);
        newNode.setSender(sender2);
        return newNode;
    }

    



    





    
}
