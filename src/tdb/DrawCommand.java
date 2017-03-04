
package tdb;

import java.io.Serializable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 *  Draw command for Canvas objects using the graphics context.
 * @author cheikh
 */
public abstract class DrawCommand implements Serializable{
    
    private static final long serialVersionUID = 1L;
    double x, y;
    double lineWidth;
    double[] colors;
    
    public abstract void doIt(GraphicsContext gc);
}

class StartLine extends DrawCommand {
    
    public StartLine (double x, double y, Color drawColor, double lineWidth) {
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
        gc.beginPath();
        gc.moveTo(this.x, this.y);
    }
}

class AddToLine extends DrawCommand {
    
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

class ClearDrawing extends DrawCommand {

    public ClearDrawing() {
        
    }
    
    @Override
    public void doIt(GraphicsContext gc) {
        Utilities.initDraw(gc);
    }
    
}