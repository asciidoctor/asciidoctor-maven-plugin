/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the &quot;License&quot;);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.asciidoc.maven.test

import org.asciidoc.maven.AsciidoctorMojo
import spock.lang.Specification

/**
 *
 */
class AsciidoctorMojoTest extends Specification {
    def "renders docbook"() {
        when:
            File srcDir = new File('target/test-classes/src/asciidoc')
            File outputDir = new File('target/asciidoc-output')

            if (!outputDir.exists())
                outputDir.mkdir()

            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mojo.backend = 'docbook'
            mojo.sourceDirectory = srcDir
            mojo.outputDirectory = outputDir
            mojo.execute()
        then:
            outputDir.list().toList().isEmpty() == false
            outputDir.list().toList().contains('sample.xml')

            File sampleOutput = new File('sample.xml', outputDir)
            sampleOutput.length() > 0
    }
}
