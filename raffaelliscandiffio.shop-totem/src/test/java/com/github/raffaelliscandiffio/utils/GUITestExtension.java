package com.github.raffaelliscandiffio.utils;

import org.assertj.swing.junit.runner.FailureScreenshotTaker;
import org.assertj.swing.junit.runner.ImageFolderCreator;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;

import static org.assertj.swing.annotation.GUITestFinder.isGUITest;
import static org.assertj.swing.junit.runner.Formatter.testNameFrom;

/**
 * Understands a JUnit 5 extension that takes a screenshot of a failed GUI test.
 * The Junit 4 runner is available in {@link org.assertj.swing.junit.runner.GUITestRunner}.
 *
 * @see <a href="https://github.com/assertj/assertj-swing/issues/259">assertj-swing #259</a>
 * @author William Bakker
 */
public class GUITestExtension implements Extension, InvocationInterceptor {
    private final FailureScreenshotTaker screenshotTaker;

    public GUITestExtension() {
        screenshotTaker = new FailureScreenshotTaker(new ImageFolderCreator().createImageFolder());
    }

    @Override
    public void interceptTestMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) 
            throws Throwable {
        try {
            invocation.proceed();
        } catch (Throwable t) {
            takeScreenshot(invocationContext.getExecutable());
            throw t;
        }
    }

    private void takeScreenshot(Method method) {
        final Class<?> testClass = method.getDeclaringClass();
        if (!(isGUITest(testClass, method)))
            return;
        screenshotTaker.saveScreenshot(testNameFrom(testClass, method));
    }
}