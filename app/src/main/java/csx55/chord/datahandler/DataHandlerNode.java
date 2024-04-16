package csx55.chord.datahandler;

import java.io.IOException;
import java.net.Socket;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import csx55.chord.util.connectionData;
import csx55.chord.util.queueObject;
import csx55.chord.Node.*;
import csx55.chord.wireformat.*;


//The point of this class is hard syncrhonization. Created by ndoe on creation alongside its sending and
//Recieving. 


public class DataHandlerNode extends DataHandler{
    //this holds all incoming data, and is done here. THREAD SAFE

    private String nodeName;
    protected final AtomicInteger messagesRecievedCount;
    protected final AtomicInteger messagesRelayed;
    protected peerNode parent = null;

    public DataHandlerNode(LinkedBlockingQueue<queueObject> dataQueue, peerNode parent, AtomicInteger mesRec, AtomicInteger mesRel){
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
       //System.out.println("HandlingSomething%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        switch(type){
            case MESSAGE:
                //System.out.println("MESSAGE RECIEVED");
                //handleMessage(data, sock);
                break;
            case REGISTRATION_RESPONSE:
                System.out.println("Registration Response");
                parent.registerRespond(data, con.getSocket());
                //this.nodeName = this.parent.myIP + ":" + this.parent.myPort;
                break;

            case contactPEERINITIAL:
                parent.registerPeer(data, con);
                break;
            
            case CONTACTPEERRESPONSE:
                parent.peerResponseRecieved(data, con);
                break;

            case DEREGISTRATION_RESPONSE:
                //parent.deregisterRespond(data, con.getSocket());
                break;
            case NOTIFYNEIGHBOR:
                System.out.println("Neighbor request");
                parent.recievedNeighbor(data, con);
                break;
            case GIVEFINGERTABLE:
                parent.recievedFingerTable(data, con);
                break;
            case REQUESTTABLEINFO:
                parent.handleTableQuery(data, con);
                break;
            case RESPONSETABLEINFO:
                parent.responseTableInfo(data, con);
                break;

            case VALIDATEFORWARD:
                parent.validateForwardReply(data, con);
                break;

            case VALIDATEFORWARDRESPONSE:
                parent.validateForwardRespond(data, con);
                break;
            
            case VALIDATEFINGER:
                parent.validateRandomReply(data, con);
                break;

            case VALIDATEFINGERRESPONSE:
                parent.validationReturn(data, con);
                break;
            case REQUESTHASHlOCATION:
                parent.requestHashLocation(data, con);
                break;

            case REQUESTHASHRESPONSE:
                parent.requestHashResponse(data, con);
                break;

            case NEWPEER:
                parent.newPeerRecieved(con, data);
                break;

            default:
                //System.out.println("Invalid response for Node");

            
        }

    }




    
}
