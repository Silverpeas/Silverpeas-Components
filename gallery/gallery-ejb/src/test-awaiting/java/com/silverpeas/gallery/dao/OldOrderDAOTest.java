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

import com.silverpeas.gallery.BaseGalleryTest;
import com.silverpeas.gallery.model.Order;
import com.silverpeas.gallery.model.OrderRow;
import org.silverpeas.util.CollectionUtil;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.util.DBUtil;
import org.junit.Test;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author ehugonnet
 */
public class OldOrderDAOTest extends BaseGalleryTest {

  @Override
  public String[] getApplicationContextPath() {
    return new String[]{"/spring-gallery-embbed-datasource.xml"};
  }

  @Override
  public String getDataSetPath() {
    return "com/silverpeas/gallery/dao/order_dataset.xml";
  }

  @Override
  protected void verifyDataBeforeTest() throws Exception {
    // Skip
  }

  /**
   * Test of createOrder method, of class OldOrderDAO.
   */
  @Test
  public void testCreateOrder() throws Exception {
    Collection<String> basket = CollectionUtil.asList("100", "102", "101");
    String userId = "10";
    String instanceId = "gallery50";
    UserDetail bart = new UserDetail();
    bart.setFirstName("Bart");
    bart.setLastName("Simpson");
    when(getOrganisationControllerMock().getUserDetail(userId)).thenReturn(bart);
    Connection con = getConnection();
    try {
      OldOrderDAO dao = new OldOrderDAO(getOrganisationControllerMock());
      String result = dao.createOrder(con, basket, userId, instanceId);
      String newOrderId = "210";
      assertThat(result, is(newOrderId));
      con.commit();
      Order newOrder = dao.getOrder(con, newOrderId, instanceId);
      assertThat(newOrder, is(notNullValue()));
      assertThat(newOrder.getInstanceId(), is(instanceId));
      assertThat(newOrder.getUserName(), is("Bart Simpson"));
      assertThat(newOrder.getOrderId(), is("210"));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of getAllPhotos method, of class OldOrderDAO.
   */
  @Test
  public void testGetAllPhotos() throws Exception {
    String userId = "10";
    UserDetail bart = new UserDetail();
    bart.setFirstName("Bart");
    bart.setLastName("Simpson");
    when(getOrganisationControllerMock().getUserDetail(userId)).thenReturn(bart);
    Connection con = getConnection();
    try {
      OldOrderDAO dao = new OldOrderDAO(getOrganisationControllerMock());
      int orderId = 201;
      List<OrderRow> photos = dao.getAllPhotos(con, orderId);
      assertThat(photos, is(notNullValue()));
      assertThat(photos, hasSize(2));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of updateOrder method, of class OldOrderDAO.
   */
  //@Test
  public void testUpdateOrder() throws Exception {
    System.out.println("updateOrder");
    Connection con = null;
    Order order = null;
    OldOrderDAO.updateOrder(con, order);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of updateOrderRow method, of class OldOrderDAO.
   */
  //@Test
  public void testUpdateOrderRow() throws Exception {
    System.out.println("updateOrderRow");
    Connection con = null;
    OrderRow row = null;
    OldOrderDAO.updateOrderRow(con, row);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getAllOrders method, of class OldOrderDAO.
   */
  // @Test
  public void testGetAllOrders() throws Exception {
    String userId = "10";
    String instanceId = "gallery50";
    UserDetail bart = new UserDetail();
    bart.setFirstName("Bart");
    bart.setLastName("Simpson");
    when(getOrganisationControllerMock().getUserDetail(userId)).thenReturn(bart);
    Connection con = getConnection();
    try {
      OldOrderDAO dao = new OldOrderDAO(getOrganisationControllerMock());
      List<Order> orders = dao.getAllOrders(con, userId, instanceId);
      assertThat(orders, is(notNullValue()));
      assertThat(orders, hasSize(2));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of getOrder method, of class OldOrderDAO.
   */
  @Test
  public void testGetOrder() throws Exception {
    String userId = "10";
    String orderId = "200";
    String instanceId = "gallery50";
    UserDetail bart = new UserDetail();
    bart.setFirstName("Bart");
    bart.setLastName("Simpson");
    when(getOrganisationControllerMock().getUserDetail(userId)).thenReturn(bart);
    Connection con = getConnection();
    try {
      OldOrderDAO dao = new OldOrderDAO(getOrganisationControllerMock());
      Order order = dao.getOrder(con, orderId, instanceId);
      assertThat(orderId, is(notNullValue()));
      assertThat(order.getInstanceId(), is(instanceId));
      assertThat(order.getUserName(), is("Bart Simpson"));
      assertThat(order.getOrderId(), is("200"));
      assertThat(order.getNbRows(), is(2));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of getDownloadDate method, of class OldOrderDAO.
   */
  @Test
  public void testGetDownloadDate() throws Exception {
    String userId = "10";
    String orderId = "200";
    String instanceId = "gallery50";
    UserDetail bart = new UserDetail();
    bart.setFirstName("Bart");
    bart.setLastName("Simpson");
    when(getOrganisationControllerMock().getUserDetail(userId)).thenReturn(bart);
    Connection con = getConnection();
    try {
      OldOrderDAO dao = new OldOrderDAO(getOrganisationControllerMock());
      Order order = dao.getOrder(con, orderId, instanceId);
      assertThat(orderId, is(notNullValue()));
      assertThat(order.getInstanceId(), is(instanceId));
      assertThat(order.getUserName(), is("Bart Simpson"));
      assertThat(order.getOrderId(), is("200"));
      assertThat(order.getNbRows(), is(2));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of getAllOrdersToDelete method, of class OldOrderDAO.
   */
  //@Test
  public void testGetAllOrdersToDelete() throws Exception {
    String userId = "10";
    UserDetail bart = new UserDetail();
    bart.setFirstName("Bart");
    bart.setLastName("Simpson");
    when(getOrganisationControllerMock().getUserDetail(userId)).thenReturn(bart);
    Connection con = getConnection();
    try {
      int nbDays = 3;
      OldOrderDAO dao = new OldOrderDAO(getOrganisationControllerMock());
      List<Order> orders = dao.getAllOrdersToDelete(con, nbDays);
      assertThat(orders, is(notNullValue()));
      assertThat(orders, hasSize(2));
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of deleteOrder method, of class OldOrderDAO.
   */
  @Test
  public void testDeleteOrder() throws Exception {
    String userId = "10";
    String orderId = "200";
    String instanceId = "gallery50";
    UserDetail bart = new UserDetail();
    bart.setFirstName("Bart");
    bart.setLastName("Simpson");
    when(getOrganisationControllerMock().getUserDetail(userId)).thenReturn(bart);
    Connection con = getConnection();
    try {
      OldOrderDAO dao = new OldOrderDAO(getOrganisationControllerMock());
      dao.deleteOrder(con, orderId);
      con.commit();
      Order order = dao.getOrder(con, orderId, instanceId);
      assertThat(order, is(notNullValue()));
      assertThat(order.getInstanceId(), is(nullValue()));
      assertThat(order.getUserName(), is(nullValue()));
      assertThat(order.getOrderId(), is("200"));
      assertThat(order.getNbRows(), is(0));
    } finally {
      DBUtil.close(con);
    }
  }
}
