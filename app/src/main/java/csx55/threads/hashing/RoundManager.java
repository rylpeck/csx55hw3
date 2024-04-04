package csx55.threads.hashing;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import csx55.threads.Node.*;
import csx55.threads.transport.TCPSender;
import csx55.threads.wireformat.Event;
import csx55.threads.wireformat.EventFactory;
import csx55.threads.wireformat.Protocol;

public class RoundManager implements Runnable, Protocol{

    ConcurrentLinkedQueue<Task> taskQueue = null;
    ConcurrentLinkedQueue<Task> taskQueueTemp = null;
    MinerManager myManager = null;
    Thread myManagerThread = null;
    ComputeNodeRunner parent = null;
    final AtomicInteger tasksCreated;
    int mining = 0;
    

    public String myIp = null;
    int myPort = 0;

    public int rounds = 0;

    public RoundManager(AtomicInteger tasksCreated, ConcurrentLinkedQueue<Task> activeQueue, ConcurrentLinkedQueue<Task> tempQueue, String myIp, ComputeNodeRunner parent){
        this.taskQueue = activeQueue;
        this.tasksCreated = tasksCreated;
        this.taskQueueTemp = tempQueue;
        this.parent = parent;
        this.myIp = myIp;
    }

    public void setRound(int r){
        this.rounds = r;
    }

    @Override
    public void run(){

        System.out.println("UWU");

        boolean simpleToggle = true;

        Random rand = new Random();
        
        System.out.println("Starting rounds");
        System.out.println("My con is: " + this.parent.myConnection.getPort());
        
        for (int j = 0; j < rounds; j++){
            this.taskQueueTemp.clear();
            this.mining = 0;
            simpleToggle = true;
            //ROUND NUMBER CHANGE THIS ANGRY NOISES
            int randomNumber = rand.nextInt((1000 - 1) + 1) + 1;
            //make equivelent nodes
            for (int i =0; i < randomNumber; i++){
                Task tempTask2 = new Task(this.myIp, this.myPort, j, new Random().nextInt());
                this.taskQueueTemp.add(tempTask2);
                
                this.tasksCreated.getAndIncrement();
            }
            System.out.println("Finished that looper.");
            
            System.out.println("Sitting here");
            while(simpleToggle){
                //idle
            }

            while (simpleToggle){
                if (this.taskQueueTemp.size() == this.parent.avg){
                    for (int y = 0; y < randomNumber; y++){
                        this.taskQueue.add(this.taskQueueTemp.poll());
                    }
                    simpleToggle = false;
                }
            }

            simpleToggle = true;

            while(simpleToggle){
                if (this.taskQueue.size() == 0){
                    simpleToggle = false;
                }
            }
            
            System.out.println("Vibing");
            System.out.println("Balanced, lets GO");

            

        }

        this.parent.speakFinished();

    }

    
}
