package csx55.threads.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import csx55.threads.ComputeNode;
import csx55.threads.Node.ComputeNodeRunner;
import csx55.threads.hashing.Task;
import csx55.threads.wireformat.Event;
import csx55.threads.wireformat.EventFactory;
import csx55.threads.wireformat.Protocol;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.LinkedBlockingDeque;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class LoadManager implements Protocol{
    //class to keep track of our load, and balanace to others if need be.
    ConcurrentLinkedQueue<Task> taskQueue = null;


    public String myName = "";
    

    //we will also keep track of other peoples loads

    HashMap<String, Integer> loadMap = new HashMap<String, Integer>();
    HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
    HashMap<String, Integer> passMap = new HashMap<String, Integer>();


    ComputeNodeRunner parent = null;

    
    public int lastCurrentLoad = 0;
    private int numberOfWorkers = 0;
    public int numberofNodes = 0;
    public int shoved = 0;

    public int leftLoad = 0;
    public int rightLoad = 0;

    public LoadManager(ConcurrentLinkedQueue<Task> taskQueue2, ComputeNodeRunner dad){
        this.taskQueue = taskQueue2;
        this.parent = dad;
        //this.tasksHeldMigrated = thm;
        //this.tasksSent = ts;
    }

    public int checkMap2(){
        //System.out.println(this.loadMap.size());
        if  (this.passMap.size() > this.numberofNodes){
           return 1;
        }
        return 0;
    }

    public int getLoadMapSize(){
        return this.loadMap.size();
    }

    public synchronized void addPassData(String name, String namesLoad){
        System.out.println("HEARD");
        this.passMap.put(name, Integer.valueOf(namesLoad));
        if (checkMap2() == 1){
            this.parent.go();
            this.passMap.clear();
        }
        //balanceLoads();
    } 

    public synchronized void addLoadData(String name, String namesLoad){
        System.out.println("Adding data");
        System.out.println("Name : " + name + " : " + namesLoad);
        this.loadMap.put(name, Integer.valueOf(namesLoad));
        if (checkMap() == 1){
            System.out.println(getMySpare());
        }

        //balanceLoads();
    }    

 
    public int GiveOurLoad(){
        return this.taskQueue.size();
    }



    public int checkMap(){
        //System.out.println(this.loadMap.size());
        if  (this.loadMap.size() == this.numberofNodes){
           return 1;
        }
        return 0;
    }

    public int verifyEnd(){
        if (checkMap() == 1){
            int totalLoadLeft = 0;
            for (Map.Entry<String, Integer> entry : this.loadMap.entrySet()) {
                //System.out.println(entry.getKey() + " " + entry.getValue());
                totalLoadLeft += entry.getValue();
            }
            
            totalLoadLeft += this.parent.myManager.getAvailWorkers();
            System.out.println("Runback:");
            return totalLoadLeft;
        }

        return 100;
    }

    public synchronized int getMySpare(){



        this.lastCurrentLoad = this.taskQueue.size();
        //System.out.println("Balancing");
        this.tempMap.clear();
         int totalLoad = this.taskQueue.size() + 0;

        for (Map.Entry<String, Integer> entry : this.loadMap.entrySet()) {
            totalLoad += entry.getValue();
            System.out.println(entry.getValue());
        }

        //lets get the avg load

        int avgLoad = (int)Math.floor((double)totalLoad/(this.loadMap.size() +1));
        int oddjust = 0;
        if (totalLoad%this.loadMap.size() != 0){
            oddjust = 1;
        }
        System.out.println("My load is: " + this.lastCurrentLoad);
        System.out.println("Avg will be: " + avgLoad);
        
        this.parent.avg = avgLoad + oddjust;


        
        int iCanDonate = (this.lastCurrentLoad - avgLoad);
        if (iCanDonate < 0){
            iCanDonate = 0;
        }
        System.out.println("I will share: "  + iCanDonate);

        this.parent.sendTask(iCanDonate, this.myName);

        this.loadMap.clear();

        return iCanDonate;
    }


    

   
    
        
    
}
