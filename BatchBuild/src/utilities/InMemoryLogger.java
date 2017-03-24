package utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

public class InMemoryLogger extends Logger {
	public InMemoryLogger(final String name) {
		super(name);
	}

	private final LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<String>();

	private void append(final String loggingEvent, final String logType) {
		final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
		final Date date = new Date();
		messages.offer(dateFormat.format(date) + " " + logType + " " + loggingEvent);
	}

	@Override
	public void debug(final Object message) {
		append(String.valueOf(message), "DEBUG");
	}

	@Override
	public void info(final Object message) {
		append(String.valueOf(message), " INFO");
	}

	@Override
	public void warn(final Object message) {
		append(String.valueOf(message), " WARN");
	}

	@Override
	public void error(final Object message) {
		append(String.valueOf(message), "ERROR");
	}

	@Override
	public void fatal(final Object message) {
		append(String.valueOf(message), "FATAL");
	}

	@Override
	public String toString() {
		final StringBuilder messageBlock = new StringBuilder();
		messageBlock.append(Log.CRLF);
		for (final String message : messages) {
			messageBlock.append(message).append(Log.CRLF);
		}
		return messageBlock.toString();
	}
}
