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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.service;

import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.node.dao.NodeDAO;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.components.gallery.GalleryComponentSettings;
import org.silverpeas.components.gallery.GalleryContentManager;
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
import org.silverpeas.components.gallery.process.GalleryProcessExecutionContext;
import org.silverpeas.components.gallery.process.GalleryProcessManagement;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.index.search.SearchEngineProvider;
import org.silverpeas.core.index.search.model.MatchingIndexEntry;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.process.ProcessProvider;
import org.silverpeas.core.process.util.ProcessList;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.SilverpeasRuntimeException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.silverpeas.components.gallery.model.MediaCriteria.QUERY_ORDER_BY.CREATE_DATE_DESC;
import static org.silverpeas.components.gallery.model.MediaCriteria.QUERY_ORDER_BY.IDENTIFIER_DESC;

/**
 * DefaultGalleryService is the service layer which manage a media gallery
 */
@Singleton
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultGalleryService implements GalleryService {

  @Inject
  private NodeService nodeService;
  @Inject
  private OrganizationController organizationController;

  @Override
  public AlbumDetail getAlbum(final NodePK nodePK) {
    return getAlbum(nodePK, MediaCriteria.VISIBILITY.BY_DEFAULT);
  }

  @Override
  public AlbumDetail getAlbum(final NodePK nodePK, MediaCriteria.VISIBILITY visibility) {
    try {
      final AlbumDetail album = new AlbumDetail(nodeService.getDetailTransactionally(nodePK));
      // Loading the media
      final Collection<Media> media = getAllMedia(nodePK, visibility);
      // Setting the media into the album object instance.
      album.setMedia(media);
      return album;
    } catch (final Exception e) {
      throw new GalleryRuntimeException("DefaultGalleryService.getAlbum()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ALBUM_NOT_EXIST", e);
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
      throw new GalleryRuntimeException("DefaultGalleryService.getAllAlbums()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ALBUM_NOT_EXIST", e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public NodePK createAlbum(final AlbumDetail album, final NodePK nodePK) {
    try {
      final AlbumDetail currentAlbum = getAlbum(nodePK);
      return nodeService.createNode(album, currentAlbum);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("DefaultGalleryService.createAlbum()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ALBUM_NOT_CREATE", e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void updateAlbum(final AlbumDetail album) {
    try {
      nodeService.setDetail(album);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("DefaultGalleryService.updateAlbum()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ALBUM_NOT_UPDATE", e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void deleteAlbum(final UserDetail user, final String componentInstanceId,
      final NodePK nodePK) {
    try {
      final GalleryProcessManagement processManagement = new GalleryProcessManagement(user,
          componentInstanceId);
      processManagement.addDeleteAlbumProcesses(nodePK);
      processManagement.execute();
    } catch (final Exception e) {
      throw new GalleryRuntimeException("DefaultGalleryService.deleteAlbum()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ALBUM_NOT_DELETE", e);
    }
  }

  @Override
  public Photo getPhoto(final MediaPK mediaPK) {
    try {
      return PhotoDAO.getPhoto(mediaPK.getId());
    } catch (final Exception e) {
      throw new GalleryRuntimeException("DefaultGalleryService.getPhoto()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
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
              .identifierIsOneOf(mediaPK.getId()).withVisibility(visibility));
    } catch (final Exception e) {
      throw new GalleryRuntimeException("DefaultGalleryService.getMedia()", SilverpeasRuntimeException.ERROR,
          "gallery.MSG_PHOTO_NOT_EXIST", e);
    }
  }

  @Override
  public Collection<Media> getAllMedia(final String instanceId) {
    return getAllMedia(instanceId, MediaCriteria.VISIBILITY.BY_DEFAULT);
  }

  @Override
  public Collection<Media> getAllMedia(final String instanceId,
      final MediaCriteria.VISIBILITY visibility) {
    try {
      return MediaDAO.findByCriteria(
          MediaCriteria.fromComponentInstanceId(instanceId).withVisibility(visibility));
    } catch (final Exception e) {
      throw new GalleryRuntimeException("DefaultGalleryService.getAllMedia()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    }
  }

  @Override
  public Collection<Media> getAllMedia(final NodePK nodePK) {
    return getAllMedia(nodePK, MediaCriteria.VISIBILITY.BY_DEFAULT);
  }

  @Override
  public Collection<Media> getAllMedia(final NodePK nodePK,
      final MediaCriteria.VISIBILITY visibility) {
    try {
      final String albumId = nodePK.getId();
      final String instanceId = nodePK.getInstanceId();
      return MediaDAO.findByCriteria(
          MediaCriteria.fromComponentInstanceId(instanceId).albumIdentifierIsOneOf(albumId)
              .withVisibility(visibility));
    } catch (final Exception e) {
      throw new GalleryRuntimeException("DefaultGalleryService.getAllMedia()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
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
      throw new GalleryRuntimeException("DefaultGalleryService.getAllPhotos()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    }
  }

  @Override
  public Collection<Media> getNotVisible(final String instanceId) {
    try {
      return MediaDAO.findByCriteria(MediaCriteria.fromComponentInstanceId(instanceId)
          .withVisibility(MediaCriteria.VISIBILITY.HIDDEN_ONLY));
    } catch (final Exception e) {
      throw new GalleryRuntimeException("DefaultGalleryService.getNotVisible()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void paste(final UserDetail user, final String componentInstanceId,
      final GalleryPasteDelegate delegate) {
    try {
      final GalleryProcessManagement processManagement = new GalleryProcessManagement(user,
          componentInstanceId);
      // MediaList
      if (delegate.getAlbum().hasFather()) {
        for (final Map.Entry<Media, Boolean> mediaToPaste : delegate.getMediaToPaste().entrySet()) {
          processManagement.addPasteMediaProcesses(
              getMedia(mediaToPaste.getKey().getMediaPK(), MediaCriteria.VISIBILITY.FORCE_GET_ALL),
              delegate.getAlbum().getNodePK(), mediaToPaste.getValue());
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
      throw new GalleryRuntimeException("GalleryService.paste()", SilverpeasRuntimeException.ERROR,
          "gallery.MSG_PASTE_ERROR", e);
    }
  }

  @SuppressWarnings("EjbProhibitedPackageUsageInspection")
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void importFromRepository(final UserDetail user, final String componentInstanceId,
      final File repository, final boolean watermark, final String watermarkHD,
      final String watermarkOther, final MediaDataCreateDelegate delegate) {
    try {
      final GalleryProcessManagement processManagement = new GalleryProcessManagement(user,
          componentInstanceId);
      processManagement.addImportFromRepositoryProcesses(repository, delegate.getAlbumId(),
          watermark, watermarkHD, watermarkOther, delegate);
      processManagement.execute();
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryService.importFromRepository()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTOS_NOT_IMPORTED", e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public Media createMedia(final UserDetail user, final String componentInstanceId,
      final boolean watermark, final String watermarkHD, final String watermarkOther,
      final MediaDataCreateDelegate delegate) {
    try {
      final GalleryProcessManagement processManagement = new GalleryProcessManagement(user,
          componentInstanceId);
      Media newMedia = delegate.newInstance();
      processManagement
          .addCreateMediaProcesses(newMedia, delegate.getAlbumId(), delegate.getFileItem(),
              watermark, watermarkHD, watermarkOther, delegate);
      processManagement.execute();
      return newMedia;
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryService.createMedia()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_CREATE", e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void updateMedia(final UserDetail user, final String componentInstanceId,
      final Collection<String> mediaIds, final String albumId,
      final MediaDataUpdateDelegate delegate) {
    try {
      final GalleryProcessManagement processManagement = new GalleryProcessManagement(user,
          componentInstanceId);
      for (final String mediaId : mediaIds) {
        processManagement
            .addUpdateMediaProcesses(getMedia(new MediaPK(mediaId, componentInstanceId)), false,
                null, null, delegate);
      }
      processManagement.execute();
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryService.updateMedia()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_UPDATE", e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void updateMedia(final UserDetail user, final String componentInstanceId,
      final Media media, final boolean watermark, final String watermarkHD,
      final String watermarkOther, final MediaDataUpdateDelegate delegate) {
    try {
      final GalleryProcessManagement processManagement = new GalleryProcessManagement(user,
          componentInstanceId);
      processManagement
          .addUpdateMediaProcesses(media, watermark, watermarkHD, watermarkOther, delegate);
      processManagement.execute();
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryService.updateMedia()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_UPDATE", e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void deleteMedia(final UserDetail user, final String componentInstanceId,
      final Collection<String> mediaIds) {
    try {
      final GalleryProcessManagement processManagement = new GalleryProcessManagement(user,
          componentInstanceId);
      for (final String mediaId : mediaIds) {
        processManagement
            .addDeleteMediaProcesses(getMedia(new MediaPK(mediaId, componentInstanceId)));
      }
      processManagement.execute();
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryService.deleteMedia()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_DELETE", e);
    }
  }

  @Override
  public List<Media> getLastRegisteredMedia(final String instanceId) {
    try {
      return MediaDAO.findByCriteria(MediaCriteria.fromComponentInstanceId(instanceId)
          .orderedBy(CREATE_DATE_DESC, IDENTIFIER_DESC)
          .limitResultTo(GalleryComponentSettings.getNbMediaDisplayedPerPage()));
    } catch (final Exception e) {
      throw new GalleryRuntimeException("DefaultGalleryService.getLastRegisteredMedia()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    }
  }

  protected Connection openConnection() {
    try {
      return DBUtil.openConnection();
    } catch (final SQLException e) {
      throw new GalleryRuntimeException("DefaultGalleryService.openConnection()", SilverpeasException.ERROR,
          "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  @Override
  public Collection<NodeDetail> getPath(final NodePK nodePK) {
    Collection<NodeDetail> path;
    try {
      path = nodeService.getPath(nodePK);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("DefaultGalleryService.getPath()", SilverpeasRuntimeException.ERROR,
          "gallery.MSG_PATH", e);
    }
    return path;
  }

  @Override
  public Collection<String> getAlbumIdsOf(final Media media) {
    try {
      return MediaDAO.getAlbumIdsOf(media);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("DefaultGalleryService.getPathList()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PATH", e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void removeMediaFromAllAlbums(final Media media) {
    try {
      MediaDAO.deleteAllMediaPath(media);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("DefaultGalleryService.removeMediaFromAllAlbums()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PATH", e);
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
      throw new GalleryRuntimeException("DefaultGalleryService.addMediaToAlbums()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PATH", e);
    }
  }

  @Override
  public String getHTMLNodePath(final NodePK nodePK) {
    String htmlPath;
    try {
      final List<NodeDetail> path = (List<NodeDetail>) getPath(nodePK);
      if (!path.isEmpty()) {
        path.remove(path.size() - 1);
      }
      htmlPath = getSpacesPath(nodePK.getInstanceId()) + getComponentLabel(nodePK.getInstanceId())
          + " > "
          + displayPath(path, 10);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("DefaultGalleryService.getHTMLNodePath()",
          SilverpeasRuntimeException.ERROR,
          "gallery.EX_IMPOSSIBLE_DOBTENIR_LES_EMPLACEMENTS_DE_LA_PUBLICATION", e);
    }
    return htmlPath;
  }

  private String getSpacesPath(final String componentId) {
    StringBuilder spacesPath = new StringBuilder();
    final List<SpaceInst> spaces = getOrganizationController().getSpacePathToComponent(componentId);
    for (final SpaceInst spaceInst : spaces) {
      spacesPath.append(spaceInst.getName()).append(" > ");
    }
    return spacesPath.toString();
  }

  private String getComponentLabel(final String componentId) {
    final ComponentInstLight component = getOrganizationController().getComponentInstLight(
        componentId);
    String componentLabel = "";
    if (component != null) {
      componentLabel = component.getLabel();
    }
    return componentLabel;
  }

  private OrganizationController getOrganizationController() {
    return organizationController;
  }

  private String displayPath(final Collection<NodeDetail> path, final int beforeAfter) {
    String pathString = "";
    final int nbItemInPath = path.size();
    final Iterator<NodeDetail> iterator = path.iterator();
    boolean alreadyCut = false;
    int nb = 0;

    NodeDetail nodeInPath;
    while (iterator.hasNext()) {
      nodeInPath = iterator.next();
      if ((nb <= beforeAfter) || (nb + beforeAfter >= nbItemInPath - 1)) {
        pathString = nodeInPath.getName() + " " + pathString;
        if (iterator.hasNext()) {
          pathString = " > " + pathString;
        }
      } else {
        if (!alreadyCut) {
          pathString += " ... > ";
          alreadyCut = true;
        }
      }
      nb++;
    }
    return pathString;
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
          throw new GalleryRuntimeException("DefaultGalleryService.indexGallery()",
              SilverpeasRuntimeException.ERROR, "gallery.MSG_INDEXALBUM", e);
        }

        final Collection<Media> media = getAllMedia(album.getNodePK(),
            MediaCriteria.VISIBILITY.FORCE_GET_ALL);
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
      final GalleryProcessManagement processManagement =
          new GalleryProcessManagement(user, media.getComponentInstanceId());
      processManagement.addIndexMediaProcesses(media);
      processManagement.execute();
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryService.createIndex()", SilverpeasRuntimeException.ERROR,
          "gallery.MSG_PHOTO_NOT_CREATE", e);
    }
  }

  @Override
  public int getSilverObjectId(final MediaPK mediaPK) {

    int silverObjectId;
    try {
      silverObjectId = getGalleryContentManager().getSilverObjectId(mediaPK.getId(), mediaPK
          .getInstanceId());

      if (silverObjectId == -1) {
        Media media = getMedia(mediaPK, MediaCriteria.VISIBILITY.FORCE_GET_ALL);
        silverObjectId = Transaction.performInOne(() -> {
          final Connection con = openConnection();
          try {
            return createSilverContent(con, media, media.getCreatorId());
          } finally {
            DBUtil.close(con);
          }
        });
      }
    } catch (final Exception e) {
      throw new GalleryRuntimeException("DefaultGalleryService.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR, "gallery.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
    return silverObjectId;
  }

  private int createSilverContent(final Connection con, final Media media, final String creatorId) {

    try {
      return getGalleryContentManager().createSilverContent(con, media, creatorId);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("DefaultGalleryService.createSilverContent()",
          SilverpeasRuntimeException.ERROR, "gallery.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  @Override
  public Collection<Media> search(final QueryDescription query) {
    final Collection<Media> mediaList = new ArrayList<>();
    try {
      final List<MatchingIndexEntry> result = SearchEngineProvider.getSearchEngine().search(query).
          getEntries();
      // création des médias à partir des résultats
      // Ne retourne que les médias
      result.stream()
          .filter(matchIndex -> MediaType.from(matchIndex.getObjectType()) != MediaType.Unknown)
          .forEach(matchIndex -> {
            final MediaPK mediaPK = new MediaPK(matchIndex.getObjectId());
            final Media media = getMedia(mediaPK);
            if (media != null) {
              mediaList.add(media);
            }
          });
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.search()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_ADD_OBJECT", e);
    }
    return mediaList;
  }

  @Override
  public Collection<Media> getAllMediaThatWillBeNotVisible(final int nbDays) {
    try {
      return MediaDAO.findByCriteria(MediaCriteria.fromNbDaysBeforeThatMediaIsNotVisible(nbDays)
          .withVisibility(MediaCriteria.VISIBILITY.FORCE_GET_ALL));
    } catch (final Exception e) {
      throw new GalleryRuntimeException("DefaultGalleryService.getAllMediaThatWillBeNotVisible()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public String createOrder(final Collection<String> basket, final String userId,
      final String componentId) {
    try {
      return OrderDAO.createOrder(basket, userId, componentId);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("DefaultGalleryService.createOrder()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_REQUEST_NOT_CREATE", e);
    }
  }

  @Override
  public List<Order> getAllOrders(final String userId, final String instanceId) {
    try {
      return OrderDAO.findByCriteria(
          MediaOrderCriteria.fromComponentInstanceId(instanceId).withOrdererId(userId));
    } catch (final Exception e) {
      throw new GalleryRuntimeException("DefaultGalleryService.getAllOrders()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_REQUEST_LIST_NOT_EXIST", e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void updateOrder(final Order order) {
    try {
      OrderDAO.updateOrder(order);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("DefaultGalleryService.updateORder()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_REQUEST_ORDER_NOT_EXIST", e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void updateOrderRow(final OrderRow row) {
    try {
      OrderDAO.updateOrderRow(row);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("DefaultGalleryService.updateOrderRow()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_REQUEST_ORDER_ROW_NOT_EXIST", e);
    }
  }

  @Override
  public Order getOrder(final String orderId, final String instanceId) {
    try {
      return OrderDAO.getByCriteria(
          MediaOrderCriteria.fromComponentInstanceId(instanceId).identifierIsOneOf(orderId));
    } catch (final Exception e) {
      throw new GalleryRuntimeException("DefaultGalleryService.getOrder()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ORDER_NOT_EXIST", e);
    }
  }

  @Override
  public List<Order> getAllOrderToDelete(final int nbDays) {
    try {
      return OrderDAO.findByCriteria(MediaOrderCriteria.fromNbDaysAfterThatDeleteAnOrder(nbDays));
    } catch (final Exception e) {
      throw new GalleryRuntimeException("DefaultGalleryService.getAllOrderToDelete()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ORDER_NOT_EXIST", e);
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
      throw new GalleryRuntimeException("DefaultGalleryService.deleteOrders()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ORDER_NOT_EXIST", e);
    }
  }

  private GalleryContentManager getGalleryContentManager() {
    return new GalleryContentManager();
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
      throw new GalleryRuntimeException("DefaultGalleryService.getAllMediaByUserId()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
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
      return MediaDAO
          .getSocialInformationListOfMyContacts(listOfUserId, availableComponent, period);
    } catch (final SQLException e) {
      throw new GalleryRuntimeException("DefaultGalleryService.getSocialInformationListOfMyContacts()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    }
  }

  @Override
  public void sortAlbums(final List<NodePK> nodePKs) {
    try (final Connection con = openConnection()) {
      NodeDAO.sortNodes(con, nodePKs);
    } catch (final SQLException e) {
      throw new GalleryRuntimeException("sortAlbums()", SilverpeasRuntimeException.ERROR,
          "gallery.MSG_NOT_ORDER", e);
    }
  }

  /**
   * Executes a process list
   * @param processList the list of processes to execute.
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void executeProcessList(final ProcessList<GalleryProcessExecutionContext> processList,
      final GalleryProcessExecutionContext processExecutionContext) {
    try (final Connection connection = openConnection()) {
      processExecutionContext.setConnection(connection);
      ProcessProvider.getProcessManagement().execute(processList, processExecutionContext);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("executeProcessList()", SilverpeasRuntimeException.ERROR,
          "gallery.TRANSACTION_ERROR", e);
    }
  }
}
