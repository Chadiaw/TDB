/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdb;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.stage.WindowEvent;
import tdb.model.MultiplayerGameState;
import tdb.model.PlayerTableItem;
import tdb.network.MultiplayerClient;

/**
 * FXML Controller class
 *
 * @author cheikh
 */
public class MultiplayerLobbyController implements Initializable {

    @FXML
    private Label gameLobbyLabel;
    @FXML 
    private Label playersLabel;
    @FXML
    private Label drawingTimeLabel;
    @FXML
    private Label winningScore;
    @FXML 
    private Button startButton;
    @FXML
    private TextArea chatOutput;
    @FXML 
    private TextField chatInput;
    @FXML
    private TextField timeField;
    @FXML
    private CheckBox randomCheck;
    @FXML
    private TextField scoreField;
    @FXML 
    private ListView<PlayerTableItem> playersList = new ListView<PlayerTableItem>();
    ObservableList<PlayerTableItem> items = FXCollections.observableArrayList();
    
    private MultiplayerGameState gameState;
    private MultiplayerClient client;
    private String username;
    
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        client = TheDrawingBoard.getMultiplayerClient();
        client.startReceiveThread();
        
        if(client.isHostClient()) {
            timeField.setDisable(false);
            timeField.setPromptText("Time");
            randomCheck.setDisable(false);
            randomCheck.setSelected(true);
            randomCheck.setTooltip(new Tooltip("Pick artist randomly"));
            scoreField.setDisable(false);
            scoreField.setText("5");
            startButton.setDisable(false);
        } else {
            timeField.setDisable(true);
            randomCheck.setDisable(true);
            scoreField.setDisable(true);
            startButton.setDisable(true);
        }
            
        client.setChatData(chatOutput);
        
        
        while(!client.isUserAck()) {
            // Ask for username
            Dialog dialog = new TextInputDialog();
            dialog.setTitle("Username selection");
            dialog.setHeaderText(null);
            dialog.setContentText("Please select a username : ");
            //Utilities.initDialogOwner(dialog, chatInput);
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                username = result.get();
                client.sendUsername(username);
                while(client.getUserAckMessage().equals("")) {
                    try {
                        // Wait for server response;
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MultiplayerLobbyController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if(!client.isUserAck()) {
                    // If username was not acknowledged, display the reason why
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Username not valid");
                    alert.setHeaderText(null);
                    alert.setContentText(client.getUserAckMessage());
                    //Utilities.initDialogOwner(alert, startButton);
                    alert.showAndWait();
                }
            } else {
                client.disconnect();
                client = null;
                TheDrawingBoard.setMultiplayerClient(null);
                Utilities.goToHomeScreen(startButton, "MultiplayerLobbyController");
                break;
            }    
        }
        
        // Update game state. Create it if this is the host's client.
        if(client.isHostClient()) {
            client.setGameState(new MultiplayerGameState(username));
            client.updateGameState();
        } 
        
        client.setPlayersList(items);
        playersList.setItems(items);
        playersList.setCellFactory(param -> new ListCell<PlayerTableItem>() {
            @Override
            protected void updateItem(PlayerTableItem item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    if(item.isHost()) {
                        setStyle("-fx-font-weight: bold");
                        setText(item.getName() + " (Host)");
                    } else {
                        setText(item.getName());
                    }
                    
                }
            }
        });
        
    }
    
    @FXML
    private void quitMode(Event dummyEvent) {
        playersList.getScene().getWindow().setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (client.isHostClient()) {
                    TheDrawingBoard.disconnectMultiplayerServer();
                }
                client.disconnect();
                TheDrawingBoard.setMultiplayerClient(null);
            }
        });
    }

    @FXML
    private void sendMessage(ActionEvent event) {
        if (client != null && !chatInput.getText().isEmpty()) {
            client.sendMessage(username + ": " + chatInput.getText());
            chatInput.clear();
        }
    }
    
    @FXML
    private void startGame(ActionEvent event) {
        if(playersList.getItems().size() < 2) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Not enough players");
            alert.setHeaderText(null);
            alert.setContentText("You need at least two players to start a game.");
            Utilities.initDialogOwner(alert, startButton);
            alert.showAndWait();
            return;
        }
        try {
          int drawingTime = Integer.parseInt(timeField.getText());
          int maxScore = Integer.parseInt(scoreField.getText());
          if(drawingTime <= 0 || drawingTime <= 0) {
              throw new NumberFormatException();
          }
          // Get game parameters and start the game
          client.getGameState().setDrawingTime(drawingTime);
          client.getGameState().setWinningScore(maxScore);
          client.getGameState().setRandomArtist(randomCheck.isSelected());
          client.updateGameState();
          client.sendStartSignal();
          
        } catch(NumberFormatException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid drawing time or winning score");
            alert.setHeaderText(null);
            alert.setContentText("Please enter a valid number in every field. ");
            Utilities.initDialogOwner(alert, startButton);
            alert.showAndWait();
        }
        
    }
    
}
