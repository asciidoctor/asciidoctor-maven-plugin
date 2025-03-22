package org.asciidoctor.maven.site;

import java.util.Optional;

import org.asciidoctor.log.Severity;
import org.asciidoctor.maven.log.FailIf;
import org.asciidoctor.maven.log.LogHandler;
import org.codehaus.plexus.util.xml.Xpp3Dom;

public class SiteLogHandlerDeserializer {

    public static final String NODE_NAME = "logHandler";

    public LogHandler deserialize(Xpp3Dom configNode) {
        final LogHandler logHandler = defaultLogHandler();
        if (configNode == null)
            return logHandler;

        final Xpp3Dom logHandlerNode = configNode.getChild("logHandler");
        if (logHandlerNode == null || !logHandlerNode.getName().equals(NODE_NAME))
            return logHandler;

        logHandler.setOutputToConsole(getBoolean(logHandlerNode, "outputToConsole"));
        logHandler.setFailFast(getBoolean(logHandlerNode, "failFast"));

        deserializeFailIf(logHandlerNode.getChild("failIf"))
            .ifPresent(logHandler::setFailIf);

        return logHandler;
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

    private LogHandler defaultLogHandler() {
        LogHandler logHandler = new LogHandler();
        logHandler.setOutputToConsole(Boolean.TRUE);
        return logHandler;
    }

    private static String sanitizeString(Xpp3Dom node) {
        final String value = node.getValue();
        return value == null ? "" : value.trim();
    }

    private static Boolean getBoolean(Xpp3Dom node, String name) {
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
