/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdb.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author cheikh
 */
public class PlayerTableItem {
        
        private SimpleStringProperty name;
        private SimpleIntegerProperty score; 
        private SimpleBooleanProperty isHost;

        public String getName() {
            return name.get();
        }

        public int getScore() {
            return score.get();
        }
        
        public boolean isHost() {
            return isHost.get();
        }
        
        public PlayerTableItem(Player p) {
            name = new SimpleStringProperty(p.getName());
            if(p.isDrawing()) {
                name.set(p.getName() + " (drawing)");
            }
            score = new SimpleIntegerProperty(p.getScore());
            isHost = new SimpleBooleanProperty(p.isHost());
        }
    }
