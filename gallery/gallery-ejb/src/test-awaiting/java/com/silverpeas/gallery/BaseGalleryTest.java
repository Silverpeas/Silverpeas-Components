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
package com.silverpeas.gallery;

import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.cache.service.CacheServiceProvider;
import org.silverpeas.core.admin.OrganisationControllerProvider;
import org.silverpeas.util.DBUtil;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.silverpeas.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.persistence.dao.DAOBasedTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Base class for tests in the gallery component.
 * It prepares the database to use in tests.
 */
public abstract class BaseGalleryTest extends DAOBasedTest {

  protected static final String GALLERY0 = "gallery25";
  protected static final String GALLERY1 = "gallery26";
  protected static final String GALLERY2 = "gallery27";

  protected final static String INSTANCE_A = "instanceId_A";
  protected final static Date TODAY = java.sql.Date.valueOf("2014-03-01");
  protected final static Date CREATE_DATE = Timestamp.valueOf("2014-06-01 16:38:52.253");
  protected final static Date LAST_UPDATE_DATE = Timestamp.valueOf("2014-06-03 11:20:42.637");

  protected final static int MEDIA_ROW_COUNT = 12;
  protected final static int MEDIA_INTERNAL_ROW_COUNT = 9;
  protected final static int MEDIA_PHOTO_ROW_COUNT = 3;
  protected final static int MEDIA_VIDEO_ROW_COUNT = 3;
  protected final static int MEDIA_SOUND_ROW_COUNT = 3;
  protected final static int MEDIA_STREAMING_ROW_COUNT = 3;
  protected final static int MEDIA_PATH_ROW_COUNT = 13;
  protected final static int MEDIA_ORDER_ROW_COUNT = 3;
  protected final static int MEDIA_ORDER_DETAIL_ROW_COUNT = 6;

  protected UserDetail adminAccessUser;
  protected UserDetail publisherUser;
  protected UserDetail writerUser;
  protected UserDetail userUser;

  private OrganisationController organisationControllerMock;

  @Override
  public String[] getApplicationContextPath() {
    return new String[]{"/spring-media-embbed-datasource.xml"};
  }

  @Override
  public String getDataSetPath() {
    return "com/silverpeas/gallery/dao/media_dataset.xml";
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    organisationControllerMock = mock(OrganisationController.class);
    ReflectionTestUtils
        .setField(OrganisationControllerProvider.getFactory(), "organisationController",
            organisationControllerMock);
    DBUtil.getInstanceForTest(getDataSource().getConnection());


    verifyDataBeforeTest();

    adminAccessUser = new UserDetail();
    adminAccessUser.setId("adminUserId");
    adminAccessUser.setLastName("adminUserName");
    adminAccessUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    publisherUser = new UserDetail();
    publisherUser.setId("publisherUserId");
    publisherUser.setLastName("publisherUserName");
    writerUser = new UserDetail();
    writerUser.setId("writerUserId");
    writerUser.setLastName("writerUserName");
    userUser = new UserDetail();
    userUser.setId("userUserId");
    userUser.setLastName("userUserName");

    when(getOrganisationControllerMock().getUserDetail(adminAccessUser.getId()))
        .thenReturn(adminAccessUser);
    when(getOrganisationControllerMock().getUserDetail(publisherUser.getId()))
        .thenReturn(publisherUser);
    when(getOrganisationControllerMock().getUserDetail(writerUser.getId())).thenReturn(writerUser);
    when(getOrganisationControllerMock().getUserDetail(userUser.getId())).thenReturn(userUser);

    for (String instanceId : new String[]{INSTANCE_A, GALLERY0, GALLERY1, GALLERY2}) {
      when(getOrganisationControllerMock().getUserProfiles(publisherUser.getId(), instanceId))
          .thenReturn(new String[]{SilverpeasRole.reader.name(), SilverpeasRole.publisher.name()});
      when(getOrganisationControllerMock().getUserProfiles(writerUser.getId(), instanceId))
          .thenReturn(new String[]{SilverpeasRole.reader.name(), SilverpeasRole.writer.name()});
      when(getOrganisationControllerMock().getUserProfiles(userUser.getId(), instanceId))
          .thenReturn(new String[]{SilverpeasRole.reader.name()});
    }
  }

  @Override
  public void tearDown() throws Exception {
    try {
      super.tearDown();
      OrganisationControllerProvider.getFactory().clearFactory();
      CacheServiceProvider.getSessionCacheService().put(UserDetail.CURRENT_REQUESTER_KEY, null);
    } finally {
      DBUtil.clearTestInstance();
    }
  }

  protected OrganisationController getOrganisationControllerMock() {
    return organisationControllerMock;
  }

  /**
   * Verifying the data before a test.
   */
  protected void verifyDataBeforeTest() throws Exception {
    IDataSet actualDataSet = getActualDataSet();
    ITable mediaTable = actualDataSet.getTable("SC_Gallery_Media");
    assertThat(mediaTable.getRowCount(), is(MEDIA_ROW_COUNT));
    ITable internalTable = actualDataSet.getTable("SC_Gallery_Internal");
    assertThat(internalTable.getRowCount(), is(MEDIA_INTERNAL_ROW_COUNT));
    ITable photoTable = actualDataSet.getTable("SC_Gallery_Photo");
    assertThat(photoTable.getRowCount(), is(MEDIA_PHOTO_ROW_COUNT));
    ITable videoTable = actualDataSet.getTable("SC_Gallery_Video");
    assertThat(videoTable.getRowCount(), is(MEDIA_VIDEO_ROW_COUNT));
    ITable soundTable = actualDataSet.getTable("SC_Gallery_Sound");
    assertThat(soundTable.getRowCount(), is(MEDIA_SOUND_ROW_COUNT));
    ITable streamingTable = actualDataSet.getTable("SC_Gallery_Streaming");
    assertThat(streamingTable.getRowCount(), is(MEDIA_STREAMING_ROW_COUNT));
    ITable pathTable = actualDataSet.getTable("SC_Gallery_Path");
    assertThat(pathTable.getRowCount(), is(MEDIA_PATH_ROW_COUNT));
    ITable orderTable = actualDataSet.getTable("SC_Gallery_Order");
    assertThat(orderTable.getRowCount(), is(MEDIA_ORDER_ROW_COUNT));
    ITable orderDetailTable = actualDataSet.getTable("SC_Gallery_OrderDetail");
    assertThat(orderDetailTable.getRowCount(), is(MEDIA_ORDER_DETAIL_ROW_COUNT));
  }
}
