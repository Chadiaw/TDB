/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdb;

import java.io.File;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import tdb.network.TheBoardClient;
import tdb.network.TheBoardServer;

/**
 *
 * @author cheikh
 */
public class TheDrawingBoard extends Application {
    
    private static TheBoardClient uniqueClient = null; 
    private TheBoardServer boardServer;
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("HomeScreen.fxml"));
        
        Scene scene = new Scene(root);
        
        stage.setTitle("The Drawing Board");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        Utilities.setAppIcon(stage);
        boardServer = new TheBoardServer(4444, true);
        boardServer.start();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void stop() {
        if (uniqueClient != null) {
            uniqueClient.disconnect();
        }
        if (boardServer.isAlive())
            boardServer.disconnect();
    }
    
    public static void setBoardClient(TheBoardClient client) {
        uniqueClient = client;
    }
    
}
