package labs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public interface AffineCipher {
    String TAB = "\t";
    String LAB3_ALPHABET = "абвгдежзийклмнопрстуфхцчшщьыэюя";
    Map<String, Double> RUS_BIGRAMS_PROBABILITIES = readGrams("lab1/2_grams_no_spaces_cross_voyna-i-mir-tom-1.tsv");
    int M_SQUARED = LAB3_ALPHABET.length() * LAB3_ALPHABET.length();
    String INPUT_DIR = "variants.utf8/";

    //TODO make readable
    static int inverse(int a, int b) {
        assert (gcd(a, b) == 1);
        int x = a;
        int y = b;
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
    }

    static int gcd(int a, int b) {
        if (b == 0) return a;
        else return gcd(b, Math.floorMod(a, b));
    }

    static List<Integer> solveEq(int a, int b, int n) {
        int d = gcd(a, n);
        List<Integer> roots = new ArrayList<>();
        if (Math.floorMod(b, d) == 0) {
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

    static String perform(TriFunction<String, Integer, Integer, String> mapper, String text, int a, int b) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < text.length() / 2; i++) {
            res.append(mapper.apply(text.substring(2 * i, 2 * i + 2), a, b));
        }
        return res.toString();
        /*return  IntStream.iterate(0, i->i+2)
                        .limit(text.length()/2)
                        .mapToObj(i ->mapper.apply(text.substring(i, i + 2), a, b))
                        .collect(Collectors.joining(""));*/

    }

    static void findKeys(String mainFilename) throws IOException {

        Text textObj = new Text(mainFilename, INPUT_DIR, "lab3/", LAB3_ALPHABET);

        List<Map.Entry<String, Double>> textBigramsSorted = textObj.ngrams(2, false).entrySet()
                .stream().sorted(Comparator.comparingDouble(Map.Entry<String, Double>::getValue).reversed())
                .collect(Collectors.toList());

        List<Map.Entry<String, Double>> rusBigramProbSorted = RUS_BIGRAMS_PROBABILITIES.entrySet()
                .stream().sorted(Comparator.comparingDouble(Map.Entry<String, Double>::getValue).reversed())
                .collect(Collectors.toList());

        String text = textObj.getStr();

        //textObj.printNgrams(2, false, 1);

        int numberOfBigrams = 10;
        for (int i = 0; i < numberOfBigrams; i++) {
            for (int j = 0; j < numberOfBigrams; j++) {
                for (int k = 0; k < numberOfBigrams; k++) {
                    for (int m = 0; m < numberOfBigrams; m++) {
                        if (i == k || j == m) continue;

                        int y1 = bigramToInt(textBigramsSorted.get(i).getKey());
                        int x1 = bigramToInt(rusBigramProbSorted.get(j).getKey());
                        int y2 = bigramToInt(textBigramsSorted.get(k).getKey());
                        int x2 = bigramToInt(rusBigramProbSorted.get(m).getKey());

                        List<Integer> roots = solveEq(x1 - x2, y1 - y2, M_SQUARED);

                        for (int a : roots) {

                            if (gcd(a, M_SQUARED) != 1) continue;

                            int b = Math.floorMod(y1 - a * x1, M_SQUARED);
                            String deText = perform(AffineCipher::rusDecrypt, text, inverse(a, M_SQUARED), b);
                            if (rusLangRecogniser(deText)) {
                                System.out.println("a: " + a + "  b: " + b);
                                Files.write(Paths.get("lab3/key_" + mainFilename + ".txt"), ("a: " + a + "  b: " + b).getBytes());
                                Files.write(Paths.get("lab3/" + mainFilename + "__" + a + "_" + b + ".txt"), deText.getBytes());
                                return;
                            }

                        }

                    }
                }
            }
        }
    }


    static int bigramToInt(String bigram) {
        return LAB3_ALPHABET.indexOf(bigram.charAt(0)) * LAB3_ALPHABET.length() + LAB3_ALPHABET.indexOf(bigram.charAt(1));
    }

    static String intToBigram(int x) {
        return "" + LAB3_ALPHABET.charAt(x / LAB3_ALPHABET.length()) + LAB3_ALPHABET.charAt(Math.floorMod(x, LAB3_ALPHABET.length()));
    }

    static String rusEncrypt(String bigram, int a, int b) {
        return intToBigram((bigramToInt(bigram) * a + b) % M_SQUARED);
    }

    static String rusDecrypt(String bigram, int aInversed, int b) {
        return intToBigram(Math.floorMod((bigramToInt(bigram) - b) * aInversed, M_SQUARED));
    }


//////////////////////////////////

    static boolean rusLangRecogniser(String text) {
        final double SMOOTHER = Collections.min(RUS_BIGRAMS_PROBABILITIES.values()) / 1000d;/// / 1000000.;
        double nonRusBigramBit = Math.log(1d / (M_SQUARED));
        double rusScore = 0;
        double nonRusScore = 0;
        for (int i = 0; i < text.length() / 2; i++) {
            String bigram = text.substring(i, i + 2);
            rusScore += Math.log(RUS_BIGRAMS_PROBABILITIES.getOrDefault(bigram, SMOOTHER));
            nonRusScore += nonRusBigramBit;
        }
        System.out.println(nonRusScore / rusScore);
        return rusScore >= nonRusScore;
    }

}
