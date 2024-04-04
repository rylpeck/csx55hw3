package csx55.threads.Node;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import csx55.threads.wireformat.*;
import csx55.threads.transport.*;
import csx55.threads.util.*;
import csx55.threads.datahandler.*;
import csx55.threads.hashing.MinerManager;
import csx55.threads.hashing.MinerThread;
import java.util.concurrent.LinkedBlockingDeque;
import csx55.threads.hashing.RoundManager;
import csx55.threads.hashing.Task;


//i know theres one above this called computenoderunner is compute node, its for naming simplicity.
public class ComputeNodeRunner extends Node{

    //class specific nodes
    private connectionData register;
    protected String regHostName = null;
    protected Integer regPortNumber = null;

    ExecutorService threadPool = null;

    LinkedBlockingQueue<String> threadQueue = new LinkedBlockingQueue<String>();

    public ConcurrentLinkedQueue<Task> taskQueue = new ConcurrentLinkedQueue<Task>();

    public connectionData myConnection = null;
    public connectionData myIncomingCon = null;

    public MinerManager myManager = null;
    private Thread myManagerThread = null;

    HashMap<String, minerObject> workerMap = new HashMap<String, minerObject>();
    public LoadManager LoadManager = null;

    protected AtomicInteger tasksCompleted = new AtomicInteger(0);
    protected AtomicInteger tasksCreated = new AtomicInteger(0);

    protected AtomicInteger tasksSent = new AtomicInteger(0);
    protected AtomicInteger tasksRecieved = new AtomicInteger(0);

    private int tasksThisround = 0;


    public RoundManager myRoundManager = null;
    private Thread myRoundManagerThread = null;

    protected AtomicInteger nodesDone;
    
    //public RoundManager myRounds = null;
    Thread myRoundThread = null;

    private volatile String myName;

    //internal system to keept rack of held tasks, so we dont cycle them around. 
    protected AtomicInteger tasksHeldMigrated = new AtomicInteger(0);

    public synchronized void setMyName(String given){
        this.myName = given;
    }

    public synchronized String getMyName(){
        return this.myName;
    }

    
    public int avg = 0;

    public ComputeNodeRunner(String ip, int port, String reghost, int regpot){
        this.myIP = ip;
        this.myPort = port;
        

        setMyName(ip + ":" + port);
        //System.out.println("I am : " + getMyName());
        
        this.NodesList = new HashMap<String, connectionData>();  
        DataHandlerNode handle = new DataHandlerNode(dataQueue, this, this.messagesRecievedCount, this.messagesRelayed);
        this.hndl = new Thread(handle);
        this.datahandler = handle;
        //System.out.println("My socket is: " + this.mySocket);

        this.LoadManager = new LoadManager(taskQueueTemp, this);
        this.regHostName = reghost;
        this.regPortNumber = regpot;

        this.myRoundManager = new RoundManager(this.tasksCreated, this.taskQueue, this.taskQueueTemp,this.myIP, this);
        this.myRoundManagerThread = new Thread(this.myRoundManager);


        setup();
        setupThreads();
        registerConnection();
        register();

    }


    public void deregisterRespond(String[][] data, Socket incomingSock){
        synchronized (registerLock) {
            
            //System.out.println("Data" + data[0][0]);

            if (Integer.parseInt(data[0][0])== 0){
                //Success recieved, we can now remove the registry
                this.register = null;
                //todo clean other registers and such out   
                //System.out.println("Successful delist, closing");    
                System.exit(0);
            }
            else {
                //its a failure
                //System.out.println("Error, register refusal to delist");
            }
            registerLock.notify();
        }

    }

    //setup inhereted


    public synchronized void run() {            
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

    private synchronized connectionData registerConnection(){
        synchronized (registerLock) {
            //System.out.println("Register");
            if (this.register != null){

                registerLock.notify();
                return this.register;            
            }
            else{
                Socket clientSock = null;
                TCPSender TCPSender = null;
                try{
                    clientSock = new Socket(this.regHostName, this.regPortNumber);
                    TCPSender = new TCPSender(clientSock);
                    connectionData connectionData = new connectionData(clientSock, TCPSender, this.regHostName, this.regPortNumber);
                    this.register = connectionData;
                    TCPReceiver temp2 = tcpRec.nodeListen(clientSock, myName, connectionData);
                    connectionData.setReciever(temp2);
                    registerLock.notify();
                } catch (ConnectException e){
                    return null;
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return this.register; 
                //sendMessage(commandBroken, mess.connections.get(Integer.parseInt(commandBroken[1])));
            }
        //registerLock.notify();
        }
      
    }



    private synchronized void register(){
        //System.out.println("Registration Request");
        //this one could use the registry lock, however it does not access the variable, just starts the process.
        Event Registration = EventFactory.createEvent(REGISTRATION_REQUEST); 
        TCPSender sender = this.register.getTcpSender();
        

        try{
            InetAddress myAddr = InetAddress.getLocalHost();
            this.myIP = myAddr.toString();
            //System.out.println("True address" + myAddr);
        }
        catch (Exception e){
            //System.out.println("Are you sure youre connected to the internet");
        }
        
        //this.myIP = this.mess.register.getSocket().getInetAddress().toString();
        //interesting thing, it tries to tag on localhost sometimes, for ease we check nad  make it 127
        if (this.myIP.contains("/")){
            String [] temp = this.myIP.split("/");
            this.myIP = temp[1];
        }
       
        String arguments = (this.myIP + " " + this.myPort);
        //System.out.println("ARguments: " + arguments);
        Registration.setData(arguments);
        
        try {
            sender.sendMessage(Registration.getBytes(), REGISTRATION_REQUEST);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void registerRespond(String[][]data, Socket incomingSock){
        //System.out.println("RegRespond");

        synchronized (registerLock) {
        
            //We got a response, change our name, and rejoice we now have a connection to father register
            //System.out.println("Responding");
            //this.register.setReciever(incomingSock);
            //System.out.print("Data" + data[0][0]);
            if (Integer.parseInt(data[0][0]) == 1){
                //We were already registered :D
            }
            else{
                //this.name = data[0][1];
                //System.out.println("We are now named: "+ this.name);
                this.datahandler.myNodesName = this.myIP + ":" + this.myPort;
            }
            registerLock.notify();
        }

    }




    public void makeMessengerCon(String[][] data, connectionData con){
        System.out.println("Attempting connection");
        if (this.myIncomingCon != null){
            //System.out.println("Connection Valid");
        }
        else{
            try {
                //this.tcpRec.nodeListen(sock, data[0][0]);

                String[]temp = data[0][0].split(":");
                //con.setIP(temp[0]);
                //con.setPort(Integer.valueOf(temp[1]));
                this.myIncomingCon = con;
                nodeSend(con.getTcpSender());
                //this.NodesList.put(tempkey, newNode);

            } catch (NumberFormatException e) {
                
                e.printStackTrace();
            }
        }

    }

    public void trafficReport(){
        Event summation = EventFactory.createEvent(TRAFFIC_SUMMARY);
        int TasksCompleted = this.tasksCompleted.get();
        int TasksCreated = this.tasksCreated.get();
        int TasksPulled = this.tasksRecieved.get();
        int tasksPushed = this.tasksSent.get();
        
        String tempkey = this.myIP + ":" + this.myPort;
        String data = tempkey + " " + TasksCreated + " " + TasksPulled + " " + tasksPushed + " " + tasksCompleted;
        summation.setData(data);
        TCPSender sender = this.register.getTcpSender();
        try {
            sender.sendMessage(summation.getBytes(), TRAFFIC_SUMMARY);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       //summation.setData(myIP);

    }
    
    
    public void passLeft(byte[] bytes, int type){
        TCPSender tempSender = this.myConnection.getTcpSender();
        
        try {
            tempSender.sendMessage(bytes, type);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //shareMyLoad();
    } 



    //this.taskqueue
    

    private void setupThreadPool(int size){
         //fix issue with more threads
        System.out.println("SEtting up threadpool");
        for (int i =0; i < size; i++){
            
            String tempName = "miner" + i;
            MinerThread tempMiner = new MinerThread(tempName, this.tasksCompleted, this.taskQueue);
            Thread tempThread = new Thread(tempMiner);
            tempThread.setName(tempName);
            tempThread.start();
            minerObject saveMe = new minerObject(tempThread, tempMiner);
            this.workerMap.put(tempName, saveMe);
        }

    }
    private Boolean spoken = false;

    public void speakFinished(){
        //System.out.println("WE ARE DONE");
        if (this.spoken == false){
            this.spoken = true;
            sendFinish();
        }
       

    }

    private synchronized void setNodesDone(int value){
        this.nodesDone = new AtomicInteger(value);
    }

    private int NumNodes = 0;

    public synchronized void setupConnections(String[][]data){
        this.myName = this.myIP +":" + this.myPort;
        this.LoadManager.myName = this.myName;
        messageMyConnections(data);
        this.LoadManager.numberofNodes = Integer.valueOf(data[3][0]) - 1;
        setupThreadPool(Integer.valueOf(data[2][0]));
        setNodesDone(Integer.valueOf(data[3][0]) - 1);
        this.NumNodes = (Integer.valueOf(data[3][0]) - 1);
        this.LoadManager.numberofNodes = (Integer.valueOf(data[3][0]) - 1);
    }

    public int mining = 0;

    private ConcurrentLinkedQueue<Task> taskQueueTemp = new ConcurrentLinkedQueue<Task>();

    int currentRound = 0;
    int maxRounds = 0;


    public void startRounds(String[][] data){

        this.currentRound = 0;
        this.maxRounds = Integer.valueOf(data[0][0]);
        runRound();

    }

    public void runRound(){

        if (this.currentRound == this.maxRounds){
            speakFinished();
            return;
        }

        currentRound++;
        this.readyCount = 0;
        //this.myRoundManager.setRound(Integer.valueOf(data[0][0]));
        //this.myRoundManagerThread.start();

        System.out.println("Good luck");

        System.out.println("UWU");

        boolean simpleToggle = true;

        Random rand = new Random();
        
        System.out.println("Starting rounds");
        System.out.println("My con is: " + this.myConnection.getPort());
        
    
        this.taskQueueTemp.clear();
        this.mining = 0;
        simpleToggle = true;
        //ROUND NUMBER CHANGE THIS ANGRY NOISES
        int randomNumber = rand.nextInt((1000 - 1) + 1) + 1;
        //make equivelent nodes
        for (int i =0; i < randomNumber; i++){
            Task tempTask2 = new Task(this.myIP, this.myPort, this.currentRound, new Random().nextInt());
            this.taskQueueTemp.add(tempTask2);
            
            this.tasksCreated.getAndIncrement();
        }
        System.out.println("Finished that looper.");
        sendBalance();
        System.out.println("Sitting here");

        //this.myRounds.setRound(Integer.valueOf(data[0][0]));
        //this.myManagerThread.start();
        //this.myRoundThread.start();
    
    }


    public void sendBalance(){

        System.out.println("Sending Balance");
        Event myLoad = EventFactory.createEvent(NODE_LOAD_REPORT);
        String message = this.getMyName() + " " + this.taskQueueTemp.size();
        myLoad.setData(message);
        TCPSender tempSender = this.myConnection.getTcpSender();

        try {
            tempSender.sendMessage(myLoad.getBytes(), NODE_LOAD_REPORT);
        } catch (IOException e) {
           // System.exit(45);
            e.printStackTrace();
        }
        
    }

    

    public void addFinished(String[][]data, String named){
        System.out.println("Speaking Finished");
  
        //System.out.println(data[0][0]);
        //System.out.println(named);
        System.out.println(this.getMyName());

        if (data[0][0].equals(this.myName)){

            //killed
            //System.out.println("killed");
            //System.out.println("This message is killed");
            //System.out.println("Next was: " + name);
            //this.LoadManager.listLoads();
            //System.exit(97070);
            //System.out.println("Passed");
           
        }
        else{
            //System.out.println("Hit the Finished");
            this.nodesDone.getAndDecrement();
           //System.out.println("Total: " + this.nodesDone.get());
            if (this.nodesDone.get() == 0){
                this.speakFinished();
            }
            Event myLoad = EventFactory.createEvent(NODE_DONE);
            String message = data[0][0] + " " + data[0][1];
            myLoad.setData(message);
            TCPSender tempSender = this.myConnection.getTcpSender();
            try {
                tempSender.sendMessage(myLoad.getBytes(), NODE_DONE);
            } catch (IOException e) {
            // System.exit(45);
                e.printStackTrace();
            }
        
           
        }
    }

    
    
    public void passAlongPoll(String[][]data, String named){
        //System.out.println("Came from? " + (this.myIncomingCon.getIP() + ":" + this.myIncomingCon.getPort()));
        System.out.println(named);
         //if its us
        if (data[0][0].equals(named)){
            //killed
            System.out.println("killed");
            //this.LoadManager.getMySpare();      
        }
        else{
            String name = data[0][0];
            String namesLoad = data[0][1];
            System.out.println("Adding Load: " + name + " " + namesLoad);
            this.LoadManager.addLoadData(data[0][0], data[0][1]);
            //System.out.println("Passed");
            Event myLoad = EventFactory.createEvent(NODE_LOAD_REPORT);
            TCPSender tempSender = this.myConnection.getTcpSender();
            String message = data[0][0] + " " + data[0][1];
            myLoad.setData(message);
            try {
                tempSender.sendMessage(myLoad.getBytes(), NODE_LOAD_REPORT);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        //System.out.println("Exiting");
    }

    public synchronized int checkFinished(){

        if (this.tasksCreated.get() + this.tasksRecieved.get() - this.tasksSent.get() == this.tasksCompleted.get()){
            System.out.println("YES");
           // System.out.println(this.tasksCreated.get());
           // System.out.println(this.tasksSent.get());
           // System.out.println(this.tasksRecieved.get());
           // System.out.println(this.tasksCompleted.get());
            return 0;
        }
        //System.out.println("NO");
        return 1;
    }

    public void passMessage(byte[] bytes, int type){
        TCPSender tempSender = this.myConnection.getTcpSender();
        try {
            tempSender.sendMessage(bytes, type);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //shareMyLoad();
    }

    public void sendTask(int WS, String dest){
        String message = dest + " " + WS;

        this.tasksSent.getAndAdd(WS);

        if (WS == 0){
            message = message + "0";

        }
        else{
            for (int i = 0; i < WS; i++){
                Task temp = this.taskQueueTemp.poll();
                // System.out.println("Removing: " + i);
                message = message + " " + temp.getIp() + " " + temp.getPort() + " " + temp.getRoundNumber() + " " + temp.getPayload();
            }
        }
        
       //System.out.println(message);
        Event sendMyTasks = EventFactory.createEvent(TASK_SEND);
        sendMyTasks.setData(message);
        byte [] sendMe = sendMyTasks.getBytes();
        passLeft(sendMe, TASK_SEND);

        // System.out.println("Just sent " + WS + " loads");
        //System.out.println("Loads shifted : " + this.tasksSent.get());


    }

    public void handleIncomingTask (String[][]data){
       // System.out.println("I am: " + myName);
        System.out.println("Handlin");
        System.out.println(data[0][0]);
        System.out.println(data[0][1]);
        System.out.println(this.myName);
        if (data[0][0].equals(this.myName)){

            //killed
            System.out.println("this was mine");
            //probably an odd number
            for (int i = 0; i < data[1].length; i = i+4) {
                System.out.println("Insert " + i);
                Task newTask = new Task(data[1][i], Integer.valueOf(data[1][i+1]), Integer.valueOf(data[1][i+2]), Integer.valueOf(data[1][i+3]));
                System.out.println("My load is: " + this.taskQueueTemp.size());
                this.tasksRecieved.getAndIncrement();
                this.tasksHeldMigrated.getAndIncrement();
                //newTask.setMigration();
                this.taskQueueTemp.add(newTask);
            }

            
            System.out.println("My load is: " + this.taskQueueTemp.size());
            //System.out.println("RUN IT");
            
        }

        else{
            int canTake = avg - this.taskQueueTemp.size();
            
            System.out.println("I can take: " + canTake);
            if (canTake <= 0){
                canTake = 0;
            }

            else{
                if (canTake > Integer.valueOf(data[0][1])){
                    //cant take ita ll, but we will eat it 
                    canTake = Integer.valueOf(data[0][1]);
                }
                System.out.println("I will take: " + canTake);
                //add however many tasks migrated, so we dont transfer those!
               
                for (int i = 0; i < (canTake*4); i = i+4) {
                    System.out.println("Insert " + i);
                    Task newTask = new Task(data[1][i], Integer.valueOf(data[1][i+1]), Integer.valueOf(data[1][i+2]), Integer.valueOf(data[1][i+3]));
                    System.out.println("My load is: " + this.taskQueueTemp.size());
                    this.tasksRecieved.getAndIncrement();
                    this.tasksHeldMigrated.getAndIncrement();
                    //newTask.setMigration();
                    this.taskQueueTemp.add(newTask);
                }
    

            }
            //round up reminader
            int remainder = Integer.valueOf(data[0][1]) - canTake;
            String tempMessage = data[0][0] + " " + remainder;

            for (int i = (canTake*4); i < data[1].length; i = i+4){
                tempMessage = tempMessage + " " + data[1][i] + " " + data[1][i+1] + " " + data[1][i+2] + " " + data[1][i+3];
            }

            Event sendMyTasks = EventFactory.createEvent(TASK_SEND);
            sendMyTasks.setData(tempMessage);
            TCPSender tempSender = this.myConnection.getTcpSender();
            sendMyTasks.setData(tempMessage);
            try {
                tempSender.sendMessage(sendMyTasks.getBytes(), TASK_SEND);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            System.out.println("My load is: " + this.taskQueueTemp.size());         
        }
        this.LoadManager.addPassData(data[0][0], data[0][1]);

        if (this.LoadManager.checkMap2() == 1){
            //sendReady();

        }


        //System.out.println("My load is: " + this.taskQueueTemp.size());
        //System.out.println("inserted");
    }

    public void go(){

        boolean toggleDone = true;
        int roundRunner = avg;
        System.out.println("I have: " + this.taskQueueTemp.size());
        int TotalSize = this.taskQueueTemp.size() + 0;
        
        for (int y = 0; y < TotalSize; y++){
            this.taskQueue.add(this.taskQueueTemp.poll());
        }
        this.readyCount = 0;
        //boolean toggleDone = true;
        while (toggleDone) {
            //System.out.println("Sizee");
            //System.out.println(this.tasksCompleted.get());
            //System.out.println("Expected : " + roundRunner);
            if (this.taskQueue.size () == 0){
                if (this.tasksCreated.get() + this.tasksRecieved.get() - this.tasksSent.get() == this.tasksCompleted.get()){
                    toggleDone = false;
                }
            }
            //sleep to make sure we are done
            

        }
        runRound();

    }


    int readyCount = 0;

    public void RecievedReady(String[][]data){

        System.out.println(this.getMyName());
        
        if (data[0][0].equals(this.getMyName())){

        }
        else{
            System.out.println("Ready");
            readyCount++;
            Event myLoad = EventFactory.createEvent(READY_WORK);
            String message = data[0][0] + " " + data[0][1];
            myLoad.setData(message);
            TCPSender tempSender = this.myConnection.getTcpSender();
            try {
                tempSender.sendMessage(myLoad.getBytes(), READY_WORK);
            } catch (IOException e) {
            // System.exit(45);
                e.printStackTrace();
            }

            System.out.println(this.NumNodes);

            if (readyCount == this.NumNodes ){
                go();

            }
        }
            
    }

    public void sendReady(){
        Event myLoad = EventFactory.createEvent(READY_WORK);
        String message = this.myName + " " + 1;
        myLoad.setData(message);
        TCPSender tempSender = this.myConnection.getTcpSender();
        try {
            tempSender.sendMessage(myLoad.getBytes(), READY_WORK);
        } catch (IOException e) {
        // System.exit(45);
            e.printStackTrace();
        }

    } 




    //might be useful
    public void messageMyConnections(String[][] data){
        //first we clear the messaging list
        //System.out.println("Message freinds");

        //this.NodesList = new HashMap<String, MessengerData>();
        for (int i = 0; i < 1; i++){
            try {
            String [] temp = data[1][i].split(":");
                      

            Socket outgoingSock = new Socket(temp[0], Integer.valueOf(temp[1]));
            TCPSender TCPSender = new TCPSender(outgoingSock);
         
            connectionData connection = new connectionData(outgoingSock, TCPSender, temp[0], Integer.valueOf(temp[1]));

            TCPReceiver temp2 = tcpRec.nodeListen(outgoingSock, myName, connection);
            connection.setReciever(temp2);
            connection.setName(temp[0] + ":" + temp[1]);
            this.myConnection = connection;
            
            //this.NodesList.put(data[1][i], connection);
            nodeSend(TCPSender); //helper method to just say hi


            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }
        //System.out.println("End of connection threading");
    }

    protected void nodeSend(TCPSender sender){
        Event nodetonode = EventFactory.createEvent(ANOTHER_NODE);
        String arguments = (this.myIP + ":" + this.myPort);
        nodetonode.setData(arguments);
        try {
            sender.sendMessage(nodetonode.getBytes(), ANOTHER_NODE);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    


    public synchronized void sendFinish(){
        System.out.println("waiting for sender to finish.....");
    
        //System.out.println("Reporting Status");
        TCPSender sender = this.register.getTcpSender();
        Event finished = EventFactory.createEvent(FINISHED_ROUNDS);
        String arguments = (this.myIP + " " + this.myPort);
        finished.setData(arguments);
        try {
            sender.sendMessage(finished.getBytes(), FINISHED_ROUNDS);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return;
    }


    public void handleClose(Socket socket){
        //System.out.println("Handle close");
        System.exit(2);
        
        synchronized (registerLock) {


                //node side
            if (socket == this.register.getSocket()){
                //System.out.println("Error, lost connection to register, shutting down");
                System.exit(255);

                //This is a register connection closure, we will attempt to reconnect
            }
            else{
                //System.out.println("Lost connection to node, please check overlay setup");
                //System.exit(256);
            }

            registerLock.notify();
        }

    }
 


     
    
    
}
