/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdb;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javafx.collections.FXCollections;

/**
 *
 * @author Kadiatou
 */
public class WordsManager {

    public static final List<String> wordsEN = Arrays.asList(
            "Airplane",
            "Apple",
            "Arrow",
            "Ball",
            "Banana",
            "Basketball",
            "Bed",
            "Bell",
            "Bird",
            "Book",
            "Bottle",
            "Bowling",
            "Box",
            "Broom",
            "Bracelet",
            "Bunny",
            "Car",
            "Carrot",
            "Cat",
            "Chair",
            "Circle",
            "Computer",
            "Cookie",
            "Corn",
            "Clock",
            "Cloud",
            "Cupcake",
            "Dog",
            "Door",
            "Duck",
            "Elephant",
            "Eye",
            "Family",
            "Finger",
            "Flower",
            "Football",
            "Frog",
            "Ghost",
            "Giraffe",
            "Grapes",
            "Hand",
            "Hat",
            "Heart",
            "Helicopter",
            "House",
            "Ice cream",
            "Keyboard",
            "Lamp",
            "Leaf",
            "Legs",
            "Monkey",
            "Moon",
            "Mouse",
            "Nose",
            "Ocean",
            "Orange",
            "Person",
            "Ring",
            "Robot",
            "Rocket",
            "Shoe",
            "Smile",
            "Snake",
            "Snowman",
            "Spider",
            "Stairs",
            "Sun",
            "Sunglasses",
            "Train",
            "Tree",
            "Turtle",
            "Whale",
            "Worm");

    /*
     * lecture des mots d'un fichier .text et stockage dans une list de
     * string
     */
    public ArrayList<String> letterReader() {
        ArrayList<String> words = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader("src/tdb/mots.txt"));
            String line = reader.readLine();

            while (line != null) {
                String[] wordsLine = line.split(" ");
                for (String word : wordsLine) {
                    words.add(word);
                }
                line = reader.readLine();
            }
        } catch (Exception e) {
        }
        return words;
    }

    /*
     * choix aléatoire d'un mot dans une liste de mots
     */
    public void RandWord(ArrayList<String> words) {

        Random rand = new Random(System.currentTimeMillis());
        String randomWord = words.get(rand.nextInt(words.size()));
        System.out.println(randomWord); // optionnel cet affichage doit se faire au niveau de l'interface 

    }

    /**
     * Returns a random word.
     * @param language : EN for english words, FR for french etc. 
     * @return 
     */
    public static String getRandomWord(String language) {
        List<String> words;
        
        switch(language) {
            case "EN":
                words = wordsEN;
                break;
            case "FR":
                // Use FR list of words, etc. 
            default:
                words = wordsEN;
                break;
        }
        
        Random rand = new Random(System.currentTimeMillis());
        String randomWord = words.get(rand.nextInt(words.size()));
        
        return randomWord;
    }
    
    public static int getWordsCount(String language) {
        List<String> words;
        
        switch(language) {
            case "EN":
                words = wordsEN;
                break;
            case "FR":
                // Use FR list of words, etc. 
            default:
                words = wordsEN;
                break;
        }
        
        return words.size();
    }

}
