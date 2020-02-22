package main;

import lab1.Text;
import lab1.Vigenere;
import lab1.VigenereCipher;

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


       //VigenereCipher.encrypt("texts/test.txt", "lab2/en_test.txt","котойкак");


      //VigenereCipher.decrypt("lab2/en_test.txt", "lab2/de_test.txt", "котойкак");
       // System.out.println("texts/test.txt".replaceAll(".+/\\b*", ""));
        //VigenereCipher.getPeriod("приветмир");

        //VigenereCipher.compareIndexesOfCoincidence("lab2/de_test.txt", "lab2/en_test.txt","test4.tsv" ,2, 23);

        //VigenereCipher.lab2Task2("texts/test.txt", "lab2/task12/en_test.txt", "lab2/task12/test");

        /*try {
            Runtime.getRuntime().exec("python plot.py hello world" );
        } catch (Exception e) {
            e.printStackTrace();
        }*/


       VigenereCipher.perform(VigenereCipher::encryptChar, "texts/test.txt", "lab2/en_test.txt","ывапрокеукенготимавкенр");
        VigenereCipher.perform(VigenereCipher::decryptChar, "lab2/en_test.txt", "lab2/de_test.txt", "ывапрокеукенготимавкенр");
        /*System.out.println((char)((int)',' + (int)'к'));
        System.out.println((char)((int)',' + (int)'о'));
        System.out.println((char)((int)',' + (int)'т'));
        System.out.println((char)((int)',' + (int)'э'));
        System.out.println((char)((int)',' + (int)'й'));
        System.out.println((char)((int)',' + (int)'к'));
        System.out.println((char)((int)',' + (int)'а'));*/
/*

        System.out.println(VigenereCipher.decryptChar(VigenereCipher.encryptChar(',', 0, "котєйка"), 0, "котєйка"));
        System.out.println(VigenereCipher.decryptChar(VigenereCipher.encryptChar(',', 1, "котєйка"), 1, "котєйка"));
        System.out.println(VigenereCipher.decryptChar(VigenereCipher.encryptChar(',', 2, "котєйка"), 2, "котєйка"));
        System.out.println(VigenereCipher.decryptChar(VigenereCipher.encryptChar(',', 3, "котєйка"), 3, "котєйка"));
        System.out.println(VigenereCipher.decryptChar(VigenereCipher.encryptChar(',', 4, "котєйка"), 4, "котєйка"));
        System.out.println(VigenereCipher.decryptChar(VigenereCipher.encryptChar(',', 5, "котєйка"), 5, "котєйка"));
        System.out.println(VigenereCipher.decryptChar(VigenereCipher.encryptChar(',', 6, "котєйка"), 6, "котєйка"));
*/


        System.out.println((int)',');
        System.out.println((int)'\n');
        System.out.println((int)'й');
    }
}
