package csx55.threads.transport;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.io.IOException;

import csx55.threads.Node.Node;
import csx55.threads.util.*;

//this manages our tcprecievers
public class TCPReceiverThread implements Runnable{
    private ServerSocket socket;
    private Boolean running = true;
    private Node parent = null;

    //data input line, shared between recievers and datahandler
    //IS threadsafe, uses CAS compare and swap. 
    LinkedBlockingQueue<queueObject> dataQueue;


    //List of threads
    //ArrayList<Thread> threadList = new ArrayList<Thread>();
    HashMap<String, Thread> threadMap = new HashMap<String, Thread>();

    //Number of messages
    private AtomicInteger MessagesCaught = new AtomicInteger(0);

    //starts this
    public TCPReceiverThread(ServerSocket socket, LinkedBlockingQueue<queueObject> data, Node Parent) {
        this.socket = socket;
        this.parent = Parent;
        this.dataQueue = data;
    }
    //in case we need to tell it to listen toa  socket, say that we made a connection, we know who this line is
    public TCPReceiver nodeListen(Socket client, String tempname, connectionData con){
        //ln("Listening....");
        try {
            TCPReceiver tcpReceiver = new TCPReceiver(tempname, client, this.dataQueue, this.MessagesCaught, this);
            tcpReceiver.setCon(con);
            Thread tcpT = new Thread(tcpReceiver);
            tcpT.setName(tempname);
            threadMap.put(tempname, tcpT);
            tcpT.start();
            return tcpReceiver;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //return an error basically
        return null;
    }

    //listens to register, not a node, dedicated
    public void listenTo(Socket client){
        //if sender is making a connection, we should open a listener to it as well
        String tempname = ("RecieverT" + threadMap.size());
        
        try {
            TCPReceiver tcpReceiver = new TCPReceiver(tempname, client, this.dataQueue, this.MessagesCaught, this);
            Thread tcpT = new Thread(tcpReceiver);
            tcpT.setName(tempname);
            threadMap.put(tempname, tcpT);
            tcpT.start();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
                    


    }
 
    //Reciever starts here
    
    public void run() {

        while (running){
            //Start a datahandler, maybe we scale as well?
            
            try {
                Socket client = this.socket.accept();
                //System.out.println("Connection Established");

                try {
                    //make our connection data to be slid up with the rest of the information

                    InetSocketAddress remoteAddress = (InetSocketAddress) client.getRemoteSocketAddress();
                    
                    int tempPort = remoteAddress.getPort();
                    
                    String tempIP = client.getInetAddress().toString();

                   // System.out.println("Tempy " + tempIP);
                    
                    if (tempIP.contains("/")){
                        tempIP = tempIP.replace("/", "");
                    }

                    //localhost? lets make it a better addr

                    TCPSender newSender = new TCPSender(client);
                    //end of making connection data

                    connectionData con = new connectionData(client, newSender, tempIP, tempPort);
                    String tempname = ("RecieverT" + threadMap.size());
                    TCPReceiver tcpReceiver = new TCPReceiver(tempname, client, this.dataQueue, this.MessagesCaught, this);
                    tcpReceiver.setCon(con);
                    

                    //we will now pass this up and see if it can be a node in our list
                    
                    Thread tcpT = new Thread(tcpReceiver);
                    tcpT.setName(tempname);
                    threadMap.put(tempname, tcpT);
                    tcpT.start();
                    //tcpT.join();
                    //System.out.println(outputString);

                } catch (IOException e) {
                    //System.out.println("Error creating TCPReceiver: " + e.getMessage());
                
                }
            } catch (IOException e) {
                
                e.printStackTrace();
            }
        }
    }

    //handle a close gracefully
    protected void handleClose(String threadName, Socket socket){
        
        Thread temp = threadMap.get(threadName);
        temp.interrupt(); //stop that thread, pass it up
        parent.handleClose(socket);
    }
}