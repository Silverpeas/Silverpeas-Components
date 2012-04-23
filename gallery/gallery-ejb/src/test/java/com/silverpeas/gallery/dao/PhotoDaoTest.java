/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.dao;

import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;
import com.silverpeas.gallery.model.PhotoWithStatus;
import com.silverpeas.gallery.socialNetwork.SocialInformationGallery;
import com.silverpeas.socialNetwork.model.SocialInformation;
import com.stratelia.webactiv.util.DateUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-gallery-embbed-datasource.xml"})
public class PhotoDaoTest {

  @Inject
  private DataSource dataSource;

  @Before
  public void generalSetUp() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(
            PhotoDaoTest.class.getClassLoader().getResourceAsStream(
            "com/silverpeas/gallery/dao/photo_dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
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
      SocialInformation socialFleur = new SocialInformationGallery(new PhotoWithStatus(fleur, false));
      SocialInformation socialmer1 = new SocialInformationGallery(new PhotoWithStatus(mer, true));
      SocialInformation socialmer2 = new SocialInformationGallery(new PhotoWithStatus(mer, false));
      Date begin = DateUtil.parse("2010/05/01");
      Date end = DateUtil.parse("2010/08/31");
      List<SocialInformation> photos = PhotoDAO.getAllPhotosIDbyUserid(connexion, userid, begin, end);
      assertThat(photos, is(notNullValue()));
      assertThat(photos, hasSize(4));
      assertThat(photos.get(0), is(socialmer1));
      assertThat(photos.get(0).isUpdeted(), is(true));
      assertThat(photos.get(1), is(socialmer2));
      assertThat(photos.get(1).isUpdeted(), is(false));
      assertThat(photos.get(2), is(socialFleur));
      assertThat(photos.get(3), is(socialCiel));
    } finally {
      connexion.close();
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
      PhotoDetail photo = PhotoDAO.getPhoto(connexion, fleurId);
      assertThat(photo, is(notNullValue()));
      assertThat(photo.getTitle(), is(fleur.getTitle()));
      assertThat(photo.getId(), is(photoPK.getId()));
      assertThat(photo.getDescription(), is(fleur.getDescription()));
      assertThat(photo.getCreationDate(), is(fleurcreated));
      assertThat(photo.getUpdateDate(), is(fleurupdate));
      assertThat(photo, is(fleur));

    } finally {
      connexion.close();
    }
  }

  @Test
  public void testgetSocialInformationsList() throws Exception {
    Connection connexion = getConnection();
    List<String> availableList = new ArrayList<String>();
    availableList.add("gallery25");
    availableList.add("gallery26");
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
      assertThat(photos, is(notNullValue()));
      photos = PhotoDAO.getSocialInformationsListOfMyContacts(connexion,
              listOfuserId, availableList, begin, end);
      assertThat(photos, is(notNullValue()));
      assertThat(photos, hasSize(2));
      assertThat(photos.get(0), is(socialCiel));
      assertThat(photos.get(1), is(socialFleur));
    } finally {
      connexion.close();
    }
  }

  public Connection getConnection() throws SQLException {
    return this.dataSource.getConnection();
  }
}
