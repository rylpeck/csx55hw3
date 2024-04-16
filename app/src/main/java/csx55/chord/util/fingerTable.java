package csx55.chord.util;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Random;
import java.util.ArrayList;



import csx55.chord.Node.peerNode;

import csx55.chord.transport.*;
import csx55.chord.util.*;
import csx55.chord.datahandler.*;

import csx55.chord.wireformat.*;


public class fingerTable implements Protocol{


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
        System.out.println("================================");
        System.out.println("I am hash of: " + this.parent.giveHash());
        System.out.println("My ip is: " + this.parent.giveName());
        for (HashMap.Entry<Integer,chord> entry : this.fingerTable.entrySet()){

            DecimalFormat df = new DecimalFormat("#");
            df.setMaximumFractionDigits(8);
           
            System.out.println("Entry: " + df.format(entry.getValue().getPosition()) +" " + entry.getKey() + " " + df.format(entry.getValue().getHash()) + " " + entry.getValue().getAddress());
            note++;
        }
        System.out.println("My forward is : " + this.parent.forwardHash);
        System.out.println("My backhash is  : " + this.parent.backHash);
        System.out.println("================================");


    }

    //default is called when we know we are hte only node in the system.

    public void defaultSetup(double myHash, String myIp){
        int note = 1;
        for (Integer i = 1; i < 6; i++){
            chord tempChord = new chord(myHash, this.parent.giveName());

            double temp = this.parent.giveHash()+(Math.pow(2, note-1));
            //System.out.println((Math.pow(2, note-1)));
            //System.out.println(temp);
            System.out.println(Math.pow(2, note-1));
            //formating issues:
            DecimalFormat df = new DecimalFormat("#");
            df.setMaximumFractionDigits(8);
            tempChord.setPosition(normalize(temp));

            this.fingerTable.put(i, tempChord);
            note++;
        }

        this.parent.myForwardCon = null;
        this.parent.myBackwardCon = null;
        this.parent.forwardHash = myHash;
        this.parent.backHash = myHash;

        printTable();
    }

    public int giveRandomFinger(){
        //private HashMap<Integer, chord> fingerTable;
        Random random = new Random();
        Object[] keys = fingerTable.keySet().toArray();
        if (keys.length == 0){
            System.out.println("not connected yet");
            return 0;
        }
        Integer randomKey = (Integer) keys[random.nextInt(keys.length)];

        // Retrieve the value associated with the random key
        //chord randomValue = fingerTable.get(randomKey);
        return randomKey;

    }

    public void generateOnlyTable(double myHash, String myIp){
        int note = 1;
        for (Integer i = 1; i < 6; i++){
            chord tempChord = new chord(myHash, this.parent.giveName());

            double temp = this.parent.giveHash()+(Math.pow(2, note-1));
            //System.out.println((Math.pow(2, note-1)));
            //System.out.println(temp);
            System.out.println(Math.pow(2, note-1));
            //formating issues:
            
            DecimalFormat df = new DecimalFormat("#");
            df.setMaximumFractionDigits(8);
            tempChord.setPosition(normalize(temp));

            this.fingerTable.put(i, tempChord);
            note++;
        }
        System.out.println("PRINTING");
        printTable();

    }

    public void searchForThisHash(double hash){
        normalize(hash);
        if (backwardRange(hash) == true){
            //we totally do
            String arguments = "0";
            System.out.println("We do");
            //return "0";
        }
        else{
            int gothisspot = searchHighestLowest(hash);
            System.out.println(gothisspot);
            System.out.println("Is probably held under : " + giveEntry(gothisspot).getHash());
            //send it along to there

            Event helpMe = EventFactory.createEvent(REQUESTHASHlOCATION);
            String [] brokenString = giveEntry(gothisspot).getAddress().split(":");

            String arguments = this.parent.giveName() + " " + hash;
            helpMe.setData(arguments);
            connectionData tempCon = this.parent.peerConnection(brokenString[0], Integer.valueOf(brokenString[1]));
            try {
                tempCon.getTcpSender().sendMessage(helpMe.getBytes(), REQUESTHASHlOCATION);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        //return "";
    }




    //method specifically for searching
    public int searchHighestLowest(double n){
        double hashFound = 0;
        int position = 0;
        for (HashMap.Entry<Integer, chord> entry : this.fingerTable.entrySet()) {
            System.out.println(hashFound);
            double temp = entry.getValue().getPosition();
            if (temp > hashFound && temp <= n){
                hashFound = temp;
                position = entry.getKey();
            }
        }
        System.out.println("End of loop2");
        //we need to verify the loop over
        Object[] keys = fingerTable.keySet().toArray();

        if (hashFound == 0){
            //lets see if we wrap around zero here with this hash
            if (n < this.parent.giveHash()){
                //odds are we wrap around, lets be cautious
                searchForThisHash(31);
            }

            System.out.println("Go far");
            //if (giveEntry(keys.length )){

            //}
            //verify our farthest and see if it would work under that
           //if its not then we go to max, and assume they can find it, its past whats in our finger range.
           
            
        }

        return position;


        
    }


    //method to route to another given node, or hte node that hodls it
    //if its not under our hash, we route it along.
    //if its not our hash, we give the next possoble place to look. REturn 0 or next node
    //up to whoever calls it to route it along


    
    public int findHighestLowest(double n){
        //iterate through whole list, dont just break, stupid i know but it helps witht he 0th over
        double hashFound = 0;
        int position = 0;
        for (HashMap.Entry<Integer, chord> entry : this.fingerTable.entrySet()) {
            System.out.println(hashFound);
            double temp = entry.getValue().getPosition();
            if (temp > hashFound && temp <= n){
                hashFound = temp;
                position = entry.getKey();
            }
        }
        System.out.println("End of loop");

        if (hashFound == 0){
            System.out.println("Override, creep forward");
            if (hashFound < this.parent.backHash && n > 0){
                return -1;
            }
            if (hashFound < this.parent.backHash){
                //might have overshot, lets backtrack it a little bit
                return findHighestLowest(31);
            }
            
        //end of traditional range, do we go over zero?
            if (n < this.parent.giveHash()){
                //its probably over the zeroth range
                return 1;
            }
            
        }


        return position;
    }

    
    public int findHighestLowestOLD(double n) {
        int note = 1;
        int highestLowest = 1;

        DecimalFormat df = new DecimalFormat("#");
            df.setMaximumFractionDigits(8);

        for (HashMap.Entry<Integer, chord> entry : this.fingerTable.entrySet()) {
            double temp = entry.getValue().getPosition();
            
            if (temp <= n) {
                highestLowest = entry.getKey();
                System.out.println("Entry: " + df.format(temp) + " " + df.format(entry.getKey()) + " " + " " + df.format(entry.getValue().getHash()));
            }
            // gogogogogogogog
            if (temp > n) {
                break;
            }
        }
        return highestLowest;
    }

    public int findHighestLowestTemp(double n, ArrayList<chord> tempFingerTable) {
        //arraylist means count lower?
        int note = 0;
        int highestLowest = 0;
        for (chord curchord : tempFingerTable){
            
            //System.out.println((Math.pow(2, note-1)));
            //System.out.println(temp);
            double temp = curchord.getPosition();
            if (temp <= n) {
                highestLowest = note;
            }
            
            // gogogogogogogog
            if (temp > n) {
                break;
            }
            highestLowest = note;
            //formating issues:
            DecimalFormat df = new DecimalFormat("#");
            df.setMaximumFractionDigits(8);
            note++;
        }
        return highestLowest;
    }



    //current working version
    public void validateFingerTable(){
        for (HashMap.Entry<Integer,chord> entry : this.fingerTable.entrySet()){
            //lets validate our table, and Ask out for all of it.
            Event validateFinger = EventFactory.createEvent(VALIDATEFINGER);
            
            double findMe = normalize(entry.getValue().getPosition());
            if (entry.getValue().getHash() == this.parent.giveHash()){
            //this is the one thing we will validate, else we send it all out, outsourced.
                if (backwardRange(findMe) == true){
                    //we mark it as ours, its under our back range
                    System.out.println("Yeah, we own it");
                     //ours
                }
                else {
                    //if its not ours, we just assign it to our forward node, and ask forward node.
                    System.out.println("Says it was us, thats wrong");
                    chord tempChord = entry.getValue();
                    tempChord.setHash(this.parent.forwardHash);
                    tempChord.setAddresss(this.parent.myForwardCon.getName());
                    // System.out.println("this hash : " + this.parent.giveHash());

                    this.fingerTable.put(entry.getKey(), tempChord);
                    double normalized = normalize(entry.getValue().getPosition());
                    String arguments = this.parent.giveName() + " " + normalized + " " + entry.getKey();
                    System.out.println("Arguments here : " + arguments);
                    validateFinger.setData(arguments);
                
                    try {
                        this.parent.myForwardCon.getTcpSender().sendMessage(validateFinger.getBytes(), VALIDATEFINGER);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                    
                    //forwards
                }
            }
            else{
                String [] brokenString = entry.getValue().getAddress().split(":");
                
                //connectionData thisCon = this.parent.peerConnection(brokenString[0],Integer.valueOf(brokenString[1]));
                
                String arguments = this.parent.giveName() + " " + normalize(entry.getValue().getPosition())+ " " + entry.getKey();
                System.out.println("Arguments here : " + arguments);
                validateFinger.setData(arguments);

                try {
                    this.parent.myForwardCon.getTcpSender().sendMessage(validateFinger.getBytes(), VALIDATEFINGER);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // System.out.println("this hash : " + this.parent.giveHash());

            }

            

        }

        printTable();


    }

    //helper method to see if number is in our backhash range that we currently control
    public boolean backwardRange(double findHash){
        if (findHash > 31){
            //normalize it
            findHash = findHash - 31;
        }
        if (findHash == this.parent.giveHash()){
            return true;
        }

        //dont need to give anything, because itll work on its own.
        if (this.parent.backHash > this.parent.giveHash()){
            //this means that we have to cross 0 for it to work, lets give the approx range
            //String[] fullRange = {this.parent.backHash, "0", this.parent.myHash};
            if (findHash >= this.parent.backHash){
                return true;
            }
            if (findHash < this.parent.giveHash()){
                return true;
            }

            //odds are we cross 0
        }
        else{
            if (this.parent.backHash < findHash && findHash <= this.parent.giveHash()){
                return true;
            }
            //String [] fullRange = {this.parent.backHash, this.parent.myHash};
        }

        return false;
    }

    public boolean forwardRange(double findHash){
        if (findHash > 31){
            //normalize it
            findHash = findHash - 31;
        }
        //see if this hash is between me and my next
       
        if (this.parent.forwardHash < this.parent.giveHash()){

            //zero over?
            if (findHash > this.parent.giveHash()){
                return true;
            }

            //String[] fullRange = {this.parent.backHash, "0", this.parent.myHash};
            if (findHash >= this.parent.forwardHash){
                return false;
            }
            if (findHash < this.parent.forwardHash){
                return true;
            }
            //odds are we cross 0
        }
        else{
            if (this.parent.forwardHash >= findHash && findHash > this.parent.giveHash()){
                return true;
            }
            //String [] fullRange = {this.parent.backHash, this.parent.myHash};
        }

        return false;
    }


    public String determineSpot(double findHash){

        if (this.parent.forwardHash == this.parent.giveHash() && this.parent.backHash == this.parent.giveHash()){
            //default setup, its a loop
            return "0";
        }

        if (this.backwardRange(findHash)){
            return "1";
        }

        else{
            return this.parent.myForwardCon.getName();
        }


    }

    public String determineSpotOld(Double findHash){
        double currentHash = this.parent.giveHash();
        printTable();

        //see if we are currently the only node here, in which case we are both
        if (this.parent.forwardHash == this.parent.giveHash() && this.parent.backHash == this.parent.giveHash()){
            //default setup, its a loop
            return "0";
        }
        //first we see if its within our current range
        if (backwardRange(findHash) == true){
            //this is within our range, we are its new forward
            return "1";
        }
        if (forwardRange(findHash) == true){
            //this means its inbetween us and the next one, we are now its back, and its now our forward
            return "2";
        }

        //we didnt find it, send it to the max
        Object[] keys = fingerTable.keySet().toArray();
        chord thisOne = this.fingerTable.get(keys.length);
        return thisOne.getAddress();
        
        //return "";

    }

    public void setEntry(int key, chord me){
        this.fingerTable.put(key, me);
    }

    public chord giveEntry(int spot){
        return this.fingerTable.get(spot);
    }

    //we see a node in the system already, lets take their information!
    

    public String giveFingerTable() {
        StringBuilder stringBuilder = new StringBuilder();
    
        for (HashMap.Entry<Integer, chord> entry : this.fingerTable.entrySet()) {
            
            //i have today learned, stringbuilder is faster, lets use that?

            stringBuilder.append(entry.getValue().getPosition())
                         .append(" ")
                         .append(entry.getValue().getAddress())
                         .append(" ")
                         .append(entry.getValue().getHash())
                         .append(" ");
        }
        printTable();
        return stringBuilder.toString();
    }

    public void loopValidate(){
        System.out.println("self update");
        System.out.println("my hash: " + this.parent.giveHash());
        System.out.println("forward/both hash " + this.parent.forwardHash);
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
                        //System.out.println("this hash : " + this.parent.giveHash());
                        this.fingerTable.put(entry.getKey(), tempChord);

                    }
                    else{
                        chord tempChord = entry.getValue();
                        tempChord.setHash(this.parent.forwardHash);
                        tempChord.setAddresss(this.parent.myForwardCon.getName());
                       // System.out.println("this hash : " + this.parent.giveHash());
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
                        //System.out.println("this hash : " + this.parent.giveHash());
                        this.fingerTable.put(entry.getKey(), tempChord);

                    }
                    else{
                        chord tempChord = entry.getValue();
                        tempChord.setHash(this.parent.giveHash());
                        //System.out.println("this hash : " + this.parent.giveHash());
                        this.fingerTable.put(entry.getKey(), tempChord);
                        //ours
                    }
                }

            }

        }

    }

    public double normalize(double hash){
        if (hash > 31){
            hash = hash - 31;
        }
        return hash;
    }

    public void updateTableNeighbors(){
        System.out.println("self update");
        for (HashMap.Entry<Integer,chord> entry : this.fingerTable.entrySet()){
            System.out.println("Validating : " + entry.getValue().getPosition());
            double findMe = normalize(entry.getValue().getPosition());

            if (backwardRange(findMe) == true){
                //we mark it as ours, its under our back range
                System.out.println("Yeah, we own it");
                chord tempChord = entry.getValue();
                tempChord.setHash(this.parent.giveHash());
                tempChord.setAddresss(this.parent.myBackwardCon.getName());
                this.fingerTable.put(entry.getKey(), tempChord);
                //ours
            }
            else if (forwardRange(findMe) == true){
                System.out.println("Its our forward node:");
                chord tempChord = entry.getValue();
                tempChord.setHash(this.parent.forwardHash);
                tempChord.setAddresss(this.parent.myForwardCon.getName());
                this.fingerTable.put(entry.getKey(), tempChord);
                //forwards
            }
            //else we just leave it as is. 
            else if ((entry.getValue().getHash() == this.parent.giveHash()) && (backwardRange(findMe) == false)){
                //its very clearly not ours anymore, lets revise it.
                //we are setting these here, we know they are wrong, so willbe revized onl ater checking.
                if (findMe < this.parent.giveHash()){
                    //we are turning it to our behind node
                    chord tempChord = entry.getValue();
                    tempChord.setHash(this.parent.backHash);
                    tempChord.setAddresss(this.parent.myBackwardCon.getName());
                    this.fingerTable.put(entry.getKey(), tempChord);
                }
                if (findMe >= this.parent.giveHash()){
                    chord tempChord = entry.getValue();
                    tempChord.setHash(this.parent.forwardHash);
                    tempChord.setAddresss(this.parent.myForwardCon.getName());
                    this.fingerTable.put(entry.getKey(), tempChord);
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
                this.validateFingerTable();
                //this.updateTableNeighbors();
            }
            //if its not a loop we can resolve this traditionally
            //System.out.println("Entry: " + entry.getValue().getPosition() +" " + entry.getKey() + " " + " " + entry.getValue().getHash());
            else if (backwardRange(entry.getValue().getPosition()) == true){
                //we mark it as ours, its under our back range
                chord tempChord = entry.getValue();
                tempChord.setHash(this.parent.giveHash());
                System.out.println("this hash : " + this.parent.giveHash());
                this.fingerTable.put(entry.getKey(), tempChord);
                //ours
                spot = "ours";
            }
            else if (forwardRange(entry.getValue().getPosition()) == true){
                chord tempChord = entry.getValue();
                tempChord.setHash(this.parent.forwardHash);
                tempChord.setAddresss(this.parent.myForwardCon.getName());
                System.out.println("this hash : " + this.parent.giveHash());
                this.fingerTable.put(entry.getKey(), tempChord);
                spot = "theirs";
            }
            else{
                //else we get as close to it as we can from the finger tabler
                int lowestHighest = findHighestLowestTemp(entry.getValue().getPosition(), tempFingerTable);
                System.out.println("Lowest highest here: " + lowestHighest);
                chord oldChord = tempFingerTable.get(lowestHighest);
                chord tempChord = entry.getValue();

                tempChord.setHash(oldChord.getHash());
                tempChord.setAddresss(oldChord.getAddress());
                System.out.println("this hash : " + this.parent.giveHash());
                this.fingerTable.put(entry.getKey(), tempChord);
                spot = oldChord.getAddress();
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
        //askForInfo(callTable);

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
