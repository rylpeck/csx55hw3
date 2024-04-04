package csx55.threads.hashing;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import csx55.threads.Node.ComputeNodeRunner;
import csx55.threads.transport.TCPSender;
import csx55.threads.util.*;
import csx55.threads.wireformat.*;

//this is our miner manager, controls our threadpool and inserts as miners become available. 
public class MinerManager implements Runnable, Protocol{

    HashMap<String, minerObject> workerMap = null;
    LinkedBlockingDeque<Task> taskQueue = null;
    LinkedBlockingQueue<String> availWorkers = null;

    final AtomicInteger tasksCompleted;
    final AtomicInteger tasksHeldMigrated; // track how many tasks we have taken, decrement it here. 
    
    ComputeNodeRunner parent = null;
    LoadManager LoadManager = null;
    public boolean running = true;
    public boolean working = true;

    private volatile int maxWorkers = 0;

    

    public MinerManager(LinkedBlockingDeque<Task> taskQ, LoadManager LM, ComputeNodeRunner dad, AtomicInteger tC, AtomicInteger thm){

        this.workerMap = new HashMap<String, minerObject>();
        this.availWorkers = new LinkedBlockingQueue<String>(); 
        this.tasksCompleted = tC;
        this.tasksHeldMigrated = thm;
        this.taskQueue = taskQ;
        this.parent = dad;
        this.LoadManager = LM;
  
    }

    public synchronized void setMaxworkers(Integer inte){
        this.maxWorkers = inte;
    }

    public synchronized int getMaxWorkers(){
        return this.maxWorkers;
    }

    public void setup(int size){
        //fix issue with more threads
        setMaxworkers(size);
        if (this.workerMap!=null){
            //System.out.println("Killing threads");
            for (Map.Entry<String, minerObject> entry: this.workerMap.entrySet()){
                Thread temp = entry.getValue().getThread();
                temp.interrupt();

            }
        }

        for (int i =0; i < size; i++){
            
            String tempName = "miner" + i;
            //MinerThread tempMiner = new MinerThread(tempName, availWorkers, this.tasksCompleted);
            //Thread tempThread = new Thread(tempMiner);
            //tempThread.setName(tempName);
            try {
                this.availWorkers.put(tempName);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //tempThread.start();
            //minerObject saveMe = new minerObject(tempThread, tempMiner);
            //workerMap.put(tempName, saveMe);
        }
    }

    @Override
    public void run(){
        while (running){
            //System.out.println("Initial size: " + this.taskQueue.size());
            //manageWorkers();
            //stall();
            //System.out.println("NOW WORK");
            //runWork();

            //do mining
        }
        
    }

    
    public int getAvailWorkers(){
       // System.out.println("PRE");
        //System.out.println("Avail workers: "+ this.availWorkers.size());
        ///System.out.println("Max workers: " + this.workerMap.size());
        //System.out.println("After");
        if (this.workerMap.size() == this.availWorkers.size()){
            return 0;
        }
        return 4;
    }


    public void manageWorkers(){
        Boolean workerSet = true;
        int emptyCycle = 0;
        int initSize = this.taskQueue.size() + 0;
        while(workerSet){
            try {
                
                if (emptyCycle > 1){
                    //this.parent.speakFinished();
                    workerSet = false;
                }
                //Thread.sleep(500);
                
                //System.out.println("My current load is: " + this.taskQueue.size());
            //prevent overloading with how many times we send and process messages.
                
                emptyCycle++;
                
                //this.LoadManager.balanceLoads();

                //this.parent.myRounds.sendBalance();

                initSize = this.taskQueue.size();
                Thread.sleep(400);
                //System.out.println("Current Load: " + this.taskQueue.size());
                
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                //System.out.println("DEad");
                //assume we have been shut down, lets kill every thread
                for (Map.Entry<String, minerObject> entry: this.workerMap.entrySet()){
                    Thread temp = entry.getValue().getThread();
                    entry.getValue().getMiner().prepDead();
                    temp.interrupt();
                }
                //System.out.println("Miner shutting down");
                return;
            }

        }
        return;
    }

    public void runWork(){
        //check our sorted, then just run bare
        int sortCycle = 0;
        this.working = true;
        while(this.working){
            //System.out.println("I am working888888888888888888888888888888");
            String availWorker;
            minerObject tempWorker;
            Task curTask = null;
    
            availWorker = availWorkers.peek();
            if (availWorker != null){
                curTask = this.taskQueue.peek();
                if (curTask != null){

                    if (this.tasksHeldMigrated.get() > 0){
                        this.tasksHeldMigrated.getAndDecrement();
                    }
                    curTask = this.taskQueue.poll();
                    availWorker = this.availWorkers.poll();
                    tempWorker = this.workerMap.get(availWorker);
                    tempWorker.getMiner().setTask(curTask);
                    tempWorker.getThread().interrupt();
                    //System.out.println("STuff");
                   
                }
               //Thread.sleep(400);
    
                //System.out.println("My current load is: " + this.taskQueue.size());
                //prevent overloading with how many times we send and process messages.
             }


        }


    }

    public synchronized void checkEnd(){
        //jank
        //share load
        //System.out.println("WE ARE DONE");
        int neighborLoad = this.parent.LoadManager.leftLoad + this.parent.LoadManager.rightLoad + this.taskQueue.size();
        if (neighborLoad == 0 && (this.availWorkers.size() == getMaxWorkers())){
            //this.parent.speakFinished();
            this.working = false;
            this.running = false;
        }

    }

    
}
