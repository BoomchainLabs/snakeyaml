/**
 * Copyright (c) 2008, SnakeYAML
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.yaml.snakeyaml.issues.issue1116;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import org.junit.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Representer;

/**
 * https://codeberg.org/snakeyaml/snakeyaml/issues/1116
 */
public class SequenceCommentDashTest {

  private Yaml createYaml() {
    LoaderOptions loaderOptions = new LoaderOptions();
    loaderOptions.setProcessComments(true);
    DumperOptions dumperOptions = new DumperOptions();
    dumperOptions.setProcessComments(true);
    return new Yaml(new Constructor(loaderOptions), new Representer(dumperOptions), dumperOptions,
        loaderOptions);
  }

  private String composeAndSerialize(Yaml yaml, String input) throws Exception {
    Node node = yaml.compose(new StringReader(input));
    StringWriter output = new StringWriter();
    yaml.serialize(node, output);
    return output.toString();
  }

  @Test
  public void standaloneCommentBetweenEntriesKeepsItsOwnLineAndEachEntryKeepsItsDash()
      throws Exception {
    Yaml yaml = createYaml();
    String input = "entries:\n" + "# comment before entry1\n" + "  - entry1\n" + "  - entry2\n"
        + "  - entry3\n" + "# comment before entry4 line1\n" + "# comment before entry4 line2\n"
        + "  - entry4\n";

    String expected =
        "entries:\n" + "# comment before entry1\n" + "- entry1\n" + "- entry2\n" + "- entry3\n"
            + "# comment before entry4 line1\n" + "# comment before entry4 line2\n" + "- entry4\n";

    String actual = composeAndSerialize(yaml, input);
    assertEquals(expected, actual);

    Yaml plain = new Yaml();
    @SuppressWarnings("unchecked")
    Map<String, Object> reparsed = plain.load(actual);
    assertEquals(java.util.Arrays.asList("entry1", "entry2", "entry3", "entry4"),
        reparsed.get("entries"));
  }

  @Test
  public void commentGluedAfterDashStaysGluedToThatEntry() throws Exception {
    Yaml yaml = createYaml();
    String input = "list:\n" + "  - item1\n" + "  - # glued comment\n" + "    item2\n";

    String expected = "list:\n" + "- item1\n" + "- # glued comment\n" + "  item2\n";

    String actual = composeAndSerialize(yaml, input);
    assertEquals(expected, actual);
  }
}
