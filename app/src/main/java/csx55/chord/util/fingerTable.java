package csx55.chord.util;
import java.text.DecimalFormat;
import java.util.HashMap;

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

            double temp = this.parent.giveHash()+(Math.pow(2, note-1));
            //System.out.println((Math.pow(2, note-1)));
            //System.out.println(temp);
            while(temp > 2147483647){
                //System.out.println("looper");
                temp = temp - 2147483647;    
            }
            //formating issues:
            DecimalFormat df = new DecimalFormat("#");
            df.setMaximumFractionDigits(8);
            
            System.out.println("Entry: " + df.format(temp) +" " + entry.getKey() + " " + " " + entry.getValue().getHash());
            note++;
        }


    }

    //default is called when we know we are hte only node in the system.

    public void defaultSetup(int myHash, String myIp){

        for (int i = 1; i < 33; i++){
            chord tempChord = new chord(myHash, this.parent.giveName());

            this.fingerTable.put(i, tempChord);
        }
        this.parent.myForwardCon = null;
        this.parent.myBackwardCon = null;


        printTable();
        determineSpot(20488);

    }

    public int findHighestLowest(int n) {
        int note = 1;
        int highestLowest = 1;
        for (HashMap.Entry<Integer,chord> entry : this.fingerTable.entrySet()){

            double temp = this.parent.giveHash()+(Math.pow(2, note-1));
            //System.out.println((Math.pow(2, note-1)));
            //System.out.println(temp);
            while(temp > 2147483647){
                //System.out.println("looper");
                temp = temp - 2147483647;    
            }
            if (temp > n){
                //hash will probably be under the last one then
                break;
            }
            highestLowest = note;
            //formating issues:
            DecimalFormat df = new DecimalFormat("#");
            df.setMaximumFractionDigits(8);
            
            System.out.println("Entry: " + df.format(temp) +" " + entry.getKey() + " " + " " + entry.getValue().getHash());
            note++;
        }


        return highestLowest;
    }


    //we see a node in the system already, lets take their information!
    public chord determineSpot(int findHash){
        int currentSpot = this.parent.giveHash();
    //methodbefore this will shear the data to what we want. 
        HashMap<Integer, chord> tempTable = new HashMap<Integer, chord>();
        int lowestHighest = findHighestLowest(findHash);
        System.out.println("Power is gonna be: " + lowestHighest);
        chord thisOne = this.fingerTable.get(lowestHighest);
        System.out.println("Which is: " + thisOne.getAddress() + " and " + thisOne.getHash());
        if (thisOne.getHash() == (this.parent.giveHash())){
            System.out.println("That node is me!");
            return null;
        }
        else{
            System.out.println("Giving you information, closest I can get you");
            return thisOne;
        }
       
    }

    


    
}
