package fileServerServer;

import java.io.*;
import java.net.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server {

    //Server Config
    private int PORT = 4444;
    private String host = "localhost";
    private DatagramSocket serverSocket;
    private DatagramPacket serverPacket;
    private byte[] buffer;

    
    public static void main(String[] args){
        
        Server server = new Server();
        
        server.executeServer();
    }
    
    public void executeServer(){

        //Inicialización del servidor
        try {
            serverSocket = new DatagramSocket(PORT, InetAddress.getByName(host));
            buffer = new byte[255]; // Buffer de lectura de bytes.
            serverPacket = new DatagramPacket(buffer , buffer.length);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }


        //Ciclo infinito de recepción
        while(true) {

            try {
                serverSocket.receive(serverPacket);

                //Convierte los bytes[] recibidos a String
                String received = new String(serverPacket.getData());

                //Imprime lo que reciba en el serverPacket
                if(received != null)
                    System.out.println(received.trim());

                sendResponse(serverPacket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendResponse(DatagramPacket data){
        int clientPort = data.getPort();
        String clientAddress = data.getAddress().toString();
        String dataReceived = new String(data.getData()).trim();

        File requestedFile = new File(dataReceived);

        buffer = new byte[255];

        try {
            FileInputStream fileStream = new FileInputStream(requestedFile);

            fileStream.read(buffer);

            fileStream.close();

            serverPacket.setData(buffer);

            serverSocket.send(serverPacket);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
