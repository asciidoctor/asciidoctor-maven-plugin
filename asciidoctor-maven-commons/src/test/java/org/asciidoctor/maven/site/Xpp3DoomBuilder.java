package org.asciidoctor.maven.site;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public class Xpp3DoomBuilder {

    private Xpp3Dom rootNode;
    private Xpp3Dom currentNode;
    private Xpp3Dom parentNode;

    private Xpp3DoomBuilder(String name) {
        rootNode = currentNode = new Xpp3Dom(name);
    }

    public static Xpp3DoomBuilder siteNode() {
        return new Xpp3DoomBuilder("site");
    }

    public static Xpp3DoomBuilder asciidocNode() {
        return siteNode().addChild("asciidoc");
    }

    public static Xpp3DoomBuilder logHandler() {
        return new Xpp3DoomBuilder("asciidoc").addChild("logHandler");
    }

    public Xpp3DoomBuilder addChild(String name) {
        final Xpp3Dom newNode = new Xpp3Dom(name);
        currentNode.addChild(newNode);
        parentNode = currentNode;
        currentNode = newNode;
        return this;
    }

    public Xpp3DoomBuilder addChild(String name, String... values) {
        Xpp3Dom newNode = null;
        if (values == null) {
            newNode = new Xpp3Dom(name);
            currentNode.addChild(newNode);
        } else {
            for (String value : values) {
                newNode = new Xpp3Dom(name);
                newNode.setValue(value);
                currentNode.addChild(newNode);
            }
        }
        parentNode = currentNode;
        currentNode = newNode;
        return this;
    }

    public Xpp3DoomBuilder parent() {
        currentNode = parentNode;
        return this;
    }

    public Xpp3Dom build() {
        return rootNode;
    }

}
