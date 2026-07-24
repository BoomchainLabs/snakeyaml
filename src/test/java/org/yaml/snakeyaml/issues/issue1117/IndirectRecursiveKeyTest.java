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
package org.yaml.snakeyaml.issues.issue1117;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.ConstructorException;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * An alias wrapped inside a freshly composed collection key (instead of being the key itself) is
 * not marked by the Composer as `isTwoStepsConstruction()`. This lets a self-referential mapping
 * key bypass the `allowRecursiveKeys=false` guard (the default) and reach
 * `key.hashCode()`/`key.toString()`, which recurse without bound over the self-referential
 * structure and previously caused an uncaught `StackOverflowError` to escape `Yaml.load`.
 */
public class IndirectRecursiveKeyTest {

  // 27-byte payload: anchor &m is used as a *key inside a fresh sequence* (`[*m]`), not as the key
  // itself, so Composer never sets isTwoStepsConstruction() on the sequence key node and the
  // allowRecursiveKeys=false guard (the default) is never evaluated.
  private static final String PAYLOAD = "&m\n" + "? [*m]\n" + ": 1\n" + "? [[*m]]\n" + ": 2\n";

  @Test
  public void defaultYamlRejectsIndirectRecursiveKeyInsteadOfCrashing() {
    Yaml yaml = new Yaml();
    try {
      yaml.load(PAYLOAD);
      fail("A self-referential key must be rejected, not silently constructed");
    } catch (StackOverflowError soe) {
      fail("StackOverflowError must not escape Yaml.load - it should be reported as a "
          + "ConstructorException, like any other unacceptable/recursive key");
    } catch (ConstructorException e) {
      assertTrue(true);
    }
  }

  @Test
  public void safeConstructorRejectsIndirectRecursiveKeyInsteadOfCrashing() {
    Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
    try {
      yaml.load(PAYLOAD);
      fail("A self-referential key must be rejected, not silently constructed");
    } catch (StackOverflowError soe) {
      fail("StackOverflowError must not escape Yaml.load - it should be reported as a "
          + "ConstructorException, like any other unacceptable/recursive key");
    } catch (ConstructorException e) {
      assertTrue(true);
    }
  }
}
