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


public class DataHandlerRegister extends DataHandler{


    private RegisterNode parent = null;

    public DataHandlerRegister(LinkedBlockingQueue<queueObject> dataQueue, RegisterNode parent){
        this.parent = parent;
        this.dataQueue = dataQueue;
    }

    public void run(){
        //System.out.println("Handler STarted");
 
         while(true){
            //System.out.println("EEEE");
             try {
                 queueObject currentOBJ = dataQueue.poll(500, TimeUnit.MILLISECONDS); 
                 //we await a bit
                 if (currentOBJ != null) {
                   //System.out.println("Handling something");
                   processEvent(currentOBJ);
                  }
                 
                 else {
                     //this.parent.sendFinish();
 
                     Thread.sleep(500); 
                 }
             } catch (InterruptedException e) {
                 //System.out.println("Thread interrupted");
                 //Thread.currentThread().interrupt();
             }
         }
 
 
    }

    



    protected synchronized void switchEvent(int type, String[][] data, connectionData con){
        //System.out.println("Type in handler: " + type);
        switch(type){
            case MESSAGE:
                    //System.out.println("Register has recieved message, ignoring, please check links");
                    
                break;
            case REGISTRATION_REQUEST:
                //number 3
                //System.out.println("Registration Request");
                    try {
                        this.parent.registerRequest(data, con);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
            
                //Call to RegistryNode, let it do the work
                break;
            case DEREGISTRATION_REQUEST:
                //number 4
            
                //this.parent.deregisterRequest(currentEvent, currentSocket);
                break;
           
            case FINISHED_ROUNDS:
                this.parent.nodeReport(data, con.getSocket());
                break;
            
            case TRAFFIC_SUMMARY:
                //System.out.println("Summary Recieved");
                this.parent.summaryRecieved(data, con.getSocket());
                break;
                
            //Call to deRegistryNode, let it do the work
                
            default:
                System.out.println("Unknown Message");
                break;
        }

    }
                //System.out.println("Unknown Message");



    
}
