package labs;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public interface VigenereCipher {
    String LAB2_ALPHABET = "абвгдежзийклмнопрстуфхцчшщъыьэюя";
    int ALPHABET_LEN = 1112;
    List<String> MY_KEYS = Arrays.asList("rg", "fgh", "ebjg", "slgjy", "qdrgtkdvkatrldb");

    static char encryptChar(char x, char keyI) {
        return (char) (((int) x + keyI) % 1112);
    }

    static char decryptChar(char x, char keyI) {
        return (char) (((int) x + 1112 - keyI) % 1112);
    }

    static char keyGet(char x, char y) {
        return (char) (((int) x - (int) y + 32) % 32 + 1072);
    }

    static char rusEncryptChar(char x, char keyI) {
        return (char) (((int) x - 1072 + (int) keyI - 1072) % 32 + 1072);
    }

    static char rusDecryptChar(char x, char keyI) {
        return (char) (((int) x - (int) keyI + 32) % 32 + 1072);
    }

    static List<List<Character>> getSequences(String message, int r) {
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

    static Map<Character, Integer> N(List<Character> text) {
        Map<Character, Integer> frequencies = new HashMap<>();
        for (Character letter : text) {
            frequencies.put(letter, frequencies.getOrDefault(letter, 0) + 1);
        }
        return frequencies;
    }

    static void perform(CharMapperWithKey charMapper, String inputFilename, String outputFilename, String key) {
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

    static void lab2Task2(String plaintextFilename, String resultsFileMainName) {

        String resultsFileNameLen;
        String ciphertextFilename;

        printIndexesOfCoincidence(plaintextFilename, resultsFileMainName + "_plain", 1, 2);

        for (String key : MY_KEYS) {
            resultsFileNameLen = resultsFileMainName + key.length();
            ciphertextFilename = resultsFileNameLen + "_en.txt";

            perform(VigenereCipher::encryptChar, plaintextFilename, ciphertextFilename, key);
            perform(VigenereCipher::decryptChar, ciphertextFilename, resultsFileNameLen + "_de.txt", key);

            printIndexesOfCoincidence(ciphertextFilename, resultsFileNameLen + "_ic", 2, 33);

        }

        try {
            Runtime.getRuntime().exec("python plot_ic_all.py " + resultsFileMainName + " " + resultsFileMainName + "_ic.png");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void printIndexesOfCoincidence(String textFilename, String mainResultsFileName, int rMin, int rMax) {

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
        try (OutputStream out = new FileOutputStream(mainResultsFileName + ".tsv")) {
            for (int i = 0; i < rMax - rMin; i++) {
                out.write((i + rMin + "\t" + textIndexes.get(i) + "\n").getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Runtime.getRuntime().exec("python plot_ic.py " + mainResultsFileName + ".tsv" + " " + mainResultsFileName + ".png");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static double indexOfCoincidence(String message, int r) {

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

    static void lab2Task3(String textFilename, String mainFilename, String rusFreqFilename, int r) {
        String message = "";

        //PINT INDEXES OF COINCIDENCE TABLE AND DIAGRAM
        printIndexesOfCoincidence(textFilename, mainFilename + "_ic", 2, 31);

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

    static String getKeyVar1(List<List<Map.Entry<Character, Double>>> sequencesFreq, List<Map.Entry<Character, Double>> rusFreq) {
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

    static String getKeyVar2(List<Map<Character, Integer>> NS, Map<Character, Double> rusFreq) {
        StringBuilder key = new StringBuilder();
        for (Map<Character, Integer> N : NS) {
            int maxM = -1;
            char k = 'a';
            for (char g : LAB2_ALPHABET.toCharArray()) {
                int Mg = 0;
                for (char t : LAB2_ALPHABET.toCharArray()) {
                    Mg += rusFreq.get(t) * N.getOrDefault((char) ((t + g) % 32 + 1072), 0);
                }
                k = (Mg > maxM) ? g : k;
                maxM = Math.max(Mg, maxM);
            }
            key.append(k);

        }
        return key.toString();
    }
}
