/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
package com.silverpeas.gallery.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.silverpeas.gallery.model.Order;
import com.silverpeas.gallery.model.OrderRow;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.UtilException;

public class OrderDAO {

  public static String createOrder(Connection con, Collection<String> basket,
      String userId, String instanceId) throws SQLException, UtilException {
    // Création d'une commande
    String id = "";
    PreparedStatement prepStmt = null;
    try {
      // 1. création de l'entête de la demande
      Date today = new Date();
      int newId = DBUtil.getNextId("SC_Gallery_Order", "orderId");
      id = new Integer(newId).toString();
      // création de la requête
      String query = "insert into SC_Gallery_Order (orderId, userId, instanceId, creationDate) values (?,?,?,?)";
      // initialisation des paramètres
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, newId);
      prepStmt.setInt(2, Integer.parseInt(userId));
      prepStmt.setString(3, instanceId);
      prepStmt.setString(4, Long.toString(today.getTime()));
      prepStmt.executeUpdate();

      // 2. création des lignes de la demande
      Iterator<String> it = basket.iterator();
      while (it.hasNext()) {
        String photoId = it.next();
        addPhoto(con, photoId, id, instanceId);
      }
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
    return id;
  }

  public static List<OrderRow> getAllPhotos(Connection con, String orderId)
      throws SQLException {
    // récupérer toutes les photos de la demande
    ArrayList<OrderRow> listPhoto = null;

    String query = "select photoId, instanceId, downloadDate, downloadDecision  from SC_Gallery_OrderDetail where orderId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(orderId));
      rs = prepStmt.executeQuery();
      listPhoto = new ArrayList<OrderRow>();
      while (rs.next()) {
        String photoId = rs.getString(1);
        String instanceId = rs.getString(2);
        OrderRow orderRow = new OrderRow(Integer.parseInt(orderId), Integer
            .parseInt(photoId), instanceId);
        String downloadDate = null;
        downloadDate = rs.getString(3);
        try {
          if (downloadDate != null)
            orderRow.setDownloadDate(new Date(Long
                .parseLong((String) downloadDate)));
        } catch (Exception e) {
          throw new SQLException(e.getMessage());
        }
        orderRow.setDownloadDecision(rs.getString(4));
        listPhoto.add(orderRow);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listPhoto;
  }

  public static void updateOrder(Connection con, Order order)
      throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      // mettre à jour l'entête
      String query = "update SC_Gallery_Order set processDate = ?, processUser = ? where orderId = ?";
      // initialisation des paramètres
      Date today = new Date();
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, Long.toString(today.getTime()));
      prepStmt.setInt(2, order.getProcessUserId());
      prepStmt.setInt(3, new Integer(order.getOrderId()).intValue());
      prepStmt.executeUpdate();

      // pour chaque ligne
      List<OrderRow> rows = order.getRows();
      Iterator<OrderRow> it = rows.iterator();
      while (it.hasNext()) {
        OrderRow row = (OrderRow) it.next();
        query = "update SC_Gallery_OrderDetail set downloadDecision = ? where orderId = ? and photoId = ? ";
        // initialisation des paramètres
        prepStmt = con.prepareStatement(query);
        prepStmt.setString(1, row.getDownloadDecision());
        prepStmt.setInt(2, new Integer(row.getOrderId()).intValue());
        prepStmt.setInt(3, new Integer(row.getPhotoId()).intValue());
        prepStmt.executeUpdate();
      }
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  public static void updateOrderRow(Connection con, OrderRow row)
      throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      // mettre à jour l'entête
      String query = "update SC_Gallery_OrderDetail set downloadDate = ?, downloadDecision = ? where orderId = ? and photoId = ? ";
      // initialisation des paramètres
      Date today = new Date();
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, Long.toString(today.getTime()));
      prepStmt.setString(2, row.getDownloadDecision());
      prepStmt.setInt(3, new Integer(row.getOrderId()).intValue());
      prepStmt.setInt(4, new Integer(row.getPhotoId()).intValue());
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  private static void addPhoto(Connection con, String photoId, String orderId,
      String instanceId) throws SQLException {
    // ajout d'une photo dans le panier
    PreparedStatement prepStmt = null;
    try {
      // création de la requete
      String query = "insert into SC_Gallery_OrderDetail (orderId, photoId, instanceId) values (?,?,?)";
      // initialisation des paramètres
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(orderId));
      prepStmt.setInt(2, Integer.parseInt(photoId));
      prepStmt.setString(3, instanceId);
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  public static List<Order> getAllOrders(Connection con, String userId,
      String instanceId) throws SQLException {
    // récupérer toutes les demandes de l'utilisateur sur cette instance
    ArrayList<Order> listOrder = null;

    boolean allUsers = false;
    if (userId.equals("-1"))
      allUsers = true;
    String query = "select orderId, userId, creationDate, processDate, processUser from SC_Gallery_Order where instanceId = ? ";
    if (!allUsers)
      query = query + "and userId = ? ";
    query = query + " order by creationDate desc";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      if (!allUsers)
        prepStmt.setInt(2, Integer.parseInt(userId));

      rs = prepStmt.executeQuery();
      listOrder = new ArrayList<Order>();
      while (rs.next()) {
        int orderId = rs.getInt(1);

        Order order = new Order(orderId);
        order.setUserId(rs.getInt(2));
        try {
          order.setCreationDate(new Date(Long.parseLong((String) rs
              .getString(3))));
        } catch (Exception e) {
          throw new SQLException(e.getMessage());
        }

        String processDate = null;
        if (StringUtil.isDefined(rs.getString(4))) {
          try {
            processDate = (String) rs.getString(4);
          } catch (Exception e) {
            throw new SQLException(e.getMessage());
          }
          order.setProcessDate(new Date(Long.parseLong(processDate)));
        }

        int processUserId = -1;
        if (StringUtil.isDefined(rs.getString(5)))
          processUserId = rs.getInt(5);
        order.setProcessUserId(processUserId);

        order.setInstanceId(instanceId);
        OrganizationController orga = new OrganizationController();
        order.setUserName(orga.getUserDetail(
            Integer.toString(order.getUserId())).getDisplayedName());

        // récupérer les lignes
        order.setRows(getAllPhotos(con, Integer.toString(orderId)));

        listOrder.add(order);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listOrder;
  }

  public static Order getOrder(Connection con, String orderId, String instanceId)
      throws SQLException {
    String query = "select userId, creationDate, processDate, processUser from SC_Gallery_Order where orderId = ? and instanceId = ?";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    Order order = new Order(Integer.parseInt(orderId));
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(orderId));
      prepStmt.setString(2, instanceId);
      rs = prepStmt.executeQuery();

      if (rs.next()) {
        order.setUserId(rs.getInt(1));
        try {
          order.setCreationDate(new Date(Long.parseLong((String) rs
              .getString(2))));
        } catch (Exception e) {
          throw new SQLException(e.getMessage());
        }

        String processDate = null;
        if (StringUtil.isDefined(rs.getString(3))) {
          try {
            processDate = (String) rs.getString(3);
          } catch (Exception e) {
            throw new SQLException(e.getMessage());
          }
          order.setProcessDate(new Date(Long.parseLong(processDate)));
        }

        int processUserId = -1;
        if (StringUtil.isDefined(rs.getString(4)))
          processUserId = rs.getInt(4);
        order.setProcessUserId(processUserId);

        order.setInstanceId(instanceId);
        OrganizationController orga = new OrganizationController();
        order.setUserName(orga.getUserDetail(
            Integer.toString(order.getUserId())).getDisplayedName());
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    // ajouter les lignes
    order.setRows(getAllPhotos(con, orderId));
    return order;
  }

  public static Date getDownloadDate(Connection con, String orderId,
      String photoId) throws SQLException {
    // rechercher la date de téléchargement si elle existe
    String query = "select downloadDate from SC_Gallery_OrderDetail where orderId = ? and photoId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    Date downloadDate = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(orderId));
      prepStmt.setString(2, photoId);
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        if (rs.getString(1) != null)
          downloadDate = new Date(Long.parseLong((String) rs.getString(1)));
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return downloadDate;
  }

  public static List<Order> getAllOrdersToDelete(Connection con, int nbDays)
      throws SQLException {
    // récupérer toutes les demandes arrivant à échéance
    ArrayList<Order> listOrder = null;

    // calcul de la date de fin
    Calendar calendar = Calendar.getInstance(Locale.FRENCH);

    calendar.add(Calendar.DATE, -nbDays);
    Date date = calendar.getTime();
    // String dateLimite = DateUtil.date2SQLDate(date);

    String query = "select orderId, userId, instanceId, creationDate, processDate, processUser from SC_Gallery_Order where creationDate < ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, Long.toString(date.getTime()));

      rs = prepStmt.executeQuery();
      listOrder = new ArrayList<Order>();
      while (rs.next()) {
        int orderId = rs.getInt(1);

        Order order = new Order(orderId);
        order.setUserId(rs.getInt(2));
        order.setInstanceId(rs.getString(3));
        try {
          order.setCreationDate(new Date(Long.parseLong((String) rs
              .getString(4))));
        } catch (Exception e) {
          throw new SQLException(e.getMessage());
        }

        String processDate = null;
        if (StringUtil.isDefined(rs.getString(5))) {
          try {
            processDate = (String) rs.getString(5);
          } catch (Exception e) {
            throw new SQLException(e.getMessage());
          }
          order.setProcessDate(new Date(Long.parseLong(processDate)));
        }

        int processUserId = -1;
        if (StringUtil.isDefined(rs.getString(6)))
          processUserId = rs.getInt(6);
        order.setProcessUserId(processUserId);

        OrganizationController orga = new OrganizationController();
        order.setUserName(orga.getUserDetail(
            Integer.toString(order.getUserId())).getDisplayedName());

        // récupérer les lignes
        order.setRows(getAllPhotos(con, Integer.toString(orderId)));

        listOrder.add(order);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listOrder;
  }

  public static void deleteOrder(Connection con, String orderId)
      throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      String query = "delete from SC_Gallery_Order where orderId = ? ";
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(orderId));
      prepStmt.executeUpdate();

      // TODO : supprimer les lignes
      Collection<OrderRow> photos = getAllPhotos(con, orderId);
      if (photos != null) {
        Iterator<OrderRow> it = photos.iterator();
        while (it.hasNext()) {
          OrderRow row = it.next();
          deletePhotoByOrder(con, orderId, Integer.toString(row.getPhotoId()));
        }
      }

    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  private static void deletePhotoByOrder(Connection con, String orderId,
      String photoId) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      String query = "delete from SC_Gallery_OrderDetail where photoId = ? and orderId = ? ";
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(photoId));
      prepStmt.setInt(2, Integer.parseInt(orderId));
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }
}