package org.asciidoctor.maven.log;

import org.asciidoctor.ast.Cursor;

class TestCursor implements Cursor {

    private final int lineNumber;
    private final String file;
    private final String path;
    private final String dir;

    TestCursor(String file, int lineNumber, String path, String dir) {
        this.file = file;
        this.lineNumber = lineNumber;
        this.path = path;
        this.dir = dir;
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getDir() {
        return dir;
    }

    @Override
    public String getFile() {
        return file;
    }
}
