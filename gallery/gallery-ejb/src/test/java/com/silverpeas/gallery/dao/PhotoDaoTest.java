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

import com.silverpeas.gallery.BaseGalleryTest;
import com.silverpeas.gallery.constant.MediaMimeType;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.model.MediaPK;
import com.silverpeas.gallery.model.MediaWithStatus;
import com.silverpeas.gallery.model.Photo;
import com.silverpeas.gallery.socialNetwork.SocialInformationGallery;
import com.silverpeas.socialnetwork.model.SocialInformation;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;
import org.junit.Test;
import org.silverpeas.cache.service.CacheServiceFactory;
import org.silverpeas.date.Period;
import org.silverpeas.media.Definition;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PhotoDaoTest extends BaseGalleryTest {

  @Override
  public String getDataSetPath() {
    return "com/silverpeas/gallery/dao/photo_dataset.xml";
  }

  @Override
  protected void verifyDataBeforeTest() throws Exception {
    // Skip
  }

  @Test
  public void testGetAllPhotosIdByUserid() throws Exception {
    Connection connexion = getConnection();
    String userid = "1";
    try {
      Photo ciel = PhotoDAO.getPhoto(connexion, "0");
      Photo fleur = PhotoDAO.getPhoto(connexion, "3");
      Photo mer = PhotoDAO.getPhoto(connexion, "4");
      SocialInformation socialCiel =
          new SocialInformationGallery(new MediaWithStatus(ciel.getPhoto(), true));
      SocialInformation socialFleur =
          new SocialInformationGallery(new MediaWithStatus(fleur.getPhoto(), false));
      SocialInformation socialmer1 =
          new SocialInformationGallery(new MediaWithStatus(mer.getPhoto(), true));
      SocialInformation socialmer2 =
          new SocialInformationGallery(new MediaWithStatus(mer.getPhoto(), false));
      Date begin = DateUtil.parse("2010/05/01");
      Date end = DateUtil.parse("2010/08/31");
      List<SocialInformation> photos =
          MediaDAO.getAllMediaIdByUserId(connexion, userid, Period.from(begin, end));
      assertThat(photos, notNullValue());
      assertThat(photos, hasSize(4));
      assertThat(photos.get(0), equalTo(socialmer1));
      assertThat(photos.get(0).isUpdated(), equalTo(true));
      assertThat(photos.get(1), equalTo(socialmer2));
      assertThat(photos.get(1).isUpdated(), equalTo(false));
      assertThat(photos.get(2), equalTo(socialFleur));
      assertThat(photos.get(3), equalTo(socialCiel));
    } finally {
      connexion.close();
    }
  }

  @Test
  public void testGetPhotoDetail() throws Exception {
    Connection connexion = getConnection();
    Date createFlowerDate = Timestamp.valueOf("2010-06-15 00:00:00.0");
    Date updateFlowerDate = Timestamp.valueOf("2010-06-16 00:00:00.0");
    String fleurId = "3";
    MediaPK mediaPK = new MediaPK(fleurId, GALLERY1);
    Photo fleur = prepareExpectedPhoto(createFlowerDate, updateFlowerDate, mediaPK);
    try {
      Photo photo = PhotoDAO.getPhoto(connexion, fleurId);
      assertThat(photo, notNullValue());
      assertThat(photo.getTitle(), equalTo(fleur.getTitle()));
      assertThat(photo.getId(), equalTo(mediaPK.getId()));
      assertThat(photo.getDescription(), equalTo(fleur.getDescription()));
      assertThat(photo.getCreationDate(), equalTo(createFlowerDate));
      assertThat(photo.getLastUpdateDate(), equalTo(updateFlowerDate));
      assertThat(photo, equalTo(fleur));

    } finally {
      connexion.close();
    }
  }

  private Photo prepareExpectedPhoto(Date createFlowerDate, Date updateFlowerDate, MediaPK mediaPK) {
    Photo fleur = new Photo();
    fleur.setTitle("fleur");
    fleur.setDescription("tulipe");
    fleur.setCreationDate(createFlowerDate);
    fleur.setLastUpdateDate(updateFlowerDate);
    fleur.setAuthor(null);
    fleur.setDownloadAuthorized(false);
    fleur.setMediaPK(mediaPK);
    fleur.setCreatorId("1");
    fleur.setLastUpdatedBy("0");
    fleur.setDefinition(Definition.of(110, 110));
    fleur.setFileName("fleur.jpg");
    fleur.setFileSize(5146);
    fleur.setFileMimeType(MediaMimeType.PNG);
    return fleur;
  }

  @Test
  public void testUpdatePhotoDetail() throws Exception {
    Connection con = getConnection();
    Date now = DateUtil.getNow();
    Date createdFlowerDate = Timestamp.valueOf("2010-06-15 00:00:00.0");
    String flowerId = "3";
    MediaPK mediaPK = new MediaPK(String.valueOf(flowerId), GALLERY1);
    Photo flower = prepareExpectedPhoto(createdFlowerDate, createdFlowerDate, mediaPK);
    long imageSize = 6000;
    flower.setLastUpdatedBy("0");
    flower.setDefinition(Definition.of(220, 220));
    flower.setFileName("flower.jpg");
    flower.setFileSize(imageSize);
    flower.setFileMimeType(MediaMimeType.PNG);
    flower.setKeyWord("flower test");
    try {
      PhotoDAO.updatePhoto(con, flower);
      Photo photo = PhotoDAO.getPhoto(con, flowerId);
      assertThat(photo, notNullValue());
      assertThat(photo.getTitle(), equalTo(flower.getTitle()));
      assertThat(photo.getId(), equalTo(mediaPK.getId()));
      assertThat(photo.getDescription(), equalTo(flower.getDescription()));
      assertThat(photo.getCreationDate(), equalTo(createdFlowerDate));
      assertThat(photo.getLastUpdateDate(), greaterThanOrEqualTo(now));
      assertThat(photo.getFileSize(), equalTo(imageSize));
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
      Photo ciel = PhotoDAO.getPhoto(connexion, "0");
      Photo fleur = PhotoDAO.getPhoto(connexion, "3");
      SocialInformation socialCiel =
          new SocialInformationGallery(new MediaWithStatus(ciel, true));
      SocialInformation socialFleur =
          new SocialInformationGallery(new MediaWithStatus(fleur, false));

      Date begin = DateUtil.parse("2010/05/01");
      Date end = DateUtil.parse("2010/08/31");

      List<SocialInformation> photos = MediaDAO.getSocialInformationListOfMyContacts(connexion,
          listOfuserId, null, Period.from(begin, end));
      assertThat(photos, notNullValue());
      photos = MediaDAO.getSocialInformationListOfMyContacts(connexion,
          listOfuserId, availableList, Period.from(begin, end));
      assertThat(photos, notNullValue());
      assertThat(photos, hasSize(4));
      assertThat(photos.get(3), equalTo(socialCiel));
      assertThat(photos.get(2), equalTo(socialFleur));
    } finally {
      connexion.close();
    }
  }

  @Test
  public void testDeletePhoto() throws Exception {
    Connection con = getConnection();
    String photoIdToDelete = "2";
    try {
      assertThat(PhotoDAO.getPhoto(con, photoIdToDelete), notNullValue());
      PhotoDAO.removePhoto(con, photoIdToDelete);
      assertThat(PhotoDAO.getPhoto(con, photoIdToDelete).getMediaPK().getId(), nullValue());
    } finally {
      con.close();
    }
  }

  @Test
  public void testGetPhotoNotVisible() throws Exception {
    Connection con = getConnection();
    try {
      String instanceWithNotVisiblePhoto = GALLERY1;
      Collection<Photo> photos =
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
      String mediaIdToPerform = "5";
      Media media = new Photo();
      media.setId(mediaIdToPerform);
      media.setComponentInstanceId(GALLERY1);

      Collection<String> pathList = MediaDAO.getAlbumIdsOf(con, media);
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
      Media media = new Photo();
      media.setId(photoId);
      media.setComponentInstanceId(GALLERY2);

      PhotoDAO.deletePhotoPath(con, media.getId(), media.getComponentInstanceId());
      Collection<String> pathList = MediaDAO.getAlbumIdsOf(con, media);
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
      Media media = new Photo();
      media.setId(merPhotoId);
      media.setComponentInstanceId(GALLERY1);

      Collection<String> pathList = MediaDAO.getAlbumIdsOf(con, media);
      assertThat(pathList, hasSize(1));
      Photo mer = PhotoDAO.getPhoto(con, merPhotoId);
      PhotoDAO.createPath(con, mer, "2");
      pathList = MediaDAO.getAlbumIdsOf(con, media);
      assertThat(pathList, hasSize(2));
    } finally {
      con.close();
    }

  }

  @Test
  public void testGetLastUploaded() throws Exception {
    Connection con = getConnection();
    try {
      CacheServiceFactory.getSessionCacheService()
          .put(UserDetail.CURRENT_REQUESTER_KEY, publisherUser);

      Collection<Photo> allLastUploadedPhotos = PhotoDAO.getLastRegisteredMedia(con, GALLERY1);
      assertThat(allLastUploadedPhotos, notNullValue());
      assertThat(allLastUploadedPhotos, hasSize(4));

      CacheServiceFactory.getSessionCacheService().put(UserDetail.CURRENT_REQUESTER_KEY, null);

      Collection<Photo> lastUploadedPhotos = PhotoDAO.getLastRegisteredMedia(con, GALLERY1);
      assertThat(lastUploadedPhotos, notNullValue());
      assertThat(lastUploadedPhotos, hasSize(3));

    } finally {
      con.close();
    }
  }
}
