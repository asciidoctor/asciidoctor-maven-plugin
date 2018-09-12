package org.asciidoctor.maven.log;

import org.asciidoctor.log.Severity;

public class FailIf {

    private Severity severity;
    private String containsText;

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getContainsText() {
        return containsText;
    }

    public void setContainsText(String containsText) {
        this.containsText = containsText;
    }
}
