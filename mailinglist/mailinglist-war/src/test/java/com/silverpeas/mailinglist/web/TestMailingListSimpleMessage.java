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

public class TestMailingListSimpleMessage extends
    AbstractSilverpeasDatasourceSpringContextTests {

  public void testSimpleMessage() throws Exception {
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
    tableTabbedPane = listPage.getTableWithID("tabbedPane");
    assertNotNull(tableTabbedPane);
    tableTabbedPane = tableTabbedPane.getTableCell(0, 0).getTables()[0];
    assertNotNull(tableTabbedPane);
    assertEquals("Activité", tableTabbedPane.getCellAsText(0, 2));
    assertEquals("Liste des Messages", tableTabbedPane.getCellAsText(0, 5));
    assertFalse(listPage.getText().indexOf("Modération") > 0);
    assertFalse(activityPage.getText().indexOf("Abonnés Extérieurs") > 0);
    WebTable tableMessages = listPage.getTableWithID("list");
    assertNotNull(tableMessages);
    assertEquals("Messages", tableMessages.getCellAsText(0, 0));
    assertEquals("Fichiers Joints", tableMessages.getCellAsText(0, 1));
    assertEquals("Expéditeur", tableMessages.getCellAsText(0, 2));
    assertEquals("Date", tableMessages.getCellAsText(0, 3));
    assertEquals("Simple database message 3", tableMessages.getCellAsText(1, 0));
    assertEquals(
        "/silverpeas/Rmailinglist/mailinglist45/destination/list/message/3",
        tableMessages.getTableCell(1, 0).getLinkWith(
            "Simple database message 3").getURLString());
    WebResponse messageDetailPage = tableMessages.getTableCell(1, 0)
        .getLinkWith("Simple database message 3").click();
    assertNotNull(messageDetailPage);
    WebTable browseBar = messageDetailPage.getTableWithID("browseBar");
    assertNotNull(browseBar);
    assertEquals(
        "MGI Coutier > Liste de diffusion > Liste des Messages > Simple database message 3",
        browseBar.getCellAsText(0, 0));
    assertEquals("/silverpeas/Rmailinglist/mailinglist45/Main", browseBar
        .getTableCell(0, 0).getLinkWith("Liste de diffusion").getURLString());
    assertEquals("/silverpeas/Rmailinglist/mailinglist45/list/mailinglist45",
        browseBar.getTableCell(0, 0).getLinkWith("Liste des Messages")
            .getURLString());
    WebTable messageTable = messageDetailPage.getTableWithID("message");
    assertNotNull(messageTable);
    assertEquals("Simple database message 3", messageTable.getCellAsText(0, 0));
    assertEquals(
        "Bonjour famille Simpson, j'espère que vous allez bien. "
            + "Ici tout se passe bien et Krusty est très sympathique. Surtout depuis "
            + "que Tahiti Bob est retourné en prison. Je dois remplacer l'homme "
            + "canon dans la prochaine émission.Bart", messageTable
            .getCellAsText(1, 0));
    assertEquals("bart.simpson@silverpeas.com - 02/03/2008 10:34:15",
        messageTable.getCellAsText(2, 0));
    WebTable attachmentsTable = messageDetailPage.getTableWithID("attachments");
    assertNull(attachmentsTable);
  }

  public void testMessageWithAttachment() throws Exception {
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
    tableTabbedPane = listPage.getTableWithID("tabbedPane");
    assertNotNull(tableTabbedPane);
    tableTabbedPane = tableTabbedPane.getTableCell(0, 0).getTables()[0];
    assertNotNull(tableTabbedPane);
    assertEquals("Activité", tableTabbedPane.getCellAsText(0, 2));
    assertEquals("Liste des Messages", tableTabbedPane.getCellAsText(0, 5));
    assertFalse(listPage.getText().indexOf("Modération") > 0);
    assertFalse(activityPage.getText().indexOf("Abonnés Extérieurs") > 0);
    WebTable tableMessages = listPage.getTableWithID("list");
    assertNotNull(tableMessages);
    assertEquals("Messages", tableMessages.getCellAsText(0, 0));
    assertEquals("Fichiers Joints", tableMessages.getCellAsText(0, 1));
    assertEquals("Expéditeur", tableMessages.getCellAsText(0, 2));
    assertEquals("Date", tableMessages.getCellAsText(0, 3));
    assertEquals("Simple database message 1", tableMessages.getCellAsText(5, 0));
    assertEquals(
        "/silverpeas/Rmailinglist/mailinglist45/destination/list/message/1",
        tableMessages.getTableCell(5, 0).getLinkWith(
            "Simple database message 1").getURLString());
    WebResponse messageDetailPage = tableMessages.getTableCell(5, 0)
        .getLinkWith("Simple database message 1").click();
    assertNotNull(messageDetailPage);
    WebTable browseBar = messageDetailPage.getTableWithID("browseBar");
    assertNotNull(browseBar);
    assertEquals(
        "MGI Coutier > Liste de diffusion > Liste des Messages > Simple database message 1",
        browseBar.getCellAsText(0, 0));
    assertEquals("/silverpeas/Rmailinglist/mailinglist45/Main", browseBar
        .getTableCell(0, 0).getLinkWith("Liste de diffusion").getURLString());
    assertEquals("/silverpeas/Rmailinglist/mailinglist45/list/mailinglist45",
        browseBar.getTableCell(0, 0).getLinkWith("Liste des Messages")
            .getURLString());
    WebTable messageTable = messageDetailPage.getTableWithID("message");
    assertNotNull(messageTable);
    assertEquals("Simple database message 1", messageTable.getCellAsText(0, 0));
    assertEquals(
        "Bonjour famille Simpson, j'espère que vous allez bien. "
            + "Ici tout se passe bien et Krusty est très sympathique. Surtout depuis "
            + "que Tahiti Bob est retourné en prison. Je dois remplacer l'homme "
            + "canon dans la prochaine émission.Bart", messageTable
            .getCellAsText(1, 0));
    assertEquals("bart.simpson@silverpeas.com - 01/03/2008 10:34:15",
        messageTable.getCellAsText(2, 0));
    WebTable attachmentsTable = messageDetailPage.getTableWithID("attachments");
    assertNotNull(attachmentsTable);
    assertNotNull(attachmentsTable.getTableCell(0, 0).getImages());
    assertEquals("/silverpeas/util/icons/attachedFiles.gif", attachmentsTable
        .getTableCell(0, 0).getImages()[0].getSource());
    WebLink attachment = attachmentsTable.getTableCell(1, 0).getLinkWithID(
        "lemonde.html");
    assertNotNull(attachment);
    assertEquals("/silverpeas/mailingListAttachment/1/message/1", attachment
        .getURLString());
    assertEquals("lemonde.html", attachment.getText());
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