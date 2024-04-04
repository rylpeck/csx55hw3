package csx55.threads.util;

import java.net.Socket;

import csx55.threads.transport.TCPReceiver;
import csx55.threads.transport.TCPSender;


//small helper class of all the data we need to keep track of in methods
public class connectionData {

    //connection data is all about the connection, this isnt mine, its theirs

    //this is their info
    private Socket theirSocket;
    //this is my TCP sender talks to them
    private TCPSender tcp;
    //this is how I are getting messages from them;
    private TCPReceiver tcr;
    //this is their ip
    private String ip;
    //this is their port
    private int port;
    private String name;
    private Socket reciever;


    //HashMap <Integer, connectionData> connections = new HashMap<Integer, connectionData>();
    //setters, getters
    public connectionData(Socket b, TCPSender tcp, String ip, int p){
        this.theirSocket = b;
        this.tcp = tcp;
        this.ip = ip;
        this.port = p;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
       this.name = name;
    }


    public void setIP(String ip){
        this.ip = ip;
    }
    public String getIP(){
        return this.ip;
    }

    public void setPort(int p){
        this.port = p;
    }
    
    public int getPort(){
        return this.port;
    }
    public void setSender(TCPSender sender){
        this.tcp = sender;
    }

    //reciever stuff
    public void setReciever(TCPReceiver reciever){
        this.tcr = reciever;
    }
    public Socket getReciever(){
        return this.reciever;
    }
    //set socket
    public void setSocket(Socket sock){
        this.theirSocket = sock;
    }

    public Socket getSocket(){
        return this.theirSocket;
    }

    public TCPSender getTcpSender(){
        return this.tcp;
    }
}
