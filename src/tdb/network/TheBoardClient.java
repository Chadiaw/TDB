/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdb.network;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import tdb.DrawCommand;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextArea;
import tdb.TheDrawingBoard;

/**
 * Defines the client-side operations for 'The Board' game mode. 
 * @author cheikh
 */
public class TheBoardClient {

    private static Socket socket = null;
    private static ObjectOutputStream output = null;
    private static ObjectInputStream input = null;
    private static int port;
    private static GraphicsContext gc;
    private static ObservableList<String> users;
    private boolean userAck = false;
    private String userAckMessage = null;
    private TextArea chatData;

    public TheBoardClient(String hostname, GraphicsContext gc, ObservableList<String> usersList, TextArea chatData) throws UnknownHostException, IOException {

        port = TheBoardServer.listeningPort;
        socket = new Socket(hostname, port);
        output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();
        input = new ObjectInputStream(socket.getInputStream());
        users = usersList;
        this.chatData = chatData;
        this.gc = gc;

    }

    /**
     * Starts the receiving thread. Receives different types of packets from server
     * and acts accordingly (Drawing commands, usernames list, chat messages, etc.).
     * Note : it is necessary to use the Platform.runLater construction when updating GUI components
     */
    public void startReceiveThread() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SocketPacket received = null;
                    while (true) {
                        received = (SocketPacket) input.readObject();
       
                        switch (received.getType()) {
                            case DRAW_INPUT:
                                // Execute the draw command with the current GraphicsContext (gc)
                                DrawCommand command = (DrawCommand) received.getObject();
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        synchronized(gc) {
                                            command.doIt(gc);
                                        }
                                    }
                                }
                                );  break;
                            case LIST:
                                // List of usernames, update it. 
                                List<String> list = (List<String>) received.getObject();
                                for (String user : list) {
                                    System.out.print(user + " | ");
                                }   Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        users.clear();
                                        users.addAll(list);
                                    }
                                }
                                );  break;
                            case USERNAME_ACK:
                                userAck = (boolean) received.getObject();
                                userAckMessage = received.getMsg();
                                break;
                            case MESSAGE:
                                String message = (String) received.getObject();
                                chatData.appendText("\n" + message);
                                break;
                            default:
                                break;
                        }
                    }
                } catch (EOFException e) {
                    Logger.getLogger(TheBoardClient.class.getName()).log(Level.FINE, "User succesfully disconnected", e);
                } catch (IOException ex) {
                    Logger.getLogger(TheBoardClient.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(TheBoardClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    /**
     * Send draw input to the server.
     *
     * @param command : draw command.
     */
    public void sendCommand(DrawCommand command) {
        if (socket != null && input != null && output != null) {
            try {
                SocketPacket packet = new SocketPacket(PacketType.DRAW_INPUT, command);
                output.writeObject(packet);
            } catch (IOException ex) {
                Logger.getLogger(TheBoardClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Send username to the server.
     *
     * @param username
     */
    public void sendUsername(String username) {
        if (socket != null && input != null && output != null) {
            try {
                SocketPacket packet = new SocketPacket(PacketType.USERNAME, username);
                output.writeObject(packet);
            } catch (IOException ex) {
                Logger.getLogger(TheBoardClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void sendMessage(String message) {
        if (socket != null && input != null && output != null) {
            try {
                SocketPacket packet = new SocketPacket(PacketType.MESSAGE, message);
                output.writeObject(packet);
            } catch(IOException ex) {
                Logger.getLogger(TheBoardClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void disconnect() {
        if (socket != null && input != null && output != null) {
            try {
                // Notify server that we are disconnecting. Server is in charge of closing the socket. 
                SocketPacket packet = new SocketPacket(PacketType.DISCONNECT, null);
                output.writeObject(packet);
                TheDrawingBoard.setBoardClient(null);
            } catch (IOException ex) {
                Logger.getLogger(TheBoardClient.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public boolean isUserAck() {
        return userAck;
    }

    public String getUserAckMessage() {
        return userAckMessage;
    }
    
    
    

}
