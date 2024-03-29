/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.components.gallery.dao;

import org.apache.commons.lang3.time.DateUtils;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.silverpeas.components.gallery.BaseGalleryIT;
import org.silverpeas.components.gallery.constant.MediaMimeType;
import org.silverpeas.components.gallery.constant.MediaType;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.MediaCriteria;
import org.silverpeas.components.gallery.model.Photo;
import org.silverpeas.components.gallery.model.Sound;
import org.silverpeas.components.gallery.model.Streaming;
import org.silverpeas.components.gallery.model.Video;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.cache.service.SessionCacheAccessor;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.io.media.Definition;
import org.silverpeas.core.media.streaming.StreamingProvider;
import org.silverpeas.core.media.streaming.StreamingProvidersRegistry;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.test.integration.rule.DbSetupRule.TableRow;
import org.silverpeas.core.util.DateUtil;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.components.gallery.model.MediaCriteria.QUERY_ORDER_BY.*;
import static org.silverpeas.components.gallery.model.MediaCriteria.VISIBILITY.*;
import static org.silverpeas.core.test.integration.rule.DbSetupRule.getActualDataSet;
import static org.silverpeas.core.test.integration.rule.DbSetupRule.getTableRowFor;
import static org.silverpeas.core.test.util.TestRuntime.awaitUntil;

/**
 * This class of unit tests has been written during Entity and SGBD model migration.
 */
public class MediaDaoIT extends BaseGalleryIT {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void getAllMedia() throws Exception {
    List<Media> media = MediaDAO.findByCriteria(defaultMediaCriteria());
    assertThat(media, hasSize(8));
    assertMediaIdentifiers(media, false, "1", "2", "v_1", "v_2", "s_1", "s_2", "stream_1",
        "stream_2");
  }

  @Test
  public void countAllMedia() throws Exception {
    long nbMedia = MediaDAO.countByCriteria(defaultMediaCriteria());
    assertThat(nbMedia, is(8L));
  }

  @Test
  public void getAllMediaOfAlbum() throws Exception {
    List<Media> media = MediaDAO.findByCriteria(defaultMediaCriteria().albumIdentifierIsOneOf("1"));
    assertMediaIdentifiers(media, false, "1", "v_1", "v_2", "s_1", "s_2", "stream_2");

    media = MediaDAO.findByCriteria(defaultMediaCriteria().albumIdentifierIsOneOf("2"));
    assertMediaIdentifiers(media, false, "2", "v_1", "stream_1");

    // Album that does not exist
    media = MediaDAO.findByCriteria(defaultMediaCriteria().albumIdentifierIsOneOf("999"));
    assertThat(media, hasSize(0));

    // Several albums
    media = MediaDAO
        .findByCriteria(defaultMediaCriteria().albumIdentifierIsOneOf("1", "999", "2", "89"));
    assertMediaIdentifiers(media, false, "1", "v_2", "s_1", "s_2", "stream_2", "2", "v_1",
        "stream_1");

    // Removing the identifier of component instance
    media = MediaDAO.findByCriteria(
        defaultMediaCriteria().albumIdentifierIsOneOf("1").onComponentInstanceId(null));
    assertMediaIdentifiers(media, false, "1", "v_1", "v_2", "s_1", "s_2", "stream_2");

    media = MediaDAO.findByCriteria(
        defaultMediaCriteria().albumIdentifierIsOneOf("2").onComponentInstanceId(null));
    assertMediaIdentifiers(media, false, "2", "v_1", "stream_1");
  }

  @Test
  public void countAllMediaOfAlbum() throws Exception {
    long nbMedia = MediaDAO.countByCriteria(defaultMediaCriteria().albumIdentifierIsOneOf("1"));
    assertThat(nbMedia, is(6L));

    nbMedia = MediaDAO.countByCriteria(defaultMediaCriteria().albumIdentifierIsOneOf("2"));
    assertThat(nbMedia, is(3L));

    // Album that does not exist
    nbMedia = MediaDAO.countByCriteria(defaultMediaCriteria().albumIdentifierIsOneOf("999"));
    assertThat(nbMedia, is(0L));

    // Several albums
    nbMedia = MediaDAO
        .countByCriteria(defaultMediaCriteria().albumIdentifierIsOneOf("1", "999", "2", "89"));
    assertThat(nbMedia, is(8L));

    // Removing the identifier of component instance
    nbMedia = MediaDAO.countByCriteria(
        defaultMediaCriteria().albumIdentifierIsOneOf("1").onComponentInstanceId(null));
    assertThat(nbMedia, is(6L));

    nbMedia = MediaDAO.countByCriteria(
        defaultMediaCriteria().albumIdentifierIsOneOf("2").onComponentInstanceId(null));
    assertThat(nbMedia, is(3L));
  }

  @Test
  public void getAllPhotos() throws Exception {
    List<Media> media =
        MediaDAO.findByCriteria(defaultMediaCriteria().mediaTypeIsOneOf(MediaType.Photo));
    assertThat(media, hasSize(2));
    assertMediaType(media, MediaType.Photo, Photo.class);
  }

  @Test
  public void countAllPhotos() throws Exception {
    long nbMedia =
        MediaDAO.countByCriteria(defaultMediaCriteria().mediaTypeIsOneOf(MediaType.Photo));
    assertThat(nbMedia, is(2L));
  }

  @Test
  public void getAllVideos() throws Exception {
    List<Media> media =
        MediaDAO.findByCriteria(defaultMediaCriteria().mediaTypeIsOneOf(MediaType.Video));
    assertThat(media, hasSize(2));
    assertMediaType(media, MediaType.Video, Video.class);
  }

  @Test
  public void countAllVideos() throws Exception {
    long nbMedia =
        MediaDAO.countByCriteria(defaultMediaCriteria().mediaTypeIsOneOf(MediaType.Video));
    assertThat(nbMedia, is(2L));
  }

  @Test
  public void getAllPhotosAndVideos() throws Exception {
    List<Media> media = MediaDAO
        .findByCriteria(defaultMediaCriteria().mediaTypeIsOneOf(MediaType.Photo, MediaType.Video));
    assertThat(media, hasSize(4));

    media = MediaDAO.findByCriteria(
        defaultMediaCriteria().mediaTypeIsOneOf(MediaType.Photo, MediaType.Video).limitResultTo(2));
    assertThat(media, hasSize(2));
  }

  @Test
  public void countAllPhotosAndVideos() throws Exception {
    long nbMedia = MediaDAO
        .countByCriteria(defaultMediaCriteria().mediaTypeIsOneOf(MediaType.Photo, MediaType.Video));
    assertThat(nbMedia, is(4L));
  }

  @Test
  public void getAllSounds() throws Exception {
    List<Media> media =
        MediaDAO.findByCriteria(defaultMediaCriteria().mediaTypeIsOneOf(MediaType.Sound));
    assertThat(media, hasSize(2));
    assertMediaType(media, MediaType.Sound, Sound.class);
  }

  @Test
  public void countAllSounds() throws Exception {
    long nbMedia =
        MediaDAO.countByCriteria(defaultMediaCriteria().mediaTypeIsOneOf(MediaType.Sound));
    assertThat(nbMedia, is(2L));
  }

  @Test
  public void getAllStreamings() throws Exception {
    List<Media> media =
        MediaDAO.findByCriteria(defaultMediaCriteria().mediaTypeIsOneOf(MediaType.Streaming));
    assertThat(media, hasSize(2));
    assertMediaType(media, MediaType.Streaming, Streaming.class);
  }

  @Test
  public void countAllStreamings() throws Exception {
    long nbMedia =
        MediaDAO.countByCriteria(defaultMediaCriteria().mediaTypeIsOneOf(MediaType.Streaming));
    assertThat(nbMedia, is(2L));
  }

  @Test
  public void getByCriteria() throws Exception {
    Media media = MediaDAO.getByCriteria(defaultMediaCriteria().identifierIsOneOf("v_2"));
    assertThat(media, instanceOf(Video.class));
    assertThat(media.getId(), is("v_2"));

    // Media that does not exist
    media = MediaDAO.getByCriteria(defaultMediaCriteria().identifierIsOneOf("v_989898"));
    assertThat(media, nullValue());
  }

  @Test(expected = IllegalArgumentException.class)
  public void getByCriteriaError() throws Exception {
    MediaDAO.getByCriteria(defaultMediaCriteria());
  }

  @Test
  public void getByIdentifiers() throws Exception {
    List<Media> media = MediaDAO.findByCriteria(defaultMediaCriteria().identifierIsOneOf("v_2"));
    assertMediaIdentifiers(media, false, "v_2");

    media = MediaDAO
        .findByCriteria(defaultMediaCriteria().identifierIsOneOf("v_2", "v_26", "stream_9", "2"));
    assertMediaIdentifiers(media, false, "v_2", "2");

    // Removing the identifier of component instance
    media = MediaDAO.findByCriteria(
        defaultMediaCriteria().identifierIsOneOf("v_2", "v_26", "stream_9", "2")
            .onComponentInstanceId(null));
    assertMediaIdentifiers(media, false, "v_2", "stream_9", "2");
  }

  @Test
  public void getStreamingsAccordingToRequesterAndVisibility() throws Exception {
    List<Media> media = MediaDAO
        .findByCriteria(mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Streaming));
    assertThat(media, hasSize(1));
    assertMediaType(media, MediaType.Streaming, Streaming.class);
    assertThat(media.get(0).getId(), is("stream_2"));

    media = MediaDAO.findByCriteria(
        mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Streaming)
            .setRequester(adminAccessUser));
    assertThat(media, hasSize(2));
    assertMediaType(media, MediaType.Streaming, Streaming.class);

    media = MediaDAO.findByCriteria(
        mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Streaming)
            .setRequester(publisherUser));
    assertThat(media, hasSize(2));
    assertMediaType(media, MediaType.Streaming, Streaming.class);

    media = MediaDAO.findByCriteria(
        mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Streaming)
            .setRequester(writerUser));
    assertThat(media, hasSize(1));
    assertMediaType(media, MediaType.Streaming, Streaming.class);
    assertThat(media.get(0).getId(), is("stream_2"));

    // Simulating a connected publisher user
    ((SessionCacheAccessor) CacheAccessorProvider.getSessionCacheAccessor())
        .newSessionCache(publisherUser);

    media = MediaDAO
        .findByCriteria(mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Streaming));
    assertThat(media, hasSize(2));
    assertMediaType(media, MediaType.Streaming, Streaming.class);

    // Simulating a connected writer user
    ((SessionCacheAccessor) CacheAccessorProvider.getSessionCacheAccessor())
        .newSessionCache(writerUser);

    media = MediaDAO
        .findByCriteria(mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Streaming));
    assertThat(media, hasSize(1));
    assertMediaType(media, MediaType.Streaming, Streaming.class);
    assertThat(media.get(0).getId(), is("stream_2"));

    media = MediaDAO.findByCriteria(
        mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Streaming)
            .withVisibility(FORCE_GET_ALL));
    assertThat(media, hasSize(2));
    assertMediaType(media, MediaType.Streaming, Streaming.class);

    media = MediaDAO.findByCriteria(
        mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Streaming)
            .withVisibility(HIDDEN_ONLY));
    assertThat(media, hasSize(1));
    assertMediaType(media, MediaType.Streaming, Streaming.class);
    assertThat(media.get(0).getId(), is("stream_1"));

    media = MediaDAO.findByCriteria(
        mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Streaming)
            .withVisibility(VISIBLE_ONLY));
    assertThat(media, hasSize(1));
    assertMediaType(media, MediaType.Streaming, Streaming.class);
    assertThat(media.get(0).getId(), is("stream_2"));

    // Simulating a connected publisher user
    ((SessionCacheAccessor) CacheAccessorProvider.getSessionCacheAccessor())
        .newSessionCache(publisherUser);

    media = MediaDAO.findByCriteria(
        mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Streaming)
            .withVisibility(HIDDEN_ONLY));
    assertThat(media, hasSize(1));
    assertMediaType(media, MediaType.Streaming, Streaming.class);
    assertThat(media.get(0).getId(), is("stream_1"));

    media = MediaDAO.findByCriteria(
        mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Streaming)
            .withVisibility(VISIBLE_ONLY));
    assertThat(media, hasSize(1));
    assertMediaType(media, MediaType.Streaming, Streaming.class);
    assertThat(media.get(0).getId(), is("stream_2"));
  }

  @Test
  public void countStreamingsAccordingToRequesterAndVisibility() throws Exception {
    long nbMedia = MediaDAO
        .countByCriteria(mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Streaming));
    assertThat(nbMedia, is(1L));

    nbMedia = MediaDAO.countByCriteria(
        mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Streaming)
            .setRequester(adminAccessUser));
    assertThat(nbMedia, is(2L));

    nbMedia = MediaDAO.countByCriteria(
        mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Streaming)
            .setRequester(publisherUser));
    assertThat(nbMedia, is(2L));

    nbMedia = MediaDAO.countByCriteria(
        mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Streaming)
            .setRequester(writerUser));
    assertThat(nbMedia, is(1L));

    // Simulating a connected publisher user
    ((SessionCacheAccessor) CacheAccessorProvider.getSessionCacheAccessor())
        .newSessionCache(publisherUser);

    nbMedia = MediaDAO
        .countByCriteria(mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Streaming));
    assertThat(nbMedia, is(2L));

    // Simulating a connected writer user
    ((SessionCacheAccessor) CacheAccessorProvider.getSessionCacheAccessor())
        .newSessionCache(writerUser);

    nbMedia = MediaDAO
        .countByCriteria(mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Streaming));
    assertThat(nbMedia, is(1L));

    nbMedia = MediaDAO.countByCriteria(
        mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Streaming)
            .withVisibility(FORCE_GET_ALL));
    assertThat(nbMedia, is(2L));

    nbMedia = MediaDAO.countByCriteria(
        mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Streaming)
            .withVisibility(HIDDEN_ONLY));
    assertThat(nbMedia, is(1L));

    nbMedia = MediaDAO.countByCriteria(
        mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Streaming)
            .withVisibility(VISIBLE_ONLY));
    assertThat(nbMedia, is(1L));

    // Simulating a connected publisher user
    ((SessionCacheAccessor) CacheAccessorProvider.getSessionCacheAccessor())
        .newSessionCache(publisherUser);

    nbMedia = MediaDAO.countByCriteria(
        mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Streaming)
            .withVisibility(HIDDEN_ONLY));
    assertThat(nbMedia, is(1L));

    nbMedia = MediaDAO.countByCriteria(
        mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Streaming)
            .withVisibility(VISIBLE_ONLY));
    assertThat(nbMedia, is(1L));
  }

  @Test
  public void getSoundsAccordingToRequesterAndVisibility() throws Exception {
    List<Media> media = MediaDAO
        .findByCriteria(mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Sound));
    assertThat(media, hasSize(1));
    assertMediaType(media, MediaType.Sound, Sound.class);
    assertThat(media.get(0).getId(), is("s_2"));

    media = MediaDAO.findByCriteria(
        mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Sound)
            .setRequester(adminAccessUser));
    assertThat(media, hasSize(2));
    assertMediaType(media, MediaType.Sound, Sound.class);

    media = MediaDAO.findByCriteria(
        mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Sound)
            .setRequester(publisherUser));
    assertThat(media, hasSize(2));
    assertMediaType(media, MediaType.Sound, Sound.class);

    media = MediaDAO.findByCriteria(
        mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Sound)
            .setRequester(writerUser));
    assertThat(media, hasSize(2));
    assertMediaType(media, MediaType.Sound, Sound.class);

    media = MediaDAO.findByCriteria(
        mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Sound)
            .setRequester(userUser));
    assertThat(media, hasSize(1));
    assertMediaType(media, MediaType.Sound, Sound.class);
    assertThat(media.get(0).getId(), is("s_2"));
  }

  @Test
  public void countSoundsAccordingToRequesterAndVisibility() throws Exception {
    long nbMedia = MediaDAO
        .countByCriteria(mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Sound));
    assertThat(nbMedia, is(1L));

    nbMedia = MediaDAO.countByCriteria(
        mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Sound)
            .setRequester(adminAccessUser));
    assertThat(nbMedia, is(2L));

    nbMedia = MediaDAO.countByCriteria(
        mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Sound)
            .setRequester(publisherUser));
    assertThat(nbMedia, is(2L));

    nbMedia = MediaDAO.countByCriteria(
        mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Sound)
            .setRequester(writerUser));
    assertThat(nbMedia, is(2L));

    nbMedia = MediaDAO.countByCriteria(
        mediaCriteriaFutureReferenceDate().mediaTypeIsOneOf(MediaType.Sound)
            .setRequester(userUser));
    assertThat(nbMedia, is(1L));
  }

  @Test
  public void getMediaThatWillBeNotVisible() throws Exception {
    Date today = Timestamp.valueOf("2014-03-30 11:33:45.854");
    List<Media> media = MediaDAO.findByCriteria(
        MediaCriteria.fromNbDaysBeforeThatMediaIsNotVisible(0).referenceDateOf(today));
    assertThat(media, hasSize(0));

    media = MediaDAO.findByCriteria(
        MediaCriteria.fromNbDaysBeforeThatMediaIsNotVisible(1).referenceDateOf(today));
    assertThat(media, hasSize(1));

    media = MediaDAO.findByCriteria(
        MediaCriteria.fromNbDaysBeforeThatMediaIsNotVisible(2).referenceDateOf(today));
    assertThat(media, hasSize(0));
  }

  @Test
  public void countMediaThatWillBeNotVisible() throws Exception {
    Date today = Timestamp.valueOf("2014-03-30 11:33:45.854");
    long nbMedia = MediaDAO.countByCriteria(
        MediaCriteria.fromNbDaysBeforeThatMediaIsNotVisible(0).referenceDateOf(today));
    assertThat(nbMedia, is(0L));

    nbMedia = MediaDAO.countByCriteria(
        MediaCriteria.fromNbDaysBeforeThatMediaIsNotVisible(1).referenceDateOf(today));
    assertThat(nbMedia, is(1L));

    nbMedia = MediaDAO.countByCriteria(
        MediaCriteria.fromNbDaysBeforeThatMediaIsNotVisible(2).referenceDateOf(today));
    assertThat(nbMedia, is(0L));
  }

  @Test
  public void getPathList() throws Exception {
    String mediaIdToPerform = "v_2";
    Media media = new Photo();
    media.setId(mediaIdToPerform);
    media.setComponentInstanceId(INSTANCE_A);

    Collection<String> pathList = MediaDAO.getAlbumIdsOf(media);
    assertThat(pathList, contains("1"));

    media.setId("v_1");

    pathList = MediaDAO.getAlbumIdsOf(media);
    assertThat(pathList, containsInAnyOrder("1", "2"));
  }

  @Test
  public void getAllMediaIdByUserId() throws Exception {
    Date beginDate = DateUtils.addDays(CREATE_DATE, +1);
    Date endDate = DateUtils.addDays(CREATE_DATE, +2);
    List<SocialInformation> socialInformationList =
        MediaDAO.getAllMediaIdByUserId(writerUser.getId(), Period.from(beginDate, endDate));
    assertThat(socialInformationList, hasSize(0));

    beginDate = DateUtils.addDays(CREATE_DATE, 0);
    endDate = DateUtils.addDays(CREATE_DATE, +2);
    socialInformationList =
        MediaDAO.getAllMediaIdByUserId(writerUser.getId(), Period.from(beginDate, endDate));
    assertThat(socialInformationList, hasSize(1));

    beginDate = DateUtils.addDays(LAST_UPDATE_DATE, -2);
    endDate = DateUtils.addDays(LAST_UPDATE_DATE, +2);
    socialInformationList =
        MediaDAO.getAllMediaIdByUserId(writerUser.getId(), Period.from(beginDate, endDate));
    assertThat(socialInformationList, hasSize(1));
  }

  @Test
  public void getSocialInformationListOfMyContacts() throws Exception {
    Date beginDate = DateUtils.addDays(CREATE_DATE, +1);
    Date endDate = DateUtils.addDays(CREATE_DATE, +2);
    List<SocialInformation> socialInformationList = MediaDAO.getSocialInformationListOfMyContacts(
        Arrays.asList(writerUser.getId(), adminAccessUser.getId(), publisherUser.getId()),
        Arrays.asList(INSTANCE_A, "otherInstanceId"), Period.from(beginDate, endDate));
    assertThat(socialInformationList, hasSize(3));

    beginDate = DateUtils.addDays(CREATE_DATE, 0);
    endDate = DateUtils.addDays(CREATE_DATE, +2);
    socialInformationList = MediaDAO.getSocialInformationListOfMyContacts(
        Arrays.asList(writerUser.getId(), adminAccessUser.getId(), publisherUser.getId()),
        Arrays.asList(INSTANCE_A, "otherInstanceId"), Period.from(beginDate, endDate));
    assertThat(socialInformationList, hasSize(7));

    beginDate = DateUtils.addDays(LAST_UPDATE_DATE, -2);
    endDate = DateUtils.addDays(LAST_UPDATE_DATE, +2);
    socialInformationList = MediaDAO.getSocialInformationListOfMyContacts(
        Arrays.asList(writerUser.getId(), adminAccessUser.getId(), publisherUser.getId()),
        Arrays.asList(INSTANCE_A, "otherInstanceId"), Period.from(beginDate, endDate));
    assertThat(socialInformationList, hasSize(7));
  }

  @Test
  public void verifyPhotoData() throws Exception {
    Photo photo = MediaDAO.getByCriteria(defaultMediaCriteria().identifierIsOneOf("1")).getPhoto();
    assertThat(photo, notNullValue());
    assertThat(photo.getMediaPK(), notNullValue());
    assertThat(photo.getId(), is("1"));
    assertThat(photo.getInstanceId(), is(INSTANCE_A));
    assertThat(photo.getType(), is(MediaType.Photo));
    assertThat(photo.getTitle(), is("title 1"));
    assertThat(photo.getDescription(), is("a description 1"));
    assertThat(photo.getAuthor(), is("an author 1"));
    assertThat(photo.getKeyWord(), is("keywords 1"));
    assertThat(photo.getVisibilityPeriod().getBeginDate().getTime(), is(1388530800000L));
    assertThat(photo.getVisibilityPeriod().getEndDate().getTime(), is(1420027199999L));
    assertThat(photo.getCreationDate(), is(CREATE_DATE));
    assertThat(photo.getCreator(), is(adminAccessUser));
    assertThat(photo.getLastUpdateDate(), is(CREATE_DATE));
    assertThat(photo.getLastUpdater(), is(adminAccessUser));

    assertThat(photo.getFileName(), is("fileName_1"));
    assertThat(photo.getFileSize(), is(101L));
    assertThat(photo.getFileMimeType(), is(MediaMimeType.JPG));
    assertThat(photo.isDownloadAuthorized(), is(false));
    assertThat(photo.getDownloadPeriod(), sameInstance(Period.UNDEFINED));

    assertThat(photo.getDefinition().getWidth(), is(1000));
    assertThat(photo.getDefinition().getHeight(), is(750));
  }

  @Test
  public void verifyVideoData() throws Exception {
    Video video =
        MediaDAO.getByCriteria(defaultMediaCriteria().identifierIsOneOf("v_1")).getVideo();
    assertThat(video, notNullValue());
    assertThat(video.getMediaPK(), notNullValue());
    assertThat(video.getId(), is("v_1"));
    assertThat(video.getInstanceId(), is(INSTANCE_A));
    assertThat(video.getType(), is(MediaType.Video));
    assertThat(video.getTitle(), is("title v_1"));
    assertThat(video.getDescription(), is(""));
    assertThat(video.getAuthor(), is(""));
    assertThat(video.getKeyWord(), is(""));
    assertThat(video.getVisibilityPeriod().getBeginDate().getTime(), is(1388530800000L));
    assertThat(video.getVisibilityPeriod().getEndDate().getTime(), is(1404125999999L));
    assertThat(video.getCreationDate(), is(CREATE_DATE));
    assertThat(video.getCreator(), is(adminAccessUser));
    assertThat(video.getLastUpdateDate(), is(LAST_UPDATE_DATE));
    assertThat(video.getLastUpdater(), is(publisherUser));

    assertThat(video.getFileName(), is("fileName_v_1"));
    assertThat(video.getFileSize(), is(201L));
    assertThat(video.getFileMimeType(), is(MediaMimeType.MP4));
    assertThat(video.isDownloadAuthorized(), is(true));
    assertThat(video.getDownloadPeriod(), not(sameInstance(Period.UNDEFINED)));
    assertThat(video.getDownloadPeriod().getBeginDate().getTime(), is(1388530800000L));
    assertThat(video.getDownloadPeriod().getEndDate(), is(DateUtil.MAXIMUM_DATE));

    assertThat(video.getDefinition().getWidth(), is(1920));
    assertThat(video.getDefinition().getHeight(), is(1080));
    assertThat(video.getBitrate(), is(5000L));
    assertThat(video.getDuration(), is(36000000L));
  }

  @Test
  public void verifySoundData() throws Exception {
    Sound sound =
        MediaDAO.getByCriteria(defaultMediaCriteria().identifierIsOneOf("s_1")).getSound();
    assertThat(sound, notNullValue());
    assertThat(sound.getMediaPK(), notNullValue());
    assertThat(sound.getId(), is("s_1"));
    assertThat(sound.getInstanceId(), is(INSTANCE_A));
    assertThat(sound.getType(), is(MediaType.Sound));
    assertThat(sound.getTitle(), is("title s_1"));
    assertThat(sound.getDescription(), is(""));
    assertThat(sound.getAuthor(), is(""));
    assertThat(sound.getKeyWord(), is(""));
    assertThat(sound.getVisibilityPeriod().getBeginDate().getTime(), is(1388530800000L));
    assertThat(sound.getVisibilityPeriod().getEndDate().getTime(), is(1420027199999L));
    assertThat(sound.getCreationDate(), is(CREATE_DATE));
    assertThat(sound.getCreator(), is(writerUser));
    assertThat(sound.getLastUpdateDate(), is(LAST_UPDATE_DATE));
    assertThat(sound.getLastUpdater(), is(publisherUser));

    assertThat(sound.getFileName(), is("fileName_s_1"));
    assertThat(sound.getFileSize(), is(301L));
    assertThat(sound.getFileMimeType(), is(MediaMimeType.MP3));
    assertThat(sound.isDownloadAuthorized(), is(false));
    assertThat(sound.getDownloadPeriod(), not(sameInstance(Period.UNDEFINED)));
    assertThat(sound.getDownloadPeriod().getBeginDate(), is(DateUtil.MINIMUM_DATE));
    assertThat(sound.getDownloadPeriod().getEndDate().getTime(), is(1420027199999L));

    assertThat(sound.getBitrate(), is(500L));
    assertThat(sound.getDuration(), is(3600000L));
  }

  @Test
  public void verifyStreamingData() throws Exception {
    Streaming streaming =
        MediaDAO.getByCriteria(defaultMediaCriteria().identifierIsOneOf("stream_1")).getStreaming();
    assertThat(streaming, notNullValue());
    assertThat(streaming.getMediaPK(), notNullValue());
    assertThat(streaming.getId(), is("stream_1"));
    assertThat(streaming.getInstanceId(), is(INSTANCE_A));
    assertThat(streaming.getType(), is(MediaType.Streaming));
    assertThat(streaming.getTitle(), is("title stream_1"));
    assertThat(streaming.getDescription(), is(""));
    assertThat(streaming.getAuthor(), is("an author stream_1"));
    assertThat(streaming.getKeyWord(), is(""));
    assertThat(streaming.getVisibilityPeriod().getBeginDate().getTime(), is(1388530800000L));
    assertThat(streaming.getVisibilityPeriod().getEndDate().getTime(), is(1396263599999L));
    assertThat(streaming.getCreationDate(), is(CREATE_DATE));
    assertThat(streaming.getCreator(), is(publisherUser));
    assertThat(streaming.getLastUpdateDate(), is(LAST_UPDATE_DATE));
    assertThat(streaming.getLastUpdater(), is(adminAccessUser));

    assertThat(streaming.getHomepageUrl(), is("url_1"));
    assertThat(streaming.getProvider().map(StreamingProvider::getName).orElse(null), is("youtube"));
  }

  @Test
  public void verifyAuthorOrdering() throws Exception {
    List<Media> media = MediaDAO.findByCriteria(defaultMediaCriteria());
    assertThat(media, hasSize(8));
    assertMediaIdentifiers(media, false, "1", "2", "v_1", "v_2", "s_1", "s_2", "stream_1",
        "stream_2");

    media = MediaDAO.findByCriteria(defaultMediaCriteria().orderedBy(AUTHOR_ASC));
    assertThat(media, hasSize(8));
    int index = 3;
    assertThat(media.get(index++).getAuthor(), is(emptyString()));
    assertThat(media.get(index++).getAuthor(), is("an author 1"));
    assertThat(media.get(index++).getAuthor(), is("an author s_2"));
    assertThat(media.get(index++).getAuthor(), is("an author stream_1"));
    assertThat(media.get(index++).getAuthor(), is("an author v_2"));
    assertThat(index, is(8));

    media = MediaDAO.findByCriteria(defaultMediaCriteria().orderedBy(AUTHOR_DESC));
    assertThat(media, hasSize(8));
    index = 0;
    assertThat(media.get(index++).getAuthor(), is("an author v_2"));
    assertThat(media.get(index++).getAuthor(), is("an author stream_1"));
    assertThat(media.get(index++).getAuthor(), is("an author s_2"));
    assertThat(media.get(index++).getAuthor(), is("an author 1"));
    assertThat(media.get(index++).getAuthor(), is(emptyString()));
    assertThat(index, is(5));

    media = MediaDAO.findByCriteria(defaultMediaCriteria().orderedBy(AUTHOR_ASC_EMPTY_END));
    assertThat(media, hasSize(8));
    index = 0;
    assertThat(media.get(index++).getAuthor(), is("an author 1"));
    assertThat(media.get(index++).getAuthor(), is("an author s_2"));
    assertThat(media.get(index++).getAuthor(), is("an author stream_1"));
    assertThat(media.get(index++).getAuthor(), is("an author v_2"));
    assertThat(media.get(index++).getAuthor(), is(emptyString()));
    assertThat(index, is(5));

    media = MediaDAO.findByCriteria(defaultMediaCriteria().orderedBy(AUTHOR_DESC_EMPTY_END));
    assertThat(media, hasSize(8));
    index = 0;
    assertThat(media.get(index++).getAuthor(), is("an author v_2"));
    assertThat(media.get(index++).getAuthor(), is("an author stream_1"));
    assertThat(media.get(index++).getAuthor(), is("an author s_2"));
    assertThat(media.get(index++).getAuthor(), is("an author 1"));
    assertThat(media.get(index++).getAuthor(), is(emptyString()));
    assertThat(index, is(5));


    // Two stupid orderings that shows that it works !
    media =
        MediaDAO.findByCriteria(defaultMediaCriteria().orderedBy(AUTHOR_ASC_EMPTY_END, AUTHOR_ASC));
    assertThat(media, hasSize(8));
    index = 0;
    assertThat(media.get(index++).getAuthor(), is("an author 1"));
    assertThat(media.get(index++).getAuthor(), is("an author s_2"));
    assertThat(media.get(index++).getAuthor(), is("an author stream_1"));
    assertThat(media.get(index++).getAuthor(), is("an author v_2"));
    assertThat(media.get(index++).getAuthor(), is(emptyString()));
    assertThat(index, is(5));

    media =
        MediaDAO.findByCriteria(defaultMediaCriteria().orderedBy(AUTHOR_ASC, AUTHOR_ASC_EMPTY_END));
    assertThat(media, hasSize(8));
    index = 3;
    assertThat(media.get(index++).getAuthor(), is(emptyString()));
    assertThat(media.get(index++).getAuthor(), is("an author 1"));
    assertThat(media.get(index++).getAuthor(), is("an author s_2"));
    assertThat(media.get(index++).getAuthor(), is("an author stream_1"));
    assertThat(media.get(index++).getAuthor(), is("an author v_2"));
    assertThat(index, is(8));
  }

  @Test
  public void verifyTitleOrdering() throws Exception {
    List<Media> media = MediaDAO.findByCriteria(defaultMediaCriteria());
    assertThat(media, hasSize(8));
    assertMediaIdentifiers(media, false, "1", "2", "v_1", "v_2", "s_1", "s_2", "stream_1",
        "stream_2");

    media = MediaDAO.findByCriteria(defaultMediaCriteria().orderedBy(TITLE_ASC));
    assertThat(media, hasSize(8));
    int index = 0;
    assertThat(media.get(index++).getTitle(), is("title 1"));
    assertThat(media.get(index++).getTitle(), is("title 2"));
    assertThat(media.get(index++).getTitle(), is("title s_1"));
    assertThat(media.get(index++).getTitle(), is("title s_2"));
    assertThat(media.get(index++).getTitle(), is("title stream_1"));
    assertThat(media.get(index++).getTitle(), is("title stream_2"));
    assertThat(media.get(index++).getTitle(), is("title v_1"));
    assertThat(media.get(index++).getTitle(), is("title v_2"));
    assertThat(index, is(8));

    media = MediaDAO.findByCriteria(defaultMediaCriteria().orderedBy(TITLE_DESC, AUTHOR_DESC));
    assertThat(media, hasSize(8));
    index = 0;
    assertThat(media.get(index++).getTitle(), is("title v_2"));
    assertThat(media.get(index++).getTitle(), is("title v_1"));
    assertThat(media.get(index++).getTitle(), is("title stream_2"));
    assertThat(media.get(index++).getTitle(), is("title stream_1"));
    assertThat(media.get(index++).getTitle(), is("title s_2"));
    assertThat(media.get(index++).getTitle(), is("title s_1"));
    assertThat(media.get(index++).getTitle(), is("title 2"));
    assertThat(media.get(index++).getTitle(), is("title 1"));
    assertThat(index, is(8));

    media =
        MediaDAO.findByCriteria(defaultMediaCriteria().orderedBy(AUTHOR_DESC_EMPTY_END, TITLE_ASC));
    assertThat(media, hasSize(8));
    index = 0;
    assertThat(media.get(index++).getTitle(), is("title v_2"));
    assertThat(media.get(index++).getTitle(), is("title stream_1"));
    assertThat(media.get(index++).getTitle(), is("title s_2"));
    assertThat(media.get(index++).getTitle(), is("title 1"));
    assertThat(media.get(index++).getTitle(), is("title 2"));
    assertThat(media.get(index++).getTitle(), is("title s_1"));
    assertThat(media.get(index++).getTitle(), is("title stream_2"));
    assertThat(media.get(index++).getTitle(), is("title v_1"));
    assertThat(index, is(8));
  }

  @Test
  public void verifyDimensionOrdering() throws Exception {
    List<Media> media = MediaDAO.findByCriteria(defaultMediaCriteria());
    assertThat(media, hasSize(8));
    assertMediaIdentifiers(media, false, "1", "2", "v_1", "v_2", "s_1", "s_2", "stream_1",
        "stream_2");

    media = MediaDAO.findByCriteria(defaultMediaCriteria().orderedBy(DIMENSION_ASC));
    assertThat(media, hasSize(8));
    int index = 0;
    assertThat(media.get(index++).getId(), is("2"));
    assertThat(media.get(index++).getId(), is("1"));
    assertThat(media.get(index++).getId(), is("v_2"));
    assertThat(media.get(index++).getId(), is("v_1"));
    assertThat(index, is(4));

    media = MediaDAO.findByCriteria(defaultMediaCriteria().orderedBy(DIMENSION_DESC));
    assertThat(media, hasSize(8));
    index = 0;
    assertThat(media.get(index++).getId(), is("v_1"));
    assertThat(media.get(index++).getId(), is("v_2"));
    assertThat(media.get(index++).getId(), is("1"));
    assertThat(media.get(index++).getId(), is("2"));
    assertThat(index, is(4));
  }

  @SuppressWarnings("DataFlowIssue")
  @Test
  public void saveNewPhoto() throws Exception {
    Date now = DateUtil.getNow();
    Date beginVisibilityDate = DateUtils.addMonths(now, -1);
    Date endVisibilityDate = DateUtils.addMonths(now, 4);
    Date beginDownloadDate = DateUtils.addDays(now, -10);
    Date endDownloadDate = DateUtils.addDays(now, 5);

    Photo newPhoto = new Photo();

    newPhoto.setComponentInstanceId(INSTANCE_A);
    newPhoto.setTitle("A title");
    newPhoto.setDescription("A description");
    newPhoto.setAuthor("An author");
    newPhoto.setKeyWord("keywords");
    newPhoto.setVisibilityPeriod(Period.from(beginVisibilityDate, endVisibilityDate));

    newPhoto.setFileName("new file name");
    newPhoto.setFileSize(2048);
    newPhoto.setFileMimeType(MediaMimeType.TIFF);
    newPhoto.setDownloadAuthorized(true);
    newPhoto.setDownloadPeriod(Period.from(beginDownloadDate, endDownloadDate));

    newPhoto.setDefinition(Definition.of(200, 100));

    assertThat(newPhoto.getId(), nullValue());
    String newId = Transaction.performInOne(
        () -> MediaDAO.saveMedia(OperationContext.fromUser(adminAccessUser), newPhoto));
    assertThat(newPhoto.getId(), notNullValue());
    assertThat(newPhoto.getId(), is(newId));

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
      ITable mediaTable = actualDataSet.getTable("SC_Gallery_Media");
      assertThat(mediaTable.getRowCount(), is(MEDIA_ROW_COUNT + 1));
      ITable internalTable = actualDataSet.getTable("SC_Gallery_Internal");
      assertThat(internalTable.getRowCount(), is(MEDIA_INTERNAL_ROW_COUNT + 1));
      ITable photoTable = actualDataSet.getTable("SC_Gallery_Photo");
      assertThat(photoTable.getRowCount(), is(MEDIA_PHOTO_ROW_COUNT + 1));
      ITable videoTable = actualDataSet.getTable("SC_Gallery_Video");
      assertThat(videoTable.getRowCount(), is(MEDIA_VIDEO_ROW_COUNT));
      ITable soundTable = actualDataSet.getTable("SC_Gallery_Sound");
      assertThat(soundTable.getRowCount(), is(MEDIA_SOUND_ROW_COUNT));
      ITable streamingTable = actualDataSet.getTable("SC_Gallery_Streaming");
      assertThat(streamingTable.getRowCount(), is(MEDIA_STREAMING_ROW_COUNT));
      ITable pathTable = actualDataSet.getTable("SC_Gallery_Path");
      assertThat(pathTable.getRowCount(), is(MEDIA_PATH_ROW_COUNT));

      TableRow mediaRow = getTableRowFor(mediaTable, "mediaId", newPhoto.getId());
      assertThat(mediaRow.getString("mediaId"), is(newId));
      assertThat(mediaRow.getString("mediaType"), is(MediaType.Photo.name()));
      assertThat(mediaRow.getString("instanceId"), is(INSTANCE_A));
      assertThat(mediaRow.getString("title"), is("A title"));
      assertThat(mediaRow.getString("description"), is("A description"));
      assertThat(mediaRow.getString("author"), is("An author"));
      assertThat(mediaRow.getString("keyword"), is("keywords"));
      assertThat(mediaRow.getLong("beginVisibilityDate"), is(beginVisibilityDate.getTime()));
      assertThat(mediaRow.getLong("endVisibilityDate"), is(endVisibilityDate.getTime()));
      assertThat(mediaRow.getDate("createDate"), greaterThanOrEqualTo(now));
      assertThat(mediaRow.getString("createdBy"), is(adminAccessUser.getId()));
      assertThat(mediaRow.getDate("lastUpdateDate"), is(mediaRow.getDate("createDate")));
      assertThat(mediaRow.getString("lastUpdatedBy"), is(adminAccessUser.getId()));

      TableRow iMediaRow = getTableRowFor(internalTable, "mediaId", newPhoto.getId());
      assertThat(iMediaRow.getString("mediaId"), is(newId));
      assertThat(iMediaRow.getString("fileName"), is("new file name"));
      assertThat(iMediaRow.getLong("fileSize"), is(2048L));
      assertThat(iMediaRow.getString("fileMimeType"), is("image/tiff"));
      assertThat(iMediaRow.getInteger("download"), is(1));
      assertThat(iMediaRow.getLong("beginDownloadDate"), is(beginDownloadDate.getTime()));
      assertThat(iMediaRow.getLong("endDownloadDate"), is(endDownloadDate.getTime()));

      TableRow photoRow = getTableRowFor(photoTable, "mediaId", newPhoto.getId());
      assertThat(photoRow.getString("mediaId"), is(newId));
      assertThat(photoRow.getInteger("resolutionH"), is(100));
      assertThat(photoRow.getInteger("resolutionW"), is(200));
    }
  }

  @SuppressWarnings("DataFlowIssue")
  @Test
  public void saveExistingPhoto() throws Exception {
    Date now = DateUtil.getNow();
    Date endVisibilityDate = DateUtils.addMonths(now, 4);
    Date beginDownloadDate = DateUtils.addDays(now, -10);

    String mediaIdToUpdate = "2";

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
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

      TableRow mediaRow = getTableRowFor(mediaTable, "mediaId", mediaIdToUpdate);
      assertThat(mediaRow.getString("mediaId"), is(mediaIdToUpdate));
      assertThat(mediaRow.getString("mediaType"), is(MediaType.Photo.name()));
      assertThat(mediaRow.getString("instanceId"), is(INSTANCE_A));
      assertThat(mediaRow.getString("title"), is("title 2"));
      assertThat(mediaRow.getValue("description"), nullValue());
      assertThat(mediaRow.getValue("author"), nullValue());
      assertThat(mediaRow.getValue("keyword"), nullValue());
      assertThat(mediaRow.getLong("beginVisibilityDate"), is(DateUtil.MINIMUM_DATE.getTime()));
      assertThat(mediaRow.getLong("endVisibilityDate"), is(DateUtil.MAXIMUM_DATE.getTime()));
      assertThat(mediaRow.getDate("createDate"), is(CREATE_DATE));
      assertThat(mediaRow.getString("createdBy"), is("0"));
      assertThat(mediaRow.getDate("lastUpdateDate"), is(CREATE_DATE));
      assertThat(mediaRow.getString("lastUpdatedBy"), is("0"));

      TableRow iMediaRow = getTableRowFor(internalTable, "mediaId", mediaIdToUpdate);
      assertThat(iMediaRow.getString("mediaId"), is(mediaIdToUpdate));
      assertThat(iMediaRow.getString("fileName"), is("fileName_2"));
      assertThat(iMediaRow.getLong("fileSize"), is(102L));
      assertThat(iMediaRow.getString("fileMimeType"), is("image/png"));
      assertThat(iMediaRow.getInteger("download"), is(0));
      assertThat(iMediaRow.getValue("beginDownloadDate"), nullValue());
      assertThat(iMediaRow.getValue("endDownloadDate"), nullValue());

      TableRow photoRow = getTableRowFor(photoTable, "mediaId", mediaIdToUpdate);
      assertThat(photoRow.getString("mediaId"), is(mediaIdToUpdate));
      assertThat(photoRow.getInteger("resolutionH"), is(600));
      assertThat(photoRow.getInteger("resolutionW"), is(800));
    }

    Photo photoToUpdate = Transaction.performInOne(
        () -> MediaDAO.getByCriteria(defaultMediaCriteria().identifierIsOneOf(mediaIdToUpdate)))
        .getPhoto();

    photoToUpdate.setTitle(photoToUpdate.getTitle() + "_updated");
    photoToUpdate.setKeyWord("keywords_updated");
    photoToUpdate.setVisibilityPeriod(Period.from(DateUtil.MINIMUM_DATE, endVisibilityDate));

    photoToUpdate.setFileName(photoToUpdate.getFileName() + "_updated");
    photoToUpdate.setFileSize(2048);
    photoToUpdate.setFileMimeType(MediaMimeType.TIFF);
    photoToUpdate.setDownloadAuthorized(true);
    photoToUpdate.setDownloadPeriod(Period.from(beginDownloadDate, DateUtil.MAXIMUM_DATE));

    photoToUpdate.setDefinition(Definition.of(200, 100));

    String savedMediaId = photoToUpdate.getId();
    String mediaId = Transaction.performInOne(
        () -> MediaDAO.saveMedia(OperationContext.fromUser(publisherUser), photoToUpdate));
    assertThat(mediaId, is(savedMediaId));
    assertThat(mediaId, is(photoToUpdate.getId()));

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
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

      TableRow mediaRow = getTableRowFor(mediaTable, "mediaId", mediaIdToUpdate);
      assertThat(mediaRow.getString("mediaId"), is(mediaIdToUpdate));
      assertThat(mediaRow.getString("mediaType"), is(MediaType.Photo.name()));
      assertThat(mediaRow.getString("instanceId"), is(INSTANCE_A));
      assertThat(mediaRow.getString("title"), is("title 2_updated"));
      assertThat(mediaRow.getString("description"), is(emptyString()));
      assertThat(mediaRow.getString("author"), is(emptyString()));
      assertThat(mediaRow.getString("keyword"), is("keywords_updated"));
      assertThat(mediaRow.getLong("beginVisibilityDate"), is(DateUtil.MINIMUM_DATE.getTime()));
      assertThat(mediaRow.getLong("endVisibilityDate"), is(endVisibilityDate.getTime()));
      assertThat(mediaRow.getDate("createDate"), is(CREATE_DATE));
      assertThat(mediaRow.getString("createdBy"), is("0"));
      assertThat(mediaRow.getDate("lastUpdateDate"), greaterThanOrEqualTo(now));
      assertThat(mediaRow.getString("lastUpdatedBy"), is(publisherUser.getId()));

      TableRow iMediaRow = getTableRowFor(internalTable, "mediaId", mediaIdToUpdate);
      assertThat(iMediaRow.getString("mediaId"), is(mediaIdToUpdate));
      assertThat(iMediaRow.getString("fileName"), is("fileName_2_updated"));
      assertThat(iMediaRow.getLong("fileSize"), is(2048L));
      assertThat(iMediaRow.getString("fileMimeType"), is("image/tiff"));
      assertThat(iMediaRow.getInteger("download"), is(1));
      assertThat(iMediaRow.getLong("beginDownloadDate"), is(beginDownloadDate.getTime()));
      assertThat(iMediaRow.getValue("endDownloadDate"), nullValue());

      TableRow photoRow = getTableRowFor(photoTable, "mediaId", mediaIdToUpdate);
      assertThat(photoRow.getString("mediaId"), is(mediaIdToUpdate));
      assertThat(photoRow.getInteger("resolutionH"), is(100));
      assertThat(photoRow.getInteger("resolutionW"), is(200));
    }
  }

  @Test
  public void deleteMediaThatDoesNotExist() {
    expectedException.expectCause(instanceOf(NullPointerException.class));
    Transaction.performInOne(() -> {
      MediaDAO.deleteMedia(null);
      return null;
    });
  }

  @Test
  public void deletePhoto() throws Exception {

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
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
    }

    String mediaIdToDelete = "2";

    Photo photoToDelete =
        MediaDAO.getByCriteria(defaultMediaCriteria().identifierIsOneOf(mediaIdToDelete))
            .getPhoto();

    Transaction.performInOne(() -> {
      MediaDAO.deleteMedia(photoToDelete);
      return null;
    });

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
      ITable mediaTable = actualDataSet.getTable("SC_Gallery_Media");
      assertThat(mediaTable.getRowCount(), is(MEDIA_ROW_COUNT - 1));
      ITable internalTable = actualDataSet.getTable("SC_Gallery_Internal");
      assertThat(internalTable.getRowCount(), is(MEDIA_INTERNAL_ROW_COUNT - 1));
      ITable photoTable = actualDataSet.getTable("SC_Gallery_Photo");
      assertThat(photoTable.getRowCount(), is(MEDIA_PHOTO_ROW_COUNT - 1));
      ITable videoTable = actualDataSet.getTable("SC_Gallery_Video");
      assertThat(videoTable.getRowCount(), is(MEDIA_VIDEO_ROW_COUNT));
      ITable soundTable = actualDataSet.getTable("SC_Gallery_Sound");
      assertThat(soundTable.getRowCount(), is(MEDIA_SOUND_ROW_COUNT));
      ITable streamingTable = actualDataSet.getTable("SC_Gallery_Streaming");
      assertThat(streamingTable.getRowCount(), is(MEDIA_STREAMING_ROW_COUNT));
      ITable pathTable = actualDataSet.getTable("SC_Gallery_Path");
      assertThat(pathTable.getRowCount(), is(MEDIA_PATH_ROW_COUNT - 1));
    }

    Transaction.performInOne(() -> {
      MediaDAO.deleteMedia(photoToDelete);
      return null;
    });

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
      ITable mediaTable = actualDataSet.getTable("SC_Gallery_Media");
      assertThat(mediaTable.getRowCount(), is(MEDIA_ROW_COUNT - 1));
      ITable internalTable = actualDataSet.getTable("SC_Gallery_Internal");
      assertThat(internalTable.getRowCount(), is(MEDIA_INTERNAL_ROW_COUNT - 1));
      ITable photoTable = actualDataSet.getTable("SC_Gallery_Photo");
      assertThat(photoTable.getRowCount(), is(MEDIA_PHOTO_ROW_COUNT - 1));
      ITable videoTable = actualDataSet.getTable("SC_Gallery_Video");
      assertThat(videoTable.getRowCount(), is(MEDIA_VIDEO_ROW_COUNT));
      ITable soundTable = actualDataSet.getTable("SC_Gallery_Sound");
      assertThat(soundTable.getRowCount(), is(MEDIA_SOUND_ROW_COUNT));
      ITable streamingTable = actualDataSet.getTable("SC_Gallery_Streaming");
      assertThat(streamingTable.getRowCount(), is(MEDIA_STREAMING_ROW_COUNT));
      ITable pathTable = actualDataSet.getTable("SC_Gallery_Path");
      assertThat(pathTable.getRowCount(), is(MEDIA_PATH_ROW_COUNT - 1));
    }
  }

  @SuppressWarnings("DataFlowIssue")
  @Test
  public void saveNewVideo() throws Exception {
    Date now = DateUtil.getNow();
    Date beginVisibilityDate = DateUtils.addMonths(now, -1);
    Date endDownloadDate = DateUtils.addDays(now, 5);

    Video newVideo = new Video();

    newVideo.setComponentInstanceId(INSTANCE_A);
    newVideo.setTitle("A video title");
    newVideo.setAuthor("A video author");
    newVideo.setKeyWord("video keywords");
    newVideo.setVisibilityPeriod(Period.from(beginVisibilityDate, DateUtil.MAXIMUM_DATE));

    newVideo.setFileName("new video file name");
    newVideo.setFileSize(2048);
    newVideo.setFileMimeType(MediaMimeType.MP4);
    newVideo.setDownloadAuthorized(false);
    newVideo.setDownloadPeriod(Period.from(DateUtil.MINIMUM_DATE, endDownloadDate));

    newVideo.setDefinition(Definition.of(1920, 1080));

    assertThat(newVideo.getId(), nullValue());
    String newId = Transaction
        .performInOne(() -> MediaDAO.saveMedia(OperationContext.fromUser(writerUser), newVideo));
    assertThat(newVideo.getId(), notNullValue());
    assertThat(newVideo.getId(), is(newId));

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
      ITable mediaTable = actualDataSet.getTable("SC_Gallery_Media");
      assertThat(mediaTable.getRowCount(), is(MEDIA_ROW_COUNT + 1));
      ITable internalTable = actualDataSet.getTable("SC_Gallery_Internal");
      assertThat(internalTable.getRowCount(), is(MEDIA_INTERNAL_ROW_COUNT + 1));
      ITable photoTable = actualDataSet.getTable("SC_Gallery_Photo");
      assertThat(photoTable.getRowCount(), is(MEDIA_PHOTO_ROW_COUNT));
      ITable videoTable = actualDataSet.getTable("SC_Gallery_Video");
      assertThat(videoTable.getRowCount(), is(MEDIA_VIDEO_ROW_COUNT + 1));
      ITable soundTable = actualDataSet.getTable("SC_Gallery_Sound");
      assertThat(soundTable.getRowCount(), is(MEDIA_SOUND_ROW_COUNT));
      ITable streamingTable = actualDataSet.getTable("SC_Gallery_Streaming");
      assertThat(streamingTable.getRowCount(), is(MEDIA_STREAMING_ROW_COUNT));
      ITable pathTable = actualDataSet.getTable("SC_Gallery_Path");
      assertThat(pathTable.getRowCount(), is(MEDIA_PATH_ROW_COUNT));


      TableRow mediaRow = getTableRowFor(mediaTable, "mediaId", newVideo.getId());
      assertThat(mediaRow.getString("mediaId"), is(newId));
      assertThat(mediaRow.getString("mediaType"), is(MediaType.Video.name()));
      assertThat(mediaRow.getString("instanceId"), is(INSTANCE_A));
      assertThat(mediaRow.getString("title"), is("A video title"));
      assertThat(mediaRow.getString("description"), is(emptyString()));
      assertThat(mediaRow.getString("author"), is("A video author"));
      assertThat(mediaRow.getString("keyword"), is("video keywords"));
      assertThat(mediaRow.getLong("beginVisibilityDate"), is(beginVisibilityDate.getTime()));
      assertThat(mediaRow.getLong("endVisibilityDate"), is(DateUtil.MAXIMUM_DATE.getTime()));
      assertThat(mediaRow.getDate("createDate"), greaterThanOrEqualTo(now));
      assertThat(mediaRow.getString("createdBy"), is(writerUser.getId()));
      assertThat(mediaRow.getDate("lastUpdateDate"), is(mediaRow.getDate("createDate")));
      assertThat(mediaRow.getString("lastUpdatedBy"), is(writerUser.getId()));

      TableRow iMediaRow = getTableRowFor(internalTable, "mediaId", newVideo.getId());
      assertThat(iMediaRow.getString("mediaId"), is(newId));
      assertThat(iMediaRow.getString("fileName"), is("new video file name"));
      assertThat(iMediaRow.getLong("fileSize"), is(2048L));
      assertThat(iMediaRow.getString("fileMimeType"), is("video/mp4"));
      assertThat(iMediaRow.getInteger("download"), is(0));
      assertThat(iMediaRow.getLong("beginDownloadDate"), nullValue());
      assertThat(iMediaRow.getLong("endDownloadDate"), is(endDownloadDate.getTime()));

      TableRow videoRow = getTableRowFor(videoTable, "mediaId", newVideo.getId());
      assertThat(videoRow.getString("mediaId"), is(newId));
      assertThat(videoRow.getInteger("resolutionH"), is(1080));
      assertThat(videoRow.getInteger("resolutionW"), is(1920));
      assertThat(videoRow.getLong("bitrate"), is(0L));
      assertThat(videoRow.getLong("duration"), is(0L));
    }
  }

  @SuppressWarnings("DataFlowIssue")
  @Test
  public void saveExistingVideo() throws Exception {
    String mediaIdToUpdate = "v_2";

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
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

      TableRow videoRow = getTableRowFor(videoTable, "mediaId", mediaIdToUpdate);
      //noinspection DataFlowIssue
      assertThat(videoRow.getString("mediaId"), is(mediaIdToUpdate));
      assertThat(videoRow.getInteger("resolutionH"), is(720));
      assertThat(videoRow.getInteger("resolutionW"), is(1280));
      assertThat(videoRow.getLong("bitrate"), is(5000L));
      assertThat(videoRow.getLong("duration"), nullValue());
    }

    Video videoToUpdate = Transaction.performInOne(
        () -> MediaDAO.getByCriteria(defaultMediaCriteria().identifierIsOneOf(mediaIdToUpdate)))
        .getVideo();

    videoToUpdate.setDefinition(Definition.of(1920, 1080));
    videoToUpdate.setBitrate(10000);
    videoToUpdate.setDuration(72000000);

    String savedMediaId = videoToUpdate.getId();
    String mediaId = Transaction.performInOne(
        () -> MediaDAO.saveMedia(OperationContext.fromUser(writerUser), videoToUpdate));
    assertThat(mediaId, is(savedMediaId));
    assertThat(mediaId, is(videoToUpdate.getId()));

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
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

      TableRow videoRow = getTableRowFor(videoTable, "mediaId", videoToUpdate.getId());
      assertThat(videoRow.getString("mediaId"), is(mediaIdToUpdate));
      assertThat(videoRow.getInteger("resolutionH"), is(1080));
      assertThat(videoRow.getInteger("resolutionW"), is(1920));
      assertThat(videoRow.getLong("bitrate"), is(10000L));
      assertThat(videoRow.getLong("duration"), is(72000000L));
    }
  }

  @Test
  public void deleteVideo() throws Exception {

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
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
    }

    String mediaIdToDelete = "v_1";

    Video videoToDelete = Transaction.performInOne(
        () -> MediaDAO.getByCriteria(defaultMediaCriteria().identifierIsOneOf(mediaIdToDelete)))
        .getVideo();

    Transaction.performInOne(() -> {
      MediaDAO.deleteMedia(videoToDelete);
      return null;
    });

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
      ITable mediaTable = actualDataSet.getTable("SC_Gallery_Media");
      assertThat(mediaTable.getRowCount(), is(MEDIA_ROW_COUNT - 1));
      ITable internalTable = actualDataSet.getTable("SC_Gallery_Internal");
      assertThat(internalTable.getRowCount(), is(MEDIA_INTERNAL_ROW_COUNT - 1));
      ITable photoTable = actualDataSet.getTable("SC_Gallery_Photo");
      assertThat(photoTable.getRowCount(), is(MEDIA_PHOTO_ROW_COUNT));
      ITable videoTable = actualDataSet.getTable("SC_Gallery_Video");
      assertThat(videoTable.getRowCount(), is(MEDIA_VIDEO_ROW_COUNT - 1));
      ITable soundTable = actualDataSet.getTable("SC_Gallery_Sound");
      assertThat(soundTable.getRowCount(), is(MEDIA_SOUND_ROW_COUNT));
      ITable streamingTable = actualDataSet.getTable("SC_Gallery_Streaming");
      assertThat(streamingTable.getRowCount(), is(MEDIA_STREAMING_ROW_COUNT));
      ITable pathTable = actualDataSet.getTable("SC_Gallery_Path");
      assertThat(pathTable.getRowCount(), is(MEDIA_PATH_ROW_COUNT - 2));

      Transaction.performInOne(() -> {
        MediaDAO.deleteMedia(videoToDelete);
        return null;
      });

      actualDataSet = getActualDataSet(connection);
      mediaTable = actualDataSet.getTable("SC_Gallery_Media");
      assertThat(mediaTable.getRowCount(), is(MEDIA_ROW_COUNT - 1));
      internalTable = actualDataSet.getTable("SC_Gallery_Internal");
      assertThat(internalTable.getRowCount(), is(MEDIA_INTERNAL_ROW_COUNT - 1));
      photoTable = actualDataSet.getTable("SC_Gallery_Photo");
      assertThat(photoTable.getRowCount(), is(MEDIA_PHOTO_ROW_COUNT));
      videoTable = actualDataSet.getTable("SC_Gallery_Video");
      assertThat(videoTable.getRowCount(), is(MEDIA_VIDEO_ROW_COUNT - 1));
      soundTable = actualDataSet.getTable("SC_Gallery_Sound");
      assertThat(soundTable.getRowCount(), is(MEDIA_SOUND_ROW_COUNT));
      streamingTable = actualDataSet.getTable("SC_Gallery_Streaming");
      assertThat(streamingTable.getRowCount(), is(MEDIA_STREAMING_ROW_COUNT));
      pathTable = actualDataSet.getTable("SC_Gallery_Path");
      assertThat(pathTable.getRowCount(), is(MEDIA_PATH_ROW_COUNT - 2));
    }
  }

  @SuppressWarnings("DataFlowIssue")
  @Test
  public void saveNewSound() throws Exception {
    Date now = DateUtil.getNow();

    Sound newSound = new Sound();

    newSound.setComponentInstanceId(INSTANCE_A);
    newSound.setTitle("A sound title");
    newSound.setAuthor("A sound author");
    newSound.setKeyWord("sound keywords");

    newSound.setFileName("new sound file name");
    newSound.setFileSize(5685);
    newSound.setFileMimeType(MediaMimeType.MP3);
    newSound.setDownloadAuthorized(true);

    newSound.setBitrate(1500);
    newSound.setDuration(50000000);

    assertThat(newSound.getId(), nullValue());
    String newId = Transaction.performInOne(
        () -> MediaDAO.saveMedia(OperationContext.fromUser(adminAccessUser), newSound));
    assertThat(newSound.getId(), notNullValue());
    assertThat(newSound.getId(), is(newId));

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
      ITable mediaTable = actualDataSet.getTable("SC_Gallery_Media");
      assertThat(mediaTable.getRowCount(), is(MEDIA_ROW_COUNT + 1));
      ITable internalTable = actualDataSet.getTable("SC_Gallery_Internal");
      assertThat(internalTable.getRowCount(), is(MEDIA_INTERNAL_ROW_COUNT + 1));
      ITable photoTable = actualDataSet.getTable("SC_Gallery_Photo");
      assertThat(photoTable.getRowCount(), is(MEDIA_PHOTO_ROW_COUNT));
      ITable videoTable = actualDataSet.getTable("SC_Gallery_Video");
      assertThat(videoTable.getRowCount(), is(MEDIA_VIDEO_ROW_COUNT));
      ITable soundTable = actualDataSet.getTable("SC_Gallery_Sound");
      assertThat(soundTable.getRowCount(), is(MEDIA_SOUND_ROW_COUNT + 1));
      ITable streamingTable = actualDataSet.getTable("SC_Gallery_Streaming");
      assertThat(streamingTable.getRowCount(), is(MEDIA_STREAMING_ROW_COUNT));
      ITable pathTable = actualDataSet.getTable("SC_Gallery_Path");
      assertThat(pathTable.getRowCount(), is(MEDIA_PATH_ROW_COUNT));


      TableRow mediaRow = getTableRowFor(mediaTable, "mediaId", newSound.getId());
      assertThat(mediaRow.getString("mediaId"), is(newId));
      assertThat(mediaRow.getString("mediaType"), is(MediaType.Sound.name()));
      assertThat(mediaRow.getString("instanceId"), is(INSTANCE_A));
      assertThat(mediaRow.getString("title"), is("A sound title"));
      assertThat(mediaRow.getString("description"), is(emptyString()));
      assertThat(mediaRow.getString("author"), is("A sound author"));
      assertThat(mediaRow.getString("keyword"), is("sound keywords"));
      assertThat(mediaRow.getLong("beginVisibilityDate"), is(DateUtil.MINIMUM_DATE.getTime()));
      assertThat(mediaRow.getLong("endVisibilityDate"), is(DateUtil.MAXIMUM_DATE.getTime()));
      assertThat(mediaRow.getDate("createDate"), greaterThanOrEqualTo(now));
      assertThat(mediaRow.getString("createdBy"), is(adminAccessUser.getId()));
      assertThat(mediaRow.getDate("lastUpdateDate"), is(mediaRow.getDate("createDate")));
      assertThat(mediaRow.getString("lastUpdatedBy"), is(adminAccessUser.getId()));

      TableRow iMediaRow = getTableRowFor(internalTable, "mediaId", newSound.getId());
      assertThat(iMediaRow.getString("mediaId"), is(newId));
      assertThat(iMediaRow.getString("fileName"), is("new sound file name"));
      assertThat(iMediaRow.getLong("fileSize"), is(5685L));
      assertThat(iMediaRow.getString("fileMimeType"), is("audio/x-mpeg"));
      assertThat(iMediaRow.getInteger("download"), is(1));
      assertThat(iMediaRow.getLong("beginDownloadDate"), nullValue());
      assertThat(iMediaRow.getLong("endDownloadDate"), nullValue());

      TableRow soundRow = getTableRowFor(soundTable, "mediaId", newSound.getId());
      assertThat(soundRow.getString("mediaId"), is(newId));
      assertThat(soundRow.getLong("bitrate"), is(1500L));
      assertThat(soundRow.getLong("duration"), is(50000000L));
    }
  }

  @SuppressWarnings("DataFlowIssue")
  @Test
  public void saveExistingSound() throws Exception {
    String mediaIdToUpdate = "s_2";

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
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

      TableRow soundRow = getTableRowFor(soundTable, "mediaId", mediaIdToUpdate);
      assertThat(soundRow.getString("mediaId"), is(mediaIdToUpdate));
      assertThat(soundRow.getLong("bitrate"), nullValue());
      assertThat(soundRow.getLong("duration"), is(3600000L));
    }

    Sound soundToUpdate = Transaction.performInOne(
        () -> MediaDAO.getByCriteria(defaultMediaCriteria().identifierIsOneOf(mediaIdToUpdate)))
        .getSound();

    soundToUpdate.setBitrate(1500);
    soundToUpdate.setDuration(50000000);

    String savedMediaId = soundToUpdate.getId();
    String mediaId = Transaction.performInOne(
        () -> MediaDAO.saveMedia(OperationContext.fromUser(writerUser), soundToUpdate));
    assertThat(mediaId, is(savedMediaId));
    assertThat(mediaId, is(soundToUpdate.getId()));

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
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

      TableRow soundRow = getTableRowFor(soundTable, "mediaId", soundToUpdate.getId());
      assertThat(soundRow.getString("mediaId"), is(mediaIdToUpdate));
      assertThat(soundRow.getLong("bitrate"), is(1500L));
      assertThat(soundRow.getLong("duration"), is(50000000L));
    }
  }

  @Test
  public void deleteSound() throws Exception {

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
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
    }

    String mediaIdToDelete = "s_1";

    Sound soundToDelete = Transaction.performInOne(
        () -> MediaDAO.getByCriteria(defaultMediaCriteria().identifierIsOneOf(mediaIdToDelete)))
        .getSound();

    Transaction.performInOne(() -> {
      MediaDAO.deleteMedia(soundToDelete);
      return null;
    });

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
      ITable mediaTable = actualDataSet.getTable("SC_Gallery_Media");
      assertThat(mediaTable.getRowCount(), is(MEDIA_ROW_COUNT - 1));
      ITable internalTable = actualDataSet.getTable("SC_Gallery_Internal");
      assertThat(internalTable.getRowCount(), is(MEDIA_INTERNAL_ROW_COUNT - 1));
      ITable photoTable = actualDataSet.getTable("SC_Gallery_Photo");
      assertThat(photoTable.getRowCount(), is(MEDIA_PHOTO_ROW_COUNT));
      ITable videoTable = actualDataSet.getTable("SC_Gallery_Video");
      assertThat(videoTable.getRowCount(), is(MEDIA_VIDEO_ROW_COUNT));
      ITable soundTable = actualDataSet.getTable("SC_Gallery_Sound");
      assertThat(soundTable.getRowCount(), is(MEDIA_SOUND_ROW_COUNT - 1));
      ITable streamingTable = actualDataSet.getTable("SC_Gallery_Streaming");
      assertThat(streamingTable.getRowCount(), is(MEDIA_STREAMING_ROW_COUNT));
      ITable pathTable = actualDataSet.getTable("SC_Gallery_Path");
      assertThat(pathTable.getRowCount(), is(MEDIA_PATH_ROW_COUNT - 1));
    }

    Transaction.performInOne(() -> {
      MediaDAO.deleteMedia(soundToDelete);
      return null;
    });

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
      ITable mediaTable = actualDataSet.getTable("SC_Gallery_Media");
      assertThat(mediaTable.getRowCount(), is(MEDIA_ROW_COUNT - 1));
      ITable internalTable = actualDataSet.getTable("SC_Gallery_Internal");
      assertThat(internalTable.getRowCount(), is(MEDIA_INTERNAL_ROW_COUNT - 1));
      ITable photoTable = actualDataSet.getTable("SC_Gallery_Photo");
      assertThat(photoTable.getRowCount(), is(MEDIA_PHOTO_ROW_COUNT));
      ITable videoTable = actualDataSet.getTable("SC_Gallery_Video");
      assertThat(videoTable.getRowCount(), is(MEDIA_VIDEO_ROW_COUNT));
      ITable soundTable = actualDataSet.getTable("SC_Gallery_Sound");
      assertThat(soundTable.getRowCount(), is(MEDIA_SOUND_ROW_COUNT - 1));
      ITable streamingTable = actualDataSet.getTable("SC_Gallery_Streaming");
      assertThat(streamingTable.getRowCount(), is(MEDIA_STREAMING_ROW_COUNT));
      ITable pathTable = actualDataSet.getTable("SC_Gallery_Path");
      assertThat(pathTable.getRowCount(), is(MEDIA_PATH_ROW_COUNT - 1));
    }
  }

  @SuppressWarnings("DataFlowIssue")
  @Test
  public void saveNewStreaming() throws Exception {
    Date now = DateUtil.getNow();

    Streaming newStreaming = new Streaming();

    newStreaming.setComponentInstanceId(INSTANCE_A);
    newStreaming.setTitle("A streaming title");
    newStreaming.setAuthor("A streaming author");
    newStreaming.setKeyWord("streaming keywords");

    newStreaming.setHomepageUrl("streaming URL");
    newStreaming.setProvider(StreamingProvidersRegistry.get().getByName("vimeo").orElse(null));

    assertThat(newStreaming.getId(), nullValue());
    String newId = Transaction.performInOne(
        () -> MediaDAO.saveMedia(OperationContext.fromUser(writerUser), newStreaming));
    assertThat(newStreaming.getId(), notNullValue());
    assertThat(newStreaming.getId(), is(newId));

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
      ITable mediaTable = actualDataSet.getTable("SC_Gallery_Media");
      assertThat(mediaTable.getRowCount(), is(MEDIA_ROW_COUNT + 1));
      ITable internalTable = actualDataSet.getTable("SC_Gallery_Internal");
      assertThat(internalTable.getRowCount(), is(MEDIA_INTERNAL_ROW_COUNT));
      ITable photoTable = actualDataSet.getTable("SC_Gallery_Photo");
      assertThat(photoTable.getRowCount(), is(MEDIA_PHOTO_ROW_COUNT));
      ITable videoTable = actualDataSet.getTable("SC_Gallery_Video");
      assertThat(videoTable.getRowCount(), is(MEDIA_VIDEO_ROW_COUNT));
      ITable soundTable = actualDataSet.getTable("SC_Gallery_Sound");
      assertThat(soundTable.getRowCount(), is(MEDIA_SOUND_ROW_COUNT));
      ITable streamingTable = actualDataSet.getTable("SC_Gallery_Streaming");
      assertThat(streamingTable.getRowCount(), is(MEDIA_STREAMING_ROW_COUNT + 1));
      ITable pathTable = actualDataSet.getTable("SC_Gallery_Path");
      assertThat(pathTable.getRowCount(), is(MEDIA_PATH_ROW_COUNT));


      TableRow mediaRow = getTableRowFor(mediaTable, "mediaId", newStreaming.getId());
      assertThat(mediaRow.getString("mediaId"), is(newId));
      assertThat(mediaRow.getString("mediaType"), is(MediaType.Streaming.name()));
      assertThat(mediaRow.getString("instanceId"), is(INSTANCE_A));
      assertThat(mediaRow.getString("title"), is("A streaming title"));
      assertThat(mediaRow.getString("description"), is(emptyString()));
      assertThat(mediaRow.getString("author"), is("A streaming author"));
      assertThat(mediaRow.getString("keyword"), is("streaming keywords"));
      assertThat(mediaRow.getLong("beginVisibilityDate"), is(DateUtil.MINIMUM_DATE.getTime()));
      assertThat(mediaRow.getLong("endVisibilityDate"), is(DateUtil.MAXIMUM_DATE.getTime()));
      assertThat(mediaRow.getDate("createDate"), greaterThanOrEqualTo(now));
      assertThat(mediaRow.getString("createdBy"), is(writerUser.getId()));
      assertThat(mediaRow.getDate("lastUpdateDate"), is(mediaRow.getDate("createDate")));
      assertThat(mediaRow.getString("lastUpdatedBy"), is(writerUser.getId()));

      TableRow iMediaRow = getTableRowFor(internalTable, "mediaId", newStreaming.getId());
      assertThat(iMediaRow, nullValue());

      TableRow streamingRow = getTableRowFor(streamingTable, "mediaId", newStreaming.getId());
      assertThat(streamingRow.getString("mediaId"), is(newId));
      assertThat(streamingRow.getString("homepageUrl"), is("streaming URL"));
      assertThat(streamingRow.getString("provider"), is("vimeo"));
    }

    // Updating to test (to verify update data in context of creation)
    awaitUntil(100, MILLISECONDS);
    Transaction.performInOne(
        () -> MediaDAO.saveMedia(OperationContext.fromUser(adminAccessUser), newStreaming));

    Date lastUpdateDate;

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
      ITable mediaTable = actualDataSet.getTable("SC_Gallery_Media");
      assertThat(mediaTable.getRowCount(), is(MEDIA_ROW_COUNT + 1));

      // Update date has changed because context of creation has not been set.
      TableRow mediaRow = getTableRowFor(mediaTable, "mediaId", newStreaming.getId());
      lastUpdateDate = mediaRow.getDate("lastUpdateDate");
      assertThat(mediaRow.getDate("createDate"), greaterThanOrEqualTo(now));
      assertThat(mediaRow.getString("createdBy"), is(writerUser.getId()));
      assertThat(lastUpdateDate, greaterThan(mediaRow.getDate("createDate")));
      assertThat(mediaRow.getString("lastUpdatedBy"), is(adminAccessUser.getId()));
    }

    awaitUntil(100, MILLISECONDS);
    Transaction.performInOne(() -> MediaDAO
        .saveMedia(OperationContext.fromUser(writerUser).setUpdatingInCaseOfCreation(),
            newStreaming));

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
      ITable mediaTable = actualDataSet.getTable("SC_Gallery_Media");
      assertThat(mediaTable.getRowCount(), is(MEDIA_ROW_COUNT + 1));

      // Update date has not changed because context of creation has been set.
      TableRow mediaRow = getTableRowFor(mediaTable, "mediaId", newStreaming.getId());
      assertThat(mediaRow.getDate("createDate"), greaterThanOrEqualTo(now));
      assertThat(mediaRow.getString("createdBy"), is(writerUser.getId()));
      assertThat(mediaRow.getDate("lastUpdateDate"), is(lastUpdateDate));
      assertThat(mediaRow.getString("lastUpdatedBy"), is(adminAccessUser.getId()));
    }
  }

  @SuppressWarnings("DataFlowIssue")
  @Test
  public void saveExistingStreaming() throws Exception {
    String mediaIdToUpdate = "stream_1";

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
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

      TableRow streamingRow = getTableRowFor(streamingTable, "mediaId", mediaIdToUpdate);
      assertThat(streamingRow.getString("mediaId"), is(mediaIdToUpdate));
      assertThat(streamingRow.getString("homepageUrl"), is("url_1"));
      assertThat(streamingRow.getString("provider"), is("yOuTuBE"));
    }

    Streaming streamingToUpdate = Transaction.performInOne(
        () -> MediaDAO.getByCriteria(defaultMediaCriteria().identifierIsOneOf(mediaIdToUpdate)))
        .getStreaming();

    streamingToUpdate.setHomepageUrl("streaming URL");
    streamingToUpdate.setProvider(StreamingProvidersRegistry.get().getByName("vimeo").orElse(null));

    String savedMediaId = streamingToUpdate.getId();
    String mediaId = Transaction.performInOne(
        () -> MediaDAO.saveMedia(OperationContext.fromUser(writerUser), streamingToUpdate));
    assertThat(mediaId, is(savedMediaId));
    assertThat(mediaId, is(streamingToUpdate.getId()));

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
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

      TableRow streamingRow = getTableRowFor(streamingTable, "mediaId", streamingToUpdate.getId());
      assertThat(streamingRow.getString("mediaId"), is(mediaIdToUpdate));
      assertThat(streamingRow.getString("homepageUrl"), is("streaming URL"));
      assertThat(streamingRow.getString("provider"), is("vimeo"));
    }
  }

  @Test
  public void deleteStreaming() throws Exception {

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
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
    }

    String mediaIdToDelete = "stream_2";

    Streaming streamingToDelete = Transaction.performInOne(
        () -> MediaDAO.getByCriteria(defaultMediaCriteria().identifierIsOneOf(mediaIdToDelete)))
        .getStreaming();

    Transaction.performInOne(() -> {
      MediaDAO.deleteMedia(streamingToDelete);
      return null;
    });

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
      ITable mediaTable = actualDataSet.getTable("SC_Gallery_Media");
      assertThat(mediaTable.getRowCount(), is(MEDIA_ROW_COUNT - 1));
      ITable internalTable = actualDataSet.getTable("SC_Gallery_Internal");
      assertThat(internalTable.getRowCount(), is(MEDIA_INTERNAL_ROW_COUNT));
      ITable photoTable = actualDataSet.getTable("SC_Gallery_Photo");
      assertThat(photoTable.getRowCount(), is(MEDIA_PHOTO_ROW_COUNT));
      ITable videoTable = actualDataSet.getTable("SC_Gallery_Video");
      assertThat(videoTable.getRowCount(), is(MEDIA_VIDEO_ROW_COUNT));
      ITable soundTable = actualDataSet.getTable("SC_Gallery_Sound");
      assertThat(soundTable.getRowCount(), is(MEDIA_SOUND_ROW_COUNT));
      ITable streamingTable = actualDataSet.getTable("SC_Gallery_Streaming");
      assertThat(streamingTable.getRowCount(), is(MEDIA_STREAMING_ROW_COUNT - 1));
      ITable pathTable = actualDataSet.getTable("SC_Gallery_Path");
      assertThat(pathTable.getRowCount(), is(MEDIA_PATH_ROW_COUNT - 1));
    }

    Transaction.performInOne(() -> {
      MediaDAO.deleteMedia(streamingToDelete);
      return null;
    });

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
      ITable mediaTable = actualDataSet.getTable("SC_Gallery_Media");
      assertThat(mediaTable.getRowCount(), is(MEDIA_ROW_COUNT - 1));
      ITable internalTable = actualDataSet.getTable("SC_Gallery_Internal");
      assertThat(internalTable.getRowCount(), is(MEDIA_INTERNAL_ROW_COUNT));
      ITable photoTable = actualDataSet.getTable("SC_Gallery_Photo");
      assertThat(photoTable.getRowCount(), is(MEDIA_PHOTO_ROW_COUNT));
      ITable videoTable = actualDataSet.getTable("SC_Gallery_Video");
      assertThat(videoTable.getRowCount(), is(MEDIA_VIDEO_ROW_COUNT));
      ITable soundTable = actualDataSet.getTable("SC_Gallery_Sound");
      assertThat(soundTable.getRowCount(), is(MEDIA_SOUND_ROW_COUNT));
      ITable streamingTable = actualDataSet.getTable("SC_Gallery_Streaming");
      assertThat(streamingTable.getRowCount(), is(MEDIA_STREAMING_ROW_COUNT - 1));
      ITable pathTable = actualDataSet.getTable("SC_Gallery_Path");
      assertThat(pathTable.getRowCount(), is(MEDIA_PATH_ROW_COUNT - 1));
    }
  }

  @SuppressWarnings("DataFlowIssue")
  @Test
  public void saveStreamingPath() throws Exception {

    String mediaIdToPerform = "stream_1";
    Streaming streamingToPerform;

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
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

      streamingToPerform =
          MediaDAO.getByCriteria(defaultMediaCriteria().identifierIsOneOf(mediaIdToPerform))
              .getStreaming();

      TableRow mediaPathRow = getTableRowFor(pathTable, "nodeId", "26");
      assertThat(mediaPathRow, nullValue());
    }

    Transaction.performInOne(() -> {
      MediaDAO.saveMediaPath(streamingToPerform, "26");
      return null;
    });

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
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
      assertThat(pathTable.getRowCount(), is(MEDIA_PATH_ROW_COUNT + 1));

      TableRow mediaPathRow = getTableRowFor(pathTable, "nodeId", 26);
      assertThat(mediaPathRow.getString("mediaId"), is(mediaIdToPerform));
      assertThat(mediaPathRow.getString("instanceId"), is(INSTANCE_A));
      assertThat(mediaPathRow.getInteger("nodeId"), is(26));
    }

    Transaction.performInOne(() -> {
      MediaDAO.saveMediaPath(streamingToPerform, "26");
      return null;
    });

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
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
      assertThat(pathTable.getRowCount(), is(MEDIA_PATH_ROW_COUNT + 1));
    }

    Transaction.performInOne(() -> {
      MediaDAO.saveMediaPath(streamingToPerform, "38");
      return null;
    });

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
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
      assertThat(pathTable.getRowCount(), is(MEDIA_PATH_ROW_COUNT + 2));

      TableRow mediaPathRow = getTableRowFor(pathTable, "nodeId", 26);
      assertThat(mediaPathRow.getString("mediaId"), is(mediaIdToPerform));
      assertThat(mediaPathRow.getString("instanceId"), is(INSTANCE_A));
      assertThat(mediaPathRow.getInteger("nodeId"), is(26));

      mediaPathRow = getTableRowFor(pathTable, "nodeId", 38);
      assertThat(mediaPathRow.getString("mediaId"), is(mediaIdToPerform));
      assertThat(mediaPathRow.getString("instanceId"), is(INSTANCE_A));
      assertThat(mediaPathRow.getInteger("nodeId"), is(38));
    }
  }

  @Test
  public void deleteAllVideoPath() throws Exception {

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
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
    }

    String mediaIdToPerform = "v_1";

    Video videoToPerform =
        MediaDAO.getByCriteria(defaultMediaCriteria().identifierIsOneOf(mediaIdToPerform))
            .getVideo();

    Transaction.performInOne(() -> {
      MediaDAO.deleteAllMediaPath(videoToPerform);
      return null;
    });

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
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
      assertThat(pathTable.getRowCount(), is(MEDIA_PATH_ROW_COUNT - 2));
    }

    Transaction.performInOne(() -> {
      MediaDAO.deleteAllMediaPath(videoToPerform);
      return null;
    });

    try (Connection connection = getConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
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
      assertThat(pathTable.getRowCount(), is(MEDIA_PATH_ROW_COUNT - 2));
    }
  }

  private MediaCriteria defaultMediaCriteria() {
    return MediaCriteria.fromComponentInstanceId(INSTANCE_A).referenceDateOf(TODAY);
  }

  private MediaCriteria mediaCriteriaFutureReferenceDate() {
    return MediaCriteria.fromComponentInstanceId(INSTANCE_A)
        .referenceDateOf(DateUtils.addYears(TODAY, 1));
  }

  public void assertMediaType(List<Media> media, MediaType expectedType,
      Class<? extends Media> expectedClass) {
    for (Media aMedia : media) {
      assertThat(aMedia.getType(), is(expectedType));
      assertThat(aMedia, instanceOf(expectedClass));
    }
  }

  public void assertMediaIdentifiers(List<Media> media, boolean ordered, String... ids) {
    List<String> identifiers = media.stream().map(Media::getId).collect(Collectors.toList());
    assertThat(identifiers, hasSize(ids.length));
    if (ordered) {
      assertThat(identifiers, contains(ids));
    } else {
      assertThat(identifiers, containsInAnyOrder(ids));
    }
  }
}
