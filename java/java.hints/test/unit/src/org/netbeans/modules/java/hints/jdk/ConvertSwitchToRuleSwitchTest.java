/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.hints.jdk;

import com.sun.tools.javac.tree.JCTree;
import javax.lang.model.SourceVersion;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class ConvertSwitchToRuleSwitchTest extends NbTestCase {
    
    public ConvertSwitchToRuleSwitchTest(String name) {
        super(name);
    }
    
    public void testSwitch2RuleSwitch() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 1: result = \"1\"; break;\n" +
                       "             case 2: if (true) result = \"2\"; break;\n" +
                       "             case 3: System.err.println(3); result = \"3\"; break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConverSwitchToRuleSwitch())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "     private void test(int p) {\n" +
                              "         String result;\n" +
                              "         switch (p) {\n" +
                              "             case 1 -> result = \"1\";\n" +
                              "             case 2 -> { if (true) result = \"2\"; }\n" +
                              "             case 3 -> { System.err.println(3); result = \"3\"; }\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }
    
    public void testLastNotBreak() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 1: if (p == 1) { result = \"1\"; break; } else { result = \"2\"; break; }\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConverSwitchToRuleSwitch())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "     private void test(int p) {\n" +
                              "         String result;\n" +
                              "         switch (p) {\n" +
                              "             case 1 -> { if (p == 1) { result = \"1\"; } else { result = \"2\"; } }\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }
    
    public void testMultipleCases() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 0:\n" +
                       "             case 1: result = \"1\"; break;\n" +
                       "             case 2: result = \"2\"; break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConverSwitchToRuleSwitch())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "     private void test(int p) {\n" +
                              "         String result;\n" +
                              "         switch (p) {\n" +
                              "             case 0, 1 -> result = \"1\";\n" +
                              "             case 2 -> result = \"2\";\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }
    
    public void testFallThrough() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 0: System.err.println(0);\n" +
                       "             case 1: result = \"1\"; break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToRuleSwitch.class)
                .assertWarnings();
    }
    
    public void testMissingLastBreak() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 0:\n" +
                       "             case 1: result = \"1\"; break;\n" +
                       "             case 2: result = \"2\";\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConverSwitchToRuleSwitch())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "     private void test(int p) {\n" +
                              "         String result;\n" +
                              "         switch (p) {\n" +
                              "             case 0, 1 -> result = \"1\";\n" +
                              "             case 2 -> result = \"2\";\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testDefault() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 0: result = \"1\"; break;\n" +
                       "             default: result = \"d\"; break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConverSwitchToRuleSwitch())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "     private void test(int p) {\n" +
                              "         String result;\n" +
                              "         switch (p) {\n" +
                              "             case 0 -> result = \"1\";\n" +
                              "             default -> result = \"d\";\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testVariables1() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 0:\n" +
                       "                 int i = 0;\n" +
                       "                 int j = 0;\n" +
                       "                 break;\n" +
                       "             default:\n" +
                       "                 i = 0;\n" +
                       "                 System.err.println(i);\n" +
                       "                 System.err.println(j = 15);\n" +
                       "                 break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConverSwitchToRuleSwitch())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "     private void test(int p) {\n" +
                              "         String result;\n" +
                              "         switch (p) {\n" +
                              "             case 0 -> {\n" +
                              "                 int i = 0;\n" +
                              "                 int j = 0;\n" +
                              "             }\n" +
                              "             default -> {\n" +
                              "                 int i = 0;\n" +
                              "                 System.err.println(i);\n" +
                              "                 int j;\n" +
                              "                 System.err.println(j = 15);\n" +
                              "             }\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testFallThroughDefault1() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             default:\n" +
                       "             case 0:\n" +
                       "                 break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToRuleSwitch.class)
                .assertWarnings();
    }

    public void testFallThroughDefault2() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 0:\n" +
                       "             default:\n" +
                       "                 break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToRuleSwitch.class)
                .assertWarnings();
    }

    public void testTrailingEmptyCase() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 0:\n" +
                       "                 int i = 0;\n" +
                       "                 int j = 0;\n" +
                       "                 break;\n" +
                       "             default:\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConverSwitchToRuleSwitch())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "     private void test(int p) {\n" +
                              "         String result;\n" +
                              "         switch (p) {\n" +
                              "             case 0 -> {\n" +
                              "                 int i = 0;\n" +
                              "                 int j = 0;\n" +
                              "             }\n" +
                              "             default -> { }\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testNeedsPreview() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 0:\n" +
                       "                 int i = 0;\n" +
                       "                 int j = 0;\n" +
                       "                 break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .run(ConvertSwitchToRuleSwitch.class)
                .assertWarnings();
    }

    public void testBreakInside1() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 0: while (true) break;\n" +
                       "             case 1: INNER: while (true) break INNER;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToRuleSwitch.class)
                .assertWarnings();
    }

    public void testBreakInside2() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 0: while (true) break;\n" +
                       "                     break;\n" +
                       "             case 1: INNER: while (true) break INNER;\n" +
                       "                     break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("4:9-4:15:verifier:Convert switch to rule switch")
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private void test(int p) {\n" +
                              "         String result;\n" +
                              "         switch (p) {\n" +
                              "             case 0 -> {\n" +
                              "                 while (true) break;\n" +
                              "             }\n" +
                              "             case 1 -> {\n" +
                              "                 INNER: while (true) break INNER;\n" +
                              "             }\n" +
                              "         }\n" +
                              "     }\n" +
                              "}");
    }

    public void testContinueInside1() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 0: while (p++ < 12) continue;\n" +
                       "             case 1: INNER: while (p++ < 12) continue INNER;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToRuleSwitch.class)
                .assertWarnings();
    }

    public void testContinueInside2() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         OUTER: while (p-- > 0) {\n" +
                       "             switch (p) {\n" +
                       "                 case 0: while (p++ < 12) continue OUTER;\n" +
                       "                         break;\n" +
                       "                 case 1: INNER: while (p++ < 12) continue OUTER;\n" +
                       "                         break;\n" +
                       "             }\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("5:13-5:19:verifier:Convert switch to rule switch")
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private void test(int p) {\n" +
                              "         String result;\n" +
                              "         OUTER: while (p-- > 0) {\n" +
                              "             switch (p) {\n" +
                              "                 case 0 -> {\n" +
                              "                     while (p++ < 12) continue OUTER;\n" +
                              "                 }\n" +
                              "                 case 1 -> {\n" +
                              "                     INNER: while (p++ < 12) continue OUTER;\n" +
                              "                 }\n" +
                              "             }\n" +
                              "         }\n" +
                              "     }\n" +
                              "}");
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        try {
            Class.forName("com.sun.source.tree.CaseTree$CaseKind", false, JCTree.class.getClassLoader());
            suite.addTestSuite(ConvertSwitchToRuleSwitchTest.class);
        } catch (ClassNotFoundException ex) {
            //OK
            suite.addTest(new ConvertSwitchToRuleSwitchTest("noop"));
        }
        return suite;
    }

    public void noop() {}
}
