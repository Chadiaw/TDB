/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdb;

import tdb.model.AddToLine;
import tdb.model.DrawCommand;
import tdb.model.StartLine;
import tdb.model.ClearDrawing;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.WindowEvent;
import tdb.model.PlayerTableItem;
import tdb.network.MultiplayerClient;

/**
 * FXML Controller class
 *
 * @author cheikh
 */
public class MultiplayerViewController implements Initializable {

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
    private Label timeLabel;
    @FXML
    private Canvas drawCanvas;
    @FXML
    private TextField chatInput;
    @FXML
    private TextArea chatOutput;
    @FXML
    private TableView<PlayerTableItem> playerTable;
    @FXML private TableColumn nameColumn;
    @FXML private TableColumn scoreColumn;
    ObservableList<PlayerTableItem> items = FXCollections.observableArrayList();

    GraphicsContext graphicsContext;
    SimpleBooleanProperty turnToDraw = new SimpleBooleanProperty(false);
    
    private MultiplayerClient client;
    private String username;

    /**
     * Initializes all the controls.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Initialize the toolbar
        homeButton.setVisible(true);
        sizeTextField.setText("1.0");
        sizeTextField.setEditable(false);
        colorPicker.setValue(Color.RED);
        graphicsContext = drawCanvas.getGraphicsContext2D();

        // Bind controls to boolean value
        clearButton.disableProperty().bind(turnToDraw.not());
        eraserToggle.disableProperty().bind(turnToDraw.not());
        colorPicker.disableProperty().bind(turnToDraw.not());
        sizeMinusButton.disableProperty().bind(turnToDraw.not());
        sizePlusButton.disableProperty().bind(turnToDraw.not());
        chatInput.disableProperty().bind(turnToDraw);
        
        Utilities.initDraw(graphicsContext);
        getCurrentValues();
        drawCanvas.setDisable(false);

        // Set up chat
        chatOutput.setEditable(false);
        chatOutput.setWrapText(true); // Disable horizontal scollbar
        chatOutput.setDisable(false);
        chatInput.setPromptText("Enter msg/guess here..");        

        client = TheDrawingBoard.getMultiplayerClient();
        
        client.setChatData(chatOutput);
        client.setGc(graphicsContext);
        client.setTurnToDraw(turnToDraw);
        client.setPlayersList(items);
        client.setCurrentWord(wordLabel);
        client.setTimeLeft(timeLabel);
        
        // Set up players' table
        nameColumn.setCellValueFactory(new PropertyValueFactory<PlayerTableItem, String>("name"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<PlayerTableItem, Integer>("score"));
        playerTable.setItems(items);
        
        username = client.getUserAckMessage();
        
        // Initialize time and word
        timeLabel.setText(Integer.toString(client.getGameState().getDrawingTime()));
        wordLabel.setText("Ready");
        
        // Send ready signal
        client.sendReady();
    }

    /**
     * Set the graphicsContext color and line width to the current values
     */
    private void getCurrentValues() {
        graphicsContext.setStroke(colorPicker.getValue());
        graphicsContext.setLineWidth(Double.parseDouble(sizeTextField.getText()));
    }

    /**
     * Called when the user clicks on the home screen button, AKA tries to
     * disconnect.
     *
     * @param event
     */
    @FXML
    private void homeScreen(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to disconnect ? You won't be able to rejoin the game.",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Disconnect");
        Optional<ButtonType> result = confirm.showAndWait();

        // If user clicked yes -> Disconnect, go back to home screen
        if (result.isPresent() && result.get().equals(ButtonType.YES)) {
            if(client.isHostClient()) {
                client.disconnect(); 
                TheDrawingBoard.disconnectMultiplayerServer();               
            } else {
                client.disconnect();
            }
        }
    }
    
    @FXML
    private void quitMode(Event dummyEvent) {
        playerTable.getScene().getWindow().setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                client.disconnect();
                if (client.isHostClient()) {
                    TheDrawingBoard.disconnectMultiplayerServer();
                }
            }
        });
    }

    @FXML
    private void saveImage(ActionEvent event) {
        Utilities.saveImage(drawCanvas, MultiplayerViewController.class.getName());
    }

    @FXML
    private void clearPanel(ActionEvent event) {
        DrawCommand command = new ClearDrawing();
        command.doIt(graphicsContext);
        client.sendCommand(command);
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
        if (turnToDraw.get()) {
            // If eraser is not toggled, set the cursor back to default here
            if (!eraserToggle.isSelected()) {
                drawCanvas.getScene().setCursor(Cursor.DEFAULT);
            } else {
                Utilities.setEraserCursor(drawCanvas);
            }
        } else {
            // Show to user he is not allowed to draw
            Utilities.setForbiddenCursor(drawCanvas);
        }
    }

    @FXML
    private void dragOnPanelAction(MouseEvent event) {
        if (turnToDraw.get()) {
            // Get the color
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

            DrawCommand command = new AddToLine(event.getX(), event.getY(), drawColor, lineWidth);
            command.doIt(graphicsContext);

            client.sendCommand(command);
        } else {
            Utilities.setForbiddenCursor(drawCanvas);
        }
    }

    @FXML
    private void exitPanel(MouseEvent event) {
        drawCanvas.getScene().setCursor(Cursor.DEFAULT);
    }

    @FXML
    private void enterPanel(MouseEvent event) {
        if (turnToDraw.get()) {
            if (eraserToggle.isSelected()) {
                Utilities.setEraserCursor(drawCanvas);
            } else {
                drawCanvas.getScene().setCursor(Cursor.DEFAULT);
            }
        } else {
            Utilities.setForbiddenCursor(drawCanvas);
        }
    }

    @FXML
    private void clickPanelAction(MouseEvent event) {
        if (turnToDraw.get()) {
            double lineWidth = Double.parseDouble(sizeTextField.getText());
            DrawCommand command = new StartLine(event.getX(), event.getY(), colorPicker.getValue(), lineWidth);
            command.doIt(graphicsContext);
            client.sendCommand(command);
        } else {
            Utilities.setForbiddenCursor(drawCanvas);
        }
    }

    @FXML
    private void sendMessage(ActionEvent event) {
        if (client != null && !chatInput.getText().isEmpty()) {
            client.sendMessage(username + ": " + chatInput.getText());
            chatInput.clear();
        }
    }

}
