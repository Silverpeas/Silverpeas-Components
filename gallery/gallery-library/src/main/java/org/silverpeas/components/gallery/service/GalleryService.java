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

import org.silverpeas.components.gallery.Watermark;
import org.silverpeas.components.gallery.delegate.GalleryPasteDelegate;
import org.silverpeas.components.gallery.delegate.MediaDataCreateDelegate;
import org.silverpeas.components.gallery.delegate.MediaDataUpdateDelegate;
import org.silverpeas.components.gallery.model.AlbumDetail;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.MediaCriteria;
import org.silverpeas.components.gallery.model.MediaPK;
import org.silverpeas.components.gallery.model.Order;
import org.silverpeas.components.gallery.model.OrderRow;
import org.silverpeas.components.gallery.model.Photo;
import org.silverpeas.core.ApplicationService;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.socialnetwork.model.SocialInformation;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GalleryService extends ApplicationService {

  @Override
  @SuppressWarnings("unchecked")
  Optional<Media> getContributionById(final ContributionIdentifier contributionId);

  AlbumDetail getAlbum(NodePK nodePK);

  AlbumDetail getAlbum(NodePK nodePK, MediaCriteria.VISIBILITY visibility);

  NodePK createAlbum(AlbumDetail album, NodePK nodePK);

  void updateAlbum(AlbumDetail album);

  void deleteAlbum(UserDetail user, String componentInstanceId, NodePK nodePK);

  Collection<AlbumDetail> getAllAlbums(String instanceId);

  void removeMediaFromAllAlbums(Media media);

  void addMediaToAlbums(Media media, String... albums);

  Photo getPhoto(MediaPK mediaPK);

  Media getMedia(MediaPK mediaPK);

  Media getMedia(MediaPK mediaPK, MediaCriteria.VISIBILITY visibility);

  List<Media> getMedia(List<String> mediaIds, final String componentInstanceId);

  List<Media> getMedia(List<String> mediaIds, final String componentInstanceId,
      MediaCriteria.VISIBILITY visibility);

  Collection<Photo> getAllPhotos(NodePK nodePK);

  Collection<Photo> getAllPhotos(NodePK nodePK, MediaCriteria.VISIBILITY visibility);

  long countAllMedia(NodePK nodePK);

  long countAllMedia(NodePK nodePK, MediaCriteria.VISIBILITY visibility);

  Collection<Media> getAllMedia(NodePK nodePK, MediaCriteria.VISIBILITY visibility);

  Collection<Media> getAllMedia(String instanceId, MediaCriteria.VISIBILITY visibility);

  void paste(UserDetail user, String componentInstanceId, GalleryPasteDelegate delegate);

  void importFromRepository(UserDetail user, String componentInstanceId, File repository,
      MediaDataCreateDelegate delegate);

  Media createMedia(UserDetail user, String componentInstanceId, Watermark watermark,
      MediaDataCreateDelegate delegate);

  void updateMedia(UserDetail user, String componentInstanceId, Collection<String> mediaIds,
      String albumId, MediaDataUpdateDelegate delegate);

  void updateMedia(UserDetail user, String componentInstanceId, Media media, Watermark watermark,
      MediaDataUpdateDelegate delegate);

  void deleteMedia(UserDetail user, String componentInstanceId, Collection<String> mediaIds);

  List<Media> getLastRegisteredMedia(String instanceId);

  Collection<Media> getAllMediaThatWillBeNotVisible(int nbDays);

  Collection<Media> getNotVisible(String instanceId);

  Collection<NodeDetail> getPath(NodePK nodePK);

  Collection<String> getAlbumIdsOf(Media media);

  void indexGallery(final UserDetail user, String instanceId);

  int getSilverObjectId(MediaPK mediaPK);

  Collection<Media> search(QueryDescription query);

  String createOrder(Collection<String> basket, String userId, String instanceId);

  Order getOrder(String orderId, String instanceId);

  List<Order> getAllOrders(String userId, String instanceId);

  void updateOrderRow(OrderRow row);

  void updateOrder(Order order);

  List<Order> getAllOrderToDelete(int nbDays);

  void deleteOrders(List<Order> orders);

  /**
   * Gets the list of social information about the media for the specified user and in the given
   * period of time.
   * @param userId the unique identifier of a user
   * @param period interval of time in which the media were created or updated.
   * @return a list of {@link SocialInformation} objects.
   */
  List<SocialInformation> getAllMediaByUserId(String userId, Period period);

  /**
   * Gets the list of social information of the specified users about all the media in the given
   * component instances and in the specified period of time.
   * @param listOfUserId a list of unique identifier of users.
   * @param availableComponent a list of unique identifier of component instances.
   * @param period interval of time in which the media were created or updated.
   * @return a list of {@link SocialInformation} objects.
   */
  List<SocialInformation> getSocialInformationListOfMyContacts(List<String> listOfUserId,
      List<String> availableComponent, Period period);

  void sortAlbums(List<NodePK> albumIds);
}
