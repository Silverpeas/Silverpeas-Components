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

package com.silverpeas.rssAgregator.servlets;

import com.silverpeas.rssAgregator.control.RssAgregatorSessionController;
import com.silverpeas.rssAgregator.model.SPChannel;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class RssAgregatorRequestRouter extends ComponentRequestRouter<RssAgregatorSessionController> {

  private static final long serialVersionUID = -4056285757621649567L;

  public String getSessionControlBeanName() {
    return "rssAgregator";
  }

  public RssAgregatorSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new RssAgregatorSessionController(mainSessionCtrl, componentContext);
  }

  public String getDestination(String function, RssAgregatorSessionController rssSC, HttpServletRequest request) {
    String destination = "";
    SilverTrace.info("rssAgregator", "rssAgregatorRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + rssSC.getUserId() + " Function=" + function);

    String role = getRole(rssSC.getUserRoles());

    try {
      if (function.startsWith("Main") || function.equals("portlet")) {
        List<SPChannel> channels = rssSC.getAvailableChannels();

        if (function.startsWith("Main")) {
          request.setAttribute("Role", role);
        } else {
          request.setAttribute("Role", "user");
        }

        if (channels.size() != 0) {
          request.setAttribute("Channels", channels);
          destination = "/rssAgregator/jsp/welcome.jsp";
        } else {
          request.setAttribute("Content", rssSC.getRSSIntroductionContent());
          destination = "/rssAgregator/jsp/whatIsRss.jsp";
        }
      } else if (function.equals("LoadChannels")) {
        rssSC.getChannelsContent();

        destination = getDestination("Main", rssSC, request);
      } else if (function.equals("ToCreateChannel")) {
        destination = "/rssAgregator/jsp/newChannel.jsp";
      } else if (function.equals("CreateChannel")) {
        SPChannel channel = buildSPChannelFromRequest(request);

        rssSC.addChannel(channel);

        destination = "/rssAgregator/jsp/reload.jsp";
      } else if (function.equals("ToUpdateChannel")) {
        String id = request.getParameter("Id");
        SPChannel channel = rssSC.getChannel(id);

        request.setAttribute("Channel", channel);

        destination = "/rssAgregator/jsp/updateChannel.jsp";
      } else if (function.equals("UpdateChannel")) {
        SPChannel channel = buildSPChannelFromRequest(request);

        rssSC.updateChannel(channel);

        destination = "/rssAgregator/jsp/reload.jsp";
      } else if (function.equals("DeleteChannel")) {
        String id = request.getParameter("Id");

        rssSC.deleteChannel(id);

        destination = getDestination("Main", rssSC, request);
      }
    } catch (Exception e) {
      SilverTrace.error(getSessionControlBeanName(), RssAgregatorRequestRouter.class.getName(),
          "getDestination error", e);
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace.info("rssAgregator", "rssAgregatorRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }

  /**
   * This method return the highest profiles, for a couple of profiles given
   * @param profiles User's profiles for this instance of kmelia
   * @return profile which gives the higher access
   */
  public String getRole(String[] profiles) {
    String role = "user";
    for (String profile : profiles) {
      // if admin, return it, we won't find a better profile
      if ("admin".equals(profile)) {
        return profile;
      }
    }
    return role;
  }

  /**
   * Build SPChannel from HttpServletRequest
   * @param request the HttpServletRequest
   * @return a new SPChannel
   */
  private SPChannel buildSPChannelFromRequest(HttpServletRequest request) {
    // Retrieve request parameter
    String id = request.getParameter("Id");
    String url = request.getParameter("Url");
    String refreshRate = request.getParameter("RefreshRate");
    String nbItems = request.getParameter("NbItems");
    String displayImage = request.getParameter("DisplayImage");

    SilverTrace.info("rssAgregator", "rssAgregatorRequestRouter.buildSPChannelFromRequest",
        "root.MSG_GEN_PARAM_VALUE", "Id = " + id + ", url = " + url + ", refreshRate = " +
            refreshRate + ", nbItems = " + nbItems + ", displayImage = " + displayImage);

    // Return object declaration
    SPChannel channel;
    if (StringUtil.isDefined(id)) {
      channel = new SPChannel(id, url);
    } else {
      channel = new SPChannel(url);
    }

    if (StringUtil.isDefined(nbItems)) {
      channel.setNbDisplayedItems(Integer.parseInt(nbItems));
    }
    channel.setRefreshRate(Integer.parseInt(refreshRate));
    if (displayImage != null) {
      channel.setDisplayImage(1);
    }
    return channel;
  }

}