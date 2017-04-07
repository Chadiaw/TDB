/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdb;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
    
    public void goSoloMode (ActionEvent event) {
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
    
    public void goTwoPlayersMode (ActionEvent event) {
        
    }
    
    public void goMultiplayerMode (ActionEvent event) {
       // Close home screen
        Stage stage = (Stage) multiplayerButton.getScene().getWindow();
        stage.close();
        
        // Open The Board View
        try {
            ViewManager.getInstance().openView("MultiplayerView.fxml", "The Drawing Board - Multiplayer", StageStyle.DECORATED);
        } catch (IOException ex) {
            Logger.getLogger(HomeScreenController.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    public void goBoardMode (ActionEvent event) {
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
