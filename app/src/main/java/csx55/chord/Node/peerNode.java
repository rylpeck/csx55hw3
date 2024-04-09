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

import csx55.chord.wireformat.*;
import csx55.chord.transport.*;
import csx55.chord.util.*;
import csx55.chord.datahandler.*;

import java.util.concurrent.LinkedBlockingDeque;



public class peerNode extends Node {

     //class specific nodes
    private connectionData register;
    protected String regHostName = null;
    protected Integer regPortNumber = null;
    private volatile String myName;
    private int myHash;

    private fingerTable myFingerTable;



    public connectionData myForwardCon = null;
    public connectionData myBackwardCon = null;


    public peerNode(String ip, int port, String reghost, int regpot){
        this.myIP = ip;
        this.myPort = port;
        System.out.println("hi");
        this.myName = ip + ":" + port;
        this.myHash = Math.abs(this.myName.hashCode());
        this.myHash = 24;
        System.out.println("Hash as follows: ");
        System.out.println(myHash);
  
        System.out.println(myHash);

        //System.out.println("I am : " + getMyName());
        
        this.NodesList = new HashMap<String, connectionData>();  
        DataHandlerNode handle = new DataHandlerNode(dataQueue, this, this.messagesRecievedCount, this.messagesRelayed);
        this.hndl = new Thread(handle);
        this.datahandler = handle;
        //System.out.println("My socket is: " + this.mySocket);

        this.regHostName = reghost;
        this.regPortNumber = regpot;
        this.myFingerTable = new fingerTable(this);


        setup();
        setupThreads();
        setupRegisterConnection();
        register();

    }

    public int giveHash(){
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
                this.datahandler.myNodesName = this.myIP + ":" + this.myPort;
            }
            registerLock.notify();
        }

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
       
        String arguments = (this.myIP + " " + this.myPort + " " + this.myHash);
        this.myName = this.myIP +":" + this.myPort;
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
