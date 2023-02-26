package fileServerServer;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    //Server Config
    private int PORT = 4444;
    private String host = "localhost";
    private DatagramSocket serverSocket;
    private DatagramPacket serverPacket;
    private byte[] buffer;
    private ExecutorService executorService;
    
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
            executorService = Executors.newFixedThreadPool(5);
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
                    System.out.println("Attending client on from: " + serverPacket.getAddress().getHostAddress() + "\n" + "Requested: " +received.trim() + "\n");

                //Manejo del envío del archivo pedido con un hilo
                executorService.execute(new ServerProtocol(serverPacket));

                buffer = new byte[255];

                //OLD SINGLE-THREAD SOLUTION
                //sendResponse(serverPacket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendResponse(DatagramPacket data){
        int clientPort = data.getPort();
        String clientAddress = data.getAddress().getHostAddress();
        String dataReceived = new String(data.getData()).trim();

        File requestedFile = new File(dataReceived);

        byte[] packetHead = new byte[8];
        byte[] filePacket = new byte[247];

        ArrayList<byte[]> fileInPackets = new ArrayList<byte[]>();

        int filePackets = 0;

        try{
            FileInputStream fileIn = new FileInputStream(requestedFile);

            filePackets = Math.ceilDiv(fileIn.readAllBytes().length , 247);

            fileIn.getChannel().position(0);

            int packetCount = 0;

            while(packetCount <= filePackets){

                packetHead = ByteBuffer.allocate(8).putInt(packetCount).array();

                fileIn.read(filePacket);

                byte[] packetToSend = new byte[255];
                System.arraycopy(packetHead , 0 , packetToSend , 0 , 8);
                System.arraycopy(filePacket , 0 , packetToSend , 8 , 247);

                ByteBuffer buffer = ByteBuffer.allocate(8);
                buffer.put(packetHead);
                buffer.rewind();

                System.out.println(buffer.getInt());

                serverSocket.send(new DatagramPacket(packetToSend , 255 , InetAddress.getByName(clientAddress), clientPort));

                filePacket = new byte[247];
                packetCount++;
            }

        }catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.arraycopy("end".getBytes() , 0 , buffer , 0 , 3);

        try {
            serverSocket.send(new DatagramPacket(buffer , 255 , InetAddress.getByName(clientAddress), clientPort));
            System.out.println("File transfer finished");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
