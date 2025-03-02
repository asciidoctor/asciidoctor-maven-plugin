package org.asciidoctor.maven.log;


import static org.asciidoctor.maven.commons.StringUtils.isNotBlank;

/**
 * POJO for Maven XML mapping.
 *
 * @author abelsromero
 * @since 1.5.7
 */
public class LogHandler {

    private Boolean outputToConsole;
    private FailIf failIf;
    private Boolean failFast;

    public LogHandler() {
        outputToConsole = Boolean.TRUE;
        failFast = Boolean.TRUE;
    }

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
        return failIf != null && isNotBlank(failIf.getContainsText());
    }

    public Boolean getFailFast() {
        return failFast;
    }

    public void setFailFast(Boolean failFast) {
        this.failFast = failFast;
    }
}
