package org.asciidoctor.maven.refresh;

import java.util.concurrent.TimeUnit;

public class TimeCounter {

    public static long timed(Runnable runnable) {
        final long start = System.nanoTime();
        runnable.run();
        final long end = System.nanoTime();
        return TimeUnit.NANOSECONDS.toMillis(end - start);
    }
}
