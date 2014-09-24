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
package com.silverpeas.gallery.dao;

import com.silverpeas.gallery.model.Order;
import com.silverpeas.gallery.model.OrderRow;
import org.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.UtilException;
import org.silverpeas.core.admin.OrganisationController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OldOrderDAO {

  private OrganisationController orga;

  public OldOrderDAO() {
    this(new OrganizationController());
  }

  OldOrderDAO(OrganisationController orga) {
    this.orga = orga;
  }

  public String createOrder(Connection con, Collection<String> basket,
          String userId, String instanceId) throws SQLException, UtilException {
    // Création d'une commande
    String id = "";
    PreparedStatement prepStmt = null;
    try {
      // 1. création de l'entête de la demande
      int newId = DBUtil.getNextId("SC_Gallery_Order", "orderId");
      id = Integer.toString(newId);
      // création de la requête
      String query =
              "insert into SC_Gallery_Order (orderId, userId, instanceId, creationDate) values (?,?,?,?)";
      // initialisation des paramètres
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, newId);
      prepStmt.setInt(2, Integer.parseInt(userId));
      prepStmt.setString(3, instanceId);
      prepStmt.setString(4, Long.toString(System.currentTimeMillis()));
      prepStmt.executeUpdate();

      // 2. création des lignes de la demande
      for (String photoId : basket) {
        addPhoto(con, photoId, id, instanceId);
      }
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
    return id;
  }

  public List<OrderRow> getAllPhotos(Connection con, int orderId) throws SQLException {
    List<OrderRow> listPhoto = new ArrayList<OrderRow>();
    String query =
            "select photoId, instanceId, downloadDate, downloadDecision  from SC_Gallery_OrderDetail where orderId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, orderId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        String photoId = rs.getString(1);
        String instanceId = rs.getString(2);
        OrderRow orderRow = new OrderRow(String.valueOf(orderId), photoId, instanceId);
        String downloadDate = rs.getString(3);
        if (downloadDate != null && StringUtil.isLong(downloadDate)) {
          orderRow.setDownloadDate(new Date(Long.parseLong(downloadDate)));
        }
        orderRow.setDownloadDecision(rs.getString(4));
        listPhoto.add(orderRow);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return listPhoto;
  }

  public static void updateOrder(Connection con, Order order) throws SQLException {
    updateOrderStatus(con, order);
    String query =
            "update SC_Gallery_OrderDetail set downloadDecision = ? where orderId = ? and photoId = ? ";
    PreparedStatement prepStmt = con.prepareStatement(query);
    try {
      List<OrderRow> rows = order.getRows();
      for (OrderRow row : rows) {
        prepStmt.setString(1, row.getDownloadDecision());
        prepStmt.setInt(2, Integer.valueOf(row.getOrderId()));
        prepStmt.setInt(3, Integer.valueOf(row.getMediaId()));
        prepStmt.executeUpdate();
      }
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  private static void updateOrderStatus(Connection con, Order order) throws SQLException {
    String query = "update SC_Gallery_Order set processDate = ?, processUser = ? where orderId = ?";
    PreparedStatement prepStmt = con.prepareStatement(query);
    try {
      prepStmt.setString(1, String.valueOf(System.currentTimeMillis()));
      prepStmt.setInt(2, Integer.valueOf(order.getProcessUserId()));
      prepStmt.setInt(3, Integer.valueOf(order.getOrderId()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void updateOrderRow(Connection con, OrderRow row)
          throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      // mettre à jour l'entête
      String query =
              "update SC_Gallery_OrderDetail set downloadDate = ?, downloadDecision = ? where orderId = ? and photoId = ? ";
      // initialisation des paramètres
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, Long.toString(System.currentTimeMillis()));
      prepStmt.setString(2, row.getDownloadDecision());
      prepStmt.setInt(3, new Integer(row.getOrderId()));
      prepStmt.setInt(4, new Integer(row.getMediaId()));
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
      String query =
              "insert into SC_Gallery_OrderDetail (orderId, photoId, instanceId) values (?,?,?)";
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

  public List<Order> getAllOrders(Connection con, String userId,
          String instanceId) throws SQLException {
    // récupérer toutes les demandes de l'utilisateur sur cette instance
    ArrayList<Order> listOrder = null;

    boolean allUsers = false;
    if (userId.equals("-1")) {
      allUsers = true;
    }
    String query =
            "select orderId, userId, creationDate, processDate, processUser from SC_Gallery_Order where instanceId = ? ";
    if (!allUsers) {
      query = query + "and userId = ? ";
    }
    query = query + " order by creationDate desc";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      if (!allUsers) {
        prepStmt.setInt(2, Integer.parseInt(userId));
      }

      rs = prepStmt.executeQuery();
      listOrder = new ArrayList<Order>();
      while (rs.next()) {
        int orderId = rs.getInt("orderId");

        Order order = new Order(String.valueOf(orderId));
        order.setUserId(String.valueOf(rs.getInt("userId")));
        try {
          order.setCreationDate(new Date(Long.parseLong(rs.getString("creationDate"))));
        } catch (Exception e) {
          throw new SQLException(e.getMessage());
        }

        String processDate = null;
        if (StringUtil.isDefined(rs.getString("processDate"))) {
          try {
            processDate = rs.getString("processDate");
          } catch (Exception e) {
            throw new SQLException(e.getMessage());
          }
          order.setProcessDate(new Date(Long.parseLong(processDate)));
        }

        int processUserId = -1;
        if (StringUtil.isDefined(rs.getString("processUser"))) {
          processUserId = rs.getInt("processUser");
        }
        order.setProcessUserId(String.valueOf(processUserId));

        order.setInstanceId(instanceId);
        // récupérer les lignes
        order.setRows(getAllPhotos(con, orderId));
        listOrder.add(order);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listOrder;
  }

  public Order getOrder(Connection con, String orderId, String instanceId) throws SQLException {
    String query =
            "select userId, creationDate, processDate, processUser from SC_Gallery_Order where orderId = ? and instanceId = ?";
    PreparedStatement prepStmt = con.prepareStatement(query);
    ResultSet rs = null;
    int currentOrderId = Integer.parseInt(orderId);
    Order order = new Order(orderId);
    try {
      prepStmt.setInt(1, Integer.parseInt(orderId));
      prepStmt.setString(2, instanceId);
      rs = prepStmt.executeQuery();

      if (rs.next()) {
        order.setUserId(String.valueOf(rs.getInt(1)));
        try {
          order.setCreationDate(new Date(Long.parseLong(rs.getString(2))));
        } catch (Exception e) {
          throw new SQLException(e.getMessage());
        }

        String processDate = null;
        if (StringUtil.isDefined(rs.getString(3))) {
          try {
            processDate = rs.getString(3);
          } catch (Exception e) {
            throw new SQLException(e.getMessage());
          }
          order.setProcessDate(new Date(Long.parseLong(processDate)));
        }

        int processUserId = -1;
        if (StringUtil.isDefined(rs.getString(4))) {
          processUserId = rs.getInt(4);
        }
        order.setProcessUserId(String.valueOf(processUserId));

        order.setInstanceId(instanceId);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    // ajouter les lignes
    order.setRows(getAllPhotos(con, currentOrderId));
    return order;
  }

  public List<Order> getAllOrdersToDelete(Connection con, int nbDays)
          throws SQLException {
    // récupérer toutes les demandes arrivant à échéance
    ArrayList<Order> listOrder = null;

    // calcul de la date de fin
    Calendar calendar = Calendar.getInstance(Locale.FRENCH);

    calendar.add(Calendar.DATE, -nbDays);
    Date date = calendar.getTime();
    // String dateLimite = DateUtil.date2SQLDate(date);

    String query =
            "select orderId, userId, instanceId, creationDate, processDate, processUser from SC_Gallery_Order where creationDate < ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, Long.toString(date.getTime()));

      rs = prepStmt.executeQuery();
      listOrder = new ArrayList<Order>();
      while (rs.next()) {
        int orderId = rs.getInt(1);

        Order order = new Order(String.valueOf(orderId));
        order.setUserId(String.valueOf(rs.getInt(2)));
        order.setInstanceId(rs.getString(3));
        try {
          order.setCreationDate(new Date(Long.parseLong(rs.getString(4))));
        } catch (Exception e) {
          throw new SQLException(e.getMessage());
        }

        String processDate = null;
        if (StringUtil.isDefined(rs.getString(5))) {
          try {
            processDate = rs.getString(5);
          } catch (Exception e) {
            throw new SQLException(e.getMessage());
          }
          order.setProcessDate(new Date(Long.parseLong(processDate)));
        }

        int processUserId = -1;
        if (StringUtil.isDefined(rs.getString(6))) {
          processUserId = rs.getInt(6);
        }
        order.setProcessUserId(String.valueOf(processUserId));

        // récupérer les lignes
        order.setRows(getAllPhotos(con, orderId));

        listOrder.add(order);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listOrder;
  }

  public void deleteOrder(Connection con, String orderId) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      int currentOrderId = Integer.parseInt(orderId);
      String query = "delete from SC_Gallery_Order where orderId = ? ";
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, currentOrderId);
      prepStmt.executeUpdate();

      // TODO : supprimer les lignes
      Collection<OrderRow> photos = getAllPhotos(con, currentOrderId);
      if (photos != null) {
        for (final OrderRow row : photos) {
          deletePhotoByOrder(con, orderId, row.getMediaId());
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