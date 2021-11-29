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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.dao;

import org.silverpeas.components.gallery.constant.MediaMimeType;
import org.silverpeas.components.gallery.constant.MediaType;
import org.silverpeas.components.gallery.model.InternalMedia;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.MediaCriteria;
import org.silverpeas.components.gallery.model.MediaPK;
import org.silverpeas.components.gallery.model.MediaWithStatus;
import org.silverpeas.components.gallery.model.Photo;
import org.silverpeas.components.gallery.model.Sound;
import org.silverpeas.components.gallery.model.Streaming;
import org.silverpeas.components.gallery.model.Video;
import org.silverpeas.components.gallery.socialnetwork.SocialInformationGallery;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.io.media.Definition;
import org.silverpeas.core.media.streaming.StreamingProvider;
import org.silverpeas.core.media.streaming.StreamingProvidersRegistry;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQueries;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.persistence.jdbc.sql.ResultSetWrapper;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.silverpeas.core.persistence.jdbc.DBUtil.getUniqueId;
import static org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery.*;

public class MediaDAO {

  private static final String GALLERY_PATH_TABLE = "SC_Gallery_Path";

  private static final String SELECT_INTERNAL_MEDIA_PREFIX =
      "I.mediaId, I.fileName, I.fileSize, I.fileMimeType, I.download, I.beginDownloadDate," +
          " I.endDownloadDate, ";
  private static final String GALLERY_MEDIA_TABLE = "SC_Gallery_Media";
  private static final String GALLERY_PHOTO_TABLE = "SC_Gallery_Photo";
  private static final String GALLERY_VIDEO_TABLE = "SC_Gallery_Video";
  private static final String GALLERY_SOUND_TABLE = "SC_Gallery_Sound";
  private static final String GALLERY_STREAMING_TABLE = "SC_Gallery_Streaming";
  private static final String GALLERY_INTERNAL_TABLE = "SC_Gallery_Internal";
  private static final String MEDIA_ID_CRITERIA = "mediaId = ?";
  private static final String MEDIA_ID_PARAM = "mediaId";
  private static final String INSTANCE_ID_PARAM = "instanceId";

  private MediaDAO() {
  }

  /**
   * Gets the media behind the specified criteria.
   * @param criteria the media criteria.
   * @return the media behind the criteria, null if no media found and throws
   * {@link IllegalArgumentException} if several media are found.
   * @throws SQLException on SQL error
   * @throws IllegalArgumentException on bad argument
   */
  public static Media getByCriteria(final MediaCriteria criteria)
      throws SQLException {
    return unique(findByCriteria(criteria));
  }

  /**
   * Finds media according to the given criteria.
   * @param criteria the media criteria.
   * @return the media list corresponding to the given criteria.
   */
  public static List<Media> findByCriteria(final MediaCriteria criteria) throws SQLException {
    MediaSQLQueryBuilder queryBuilder = MediaSQLQueryBuilder.selectBuilder();
    criteria.processWith(queryBuilder);

    JdbcSqlQuery selectQuery = queryBuilder.result();

    final Map<String, Photo> photos = new HashMap<>();
    final Map<String, Video> videos = new HashMap<>();
    final Map<String, Sound> sounds = new HashMap<>();
    final Map<String, Streaming> streamings = new HashMap<>();

    List<Media> media = selectQuery.execute(row -> {
      String mediaId = row.getString(1);
      MediaType mediaType = MediaType.from(row.getString(2));
      String instanceId = row.getString(3);
      MediaInfo mediaInfo = new MediaInfo(mediaId, mediaType, instanceId);
      return fetchCurrentMedia(photos, videos, sounds, streamings, row, mediaInfo);
    });

    decoratePhotos(media, photos);
    decorateVideos(media, videos);
    decorateSounds(media, sounds);
    decorateStreamings(media, streamings);

    return queryBuilder.orderingResult(media);
  }

  private static Media fetchCurrentMedia(final Map<String, Photo> photos,
      final Map<String, Video> videos, final Map<String, Sound> sounds,
      final Map<String, Streaming> streaming, final ResultSetWrapper row, final MediaInfo mediaInfo)
      throws SQLException {
    final Media currentMedia;
    switch (mediaInfo.getMediaType()) {
      case Photo:
        currentMedia = new Photo();
        photos.put(mediaInfo.getMediaId(), (Photo) currentMedia);
        break;
      case Video:
        currentMedia = new Video();
        videos.put(mediaInfo.getMediaId(), (Video) currentMedia);
        break;
      case Sound:
        currentMedia = new Sound();
        sounds.put(mediaInfo.getMediaId(), (Sound) currentMedia);
        break;
      case Streaming:
        currentMedia = new Streaming();
        streaming.put(mediaInfo.getMediaId(), (Streaming) currentMedia);
        break;
      default:
        currentMedia = null;
    }
    if (currentMedia == null) {
      // Unknown media ...
      SilverLogger.getLogger(MediaDAO.class)
          .warn("Unknown media type {0}", mediaInfo.getMediaType());
      return null;
    }

    currentMedia.setMediaPK(new MediaPK(mediaInfo.getMediaId(), mediaInfo.getInstanceId()));
    currentMedia.setTitle(row.getString(4));
    currentMedia.setDescription(row.getString(5));
    currentMedia.setAuthor(row.getString(6));
    currentMedia.setKeyWord(row.getString(7));
    currentMedia.setVisibilityPeriod(
        Period.check(Period.from(new Date(row.getLong(8)), new Date(row.getLong(9)))));
    currentMedia.setCreationDate(row.getTimestamp(10));
    currentMedia.setCreatorId(row.getString(11));
    currentMedia.setLastUpdateDate(row.getTimestamp(12));
    currentMedia.setLastUpdatedBy(row.getString(13));
    return currentMedia;
  }

  /**
   * Adding all data of photos.
   * @param media the list of media that have not been yet decorated.
   * @param photos indexed photo media to decorate.
   * @throws SQLException on SQL error
   */
  private static void decoratePhotos(List<Media> media, Map<String, Photo> photos)
      throws SQLException {
    if (!photos.isEmpty()) {
      Collection<Collection<String>> idGroups =
          CollectionUtil.split(new ArrayList<>(photos.keySet()));
      String queryBase = SELECT_INTERNAL_MEDIA_PREFIX +
          "P.resolutionW, P.resolutionH from SC_Gallery_Internal I join SC_Gallery_Photo P on I" +
          ".mediaId = P.mediaId where I.mediaId";
      for (Collection<String> mediaIds : idGroups) {
        createSelect(queryBase).in(mediaIds).execute(row -> {
          String mediaId = row.getString(1);
          mediaIds.remove(mediaId);
          Photo currentPhoto = photos.get(mediaId);
          decorateInternalMedia(row, currentPhoto);
          currentPhoto.setDefinition(Definition.of(row.getInt(8), row.getInt(9)));
          return null;
        });
        // Not found
        for (String mediaIdNotFound : mediaIds) {
          Photo currentPhoto = photos.remove(mediaIdNotFound);
          media.remove(currentPhoto);
          SilverLogger.getLogger(MediaDAO.class)
              .warn("Photo not found (removed from result): {0}", mediaIdNotFound);
        }
      }
    }
  }

  /**
   * Adding all data of videos.
   * @param media the list of media that have not been yet decorated.
   * @param videos indexed video media to decorate.
   * @throws SQLException on SQL error
   */
  private static void decorateVideos(List<Media> media, Map<String, Video> videos)
      throws SQLException {
    if (!videos.isEmpty()) {
      Collection<Collection<String>> idGroups =
          CollectionUtil.split(new ArrayList<>(videos.keySet()));
      String queryBase = SELECT_INTERNAL_MEDIA_PREFIX +
          "V.resolutionW, V.resolutionH, V.bitrate, V.duration from SC_Gallery_Internal I join " +
          "SC_Gallery_Video V on I.mediaId = V.mediaId where I.mediaId";
      for (Collection<String> mediaIds : idGroups) {
        createSelect(queryBase).in(mediaIds).execute(row -> {
          String mediaId = row.getString(1);
          mediaIds.remove(mediaId);
          Video currentVideo = videos.get(mediaId);
          decorateInternalMedia(row, currentVideo);
          currentVideo.setDefinition(Definition.of(row.getInt(8), row.getInt(9)));
          currentVideo.setBitrate(row.getLong(10));
          currentVideo.setDuration(row.getLong(11));
          return null;
        });
        // Not found
        for (String mediaIdNotFound : mediaIds) {
          Video currentVideo = videos.remove(mediaIdNotFound);
          media.remove(currentVideo);
          SilverLogger.getLogger(MediaDAO.class)
              .warn("Video not found (removed from result): {0}", mediaIdNotFound);
        }
      }
    }
  }

  /**
   * Adding all data of sounds.
   * @param media the list of media that have not been yet decorated.
   * @param sounds indexed sound media to decorate.
   * @throws SQLException on SQL error
   */
  private static void decorateSounds(List<Media> media, Map<String, Sound> sounds)
      throws SQLException {
    if (!sounds.isEmpty()) {
      Collection<Collection<String>> idGroups =
          CollectionUtil.split(new ArrayList<>(sounds.keySet()));
      String queryBase = SELECT_INTERNAL_MEDIA_PREFIX +
          "S.bitrate, S.duration from SC_Gallery_Internal I join SC_Gallery_Sound S on I.mediaId " +
          "= S.mediaId where I.mediaId";
      for (Collection<String> mediaIds : idGroups) {
        createSelect(queryBase).in(mediaIds).execute(row -> {
          String mediaId = row.getString(1);
          mediaIds.remove(mediaId);
          Sound currentSound = sounds.get(mediaId);
          decorateInternalMedia(row, currentSound);
          currentSound.setBitrate(row.getLong(8));
          currentSound.setDuration(row.getLong(9));
          return null;
        });
        // Not found
        for (String mediaIdNotFound : mediaIds) {
          Sound currentSound = sounds.remove(mediaIdNotFound);
          media.remove(currentSound);
          SilverLogger.getLogger(MediaDAO.class)
              .warn("Sound not found (removed from result): {0}", mediaIdNotFound);
        }
      }
    }
  }

  /**
   * Adding all data of streamings.
   * @param media the list of media that have not been yet decorated.
   * @param streamings indexed streaming media to decorate.
   * @throws SQLException on SQL error
   */
  private static void decorateStreamings(List<Media> media, Map<String, Streaming> streamings)
      throws SQLException {
    if (!streamings.isEmpty()) {
      Collection<Collection<String>> idGroups =
          CollectionUtil.split(new ArrayList<>(streamings.keySet()));
      String queryBase =
          "S.mediaId, S.homepageUrl, S.provider from SC_Gallery_Streaming S where S.mediaId";
      for (Collection<String> mediaIds : idGroups) {
        createSelect(queryBase).in(mediaIds).execute(row -> {
          String mediaId = row.getString(1);
          mediaIds.remove(mediaId);
          Streaming currentStreaming = streamings.get(mediaId);
          currentStreaming.setHomepageUrl(row.getString(2));
          StreamingProvidersRegistry.get().getByName(row.getString(3))
              .ifPresent(currentStreaming::setProvider);
          return null;
        });
        // Not found
        for (String mediaIdNotFound : mediaIds) {
          Streaming currentStreaming = streamings.remove(mediaIdNotFound);
          media.remove(currentStreaming);
          SilverLogger.getLogger(MediaDAO.class)
              .warn("Streaming not found (removed from result): {0}", mediaIdNotFound);
        }
      }
    }
  }

  /**
   * Centralization of internal media decoration.
   * @param rsw the wrapper of the result set.
   * @param iMedia the internal media instance to decorate.
   */
  private static void decorateInternalMedia(ResultSetWrapper rsw, InternalMedia iMedia)
      throws SQLException {
    iMedia.setFileName(rsw.getString(2));
    iMedia.setFileSize(rsw.getLong(3));
    iMedia.setFileMimeType(MediaMimeType.fromMimeType(rsw.getString(4)));
    iMedia.setDownloadAuthorized(rsw.getInt(5) == 1);
    iMedia.setDownloadPeriod(getPeriod(rsw));
  }

  /**
   * Counts media according to the given criteria.
   * @param criteria the media criteria.
   * @return the number of media count corresponding to the given criteria.
   */
  public static long countByCriteria(final MediaCriteria criteria) throws SQLException {
    MediaSQLQueryBuilder queryBuilder = MediaSQLQueryBuilder.countBuilder();
    criteria.processWith(queryBuilder);

    JdbcSqlQuery selectQuery = queryBuilder.result();

    return selectQuery.executeUnique(row -> row.getLong(1));
  }

  /**
   * Gets a period from the specified result set.
   * @param rsw the wrapper of the result set.
   * @return the period guessed from the given indexes of start and end date information.
   * @throws SQLException on SQL data fetching
   */
  private static Period getPeriod(ResultSetWrapper rsw)
      throws SQLException {
    Date begin = rsw.getDateFromLong(6);
    if (begin == null) {
      begin = DateUtil.MINIMUM_DATE;
    }
    Date end = rsw.getDateFromLong(7);
    if (end == null) {
      end = DateUtil.MAXIMUM_DATE;
    }
    return Period.check(Period.from(begin, end));
  }

  /**
   * Saves (insert or update) a media.
   * @param context the context of save operation.
   * @param media the media to save.
   * @return the id of the saved media.
   * @throws SQLException on SQL error
   */
  public static String saveMedia(OperationContext context, Media media) throws SQLException {
    JdbcSqlQueries updateQueries = new JdbcSqlQueries();

    // The current Uuid
    String uuid = media.getId();

    boolean isInsert = !isSqlDefined(uuid) ||
        createCountFor(GALLERY_MEDIA_TABLE).where(MEDIA_ID_CRITERIA, uuid).execute() == 0;

    // A new ID
    if (isInsert) {
      uuid = getUniqueId();
      media.getMediaPK().setId(uuid);
    }

    // Media
    updateQueries.add(prepareSaveMedia(context, media, isInsert));

    // Photo
    if (media.getType().isPhoto()) {
      updateQueries.addAll(prepareSavePhoto(media.getPhoto(), isInsert));
    }
    // Video
    if (media.getType().isVideo()) {
      updateQueries.addAll(prepareSaveVideo(media.getVideo(), isInsert));
    }
    // Sound
    if (media.getType().isSound()) {
      updateQueries.addAll(prepareSaveSound(media.getSound(), isInsert));
    }
    // Streaming
    if (media.getType().isStreaming()) {
      updateQueries.add(prepareSaveStreaming(media.getStreaming(), isInsert));
    }

    // Execution of update queries
    updateQueries.execute();
    return uuid;
  }

  /**
   * Prepares query and parameters in order to save a photo.
   * @param photo the photo media to save.
   * @param isInsert true to indicate an insert context, false to indicated an update one.
   * @return the prepared query to save data at photo media level.
   */
  private static List<JdbcSqlQuery> prepareSavePhoto(Photo photo, boolean isInsert) {
    List<JdbcSqlQuery> updateQueries = new ArrayList<>();
    updateQueries.add(prepareSaveInternalMedia(photo, isInsert));
    final JdbcSqlQuery photoSave;
    if (isInsert) {
      photoSave = createInsertFor(GALLERY_PHOTO_TABLE);
      photoSave.addInsertParam(MEDIA_ID_PARAM, photo.getId());
    } else {
      photoSave = createUpdateFor(GALLERY_PHOTO_TABLE);
    }
    Definition definition = photo.getDefinition();
    photoSave.addSaveParam("resolutionW", definition.getWidth(), isInsert);
    photoSave.addSaveParam("resolutionH", definition.getHeight(), isInsert);
    if (!isInsert) {
      photoSave.where(MEDIA_ID_CRITERIA, photo.getId());
    }
    updateQueries.add(photoSave);
    return updateQueries;
  }

  /**
   * Prepares query and parameters in order to save a video.
   * @param video the video media to save.
   * @param isInsert true to indicate an insert context, false to indicated an update one.
   * @return the prepared query to save data at video media level.
   */
  private static List<JdbcSqlQuery> prepareSaveVideo(Video video, boolean isInsert) {
    List<JdbcSqlQuery> updateQueries = new ArrayList<>();
    updateQueries.add(prepareSaveInternalMedia(video, isInsert));
    final JdbcSqlQuery videoSave;
    if (isInsert) {
      videoSave = createInsertFor(GALLERY_VIDEO_TABLE);
      videoSave.addInsertParam(MEDIA_ID_PARAM, video.getId());
    } else {
      videoSave = createUpdateFor(GALLERY_VIDEO_TABLE);
    }
    Definition definition = video.getDefinition();
    videoSave.addSaveParam("resolutionW", definition.getWidth(), isInsert);
    videoSave.addSaveParam("resolutionH", definition.getHeight(), isInsert);
    videoSave.addSaveParam("bitrate", video.getBitrate(), isInsert);
    videoSave.addSaveParam("duration", video.getDuration(), isInsert);
    if (!isInsert) {
      videoSave.where(MEDIA_ID_CRITERIA, video.getId());
    }
    updateQueries.add(videoSave);
    return updateQueries;
  }

  /**
   * Prepares query and parameters in order to save a sound.
   * @param sound the sound media to save.
   * @param isInsert true to indicate an insert context, false to indicated an update one.
   * @return the prepared query to save data at sound media level.
   */
  private static List<JdbcSqlQuery> prepareSaveSound(Sound sound, boolean isInsert) {
    List<JdbcSqlQuery> updateQueries = new ArrayList<>();
    updateQueries.add(prepareSaveInternalMedia(sound, isInsert));
    final JdbcSqlQuery soundSave;
    if (isInsert) {
      soundSave = createInsertFor(GALLERY_SOUND_TABLE);
      soundSave.addInsertParam(MEDIA_ID_PARAM, sound.getId());
    } else {
      soundSave = createUpdateFor(GALLERY_SOUND_TABLE);
    }
    soundSave.addSaveParam("bitrate", sound.getBitrate(), isInsert);
    soundSave.addSaveParam("duration", sound.getDuration(), isInsert);
    if (!isInsert) {
      soundSave.where(MEDIA_ID_CRITERIA, sound.getId());
    }
    updateQueries.add(soundSave);
    return updateQueries;
  }

  /**
   * Prepares query and parameters in order to save a streaming.
   * @param streaming the streaming to save.
   * @param isInsert true to indicate an insert context, false to indicated an update one.
   * @return the prepared query to save data at streaming media level.
   */
  private static JdbcSqlQuery prepareSaveStreaming(Streaming streaming, boolean isInsert) {
    final JdbcSqlQuery streamingSave;
    if (isInsert) {
      streamingSave = createInsertFor(GALLERY_STREAMING_TABLE);
      streamingSave.addInsertParam(MEDIA_ID_PARAM, streaming.getId());
    } else {
      streamingSave = createUpdateFor(GALLERY_STREAMING_TABLE);
    }
    streamingSave.addSaveParam("homepageUrl", streaming.getHomepageUrl(), isInsert);
    streamingSave.addSaveParam("provider", streaming.getProvider()
        .map(StreamingProvider::getName)
        .orElseThrow(() -> new IllegalArgumentException("Provider MUST exists")), isInsert);
    if (!isInsert) {
      streamingSave.where(MEDIA_ID_CRITERIA, streaming.getId());
    }
    return streamingSave;
  }

  /**
   * Prepares query and parameters in order to save a media.
   * @param context the context of save operation.
   * @param media the media to save.
   * @param isInsert true to indicate an insert context, false to indicated an update one.
   * @return the prepared query to save data at common media level.
   */
  private static JdbcSqlQuery prepareSaveMedia(OperationContext context, Media media,
      boolean isInsert) {
    final JdbcSqlQuery mediaSave;
    if (isInsert) {
      mediaSave = createInsertFor(GALLERY_MEDIA_TABLE);
      mediaSave.addInsertParam(MEDIA_ID_PARAM, media.getId());
    } else {
      mediaSave = createUpdateFor(GALLERY_MEDIA_TABLE);
    }
    mediaSave.addSaveParam("mediaType", media.getType(), isInsert);
    mediaSave.addSaveParam(INSTANCE_ID_PARAM, media.getInstanceId(), isInsert);
    mediaSave.addSaveParam("title", media.getTitle(), isInsert);
    mediaSave.addSaveParam("description", media.getDescription(), isInsert);
    mediaSave.addSaveParam("author", media.getAuthor(), isInsert);
    mediaSave.addSaveParam("keyword", media.getKeyWord(), isInsert);
    mediaSave
        .addSaveParam("beginVisibilityDate", media.getVisibilityPeriod().getBeginDate().getTime(),
            isInsert);
    mediaSave.addSaveParam("endVisibilityDate", media.getVisibilityPeriod().getEndDate().getTime(),
        isInsert);
    Timestamp saveDate = new Timestamp(new Date().getTime());
    if (isInsert) {
      media.setCreationDate(saveDate);
      media.setCreator(context.getUser());
      media.setLastUpdateDate(saveDate);
      media.setLastUpdater(context.getUser());
      mediaSave.addInsertParam("createDate", media.getCreationDate());
      mediaSave.addInsertParam("createdBy", media.getCreatorId());
      mediaSave.addInsertParam("lastUpdateDate", media.getLastUpdateDate());
      mediaSave.addInsertParam("lastUpdatedBy", media.getLastUpdatedBy());
    } else if (!context.isUpdatingInCaseOfCreation()) {
      media.setLastUpdateDate(saveDate);
      media.setLastUpdater(context.getUser());
      mediaSave.addUpdateParam("lastUpdateDate", media.getLastUpdateDate());
      mediaSave.addUpdateParam("lastUpdatedBy", media.getLastUpdatedBy());
    }
    if (!isInsert) {
      mediaSave.where(MEDIA_ID_CRITERIA, media.getId());
    }
    return mediaSave;
  }

  /**
   * Prepares query and parameters in order to save a media.
   * @param iMedia the internal media to save.
   * @param isInsert true to indicate an insert context, false to indicated an update one.
   * @return the prepared query to save data at internal media level.
   */
  private static JdbcSqlQuery prepareSaveInternalMedia(InternalMedia iMedia, boolean isInsert) {
    final JdbcSqlQuery iMediaSave;
    if (isInsert) {
      iMediaSave = createInsertFor(GALLERY_INTERNAL_TABLE);
      iMediaSave.addInsertParam(MEDIA_ID_PARAM, iMedia.getId());
    } else {
      iMediaSave = createUpdateFor(GALLERY_INTERNAL_TABLE);
    }
    iMediaSave.addSaveParam("fileName", iMedia.getFileName(), isInsert);
    iMediaSave.addSaveParam("fileSize", iMedia.getFileSize(), isInsert);
    iMediaSave.addSaveParam("fileMimeType", iMedia.getFileMimeType().getMimeType(), isInsert);
    iMediaSave.addSaveParam("download", iMedia.isDownloadAuthorized() ? 1 : 0, isInsert);
    Long beginDate = iMedia.getDownloadPeriod().getBeginDatable().isDefined() ?
        iMedia.getDownloadPeriod().getBeginDate().getTime() : null;
    iMediaSave.addSaveParam("beginDownloadDate", beginDate, isInsert);
    Long endDate = iMedia.getDownloadPeriod().getEndDatable().isDefined() ?
        iMedia.getDownloadPeriod().getEndDate().getTime() : null;
    iMediaSave.addSaveParam("endDownloadDate", endDate, isInsert);
    if (!isInsert) {
      iMediaSave.where(MEDIA_ID_CRITERIA, iMedia.getId());
    }
    return iMediaSave;
  }

  /**
   * Deletes the specified media (and its album links).
   * @param media the media to delete.
   * @throws SQLException on SQL error
   */
  public static void deleteMedia(Media media) throws SQLException {
    String mediaId = media.getId();
    JdbcSqlQueries updateQueries = new JdbcSqlQueries();
    updateQueries.add(createDeleteFor(GALLERY_MEDIA_TABLE).where(MEDIA_ID_CRITERIA, mediaId));
    if (MediaType.Photo == media.getType() || MediaType.Video == media.getType() ||
        MediaType.Sound == media.getType()) {
      updateQueries.add(createDeleteFor(GALLERY_INTERNAL_TABLE).where(MEDIA_ID_CRITERIA, mediaId));
    }
    switch (media.getType()) {
      case Photo:
        updateQueries.add(createDeleteFor(GALLERY_PHOTO_TABLE).where(MEDIA_ID_CRITERIA, mediaId));
        break;
      case Video:
        updateQueries.add(createDeleteFor(GALLERY_VIDEO_TABLE).where(MEDIA_ID_CRITERIA, mediaId));
        break;
      case Sound:
        updateQueries.add(createDeleteFor(GALLERY_SOUND_TABLE).where(MEDIA_ID_CRITERIA, mediaId));
        break;
      case Streaming:
        updateQueries.add(createDeleteFor(GALLERY_STREAMING_TABLE).where(MEDIA_ID_CRITERIA, mediaId));
        break;
      default:
        SilverLogger.getLogger(MediaDAO.class).warn("Unknown media type: {0}", media.getId());
        break;
    }
    updateQueries.execute();
    deleteAllMediaPath(media);
  }

  /**
   * Saves the album link for the given media.
   * @param media the media that must be associated to the given album.
   * @param albumId the identifier of the album.
   * @throws SQLException on SQL error
   */
  public static void saveMediaPath(Media media, String albumId) throws SQLException {
    List<?> pathParams =
        Arrays.asList(media.getId(), media.getInstanceId(), Integer.valueOf(albumId));

    boolean isInsert = createCountFor(GALLERY_PATH_TABLE)
        .where("mediaId = ? and instanceId = ? and nodeId = ?", pathParams).execute() == 0;

    if (isInsert) {
      Iterator<?> paramIt = pathParams.iterator();
      JdbcSqlQuery insert = createInsertFor(GALLERY_PATH_TABLE);
      insert.addInsertParam(MEDIA_ID_PARAM, paramIt.next());
      insert.addInsertParam(INSTANCE_ID_PARAM, paramIt.next());
      insert.addInsertParam("nodeId", paramIt.next());
      insert.execute();
    }
  }

  /**
   * Deletes the album links of the specified media.
   * @param media the media for which all album links must be deleted.
   * @throws SQLException on SQL error
   */
  public static void deleteAllMediaPath(Media media) throws SQLException {
    createDeleteFor(GALLERY_PATH_TABLE)
        .where("mediaId = ? and instanceId = ?", media.getId(), media.getInstanceId()).execute();
  }

  /**
   * Gets the identifier list of albums which the given media is associated to.
   * @param media the media aimed.
   * @return the identifier list of albums in which the given media is attached to.
   * @throws SQLException on SQL error
   */
  public static Collection<String> getAlbumIdsOf(Media media) throws SQLException {
    return createSelect(
        "N.NodeId from SC_Gallery_Path P, SB_Node_Node N where P.mediaId = ? and N.nodeId " +
            "= P.NodeId and P.instanceId = ? and N.instanceId = P.instanceId", media.getId(),
        media.getInstanceId()).execute(row -> String.valueOf(row.getInt(1)));
  }

  /**
   * get my SocialInformationGallery according to the type of data base used (PostgresSQL,Oracle,
   * MMS).
   * @param userId the identifier of a user.
   * @param period the period on which the data are requested.
   * @return List<SocialInformation>
   * @throws SQLException on SQL error
   */
  public static List<SocialInformation> getAllMediaIdByUserId(String userId, Period period)
      throws SQLException {
    return create(
        "(select createDate AS dateinformation, mediaId, 'new' as type from SC_Gallery_Media " +
            "where createdBy = ? and createDate >= ? and createDate <= ? ) " +
            "union (select lastUpdateDate AS dateinformation, mediaId , " +
            "'update' as type from SC_Gallery_Media where lastUpdatedBy = ? and lastUpdateDate <>" +
            " createDate and lastUpdateDate >= ? and lastUpdateDate <= ? ) order by " +
            "dateinformation desc, mediaId desc", userId, period.getBeginDatable(),
        period.getEndDatable(), userId, period.getBeginDatable(), period.getEndDatable()).execute(row -> {
      Media media = getByCriteria(MediaCriteria.fromMediaId(row.getString(2))
          .withVisibility(MediaCriteria.VISIBILITY.FORCE_GET_ALL));
      MediaWithStatus withStatus =
          new MediaWithStatus(media, "update".equalsIgnoreCase(row.getString(3)));
      return new SocialInformationGallery(withStatus);
    });
  }

  /**
   * get list of socialInformationGallery of my contacts according to the type of data base
   * used(PostgresSQL,Oracle,MMS) .
   * @param userIds the identifiers of users.
   * @param availableComponents the list of available components.
   * @param period the period on which the data are requested.
   * @return the information for social data.
   * @throws SQLException on SQL error
   */
  public static List<SocialInformation> getSocialInformationListOfMyContacts(List<String> userIds,
      List<String> availableComponents, Period period) throws SQLException {
    JdbcSqlQuery query = create("(select createDate as dateinformation, mediaId, 'new' as type");
    query.addSqlPart("from SC_Gallery_Media where createdBy").in(userIds);
    query.and(INSTANCE_ID_PARAM).in(availableComponents);
    query.and("createDate >= ? and createDate <= ?)", period.getBeginDatable(),
        period.getEndDatable());
    query.addSqlPart("union (select lastUpdateDate as dateinformation, mediaId, 'update' as type");
    query.addSqlPart("from SC_Gallery_Media where lastUpdatedBy").in(userIds);
    query.and(INSTANCE_ID_PARAM).in(availableComponents);
    query.and("lastUpdateDate <> createDate");
    query.and("lastUpdateDate >= ? and lastUpdateDate <= ?)", period.getBeginDatable(),
        period.getEndDatable());
    query.addSqlPart("order by dateinformation desc, mediaId desc");

    return query.execute(row -> {
      Media media = getByCriteria(MediaCriteria.fromMediaId(row.getString(2))
          .withVisibility(MediaCriteria.VISIBILITY.FORCE_GET_ALL));
      MediaWithStatus withStatus =
          new MediaWithStatus(media, "update".equalsIgnoreCase(row.getString(3)));
      return new SocialInformationGallery(withStatus);
    });
  }

  private static class MediaInfo {
    private final String mediaId;
    private final MediaType mediaType;
    private final String instanceId;

    public MediaInfo(final String mediaId, final MediaType mediaType, final String instanceId) {
      this.mediaId = mediaId;
      this.mediaType = mediaType;
      this.instanceId = instanceId;
    }

    public String getMediaId() {
      return mediaId;
    }

    public MediaType getMediaType() {
      return mediaType;
    }

    public String getInstanceId() {
      return instanceId;
    }
  }
}
