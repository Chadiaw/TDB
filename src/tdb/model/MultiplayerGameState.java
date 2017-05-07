/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdb.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 *
 * @author cheikh
 */
public class MultiplayerGameState implements Serializable {

    private static final long serialVersionUID = 56340715031521325L;
    
    
    //private Player host;
    private ArrayList<Player> players;
    private int drawingTime, winningScore;
    private String currentWord;
    private ArrayList<String> chosenWords;
    private Player artist; 
    private int artistIndex = 0;
    private boolean gameStarted;
    private boolean randomArtist;
    
    public MultiplayerGameState(String hostUsername) {
        Player host = new Player(hostUsername, true);
        players = new ArrayList<>();
        players.add(host);
        gameStarted = false;
        chosenWords = new ArrayList<>();
    }
    
    public void addPlayer(Player player) {
        players.add(player);
    }
    
    public void removePlayer(String playerName) {
        Iterator<Player> i = players.iterator();
        while(i.hasNext()) {
            Player p = i.next();
            if(p.getName().equals(playerName)) {
                players.remove(p);
                break;
            }
        }
    }

    public void setDrawingTime(int drawingTime) {
        this.drawingTime = drawingTime;
    }

    public void setRandomArtist(boolean randomArtist) {
        this.randomArtist = randomArtist;
    }
    
    public void changeCurrentWord() {
        currentWord = WordsManager.getRandomWord("EN");
        // Almost every word in the list has been chosen, reset the list.
        if (chosenWords.size() > WordsManager.getWordsCount("EN") - 2) {
            chosenWords.clear();
        }

        // This loop makes sure no words are repeated, until the whole list is used. 
        while (chosenWords.contains(currentWord)) {
            currentWord = WordsManager.getRandomWord("EN");
        }
        chosenWords.add(currentWord);
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public int getDrawingTime() {
        return drawingTime;
    }

    public String getCurrentWord() {
        return currentWord;
    }

    public Player getArtist() {
        return artist;
    }
    
    private void randomArtist() {
        Player randomPlayer = players.get(new Random().nextInt(players.size()));
        while(randomPlayer.equals(artist)) {
            randomPlayer = players.get(new Random().nextInt(players.size()));
        }
        artist = randomPlayer;
    }
    
    private void nextArtist() {
        artistIndex++;
        if(artistIndex >= players.size()) {
            artistIndex = 0;
        }
        artist = players.get(artistIndex);
    }
    
    public void changeArtist() {
        if(artist != null)
            artist.setDrawing(false);
        if (randomArtist) {
            randomArtist();
        } else {
            nextArtist();
        }
        artist.setDrawing(true);
    }

    public void setWinningScore(int winningScore) {
        this.winningScore = winningScore;
    }

    public int getWinningScore() {
        return winningScore;
    }

    public boolean hasGameStarted() {
        return gameStarted;
    }
    
    public void nextRound() {
        gameStarted = true;
        changeCurrentWord();
        changeArtist();
        
    }
    
    public void roundWinner(String name) {
        Iterator<Player> i = players.iterator();
        while(i.hasNext()) {
            Player p = i.next();
            if(p.getName().equals(name)) {
                p.setScore(p.getScore() + 1);
                break;
            }
        }
    }
    
    public String checkWinner() {
        String winnerName = ""; 
        Iterator<Player> i = players.iterator();
        while(i.hasNext()) {
            Player p = i.next();
            if(p.getScore() >= winningScore) {
                winnerName = p.getName();
                return winnerName;
            }
        }
        return winnerName;
    }
    
    
    
    
}
