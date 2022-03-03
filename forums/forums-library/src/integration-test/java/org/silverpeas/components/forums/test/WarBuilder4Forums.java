/*
 * Copyright (C) 2000 - 2022 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.forums.test;

import org.silverpeas.core.test.BasicWarBuilder;

/**
 * A builder of a war archive for the Forums application dedicated to integration tests with
 * Arquillian.
 * @author mmoquillon
 */
public class WarBuilder4Forums extends BasicWarBuilder {

  public static <T> WarBuilder4Forums onWarForTestClass(Class<T> test) {
    return (WarBuilder4Forums) new WarBuilder4Forums(test)
        .addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core")
        .createMavenDependencies("org.silverpeas.core.services:silverpeas-core-tagcloud")
        .testFocusedOn(war -> {
          war.addPackages(true, "org.silverpeas.components.forums")
              .addAsResource("org/silverpeas/components/forums");
        });
  }
  /**
   * Constructs a war builder for the specified test class. It will load all the resources in the
   * same packages of the specified test class.
   * @param test the class of the test for which a war archive will be build.
   */
  protected <T> WarBuilder4Forums(final Class<T> test) {
    super(test);
  }
}
