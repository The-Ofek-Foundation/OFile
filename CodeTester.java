import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;

public abstract class CodeTester {

	public static final String TEST_METHOD_PREFIX = "_test";
	private int testsOk, testsFail, testsError, testsTotal;

	public CodeTester() {
		testsOk = testsFail = testsError = testsTotal = 0;
	}

	public void runTest(CodeTester childTester, Method testMethod) {
		System.out.printf("%-50s --> ", parseMethodName(testMethod.getName()));
		try {
			testMethod.invoke(childTester);
			System.out.println("ok");
			testsOk++;
		} catch (InvocationTargetException ite) {
			System.out.println("FAIL\n");
			ite.getCause().printStackTrace();
			System.out.println();
			testsFail++;
		} catch (IllegalAccessException iae) {
			System.out.println("ERR\n");
			iae.getCause().printStackTrace();
			System.out.println();
			testsError++;
		} catch (AssertionError ae) {
			System.out.println("FAIL\n");
			ae.printStackTrace();
			System.out.println();
			testsFail++;
		} catch (Exception err) {
			System.out.println("FAIL\n");
			err.printStackTrace();
			System.out.println();
			testsFail++;
		}
		testsTotal++;
	}

	public void printSummary() {
		System.out.println();
		if (testsFail > 0)
			System.out.printf("!!!! %d test%s failed out of %d tests total.\n",
				testsFail, testsFail == 1 ? "":"s", testsTotal);
		else System.out.printf("Passed all %d tests!\n", testsTotal);
		System.out.println();
	}

	public final void runTests(CodeTester childTester) {
		Class c = childTester.getClass();
		for (Method method : c.getDeclaredMethods())
			if (isTestMethod(method))
				runTest(childTester, method);
		printSummary();
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