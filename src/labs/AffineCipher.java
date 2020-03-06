package labs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public interface AffineCipher {
    String TAB = "\t";
    String LINE_SEPARATOR = System.lineSeparator();//"\n";
    String LAB3_ALPHABET = "абвгдежзийклмнопрстуфхцчшщыьэюя";
    Map<String, Double> RUS_BIGRAMS_PROBABILITIES = readGrams("lab1/2_grams_no_spaces_cross_voyna-i-mir-tom-1.tsv");
    int M_SQUARED = LAB3_ALPHABET.length() * LAB3_ALPHABET.length();

    static int inverse(int a, int b) {
        int x = a;
        int y = b;
        assert (gcd(a, b) == 1);
        int u0 = 1, u1 = 0;
        while (b != 0) {
            int temp = u1;
            u1 = u0 - a / b * u1;
            u0 = temp;

            temp = b;
            b = Math.floorMod(a, b);
            a = temp;
        }
        assert (Math.floorMod(u0 * x, y) == 1);
        return u0;
        /*a = a % b;
        for (int x = 1; x < b; x++)
            if (Math.floorMod((a * x) , b) == 1)
                return x;
        return 1;*/
    }

    static int gcd(int a, int b) {
        if (b == 0) return a;
        else return gcd(b, Math.floorMod(a, b));
    }

    static List<Integer> solveEq(int a, int b, int n) {
        int d = gcd(a, n);
        List<Integer> roots = new ArrayList<>();
        if (d == 1) {
            assert (Math.floorMod(Math.floorMod(inverse(a, n) * b, n) * a - b, n) == 0);
            roots.add(Math.floorMod((inverse(a, n) + n) * b, n));
        } else if (Math.floorMod(b, d) == 0) {
            int x0 = Math.floorMod(inverse(a / d, n / d) * b / d, n / d);
            int n1 = n / d;
            for (int i = 0; i < d; i++) {
                assert (Math.floorMod((x0 + i * n1) * a - b, n) == 0);
                roots.add(x0 + i * n1);
            }
        }
        return roots;
    }

    static Map<String, Double> readGrams(String filepath) {
        Map<String, Double> grams = new HashMap<>();
        try {
            grams = Files.readAllLines(Paths.get(filepath)).stream().collect(Collectors.toMap(
                    k -> k.split(TAB)[0], v ->
                            Double.valueOf(v.split(TAB)[1].replace(',', '.'))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return grams;
    }


    static void findKeys(String mainFilename) throws IOException {
        Text textObj = new Text(String.join("", Files.readAllLines(Paths.get("texts/" + mainFilename + ".txt"))),
                mainFilename, LAB3_ALPHABET);
        List<Map.Entry<String, Double>> textBigramsSorted = new ArrayList<>(
                textObj.ngrams(2, false).entrySet());
        textBigramsSorted.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        List<Map.Entry<String, Double>> rusBigramProbSorted = new ArrayList<>(RUS_BIGRAMS_PROBABILITIES.entrySet());
        rusBigramProbSorted.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        String text = textObj.getStr();

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                for (int k = 0; k < 5; k++) {
                    for (int m = 0; m < 5; m++) {
                        if (i != k && j != m) {

                            int y1 = bigramToInt(textBigramsSorted.get(i).getKey());
                            int x1 = bigramToInt(rusBigramProbSorted.get(j).getKey());
                            int y2 = bigramToInt(textBigramsSorted.get(k).getKey());
                            int x2 = bigramToInt(rusBigramProbSorted.get(m).getKey());

                            List<Integer> roots = solveEq(x1 - x2, y1 - y2, M_SQUARED);
                            for (int a : roots) {
                                int b = Math.floorMod(y1 - a * x1, M_SQUARED);
                                if (gcd(a, M_SQUARED) == 1) {
                                    String detext = perform(AffineCipher::rusDecrypt, text, inverse(a, M_SQUARED), b);
                                    if (rusLangRecogniser(detext)) {
                                        System.out.println("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFOOOOOOOOOOOOOOOOOOOUUUUUUUUUUNNNNNDDD");
                                        System.out.println("a: " + a + "  b: " + b);
                                        Files.write(Paths.get("lab3/" + mainFilename + "__" + a + "_" + b + ".txt"), detext.getBytes());
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    static void perform(StringMapperWithKeys mapper, String inputFilename, String mainOutputFilename, int a, int b) throws IOException {
        String text = String.join("", Files.readAllLines(Paths.get(inputFilename)));
        try (OutputStream out = new FileOutputStream("lab3/" + mainOutputFilename + ".txt")) {
            for (int i = 0; i < text.length() / 2; i++) {
                out.write(mapper.map(text.substring(2 * i, 2 * i + 2), a, b).getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String perform(StringMapperWithKeys mapper, String text, int a, int b) throws IOException {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < text.length() / 2; i++) {
            res.append(mapper.map(text.substring(2 * i, 2 * i + 2), a, b));
        }
        return res.toString();
    }


    static int bigramToInt(String bigram) {
        int first = bigram.charAt(0) - 1072;
        int second = bigram.charAt(1) - 1072;
        return (first - ((first > 25) ? 1 : 0)) * LAB3_ALPHABET.length()
                + (second - ((second > 25) ? 1 : 0));
    }

    static String rusEncrypt(String bigram, int a, int b) {
        return intToBigram((bigramToInt(bigram) * a + b) % M_SQUARED);
    }

    static String rusDecrypt(String bigram, int a, int b) {
        return intToBigram(Math.floorMod((bigramToInt(bigram) - b) * a/*inverse(a, M_SQUARED)*/, M_SQUARED));
    }

    static String intToBigram(int x) {
        int first = x / LAB3_ALPHABET.length();
        int second = Math.floorMod(x, LAB3_ALPHABET.length());
        return "" + (char) (first + 1072 + ((first > 25) ? 1 : 0)) + (char) (second + 1072 + ((second > 25) ? 1 : 0));
    }

//////////////////////////////////

    static boolean rusLangRecogniser(Path path) throws IOException {
        //Map<String, Double> rusGrams = readGrams("lab1/2_grams_no_spaces_cross_voyna-i-mir-tom-1.tsv");
        final double SMOOTHER = Collections.min(RUS_BIGRAMS_PROBABILITIES.values()) / 100.;///1000000.;
        double nonRusBigramBit = Math.log(1.0 / (LAB3_ALPHABET.length() * LAB3_ALPHABET.length()));
        double rusScore = 0;
        double nonRusScore = 0;
        String text = String.join("", Files.readAllLines(path));
        for (int i = 0; i < text.length() / 2; i++) {
            String bigram = text.substring(i, i + 2);
            rusScore += Math.log(RUS_BIGRAMS_PROBABILITIES.getOrDefault(bigram, SMOOTHER));
            nonRusScore += nonRusBigramBit;
        }
        System.out.println(nonRusScore / rusScore);
        return rusScore >= nonRusScore;
    }

    static boolean rusLangRecogniser(String text) throws IOException {
        final double SMOOTHER = Collections.min(RUS_BIGRAMS_PROBABILITIES.values()) / 1000.;/// / 1000000.;
        double nonRusBigramBit = Math.log(1.0 / (LAB3_ALPHABET.length() * LAB3_ALPHABET.length()));
        double rusScore = 0;
        double nonRusScore = 0;
        for (int i = 0; i < text.length() / 2; i++) {
            String bigram = text.substring(i, i + 2);
            rusScore += Math.log(RUS_BIGRAMS_PROBABILITIES.getOrDefault(bigram, SMOOTHER));
            nonRusScore += nonRusBigramBit;
        }
        //System.out.println(nonRusScore / rusScore);
        return rusScore >= nonRusScore;
    }


}
