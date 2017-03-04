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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Server for 'The Board' mode
 *
 * @author cheikh
 */
public class TheBoardServer extends Thread {

    protected static int listeningPort = 44444;
    public static boolean DEBUG_MODE = true;
    private static final List<String> USERNAMES = Collections.synchronizedList(new ArrayList<String>());
    private static final List<ObjectOutputStream> CLIENTS_STREAMS = Collections
            .synchronizedList(new ArrayList<ObjectOutputStream>());

    private ServerSocket serverSocket;

    public TheBoardServer(int port, boolean debug) {
        super("TheBoardServer");
        DEBUG_MODE = debug;
        listeningPort = port;

    }

    public void disconnect() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(TheBoardServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(listeningPort);
            while (true) {

                if (DEBUG_MODE) {
                    System.out.println(String.format("(TheBoardServer) : Listening on port %1$d for connection...", listeningPort));
                }
                // Accept connection
                Socket socket;
                try {
                    socket = serverSocket.accept();
                } catch (SocketException ex) {
                    Logger.getLogger(TheBoardServer.class.getName()).log(Level.FINE, "Server socket closed.", ex);
                    break;
                }

                if (DEBUG_MODE) {
                    System.out.println(String.format(
                            "(TheBoardServer) : Connection accepted. Creating thread to deal with client [%1$s]..",
                            socket.getInetAddress().getCanonicalHostName()));
                }

                // Create a thread to deal with the client
                BoardServerThread t = new BoardServerThread(socket);

                // Start thread
                t.start();
            }
        } catch (IOException e) {
            Logger.getLogger(TheBoardServer.class.getName()).log(Level.SEVERE,
                    String.format("Could not listen on port %1$d", listeningPort), e);
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(TheBoardServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Defines the server-side operations for 'The Board' mode. AFter getting
     * the username from the client, the server loops and waits for drawing info
     * to be sent by the client. That info is then broadcasted to all the
     * clients.
     *
     * @author cheikh
     */
    private static class BoardServerThread extends Thread {

        private Socket clientSocket = null;
        private String clientUsername;

        public BoardServerThread(Socket socket) {
            super("BoardServerThread");
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (
                    // Gets the socket input and output streams
                    ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());) {
                boolean disconnect = false;
                // Connection was succesful, add output stream to the list
                CLIENTS_STREAMS.add(out);
                
                SocketPacket received;

                // Wait for the username to be sent
                if (DEBUG_MODE) {
                    System.out.println(String.format("Waiting for username (client : %1$s)..",
                            clientSocket.getInetAddress().getCanonicalHostName()));
                }

                while ((received = (SocketPacket) in.readObject()) != null) {

                    if (received.getType().equals(PacketType.USERNAME)) {
                        // String was passed : it's the username
                        clientUsername = (String) received.getMsg();
                        if (clientUsername.equals("") || clientUsername == null) {
                            disconnect = true;
                            break;
                        }

                        synchronized (USERNAMES) {
                            if (!USERNAMES.contains(clientUsername)) {
                                // If the username is available, add it to the list
                                USERNAMES.add(clientUsername);

                                if (DEBUG_MODE) {
                                    System.out.println(String.format(" Received username : '%1$s'. Added to the list.",
                                            clientUsername));
                                }
                                break;
                            } else {
                                String msg = "Username is already selected. Choose another.";

                                if (DEBUG_MODE) {
                                    System.out.println(String.format(" Received username : '%1$s'. Already selected.",
                                            clientUsername));
                                }

                                SocketPacket packet = new SocketPacket(PacketType.ALERT, msg);
                                out.writeObject(packet);
                            }
                        }
                    } else if (received.getType().equals(PacketType.DISCONNECT)) {
                        CLIENTS_STREAMS.remove(out);
                        disconnect = true;
                        break;
                    }
                }

                if (disconnect) {
                    if (DEBUG_MODE) {
                        System.out.println(String.format("Client is disconnected (No username. Socket closed."));
                    }
                    clientSocket.close();
                    return;
                }

                // New client's username has been added, broadcast the updated list 
                SocketPacket usernamesList = new SocketPacket(PacketType.LIST, USERNAMES);
                broadcast(usernamesList);

                if (DEBUG_MODE) {
                    System.out.println(String.format(" List of usernames sent to client '%1$s'.", clientUsername));
                    System.out.println(String.format("  Getting drawing input from client '%1$s' and broadcasting it..", clientUsername));
                }

                // Get draw inputs and broadcast them to all clients. 
                while ((received = (SocketPacket) in.readObject()) != null) {
                    if (received.getType().equals(PacketType.DRAW_INPUT)) {
                        // Send it to all the other clients
                        broadcast(received);

                        /*
                        if (DEBUG_MODE) {
                            System.out.println("  Drawing input received from '" + clientUsername + "' and passed to all the clients");
                        }
                         */
                    } else if (received.getType().equals(PacketType.DISCONNECT)) {
                        // The clients wants to disconnect. Acknowledge it by removing him from current users. 
                        USERNAMES.remove(clientUsername);
                        CLIENTS_STREAMS.remove(out);

                        // Send updated list to the other clients before closing out the socket.
                        SocketPacket newList = new SocketPacket(PacketType.LIST, USERNAMES);
                        broadcast(newList);

                        break;

                    }
                }
                if (DEBUG_MODE) {
                    System.out.println(String.format("Client '%1$s' has disconnected. Socket closed.", clientUsername));
                }
                clientSocket.close();

            } catch (IOException e) {
                Logger.getLogger(BoardServerThread.class.getName()).log(Level.SEVERE, null, e);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(TheBoardServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /**
         * Sends a SocketPacket to all the connected clients
         *
         * @param packet
         */
        private void broadcast(SocketPacket packet) {
            synchronized (CLIENTS_STREAMS) {
                Iterator<ObjectOutputStream> i = CLIENTS_STREAMS.iterator();
                ObjectOutputStream output;
                while (i.hasNext()) {
                    try {
                        output = i.next();
                        output.writeObject(packet);
                        output.reset();
                    } catch (IOException ex) {
                        Logger.getLogger(TheBoardServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

}
