package com.topper.tests.utility;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Source: https://binkley.blogspot.com/2012/11/junit-testing-that-call-blocks.html
 * */
public interface BlockingCall {

	/**
     * Calls blocking code.  The blocking code must throw {@code
     * InterruptedException} when its current thread is interrupted.
     *
     * @throws InterruptedException if interrupted.
     */
    void call() throws InterruptedException;

    /** Wrapper class for block assertion. */
    public static final class Assert {
        /**
         * Asserts the given <var>code</var> blocks at least 100ms.  Interrupts
         * the blocking code after <var>timeoutMillis</var>ms and checks {@code InterruptedException}
         * was thrown.  When not blocking, appends <var>block</var> to the
         * failure message.
         *
         * @param block the blocking call, never missing
         */
        public static void assertBlocks(final long timeoutMillis, final BlockingCall block) {
            final Timer timer = new Timer(true);
            try {
                final Thread current = Thread.currentThread();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        current.interrupt();
                    }
                }, timeoutMillis);
                block.call();
                //fail(String.format("Did not block: %s", block));
            } catch (final InterruptedException ignored) {
            } finally {
                timer.cancel();
            }
        }
    }
}
