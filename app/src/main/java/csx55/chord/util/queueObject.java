package csx55.chord.util;

import java.net.ConnectException;
import java.net.Socket;

import csx55.chord.wireformat.*;

//an object used so we dont ahve two queues. I ran into a race situation for data processing.was bad.

public class queueObject {

    private Event event;
    private connectionData con;
    private byte[] bytes;

    public queueObject(Event e, connectionData s){
        this.event = e;
        this.con = s;
    }

    public byte[] getBytes(){ 
        return this.bytes;
    }

    public void setBytes(byte[] byter){
        this.bytes = byter;
    }

    public connectionData getConnectionData(){
        return this.con;
    }

    public Event getEvent(){
        return this.event;
    }
    
}
