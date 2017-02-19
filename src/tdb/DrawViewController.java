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
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;

/**
 *
 * @author cheikh
 */
public class DrawViewController implements Initializable {

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

    //private Videoclub application;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sizeTextField.setText("10.0");
        sizeTextField.setEditable(false);
        wordLabel.setText("Arrow");
        colorPicker.setValue(Color.ORANGE);
        graphicsContext = drawCanvas.getGraphicsContext2D();
        initDraw(graphicsContext);

    }

    /**
     * Save the drawing as an image file locally.
     */
    public void saveImage(ActionEvent event) {
        WritableImage image = drawCanvas.snapshot(new SnapshotParameters(), null);

        // Select where to save the image
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save drawing");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG","*.png"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPEG","*.jpg"));
    
        File file = fileChooser.showSaveDialog(saveButton.getScene().getWindow());
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException e) {
            // TODO: handle exception here
        }
    }

    public void minusSizeAction(ActionEvent event) {
        double currentSize = Double.parseDouble(sizeTextField.getText());
        if (currentSize > 1) {
            currentSize--;
        }
        sizeTextField.setText(String.valueOf(currentSize));
    }

    public void plusSizeAction(ActionEvent event) {
        double currentSize = Double.parseDouble(sizeTextField.getText());
        if (currentSize < 50) {
            currentSize++;
        }
        sizeTextField.setText(String.valueOf(currentSize));
    }

    public void clickPanelAction(MouseEvent event) {
        graphicsContext.beginPath();
        graphicsContext.moveTo(event.getX(), event.getY());
        graphicsContext.stroke();
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

        graphicsContext.lineTo(event.getX(), event.getY());
        graphicsContext.stroke();
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
        initDraw(graphicsContext);

    }

    /**
     * *
     * Initializes the drawing panel.
     *
     * @param gc
     */
    private void initDraw(GraphicsContext gc) {
        double canvasWidth = gc.getCanvas().getWidth();
        double canvasHeight = gc.getCanvas().getHeight();

        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(3);

        gc.fill();

        // Draw the panel outline 
        gc.fillRect(0, 0, canvasWidth, canvasWidth);
        gc.strokeRect(
                0, //x of the upper left corner
                0, //y of the upper left corner
                canvasWidth, //width of the rectangle
                canvasHeight);  //height of the rectangle

    }
}
