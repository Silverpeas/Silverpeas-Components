/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.rssaggregator.servlets;

import org.silverpeas.components.rssaggregator.control.RssAgregatorSessionController;
import org.silverpeas.components.rssaggregator.model.RSSViewType;
import org.silverpeas.components.rssaggregator.model.RssAgregatorException;
import org.silverpeas.components.rssaggregator.model.SPChannel;
import org.silverpeas.components.rssaggregator.service.RSSServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.silverpeas.core.util.StringUtil.isDefined;

public class RssAgregatorRequestRouter
    extends ComponentRequestRouter<RssAgregatorSessionController> {

  private static final long serialVersionUID = -4056285757621649567L;

  @Override
  public String getSessionControlBeanName() {
    return "rssAgregator";
  }

  public RssAgregatorSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new RssAgregatorSessionController(mainSessionCtrl, componentContext);
  }

  public String getDestination(String function, RssAgregatorSessionController rssSC,
      HttpRequest request) {
    String destination = "";
    String action = request.getParameter("action");
    if (StringUtil.isDefined(action)) {
      rssSC.setViewMode(RSSViewType.valueOf(action.toUpperCase()));
    }
    try {
      if (function.startsWith("Main")) {
        request.setAttribute("Role", rssSC.getHighestRole());
        if (RSSViewType.SEPARATED.equals(rssSC.getViewMode())) {
          destination = prepareSeparatedView(rssSC, request);
        } else if (RSSViewType.AGGREGATED.equals(rssSC.getViewMode())) {
          destination = prepareAggregatedView(rssSC, request, false);
        }
      } else if (function.equals("portlet")) {
        request.setAttribute("Role", "user");
        destination = prepareAggregatedView(rssSC, request, true);
      } else if (function.equals("LoadChannels")) {
        rssSC.getChannelsContent();
        destination = getDestination("Main", rssSC, request);
      } else if (function.equals("ToAddChannel")) {
        destination = getChannelManagerDestination(rssSC, request);
      } else if (function.equals("CreateChannel")) {
        final SPChannel channel = buildSPChannelFromRequest(request);
        rssSC.addChannel(channel);
        destination = getDestination("Main", rssSC, request);
      } else if (function.equals("ToModifyChannel")) {
        destination = getChannelManagerDestination(rssSC, request);
      } else if (function.equals("UpdateChannel")) {
        final SPChannel channel = buildSPChannelFromRequest(request);
        rssSC.updateChannel(channel);
        destination = getDestination("LoadChannels", rssSC, request);
      } else if (function.equals("ToRemoveChannel")) {
        request.setAttribute("DeletionMode", true);
        destination = getChannelManagerDestination(rssSC, request);
      } else if (function.equals("DeleteChannel")) {
        String id = request.getParameter("Id");
        rssSC.deleteChannel(id);
        destination = getDestination("Main", rssSC, request);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }

  private String getChannelManagerDestination(final RssAgregatorSessionController rssSC,
      final HttpRequest request) throws RssAgregatorException {
    final String id = request.getParameter("Id");
    if (isDefined(id)) {
      final SPChannel channel = rssSC.getChannel(id);
      request.setAttribute("Channel", channel);
    }
    return "/rssAgregator/jsp/channelManager.jsp";
  }

  /**
   * Prepare data for aggregated view
   * @param rssSC the rss session controller
   * @param request the Http Servlet Request
   * @param isPortletView true if for portlet view, false else if
   * @return
   * @throws RssAgregatorException
   */
  private String prepareAggregatedView(RssAgregatorSessionController rssSC,
      HttpServletRequest request, boolean isPortletView) throws RssAgregatorException {
    String destination;
    List<SPChannel> channels = rssSC.getAvailableChannels();
    if (!channels.isEmpty()) {
      request.setAttribute("Channels", channels);
      request.setAttribute("aggregate", true);
      request.setAttribute("allChannels",
          RSSServiceProvider.getRSSService().getAllChannels(rssSC.getComponentId()));
      request.setAttribute("items",
          RSSServiceProvider.getRSSService().getApplicationItems(rssSC.getComponentId(), true));
      if (isPortletView) {
        destination = "/rssAgregator/jsp/rssPortletView.jsp";
      } else {
        destination = "/rssAgregator/jsp/displayRSS.jsp";
      }
    } else {
      request.setAttribute("Content", rssSC.getRSSIntroductionContent());
      destination = "/rssAgregator/jsp/whatIsRss.jsp";
    }
    return destination;
  }

  /**
   * Prepare data for
   * @param rssSC
   * @param request
   * @return
   * @throws RssAgregatorException
   */
  private String prepareSeparatedView(RssAgregatorSessionController rssSC,
      HttpServletRequest request) throws RssAgregatorException {
    String destination;
    List<SPChannel> channels = rssSC.getAvailableChannels();
    if (!channels.isEmpty()) {
      request.setAttribute("Channels", channels);
      destination = "/rssAgregator/jsp/displayRSS.jsp";
    } else {
      request.setAttribute("Content", rssSC.getRSSIntroductionContent());
      destination = "/rssAgregator/jsp/whatIsRss.jsp";
    }
    return destination;
  }

  /**
   * Build SPChannel from HttpServletRequest
   * @param request the HttpServletRequest
   * @return a new SPChannel
   */
  private SPChannel buildSPChannelFromRequest(final HttpRequest request) {
    // Retrieve request parameter
    String id = request.getParameter("Id");
    String url = request.getParameter("Url");
    boolean safeUrl = request.getParameter("SafeUrl") != null;
    String refreshRate = request.getParameter("RefreshRate");
    String nbItems = request.getParameter("NbItems");
    String displayImage = request.getParameter("DisplayImage");
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
    channel.setSafeUrl(safeUrl);
    return channel;
  }
}