/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdb.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Kadiatou
 */
public class WordsManager {

    public static final List<String> wordsEN = Arrays.asList(
            "Airplane",
            "Angel",
            "Apple",
            "Arrow",
            "Ass",
            "Ball",
            "Banana",
            "Baseball",
            "Basketball",
            "Battery",
            "Bed",
            "Bell",
            "Big",
            "Bird",
            "Board",
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
            "Chicken",
            "Cigarette",
            "Circle",
            "Computer",
            "Cookie",
            "Corn",
            "Clock",
            "Cloud",
            "Crab",
            "Cupcake",
            "Dragon",
            "Diamond",
            "Doctor",
            "Dog",
            "Door",
            "Duck",
            "Elephant",
            "Eraser",
            "Eye",
            "Family",
            "Finger",
            "Flower",
            "Football",
            "Frog",
            "Ghost",
            "Giraffe",
            "Golf",
            "Grapes",
            "Guitar",
            "Gun",
            "Hand",
            "Hat",
            "Heart",
            "Helicopter",
            "Hockey",
            "House",
            "Ice-cream",
            "Jersey",
            "Keyboard",
            "Ladder",
            "Lamp",
            "Leaf",
            "Legs",
            "Little",
            "Mercedes",
            "Money",
            "Monkey",
            "Moon",
            "Mouse",
            "Music",
            "Ninja",
            "Notepad",
            "Nose",
            "Ocean",
            "Orange",
            "Person",
            "Rain",
            "Ring",
            "Robot",
            "Rocket",
            "Shoe",
            "Smile",
            "Smoke",
            "Snake",
            "Snowman",
            "Soccer",
            "Spider",
            "Spongebob",
            "Square",
            "Stairs",
            "Sun",
            "Sunglasses",
            "Tank",
            "Tennis",
            "Train",
            "Trashcan",
            "Tree",
            "Truck",
            "Turtle",
            "Whale",
            "Wheel",
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
