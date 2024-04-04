package csx55.threads.Node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import csx55.threads.wireformat.*;
import csx55.threads.transport.*;
import csx55.threads.util.*;
import csx55.threads.datahandler.*;

public class Node implements Runnable, Protocol{

    //lets make it cleaner this time


    private volatile String myName;
    protected volatile int myPort;
    protected volatile String myIP;

    protected volatile ServerSocket mySocket = null;

    //still use a lock for register operations, just in case.
    protected final Object registerLock = new Object();

    //we may use this agian
    public volatile Semaphore statsLock = new Semaphore(0);



    //these are only used by methods that are synchronzied
    protected long intSent = 0;
    protected long intRecieved = 0;

    //atomic style ints to keep tarck of recieved, sent and relayed.
    protected AtomicInteger messagesRecievedCount = new AtomicInteger(0);
    protected AtomicInteger messagesSentCount = new AtomicInteger(0);
    protected AtomicInteger messagesRelayed = new AtomicInteger(0);


    //these five things may be eliminated, depends on how im feeling
    
    //data queue to be processed
    LinkedBlockingQueue<queueObject> dataQueue = new LinkedBlockingQueue<queueObject>();

    //routing map taken from the ADJ graph
    public Map<String, List<String>> routingMap;
    
    //converted routing map, with proper changes fromt he routing map to a string, instead of a list
    public Map<String, String> convertedRoutingMap;

    //structure to hold all we talk to, seems better then what we used before.
    public HashMap<String, connectionData> NodesList;

    //bundle of our threads, and other methods called, and used. TCP send is not used, doesnt need to be threaded.
    protected Thread  tcpRecThread = null;
    protected Thread tcpSendThread = null;
    protected TCPReceiverThread tcpRec = null;
    

    protected DataHandler datahandler = null;
    protected Thread hndl;



    public Node(){
        //this one is designed to runsetup
        this.NodesList = new HashMap<String, connectionData>();
    }
    public Node(int port, String ip){
        //odds are this is registry
        this.myPort = port;
        this.myIP = ip;
        this.NodesList = new HashMap<String, connectionData>();
    }

    public void showConnections(){

    }

    public void debug(){
        
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

    public synchronized void setup(){

        synchronized (registerLock) {
            try {
                this.mySocket = new ServerSocket(this.myPort);
            } catch (IOException e) {
                // TODO 
                e.printStackTrace();
            }
            this.myPort = this.mySocket.getLocalPort();
            //sock.getInetAddress().toString();
            this.myIP = this.mySocket.getInetAddress().getHostAddress();
        }
    }

    public void handleClose(Socket socket){
        //to be overloaded
    }


    protected void setupThreads(){
        //ln("Thread setup");
        synchronized (registerLock) {
            //System.out.println("Starting Reciever and Sender");
            TCPReceiverThread tcpt = new TCPReceiverThread(this.mySocket, dataQueue, this);
            Thread tcpThread = new Thread(tcpt);
            tcpThread.setName("TCPRecieverThread");
            this.tcpRecThread = tcpThread;
            this.tcpRec = tcpt;
            
            //hndl1.setName("handler thread" + handlerList.size());
            //handlerList.add(hndl1);
            registerLock.notify();
            this.hndl.start();
            this.tcpRecThread.start();
        }
        //System.out.println("Threads setup");
    }

   
}
