package com.silverpeas.gallery.control.ejb;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ejb.EJBObject;

import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.gallery.model.Order;
import com.silverpeas.gallery.model.OrderRow;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * @author
 */
public interface GalleryBm extends EJBObject {
  // les albums ...
  public AlbumDetail getAlbum(NodePK nodePK, boolean viewAllPhoto)
      throws RemoteException;

  public NodePK createAlbum(AlbumDetail album, NodePK nodePK)
      throws RemoteException;

  public void updateAlbum(AlbumDetail album) throws RemoteException;

  public void deleteAlbum(NodePK nodePK) throws RemoteException;

  public Collection getAllAlbums(String instanceId) throws RemoteException;

  public void setPhotoPath(String photoId, String[] albums, String instanceId)
      throws RemoteException;

  public void updatePhotoPath(String photoId, String[] albums,
      String instanceIdFrom, String instanceIdTo) throws RemoteException;

  // les photos ...
  public PhotoDetail getPhoto(PhotoPK photoPK) throws RemoteException;

  public Collection getAllPhoto(NodePK nodePK, boolean viewAllPhoto)
      throws RemoteException;

  public Collection getAllPhotos(String instanceId) throws RemoteException;

  public String createPhoto(PhotoDetail photo, String albumId)
      throws RemoteException;

  public void updatePhoto(PhotoDetail photo) throws RemoteException;

  public void deletePhoto(PhotoPK photoPK) throws RemoteException;

  public Collection getDernieres(String instanceId, boolean viewAllPhoto)
      throws RemoteException;

  public Collection getAllPhotoEndVisible(int nbDays) throws RemoteException;

  public void notifyUsers(NotificationMetaData notifMetaData, String senderId,
      String instanceId) throws RemoteException;

  public Collection getNotVisible(String instanceId) throws RemoteException;

  // ...
  public Collection getPath(NodePK nodePK) throws RemoteException;

  public Collection getPathList(String instanceId, String photoId)
      throws RemoteException;

  public String getHTMLNodePath(NodePK nodePK) throws RemoteException;

  public void createIndex(PhotoDetail photo) throws RemoteException;

  public void deleteIndex(PhotoPK photoPK) throws RemoteException;

  public void indexGallery(String instanceId) throws RemoteException;

  public int getSilverObjectId(PhotoPK photoPK) throws RemoteException;

  public Collection search(QueryDescription query) throws RemoteException;

  // les demandes
  public String createOrder(Collection basket, String userId, String instanceId)
      throws RemoteException;

  public Order getOrder(String orderId, String instanceId)
      throws RemoteException;

  public List getAllOrders(String userId, String instanceId)
      throws RemoteException;

  // public List getPhotosOrder(String orderId) throws RemoteException;
  public Date getDownloadDate(String orderId, String photoId)
      throws RemoteException;

  public void updateOrderRow(OrderRow row) throws RemoteException;

  public void updateOrder(Order order) throws RemoteException;

  public Collection getAllOrderToDelete(int nbDays) throws RemoteException;

  public void deleteOrder(String orderId) throws RemoteException;

}
