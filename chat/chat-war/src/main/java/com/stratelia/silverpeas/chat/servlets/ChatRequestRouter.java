package com.stratelia.silverpeas.chat.servlets;

import jChatBox.Chat.ChatroomManager;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.chat.control.ChatSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class ChatRequestRouter extends ComponentRequestRouter {

  private Vector listChatroom = new Vector();
  private Vector listBanned = new Vector();

  private String getFlag(String[] profiles) {
    String flag = "user";
    int i;

    for (i = 0; i < profiles.length; i++) {
      if (profiles[i].equals("admin"))
        return profiles[i];
      if (profiles[i].equals("publisher"))
        return profiles[i];
    }
    return flag;
  }

  public String getSessionControlBeanName() {
    return "chat";
  }

  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new ChatSessionController(mainSessionCtrl, componentContext,
        "com.stratelia.silverpeas.chat.multilang.chatBundle",
        "com.stratelia.silverpeas.chat.settings.chatIcons");
  }

  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {
    SilverTrace.debug("chat", "ChatRequestRouter.getDestination()",
        "root.MSG_GEN_ENTER_METHOD", "function = " + function);
    ChatSessionController chatSC = (ChatSessionController) componentSC;

    String flag = getFlag(chatSC.getUserRoles());
    String destination = "";
    String chat_fullName = "";

    try {

      // System.out.println("RR: URI="+request.getRequestURI());

      chat_fullName = chatSC.getUserDetail().getDisplayedName();
      request.setAttribute("chat_fullName", chat_fullName);

      // Set a value if current user is publisher (can notify peoples)
      if (flag.equals("publisher"))
        request.setAttribute("chat_isPublisher", "yes");
      else
        request.setAttribute("chat_isPublisher", "no");

      // Set a value if current user is admin
      if (flag.equals("admin"))
        request.setAttribute("chat_isAdmin", "yes");
      else
        request.setAttribute("chat_isAdmin", "no");

      // Set flag for PdC usage
      if (chatSC.isPdcUsed())
        request.setAttribute("isPdcUsed", "yes");
      else
        request.setAttribute("isPdcUsed", "no");

      // Redirect to the correct first page when user click on the domain's
      // barre
      if (function.startsWith("Main") || function.startsWith("login.jsp")
          || function.startsWith("searchResult")) {
        request.setAttribute("chatSC", chatSC);
        if (function.startsWith("searchResult")) {
          request.setAttribute("chat_id_search", request.getParameter("Id"));
        }
        listChatroom = chatSC.getListChatRoom();
        request.setAttribute("chat_listChatRoom", listChatroom);
        destination = "/chat/jsp/skin_mirc/login.jsp";
      }

      // 2 routes for the Admin part
      else if (function.startsWith("GoAdmin")
          || function.startsWith("index.jsp")) {
        destination = "/chat/jsp/admin/index.jsp";
      }

      // Check for the page menu.jsp
      else if (function.startsWith("menu.jsp")) {
        // for create a chatroom:
        ChatroomManager chatroomManager = ChatroomManager.getInstance();

        // check for chatroom removing:
        if ((request.getParameter("todo") != null)
            && (request.getParameter("todo").equals("closechatroom"))) {
          Integer current_id = Integer.valueOf(request.getParameter("id"));
          chatSC.DeleteChatroom(current_id); // delete database entry
        }

        // check for chatroom insert:
        if ((request.getParameter("todo") != null)
            && (request.getParameter("todo").equals("openchatroom"))) {
          int next_ID = chatroomManager.getChatroomUID() + 1; // next channel ID
                                                              // (reverse from
                                                              // original
                                                              // object...)
          chatSC.InsertChatroom(next_ID, request.getParameter("name"), request
              .getParameter("subject")); // insert database entry
        }

        // for obtain the chatroom list
        listChatroom = chatSC.getListChatRoom();
        request.setAttribute("chat_listChatRoom", listChatroom);

        destination = "/chat/jsp/admin/menu.jsp";
      } else if (function.startsWith("chatroom.jsp")) {

        Integer ID = new Integer(request.getParameter("id"));
        chatSC.setCurrentChatRoomId(request.getParameter("id"));

        if ((request.getParameter("todo") != null)
            && (request.getParameter("todo").equals("updatechatroom"))) {
          // update the setting of a chatroom
          chatSC.UpdateChatroom(ID, request.getParameter("chatroomName"),
              request.getParameter("subject"));
        } else if ((request.getParameter("todo") != null)
            && (request.getParameter("todo").equals("remove"))) {
          // removed from the banned list
          chatSC.DeleteBanned(request.getParameter("ip"), ID.intValue());
        } else if ((request.getParameter("todo") != null)
            && (request.getParameter("todo").equals("ban"))) {
          // banned a guys
          chatSC.InsertBanned(request.getParameter("username"), ID.intValue());
        } else if ((request.getParameter("todo") != null)
            && (request.getParameter("todo").equals("clear"))) {
          // banned a guys
          chatSC.DeleteBannedAll(ID.intValue());
        }

        listBanned = chatSC.RetreiveListBanned(ID.intValue());
        request.setAttribute("chat_listBanned", listBanned);

        destination = "/chat/jsp/admin/" + function;
      } else if (function.startsWith("ToUserPanel")) {

        SilverTrace.debug("chat", "ChatRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "ToUserPanel: function = " + function);
        SilverTrace.debug("chat", "ChatRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "ToUserPanel: function = " + function
                + " spaceId=" + chatSC.getSpaceId() + " componentId="
                + chatSC.getComponentId() + " room="
                + chatSC.getCurrentSilverObjectId());

        destination = chatSC.initUserPanel();

        SilverTrace.debug("chat", "ChatRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "ToUserPanel: function = " + function
                + "=> destination=" + destination);
      } else if (function.startsWith("close.jsp")) {
        destination = "/chat/jsp/" + function;
      }
      // PdC classification
      else if (function.startsWith("pdcPositions.jsp")) {
        request.setAttribute("silverObjectId", chatSC
            .getCurrentSilverObjectId());
        destination = "/chat/jsp/admin/pdcPositions.jsp";
      }
      // rest of JSP page for admin part
      else if (function.startsWith("open.jsp")) {
        destination = "/chat/jsp/admin/" + function;
      }
      // Special route for GIF files (function = images/***.gif or
      // styles/***.css )
      else if (function.endsWith(".gif") || function.endsWith(".css")) {
        destination = "/chat/jsp/admin/" + function;
      }

      // default route for classic chat process
      else {
        destination = "/chat/jsp/skin_mirc/" + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace.info("chat", "ChatRequestRooter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "destination" + destination);
    return destination;

  }
}