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
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.date.Period;
import org.silverpeas.media.Definition;
import org.silverpeas.persistence.jdbc.JdbcSqlQueries;
import org.silverpeas.persistence.jdbc.JdbcSqlQuery;
import org.silverpeas.persistence.jdbc.ResultSetWrapper;
import org.silverpeas.persistence.jdbc.SelectResultRowProcessor;
import org.silverpeas.persistence.repository.OperationContext;
import org.silverpeas.util.CollectionUtil;
import org.silverpeas.util.DateUtil;
import org.silverpeas.util.exception.UtilException;

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

import static org.silverpeas.persistence.jdbc.JdbcSqlQuery.*;
import static org.silverpeas.util.DBUtil.getUniqueId;

public class MediaDAO {

  private MediaDAO() {
  }

  private static final String SELECT_INTERNAL_MEDIA_PREFIX =
      "I.mediaId, I.fileName, I.fileSize, I.fileMimeType, I.download, I.beginDownloadDate," +
          " I.endDownloadDate, ";

  /**
   * Gets the media behind the specified criteria.
   * @param criteria the media criteria.
   * @return the media behind the criteria, null if no media found and throws
   * {@link IllegalArgumentException} if several media are found.
   * @throws SQLException
   * @throws IllegalArgumentException
   */
  public static Media getByCriteria(final MediaCriteria criteria)
      throws SQLException, IllegalArgumentException {
    return unique(findByCriteria(criteria));
  }

  /**
   * Finds media according to the given criteria.
   * @param criteria the media criteria.
   * @return the media list corresponding to the given criteria.
   */
  public static List<Media> findByCriteria(final MediaCriteria criteria) throws SQLException {
    MediaSQLQueryBuilder queryBuilder = new MediaSQLQueryBuilder();
    criteria.processWith(queryBuilder);

    JdbcSqlQuery selectQuery = queryBuilder.result();

    final Map<String, Photo> photos = new HashMap<>();
    final Map<String, Video> videos = new HashMap<>();
    final Map<String, Sound> sounds = new HashMap<>();
    final Map<String, Streaming> streamings = new HashMap<>();

    List<Media> media =
        selectQuery.execute(new SelectResultRowProcessor<Media>(criteria.getResultLimit()) {
          @Override
          protected Media currentRow(final ResultSetWrapper row) throws SQLException {
            String mediaId = row.getString(1);
            MediaType mediaType = MediaType.from(row.getString(2));
            String instanceId = row.getString(3);
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
              SilverTrace.warn(GalleryComponentSettings.COMPONENT_NAME, "MediaDAO.findByCriteria()",
                  "root.MSG_GEN_PARAM_VALUE", "unknown media type: " + mediaType);
              return null;
            }

            currentMedia.setMediaPK(new MediaPK(mediaId, instanceId));
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
        });

    decoratePhotos(media, photos);
    decorateVideos(media, videos);
    decorateSounds(media, sounds);
    decorateStreamings(media, streamings);

    return queryBuilder.orderingResult(media);
  }

  /**
   * Adding all data of photos.
   * @param media the list of media that have not been yet decorated.
   * @param photos indexed photo media to decorate.
   * @throws SQLException
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
        createSelect(queryBase).in(mediaIds).execute(new SelectResultRowProcessor<Photo>() {
          @Override
          protected Photo currentRow(final ResultSetWrapper row) throws SQLException {
            String mediaId = row.getString(1);
            mediaIds.remove(mediaId);
            Photo currentPhoto = photos.get(mediaId);
            decorateInternalMedia(row, currentPhoto);
            currentPhoto.setDefinition(Definition.of(row.getInt(8), row.getInt(9)));
            return null;
          }
        });
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
   * @param media the list of media that have not been yet decorated.
   * @param videos indexed video media to decorate.
   * @throws SQLException
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
        createSelect(queryBase).in(mediaIds).execute(new SelectResultRowProcessor<Video>() {
          @Override
          protected Video currentRow(final ResultSetWrapper row) throws SQLException {
            String mediaId = row.getString(1);
            mediaIds.remove(mediaId);
            Video currentVideo = videos.get(mediaId);
            decorateInternalMedia(row, currentVideo);
            currentVideo.setDefinition(Definition.of(row.getInt(8), row.getInt(9)));
            currentVideo.setBitrate(row.getLong(10));
            currentVideo.setDuration(row.getLong(11));
            return null;
          }
        });
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
   * @param media the list of media that have not been yet decorated.
   * @param sounds indexed sound media to decorate.
   * @throws SQLException
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
        createSelect(queryBase).in(mediaIds).execute(new SelectResultRowProcessor<Sound>() {
          @Override
          protected Sound currentRow(final ResultSetWrapper row) throws SQLException {
            String mediaId = row.getString(1);
            mediaIds.remove(mediaId);
            Sound currentSound = sounds.get(mediaId);
            decorateInternalMedia(row, currentSound);
            currentSound.setBitrate(row.getLong(8));
            currentSound.setDuration(row.getLong(9));
            return null;
          }
        });
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
   * @param media the list of media that have not been yet decorated.
   * @param streamings indexed streaming media to decorate.
   * @throws SQLException
   */
  private static void decorateStreamings(List<Media> media, Map<String, Streaming> streamings)
      throws SQLException {
    if (!streamings.isEmpty()) {
      Collection<Collection<String>> idGroups =
          CollectionUtil.split(new ArrayList<>(streamings.keySet()));
      String queryBase =
          "select S.mediaId, S.homepageUrl, S.provider from SC_Gallery_Streaming S where S.mediaId";
      for (Collection<String> mediaIds : idGroups) {
        createSelect(queryBase).in(mediaIds).execute(new SelectResultRowProcessor<Streaming>() {
          @Override
          protected Streaming currentRow(final ResultSetWrapper row) throws SQLException {
            String mediaId = row.getString(1);
            mediaIds.remove(mediaId);
            Streaming currentStreaming = streamings.get(mediaId);
            currentStreaming.setHomepageUrl(row.getString(2));
            currentStreaming.setProvider(StreamingProvider.from(row.getString(3)));
            return null;
          }
        });
        // Not found
        for (String mediaIdNotFound : mediaIds) {
          Streaming currentStreaming = streamings.remove(mediaIdNotFound);
          media.remove(currentStreaming);
          SilverTrace.warn(GalleryComponentSettings.COMPONENT_NAME, "MediaDAO.decorateStreamings()",
              "root.MSG_GEN_PARAM_VALUE",
              "streaming not found (removed from result): " + mediaIdNotFound);
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
    iMedia.setDownloadPeriod(getPeriod(rsw, 6, 7));
  }

  /**
   * Gets a period.
   * @param rsw the wrapper of the result set.
   * @param indexBegin the index of the start date information in the current result set row.
   * @param indexEnd the index of the end date information in the current result set row.
   * @return the period guessed from the given indexes of start and end date information.
   * @throws SQLException
   */
  private static Period getPeriod(ResultSetWrapper rsw, int indexBegin, int indexEnd)
      throws SQLException {
    Date begin = rsw.getDateFromLong(indexBegin);
    if (begin == null) {
      begin = DateUtil.MINIMUM_DATE;
    }
    Date end = rsw.getDateFromLong(indexEnd);
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
   * @throws SQLException
   * @throws UtilException
   */
  public static String saveMedia(OperationContext context, Media media)
      throws SQLException, UtilException {
    List<JdbcSqlQuery> updateQueries = new ArrayList<>();

    // The current Uuid
    String uuid = media.getId();

    boolean isInsert = !isSqlDefined(uuid) ||
        createCountFor("SC_Gallery_Media").where("mediaId = ?", uuid).execute() == 0;

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
    JdbcSqlQueries.execute(updateQueries);
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
      photoSave = createInsertFor("SC_Gallery_Photo");
      photoSave.addInsertParam("mediaId", photo.getId());
    } else {
      photoSave = createUpdateFor("SC_Gallery_Photo");
    }
    Definition definition = photo.getDefinition();
    photoSave.addSaveParam("resolutionW", definition.getWidth(), isInsert);
    photoSave.addSaveParam("resolutionH", definition.getHeight(), isInsert);
    if (!isInsert) {
      photoSave.where("mediaId = ?", photo.getId());
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
      videoSave = createInsertFor("SC_Gallery_Video");
      videoSave.addInsertParam("mediaId", video.getId());
    } else {
      videoSave = createUpdateFor("SC_Gallery_Video");
    }
    Definition definition = video.getDefinition();
    videoSave.addSaveParam("resolutionW", definition.getWidth(), isInsert);
    videoSave.addSaveParam("resolutionH", definition.getHeight(), isInsert);
    videoSave.addSaveParam("bitrate", video.getBitrate(), isInsert);
    videoSave.addSaveParam("duration", video.getDuration(), isInsert);
    if (!isInsert) {
      videoSave.where("mediaId = ?", video.getId());
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
      soundSave = createInsertFor("SC_Gallery_Sound");
      soundSave.addInsertParam("mediaId", sound.getId());
    } else {
      soundSave = createUpdateFor("SC_Gallery_Sound");
    }
    soundSave.addSaveParam("bitrate", sound.getBitrate(), isInsert);
    soundSave.addSaveParam("duration", sound.getDuration(), isInsert);
    if (!isInsert) {
      soundSave.where("mediaId = ?", sound.getId());
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
      streamingSave = createInsertFor("SC_Gallery_Streaming");
      streamingSave.addInsertParam("mediaId", streaming.getId());
    } else {
      streamingSave = createUpdateFor("SC_Gallery_Streaming");
      streamingSave.append("update SC_Gallery_Streaming set ");
    }
    streamingSave.addSaveParam("homepageUrl", streaming.getHomepageUrl(), isInsert);
    streamingSave.addSaveParam("provider", streaming.getProvider(), isInsert);
    if (!isInsert) {
      streamingSave.where("mediaId = ?", streaming.getId());
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
      mediaSave = createInsertFor("SC_Gallery_Media");
      mediaSave.addInsertParam("mediaId", media.getId());
    } else {
      mediaSave = createUpdateFor("SC_Gallery_Media");
    }
    mediaSave.addSaveParam("mediaType", media.getType(), isInsert);
    mediaSave.addSaveParam("instanceId", media.getComponentInstanceId(), isInsert);
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
      mediaSave.where("mediaId = ?", media.getId());
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
      iMediaSave = createInsertFor("SC_Gallery_Internal");
      iMediaSave.addInsertParam("mediaId", iMedia.getId());
    } else {
      iMediaSave = createUpdateFor("SC_Gallery_Internal");
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
      iMediaSave.where("mediaId = ?", iMedia.getId());
    }
    return iMediaSave;
  }

  /**
   * Deletes the specified media (and its album links).
   * @param media the media to delete.
   * @throws SQLException
   */
  public static void deleteMedia(Media media) throws SQLException {
    String mediaId = media.getId();
    List<JdbcSqlQuery> updateQueries = new ArrayList<>();
    updateQueries.add(createDeleteFor("SC_Gallery_Media").where("mediaId = ?", mediaId));
    if (MediaType.Photo == media.getType() || MediaType.Video == media.getType() ||
        MediaType.Sound == media.getType()) {
      updateQueries.add(createDeleteFor("SC_Gallery_Internal").where("mediaId = ?", mediaId));
    }
    switch (media.getType()) {
      case Photo:
        updateQueries.add(createDeleteFor("SC_Gallery_Photo").where("mediaId = ?", mediaId));
        break;
      case Video:
        updateQueries.add(createDeleteFor("SC_Gallery_Video").where("mediaId = ?", mediaId));
        break;
      case Sound:
        updateQueries.add(createDeleteFor("SC_Gallery_Sound").where("mediaId = ?", mediaId));
        break;
      case Streaming:
        updateQueries.add(createDeleteFor("SC_Gallery_Streaming").where("mediaId = ?", mediaId));
        break;
      default:
        SilverTrace.warn(GalleryComponentSettings.COMPONENT_NAME, "MediaDAO.deleteMedia",
            "Unknown media type to delete id=" + media.getId());
        break;
    }
    JdbcSqlQueries.execute(updateQueries);
    deleteAllMediaPath(media);
  }

  /**
   * Saves the album link for the given media.
   * @param media the media that must be associated to the given album.
   * @param albumId the identifier of the album.
   * @throws SQLException
   * @throws UtilException
   */
  public static void saveMediaPath(Media media, String albumId) throws SQLException, UtilException {
    List<?> pathParams =
        Arrays.asList(media.getId(), media.getInstanceId(), Integer.valueOf(albumId));

    boolean isInsert = createCountFor("SC_Gallery_Path")
        .where("mediaId = ? and instanceId = ? and nodeId = ?", pathParams).execute() == 0;

    if (isInsert) {
      Iterator<?> paramIt = pathParams.iterator();
      JdbcSqlQuery insert = createInsertFor("SC_Gallery_Path");
      insert.addInsertParam("mediaId", paramIt.next());
      insert.addInsertParam("instanceId", paramIt.next());
      insert.addInsertParam("nodeId", paramIt.next());
      insert.execute();
    }
  }

  /**
   * Deletes the album links of the specified media.
   * @param media the media for which all album links must be deleted.
   * @throws SQLException
   */
  public static void deleteAllMediaPath(Media media) throws SQLException {
    createDeleteFor("SC_Gallery_Path")
        .where("mediaId = ? and instanceId = ?", media.getId(), media.getInstanceId());
  }

  /**
   * Gets the identifier list of albums which the given media is associated to.
   * @param media the media aimed.
   * @return the identifier list of albums in which the given media is attached to.
   * @throws SQLException
   */
  public static Collection<String> getAlbumIdsOf(Media media) throws SQLException {
    return createSelect(
        "N.NodeId from SC_Gallery_Path P, SB_Node_Node N where P.mediaId = ? and N.nodeId " +
            "= P.NodeId and P.instanceId = ? and N.instanceId = P.instanceId", media.getId(),
        media.getInstanceId()).execute(new SelectResultRowProcessor<String>() {
      @Override
      protected String currentRow(final ResultSetWrapper row) throws SQLException {
        return String.valueOf(row.getInt(1));
      }
    });
  }

  /**
   * get my SocialInformationGallery according to the type of data base used(PostgresSQL,Oracle,
   * MMS).
   * @param userId the identifier of a user.
   * @param period the period on which the data are requested.
   * @return List<SocialInformation>
   * @throws SQLException
   */
  public static List<SocialInformation> getAllMediaIdByUserId(String userId, Period period)
      throws SQLException {
    return createSelect(
        "(select createDate AS dateinformation, mediaId, 'new' as type from SC_Gallery_Media " +
            "where createdBy = ? and createDate >= ? and createDate <= ? ) " +
            "union (select lastUpdateDate AS dateinformation, mediaId , " +
            "'update' as type from SC_Gallery_Media where lastUpdatedBy = ? and lastUpdateDate <>" +
            " createDate and lastUpdateDate >= ? and lastUpdateDate <= ? ) order by " +
            "dateinformation desc, mediaId desc", userId, period.getBeginDate(),
        period.getEndDate(), userId, period.getBeginDate(), period.getEndDate())
        .execute(new SelectResultRowProcessor<SocialInformation>() {
          @Override
          protected SocialInformation currentRow(final ResultSetWrapper row) throws SQLException {
            Media media = getByCriteria(MediaCriteria.fromMediaId(row.getString(2))
                .withVisibility(MediaCriteria.VISIBILITY.FORCE_GET_ALL));
            MediaWithStatus withStatus =
                new MediaWithStatus(media, "update".equalsIgnoreCase(row.getString(3)));
            return new SocialInformationGallery(withStatus);
          }
        });
  }

  /**
   * get list of socialInformationGallery of my contacts according to the type of data base
   * used(PostgresSQL,Oracle,MMS) .
   * @param userIds the identifiers of users.
   * @param availableComponents the list of available components.
   * @param period the period on which the data are requested.
   * @return the information for social data.
   * @throws SQLException
   */
  public static List<SocialInformation> getSocialInformationListOfMyContacts(List<String> userIds,
      List<String> availableComponents, Period period) throws SQLException {
    JdbcSqlQuery query = create("(select createDate as dateinformation, mediaId, 'new' as type");
    query.append("from SC_Gallery_Media where createdBy").in(userIds);
    query.append("and instanceId").in(availableComponents);
    query.append("and createDate >= ? and createDate <= ?)", period.getBeginDate(),
        period.getEndDate());
    query.append("union (select lastUpdateDate as dateinformation, mediaId, 'update' as type");
    query.append("from SC_Gallery_Media where lastUpdatedBy").in(userIds);
    query.append("and instanceId").in(availableComponents);
    query.append("and lastUpdateDate <> createDate");
    query.append("and lastUpdateDate >= ? and lastUpdateDate <= ?)", period.getBeginDate(),
        period.getEndDate());
    query.append("order by dateinformation desc, mediaId desc");

    return query.execute(new SelectResultRowProcessor<SocialInformation>() {
      @Override
      protected SocialInformation currentRow(final ResultSetWrapper row) throws SQLException {
        Media media = getByCriteria(MediaCriteria.fromMediaId(row.getString(2))
            .withVisibility(MediaCriteria.VISIBILITY.FORCE_GET_ALL));
        MediaWithStatus withStatus =
            new MediaWithStatus(media, "update".equalsIgnoreCase(row.getString(3)));
        return new SocialInformationGallery(withStatus);
      }
    });
  }
}
