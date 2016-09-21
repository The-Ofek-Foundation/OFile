# OFile
Super fast and simple FileIO

## Features

Switches between file input and output automatically, quickly and intuitively. Reads and writes using BufferedReaders and BufferedWriters. This can also read a whole file to string *very* quickly.

Example usage:

OFile ofile = new OFile("tester.txt");
ofile.write("hello");
System.out.println(ofile.read());

Prints: hello

For more extensive sampling, check OFileTester.java (or even run it!)

Enjoy
