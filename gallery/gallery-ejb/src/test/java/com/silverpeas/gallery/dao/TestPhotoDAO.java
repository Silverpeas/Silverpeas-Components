/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.dao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.dbunit.database.IDatabaseConnection;
import org.junit.Test;

import com.silverpeas.components.model.AbstractTestDao;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;

public class TestPhotoDAO extends AbstractTestDao {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Test
  public void testGetAllPhotosIdbyUserid() throws Exception {
    IDatabaseConnection connexion = null;
    this.setUp();
    String fleurId = "1";
    String animalId = "2";
    String merId = "3";
    String montagneId = "4";

    String userid = "1";
    try {
      connexion = getConnection();
      List<String> photos = PhotoDAO.getAllPhotosIDbyUserid(connexion.getConnection(), userid);
      assertNotNull("Photos should exist", photos);
      assertEquals("Should have 5 date creation or update ", 5, photos.size());
      assertEquals(photos.get(0), montagneId);
      assertEquals(photos.get(1), merId);
      assertEquals(photos.get(2), merId);
      assertEquals(photos.get(3), fleurId);
      assertEquals(photos.get(4), animalId);
    } finally {
      closeConnection(connexion);
    }
  }

  @Test
  public void testGetPhotoDetail() throws Exception {
    IDatabaseConnection connexion = null;
    this.setUp();
    PhotoDetail photo = new PhotoDetail();
    connexion = getConnection();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    Date fleurcreated = sdf.parse("2010/06/15");
    Date fleurupdate = sdf.parse("2010/06/16");
    PhotoDetail fleur =
        new PhotoDetail("fleur", "tulipe", fleurcreated, fleurupdate, null, null, false, false);
    int fleurId = 1;
    PhotoPK photoPK = new PhotoPK(String.valueOf(fleurId), "gallery26");
    fleur.setPhotoPK(photoPK);
    fleur.setCreatorId("1");
    fleur.setUpdateId("0");
    fleur.setSizeH(110);
    fleur.setSizeL(110);
    fleur.setAlbumId("0");
    fleur.setImageName("fleur.jpg");
    fleur.setImageSize(5146);
    fleur.setImageMimeType("image/png");

    try {
      photo = PhotoDAO.getPhoto(connexion.getConnection(), fleurId);
      assertNotNull("Photo should exist", photo);
      assertEquals(photo.getTitle(), fleur.getTitle());
      assertEquals(photo.getId(), photoPK.getId());
      assertEquals(photo.getDescription(), fleur.getDescription());
      assertEquals(photo.getCreationDate(), fleurcreated);
      assertEquals(photo.getUpdateDate(), fleurupdate);
      assertEquals(fleur, photo);

    } finally {
      closeConnection(connexion);
    }
  }

  @Override
  protected String getDatasetFileName() {
    return "photo_dataset.xml";
  }
}
