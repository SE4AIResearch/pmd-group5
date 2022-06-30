/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.ant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.tools.ant.BuildException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.sourceforge.pmd.util.IOUtil;

class PMDTaskTest extends AbstractAntTest {

    @BeforeEach
    void setUp() {
        configureProject("src/test/resources/net/sourceforge/pmd/ant/xml/pmdtasktest.xml");
    }

    @Test
    void testFormatterWithNoToFileAttribute() {
        try {
            executeTarget("testFormatterWithNoToFileAttribute");
            fail("This should throw an exception");
        } catch (BuildException ex) {
            assertEquals("toFile or toConsole needs to be specified in Formatter", ex.getMessage());
        }
    }

    @Test
    void testNoRuleSets() {
        try {
            executeTarget("testNoRuleSets");
            fail("This should throw an exception");
        } catch (BuildException ex) {
            assertEquals("No rulesets specified", ex.getMessage());
        }
    }

    @Test
    void testBasic() {
        executeTarget("testBasic");
    }

    @Test
    void testInvalidLanguageVersion() {
        try {
            executeTarget("testInvalidLanguageVersion");
            assertEquals(
                    "The following language is not supported:<sourceLanguage name=\"java\" version=\"42\" />.",
                    log.toString());
            fail("This should throw an exception");
        } catch (BuildException ex) {
            assertEquals(
                    "The following language is not supported:<sourceLanguage name=\"java\" version=\"42\" />.",
                    ex.getMessage());
        }
    }

    @Test
    void testWithShortFilenames() throws IOException {
        executeTarget("testWithShortFilenames");

        try (InputStream in = new FileInputStream("target/pmd-ant-test.txt")) {
            String actual = IOUtil.readToString(in, StandardCharsets.UTF_8);
            // remove any trailing newline
            actual = actual.trim();
            assertEquals("sample.dummy:1:\tSampleXPathRule:\tTest Rule 2", actual);
        }
    }

    @Test
    void testXmlFormatter() throws IOException {
        executeTarget("testXmlFormatter");

        try (InputStream in = new FileInputStream("target/pmd-ant-xml.xml");
             InputStream expectedStream = PMDTaskTest.class.getResourceAsStream("xml/expected-pmd-ant-xml.xml")) {
            String actual = IOUtil.readToString(in, StandardCharsets.UTF_8);
            actual = actual.replaceFirst("timestamp=\"[^\"]+\"", "timestamp=\"\"");
            actual = actual.replaceFirst("\\.xsd\" version=\"[^\"]+\"", ".xsd\" version=\"\"");

            String expected = IOUtil.readToString(expectedStream, StandardCharsets.UTF_8);
            expected = expected.replaceFirst("timestamp=\"[^\"]+\"", "timestamp=\"\"");
            expected = expected.replaceFirst("\\.xsd\" version=\"[^\"]+\"", ".xsd\" version=\"\"");

            // under windows, the file source sample.dummy has different line endings
            // and therefore the endcolumn of the nodes also change
            if (System.lineSeparator().equals("\r\n")) {
                expected = expected.replaceFirst("endcolumn=\"109\"", "endcolumn=\"110\"");
            }

            assertEquals(expected, actual);
        }
    }
}
