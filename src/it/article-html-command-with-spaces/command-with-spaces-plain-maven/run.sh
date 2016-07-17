#!/bin/bash
mvn clean asciidoctor:process-asciidoc -Dasciidoctor.sourceDirectory=src/main/doc -Dasciidoctor.outputDirectory=target/docs -Dasciidoctor.attributes="toc=left source-highlighter=coderay"
