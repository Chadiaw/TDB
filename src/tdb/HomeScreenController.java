/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdb;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tdb.network.MultiplayerClient;
import tdb.network.MultiplayerServer;

/**
 * FXML Controller class
 *
 * @author cheikh
 */
public class HomeScreenController implements Initializable {

    @FXML
    Button soloButton;
    @FXML
    Button twoPlayersButton;
    @FXML
    Button multiplayerButton;
    @FXML
    Button theBoardButton;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        twoPlayersButton.setDisable(true);

    }

    public void goSoloMode(ActionEvent event) {
        // Close home screen
        Stage stage = (Stage) soloButton.getScene().getWindow();
        stage.close();

        // Open Draw View
        try {
            ViewManager.getInstance().openView("DrawView.fxml", "Solo Mode", StageStyle.DECORATED);
        } catch (IOException ex) {
            Logger.getLogger(HomeScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void goTwoPlayersMode(ActionEvent event) {

    }

    public void goMultiplayerMode(ActionEvent event) {
        // Ask user whether he wants to host or join a game
        Alert choiceDialog = new Alert(AlertType.CONFIRMATION);
        choiceDialog.setTitle("Multiplayer Mode");
        choiceDialog.setHeaderText("If you want to host a non-LAN game, make sure\nport forwarding"
                + " is enabled on your router (port " + MultiplayerServer.listeningPort + ").");
        choiceDialog.setContentText("Choose your option : ");

        ButtonType buttonHost = new ButtonType("Host Game");
        ButtonType buttonJoin = new ButtonType("Join Game");
        ButtonType buttonCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

        choiceDialog.getButtonTypes().setAll(buttonHost, buttonJoin, buttonCancel);

        // Style the dialog
        DialogPane dialogPane = choiceDialog.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("styles/CustomDialog.css").toExternalForm());
        dialogPane.getStyleClass().add("myDialog");
        Utilities.initDialogOwner(choiceDialog, multiplayerButton);

        Optional<ButtonType> result = choiceDialog.showAndWait();
        if (result.get() == buttonHost) {
            // ... user chose "Host a game"
            // Start the server
            TheDrawingBoard.startMultiplayerServer();
            // Start a hosting client
            try {
                MultiplayerClient client = new MultiplayerClient();
                TheDrawingBoard.setMultiplayerClient(client);
                
                // Connection succesful : close home screen and open lobby
                    Stage stage = (Stage) multiplayerButton.getScene().getWindow();
                    stage.close();
                    try {
                        ViewManager.getInstance().openView("MultiplayerLobby.fxml", "Multiplayer - Game lobby", StageStyle.DECORATED);
                    } catch (IOException ex) {
                        Logger.getLogger(HomeScreenController.class.getName()).log(Level.SEVERE, null, ex);
                    }
            } catch(IOException ex) {
                Logger.getLogger(HomeScreenController.class.getName()).log(Level.SEVERE, null, ex);
            } 
            
        } else if (result.get() == buttonJoin) {
            // ... user chose "Join a game"
            // Ask for IP and try to connect to a lobby
            TextInputDialog inputDialog = new TextInputDialog("localhost");
            inputDialog.setHeaderText(null);
            inputDialog.setTitle("Join a lobby");
            inputDialog.setContentText("Please enter the hostname or IP to join : ");
            Utilities.initDialogOwner(inputDialog, multiplayerButton);
            Optional<String> inputResult = inputDialog.showAndWait();
            if (inputResult.isPresent()) {
                String hostname = inputResult.get();
                try {
                    // Connect to the server with given hostname
                    MultiplayerClient client = new MultiplayerClient(hostname);
                    TheDrawingBoard.setMultiplayerClient(client);

                    // Connection succesful : close home screen and open lobby
                    Stage stage = (Stage) multiplayerButton.getScene().getWindow();
                    stage.close();
                    try {
                        ViewManager.getInstance().openView("MultiplayerLobby.fxml", "Multiplayer - Game lobby", StageStyle.DECORATED);
                    } catch (IOException ex) {
                        Logger.getLogger(HomeScreenController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } catch (UnknownHostException ex) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Connection error");
                    alert.setHeaderText(null);
                    alert.setContentText("Could not resolve the host. Verify the hostname (" + ex.getMessage()+ ").");
                    Utilities.initDialogOwner(alert, multiplayerButton);
                    alert.showAndWait();

                } catch (IOException ex) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Connection error");
                    alert.setHeaderText(null);
                    alert.setContentText("Could not connect to server. " + ex.getMessage());
                    Utilities.initDialogOwner(alert, multiplayerButton);
                    alert.showAndWait();

                }

            }
        } else {
            // ... user chose CANCEL or closed the dialog
            //return;
        }
        
    }

    public void goBoardMode(ActionEvent event) {
        // Close home screen
        Stage stage = (Stage) theBoardButton.getScene().getWindow();
        stage.close();

        // Open The Board View
        try {
            ViewManager.getInstance().openView("TheBoardView.fxml", "The Board", StageStyle.DECORATED);
        } catch (IOException ex) {
            Logger.getLogger(HomeScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
