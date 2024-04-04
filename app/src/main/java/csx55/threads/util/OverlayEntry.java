package csx55.threads.util;

import java.util.ArrayList;

public class OverlayEntry {

    //helper object for overlay
    //this is a list of nodes, to help us keep track of all connections

    public  String name;
    public  String ip;
    public  int port;
    public  int weight;

    public OverlayEntry(String name, String ip, int port){
        this.name = name;
        this.ip = ip;
        this.port = port;
    }
    
}
