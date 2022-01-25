package com.github.raffaelliscandiffio.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LogUtilityTest {
	
	LogUtility logUtility = new LogUtility();

	@Test
	@DisplayName("'getReducedStackTrace' should return custom stack trace string")
	void testGetReducedStackTraceShouldReturnCustomString() {
		
		StackTraceElement[] currentStack = new Throwable().getStackTrace();
		currentStack[0] = new StackTraceElement("class", "function", "", 12);
		Exception e = new Exception();
		e.setStackTrace(currentStack);
		
		assertThat(logUtility.getReducedStackTrace(e)).isEqualTo("-> at class.function() - line 12");
	}
}
