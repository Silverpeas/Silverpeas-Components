/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.control.ejb;


import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.gallery.model.Order;
import com.silverpeas.gallery.model.OrderRow;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;
import com.silverpeas.socialNetwork.model.SocialInformation;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public interface GalleryBmBusinessSkeleton {
  // les albums ...

  public AlbumDetail getAlbum(NodePK nodePK, boolean viewAllPhoto)
      throws RemoteException;
  
  public NodePK createAlbum(AlbumDetail album, NodePK nodePK)
      throws RemoteException;

  public void updateAlbum(AlbumDetail album) throws RemoteException;

  public void deleteAlbum(NodePK nodePK) throws RemoteException;

  public Collection<AlbumDetail> getAllAlbums(String instanceId) throws RemoteException;

  public void setPhotoPath(String photoId, String[] albums, String instanceId)
      throws RemoteException;
  
  public void addPhotoPaths(String photoId, String[] albums, String instanceId)
  throws RemoteException;

  public void updatePhotoPath(String photoId, String[] albums,
      String instanceIdFrom, String instanceIdTo) throws RemoteException;

  // les photos ...
  public PhotoDetail getPhoto(PhotoPK photoPK) throws RemoteException;

  public Collection<PhotoDetail> getAllPhoto(NodePK nodePK, boolean viewAllPhoto)
      throws RemoteException;
  
  public Collection<PhotoDetail> getAllPhotosSorted(NodePK nodePK,
      HashMap<String, String> parsedParameters, boolean viewAllPhoto) throws RemoteException;

  public Collection<PhotoDetail> getAllPhotos(String instanceId) throws RemoteException;

  public String createPhoto(PhotoDetail photo, String albumId)
      throws RemoteException;

  public void updatePhoto(PhotoDetail photo) throws RemoteException;

  public void deletePhoto(PhotoPK photoPK) throws RemoteException;

  public Collection<PhotoDetail> getDernieres(String instanceId, boolean viewAllPhoto) throws
      RemoteException;

  public Collection<PhotoDetail> getAllPhotoEndVisible(int nbDays) throws RemoteException;

  public Collection<PhotoDetail> getNotVisible(String instanceId) throws RemoteException;

  // ...
  public Collection<NodeDetail> getPath(NodePK nodePK) throws RemoteException;

  public Collection<String> getPathList(String instanceId, String photoId)
      throws RemoteException;

  public String getHTMLNodePath(NodePK nodePK) throws RemoteException;

  public void createIndex(PhotoDetail photo) throws RemoteException;

  public void deleteIndex(PhotoPK photoPK) throws RemoteException;

  public void indexGallery(String instanceId) throws RemoteException;

  public int getSilverObjectId(PhotoPK photoPK) throws RemoteException;

  public Collection<PhotoDetail> search(QueryDescription query) throws RemoteException;

  // les demandes
  public String createOrder(Collection<String> basket, String userId, String instanceId)
      throws RemoteException;

  public Order getOrder(String orderId, String instanceId)
      throws RemoteException;

  public List<Order> getAllOrders(String userId, String instanceId)
      throws RemoteException;

  // public List getPhotosOrder(String orderId) throws RemoteException;
  public Date getDownloadDate(String orderId, String photoId)
      throws RemoteException;

  public void updateOrderRow(OrderRow row) throws RemoteException;

  public void updateOrder(Order order) throws RemoteException;

  public Collection<Order> getAllOrderToDelete(int nbDays) throws RemoteException;

  public void deleteOrder(String orderId) throws RemoteException;

  /**
   * get my list of SocialInformationGallery
   * according to options and number of Item and the first Index
   * @return: List <SocialInformation>
   * @param userId
   * @param begin
   * @param end
   * @return 
   * @throws RemoteException
   */
  public List<SocialInformation> getAllPhotosByUserid(String userId, Date begin, Date end)
      throws RemoteException;

  /**
   * get list of SocialInformationGallery of my contacts
   * according to options and number of Item and the first Index
   * @param listOfuserId
   * @param availableComponent
   * @param begin
   * @param end
   * @return List<SocialInformation>
   * @throws RemoteException
   */
  public List<SocialInformation> getSocialInformationsListOfMyContacts(List<String> listOfuserId,
      List<String> availableComponent, Date begin, Date end) throws RemoteException;
  
  public void sortAlbums (List<NodePK> albumIds) throws RemoteException;
  
}
