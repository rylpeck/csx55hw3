package csx55.chord.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Scanner;

import csx55.chord.peer;
import csx55.chord.Node.peerNode;
import csx55.chord.wireformat.Event;
import csx55.chord.wireformat.EventFactory;
import csx55.chord.wireformat.Protocol;

public class fileManager implements Protocol{

    private double myHash;
    private String myFolder = "";
    peerNode parent;

    public fileManager(double hash, peerNode myParent){
        this.myHash = hash;
        this.parent = myParent;
        boolean result = createFolder(this.myHash);
        //System.out.println("fileFolderCreated " + result);
    }

    public boolean createFolder(double hash) {
        DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(8);
        
        String folderPath = "/tmp/" + df.format(hash);
        this.myFolder = folderPath;
        File folder = new File(folderPath);
        // Check if the folder already exists
        if (folder.exists()) {
            return true; // Folder already exists
        } else {
            // Try creating the folder
            return folder.mkdirs(); // mkdirs() creates both the folder and any necessary parent directories
        }
    }

    public void migrateFiles(double newHash){
        //give it the new hash, any files under that, get shipped to it, or however idk
        File folder = new File(this.myFolder);

        if (!folder.isDirectory()) {
           // System.out.println("Provided path is not a directory.");
            return;
        }

        File[] files = folder.listFiles();
        
        // Check if the directory is empty
        if (files == null || files.length == 0) {
            //System.out.println("Directory is empty.");
            return;
        }
            
        System.out.println("Files in directory: " + this.myFolder);
        for (File file : files) {
            System.out.println(file.getName());
        }

    }
    public byte[] readFileBytes(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void sendTheFileString(String address, String filepath){
        String readFile = readFileToString(filepath);
        Event sendFile = EventFactory.createEvent(SENDFILE);
        String [] brokenString = address.split(":");
        connectionData tempCon = this.parent.peerConnection(brokenString[0], Integer.valueOf(brokenString[1]));

        String arguments = getFileName(filepath) + " " + readFile;
        sendFile.setData(arguments);


        try {
            //System.out.println(tempCon.getIP() + " " + tempCon.getPort());
            tempCon.getTcpSender().sendMessage(readFile.getBytes(), CONTACTPEERRESPONSE);
            
            //System.out.println("Sent message");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
 
    }



    public void sendTheFileBytes(String address, String filepath){
        Event sendFile = EventFactory.createEvent(SENDFILE);
        String [] brokenString = address.split(":");
        connectionData tempCon = this.parent.peerConnection(brokenString[0], Integer.valueOf(brokenString[1]));

        //the sendfile connection will have [1][0] be the filename. lets do this.

        byte[] fileByte = readFileBytes(filepath);
       
        String arguments = getFileName(filepath);

        byte [] sendMe = writeFilePackage(fileByte, arguments);

        sendFile.setData(arguments + " ");
        byte[] sendFileByte = sendFile.getBytes();

        byte[] combinedBytes = new byte[sendFileByte.length + fileByte.length];

        System.arraycopy(sendFileByte, 0, combinedBytes, 0, sendFileByte.length);

        System.arraycopy(fileByte, 0, combinedBytes, sendFileByte.length, fileByte.length);

        
        try {
            //System.out.println(tempCon.getIP() + " " + tempCon.getPort());
            tempCon.getTcpSender().sendMessage(sendMe, CONTACTPEERRESPONSE);
            
            //System.out.println("Sent message");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getFileName(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file.getName();
        } else {
            System.out.println("File does not exist.");
            return null; // or throw an exception depending on your requirement
        }
    }


    public static String readFileToString(String filePath) {

        StringBuilder fileContents = new StringBuilder();

        Scanner scanner;
        try {
            scanner = new Scanner(new File(filePath));
        
            // Read line by line until there are no more lines
            while (scanner.hasNextLine()) {
                // Read the next line
                String line = scanner.nextLine();
                // Append the line to the StringBuilder
                fileContents.append(line).append("\n");
            }

            // Close the scanner
            scanner.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Convert StringBuilder to String and return
        return fileContents.toString();
    }
    
    public void writeTheFileBytes(String[][]data, byte[] fileData){

        //System.out.println("Caught a file eyyyyy");
        //System.out.println("name of: " + data[0][0]);

        StringBuilder sb = new StringBuilder();

        for (int i = data.length / 2; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                sb.append(data[i][j]);
            }
        }

        try {
            String fileLoc = this.myFolder + "/" + data[0][0];
            
            FileOutputStream fos = new FileOutputStream(fileLoc);
            //System.out.println(fileData);
            fos.write(fileData);
            
        
            fos.close();
            System.out.println("Wrote an incoming file");
        } catch (IOException e) {
            System.out.println("WHAT");
            e.printStackTrace();
        }

    }

    public byte[] writeFilePackage(byte[] fileBytess, String arguments){
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        try {
            int temp = 70;
            dout.writeInt(temp);
        
            long timeStamp = Instant.now().toEpochMilli();
            dout.writeLong(timeStamp);

            String message = arguments;

            byte[] messageBytes = message.getBytes();
            int messageLength = messageBytes.length;
            dout.writeInt(messageLength);
            dout.write(messageBytes);

            //byte[] fileBytes = 
            int fileBytesLen = fileBytess.length;
            dout.writeInt(fileBytesLen);
            dout.write(fileBytess);

            dout.flush();
            marshalledBytes = baOutputStream.toByteArray();
            //baOutputStream.close();
            //dout.close();

        } catch (IOException e) {
            System.err.println("ERROR MARSHALLING");
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return marshalledBytes;
    }

    public String stringBuilder(String[][] atrArr){
        //System.out.println("String builder");
        //System.out.println(atrArr[0][0]);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < atrArr.length; i++) {
            String[] row = atrArr[i];
            for (int j = 0; j < row.length; j++) {
                sb.append(row[j]);
                if (!(i == atrArr.length - 1 && j == row.length - 1)) {
                    sb.append(" ");
                }
            }
        }
        return sb.toString();

    }

    //ran after declaring our backhas as new to maket he math easier
    public void backTransfer(){

       
        File folder = new File(this.myFolder);
        
       
        // Get list of files in the folder
        File[] files = folder.listFiles();
        
        // Check if there are any files
        if (files == null || files.length == 0) {
            //System.out.println("No files found this node");
            return;
        }
        
        // Process each file
        for (File file : files) {
            if (file.isFile()) {
                // Call the method to process the file
                verifyBackTransfer(file);
            }
        }
        
    }



    public void throwAllForward(){
        //throw all data forward, dont care, gg, im out, bye
        File folder = new File(this.myFolder);
        
        // Get list of files in the folder
        File[] files = folder.listFiles();
        
        // Check if there are any files
        if (files == null || files.length == 0) {
            System.out.println("No files in this node");
            return;
        }
        
        // Process each file
        for (File file : files) {
            if (file.isFile()) {
                // Call the method to process the file
                sendTheFileBytes(this.parent.myBackwardCon.getName(), file.getName());
            }
        }
    }


    double maxSize = 2147483647 * 2;
    public double normalize(double hash){
        if (hash < 0){
            double temp = -1 * hash;
            //hashedName = (-1.0 * hashedName) + 2147483647;
            hash = (temp + 2147483647);
        }
        if (hash > maxSize){
            hash = hash - this.maxSize;
        }
        return hash;
    }


    private void verifyBackTransfer(File file){
        String fileName = file.getName();
        double trueHash = normalize(fileName.hashCode());
        if (this.parent.myFingerTable.backwardRange(trueHash) == true){
            //this means its under our hood
        }
        else{
            //if its not under our area then we send it
            sendTheFileBytes(this.parent.myBackwardCon.getName(), fileName);
        }
        //method to verify if the folder is backtransferable
    }


    public int listMyFolder(){
        File folder = new File(this.myFolder);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                Double quickHash = normalize(file.getName().hashCode());
                System.out.println(file.getName() + " " + normalize(quickHash));
            }
            return 0;
        } else {
            return 1;
        }
    }



    public boolean doIhaveThisFile(String fileName) {
        // Construct the file path
        String filePath = this.myFolder + "/" + fileName;
        // Create a File object
        File file = new File(filePath);
        
        // Check if the file exists
        return file.exists();
    }
    
}
