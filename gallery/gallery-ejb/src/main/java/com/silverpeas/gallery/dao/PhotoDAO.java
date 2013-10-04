/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;
import com.silverpeas.gallery.model.PhotoWithStatus;
import com.silverpeas.gallery.socialNetwork.SocialInformationGallery;
import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.exception.UtilException;

public class PhotoDAO {

  private static String nullBeginDate =  "0000/00/00";
  private static String nullEndDate = "9999/99/99";

  public static PhotoDetail getPhoto(Connection con, int photoId)
      throws SQLException {
    // récupérer une photo
    PhotoDetail photo = new PhotoDetail();
    String query = "select * from SC_Gallery_Photo where photoId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, photoId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        // recuperation des colonnes du resulSet et construction de l'objet
        // photo
        photo = recupPhoto(rs);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return photo;
  }

  public static Collection<PhotoDetail> getAllPhotosSorted(Connection con, String albumId,
      String instanceId, HashMap<String, String> sortingParameters, boolean viewAllPhoto)
      throws SQLException {
    // récupérer toutes les photos d'un album
    ArrayList<PhotoDetail> listPhoto = null;
    Date today = new Date();

    String fieldOrdering = sortingParameters.get("fieldName");
    String fieldOrderingType = sortingParameters.get("sortType");

    String query =
        "select * from SC_Gallery_Photo P, SC_Gallery_Path A "
            +
            "where P.photoId = A.photoId and P.instanceId = A.instanceId and A.nodeId = ? and P.instanceId = ? order by P."
            + fieldOrdering + " " + fieldOrderingType;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(albumId));
      prepStmt.setString(2, instanceId);
      rs = prepStmt.executeQuery();
      listPhoto = new ArrayList<PhotoDetail>();
      while (rs.next()) {
        PhotoDetail photo = recupPhoto(rs);
        if (viewAllPhoto || isVisible(photo, today)) {
          listPhoto.add(photo);
        }
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listPhoto;
  }

  public static Collection<PhotoDetail> getAllPhoto(Connection con, String albumId,
      String instanceId, boolean viewAllPhoto) throws SQLException {
    // récupérer toutes les photos d'un album
    ArrayList<PhotoDetail> listPhoto = null;
    Date today = new Date();

    String query =
        "select * from SC_Gallery_Photo P, SC_Gallery_Path A "
            + "where P.photoId = A.photoId and P.instanceId = A.instanceId and A.nodeId = ? and P.instanceId = ? order by P.photoId";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(albumId));
      prepStmt.setString(2, instanceId);
      rs = prepStmt.executeQuery();
      listPhoto = new ArrayList<PhotoDetail>();
      while (rs.next()) {
        PhotoDetail photo = recupPhoto(rs);
        if (viewAllPhoto || isVisible(photo, today)) {
          listPhoto.add(photo);
        }
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listPhoto;
  }

  public static Collection<PhotoDetail> getPhotoNotVisible(Connection con, String instanceId)
      throws SQLException {
    // récupérer les photos qui ne sont plus visibles pour l'instance
    ArrayList<PhotoDetail> listPhoto = null;
    Date today = new Date();
    String dateToday = DateUtil.date2SQLDate(today);

    String query = "select * from SC_Gallery_Photo  where instanceId = ? "
        + "and (beginDate > ? or endDate < ? )";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, dateToday);
      prepStmt.setString(3, dateToday);
      rs = prepStmt.executeQuery();
      listPhoto = new ArrayList<PhotoDetail>();
      while (rs.next()) {
        PhotoDetail photo = recupPhoto(rs);
        listPhoto.add(photo);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listPhoto;
  }

  public static Collection<PhotoDetail> getAllPhotos(Connection con, String instanceId)
      throws SQLException {
    // récupérer toutes les photos de l'instance
    ArrayList<PhotoDetail> listPhoto = null;

    String query = "select * from SC_Gallery_Photo where instanceId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      rs = prepStmt.executeQuery();
      listPhoto = new ArrayList<PhotoDetail>();
      while (rs.next()) {
        PhotoDetail photo = recupPhoto(rs);
        listPhoto.add(photo);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listPhoto;
  }

  public static Collection<PhotoDetail> getAllPhotoEndVisible(Connection con, int nbDays)
      throws SQLException {
    // récupérer toutes les photos de l'instance ayant une date de visibilité
    // arrivant à terme dans 'nbDays' jours
    ArrayList<PhotoDetail> listPhoto = null;

    // calcul de la date de fin de visibilité
    Calendar calendar = Calendar.getInstance(Locale.FRENCH);

    calendar.add(Calendar.DATE, nbDays);
    Date date = calendar.getTime();
    String dateLimite = DateUtil.date2SQLDate(date);

    SilverTrace.debug("gallery", "PhotoDAO.getAllPhotoEndVisible()",
        "root.MSG_GEN_PARAM_VALUE", "dateLimite = " + dateLimite);

    String query = "select * from SC_Gallery_Photo where endDate = ? order by instanceId";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, dateLimite);
      rs = prepStmt.executeQuery();
      listPhoto = new ArrayList<PhotoDetail>();
      while (rs.next()) {
        PhotoDetail photo = recupPhoto(rs);
        listPhoto.add(photo);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listPhoto;
  }

  public static String createPhoto(Connection con, PhotoDetail photo)
      throws SQLException, UtilException {
    // Création d'une nouvelle photo
    PhotoDetail newPhoto = photo;
    String id = "";
    PreparedStatement prepStmt = null;
    try {
      int newId = DBUtil.getNextId("SC_Gallery_Photo", "photoId");
      id = Integer.toString(newId);
      // création de la requete
      String query =
          "insert into SC_Gallery_Photo (photoId,title,description,sizeH,sizeL,creationDate,updateDate,vueDate"
              + ",author,download,albumLabel,status,albumId,creatorId,updateId,instanceId,imageName,imageSize,beginDate,endDate"
              + ",imageMimeType,keyWord, beginDownloadDate, endDownloadDate) "
              + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      // initialisation des paramètres
      prepStmt = con.prepareStatement(query);
      initParam(prepStmt, newId, newPhoto);
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
    return id;
  }

  public static String createPath(Connection con, PhotoDetail photo,
      String albumId) throws SQLException, UtilException {
    // Création d'un emplacement
    String id = "";
    PreparedStatement prepStmt = null;
    try {
      int newId = DBUtil.getNextId("SC_Gallery_Path", "photoId");
      id = Integer.toString(newId);
      // création de la requete
      String query = "insert into SC_Gallery_Path (photoId, nodeId, instanceId) values (?,?,?)";
      // initialisation des paramètres
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(photo.getId()));
      prepStmt.setInt(2, Integer.parseInt(albumId));
      prepStmt.setString(3, photo.getInstanceId());
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
    return id;
  }

  public static void updatePhoto(Connection con, PhotoDetail photo)
      throws SQLException {
    PhotoDetail updatedPhoto = photo;
    PreparedStatement prepStmt = null;
    try {
      String query =
          "update SC_Gallery_Photo set photoId = ? , title = ? , description = ? , sizeH = ? ,"
              + " sizeL = ? , creationDate = ? , updateDate = ? , vueDate = ? , author = ? ,"
              + " download = ? , albumLabel = ? , status = ? , albumId = ? , creatorId = ? , updateId = ? , instanceId = ? ,"
              + " imageName = ? , imageSize = ? , beginDate = ? , endDate = ? , imageMimeType = ? , keyWord = ? ,"
              + " beginDownloadDate = ?, endDownloadDate = ? "
              + " where photoId = ? ";
      // initialisation des paramètres
      prepStmt = con.prepareStatement(query);
      int photoId = Integer.parseInt(updatedPhoto.getPhotoPK().getId());
      initParam(prepStmt, photoId, updatedPhoto);
      // initialisation du dernier paramètre
      prepStmt.setInt(25, photoId);
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  public static void removePhoto(Connection con, int photoId)
      throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      String query = "delete from SC_Gallery_Photo where photoId = ? ";
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, photoId);
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  public static Collection<PhotoDetail> getDernieres(Connection con, String instanceId,
      boolean viewAllPhoto) throws SQLException {
    // récupérer toutes les photos d'un album
    ArrayList<PhotoDetail> listPhoto = null;
    Date today = new Date();

    String query =
        "select * from SC_Gallery_Photo where instanceId = ? order by creationDate desc,photoId desc";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      rs = prepStmt.executeQuery();
      listPhoto = new ArrayList<PhotoDetail>();
      while (rs.next()) {
        PhotoDetail photo = recupPhoto(rs);
        if (viewAllPhoto || isVisible(photo, today)) {
          listPhoto.add(photo);
        }
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listPhoto;
  }

  public static boolean isVisible(PhotoDetail photo, Date today) {
    boolean result = false;
    Date beginDate = photo.getBeginDate();
    Date endDate = photo.getEndDate();

    if (!StringUtil.isDefined(String.valueOf(beginDate))
        && !StringUtil.isDefined(String.valueOf(endDate))) {
      result = true;
    } else {
      if (StringUtil.isDefined(String.valueOf(beginDate))
          && !StringUtil.isDefined(String.valueOf(endDate))) {
        result = beginDate.compareTo(today) <= 0;
      }
      if (!StringUtil.isDefined(String.valueOf(beginDate))
          && StringUtil.isDefined(String.valueOf(endDate))) {
        result = endDate.compareTo(today) >= 0;
      }
      if (StringUtil.isDefined(String.valueOf(beginDate))
          && StringUtil.isDefined(String.valueOf(endDate))) {
        result = beginDate.compareTo(today) <= 0
            && endDate.compareTo(today) >= 0;
      }
    }
    return result;
  }

  public static Collection<String> getPathList(Connection con, String instanceId,
      String photoId) throws SQLException {
    // récupérer la liste des emplacements de la photo
    ArrayList<String> listPath = null;

    String query =
        "select N.NodeId from SC_Gallery_Path P, SB_Node_Node N "
            + "where P.PhotoId = ? and N.nodeId = P.NodeId and P.instanceId = ? and N.instanceId = P.instanceId ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(photoId));
      prepStmt.setString(2, instanceId);
      rs = prepStmt.executeQuery();
      listPath = new ArrayList<String>();
      while (rs.next()) {
        int nodeId = rs.getInt(1);
        listPath.add(Integer.toString(nodeId));
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listPath;
  }

  public static void deletePhotoPath(Connection con, String photoId,
      String instanceId) throws SQLException {
    SilverTrace.debug("gallery", "PhotoDAO.deleteAlbumPath()",
        "root.MSG_GEN_PARAM_VALUE", "photoId = " + photoId);
    // suppression de tous les emplacements
    PreparedStatement prepStmt = null;
    try {
      // création de la requete
      String query = "delete from SC_Gallery_Path where photoId = ? and instanceId = ? ";
      // initialisation des paramètres
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(photoId));
      prepStmt.setString(2, instanceId);
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  public static void addPhotoPath(Connection con, String photoId,
      String albumId, String instanceId) throws SQLException {
    SilverTrace.debug("gallery", "PhotoDAO.addPhotoPath()",
        "root.MSG_GEN_PARAM_VALUE", "photoId = " + photoId + " albumId = "
        + albumId);
    // ajout d'un emplacement
    PreparedStatement prepStmt = null;
    try {
      // création de la requete
      String query = "insert into SC_Gallery_Path values (?,?,?)";
      // initialisation des paramètres
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(photoId));
      prepStmt.setInt(2, Integer.parseInt(albumId));
      prepStmt.setString(3, instanceId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  private static PhotoDetail recupPhoto(ResultSet rs) throws SQLException {
    PhotoDetail photo = new PhotoDetail();
    // recuperation des colonnes du resulSet et construction de l'objet photo
    String photoId = rs.getString(1);
    String title = rs.getString(2);
    String description = rs.getString(3);
    int sizeH = rs.getInt(4);
    int sizeL = rs.getInt(5);
    String creationDate = rs.getString(6);
    String updateDate = rs.getString(7);
    String vueDate = rs.getString(8);
    String author = rs.getString(9);
    boolean download = false;
    if (rs.getInt(10) == 1) {
      download = true;
    }
    boolean albumLabel = false;
    if (rs.getInt(11) == 1) {
      albumLabel = true;
    }
    String status = rs.getString(12);
    // String albumId = new Integer(rs.getInt(13)).toString();
    String albumId = "";
    String creatorId = rs.getString(14);
    String updateId = rs.getString(15);
    String instanceId = rs.getString(16);
    String imageName = rs.getString(17);
    int imageSize = rs.getInt(18);
    String imageMimeType = rs.getString(19);
    String beginDate = rs.getString(20);
    String endDate = rs.getString(21);
    String keyWord = rs.getString(22);
    String beginDownloadDate = rs.getString(23);
    String endDownloadDate = rs.getString(24);

    PhotoPK photoPK = new PhotoPK(photoId, instanceId);
    photo.setPhotoPK(photoPK);
    photo.setTitle(title);
    photo.setDescription(description);
    photo.setSizeH(sizeH);
    photo.setSizeL(sizeL);
    try {
      photo.setCreationDate(DateUtil.parse(creationDate));
      if (updateDate != null) {
        photo.setUpdateDate(DateUtil.parse(updateDate));
      }
    } catch (Exception e) {
      throw new SQLException(e.getMessage());
    }

    photo.setVueDate(vueDate);
    photo.setAuthor(author);
    photo.setDownload(download);
    photo.setAlbumLabel(albumLabel);
    photo.setStatus(status);
    photo.setAlbumId(albumId);
    photo.setCreatorId(creatorId);
    photo.setUpdateId(updateId);
    photo.setImageName(imageName);
    photo.setImageSize(imageSize);
    photo.setImageMimeType(imageMimeType);
    photo.setKeyWord(keyWord);
    try {
      if (beginDownloadDate != null) {
        photo.setBeginDownloadDate(DateUtil.parse(beginDownloadDate));
      }
      if (endDownloadDate != null) {
        photo.setEndDownloadDate(DateUtil.parse(endDownloadDate));
      }
    } catch (Exception e) {
      throw new SQLException(e.getMessage());
    }
    if (nullBeginDate.equals(beginDate)) {
      beginDate = null;
    }
    if (nullEndDate.equals(endDate)) {
      endDate = null;
    }
    try {
      if (beginDate != null) {
        photo.setBeginDate(DateUtil.parse(beginDate));
      }
      if (endDate != null) {
        photo.setEndDate(DateUtil.parse(endDate));
      }
    } catch (Exception e) {
      throw new SQLException(e.getMessage());
    }

    return photo;
  }

  private static void initParam(PreparedStatement prepStmt, int photoId,
      PhotoDetail photo) throws SQLException {
    prepStmt.setInt(1, photoId);
    prepStmt.setString(2, photo.getTitle());
    prepStmt.setString(3, photo.getDescription());
    prepStmt.setInt(4, photo.getSizeH());
    prepStmt.setInt(5, photo.getSizeL());
    prepStmt.setString(6, DateUtil.date2SQLDate(photo.getCreationDate()));
    if (photo.getUpdateDate() != null) {
      prepStmt.setString(7, DateUtil.date2SQLDate(photo.getUpdateDate()));
    } else {
      prepStmt.setString(7, null);
    }
    prepStmt.setString(8, photo.getVueDate());
    prepStmt.setString(9, photo.getAuthor());
    if (photo.isDownload()) {
      prepStmt.setInt(10, 1);
    } else {
      prepStmt.setInt(10, 0);
    }
    if (photo.isAlbumLabel()) {
      prepStmt.setInt(11, 1);
    } else {
      prepStmt.setInt(11, 0);
    }
    prepStmt.setString(12, photo.getStatus());
    // on met "0" dans l'albumId qui n'est plus utilisé
    prepStmt.setString(13, "0");
    prepStmt.setString(14, photo.getCreatorId());
    prepStmt.setString(15, photo.getUpdateId());
    prepStmt.setString(16, photo.getInstanceId());
    prepStmt.setString(17, photo.getImageName());
    prepStmt.setLong(18, photo.getImageSize());
    if (photo.getBeginDate() != null) {
      prepStmt.setString(19, DateUtil.date2SQLDate(photo.getBeginDate()));
    } else {
      prepStmt.setString(19, nullBeginDate);
    }

    if (photo.getEndDate() != null) {
      prepStmt.setString(20, DateUtil.date2SQLDate(photo.getEndDate()));
    } else {
      prepStmt.setString(20, nullEndDate);
    }
    prepStmt.setString(21, photo.getImageMimeType());
    prepStmt.setString(22, photo.getKeyWord());
    if (photo.getBeginDownloadDate() != null) {
      prepStmt.setString(23, DateUtil.date2SQLDate(photo.getBeginDownloadDate()));
    } else {
      prepStmt.setString(23, null);
    }

    if (photo.getEndDownloadDate() != null) {
      prepStmt.setString(24, DateUtil.date2SQLDate(photo.getEndDownloadDate()));
    } else {
      prepStmt.setString(24, null);
    }
  }

  /**
   * @param userId ID of user
   * @return
   * @throws SQLException
   * @throws ParseException
   **/
  public static List<String> getAllPhotosIDbyUserid(Connection con, String userId) throws
      SQLException {
    List<String> listPhoto = new ArrayList<String>();
    String query =
        "(SELECT creationdate AS dateinformation, photoId,'new'as type FROM SC_Gallery_Photo  WHERE creatorid = ?) "
            + "UNION (SELECT updatedate AS dateinformation, photoId ,'update'as type FROM sc_gallery_photo  WHERE  updateid = ? ) "
            + "ORDER BY dateinformation DESC, photoId DESC ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, userId);
      prepStmt.setString(2, userId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        listPhoto.add(rs.getString("photoId"));
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }

    return listPhoto;

  }

  /**
   * get my SocialInformationGallery accordint to the type of data base used(PostgreSQL,Oracle,MMS)
   * .
   * @param con
   * @param userId
   * @param firstIndex
   * @param nbElement
   * @return List<SocialInformation>
   * @throws SQLException
   * @throws ParseException
   */
  public static List<SocialInformation> getAllPhotosIDbyUserid(Connection con,
      String userId, Date begin, Date end) throws SQLException {
    List<SocialInformation> listPhoto = new ArrayList<SocialInformation>();
    String query =
        "(SELECT creationdate AS dateinformation, photoId, 'new' as type FROM SC_Gallery_Photo WHERE creatorid = ? and creationdate >= ? and creationdate <= ? ) "
            + "UNION (SELECT updatedate AS dateinformation, photoId , 'update' as type FROM sc_gallery_photo WHERE updateid = ?  and updatedate >= ? and updatedate <= ? ) "
            + "ORDER BY dateinformation DESC, photoId DESC";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, userId);
      prepStmt.setString(2, DateUtil.date2SQLDate(begin));
      prepStmt.setString(3, DateUtil.date2SQLDate(end));
      prepStmt.setString(4, userId);
      prepStmt.setString(5, DateUtil.date2SQLDate(begin));
      prepStmt.setString(6, DateUtil.date2SQLDate(end));
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        PhotoDetail pd = getPhoto(con, rs.getInt(2));
        PhotoWithStatus withStatus = new PhotoWithStatus(pd, "update".equalsIgnoreCase(rs.getString(3)));
        listPhoto.add(new SocialInformationGallery(withStatus));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return listPhoto;
  }

  /**
   * get list of socialInformationGallery of my contacts according to the type of data base
   * used(PostgreSQL,Oracle,MMS) .
   * @param con
   * @param listOfuserId
   * @param availableComponent
   * @param numberOfElement
   * @param firstIndex
   * @return
   * @throws SQLException
   * @throws ParseException
   */
  public static List<SocialInformation> getSocialInformationsListOfMyContacts(Connection con,
      List<String> listOfuserId, List<String> availableComponent, Date begin, Date end) throws SQLException {
    List<SocialInformation> listPhoto = new ArrayList<SocialInformation>();
    String query =
        "(SELECT creationdate AS dateinformation, photoId, 'new' as type FROM sc_gallery_photo"
            + " WHERE creatorid IN (" + toSqlString(listOfuserId) + ") AND instanceid IN (" +
            toSqlString(availableComponent) + ") AND creationdate >= ? AND creationdate <= ?)"
            +
            "UNION (SELECT updatedate AS dateinformation, photoId, 'update' as type FROM sc_gallery_photo"
            + " WHERE updateid IN (" + toSqlString(listOfuserId) + ") AND instanceid IN (" +
            toSqlString(availableComponent) + ") AND updatedate >= ? AND updatedate <= ?)"
            + " ORDER BY dateinformation DESC, photoId DESC";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, DateUtil.date2SQLDate(begin));
      prepStmt.setString(2, DateUtil.date2SQLDate(end));
      prepStmt.setString(3, DateUtil.date2SQLDate(begin));
      prepStmt.setString(4, DateUtil.date2SQLDate(end));
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        PhotoDetail pd = getPhoto(con, rs.getInt(2));
        PhotoWithStatus withStatus = new PhotoWithStatus(pd, "update".equalsIgnoreCase(rs.getString(3)));
        listPhoto.add(new SocialInformationGallery(withStatus));
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listPhoto;
  }

  /**
   * tronsform the list of string to String for using in query sql
   * @param list
   * @return String
   */
  private static String toSqlString(List<String> list) {
    StringBuilder result = new StringBuilder(100);
    if (list == null || list.isEmpty()) {
      return "''";
    }
    int i = 0;
    for (String var : list) {
      if (i != 0) {
        result.append(",");
      }
      result.append("'").append(var).append("'");
      i++;
    }
    return result.toString();
  }
}
