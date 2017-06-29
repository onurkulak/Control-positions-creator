/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controlgroupcreator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Comparator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 *
 * @author onur
 */
public class ControlGroupCreator {

    /**
     * @param args the command line arguments
     * args[0] is the folder of the control files 
     * args[1] is the folder of the case files
     * args[2] is the file that holds the case matches
     * 
     * in the end of the program a folder will be created 
     * consisting of some of the control files, each file in this folder
     * corresponds to another file in the case files.
     * 
     * these matching files-positions have same number of SNP's 
     * and are in same chromosomes, but actually they are different positions
     * 
     * at the end of the program another file will be created that has the same 
     * structure with the given case matches, 
     * but this file replaces every case position with the matched control position
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        File controlDirectory = new File(args[0]);
        File caseDirectory = new File(args[1]);
        String folderName = "filtered_control";
        new File(folderName).mkdir();
        //folders are taken as inputs, and output folder is created
        File[] listOfControlFiles = controlDirectory.listFiles();
        File[] listOfCaseFiles = caseDirectory.listFiles();
        
        //every matching event of case and control positions kept in this map
        //to later replace them in the newly created positions file
        HashMap<String, String> positions = new HashMap(listOfCaseFiles.length * 4 / 3);

        //arraylists for every chromosome, 
        //each control-case pair must be in the same chromosome
        //a position object holds the number of SNP's in that position file 
        //and the name of the position
        ArrayList<Position>[] caseList = new ArrayList[23];
        ArrayList<Position>[] controlList = new ArrayList[23];
        for (int i = 0; i < caseList.length; i++) {
            caseList[i] = new ArrayList<>();
            controlList[i] = new ArrayList<>();
        }
        
        //arraylists are filled with positions, their name and SNP count is taken
        //and each position is put into the respective chromosome arraylist
        for (File listOfControlFile : listOfControlFiles) {
            String name = listOfControlFile.getName();
            int count = countSNP(listOfControlFile);
            int t = name.indexOf("_");
            int chrNumber;
            try{
                chrNumber = Integer.parseInt(name.substring(3, t));
            }
            catch(Exception E){
                chrNumber = 23;
            }
            controlList[chrNumber - 1].add(new Position(name, count));
        }
        for (File listOfCaseFile : listOfCaseFiles) {
            String name = listOfCaseFile.getName();
            int count = countSNP(listOfCaseFile);
            int t = name.indexOf("_");
            int chrNumber;
            try{
                chrNumber = Integer.parseInt(name.substring(3, t));
            }
            catch(Exception E){
                chrNumber = 23;
            }
            caseList[chrNumber - 1].add(new Position(name, count));
        }

        //all arraylists are sorted according to their number of SNP's
        //in descending order
        for (int i = 0; i < caseList.length; i++) {
            Collections.sort(caseList[i], new CustomComparator());
            Collections.sort(controlList[i], new CustomComparator());
        }

        //for every chromosome
        for (int i = 0; i < caseList.length; i++) {
            
            //from the arraylists, the files that include same number of SNP's are matched
            //two iterators iterate through the positions, 
            //starting from the positions with lowest number of SNP's in both control and case folders
            for (int j = caseList[i].size() - 1, k = controlList[i].size() - 1; j >= 0 && k >= 0; k--) {
                String controlName = controlList[i].get(k).name;
                String caseName = caseList[i].get(j).name;
                
                //if two files with same number of SNP's are found they are matched
                if (caseList[i].get(j).SNPCount == controlList[i].get(k).SNPCount) {
                    Path sourcePath = Paths.get(args[0] + "/" + controlName);
                    Path targetPath = Paths.get(folderName + "/" + controlName);
                    Files.copy(sourcePath, targetPath);
                    System.out.println(caseName + " and " + controlName + " are matched");
                    positions.put(caseName.substring(0, caseName.length() - 4), controlName.substring(0, controlName.length() - 4));
                    j--;
                } 
                
                //if there's no control file that has the same number of SNP's with a specific case file
                //the smallest control file with a larger number of elements is cropped
                //to include same number of SNP's
                else if (caseList[i].get(j).SNPCount < controlList[i].get(k).SNPCount) {
                    Scanner scan = new Scanner(new File(args[0] + "/" + controlName));
                    String snps = "";
                    for (int z = 0; z < caseList[i].get(j).SNPCount; z++) {
                        snps += scan.next();
                    }
                    FileWriter fw = new FileWriter(folderName + "/" + controlName);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(snps);
                    bw.close();
                    fw.close();
                    System.out.println(caseName + " and " + controlName + " are matched");
                    positions.put(caseName.substring(0, caseName.length() - 4), controlName.substring(0, controlName.length() - 4));
                    j--;
                }
                
                
                //if a case file has no corresponding control file, program prints an error
                if (k == 0 && j != 0) {
                    System.out.println("\nCouldn't find any matching control position");
                }
            }
            System.out.println("\nchr: " + (i + 1) + "  is finished\n");
        }

        //creates new position file for Taha's program
        fixPositionFile(positions, args[2]);

    }

    ///counts number of SNP's in a file
    private static int countSNP(File posFile) throws FileNotFoundException {
        int numberOfSNP = 0;
        try (Scanner scan = new Scanner(posFile)) {
            while (scan.hasNext()) {
                scan.next();
                numberOfSNP++;
            }
        }
        return numberOfSNP;
    }

    //from the created map and the previous positions.txt, creates a new, positions.txt
    private static void fixPositionFile(HashMap<String, String> positions, String arg) throws FileNotFoundException, IOException {
        
        Scanner scan = new Scanner(new File(arg));
        String output = scan.nextLine()+"\n";
        while (scan.hasNextLine()) {
            
            //for every position in the previous file finds its replaced equivalent from the map
            String chr = scan.next();
            String pos1 = scan.next();
            scan.next(); //if hiC file this should be in there
            String pos2 = scan.next();
            scan.nextLine();
            String key1 = chr + "_" + pos1;
            String key2 = chr + "_" + pos2;
            String newVal1 = positions.get(key1);
            newVal1 = newVal1.substring(newVal1.indexOf('_') + 1);
            String newVal2 = positions.get(key2);
            newVal2 = newVal2.substring(newVal2.indexOf('_') + 1);

            //these new positions are written to the new file 
            //overwriting the same places in the previous file
            output += chr + " " + newVal1 + " " + newVal2 + "\n";
        }

        FileWriter fw = new FileWriter("newPositions.txt");
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(output);
        bw.close();
        fw.close();
    }

}

class Position {

    public Position(String pname, int count) {
        name = pname;
        SNPCount = count;
    }
    public String name;
    public int SNPCount;

}

class CustomComparator implements Comparator<Position> {

    @Override
    public int compare(Position o1, Position o2) {
        return o2.SNPCount - o1.SNPCount;
    }
}
