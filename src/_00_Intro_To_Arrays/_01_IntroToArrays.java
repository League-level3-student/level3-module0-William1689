package _00_Intro_To_Arrays;

import java.util.Random;

import javax.swing.JOptionPane;

public class _01_IntroToArrays {
    public static void main(String[] args) {
        // 1. declare and Initialize an array 5 Strings
    	String words[] = new String[5];
    	Random ran = new Random();
    	
    	words[1]="gerald";
    	words[2]="bob";
    	words[3]="banana";
    	words[4]="apple";
    	words[0]="green";
        // 2. print the third element in the array
    	System.out.println(words[3]);
        // 3. set the third element to a different value
    	words[3] = "papple";
        // 4. print the third element again
    	System.out.println(words[3]);
        // 5. use a for loop to set all the elements in the array to a string
        //    of your choice
//    	for(int i = 0; i<words.length; i++){
//    		String ask = JOptionPane.showInputDialog("pick a word");
//    		words[i]=ask;
//    	
//    	}
        // 6. use a for loop to print all the values in the array
        //    BE SURE TO USE THE ARRAY'S length VARIABLE
    	for(int i =0;i<words.length;i++) {
    		System.out.println(words[i]);
    	}
        // 7. make an array of 50 integers
    	     int[] num = new int[50];
    	// 8. use a for loop to make every value of the integer array a random
        //    number
    	     for(int i = 0;i<num.length;i++){
    	    	num[i] = ran.nextInt();
    	    	System.out.println(num[i]);
    	    
    	
    	    	
    	     }
    	   
        // 9. without printing the entire array, print only the smallest number
        //    on the array
    	     
        // 10 print the entire array to see if step 8 was correct

        // 11. print the largest number in the array.

        // 12. print only the last element in the array
    	     System.out.println(num[49]);
    }
}
