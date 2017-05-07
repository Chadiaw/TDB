/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdb;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tdb.network.MultiplayerClient;
import tdb.network.MultiplayerServer;
import tdb.network.TheBoardClient;
import tdb.network.TheBoardServer;

/**
 *
 * @author cheikh
 */
public class TheDrawingBoard extends Application {
    
    private static TheBoardClient boardClient = null; 
    private static MultiplayerClient multiplayerClient = null; 

    private TheBoardServer boardServer;
    private static MultiplayerServer multiServer;
    
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
        if (boardClient != null) {
            boardClient.disconnect();
        }
        if (boardServer.isAlive()) {
            boardServer.disconnect();
        }
        
        if(multiServer != null) {
            disconnectMultiplayerServer();
        }
        Platform.exit();
    }
    
    public static void setBoardClient(TheBoardClient client) {
        boardClient = client;
    }

    public static void setMultiplayerClient(MultiplayerClient client) {
        multiplayerClient = client;
    }
    
    public static MultiplayerClient getMultiplayerClient() {
        return multiplayerClient;
    }
    
    public static void disconnectMultiplayerServer() {
        if (multiServer.isAlive()) {
            multiServer.sendDisconnectNotice();
            multiServer.disconnect();
        }
    }
    
    public static void startMultiplayerServer() {
        multiServer = new MultiplayerServer();
        multiServer.start();
    }
    
}
