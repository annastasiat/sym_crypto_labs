package main;

import lab1.Text;

public class Main {

    public static void main(String[] args) {
        for(String filename: new String[]{"Отверженные. Виктор Гюго.txt", "Улисс.txt"}){
            Text text = new Text(filename, true);
            System.out.println("\n" + filename);
            System.out.println("with spaces:" );
            System.out.println("Text length(symbols): " + text.getLength());
            System.out.println("H1: " + text.H(1));
            System.out.println("H2: " + text.H(2) + "\n");
            //text.printNgrams(1);
            //text.printNgrams(2);

            text = new Text(filename, false);
            System.out.println(filename);
            System.out.println("with no spaces:" );
            System.out.println("Text length(symbols): " + text.getLength());
            System.out.println("H1: " + text.H(1));
            System.out.println("H2: " + text.H(2));
            //text.printNgrams(1);
            //text.printNgrams(2);
        }

        /*String filename = "Отверженные. Виктор Гюго.txt";//"Улисс.txt";//"voyna-i-mir-tom-1.txt";
        Text text = new Text(filename);


        System.out.println("\nText length(symbols): " + text.getLength());
        System.out.println("H1: " + text.H(1));
        System.out.println("H2: " + text.H(2));

        text.printNgrams(1);
        text.printNgrams(2);*/
    }
}
