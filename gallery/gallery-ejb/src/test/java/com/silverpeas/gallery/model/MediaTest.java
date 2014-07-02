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
import com.silverpeas.gallery.constant.MediaResolution;
import com.silverpeas.gallery.constant.MediaType;
import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.control.ejb.MediaServiceFactory;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;

import org.apache.commons.lang.time.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.silverpeas.admin.user.constant.UserAccessLevel;
import org.silverpeas.date.Period;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class MediaTest {

  private GalleryBm mediaServiceMock;
  private ApplicationContext accessControllerProviderSave;
  private UserDetail userForTest = new UserDetail();
  private UserDetail lastUpdaterForTest = new UserDetail();
  private Date createDate = Timestamp.valueOf("2013-02-12 14:56:38.452");
  private Date lastUpdateDate = Timestamp.valueOf("2013-04-02 10:47:10.102");
  private Date beginVisibilityDate = DateUtils.addDays(DateUtil.getNow(), -50);
  private Date endVisibilityDate = DateUtils.addDays(DateUtil.getNow(), 50);

  @SuppressWarnings("unchecked")
  @Before
  public void setup() {
    mediaServiceMock = mock(GalleryBm.class);
    MediaServiceFactory.getInstance().setMediaService(mediaServiceMock);
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
  }

  @After
  public void tearDown() {
    // For all other tests...
    AccessControllerProvider.getInstance().setApplicationContext(accessControllerProviderSave);
    MediaServiceFactory.getInstance().setMediaService(null);
  }

  @Test
  public void justInstancedTest() {
    Media media = new MediaForTest();
    assertThat(media.getMediaPK(), notNullValue());
    assertThat(media.getMediaPK().getId(), nullValue());
    assertThat(media.getMediaPK().getInstanceId(), nullValue());
    assertThat(media.getId(), nullValue());
    assertThat(media.getInstanceId(), nullValue());
    assertThat(media.getType(), is(MediaType.Unknown));
    assertThat(media.getContributionType(), is(MediaType.Unknown.name()));
    assertThat(media.getTitle(), isEmptyString());
    assertThat(media.getName(), isEmptyString());
    assertThat(media.getName("en"), isEmptyString());
    assertThat(media.getDescription(), isEmptyString());
    assertThat(media.getDescription("en"), isEmptyString());
    assertThat(media.getAuthor(), isEmptyString());
    assertThat(media.getKeyWord(), isEmptyString());
    assertThat(media.getVisibilityPeriod(), sameInstance(Period.UNDEFINED));
    assertThat(media.getCreationDate(), nullValue());
    assertThat(media.getSilverCreationDate(), nullValue());
    assertThat(media.getCreator(), nullValue());
    assertThat(media.getCreatorId(), nullValue());
    assertThat(media.getCreatorName(), isEmptyString());
    assertThat(media.getLastUpdateDate(), nullValue());
    assertThat(media.getDate(), nullValue());
    assertThat(media.getLastUpdater(), nullValue());
    assertThat(media.getLastUpdatedBy(), nullValue());
    assertThat(media.getLastUpdaterName(), isEmptyString());
    assertThat(media.canBeAccessedBy(userForTest), is(false));
    assertThat(media.getPermalink(), is("/silverpeas/Media/null"));
    assertThat(media.isPreviewable(), is(true));
    assertThat(media.getSilverpeasContentId(), nullValue());
    assertThat(media.getIconUrl(), nullValue());
    assertThat(media.isDownloadable(), is(true));
    assertThat(media.getURL(), is("searchResult?Type=Media&Id=null"));
    assertThat(media.getLanguages(), nullValue());
    assertThat(media.toString(), is("(pk = (id = null, instanceId = null), name = )"));
  }

  @Test
  public void justCreatedTest() {
    Media media = defaultMedia();
    assertDefaultMedia(media);
  }

  @Test
  public void justUpdatedTest() {
    Media media = defaultMedia();
    media.setLastUpdateDate(lastUpdateDate);
    media.setLastUpdater(lastUpdaterForTest);

    assertThat(media.getCreationDate(), is(createDate));
    assertThat(media.getSilverCreationDate(), is("2013/02/12"));
    assertThat(media.getCreator(), is(userForTest));
    assertThat(media.getCreatorId(), is(userForTest.getId()));
    assertThat(media.getCreatorName(), is(userForTest.getDisplayedName()));
    assertThat(media.getLastUpdateDate(), is(lastUpdateDate));
    assertThat(media.getDate(), is("2013/04/02"));
    assertThat(media.getLastUpdater(), is(lastUpdaterForTest));
    assertThat(media.getLastUpdatedBy(), is(lastUpdaterForTest.getId()));
    assertThat(media.getLastUpdaterName(), is(lastUpdaterForTest.getDisplayedName()));
    assertThat(media.canBeAccessedBy(userForTest), is(false));

    media.setAuthor(null);
    media.setKeyWord(null);
    media.setTitle(null);
    media.setDescription(null);
    assertThat(media.getAuthor(), isEmptyString());
    assertThat(media.getKeyWord(), isEmptyString());
    assertThat(media.getTitle(), isEmptyString());
    assertThat(media.getName(), isEmptyString());
    assertThat(media.getName("en"), isEmptyString());
    assertThat(media.getDescription(), isEmptyString());
    assertThat(media.getDescription("en"), isEmptyString());

    media.setAuthor("   ");
    media.setKeyWord("      ");
    media.setTitle("   ");
    media.setDescription("      ");
    assertThat(media.getAuthor(), isEmptyString());
    assertThat(media.getKeyWord(), isEmptyString());
    assertThat(media.getTitle(), isEmptyString());
    assertThat(media.getName(), isEmptyString());
    assertThat(media.getName("en"), isEmptyString());
    assertThat(media.getDescription(), isEmptyString());
    assertThat(media.getDescription("en"), isEmptyString());
  }

  @Test
  public void canAccessBy() {
    Media media = defaultMedia();

    assertThat(media.getInstanceId(), is("instanceId"));
    assertThat(media.isVisible(DateUtil.getNow()), is(true));
    assertThat(media.canBeAccessedBy(userForTest), is(false));

    media.getMediaPK().setComponentName("instanceIdForTest");

    assertThat(media.getInstanceId(), is("instanceIdForTest"));
    assertThat(media.isVisible(DateUtil.getNow()), is(true));
    assertThat(media.canBeAccessedBy(userForTest), is(true));

    media.setVisibilityPeriod(Period.from(DateUtil.MINIMUM_DATE, beginVisibilityDate));

    assertThat(media.isVisible(DateUtil.getNow()), is(false));
    assertThat(media.canBeAccessedBy(userForTest), is(false));

    userForTest.setAccessLevel(UserAccessLevel.ADMINISTRATOR);

    assertThat(media.isVisible(DateUtil.getNow()), is(false));
    assertThat(media.canBeAccessedBy(userForTest), is(true));
  }

  @Test
  public void isVisible() {
    Media media = defaultMedia();

    media.setVisibilityPeriod(null);

    assertThat(media.isVisible(DateUtil.getNow()), is(true));
    assertThat(media.isVisible(beginVisibilityDate), is(true));
    assertThat(media.isVisible(DateUtils.addMilliseconds(beginVisibilityDate, -1)), is(true));
    assertThat(media.isVisible(endVisibilityDate), is(true));
    assertThat(media.isVisible(DateUtils.addMilliseconds(endVisibilityDate, 1)), is(true));

    media.setVisibilityPeriod(Period.from(beginVisibilityDate, DateUtil.MAXIMUM_DATE));

    assertThat(media.isVisible(DateUtil.getNow()), is(true));
    assertThat(media.isVisible(beginVisibilityDate), is(true));
    assertThat(media.isVisible(DateUtils.addMilliseconds(beginVisibilityDate, -1)), is(false));
    assertThat(media.isVisible(endVisibilityDate), is(true));
    assertThat(media.isVisible(DateUtils.addMilliseconds(endVisibilityDate, 1)), is(true));

    media.setVisibilityPeriod(Period.from(DateUtil.MINIMUM_DATE, endVisibilityDate));

    assertThat(media.isVisible(DateUtil.getNow()), is(true));
    assertThat(media.isVisible(beginVisibilityDate), is(true));
    assertThat(media.isVisible(DateUtils.addMilliseconds(beginVisibilityDate, -1)), is(true));
    assertThat(media.isVisible(endVisibilityDate), is(true));
    assertThat(media.isVisible(DateUtils.addMilliseconds(endVisibilityDate, 1)), is(false));


    media.setVisibilityPeriod(Period.from(beginVisibilityDate, endVisibilityDate));

    assertThat(media.isVisible(DateUtil.getNow()), is(true));
    assertThat(media.isVisible(beginVisibilityDate), is(true));
    assertThat(media.isVisible(DateUtils.addMilliseconds(beginVisibilityDate, -1)), is(false));
    assertThat(media.isVisible(endVisibilityDate), is(true));
    assertThat(media.isVisible(DateUtils.addMilliseconds(endVisibilityDate, 1)), is(false));
  }

  private Media defaultMedia() {
    MediaPK mediaPK = new MediaPK("mediaId", "instanceId");
    Media media = new MediaForTest();
    media.setMediaPK(mediaPK);
    media.setTitle("A title");
    media.setDescription("A description");
    media.setAuthor("An author");
    media.setKeyWord("keywords");
    media.setVisibilityPeriod(Period.from(beginVisibilityDate, endVisibilityDate));
    media.setCreationDate(createDate);
    media.setCreator(userForTest);
    media.setSilverpeasContentId("silverObjectId");
    media.setIconUrl("iconUrl");
    assertDefaultMedia(media);
    return media;
  }

  private void assertDefaultMedia(Media media) {
    assertThat(media.getMediaPK(), notNullValue());
    assertThat(media.getMediaPK().getId(), is("mediaId"));
    assertThat(media.getMediaPK().getInstanceId(), is("instanceId"));
    assertThat(media.getId(), is("mediaId"));
    assertThat(media.getInstanceId(), is("instanceId"));
    assertThat(media.getType(), is(MediaType.Unknown));
    assertThat(media.getContributionType(), is(MediaType.Unknown.name()));
    assertThat(media.getTitle(), is("A title"));
    assertThat(media.getName(), is("A title"));
    assertThat(media.getName("en"), is("A title"));
    assertThat(media.getDescription(), is("A description"));
    assertThat(media.getDescription("en"), is("A description"));
    assertThat(media.getAuthor(), is("An author"));
    assertThat(media.getKeyWord(), is("keywords"));
    assertThat(media.getVisibilityPeriod().getBeginDate(), is(beginVisibilityDate));
    assertThat(media.getVisibilityPeriod().getEndDate(), is(endVisibilityDate));
    assertThat(media.getCreationDate(), is(createDate));
    assertThat(media.getSilverCreationDate(), is("2013/02/12"));
    assertThat(media.getCreator(), is(userForTest));
    assertThat(media.getCreatorId(), is(userForTest.getId()));
    assertThat(media.getCreatorName(), is(userForTest.getDisplayedName()));
    assertThat(media.getLastUpdateDate(), is(createDate));
    assertThat(media.getDate(), is("2013/02/12"));
    assertThat(media.getLastUpdater(), is(userForTest));
    assertThat(media.getLastUpdatedBy(), is(userForTest.getId()));
    assertThat(media.getLastUpdaterName(), is(userForTest.getDisplayedName()));
    assertThat(media.canBeAccessedBy(userForTest), is(false));
    assertThat(media.getPermalink(), is("/silverpeas/Media/mediaId"));
    assertThat(media.isPreviewable(), is(true));
    assertThat(media.getSilverpeasContentId(), is("silverObjectId"));
    assertThat(media.getIconUrl(), is("iconUrl"));
    assertThat(media.isDownloadable(), is(true));
    assertThat(media.getURL(), is("searchResult?Type=Media&Id=mediaId"));
    assertThat(media.getLanguages(), nullValue());
    assertThat(media.toString(),
        is("(pk = (id = mediaId, instanceId = instanceId), name = A title)"));
  }

  @Test
  public void testAddToAlbums() {
    Media media = defaultMedia();
    media.addToAlbums("1", "2");
    verify(mediaServiceMock, times(1)).addMediaToAlbums(media, "1", "2");
  }

  @Test
  public void testSetToAlbums() {
    Media media = defaultMedia();
    media.setToAlbums("1", "2");
    verify(mediaServiceMock, times(1)).removeMediaFromAllAlbums(media);
    verify(mediaServiceMock, times(1)).addMediaToAlbums(media, "1", "2");
  }

  @Test
  public void testRemoveMediaFromAllAlbums() {
    Media media = defaultMedia();
    media.removeFromAllAlbums();
    verify(mediaServiceMock, times(1)).removeMediaFromAllAlbums(media);
  }

  private class MediaForTest extends Media {
    private static final long serialVersionUID = 7114643185671416374L;

    @Override
    public MediaType getType() {
      return MediaType.Unknown;
    }

    @Override
    public String getThumbnailUrl(final MediaResolution mediaResolution) {
      return null;
    }

    @Override
    protected SilverpeasRole getGreatestUserRole(final UserDetail user) {
      return SilverpeasRole.reader;
    }
  }
}