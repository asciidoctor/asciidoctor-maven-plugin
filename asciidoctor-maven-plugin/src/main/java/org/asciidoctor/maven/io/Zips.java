package org.asciidoctor.maven.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

public final class Zips {

    public static void zip(final File dir, final File zipName) throws IOException, IllegalArgumentException {
        final String[] entries = dir.list();

        try (final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipName))) {
            String prefix = dir.getAbsolutePath();
            if (!prefix.endsWith(File.separator)) {
                prefix += File.separator;
            }

            for (final String entry : entries) {
                File f = new File(dir, entry);
                zip(out, f, prefix, zipName.getName().substring(0, zipName.getName().length() - 4));
            }
        }
    }

    private static void zip(final ZipOutputStream out, final File f, final String prefix, final String root) throws IOException {
        if (f.isDirectory()) {
            final File[] files = f.listFiles();
            if (files != null) {
                for (final File child : files) {
                    zip(out, child, prefix, root);
                }
            }
        } else {
            try (final FileInputStream in = new FileInputStream(f)) {
                final ZipEntry entry = new ZipEntry(root + "/" + f.getPath().replace(prefix, ""));
                out.putNextEntry(entry);
                IOUtils.copy(in, out);
            }
        }
    }

    private Zips() {
        // no-op
    }
}
