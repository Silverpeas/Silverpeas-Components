/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.silverpeas.gallery.BaseGalleryTest;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;
import com.silverpeas.gallery.model.PhotoWithStatus;
import com.silverpeas.gallery.socialNetwork.SocialInformationGallery;
import com.silverpeas.socialnetwork.model.SocialInformation;
import com.stratelia.webactiv.util.DateUtil;

public class PhotoDaoTest extends BaseGalleryTest {

  private static final String GALLERY0 = "gallery25";
  private static final String GALLERY1 = "gallery26";
  private static final String GALLERY2 = "gallery27";

  @Override
  public String getResource() {
    return "com/silverpeas/gallery/dao/photo_dataset.xml";
  }

  @Test
  public void testGetAllPhotosIdbyUserid() throws Exception {
    Connection connexion = getConnection();
    String userid = "1";
    try {
      PhotoDetail ciel = PhotoDAO.getPhoto(connexion, 0);
      PhotoDetail fleur = PhotoDAO.getPhoto(connexion, 3);
      PhotoDetail mer = PhotoDAO.getPhoto(connexion, 4);
      SocialInformation socialCiel = new SocialInformationGallery(new PhotoWithStatus(ciel, true));
      SocialInformation socialFleur =
          new SocialInformationGallery(new PhotoWithStatus(fleur, false));
      SocialInformation socialmer1 = new SocialInformationGallery(new PhotoWithStatus(mer, true));
      SocialInformation socialmer2 = new SocialInformationGallery(new PhotoWithStatus(mer, false));
      Date begin = DateUtil.parse("2010/05/01");
      Date end = DateUtil.parse("2010/08/31");
      List<SocialInformation> photos =
          PhotoDAO.getAllPhotosIDbyUserid(connexion, userid, begin, end);
      assertThat(photos, notNullValue());
      assertThat(photos, hasSize(4));
      assertThat(photos.get(0), equalTo(socialmer1));
      assertThat(photos.get(0).isUpdeted(), equalTo(true));
      assertThat(photos.get(1), equalTo(socialmer2));
      assertThat(photos.get(1).isUpdeted(), equalTo(false));
      assertThat(photos.get(2), equalTo(socialFleur));
      assertThat(photos.get(3), equalTo(socialCiel));
    } finally {
      connexion.close();
    }
  }

  @Test
  public void testGetAllPhotosIDbyUserid() throws Exception {
    Connection con = getConnection();
    final String creatorId = "0";
    List<String> expectedItems = Arrays.asList("0", "2", "3");
    try {
      List<String> photos = PhotoDAO.getAllPhotosIDbyUserid(con, creatorId);
      for (String expectedItem : expectedItems) {
        assertThat(photos, hasItem(expectedItem));
      }
    } finally {
      con.close();
    }
  }

  @Test
  public void testGetPhotoDetail() throws Exception {
    Connection connexion = getConnection();
    Date fleurcreated = DateUtil.parse("2010/06/15");
    Date fleurupdate = DateUtil.parse("2010/06/16");
    PhotoDetail fleur = new PhotoDetail("fleur", "tulipe", fleurcreated, fleurupdate, null, null,
        false, false);
    int fleurId = 3;
    PhotoPK photoPK = new PhotoPK(String.valueOf(fleurId), GALLERY1);
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
      PhotoDetail photo = PhotoDAO.getPhoto(connexion, fleurId);
      assertThat(photo, notNullValue());
      assertThat(photo.getTitle(), equalTo(fleur.getTitle()));
      assertThat(photo.getId(), equalTo(photoPK.getId()));
      assertThat(photo.getDescription(), equalTo(fleur.getDescription()));
      assertThat(photo.getCreationDate(), equalTo(fleurcreated));
      assertThat(photo.getUpdateDate(), equalTo(fleurupdate));
      assertThat(photo, equalTo(fleur));

    } finally {
      connexion.close();
    }
  }

  @Test
  public void testUpdatePhotoDetail() throws Exception {
    Connection con = getConnection();
    Date createdFlower = DateUtil.parse("2010/06/15");
    Date updatedFlower = DateUtil.parse("2010/06/18");
    PhotoDetail flower =
        new PhotoDetail("Flower", "tulip", createdFlower, updatedFlower, null, null,
            false, true);
    int flowerId = 3;
    long imageSize = 6000;
    PhotoPK photoPK = new PhotoPK(String.valueOf(flowerId), GALLERY1);
    flower.setPhotoPK(photoPK);
    flower.setCreatorId("1");
    flower.setUpdateId("0");
    flower.setSizeH(220);
    flower.setSizeL(220);
    flower.setAlbumId("0");
    flower.setImageName("flower.jpg");
    flower.setImageSize(imageSize);
    flower.setImageMimeType("image/png");
    flower.setKeyWord("flower test");
    try {
      PhotoDAO.updatePhoto(con, flower);
      PhotoDetail photo = PhotoDAO.getPhoto(con, flowerId);
      assertThat(photo, notNullValue());
      assertThat(photo.getTitle(), equalTo(flower.getTitle()));
      assertThat(photo.getId(), equalTo(photoPK.getId()));
      assertThat(photo.getDescription(), equalTo(flower.getDescription()));
      assertThat(photo.getCreationDate(), equalTo(createdFlower));
      assertThat(photo.getUpdateDate(), equalTo(updatedFlower));
      assertThat(photo.getImageSize(), equalTo(imageSize));
      assertThat(photo, equalTo(flower));
    } finally {
      con.close();
    }
  }

  @Test
  public void testgetSocialInformationsList() throws Exception {
    Connection connexion = getConnection();
    List<String> availableList = new ArrayList<String>();
    availableList.add(GALLERY0);
    availableList.add(GALLERY1);
    List<String> listOfuserId = new ArrayList<String>();
    listOfuserId.add("1");
    try {
      PhotoDetail ciel = PhotoDAO.getPhoto(connexion, 0);
      PhotoDetail fleur = PhotoDAO.getPhoto(connexion, 3);
      SocialInformation socialCiel = new SocialInformationGallery(new PhotoWithStatus(ciel,
          true));
      SocialInformation socialFleur = new SocialInformationGallery(new PhotoWithStatus(fleur,
          false));

      Date begin = DateUtil.parse("2010/05/01");
      Date end = DateUtil.parse("2010/08/31");

      List<SocialInformation> photos = PhotoDAO.getSocialInformationsListOfMyContacts(connexion,
          listOfuserId, null, begin, end);
      assertThat(photos, notNullValue());
      photos = PhotoDAO.getSocialInformationsListOfMyContacts(connexion,
          listOfuserId, availableList, begin, end);
      assertThat(photos, notNullValue());
      assertThat(photos, hasSize(4));
      assertThat(photos.get(0), equalTo(socialCiel));
      assertThat(photos.get(1), equalTo(socialFleur));
    } finally {
      connexion.close();
    }
  }

  @Test
  public void testDeletePhoto() throws Exception {
    Connection con = getConnection();
    int photoIdToDelete = 2;
    try {
      assertThat(PhotoDAO.getPhoto(con, photoIdToDelete), notNullValue());
      PhotoDAO.removePhoto(con, photoIdToDelete);
      assertThat(PhotoDAO.getPhoto(con, photoIdToDelete).getPhotoPK(), nullValue());
    } finally {
      con.close();
    }
  }

  @Test
  public void testGetPhotoNotVisible() throws Exception {
    Connection con = getConnection();
    try {
      String instanceWithNotVisiblePhoto = GALLERY1;
      Collection<PhotoDetail> photos =
          PhotoDAO.getPhotoNotVisible(con, instanceWithNotVisiblePhoto);
      assertThat(photos, notNullValue());
      assertThat(photos, hasSize(1));
      String instanceWithoutNotVisiblePhoto = GALLERY2;
      photos = PhotoDAO.getPhotoNotVisible(con, instanceWithoutNotVisiblePhoto);
      assertThat(photos, empty());
    } finally {
      con.close();
    }
  }

  @Test
  public void testGetPhotoPathList() throws Exception {
    Connection con = getConnection();
    try {
      Collection<String> pathList = PhotoDAO.getPathList(con, GALLERY1, "5");
      assertThat(pathList, notNullValue());
      assertThat(pathList, hasSize(1));
      assertThat(pathList, contains("2"));
    } finally {
      con.close();
    }
  }

  @Test
  public void testDeletePhotoPath() throws Exception {
    String photoId = "5";
    Connection con = getConnection();
    try {
      PhotoDAO.deletePhotoPath(con, photoId, GALLERY2);
      Collection<String> pathList = PhotoDAO.getPathList(con, GALLERY2, photoId);
      assertThat(pathList, empty());
    } finally {
      con.close();
    }
  }

  @Test
  public void testCreatePhotoPath() throws Exception {
    String merPhotoId = "4";
    Connection con = getConnection();
    try {
      Collection<String> pathList = PhotoDAO.getPathList(con, GALLERY1, merPhotoId);
      assertThat(pathList, hasSize(1));
      PhotoDetail mer = PhotoDAO.getPhoto(con, Integer.parseInt(merPhotoId));
      PhotoDAO.createPath(con, mer, "2");
      pathList = PhotoDAO.getPathList(con, GALLERY1, merPhotoId);
      assertThat(pathList, hasSize(2));
    } finally {
      con.close();
    }

  }

  @Test
  public void testIsVisible() throws Exception {
    Connection con = getConnection();
    String visibleDateStr = "02/05/2014";
    String visibleDateBeginLimitStr = "01/05/2014";
    String visibleDateEndLimitStr = "15/05/2014";
    String invisibleDateBeforeStr = "30/04/2014";
    String invisibleDateAfterStr = "16/05/2014";
    String instanceWithNotVisiblePhoto = GALLERY1;
    try {
      Collection<PhotoDetail> photos =
          PhotoDAO.getPhotoNotVisible(con, instanceWithNotVisiblePhoto);
      PhotoDetail photo = photos.iterator().next();
      assertThat(PhotoDAO.isVisible(photo, DateUtil.parse(visibleDateStr, "dd/MM/yyyy")),
          equalTo(true));
      assertThat(PhotoDAO.isVisible(photo, DateUtil.parse(visibleDateBeginLimitStr, "dd/MM/yyyy")),
          equalTo(true));
      assertThat(PhotoDAO.isVisible(photo, DateUtil.parse(visibleDateEndLimitStr, "dd/MM/yyyy")),
          equalTo(true));
      assertThat(PhotoDAO.isVisible(photo, DateUtil.parse(invisibleDateBeforeStr, "dd/MM/yyyy")),
          equalTo(false));
      assertThat(PhotoDAO.isVisible(photo, DateUtil.parse(invisibleDateAfterStr, "dd/MM/yyyy")),
          equalTo(false));
    } finally {
      con.close();
    }

  }

  @Test
  public void testGetLastUploaded() throws Exception {
    Connection con = getConnection();

    try {
      Collection<PhotoDetail> allLastUploadedPhotos = PhotoDAO.getDernieres(con, GALLERY1, true);
      assertThat(allLastUploadedPhotos, notNullValue());
      assertThat(allLastUploadedPhotos, hasSize(4));

      Collection<PhotoDetail> lastUploadedPhotos = PhotoDAO.getDernieres(con, GALLERY1, false);
      assertThat(lastUploadedPhotos, notNullValue());
      assertThat(lastUploadedPhotos, hasSize(3));

    } finally {
      con.close();
    }
  }
}
