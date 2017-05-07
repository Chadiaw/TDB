/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdb.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 *
 * @author cheikh
 */
public class AddToLine extends DrawCommand {
    
    public AddToLine(double x, double y, Color drawColor, double lineWidth) {
        this.lineWidth = lineWidth;
        this.x = x;
        this.y = y;
        colors = new double[4];
        colors[0] = drawColor.getRed();
        colors[1] = drawColor.getGreen();
        colors[2] = drawColor.getBlue();
        colors[3] = drawColor.getOpacity();
    }
    
    @Override
    public void doIt(GraphicsContext gc) {
        Color c = new Color(colors[0], colors[1], colors[2], colors[3]);
        gc.setStroke(c);
        gc.setLineWidth(lineWidth);
        gc.lineTo(this.x, this.y);
        gc.stroke();
    }
}
