package csx55.threads.hashing;

import java.security.MessageDigest;

import java.security.NoSuchAlgorithmException;
import java.util.Random;

import java.util.concurrent.ConcurrentLinkedQueue;

import java.util.concurrent.atomic.AtomicInteger;

public class MinerThread implements Runnable{

    Task myTask = null;
    String myID = null;
    private static final int LEADING_ZEROS = 17;
    private final MessageDigest sha256;
    private Task currentTask;
    private boolean running = true;

    final AtomicInteger tasksCompleted;

    final ConcurrentLinkedQueue<Task> taskQueue;

    

    public MinerThread(String id, AtomicInteger TC, ConcurrentLinkedQueue<Task> tq){
        this.myID = id;
        this.taskQueue = tq;
        this.tasksCompleted = TC;

        try {
            sha256 = MessageDigest.getInstance("SHA3-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    //send out ho many
    //thne wait
    //then send your all extra
    //then if avg you are good, mine

    //mining cant happen until you get all passed htough packets.

    //then send yours and wait
    //then again and again


    @Override
    public void run(){

        while(true){
 
            this.myTask = taskQueue.poll();
            if (this.myTask != null){
                mine(this.myTask);
            }

        }
       // do stuff
    }

    //verbatum copy
    private int leadingZeros(byte[] hash) {
        int count = 0;
       
        for (byte b : hash) {
            if (b == 0) {
                count += 8;
            } else {
                int i = 0x80;
                while ((b & i) == 0) {
                    count++;
                    i >>= 1;
                }
                break;
            }
        }
        return count;
    }

    public void prepDead(){
        this.running = !running;
    }

    public void setTask(Task task){
        this.currentTask = task;
    }

    //VERBATUM
    public void mine(Task task) {
        this.myTask = task;
        task.setThreadId();
        Random random = new Random();
        byte[] hash;
        while (true) {
            task.setTimestamp();
            task.setNonce(random.nextInt());
            hash = sha256.digest(task.toBytes());
            if (leadingZeros(hash) >= LEADING_ZEROS) {
                break;
            }
        }
        reportFinished(hash);
    }

    private synchronized void reportFinished(byte[] hash){
        this.tasksCompleted.getAndIncrement();
        //actual output
        System.out.println(this.myTask.toString());
        
        this.myTask = null;
    }






    
}
