package lab1;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VigenereCipher {
    private final static String LAB2_ALPHABET = "абвгдежзийклмнопрстуфхцчшщъыьэюя";
    private final static int ALPHABET_LEN = 1112;
    private final static String alpabet = " абвгдеёжзийклмнопрстуфхцчшщъыьэюя";

    public static char encryptChar(char x, int keyI, String key){
        return (char) (((int) x + key.charAt(keyI % key.length())) % 1112);
    }

    public static char decryptChar(char x, int keyI, String key){
        return (char) (((int) x + 1112 - key.charAt(keyI % key.length())) % 1112);
    }

    public static void perform(Vigenere charMapper, String inputFilename, String outputFilename, String key){
        String text="";

        try (Stream<String> stream = Files.lines(Paths.get(inputFilename))) {
            text = stream.collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (OutputStream out = new FileOutputStream(outputFilename)) {
            for(int i=0;i<text.length();i++){
                out.write(Character.toString(charMapper.map(text.charAt(i), i, key)).getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void lab2Task2(String plaintextFilename, String ciphertextFilename, String resultsFileMainName) {

        List<String> keys = new ArrayList<>();
        keys.add("rg");
        keys.add("fgh");
        keys.add("ebjg");
        keys.add("slgjy");
        keys.add("qdrgtkdvkatrldb");

        String resultsFileNameTsv;
        String resultsFileNamePng;

        printIndexesOfCoincidence(plaintextFilename, resultsFileMainName + "_plain.tsv", 1, 2);

        for (String key : keys) {
            resultsFileNameTsv = resultsFileMainName + key.length() + ".tsv";
            resultsFileNamePng = resultsFileMainName + key.length() + ".png";

            perform(VigenereCipher::encryptChar, plaintextFilename, ciphertextFilename, key);

            printIndexesOfCoincidence(ciphertextFilename, resultsFileNameTsv, 2, 33);

            try {
                Runtime.getRuntime().exec("python plot_ic.py " + resultsFileNameTsv + " " + resultsFileNamePng);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void printIndexesOfCoincidence(String textFilename, String resultsFileName, int rMin, int rMax) {

        String text = "";

        try (Stream<String> stream = Files.lines(Paths.get(textFilename))) {
            text = stream.collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Double> textIndexes = new ArrayList<>();
        for (int r = rMin; r < rMax; r++) {
            textIndexes.add(indexOfCoincidence(text, r));
        }

        ///PRINT
        try (OutputStream out = new FileOutputStream(resultsFileName)) {
            for (int i = 0; i < rMax - rMin; i++) {
                out.write((i + rMin + "\t" + textIndexes.get(i) + "\n").getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static double indexOfCoincidence(String message, int r) {

        int charsInOneSequence = message.length() / r;
        double ICMult = 1. / (charsInOneSequence * (charsInOneSequence - 1));

        List<List<Character>> sequences = new ArrayList<>();
        List<Double> indexes = new ArrayList<>();

        //getting sequences
        for (int seq = 0; seq < r; seq++) {
            sequences.add(new ArrayList<>());
            for (int i = 0; i < charsInOneSequence; i++) {
                sequences.get(sequences.size() - 1).add(message.charAt(i * r + seq));
            }
        }

        //calculating indexes of coincidence for each sequence
        for (List<Character> sequence : sequences) {

            //count frequencies
            Map<Character, Integer> frequencies = new HashMap<>();
            for (Character letter : sequence) {
                frequencies.put(letter, frequencies.getOrDefault(letter, 0) + 1);
            }

            //map frequencies and summarize
            indexes.add(frequencies.values().stream()
                    .map(x -> x * (x - 1))
                    .reduce(Integer::sum)
                    .orElse(0) * ICMult);
        }
        //returning average
        return indexes.stream().mapToDouble(x -> x).average().orElse(0.);

    }

    /*public static void encrypt(String inputFilename, String outputFilename, String key) {
        List<String> message = new ArrayList<>();

        try (Stream<String> stream = Files.lines(Paths.get(inputFilename))) {
            message = stream.collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        int messageLength = 0;
        try (OutputStream out = new FileOutputStream(outputFilename)) {
            //for each line
            for (int i = 0; i < message.size(); i++) {
                String line = message.get(i);
                //for each char in line
                for (int j = 0; j < line.length(); j++) {
                    //write mapped char
                    out.write(Character.toString(
                            (char) (((int) line.charAt(j) + key.charAt((messageLength + j) % key.length())) % 1112))
                            .getBytes());

                }
                //if  next - the end of the line
                if (i != message.size() - 1)
                    //map '\n' and write
                    out.write(Character.toString(
                            (char) (((int) '\n' + key.charAt((messageLength + line.length()) % key.length())) % 1112))
                            .getBytes());
                //increace messageLength beacouse of '\n'
                messageLength += line.length() + 1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void decrypt(String inputFilename, String outputFilename, String key) {
        List<String> message = new ArrayList<>();

        try (Stream<String> stream = Files.lines(Paths.get(inputFilename))) {
            message = stream.collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        int messageLength = 0;

        try (OutputStream out = new FileOutputStream(outputFilename)) {
            //out.write((n + "грамма\tчастота*" + multiplier + "\n").getBytes());
            for (int i = 0; i < message.size(); i++) {
                String line = message.get(i);
                for (int j = 0; j < line.length(); j++) {
                    out.write(Character.toString((char) (((int) line.charAt(j) + 1112 - key.charAt((messageLength + j) % key.length())) % 1112)).getBytes());

                }
                if (i != message.size() - 1)
                    out.write(Character.toString((char) (((int) '\n' + 1112 - key.charAt((messageLength + line.length()) % key.length())) % 1112)).getBytes());
                messageLength += line.length() + 1;
            }

        } catch (IOException e) {
            e.printStackTrace();


    }

 }*/













   /*

       public static void analyze(String filename) {
        String message = "";

        try {
            message = String.join("", Files.readAllLines(Paths.get(filename)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int period = getPeriod(message);
    }

   public static double indexOfCoincidence(int r, String filename, int task) {
        double indexOfCoincidence = 0;
        String message = "";

        try (Stream<String> stream = Files.lines(Paths.get(filename))) {
            message = stream.collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<Character, Integer> freq = new HashMap<>();
        for (char x : message.toCharArray()) {
            freq.put(x, freq.getOrDefault(x, 0) + 1);
        }

        if (task == 1) {
            for (int i = 0; i < ALPHABET_LEN; i++) {
                indexOfCoincidence += freq.getOrDefault((char) i, 0) * (freq.getOrDefault((char) i, 0) - 1);
            }
        } else {
            for (char x : LAB2_ALPHABET.toCharArray()) {
                indexOfCoincidence += freq.getOrDefault(x, 0) * (freq.getOrDefault(x, 0) - 1);
            }
        }

        return indexOfCoincidence / (message.length() * (message.length() - 1));


    }

    public static int getPeriod(String message) {
        double indexOfCoincidence = 0;

        Map<Character, Integer> freq = new HashMap<>();
        for (char x : message.toCharArray()) {
            freq.put(x, freq.getOrDefault(x, 0) + 1);
        }

        for (char x : LAB2_ALPHABET.toCharArray()) {
            indexOfCoincidence += freq.get(x) * (freq.get(x) - 1);
        }

        indexOfCoincidence *= 1.0 / (message.length() * (message.length() - 1));


        return 0;

    }*/
}
