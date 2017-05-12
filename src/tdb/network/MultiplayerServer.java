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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import tdb.model.MultiplayerGameState;
import tdb.model.Player;

/**
 *
 * @author cheikh
 */
public class MultiplayerServer extends Thread {

    public static int listeningPort = 54445;
    private ServerSocket serverSocket;

    private static boolean gameStarted = false;
    private boolean hostDefined = false; 
    
    private static MultiplayerGameState sharedGameState;
    private static final List<String> USERNAMES = Collections.synchronizedList(new ArrayList<String>());
    private static int readyClients = 0;
    private static final List<ObjectOutputStream> CLIENTS_STREAMS = Collections
            .synchronizedList(new ArrayList<ObjectOutputStream>());

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
                    if(hostDefined) {
                        MultiplayerServerThread t = new MultiplayerServerThread(socket, false);
                        t.start();
                    }
                    else {
                        MultiplayerServerThread t = new MultiplayerServerThread(socket, true);
                        hostDefined = true;
                        t.start();
                    }
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

    public void sendDisconnectNotice() {
        SocketPacket notice = new SocketPacket(PacketType.SERVER_DISCONNECT, true);
        synchronized (CLIENTS_STREAMS) {
            Iterator<ObjectOutputStream> i = CLIENTS_STREAMS.iterator();
            ObjectOutputStream output;
            while (i.hasNext()) {
                try {
                    output = i.next();
                    output.writeObject(notice);
                    output.reset();
                } catch (IOException ex) {
                    Logger.getLogger(TheBoardServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
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

                // Add output stream to the list
                CLIENTS_STREAMS.add(out);
                String clientUsername = "";
                // Identification - Waiting for valid username
                while(true) {
                    SocketPacket received = (SocketPacket)in.readObject();
                    if(received.getType().equals(PacketType.USERNAME)) {
                        clientUsername = (String) received.getMsg();
                        // Acknowledge username
                        synchronized (USERNAMES) {
                            if (clientUsername.equals("") || USERNAMES.contains(clientUsername)) {
                                if (USERNAMES.contains(clientUsername)) {
                                    String msg = "Username is already selected. Choose another.";
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
                                SocketPacket packet = new SocketPacket(PacketType.USERNAME_ACK, true, clientUsername);
                                out.writeObject(packet);
                                break;
                            }
                        }
                    } else if (received.getType().equals(PacketType.DISCONNECT)) {
                        CLIENTS_STREAMS.remove(out);
                        clientSocket.close();
                        return;
                    }
                }
                
                SocketPacket connect = new SocketPacket(PacketType.MESSAGE, 
                        "Player '"+ clientUsername + "' joined the lobby.\n");
                broadcast(connect);
                
                if(sharedGameState != null) {
                    sharedGameState.addPlayer(new Player(clientUsername, false));
                    SocketPacket state = new SocketPacket(PacketType.GAMESTATE_UPDATE, sharedGameState);
                    broadcast(state);
                    SocketPacket players = new SocketPacket(PacketType.LIST, sharedGameState.getPlayers());
                    broadcast(players);
                }
                
                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                
                OUTER:
                // Phase 2 : In lobby or game
                while(true) {
                    SocketPacket received = (SocketPacket)in.readObject();
                    if(received == null)
                        break;
                    switch(received.getType()) {
                        case DRAW_INPUT:
                            broadcast(received);
                            break;
                        case GAME_START:
                            gameStarted = true;
                            //sharedGameState.nextRound();
                            //SocketPacket initState = new SocketPacket(PacketType.NEXT_ROUND, sharedGameState);
                            broadcast(received); 
                            break;
                        case READY:
                            readyClients++;
                            if(readyClients == CLIENTS_STREAMS.size()) {
                                // All the clients are ready
                                if (sharedGameState.checkWinner().equals("")) {
                                    // No winner, start next round
                                    sharedGameState.nextRound();
                                    SocketPacket nextRound = new SocketPacket(PacketType.NEXT_ROUND, sharedGameState);
                                    broadcast(nextRound);
                                    
                                    // Schedule future timeup signal
                                    if(scheduler == null || scheduler.isShutdown()) {
                                        scheduler = Executors.newScheduledThreadPool(1);
                                    }
                                    scheduler.schedule(new Runnable() {
                                        @Override
                                        public void run() {
                                            SocketPacket timeUp = new SocketPacket(PacketType.TIME_UP, true);
                                            broadcast(timeUp);
                                        }
                                    }, sharedGameState.getDrawingTime(), TimeUnit.SECONDS);
                                } else {
                                    // We have a winner
                                    String msg = "We have a winner ! Congratulations " + sharedGameState.checkWinner() + " :D\n";
                                    SocketPacket announceWinner = new SocketPacket(PacketType.MESSAGE, msg);
                                    broadcast(announceWinner);
                                    // End the game
                                    SocketPacket endGameNotice = new SocketPacket(PacketType.END_GAME, true);
                                    broadcast(endGameNotice);
                                }
                                SocketPacket list = new SocketPacket(PacketType.LIST, sharedGameState.getPlayers());
                                broadcast(list);
                                readyClients = 0;
                            }
                            break;
                        case MESSAGE:
                            broadcast(received);
                            String message = received.getMsg();
                            // If game started, check if message matches the current hidden word. 
                            if(sharedGameState != null && sharedGameState.hasGameStarted()) {
                                    String[] words = message.replaceAll("\n", "").split(" ");
                                    String lastWord = words[words.length-1].toLowerCase();
                                    if(lastWord.equals(sharedGameState.getCurrentWord().toLowerCase())) {
                                        // Stop timeout
                                        if(scheduler != null) {
                                            scheduler.shutdownNow();
                                        }
                                        // Good guess. Player wins the round
                                        SocketPacket roundWinner = new SocketPacket(PacketType.FOUND_WORD, clientUsername);
                                        broadcast(roundWinner);
                                        sharedGameState.roundWinner(clientUsername);
                                        SocketPacket updatedScores = new SocketPacket(PacketType.LIST, sharedGameState.getPlayers());
                                        broadcast(updatedScores);
                                        readyClients = 0;
                                    } 
                            }
                            break;
                        case GAMESTATE_UPDATE:
                            sharedGameState = (MultiplayerGameState) received.getObject();
                            broadcast(received);
                            SocketPacket listUpdated = new SocketPacket(PacketType.LIST, sharedGameState.getPlayers());
                            broadcast(listUpdated);
                            break;
                        case DISCONNECT:
                            sharedGameState.removePlayer(clientUsername);
                            USERNAMES.remove(clientUsername);
                            CLIENTS_STREAMS.remove(out);
                            String msg = "Player '" + clientUsername + "' disconnected.\n";
                            SocketPacket notice = new SocketPacket(PacketType.MESSAGE, msg);
                            broadcast(notice);
                            if (sharedGameState.hasGameStarted()) {
                                // If not enough players left, announce default winner and end the game.
                                if (sharedGameState.getPlayers().size() < 2) {
                                    String str = "Not enough players left. The winner by default is : "
                                            + sharedGameState.getPlayers().get(0).getName() + ".\n";
                                    SocketPacket defaultWin = new SocketPacket(PacketType.MESSAGE, str);
                                    broadcast(defaultWin);
                                    SocketPacket endGameNotice = new SocketPacket(PacketType.END_GAME, true);
                                    broadcast(endGameNotice);
                                }
                            }
                            break OUTER;
                        default:
                            break;
                    }  
                }
                clientSocket.close();
                
            } catch (IOException ex) {
                Logger.getLogger(MultiplayerServer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(MultiplayerServer.class.getName()).log(Level.SEVERE, null, ex);
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
