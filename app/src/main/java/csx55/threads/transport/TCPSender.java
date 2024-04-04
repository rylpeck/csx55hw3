package csx55.threads.transport;

import java.io.*;
import java.net.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//sends a message, is per socket, is not threaded

public class TCPSender{
    //our socket
    private Socket socket;
    //stream of stuff ot send
    public DataOutputStream dout;

    private int AckType = 0;
    private int AckLen = 0; 

    private int type = 0;
    private int len = 0;

    private final Lock lock = new ReentrantLock();
    private final Condition responseCondition = lock.newCondition();

    private boolean responseLock = false;
    private int status = 0; 

    public TCPSender(Socket socket) throws IOException {
        this.socket = socket;
        this.dout = new DataOutputStream(socket.getOutputStream());
    }

    public synchronized void sendMessage(byte[] dataToSend, int type) throws IOException {
        //System.out.println("Sending message: " + dataToSend);
        
        int dataLength = dataToSend.length;
        this.responseLock = false;
        this.status = 1;
        this.type = type;
        this.len = dataLength;
        dout.writeInt(dataLength);
        dout.write(dataToSend, 0, this.len);
        dout.flush();
        //System.out.println("Printed length sender " + this.len);
        //lock.lock();
        //try {
            //while (!responseLock) {
            //    responseCondition.await();
            //}
       // } catch (InterruptedException e) {
        //    e.printStackTrace();
        //} finally {
        //    lock.unlock();
        //}
        //if 0 resend?
        //if (this.status == 0){
            //System.out.println("Resend");
            //dout.flush();
           //sendMessage(dataToSend, type);

        //}
        
       // System.out.println("WAs fine");
        //dout.flush();
        return;
    }

    public void sendAck(byte[] dataToSend, int type) throws IOException {
        int dataLength = dataToSend.length;
        
        dout.writeInt(dataLength);
        dout.write(dataToSend, 0, dataLength);
        dout.flush();
    }


    public int checkACK(int Atype, int Alength){

        //System.err.println(Atype + " " + Alength);
       // System.out.println(this.type + " " + this.len);
        if (this.type == Atype && this.len == Alength) {
            lock.lock();
            try {
                responseLock = true;
                responseCondition.signal();
            } finally {
                lock.unlock();
            }

            this.status = 1;
           // System.out.println("ACK accepted");
        } else {
            this.status = 0;
            //System.out.println("Ack not accepted");
        }

        return 0;
    }

}