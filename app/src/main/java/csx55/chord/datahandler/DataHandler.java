package csx55.chord.datahandler;

import java.io.IOException;
import java.net.Socket;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import csx55.chord.wireformat.*;
import csx55.chord.util.*;
import csx55.chord.Node.*;


public class DataHandler implements Runnable, Protocol{

    protected LinkedBlockingQueue<queueObject> dataQueue;

    public String myNodesName;

    public DataHandler(LinkedBlockingQueue<queueObject> dataQueue, Node parent, AtomicInteger mesRec, AtomicInteger mesRel){


    }

    public DataHandler() {


    }

    public void run(){
        // System.out.println("Handler STarted");
 
         while(true){
             try {
                 queueObject currentOBJ = dataQueue.poll(500, TimeUnit.MILLISECONDS); 
                 //we await a bit
                 if (currentOBJ != null) {
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


    protected void processEvent(queueObject currentOBJ){
        Event currentEvent = currentOBJ.getEvent();
        
        connectionData curCon = currentOBJ.getConnectionData();
        //System.out.print("Type Event Recieved:" );
        //System.out.println("Socket was: " + curSock);
        String[][] data = currentEvent.giveData();
        int type = currentEvent.getType();
        //System.out.println(type);
        switchEvent(type, data, curCon, currentOBJ.getBytes());
        //each thing will have its own switch event
    
    }

    protected void switchEvent(int type, String[][] data, connectionData con, byte[] fileData){
        //to be overloaded by the class
    }
    
}
