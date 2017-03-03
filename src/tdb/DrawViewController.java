/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdb;

/**
 *
 * @author cheikh
 */
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.imageio.ImageIO;

/**
 *
 * @author cheikh
 */
public class DrawViewController implements Initializable {

    @FXML
    Button homeButton;
    @FXML
    Button saveButton;
    @FXML
    Button clearButton;
    @FXML
    Button resetWordButton;
    @FXML
    ToggleButton eraserToggle;
    @FXML
    Button sizeMinusButton;
    @FXML
    Button sizePlusButton;
    @FXML
    TextField sizeTextField;
    @FXML
    Label wordLabel;
    @FXML
    ColorPicker colorPicker;

    @FXML
    Canvas drawCanvas;

    GraphicsContext graphicsContext;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sizeTextField.setText("10.0");
        sizeTextField.setEditable(false);
        wordLabel.setText("ARROW");
        colorPicker.setValue(Color.ORANGE);
        graphicsContext = drawCanvas.getGraphicsContext2D();
        Utilities.initDraw(graphicsContext);

    }

    public void homeScreen(ActionEvent event) {
        Utilities.goToHomeScreen(homeButton, DrawViewController.class.getName());
    }

    /**
     * Save the drawing as an image file locally.
     */
    public void saveImage(ActionEvent event) {
        Utilities.saveImage(drawCanvas, DrawViewController.class.getName());
    }

    public void minusSizeAction(ActionEvent event) {
        Utilities.decreasePencilSize(sizeTextField);
    }

    public void plusSizeAction(ActionEvent event) {
        Utilities.increasePencilSize(sizeTextField);
    }

    public void enterPanel(MouseEvent event) {
        if (eraserToggle.isSelected()) {
            Image image = new Image(new File("src/tdb/images/circle-cursor.png").toURI().toString());
            ImageCursor cursor = new ImageCursor(image,
                                image.getWidth() / 2,
                                image.getHeight() /2);

            drawCanvas.getScene().setCursor(cursor);
        } else {
            drawCanvas.getScene().setCursor(Cursor.DEFAULT);
        }
    }

    public void exitPanel(MouseEvent event) {
        drawCanvas.getScene().setCursor(Cursor.DEFAULT);
    }

    public void clickPanelAction(MouseEvent event) {
        new StartLine(event.getX(), event.getY()).doIt(graphicsContext);
    }

    public void dragOnPanelAction(MouseEvent event) {
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
        
        new AddToLine(event.getX(), event.getY()).doIt(graphicsContext);
    }

    public void releaseOnPanelAction(MouseEvent event) {

    }

    /**
     * *
     * Clears the drawing panel.
     */
    public void clearPanel(ActionEvent event) {
        graphicsContext.clearRect(0, 0, drawCanvas.getWidth(), drawCanvas.getHeight());
        graphicsContext.beginPath();
        Utilities.initDraw(graphicsContext);

    }

}
