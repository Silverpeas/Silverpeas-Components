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
import com.stratelia.webactiv.util.exception.UtilException;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.stratelia.webactiv.util.DBUtil.*;

public class OrderDAO {

  /**
   * Persists a new order.
   * @param con
   * @param basket
   * @param userId
   * @param instanceId
   * @return the identifier of the new order.
   * @throws SQLException
   * @throws UtilException
   */
  public static String createOrder(Connection con, Collection<String> basket, String userId,
      String instanceId) throws SQLException, UtilException {

    // New order
    String uuid = getUniqueId();
    executeUpdate(con,
        "insert into SC_Gallery_Order (orderId, userId, instanceId, createDate) values (?,?,?,?)",
        uuid, userId, instanceId, new Timestamp(new Date().getTime()));

    // Details of the order
    for (String mediaId : basket) {
      addOrderMedia(con, mediaId, uuid, instanceId);
    }

    return uuid;
  }

  /**
   * Adds a media to an order.
   * @param con
   * @param mediaId
   * @param orderId
   * @param instanceId
   * @throws SQLException
   */
  private static void addOrderMedia(Connection con, String mediaId, String orderId,
      String instanceId) throws SQLException {
    executeUpdate(con,
        "insert into SC_Gallery_OrderDetail (orderId, mediaId, instanceId) values (?,?,?)", orderId,
        mediaId, instanceId);
  }

  /**
   * Updates an order and its details.
   * @param con
   * @param order
   * @throws SQLException
   */
  public static void updateOrder(Connection con, Order order) throws SQLException {
    updateOrderStatus(con, order);
    List<OrderRow> rows = order.getRows();
    for (OrderRow row : rows) {
      executeUpdate(con, "update SC_Gallery_OrderDetail " +
              "set downloadDecision = ? " +
              "where orderId = ? and mediaId = ?", row.getDownloadDecision(), row.getOrderId(),
          row.getMediaId());
    }
  }

  /**
   * Updates the status of an order.
   * @param con
   * @param order
   * @throws SQLException
   */
  private static void updateOrderStatus(Connection con, Order order) throws SQLException {
    executeUpdate(con,
        "update SC_Gallery_Order set processDate = ?, processUser = ? where orderId = ?",
        new Timestamp(new Date().getTime()), order.getProcessUserId(), order.getOrderId());
  }

  /**
   * Deletes an order and its details.
   * @param con
   * @param order
   * @throws SQLException
   */
  public static void deleteOrder(Connection con, Order order) throws SQLException {
    executeUpdate(con, "delete from SC_Gallery_Order where orderId = ?", order.getOrderId());
    Collection<OrderRow> orderRows = getAllOrderDetails(con, order.getOrderId());
    if (orderRows != null) {
      for (final OrderRow row : orderRows) {
        deleteMediaFromOrder(con, row.getMediaId(), order.getOrderId());
      }
    }
  }

  /**
   * Deletes a media for an order.
   * @param con
   * @param mediaId
   * @param orderId
   * @throws SQLException
   */
  private static void deleteMediaFromOrder(Connection con, String mediaId, String orderId)
      throws SQLException {
    executeUpdate(con, "delete from SC_Gallery_OrderDetail where mediaId = ? and orderId = ?",
        mediaId, orderId);
  }

  /**
   * Gets all medias of an order.
   * @param con
   * @param orderId
   * @return
   * @throws SQLException
   */
  public static List<OrderRow> getAllOrderDetails(Connection con, final String orderId)
      throws SQLException {
    return select(con,
        "select mediaId, instanceId, downloadDate, downloadDecision  from SC_Gallery_OrderDetail " +
            "where orderId = ?", orderId, new SelectResultRowProcessor<OrderRow>() {

          @Override
          protected OrderRow currentRow(final int rowIndex, final ResultSet rs) throws SQLException {
            String mediaId = rs.getString(1);
            String instanceId = rs.getString(2);
            OrderRow orderRow = new OrderRow(orderId, mediaId, instanceId);
            orderRow.setDownloadDate(rs.getTimestamp(3));
            orderRow.setDownloadDecision(rs.getString(4));
            return orderRow;
          }
        });
  }

  /**
   * Updates a row of an order.
   * @param con
   * @param row
   * @throws SQLException
   */
  public static void updateOrderRow(Connection con, OrderRow row) throws SQLException {
    executeUpdate(con,
        "update SC_Gallery_OrderDetail set downloadDate = ?, downloadDecision = ? where orderId =" +
            " ? and mediaId = ?", new Timestamp(new Date().getTime()), row.getDownloadDecision(),
        row.getOrderId(), row.getMediaId());
  }

  /**
   * Gets a unique result.
   * @param con
   * @param criteria
   * @return
   * @throws SQLException
   */
  public static Order getByCriteria(final Connection con, MediaOrderCriteria criteria)
      throws SQLException {
    return unique(findByCriteria(con, criteria));
  }

  /**
   * Finds orders from the specified criteria.
   * @param con
   * @param criteria
   * @return a list of orders, empty if no order found.
   */
  public static List<Order> findByCriteria(final Connection con, MediaOrderCriteria criteria)
      throws SQLException {

    MediaOrderSQLQueryBuilder queryBuilder = new MediaOrderSQLQueryBuilder();
    criteria.processWith(queryBuilder);

    Pair<String, List<Object>> queryBuild = queryBuilder.result();

    return queryBuilder.orderingResult(select(con, queryBuild.getLeft(), queryBuild.getRight(),
        new SelectResultRowProcessor<Order>() {

          @Override
          protected Order currentRow(final int rowIndex, final ResultSet rs) throws SQLException {
            Order order = new Order(rs.getString(1));
            order.setUserId(rs.getString(2));
            order.setInstanceId(rs.getString(3));
            order.setCreationDate(rs.getTimestamp(4));
            order.setProcessDate(rs.getTimestamp(5));
            order.setProcessUserId(rs.getString(6));
            order.setRows(getAllOrderDetails(con, order.getOrderId()));
            return order;
          }
        }));
  }
}