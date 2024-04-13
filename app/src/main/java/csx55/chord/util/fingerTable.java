package csx55.chord.util;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.ArrayList;

import csx55.chord.Node.peerNode;

public class fingerTable {


    //our finger table to track things. Is stored in every working peer, and can be modified by whomstever

    private HashMap<Integer, chord> fingerTable;
    private peerNode parent;


    //for ours we will always have 32 entries in our space, 32 bit id baby and such...

    public fingerTable(peerNode par){
        this.fingerTable = new HashMap<Integer, chord>();    
        this.parent = par;   
    }

    public void printTable(){
        int note = 1;
        for (HashMap.Entry<Integer,chord> entry : this.fingerTable.entrySet()){

            DecimalFormat df = new DecimalFormat("#");
            df.setMaximumFractionDigits(8);
           
            System.out.println("Entry: " + df.format(entry.getValue().getPosition()) +" " + entry.getKey() + " " + df.format(entry.getValue().getHash()));
            note++;
        }


    }

    //default is called when we know we are hte only node in the system.

    public void defaultSetup(double myHash, String myIp){
        int note = 1;
        for (Integer i = 1; i < 33; i++){
            chord tempChord = new chord(myHash, this.parent.giveName());

            double temp = this.parent.giveHash()+(Math.pow(2, note-1));
            //System.out.println((Math.pow(2, note-1)));
            //System.out.println(temp);
            System.out.println(Math.pow(2, note-1));
            //formating issues:
            DecimalFormat df = new DecimalFormat("#");
            df.setMaximumFractionDigits(8);
            tempChord.setPosition(temp);

            this.fingerTable.put(i, tempChord);
            note++;
        }
        
        this.parent.myForwardCon = null;
        this.parent.myBackwardCon = null;

        this.parent.forwardHash = myHash;
        this.parent.backHash = myHash;


        printTable();
    }

    public void generateOnlyTable(double myHash, String myIp){
        int note = 1;
        for (Integer i = 1; i < 33; i++){
            chord tempChord = new chord(myHash, this.parent.giveName());

            double temp = this.parent.giveHash()+(Math.pow(2, note-1));
            //System.out.println((Math.pow(2, note-1)));
            //System.out.println(temp);
            System.out.println(Math.pow(2, note-1));
            //formating issues:
            DecimalFormat df = new DecimalFormat("#");
            df.setMaximumFractionDigits(8);
            tempChord.setPosition(temp);

            this.fingerTable.put(i, tempChord);
            note++;
        }
        System.out.println("PRINTING");
        printTable();

    }

    
    public int findHighestLowest(double n) {
        int note = 1;
        int highestLowest = 1;
        for (HashMap.Entry<Integer,chord> entry : this.fingerTable.entrySet()){
            
            //System.out.println((Math.pow(2, note-1)));
            //System.out.println(temp);
            double temp = entry.getValue().getPosition();
            if (temp > 0){
                if(temp < n){
                    break;
                }
            }
            highestLowest = note;
            //formating issues:
            DecimalFormat df = new DecimalFormat("#");
            df.setMaximumFractionDigits(8);
            
            System.out.println("Entry: " + df.format(temp) +" " + df.format(entry.getKey()) + " " + " " + df.format(entry.getValue().getHash()));
            note++;
        }
        return highestLowest;
    }

  

    //we see a node in the system already, lets take their information!
    public String determineSpot(Double findHash){
        double currentSpot = this.parent.giveHash();
    //methodbefore this will shear the data to what we want. 
        int lowestHighest = findHighestLowest(findHash);
        System.out.println("Power is gonna be: " + lowestHighest);
        chord thisOne = this.fingerTable.get(lowestHighest);
        
        System.out.println("Which is: " + thisOne.getAddress() + " and " + thisOne.getHash());
        if (thisOne.getHash() == (this.parent.giveHash())){
            System.out.println("That node is me!");
            if (this.parent.forwardHash == this.parent.giveHash()){
                System.out.println("Oh im the next one, its a loop,  probably default setup");
                return "second";
            }
            else if(this.parent.forwardHash > findHash){
                //we are the last one then, so it fits between us and next, we give it next, and tell it
                //that its forward is that then. 

                return "forward";
            }
            //should not reach teh below but im just gonna keep it to be safe


            return null;
        }
        else{
            System.out.println("Giving you information, closest I can get you");
            return thisOne.getAddress();
        }
       
    }

    public String giveFingerTable() {
        StringBuilder stringBuilder = new StringBuilder();
    
        for (HashMap.Entry<Integer, chord> entry : this.fingerTable.entrySet()) {
            
            //i have today learned, stringbuilder is faster, lets use that?

            stringBuilder.append(entry.getValue().getPosition())
                         .append(" ")
                         .append(entry.getValue().getAddress())
                         .append(" ");
        }
        return stringBuilder.toString();
    }

    public void selfCheckFinger(){
        System.out.println("self upate");
        System.out.println("my hash: " + this.parent.giveHash());
        System.out.println("forward/both hash " + this.parent.forwardHash);
        System.out.println(" you sure? " + this.parent.myForwardCon);
        System.out.println("this connection : " + this.parent.myForwardCon.getName());
        for (HashMap.Entry<Integer,chord> entry : this.fingerTable.entrySet()){
            if (this.parent.backHash == this.parent.forwardHash){
                //System.out.println("Loop detected, are we behind or forward?");
                if (this.parent.forwardHash > this.parent.giveHash()){
                    //everything not inbetween us is going to be ours then
                    if (entry.getValue().getPosition() > this.parent.forwardHash){
                        //ours
                        chord tempChord = entry.getValue();
                        tempChord.setHash(this.parent.giveHash());
                        System.out.println("this hash : " + this.parent.giveHash());
                        this.fingerTable.put(entry.getKey(), tempChord);

                    }
                    else{

                        chord tempChord = entry.getValue();
                        tempChord.setHash(this.parent.forwardHash);
                        tempChord.setAddresss(this.parent.myForwardCon.getName());
                        System.out.println("this hash : " + this.parent.giveHash());
                        this.fingerTable.put(entry.getKey(), tempChord);
                        //theirs
                    }
                }
                if (this.parent.forwardHash < this.parent.giveHash()){
                    if (entry.getValue().getPosition() > this.parent.forwardHash){
                        //theirs
                        chord tempChord = entry.getValue();
                        tempChord.setHash(this.parent.forwardHash);
                        tempChord.setAddresss(this.parent.myForwardCon.getName());
                        System.out.println("this hash : " + this.parent.giveHash());
                        this.fingerTable.put(entry.getKey(), tempChord);

                    }
                    else{
                        chord tempChord = entry.getValue();
                        tempChord.setHash(this.parent.giveHash());
                        System.out.println("this hash : " + this.parent.giveHash());
                        this.fingerTable.put(entry.getKey(), tempChord);
                        //ours
                    }
                }

            }

        }
        printTable();

    }


    public void updateFingerTable(ArrayList<chord> tempFingerTable){
        System.out.println("UPDATE");
        DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(8);
        HashMap<String, String> callTable = new HashMap<String, String>();

        int note = 1;
        for (HashMap.Entry<Integer,chord> entry : this.fingerTable.entrySet()){

            System.out.println("Entry: " + entry.getValue().getPosition() +" " + entry.getKey() + " " + " " + entry.getValue().getHash());

            //find the entry.getValue.getPosition
            double last = 0;
            String spot = "";
            System.out.println("Forward finger: " + df.format(this.parent.forwardHash));
            System.out.println(entry.getValue().getPosition());
            //loop scenario first
            if (this.parent.backHash == this.parent.forwardHash){
                System.out.println("Loop detected, are we behind or forward?");
                if (this.parent.forwardHash > this.parent.giveHash()){
                    //everything not inbetween us is going to be ours then
                    if (entry.getValue().getPosition() > this.parent.forwardHash){
                        //ours
                        chord tempChord = entry.getValue();
                        tempChord.setHash(this.parent.giveHash());
                        System.out.println("this hash : " + this.parent.giveHash());
                        this.fingerTable.put(entry.getKey(), tempChord);
                        spot = "myself";

                    }
                    else{

                        chord tempChord = entry.getValue();
                        tempChord.setHash(this.parent.forwardHash);
                        System.out.println("this hash : " + this.parent.giveHash());
                        this.fingerTable.put(entry.getKey(), tempChord);
                        spot = "theirs";

                        //theirs
                    }
                }
                if (this.parent.forwardHash < this.parent.giveHash()){
                    if (entry.getValue().getPosition() > this.parent.forwardHash){
                        //theirs
                        chord tempChord = entry.getValue();
                        tempChord.setHash(this.parent.forwardHash);
                        System.out.println("this hash : " + this.parent.giveHash());
                        this.fingerTable.put(entry.getKey(), tempChord);

                    }
                    else{
                        chord tempChord = entry.getValue();
                        tempChord.setHash(this.parent.giveHash());
                        System.out.println("this hash : " + this.parent.giveHash());
                        this.fingerTable.put(entry.getKey(), tempChord);
                        //ours
                    }
                }
            }
            else if (entry.getValue().getPosition() < this.parent.forwardHash && entry.getValue().getPosition() > this.parent.giveHash()){
                //this is our forward hashes
                chord tempChord = entry.getValue();
                tempChord.setHash(this.parent.forwardHash);
                System.out.println("this hash : " + this.parent.giveHash());
                
                this.fingerTable.put(entry.getKey(), tempChord);
                spot = "myself";

            }
            else if (this.parent.backHash > this.parent.giveHash()){
                double tempmyHash = 2147483647 * 2;
                if (entry.getValue().getPosition() < tempmyHash && entry.getValue().getPosition() > this.parent.backHash){
                    //this is ours now too!
                    System.out.println("thjis is ours now!");
                    chord tempChord = entry.getValue();
                    tempChord.setHash(this.parent.forwardHash);
                    System.out.println("this hash : " + this.parent.giveHash());
                    
                    this.fingerTable.put(entry.getKey(), tempChord);
                    spot = "myself";

                }
            }
            else if (entry.getValue().getPosition() > this.parent.forwardHash){
                System.out.println("last ditch;");
                double spotHash = 0;
                System.out.println("Looking for: " + entry.getValue().getPosition());
                
                for (chord c : tempFingerTable){
                    last = c.getPosition();
                    //System.out.println(c.getPosition());
                    if (c.getPosition() > entry.getValue().getPosition()){
                        System.out.println("BREAK ON: " + entry.getValue().getAddress());
                        spot = c.getAddress();
                        spotHash = c.getHash();
                        break;
                    }
                //if end assume largest
                }

                if (spot == ""){
                    //we hit last, default to last possible entry
                    spot = tempFingerTable.get(31).getAddress();
                    spotHash = tempFingerTable.get(31).getHash();
                }

                if (callTable.containsKey(spot)){
                    String temp = callTable.get(spot);
                    temp = temp +" " + entry.getKey() + " " + entry.getValue().getPosition();
                    callTable.put(spot, temp);
                }
                else{
                    callTable.put(spot, entry.getKey() + " " +String.valueOf(entry.getValue().getPosition()));
                    //new entry here
                }
                //populate our table, we hold it until we hear not to
                chord tempChord = entry.getValue();
                tempChord.setHash(spotHash);

                this.fingerTable.put(entry.getKey(), tempChord);
            }

            System.out.println(entry.getValue().getPosition());
            System.out.println("I will contact: " + spot + " about " + df.format(entry.getKey()) + " with value of " + df.format(entry.getValue().getHash()));
           
            note++;
        }

        for (HashMap.Entry<String,String> entry : callTable.entrySet()){

            //double temp = this.parent.giveHash()+(Math.pow(2, note-1));
            //System.out.println((Math.pow(2, note-1)));
            //System.out.println(temp);
           // this.myFingerTable.defaultSetup(myHash, myIP);
            
            //formating issues:
            
            
           System.out.println(entry.getKey() + " " + entry.getValue());
        }


    


        System.out.println("Done");
        printTable();
        askForInfo(callTable);

    }


    public void askForInfo(HashMap<String, String> callTable){
        for (HashMap.Entry<String,String> entry : callTable.entrySet()){
            //we go through it bit by bit, then send it and that validates it.EASY
            entry.getKey(); // this is who we send to
            this.parent.queryForTable(entry.getKey(), entry.getValue());
        }
        
    }

    public void validateInfo(String[][] data){

        int sizer = data[0].length;
        StringBuilder returnString = new StringBuilder();
        HashMap<String, String> newCallTable = new HashMap<String, String>();
        

        for (int i = 0; i < sizer/2; i = i + 2){
            if (data[0][i+1].equals("valid")){
                //nothing
            }
            else if (newCallTable.containsKey(data[0][i+1])){
                String temp = newCallTable.get(data[0][i+1]);
                chord entry = this.fingerTable.get(i);
                temp = temp +" " + i + " " + entry.getPosition();
                newCallTable.put(data[0][i+1], temp);
            }
            else{
                chord entry = this.fingerTable.get(i);
                newCallTable.put(data[0][i+1], i + " " +String.valueOf(entry.getPosition()));
                //new entry here
            }


        }

    }

    


    
}
