package org.asciidoctor.maven.site;

import org.asciidoctor.log.Severity;
import org.asciidoctor.maven.log.FailIf;
import org.asciidoctor.maven.log.LogHandler;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.util.Optional;

public class SiteLogHandlerDeserializer {

    public LogHandler deserialize(Xpp3Dom node) {
        final LogHandler logHandler = defaultLogHandler();
        if (node == null || !node.getName().equals("logHandler"))
            return logHandler;

        logHandler.setOutputToConsole(deserializeOutputToConsole(node));

        deserializeFailIf(node.getChild("failIf"))
                .ifPresent(logHandler::setFailIf);

        return logHandler;
    }

    private Boolean deserializeOutputToConsole(Xpp3Dom node) {
        return getBoolean(node, "outputToConsole");
    }

    private Optional<FailIf> deserializeFailIf(Xpp3Dom node) {
        if (node == null)
            return Optional.empty();

        Xpp3Dom severity = node.getChild("severity");
        FailIf failIf = null;
        if (severity != null) {
            String sanitizedSeverity = sanitizeString(severity);
            if (sanitizedSeverity.length() > 0) {
                Severity severityEnumValue = Severity.valueOf(sanitizedSeverity);
                failIf = new FailIf();
                failIf.setSeverity(severityEnumValue);
            }
        }

        Xpp3Dom containsText = node.getChild("containsText");
        if (containsText != null) {
            String sanitizedContainsText = sanitizeString(containsText);
            if (sanitizedContainsText.length() > 0) {
                failIf = failIf == null ? new FailIf() : failIf;
                failIf.setContainsText(sanitizedContainsText);
            }
        }
        return Optional.ofNullable(failIf);
    }

    private String sanitizeString(Xpp3Dom severity) {
        String value = severity.getValue();
        return value == null ? "" : value.trim();
    }

    private LogHandler defaultLogHandler() {
        LogHandler logHandler = new LogHandler();
        logHandler.setOutputToConsole(Boolean.TRUE);
        return logHandler;
    }

    private Boolean getBoolean(Xpp3Dom node, String name) {
        final Xpp3Dom child = node.getChild(name);
        if (child == null) {
            return Boolean.TRUE;
        } else {
            if (child.getValue() == null) {
                return Boolean.TRUE;
            }
            return Boolean.valueOf(child.getValue());
        }
    }

}
