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
package com.stratelia.silverpeas.chat.control;

import jChatBox.Chat.Chatroom;
import jChatBox.Chat.ChatroomManager;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Vector;

import com.stratelia.silverpeas.alertUser.AlertUser;
import com.stratelia.silverpeas.chat.ChatDataAccess;
import com.stratelia.silverpeas.chat.ChatException;
import com.stratelia.silverpeas.chat.ChatRoomDetail;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;

public class ChatSessionController extends AbstractComponentSessionController {
  // utilisation de userPanel/ userpanelPeas
  String[] idSelectedUser = null;
  String[] nameSelectedUser = null;
  String[] lastnameSelectedUser = null;

  private UserDetail currentUser = getUserDetail();
  private String componentName = getComponentId();
  private String componentName2 = getComponentLabel();

  /** utilise pour notifier les utilisateurs */

  private ChatDataAccess chatDAO = new ChatDataAccess(getComponentId());
  private String currentRoomId = null;

  public ChatSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext, String multilangBaseName,
      String iconBaseName) {
    super(mainSessionCtrl, componentContext, multilangBaseName, iconBaseName);
  }

  public void setCurrentChatRoomId(String roomId) {
    currentRoomId = roomId;
  }

  public String getCurrentSilverObjectId() {
    return String.valueOf(getSilverObjectId(currentRoomId));
  }

  // DAO for the Chatroom
  public Vector getListChatRoom() throws ChatException {
    return chatDAO.RetreiveChatroom();
  }

  public void DeleteChatroom(Integer ID) throws ChatException {
    // Delete chatroom from database
    chatDAO.DeleteChatroom(ID);

    // Delete index from search Engine
    deleteIndexChatroom(ID);
  }

  public void InsertChatroom(int ID, String name, String comment)
      throws ChatException {
    // Insert chatroom in database
    ChatRoomDetail roomDetail = new ChatRoomDetail(getComponentId(), String
        .valueOf(ID), name, comment, getUserId());
    chatDAO.InsertChatroom(roomDetail);

    // Insert index to search Engine
    createIndexChatroom(roomDetail);
  }

  public void UpdateChatroom(Integer ID, String name, String comment)
      throws ChatException {
    ChatRoomDetail roomDetail = new ChatRoomDetail(getComponentId(), String
        .valueOf(ID), name, comment, getUserId());

    // Update classification stuff
    createIndexChatroom(roomDetail); // update search engine stuff
  }

  // Indexer calls
  public void createIndexChatroom(ChatRoomDetail room) {
    FullIndexEntry indexEntry = null;
    indexEntry = new FullIndexEntry(getComponentId(), "Chat", room.getId());
    indexEntry.setTitle(room.getName());
    indexEntry.setPreView(room.getDescription());
    indexEntry.setCreationUser(room.getCreatorId());
    IndexEngineProxy.addIndexEntry(indexEntry);
  }

  private void deleteIndexChatroom(Integer ID) {
    IndexEntryPK indexEntry = new IndexEntryPK(getComponentId(), "Chat", ID
        .toString());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  public void index() throws ChatException, jChatBox.Chat.ChatException {
    ChatroomManager chatroomManager = ChatroomManager.getInstance();

    Vector rooms = getListChatRoom();
    Iterator itRooms = rooms.iterator();
    String roomId = null;
    Chatroom room = null;
    ChatRoomDetail detail = null;
    while (itRooms.hasNext()) {
      roomId = (String) itRooms.next();
      room = chatroomManager.getChatroom(roomId);
      if (room != null) {
        detail = getChatRoomDetail(room, roomId);
        createIndexChatroom(detail);
      }
    }
  }

  private ChatRoomDetail getChatRoomDetail(Chatroom room, String id) {
    return new ChatRoomDetail(getComponentId(), id, room.getParams().getName(),
        room.getParams().getSubject(), null);
  }

  // DAO for the Banned
  public boolean RetreiveBanned(String user, int chatRoomId)
      throws ChatException {
    return chatDAO.RetreiveBanned(user, chatRoomId);
  }

  public void DeleteBanned(String user, int chatRoomId) throws ChatException {
    chatDAO.DeleteBanned(user, chatRoomId);
  }

  public void DeleteBannedAll(int chatRoomId) throws ChatException {
    chatDAO.DeleteBannedAll(chatRoomId);
  }

  public void InsertBanned(String user, int chatRoomId) throws ChatException {
    chatDAO.InsertBanned(user, chatRoomId);
  }

  public Vector RetreiveListBanned(int chatRoomId) throws ChatException {
    return chatDAO.RetreiveListBanned(chatRoomId);
  }

  public String initUserPanel() throws RemoteException,
      jChatBox.Chat.ChatException {
    AlertUser sel = getAlertUser();
    // Initialisation de AlertUser
    sel.resetAll();
    sel.setHostSpaceName(getSpaceLabel()); // set nom de l'espace pour browsebar
    sel.setHostComponentId(getComponentId()); // set id du composant pour appel
    // selectionPeas (extra param permettant de filtrer les users ayant acces au composant)
    PairObject hostComponentName = new PairObject(getComponentLabel(), null); // set
    // nom du composant pour browsebar
    // (PairObject(nom_composant, lien_vers_composant))
    // NB : seul le 1er element est actuellement utilisé
    // (alertUserPeas est toujours présenté en popup => pas de lien sur nom du composant)
    sel.setHostComponentName(hostComponentName);
    sel.setNotificationMetaData(getAlertNotificationMetaData()); // set
    // NotificationMetaData contenant les informations à notifier
    // fin initialisation de AlertUser
    // l'url de nav vers alertUserPeas et demandée à AlertUser et retournée
    return AlertUser.getAlertUserURL();
  }

  public String[] getNameSelectedUsers() {
    return nameSelectedUser;
  }

  public String[] getLastnameSelectedUsers() {
    return lastnameSelectedUser;
  }

  public String[] getSelectedUsers() {
    return idSelectedUser;
  }

  public NotificationMetaData getAlertNotificationMetaData()
      throws RemoteException, jChatBox.Chat.ChatException {

    String url = "/Rchat/" + this.componentName + "/Main";
    String senderName = getUserDetail().getDisplayedName();
    Chatroom chatroom = ChatroomManager.getInstance().getChatroom(
        Integer.parseInt(currentRoomId));
    String roomName = chatroom.getParams().getName();

    ResourceLocator message = new ResourceLocator(
        "com.stratelia.silverpeas.chat.multilang.chatBundle", "fr");
    ResourceLocator message_en = new ResourceLocator(
        "com.stratelia.silverpeas.chat.multilang.chatBundle", "en");

    String subject = getNotificationSubject(message);
    String body = getNotificationBody(message, senderName, roomName);

    // english notifications
    String subject_en = getNotificationSubject(message_en);
    String body_en = getNotificationBody(message_en, senderName, roomName);

    NotificationMetaData notification_message = new NotificationMetaData(
        NotificationParameters.NORMAL, subject, body);
    notification_message.addLanguage("en", subject_en, body_en);

    // German notifications
    ResourceLocator message_de = new ResourceLocator(
        "com.stratelia.silverpeas.chat.multilang.chatBundle", "de");
    if (message_de != null) {
      String subject_de = getNotificationSubject(message_de);
      String body_de = getNotificationBody(message_de, senderName, roomName);
      notification_message.addLanguage("de", subject_de, body_de);
    }

    notification_message.setLink(url);
    notification_message.setSender(this.currentUser.getId());
    return notification_message;
  }

  private String getNotificationSubject(ResourceLocator message) {
    return message.getString("chat.SUBJECT");
  }

  private String getNotificationBody(ResourceLocator message,
      String senderName, String roomName) {
    StringBuffer messageText = new StringBuffer();
    messageText.append(senderName).append(" ");
    messageText.append(message.getString("chat.notifInfo")).append(" ").append(
        roomName).append(".\n\n");

    return messageText.toString();
  }

  public UserDetail[] getAvailableUsers() {
    OrganizationController orga = getOrganizationController();
    return orga.getAllUsers(this.componentName2);
  }

  public boolean isPdcUsed() {
    String parameterValue = getComponentParameterValue("usepdc");
    return "yes".equals(parameterValue.toLowerCase());
  }

  public int getSilverObjectId(String objectId) {
    int silverObjectId = -1;
    try {
      silverObjectId = chatDAO.getSilverObjectId(objectId);
    } catch (Exception e) {
      SilverTrace.error("chat", "ChatSessionController.getSilverObjectId()",
          "root.EX_CANT_GET_LANGUAGE_RESOURCE", "objectId=" + objectId, e);
    }
    return silverObjectId;
  }
}