/*
 * Copyright (C) 2000 - 2014 Silverpeas
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

package com.silverpeas.gallery.constant;

import com.silverpeas.gallery.GalleryComponentSettings;
import com.silverpeas.gallery.GalleryWarBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.test.BasicWarBuilder;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class MediaDefinitionTest {

  @Deployment
  public static Archive<?> createTestArchive() {
    return GalleryWarBuilder.onWarForTestClass(MediaDefinitionTest.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addClasses(GalleryComponentSettings.class);
          warBuilder.addPackages(true, "com.silverpeas.gallery.constant");
        }).build();
  }

  @Test
  public void fromNameOrLabel() {
    assertThat(MediaResolution.fromNameOrLabel("kljlkj"), nullValue());
    assertThat(MediaResolution.fromNameOrLabel("tInY"), is(MediaResolution.TINY));
    assertThat(MediaResolution.fromNameOrLabel("_266x150.jpg"), is(MediaResolution.MEDIUM));
  }
}