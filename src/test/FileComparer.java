package test;


import ast.SysoutStatement;
import jflex.io.FileUtils;

import java.io.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class FileComparer {

    public static boolean rEqual(String path1, String path2) throws IOException{


        BufferedReader reader1 = new BufferedReader(new FileReader(path1));

        BufferedReader reader2 = new BufferedReader(new FileReader(path2));

        String line1 = reader1.readLine();

        String line2 = reader2.readLine();

        boolean areEqual = true;

        int lineNum = 1;

        while (line1 != null || line2 != null) {
            if (line1 == null || line2 == null) {
                areEqual = false;

                break;
            } else if (!line1.equalsIgnoreCase(line2)) {
                areEqual = false;

                break;
            }

            line1 = reader1.readLine();

            line2 = reader2.readLine();

            lineNum++;
        }

        if (areEqual) {
            System.out.println("Two files have same content.");

            reader1.close();
            reader2.close();
            return true;

        } else {
            System.out.println("Two files have different content. They differ at line " + lineNum);

            System.out.println("File1 has " + line1 + " and File2 has " + line2 + " at line " + lineNum);

            reader1.close();
            reader2.close();
            return false;
        }


    }

    public  static void main(String[] args) throws IOException {

            File dir1 = new File(args[0]);
            File dir2 = new File(args[1]);

            int n = dir1.listFiles().length;

            for(int i=0; i<n; i++){
                String path1 = dir1.listFiles()[i].getAbsolutePath();
                String path2 = dir2.listFiles()[i].getAbsolutePath();
               if(!rEqual(path1, path2)){
                   System.out.println("Something's Wrong with test number " + i+1 );
               }
            }
            System.out.println("Everything is great!");
    }

}

