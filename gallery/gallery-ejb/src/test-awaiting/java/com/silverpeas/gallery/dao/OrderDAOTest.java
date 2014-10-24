/*
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
 *
 */
package com.silverpeas.gallery.dao;

import com.silverpeas.gallery.BaseGalleryTest;
import com.silverpeas.gallery.model.MediaOrderCriteria;
import com.silverpeas.gallery.model.Order;
import com.silverpeas.gallery.model.OrderRow;
import org.silverpeas.util.CollectionUtil;
import org.silverpeas.util.DateUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.junit.Test;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author ehugonnet
 */
public class OrderDAOTest extends BaseGalleryTest {

  /**
   * Test of createOrder method, of class OrderDAO.
   */
  @Test
  public void testCreateOrder() throws Exception {
    performDAOTest(new DAOTest() {
      @Override
      public void test(final Connection connection) throws Exception {
        Date now = DateUtil.getNow();
        Collection<String> basket = CollectionUtil.asList("100", "102", "101");
        String userId = "10";
        String instanceId = "gallery50";
        String newId = OrderDAO.createOrder(connection, basket, userId, instanceId);
        assertThat(newId, notNullValue());

        IDataSet actualDataSet = getActualDataSet();
        ITable mediaTable = actualDataSet.getTable("SC_Gallery_Media");
        assertThat(mediaTable.getRowCount(), is(MEDIA_ROW_COUNT));
        ITable internalTable = actualDataSet.getTable("SC_Gallery_Internal");
        assertThat(internalTable.getRowCount(), is(MEDIA_INTERNAL_ROW_COUNT));
        ITable photoTable = actualDataSet.getTable("SC_Gallery_Photo");
        assertThat(photoTable.getRowCount(), is(MEDIA_PHOTO_ROW_COUNT));
        ITable videoTable = actualDataSet.getTable("SC_Gallery_Video");
        assertThat(videoTable.getRowCount(), is(MEDIA_VIDEO_ROW_COUNT));
        ITable soundTable = actualDataSet.getTable("SC_Gallery_Sound");
        assertThat(soundTable.getRowCount(), is(MEDIA_SOUND_ROW_COUNT));
        ITable streamingTable = actualDataSet.getTable("SC_Gallery_Streaming");
        assertThat(streamingTable.getRowCount(), is(MEDIA_STREAMING_ROW_COUNT));
        ITable pathTable = actualDataSet.getTable("SC_Gallery_Path");
        assertThat(pathTable.getRowCount(), is(MEDIA_PATH_ROW_COUNT));
        ITable orderTable = actualDataSet.getTable("SC_Gallery_Order");
        assertThat(orderTable.getRowCount(), is(MEDIA_ORDER_ROW_COUNT + 1));
        ITable orderDetailTable = actualDataSet.getTable("SC_Gallery_OrderDetail");
        assertThat(orderDetailTable.getRowCount(),
            is(MEDIA_ORDER_DETAIL_ROW_COUNT + basket.size()));

        TableRow orderRow = getTableRowFor(orderTable, "orderId", newId);
        assertThat(orderRow.getString("userId"), is(userId));
        assertThat(orderRow.getString("instanceId"), is(instanceId));
        assertThat(orderRow.getDate("createDate"), greaterThanOrEqualTo(now));
        assertThat(orderRow.getDate("processDate"), nullValue());
        assertThat(orderRow.getString("processUser"), nullValue());

        List<TableRow> orderDetails = getTableRowsFor(orderDetailTable, "orderId", newId);
        assertThat(orderDetails, hasSize(basket.size()));
        Iterator<String> mediaIdIt = basket.iterator();
        for (TableRow orderDetail : orderDetails) {
          String mediaId = mediaIdIt.next();
          assertThat(basket, hasItem(mediaId));
          assertThat(orderDetail.getString("orderId"), is(newId));
          assertThat(orderDetail.getString("mediaId"), is(mediaId));
          assertThat(orderDetail.getString("instanceId"), is(instanceId));
          assertThat(orderDetail.getDate("downloadDate"), nullValue());
          assertThat(orderDetail.getString("downloadDecision"), nullValue());
        }
      }
    });
  }

  /**
   * Test of getAllPhotos method, of class OrderDAO.
   */
  @Test
  public void testGetAllOrderDetail() throws Exception {
    performDAOTest(new DAOTest() {
      @Override
      public void test(final Connection connection) throws Exception {
        String orderId = "201";
        List<OrderRow> orderDetails = OrderDAO.getAllOrderDetails(orderId);
        assertThat(orderDetails, hasSize(2));
        assertThat(
            Arrays.asList(orderDetails.get(0).getMediaId(), orderDetails.get(1).getMediaId()),
            containsInAnyOrder("1", "v_2"));
      }
    });
  }

  /**
   * Test of updateOrder method, of class OrderDAO.
   */
  @Test
  public void testUpdateOrder() throws Exception {
    performDAOTest(new DAOTest() {
      @Override
      public void test(final Connection connection) throws Exception {
        Date now = DateUtil.getNow();
        Order order = OrderDAO.getByCriteria(connection,
            MediaOrderCriteria.fromComponentInstanceId(INSTANCE_A).identifierIsOneOf("201"));
        order.setProcessUserId(adminAccessUser.getId());
        for (OrderRow orderRow : order.getRows()) {
          orderRow.setDownloadDecision("D");
        }
        OrderDAO.updateOrder(connection, order);

        IDataSet actualDataSet = getActualDataSet();
        ITable mediaTable = actualDataSet.getTable("SC_Gallery_Media");
        assertThat(mediaTable.getRowCount(), is(MEDIA_ROW_COUNT));
        ITable internalTable = actualDataSet.getTable("SC_Gallery_Internal");
        assertThat(internalTable.getRowCount(), is(MEDIA_INTERNAL_ROW_COUNT));
        ITable photoTable = actualDataSet.getTable("SC_Gallery_Photo");
        assertThat(photoTable.getRowCount(), is(MEDIA_PHOTO_ROW_COUNT));
        ITable videoTable = actualDataSet.getTable("SC_Gallery_Video");
        assertThat(videoTable.getRowCount(), is(MEDIA_VIDEO_ROW_COUNT));
        ITable soundTable = actualDataSet.getTable("SC_Gallery_Sound");
        assertThat(soundTable.getRowCount(), is(MEDIA_SOUND_ROW_COUNT));
        ITable streamingTable = actualDataSet.getTable("SC_Gallery_Streaming");
        assertThat(streamingTable.getRowCount(), is(MEDIA_STREAMING_ROW_COUNT));
        ITable pathTable = actualDataSet.getTable("SC_Gallery_Path");
        assertThat(pathTable.getRowCount(), is(MEDIA_PATH_ROW_COUNT));
        ITable orderTable = actualDataSet.getTable("SC_Gallery_Order");
        assertThat(orderTable.getRowCount(), is(MEDIA_ORDER_ROW_COUNT));
        ITable orderDetailTable = actualDataSet.getTable("SC_Gallery_OrderDetail");
        assertThat(orderDetailTable.getRowCount(), is(MEDIA_ORDER_DETAIL_ROW_COUNT));

        TableRow orderRow = getTableRowFor(orderTable, "orderId", order.getOrderId());
        assertThat(orderRow.getString("userId"), is(order.getUserId()));
        assertThat(orderRow.getString("instanceId"), is(INSTANCE_A));
        assertThat(orderRow.getDate("createDate"), lessThan(now));
        assertThat(orderRow.getDate("processDate"), greaterThanOrEqualTo(now));
        assertThat(orderRow.getString("processUser"), is(adminAccessUser.getId()));

        List<TableRow> orderDetails =
            getTableRowsFor(orderDetailTable, "orderId", order.getOrderId());
        assertThat(orderDetails, hasSize(2));
        for (TableRow orderDetail : orderDetails) {
          assertThat(orderDetail.getString("orderId"), is(order.getOrderId()));
          assertThat(orderDetail.getString("instanceId"), is(INSTANCE_A));
          if (orderDetail.getString("mediaId").equals("1")) {
            assertThat(orderDetail.getDate("downloadDate"), nullValue());
          } else {
            assertThat(orderDetail.getDate("downloadDate").getTime(),
                is(Timestamp.valueOf("2014-12-31 12:59:59.999").getTime()));
          }
          assertThat(orderDetail.getString("downloadDecision"), is("D"));
        }
      }
    });
  }

  /**
   * Test of updateOrderRow method, of class OrderDAO.
   */
  @Test
  public void testUpdateOrderRow() throws Exception {
    performDAOTest(new DAOTest() {
      @Override
      public void test(final Connection connection) throws Exception {
        Date now = DateUtil.getNow();
        Order order = OrderDAO.getByCriteria(connection,
            MediaOrderCriteria.fromComponentInstanceId(INSTANCE_A).identifierIsOneOf("201"));
        order.setProcessUserId(adminAccessUser.getId());
        order.getRows().get(0).setDownloadDecision("D");
        OrderDAO.updateOrderRow(connection, order.getRows().get(0));
        String mediaId = order.getRows().get(0).getMediaId();

        IDataSet actualDataSet = getActualDataSet();
        ITable orderDetailTable = actualDataSet.getTable("SC_Gallery_OrderDetail");
        assertThat(orderDetailTable.getRowCount(), is(MEDIA_ORDER_DETAIL_ROW_COUNT));

        List<TableRow> orderDetails =
            getTableRowsFor(orderDetailTable, "orderId", order.getOrderId());
        assertThat(orderDetails, hasSize(2));
        for (TableRow orderDetail : orderDetails) {
          assertThat(orderDetail.getString("orderId"), is(order.getOrderId()));
          assertThat(orderDetail.getString("instanceId"), is(INSTANCE_A));
          if (mediaId.equals(orderDetail.getString("mediaId"))) {
            assertThat(orderDetail.getDate("downloadDate"), greaterThanOrEqualTo(now));
            assertThat(orderDetail.getString("downloadDecision"), is("D"));
          } else {
            assertThat(orderDetail.getString("downloadDecision"), not(is("D")));
          }
        }
      }
    });
  }

  /**
   * Test of getAllOrders method, of class OrderDAO.
   */
  @Test
  public void testGetAllOrders() throws Exception {
    performDAOTest(new DAOTest() {
      @Override
      public void test(final Connection connection) throws Exception {
        List<Order> orders = OrderDAO.findByCriteria(connection,
            MediaOrderCriteria.fromComponentInstanceId(INSTANCE_A)
                .withOrdererId(writerUser.getId()));
        assertThat(orders, is(notNullValue()));
        assertThat(orders, hasSize(2));
        String orderId = null;
        for (Order order : orders) {
          assertThat(order.getUserId(), is(writerUser.getId()));
          if (orderId == null) {
            orderId = order.getOrderId();
          } else {
            assertThat(orderId, not(is(order.getOrderId())));
          }
        }
      }
    });
  }

  /**
   * Test of getOrder method, of class OrderDAO.
   */
  @Test
  public void testGetOrder() throws Exception {
    performDAOTest(new DAOTest() {
      @Override
      public void test(final Connection connection) throws Exception {
        Order order = OrderDAO.getByCriteria(connection,
            MediaOrderCriteria.fromComponentInstanceId(INSTANCE_A).identifierIsOneOf("100"));
        assertThat(order, nullValue());

        assertThat(OrderDAO.findByCriteria(connection,
            MediaOrderCriteria.fromComponentInstanceId(INSTANCE_A)
                .identifierIsOneOf("100", "200", "201", "202")), hasSize(2));

        order = OrderDAO.getByCriteria(connection,
            MediaOrderCriteria.fromComponentInstanceId(INSTANCE_A).identifierIsOneOf("201"));

        assertThat(order.getOrderId(), is("201"));
        assertThat(order.getUserId(), is(writerUser.getId()));
        assertThat(order.getInstanceId(), is(INSTANCE_A));
        assertThat(order.getCreationDate(), is(CREATE_DATE));
        assertThat(order.getProcessDate().getTime(),
            is(Timestamp.valueOf("2014-06-30 12:59:59.999").getTime()));
        assertThat(order.getProcessUserId(), is(adminAccessUser.getId()));

        // This block is for orderer testing.
        assertThat(order.getUserName(), is(writerUser.getDisplayedName()));
        order.setUserId(adminAccessUser.getId());
        assertThat(order.getUserName(), is(adminAccessUser.getDisplayedName()));
        order.setOrderer(publisherUser);
        assertThat(order.getUserId(), is(publisherUser.getId()));
        assertThat(order.getUserName(), is(publisherUser.getDisplayedName()));

        assertThat(order.getRows(), hasSize(2));

        OrderRow orderRow = order.getRows().get(0);
        if (!orderRow.getOrderId().equals("v_2")) {
          orderRow = order.getRows().get(1);
        }
        assertThat(orderRow.getOrderId(), is("201"));
        assertThat(orderRow.getMediaId(), is("v_2"));
        assertThat(orderRow.getInstanceId(), is(INSTANCE_A));
        assertThat(orderRow.getDownloadDate().getTime(),
            is(Timestamp.valueOf("2014-12-31 12:59:59.999").getTime()));
        assertThat(orderRow.getDownloadDecision(), is("T"));
      }
    });
  }

  /**
   * Test of getAllOrdersToDelete method, of class OrderDAO.
   */
  @Test
  public void testGetAllOrdersToDelete() throws Exception {
    performDAOTest(new DAOTest() {
      @Override
      public void test(final Connection connection) throws Exception {
        List<Order> ordersToDelete = OrderDAO.findByCriteria(connection,
            MediaOrderCriteria.fromNbDaysAfterThatDeleteAnOrder(0)
                .referenceDateOf(DateUtils.addDays(CREATE_DATE, 1)));
        assertThat(ordersToDelete, hasSize(3));

        ordersToDelete = OrderDAO.findByCriteria(connection,
            MediaOrderCriteria.fromNbDaysAfterThatDeleteAnOrder(1)
                .referenceDateOf(DateUtils.addDays(CREATE_DATE, 1)));
        assertThat(ordersToDelete, hasSize(0));
      }
    });
  }

  /**
   * Test of deleteOrder method, of class OrderDAO.
   */
  @Test
  public void testDeleteOrder() throws Exception {
    performDAOTest(new DAOTest() {
      @Override
      public void test(final Connection connection) throws Exception {
        Order order = OrderDAO.getByCriteria(connection,
            MediaOrderCriteria.fromComponentInstanceId(INSTANCE_A).identifierIsOneOf("201"));
        assertThat(order, notNullValue());

        OrderDAO.deleteOrder(connection, order);

        IDataSet actualDataSet = getActualDataSet();
        ITable mediaTable = actualDataSet.getTable("SC_Gallery_Media");
        assertThat(mediaTable.getRowCount(), is(MEDIA_ROW_COUNT));
        ITable internalTable = actualDataSet.getTable("SC_Gallery_Internal");
        assertThat(internalTable.getRowCount(), is(MEDIA_INTERNAL_ROW_COUNT));
        ITable photoTable = actualDataSet.getTable("SC_Gallery_Photo");
        assertThat(photoTable.getRowCount(), is(MEDIA_PHOTO_ROW_COUNT));
        ITable videoTable = actualDataSet.getTable("SC_Gallery_Video");
        assertThat(videoTable.getRowCount(), is(MEDIA_VIDEO_ROW_COUNT));
        ITable soundTable = actualDataSet.getTable("SC_Gallery_Sound");
        assertThat(soundTable.getRowCount(), is(MEDIA_SOUND_ROW_COUNT));
        ITable streamingTable = actualDataSet.getTable("SC_Gallery_Streaming");
        assertThat(streamingTable.getRowCount(), is(MEDIA_STREAMING_ROW_COUNT));
        ITable pathTable = actualDataSet.getTable("SC_Gallery_Path");
        assertThat(pathTable.getRowCount(), is(MEDIA_PATH_ROW_COUNT));
        ITable orderTable = actualDataSet.getTable("SC_Gallery_Order");
        assertThat(orderTable.getRowCount(), is(MEDIA_ORDER_ROW_COUNT - 1));
        ITable orderDetailTable = actualDataSet.getTable("SC_Gallery_OrderDetail");
        assertThat(orderDetailTable.getRowCount(), is(MEDIA_ORDER_DETAIL_ROW_COUNT - 2));
      }
    });
  }
}
