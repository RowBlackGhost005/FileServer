package rbg.host;

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

/**
 * Server Protocol of File-Server that holds the logic of opening and sending a file divided in packets of 255 bytes.
 * The packets sent are as follows:
 * 8 bytes of "HEADER" - Which says the order of the packet. Represented as an INTEGER.
 * 247 bytes of "DATA" - That holds the data from the file requested. Represented as BYTES.
 *
 * This protocol handles represents ONE connection and keeps the requested file open till client confirms 0 loss packets or closes the connection.
 *
 * @author Luis Angel Marin
 */
public class HostProtocol implements Runnable {

    //Port of the client to send the packets.
    private int clientPort;
    //Host of the client to send the packets.
    private String clientHost;
    //Socket that holds the communication.
    private DatagramSocket connectionSocket;
    //PATH of the requested resource.
    private String requestedResource;
    //FILE that represents the requested resource.
    File requestedFile;
    //STREAM that to read all the bytes of the file.
    FileInputStream fileIn;
    //Represents the ammount of packets of 247 bytes that requestedFile has.
    int filePackets = 0;

    /**
     * Creates a new ServerProtocol that will connect to the sender of the DatagramPacket given as parameter.
     * The DatagramPacket parameter has to come with the PATH of the file requested.
     * @param packet Packet to get the information.
     * @throws SocketException Exception if the socket cannot be created.
     */
    public HostProtocol(DatagramPacket packet) throws SocketException {
        //Sets the Client information
        clientPort = packet.getPort();
        clientHost = packet.getAddress().getHostAddress();

        requestedResource = new String(packet.getData()).trim();

        connectionSocket = new DatagramSocket();
    }

    /**
     * Executes this ServerProtocol to start reading and slicing into packets the file requested.
     */
    @Override
    public void run() {

        //Opens the file requested.
        requestedFile = new File(requestedResource);

        //Creates both parts of a complete packet
        byte[] packetHead = new byte[8];
        byte[] filePacket = new byte[247];

        try{
            //Opens the requested file in a stream.
            fileIn = new FileInputStream(requestedFile);

            //Determines how many packets of 247 bytes are needed to send the file.
            filePackets = Math.ceilDiv(fileIn.readAllBytes().length , 247);
            //Return stream pointer to 0.
            fileIn.getChannel().position(0);



            String fileExtension = requestedFile.getAbsolutePath().substring(requestedFile.getAbsolutePath().lastIndexOf(".") , requestedFile.getAbsolutePath().length());

            //Sends the header of the file
            byte[] transferHeader = new byte[255];
            //Sends a header with "amountPackets|fileExtension" to the client
            byte[] fileInfo = (filePackets + "|" + fileExtension).getBytes();
            System.arraycopy( fileInfo , 0 , transferHeader , 0 , fileInfo.length);
            connectionSocket.send(new DatagramPacket(transferHeader , 255 , InetAddress.getByName(clientHost), clientPort));

            //Holds the number of the packet that is being procesed.
            int packetCount = 0;

            //Starts to assamble all the packets
            while(packetCount < filePackets){

                //Creates the "HEAD" of the packet that represents the order of this packet
                packetHead = ByteBuffer.allocate(8).putInt(packetCount).array();

                //Reads 247 bytes ot ouf the file requested.
                fileIn.read(filePacket);

                //Combines both arrays into a single packet of 255 bytes as follows: HEAD (8 bytes) + DATA (247 bytes).
                byte[] packetToSend = new byte[255];
                System.arraycopy(packetHead , 0 , packetToSend , 0 , 8);
                System.arraycopy(filePacket , 0 , packetToSend , 8 , 247);

                //Sends the 255 bytes packet to the client.
                connectionSocket.send(new DatagramPacket(packetToSend , 255 , InetAddress.getByName(clientHost), clientPort));

                //Resets the data packet and increase the packetNumber.
                filePacket = new byte[247];
                packetCount++;
            }

        }catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Sends an "end" message to the client meaning that all packets had been sent.
        try {
            byte[] buffer = new byte[255];
            System.arraycopy("end".getBytes() , 0 , buffer , 0 , 3);
            connectionSocket.send(new DatagramPacket(buffer , 255 , InetAddress.getByName(clientHost), clientPort));
            System.out.println("File transfer to: " + clientHost + " finished, packet(s) send: " + filePackets + " waiting for confirmation. . .");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Waits for confirmation
        waitConfirmation();
    }

    /**
     * Method that starts listen again to the client for any missing packets before closing the file and the connection.
     */
    public void waitConfirmation(){
        //Holds number of the missed packets.
        ArrayList<Integer> missedPackets = new ArrayList<>();

        try {
            while(true) {
                //Starts to listen to the client.
                DatagramPacket datagramReceived = new DatagramPacket(new byte[255], 255);
                connectionSocket.receive(datagramReceived);

                //Tries to read a String from the received data.
                String isEnd = new String(datagramReceived.getData()).trim();
                //If client sent "aok" no packets were missing, connection will be closed.
                if (isEnd.equalsIgnoreCase("aok")) {
                    break;
                }

                //Reads the first 8 bytes from the 255 packet sent from the client.
                byte[] packetHead = new byte[8];
                System.arraycopy(datagramReceived.getData(), 0, packetHead, 0, 8);
                //Convert the 8 bytes received into an integer that represents the packet index that were missing.
                ByteBuffer buffer = ByteBuffer.allocate(8);
                buffer.put(packetHead);
                buffer.rewind();
                int packetIndex = buffer.getInt();

                missedPackets.add(packetIndex);

                //If client sent a "mpf" means MissedPacketsFinish, so client will not request more packets
                if(isEnd.equalsIgnoreCase("mpf")){
                    //This protocol starts re-sending said missed packets.
                    reSendMissedPackets(missedPackets);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            closeConnection();
        }
    }

    /**
     * Method that re-sends the missed packets requested by the client.
     * Said missedPackets come from the ArrayList given as parameter.
     * @param missedPackets List of packet indexes to re-send.
     */
    public void reSendMissedPackets(ArrayList<Integer> missedPackets){

        System.out.println("Re-Sending " + missedPackets.size() + " packet(s) to: " + clientHost);

        for(Integer packetIndex : missedPackets){
            try {
                //Sets the same index to the packet
                byte[] packetHead = ByteBuffer.allocate(8).putInt(packetIndex).array();
                byte[] filePacket = new byte[247];

                //If any other data was sent or index is larger than file means end of the file has been reached
                if((packetIndex * 247) < 0){
                    break;
                }

                //Sets the pointer of the stream to the location of the bytes that went missing in "packetIndex".
                fileIn.getChannel().position((packetIndex * 247));
                fileIn.read(filePacket);

                //Creates a single packet.
                byte[] packetToSend = new byte[255];
                System.arraycopy(packetHead , 0 , packetToSend , 0 , 8);
                System.arraycopy(filePacket , 0 , packetToSend , 8 , 247);

                connectionSocket.send(new DatagramPacket(packetToSend , 255 , InetAddress.getByName(clientHost), clientPort));

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        //Sends an "end" message to the client meaning that all packets had been sent.
        try {
            byte[] buffer = new byte[255];
            System.arraycopy("end".getBytes() , 0 , buffer , 0 , 3);
            connectionSocket.send(new DatagramPacket(buffer , 255 , InetAddress.getByName(clientHost), clientPort));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Releases the file that was requested and closes this socket.
     */
    public void closeConnection(){
        try {
            fileIn.close();
            connectionSocket.close();
            System.out.println("File transfer to: " + clientHost + ":" + clientPort + " finished successfully!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}