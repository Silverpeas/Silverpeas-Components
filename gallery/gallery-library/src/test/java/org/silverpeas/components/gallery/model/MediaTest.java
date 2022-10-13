/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.model;

import org.apache.commons.lang3.time.DateUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.components.gallery.constant.MediaResolution;
import org.silverpeas.components.gallery.constant.MediaType;
import org.silverpeas.components.gallery.notification.AlbumMediaEventNotifier;
import org.silverpeas.components.gallery.service.GalleryService;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.io.file.SilverpeasFile;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedBeans;
import org.silverpeas.core.test.extention.TestManagedMock;
import org.silverpeas.core.util.DateUtil;

import java.sql.Timestamp;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv
@TestManagedBeans(AlbumMediaEventNotifier.class)
class MediaTest {
  private final UserDetail userForTest = new UserDetail();
  private final UserDetail lastUpdaterForTest = new UserDetail();
  private final Date createDate = Timestamp.valueOf("2013-02-12 14:56:38.452");
  private final Date lastUpdateDate = Timestamp.valueOf("2013-04-02 10:47:10.102");
  private final Date beginVisibilityDate = DateUtils.addDays(DateUtil.getNow(), -50);
  private final Date endVisibilityDate = DateUtils.addDays(DateUtil.getNow(), 50);

  @BeforeEach
  public void setup(@TestManagedMock final ComponentAccessControl componentAccessController) {
    when(componentAccessController.isUserAuthorized("userIdAccessTest", "instanceIdForTest"))
        .thenReturn(true);

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

  @Test
  void justInstantiatedTest() {
    Media media = new MediaForTest();
    assertThat(media.getMediaPK(), notNullValue());
    assertThat(media.getMediaPK().getId(), nullValue());
    assertThat(media.getMediaPK().getInstanceId(), nullValue());
    assertThat(media.getId(), nullValue());
    assertThat(media.getInstanceId(), nullValue());
    MatcherAssert.assertThat(media.getType(), Matchers.is(MediaType.Photo));
    assertThat(media.getContributionType(), is(MediaType.Photo.name()));
    assertThat(media.getTitle(), is(emptyString()));
    assertThat(media.getName(), is(emptyString()));
    assertThat(media.getName("en"), is(emptyString()));
    assertThat(media.getDescription(), is(emptyString()));
    assertThat(media.getDescription("en"), is(emptyString()));
    assertThat(media.getAuthor(), is(emptyString()));
    assertThat(media.getKeyWord(), is(emptyString()));
    assertThat(media.getVisibilityPeriod(), sameInstance(Period.UNDEFINED));
    assertThat(media.getCreationDate(), nullValue());
    assertThat(media.getSilverCreationDate(), nullValue());
    assertThat(media.getCreator(), nullValue());
    assertThat(media.getCreatorId(), nullValue());
    assertThat(media.getCreatorName(), is(emptyString()));
    assertThat(media.getLastUpdateDate(), nullValue());
    assertThat(media.getDate(), nullValue());
    assertThat(media.getLastUpdater(), nullValue());
    assertThat(media.getLastUpdatedBy(), nullValue());
    assertThat(media.getLastUpdaterName(), is(emptyString()));
    assertThat(media.canBeAccessedBy(userForTest), is(false));
    assertThat(media.getPermalink(), is("/silverpeas/Media/null"));
    assertThat(media.isPreviewable(), is(true));
    assertThat(media.getSilverpeasContentId(), nullValue());
    assertThat(media.getIconUrl(), nullValue());
    assertThat(media.isDownloadable(), is(true));
    assertThat(media.getURL(), is("searchResult?Type=Photo&Id=null"));
    assertThat(media.getLanguages(), hasSize(0));
    assertThat(media.toString(), is("(pk = (id = null, instanceId = null), name = )"));
  }

  @Test
  void justCreatedTest() {
    Media media = defaultMedia();
    assertDefaultMedia(media);
  }

  @Test
  void justUpdatedTest() {
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
    assertThat(media.getAuthor(), is(emptyString()));
    assertThat(media.getKeyWord(), is(emptyString()));
    assertThat(media.getTitle(), is(emptyString()));
    assertThat(media.getName(), is(emptyString()));
    assertThat(media.getName("en"), is(emptyString()));
    assertThat(media.getDescription(), is(emptyString()));
    assertThat(media.getDescription("en"), is(emptyString()));

    media.setAuthor("   ");
    media.setKeyWord("      ");
    media.setTitle("   ");
    media.setDescription("      ");
    assertThat(media.getAuthor(), is(emptyString()));
    assertThat(media.getKeyWord(), is(emptyString()));
    assertThat(media.getTitle(), is(emptyString()));
    assertThat(media.getName(), is(emptyString()));
    assertThat(media.getName("en"), is(emptyString()));
    assertThat(media.getDescription(), is(emptyString()));
    assertThat(media.getDescription("en"), is(emptyString()));
  }

  @Test
  void canAccessBy() {
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
  void isVisible() {
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
    MatcherAssert.assertThat(media.getType(), is(MediaType.Photo));
    assertThat(media.getContributionType(), is(MediaType.Photo.name()));
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
    assertThat(media.getURL(), is("searchResult?Type=Photo&Id=mediaId"));
    assertThat(media.getLanguages(), hasSize(0));
    assertThat(media.toString(),
        is("(pk = (id = mediaId, instanceId = instanceId), name = A title)"));
  }

  @Test
  void testAddToAlbums(@TestManagedMock GalleryService mediaServiceMock) {
    Media media = defaultMedia();
    media.addToAlbums("1", "2");
    verify(mediaServiceMock, times(1)).addMediaToAlbums(media, "1", "2");
  }

  @Test
  void testSetToAlbums(@TestManagedMock GalleryService mediaServiceMock) {
    Media media = defaultMedia();
    media.setToAlbums("1", "2");
    verify(mediaServiceMock, times(1)).removeMediaFromAllAlbums(media);
    verify(mediaServiceMock, times(1)).addMediaToAlbums(media, "1", "2");
  }

  @Test
  void testRemoveMediaFromAllAlbums(@TestManagedMock GalleryService mediaServiceMock) {
    Media media = defaultMedia();
    media.removeFromAllAlbums();
    verify(mediaServiceMock, times(1)).removeMediaFromAllAlbums(media);
  }

  private static class MediaForTest extends Media {
    private static final long serialVersionUID = 7114643185671416374L;

    public MediaForTest() {
      super();
    }

    protected MediaForTest(final Media other) {
      super(other);
    }

    @Override
    public MediaType getType() {
      return MediaType.Photo;
    }

    @Override
    public String getApplicationThumbnailUrl(final MediaResolution mediaResolution) {
      return null;
    }

    @Override
    protected SilverpeasRole getHighestUserRole(final User user) {
      return SilverpeasRole.READER;
    }

    @Override
    public SilverpeasFile getFile(final MediaResolution mediaResolution, final String size) {
      return null;
    }

    @Override
    public Media getCopy() {
      return new MediaForTest(this);
    }
  }
}