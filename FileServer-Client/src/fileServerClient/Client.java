package fileServerClient;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    private int packetsToReceive;

    private String fileExtension;

    private FilePacket[] filePackets;

    ArrayList fileInPackets = new ArrayList<FilePacket>();
    
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

            System.out.println("Ingrese el PATH del recurso a solicitar: ");
            buffer = userInput.nextLine().getBytes();//"Ping!".getBytes();

            System.out.println("Ingrese el nombre que tendrá el archivo");
            String fileName = userInput.nextLine();

            clientPacket = new DatagramPacket(buffer , buffer.length , InetAddress.getByName(serverAddress) , serverPort);

            clientSocket.send(clientPacket);

            //Packet de respuesta
            DatagramPacket inPacket = new DatagramPacket(new byte[255] , 255);

            FileOutputStream fileReceived = new FileOutputStream("C:\\Users\\lamar\\OneDrive\\Documentos\\NetBeansProjects\\FileServer\\FileServer-Client\\received.jpg");



            //Establece si el server ya mandó la información del archivo.
            boolean serverHeader = false;
            String serverHeaderInfo = "";

            while (true){

                //Comienza a escuchar para recibir lo solicitado
                clientSocket.receive(inPacket);

                if(!serverHeader){
                    serverHeaderInfo = new String(inPacket.getData()).trim();

                    packetsToReceive = Integer.parseInt(serverHeaderInfo.substring(0 , serverHeaderInfo.indexOf("|")));
                    fileExtension = serverHeaderInfo.substring(serverHeaderInfo.indexOf("|") + 1 , serverHeaderInfo.length());

                    System.out.println(serverHeaderInfo);
                    System.out.println(packetsToReceive + " | " + fileExtension);

                    //SettingUp the packets cache
                    filePackets = new FilePacket[packetsToReceive];

                    serverHeader = true;
                }

                byte[] receivedPacket = inPacket.getData();
                byte[] fileDataPacket = new byte[247];



                //fileInPackets.add(receivedPacket);

                //System.out.println(new String(inPacket.getData()));
                byte[] packetHead = new byte[8];
                System.arraycopy(inPacket.getData() , 0 , packetHead , 0 , 8);
                System.arraycopy(receivedPacket , 8 , fileDataPacket , 0 , 247);

                //fileReceived.write(writePacket);

                ByteBuffer buffer = ByteBuffer.allocate(8);
                buffer.put(packetHead);
                buffer.rewind();
                int packetIndex = buffer.getInt();




                //System.out.println("Head: " + (fileInPackets.size()-1) + " | " + buffer.getInt() + " Index");




                String isEnd = new String(inPacket.getData()).trim();

                //System.out.println(isEnd);

                if(isEnd.equalsIgnoreCase("end")){
                    break;
                }

                if(!(packetIndex > packetsToReceive)) {
                    //Creates a new FilePacket
                    FilePacket filePacket = new FilePacket(packetIndex, fileDataPacket);

                    //Add packets into the buffer
                    //fileInPackets.add(filePacket);
                    filePackets[filePacket.getFilePacketNumber()] = filePacket;
                }



                inPacket.setData(new byte[255]);
            }

            fileReceived.flush();
            fileReceived.close();

            checkFileIntegrity();

        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void checkFileIntegrity(){
        //Collections.sort(fileInPackets);

        ArrayList<Integer> packetsMissied = new ArrayList<Integer>();


        for(int i = 0 ; i < filePackets.length ; i++){
            if(filePackets[i] == null){
                packetsMissied.add(i);
            }
        }
        System.out.println("Missing " + packetsMissied.size() + " Packet(s)!");
        System.out.println("Missed Packets: ");
        for(Integer packetIndex : packetsMissied){
            System.out.println(packetIndex);
        }
/*
        for(Object file : fileInPackets){
            System.out.println(((FilePacket) file).getFilePacketNumber());
        }
 */


    }

}
