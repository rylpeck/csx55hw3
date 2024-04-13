package csx55.chord.Node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import csx55.chord.wireformat.*;
import csx55.chord.transport.*;
import csx55.chord.util.*;
import csx55.chord.datahandler.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;


public class discoveryNode extends Node{

    private HashMap <String, String> peerList = new HashMap<String, String>();
    private int nodesCurr = 0;


    public discoveryNode(int portNumber, String name){
        //Assume only one register ever
        this.myPort = portNumber;

        this.datahandler = new DataHandlerDiscovery(dataQueue, this);
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
        System.out.println("peer connected to this register:");
        for (Map.Entry<String, connectionData> entry : this.NodesList.entrySet()) {
            //String name = entry.getKey();
            connectionData value = entry.getValue();
            String name = value.getIP() + ":" + value.getPort();
            System.out.println("Name = " + name + " | Host = " + value.getSocket().getInetAddress() + " | Port = " + value.getPort());
        }
        System.out.println("========================");
    }


    private void registerResponse(connectionData temp, int status) throws IOException{
        //Successful Registration, lets tell him he did a good job
        //The connection is already open lets use that. 
        String findME = "";
        //pull a random node
        Random rand = new Random();
        if (this.NodesList.size() == 0){
            System.out.println("you are first node, nothing will be sent, except yay");
            findME = "yay";
        }
        else{
            int randomID = rand.nextInt(this.NodesList.size());
            List<connectionData> values = new ArrayList<>(NodesList.values());
            connectionData randomValue = values.get(randomID);
            System.out.print("IP: " + randomValue.getIP());
            System.out.print("Port: " + randomValue.getPort());
            findME = randomValue.getIP() + ":" + randomValue.getPort();
        }

       

               
        
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
            String arguments = ("0" + " " + newName + " " + findME) ;
            RegResponse.setData(arguments);
        }
        else{
            String arguments = ("1" + " " + "NoName" + " " + "goawayNOW");
            RegResponse.setData(arguments);
        }

        TCPSender sender = temp.getTcpSender();
        sender.sendMessage(RegResponse.getBytes(), 1);
    }
        
   


    public int registerRequest(String [][]data, connectionData con) throws IOException{
        //System.out.println("Register Request Recieved");
            
        //improvedConnection(newNode);
        String tempkey = data[0][0] +":" +data[0][1];
        double nodesHash = Double.valueOf(data[0][2]);
        Boolean correct = validateConnection(con.getSocket(), tempkey, nodesHash);
        if (correct == false){
            //System.out.println("Rejected, not true ip");
            return 0;
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

            //we now use the hashkey to store it
            this.NodesList.put(data[0][2], con);
        }
        //Key is just ipstring+node
        //System.out.println("Processed request");

      
                
        return 0;
    }

    public Boolean validateConnection(Socket sock, String nodeName, double nodesHash){
        //System.out.print("Validating connection...");

        //this.myIP = this.serverSocket.getInetAddress().getHostAddress();
        //it wont come from our reciever port!
        String[] justIP = nodeName.split(":");
        String tempName = sock.getInetAddress().getHostAddress().toString();

        if (tempName.contains("/")){
            tempName = tempName.replace("/", "");
        }  
        if (sock.getInetAddress().isLoopbackAddress() || sock.getInetAddress().isAnyLocalAddress()){
            System.out.println("Is localhost:");
            //so that parts good
            //check port

            //heres how we cheat the hashcode
            //-2147483648 to 2147483647
            double hashedName = nodeName.hashCode();

            if (hashedName < 0){
                hashedName = (-hashedName) + 2147483647;
            }

            if (nodesHash == ((hashedName))){
                return true;
            }
            System.out.println("FALSE");
            return false;
        }
        else{
            //System.out.println(justIP[0]);
            ///System.out.println(tempName);

            if (justIP[0].equals(tempName))
                return true;
        }

        return false;
        
    }
    


    
}
