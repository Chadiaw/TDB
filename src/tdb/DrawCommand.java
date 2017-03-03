
package tdb;

import java.io.Serializable;
import javafx.scene.canvas.GraphicsContext;

/**
 *  Draw command for Canvas objects using the graphics context.
 * @author cheikh
 */
public abstract class DrawCommand implements Serializable{
    
    private static final long serialVersionUID = 1L;
    double x, y;
    
    public abstract void doIt(GraphicsContext gc);
}

class StartLine extends DrawCommand {
    
    public StartLine (double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public void doIt(GraphicsContext gc) {
        gc.beginPath();
        gc.moveTo(this.x, this.y);
    }
}

class AddToLine extends DrawCommand {
    
    public AddToLine(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public void doIt(GraphicsContext gc) {
        gc.lineTo(this.x, this.y);
        gc.stroke();
    }
}