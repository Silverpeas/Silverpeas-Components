/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
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
package com.silverpeas.mailinglist.web;

import com.meterware.httpunit.ClientProperties;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;
import com.silverpeas.mailinglist.AbstractMailingListTest;
import com.silverpeas.mailinglist.service.MailingListServicesProvider;
import com.silverpeas.mailinglist.service.model.beans.Message;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestMailingListModeration extends AbstractMailingListTest {

  @Before
  public void onSetUp() {
    HttpUnitOptions.setExceptionsThrownOnErrorStatus(true);
    HttpUnitOptions.setExceptionsThrownOnScriptError(false);
    HttpUnitOptions.setScriptingEnabled(true);
    ClientProperties.getDefaultProperties().setAcceptCookies(true);
    ClientProperties.getDefaultProperties().setAutoRedirect(true);
  }

  @Test
  public void testModerateMessage() throws Exception {
    MailingListServicesProvider
        mailingListServicesProvider = MailingListServicesProvider.getFactory();
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
    WebLink moderationLink = tableTabbedPane.getTableCell(0, 8).getLinkWith(
        "Modération");
    assertNotNull(moderationLink);
    WebResponse moderationPage = moderationLink.click();
    assertNotNull(moderationPage);
    WebTable browseBar = moderationPage.getTableWithID("browseBar");
    assertNotNull(browseBar);
    assertEquals("MGI Coutier > Liste de diffusion > Modération", browseBar
        .getCellAsText(0, 0));
    assertEquals("/silverpeas/Rmailinglist/mailinglist45/Main", browseBar
        .getTableCell(0, 0).getLinkWith("Liste de diffusion").getURLString());
    HttpUnitOptions.setScriptingEnabled(true);
    WebTable tableMessages = moderationPage.getTableWithID("list");
    assertNotNull(tableMessages);
    assertEquals("Messages", tableMessages.getCellAsText(0, 1));
    assertEquals("Fichiers Joints", tableMessages.getCellAsText(0, 2));
    assertEquals("Expéditeur", tableMessages.getCellAsText(0, 3));
    assertEquals("Simple database message 13", tableMessages
        .getCellAsText(1, 1));
    assertEquals(
        "/silverpeas/Rmailinglist/mailinglist45/destination/moderation/message/13",
        tableMessages.getTableCell(1, 1).getLinkWith(
        "Simple database message 13").getURLString());
    assertEquals("Simple database message 12", tableMessages
        .getCellAsText(3, 1));
    assertEquals(
        "/silverpeas/Rmailinglist/mailinglist45/destination/moderation/message/12",
        tableMessages.getTableCell(3, 1).getLinkWith(
        "Simple database message 12").getURLString());
    WebForm moderateForm = moderationPage.getFormWithID("moderate");
    moderateForm.setParameter("message", new String[]{"12", "13"});
    String formSubmit = moderateForm.getAction() + "/message/put"
        + "?message=12&message=13";
    HttpUnitOptions.setScriptingEnabled(false);
    moderationPage = connection.getResponse(buildUrl(formSubmit));
    assertFalse(moderationPage.getText().indexOf("Simple database message") > 0);
    Message message = mailingListServicesProvider.getMessageService().getMessage("12");
    assertNotNull(message);
    assertTrue(message.isModerated());
    message = mailingListServicesProvider.getMessageService().getMessage("13");
    assertNotNull(message);
    assertTrue(message.isModerated());
  }

  @Test
  public void testDeleteMessage() throws Exception {
    MailingListServicesProvider
        mailingListServicesProvider = MailingListServicesProvider.getFactory();
    WebConversation connection = new WebConversation();
    WebResponse loginPage = connection.getResponse(buildUrl("silverpeas/"));
    HttpUnitOptions.setScriptingEnabled(false);
    assertNotNull(loginPage);
    WebForm loginForm = loginPage.getFormWithName("EDform");
    assertNotNull(loginForm);
    loginForm.setParameter("Login", "bsimpson");
    loginForm.setParameter("Password", "bart");
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
    WebLink moderationLink = tableTabbedPane.getTableCell(0, 8).getLinkWith(
        "Modération");
    assertNotNull(moderationLink);
    WebResponse moderationPage = moderationLink.click();
    assertNotNull(moderationPage);
    WebTable browseBar = moderationPage.getTableWithID("browseBar");
    assertNotNull(browseBar);
    assertEquals("MGI Coutier > Liste de diffusion > Modération", browseBar
        .getCellAsText(0, 0));
    assertEquals("/silverpeas/Rmailinglist/mailinglist45/Main", browseBar
        .getTableCell(0, 0).getLinkWith("Liste de diffusion").getURLString());
    HttpUnitOptions.setScriptingEnabled(true);
    WebTable tableMessages = moderationPage.getTableWithID("list");
    assertNotNull(tableMessages);
    assertEquals("Messages", tableMessages.getCellAsText(0, 1));
    assertEquals("Fichiers Joints", tableMessages.getCellAsText(0, 2));
    assertEquals("Expéditeur", tableMessages.getCellAsText(0, 3));
    assertEquals("Simple database message 13", tableMessages
        .getCellAsText(1, 1));
    assertEquals(
        "/silverpeas/Rmailinglist/mailinglist45/destination/moderation/message/13",
        tableMessages.getTableCell(1, 1).getLinkWith(
        "Simple database message 13").getURLString());
    assertEquals("Simple database message 12", tableMessages
        .getCellAsText(3, 1));
    assertEquals(
        "/silverpeas/Rmailinglist/mailinglist45/destination/moderation/message/12",
        tableMessages.getTableCell(3, 1).getLinkWith(
        "Simple database message 12").getURLString());
    WebForm moderateForm = moderationPage.getFormWithID("moderate");
    moderateForm.setParameter("message", new String[]{"12", "13"});
    String formSubmit = moderateForm.getAction()
        + "/destination/moderation/message/delete" + "?message=12&message=13";
    HttpUnitOptions.setScriptingEnabled(false);
    moderationPage = connection.getResponse(buildUrl(formSubmit));
    assertFalse(moderationPage.getText().indexOf("Simple database message") > 0);
    Message message = mailingListServicesProvider.getMessageService().getMessage("12");
    assertNull(message);
    message = mailingListServicesProvider.getMessageService().getMessage("13");
    assertNull(message);
  }

  protected String buildUrl(String path) {
    return "http://localhost:8000/" + path;
  }

  @Override
  protected IDataSet getDataSet() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(
        TestMailingListActivity.class
        .getResourceAsStream("test-mailinglist-messages-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;
  }

  @Override
  protected String[] getContextConfigurations() {
    return new String[]{"/spring-checker.xml", "/spring-notification.xml",
      "/spring-mailinglist-services-factory.xml", "/spring-mailinglist-dao.xml",
      "/spring-mailinglist-embbed-datasource.xml"};
  }
}
