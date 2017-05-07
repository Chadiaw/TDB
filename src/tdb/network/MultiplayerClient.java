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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import tdb.model.DrawCommand;
import tdb.TheDrawingBoard;
import tdb.Utilities;
import tdb.ViewManager;
import tdb.model.ClearDrawing;
import tdb.model.MultiplayerGameState;
import tdb.model.Player;
import tdb.model.PlayerTableItem;

/**
 *
 * @author cheikh
 */
public class MultiplayerClient {
    // Socket/Network variables
    private Socket socket;
    private ObjectOutputStream output = null;
    private ObjectInputStream input = null;
    private boolean userAck;    // says whether username has been ack or not
    private final boolean isHostClient;     // is this client hosting the game
    
    // UI variables
    private GraphicsContext gc;
    private TextArea chatData;
    private Label currentWord, timeLeft;
    
    // Game variables
    private MultiplayerGameState gameState;
    private ObservableList<PlayerTableItem> playersList; 
    private String userAckMessage = ""; // contains username or the reason why it's not valid.
    private final int START_TIME = 5;
    private Integer timeLeftValue, startTime = START_TIME;
    private SimpleBooleanProperty turnToDraw;
    private Timeline roundTimeline;
    
     
    
    public MultiplayerClient(String hostname) throws UnknownHostException, IOException {
        socket = new Socket(hostname, MultiplayerServer.listeningPort);
        output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();
        input = new ObjectInputStream(socket.getInputStream());
        isHostClient = false;
    }
    
    public MultiplayerClient() throws UnknownHostException, IOException {
        socket = new Socket("localhost", MultiplayerServer.listeningPort);
        output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();
        input = new ObjectInputStream(socket.getInputStream());
        isHostClient = true;
    }

    public boolean isHostClient() {
        return isHostClient;
    }

    
    public void setGc(GraphicsContext gc) {
        this.gc = gc;
    }

    public void setChatData(TextArea chatData) {
        this.chatData = chatData;
    }

    public void setCurrentWord(Label currentWord) {
        this.currentWord = currentWord;
    }

    public void setTimeLeft(Label timeLeft) {
        this.timeLeft = timeLeft;
    }

    public void setGameState(MultiplayerGameState gameState) {
        this.gameState = gameState;
        //this.playersList = FXCollections.observableArrayList(gameState.getPlayers());
    }

    public MultiplayerGameState getGameState() {
        return gameState;
    }
    
    public void setPlayersList(ObservableList<PlayerTableItem> playersList) {
        this.playersList = playersList;
    }

    public void setTurnToDraw(SimpleBooleanProperty turnToDraw) {
        this.turnToDraw = turnToDraw;
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
                Logger.getLogger(MultiplayerClient.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(MultiplayerClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void sendMessage(String message) {
        if (socket != null && input != null && output != null) {
            try {
                SocketPacket packet = new SocketPacket(PacketType.MESSAGE, message + "\n");
                output.writeObject(packet);
            } catch (IOException ex) {
                Logger.getLogger(MultiplayerClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void disconnect() {
        if (socket != null && input != null && output != null) {
            try {
                // Notify server that we are disconnecting. Server is in charge of closing the socket. 
                SocketPacket packet = new SocketPacket(PacketType.DISCONNECT, null);
                output.writeObject(packet);
                TheDrawingBoard.setMultiplayerClient(null);
                Utilities.goToHomeScreen(chatData, "MultiplayerLobbyController");
            } catch (IOException ex) {
                Logger.getLogger(MultiplayerClient.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
    
    public boolean isUserAck() {
        return userAck;
    }

    public String getUserAckMessage() {
        return userAckMessage;
    }

    public void updateGameState() {
        if (socket != null && input != null && output != null) {
            try {
                SocketPacket packet = new SocketPacket(PacketType.GAMESTATE_UPDATE, gameState);
                output.writeObject(packet);
            } catch (IOException ex) {
                Logger.getLogger(MultiplayerClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void sendStartSignal() {
        if (socket != null && input != null && output != null) {
            try {
                SocketPacket packet = new SocketPacket(PacketType.GAME_START, true);
                output.writeObject(packet);
            } catch (IOException ex) {
                Logger.getLogger(MultiplayerClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void sendReady() {
        if (socket != null && input != null && output != null) {
            try {
                SocketPacket packet = new SocketPacket(PacketType.READY, true);
                output.writeObject(packet);
            } catch (IOException ex) {
                Logger.getLogger(MultiplayerClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void initRoundTimeline() {
        timeLeftValue = gameState.getDrawingTime();
        roundTimeline = new Timeline();
        roundTimeline.setCycleCount(Timeline.INDEFINITE);
        roundTimeline.getKeyFrames().add(
                        new KeyFrame(Duration.seconds(1),
                                new EventHandler() {
                            public void handle(Event event) {
                                timeLeftValue--;
                                if (timeLeftValue <= 0) {
                                    roundTimeline.stop();
                                    timeLeft.setText("N/A");
                                    sendTimeUpEvent();
                                    return;
                                }
                                timeLeft.setText(Integer.toString(timeLeftValue));

                            }
                        }));
    }
    
    private void sendTimeUpEvent() {
        if (socket != null && input != null && output != null) {
            try {
                SocketPacket packet = new SocketPacket(PacketType.TIME_UP, true);
                output.writeObject(packet);
            } catch (IOException ex) {
                Logger.getLogger(MultiplayerClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
    }
    
    private void executeTimeUpEvent() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                roundTimeline.stop();
                timeLeft.setText("N/A");
                chatData.appendText("Time up ! Next round starting soon...\n\n");
                sendReady(); // Ready for next round
            }
        }
        );
    } 
    
    private void roundWinner(String winner) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                roundTimeline.stop();
                timeLeft.setText("N/A");
                chatData.appendText(winner + " guessed correctly and wins the round !\n\n");
                sendReady(); // Ready for next round
            }
        }
        );
    }
    
    private void startGame() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                chatData.appendText("Game will start with the following settings... \n");
                chatData.appendText("Drawing time : " + gameState.getDrawingTime() + ". Winning score : " 
                        + gameState.getWinningScore() + ".\n");
                
                Timeline timeline = new Timeline();
                timeline.setCycleCount(Timeline.INDEFINITE);
                timeline.getKeyFrames().add(
                        new KeyFrame(Duration.seconds(1),
                                new EventHandler() {
                            public void handle(Event event) {
                                startTime--;
                                chatData.appendText("The game is starting in " + startTime.toString() + "...\n");
                                if (startTime <= 0) {
                                    timeline.stop();
                                    startTime = START_TIME;
                                    Stage stage = (Stage) chatData.getScene().getWindow();
                                    stage.close();
                                    try {
                                        ViewManager.getInstance().openView("MultiplayerView.fxml", "The Drawing Board - Multiplayer", StageStyle.DECORATED);
                                    } catch (IOException ex) {
                                        Logger.getLogger(MultiplayerClient.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }

                            }
                        }));
                timeline.playFromStart();
                
            }
        }
        );
        
    }
    
    public void startReceiveThread() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SocketPacket received = null;
                    while (true) {
                        received = (SocketPacket) input.readObject();

                        switch (received.getType()) {
                            case ALERT:
                                break;
                            case DRAW_INPUT:
                                // Execute the draw command with the current GraphicsContext (gc)
                                DrawCommand command = (DrawCommand) received.getObject();
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        command.doIt(gc);
                                    }
                                }
                                );
                                break;
                            case END_GAME:
                                chatData.appendText("Game will now end...\n");
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(MultiplayerClient.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        disconnect();
                                        if (isHostClient) {
                                            TheDrawingBoard.disconnectMultiplayerServer();
                                        }
                                    }
                                }
                                );
                                break;
                            case GAMESTATE_UPDATE:
                                gameState = (MultiplayerGameState) received.getObject();
                                break;
                            case NEXT_ROUND:
                                gameState = (MultiplayerGameState) received.getObject();
                                if(gameState.hasGameStarted()) {
                                    // Display current artist
                                    chatData.appendText("Current artist is : " + gameState.getArtist().getName()
                                            + ".\n");
                                    // Update turnToDraw value
                                    if(gameState.getArtist().getName().equals(userAckMessage)) {
                                        // This client is drawing. 
                                        turnToDraw.set(true);
                                    } else {
                                        turnToDraw.set(false);
                                    }
                                    
                                    // Change current word displayed
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            new ClearDrawing().doIt(gc);
                                            if (turnToDraw.get()) {
                                                currentWord.setText(gameState.getCurrentWord());
                                            } else {
                                                String displayed = gameState.getCurrentWord().toLowerCase().replaceAll(
                                                        "([a-z])", "* ");
                                                currentWord.setText(displayed);
                                            }
                                        }
                                    });
                                    
                                    // Update time (init timeline and start it)
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            initRoundTimeline();
                                            roundTimeline.playFromStart();
                                        }
                                    });
                                }
                                break;
                            case LIST:
                                ArrayList<Player> list = (ArrayList<Player>) received.getObject();
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        playersList.clear();
                                        for(Player p:list) {
                                            playersList.add(new PlayerTableItem(p));
                                        }
                                    }
                                });
                                break;
                            case USERNAME_ACK:
                                userAck = (boolean) received.getObject();
                                userAckMessage = received.getMsg();
                                break;
                            case MESSAGE:
                                String message = (String) received.getObject();
                                chatData.appendText(message);
                                break;
                            case FOUND_WORD:
                                roundWinner(received.getMsg());
                                break;
                            case TIME_UP:
                                executeTimeUpEvent();
                                break;
                            case GAME_START:
                                startGame();
                                break;
                            case SERVER_DISCONNECT:
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(!isHostClient) {
                                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                            alert.setTitle("Host disconnected");
                                            alert.setHeaderText(null);
                                            alert.setContentText("Host disconnected. You will now be returned to home screen.");
                                            Utilities.initDialogOwner(alert, chatData);
                                            alert.showAndWait();
                                        }
                                        Utilities.goToHomeScreen(chatData, "MultiplayerClient");
                                        TheDrawingBoard.setMultiplayerClient(null);
                                    }
                                }
                                );
                                break;
                            default:
                                break;
                        }
                    }
                } catch (EOFException e) {
                    Logger.getLogger(TheBoardClient.class.getName()).log(Level.FINE, "User succesfully disconnected", e);
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(TheBoardClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }
}
