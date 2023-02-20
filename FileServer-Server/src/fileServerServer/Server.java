package fileServerServer;

import java.io.IOException;
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
            ServerSocket serverSocket = null;
            serverSocket = new ServerSocket(PORT);
	    System.out.println("Server ready!");
            Socket clientSocket = null;

            //Servicio de pool de hilos
            Executor service = Executors.newFixedThreadPool(3);
            
            //Acepta todas las conexiones asignando la conexión a un protocolo kockknock nuevo
            while (true){
                clientSocket = serverSocket.accept();
                
                service.execute(new serverProtocol(clientSocket));
                
                System.out.println("Attending a new client on port: " + clientSocket.getPort() + " . . .");
            }

        } catch (IOException e) {
            System.err.println("Could not listen on port: " + PORT);
            System.exit(1);
        }
    }
}
