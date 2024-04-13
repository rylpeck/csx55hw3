package csx55.chord.util;

public class chord {


    private double Hash;
    private String address;
    private double position;

    public chord(double hash, String address){
        this.Hash = hash;
        this.address = address;
    }

    public void setPosition(double pos){
        this.position = pos;
    }

    public void setHash(double hash){
        this.Hash = hash;
    }   

    //psotion ranging from 1-32
    public double getPosition(){
        return this.position;
    }

    //hash of node
    public double getHash(){
        return this.Hash;
    }

    //actual address
    public String getAddress(){
        return this.address;
    }


    
}
