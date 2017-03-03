/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdb;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.imageio.ImageIO;

/**
 * Utilities class.
 * @author cheikh
 */
public class Utilities {
    
    public static void goToHomeScreen(Button homeButton, String callerName) {
        // Close this view
        Stage stage = (Stage) homeButton.getScene().getWindow();
        stage.close();
        
        // Open Home Screen
        try {
            ViewManager.getInstance().openView("HomeScreen.fxml", "The Drawing Board", StageStyle.DECORATED);
        } catch (IOException ex) {
            Logger.getLogger(callerName).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void saveImage (Canvas canvas, String callerName) {
        WritableImage image = canvas.snapshot(new SnapshotParameters(), null);

        // Select where to save the image
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save drawing");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPEG", "*.jpg"));

        File fileChoice = fileChooser.showSaveDialog(canvas.getScene().getWindow());
        if (fileChoice == null) {
            // User canceled the save.
            return;
        }
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", fileChoice);
        } catch (IOException ex) {
            Logger.getLogger(callerName).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void decreasePencilSize(TextField sizeTextField) {
        double currentSize = Double.parseDouble(sizeTextField.getText());
        if (currentSize > 1) {
            currentSize--;
        }
        sizeTextField.setText(String.valueOf(currentSize));
    }
    
    public static void increasePencilSize(TextField sizeTextField) {
         double currentSize = Double.parseDouble(sizeTextField.getText());
        if (currentSize < 50) {
            currentSize++;
        }
        sizeTextField.setText(String.valueOf(currentSize));
    }
    
    public static void initDraw(GraphicsContext gc) {
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
