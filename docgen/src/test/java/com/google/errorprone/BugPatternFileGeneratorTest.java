/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.errorprone;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.io.CharStreams;
import com.google.common.io.Files;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@RunWith(JUnit4.class)
public class BugPatternFileGeneratorTest {

  @Rule
  public TemporaryFolder tmpfolder = new TemporaryFolder();

  private File exampleDir;
  private File wikiDir;
  private File exampleDirBase;

  @Before
  public void setUp() throws Exception {
    wikiDir = tmpfolder.newFolder("wiki");
    exampleDirBase = tmpfolder.newFolder("examples");
    exampleDir = new File(exampleDirBase, "com/google/errorprone/bugpatterns");
    assertTrue(exampleDir.mkdirs());
    Files.write("here is an example",
        new File(exampleDir, "DeadExceptionPositiveCase.java"), UTF_8);
  }

  private static final String BUGPATTERN_LINE =
      "com.google.errorprone.bugpatterns.DeadException\t"
      + "DeadException\tThrowableInstanceNeverThrown\tJDK\tERROR\tMATURE\tSUPPRESS_WARNINGS\t"
      + "com.google.errorprone.BugPattern.NoCustomSuppression\t"
      + "Exception created but not thrown\t"
      + "The exception is created with new, but is not thrown, and the reference is lost.\n";

  // Assert that the generator produces the same output it did before.
  // This is brittle, but you can open the golden file
  // src/test/resources/com/google/errorprone/DeadException.md
  // in the same Jekyll environment you use for prod, and verify it looks good.
  @Test
  public void regressionTest_frontmatter_pygments() throws Exception {
    BugPatternFileGenerator generator =
        new BugPatternFileGenerator(wikiDir, exampleDirBase, true, true);
    generator.processLine(BUGPATTERN_LINE);
    String expected = CharStreams.toString(new InputStreamReader(
        getClass().getResourceAsStream("DeadException_frontmatter_pygments.md"), UTF_8));
    String actual = CharStreams.toString(
        Files.newReader(new File(wikiDir, "DeadException.md"), StandardCharsets.UTF_8));
    System.err.println(actual);
    assertThat(actual, is(expected));
  }

  @Test
  public void regressionTest_nofrontmatter_gfm() throws Exception {
    BugPatternFileGenerator generator =
        new BugPatternFileGenerator(wikiDir, exampleDirBase, false, false);
    generator.processLine(BUGPATTERN_LINE);
    String expected = CharStreams.toString(new InputStreamReader(
        getClass().getResourceAsStream("DeadException_nofrontmatter_gfm.md"), UTF_8));
    String actual = CharStreams.toString(new FileReader(new File(wikiDir, "DeadException.md")));
    System.err.println(actual);
    assertThat(actual, is(expected));
  }
}
