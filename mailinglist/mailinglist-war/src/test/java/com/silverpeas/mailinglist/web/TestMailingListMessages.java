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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.mailinglist.web;

import java.sql.SQLException;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;

import com.meterware.httpunit.ClientProperties;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;
import com.silverpeas.mailinglist.AbstractSilverpeasDatasourceSpringContextTests;

public class TestMailingListMessages extends
    AbstractSilverpeasDatasourceSpringContextTests {

  private static String MESSAGE_BASE = "/silverpeas/Rmailinglist/mailinglist45/destination/list/message/";

  public void testSimpleUser() throws Exception {
    WebConversation connection = new WebConversation();
    WebResponse loginPage = connection.getResponse(buildUrl("silverpeas/"));
    assertNotNull(loginPage);
    WebForm loginForm = loginPage.getFormWithName("EDform");
    assertNotNull(loginForm);
    loginForm.setParameter("Login", "SilverAdmin");
    loginForm.setParameter("Password", "SilverAdmin");
    HttpUnitOptions.setScriptingEnabled(false);
    WebResponse welcomePage = loginForm.submit();
    assertNotNull(welcomePage);
    String[] frameNames = welcomePage.getFrameNames();
    assertEquals(5, frameNames.length);
    WebResponse navigationFrame = connection.getFrameContents("bottomFrame");
    assertNotNull(navigationFrame);
    navigationFrame = connection.getFrameContents("SpacesBar");
    assertNotNull(navigationFrame);
    WebLink mailingListLink = navigationFrame.getLinkWith("Liste de diffusion");
    assertNotNull(mailingListLink);
    HttpUnitOptions.setScriptingEnabled(false);
    WebResponse activityPage = connection.getResponse(buildUrl(mailingListLink
        .getURLString()));
    assertNotNull(activityPage);
    WebTable tableDescription = activityPage.getTableWithID("description");
    assertNotNull(tableDescription);
    String description = tableDescription.getCellAsText(1, 0);
    assertEquals("Liste de diffusion de test", description);
    WebTable tableAddress = activityPage.getTableWithID("subscribedAddress");
    assertNotNull(tableAddress);
    String subscribedAddress = tableAddress.getCellAsText(1, 0);
    assertEquals("thesimpsons@silverpeas.com", subscribedAddress);
    WebTable tableTabbedPane = activityPage.getTableWithID("tabbedPane");
    assertNotNull(tableTabbedPane);
    tableTabbedPane = tableTabbedPane.getTableCell(0, 0).getTables()[0];
    assertNotNull(tableTabbedPane);
    assertEquals("Activité", tableTabbedPane.getCellAsText(0, 2));
    assertEquals("Liste des Messages", tableTabbedPane.getCellAsText(0, 5));
    assertFalse(activityPage.getText().indexOf("Modération") > 0);
    assertFalse(activityPage.getText().indexOf("Abonnés Extérieurs") > 0);
    WebResponse listPage = tableTabbedPane.getTableCell(0, 5).getLinkWith(
        "Liste des Messages").click();
    assertNotNull(listPage);
    WebTable browseBar = listPage.getTableWithID("browseBar");
    assertNotNull(browseBar);
    assertEquals("MGI Coutier > Liste de diffusion > Liste des Messages",
        browseBar.getCellAsText(0, 0));
    assertEquals("Main", browseBar.getTableCell(0, 0).getLinkWith(
        "Liste de diffusion").getURLString());
    tableTabbedPane = listPage.getTableWithID("tabbedPane");
    assertNotNull(tableTabbedPane);
    tableTabbedPane = tableTabbedPane.getTableCell(0, 0).getTables()[0];
    assertNotNull(tableTabbedPane);
    assertEquals("Activité", tableTabbedPane.getCellAsText(0, 2));
    assertEquals("Liste des Messages", tableTabbedPane.getCellAsText(0, 5));
    assertFalse(listPage.getText().indexOf("Modération") > 0);
    WebTable tableMessages = listPage.getTableWithID("list");
    assertNotNull(tableMessages);
    assertEquals("Messages", tableMessages.getCellAsText(0, 0));
    assertEquals("Fichiers Joints", tableMessages.getCellAsText(0, 1));
    assertEquals("Expéditeur", tableMessages.getCellAsText(0, 2));
    assertEquals("Date", tableMessages.getCellAsText(0, 3));
    assertEquals("Simple database message 3", tableMessages.getCellAsText(1, 0));
    assertEquals(MESSAGE_BASE + 3, tableMessages.getTableCell(1, 0)
        .getLinkWith("Simple database message 3").getURLString());
    assertEquals("Simple database message 4", tableMessages.getCellAsText(3, 0));
    assertEquals(MESSAGE_BASE + 4, tableMessages.getTableCell(3, 0)
        .getLinkWith("Simple database message 4").getURLString());
    assertEquals("Simple database message 1", tableMessages.getCellAsText(5, 0));
    assertEquals(MESSAGE_BASE + 1, tableMessages.getTableCell(5, 0)
        .getLinkWith("Simple database message 1").getURLString());
    assertEquals("Simple database message 11", tableMessages
        .getCellAsText(7, 0));
    assertEquals(MESSAGE_BASE + 11, tableMessages.getTableCell(7, 0)
        .getLinkWith("Simple database message 11").getURLString());
    assertEquals("Simple database message 10", tableMessages
        .getCellAsText(9, 0));
    assertEquals(MESSAGE_BASE + 10, tableMessages.getTableCell(9, 0)
        .getLinkWith("Simple database message 10").getURLString());
    assertEquals("Simple database message 2", tableMessages
        .getCellAsText(11, 0));
    assertEquals(MESSAGE_BASE + 2, tableMessages.getTableCell(11, 0)
        .getLinkWith("Simple database message 2").getURLString());
    assertEquals("Simple database message 9", tableMessages
        .getCellAsText(13, 0));
    assertEquals(MESSAGE_BASE + 9, tableMessages.getTableCell(13, 0)
        .getLinkWith("Simple database message 9").getURLString());
    assertEquals("Simple database message 8", tableMessages
        .getCellAsText(15, 0));
    assertEquals(MESSAGE_BASE + 8, tableMessages.getTableCell(15, 0)
        .getLinkWith("Simple database message 8").getURLString());
    assertEquals("Simple database message 7", tableMessages
        .getCellAsText(17, 0));
    assertEquals(MESSAGE_BASE + 7, tableMessages.getTableCell(17, 0)
        .getLinkWith("Simple database message 7").getURLString());
    assertEquals("Simple database message 6", tableMessages
        .getCellAsText(19, 0));
    assertEquals(MESSAGE_BASE + 6, tableMessages.getTableCell(19, 0)
        .getLinkWith("Simple database message 6").getURLString());
    assertNull(listPage.getLinkWithID("Supprimer les messages"));
    WebTable tablePagination = listPage.getTableWithID("pagination");
    assertNotNull(tablePagination);
    assertEquals("1 \n2", tablePagination.getCellAsText(0, 0));
    WebLink nextPage = tablePagination.getTableCell(0, 0).getLinkWith("2");
    assertNotNull(nextPage);
    assertEquals(
        "/silverpeas/Rmailinglist/mailinglist45/list/mailinglist45?currentPage=1",
        nextPage.getURLString());
    listPage = nextPage.click();
    tableTabbedPane = listPage.getTableWithID("tabbedPane");
    assertNotNull(tableTabbedPane);
    tableTabbedPane = tableTabbedPane.getTableCell(0, 0).getTables()[0];
    assertNotNull(tableTabbedPane);
    assertEquals("Activité", tableTabbedPane.getCellAsText(0, 2));
    assertEquals("Liste des Messages", tableTabbedPane.getCellAsText(0, 5));
    assertFalse(listPage.getText().indexOf("Modération") > 0);
    assertFalse(activityPage.getText().indexOf("Abonnés Extérieurs") > 0);
    tableMessages = listPage.getTableWithID("list");
    assertNotNull(tableMessages);
    assertEquals("Messages", tableMessages.getCellAsText(0, 0));
    assertEquals("Fichiers Joints", tableMessages.getCellAsText(0, 1));
    assertEquals("Expéditeur", tableMessages.getCellAsText(0, 2));
    assertEquals("Date", tableMessages.getCellAsText(0, 3));
    assertEquals("Simple database message 5", tableMessages.getCellAsText(1, 0));
    assertEquals(MESSAGE_BASE + 5, tableMessages.getTableCell(1, 0)
        .getLinkWith("Simple database message 5").getURLString());
    tablePagination = listPage.getTableWithID("pagination");
    assertNotNull(tablePagination);
    assertEquals("1   2", tablePagination.getCellAsText(0, 0));
    WebLink previousPage = tablePagination.getTableCell(0, 0).getLinkWith("1");
    assertNotNull(previousPage);
    assertEquals(
        "/silverpeas/Rmailinglist/mailinglist45/list/mailinglist45?currentPage=0",
        previousPage.getURLString());
    assertNull(listPage.getLinkWithID("Supprimer les messages"));
  }

  public void testAdmin() throws Exception {
    WebConversation connection = new WebConversation();
    WebResponse loginPage = connection.getResponse(buildUrl("silverpeas/"));
    assertNotNull(loginPage);
    WebForm loginForm = loginPage.getFormWithName("EDform");
    assertNotNull(loginForm);
    loginForm.setParameter("Login", "bsimpson");
    loginForm.setParameter("Password", "bart");
    HttpUnitOptions.setScriptingEnabled(false);
    WebResponse welcomePage = loginForm.submit();
    assertNotNull(welcomePage);
    String[] frameNames = welcomePage.getFrameNames();
    assertEquals(5, frameNames.length);
    WebResponse navigationFrame = connection.getFrameContents("bottomFrame");
    assertNotNull(navigationFrame);
    navigationFrame = connection.getFrameContents("SpacesBar");
    assertNotNull(navigationFrame);
    WebLink mailingListLink = navigationFrame.getLinkWith("Liste de diffusion");
    assertNotNull(mailingListLink);
    HttpUnitOptions.setScriptingEnabled(false);
    WebResponse activityPage = connection.getResponse(buildUrl(mailingListLink
        .getURLString()));
    assertNotNull(activityPage);
    WebTable tableDescription = activityPage.getTableWithID("description");
    assertNotNull(tableDescription);
    String description = tableDescription.getCellAsText(1, 0);
    assertEquals("Liste de diffusion de test", description);
    WebTable tableAddress = activityPage.getTableWithID("subscribedAddress");
    assertNotNull(tableAddress);
    String subscribedAddress = tableAddress.getCellAsText(1, 0);
    assertEquals("thesimpsons@silverpeas.com", subscribedAddress);
    WebTable tableTabbedPane = activityPage.getTableWithID("tabbedPane");
    assertNotNull(tableTabbedPane);
    tableTabbedPane = tableTabbedPane.getTableCell(0, 0).getTables()[0];
    assertNotNull(tableTabbedPane);
    assertEquals("Activité", tableTabbedPane.getCellAsText(0, 2));
    assertEquals("Liste des Messages", tableTabbedPane.getCellAsText(0, 5));
    assertEquals("Modération", tableTabbedPane.getCellAsText(0, 8));
    assertEquals("Abonnés Extérieurs", tableTabbedPane.getCellAsText(0, 11));
    HttpUnitOptions.setScriptingEnabled(false);
    WebResponse listPage = tableTabbedPane.getTableCell(0, 5).getLinkWith(
        "Liste des Messages").click();
    HttpUnitOptions.setScriptingEnabled(false);
    assertNotNull(listPage);
    WebTable browseBar = listPage.getTableWithID("browseBar");
    assertNotNull(browseBar);
    assertEquals("MGI Coutier > Liste de diffusion > Liste des Messages",
        browseBar.getCellAsText(0, 0));
    assertEquals("Main", browseBar.getTableCell(0, 0).getLinkWith(
        "Liste de diffusion").getURLString());
    assertNotNull(listPage.getLinkWithID("Supprimer les messages"));
    tableTabbedPane = listPage.getTableWithID("tabbedPane");
    assertNotNull(tableTabbedPane);
    tableTabbedPane = tableTabbedPane.getTableCell(0, 0).getTables()[0];
    assertNotNull(tableTabbedPane);
    assertEquals("Activité", tableTabbedPane.getCellAsText(0, 2));
    assertEquals("Liste des Messages", tableTabbedPane.getCellAsText(0, 5));
    assertEquals("Modération", tableTabbedPane.getCellAsText(0, 8));
    assertEquals("Abonnés Extérieurs", tableTabbedPane.getCellAsText(0, 11));
    WebTable tableMessages = listPage.getTableWithID("list");
    assertNotNull(tableMessages);
    assertEquals("Messages", tableMessages.getCellAsText(0, 1));
    assertEquals("Fichiers Joints", tableMessages.getCellAsText(0, 2));
    assertEquals("Expéditeur", tableMessages.getCellAsText(0, 3));
    assertEquals("Date", tableMessages.getCellAsText(0, 4));
    assertEquals("Simple database message 3", tableMessages.getCellAsText(1, 1));
    assertEquals(MESSAGE_BASE + 3, tableMessages.getTableCell(1, 1)
        .getLinkWith("Simple database message 3").getURLString());
    assertEquals("Simple database message 4", tableMessages.getCellAsText(3, 1));
    assertEquals(MESSAGE_BASE + 4, tableMessages.getTableCell(3, 1)
        .getLinkWith("Simple database message 4").getURLString());
    assertEquals("Simple database message 1", tableMessages.getCellAsText(5, 1));
    assertEquals(MESSAGE_BASE + 1, tableMessages.getTableCell(5, 1)
        .getLinkWith("Simple database message 1").getURLString());
    assertEquals("Simple database message 11", tableMessages
        .getCellAsText(7, 1));
    assertEquals(MESSAGE_BASE + 11, tableMessages.getTableCell(7, 1)
        .getLinkWith("Simple database message 11").getURLString());
    assertEquals("Simple database message 10", tableMessages
        .getCellAsText(9, 1));
    assertEquals(MESSAGE_BASE + 10, tableMessages.getTableCell(9, 1)
        .getLinkWith("Simple database message 10").getURLString());
    assertEquals("Simple database message 2", tableMessages
        .getCellAsText(11, 1));
    assertEquals(MESSAGE_BASE + 2, tableMessages.getTableCell(11, 1)
        .getLinkWith("Simple database message 2").getURLString());
    assertEquals("Simple database message 9", tableMessages
        .getCellAsText(13, 1));
    assertEquals(MESSAGE_BASE + 9, tableMessages.getTableCell(13, 1)
        .getLinkWith("Simple database message 9").getURLString());
    assertEquals("Simple database message 8", tableMessages
        .getCellAsText(15, 1));
    assertEquals(MESSAGE_BASE + 8, tableMessages.getTableCell(15, 1)
        .getLinkWith("Simple database message 8").getURLString());
    assertEquals("Simple database message 7", tableMessages
        .getCellAsText(17, 1));
    assertEquals(MESSAGE_BASE + 7, tableMessages.getTableCell(17, 1)
        .getLinkWith("Simple database message 7").getURLString());
    assertEquals("Simple database message 6", tableMessages
        .getCellAsText(19, 1));
    assertEquals(MESSAGE_BASE + 6, tableMessages.getTableCell(19, 1)
        .getLinkWith("Simple database message 6").getURLString());
    WebTable tablePagination = listPage.getTableWithID("pagination");
    assertNotNull(tablePagination);
    assertEquals("1 \n2", tablePagination.getCellAsText(0, 0));
    WebLink nextPage = tablePagination.getTableCell(0, 0).getLinkWith("2");
    assertNotNull(nextPage);
    assertEquals(
        "/silverpeas/Rmailinglist/mailinglist45/list/mailinglist45?currentPage=1",
        nextPage.getURLString());
    listPage = nextPage.click();
    assertNotNull(listPage);
    assertNotNull(listPage.getLinkWithID("Supprimer les messages"));
    tableTabbedPane = listPage.getTableWithID("tabbedPane");
    assertNotNull(tableTabbedPane);
    tableTabbedPane = tableTabbedPane.getTableCell(0, 0).getTables()[0];
    assertNotNull(tableTabbedPane);
    assertEquals("Activité", tableTabbedPane.getCellAsText(0, 2));
    assertEquals("Liste des Messages", tableTabbedPane.getCellAsText(0, 5));
    assertEquals("Modération", tableTabbedPane.getCellAsText(0, 8));
    assertEquals("Abonnés Extérieurs", tableTabbedPane.getCellAsText(0, 11));
    tableMessages = listPage.getTableWithID("list");
    assertNotNull(tableMessages);
    assertEquals("Messages", tableMessages.getCellAsText(0, 1));
    assertEquals("Fichiers Joints", tableMessages.getCellAsText(0, 2));
    assertEquals("Expéditeur", tableMessages.getCellAsText(0, 3));
    assertEquals("Date", tableMessages.getCellAsText(0, 4));
    assertEquals("Simple database message 5", tableMessages.getCellAsText(1, 1));
    assertEquals(MESSAGE_BASE + 5, tableMessages.getTableCell(1, 1)
        .getLinkWith("Simple database message 5").getURLString());
    tablePagination = listPage.getTableWithID("pagination");
    assertNotNull(tablePagination);
    assertEquals("1   2", tablePagination.getCellAsText(0, 0));
    WebLink previousPage = tablePagination.getTableCell(0, 0).getLinkWith("1");
    assertNotNull(previousPage);
    assertEquals(
        "/silverpeas/Rmailinglist/mailinglist45/list/mailinglist45?currentPage=0",
        previousPage.getURLString());
    listPage = previousPage.click();
    assertNotNull(listPage);
    WebForm deleteMessagesForm = listPage.getFormWithID("removeMessage");
    assertNotNull(deleteMessagesForm);
    deleteMessagesForm.setParameter("message", new String[] { "1", "4" });
    listPage = deleteMessagesForm.submit();
    assertNotNull(listPage);
    tableMessages = listPage.getTableWithID("list");
    assertNotNull(tableMessages);
    assertEquals("Messages", tableMessages.getCellAsText(0, 1));
    assertEquals("Fichiers Joints", tableMessages.getCellAsText(0, 2));
    assertEquals("Expéditeur", tableMessages.getCellAsText(0, 3));
    assertEquals("Date", tableMessages.getCellAsText(0, 4));
    assertEquals("Simple database message 3", tableMessages.getCellAsText(1, 1));
    assertEquals(MESSAGE_BASE + 3, tableMessages.getTableCell(1, 1)
        .getLinkWith("Simple database message 3").getURLString());
    assertEquals("Simple database message 11", tableMessages
        .getCellAsText(3, 1));
    assertEquals(MESSAGE_BASE + 11, tableMessages.getTableCell(3, 1)
        .getLinkWith("Simple database message 11").getURLString());
    assertEquals("Simple database message 10", tableMessages
        .getCellAsText(5, 1));
    assertEquals(MESSAGE_BASE + 10, tableMessages.getTableCell(5, 1)
        .getLinkWith("Simple database message 10").getURLString());
    assertEquals("Simple database message 2", tableMessages.getCellAsText(7, 1));
    assertEquals(MESSAGE_BASE + 2, tableMessages.getTableCell(7, 1)
        .getLinkWith("Simple database message 2").getURLString());
    assertEquals("Simple database message 9", tableMessages.getCellAsText(9, 1));
    assertEquals(MESSAGE_BASE + 9, tableMessages.getTableCell(9, 1)
        .getLinkWith("Simple database message 9").getURLString());
    assertEquals("Simple database message 8", tableMessages
        .getCellAsText(11, 1));
    assertEquals(MESSAGE_BASE + 8, tableMessages.getTableCell(11, 1)
        .getLinkWith("Simple database message 8").getURLString());
    assertEquals("Simple database message 7", tableMessages
        .getCellAsText(13, 1));
    assertEquals(MESSAGE_BASE + 7, tableMessages.getTableCell(13, 1)
        .getLinkWith("Simple database message 7").getURLString());
    assertEquals("Simple database message 6", tableMessages
        .getCellAsText(15, 1));
    assertEquals(MESSAGE_BASE + 6, tableMessages.getTableCell(15, 1)
        .getLinkWith("Simple database message 6").getURLString());
    assertEquals("Simple database message 5", tableMessages
        .getCellAsText(17, 1));
    assertEquals(MESSAGE_BASE + 5, tableMessages.getTableCell(17, 1)
        .getLinkWith("Simple database message 5").getURLString());
    tablePagination = listPage.getTableWithID("pagination");
    assertNull(tablePagination);
  }

  protected String buildUrl(String path) {
    return "http://localhost:8000/" + path;
  }

  protected String[] getConfigLocations() {
    return new String[] { "spring-checker.xml", "spring-notification.xml",
        "spring-hibernate.xml", "spring-datasource.xml" };
  }

  protected IDataSet getDataSet() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(
        TestMailingListActivity.class
            .getResourceAsStream("test-mailinglist-messages-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;
  }

  protected void onSetUp() {
    super.onSetUp();
    HttpUnitOptions.setExceptionsThrownOnErrorStatus(true);
    HttpUnitOptions.setExceptionsThrownOnScriptError(false);
    HttpUnitOptions.setScriptingEnabled(true);
    ClientProperties.getDefaultProperties().setAcceptCookies(true);
    ClientProperties.getDefaultProperties().setAutoRedirect(true);
    IDatabaseConnection connection = null;
    try {
      connection = getConnection();
      DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
      DatabaseOperation.CLEAN_INSERT.execute(connection, getDataSet());
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      if (connection != null) {
        try {
          connection.getConnection().close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  protected void onTearDown() {
    IDatabaseConnection connection = null;
    try {
      connection = getConnection();
      DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      if (connection != null) {
        try {
          connection.getConnection().close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

}