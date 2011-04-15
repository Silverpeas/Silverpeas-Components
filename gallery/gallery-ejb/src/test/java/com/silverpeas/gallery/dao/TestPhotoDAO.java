/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dbunit.database.IDatabaseConnection;

import com.silverpeas.components.model.AbstractTestDao;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;
import com.silverpeas.gallery.model.PhotoWithStatus;
import com.silverpeas.gallery.socialNetwork.SocialInformationGallery;
import com.silverpeas.socialNetwork.model.SocialInformation;
import com.stratelia.webactiv.util.DateUtil;

public class TestPhotoDAO extends AbstractTestDao {

  public void testGetAllPhotosIdbyUserid() throws Exception {
    IDatabaseConnection connexion = null;
    this.setUp();

    String userid = "1";
    try {
      connexion = getConnection();
      PhotoDetail ciel = PhotoDAO.getPhoto(connexion.getConnection(), 0);
      PhotoDetail fleur = PhotoDAO.getPhoto(connexion.getConnection(), 3);
      PhotoDetail mer = PhotoDAO.getPhoto(connexion.getConnection(), 4);
      SocialInformationGallery socialCiel = new SocialInformationGallery(new PhotoWithStatus(ciel,
          true));
      SocialInformationGallery socialFleur = new SocialInformationGallery(new PhotoWithStatus(fleur,
          false));
      SocialInformationGallery socialmer1 = new SocialInformationGallery(new PhotoWithStatus(mer,
          true));
      SocialInformationGallery socialmer2 = new SocialInformationGallery(new PhotoWithStatus(mer,
          false));

      Date begin = DateUtil.parse("2010/05/01");
      Date end = DateUtil.parse("2010/08/31");
      List<SocialInformation> photos = PhotoDAO.getAllPhotosIDbyUserid(connexion.getConnection(),
          userid, begin, end);
      assertNotNull("Photos should exist", photos);
      assertEquals("Should have 4 date creation or update ", 4, photos.size());
      assertEquals(photos.get(0), socialmer1);
      assertEquals(photos.get(1), socialmer2);
      assertEquals(photos.get(2), socialFleur);
      assertEquals(photos.get(3), socialCiel);
    } finally {
      closeConnection(connexion);
    }
  }

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
    int fleurId = 3;
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

  public void testgetSocialInformationsList() throws Exception {
    IDatabaseConnection connexion = null;
    this.setUp();
    connexion = getConnection();
    List<String> availableList = new ArrayList<String>();
    availableList.add("gallery25");
    availableList.add("gallery26");
    List<String> listOfuserId = new ArrayList<String>();
    listOfuserId.add("1");
    try {
      PhotoDetail ciel = PhotoDAO.getPhoto(connexion.getConnection(), 0);
      PhotoDetail fleur = PhotoDAO.getPhoto(connexion.getConnection(), 3);
      SocialInformationGallery socialCiel = new SocialInformationGallery(new PhotoWithStatus(ciel,
          true));
      SocialInformationGallery socialFleur = new SocialInformationGallery(new PhotoWithStatus(fleur,
          false));

      Date begin = DateUtil.parse("2010/05/01");
      Date end = DateUtil.parse("2010/08/31");

      List<SocialInformation> photos = PhotoDAO.getSocialInformationsListOfMyContacts(connexion.
          getConnection(), listOfuserId, null, begin, end);
      assertNotNull("Photos should exist", photos);
      photos = PhotoDAO.getSocialInformationsListOfMyContacts(connexion.getConnection(),
          listOfuserId, availableList, begin, end);
      assertNotNull("Photos should exist", photos);
      assertEquals(photos.size(), 2);
      assertEquals(photos.get(0), socialCiel);
      assertEquals(photos.get(1), socialFleur);
    } finally {
      closeConnection(connexion);
    }

  }

  @Override
  protected String getDatasetFileName() {
    return "photo_dataset.xml";
  }
}
