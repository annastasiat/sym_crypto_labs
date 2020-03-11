package labs;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public interface VigenereCipher {
    String TAB = "\t";
    String LINE_SEPARATOR = System.lineSeparator();//"\n";
    String LAB2_ALPHABET = "абвгдежзийклмнопрстуфхцчшщъыьэюя";
    List<String> MY_KEYS = Arrays.asList("на",
            "кот", "кота", "котом", "август", "архалук", "аршинный", "балетоман",
            "абстракция", "абракадабра", "аскорбиновый", "верхоглядство", "абстрагировать",
            "гильотинировать", "бумаготворчество", "запротоколировать", "последовательность", "шапкозакидательство", "трансконтинентальный");

    String RUS_PROB_FILENAME = "lab1/voyna-i-mir-tom-1_letters_freq.tsv";

    static char getKeyChar(char x, char y) {
        return (char) ((x - y + 32) % 32 + 1072);
    }

    static char rusEncryptChar(char x, char keyI) {
        return (char) ((x + keyI) % 32 + 1072);
    }

    static char rusDecryptChar(char x, char keyI) {
        return (char) ((x - keyI + 32) % 32 + 1072);
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

    static String perform(BiFunction<Character, Character, Character> charMapper, String text, String key) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            res.append(charMapper.apply(text.charAt(i), key.charAt(i % key.length())));
        }
        return res.toString();
    }

    static void printIndexesOfCoincidence(String textFilename, String mainResultsFileName, int rMin, int rMax) {

        try (OutputStream out = new FileOutputStream(mainResultsFileName + ".tsv")) {
            //READ
            String text = String.join(LINE_SEPARATOR, Files.readAllLines(Paths.get(textFilename)));
            //CALCULATE
            List<Double> textIndexes = new ArrayList<>();
            for (int r = rMin; r < rMax; r++) {
                textIndexes.add(indexOfCoincidence(text, r));
            }
            ///WRITE
            for (int i = 0; i < rMax - rMin; i++) {
                out.write((i + rMin + TAB + textIndexes.get(i) + LINE_SEPARATOR).getBytes());
            }
            //PLOT
            Runtime.getRuntime().exec("python plot_ic.py " + mainResultsFileName + ".tsv" + " " + mainResultsFileName + ".png");

        } catch (IOException e) {
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
        return indexesOfSequences.stream().mapToDouble(x -> x).average().orElse(0d);

    }

    static void lab2Task12(String filename) {

        try (OutputStream out = new FileOutputStream("lab2/task12/" + filename + "_ic.tsv")) {

            String plaintext = String.join("", Files.readAllLines(Paths.get("texts/" + filename + ".txt")));

            out.write((0 + TAB + indexOfCoincidence(plaintext, 1) + LINE_SEPARATOR).getBytes());

            for (String key : MY_KEYS) {
                String resultsFileNameLen = "lab2/task12/de_en/" + filename + key.length();

                String encryptedText = perform(VigenereCipher::rusEncryptChar, plaintext, key);
                String decryptedText = perform(VigenereCipher::rusDecryptChar, encryptedText, key);

                Files.write(Paths.get(resultsFileNameLen + "_en.txt"),
                        encryptedText.getBytes());
                Files.write(Paths.get(resultsFileNameLen + "_de.txt"),
                        decryptedText.getBytes());

                out.write((key.length() + TAB + indexOfCoincidence(encryptedText, 1) + LINE_SEPARATOR).getBytes());
            }
            Runtime.getRuntime().exec("python plot_ic.py " + "lab2/task12/" + filename + "_ic.tsv " + "lab2/task12/" + filename + "_ic.png");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void lab2Task3(String textFilename, String mainFilename, int r) {
        try {
            //PRINT INDEXES OF COINCIDENCE TABLE AND DIAGRAM
            printIndexesOfCoincidence(textFilename, "lab2/task3/" + mainFilename + "_ic", 2, 31);

            //READING TEXT AND RUS LANG PROBABILITIES
            String message = String.join("", Files.readAllLines(Paths.get(textFilename)));
            Map<Character, Double> rusFreqMap = Files.readAllLines(Paths.get(RUS_PROB_FILENAME)).stream().collect(Collectors.toMap(
                    k -> k.split(TAB)[0].charAt(0), v ->
                            Double.valueOf(v.split(TAB)[1].replace(',', '.'))));
            List<Map.Entry<Character, Double>> rusFreqSorted = rusFreqMap.entrySet()
                    .stream().sorted(Comparator.comparingDouble(Map.Entry<Character, Double>::getValue).reversed())
                    .collect(Collectors.toList());


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
/*        try (OutputStream out = new FileOutputStream("lab2/task3/" +mainFilename + ".tsv")) {
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
                out.write(line.append(LINE_SEPARATOR).toString().getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/

            //GETTING KEYS
            String key1 = getKeyVar1(sequencesFreq, rusFreqSorted);
            System.out.println("\nkey(ic): " + key1);

            List<Map<Character, Integer>> NS = sequences.stream().map(VigenereCipher::N).collect(Collectors.toList());
            String key2 = getKeyVar2(NS, rusFreqMap);
            System.out.println("\nkey(mg): " + key2);

            Files.write(Paths.get("lab2/task3/key_" + mainFilename + ".txt"), ("key1: " + key1 + "  key2: " + key2).getBytes());
            Files.write(Paths.get("lab2/task3/" + mainFilename + "_de_key_ic.txt"),
                    perform(VigenereCipher::rusDecryptChar, message, key1).getBytes());
            Files.write(Paths.get("lab2/task3/" + mainFilename + "_de_key_mg.txt"),
                    perform(VigenereCipher::rusDecryptChar, message, key2).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static String getKeyVar1(List<List<Map.Entry<Character, Double>>> sequencesFreq, List<Map.Entry<Character, Double>> rusFreq) {
        StringBuilder key = new StringBuilder();
        for (List<Map.Entry<Character, Double>> seqFreq : sequencesFreq) {
            key.append(getKeyChar(seqFreq.get(0).getKey(), rusFreq.get(0).getKey()));
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
