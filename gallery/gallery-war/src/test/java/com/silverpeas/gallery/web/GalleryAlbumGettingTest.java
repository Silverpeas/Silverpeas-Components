package com.silverpeas.gallery.web;

/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.web.ResourceGettingTest;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.junit.Before;
import org.junit.Test;

import static com.silverpeas.gallery.web.AlbumEntityMatcher.matches;
import static com.silverpeas.gallery.web.GalleryTestResources.JAVA_PACKAGE;
import static com.silverpeas.gallery.web.GalleryTestResources.SPRING_CONTEXT;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Tests on the gallery album getting by the GalleryResource web service.
 * @author Yohann Chastagnier
 */
public class GalleryAlbumGettingTest extends ResourceGettingTest<GalleryTestResources> {

  private UserDetail user;
  private String sessionKey;
  private AlbumDetail expected;

  private static String ALBUM_ID = "3";
  private static String ALBUM_ID_DOESNT_EXISTS = "8";

  public GalleryAlbumGettingTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void prepareTestResources() {
    user = aUser();
    sessionKey = authenticate(user);
    expected = AlbumBuilder.getAlbumBuilder().buildAlbum(ALBUM_ID);
  }

  @Test
  public void getAlbum() {
    final AlbumEntity entity = getAt(aResourceURI(), AlbumEntity.class);
    assertNotNull(entity);
    assertThat(entity, matches(expected));
  }

  @Override
  public String aResourceURI() {
    return aResourceURI(ALBUM_ID);
  }

  private String aResourceURI(final String albumId) {
    return "gallery/" + getExistingComponentInstances()[0] + "/albums/" + albumId;
  }

  @Override
  public String anUnexistingResourceURI() {
    return aResourceURI(ALBUM_ID_DOESNT_EXISTS);
  }

  @Override
  public AlbumDetail aResource() {
    return expected;
  }

  @Override
  public String getSessionKey() {
    return sessionKey;
  }

  @Override
  public Class<?> getWebEntityClass() {
    return AlbumEntity.class;
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{"componentName5"};
  }
}
