/*
 * Copyright (C) 2000 - 2017 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery;

import org.silverpeas.core.test.BasicCoreWarBuilder;

/**
 * @author ebonnet
 */
public class GalleryWarBuilder extends BasicCoreWarBuilder {

  /**
   * Constructs a war builder for the specified test class. It will load all the resources in the
   * same packages of the specified test class.
   * @param test the class of the test for which a war archive will be build.
   */
  protected <T> GalleryWarBuilder(final Class<T> test) {
    super(test);
    addPackages(true, "org.silverpeas.components.gallery");
    addMavenDependencies("org.apache.httpcomponents:httpclient");
    addMavenDependencies("org.silverpeas.core.services:silverpeas-core-comment");
    addAsResource("org/silverpeas/gallery/multilang/galleryBundle.properties");
    addAsResource("org/silverpeas/gallery/settings/gallerySettings.properties");
  }

  /**
   * Constructs an instance of the basic war archive builder for the specified test class.
   * @param test the test class for which a war will be built. Any resources located in the same
   * package of the test will be loaded into the war.
   * @param <T> the type of the test.
   * @return a basic builder of the war archive.
   */
  public static <T> GalleryWarBuilder onWarForTestClass(Class<T> test) {
    return new GalleryWarBuilder(test);
  }

}
