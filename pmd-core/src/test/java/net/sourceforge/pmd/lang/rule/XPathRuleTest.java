/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleContextTest;
import net.sourceforge.pmd.lang.DummyLanguageModule;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.ast.DummyNode;
import net.sourceforge.pmd.lang.ast.DummyNode.DummyRootNode;
import net.sourceforge.pmd.lang.ast.DummyNodeWithDeprecatedAttribute;
import net.sourceforge.pmd.lang.rule.xpath.XPathVersion;

import com.github.stefanbirkner.systemlambda.SystemLambda;

class XPathRuleTest {

    @Test
    void testAttributeDeprecation10() throws Exception {
        testDeprecation(XPathVersion.XPATH_1_0);
    }

    @Test
    void testAttributeDeprecation20() throws Exception {
        testDeprecation(XPathVersion.XPATH_2_0);
    }


    void testDeprecation(XPathVersion version) throws Exception {
        XPathRule xpr = makeRule(version, "SomeRule");

        DummyNode firstNode = newNode();

        String log = SystemLambda.tapSystemErrAndOut(() -> {
            // with another rule forked from the same one (in multithreaded processor)
            Report report = RuleContextTest.getReportForRuleApply(xpr, firstNode);
            assertEquals(1, report.getViolations().size());
        });
        assertThat(log, Matchers.containsString("Use of deprecated attribute 'dummyNode/@Size' by XPath rule 'SomeRule'"));
        assertThat(log, Matchers.containsString("Use of deprecated attribute 'dummyNode/@Name' by XPath rule 'SomeRule', please use @Image instead"));


        log = SystemLambda.tapSystemErrAndOut(() -> {
            // with another node
            Report report = RuleContextTest.getReportForRuleApply(xpr, newNode());
            assertEquals(1, report.getViolations().size());
        });
        assertEquals("", log); // no additional warnings


        log = SystemLambda.tapSystemErrAndOut(() -> {
            // with another rule forked from the same one (in multithreaded processor)
            Report report = RuleContextTest.getReportForRuleApply(xpr.deepCopy(), newNode());
            assertEquals(1, report.getViolations().size());
        });
        assertEquals("", log); // no additional warnings

        // with another rule on the same node, new warnings
        XPathRule otherRule = makeRule(version, "OtherRule");
        otherRule.setRuleSetName("rset.xml");

        log = SystemLambda.tapSystemErrAndOut(() -> {
            Report report = RuleContextTest.getReportForRuleApply(otherRule, firstNode);
            assertEquals(1, report.getViolations().size());
        });
        assertThat(log, Matchers.containsString("Use of deprecated attribute 'dummyNode/@Size' by XPath rule 'OtherRule' (in ruleset 'rset.xml')"));
        assertThat(log, Matchers.containsString("Use of deprecated attribute 'dummyNode/@Name' by XPath rule 'OtherRule' (in ruleset 'rset.xml'), please use @Image instead"));
    }

    XPathRule makeRule(XPathVersion version, String name) {
        XPathRule xpr = new XPathRule(version, "//dummyNode[@Size >= 2 and @Name='foo']");
        xpr.setName(name);
        xpr.setLanguage(LanguageRegistry.getLanguage("Dummy"));
        xpr.setMessage("gotcha");
        return xpr;
    }


    XPathRule makeXPath(String xpathExpr) {
        XPathRule xpr = new XPathRule(XPathVersion.XPATH_2_0, xpathExpr);
        xpr.setLanguage(LanguageRegistry.getLanguage(DummyLanguageModule.NAME));
        xpr.setName("name");
        xpr.setMessage("gotcha");
        return xpr;
    }

    @Test
    void testFileNameInXpath() {
        Report report = executeRule(makeXPath("//*[pmd:fileName() = 'Foo.cls']"),
                                    newRoot("src/Foo.cls"));

        assertThat(report.getViolations(), hasSize(1));
    }

    @Test
    void testBeginLine() {
        Report report = executeRule(makeXPath("//*[pmd:startLine(.)=1]"),
                                    newRoot("src/Foo.cls"));

        assertThat(report.getViolations(), hasSize(1));
    }

    @Test
    void testBeginCol() {
        Report report = executeRule(makeXPath("//*[pmd:startColumn(.)=1]"),
                                    newRoot("src/Foo.cls"));

        assertThat(report.getViolations(), hasSize(1));
    }

    @Test
    void testEndLine() {
        Report report = executeRule(makeXPath("//*[pmd:endLine(.)=1]"),
                                    newRoot("src/Foo.cls"));

        assertThat(report.getViolations(), hasSize(1));
    }

    @Test
    void testEndColumn() {
        Report report = executeRule(makeXPath("//*[pmd:endColumn(.)>1]"),
                                    newRoot("src/Foo.cls"));

        assertThat(report.getViolations(), hasSize(1));
    }

    Report executeRule(net.sourceforge.pmd.Rule rule, DummyNode node) {
        return RuleContextTest.getReportForRuleApply(rule, node);
    }


    DummyRootNode newNode() {
        DummyRootNode root = new DummyRootNode();
        DummyNode dummy = new DummyNodeWithDeprecatedAttribute(2);
        dummy.setCoords(1, 1, 1, 2);
        root.addChild(dummy, 0);
        return root;
    }

    DummyRootNode newRoot(String fileName) {
        DummyRootNode dummy = new DummyRootNode().withFileName(fileName);
        dummy.setCoords(1, 1, 1, 2);
        return dummy;
    }


}
