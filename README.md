# Control-positions-creator

Purpose of this program is to create a control group to our case files in a research project.

Problem in this program is that: we have a folder full of case files, each of these case files includes a number of SNP's in them. And we also have a larger folder of control files. 
We need to pick a group of control files; for every case file, there should be a corresponding control file that includes same number of SNP's and also holds the SNP ids form the same chromosome.
In the end program creates a new folder, where these picked control files are copied.

Program also needs a file which is listing the case files, file in a similar format is created by the program.
Every case position in this file is replaced by its control match.

This file's format is like this:
chr pos1 chr pos2 
chr10 3142500 chr 10 3172500 

first four column in a line should be these, other columns are disregarded.



     * args[0] is the folder of the (possible) control files 
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
