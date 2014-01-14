package com.silverpeas.gallery.web;

/*
 * Copyright (C) 2000 - 2013 Silverpeas
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

import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.web.ResourceGettingTest;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.junit.Before;
import org.junit.Test;

import static com.silverpeas.gallery.web.GalleryTestResources.JAVA_PACKAGE;
import static com.silverpeas.gallery.web.GalleryTestResources.SPRING_CONTEXT;
import static com.silverpeas.gallery.web.PhotoEntityMatcher.matches;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Tests on the gallery photo getting by the GalleryResource web service.
 * @author Yohann Chastagnier
 */
public class GalleryPhotoGettingTest extends ResourceGettingTest<GalleryTestResources> {

  private UserDetail user;
  private String sessionKey;
  private PhotoDetail expected;

  private static String ALBUM_ID = "3";
  private static String PHOTO_ID = "7";
  private static String PHOTO_ID_DOESNT_EXISTS = "8";

  public GalleryPhotoGettingTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void prepareTestResources() {
    user = aUser();
    sessionKey = authenticate(user);
    expected =
        PhotoBuilder.getPhotoBuilder().buildPhoto(PHOTO_ID, getExistingComponentInstances()[0]);
  }

  @Test
  public void getPhoto() {
    final PhotoEntity entity = getAt(aResourceURI(), PhotoEntity.class);
    assertNotNull(entity);
    assertThat(entity, matches(expected));
  }

  @Override
  public String aResourceURI() {
    return aResourceURI(PHOTO_ID);
  }

  private String aResourceURI(final String photoId) {
    return "gallery/" + getExistingComponentInstances()[0] + "/albums/" + ALBUM_ID + "/photos/" +
        photoId;
  }

  @Override
  public String anUnexistingResourceURI() {
    return aResourceURI(PHOTO_ID_DOESNT_EXISTS);
  }

  @Override
  public PhotoDetail aResource() {
    return expected;
  }

  @Override
  public String getSessionKey() {
    return sessionKey;
  }

  @Override
  public Class<?> getWebEntityClass() {
    return PhotoEntity.class;
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{"componentName5"};
  }
}
