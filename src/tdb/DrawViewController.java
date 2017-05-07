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
import tdb.model.AddToLine;
import tdb.model.ClearDrawing;
import tdb.model.StartLine;
import tdb.model.WordsManager;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

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
    ArrayList<String> chosenWords;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sizeTextField.setText("10.0");
        sizeTextField.setEditable(false);
        colorPicker.setValue(Color.ORANGE);

        wordLabel.setText(WordsManager.getRandomWord("EN"));
        chosenWords = new ArrayList<String>();
        chosenWords.add(wordLabel.getText());

        graphicsContext = drawCanvas.getGraphicsContext2D();
        Utilities.initDraw(graphicsContext);
        getCurrentValues();

    }

    public void resetWord(ActionEvent event) {
        String newWord = WordsManager.getRandomWord("EN"); // Get new random word

        // Almost every word in the list has been chosen, reset the list.
        if (chosenWords.size() > WordsManager.getWordsCount("EN") - 2) {
            chosenWords.clear();
        }

        // This loop makes sure no words are repeated, until the whole list is used. 
        while (chosenWords.contains(newWord)) {
            newWord = WordsManager.getRandomWord("EN");
        }
        wordLabel.setText(newWord);
        chosenWords.add(newWord);
    }

    /**
     * Set the graphicsContext color and line width to the current values
     */
    private void getCurrentValues() {
        graphicsContext.setStroke(colorPicker.getValue());
        graphicsContext.setLineWidth(Double.parseDouble(sizeTextField.getText()));
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
            Utilities.setEraserCursor(drawCanvas);
        } else {
            drawCanvas.getScene().setCursor(Cursor.DEFAULT);
        }
    }

    public void exitPanel(MouseEvent event) {
        drawCanvas.getScene().setCursor(Cursor.DEFAULT);
    }

    public void clickPanelAction(MouseEvent event) {
        double lineWidth = Double.parseDouble(sizeTextField.getText());
        new StartLine(event.getX(), event.getY(), colorPicker.getValue(), lineWidth).doIt(graphicsContext);
    }

    public void dragOnPanelAction(MouseEvent event) {
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

        new AddToLine(event.getX(), event.getY(), drawColor, lineWidth).doIt(graphicsContext);
    }

    public void releaseOnPanelAction(MouseEvent event) {

    }

    /**
     * *
     * Clears the drawing panel.
     */
    public void clearPanel(ActionEvent event) {
        new ClearDrawing().doIt(graphicsContext);
        getCurrentValues();

    }

}
