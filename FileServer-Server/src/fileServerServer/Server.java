package fileServerServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server {
    
    private final int PORT = 4444;
    
    public static void main(String[] args){
        
        Server server = new Server();
        
        server.executeServer();
    }
    
    public void executeServer(){
                try {
            DatagramSocket serverSocket = new DatagramSocket(PORT , InetAddress.getByName("localhost"));
	    DatagramPacket packet = new DatagramPacket(new byte[255] , 255);
            System.out.println("Server ready!");
            Socket clientSocket = null;

            //Servicio de pool de hilos
            Executor service = Executors.newFixedThreadPool(3);
            
            //Acepta todas las conexiones asignando la conexión a un protocolo kockknock nuevo
            while (true){
                serverSocket.receive(packet);
                
//                service.execute(new serverProtocol(clientSocket));
                
                System.out.println("Attending a new client on port: " + packet.getPort() + " . . .");
                
                //serverSocket.send(packet);
                packet.setLength(255);
            }

        } catch (IOException e) {
            System.err.println("Could not listen on port: " + PORT);
            System.exit(1);
        }
    }
}
