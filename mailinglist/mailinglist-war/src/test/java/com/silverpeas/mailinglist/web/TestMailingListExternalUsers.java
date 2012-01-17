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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.*;

@ContextConfiguration(locations = {"/spring-checker.xml", "/spring-notification.xml",
        "/spring-hibernate.xml", "/spring-datasource.xml"})
public class TestMailingListExternalUsers extends
    AbstractSilverpeasDatasourceSpringContextTests {

  @Test
  public void testDeleteExternalUsers() throws Exception {
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
    WebLink usersLink = tableTabbedPane.getTableCell(0, 11).getLinkWith(
        "Abonnés Extérieurs");
    assertNotNull(usersLink);
    WebResponse usersPage = usersLink.click();
    assertNotNull(usersPage);
    WebTable browseBar = usersPage.getTableWithID("browseBar");
    assertNotNull(browseBar);
    assertEquals("MGI Coutier > Liste de diffusion > Abonnés Extérieurs",
        browseBar.getCellAsText(0, 0));
    assertEquals("/silverpeas/Rmailinglist/mailinglist45/Main", browseBar
        .getTableCell(0, 0).getLinkWith("Liste de diffusion").getURLString());
    HttpUnitOptions.setScriptingEnabled(true);
    WebTable tableUsers = usersPage.getTableWithID("list");
    assertNotNull(tableUsers);
    assertEquals("Adresses mail", tableUsers.getCellAsText(0, 1));
    assertEquals("barney.gumble@silverpeas.com", tableUsers.getCellAsText(1, 1));
    assertEquals("carl.carlson@silverpeas.com", tableUsers.getCellAsText(2, 1));
    assertEquals("edna.krabappel@silverpeas.com", tableUsers
        .getCellAsText(3, 1));
    assertEquals("julius.hibbert@silverpeas.com", tableUsers
        .getCellAsText(4, 1));
    assertEquals("krusty.theklown@silverpeas.com", tableUsers.getCellAsText(5,
        1));
    assertEquals("maude.flanders@silverpeas.com", tableUsers
        .getCellAsText(6, 1));
    assertEquals("ned.flanders@silverpeas.com", tableUsers.getCellAsText(7, 1));
    assertEquals("nelson.muntz@silverpeas.com", tableUsers.getCellAsText(8, 1));
    assertEquals("patty.bouvier@silverpeas.com", tableUsers.getCellAsText(9, 1));
    WebForm deleteForm = usersPage.getFormWithID("removeUsers");
    deleteForm.setParameter("users", new String[] {
        "barney.gumble@silverpeas.com", "maude.flanders@silverpeas.com" });
    HttpUnitOptions.setScriptingEnabled(false);
    usersPage = deleteForm.submit();
    assertNotNull(usersPage);
    tableUsers = usersPage.getTableWithID("list");
    assertNotNull(tableUsers);
    assertEquals("Adresses mail", tableUsers.getCellAsText(0, 1));
    assertEquals("carl.carlson@silverpeas.com", tableUsers.getCellAsText(1, 1));
    assertEquals("edna.krabappel@silverpeas.com", tableUsers
        .getCellAsText(2, 1));
    assertEquals("julius.hibbert@silverpeas.com", tableUsers
        .getCellAsText(3, 1));
    assertEquals("krusty.theklown@silverpeas.com", tableUsers.getCellAsText(4,
        1));
    assertEquals("ned.flanders@silverpeas.com", tableUsers.getCellAsText(5, 1));
    assertEquals("nelson.muntz@silverpeas.com", tableUsers.getCellAsText(6, 1));
    assertEquals("patty.bouvier@silverpeas.com", tableUsers.getCellAsText(7, 1));
    assertEquals("rod.flanders@silverpeas.com", tableUsers.getCellAsText(8, 1));
    assertEquals("selma.bouvier@silverpeas.com", tableUsers.getCellAsText(9, 1));
  }
  @Test
  public void testAddExternalUsers() throws Exception {
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
    WebLink usersLink = tableTabbedPane.getTableCell(0, 11).getLinkWith(
        "Abonnés Extérieurs");
    assertNotNull(usersLink);
    WebResponse usersPage = usersLink.click();
    assertNotNull(usersPage);
    WebTable browseBar = usersPage.getTableWithID("browseBar");
    assertNotNull(browseBar);
    assertEquals("MGI Coutier > Liste de diffusion > Abonnés Extérieurs",
        browseBar.getCellAsText(0, 0));
    assertEquals("/silverpeas/Rmailinglist/mailinglist45/Main", browseBar
        .getTableCell(0, 0).getLinkWith("Liste de diffusion").getURLString());
    HttpUnitOptions.setScriptingEnabled(true);
    WebTable tableUsers = usersPage.getTableWithID("list");
    assertNotNull(tableUsers);
    assertEquals("Adresses mail", tableUsers.getCellAsText(0, 1));
    assertEquals("barney.gumble@silverpeas.com", tableUsers.getCellAsText(1, 1));
    assertEquals("carl.carlson@silverpeas.com", tableUsers.getCellAsText(2, 1));
    assertEquals("edna.krabappel@silverpeas.com", tableUsers
        .getCellAsText(3, 1));
    assertEquals("julius.hibbert@silverpeas.com", tableUsers
        .getCellAsText(4, 1));
    assertEquals("krusty.theklown@silverpeas.com", tableUsers.getCellAsText(5,
        1));
    assertEquals("maude.flanders@silverpeas.com", tableUsers
        .getCellAsText(6, 1));
    assertEquals("ned.flanders@silverpeas.com", tableUsers.getCellAsText(7, 1));
    assertEquals("nelson.muntz@silverpeas.com", tableUsers.getCellAsText(8, 1));
    assertEquals("patty.bouvier@silverpeas.com", tableUsers.getCellAsText(9, 1));
    WebForm addForm = usersPage.getFormWithID("add");
    assertNotNull(addForm);
    addForm
        .setParameter(
            "users",
            "barney.gumble@silverpeas.com;clancy.wiggum@"
                + "silverpeas.com;abraham.simspson@silverpeas.com;otto.man@silverpeas.com");
    HttpUnitOptions.setScriptingEnabled(false);
    usersPage = addForm.submit();
    assertNotNull(usersPage);
    tableUsers = usersPage.getTableWithID("list");
    assertNotNull(tableUsers);
    assertEquals("Adresses mail", tableUsers.getCellAsText(0, 1));
    assertEquals("abraham.simspson@silverpeas.com", tableUsers.getCellAsText(1,
        1));
    assertEquals("barney.gumble@silverpeas.com", tableUsers.getCellAsText(2, 1));
    assertEquals("carl.carlson@silverpeas.com", tableUsers.getCellAsText(3, 1));
    assertEquals("clancy.wiggum@silverpeas.com", tableUsers.getCellAsText(4, 1));
    assertEquals("edna.krabappel@silverpeas.com", tableUsers
        .getCellAsText(5, 1));
    assertEquals("julius.hibbert@silverpeas.com", tableUsers
        .getCellAsText(6, 1));
    assertEquals("krusty.theklown@silverpeas.com", tableUsers.getCellAsText(7,
        1));
    assertEquals("maude.flanders@silverpeas.com", tableUsers
        .getCellAsText(8, 1));
    assertEquals("ned.flanders@silverpeas.com", tableUsers.getCellAsText(9, 1));
  }

  protected String buildUrl(String path) {
    return "http://localhost:8000/" + path;
  }

  @Override
  protected IDataSet getDataSet() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(
        TestMailingListActivity.class
            .getResourceAsStream("test-mailinglist-messages-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;
  }

  @Before
  @Override
  public void onSetUp() {
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

  @After
  @Override
  public void onTearDown() {
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