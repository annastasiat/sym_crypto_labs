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

    public Text(String filename, boolean spaces) {
        Path path = Paths.get("texts/" + filename);
        this.filename = filename;
        this.spaces = spaces;
        try {
            str = String.join(" ", Files.readAllLines(path))
                    .toLowerCase()
                    .replaceAll(spaces ? "[^а-я ё]" : "[^а-яё]", "")
                    .replaceAll("[ ]{2,}", " ")
                    .trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Double> ngrams(int n) {
        Map<String, Double> ngramsProbs = new HashMap<>();
        //Pr(ngram) = amount of ngram / amount of all ngrams = amount of ngram *(1 / amount of all ngrams) =
        // = amount of ngram *(1 / (str.length() - n + 1))
        double probabilityBit = 1.0 / (str.length() - n + 1);

        for (int i = 0; i <= str.length() - n; i++) {
            String ngram = str.substring(i, i + n);
            ngramsProbs.put(ngram, ngramsProbs.getOrDefault(ngram, 0.0) + probabilityBit);
        }
        return ngramsProbs;
    }

    public double H(int n) {
        Map<String, Double> ngramsProbs = ngrams(n);
        double h = 0;
        for (double i : ngramsProbs.values()) {
            h += i * Math.log(i);
        }
        return -h / (Math.log(2) * n); //Math.log - natural logarithm, so log2(i) = log(i)/log(2)
    }


    public void printNgrams(int n) {
        List<Map.Entry<String, Double>> ngramsProbs = new ArrayList<>(ngrams(n).entrySet());
        String outFilename = "results/" + n + "_grams_" + (spaces ? "" : "no_spaces_")
                + filename.replaceAll("\\.txt", "") + ".tsv";

        //sort
        ngramsProbs.sort(Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder()));

        try (OutputStream out = new FileOutputStream(outFilename)) {
            out.write((n + "грамма\tчастота\n").getBytes());
            for (Map.Entry<String, Double> i : ngramsProbs) {
                out.write((i.getKey() + "\t" + i.getValue() + "\n").getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getLength() {
        return str.length();
    }
}