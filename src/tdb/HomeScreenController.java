/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdb;

import java.io.IOException;
import java.net.URL;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Multiplayer Mode");
        alert.setHeaderText("If you want to host a non-LAN game, make sure\nport forwarding"
                + " is enabled on your router (port " + MultiplayerServer.listeningPort + ").");
        alert.setContentText("Choose your option : ");

        ButtonType buttonHost = new ButtonType("Host Game");
        ButtonType buttonJoin = new ButtonType("Join Game");
        ButtonType buttonCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonHost, buttonJoin, buttonCancel);

        // Style the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("styles/CustomDialog.css").toExternalForm());
        dialogPane.getStyleClass().add("myDialog");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonHost) {
            // ... user chose "One"
        } else if (result.get() == buttonJoin) {
            // ... user chose "Two"
        } else {
            // ... user chose CANCEL or closed the dialog
            return;
        }
        /*
        // Close home screen
        Stage stage = (Stage) multiplayerButton.getScene().getWindow();
        stage.close();

        // Open The Multiplayer View
        try {
            ViewManager.getInstance().openView("MultiplayerView.fxml", "The Drawing Board - Multiplayer", StageStyle.DECORATED);
        } catch (IOException ex) {
            Logger.getLogger(HomeScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
         */
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
