public class OFileTester {
	public static void main(String... pumpkins) {

		/* Create tester.txt  */
		OFile ofile = new OFile("tester.txt");

		/* Writing to file */
		ofile.write("Line one");
		ofile.write(" - addition to same line");
		ofile.write("\nThis is a new line!");
		ofile.close();

		/* Reading line */
		System.out.println(ofile.read());

		/* Append to file */
		ofile.write(" appended text", true);
		ofile.write("\nNew line appended.");
		ofile.close();

		/* Reading whole file */
		System.out.println(ofile.readFile());

		/* Clear file */
		ofile.clear();

		/* Speed writing test */
		double startTime = System.nanoTime();
		System.out.printf("\nWriting 1,000,000 lines... ");
		for (int i = 0; i < 1E7; i++)
			ofile.write(i + "\n");
		ofile.close();
		System.out.printf("done in %f seconds.\n", (System.nanoTime() - startTime) / 1E9);

		/* Read whole file at once speed test */
		startTime = System.nanoTime();
		System.out.printf("Reading whole file at once to string... ");
		String fileContents = ofile.readFile();
		System.out.printf("done in %f seconds.\n", (System.nanoTime() - startTime) / 1E9);

		/* Read whole file line by line speed test */
		startTime = System.nanoTime();
		System.out.printf("Reading whole file line by line... ");
		for (int i = 0; i < 1E7; i++)
			ofile.read();
		ofile.close();
		System.out.printf("done in %f seconds.\n", (System.nanoTime() - startTime) / 1E9);

		System.out.println("Note: It would take much longer to save to string line by line\n");

		/* Delete file */
		ofile.delete();
	}
}