package fileServerClient;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    
    private int port;
    private DatagramSocket clientSocket;
    private DatagramPacket clientPacket;
    private byte[] buffer;
    private String serverAddress = "localhost";
    private int serverPort = 4444;
    
    public static void main(String[] args){
        
        Client client = new Client();
        
        client.connectServer();
    }
    
    public void connectServer(){

        Scanner userInput = new Scanner(System.in);

        try {
            clientSocket = new DatagramSocket(5555 , InetAddress.getByName("localhost"));
            buffer = new byte[255];


            System.out.println("Client Ready!");

            buffer = userInput.nextLine().getBytes();//"Ping!".getBytes();

            clientPacket = new DatagramPacket(buffer , buffer.length , InetAddress.getByName(serverAddress) , serverPort);

            clientSocket.send(clientPacket);

            //Packet de respuesta
            DatagramPacket inPacket = new DatagramPacket(new byte[255] , 255);

            FileOutputStream fileReceived = new FileOutputStream("C:\\Users\\lamar\\OneDrive\\Documentos\\NetBeansProjects\\FileServer\\FileServer-Client\\received.jpg");

            ArrayList<byte[]> fileInPackets = new ArrayList<byte[]>();

            while (true){

                //Comienza a escuchar para recibir lo solicitado
                clientSocket.receive(inPacket);

                byte[] receivedPacket = inPacket.getData();
                byte[] writePacket = new byte[247];



                fileInPackets.add(receivedPacket);

                //System.out.println(new String(inPacket.getData()));
                byte[] packetHead = new byte[8];
                System.arraycopy(inPacket.getData() , 0 , packetHead , 0 , 8);
                System.arraycopy(receivedPacket , 8 , writePacket , 0 , 247);

                //fileReceived.write(writePacket);

                ByteBuffer buffer = ByteBuffer.allocate(8);
                buffer.put(packetHead);
                buffer.rewind();


                System.out.println("Head: " + (fileInPackets.size()-1) + " | " + buffer.getInt() + " Index");




                String isEnd = new String(inPacket.getData()).trim();

                System.out.println(isEnd);

                if(isEnd.equalsIgnoreCase("end")){
                    break;
                }

                inPacket.setData(new byte[255]);
            }

            fileReceived.flush();
            fileReceived.close();
            clientSocket.close();

        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
