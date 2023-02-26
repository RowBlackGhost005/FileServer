package fileServerServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ServerProtocol implements Runnable {

    private int clientPort;
    private String clientHost;
    private DatagramSocket connectionSocket;
    private String requestedResource;

    public ServerProtocol(DatagramPacket packet) throws SocketException {
        clientPort = packet.getPort();
        clientHost = packet.getAddress().getHostAddress();
        requestedResource = new String(packet.getData()).trim();

        connectionSocket = new DatagramSocket();
    }

    @Override
    public void run() {

        File requestedFile = new File(requestedResource);

        byte[] packetHead = new byte[8];
        byte[] filePacket = new byte[247];

        ArrayList<byte[]> fileInPackets = new ArrayList<byte[]>();

        int filePackets = 0;

        try{
            FileInputStream fileIn = new FileInputStream(requestedFile);

            filePackets = Math.ceilDiv(fileIn.readAllBytes().length , 247);

            fileIn.getChannel().position(0);

            int packetCount = 0;

            String fileExtension = requestedFile.getAbsolutePath().substring(requestedFile.getAbsolutePath().lastIndexOf(".") , requestedFile.getAbsolutePath().length());

            //Sends the header of the file
            byte[] transferHeader = new byte[255];
            //Sends a header with "amountPackets|fileExtension" to the client
            byte[] fileInfo = (filePackets + "|" + fileExtension).getBytes();
            System.arraycopy( fileInfo , 0 , transferHeader , 0 , fileInfo.length);
            connectionSocket.send(new DatagramPacket(transferHeader , 255 , InetAddress.getByName(clientHost), clientPort));

            while(packetCount < filePackets){

                packetHead = ByteBuffer.allocate(8).putInt(packetCount).array();

                fileIn.read(filePacket);

                byte[] packetToSend = new byte[255];
                System.arraycopy(packetHead , 0 , packetToSend , 0 , 8);
                System.arraycopy(filePacket , 0 , packetToSend , 8 , 247);

                ByteBuffer buffer = ByteBuffer.allocate(8);
                buffer.put(packetHead);
                buffer.rewind();

                System.out.println(buffer.getInt());

                connectionSocket.send(new DatagramPacket(packetToSend , 255 , InetAddress.getByName(clientHost), clientPort));

                filePacket = new byte[247];
                packetCount++;
            }

        }catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] buffer = new byte[255];

        System.arraycopy("end".getBytes() , 0 , buffer , 0 , 3);

        try {
            connectionSocket.send(new DatagramPacket(buffer , 255 , InetAddress.getByName(clientHost), clientPort));
            System.out.println("File transfer to: " + clientHost + " finished!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}

//REFERENCE CODE - SPLIT FILE INTO PACKETS THEN COPY.
/*
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

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
}
 */