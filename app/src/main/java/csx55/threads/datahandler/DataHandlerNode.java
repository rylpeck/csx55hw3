package csx55.threads.datahandler;

import java.io.IOException;
import java.net.Socket;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import csx55.threads.util.connectionData;
import csx55.threads.util.queueObject;
import csx55.threads.Node.*;
import csx55.threads.wireformat.*;


//The point of this class is hard syncrhonization. Created by ndoe on creation alongside its sending and
//Recieving. 


public class DataHandlerNode extends DataHandler{
    //this holds all incoming data, and is done here. THREAD SAFE

    private String nodeName;
    protected final AtomicInteger messagesRecievedCount;
    protected final AtomicInteger messagesRelayed;
    protected ComputeNodeRunner parent = null;

    public DataHandlerNode(LinkedBlockingQueue<queueObject> dataQueue, ComputeNodeRunner parent, AtomicInteger mesRec, AtomicInteger mesRel){
        super();
        this.dataQueue = dataQueue;
        this.parent = parent;
        this.messagesRelayed = mesRel;
        this.messagesRecievedCount = mesRec;
    }
      

    //congrats, this one now ALSO handles any data in the dataqueue, this one works hard to route.
    public void run(){
        // System.out.println("Handler STarted");
 
         while(true){
             try {
                
                queueObject currentOBJ = dataQueue.poll(500, TimeUnit.MILLISECONDS); 
                 //we await a bit
                //System.out.println("Current load: " + this.parent.taskQueue.size());
                if (currentOBJ != null) {
                  processEvent(currentOBJ);
                }
                 
                else {
                     //this.parent.sendFinish();
 
                     Thread.sleep(2); 
                }
             } catch (InterruptedException e) {
                 //System.out.println("Thread interrupted");
                 //Thread.currentThread().interrupt();
             }
         }
 
 
    }


    protected void switchEvent(int type, String[][] data, connectionData con){
       // System.out.println("HandlingSomething%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        switch(type){
            case MESSAGE:
                //System.out.println("MESSAGE RECIEVED");
                //handleMessage(data, sock);
                break;
            case REGISTRATION_RESPONSE:
                //System.out.println("Registration Response");
                parent.registerRespond(data, con.getSocket());
                //this.nodeName = this.parent.myIP + ":" + this.parent.myPort;
                break;

            case DEREGISTRATION_RESPONSE:
                parent.deregisterRespond(data, con.getSocket());
                break;
            
            case ANOTHER_NODE:
                parent.makeMessengerCon(data, con);
                break;

            case INFORM_NODES_MAP:
                //System.out.println("Got an overlay registration");
                //parent.overlayCreation(currentEvent);
                
                //parent.overlayCreation(data, sock);
                break;

            case START_ROUNDS:
                parent.startRounds(data);
                break;

            case MESSAGE_NODES_LIST:
                System.out.println("THIS ONE");
                parent.setupConnections(data);
                break;
                
            case RETRIEVE_TRAFFIC:
                parent.trafficReport();
                break;
            case NODE_LOAD_REPORT:
                System.out.println("Node LOad");
                parent.passAlongPoll(data, myNodesName);
                break;

            case TASK_SEND:
                System.out.println("Task send ONE");
                parent.handleIncomingTask(data);
                break;
           
            case NODE_DONE:
            System.out.println("DoneE");
                this.parent.addFinished(data, myNodesName);
                break;

            case READY_WORK:
                this.parent.RecievedReady(data);
                break;

            default:
                //System.out.println("Invalid response for Node");

            
        }

    }




    
}
