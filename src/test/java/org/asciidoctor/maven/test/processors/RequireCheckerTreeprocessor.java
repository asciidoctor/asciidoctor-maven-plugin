package org.asciidoctor.maven.test.processors;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Treeprocessor;
import org.asciidoctor.jruby.internal.JRubyRuntimeContext;

import static org.junit.Assert.assertEquals;

public class RequireCheckerTreeprocessor extends Treeprocessor {

  @Override
  public Document process(Document document) {
    assertEquals("constant", JRubyRuntimeContext.get(document).evalScriptlet("defined? ::DateTime").toString());
    // Leave a trace in the rendered document so that the test can check that I was called
    document.getBlocks().add(createBlock(document, "paragraph", RequireCheckerTreeprocessor.class.getSimpleName() + " was here"));
    return document;
  }
}
