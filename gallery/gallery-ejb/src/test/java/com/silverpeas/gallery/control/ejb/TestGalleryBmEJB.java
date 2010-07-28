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
package com.silverpeas.gallery.control.ejb;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dbunit.database.IDatabaseConnection;
import org.junit.Test;

import com.silverpeas.components.model.AbstractTestDao;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;
import com.silverpeas.gallery.model.PhotoWithStatus;

public class TestGalleryBmEJB extends AbstractTestDao {

  private GalleryBmEJB galleryBmEjb;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    galleryBmEjb = new GalleryBmEJB();

  }

  @Test
  public void testgetAllPhotosbyUserid() throws Exception {
    IDatabaseConnection connexion = null;
    connexion = getConnection();
    this.setUp();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    Date fleurCreated = sdf.parse("2010/06/15");
    Date fleurUpdate = sdf.parse("2010/06/16");
    Date animalCreated = sdf.parse("2010/04/10");
    Date animalUpdate = sdf.parse("2010/04/14");
    Date merCreated = sdf.parse("2010/07/04");
    Date merUpdate = sdf.parse("2010/07/08");
    Date montagneCreated = sdf.parse("2010/03/12");
    Date montagneUpdate = null;
    Date cielCreated = sdf.parse("2010/03/01");
    Date cielUpdate = sdf.parse("2010/05/01");
    PhotoDetail ciel = new PhotoDetail("ciel", "france", cielCreated, cielUpdate, null, null, false,
        false);
    String cielId = "0";
    PhotoPK photoPK0 = new PhotoPK(cielId, "gallery25");
    ciel.setPhotoPK(photoPK0);
    ciel.setCreatorId("0");
    ciel.setUpdateId("1");
    ciel.setSizeH(110);
    ciel.setSizeL(110);
    ciel.setAlbumId("0");
    ciel.setImageName("ciel.jpg");
    ciel.setImageSize(5146);
    ciel.setImageMimeType("image/png");
    PhotoDetail montagne = new PhotoDetail("montagne", "alpes", montagneCreated, montagneUpdate,
        null, null, false, false);
    String montagneId = "1";
    PhotoPK photoPK1 = new PhotoPK(montagneId, "gallery27");
    montagne.setPhotoPK(photoPK1);
    montagne.setCreatorId("1");
    montagne.setSizeH(110);
    montagne.setSizeL(110);
    montagne.setAlbumId("0");
    montagne.setImageName("montagne.jpg");
    montagne.setImageSize(5146);
    montagne.setImageMimeType("image/png");
    PhotoDetail animal = new PhotoDetail("animal", "chien", animalCreated, animalUpdate, null, null,
        false, false);
    String animalId = "2";
    PhotoPK photoPK2 = new PhotoPK(animalId, "gallery26");
    animal.setPhotoPK(photoPK2);
    animal.setCreatorId("0");
    animal.setUpdateId("1");
    animal.setSizeH(110);
    animal.setSizeL(110);
    animal.setAlbumId("0");
    animal.setImageName("animal.jpg");
    animal.setImageSize(5146);
    animal.setImageMimeType("image/png");
    PhotoDetail fleur = new PhotoDetail("fleur", "tulipe", fleurCreated, fleurUpdate, null, null,
        false, false);
    String fleurId = "3";
    PhotoPK photoPK3 = new PhotoPK(fleurId, "gallery26");
    fleur.setPhotoPK(photoPK3);
    fleur.setCreatorId("1");
    fleur.setUpdateId("0");
    fleur.setSizeH(110);
    fleur.setSizeL(110);
    fleur.setAlbumId("0");
    fleur.setImageName("fleur.jpg");
    fleur.setImageSize(5146);
    fleur.setImageMimeType("image/png");
    PhotoDetail mer = new PhotoDetail("mer", "mediterranee", merCreated, merUpdate, null, null,
        false, false);
    String merId = "4";
    PhotoPK photoPK4 = new PhotoPK(merId, "gallery27");
    mer.setPhotoPK(photoPK4);
    mer.setCreatorId("1");
    mer.setUpdateId("1");
    mer.setSizeH(110);
    mer.setSizeL(110);
    mer.setAlbumId("0");
    mer.setImageName("mer.jpg");
    mer.setImageSize(5146);
    mer.setImageMimeType("image/png");

    String userid = "1";
    try {
      List<PhotoDetail> photos = galleryBmEjb.getAllPhotosbyUserid(userid);
      assertNotNull("Photos should exist", photos);
      assertEquals("Should have 6 date creation or update ", 6, photos.size());
      assertEquals(photos.get(0).getId(), merId);
      assertEquals(true, photos.get(0).getUpdateDate().equals(merUpdate));
      assertEquals(true, photos.get(1).getUpdateDate().equals(merUpdate));
      assertEquals(true, photos.get(0).getCreationDate().equals(merCreated));
      assertEquals(true, photos.get(1).getCreationDate().equals(merCreated));
      assertEquals(true, photos.get(1).getCreationDate().before(photos.get(0).getUpdateDate()));
      assertEquals(true, photos.get(1).getCreationDate().equals(photos.get(0).getCreationDate()));
      assertEquals(true, photos.get(2).getCreationDate().before(photos.get(1).getUpdateDate()));
      assertEquals(true, photos.get(2).getCreationDate().before(photos.get(1).getCreationDate()));
      assertEquals(true, photos.get(3).getUpdateDate().before(photos.get(2).getUpdateDate()));
      assertEquals(true, photos.get(3).getCreationDate().before(photos.get(2).getCreationDate()));
      assertEquals(true, photos.get(3).getCreationDate().before(photos.get(4).getUpdateDate()));
      assertEquals(true, photos.get(4).getCreationDate().before(photos.get(3).getUpdateDate()));
      assertEquals(true, photos.get(4).getUpdateDate().before(photos.get(3).getUpdateDate()));
      assertEquals(true, photos.get(3).getCreationDate().before(photos.get(5).getCreationDate()));
      assertEquals(true, photos.get(5).getCreationDate().before(photos.get(4).getCreationDate()));
      assertEquals(null, photos.get(5).getUpdateDate());

      assertEquals(mer, photos.get(0));
      assertEquals(mer, photos.get(1));
      assertEquals(fleur, photos.get(2));
      assertEquals(ciel, photos.get(3));
      assertEquals(animal, photos.get(4));
      assertEquals(montagne, photos.get(5));


    } finally {
      closeConnection(connexion);
    }

  }

  @Test
  public void testPhotoWasUpdated() throws Exception {
    IDatabaseConnection connexion = null;
    connexion = getConnection();
    this.setUp();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    Date fleurCreated = sdf.parse("2010/06/15");
    Date fleurUpdate = sdf.parse("2010/06/16");
    Date animalCreated = sdf.parse("2010/04/10");
    Date animalUpdate = sdf.parse("2010/04/14");
    Date merCreated = sdf.parse("2010/07/04");
    Date merUpdate = sdf.parse("2010/07/08");
    Date montagneCreated = sdf.parse("2010/03/12");
    Date montagneUpdate = null;
    Date cielCreated = sdf.parse("2010/03/01");
    Date cielUpdate = sdf.parse("2010/05/01");
    PhotoDetail ciel = new PhotoDetail("ciel", "france", cielCreated, cielUpdate, null, null, false,
        false);
    String cielId = "0";
    PhotoPK photoPK0 = new PhotoPK(cielId, "gallery25");
    ciel.setPhotoPK(photoPK0);
    ciel.setCreatorId("0");
    ciel.setUpdateId("1");
    ciel.setSizeH(110);
    ciel.setSizeL(110);
    ciel.setAlbumId("0");
    ciel.setImageName("ciel.jpg");
    ciel.setImageSize(5146);
    ciel.setImageMimeType("image/png");
    PhotoDetail montagne = new PhotoDetail("montagne", "alpes", montagneCreated, montagneUpdate,
        null, null, false, false);
    String montagneId = "1";
    PhotoPK photoPK1 = new PhotoPK(montagneId, "gallery27");
    montagne.setPhotoPK(photoPK1);
    montagne.setCreatorId("1");
    montagne.setSizeH(110);
    montagne.setSizeL(110);
    montagne.setAlbumId("0");
    montagne.setImageName("montagne.jpg");
    montagne.setImageSize(5146);
    montagne.setImageMimeType("image/png");
    PhotoDetail animal = new PhotoDetail("animal", "chien", animalCreated, animalUpdate, null, null,
        false, false);
    String animalId = "2";
    PhotoPK photoPK2 = new PhotoPK(animalId, "gallery26");
    animal.setPhotoPK(photoPK2);
    animal.setCreatorId("0");
    animal.setUpdateId("1");
    animal.setSizeH(110);
    animal.setSizeL(110);
    animal.setAlbumId("0");
    animal.setImageName("animal.jpg");
    animal.setImageSize(5146);
    animal.setImageMimeType("image/png");
    PhotoDetail fleur = new PhotoDetail("fleur", "tulipe", fleurCreated, fleurUpdate, null, null,
        false, false);
    String fleurId = "3";
    PhotoPK photoPK3 = new PhotoPK(fleurId, "gallery26");
    fleur.setPhotoPK(photoPK3);
    fleur.setCreatorId("1");
    fleur.setUpdateId("0");
    fleur.setSizeH(110);
    fleur.setSizeL(110);
    fleur.setAlbumId("0");
    fleur.setImageName("fleur.jpg");
    fleur.setImageSize(5146);
    fleur.setImageMimeType("image/png");
    PhotoDetail mer = new PhotoDetail("mer", "mediterranee", merCreated, merUpdate, null, null,
        false, false);
    String merId = "4";
    PhotoPK photoPK4 = new PhotoPK(merId, "gallery27");
    mer.setPhotoPK(photoPK4);
    mer.setCreatorId("1");
    mer.setUpdateId("1");
    mer.setSizeH(110);
    mer.setSizeL(110);
    mer.setAlbumId("0");
    mer.setImageName("mer.jpg");
    mer.setImageSize(5146);
    mer.setImageMimeType("image/png");
    String userid = "1";
    try {
      List<PhotoDetail> photos = galleryBmEjb.getAllPhotosbyUserid(userid);
      assertNotNull("Photos should exist", photos);
      assertEquals("Should have 6 date creation or update ", 6, photos.size());
      assertEquals(photos.get(0).getId(), merId);
      assertEquals(photos.get(1).getId(), merId);
      assertEquals(photos.get(2).getId(), fleurId);
      assertEquals(photos.get(3).getId(), cielId);
      assertEquals(photos.get(4).getId(), animalId);
      assertEquals(photos.get(5).getId(), montagneId);

      List<PhotoDetail> photos_1 = photos.subList(1, photos.size());
      List<String> photosID_1 = new ArrayList<String>();
      for (PhotoDetail photo : photos_1) {
        photosID_1.add(photo.getId());
      }
      assertEquals(photosID_1.get(0), merId);
      assertEquals(photosID_1.get(1), fleurId);
      assertEquals(photosID_1.get(2), cielId);
      assertEquals(photosID_1.get(3), animalId);
      assertEquals(photosID_1.get(4), montagneId);

      assertNotNull("Photos should exist", photos_1);
      assertNotNull("Photos should exist", photosID_1);
      assertEquals("Should have 4 date creation or update ", 5, photos_1.size());
      assertEquals(photosID_1.size(), photos_1.size());
      boolean updatemer = galleryBmEjb.photoWasUpdated(mer, userid, photosID_1);
      assertEquals(true, updatemer);

      List<PhotoDetail> photos_2 = photos.subList(2, photos.size());
      List<String> photosID_2 = new ArrayList<String>();
      for (PhotoDetail photo : photos_2) {
        photosID_2.add(photo.getId());
      }
      assertEquals(photosID_2.get(0), fleurId);
      assertEquals(photosID_2.get(1), cielId);
      assertEquals(photosID_2.get(2), animalId);
      assertNotNull("Photos should exist", photos_2);
      assertNotNull("Photos should exist", photosID_2);
      assertEquals("Should have 3 date creation or update ", 4, photos_2.size());
      assertEquals(photosID_2.size(), photos_2.size());
      boolean updatemercreation = galleryBmEjb.photoWasUpdated(mer, userid, photosID_2);
      assertEquals(false, updatemercreation);


      List<PhotoDetail> photos_3 = photos.subList(3, photos.size());
      List<String> photosID_3 = new ArrayList<String>();
      for (PhotoDetail photo : photos_3) {
        photosID_3.add(photo.getId());
      }
      assertNotNull("Photos should exist", photos_3);
      assertNotNull("Photos should exist", photosID_3);
      assertEquals("Should have 2 date creation or update ", 3, photos_3.size());
      assertEquals(photosID_3.size(), photos_3.size());
      boolean updatefleur = galleryBmEjb.photoWasUpdated(fleur, userid, photosID_3);
      assertEquals(false, updatefleur);


      List<PhotoDetail> photos_4 = photos.subList(4, photos.size());
      List<String> photosID_4 = new ArrayList<String>();
      for (PhotoDetail photo : photos_4) {
        photosID_4.add(photo.getId());
      }
      assertNotNull("Photos should exist", photos_4);
      assertNotNull("Photos should exist", photosID_4);
      assertEquals("Should have 1 date creation or update ", 2, photos_4.size());
      assertEquals(photosID_4.size(), photos_4.size());
      boolean updateciel = galleryBmEjb.photoWasUpdated(ciel, userid, photosID_3);
      assertEquals(true, updateciel);


      List<PhotoDetail> photos_5 = photos.subList(5, photos.size());
      List<String> photosID_5 = new ArrayList<String>();
      for (PhotoDetail photo : photos_5) {
        photosID_5.add(photo.getId());
      }
      assertNotNull("Photos should exist", photos_5);
      assertNotNull("Photos should exist", photosID_5);
      assertEquals("Should have 1 date creation or update ", 1, photos_5.size());
      assertEquals(photosID_5.size(), photos_5.size());
      boolean updateanimal = galleryBmEjb.photoWasUpdated(animal, userid, photosID_5);
      assertEquals(true, updateanimal);

      List<PhotoDetail> photos_6 = photos.subList(6, photos.size());
      List<String> photosID_6 = new ArrayList<String>();
      assertEquals("Should have 0 date creation or update ", 0, photos_6.size());
      assertEquals(photosID_6.size(), photos_6.size());
      boolean updateMontagne = galleryBmEjb.photoWasUpdated(montagne, userid, photosID_6);
      assertEquals(false, updateMontagne);

    } finally {
      closeConnection(connexion);
    }

  }

  @Test
  public void testGetAllPhotosWithStatusbyUserid() throws Exception {
    IDatabaseConnection connexion = null;
    connexion = getConnection();
    this.setUp();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    Date fleurCreated = sdf.parse("2010/06/15");
    Date fleurUpdate = sdf.parse("2010/06/16");
    Date animalCreated = sdf.parse("2010/04/10");
    Date animalUpdate = sdf.parse("2010/04/14");
    Date merCreated = sdf.parse("2010/07/04");
    Date merUpdate = sdf.parse("2010/07/08");
    Date montagneCreated = sdf.parse("2010/03/12");
    Date montagneUpdate = null;
    Date cielCreated = sdf.parse("2010/03/01");
    Date cielUpdate = sdf.parse("2010/05/01");
    PhotoDetail ciel = new PhotoDetail("ciel", "france", cielCreated, cielUpdate, null, null, false,
        false);
    PhotoWithStatus updateCiel = new PhotoWithStatus(ciel, true);
    String cielId = "0";
    PhotoPK photoPK0 = new PhotoPK(cielId, "gallery25");
    ciel.setPhotoPK(photoPK0);
    ciel.setCreatorId("0");
    ciel.setUpdateId("1");
    ciel.setSizeH(110);
    ciel.setSizeL(110);
    ciel.setAlbumId("0");
    ciel.setImageName("ciel.jpg");
    ciel.setImageSize(5146);
    ciel.setImageMimeType("image/png");
    PhotoDetail montagne = new PhotoDetail("montagne", "alpes", montagneCreated, montagneUpdate,
        null, null, false, false);
    PhotoWithStatus updateMontagne = new PhotoWithStatus(montagne, false);
    String montagneId = "1";
    PhotoPK photoPK1 = new PhotoPK(montagneId, "gallery27");
    montagne.setPhotoPK(photoPK1);
    montagne.setCreatorId("1");
    montagne.setSizeH(110);
    montagne.setSizeL(110);
    montagne.setAlbumId("0");
    montagne.setImageName("montagne.jpg");
    montagne.setImageSize(5146);
    montagne.setImageMimeType("image/png");
    PhotoDetail animal = new PhotoDetail("animal", "chien", animalCreated, animalUpdate, null, null,
        false, false);
    String animalId = "2";
    PhotoPK photoPK2 = new PhotoPK(animalId, "gallery26");
    animal.setPhotoPK(photoPK2);
    animal.setCreatorId("0");
    animal.setUpdateId("1");
    animal.setSizeH(110);
    animal.setSizeL(110);
    animal.setAlbumId("0");
    animal.setImageName("animal.jpg");
    animal.setImageSize(5146);
    animal.setImageMimeType("image/png");
    PhotoWithStatus updateAnimal = new PhotoWithStatus(animal, true);
    PhotoDetail fleur = new PhotoDetail("fleur", "tulipe", fleurCreated, fleurUpdate, null, null,
        false, false);
    String fleurId = "3";
    PhotoPK photoPK3 = new PhotoPK(fleurId, "gallery26");
    fleur.setPhotoPK(photoPK3);
    fleur.setCreatorId("1");
    fleur.setUpdateId("0");
    fleur.setSizeH(110);
    fleur.setSizeL(110);
    fleur.setAlbumId("0");
    fleur.setImageName("fleur.jpg");
    fleur.setImageSize(5146);
    fleur.setImageMimeType("image/png");
    PhotoWithStatus updateFleur = new PhotoWithStatus(fleur, false);
    PhotoDetail mer = new PhotoDetail("mer", "mediterranee", merCreated, merUpdate, null, null,
        false, false);
    String merId = "4";
    PhotoPK photoPK4 = new PhotoPK(merId, "gallery27");
    mer.setPhotoPK(photoPK4);
    mer.setCreatorId("1");
    mer.setUpdateId("1");
    mer.setSizeH(110);
    mer.setSizeL(110);
    mer.setAlbumId("0");
    mer.setImageName("mer.jpg");
    mer.setImageSize(5146);
    mer.setImageMimeType("image/png");
    PhotoWithStatus update1Mer = new PhotoWithStatus(mer, true);
    PhotoWithStatus update2Mer = new PhotoWithStatus(mer, false);

    String userid = "1";
    try {
      List<PhotoWithStatus> photos = galleryBmEjb.getAllPhotosWithStatusbyUserid(userid);
      assertNotNull("Photos should exist", photos);
      assertEquals("Should have 6 date creation or update ", 6, photos.size());
      assertEquals(photos.get(0), update1Mer);
      assertEquals(photos.get(1), update2Mer);
      assertEquals(photos.get(2), updateFleur);
      assertEquals(photos.get(3), updateCiel);
      assertEquals(photos.get(4), updateAnimal);
      assertEquals(photos.get(5), updateMontagne);

    } finally {
      closeConnection(connexion);
    }
  }

  @Test
  public void testGetAllPhotosWithStatusbyUseridNumberAgain() throws Exception {
    IDatabaseConnection connexion = null;
    connexion = getConnection();
    this.setUp();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    Date fleurCreated = sdf.parse("2010/06/15");
    Date fleurUpdate = sdf.parse("2010/06/16");
    Date merCreated = sdf.parse("2010/07/04");
    Date merUpdate = sdf.parse("2010/07/08");
    Date cielCreated = sdf.parse("2010/03/01");
    Date cielUpdate = sdf.parse("2010/05/01");
    PhotoDetail ciel = new PhotoDetail("ciel", "france", cielCreated, cielUpdate, null, null, false,
        false);
    PhotoWithStatus updateCiel = new PhotoWithStatus(ciel, true);
    String cielId = "0";
    PhotoPK photoPK0 = new PhotoPK(cielId, "gallery25");
    ciel.setPhotoPK(photoPK0);
    ciel.setCreatorId("0");
    ciel.setUpdateId("1");
    ciel.setSizeH(110);
    ciel.setSizeL(110);
    ciel.setAlbumId("0");
    ciel.setImageName("ciel.jpg");
    ciel.setImageSize(5146);
    ciel.setImageMimeType("image/png");
    PhotoDetail fleur = new PhotoDetail("fleur", "tulipe", fleurCreated, fleurUpdate, null, null,
        false, false);
    String fleurId = "3";
    PhotoPK photoPK3 = new PhotoPK(fleurId, "gallery26");
    fleur.setPhotoPK(photoPK3);
    fleur.setCreatorId("1");
    fleur.setUpdateId("0");
    fleur.setSizeH(110);
    fleur.setSizeL(110);
    fleur.setAlbumId("0");
    fleur.setImageName("fleur.jpg");
    fleur.setImageSize(5146);
    fleur.setImageMimeType("image/png");
    PhotoWithStatus updateFleur = new PhotoWithStatus(fleur, false);
    PhotoDetail mer = new PhotoDetail("mer", "mediterranee", merCreated, merUpdate, null, null,
        false, false);
    String merId = "4";
    PhotoPK photoPK4 = new PhotoPK(merId, "gallery27");
    mer.setPhotoPK(photoPK4);
    mer.setCreatorId("1");
    mer.setUpdateId("1");
    mer.setSizeH(110);
    mer.setSizeL(110);
    mer.setAlbumId("0");
    mer.setImageName("mer.jpg");
    mer.setImageSize(5146);
    mer.setImageMimeType("image/png");
    PhotoWithStatus update1Mer = new PhotoWithStatus(mer, true);
    PhotoWithStatus update2Mer = new PhotoWithStatus(mer, false);

    String userid = "1";
    int firstIndex = 0;

    try {
      List<PhotoWithStatus> photos = galleryBmEjb.getAllPhotosWithStatusbyUserid(userid, firstIndex,
          2);
      assertNotNull("Photos should exist", photos);
      assertEquals("Should have 2 date creation or update ", 2, photos.size());
      assertEquals(photos.get(0), update1Mer);
      PhotoWithStatus myPhoto = photos.get(1);
      assertNotNull(myPhoto);
      assertFalse(myPhoto.isUpdate());
      assertEquals(update2Mer, myPhoto);
      assertEquals(mer, myPhoto.getPhoto());


      firstIndex = firstIndex + 2;
      photos = galleryBmEjb.getAllPhotosWithStatusbyUserid(userid, firstIndex, 2);
      assertNotNull("Photos should exist", photos);
      assertEquals("Should have 2 date creation or update ", 2, photos.size());
      assertEquals(updateFleur, photos.get(0));
      assertEquals(fleur, updateFleur.getPhoto());
      assertFalse(updateFleur.isUpdate());
      assertEquals(updateCiel, photos.get(1));
      assertEquals(ciel, updateCiel.getPhoto());
      assertTrue(updateCiel.isUpdate());
    } finally {
      closeConnection(connexion);
    }
  }

  @Override
  protected String getDatasetFileName() {
    return "dataset_Gallery.xml";
  }
}
