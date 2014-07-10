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
package com.silverpeas.gallery.dao;

import com.silverpeas.gallery.GalleryComponentSettings;
import com.silverpeas.gallery.constant.MediaMimeType;
import com.silverpeas.gallery.constant.MediaType;
import com.silverpeas.gallery.constant.StreamingProvider;
import com.silverpeas.gallery.model.InternalMedia;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.model.MediaCriteria;
import com.silverpeas.gallery.model.MediaPK;
import com.silverpeas.gallery.model.MediaWithStatus;
import com.silverpeas.gallery.model.Photo;
import com.silverpeas.gallery.model.Sound;
import com.silverpeas.gallery.model.Streaming;
import com.silverpeas.gallery.model.Video;
import com.silverpeas.gallery.socialNetwork.SocialInformationGallery;
import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.util.CollectionUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.exception.UtilException;
import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.date.Period;
import org.silverpeas.persistence.repository.OperationContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stratelia.webactiv.util.DBUtil.*;

public class MediaDAO {

  private static final String SELECT_INTERNAL_MEDIA_PREFIX =
      "select I.mediaId, I.fileName, I.fileSize, I.fileMimeType, I.download, I.beginDownloadDate," +
          " I.endDownloadDate, ";

  /**
   * Gets the media behind the specified criteria.
   * @param con the database connection.
   * @param criteria the media criteria.
   * @return the media behind the criteria, null if no media found and throws
   * {@link IllegalArgumentException} if several media are found.
   * @throws SQLException
   * @throws IllegalArgumentException
   */
  public static Media getByCriteria(final Connection con, final MediaCriteria criteria)
      throws SQLException, IllegalArgumentException {
    return unique(findByCriteria(con, criteria));
  }

  /**
   * Finds media according to the given criteria.
   * @param con the database connection.
   * @param criteria the media criteria.
   * @return the media list corresponding to the given criteria.
   */
  public static List<Media> findByCriteria(final Connection con, final MediaCriteria criteria)
      throws SQLException {
    MediaSQLQueryBuilder queryBuilder = new MediaSQLQueryBuilder();
    criteria.processWith(queryBuilder);

    Pair<String, List<Object>> queryBuild = queryBuilder.result();

    final Map<String, Photo> photos = new HashMap<String, Photo>();
    final Map<String, Video> videos = new HashMap<String, Video>();
    final Map<String, Sound> sounds = new HashMap<String, Sound>();
    final Map<String, Streaming> streamings = new HashMap<String, Streaming>();

    List<Media> media = select(con, queryBuild.getLeft(), queryBuild.getRight(),
        new SelectResultRowProcessor<Media>(criteria.getResultLimit()) {
          @Override
          protected Media currentRow(final int rowIndex, final ResultSet rs) throws SQLException {
            String mediaId = rs.getString(1);
            MediaType mediaType = MediaType.from(rs.getString(2));
            String instanceId = rs.getString(3);
            final Media currentMedia;
            switch (mediaType) {
              case Photo:
                currentMedia = new Photo();
                photos.put(mediaId, (Photo) currentMedia);
                break;
              case Video:
                currentMedia = new Video();
                videos.put(mediaId, (Video) currentMedia);
                break;
              case Sound:
                currentMedia = new Sound();
                sounds.put(mediaId, (Sound) currentMedia);
                break;
              case Streaming:
                currentMedia = new Streaming();
                streamings.put(mediaId, (Streaming) currentMedia);
                break;
              default:
                currentMedia = null;
            }
            if (currentMedia == null) {
              // Unknown media ...
              SilverTrace.warn(GalleryComponentSettings.COMPONENT_NAME,
                  "MediaDAO.findByCriteria()",
                  "root.MSG_GEN_PARAM_VALUE", "unknown media type: " + mediaType);
              return null;
            }

            currentMedia.setMediaPK(new MediaPK(mediaId, instanceId));
            currentMedia.setTitle(rs.getString(4));
            currentMedia.setDescription(rs.getString(5));
            currentMedia.setAuthor(rs.getString(6));
            currentMedia.setKeyWord(rs.getString(7));
            currentMedia.setVisibilityPeriod(
                Period.check(Period.from(new Date(rs.getLong(8)), new Date(rs.getLong(9)))));
            currentMedia.setCreationDate(rs.getTimestamp(10));
            currentMedia.setCreatorId(rs.getString(11));
            currentMedia.setLastUpdateDate(rs.getTimestamp(12));
            currentMedia.setLastUpdatedBy(rs.getString(13));
            return currentMedia;
          }
        });

    decoratePhotos(con, media, photos);
    decorateVideos(con, media, videos);
    decorateSounds(con, media, sounds);
    decorateStreamings(con, media, streamings);

    return queryBuilder.orderingResult(media);
  }

  /**
   * Adding all data of photos.
   * @param con
   * @param media
   * @param photos
   * @throws SQLException
   */
  private static void decoratePhotos(final Connection con, List<Media> media,
      Map<String, Photo> photos) throws SQLException {
    if (!photos.isEmpty()) {
      Collection<Collection<String>> idGroups =
          CollectionUtil.split(new ArrayList<String>(photos.keySet()));
      StringBuilder queryBase = new StringBuilder(SELECT_INTERNAL_MEDIA_PREFIX).append(
          "P.resolutionH, P.resolutionW from SC_Gallery_Internal I join SC_Gallery_Photo P on I" +
              ".mediaId = P.mediaId where I.mediaId in ");
      for (Collection<String> mediaIds : idGroups) {
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
          prepStmt =
              con.prepareStatement(DBUtil.appendListOfParameters(queryBase, mediaIds).toString());
          DBUtil.setParameters(prepStmt, mediaIds);
          rs = prepStmt.executeQuery();
          while (rs.next()) {
            String mediaId = rs.getString(1);
            mediaIds.remove(mediaId);
            Photo currentPhoto = photos.get(mediaId);
            decorateInternalMedia(rs, currentPhoto);
            currentPhoto.setResolutionH(rs.getInt(8));
            currentPhoto.setResolutionW(rs.getInt(9));
          }
        } finally {
          DBUtil.close(rs, prepStmt);
        }
        // Not found
        for (String mediaIdNotFound : mediaIds) {
          Photo currentPhoto = photos.remove(mediaIdNotFound);
          media.remove(currentPhoto);
          SilverTrace.warn(GalleryComponentSettings.COMPONENT_NAME, "MediaDAO.decoratePhotos()",
              "root.MSG_GEN_PARAM_VALUE",
              "photo not found (removed from result): " + mediaIdNotFound);
        }
      }
    }
  }

  /**
   * Adding all data of videos.
   * @param con
   * @param media
   * @param videos
   * @throws SQLException
   */
  private static void decorateVideos(final Connection con, List<Media> media,
      Map<String, Video> videos) throws SQLException {
    if (!videos.isEmpty()) {
      Collection<Collection<String>> idGroups =
          CollectionUtil.split(new ArrayList<String>(videos.keySet()));
      StringBuilder queryBase = new StringBuilder(SELECT_INTERNAL_MEDIA_PREFIX).append(
          "V.resolutionH, V.resolutionW, V.bitrate, V.duration from SC_Gallery_Internal I join " +
              "SC_Gallery_Video V on I.mediaId = V.mediaId where I.mediaId in ");
      for (Collection<String> mediaIds : idGroups) {
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
          prepStmt =
              con.prepareStatement(DBUtil.appendListOfParameters(queryBase, mediaIds).toString());
          DBUtil.setParameters(prepStmt, mediaIds);
          rs = prepStmt.executeQuery();
          while (rs.next()) {
            String mediaId = rs.getString(1);
            mediaIds.remove(mediaId);
            Video currentVideo = videos.get(mediaId);
            decorateInternalMedia(rs, currentVideo);
            currentVideo.setResolutionH(rs.getInt(8));
            currentVideo.setResolutionW(rs.getInt(9));
            currentVideo.setBitrate(rs.getLong(10));
            currentVideo.setDuration(rs.getLong(11));
          }
        } finally {
          DBUtil.close(rs, prepStmt);
        }
        // Not found
        for (String mediaIdNotFound : mediaIds) {
          Video currentVideo = videos.remove(mediaIdNotFound);
          media.remove(currentVideo);
          SilverTrace.warn(GalleryComponentSettings.COMPONENT_NAME, "MediaDAO.decorateVideos()",
              "root.MSG_GEN_PARAM_VALUE",
              "video not found (removed from result): " + mediaIdNotFound);
        }
      }
    }
  }

  /**
   * Adding all data of sounds.
   * @param con
   * @param media
   * @param sounds
   * @throws SQLException
   */
  private static void decorateSounds(final Connection con, List<Media> media,
      Map<String, Sound> sounds) throws SQLException {
    if (!sounds.isEmpty()) {
      Collection<Collection<String>> idGroups =
          CollectionUtil.split(new ArrayList<String>(sounds.keySet()));
      StringBuilder queryBase = new StringBuilder(SELECT_INTERNAL_MEDIA_PREFIX).append(
          "S.bitrate, S.duration from SC_Gallery_Internal I join SC_Gallery_Sound S on I.mediaId " +
              "= S.mediaId where I.mediaId in ");
      for (Collection<String> mediaIds : idGroups) {
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
          prepStmt =
              con.prepareStatement(DBUtil.appendListOfParameters(queryBase, mediaIds).toString());
          DBUtil.setParameters(prepStmt, mediaIds);
          rs = prepStmt.executeQuery();
          while (rs.next()) {
            String mediaId = rs.getString(1);
            mediaIds.remove(mediaId);
            Sound currentSound = sounds.get(mediaId);
            decorateInternalMedia(rs, currentSound);
            currentSound.setBitrate(rs.getLong(8));
            currentSound.setDuration(rs.getLong(9));
          }
        } finally {
          DBUtil.close(rs, prepStmt);
        }
        // Not found
        for (String mediaIdNotFound : mediaIds) {
          Sound currentSound = sounds.remove(mediaIdNotFound);
          media.remove(currentSound);
          SilverTrace.warn(GalleryComponentSettings.COMPONENT_NAME, "MediaDAO.decorateSounds()",
              "root.MSG_GEN_PARAM_VALUE",
              "sound not found (removed from result): " + mediaIdNotFound);
        }
      }
    }
  }

  /**
   * Adding all data of streamings.
   * @param con
   * @param media
   * @param streamings
   * @throws SQLException
   */
  private static void decorateStreamings(final Connection con, List<Media> media,
      Map<String, Streaming> streamings) throws SQLException {
    if (!streamings.isEmpty()) {
      Collection<Collection<String>> idGroups =
          CollectionUtil.split(new ArrayList<String>(streamings.keySet()));
      StringBuilder queryBase = new StringBuilder(
          "select S.mediaId, S.homepageUrl, S.provider from SC_Gallery_Streaming S where S" +
              ".mediaId in ");
      for (Collection<String> mediaIds : idGroups) {
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
          prepStmt =
              con.prepareStatement(DBUtil.appendListOfParameters(queryBase, mediaIds).toString());
          DBUtil.setParameters(prepStmt, mediaIds);
          rs = prepStmt.executeQuery();
          while (rs.next()) {
            String mediaId = rs.getString(1);
            mediaIds.remove(mediaId);
            Streaming currentStreaming = streamings.get(mediaId);
            currentStreaming.setHomepageUrl(rs.getString(2));
            currentStreaming.setProvider(StreamingProvider.from(rs.getString(3)));
          }
        } finally {
          DBUtil.close(rs, prepStmt);
        }
        // Not found
        for (String mediaIdNotFound : mediaIds) {
          Streaming currentStreaming = streamings.remove(mediaIdNotFound);
          media.remove(currentStreaming);
          SilverTrace.warn(GalleryComponentSettings.COMPONENT_NAME,
              "MediaDAO.decorateStreamings()",
              "root.MSG_GEN_PARAM_VALUE",
              "streaming not found (removed from result): " + mediaIdNotFound);
        }
      }
    }
  }

  /**
   * Centralization of internal media decoration.
   * @param rs
   * @param iMedia
   */
  private static void decorateInternalMedia(ResultSet rs, InternalMedia iMedia)
      throws SQLException {
    iMedia.setFileName(rs.getString(2));
    iMedia.setFileSize(rs.getLong(3));
    iMedia.setFileMimeType(MediaMimeType.fromMimeType(rs.getString(4)));
    iMedia.setDownloadAuthorized(rs.getInt(5) == 1);
    iMedia.setDownloadPeriod(getPeriod(rs, 6, 7));
  }

  /**
   * Gets a period.
   * @param rs
   * @param indexBegin
   * @param indexEnd
   * @return
   * @throws SQLException
   */
  private static Period getPeriod(ResultSet rs, int indexBegin, int indexEnd) throws SQLException {
    Date begin = DBUtil.getDateFromLong(rs, indexBegin);
    if (begin == null) {
      begin = DateUtil.MINIMUM_DATE;
    }
    Date end = DBUtil.getDateFromLong(rs, indexEnd);
    if (end == null) {
      end = DateUtil.MAXIMUM_DATE;
    }
    return Period.check(Period.from(begin, end));
  }

  /**
   * Saves (insert or update) a media.
   * @param con
   * @param context
   * @param media
   * @return the id of the saved media.
   * @throws SQLException
   * @throws UtilException
   */
  public static String saveMedia(Connection con, OperationContext context, Media media)
      throws SQLException, UtilException {
    List<Pair<String, List<Object>>> updateQueries = new ArrayList<Pair<String, List<Object>>>();

    // The current Uuid
    String uuid = media.getId();

    boolean isInsert = !isSqlDefined(uuid) ||
        selectCount(con, "select count(*) from SC_Gallery_Media where mediaId = ?", uuid) == 0;

    // A new ID
    if (isInsert) {
      uuid = getUniqueId();
      media.getMediaPK().setId(uuid);
    }

    // Media
    updateQueries.add(prepareSaveMedia(context, media, isInsert));

    // Photo
    if (media.getType().isPhoto()) {
      updateQueries.addAll(prepareSavePhoto(context, media.getPhoto(), isInsert));
    }
    // Video
    if (media.getType().isVideo()) {
      updateQueries.addAll(prepareSaveVideo(context, media.getVideo(), isInsert));
    }
    // Sound
    if (media.getType().isSound()) {
      updateQueries.addAll(prepareSaveSound(context, media.getSound(), isInsert));
    }
    // Streaming
    if (media.getType().isStreaming()) {
      updateQueries.add(prepareSaveStreaming(media.getStreaming(), isInsert));
    }

    // Execution of update queries
    executeUpdate(con, updateQueries);
    return uuid;
  }

  /**
   * Prepares query and parameters in order to save a photo.
   * @param context
   * @param photo
   * @param isInsert
   * @return
   */
  private static List<Pair<String, List<Object>>> prepareSavePhoto(OperationContext context,
      Photo photo, boolean isInsert) {
    List<Pair<String, List<Object>>> updateQueries = new ArrayList<Pair<String, List<Object>>>();
    updateQueries.add(prepareSaveInternalMedia(photo, isInsert));
    StringBuilder photoSave = new StringBuilder();
    List<Object> photoParams = new ArrayList<Object>();
    if (isInsert) {
      photoSave.append("insert into SC_Gallery_Photo (");
      appendSaveParameter(photoSave, "mediaId", photo.getId(), true, photoParams);
    } else {
      photoSave.append("update SC_Gallery_Photo set ");
    }
    appendSaveParameter(photoSave, "resolutionH", photo.getResolutionH(), isInsert, photoParams);
    appendSaveParameter(photoSave, "resolutionW", photo.getResolutionW(), isInsert, photoParams);
    if (isInsert) {
      appendListOfParameters(photoSave.append(") values "), photoParams);
    } else {
      appendParameter(photoSave, " where mediaId = ?", photo.getId(), photoParams);
    }
    updateQueries.add(Pair.of(photoSave.toString(), photoParams));
    return updateQueries;
  }

  /**
   * Prepares query and parameters in order to save a video.
   * @param context
   * @param video
   * @param isInsert
   * @return
   */
  private static List<Pair<String, List<Object>>> prepareSaveVideo(OperationContext context,
      Video video, boolean isInsert) {
    List<Pair<String, List<Object>>> updateQueries = new ArrayList<Pair<String, List<Object>>>();
    updateQueries.add(prepareSaveInternalMedia(video, isInsert));
    StringBuilder videoSave = new StringBuilder();
    List<Object> videoParams = new ArrayList<Object>();
    if (isInsert) {
      videoSave.append("insert into SC_Gallery_Video (");
      appendSaveParameter(videoSave, "mediaId", video.getId(), true, videoParams);
    } else {
      videoSave.append("update SC_Gallery_Video set ");
    }
    appendSaveParameter(videoSave, "resolutionH", video.getResolutionH(), isInsert, videoParams);
    appendSaveParameter(videoSave, "resolutionW", video.getResolutionW(), isInsert, videoParams);
    appendSaveParameter(videoSave, "bitrate", video.getBitrate(), isInsert, videoParams);
    appendSaveParameter(videoSave, "duration", video.getDuration(), isInsert, videoParams);
    if (isInsert) {
      appendListOfParameters(videoSave.append(") values "), videoParams);
    } else {
      appendParameter(videoSave, " where mediaId = ?", video.getId(), videoParams);
    }
    updateQueries.add(Pair.of(videoSave.toString(), videoParams));
    return updateQueries;
  }

  /**
   * Prepares query and parameters in order to save a sound.
   * @param context
   * @param sound
   * @param isInsert
   * @return
   */
  private static List<Pair<String, List<Object>>> prepareSaveSound(OperationContext context,
      Sound sound, boolean isInsert) {
    List<Pair<String, List<Object>>> updateQueries = new ArrayList<Pair<String, List<Object>>>();
    updateQueries.add(prepareSaveInternalMedia(sound, isInsert));
    StringBuilder soundSave = new StringBuilder();
    List<Object> soundParams = new ArrayList<Object>();
    if (isInsert) {
      soundSave.append("insert into SC_Gallery_Sound (");
      appendSaveParameter(soundSave, "mediaId", sound.getId(), true, soundParams);
    } else {
      soundSave.append("update SC_Gallery_Sound set ");
    }
    appendSaveParameter(soundSave, "bitrate", sound.getBitrate(), isInsert, soundParams);
    appendSaveParameter(soundSave, "duration", sound.getDuration(), isInsert, soundParams);
    if (isInsert) {
      appendListOfParameters(soundSave.append(") values "), soundParams);
    } else {
      appendParameter(soundSave, " where mediaId = ?", sound.getId(), soundParams);
    }
    updateQueries.add(Pair.of(soundSave.toString(), soundParams));
    return updateQueries;
  }

  /**
   * Prepares query and parameters in order to save a streaming.
   * @param streaming
   * @param isInsert
   * @return
   */
  private static Pair<String, List<Object>> prepareSaveStreaming(Streaming streaming,
      boolean isInsert) {
    StringBuilder streamingSave = new StringBuilder();
    List<Object> streamingParams = new ArrayList<Object>();
    if (isInsert) {
      streamingSave.append("insert into SC_Gallery_Streaming (");
      appendSaveParameter(streamingSave, "mediaId", streaming.getId(), true, streamingParams);
    } else {
      streamingSave.append("update SC_Gallery_Streaming set ");
    }
    appendSaveParameter(streamingSave, "homepageUrl", streaming.getHomepageUrl(), isInsert,
        streamingParams);
    appendSaveParameter(streamingSave, "provider", streaming.getProvider(), isInsert,
        streamingParams);
    if (isInsert) {
      appendListOfParameters(streamingSave.append(") values "), streamingParams);
    } else {
      appendParameter(streamingSave, " where mediaId = ?", streaming.getId(), streamingParams);
    }
    return Pair.of(streamingSave.toString(), streamingParams);
  }

  /**
   * Prepares query and parameters in order to save a media.
   * @param context
   * @param media
   * @param isInsert
   * @return
   */
  private static Pair<String, List<Object>> prepareSaveMedia(OperationContext context, Media media,
      boolean isInsert) {
    StringBuilder mediaSave = new StringBuilder();
    List<Object> mediaParams = new ArrayList<Object>();
    if (isInsert) {
      mediaSave.append("insert into SC_Gallery_Media (");
      appendSaveParameter(mediaSave, "mediaId", media.getId(), true, mediaParams);
    } else {
      mediaSave.append("update SC_Gallery_Media set ");
    }
    appendSaveParameter(mediaSave, "mediaType", media.getType(), isInsert, mediaParams);
    appendSaveParameter(mediaSave, "instanceId", media.getComponentInstanceId(), isInsert,
        mediaParams);
    appendSaveParameter(mediaSave, "title", media.getTitle(), isInsert, mediaParams);
    appendSaveParameter(mediaSave, "description", media.getDescription(), isInsert, mediaParams);
    appendSaveParameter(mediaSave, "author", media.getAuthor(), isInsert, mediaParams);
    appendSaveParameter(mediaSave, "keyword", media.getKeyWord(), isInsert, mediaParams);
    appendSaveParameter(mediaSave, "beginVisibilityDate",
        media.getVisibilityPeriod().getBeginDate().getTime(), isInsert, mediaParams);
    appendSaveParameter(mediaSave, "endVisibilityDate",
        media.getVisibilityPeriod().getEndDate().getTime(), isInsert, mediaParams);
    Timestamp saveDate = new Timestamp(new Date().getTime());
    if (isInsert) {
      media.setCreationDate(saveDate);
      media.setCreator(context.getUser());
      media.setLastUpdateDate(saveDate);
      media.setLastUpdater(context.getUser());
      appendSaveParameter(mediaSave, "createDate", media.getCreationDate(), true, mediaParams);
      appendSaveParameter(mediaSave, "createdBy", media.getCreatorId(), true, mediaParams);
      appendSaveParameter(mediaSave, "lastUpdateDate", media.getLastUpdateDate(), true, mediaParams);
      appendSaveParameter(mediaSave, "lastUpdatedBy", media.getLastUpdatedBy(), true, mediaParams);
    } else if (!context.isUpdatingInCaseOfCreation()) {
      media.setLastUpdateDate(saveDate);
      media.setLastUpdater(context.getUser());
      appendSaveParameter(mediaSave, "lastUpdateDate", media.getLastUpdateDate(), false,
          mediaParams);
      appendSaveParameter(mediaSave, "lastUpdatedBy", media.getLastUpdatedBy(), false, mediaParams);
    }
    if (isInsert) {
      appendListOfParameters(mediaSave.append(") values "), mediaParams);
    } else {
      appendParameter(mediaSave, " where mediaId = ?", media.getId(), mediaParams);
    }
    return Pair.of(mediaSave.toString(), mediaParams);
  }

  /**
   * Prepares query and parameters in order to save a media.
   * @param iMedia
   * @param isInsert
   * @return
   */
  private static Pair<String, List<Object>> prepareSaveInternalMedia(InternalMedia iMedia,
      boolean isInsert) {
    StringBuilder iMediaSave = new StringBuilder();
    List<Object> iMediaParams = new ArrayList<Object>();
    if (isInsert) {
      iMediaSave.append("insert into SC_Gallery_Internal (");
      appendSaveParameter(iMediaSave, "mediaId", iMedia.getId(), true, iMediaParams);
    } else {
      iMediaSave.append("update SC_Gallery_Internal set ");
    }
    appendSaveParameter(iMediaSave, "fileName", iMedia.getFileName(), isInsert, iMediaParams);
    appendSaveParameter(iMediaSave, "fileSize", iMedia.getFileSize(), isInsert, iMediaParams);
    appendSaveParameter(iMediaSave, "fileMimeType", iMedia.getFileMimeType().getMimeType(),
        isInsert, iMediaParams);
    appendSaveParameter(iMediaSave, "download", iMedia.isDownloadAuthorized() ? 1 : 0, isInsert,
        iMediaParams);
    Long beginDate = iMedia.getDownloadPeriod().getBeginDatable().isDefined() ?
        iMedia.getDownloadPeriod().getBeginDate().getTime() : null;
    appendSaveParameter(iMediaSave, "beginDownloadDate", beginDate, isInsert, iMediaParams);
    Long endDate = iMedia.getDownloadPeriod().getEndDatable().isDefined() ?
        iMedia.getDownloadPeriod().getEndDate().getTime() : null;
    appendSaveParameter(iMediaSave, "endDownloadDate", endDate, isInsert, iMediaParams);
    if (isInsert) {
      appendListOfParameters(iMediaSave.append(") values "), iMediaParams);
    } else {
      appendParameter(iMediaSave, " where mediaId = ?", iMedia.getId(), iMediaParams);
    }
    return Pair.of(iMediaSave.toString(), iMediaParams);
  }

  /**
   * Deletes the specified media (and its paths).
   * @param con
   * @param media
   * @throws SQLException
   */
  public static void deleteMedia(Connection con, Media media) throws SQLException {
    List<Object> mediaIdParam = Arrays.asList((Object) media.getId());
    List<Pair<String, List<Object>>> updateQueries = new ArrayList<Pair<String, List<Object>>>();
    updateQueries.add(Pair.of("delete from SC_Gallery_Media where mediaId = ?", mediaIdParam));
    if (MediaType.Photo == media.getType() || MediaType.Video == media.getType() ||
        MediaType.Sound == media.getType()) {
      updateQueries.add(Pair.of("delete from SC_Gallery_Internal where mediaId = ?", mediaIdParam));
    }
    switch (media.getType()) {
      case Photo:
        updateQueries.add(Pair.of("delete from SC_Gallery_Photo where mediaId = ?", mediaIdParam));
        break;
      case Video:
        updateQueries.add(Pair.of("delete from SC_Gallery_Video where mediaId = ?", mediaIdParam));
        break;
      case Sound:
        updateQueries.add(Pair.of("delete from SC_Gallery_Sound where mediaId = ?", mediaIdParam));
        break;
      case Streaming:
        updateQueries
            .add(Pair.of("delete from SC_Gallery_Streaming where mediaId = ?", mediaIdParam));
        break;
      default:
        SilverTrace
            .warn(GalleryComponentSettings.COMPONENT_NAME, "MediaDAO.deleteMedia",
                "Unknown media type to delete id=" + media.getId());
        break;
    }
    executeUpdate(con, updateQueries);
    deleteAllMediaPath(con, media);
  }

  /**
   * Saves the path of the media.
   * @param con
   * @param media
   * @param albumId
   * @throws SQLException
   * @throws UtilException
   */
  public static void saveMediaPath(Connection con, Media media, String albumId)
      throws SQLException, UtilException {
    List<?> pathParams =
        Arrays.asList(media.getId(), media.getInstanceId(), Integer.valueOf(albumId));

    boolean isInsert = selectCount(con,
        "select count(*) from SC_Gallery_Path where mediaId = ? and instanceId = ? and nodeId" +
            " = ?", pathParams) == 0;

    if (isInsert) {
      executeUpdate(con,
          "insert into SC_Gallery_Path (mediaId, instanceId, nodeId) values (?,?,?)",
          pathParams);
    }
  }

  /**
   * Deletes the paths of the specified media.
   * @param con
   * @param media
   * @throws SQLException
   */
  public static void deleteAllMediaPath(Connection con, Media media) throws SQLException {
    executeUpdate(con, "delete from SC_Gallery_Path where mediaId = ? and instanceId = ?",
        Arrays.asList(media.getId(), media.getInstanceId()));
  }

  /**
   * Gets the paths of a media.
   * @param con
   * @param media
   * @return
   * @throws SQLException
   */
  public static Collection<String> getAlbumIdsOf(Connection con, Media media) throws SQLException {
    return select(con,
        "select N.NodeId from SC_Gallery_Path P, SB_Node_Node N where P.mediaId = ? and N.nodeId " +
            "= P.NodeId and P.instanceId = ? and N.instanceId = P.instanceId",
        Arrays.asList(media.getId(), media.getInstanceId()),
        new SelectResultRowProcessor<String>() {

          @Override
          protected String currentRow(final int rowIndex, final ResultSet rs) throws SQLException {
            return String.valueOf(rs.getInt(1));
          }
        });
  }

  /**
   * get my SocialInformationGallery according to the type of data base used(PostgreSQL,Oracle,MMS)
   * .
   * @param con
   * @param userId
   * @param period
   * @return List<SocialInformation>
   * @throws SQLException
   * @throws java.text.ParseException
   */
  public static List<SocialInformation> getAllMediaIdByUserId(final Connection con, String userId,
      Period period) throws SQLException {
    return select(con,
        "(select createDate AS dateinformation, mediaId, 'new' as type from SC_Gallery_Media " +
            "where createdBy = ? and createDate >= ? and createDate <= ? ) " +
            "union (select lastUpdateDate AS dateinformation, mediaId , " +
            "'update' as type from SC_Gallery_Media where lastUpdatedBy = ? and lastUpdateDate <>" +
            " createDate and lastUpdateDate >= ? and lastUpdateDate <= ? ) order by " +
            "dateinformation desc, mediaId desc", Arrays
            .asList(userId, period.getBeginDate(), period.getEndDate(), userId,
                period.getBeginDate(), period.getEndDate()),
        new SelectResultRowProcessor<SocialInformation>() {

          @Override
          protected SocialInformation currentRow(final int rowIndex, final ResultSet rs)
              throws SQLException {
            Media media = getByCriteria(con, MediaCriteria.fromMediaId(rs.getString(2))
                .withVisibility(MediaCriteria.VISIBILITY.FORCE_GET_ALL));
            MediaWithStatus withStatus =
                new MediaWithStatus(media, "update".equalsIgnoreCase(rs.getString(3)));
            return new SocialInformationGallery(withStatus);
          }
        });
  }

  /**
   * get list of socialInformationGallery of my contacts according to the type of data base
   * used(PostgreSQL,Oracle,MMS) .
   * @param con
   * @param userIds
   * @param availableComponents
   * @param period
   * @return
   * @throws SQLException
   * @throws java.text.ParseException
   */
  public static List<SocialInformation> getSocialInformationListOfMyContacts(final Connection con,
      List<String> userIds, List<String> availableComponents, Period period) throws SQLException {
    List<Object> params = new ArrayList<Object>();
    StringBuilder query =
        new StringBuilder("(select createDate AS dateinformation, mediaId, 'new' as type ");
    query.append("from SC_Gallery_Media where createdBy in ");
    appendListOfParameters(query, userIds, params);
    query.append(" and instanceId in ");
    appendListOfParameters(query, availableComponents, params);
    query.append(" AND createDate >= ? AND createDate <= ?) ");
    params.add(period.getBeginDate());
    params.add(period.getEndDate());
    query.append(" union (SELECT lastUpdateDate AS dateinformation, mediaId, 'update' as type ");
    query.append("from SC_Gallery_Media where lastUpdatedBy in ");
    appendListOfParameters(query, userIds, params);
    query.append(" and instanceId in ");
    appendListOfParameters(query, availableComponents, params);
    query.append(" and lastUpdateDate <> createDate ");
    query.append("and lastUpdateDate >= ? and lastUpdateDate <= ?) ");
    params.add(period.getBeginDate());
    params.add(period.getEndDate());
    query.append("order by dateinformation desc, mediaId desc");

    return select(con, query.toString(), params, new SelectResultRowProcessor<SocialInformation>() {

      @Override
      protected SocialInformation currentRow(final int rowIndex, final ResultSet rs)
          throws SQLException {
        Media media = getByCriteria(con, MediaCriteria.fromMediaId(rs.getString(2))
            .withVisibility(MediaCriteria.VISIBILITY.FORCE_GET_ALL));
        MediaWithStatus withStatus =
            new MediaWithStatus(media, "update".equalsIgnoreCase(rs.getString(3)));
        return new SocialInformationGallery(withStatus);
      }
    });
  }
}
