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
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.service;

import org.silverpeas.components.gallery.GalleryComponentSettings;
import org.silverpeas.components.gallery.GalleryContentManager;
import org.silverpeas.components.gallery.Watermark;
import org.silverpeas.components.gallery.constant.MediaType;
import org.silverpeas.components.gallery.dao.MediaDAO;
import org.silverpeas.components.gallery.dao.OrderDAO;
import org.silverpeas.components.gallery.dao.PhotoDAO;
import org.silverpeas.components.gallery.delegate.GalleryPasteDelegate;
import org.silverpeas.components.gallery.delegate.MediaDataCreateDelegate;
import org.silverpeas.components.gallery.delegate.MediaDataUpdateDelegate;
import org.silverpeas.components.gallery.model.AlbumDetail;
import org.silverpeas.components.gallery.model.GalleryRuntimeException;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.MediaCriteria;
import org.silverpeas.components.gallery.model.MediaOrderCriteria;
import org.silverpeas.components.gallery.model.MediaPK;
import org.silverpeas.components.gallery.model.Order;
import org.silverpeas.components.gallery.model.OrderRow;
import org.silverpeas.components.gallery.model.Photo;
import org.silverpeas.components.gallery.process.GalleryProcessManagement;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.index.search.model.SearchResult;
import org.silverpeas.core.node.dao.NodeDAO;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.search.SearchService;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.SettingBundle;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.silverpeas.components.gallery.model.MediaCriteria.QUERY_ORDER_BY.CREATE_DATE_DESC;
import static org.silverpeas.components.gallery.model.MediaCriteria.QUERY_ORDER_BY.IDENTIFIER_DESC;

/**
 * DefaultGalleryService is the service layer which manage a media gallery
 */
@Service
@Named("galleryService")
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultGalleryService implements GalleryService {

  @Inject
  private NodeService nodeService;

  @Inject
  private GalleryContentManager galleryContentManager;
  @Inject
  private NodeDAO nodeDAO;

  @Override
  public Optional<Media> getContributionById(final ContributionIdentifier contributionId) {
    return Optional.ofNullable(getMedia(
        new MediaPK(contributionId.getLocalId(), contributionId.getComponentInstanceId())));
  }

  @Override
  public SettingBundle getComponentSettings() {
    return GalleryComponentSettings.getSettings();
  }

  @Override
  public LocalizationBundle getComponentMessages(final String language) {
    return GalleryComponentSettings.getMessagesIn(language);
  }

  @Override
  public boolean isRelatedTo(final String instanceId) {
    return instanceId.startsWith(GalleryComponentSettings.COMPONENT_NAME);
  }

  @Override
  public AlbumDetail getAlbum(final NodePK nodePK) {
    return getAlbum(nodePK, MediaCriteria.VISIBILITY.BY_DEFAULT);
  }

  @Override
  public AlbumDetail getAlbum(final NodePK nodePK, MediaCriteria.VISIBILITY visibility) {
    try {
      return new AlbumDetail(nodeService.getDetail(nodePK), visibility);
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  public Collection<AlbumDetail> getAllAlbums(final String instanceId) {
    try {
      final NodePK nodePK = new NodePK(NodePK.ROOT_NODE_ID, instanceId);
      final Collection<NodeDetail> nodes = nodeService.getSubTree(nodePK);
      final List<AlbumDetail> albums = new ArrayList<>(nodes.size());
      for (final NodeDetail node : nodes) {
        albums.add(new AlbumDetail(node));
      }
      return albums;
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public NodePK createAlbum(final AlbumDetail album, final NodePK nodePK) {
    try {
      final AlbumDetail currentAlbum = getAlbum(nodePK);
      return nodeService.createNode(album, currentAlbum);
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void updateAlbum(final AlbumDetail album) {
    try {
      nodeService.setDetail(album);
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void deleteAlbum(final UserDetail user, final String componentInstanceId,
      final NodePK nodePK) {
    try {
      final GalleryProcessManagement processManagement =
          new GalleryProcessManagement(user, componentInstanceId);
      processManagement.addDeleteAlbumProcesses(nodePK);
      processManagement.execute();
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  public Photo getPhoto(final MediaPK mediaPK) {
    try {
      return PhotoDAO.getPhoto(mediaPK.getId());
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  public Media getMedia(final MediaPK mediaPK) {
    return getMedia(mediaPK, MediaCriteria.VISIBILITY.BY_DEFAULT);
  }

  @Override
  public Media getMedia(final MediaPK mediaPK, final MediaCriteria.VISIBILITY visibility) {
    try {
      return MediaDAO.getByCriteria(MediaCriteria.fromComponentInstanceId(mediaPK.getInstanceId())
          .identifierIsOneOf(mediaPK.getId())
          .withVisibility(visibility));
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  public List<Media> getMedia(final List<String> mediaIds, final String componentInstanceId) {
    return getMedia(mediaIds, componentInstanceId, MediaCriteria.VISIBILITY.BY_DEFAULT);
  }

  @Override
  public List<Media> getMedia(final List<String> mediaIds, final String componentInstanceId,
      final MediaCriteria.VISIBILITY visibility) {
    try {
      return MediaDAO.findByCriteria(MediaCriteria.fromComponentInstanceId(componentInstanceId)
          .identifierIsOneOf(mediaIds.toArray(new String[0]))
          .withVisibility(visibility));
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  public Collection<Media> getAllMedia(final String instanceId,
      final MediaCriteria.VISIBILITY visibility) {
    try {
      return MediaDAO.findByCriteria(MediaCriteria.fromComponentInstanceId(instanceId)
          .withVisibility(visibility));
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  public Collection<Media> getAllMedia(final NodePK nodePK,
      final MediaCriteria.VISIBILITY visibility) {
    try {
      final String albumId = nodePK.getId();
      final String instanceId = nodePK.getInstanceId();
      return MediaDAO.findByCriteria(MediaCriteria.fromComponentInstanceId(instanceId)
          .albumIdentifierIsOneOf(albumId)
          .withVisibility(visibility));
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  public long countAllMedia(final NodePK nodePK) {
    return countAllMedia(nodePK, MediaCriteria.VISIBILITY.BY_DEFAULT);
  }

  @Override
  public long countAllMedia(final NodePK nodePK, final MediaCriteria.VISIBILITY visibility) {
    try {
      final String albumId = nodePK.getId();
      final String instanceId = nodePK.getInstanceId();
      return MediaDAO.countByCriteria(MediaCriteria.fromComponentInstanceId(instanceId)
          .albumIdentifierIsOneOf(albumId)
          .withVisibility(visibility));
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  public Collection<Photo> getAllPhotos(final NodePK nodePK) {
    return getAllPhotos(nodePK, MediaCriteria.VISIBILITY.BY_DEFAULT);
  }

  @Override
  public Collection<Photo> getAllPhotos(final NodePK nodePK,
      final MediaCriteria.VISIBILITY visibility) {
    try {
      final String albumId = nodePK.getId();
      final String instanceId = nodePK.getInstanceId();
      return PhotoDAO.getAllPhoto(albumId, instanceId, visibility);
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  public Collection<Media> getNotVisible(final String instanceId) {
    try {
      return MediaDAO.findByCriteria(MediaCriteria.fromComponentInstanceId(instanceId)
          .withVisibility(MediaCriteria.VISIBILITY.HIDDEN_ONLY));
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void paste(final UserDetail user, final String componentInstanceId,
      final GalleryPasteDelegate delegate) {
    try {
      final GalleryProcessManagement processManagement =
          new GalleryProcessManagement(user, componentInstanceId);
      // MediaList
      if (delegate.getAlbum()
          .hasFather()) {
        for (final Map.Entry<Media, Boolean> mediaToPaste : delegate.getMediaToPaste()
            .entrySet()) {
          processManagement.addPasteMediaProcesses(getMedia(mediaToPaste.getKey()
              .getMediaPK(), MediaCriteria.VISIBILITY.FORCE_GET_ALL), delegate.getAlbum()
              .getNodePK(), mediaToPaste.getValue());
        }
      }
      // Albums
      for (final Map.Entry<AlbumDetail, Boolean> albumToPaste : delegate.getAlbumsToPaste()
          .entrySet()) {
        processManagement.addPasteAlbumProcesses(albumToPaste.getKey(), delegate.getAlbum(),
            albumToPaste.getValue());
      }
      processManagement.execute();
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public void importFromRepository(final UserDetail user, final String componentInstanceId,
      final File repository, final MediaDataCreateDelegate delegate) {
    try {
      GalleryProcessManagement.importFromRepositoryProcesses(user, componentInstanceId, repository,
          delegate.getAlbumId(), delegate);
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public Media createMedia(final UserDetail user, final String componentInstanceId,
      final Watermark watermark, final MediaDataCreateDelegate delegate) {
    try {
      final GalleryProcessManagement processManagement =
          new GalleryProcessManagement(user, componentInstanceId);
      Media newMedia = delegate.newInstance();
      processManagement.addCreateMediaProcesses(newMedia, delegate.getAlbumId(),
          delegate.getFileItem(), watermark, delegate);
      processManagement.execute();
      return newMedia;
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void updateMedia(final UserDetail user, final String componentInstanceId,
      final Collection<String> mediaIds, final String albumId,
      final MediaDataUpdateDelegate delegate) {
    try {
      final GalleryProcessManagement processManagement =
          new GalleryProcessManagement(user, componentInstanceId);
      for (final String mediaId : mediaIds) {
        processManagement.addUpdateMediaProcesses(
            getMedia(new MediaPK(mediaId, componentInstanceId)), null, delegate);
      }
      processManagement.execute();
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void updateMedia(final UserDetail user, final String componentInstanceId,
      final Media media, final Watermark watermark, final MediaDataUpdateDelegate delegate) {
    try {
      final GalleryProcessManagement processManagement =
          new GalleryProcessManagement(user, componentInstanceId);
      processManagement.addUpdateMediaProcesses(media, watermark, delegate);
      processManagement.execute();
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void deleteMedia(final UserDetail user, final String componentInstanceId,
      final Collection<String> mediaIds) {
    try {
      final GalleryProcessManagement processManagement =
          new GalleryProcessManagement(user, componentInstanceId);
      for (final String mediaId : mediaIds) {
        processManagement.addDeleteMediaProcesses(
            getMedia(new MediaPK(mediaId, componentInstanceId)));
      }
      processManagement.execute();
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  public List<Media> getLastRegisteredMedia(final String instanceId) {
    try {
      return MediaDAO.findByCriteria(MediaCriteria.fromComponentInstanceId(instanceId)
          .orderedBy(CREATE_DATE_DESC, IDENTIFIER_DESC)
          .limitResultTo(GalleryComponentSettings.getNbMediaDisplayedPerPage()));
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  private Connection openConnection() {
    try {
      return DBUtil.openConnection();
    } catch (final SQLException e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  public Collection<NodeDetail> getPath(final NodePK nodePK) {
    Collection<NodeDetail> path;
    try {
      path = nodeService.getPath(nodePK);
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
    return path;
  }

  @Override
  public Collection<String> getAlbumIdsOf(final Media media) {
    try {
      return MediaDAO.getAlbumIdsOf(media);
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void removeMediaFromAllAlbums(final Media media) {
    try {
      MediaDAO.deleteAllMediaPath(media);
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void addMediaToAlbums(final Media media, final String... albums) {
    try {
      for (final String albumId : albums) {
        MediaDAO.saveMediaPath(media, albumId);
      }
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  public void indexGallery(final UserDetail user, final String instanceId) {
    // parcourir tous les albums
    final Collection<AlbumDetail> albums = getAllAlbums(instanceId);
    if (albums != null) {

      // pour chaque album, parcourir toutes les photos
      for (final AlbumDetail album : albums) {

        // indexation de l'album
        try {
          nodeService.createIndex(album);
        } catch (final Exception e) {
          throw new GalleryRuntimeException(e);
        }

        final Collection<Media> media =
            getAllMedia(album.getNodePK(), MediaCriteria.VISIBILITY.FORCE_GET_ALL);
        if (media != null) {
          for (final Media aMedia : media) {
            createIndex(user, aMedia);
          }
        }
      }
    }
  }

  private void createIndex(final UserDetail user, final Media media) {
    try {
      @SuppressWarnings("removal") final GalleryProcessManagement processManagement =
          new GalleryProcessManagement(user, media.getComponentInstanceId());
      processManagement.addIndexMediaProcesses(media);
      processManagement.execute();
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  public int getSilverObjectId(final MediaPK mediaPK) {

    int silverObjectId;
    try {
      silverObjectId =
          galleryContentManager.getSilverContentId(mediaPK.getId(), mediaPK.getInstanceId());

      if (silverObjectId == -1) {
        Media media = getMedia(mediaPK, MediaCriteria.VISIBILITY.FORCE_GET_ALL);
        silverObjectId =
            Transaction.performInOne(() -> createSilverContent(media, media.getCreatorId()));
      }
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
    return silverObjectId;
  }

  private int createSilverContent(final Media media, final String creatorId) {
    try {
      return galleryContentManager.createSilverContent(media, creatorId);
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  public Collection<Media> search(final QueryDescription query) {
    final Collection<Media> mediaList = new ArrayList<>();
    try {
      final List<SearchResult> results = SearchService.get()
          .search(query);
      // création des médias à partir des résultats
      // Ne retourne que les médias
      results.stream()
          .filter(result -> MediaType.from(result.getType()) != MediaType.Unknown)
          .forEach(result -> {
            final MediaPK mediaPK = new MediaPK(result.getId());
            final Media media = getMedia(mediaPK);
            if (media != null) {
              mediaList.add(media);
            }
          });
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
    return mediaList;
  }

  @Override
  public Collection<Media> getAllMediaThatWillBeNotVisible(final int nbDays) {
    try {
      return MediaDAO.findByCriteria(MediaCriteria.fromNbDaysBeforeThatMediaIsNotVisible(nbDays)
          .withVisibility(MediaCriteria.VISIBILITY.FORCE_GET_ALL));
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public String createOrder(final Collection<String> basket, final String userId,
      final String componentId) {
    try {
      return OrderDAO.createOrder(basket, userId, componentId);
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  public List<Order> getAllOrders(final String userId, final String instanceId) {
    try {
      return OrderDAO.findByCriteria(MediaOrderCriteria.fromComponentInstanceId(instanceId)
          .withOrdererId(userId));
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void updateOrder(final Order order) {
    try {
      OrderDAO.updateOrder(order);
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void updateOrderRow(final OrderRow row) {
    try {
      OrderDAO.updateOrderRow(row);
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  public Order getOrder(final String orderId, final String instanceId) {
    try {
      return OrderDAO.getByCriteria(MediaOrderCriteria.fromComponentInstanceId(instanceId)
          .identifierIsOneOf(orderId));
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  public List<Order> getAllOrderToDelete(final int nbDays) {
    try {
      return OrderDAO.findByCriteria(MediaOrderCriteria.fromNbDaysAfterThatDeleteAnOrder(nbDays));
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @SuppressWarnings("Convert2streamapi")
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void deleteOrders(final List<Order> orders) {
    try {
      for (Order order : orders) {
        OrderDAO.deleteOrder(order);
      }
    } catch (final Exception e) {
      throw new GalleryRuntimeException(e);
    }
  }

  /**
   * get my list of SocialInformationGallery according to options and number of Item and the first
   * Index.
   * @param period the period on which the data are requested.
   * @return List <SocialInformation>
   */
  @Override
  public List<SocialInformation> getAllMediaByUserId(final String userId, final Period period) {
    try {
      return MediaDAO.getAllMediaIdByUserId(userId, period);
    } catch (final SQLException e) {
      throw new GalleryRuntimeException(e);
    }
  }

  /**
   * get list of SocialInformationGallery of my contacts according to options and number of Item and
   * the first Index.
   * @param period the period on which the data are requested.
   * @return List <SocialInformation>
   */
  @Override
  public List<SocialInformation> getSocialInformationListOfMyContacts(
      final List<String> listOfUserId, final List<String> availableComponent, final Period period) {
    try {
      return MediaDAO.getSocialInformationListOfMyContacts(listOfUserId, availableComponent,
          period);
    } catch (final SQLException e) {
      throw new GalleryRuntimeException(e);
    }
  }

  @Override
  public void sortAlbums(final List<NodePK> nodePKs) {
    try (final Connection con = openConnection()) {
      nodeDAO.sortNodes(con, nodePKs);
    } catch (final SQLException e) {
      throw new GalleryRuntimeException(e);
    }
  }
}
