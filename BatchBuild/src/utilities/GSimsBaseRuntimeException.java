package utilities;

public class GSimsBaseRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -7907207449299504576L;

	public GSimsBaseRuntimeException(final String message) {
		super(message);
	}

	public GSimsBaseRuntimeException(final Throwable cause) {
		super(cause);
	}

	public GSimsBaseRuntimeException(final String message, final Throwable cause) {
		super(message, cause);
	}
}