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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.dao;

import com.silverpeas.gallery.model.MediaOrderCriteria;
import com.silverpeas.gallery.model.Order;
import com.silverpeas.gallery.model.OrderRow;
import org.silverpeas.persistence.jdbc.JdbcSqlQuery;
import org.silverpeas.util.exception.UtilException;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.silverpeas.persistence.jdbc.JdbcSqlQuery.*;
import static org.silverpeas.util.DBUtil.getUniqueId;

public class OrderDAO {

  /**
   * Persists a new order.
   * @param mediaIds the identifier list of media to attach to a new order.
   * @param userId the identifier of the user that is creating the order.
   * @param instanceId the identifier of a component instance.
   * @return the identifier of the new order.
   * @throws SQLException
   * @throws UtilException
   */
  public static String createOrder(Collection<String> mediaIds, String userId, String instanceId)
      throws SQLException, UtilException {

    // New order
    String uuid = getUniqueId();
    JdbcSqlQuery insert = createInsertFor("SC_Gallery_Order");
    insert.addInsertParam("orderId", uuid);
    insert.addInsertParam("userId", userId);
    insert.addInsertParam("instanceId", instanceId);
    insert.addInsertParam("createDate", new Timestamp(new Date().getTime()));
    insert.execute();

    // Details of the order
    for (String mediaId : mediaIds) {
      addOrderMedia(mediaId, uuid, instanceId);
    }

    return uuid;
  }

  /**
   * Adds a media to an order.
   * @param mediaId the identifier of a media.
   * @param orderId the identifier of an order.
   * @param instanceId the identifier of a component instance.
   * @throws SQLException
   */
  private static void addOrderMedia(String mediaId, String orderId, String instanceId)
      throws SQLException {
    JdbcSqlQuery insert = createInsertFor("SC_Gallery_OrderDetail");
    insert.addInsertParam("orderId", orderId);
    insert.addInsertParam("mediaId", mediaId);
    insert.addInsertParam("instanceId", instanceId);
    insert.execute();
  }

  /**
   * Updates an order and its details.
   * @param order an order.
   * @throws SQLException
   */
  public static void updateOrder(Order order) throws SQLException {
    updateOrderStatus(order);
    List<OrderRow> rows = order.getRows();
    for (OrderRow row : rows) {
      JdbcSqlQuery update = createInsertFor("SC_Gallery_OrderDetail");
      update.addUpdateParam("downloadDecision", row.getDownloadDecision());
      update.where("orderId = ? and mediaId = ?", row.getOrderId(), row.getMediaId());
      update.execute();
    }
  }

  /**
   * Updates the status of an order.
   * @param order an order.
   * @throws SQLException
   */
  private static void updateOrderStatus(Order order) throws SQLException {
    JdbcSqlQuery update = createInsertFor("SC_Gallery_Order");
    update.addUpdateParam("processDate", new Timestamp(new Date().getTime()));
    update.addUpdateParam("processUser", order.getProcessUserId());
    update.where("orderId = ?", order.getOrderId());
    update.execute();
  }

  /**
   * Deletes an order and its details.
   * @param order an order.
   * @throws SQLException
   */
  public static void deleteOrder(Order order) throws SQLException {
    createDeleteFor("SC_Gallery_Order").where("orderId = ?", order.getOrderId()).execute();
    Collection<OrderRow> orderRows = getAllOrderDetails(order.getOrderId());
    if (orderRows != null) {
      for (final OrderRow row : orderRows) {
        deleteMediaFromOrder(row.getMediaId(), order.getOrderId());
      }
    }
  }

  /**
   * Deletes a media for an order.
   * @param mediaId the identifier of a media.
   * @param orderId the identifier of an order.
   * @throws SQLException
   */
  private static void deleteMediaFromOrder(String mediaId, String orderId) throws SQLException {
    createDeleteFor("SC_Gallery_OrderDetail").where("mediaId = ? and orderId = ?", mediaId, orderId)
        .execute();
  }

  /**
   * Gets all medias of an order.
   * @param orderId identifier of an order.
   * @return the list of details related of the given identifier of order.
   * @throws SQLException
   */
  public static List<OrderRow> getAllOrderDetails(final String orderId) throws SQLException {
    return createSelect(
        "mediaId, instanceId, downloadDate, downloadDecision from SC_Gallery_OrderDetail")
        .where("orderId = ?", orderId).execute(row -> {
          String mediaId = row.getString(1);
          String instanceId = row.getString(2);
          OrderRow orderRow = new OrderRow(orderId, mediaId, instanceId);
          orderRow.setDownloadDate(row.getTimestamp(3));
          orderRow.setDownloadDecision(row.getString(4));
          return orderRow;
        });
  }

  /**
   * Updates a row of an order.
   * @param row details of an order.
   * @throws SQLException
   */
  public static void updateOrderRow(OrderRow row) throws SQLException {
    JdbcSqlQuery update = createInsertFor("SC_Gallery_OrderDetail");
    update.addUpdateParam("downloadDate", new Timestamp(new Date().getTime()));
    update.addUpdateParam("downloadDecision", row.getDownloadDecision());
    update.where("orderId = ? and mediaId = ?", row.getOrderId(), row.getMediaId());
    update.execute();
  }

  /**
   * Gets a unique result.
   * @param criteria the criteria that permits to filter the result.
   * @return the unique result of the query performed.
   * @throws SQLException
   */
  public static Order getByCriteria(MediaOrderCriteria criteria) throws SQLException {
    return unique(findByCriteria(criteria));
  }

  /**
   * Finds orders from the specified criteria.
   * @param criteria the criteria that permits to filter the result.
   * @return a list of orders, empty if no order found.
   */
  public static List<Order> findByCriteria(MediaOrderCriteria criteria) throws SQLException {

    MediaOrderSQLQueryBuilder queryBuilder = new MediaOrderSQLQueryBuilder();
    criteria.processWith(queryBuilder);

    JdbcSqlQuery queryBuild = queryBuilder.result();

    return queryBuilder.orderingResult(queryBuild.execute(row -> {
      Order order = new Order(row.getString(1));
      order.setUserId(row.getString(2));
      order.setInstanceId(row.getString(3));
      order.setCreationDate(row.getTimestamp(4));
      order.setProcessDate(row.getTimestamp(5));
      order.setProcessUserId(row.getString(6));
      order.setRows(getAllOrderDetails(order.getOrderId()));
      return order;
    }));
  }
}