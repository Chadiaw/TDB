/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdb;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
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
    private TextArea chatOutput;
    String username;

    @FXML
    private TextField chatInput;

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
        getCurrentValues();
        drawCanvas.setDisable(true);
        usersList.setItems(items);

        usersList.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });

        // Set up chat
        chatOutput.setEditable(false);
        chatOutput.setWrapText(true); // Disable horizontal scollbar
        chatOutput.setDisable(true);
        chatInput.setDisable(true);

    }

    /**
     * Set the graphicsContext color and line width to the current values
     */
    private void getCurrentValues() {
        graphicsContext.setStroke(colorPicker.getValue());
        graphicsContext.setLineWidth(Double.parseDouble(sizeTextField.getText()));
    }

    @FXML
    private void sendMessage(ActionEvent event) {
        if (tbClient != null && !chatInput.getText().isEmpty()) {
            tbClient.sendMessage(username + ": " + chatInput.getText());
            chatInput.clear();
        }
    }

    @FXML
    private void homeScreen(ActionEvent event) {
        if (tbClient != null) {
            // Disconnect. 
            tbClient.disconnect();
        }
        Utilities.goToHomeScreen(homeButton, TheBoardViewController.class.getName());
    }

    @FXML
    private void saveImage(ActionEvent event) {
        Utilities.saveImage(drawCanvas, TheBoardViewController.class.getName());
    }

    @FXML
    private void clearPanel(ActionEvent event) {
        DrawCommand command = new ClearDrawing();
        command.doIt(graphicsContext);
        if (tbClient != null) {
            tbClient.sendCommand(command);
        }
        getCurrentValues();
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
        Color drawColor;
        if (!eraserToggle.isSelected()) {
            // Eraser not toggled : get the drawing color from the picker
            drawColor = colorPicker.getValue();
        } else {
            drawColor = Color.WHITE;
        }

        // Get the line width from the text field
        double lineWidth = 1.0;
        try {
            lineWidth = Double.parseDouble(sizeTextField.getText());
        } catch (NumberFormatException e) {
            sizeTextField.setText("1.0");
        }

        graphicsContext.setStroke(drawColor);
        graphicsContext.setLineWidth(lineWidth);
        graphicsContext.lineTo(event.getX(), event.getY());
        graphicsContext.stroke();

        DrawCommand command = new AddToLine(event.getX(), event.getY(), drawColor, lineWidth);

        if (tbClient != null) {
            tbClient.sendCommand(command);
        }
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
        double lineWidth = Double.parseDouble(sizeTextField.getText());
        graphicsContext.beginPath();
        graphicsContext.moveTo(event.getX(), event.getY());

        DrawCommand command = new StartLine(event.getX(), event.getY(), colorPicker.getValue(), lineWidth);
        
        if (tbClient != null) {
            tbClient.sendCommand(command);
        }
    }

    @FXML
    private void connectToServer(ActionEvent event) {

        // Ask for hostname
        String host;
        TextInputDialog dialog = new TextInputDialog("localhost");
        dialog.setTitle("Hostname");
        try {
            dialog.setHeaderText(String.format("Your hostname (localhost) is : %1$s",
                    InetAddress.getLocalHost()));
        } catch (UnknownHostException ex) {
            dialog.setHeaderText("Your IP (localhost) is : Unknown");
        }
        dialog.setContentText("Please enter the hostname to join : ");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            host = result.get();
        } else {
            // Maybe need to close the connection here, somehow. Must be tested. 
            return;
        }

        try {
            tbClient = new TheBoardClient(host, graphicsContext, items, chatOutput);
            tbClient.startReceiveThread();
        } catch (UnknownHostException ex) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Connection error");
            alert.setHeaderText(null);
            alert.setContentText("Could not resolve the host. " + ex.getMessage());
            alert.showAndWait();

        } catch (IOException ex) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Connection error");
            alert.setHeaderText(null);
            alert.setContentText("Could not connect to server. " + ex.getMessage());
            alert.showAndWait();

        }

        if (tbClient != null) {
            while (!tbClient.isUserAck()) {
                // Ask for username
                dialog = new TextInputDialog();
                dialog.setTitle("Username selection");
                dialog.setHeaderText(null);
                dialog.setContentText("Please select a username : ");
                result = dialog.showAndWait();
                if (result.isPresent()) {
                    username = result.get();
                    tbClient.sendUsername(username);
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(TheBoardViewController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    // User canceled name selection -> Disconnect. 
                    tbClient.disconnect();
                    tbClient = null;
                    TheDrawingBoard.setBoardClient(null);
                    return;
                }
                if (!tbClient.isUserAck() && tbClient.getUserAckMessage() != null) {
                    // If username was not acknowledged, display the reason why
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Username not valid");
                    alert.setHeaderText(null);
                    alert.setContentText(tbClient.getUserAckMessage());
                    alert.showAndWait();
                }
            }

            // User selected a valid name -> Connection and ID to server is complete. 
            connectButton.setDisable(true);
            drawCanvas.setDisable(false);
            chatOutput.setDisable(false);
            chatInput.setDisable(false);
            TheDrawingBoard.setBoardClient(tbClient);

        }
    }
}
