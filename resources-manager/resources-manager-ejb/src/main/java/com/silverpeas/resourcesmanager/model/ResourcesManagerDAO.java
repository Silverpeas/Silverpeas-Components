/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.resourcesmanager.model;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.UtilException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;


import static com.silverpeas.resourcesmanager.model.ResourceStatus.*;

public class ResourcesManagerDAO {

  private final static ResourceDao dao = new ResourceDao();

  
  
  public static void deleteReservedResource(Connection con, String resourceId)
          throws SQLException {
    PreparedStatement prepStmt = null;
    String query = "DELETE FROM sc_resources_reservedResource WHERE resourceId= ?";
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(resourceId));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Gestion des réservations *
   */
  private static ReservationDetail returnReservationDetail(Connection con, ResultSet rs,
          String instanceId) throws
          SQLException {
    Date creationDate = null;
    Date updateDate = null;
    Date begindate = null;
    Date enddate = null;
    int id = rs.getInt("id");
    String evenement = rs.getString("evenement");
    if (StringUtil.isDefined(rs.getString("creationDate"))) {
      creationDate = new Date(Long.parseLong(rs.getString("creationDate")));
    }
    if (StringUtil.isDefined(rs.getString("updateDate"))) {
      updateDate = new Date(Long.parseLong(rs.getString("updateDate")));
    }
    if (StringUtil.isDefined(rs.getString("begindate"))) {
      begindate = new Date(Long.parseLong(rs.getString("begindate")));
    }
    if (StringUtil.isDefined(rs.getString("enddate"))) {
      enddate = new Date(Long.parseLong(rs.getString("enddate")));
    }
    String reason = rs.getString("reason");
    String place = rs.getString("place");
    int userId = rs.getInt("userId");
    String status = rs.getString("status");
    ReservationDetail reservation = new ReservationDetail(Integer.toString(id), evenement,
            begindate, enddate, reason, place, Integer.toString(userId),
            creationDate, updateDate, instanceId, status);
    List<ResourceDetail> listResourcesReserved = dao.getResourcesofReservation(con,
            instanceId, reservation.getId());
    reservation.setListResourcesReserved(listResourcesReserved);
    return reservation;
  }

  public static String saveReservation(Connection con,
          ReservationDetail reservation, String listReservationCurrent)
          throws SQLException {
    String query =
            "INSERT INTO SC_Resources_Reservation "
            + "(id, instanceId, evenement, userId, creationdate, updatedate, "
            + "begindate, enddate, reason, place, status) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
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
      if (STATUS_REFUSED.equals(status)) {
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
    ResourceDetail resource = dao.getResource(con, resourceId);
    // si le status n'existe pas sur cette ressource
    if (!StringUtil.isDefined(resource.getStatus())) {
      List<String> managers = dao.getManagers(con, Integer.parseInt(resourceId));
      if (managers != null && !managers.isEmpty()) {
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
    int reservationIdProblem = 0;
    PreparedStatement prepStmt = null;
    String query =
            "Select id from SC_Resources_Reservation A, SC_Resources_ReservedResource B "
            + "where B.resourceId=? AND B.reservationId=A.id AND ((A.begindate<? AND ?<A.enddate) "
            + "OR (A.begindate<? AND ?<A.enddate) OR (?<=A.begindate AND A.enddate<=?))";
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

  private static final String SELECT_RESOURCES_RESERVABLE = "SELECT sc_resources_resource.id AS "
          + "resourceId, sc_resources_category.id AS categoryId, sc_resources_resource.name AS "
          + "resourceName, sc_resources_category.name AS categoryName FROM sc_resources_resource, "
          + "sc_resources_category WHERE sc_resources_resource.instanceId = ? "
          + "AND sc_resources_category.instanceId = ? AND sc_resources_resource.categoryId = "
          + "sc_resources_category.id AND sc_resources_resource.bookable=1 AND "
          + "sc_resources_category.bookable = 1 ORDER BY sc_resources_category.id";
  
  public static List<ResourceReservableDetail> getResourcesReservable(Connection con,
          String instanceId, Date startDate, Date endDate) throws SQLException {
    List<ResourceReservableDetail> list = new ArrayList<ResourceReservableDetail>(10);
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(SELECT_RESOURCES_RESERVABLE);
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, instanceId);
      rs = prepStmt.executeQuery();
      
      while (rs.next()) {
        int resourceId = rs.getInt("resourceId");
        int reservationIdProblem = idReservation(con, resourceId, startDate, endDate);
        // on envoie la liste des catégories, vides si toutes les ressources ont
        // déjà été réservées, ou avec des ressources non réservées
        String resourceName = rs.getString("resourceName");
        if (reservationIdProblem != 0) {
          resourceName = "";
          resourceId = 0;
        }
        ResourceReservableDetail resourceReservable = new ResourceReservableDetail(String.valueOf(
                rs.getInt("categoryId")), Integer.toString(resourceId), rs.getString("categoryName"),
                resourceName);
        list.add(resourceReservable);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  public static List<ResourceDetail> verificationReservation(Connection con,
      String listeReservation, Date startDate, Date endDate) throws SQLException {
    List<ResourceDetail> listeResourcesEverReserved = new ArrayList<ResourceDetail>();
    if (listeReservation != null) {
      StringTokenizer tokenizer = new StringTokenizer(listeReservation, ",");
      while (tokenizer.hasMoreTokens()) {
        String idResource = tokenizer.nextToken();
        int resourceId = Integer.parseInt(idResource);
        int reservationIdProblem = IdReservationProblem(con, resourceId, startDate, endDate);
        // si reservationIdProblem est différent de 0 c'est qu'une reservation
        // déjà faite est incompatible avec la nouvelle réservation, donc
        // on met la ressource fautive dans un arrayListe pour pouvoir en
        // informer l'utilisateur.
        if (reservationIdProblem != 0) {
          ResourceDetail resourceProblem = dao.getResource(con, idResource);
          listeResourcesEverReserved.add(resourceProblem);
        }
      }
    }
    return listeResourcesEverReserved;
  }

  /**
   * *
   * Renvoie 0 si tout va bien Renvoie l'id de la réservation si une ressource qu'on veut réserver
   * ne peut l être à cause d'une réservation déjà faite
   *
   */
  public static int IdReservationProblem(Connection con, int resourceId, Date startDate,
          Date endDate) throws SQLException {
    int reservationIdProblem = 0;
    PreparedStatement prepStmt = null;
    String query =
            "Select id from SC_Resources_Reservation A, SC_Resources_ReservedResource B "
            + "where B.resourceId=? AND B.reservationId=A.id AND ((A.begindate<? AND ?<A.enddate) "
            + "OR (A.begindate<? AND ?<A.enddate) OR (?<A.begindate AND A.enddate<?))";
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
      String listeReservation, Date startDate, Date endDate, String reservationId) throws SQLException {
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
          ResourceDetail resourceProblem = dao.getResource(con, idResource);
          listeResourcesEverReserved.add(resourceProblem);
        }
      }
    }
    return listeResourcesEverReserved;
  }

  /**
   * *
   * Renvoie 0 si tout va bien Renvoie l'id de la réservation si une ressource qu'on veut réservée
   * ne peut l être à cause d'une réservation déjà faite
   *
   */
  public static int IdReservationDateProblem(Connection con, int resourceId,
          Date startDate, Date endDate, String reservationId) throws SQLException {
    int reservationIdProblem = 0;
    int idReservation = Integer.parseInt(reservationId);
    PreparedStatement prepStmt = null;
    String query =
            "Select id from SC_Resources_Reservation A, SC_Resources_ReservedResource B "
            + "where B.resourceId=? AND B.reservationId!=? AND B.reservationId=A.id "
            + "AND ((A.begindate<? AND ?<A.enddate) OR (A.begindate<? AND ?<A.enddate) "
            + "OR (?<=A.begindate AND A.enddate<=?))";
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
          String userId) throws SQLException {
    List<ReservationDetail> list = new ArrayList<ReservationDetail>();
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String query =
            "select id,evenement,creationDate,updateDate,begindate,enddate,reason,place,userId, status "
            + "from SC_Resources_Reservation where instanceId=? AND userId=?";
    int idUser = Integer.parseInt(userId);

    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      prepStmt.setInt(2, idUser);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        list.add(returnReservationDetail(con, rs, instanceId));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  public static List<ReservationDetail> getReservations(Connection con, String instanceId)
          throws SQLException {
    List<ReservationDetail> list = null;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String query = "SELECT id, evenement, creationDate, updateDate, begindate, enddate, reason, "
            + "place, userId, status FROM SC_Resources_Reservation WHERE instanceId = ?";
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      rs = prepStmt.executeQuery();
      list = new ArrayList<ReservationDetail>();
      while (rs.next()) {
        list.add(returnReservationDetail(con, rs, instanceId));
      }
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
            + "from SC_Resources_Reservation where instanceId=? AND id=?";
    int idReservation = Integer.parseInt(reservationId);
    ReservationDetail reservation = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      prepStmt.setInt(2, idReservation);
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        reservation = returnReservationDetail(con, rs, instanceId);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return reservation;
  }

  public static List<ReservationDetail> getMonthReservation(Connection con,
          String instanceId, Date monthDate, String userId)
          throws SQLException {
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<ReservationDetail> list = null;
    Date monthBegin = getFirstDateOfMonth(monthDate);
    Date monthEnd = getEndDateOfMonth(monthDate);
    String query =
            "select id,evenement,creationDate,updateDate,begindate,enddate,reason,"
            + "place,userId, status from SC_Resources_Reservation "
            + "where instanceId=? AND userId=? AND ((?<=begindate AND begindate<=?) "
            + "OR (?<=enddate AND enddate<=?) OR (begindate<=? AND ?<=enddate)) ";
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
        ReservationDetail reservation = returnReservationDetail(con, rs, instanceId);
        list.add(reservation);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  public static List<ReservationDetail> getReservationForValidation(Connection con,
          String instanceId, Date monthDate, String userId)
          throws SQLException {
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
            "select id,evenement,creationDate,updateDate,begindate,enddate,reason,"
            + "place,userId, status from SC_Resources_Reservation "
            + "where instanceId=? AND ((?<=begindate AND begindate<=?) "
            + "OR (?<=enddate AND enddate<=?) OR (begindate<=? AND ?<=enddate)) "
            + "AND (";
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
        ReservationDetail reservation = returnReservationDetail(con, rs, instanceId);
        list.add(reservation);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  /**
   * Return the list of resources to be validated taht the current user can manage.
   *
   * @param con
   * @param userId
   * @return
   * @throws SQLException
   */
  private static List<String> getReservationByUser(Connection con, String userId)
          throws SQLException {
    List<String> list = new ArrayList<String>();
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String reservationId = "";
    String query =
            "select A.reservationId from SC_Resources_ReservedResource A, SC_Resources_Managers B "
            + "where A.resourceId=B.resourceId AND B.managerId = ? AND A.status = '"
            + STATUS_FOR_VALIDATION + "'";
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
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }

  private static Date getEndDateOfMonth(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.DAY_OF_MONTH, calendar.getMaximum(Calendar.DAY_OF_MONTH));
    calendar.set(Calendar.HOUR_OF_DAY, 23);
    calendar.set(Calendar.MINUTE, 59);
    calendar.set(Calendar.SECOND, 59);
    calendar.set(Calendar.MILLISECOND, 999);
    return calendar.getTime();
  }

  public static List<ReservationDetail> getMonthReservationOfCategory(Connection con,
      String instanceId, Date monthDate, String idCategory) throws SQLException{
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<ReservationDetail> list = new ArrayList<ReservationDetail>();
    Date monthBegin = getFirstDateOfMonth(monthDate);
    Date monthEnd = getEndDateOfMonth(monthDate);
    String query =
            "select A.id,A.evenement,A.creationDate,A.updateDate,A.begindate,A.enddate,"
            + "A.reason,A.place,A.userId, A.status from SC_Resources_Reservation A, "
            + "SC_Resources_Resource B, SC_Resources_ReservedResource C "
            + "where A.instanceId=? AND B.categoryid=? AND B.id=C.resourceId "
            + "AND C.reservationId=A.id AND ((?<=A.begindate AND A.begindate<=?) "
            + "OR (?<=A.enddate AND A.enddate<=?) OR (A.begindate<=? AND ?<=A.enddate)) ";
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
      while (rs.next()) {
        ReservationDetail reservation = returnReservationDetail(con, rs, instanceId);
        list.add(reservation);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  private static final String DELETE_RESERVED_RESOURCE = "DELETE FROM sc_resources_reservedResource WHERE reservationId = ?";
  private static final String DELETE_RESERVATION = "DELETE FROM sc_resources_reservation WHERE id = ?";
  
  public static void deleteReservation(Connection con, String id) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(DELETE_RESERVED_RESOURCE);
      prepStmt.setInt(1, Integer.parseInt(id));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
    try {
      prepStmt = con.prepareStatement(DELETE_RESERVATION);
      prepStmt.setInt(1, Integer.parseInt(id));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void updateReservation(Connection con, String listReservation,
          ReservationDetail reservationCourante, boolean updateDate) throws SQLException {
    PreparedStatement prepStmt = null;
    List<ResourceDetail> oldResources = new ArrayList<ResourceDetail>();
    if (!updateDate) {
      oldResources = dao.getResourcesofReservation(con, reservationCourante.getInstanceId(),
              reservationCourante.getId());
    }
    int reservationId = Integer.parseInt(reservationCourante.getId());
    try {
      prepStmt = con.prepareStatement(DELETE_RESERVED_RESOURCE);
      prepStmt.setInt(1, reservationId);
      prepStmt.executeUpdate();

      StringTokenizer tokenizer = new StringTokenizer(listReservation, ",");
      boolean refused = false;
      boolean forValidation = false;
      String reservationStatus = ResourceStatus.STATUS_VALIDATE;
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
        if (STATUS_REFUSED.equals(status)) {
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

  static String getStatusForExistingResource(String idResource, List<ResourceDetail> oldResources) {
    for (ResourceDetail oldResource : oldResources) {
      String resourceId = oldResource.getId();
      if (idResource.equals(resourceId)) {
        return oldResource.getStatus();
      }
    }
    return null;
  }

  public static void updateReservation(Connection con, ReservationDetail reservationCourante)
          throws SQLException {
    List<ResourceDetail> resources = reservationCourante.getListResourcesReserved();
    String reservationStatus = computeReservationStatus(resources);
    reservationCourante.setStatus(reservationStatus);
    updateIntoReservation(con, reservationCourante);

  }

  public static String computeReservationStatus(List<ResourceDetail> resources) {
    String reservationStatus = STATUS_VALIDATE;
    if (resources != null) {
      boolean forValidation = false;
      for (ResourceDetail resource : resources) {
        String status = resource.getStatus();
        if (STATUS_FOR_VALIDATION.equals(status)) {
          forValidation = true;
        }
        if (STATUS_REFUSED.equals(status)) {
          return STATUS_REFUSED;
        }
      }
      if (forValidation) {
        reservationStatus = STATUS_FOR_VALIDATION;
      }
    }
    return reservationStatus;
  }

  public static void updateIntoReservation(Connection con,
          ReservationDetail reservationCourante) throws SQLException {
    PreparedStatement prepStmt = null;
    String query =
            "UPDATE SC_Resources_Reservation SET "
            + "evenement=?, updatedate=?, begindate=?, enddate=?, reason=?, place=?, status=? "
            + "WHERE id=? and instanceId=?";
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

  public static void updateResourceStatus(Connection con, String status, int resourceId,
      int reservationId) throws SQLException {
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

  private ResourcesManagerDAO() {
  }
}
