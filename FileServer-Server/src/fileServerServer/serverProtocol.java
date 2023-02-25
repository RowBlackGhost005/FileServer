package fileServerServer;

import java.io.*;
import java.net.DatagramPacket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class serverProtocol implements Runnable {

    public static void main(String[] args) {

        byte[] packetHead = new byte[8];
        byte[] filePacket = new byte[247];

        ArrayList<byte[]> fileInPackets = new ArrayList<byte[]>();

        int filePackets = 0;

        try {
            File file = new File("C:\\Users\\lamar\\Downloads\\ESP32.png");

            FileInputStream fileIn = new FileInputStream(file);

            System.out.println("Total Space: " + file.getTotalSpace());

            //System.out.println("Bytes Content: " + fileIn.readAllBytes().length);

            filePackets = Math.ceilDiv(fileIn.readAllBytes().length , 247);

            System.out.println("Total Packets: " + filePackets);

            fileIn.getChannel().position(0);

            int packetCount = 0;

            while(packetCount < filePackets){

                packetHead = ByteBuffer.allocate(8).putInt(packetCount).array();

                fileIn.read(filePacket);

                byte[] packetToSend = new byte[255];
                System.arraycopy(packetHead , 0 , packetToSend , 0 , 8);
                System.arraycopy(filePacket , 0 , packetToSend , 8 , 247);

                fileInPackets.add(packetToSend);

                filePacket = new byte[247];
                packetCount++;
            }

            System.out.println(fileInPackets.size());

            //Write the damn file

            /*
            File fileToWrite = new File("C:\\Users\\lamar\\Downloads\\UDPSockets.png");
            FileOutputStream fileWritter = new FileOutputStream(fileToWrite);

            int filePacketsToWrite = fileInPackets.size();
            int filePacketWritting = 0;
            while(filePacketWritting < filePacketsToWrite){

                byte[] packetToWrite = new byte[247];

                System.arraycopy(fileInPackets.get(filePacketWritting) , 8 , packetToWrite , 0 , 247);

                fileWritter.write(packetToWrite);

                filePacketWritting++;
            }

             */


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public serverProtocol(DatagramPacket packet){
    }

    @Override
    public void run() {

    }
    
}
