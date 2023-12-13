package rbg.client;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that represents a Client that can request file to the FileServer
 *
 * @author Luis Angel Marin
 */
public class Client {

    //Socket used to communicate to the server.
    private DatagramSocket clientSocket;
    //Packet used to send the requests.
    private DatagramPacket clientPacket;
    //Buffer to use to store the information.
    private byte[] buffer;
    //Address of the server.
    private String serverAddress = "localhost";
    //Port of the server
    private int serverPort = 4444;
    //Represents in which address the connection was handled.
    //It is used to request missing packets.
    private String serverShardAddress;
    //Represents in which port the connection was handled.
    //It is used to request missing packets.
    private int serverShardPort;
    //Represents the amount of packets expected to be received.
    private int packetsToReceive;
    //Extension of the file to be received.
    private String fileExtension;
    //String use to name the requested file once written.
    private String fileName;
    //Represents the amount of packets of the file that has been received.
    private FilePacket[] filePackets;
    //Path to save the file requested.
    private String filePath;
    //Port where this client will be listening.
    private int clientPORT = 5555;
    
    public static void main(String[] args){
        
        Client client = new Client();
        
        client.connectServer();
    }

    /**
     * Method that connects to the server to request the file and receives the packets sent by the server.
     */
    public void connectServer(){

        Scanner userInput = new Scanner(System.in);

        int packetsReceived = 0;

        try {


            //// REQUEST FOR USER INPUT ////
            System.out.println("Ingrese el PATH del recurso a solicitar: ");
            buffer = userInput.nextLine().getBytes();//"Ping!".getBytes();

            System.out.println("Ingrese el nombre que tendrá el archivo: ");
            fileName = userInput.nextLine();

            System.out.println("Ingrese el PATH donde se guardará el archivo: ");
            filePath = userInput.nextLine();

            System.out.println("Ingrese el PUERTO donde escuchará: ");
            clientPORT = userInput.nextInt();
            userInput.nextLine();

            //// REQUEST FOR USER INPUT - end ////

            //Set up the socket
            clientSocket = new DatagramSocket(clientPORT , InetAddress.getByName("localhost"));

            System.out.println("Client Ready!");

            //Creates the packet and sends the request of the file
            clientPacket = new DatagramPacket(buffer , buffer.length , InetAddress.getByName(serverAddress) , serverPort);
            buffer = new byte[255];
            clientSocket.send(clientPacket);

            //Packet used for the server responses
            DatagramPacket inPacket = new DatagramPacket(new byte[255] , 255);

            //Says if the server has sent the first packet with information about the transfer
            boolean serverHeader = false;
            String serverHeaderInfo = "";

            while (true) {

                //Comienza a escuchar para recibir lo solicitado
                clientSocket.receive(inPacket);

                if (!serverHeader) {
                    serverHeaderInfo = new String(inPacket.getData()).trim();

                    //Receives the amount of packets to receive and the file extension of the file to be received.
                    packetsToReceive = Integer.parseInt(serverHeaderInfo.substring(0, serverHeaderInfo.indexOf("|")));
                    fileExtension = serverHeaderInfo.substring(serverHeaderInfo.indexOf("|") + 1, serverHeaderInfo.length());

                    System.out.println("----------");
                    System.out.println("Requested " + packetsToReceive + " packet(s) from a " + fileExtension + " file" );
                    System.out.println("Waiting for response. . . .");

                    //Setting the packets cache
                    filePackets = new FilePacket[packetsToReceive];

                    //Config of the shard that sends the response
                    serverShardAddress = serverAddress;
                    serverShardPort = inPacket.getPort();

                    serverHeader = true;
                }

                //Receives the packet sent by the server.
                byte[] receivedPacket = inPacket.getData();
                //Setting up both parts of the packet.
                byte[] fileDataPacket = new byte[247];
                byte[] packetHead = new byte[8];
                //Divides the packet sent by the server into the HEAD and DATA
                System.arraycopy(inPacket.getData(), 0, packetHead, 0, 8);
                System.arraycopy(receivedPacket, 8, fileDataPacket, 0, 247);

                //Decodes the HEAD of the packet, that holds the packet INDEX.
                ByteBuffer buffer = ByteBuffer.allocate(8);
                buffer.put(packetHead);
                buffer.rewind();
                int packetIndex = buffer.getInt();

                //Checks if server sent and "end" meaning that all packets were sent
                String isEnd = new String(inPacket.getData()).trim();
                if (isEnd.equalsIgnoreCase("end")) {
                    System.out.println("File transfer completed! received: " + packetsReceived + " packet(s)");
                    break;
                }

                //Stores the data in the correct buffer index
                if (!(packetIndex > packetsToReceive)) {
                    //Creates a new FilePacket
                    FilePacket filePacket = new FilePacket(packetIndex, fileDataPacket);

                    //Add packets into the buffer
                    filePackets[filePacket.getFilePacketNumber()] = filePacket;

                    packetsReceived++;
                }


                inPacket.setData(new byte[255]);
            }

            //Checks for integrity once the file transfer has been completed.
            checkFileIntegrity();

        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method that checks if all packets were received correctly.
     * If some packets were missing it stores which indexes and ask the server for those.
     * If no packets were missing, connection is closed and begins the file writting.
     */
    public void checkFileIntegrity(){
        System.out.println("----------\nIntegrity check. . .");
        //List of packet indexes missed
        ArrayList<Integer> missedPackets = new ArrayList<Integer>();

        //Check if a cell on the buffer has no information
        //If null is found means that the packet went lost.
        for(int i = 0 ; i < filePackets.length ; i++){
            if(filePackets[i] == null){
                missedPackets.add(i);
            }
        }

        System.out.println("Missing " + missedPackets.size() + " Packet(s)!");

        //Check if there were missing packets.
        if(missedPackets.size() == 0){
            System.out.println("Integrity check passed!");
            closeConnection();
        }else{
            //Request the missing packets
            requestMissingPackets(missedPackets);
        }
    }

    /**
     * Method that request the missing index packets given as parameter.
     * This method sends ONE request for each index missed.
     * @param missedPackets List of missed packets to ask for.
     */
    public void requestMissingPackets(ArrayList<Integer> missedPackets){
        System.out.println("Requesting " + missedPackets.size() + " packet(s). . .");

        try {
            byte[] packetHead = new byte[8];

            //Sends a request for each packet missed
            for(int i = 0 ; i < missedPackets.size() ; i++){
                //Only sends the HEAD and means the packet index lost.
                packetHead = ByteBuffer.allocate(8).putInt(missedPackets.get(i)).array();
                byte[] packetToSend = new byte[255];
                System.arraycopy(packetHead , 0 , packetToSend , 0 , 8);

                //Sends the request
                clientSocket.send(new DatagramPacket(packetToSend , 255 , InetAddress.getByName(serverAddress), serverShardPort));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        //Sends a "mpf" message to the server, meaning all missing packet request were sent.
        try {
            byte[] messageToSend = new byte[255];
            System.arraycopy("mpf".getBytes() , 0 , messageToSend , 0 , 3);
            clientSocket.send(new DatagramPacket(messageToSend , 255 , InetAddress.getByName(serverAddress), serverShardPort));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Missed packet(s) requested, waiting for response. . .");
        listenDataTransfer();

    }

    /**
     * Method that listen for the request of the missed packets
     */
    public void listenDataTransfer(){

        while (true){
            try {
                //Creates a datagram to use.
                DatagramPacket inPacket = new DatagramPacket(new byte[255] , 255);

                //Listen for the missed packet requested.
                clientSocket.receive(inPacket);

                //Reads the data received
                byte[] receivedPacket = inPacket.getData();

                //Divides the data received into a HEAD and the DATA
                byte[] fileDataPacket = new byte[247];
                byte[] packetHead = new byte[8];
                System.arraycopy(inPacket.getData() , 0 , packetHead , 0 , 8);
                System.arraycopy(receivedPacket , 8 , fileDataPacket , 0 , 247);

                //Try to get the index of the packet received.
                ByteBuffer buffer = ByteBuffer.allocate(8);
                buffer.put(packetHead);
                buffer.rewind();
                int packetIndex = buffer.getInt();

                //Check if the server has sent and "end" message, meaning all request were attended.
                String isEnd = new String(inPacket.getData()).trim();
                if(isEnd.equalsIgnoreCase("end")){
                    break;
                }

                //If packet index is correct saves the packet into the buffer
                if(!(packetIndex > packetsToReceive)) {
                    //Creates a new FilePacket
                    FilePacket filePacket = new FilePacket(packetIndex, fileDataPacket);

                    //Add packets into the buffer
                    filePackets[filePacket.getFilePacketNumber()] = filePacket;
                }

                inPacket.setData(new byte[255]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Missed packet(s) received.");

        //Checks for integrity once again
        checkFileIntegrity();
    }

    /**
     * Closes the socket meaning that the transfer has been succsessfull.
     */
    public void closeConnection(){

        //Sends an "aok" message to the server meaning that the file transfer has been finished
        try {
            byte[] response = new byte[255];
            System.arraycopy("aok".getBytes() , 0 , response , 0 , 3);
            clientSocket.send(new DatagramPacket(response , 255 , InetAddress.getByName(serverAddress), serverShardPort));
            System.out.println("----------\n" + "File transfer from: " + serverAddress + " finished!");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally{
            clientSocket.close();
            writeFile();
        }
    }

    /**
     * Method that writes all the packets received into a file in the given path using the given file name
     */
    public void writeFile(){
        System.out.println("----------\n"+"Writting file. . .");

        try {
            //Creates the file in the path with the given name
            FileOutputStream fileReceived = new FileOutputStream(filePath + "\\" + fileName + fileExtension);

            //Starts writting all the packets in order
            for(int i = 0 ; i < filePackets.length ; i++){
                fileReceived.write(filePackets[i].getFilePacketInfo());
            }

            fileReceived.flush();
            fileReceived.close();

            System.out.println("File writting completed!");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
