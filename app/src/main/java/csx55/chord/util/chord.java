package csx55.chord.util;

public class chord {


    private int Hash;
    private String address;

    public chord(int hash, String address){
        this.Hash = hash;
        this.address = address;
    }

    public int getHash(){
        return this.Hash;
    }

    public String getAddress(){
        return this.address;
    }


    
}
