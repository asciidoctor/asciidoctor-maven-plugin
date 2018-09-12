package org.asciidoctor.maven.log;

import org.apache.commons.lang.StringUtils;

public class LogHandler {

    private Boolean outputToConsole;
    private FailIf failIf;

    public Boolean getOutputToConsole() {
        return outputToConsole;
    }

    public void setOutputToConsole(Boolean outputToConsole) {
        this.outputToConsole = outputToConsole;
    }

    public FailIf getFailIf() {
        return failIf;
    }

    public void setFailIf(FailIf failIf) {
        this.failIf = failIf;
    }

    public boolean isSeveritySet() {
        return failIf != null && failIf.getSeverity() != null;
    }

    public boolean isContainsTextNotBlank() {
        return failIf != null && StringUtils.isNotBlank(failIf.getContainsText());
    }

}
