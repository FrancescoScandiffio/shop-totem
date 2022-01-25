package com.github.raffaelliscandiffio.utils;

public class LogUtility {

	public String getReducedStackTrace(Exception e) {
		StackTraceElement el = e.getStackTrace()[0];
		return String.format("-> at %s.%s() - line %s", el.getClassName(), el.getMethodName(), el.getLineNumber());
	}

}
