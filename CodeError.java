
public class CodeError extends Throwable {

	private final String errorMethodName;

	public CodeError(Throwable throwable, String errorMethodName) {
		super(throwable);
		this.errorMethodName = errorMethodName;
		removeSuppressedStackTraces();
	}

	private void removeSuppressedStackTraces() {
		StackTraceElement[] stackTrace = getCause().getStackTrace();
		int realTraceCount = 0;
		for (int i = 0; i < stackTrace.length; i++)
			if (!suppressedClassName(stackTrace[i].getClassName()))
				realTraceCount++;

		StackTraceElement[] realTrace = new StackTraceElement[realTraceCount];
		int count = 0;
		for (int i = 0; i < stackTrace.length; i++)
			if (!suppressedClassName(stackTrace[i].getClassName())) {
				realTrace[count] = stackTrace[i];
				count++;
			}
		setStackTrace(realTrace);
	}

	private boolean suppressedClassName(String className) {
		if (className.equals("CodeTester"))
			return true;
		if (className.indexOf("sun.reflect.") == 0)
			return true;
		if (className.indexOf("java.lang.reflect.") == 0)
			return true;
		return false;
	}

	public String getMethodName() {
		return errorMethodName;
	}

	@Override
	public void printStackTrace() {
		System.out.println(this);
		StackTraceElement[] trace = getStackTrace();
		for (StackTraceElement traceElement : trace)
			System.out.println("\tat " + traceElement);
	}
}
