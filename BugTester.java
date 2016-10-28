import java.util.Arrays;

/**
 * Tests {@link OFile} for bugs.
 * Run with -ea parameter
 */
public class BugTester {

	private final String TEST_FILE_NAME;
	private final String TEST_DIR_NAME;
	private final String COPY_FILE_NAME;
	private final String RENAMED_FILE_NAME;
	private OFile testFile;

	public BugTester(String fileName) {
		TEST_FILE_NAME = fileName;
		TEST_DIR_NAME = "test_dir";
		COPY_FILE_NAME = "copy_" + fileName;
		RENAMED_FILE_NAME = "renamed_" + fileName;
		testFile = null;
	}

	private void setupFile() {
		testFile = new OFile("tester.txt");
	}

	private void cleanupFile() {
		testFile.delete();
		new OFile(COPY_FILE_NAME).delete();
		new OFile(TEST_DIR_NAME + "/").delete();
		new OFile(TEST_DIR_NAME + "2/").delete();
	}

	interface Test {
		void test();
	}

	private void runTest(String testName, Test test) {
		System.out.printf("%-50s --> ", testName);
		try {
			test.test();
			System.out.println("ok");
		} catch (AssertionError ae) {
			System.out.println("FAIL\n");
			ae.printStackTrace();
			System.out.println();
		} catch (Exception err) {
			System.out.println("FAIL\n");
			err.printStackTrace();
			System.out.println();
		}
	}

	private void testCreateDelete(String fileName) {
		assertExists(fileName, false);

		OFile testFile = new OFile(fileName);

		assertExists(testFile, true);
		assertEmpty(testFile);

		assertDelete(testFile);
	}

	private void testCreateDeleteDirectory() {
		String filePath = TEST_DIR_NAME + "/" + TEST_FILE_NAME;

		assertExists(filePath, false);
		assertExists(TEST_DIR_NAME, false);

		testFile = new OFile(filePath);

		assertExists(filePath, true);
		assertExists(TEST_DIR_NAME, true);
		assertEmpty(testFile);

		assertDelete(testFile);
		assertDelete(testFile.getParentFile());
	}

	private void testReadWrite() {
		testFile.write("hello world");
		testFile.write(" and goodnight moon.\n");
		testFile.write("new line");

		assertEqual(testFile.read(), "hello world and goodnight moon.");
		assertEqual(testFile.read(), "new line");
		assertNull(testFile.read(), true);
		assertEqual(testFile.readFile(),
			"hello world and goodnight moon.\nnew line");

		assertClear(testFile);
	}

	private void testLargeReadWrite() {
		int numLines = (int)1e4;
		for (int i = 0; i < numLines; i++)
			testFile.write(String.format("Line Num: %d\n", (i + 1)));

		assertEqual(countCharacters(testFile.readFile(), '\n'), numLines);

		for (int i = 0; i < numLines; i++)
			assertEqual(testFile.read(),
				String.format("Line Num: %d", (i + 1)));

		assertClear(testFile);
	}

	private void testFileCopy() {
		assertExists(COPY_FILE_NAME, false);

		testFile.write("copy this");
		OFile copiedFile = testFile.copyReplace(COPY_FILE_NAME);

		// Test that copied file is the same
		assertExists(copiedFile, true);
		assertEqual(copiedFile.readFile(), testFile.readFile());
		assertEqual(copiedFile.getChecksum(), testFile.getChecksum());
		assertEqual(copiedFile, testFile, true);

		// Test that changes in copy file result in changes in equality
		copiedFile.clear();
		assertEqual(copiedFile, testFile, false);
		testFile.copyReplace(COPY_FILE_NAME);
		assertEqual(copiedFile, testFile, true);

		copiedFile = assertRenaming(copiedFile, RENAMED_FILE_NAME);
		assertEqual(copiedFile, testFile, true);

		assertDelete(copiedFile);
		assertClear(testFile.clear());
	}

	private void testFileCopyIntoDirectory() {
		String filePath = TEST_DIR_NAME + "/" + COPY_FILE_NAME;

		assertExists(TEST_DIR_NAME, false);

		testFile.write("something to copy");
		OFile copiedFile = testFile.copyReplace(filePath);

		assertExists(copiedFile, true);
		assertEqual(copiedFile.readFile(), testFile.readFile());
		assertEqual(copiedFile.getChecksum(), testFile.getChecksum());
		assertEqual(copiedFile, testFile, true);

		assertDelete(copiedFile);
		assertDelete(copiedFile.getParentFile());
		assertClear(testFile.clear());
	}

	private void testDirectoryCopyWithFile() {
		String file1Path = TEST_DIR_NAME + "/" + COPY_FILE_NAME;
		String file2Path = TEST_DIR_NAME + "2/" + COPY_FILE_NAME;

		assertExists(TEST_DIR_NAME, false);
		assertExists(TEST_DIR_NAME + "2", false);

		OFile copyFile1 = new OFile(file1Path);
		copyFile1.write("some random\nlines to copy").write("and some more");

		OFile copyFile2 = copyFile1.copyReplace(file2Path);

		assertEqual(copyFile1.getParentFile(), copyFile2.getParentFile(), true);

		copyFile2.write("\nslight modification");
		assertEqual(copyFile1.getParentFile(), copyFile2.getParentFile(),
			false);

		copyFile1.copyReplace(file2Path);
		assertEqual(copyFile1.getParentFile(), copyFile2.getParentFile(), true);

		copyFile2 = assertRenaming(copyFile2, RENAMED_FILE_NAME);
		assertEqual(copyFile1.getParentFile(), copyFile2.getParentFile(),
			false);

		assertDelete(TEST_DIR_NAME + "/");
		assertDelete(TEST_DIR_NAME + "2/");
	}

	public void runTests() {
		System.out.println();

		runTest("Test file creation and deletion", () -> testCreateDelete(TEST_FILE_NAME));
		runTest("Test directory creation and deletion", () -> testCreateDelete(TEST_DIR_NAME + "/"));
		runTest("Test file creation and deletion within directory", () ->
			testCreateDeleteDirectory());

		setupFile();

		runTest("Test read write", () -> testReadWrite());
		runTest("Test large read write", () -> testLargeReadWrite());
		runTest("Test file copying", () -> testFileCopy());
		runTest("Test file copying into a directory", () ->
			testFileCopyIntoDirectory());
		runTest("Test directory copying with file", () ->
			testDirectoryCopyWithFile());

		cleanupFile();

		System.out.println();
	}

	private void assertEqual(String s1, String s2) {
		assert s1.equals(s2) :
			String.format("\"%s\" != \"%s\"", s1, s2);
	}

	private void assertEqual(int i1, int i2) {
		assert i1 == i2 : String.format("%d != %d!", i1, i2);
	}

	private void assertEqual(byte[] b1, byte[] b2) {
		assert Arrays.equals(b1, b2) :
			"Byte arrays not equal!";
	}

	private void assertEqual(OFile f1, OFile f2, boolean equal) {
		assert f1.equalsIgnoreName(f2) == equal :
			String.format("%s %s %s", f1.getPath(), equal ? "!=":"==",
			f2.getPath());
	}

	private void assertNull(Object o, boolean nll) {
		assert (o == null) == nll : o + " should be null!";
	}

	private void assertEmpty(OFile file) {
		if (file.isDirectory())
			assert file.listFiles().length == 0 :
				String.format("Directory %s is not empty.", file.getPath());
		else assert file.readFile().length() == 0 :
			String.format("File %s is not empty.", file.getPath());
	}

	private void assertExists(String path, boolean exists) {
		assert OFile.fileExists(path) == exists :
			String.format("File %s %s!", path,
				exists ? "does not exist":"already exists");
	}

	private void assertExists(OFile file, boolean exists) {
		assert file.exists() == exists :
			String.format("File %s %s!", file.getPath(),
				exists ? "does not exist":"already exists");
		assertExists(file.getPath(), exists);
	}

	private void assertClear(OFile file) {
		assert file.clear().readFile().length() == 0 :
			String.format("Error clearing file %s!", file.getPath());
	}

	private void assertDelete(OFile file) {
		assert file.delete() :
			String.format("Error deleting file %s!", file.getPath());
		assertExists(file, false);
	}

	private void assertDelete(String path) {
		assertDelete(new OFile(path));
	}

	private OFile assertRenaming(OFile file, String newName) {
		OFile renamedFile = file.renameTo(newName);
		assert renamedFile != null:
			String.format("Error renaming file %s!", file.getPath());
		assertExists(renamedFile, true);
		return renamedFile;
	}

	private int countCharacters(String s, char c) {
		int count = 0, len = s.length();
		for (int i = 0; i < len; i++)
			if (s.charAt(i) == c)
				count++;
		return count;
	}

	public static void main(String... pumpkins) {
		BugTester BT = new BugTester("tester.txt");
		BT.runTests();
	}
}