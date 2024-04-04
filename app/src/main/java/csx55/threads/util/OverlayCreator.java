package csx55.threads.util;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class OverlayCreator {

    private HashMap <String, connectionData> mNodes;

    //stringkey will be the same tempkey as before, ip+port tempkey
    private HashMap <String, ArrayList<OverlayEntry>> nodeMap = new HashMap<String, ArrayList<OverlayEntry>>();
    
    //all the nodes we have
    private ArrayList <String> allNodes = new ArrayList<String>();
    //finished map
    private ArrayList<String[]> allMap = new ArrayList<String[]>();

    //random, for random random operations
    Random random;


    public OverlayCreator(){
        //default constructor
    }
    public ArrayList<String[]> getAllMap(){
        return this.allMap;
    }

    public String[][] getAllMapReadyShip(){
        int rows = this.allMap.size();
        int cols = 0;

        // Find the maximum number of columns among all rows
        for (String[] row : this.allMap) {
            cols = Math.max(cols, row.length);
        }

        // Create the 2D array
        String[][] result = new String[rows][cols];

        // Copy data from allMap to the 2D array
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < this.allMap.get(i).length; j++) {
                result[i][j] = this.allMap.get(i)[j];
            }
        }

        return result;
    }

    public void initialize(HashMap <String, connectionData> mNodes){
        //We will setup overlay
        //all noes wil be in the mNodes created beforehand, so lets begin populating this up. Arraylists will hold hte nodes that htat named node
        //has in its link. The names are of course on mNodes.name, the string is their tempkey.

        this.mNodes = mNodes;
        this.random = new Random();

        //This initializes our hashmap of nodeMap before we get into adding stuff
        //ArrayList<String> names = new ArrayList<String>();
        for (Map.Entry<String, connectionData> entry : this.mNodes.entrySet()) {

            String name = entry.getKey();
            //MessengerData value = entry.getValue();
            ArrayList<OverlayEntry> temp = new ArrayList<OverlayEntry>();
            
            this.allNodes.add(name);
            this.nodeMap.put(entry.getKey(), temp);

        }
        
    }

    public void assignWeights(int cr){
        for (String[] array : this.allMap) {
            array[2] = String.valueOf(cr);
        }
    }

    public void showWeights(){
        System.out.println("Showing weights/basic Map");
        System.out.println("First node | Second node | Weight");
        for (String[] array : this.allMap) { 
            for (String element : array) { 
                System.out.print(element + " "); 
            }
            System.out.println(); // Print a new line after each array
        }
    }

    public void boltConnection(){
        ///private ArrayList <String> allNodes = new ArrayList<String>();
        Boolean toggle = true;
        ArrayList<String> exclusions = new ArrayList<String>();
        //System.out.println(this.allNodes.size());

        int curIndex = (int)(Math.random() * this.allNodes.size()); 
        String name = this.allNodes.get(curIndex);
        
        exclusions.add(name);

        while(toggle){
            connectionData thisData = this.mNodes.get(name);
            //System.out.println("In while");
            ArrayList<OverlayEntry> connection = this.nodeMap.get(name);

            String candidate = notTheseNodes(name, exclusions);

            String tempname = thisData.getIP() + ":" + thisData.getPort();

            OverlayEntry thisEntry = new OverlayEntry(tempname, thisData.getIP(), thisData.getPort());
                if (candidate == null){
                    break;
                    //return 1;
                }
                if (!exclusions.contains(candidate)){
                    connectionData temp = this.mNodes.get(candidate);
                    //name, ip, and port
                    tempname = thisData.getIP() + ":" + thisData.getPort();
                    OverlayEntry newEntry = new OverlayEntry(tempname, temp.getIP(), temp.getPort());
                    //System.out.println(temp.getName() + temp.getIP() + temp.getPort());
                    connection.add(newEntry);
                    exclusions.add(candidate);
                    this.nodeMap.get(candidate).add(thisEntry);
                    //We then use this next node to pick the next node
                    //This is for weights later
                    String[] tempArray = {name, temp.getIP()+":"+temp.getPort(), "null"};
                    this.allMap.add(tempArray);
                }
                else{
                    exclusions.add(candidate); //this is to get rid of the ones that have a large size early, and cut down time
                }
            if (exclusions.size() >= this.allNodes.size()){
                toggle = false;
                name = candidate;
            }
            else{
                //else we set the candidate up as the next one:
                name = candidate;
            }
        }

        //System.out.println("LAst one is: " + name);

        connectionData thisData = this.mNodes.get(name);
        ArrayList<OverlayEntry> connection = this.nodeMap.get(name);
        OverlayEntry thisEntry = new OverlayEntry(thisData.getName(), thisData.getIP(), thisData.getPort());
        String firstCon = exclusions.get(0);
        //System.out.println("first con was: " + firstCon);
        //Last step, we connect first to last
        connectionData temp = this.mNodes.get(firstCon);
                    //name, ip, and port
        OverlayEntry newEntry = new OverlayEntry(temp.getName(), temp.getIP(), temp.getPort());
        connection.add(newEntry);
        this.nodeMap.get(firstCon).add(thisEntry);
        String[] tempArray = {name, temp.getIP()+":"+temp.getPort(), "null"};
        this.allMap.add(tempArray);

        //System.out.println("BOLTED");

    }

    public String notTheseNodes(String meNode, ArrayList<String> theseNodes){
        ArrayList<String> curNodes = new ArrayList<>(this.nodeMap.keySet());
        curNodes.removeAll(theseNodes);
        curNodes.remove(meNode);
        if (curNodes.size() <= 0){
            //Hangnail
            return null;
        }
        return curNodes.get(this.random.nextInt(curNodes.size()));
    }





    
}
