/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdb;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tdb.network.TheBoardClient;

/**
 * FXML Controller class
 *
 * @author cheikh
 */
public class TheBoardViewController implements Initializable {

    @FXML
    private Button homeButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button clearButton;
    @FXML
    private ToggleButton eraserToggle;
    @FXML
    private Button sizeMinusButton;
    @FXML
    private TextField sizeTextField;
    @FXML
    private Button sizePlusButton;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Label wordLabel;
    @FXML
    private Button resetWordButton;
    @FXML
    private Canvas drawCanvas;

    @FXML
    private Label usersLabel;
    @FXML 
    private Button connectButton;
    
    @FXML
    private ListView<String> usersList;
    ObservableList<String> items = FXCollections.observableArrayList();
    
    GraphicsContext graphicsContext;
    TheBoardClient tbClient = null;
    

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        homeButton.setVisible(true);
        sizeTextField.setText("10.0");
        sizeTextField.setEditable(false);
        wordLabel.setText("THE BOARD");
        colorPicker.setValue(Color.AQUAMARINE);
        graphicsContext = drawCanvas.getGraphicsContext2D();
        Utilities.initDraw(graphicsContext);
        drawCanvas.setDisable(true);
        usersList.setItems(items);
    }

    @FXML
    private void homeScreen(ActionEvent event) {
        if(tbClient != null) {
            // Disconnect. 
            tbClient.disconnect();
            //items.clear();
            
        }
        Utilities.goToHomeScreen(homeButton, TheBoardViewController.class.getName());
    }

    @FXML
    private void saveImage(ActionEvent event) {
        Utilities.saveImage(drawCanvas, TheBoardViewController.class.getName());
    }

    @FXML
    private void clearPanel(ActionEvent event) {
        graphicsContext.clearRect(0, 0, drawCanvas.getWidth(), drawCanvas.getHeight());
        graphicsContext.beginPath();
        Utilities.initDraw(graphicsContext);
    }

    @FXML
    private void minusSizeAction(ActionEvent event) {
        Utilities.decreasePencilSize(sizeTextField);
    }

    @FXML
    private void plusSizeAction(ActionEvent event) {
        Utilities.increasePencilSize(sizeTextField);
    }

    @FXML
    private void releaseOnPanelAction(MouseEvent event) {
    }

    @FXML
    private void dragOnPanelAction(MouseEvent event) {
        if (!eraserToggle.isSelected()) {
            // Eraser not toggled : get the drawing color from the picker
            graphicsContext.setStroke(colorPicker.getValue());
        } else {
            graphicsContext.setStroke(Color.WHITE);
        }

        // Get the line width from the text field
        try {
            graphicsContext.setLineWidth(Double.parseDouble(sizeTextField.getText()));
        } catch (NumberFormatException e) {
            graphicsContext.setLineWidth(1);
            sizeTextField.setText("1.0");
        }

        DrawCommand command = new AddToLine(event.getX(), event.getY());
        command.doIt(graphicsContext);

        if(tbClient != null)
            tbClient.sendCommand(command);
    }

    @FXML
    private void exitPanel(MouseEvent event) {
        drawCanvas.getScene().setCursor(Cursor.DEFAULT);
    }

    @FXML
    private void enterPanel(MouseEvent event) {
        if (eraserToggle.isSelected()) {
            Image image = new Image(new File("src/tdb/images/circle-cursor.png").toURI().toString());
            ImageCursor cursor = new ImageCursor(image,
                    image.getWidth() / 2,
                    image.getHeight() / 2);

            drawCanvas.getScene().setCursor(cursor);
        } else {
            drawCanvas.getScene().setCursor(Cursor.DEFAULT);
        }
    }

    @FXML
    private void clickPanelAction(MouseEvent event) {
        DrawCommand command = new StartLine(event.getX(), event.getY());
        command.doIt(graphicsContext);
        if(tbClient != null)
            tbClient.sendCommand(command);
    }

    @FXML
    private void connectToServer(ActionEvent event) {
        try {
            tbClient = new TheBoardClient(graphicsContext, items);
            tbClient.startReceiveThread();
        } catch (IOException ex) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Connection error");
            alert.setHeaderText(null);
            alert.setContentText("Could not connect to server. ");
            alert.showAndWait();

        }

        if (tbClient != null) {
            // Ask for username
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Username selection");
            dialog.setHeaderText(null);
            dialog.setContentText("Please select a username : ");
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                tbClient.sendUsername(result.get());
                connectButton.setDisable(true);
                drawCanvas.setDisable(false);
                TheDrawingBoard.setBoardClient(tbClient);
            } else {
                // Maybe need to close the connection here, somehow. Must be tested. 
                
            }
        }
    }
}
