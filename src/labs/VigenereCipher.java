package labs;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class VigenereCipher {
    private final static String LAB2_ALPHABET = "абвгдежзийклмнопрстуфхцчшщъыьэюя";
    private final static int ALPHABET_LEN = 1112;

    public static char encryptChar(char x, char keyI) {
        return (char) (((int) x + keyI) % 1112);
    }

    public static char decryptChar(char x, char keyI) {
        return (char) (((int) x + 1112 - keyI) % 1112);
    }

    public static char keyGet(char x, char y) {
        return (char) (((int) x - (int) y + 32) % 32 + 1072);
    }

    public static char rusEncryptChar(char x, char keyI) {
        return (char) (((int) x - 1072 + (int) keyI - 1072) % 32 + 1072);
    }

    public static char rusDecryptChar(char x, char keyI) {
        return (char) (((int) x - (int) keyI + 32) % 32 + 1072);
    }

    private static List<List<Character>> getSequences(String message, int r){
        int charsInOneSequence = message.length() / r;
        List<List<Character>> sequences = new ArrayList<>();

        for (int seq = 0; seq < r; seq++) {
            sequences.add(new ArrayList<>());
            for (int i = 0; i < charsInOneSequence; i++) {
                sequences.get(sequences.size() - 1).add(message.charAt(i * r + seq));
            }
        }
        return sequences;
    }

    private static Map<Character, Integer> N(List<Character> text){
        Map<Character, Integer> frequencies = new HashMap<>();
        for (Character letter : text) {
            frequencies.put(letter, frequencies.getOrDefault(letter, 0) + 1);
        }
        return frequencies;
    }

    public static void perform(CharMapperWithKey charMapper, String inputFilename, String outputFilename, String key) {
        String text = "";

        //READ
        try {
            text = String.join("\n", Files.readAllLines(Paths.get(inputFilename)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //WRITE
        try (OutputStream out = new FileOutputStream(outputFilename)) {
            for (int i = 0; i < text.length(); i++) {
                out.write(Character.toString(charMapper
                        .map(text.charAt(i), key.charAt(i % key.length())))
                        .getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void lab2Task2(String plaintextFilename, String resultsFileMainName) {

        List<String> keys = new ArrayList<>();
        keys.add("rg");
        keys.add("fgh");
        keys.add("ebjg");
        keys.add("slgjy");
        keys.add("qdrgtkdvkatrldb");

        String resultsFileNameTsv;
        String resultsFileNamePng;
        String ciphertextFilename;

        printIndexesOfCoincidence(plaintextFilename, resultsFileMainName + "_plain", 1, 2);

        for (String key : keys) {
            resultsFileNameTsv = resultsFileMainName + key.length() + ".tsv";
            resultsFileNamePng = resultsFileMainName + key.length() + ".png";
            ciphertextFilename = resultsFileMainName + key.length() + "_en.txt";

            perform(VigenereCipher::encryptChar, plaintextFilename, ciphertextFilename, key);
            perform(VigenereCipher::decryptChar, ciphertextFilename, resultsFileMainName + key.length() + "_de.txt", key);

            printIndexesOfCoincidence(ciphertextFilename, resultsFileMainName + key.length(), 2, 33);
/*
            try {
                Runtime.getRuntime().exec("python plot_ic.py " + resultsFileNameTsv + " " + resultsFileNamePng);
            } catch (Exception e) {
                e.printStackTrace();
            }*/
        }

        try {
            Runtime.getRuntime().exec("python plot_ic_all.py " + resultsFileMainName + " " + resultsFileMainName+ ".png");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printIndexesOfCoincidence(String textFilename, String mainResultsFileName, int rMin, int rMax) {

        String text = "";

        try {
            text = String.join("\n", Files.readAllLines(Paths.get(textFilename)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Double> textIndexes = new ArrayList<>();
        for (int r = rMin; r < rMax; r++) {
            textIndexes.add(indexOfCoincidence(text, r));
        }

        ///PRINT
        try (OutputStream out = new FileOutputStream(mainResultsFileName+ ".tsv")) {
            for (int i = 0; i < rMax - rMin; i++) {
                out.write((i + rMin + "\t" + textIndexes.get(i) + "\n").getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Runtime.getRuntime().exec("python plot_ic.py " + mainResultsFileName+ ".tsv" + " " + mainResultsFileName+ ".png");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static double indexOfCoincidence(String message, int r) {

        int charsInOneSequence = message.length() / r;
        double ICMult = 1. / ((charsInOneSequence) * (charsInOneSequence - 1));

        List<List<Character>> sequences = getSequences(message, r);
        List<Double> indexesOfSequences = new ArrayList<>();

        //calculating indexes of coincidence for each sequence
        for (List<Character> sequence : sequences) {
            Map<Character, Integer> frequencies = N(sequence);
            //map frequencies and summarize
            indexesOfSequences.add(frequencies.values().stream()
                    .map(x -> x * (x - 1))
                    .reduce(Integer::sum)
                    .orElse(0) * ICMult);
        }
        //returning average
        return indexesOfSequences.stream().mapToDouble(x -> x).average().orElse(0.);

    }

    public static void lab2Task3(String textFilename, String mainFilename, String rusFreqFilename, int r) {
        String message = "";

        //PINT INDEXES OF COINCIDENCE TABLE AND DIAGRAM
        printIndexesOfCoincidence(textFilename,  mainFilename +"_ic", 2, 31);

        //READING TEXT AND RUS LANG PROBABILITIES
        List<Map.Entry<Character, Double>> rusFreqSorted = new ArrayList<>();
        Map<Character, Double> rusFreqMap = new HashMap<>();

        try {
            message = String.join("", Files.readAllLines(Paths.get(textFilename)));
            rusFreqMap = Files.readAllLines(Paths.get(rusFreqFilename)).stream().collect(Collectors.toMap(
                    k -> k.split("\t")[0].charAt(0), v ->
                            Double.valueOf(v.split("\t")[1].replace(',', '.'))));
            rusFreqSorted = new ArrayList<>(rusFreqMap.entrySet());
            rusFreqSorted.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //TEXT SEQUENCES LETTERS FREQUENCIES
        List<List<Character>> sequences = getSequences(message, r);

        //calculating frequencies for each sequence
        List<List<Map.Entry<Character, Double>>> sequencesFreq = new ArrayList<>();
        for (List<Character> sequence : sequences) {
            double probabilityBit = 1.0 / sequence.size();
            //count probabilities
            Map<Character, Double> probabilities = new HashMap<>();
            for (Character letter : sequence) {
                probabilities.put(letter, probabilities.getOrDefault(letter, 0.) + probabilityBit);
            }
            List<Map.Entry<Character, Double>> probList = new ArrayList<>(probabilities.entrySet());
            probList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
            sequencesFreq.add(probList);
        }

        //PRINT SEQUENCES FREQUENCIES TABLE
        final String TAB = "\t";
        final String LF = "\n";
        try (OutputStream out = new FileOutputStream(mainFilename + ".tsv")) {
            for (int i = 0; i < LAB2_ALPHABET.length(); i++) {
                StringBuilder line =
                        new StringBuilder().append(rusFreqSorted.get(i).getKey()).append(TAB)
                                .append(String.format("%.3f", rusFreqSorted.get(i).getValue() * 100)).append(TAB);
                for (List<Map.Entry<Character, Double>> seqFreq : sequencesFreq) {
                    if (i < seqFreq.size()) {
                        line.append(seqFreq.get(i).getKey()).append(TAB).append(String.format("%.3f", seqFreq.get(i).getValue() * 100)).append(TAB);
                    } else {
                        line.append("-").append(TAB).append("-").append(TAB);
                    }

                }
                out.write(line.append(LF).toString().getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\nkey(1): " + getKeyVar1(sequencesFreq, rusFreqSorted));

        List<Map<Character, Integer>> NS = sequences.stream().map(VigenereCipher::N).collect(Collectors.toList());
        String key2 = getKeyVar2(NS, rusFreqMap);
        System.out.println("\nkey(2): " + key2);

        perform(VigenereCipher::rusDecryptChar, textFilename, mainFilename + "_de.txt", key2);


    }

    public static String getKeyVar1(List<List<Map.Entry<Character, Double>>> sequencesFreq, List<Map.Entry<Character, Double>> rusFreq) {
        StringBuilder key = new StringBuilder();
        for (List<Map.Entry<Character, Double>> seqFreq : sequencesFreq) {
            key.append(keyGet(seqFreq.get(0).getKey(), rusFreq.get(0).getKey()));
            /*Map<Character, Integer> probableCaesarKeys = new HashMap<>();
            for (int i = 0; i < seqFreq.size(); i++) {
                char k = keyGet(seqFreq.get(i).getKey(), rusFreq.get(i).getKey());
                probableCaesarKeys.put(k, probableCaesarKeys.getOrDefault(k, 0) + 1);
            }
            int maxValue = Collections.max(probableCaesarKeys.entrySet(), Map.Entry.comparingByValue()).getValue();
            key.append(probableCaesarKeys.entrySet()
                    .stream()
                    .filter(x -> x.getValue() == maxValue)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList()));*/
        }
        return key.toString();
    }

    public static String getKeyVar2(List<Map<Character, Integer>> NS, Map<Character, Double> rusFreq) {
        StringBuilder key = new StringBuilder();
        for (Map<Character, Integer> N : NS) {
            int m = -1;
            char k = 'a';
            for (char g : LAB2_ALPHABET.toCharArray()) {
                int mg = 0;
                for (char t : LAB2_ALPHABET.toCharArray()) {
                    mg += rusFreq.get(t) * N.getOrDefault((char) ((t + g) % 32 + 1072), 0);
                }
                k = (mg > m) ? g : k;
                m = Math.max(mg, m);
            }
            key.append(k);

        }
        return key.toString();
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
