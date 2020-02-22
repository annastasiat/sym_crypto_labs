package main;

import labs.Text;
import labs.VigenereCipher;

public class Main {
    public static void show(Text text, boolean cross, double H0){
        double H;
        H = text.H(1, cross);
        System.out.println("H1: " + H);
        System.out.println("Redundancy(H1): " + (1-H/H0));
        H = text.H(2, cross);
        System.out.println("H2: " + H);
        System.out.println("Redundancy(H2): " + (1-H/H0) + "\n");
        //text.printNgrams(1, cross, 100000);
        //text.printNgrams(2, cross, 100000);

    }

    public static void main(String[] args) {

        String rusAlphSpace = " абвгдеёжзийклмнопрстуфхцчшщъыьэюя";

        Text text = new Text("voyna-i-mir-tom-1",  "абвгдежзийклмнопрстуфхцчшщъыьэюя");
        text.printNgrams(1,true, 1);


        VigenereCipher.printIndexesOfCoincidence("texts/var12_lab2.txt",  "lab2/task3/var12_lab2_ic.tsv", 2, 31);
/*
        try {
            Runtime.getRuntime().exec("python plot_ic.py lab2/task3/var12_lab2.tsv lab2/task3/var12_lab2.png");
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        //VigenereCipher.perform(VigenereCipher::encryptChar, "texts/test.txt", "lab2/en_test.txt","ывапрокеукенготимавкенр");
        //VigenereCipher.perform(VigenereCipher::decryptChar, "lab2/en_test.txt", "lab2/de_test.txt", "ывапрокеукенготимавкенр");


        VigenereCipher.findKey("texts/var12_lab2.txt", "lab2/task3/var12_lab2",
                "lab1/voyna-i-mir-tom-11_grams_no_spaces___no_cross___.tsv",14);

        System.out.println((int)',');
        System.out.println((int)'\n');
        System.out.println((int)'й');
    }
}
