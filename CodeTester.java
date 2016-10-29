import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;

import java.util.List;
import java.util.ArrayList;

public abstract class CodeTester {

	public static final String TEST_METHOD_PREFIX = "_test";
	private int testsOk, testsFail, testsTotal;
	private List<CodeError> codeErrors;

	public CodeTester() {
		testsOk = testsFail = testsTotal = 0;
		codeErrors = new ArrayList<CodeError>();
	}

	public void runTest(CodeTester childTester, Method testMethod) {
		String methodName = parseMethodName(testMethod.getName());
		System.out.printf("%-50s --> ", methodName);

		try {
			testMethod.invoke(childTester);
			System.out.println("ok");
			testsOk++;
		} catch (InvocationTargetException | IllegalAccessException ie) {
			System.out.println("FAIL");
			codeErrors.add(new CodeError(ie.getCause(), methodName));
			testsFail++;
		} catch (AssertionError | Exception e) {
			System.out.println("FAIL");
			codeErrors.add(new CodeError(e, methodName));
			testsFail++;
		}
		testsTotal++;
	}

	public void printSummary(double elapsedTime) {
		System.out.println();
		if (testsFail > 0) {
			System.out.printf("!!!! %d test%s failed out of %d tests total "
				+ "in %f seconds.\n", testsFail, testsFail == 1 ? "":"s",
				testsTotal, elapsedTime);

			for (CodeError codeError : codeErrors) {
				System.out.printf("\n%s -> FAIL\n\n", codeError.getMethodName());
				codeError.printStackTrace();
			}
		} else System.out.printf("Passed all %d tests in %f seconds!\n",
			testsTotal, elapsedTime);
		System.out.println();
	}

	public final void runTests(CodeTester childTester) {
		System.out.println();
		Class c = childTester.getClass();
		double startTime = System.nanoTime();
		for (Method method : c.getDeclaredMethods())
			if (isTestMethod(method))
				runTest(childTester, method);
		printSummary((System.nanoTime() - startTime) / 1E9);
	}

	private boolean isTestMethod(Method method) {
		String methodName = method.getName();
		int prefixLength = TEST_METHOD_PREFIX.length();

		return methodName.length() > prefixLength + 1 &&
			methodName.substring(0, prefixLength).equals(TEST_METHOD_PREFIX) &&
			Character.isUpperCase(methodName.charAt(prefixLength)) &&
			Modifier.isPublic(method.getModifiers());
	}

	private String parseMethodName(String methodName) {
		String testName = "Test";
		char tempChar;

		for (int i = TEST_METHOD_PREFIX.length(); i < methodName.length(); i++) {
			tempChar = methodName.charAt(i);
			if (Character.isUpperCase(tempChar))
				testName += " ";
			testName += Character.toLowerCase(tempChar);
		}

		return testName;
	}

	public void assertEqual(String s1, String s2, boolean equal) {
		assert s1.equals(s2) == equal :
			String.format("\"%s\" %s \"%s\"", s1, equal ? "!=":"=", s2);
	}

	public void assertEqual(int i1, int i2, boolean equal) {
		assert (i1 == i2) == equal :
			String.format("%d %s %d!", i1, equal ? "!=":"=", i2);
	}

	public void assertNull(Object obj, boolean nll) {
		assert (obj == null) == nll :
			String.format("%s %s null.", obj.toString(), nll ? "!=":"=");
	}

	public void assertEqual(Object obj1, Object obj2, boolean equal) {
		assert obj1.equals(obj2) == equal :
			String.format("%s %s %s", obj1.toString(), equal ? "!=":"=",
				obj2.toString());
	}
}
