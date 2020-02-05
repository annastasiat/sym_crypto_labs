package main;


import lab1.Text;

public class Main {

    public static void main(String[] args) {

        String filename = "Улисс.txt";//"Отверженные. Виктор Гюго.txt";//"voyna-i-mir-tom-1.txt";
        Text text = new Text(filename);

        System.out.println("\nText length(symbols): " + text.getLength());
        System.out.println("H1: " + text.H(1));
        System.out.println("H2: " + text.H(2));
        text.printNgrams(1);
    }
}
