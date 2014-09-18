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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.model;

import com.silverpeas.accesscontrol.AccessController;
import com.silverpeas.accesscontrol.AccessControllerProvider;
import com.silverpeas.gallery.constant.MediaMimeType;
import com.silverpeas.gallery.constant.MediaResolution;
import com.silverpeas.gallery.constant.MediaType;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.silverpeas.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.date.Period;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PhotoDetailTest extends AbstractMediaTest {

  private ApplicationContext accessControllerProviderSave;
  private UserDetail userForTest = new UserDetail();
  private UserDetail lastUpdaterForTest = new UserDetail();
  private Date createDate = Timestamp.valueOf("2013-02-12 14:56:38.452");
  private Date lastUpdateDate = Timestamp.valueOf("2013-04-02 10:47:10.102");
  private Date beginVisibilityDate = DateUtils.addDays(DateUtil.getNow(), -50);
  private Date endVisibilityDate = DateUtils.addDays(DateUtil.getNow(), 50);
  private Date beginDownloadDate = DateUtils.addDays(DateUtil.getNow(), -20);
  private Date endDownloadDate = DateUtils.addDays(DateUtil.getNow(), 20);

  @SuppressWarnings("unchecked")
  @Before
  public void setup() {
    AccessController<String> componentAccessController = mock(AccessController.class);
    when(componentAccessController.isUserAuthorized("userIdAccessTest", "instanceIdForTest"))
        .thenReturn(true);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);
    when(applicationContextMock.getBean("componentAccessController"))
        .thenReturn(componentAccessController);
    accessControllerProviderSave = (ApplicationContext) ReflectionTestUtils
        .getField(AccessControllerProvider.getInstance(), "context");
    AccessControllerProvider.getInstance().setApplicationContext(applicationContextMock);
    ReflectionTestUtils
        .setField(AccessControllerProvider.getInstance(), "context", applicationContextMock);

    // A user for tests
    userForTest.setId("userIdAccessTest");
    userForTest.setLastName("LastName");
    userForTest.setFirstName("FirstName");
    userForTest.setAccessLevel(UserAccessLevel.USER);
    // A last updater for tests
    lastUpdaterForTest.setId("lastUpdaterId");
    lastUpdaterForTest.setLastName("Updater");
    lastUpdaterForTest.setFirstName("Last");
    lastUpdaterForTest.setAccessLevel(UserAccessLevel.USER);

    OrganisationController organisationControllerMock = mock(OrganisationController.class);
    when(organisationControllerMock.getUserDetail(userForTest.getId())).thenReturn(userForTest);
    when(organisationControllerMock.getUserDetail(lastUpdaterForTest.getId()))
        .thenReturn(lastUpdaterForTest);
    ReflectionTestUtils
        .setField(OrganisationControllerFactory.getFactory(), "organisationController",
            organisationControllerMock);
  }

  @After
  public void tearDown() {
    // For all other tests...
    AccessControllerProvider.getInstance().setApplicationContext(accessControllerProviderSave);
  }

  @Test
  public void verifyPhotoWrapping() {
    PhotoDetail photoDetail = new PhotoDetail();

    MediaPK mediaPK = new MediaPK("mediaId", "instanceId");
    photoDetail.setMediaPK(mediaPK);
    photoDetail.setKeyWord("keywords");
    photoDetail.setSilverObjectId("silverObjectId");
    photoDetail.setIconUrl("iconUrl");
    photoDetail.setBeginDate(beginVisibilityDate);
    photoDetail.setEndDate(endVisibilityDate);
    photoDetail.setAuthor("An author");
    photoDetail.setCreationDate(createDate);
    photoDetail.setCreatorId(userForTest.getId());
    photoDetail.setUpdateDate(lastUpdateDate);
    photoDetail.setUpdateId(lastUpdaterForTest.getId());
    photoDetail.setDescription("A description");
    photoDetail.setSizeL(800);
    photoDetail.setSizeH(600);
    photoDetail.addMetaData(new MetaData("ok").setProperty("metadata"));
    photoDetail.setDownload(true);
    photoDetail.setTitle("A title");
    photoDetail.setImageMimeType(MediaMimeType.JPG);
    photoDetail.setImageSize(1024);
    photoDetail.setBeginDownloadDate(beginDownloadDate);
    photoDetail.setEndDownloadDate(endDownloadDate);

    Photo wrappedPhoto = (Photo) ReflectionTestUtils.getField(photoDetail, "photo");
    assertThat(wrappedPhoto, sameInstance(photoDetail.getPhoto()));

    assertThat(wrappedPhoto.getMediaPK(), notNullValue());
    assertThat(wrappedPhoto.getMediaPK().getId(), is("mediaId"));
    assertThat(wrappedPhoto.getMediaPK().getInstanceId(), is("instanceId"));
    assertThat(wrappedPhoto.getId(), is("mediaId"));
    assertThat(wrappedPhoto.getInstanceId(), is("instanceId"));
    assertThat(wrappedPhoto.getContributionType(), is(MediaType.Photo.name()));
    assertThat(wrappedPhoto.getTitle(), is("A title"));
    assertThat(wrappedPhoto.getName(), is("A title"));
    assertThat(wrappedPhoto.getName("en"), is("A title"));
    assertThat(wrappedPhoto.getDescription(), is("A description"));
    assertThat(wrappedPhoto.getDescription("en"), is("A description"));
    assertThat(wrappedPhoto.getAuthor(), is("An author"));
    assertThat(wrappedPhoto.getKeyWord(), is("keywords"));
    assertThat(wrappedPhoto.getVisibilityPeriod().getBeginDate(), is(beginVisibilityDate));
    assertThat(wrappedPhoto.getVisibilityPeriod().getEndDate(), is(endVisibilityDate));
    assertThat(wrappedPhoto.getCreationDate(), is(createDate));
    assertThat(wrappedPhoto.getSilverCreationDate(), is("2013/02/12"));
    assertThat(wrappedPhoto.getCreator(), is(userForTest));
    assertThat(wrappedPhoto.getCreatorId(), is(userForTest.getId()));
    assertThat(wrappedPhoto.getCreatorName(), is(userForTest.getDisplayedName()));
    assertThat(wrappedPhoto.getLastUpdateDate(), is(lastUpdateDate));
    assertThat(wrappedPhoto.getDate(), is("2013/04/02"));
    assertThat(wrappedPhoto.getLastUpdater(), is(lastUpdaterForTest));
    assertThat(wrappedPhoto.getLastUpdatedBy(), is(lastUpdaterForTest.getId()));
    assertThat(wrappedPhoto.getLastUpdaterName(), is(lastUpdaterForTest.getDisplayedName()));
    assertThat(wrappedPhoto.canBeAccessedBy(userForTest), is(false));
    assertThat(wrappedPhoto.getPermalink(), is("/silverpeas/Media/mediaId"));
    assertThat(wrappedPhoto.isPreviewable(), is(false));
    assertThat(wrappedPhoto.getSilverpeasContentId(), is("silverObjectId"));
    assertThat(wrappedPhoto.getIconUrl(), is("iconUrl"));
    assertThat(wrappedPhoto.isDownloadable(), is(true));
    assertThat(wrappedPhoto.getURL(), is("searchResult?Type=Photo&Id=mediaId"));
    assertThat(wrappedPhoto.getLanguages(), nullValue());
    assertThat(wrappedPhoto.toString(),
        is("(pk = (id = mediaId, instanceId = instanceId), name = A title)"));
    assertThat(wrappedPhoto.getFileName(), nullValue());
    assertThat(wrappedPhoto.getFileSize(), is(1024L));
    assertThat(wrappedPhoto.getFileMimeType(), is(MediaMimeType.JPG));
    assertThat(wrappedPhoto.isDownloadAuthorized(), is(true));
    assertThat(wrappedPhoto.getDownloadPeriod(), not(sameInstance(Period.UNDEFINED)));
    assertThat(wrappedPhoto.getDownloadPeriod().getBeginDatable(),
        comparesEqualTo(beginDownloadDate));
    assertThat(wrappedPhoto.getDownloadPeriod().getEndDatable(), comparesEqualTo(endDownloadDate));
    assertThat(wrappedPhoto.getType(), is(MediaType.Photo));
    assertThat(wrappedPhoto.getDefinition().getWidth(), is(800));
    assertThat(wrappedPhoto.getDefinition().getHeight(), is(600));
    assertThat(wrappedPhoto.getMetaDataProperties(), hasSize(1));
    assertThat(
        wrappedPhoto.getMetaData(wrappedPhoto.getMetaDataProperties().iterator().next()).getValue(),
        is("ok"));
    assertThat(wrappedPhoto.getApplicationThumbnailUrl(MediaResolution.TINY),
        is("/silverpeas/gallery/jsp/icons/notAvailable_fr" +
            MediaResolution.TINY.getThumbnailSuffix()));

    // Previewable
    photoDetail.setImageName("image.jpg");
    assertThat(wrappedPhoto.isPreviewable(), is(true));
    assertThat(wrappedPhoto.getApplicationThumbnailUrl(MediaResolution.TINY),
        is(GALLERY_REST_WEB_SERVICE_BASE_URI +
            "photos/mediaId/content?_t=1364892430102&resolution=TINY"));
  }

  @Test
  public void checkPhotoPeriodInit() {
    PhotoDetail photo = new PhotoDetail();
    photo.setBeginDownloadDate(null);
    assertThat(photo.getBeginDownloadDate(), nullValue());
    photo.setEndDownloadDate(null);
    assertThat(photo.getEndDownloadDate(), nullValue());
    photo.setBeginDate(null);
    assertThat(photo.getBeginDate(), nullValue());
    photo.setEndDate(null);
    assertThat(photo.getEndDate(), nullValue());
  }

}