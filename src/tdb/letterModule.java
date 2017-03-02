/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdb;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Kadiatou
 */
public class letterModule {

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

}
