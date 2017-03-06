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
 * Server for 'The Board' mode. Defines the operations for the server, and
 * allows multiple clients to be connected.
 *
 * @author cheikh
 */
public class TheBoardServer extends Thread {

    public static int listeningPort = 4444; // This is default listening port.
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

    public TheBoardServer(boolean debug) {
        super("TheBoardServer");
        DEBUG_MODE = debug;
    }

    /**
     * Disconnect the server (Close the listening socket).
     */
    public void disconnect() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(TheBoardServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /**
     * Creates the server socket, accepts connections, creates threads to deal
     * with each client.
     */
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
     * clients. Chat messages are also to be broadcasted (not yet implemented).
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

                // 1st loop (Identification) : Wait for a valid username to be sent by the client.
                while ((received = (SocketPacket) in.readObject()) != null) {

                    if (received.getType().equals(PacketType.USERNAME)) {
                        // String was passed : it's the username
                        clientUsername = (String) received.getMsg();

                        synchronized (USERNAMES) {
                            if (clientUsername.equals("") || clientUsername == null || USERNAMES.contains(clientUsername)) {
                                if (USERNAMES.contains(clientUsername)) {
                                    String msg = "Username is already selected. Choose another.";

                                    if (DEBUG_MODE) {
                                        System.out.println(String.format(" Received username : '%1$s'. Already selected.",
                                                clientUsername));
                                    }

                                    // Send a 'USERNAME_ACK' with 'false' as value -> means given username was not accepted
                                    SocketPacket packet = new SocketPacket(PacketType.USERNAME_ACK, false, msg);
                                    out.writeObject(packet);
                                } else {
                                    String msg = "Username is not valid (Blank usernames are not accepted). Choose another.";
                                    SocketPacket packet = new SocketPacket(PacketType.USERNAME_ACK, false, msg);
                                    out.writeObject(packet);
                                }
                            } else {
                                // If the username is available, add it to the list
                                USERNAMES.add(clientUsername);

                                // Send packet to acknowledge the given username
                                SocketPacket packet = new SocketPacket(PacketType.USERNAME_ACK, true);
                                out.writeObject(packet);

                                if (DEBUG_MODE) {
                                    System.out.println(String.format(" Received username : '%1$s'. Added to the list.",
                                            clientUsername));
                                }
                                break;
                            }
                        }
                    } else if (received.getType().equals(PacketType.DISCONNECT)) {
                        // Client wants to disconnect. Stop the identification loop
                        CLIENTS_STREAMS.remove(out);
                        disconnect = true;
                        break;
                    }
                }

                if (disconnect) {
                    if (DEBUG_MODE) {
                        System.out.println(String.format("Client is disconnected (No username). Socket closed."));
                    }
                    clientSocket.close();
                    return;
                }

                // New client's username has been added, broadcast the updated list. 
                SocketPacket usernamesList = new SocketPacket(PacketType.LIST, USERNAMES);
                broadcast(usernamesList);

                if (DEBUG_MODE) {
                    System.out.println(String.format(" List of usernames sent to client '%1$s'.", clientUsername));
                    System.out.println(String.format("  Getting input from client '%1$s' and broadcasting it..", clientUsername));
                }

                OUTER:
                // 2nd loop : receive packets from clients and process them accordingly.
                while (true) {
                    received = (SocketPacket) in.readObject();

                    switch (received.getType()) {
                        case DISCONNECT:
                            // The client wants to disconnect. Acknowledge it by removing him from current users.
                            USERNAMES.remove(clientUsername);
                            CLIENTS_STREAMS.remove(out);
                            // Send updated list to the other clients before closing out the socket.
                            SocketPacket newList = new SocketPacket(PacketType.LIST, USERNAMES);
                            broadcast(newList);
                            break OUTER;
                        case MESSAGE:
                            broadcast(received);
                            break;
                        case DRAW_INPUT:
                            // Forbid everyone else from drawing while this client is (PANEL IS BUSY)
                            SocketPacket busy = new SocketPacket(PacketType.CLEAR_TO_DRAW, false);
                            broadcastExcept(busy, out);
                            // Send to everyone the actual drawing input.
                            broadcast(received);
                            break;
                        case CLEAR_TO_DRAW:
                            // Client sent a clear to draw, broadcast it.
                            broadcast(received);
                            break;
                        default:
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

        /**
         * Sends a SocketPacket to every client except the one specified.
         *
         * @param packet packet to send
         * @param exception client to ignore
         */
        private void broadcastExcept(SocketPacket packet, ObjectOutputStream exception) {
            synchronized (CLIENTS_STREAMS) {
                Iterator<ObjectOutputStream> i = CLIENTS_STREAMS.iterator();
                ObjectOutputStream output;
                while (i.hasNext()) {
                    try {
                        output = i.next();
                        if (output.equals(exception)) {
                            continue;
                        }
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
