package utilities;

/**
 * FAException is an object than can be used to 'wrap'
 * an exception object.
 *
 * The purpose is to keep the stack trace clutter free
 * and offer a couple of utility methods to modify the
 * exception message.
 *
 */
public class GSimsException extends Exception {
	/**
	 *
	 */
	private static final long serialVersionUID = -6484012685622929445L;

	String message = "";

	public GSimsException(final String exceptionMessage) {
		message = exceptionMessage;
	}

	/**
	 * FAException constructor accepts an exception message and
	 * the caught exception. If the exception message does not contain
	 * a 'Method: ' message, the constructor will read the first stack
	 * trace element, obtain the method, and prepend this to the message.
	 *
	 * This method will take the stack trace of the passed in exception,
	 * and assign it to this exception.
	 *
	 * @param exceptionMessage
	 * @param e
	 */
	public GSimsException(final String exceptionMessage, final Exception e) {
		super.setStackTrace(e.getStackTrace());
		message = exceptionMessage + e.toString();
		final StackTraceElement[] stack = e.getStackTrace();
		if (!message.contains("Class: ") && !message.contains(" Method: ") && stack.length > 0) {
			prependMessage("Class: " + stack[0].getClassName() + " Method: " + stack[0].getMethodName() + "() Message: ");
		}
	}

	@Override
	public String getMessage() {
		return message;
	}

	public void prependMessage(final String exceptionMessage) {
		message = exceptionMessage + message;
	}

	public void appendMessage(final String exceptionMessage) {
		message += exceptionMessage;
	}
}
