package labs;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class VigenereCipher {
    private final static String LAB2_ALPHABET = "абвгдежзийклмнопрстуфхцчшщъыьэюя";
    private final static int ALPHABET_LEN = 1112;
    private final static String alpabet = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
    private final static Map<Character, Integer> f = new HashMap<Character, Integer>() {{
        for (char x : alpabet.toCharArray()) put(x, (int) x);
    }};

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

    public static void perform(CharMapperWithKey charMapper, String inputFilename, String outputFilename, String key) {
        String text = "";

        try {
            text = String.join("\n", Files.readAllLines(Paths.get(inputFilename)));
        } catch (IOException e) {
            e.printStackTrace();
        }


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

        printIndexesOfCoincidence(plaintextFilename, resultsFileMainName + "_plain.tsv", 1, 2);

        for (String key : keys) {
            resultsFileNameTsv = resultsFileMainName + key.length() + ".tsv";
            resultsFileNamePng = resultsFileMainName + key.length() + ".png";
            ciphertextFilename = resultsFileMainName + key.length() + "_en.txt";

            perform(VigenereCipher::encryptChar, plaintextFilename, ciphertextFilename, key);
            perform(VigenereCipher::decryptChar, ciphertextFilename, resultsFileMainName + key.length() + "_de.txt", key);

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
        List<Double> indexesOfSequences = new ArrayList<>();

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
            indexesOfSequences.add(frequencies.values().stream()
                    .map(x -> x * (x - 1))
                    .reduce(Integer::sum)
                    .orElse(0) * ICMult);
        }
        //returning average
        return indexesOfSequences.stream().mapToDouble(x -> x).average().orElse(0.);

    }

    public static void findKey(String textFilename, String mainFilename, String rusFreqFilename, int r) {
        String message = "";

        //RUS LANGUAGE LETTERS FREQUENCIES
        List<Map.Entry<String, Double>> rusFreq = new ArrayList<>();
        // =new ArrayList<>((new Text("voyna-i-mir-tom-1", LAB2_ALPHABET)).ngrams(1, true).entrySet());

        try {
            message = String.join("", Files.readAllLines(Paths.get(textFilename)));
            rusFreq = new ArrayList<>(Files.readAllLines(Paths.get(rusFreqFilename)).stream().collect(Collectors.toMap(
                    k -> k.split("\t")[0], v ->
                            Double.valueOf(v.split("\t")[1].replace(',', '.')))).entrySet());
            rusFreq.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        } catch (IOException e) {
            e.printStackTrace();
        }


        //TEXT SEQUENCES LETTERS FREQUENCIES
        int charsInOneSequence = message.length() / r;
        List<List<Character>> sequences = new ArrayList<>();
        List<List<Map.Entry<Character, Double>>> sequencesFreq = new ArrayList<>();

        //getting sequences
        for (int seq = 0; seq < r; seq++) {
            sequences.add(new ArrayList<>());
            for (int i = 0; i < charsInOneSequence; i++) {
                sequences.get(sequences.size() - 1).add(message.charAt(i * r + seq));
            }
        }

        //calculating frequencies for each sequence
        for (List<Character> sequence : sequences) {
            double probabilityBit = 1.0 / sequence.size();
            //count frequencies
            Map<Character, Double> frequencies = new HashMap<>();
            for (Character letter : sequence) {
                frequencies.put(letter, frequencies.getOrDefault(letter, 0.) + probabilityBit);
            }
            List<Map.Entry<Character, Double>> freqList = new ArrayList<>(frequencies.entrySet());
            freqList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
            sequencesFreq.add(freqList);
        }

        //PRINT SEQUENCES FREQUENCIES TABLE
        final String TAB = "\t";
        final String LF = "\n";
        try (OutputStream out = new FileOutputStream(mainFilename + ".tsv")) {
            StringBuilder title =
                    new StringBuilder().append("rus_char\trus_freq\t");
            for (int i = 0; i < LAB2_ALPHABET.length(); i++) {
                int k = 0;
                StringBuilder line =
                        new StringBuilder().append(rusFreq.get(i).getKey()).append(TAB)
                                .append(String.format("%.3f", rusFreq.get(i).getValue() * 100)).append(TAB);
                for (List<Map.Entry<Character, Double>> seqFreq : sequencesFreq) {
                    if (i < seqFreq.size()) {
                        line.append(seqFreq.get(i).getKey()).append(TAB).append(String.format("%.3f", seqFreq.get(i).getValue() * 100)).append(TAB);
                    } else {
                        line.append("-").append(TAB).append("-").append(TAB);
                    }
                    if (i == 0) {
                        title.append("seq").append(k).append(TAB).append("seq").append(k).append(TAB);
                    }
                    k++;

                }
                if (i == 0) out.write(title.append(LF).toString().getBytes());
                out.write(line.append(LF).toString().getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


//KEY FINDING
        StringBuilder key = new StringBuilder();
        for (List<Map.Entry<Character, Double>> seqFreq : sequencesFreq) {
            Map<Character, Integer> caesarKeyMaybe = new HashMap<>();
            for (int i = 0; i < seqFreq.size(); i++) {

                char k = keyGet(seqFreq.get(i).getKey(), rusFreq.get(i).getKey().charAt(0));
                caesarKeyMaybe.put(k, caesarKeyMaybe.getOrDefault(k, 0) + 1);
            }
            int maxValue = Collections.max(caesarKeyMaybe.entrySet(), Map.Entry.comparingByValue()).getValue();
            key.append(caesarKeyMaybe.entrySet()
                    .stream()
                    .filter(x -> x.getValue() == maxValue)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList()));

        }

        System.out.println("\nkey: " + key.toString());
        perform(VigenereCipher::rusDecryptChar, textFilename, mainFilename + "_de.txt", "чугунныенебеса");


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
