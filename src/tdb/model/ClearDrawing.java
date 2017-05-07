/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdb.model;

import javafx.scene.canvas.GraphicsContext;
import tdb.Utilities;

/**
 *
 * @author cheikh
 */
public class ClearDrawing extends DrawCommand {

    public ClearDrawing() {
        
    }
    
    @Override
    public void doIt(GraphicsContext gc) {
        Utilities.initDraw(gc);
    }
    
}