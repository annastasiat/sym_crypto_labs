package lab1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class Text {
    private String str;
    private String filename;

    public Text(String filename) {
        Path path = Paths.get("texts/" + filename);
        this.filename = filename;
        try {
            str = String.join(" ", Files.readAllLines(path))
                    .toLowerCase()
                    .replaceAll("[^а-я ё]", "")
                    .replaceAll("[ ]{2,}", " ")
                    .trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Double> ngrams(int n) {
        Map<String, Double> ngramsProbs = new HashMap<>();
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
            h += i * Math.log(i); // / Math.log(2);
        }
        return -h / (Math.log(2) * n);
    }

    public void printNgrams(int n) {
        List<Map.Entry<String, Double>> ngramsProbs = new ArrayList<>(ngrams(n).entrySet());
        StringBuilder resStr = new StringBuilder();
        //sort:
        ngramsProbs.sort(Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder()));
        //build string for file:
        for (Map.Entry<String, Double> i : ngramsProbs) {
            resStr.append(i.getKey()).append("\t").append(i.getValue()).append("\n");
        }
        //print to file:
        try {
            Path path = Paths.get("results/" + n + "_gram_results_" + filename.substring(0, filename.length() - 3) + "tsv");
            Files.write(path, String.valueOf(n).concat("грамма\tчастота\n").getBytes());
            Files.write(path, resStr.toString().getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getLength() {
        return str.length();
    }
}