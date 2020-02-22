package lab1;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Text {
    private String str;
    private String filename;
    private boolean spaces;
    private String alphabet;

    public Text(String filename, String alphabet) {
        Path path = Paths.get("texts/" + filename);
        this.filename = filename;
        this.alphabet = alphabet;
        this.spaces = alphabet.contains(" ");
        try {
            str = String.join(" ", Files.readAllLines(path))
                    .toLowerCase()
                    .replaceAll("[^" + alphabet + "]", "")
                    .replaceAll("[ ]{2,}", " ")
                    .trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Double> ngrams(int n, boolean cross) {
        Map<String, Double> ngramsProbs = new HashMap<>();
        //Pr(ngram) = amount of ngram / amount of all ngrams = amount of ngram *(1 / amount of all ngrams) =
        // = amount of ngram *(1 / (str.length() - n + 1))
        int amountOfNgrams = cross ? (str.length() - n + 1) : (str.length() / n);
        double probabilityBit = 1.0 / (amountOfNgrams);

        for (int i = 0; i < amountOfNgrams; i++) {
            int idx = cross ? i : (n * i);
            String ngram = str.substring(idx, idx + n);
            ngramsProbs.put(ngram, ngramsProbs.getOrDefault(ngram, 0.0) + probabilityBit);
        }
        return ngramsProbs;
    }

    public double H(int n, boolean cross) {
        Map<String, Double> ngramsProbs = ngrams(n, cross);
        double h = 0;
        for (double i : ngramsProbs.values()) {
            h += i * Math.log(i);
        }
        return -h / (Math.log(2) * n); //Math.log - natural logarithm, so log2(i) = log(i)/log(2)
    }

    public void printNgrams(int n, boolean cross, int multiplier) {
        List<Map.Entry<String, Double>> ngramsProbs = new ArrayList<>(ngrams(n, cross).entrySet());
        String outFilename = "results/" + n + "_grams_" + (spaces ? "spaces_" : "no_spaces___") + (cross && n != 1 ? "cross_" : "no_cross___")
                + filename.replaceAll("\\.txt", ".tsv");

        //sort
        ngramsProbs.sort(Map.Entry.comparingByValue(Comparator.reverseOrder())); //ngramsProbs.sort(Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder()));

        try (OutputStream out = new FileOutputStream(outFilename)) {
            out.write((n + "грамма\tчастота*" + multiplier + "\n").getBytes());
            for (Map.Entry<String, Double> i : ngramsProbs) {
                out.write((i.getKey() + "\t" + String.format("%.2f", i.getValue() * multiplier) + "\n").getBytes()); ////!!!!!!!!
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printTable(int n, boolean cross, int multiplier) {
        Map<String, Double> ngramsProbs = ngrams(n, cross);
        String[] alphabetArr = alphabet.split("");
        String outFilename = "results/table_2grams_" + (spaces ? "spaces_" : "no_spaces_") + (cross && n != 1 ? "cross_" : "no_cross___")
                + filename.replaceAll("\\.txt", "") + ".tsv";

        try (OutputStream out = new FileOutputStream(outFilename)) {
            out.write((" \t" + String.join("\t", alphabetArr) + "\n").getBytes());
            for (String i : alphabetArr) {
                StringBuilder row = new StringBuilder();
                row.append(i).append("\t");
                for (String j : alphabetArr) {
                    row.append(String.format("%.2f", ngramsProbs.getOrDefault(j + i, 0.0) * multiplier)).append("\t");
                }
                row.append("\n");
                out.write(row.toString().getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getLength() {
        return str.length();
    }

    public String getStr() {
        return str;
    }

    public Map<String, Integer> MapLetterInt(){
        String[] alphabetArr = alphabet.split("");
        Map<String, Integer> letterInt = new HashMap<>();
        for(int i=0; i<alphabetArr.length; i++){
            letterInt.put(alphabetArr[i], i);
        }
        return letterInt;
    }

    public Map<Integer, String> MapIntLetter(){
        String[] alphabetArr = alphabet.split("");
        Map<Integer, String> intLetter = new HashMap<>();
        for(int i=0; i<alphabetArr.length; i++){
            intLetter.put(i, alphabetArr[i]);
        }
        return intLetter;
    }
}