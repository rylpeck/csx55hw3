package csx55.threads.wireformat;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.Instant;

//base eevent, could use more of a rework
public interface Event{

    public byte[] getBytes();
    
    public int getType();

    public void setData(String arguments);
    //specifically for our map
    default void setData(String [][]arguments){};

    default String stringBuilder(String[][] atrArr){
        //System.out.println("String builder");
        //System.out.println(atrArr[0][0]);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < atrArr.length; i++) {
            String[] row = atrArr[i];
            for (int j = 0; j < row.length; j++) {
                sb.append(row[j]);
                if (!(i == atrArr.length - 1 && j == row.length - 1)) { // Check if it's the absolute last entry
                    sb.append(" ");
                }
            }
        }
        return sb.toString();

    }
    //test

    default byte[] gatherData(String [][]data){
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        try {
            int temp = getType();
            dout.writeInt(temp);
        
            long timeStamp = Instant.now().toEpochMilli();
            dout.writeLong(timeStamp);

            String message = stringBuilder(data);

            byte[] messageBytes = message.getBytes();
            int messageLength = messageBytes.length;
            dout.writeInt(messageLength);
            dout.write(messageBytes);

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

    default String[] deepCopyArray(String[] array, int start, int end) {
        int length = end - start;
        String[] newArray = new String[length];
        for (int i = 0; i < length; i++) {
            newArray[i] = new String(array[start + i]);
        }
        return newArray;
    }

    default void deepCopyDouble(String[][] source, String[][]data){

        for (int i = 0; i < source.length; i++) {
            System.arraycopy(source[i], 0, data[i], 0, source[i].length);
        }

    }

    public String[][] giveData();




}



//Type list
//Register 0
//Message 1