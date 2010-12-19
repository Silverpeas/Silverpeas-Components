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

package com.silverpeas.kmelia;

import com.silverpeas.peasUtil.RssServlet;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.kmelia.KmeliaTransversal;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.exporters.RSS_2_0_Exporter;
import de.nava.informa.impl.basic.Channel;
import de.nava.informa.impl.basic.Item;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;


public class RssLastPublicationsServlet extends HttpServlet {
  public static final String SPACE_ID_PARAM = "spaceid";
  private static final OrganizationController orga = new OrganizationController();

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException {
    String spaceId = request.getParameter(SPACE_ID_PARAM);
    String userId = getUserId(request);
    String login = getLogin(request);
    String password = getPassword(request);
      try {
        // Vérification que le user a droit d'accès au composant
        AdminController adminController = new AdminController(null);
        UserFull user = adminController.getUserFull(userId);
        if (user != null && login.equals(user.getLogin())
            && password.equals(user.getPassword())
            && isComponentAvailable(adminController, instanceId, userId)) {

          String serverURL = getServerURL(adminController, user.getDomainId());
          ChannelIF channel = new Channel();

          // récupération de la liste des N éléments à remonter dans le flux
          int nbReturnedElements = getNbReturnedElements();
          Collection<T> listElements = getListElements(instanceId, nbReturnedElements);

          // création d'une liste de ItemIF en fonction de la liste des éléments

          for (T element : listElements) {
            String title = getElementTitle(element, userId);
            URL link = new URL(serverURL + getElementLink(element, userId));
            String description = getElementDescription(element, userId);
            Date dateElement = getElementDate(element);
            String creatorId = getElementCreatorId(element);
            ItemIF item = new Item();
            item.setTitle(title);
            item.setLink(link);
            item.setDescription(description);
            item.setDate(dateElement);

            if (StringUtil.isDefined(creatorId)) {
              UserDetail creator = adminController.getUserDetail(creatorId);
              if (creator != null) {
                item.setCreator(creator.getDisplayedName());
              }
            } else if (StringUtil.isDefined(getExternalCreatorId(element))) {
              item.setCreator(getExternalCreatorId(element));
            }
            channel.addItem(item);
          }

          // construction de l'objet Channel
          channel.setTitle(getChannelTitle(instanceId));
          URL componentUrl = new URL(serverURL + URLManager.getApplicationURL()
              + URLManager.getURL("useless", instanceId));
          channel.setLocation(componentUrl);

          // exportation du channel
          res.setContentType("application/rss+xml");
          res.setHeader("Content-Disposition", "inline; filename=feeds.rss");
          Writer writer = res.getWriter();
          RSS_2_0_Exporter rssExporter = new RSS_2_0_Exporter(writer, "UTF-8");
          rssExporter.write(channel);

          if (rssExporter == null) {
            objectNotFound(req, res);
          }
        } else {
          objectNotFound(req, res);
        }
      } catch (Exception e) {
        objectNotFound(req, res);
      }
  }

  @Override
  public Collection<PublicationDetail> getListElements(String spaceId, int nbReturned)
      throws RemoteException {
    int maxAge = 0;
    if(StringUtil.isInteger(pref.getValue("maxAge", "0"))) {
      maxAge = Integer.parseInt(pref.getValue("maxAge","0"));
    }
    KmeliaTransversal kmeliaTransversal = new KmeliaTransversal(getMainSessionController());
    List<PublicationDetail> publications;
    if(maxAge > 0) {
      maxAge = -1 * maxAge;
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.DAY_OF_MONTH, maxAge);
      publications = kmeliaTransversal.getUpdatedPublications(spaceId, calendar.getTime(), nbReturned);
    } else {
      publications = kmeliaTransversal.getPublications(spaceId, nbReturned);
    }
  }

  @Override
  public String getElementTitle(PublicationDetail element, String userId) {
    return element.getTitle();
  }

  @Override
  public String getElementLink(PublicationDetail element, String userId) {
    return element.getURL();
  }

  @Override
  public String getElementDescription(PublicationDetail element, String userId) {
    return element.getDescription();
  }

  @Override
  public Date getElementDate(PublicationDetail element) {
    return element.getUpdateDate();
  }

  @Override
  public String getElementCreatorId(PublicationDetail element) {
    return element.getAuthor();
  }


  protected String getUserId(HttpServletRequest request) {
    return request.getParameter("userId");
  }

  protected String getLogin(HttpServletRequest request) {
    return request.getParameter("login");
  }

  protected String getPassword(HttpServletRequest request) {
    return request.getParameter("password");
  }

  public String getChannelTitle(String spaceId, String lang) {
    SpaceInstLight space = orga.getSpaceInstLightById(spaceId);
    if (space != null) {
      return space.getName(lang);
    }
    return "";
  }

  public String getServerURL(AdminController admin, String domainId) {
    Domain defaultDomain = admin.getDomain(domainId);
    return defaultDomain.getSilverpeasServerURL();
  }

    public boolean isComponentAvailable(AdminController admin, String instanceId,
      String userId) {
    return admin.isComponentAvailable(instanceId, userId);
  }
}
