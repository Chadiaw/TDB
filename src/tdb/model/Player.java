/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdb.model;

import java.io.Serializable;

/**
 *
 * @author cheikh
 */
public class Player implements Serializable {

    private static final long serialVersionUID = -7717394831965271183L;
    
    private String name;
    private boolean isHost, isDrawing;
    private int score; 
    
    public Player(String username, boolean hosting) {
        name = username;
        isHost = hosting;
        isDrawing = false;
        score = 0;
    }

    public String getName() {
        return name;
    }

    public boolean isHost() {
        return isHost;
    }
    
    public boolean isDrawing() {
        return isDrawing;
    }
    
    public void setDrawing(boolean value) {
        isDrawing = value;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int value) {
        score = value;
    }
    
    
}
