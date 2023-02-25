package fileServerClient;

import java.io.*;
import java.net.*;
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
            while (true){

                //Comienza a escuchar para recibir lo solicitado
                clientSocket.receive(inPacket);

                try(FileOutputStream fileReceived = new FileOutputStream("C:\\Users\\lamar\\OneDrive\\Documentos\\NetBeansProjects\\FileServer\\FileServer-Client\\received.txt")){
                    fileReceived.write(inPacket.getData());
                }

                System.out.println(new String(inPacket.getData()));
            }


        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
