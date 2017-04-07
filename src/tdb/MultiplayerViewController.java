/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdb;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.ActionEvent;
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
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

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
    private Button resetWordButton;
    @FXML
    private Canvas drawCanvas;
    @FXML
    private TextField chatInput;
    @FXML
    private TextArea chatOutput;
    @FXML
    private TableView playerTable;

    GraphicsContext graphicsContext;
    SimpleBooleanProperty turnToDraw = new SimpleBooleanProperty(true);

    /**
     * Initializes all the controls.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Initialize the toolbar
        homeButton.setVisible(true);
        sizeTextField.setText("5.0");
        sizeTextField.setEditable(false);
        wordLabel.setText("THE BOARD"); // TODO: Give actual word here
        colorPicker.setValue(Color.RED);
        graphicsContext = drawCanvas.getGraphicsContext2D();

        // Bind controls to boolean value
        clearButton.disableProperty().bind(turnToDraw.not());
        eraserToggle.disableProperty().bind(turnToDraw.not());
        colorPicker.disableProperty().bind(turnToDraw.not());
        
        Utilities.initDraw(graphicsContext);
        getCurrentValues();
        drawCanvas.setDisable(false);

        // Set up chat
        chatOutput.setEditable(false);
        chatOutput.setWrapText(true); // Disable horizontal scollbar
        chatOutput.setDisable(false);
        chatInput.setPromptText("Enter msg/guess here..");

        // Set up players table
        // TODO: Create Players' TableView here and populate it (Name, Score).
        // The player currently drawing shoud be highlighted (say it in tooltip text too). 
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
            // TODO : Disconnect client. 
            Utilities.goToHomeScreen(homeButton, MultiplayerViewController.class.getName());
        } else {
            // User clicked no or cancelled -> Do nothing.
        }
    }

    @FXML
    private void saveImage(ActionEvent event) {
        Utilities.saveImage(drawCanvas, MultiplayerViewController.class.getName());
    }

    @FXML
    private void clearPanel(ActionEvent event) {
        DrawCommand command = new ClearDrawing();
        command.doIt(graphicsContext);
        /*
        if (tbClient != null) {
            tbClient.sendCommand(command);
        }
         */
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

            // TODO : Send command to server
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
            //TODO : Send command
        } else {
            Utilities.setForbiddenCursor(drawCanvas);
        }
    }

    @FXML
    private void sendMessage(ActionEvent event) {
        /* Prototype
        if (tbClient != null && !chatInput.getText().isEmpty()) {
            tbClient.sendMessage(username + ": " + chatInput.getText());
            chatInput.clear();
        }
        */
    }

}
