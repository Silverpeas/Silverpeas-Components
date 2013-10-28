/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.control.ejb;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.ejb.Local;

import org.silverpeas.process.util.ProcessList;
import org.silverpeas.search.searchEngine.model.QueryDescription;

import com.silverpeas.gallery.delegate.GalleryPasteDelegate;
import com.silverpeas.gallery.delegate.PhotoDataCreateDelegate;
import com.silverpeas.gallery.delegate.PhotoDataUpdateDelegate;
import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.gallery.model.Order;
import com.silverpeas.gallery.model.OrderRow;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;
import com.silverpeas.gallery.process.GalleryProcessExecutionContext;
import com.silverpeas.socialnetwork.model.SocialInformation;

import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

@Local
public interface GalleryBm {

  public AlbumDetail getAlbum(NodePK nodePK, boolean viewAllPhoto);

  public NodePK createAlbum(AlbumDetail album, NodePK nodePK);

  public void updateAlbum(AlbumDetail album);

  public void deleteAlbum(UserDetail user, String componentInstanceId, NodePK nodePK);

  public Collection<AlbumDetail> getAllAlbums(String instanceId);

  public void setPhotoPath(String photoId, String instanceId, String... albums);

  public void addPhotoPaths(String photoId, String instanceId, String... albums);

  public void updatePhotoPath(String photoId, String instanceIdFrom,
      String instanceIdTo, String... albums);

  // les photos ...
  public PhotoDetail getPhoto(PhotoPK photoPK);

  public Collection<PhotoDetail> getAllPhoto(NodePK nodePK, boolean viewAllPhoto);

  public Collection<PhotoDetail> getAllPhotosSorted(NodePK nodePK,
      HashMap<String, String> parsedParameters, boolean viewAllPhoto);

  public Collection<PhotoDetail> getAllPhotos(String instanceId);

  public void paste(UserDetail user, String componentInstanceId, GalleryPasteDelegate delegate);

  public void importFromRepository(UserDetail user, String componentInstanceId, File repository,
      boolean watermark, String watermarkHD, String watermarkOther, PhotoDataCreateDelegate delegate);

  public void createPhoto(UserDetail user, String componentInstanceId, PhotoDetail photo,
      boolean watermark, String watermarkHD, String watermarkOther, PhotoDataCreateDelegate delegate);

  public void updatePhoto(UserDetail user, String componentInstanceId, Collection<String> photoIds,
      String albumId, PhotoDataUpdateDelegate delegate);

  public void updatePhoto(UserDetail user, String componentInstanceId, PhotoDetail photo,
      boolean watermark, String watermarkHD, String watermarkOther, PhotoDataUpdateDelegate delegate);

  public void deletePhoto(UserDetail user, String componentInstanceId, Collection<String> photoIds);

  public Collection<PhotoDetail> getDernieres(String instanceId, boolean viewAllPhoto) throws
      RemoteException;

  public Collection<PhotoDetail> getAllPhotoEndVisible(int nbDays);

  public Collection<PhotoDetail> getNotVisible(String instanceId);

  public Collection<NodeDetail> getPath(NodePK nodePK);

  public Collection<String> getPathList(String instanceId, String photoId);

  public String getHTMLNodePath(NodePK nodePK);

  public void createIndex(PhotoDetail photo);

  public void indexGallery(String instanceId);

  public int getSilverObjectId(PhotoPK photoPK);

  public Collection<PhotoDetail> search(QueryDescription query);

  public String createOrder(Collection<String> basket, String userId, String instanceId);

  public Order getOrder(String orderId, String instanceId);

  public List<Order> getAllOrders(String userId, String instanceId);

  public Date getDownloadDate(String orderId, String photoId);

  public void updateOrderRow(OrderRow row);

  public void updateOrder(Order order);

  public Collection<Order> getAllOrderToDelete(int nbDays);

  public void deleteOrder(String orderId);

  /**
   * get my list of SocialInformationGallery according to options and number of Item and the first
   * Index
   *
   * @return: List <SocialInformation>
   * @param userId
   * @param begin
   * @param end
   * @return
   * @
   */
  public List<SocialInformation> getAllPhotosByUserid(String userId, Date begin, Date end);

  /**
   * get list of SocialInformationGallery of my contacts according to options and number of Item and
   * the first Index
   *
   * @param listOfuserId
   * @param availableComponent
   * @param begin
   * @param end
   * @return List<SocialInformation>
   * @
   */
  public List<SocialInformation> getSocialInformationsListOfMyContacts(List<String> listOfuserId,
      List<String> availableComponent, Date begin, Date end);

  public void sortAlbums(List<NodePK> albumIds);

  /**
   * Executes a process list
   *
   * @param processList
   * @param processExecutionContext
   * @throws Exception
   */
  public void executeProcessList(ProcessList<GalleryProcessExecutionContext> processList,
      GalleryProcessExecutionContext processExecutionContext);
}
