package csx55.threads.transport;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import csx55.threads.util.*;
import csx55.threads.wireformat.*;


import java.io.*;

//this class is an individual reciever. IT takes in messages, and sends them up to get processed by the queue

public class TCPReceiver implements Runnable {

    //our name for future reference and work
    public String threadName;
    //our socket to monitor
    private Socket socket;
    //a data input stream, of our socket
    private DataInputStream din;
    //the data recieved
    private byte[] data;
    //our handler, our thread parent
    private TCPReceiverThread parent;
    //got data? put it in here
    private LinkedBlockingQueue<queueObject> dataQueue;
    //incrememnt this
    private final AtomicInteger messagesCaught;

    private connectionData con = null;
    //init
    public TCPReceiver(String name, Socket socket, LinkedBlockingQueue<queueObject> dataInput, AtomicInteger messagesCaught, TCPReceiverThread parent) throws IOException {
        this.threadName = name;
        this.messagesCaught = messagesCaught;
        this.socket = socket;
        this.parent = parent;
        this.dataQueue = dataInput;      
    }

    public void setCon(connectionData con){
        this.con = con;
    }


    //run, process anything.
    public void run() {
        int dataLength;
        int messagesRecieved = 0;
        //System.out.println("Initialized TCPRecieverThread");

        try {
            this.din = new DataInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            
        }
        
        while (this.socket != null) {
            
            try {
                dataLength = this.din.readInt();
                //System.err.println("A length you say? " + dataLength);
                data = new byte[dataLength];
                din.readFully(data, 0, dataLength);
           
                //String outputString = new String(data, StandardCharsets.UTF_8);
                //System.out.println("Data was:" + outputString);
                
                //socketQueue.add(socket);
                processor(data, dataLength);//Process data immediatel
                
                messagesCaught.incrementAndGet();
                messagesRecieved++;
                //System.out.println("Outta here");
          
                 
            } catch (SocketException se) {
                //System.out.println("This");
                //Add in error handling to deal with closing suddently, or register dying
               // System.out.println("Closing");
                //System.out.println(se.getMessage());
                //parent.handleClose(this.threadName, socket);
                break;
            } catch (IOException ioe) {
                //System.out.println("IO AGAIN");
                //System.out.println(ioe.getMessage());
                //System.out.println(ioe.toString());
                break;
            }
        }
        parent.handleClose(this.threadName, socket);
        //System.out.println("Socket Closed");
    }
    //another method to deal with any data, strip it out, make it an event, and throw it into the queue.
    private synchronized void processor(byte [] data, int dataLength){
        int tempType = 0;
        long timeStamp = 0;
        Event tempData = null;
        String Temp = null;

              //send the ack
        try{
            ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
            DataInputStream dar = new DataInputStream(new BufferedInputStream(baInputStream));
    
            tempType = dar.readInt();
            timeStamp = dar.readLong();
                //System.out.println("Data was sent at: " + formattedTime);

           // System.out.println("Temp type was: " + tempType);

            
            tempData = EventFactory.createEvent(tempType);

            int length = dar.readInt();
            byte[] messageBytes = new byte[length];
            dar.readFully(messageBytes);            
            Temp = new String(messageBytes);
            if (tempType == 99){
               // System.out.println("ACK");
                //looks like an ack! lets ask our sender..
                String[] split = Temp.split("\\s+");
                con.getTcpSender().checkACK(Integer.valueOf(split[0]), Integer.valueOf(split[1]));
                return;
            }

            //System.out.println("What we got: " + Temp + "|");
            //System.out.println("End of got");

           
           // System.out.println("Added");    
                    
            //baInputStream.close();
            dar.close();
        }
        catch (EOFException e){
            //System.out.println("Temp type was: " + tempType);
            //System.out.println("EOF");

            if (tempType != 99){
                //System.out.println("We make ack");
                int statusAck = -1;
                Event synAck = EventFactory.createEvent(99);
                String quickMessage = 0 + " " + 0;
                //System.out.println(quickMessage);
                synAck.setData(quickMessage);
                try {
                    sendAck(synAck.getBytes(), 99);
                } catch (IOException f) {
                    // TODO Auto-generated catch block
                    f.printStackTrace();
                }
                if(statusAck == 0){
                    //this was invalid we await the redo, return
                    //System.out.println("Requesting new: ");
                    System.exit(3);
                    return;
                }

            }
           
            //System.err.println(e.toString());
            e.printStackTrace();
            System.exit(333);
            //System.out.println("DAta was: " + data.toString());
        }
        catch (IllegalArgumentException e){
           // System.out.println("What?");
            System.exit(3343434);
        }
        catch (IOException e){
            System.exit(3343434);
            //System.out.println("Derp");
        }

        if (tempType != 99){
            //System.out.println("We make ack");
            int statusAck = -1;
            Event synAck = EventFactory.createEvent(99);
            String quickMessage = tempType + " " + dataLength;
            //System.out.println(quickMessage);
            synAck.setData(quickMessage);
            try {
                sendAck(synAck.getBytes(), 99);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if(statusAck == 0){
                //this was invalid we await the redo, return
                System.out.println("Requesting new: ");
                System.exit(3);
                return;
            }

        }

        tempData.setData(Temp);

        queueObject newQueued = new queueObject(tempData, this.con);
          
            //tempType = din.readInt();
            //System.out.println(tempType);
            //System.out.println(tempData.giveData());
        dataQueue.add(newQueued);

        //all went well,synack out
   
    }


    public void sendAck(byte[] dataToSend, int type) throws IOException {
        DataOutputStream dout = new DataOutputStream(this.socket.getOutputStream());
        int dataLength = dataToSend.length;
        //System.out.println("Printed length sender " + dataLength);
        //dout.writeInt(dataLength);
        //dout.write(dataToSend, 0, dataLength);
        //dout.flush();
        //System.out.println("Send it");
    }

    
}
