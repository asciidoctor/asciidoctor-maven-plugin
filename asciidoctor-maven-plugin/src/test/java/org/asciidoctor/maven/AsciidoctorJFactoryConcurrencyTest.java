package org.asciidoctor.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.asciidoctor.Asciidoctor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Test to validate thread-safety of AsciidoctorJFactory (issue #821).
 * This test attempts to reproduce the concurrent ServiceLoader access that causes
 * NullPointerException: Cannot invoke "java.lang.ClassLoader.getParent()" because "this.currentLoader" is null
 */
class AsciidoctorJFactoryConcurrencyTest {

    @Test
    void should_create_multiple_asciidoctor_instances_concurrently() throws Exception {
        // given
        AsciidoctorJFactory factory = new AsciidoctorJFactory();
        Log mockLog = mock(Log.class);
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        // when: create multiple Asciidoctor instances concurrently
        List<Callable<Asciidoctor>> tasks = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            tasks.add(() -> factory.create(null, mockLog));
        }

        List<Future<Asciidoctor>> futures = executorService.invokeAll(tasks);
        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);

        // then: all instances should be created successfully without NPE
        assertThat(futures).hasSize(threadCount);
        for (Future<Asciidoctor> future : futures) {
            Asciidoctor asciidoctor = future.get();
            assertThat(asciidoctor).isNotNull();
            asciidoctor.shutdown();
        }
    }
}
