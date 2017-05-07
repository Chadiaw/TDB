
package tdb.model;

import java.io.Serializable;
import javafx.scene.canvas.GraphicsContext;

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


