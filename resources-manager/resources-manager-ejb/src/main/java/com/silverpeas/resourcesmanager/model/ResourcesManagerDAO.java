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
package com.silverpeas.resourcesmanager.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.UtilException;

public class ResourcesManagerDAO {

  private static String STATUS_VALIDATE = "V";
  private static String STATUS_REFUSED = "R";
  private static String STATUS_FOR_VALIDATION = "A";

  /*** Gestion des catégories ***/
  private static CategoryDetail resultSetToCategoryDetail(ResultSet rs)
      throws SQLException {
    boolean bookable = false;
    int id = rs.getInt("id");
    String name = rs.getString("name");
    Date creationDate = new Date(Long.parseLong(rs.getString("creationDate")));
    Date updateDate = new Date(Long.parseLong(rs.getString("updateDate")));
    String instanceId = rs.getString("instanceId");
    if (rs.getInt("bookable") == 1) {
      bookable = true;
    }
    String form = rs.getString("form");
    int responsibleId = rs.getInt("responsibleId");
    int createrId = rs.getInt("createrId");
    int updaterId = rs.getInt("updaterId");
    String description = rs.getString("description");
    CategoryDetail category = new CategoryDetail(Integer.toString(id),
        instanceId, name, creationDate, updateDate, bookable, form, Integer
        .toString(responsibleId), Integer.toString(createrId), Integer
        .toString(updaterId), description);
    return category;
  }

  public static int createCategory(Connection con, CategoryDetail category)
      throws SQLException {
    String query =
        "INSERT INTO SC_Resources_Category (id, instanceId, name, creationdate, updatedate," +
        "bookable, form, responsibleid, createrid, updaterid, description) " +
        "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
    PreparedStatement prepStmt = null;
    String instanceId = category.getInstanceId();
    String name = category.getName();
    Date creationdate = category.getCreationDate();
    Date updatedate = category.getUpdateDate();
    boolean bookable = category.getBookable();
    String form = category.getForm();
    String responsibleid = category.getResponsibleId();
    String createrid = category.getCreaterId();
    String updaterid = category.getUpdaterId();
    String description = category.getDescription();

    try {
      int id = DBUtil.getNextId("SC_Resources_Category", "id");
      // Preparation de la requête
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, id);
      prepStmt.setString(2, instanceId);
      prepStmt.setString(3, name);
      prepStmt.setString(4, Long.toString(creationdate.getTime()));
      prepStmt.setString(5, Long.toString(updatedate.getTime()));
      if (bookable == false) {
        prepStmt.setInt(6, 0);
      } else {
        prepStmt.setInt(6, 1);
      }
      prepStmt.setString(7, form);
      prepStmt.setInt(8, Integer.parseInt(responsibleid));
      prepStmt.setString(9, createrid);
      prepStmt.setString(10, updaterid);
      prepStmt.setString(11, description);
      prepStmt.executeUpdate();

      return id;
    } catch (UtilException e) {
      throw new SQLException();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void updateCategory(Connection con, CategoryDetail category)
      throws SQLException {
    String query =
        "UPDATE SC_Resources_Category SET instanceId=?, name=?, updatedate=?, bookable=?, " +
        "form=?, responsibleid=?, updaterid=?, description=? WHERE id=?";
    PreparedStatement prepStmt = null;
    // on récupère les informations de ContactDetail
    String id = category.getId();
    String instanceId = category.getInstanceId();
    String name = category.getName();
    Date updatedate = category.getUpdateDate();
    boolean bookable = category.getBookable();
    String form = category.getForm();
    String responsibleid = category.getResponsibleId();
    String updaterid = category.getUpdaterId();
    String description = category.getDescription();
    int idcategory = Integer.parseInt(id);
    int book = 0;
    if (bookable == true) {
      book = 1;
    }
    try {
      // Preparation de la requête
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, name);
      prepStmt.setString(3, Long.toString(updatedate.getTime()));
      prepStmt.setInt(4, book);
      prepStmt.setString(5, form);
      prepStmt.setInt(6, Integer.parseInt(responsibleid));
      prepStmt.setString(7, updaterid);
      prepStmt.setString(8, description);
      prepStmt.setInt(9, idcategory);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static List<CategoryDetail> getCategories(Connection con, String instanceId)
      throws SQLException {
    List<CategoryDetail> list = null;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    String query = "select * from SC_Resources_Category where instanceId = ?";
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);

      rs = prepStmt.executeQuery();
      list = new ArrayList<CategoryDetail>();
      while (rs.next()) {
        CategoryDetail category = resultSetToCategoryDetail(rs);
        list.add(category);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  public static CategoryDetail getCategory(Connection con, String id)
      throws SQLException {
    String query = "select * from SC_Resources_Category where id=?";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    CategoryDetail category = null;
    try {

      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(id));
      rs = prepStmt.executeQuery();
      if (rs.next())
        category = resultSetToCategoryDetail(rs);
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return category;
  }

  public static void deleteCategory(Connection con, String id)
      throws SQLException {
    PreparedStatement prepStmt = null;
    String query = "DELETE FROM SC_Resources_Category WHERE ID=?";
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(id));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /*** Gestion des Ressources ***/
  private static ResourceDetail resultSetToResourceDetail(ResultSet rs)
      throws SQLException {

    boolean book = false;
    int id = rs.getInt("id");
    String instanceId = rs.getString("instanceId");
    String categoryId = rs.getString("categoryid");
    String name = rs.getString("name");
    Date creationDate = new Date(Long.parseLong(rs.getString("creationDate")));
    Date updateDate = new Date(Long.parseLong(rs.getString("updateDate")));
    int bookable = rs.getInt("bookable");
    if (bookable == 1) {
      book = true;
    }
    int responsibleId = rs.getInt("responsibleId");
    int createrId = rs.getInt("createrId");
    int updaterId = rs.getInt("updaterId");
    String description = rs.getString("description");

    ResourceDetail resource = new ResourceDetail(Integer.toString(id),
        categoryId, name, creationDate, updateDate, description, Integer
        .toString(responsibleId), Integer.toString(createrId), Integer
        .toString(updaterId), instanceId, book);
    return resource;
  }

  public static String createResource(Connection con, ResourceDetail resource)
      throws SQLException {
    String query =
        "INSERT INTO SC_Resources_Resource (id, instanceId, categoryId, name, " +
        "creationdate, updatedate, bookable, createrid, updaterid, description) " +
        "VALUES (?,?,?,?,?,?,?,?,?,?)";
    PreparedStatement prepStmt = null;
    String instanceId = resource.getInstanceId();
    String name = resource.getName();
    Date creationdate = resource.getCreationDate();
    Date updatedate = resource.getUpdateDate();
    boolean bookable = resource.getBookable();
    // String responsibleid = resource.getResponsibleId();
    String createrid = resource.getCreaterId();
    String updaterid = resource.getUpdaterId();
    String description = resource.getDescription();
    String idCategory = resource.getCategoryId();
    int id = 0;
    int book = 0;
    if (bookable)
      book = 1;
    try {
      id = DBUtil.getNextId("SC_Resources_Resource", "id");
      // Preparation de la requête
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, id);
      prepStmt.setString(2, instanceId);
      prepStmt.setInt(3, Integer.parseInt(idCategory));
      prepStmt.setString(4, name);
      prepStmt.setString(5, Long.toString(creationdate.getTime()));
      prepStmt.setString(6, Long.toString(updatedate.getTime()));
      prepStmt.setInt(7, book);
      // prepStmt.setInt(8, Integer.parseInt(responsibleid));
      prepStmt.setString(8, createrid);
      prepStmt.setString(9, updaterid);
      prepStmt.setString(10, description);
      prepStmt.executeUpdate();
      addManagers(con, id, resource.getManagers());
    } catch (UtilException e) {
      throw new SQLException();
    } finally {
      DBUtil.close(prepStmt);
    }
    return Integer.toString(id);
  }

  public static void updateResource(Connection con, ResourceDetail resource)
      throws SQLException {
    String query =
        "UPDATE SC_Resources_Resource SET instanceId=?, name=?, updatedate=?, bookable=?, " +
        "updaterid=?, description=?, categoryid=? WHERE id=?";
    PreparedStatement prepStmt = null;

    String id = resource.getId();
    String instanceId = resource.getInstanceId();
    String name = resource.getName();
    Date updatedate = resource.getUpdateDate();
    boolean bookable = resource.getBookable();
    String updaterid = resource.getUpdaterId();
    String description = resource.getDescription();
    String categoryId = resource.getCategoryId();
    int idresource = Integer.parseInt(id);

    try {
      // Preparation de la requête
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, name);
      prepStmt.setString(3, Long.toString(updatedate.getTime()));
      if (bookable) {
        prepStmt.setInt(4, 1);
      } else {
        prepStmt.setInt(4, 0);
      }
      prepStmt.setString(5, updaterid);
      prepStmt.setString(6, description);
      prepStmt.setInt(7, Integer.parseInt(categoryId));
      prepStmt.setInt(8, idresource);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static List<ResourceDetail> getResourcesByCategory(Connection con, String categoryId)
      throws SQLException {
    List<ResourceDetail> list = null;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    String query =
        "select id,name,creationDate,updateDate,bookable,responsibleId," +
        "createrId,updaterId,description,categoryid " +
        "from SC_Resources_Resource where categoryid = ?";
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(categoryId));
      rs = prepStmt.executeQuery();
      list = returnArrayListofReservationResource(con, rs, false);
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  public static ResourceDetail getResource(Connection con, String id)
      throws SQLException {
    String query = "select * from SC_Resources_Resource where id=?";

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    ResourceDetail resource = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, new Integer(id).intValue());
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        resource = resultSetToResourceDetail(rs);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    // ajout des responsables
    List<String> managers = getManagers(con, new Integer(id).intValue());
    resource.setManagers(managers);
    return resource;
  }

  public static void deleteResource(Connection con, String id)
      throws SQLException {
    deleteReservedResource(con, id);

    PreparedStatement prepStmt = null;
    String query = "DELETE FROM SC_Resources_Resource WHERE ID=?";
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, new Integer(id).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void deleteResourceFromCategory(Connection con,
      String idCategory) throws SQLException {
    List<ResourceDetail> listOfResources = getResourcesByCategory(con, idCategory);
    try {
      for (int i = 0; i < listOfResources.size(); i++) {
        ResourceDetail resource = listOfResources.get(i);
        String idResource = resource.getId();
        deleteResource(con, idResource);
      }
    } catch (Exception e) {
      // TODO: handle exception
    }
  }

  public static void deleteReservedResource(Connection con, String resourceId)
      throws SQLException {
    PreparedStatement prepStmt = null;
    String query = "DELETE FROM SC_Resources_ReservedResource WHERE resourceId=?";
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(resourceId));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /** Gestion des réservations **/
  private static ReservationDetail returnReservationDetail(ResultSet rs,
      String instanceId) throws SQLException {
    int id;
    String evenement = "";
    Date creationDate;
    Date updateDate;
    Date begindate;
    Date enddate;
    String reason = "";
    String place = "";
    String status = "";
    ReservationDetail reservation = null;
    int userId;

    id = rs.getInt("id");
    evenement = rs.getString("evenement");
    creationDate = new Date(Long.parseLong(rs.getString("creationDate")));
    updateDate = new Date(Long.parseLong(rs.getString("updateDate")));
    begindate = new Date(Long.parseLong(rs.getString("begindate")));
    enddate = new Date(Long.parseLong(rs.getString("enddate")));
    reason = rs.getString("reason");
    place = rs.getString("place");
    userId = rs.getInt("userId");
    status = rs.getString("status");
    reservation = new ReservationDetail(Integer.toString(id), evenement,
        begindate, enddate, reason, place, Integer.toString(userId),
        creationDate, updateDate, instanceId, status);

    return reservation;
  }

  private static List<ResourceDetail> returnArrayListofReservationResource(Connection con,
      ResultSet rs, boolean resa)
      throws SQLException {
    int id;
    String instanceId = "";
    String name = "";
    int categoryId = 0;
    Date creationDate = null;
    Date updateDate = null;
    String description = "";
    int responsibleId = 0;
    int createrId = 0;
    int updaterId = 0;
    String status = "";
    List<ResourceDetail> list = new ArrayList<ResourceDetail>();
    boolean book = false;
    while (rs.next()) {
      id = rs.getInt("id");
      name = rs.getString("name");
      creationDate = new Date(Long.parseLong(rs.getString("creationDate")));
      updateDate = new Date(Long.parseLong(rs.getString("updateDate")));
      int bookable = rs.getInt("bookable");
      responsibleId = rs.getInt("responsibleId");
      createrId = rs.getInt("createrId");
      updaterId = rs.getInt("updaterId");
      description = rs.getString("description");
      categoryId = rs.getInt("categoryid");
      if (resa) {
        status = rs.getString("status");
      }
      book = (bookable == 1);

      ResourceDetail resource = new ResourceDetail(Integer.toString(id),
          Integer.toString(categoryId), name, creationDate, updateDate,
          description, Integer.toString(responsibleId), Integer
          .toString(createrId), Integer.toString(updaterId), instanceId,
          book, status);
      // TODO : ajouter les managers à la ressource
      List<String> managers = getManagers(con, id);
      resource.setManagers(managers);
      list.add(resource);
    }
    return list;
  }

  public static String saveReservation(Connection con,
      ReservationDetail reservation, String listReservationCurrent)
      throws SQLException {
    String query =
        "INSERT INTO SC_Resources_Reservation " +
        "(id, instanceId, evenement, userId, creationdate, updatedate, " +
        "begindate, enddate, reason, place, status) " +
        "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
    PreparedStatement prepStmt = null;
    // on récupère les informations de ReservationDetail
    String instanceId = reservation.getInstanceId();
    String evenement = reservation.getEvent();
    String userId = reservation.getUserId();
    int idUser = Integer.parseInt(userId);
    Date creationdate = reservation.getUpdateDate();
    Date updatedate = reservation.getUpdateDate();
    Date begindate = reservation.getBeginDate();
    Date enddate = reservation.getEndDate();
    String reason = reservation.getReason();
    String place = reservation.getPlace();
    int id;

    try {
      id = DBUtil.getNextId("SC_Resources_Reservation", "id");
      // Preparation de la requête
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, id);
      prepStmt.setString(2, instanceId);
      prepStmt.setString(3, evenement);
      prepStmt.setInt(4, idUser);
      prepStmt.setString(5, Long.toString(creationdate.getTime()));
      prepStmt.setString(6, Long.toString(updatedate.getTime()));
      prepStmt.setString(7, Long.toString(begindate.getTime()));
      prepStmt.setString(8, Long.toString(enddate.getTime()));
      prepStmt.setString(9, reason);
      prepStmt.setString(10, place);
      prepStmt.setString(11, getStatus(con, reservation, listReservationCurrent));

      prepStmt.executeUpdate();
      saveIntoReservedResource(con, id, listReservationCurrent, userId);
    } catch (UtilException e) {
      throw new SQLException();
    } finally {
      DBUtil.close(prepStmt);
    }
    return Integer.toString(id);
  }

  private static String getStatus(Connection con, ReservationDetail reservation,
      String listReservationCurrent) throws SQLException {
    StringTokenizer tokenizer = new StringTokenizer(listReservationCurrent, ",");
    boolean refused = false;
    boolean ok = false;
    String reservationStatus = STATUS_VALIDATE;
    while (tokenizer.hasMoreTokens() && !ok) {
      String idResource = tokenizer.nextToken();
      String status = getResourceStatus(con, idResource, reservation.getUserId());
      refused = false;
      if (status.equals(STATUS_FOR_VALIDATION)) {
        // si une ressource reste à valider, la reservation est à valider
        reservationStatus = status;
        ok = true;
      }
      if (status.equals(STATUS_REFUSED)) {
        refused = true;
      }
    }
    if (refused) {
      reservationStatus = STATUS_REFUSED;
    }
    return reservationStatus;
  }

  public static void saveIntoReservedResource(Connection con, int id,
      String listReservationCurrent, String userId) throws SQLException {
    StringTokenizer tokenizer = new StringTokenizer(listReservationCurrent, ",");
    while (tokenizer.hasMoreTokens()) {
      String idResource = tokenizer.nextToken();
      String status = getResourceStatus(con, idResource, userId);
      insertIntoReservedResource(con, id, idResource, status);
    }
  }

  public static String getResourceStatus(Connection con, String resourceId, String userId)
      throws SQLException {
    String status = STATUS_VALIDATE;
    ResourceDetail resource = getResource(con, resourceId);
    // si le status n'existe pas sur cette ressource
    if (!StringUtil.isDefined(resource.getStatus())) {
      List<String> managers = getManagers(con, Integer.parseInt(resourceId));
      if (managers != null && managers.size() > 0) {
        // il y a des responsables sur cette ressource
        if (!managers.contains(userId)) {
          status = STATUS_FOR_VALIDATION;
        }
      }
    }
    return status;
  }

  public static void insertIntoReservedResource(Connection con,
      int idReservation, String idResource, String status) throws SQLException {
    String query =
        "INSERT INTO SC_Resources_ReservedResource (reservationId,resourceId, status) VALUES (?, ?, ?)";
    PreparedStatement prepStmt = null;
    int resourceId = Integer.parseInt(idResource);
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, idReservation);
      prepStmt.setInt(2, resourceId);
      prepStmt.setString(3, status);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static int idReservation(Connection con, int resourceId,
      Date startDate, Date endDate) throws SQLException {
    int reservationIdProblem = 0; // Select B.resourceId from
    // SC_Resources_ReservedResource B,
    // SC_Resources_Reservation C where
    // B.reservationId = C.Id AND ((C.begindate<?
    // AND ?<C.enddate) OR (C.begindate<=? AND
    // ?<=C.enddate) OR (?<=C.begindate AND
    // C.enddate<=?))) ORDER BY D.id";

    PreparedStatement prepStmt = null;
    String query =
        "Select id from SC_Resources_Reservation A, SC_Resources_ReservedResource B " +
        "where B.resourceId=? AND B.reservationId=A.id AND ((A.begindate<? AND ?<A.enddate) " +
        "OR (A.begindate<? AND ?<A.enddate) OR (?<=A.begindate AND A.enddate<=?))";
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, resourceId);
      prepStmt.setString(2, Long.toString(startDate.getTime()));
      prepStmt.setString(3, Long.toString(startDate.getTime()));
      prepStmt.setString(4, Long.toString(endDate.getTime()));
      prepStmt.setString(5, Long.toString(endDate.getTime()));
      prepStmt.setString(6, Long.toString(startDate.getTime()));
      prepStmt.setString(7, Long.toString(endDate.getTime()));
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        reservationIdProblem = rs.getInt(1);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return reservationIdProblem;
  }

  public static List<ResourceReservableDetail> getResourcesReservable(Connection con,
      String instanceId, Date startDate, Date endDate) throws SQLException,
      ParseException {
    List<ResourceReservableDetail> list = null;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String query =
        "Select A.id, D.id, A.name, D.name " +
        "from SC_Resources_Resource A, SC_Resources_Category D " +
        "where A.instanceId = ? AND D.instanceId = ? AND A.categoryId = D.id " +
        "AND A.bookable=1 AND D.bookable=1 ORDER BY D.id";
    int categoryId;
    int resourceId;
    String categoryName = "";
    String resourceName = "";
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, instanceId);
      rs = prepStmt.executeQuery();
      list = new ArrayList<ResourceReservableDetail>();
      while (rs.next()) {
        resourceId = rs.getInt(1);
        categoryId = rs.getInt(2);
        categoryName = rs.getString(4);
        int reservationIdProblem = idReservation(con, resourceId, startDate,
            endDate);
        // on envoie la liste des catégories, vides si toutes les ressources ont
        // déjà été réservées, ou avec des ressources non réservées
        if (reservationIdProblem == 0) {
          resourceName = rs.getString(3);
        } else {
          resourceName = "";
          resourceId = 0;
        }
        ResourceReservableDetail resourceReservable = new ResourceReservableDetail(
            Integer.toString(categoryId), Integer.toString(resourceId),
            categoryName, resourceName);
        list.add(resourceReservable);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  public static List<ResourceDetail> verificationReservation(Connection con,
      String instanceId, String listeReservation, Date startDate, Date endDate)
      throws SQLException, ParseException {
    List<ResourceDetail> listeResourcesEverReserved = new ArrayList<ResourceDetail>();
    if (listeReservation != null) {
      StringTokenizer tokenizer = new StringTokenizer(listeReservation, ",");
      while (tokenizer.hasMoreTokens()) {
        String idResource = tokenizer.nextToken();
        int resourceId = Integer.parseInt(idResource);
        int reservationIdProblem = IdReservationProblem(con, resourceId,
            startDate, endDate);
        // si reservationIdProblem est différent de 0 c'est qu'une reservation
        // déjà faite est incompatible avec la nouvelle réservation, donc
        // on met la ressource fautive dans un arrayListe pour pouvoir en
        // informer l'utilisateur.
        if (reservationIdProblem != 0) {
          ResourceDetail resourceProblem = getResource(con, idResource);
          listeResourcesEverReserved.add(resourceProblem);
        }
      }
    }
    return listeResourcesEverReserved;
  }

  /***
   * Renvoie 0 si tout va bien Renvoie l'id de la réservation si une ressource qu'on veut réservée
   * ne peut l être à cause d'une réservation déjà faite
   **/

  public static int IdReservationProblem(Connection con, int resourceId,
      Date startDate, Date endDate) throws SQLException {
    int reservationIdProblem = 0;
    PreparedStatement prepStmt = null;
    String query =
        "Select id from SC_Resources_Reservation A, SC_Resources_ReservedResource B " +
        "where B.resourceId=? AND B.reservationId=A.id AND ((A.begindate<? AND ?<A.enddate) " +
        "OR (A.begindate<? AND ?<A.enddate) OR (?<A.begindate AND A.enddate<?))";
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, resourceId);
      prepStmt.setString(2, Long.toString(startDate.getTime()));
      prepStmt.setString(3, Long.toString(startDate.getTime()));
      prepStmt.setString(4, Long.toString(endDate.getTime()));
      prepStmt.setString(5, Long.toString(endDate.getTime()));
      prepStmt.setString(6, Long.toString(startDate.getTime()));
      prepStmt.setString(7, Long.toString(endDate.getTime()));
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        reservationIdProblem = rs.getInt(1);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return reservationIdProblem;
  }

  public static List<ResourceDetail> verificationNewDateReservation(Connection con,
      String instanceId, String listeReservation, Date startDate, Date endDate,
      String reservationId) throws SQLException, ParseException {
    List<ResourceDetail> listeResourcesEverReserved = new ArrayList<ResourceDetail>();
    if (listeReservation != null) {
      StringTokenizer tokenizer = new StringTokenizer(listeReservation, ",");
      while (tokenizer.hasMoreTokens()) {
        String idResource = tokenizer.nextToken();
        int resourceId = Integer.parseInt(idResource);
        int reservationIdProblem = IdReservationDateProblem(con, resourceId,
            startDate, endDate, reservationId);
        // si reservationIdProblem est différent de 0 c'est qu'une reservation
        // déjà faite est incompatible avec la nouvelle réservation, donc
        // on met la ressource fautive dans un arrayListe pour pouvoir en
        // informer l'utilisateur.
        if (reservationIdProblem != 0) {
          ResourceDetail resourceProblem = getResource(con, idResource);
          listeResourcesEverReserved.add(resourceProblem);
        }
      }
    }
    return listeResourcesEverReserved;
  }

  /***
   * Renvoie 0 si tout va bien Renvoie l'id de la réservation si une ressource qu'on veut réservée
   * ne peut l être à cause d'une réservation déjà faite
   **/

  public static int IdReservationDateProblem(Connection con, int resourceId,
      Date startDate, Date endDate, String reservationId) throws SQLException {
    int reservationIdProblem = 0;
    int idReservation = Integer.parseInt(reservationId);
    PreparedStatement prepStmt = null;
    String query =
        "Select id from SC_Resources_Reservation A, SC_Resources_ReservedResource B " +
        "where B.resourceId=? AND B.reservationId!=? AND B.reservationId=A.id " +
        "AND ((A.begindate<? AND ?<A.enddate) OR (A.begindate<? AND ?<A.enddate) " +
        "OR (?<=A.begindate AND A.enddate<=?))";
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, resourceId);
      prepStmt.setInt(2, idReservation);
      prepStmt.setString(3, Long.toString(startDate.getTime()));
      prepStmt.setString(4, Long.toString(startDate.getTime()));
      prepStmt.setString(5, Long.toString(endDate.getTime()));
      prepStmt.setString(6, Long.toString(endDate.getTime()));
      prepStmt.setString(7, Long.toString(startDate.getTime()));
      prepStmt.setString(8, Long.toString(endDate.getTime()));
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        reservationIdProblem = rs.getInt(1);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return reservationIdProblem;
  }

  public static List<ReservationDetail> getReservationUser(Connection con, String instanceId,
      String userId) throws SQLException, ParseException {
    List<ReservationDetail> list = null;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String query =
        "select id,evenement,creationDate,updateDate,begindate,enddate,reason,place " +
        "from SC_Resources_Reservation where instanceId=? AND userId=?";
    int id;
    String evenement = "";
    Date creationDate;
    Date updateDate;
    Date begindate;
    Date enddate;
    String reason = "";
    String place = "";
    int idUser = Integer.parseInt(userId);

    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      prepStmt.setInt(2, idUser);

      rs = prepStmt.executeQuery();
      list = new ArrayList<ReservationDetail>();
      while (rs.next()) {
        id = rs.getInt("id");
        evenement = rs.getString("evenement");
        creationDate = new Date(Long.parseLong(rs.getString("creationDate")));
        updateDate = new Date(Long.parseLong(rs.getString("updateDate")));
        begindate = new Date(Long.parseLong(rs.getString("begindate")));
        enddate = new Date(Long.parseLong(rs.getString("enddate")));
        reason = rs.getString("reason");
        place = rs.getString("place");
        ReservationDetail reservation = new ReservationDetail(Integer
            .toString(id), evenement, begindate, enddate, reason, place,
            userId, creationDate, updateDate, instanceId);
        list.add(reservation);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  public static List<ReservationDetail> getReservations(Connection con, String instanceId)
      throws SQLException, ParseException {
    List<ReservationDetail> list = null;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String query =
        "select id,evenement,creationDate,updateDate,begindate,enddate,reason,place,userId " +
        "from SC_Resources_Reservation where instanceId=?";
    int id;
    String evenement = "";
    Date creationDate;
    Date updateDate;
    Date begindate;
    Date enddate;
    String reason = "";
    String place = "";
    int userId = 0;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);

      rs = prepStmt.executeQuery();
      list = new ArrayList<ReservationDetail>();
      while (rs.next()) {
        id = rs.getInt("id");
        evenement = rs.getString("evenement");
        creationDate = new Date(Long.parseLong(rs.getString("creationDate")));
        updateDate = new Date(Long.parseLong(rs.getString("updateDate")));
        begindate = new Date(Long.parseLong(rs.getString("begindate")));
        enddate = new Date(Long.parseLong(rs.getString("enddate")));
        reason = rs.getString("reason");
        place = rs.getString("place");
        userId = rs.getInt("userId");
        ReservationDetail reservation = new ReservationDetail(Integer
            .toString(id), evenement, begindate, enddate, reason, place,
            Integer.toString(userId), creationDate, updateDate, instanceId);
        list.add(reservation);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  public static List<ResourceDetail> getResourcesofReservation(Connection con,
      String instanceId, String reservationId) throws SQLException,
      ParseException {
    List<ResourceDetail> list = null;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String query =
        "select A.id, A.categoryid, A.name, A.creationdate, A.updatedate, A.bookable, " +
        "A.responsibleid, A.createrid, A.updaterid, A.description, B.status " +
        "from SC_Resources_Resource A, SC_Resources_ReservedResource B " +
        "where A.id=B.resourceId AND B.reservationId=? AND A.instanceId=?";
    int idReservation = Integer.parseInt(reservationId);
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, idReservation);
      prepStmt.setString(2, instanceId);
      rs = prepStmt.executeQuery();
      list = returnArrayListofReservationResource(con, rs, true);

    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  public static ReservationDetail getReservation(Connection con,
      String instanceId, String reservationId) throws SQLException {
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String query =
        "select id,evenement,creationDate,updateDate,begindate,enddate,reason,userId,place, status "
            +
            "from SC_Resources_Reservation where instanceId=? AND id=?";
    int idReservation = Integer.parseInt(reservationId);
    ReservationDetail reservation = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      prepStmt.setInt(2, idReservation);
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        reservation = returnReservationDetail(rs, instanceId);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return reservation;
  }

  public static List<ReservationDetail> getMonthReservation(Connection con,
      String instanceId, Date monthDate, String userId, String language)
      throws SQLException, ParseException {
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<ReservationDetail> list = null;
    Date monthBegin = getFirstDateOfMonth(monthDate);
    Date monthEnd = getEndDateOfMonth(monthDate);
    String query =
        "select id,evenement,creationDate,updateDate,begindate,enddate,reason," +
        "place,userId, status from SC_Resources_Reservation " +
        "where instanceId=? AND userId=? AND ((?<=begindate AND begindate<=?) " +
        "OR (?<=enddate AND enddate<=?) OR (begindate<=? AND ?<=enddate)) ";
    int idUser = Integer.parseInt(userId);
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      prepStmt.setInt(2, idUser);
      prepStmt.setString(3, Long.toString(monthBegin.getTime()));
      prepStmt.setString(4, Long.toString(monthEnd.getTime()));
      prepStmt.setString(5, Long.toString(monthBegin.getTime()));
      prepStmt.setString(6, Long.toString(monthEnd.getTime()));
      prepStmt.setString(7, Long.toString(monthBegin.getTime()));
      prepStmt.setString(8, Long.toString(monthEnd.getTime()));
      rs = prepStmt.executeQuery();
      list = new ArrayList<ReservationDetail>();
      while (rs.next()) {
        ReservationDetail reservation = returnReservationDetail(rs, instanceId);
        list.add(reservation);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  public static List<ReservationDetail> getReservationForValidation(Connection con,
      String instanceId, Date monthDate, String userId, String language)
      throws SQLException, ParseException {
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<ReservationDetail> list = null;
    Date monthBegin = getFirstDateOfMonth(monthDate);
    Date monthEnd = getEndDateOfMonth(monthDate);
    // chercher les reservations dont une des ressources est à valider
    List<String> reservationIds = getReservationByUser(con, userId);
    if (reservationIds == null || reservationIds.isEmpty()) {
      return new ArrayList<ReservationDetail>();
    }
    String query =
        "select id,evenement,creationDate,updateDate,begindate,enddate,reason," +
        "place,userId, status from SC_Resources_Reservation " +
        "where instanceId=? AND ((?<=begindate AND begindate<=?) " +
        "OR (?<=enddate AND enddate<=?) OR (begindate<=? AND ?<=enddate)) " +
        "AND (";
    boolean first = true;
    for (String reservationId : reservationIds) {
      if (!first) {
        query += "OR ";
      } else {
        first = false;
      }
      query += "id = '" + reservationId + "' ";

    }
    query += ")";

    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, Long.toString(monthBegin.getTime()));
      prepStmt.setString(3, Long.toString(monthEnd.getTime()));
      prepStmt.setString(4, Long.toString(monthBegin.getTime()));
      prepStmt.setString(5, Long.toString(monthEnd.getTime()));
      prepStmt.setString(6, Long.toString(monthBegin.getTime()));
      prepStmt.setString(7, Long.toString(monthEnd.getTime()));

      rs = prepStmt.executeQuery();
      list = new ArrayList<ReservationDetail>();
      while (rs.next()) {
        ReservationDetail reservation = returnReservationDetail(rs, instanceId);
        list.add(reservation);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  private static List<String> getReservationByUser(Connection con, String userId)
      throws SQLException {
    List<String> list = new ArrayList<String>();
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String reservationId = "";
    String query =
        "select A.reservationId from SC_Resources_ReservedResource A, SC_Resources_Managers B " +
        "where A.resourceId=B.resourceId AND B.managerId = ? AND A.status = '" +
        STATUS_FOR_VALIDATION + "'";
    int idUser = Integer.parseInt(userId);
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, idUser);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        reservationId = Integer.toString(rs.getInt("reservationId"));
        list.add(reservationId);
      }

    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  private static Date getFirstDateOfMonth(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.DATE, 1);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    return calendar.getTime();
  }

  private static Date getEndDateOfMonth(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.DATE, calendar.getMaximum(Calendar.DATE));
    calendar.set(Calendar.HOUR_OF_DAY, 23);
    calendar.set(Calendar.MINUTE, 59);
    calendar.set(Calendar.SECOND, 59);
    calendar.set(Calendar.MILLISECOND, 59);

    return calendar.getTime();
  }

  public static List<ReservationDetail> getMonthReservationOfCategory(Connection con,
      String instanceId,
      Date monthDate, String userId, String language, String idCategory) throws SQLException,
      ParseException {
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<ReservationDetail> list = null;
    Date monthBegin = getFirstDateOfMonth(monthDate);
    Date monthEnd = getEndDateOfMonth(monthDate);
    String query =
        "select A.id,A.evenement,A.creationDate,A.updateDate,A.begindate,A.enddate," +
        "A.reason,A.place,A.userId, A.status from SC_Resources_Reservation A, " +
        "SC_Resources_Resource B, SC_Resources_ReservedResource C " +
        "where A.instanceId=? AND B.categoryid=? AND B.id=C.resourceId " +
        "AND C.reservationId=A.id AND ((?<=A.begindate AND A.begindate<=?) " +
        "OR (?<=A.enddate AND A.enddate<=?) OR (A.begindate<=? AND ?<=A.enddate)) ";
    int categoryId = Integer.parseInt(idCategory);
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      prepStmt.setInt(2, categoryId);
      prepStmt.setString(3, Long.toString(monthBegin.getTime()));
      prepStmt.setString(4, Long.toString(monthEnd.getTime()));
      prepStmt.setString(5, Long.toString(monthBegin.getTime()));
      prepStmt.setString(6, Long.toString(monthEnd.getTime()));
      prepStmt.setString(7, Long.toString(monthBegin.getTime()));
      prepStmt.setString(8, Long.toString(monthEnd.getTime()));
      rs = prepStmt.executeQuery();
      list = new ArrayList<ReservationDetail>();
      while (rs.next()) {
        ReservationDetail reservation = returnReservationDetail(rs, instanceId);
        List<ResourceDetail> listResourcesReserved = getResourcesofReservation(con,
            instanceId, reservation.getId());
        reservation.setListResourcesReserved(listResourcesReserved);
        list.add(reservation);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  public static void deleteReservation(Connection con, String id)
      throws SQLException {
    PreparedStatement prepStmt = null;

    String query = "DELETE FROM SC_Resources_ReservedResource WHERE reservationId=?";
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(id));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }

    query = "DELETE FROM SC_Resources_Reservation WHERE ID=?";
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(id));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void updateReservation(Connection con, String listReservation,
      ReservationDetail reservationCourante, boolean updateDate) throws SQLException,
      ParseException {
    PreparedStatement prepStmt = null;
    List<ResourceDetail> oldResources = new ArrayList<ResourceDetail>();
    if (!updateDate) {
      oldResources =
          getResourcesofReservation(con, reservationCourante.getInstanceId(), reservationCourante
          .getId());
    }
    String query = "DELETE FROM SC_Resources_ReservedResource WHERE reservationId=?";
    int reservationId = Integer.parseInt(reservationCourante.getId());
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, reservationId);
      prepStmt.executeUpdate();

      StringTokenizer tokenizer = new StringTokenizer(listReservation, ",");
      boolean refused = false;
      boolean forValidation = false;
      String reservationStatus = STATUS_VALIDATE;
      while (tokenizer.hasMoreTokens()) {
        String idResource = tokenizer.nextToken();
        String status = null;
        if (!updateDate) {
          status = getStatusForExistingResource(idResource, oldResources);
        }
        if (status == null) {
          status = getResourceStatus(con, idResource, reservationCourante.getUserId());
        }
        insertIntoReservedResource(con, reservationId, idResource, status);
        if (status.equals(STATUS_FOR_VALIDATION)) {
          forValidation = true;
        }
        if (status.equals(STATUS_REFUSED)) {
          refused = true;
        }
      }
      if (forValidation) {
        reservationStatus = STATUS_FOR_VALIDATION;
      }
      if (refused) {
        reservationStatus = STATUS_REFUSED;
      }
      reservationCourante.setStatus(reservationStatus);
      updateIntoReservation(con, reservationCourante);

    } finally {
      DBUtil.close(prepStmt);
    }
  }

  private static String getStatusForExistingResource(String idResource,
      List<ResourceDetail> oldResources) {
    String status = null;
    for (ResourceDetail resource : oldResources) {
      String resourceId = resource.getId();
      if (idResource.equals(resourceId)) {
        status = resource.getStatus();
        return status;
      }
    }
    return status;
  }

  public static void updateReservation(Connection con, ReservationDetail reservationCourante)
      throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      String reservationStatus = STATUS_VALIDATE;
      List<ResourceDetail> resources = reservationCourante.getListResourcesReserved();
      if (resources != null) {
        Iterator<ResourceDetail> it = resources.iterator();
        boolean refused = false;
        boolean forValidation = false;

        while (it.hasNext()) {
          ResourceDetail resource = it.next();
          String status = resource.getStatus();
          if (status.equals(STATUS_FOR_VALIDATION)) {
            forValidation = true;
          }
          if (status.equals(STATUS_REFUSED)) {
            refused = true;
          }
        }
        if (forValidation) {
          reservationStatus = STATUS_FOR_VALIDATION;
        }
        if (refused) {
          reservationStatus = STATUS_REFUSED;
        }
      }
      reservationCourante.setStatus(reservationStatus);
      updateIntoReservation(con, reservationCourante);

    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void updateIntoReservation(Connection con,
      ReservationDetail reservationCourante) throws SQLException {
    PreparedStatement prepStmt = null;
    String query =
        "UPDATE SC_Resources_Reservation SET " +
        "evenement=?, updatedate=?, begindate=?, enddate=?, reason=?, place=?, status=? " +
        "WHERE id=? and instanceId=?";
    // on récupère les informations de ReservationDetail
    String instanceId = reservationCourante.getInstanceId();
    String evenement = reservationCourante.getEvent();
    Date updatedate = new Date();
    Date begindate = reservationCourante.getBeginDate();
    Date enddate = reservationCourante.getEndDate();
    String reason = reservationCourante.getReason();
    String place = reservationCourante.getPlace();
    String status = reservationCourante.getStatus();
    int reservationId = Integer.parseInt(reservationCourante.getId());

    try {
      // Preparation de la requête
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, evenement);
      prepStmt.setString(2, Long.toString(updatedate.getTime()));
      prepStmt.setString(3, Long.toString(begindate.getTime()));
      prepStmt.setString(4, Long.toString(enddate.getTime()));
      prepStmt.setString(5, reason);
      prepStmt.setString(6, place);
      prepStmt.setString(7, status);
      prepStmt.setInt(8, reservationId);
      prepStmt.setString(9, instanceId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void addManagers(Connection con, int resourceId, List<String> managerIds)
      throws SQLException {
    if (managerIds != null && !managerIds.isEmpty()) {
      Iterator<String> it = managerIds.iterator();
      while (it.hasNext()) {
        String managerId = it.next();
        addManager(con, resourceId, Integer.parseInt(managerId));
      }
    }
  }

  public static void addManager(Connection con, int resourceId, int managerId)
      throws SQLException {
    String query = "INSERT INTO SC_Resources_Managers (resourceId, managerId) VALUES (?,?)";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, resourceId);
      prepStmt.setInt(2, managerId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void removeAllManagers(Connection con, int resourceId)
      throws SQLException {
    String query = "DELETE FROM SC_Resources_Managers WHERE resourceId = ?";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, resourceId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void removeManager(Connection con, int resourceId, int managerId)
      throws SQLException {
    String query = "DELETE FROM SC_Resources_Managers WHERE resourceId = ? AND managerId = ?";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, resourceId);
      prepStmt.setInt(2, managerId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static List<String> getManagers(Connection con, int resourceId) throws SQLException {
    List<String> managers = new ArrayList<String>();
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String query = "SELECT managerId FROM SC_Resources_Managers WHERE resourceId = ? ";
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, resourceId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        String managerId = rs.getString("managerId");
        managers.add(managerId);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return managers;
  }

  public static void updateResourceStatus(Connection con, String status, int resourceId,
      int reservationId, String componentId) throws SQLException {
    String query =
        "UPDATE SC_Resources_ReservedResource SET status = ? WHERE resourceId = ? AND reservationId = ?";
    PreparedStatement prepStmt = null;
    try {
      // Preparation de la requête
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, status);
      prepStmt.setInt(2, resourceId);
      prepStmt.setInt(3, reservationId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static String getStatusResourceOfReservation(Connection con, int resourceId,
      int reservationId) throws SQLException {
    ResultSet rs = null;
    String status = null;
    String query =
        "SELECT status FROM SC_Resources_ReservedResource WHERE resourceId = ? AND reservationId = ?";
    PreparedStatement prepStmt = null;
    try {
      // Preparation de la requête
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, resourceId);
      prepStmt.setInt(2, reservationId);
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        status = rs.getString("status");
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return status;
  }
}
