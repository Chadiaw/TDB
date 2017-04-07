/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdb.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cheikh
 */
public class MultiplayerServer extends Thread {

    public static int listeningPort = 4445;

    private ServerSocket serverSocket;

    private static boolean gameStarted = false;

    public MultiplayerServer() {
        super("MultiplayerServer");
    }

    /**
     * Disconnect the server (Close the listening socket).
     */
    public void disconnect() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(MultiplayerServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Creates the server socket, accepts connections, creates threads to deal
     * with clients
     */
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(listeningPort);
            while (true) {
                // Accept connection
                Socket socket;
                try {
                    socket = serverSocket.accept();
                } catch (SocketException ex) {
                    Logger.getLogger(TheBoardServer.class.getName()).log(Level.FINE, "Server socket closed.", ex);
                    break;
                }

                // Create thread if game hasn't started yet.
                if (!gameStarted) {

                } else {
                    // Game already started.
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    out.writeObject(new SocketPacket(PacketType.ALERT, "Game already started."));
                    socket.close();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MultiplayerServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static class MultiplayerServerThread extends Thread {

        private Socket clientSocket = null;
        private boolean isHost = false;
        

        public MultiplayerServerThread(Socket socket, boolean isHost) {
            clientSocket = socket;
            this.isHost = isHost;
        }

        @Override
        public void run() {
            try (
                    ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());) {

                // Identification
                while(true) {
                    SocketPacket received = (SocketPacket)in.readObject();
                    if(received.getType().equals(PacketType.USERNAME)) {
                        // Add to the names
                        // Add the outputStream to the list
                        break;
                    }
                }
                
                // Phase 2 : In lobby
                while(!gameStarted) {
                    // If host, wait to receive signal that the game started
                    if(isHost) {
                        SocketPacket received = (SocketPacket)in.readObject();
                        if (received.getType().equals(PacketType.GAME_START))  {
                            gameStarted = true;
                        } else {
                            // If message : broadcast
                        }
                        
                    } else {
                        // If not host, just block until host starts the game
                    }
                }
                
            } catch (IOException ex) {
                Logger.getLogger(MultiplayerServer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(MultiplayerServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
