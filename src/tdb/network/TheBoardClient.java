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
import javafx.collections.ObservableList;
import tdb.DrawCommand;
import javafx.scene.canvas.GraphicsContext;
import tdb.TheDrawingBoard;

/**
 *
 * @author cheikh
 */
public class TheBoardClient {

    private static Socket socket = null;
    private static ObjectOutputStream output = null;
    private static ObjectInputStream input = null;
    private static int port;
    private static GraphicsContext gc;
    private static ObservableList<String> users;

    public TheBoardClient(String hostname, GraphicsContext gc, ObservableList<String> usersList) throws UnknownHostException, IOException {

        port = TheBoardServer.listeningPort;
        socket = new Socket(hostname, port);
        output = new ObjectOutputStream(socket.getOutputStream());
        input = new ObjectInputStream(socket.getInputStream());
        users = usersList;
        this.gc = gc;

    }

    /**
     * Starts the receiving thread. Receives drawing commands from the server
     * and executes them with the current GraphicsContext
     */
    public void startReceiveThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SocketPacket received;
                    while ((received = (SocketPacket) input.readObject()) != null) {
                        // If the received packet is a draw input, execute it. 
                        if (received.getType().equals(PacketType.DRAW_INPUT)) {
                            DrawCommand command = (DrawCommand) received.getObject();
                            command.doIt(gc);
                        } else if (received.getType().equals(PacketType.LIST)) {
                            // It could be the list of usernames too, in that case just update it.
                            List<String> list = (List<String>) received.getObject();
                            users.clear();
                            users.addAll(list);
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

    public List<String> getUsers() {
        return users;
    }

    public void disconnect() {
        if (socket != null && input != null && output != null) {
            try {
                // Notify server that we are disconnecting. Server is in charge of closing the socket. 
                SocketPacket packet = new SocketPacket(PacketType.DISCONNECT, null);
                output.writeObject(packet);
                TheDrawingBoard.setBoardClient(null);
            }
            catch (IOException ex ) {
                Logger.getLogger(TheBoardClient.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

}
