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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.SyndFeedOutput;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.personalization.service.PersonalizationServiceProvider;
import org.silverpeas.core.util.MimeTypes;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.controller.SilverpeasWebUtil;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RssLastPublicationsServlet extends HttpServlet {

  private static final long serialVersionUID = 5196503014070113044L;
  public static final String SPACE_ID_PARAM = "spaceId";
  public static final String USER_ID_PARAM = "userId";
  public static final String PASSWORD_PARAM = "password";
  public static final String LOGIN_PARAM = "login";

  private static final SettingBundle settings = ResourceLocator.getSettingBundle(
      "org.silverpeas.kmelia.settings.kmeliaSettings");

  @Inject
  private SilverpeasWebUtil util;

  @Inject
  private AdminController adminController;

  @Inject
  private OrganizationController organizationController;

  @Override
  public void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String spaceId = request.getParameter(SPACE_ID_PARAM);
    String userId = request.getParameter(USER_ID_PARAM);
    String login = request.getParameter(LOGIN_PARAM);
    String password = request.getParameter(PASSWORD_PARAM);
    try {
      UserFull user = adminController.getUserFull(userId);
      if (isUserAuthorized(user, login, password, spaceId)) {

        String serverURL = getServerURL(user);
        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType("rss_2.0");
        feed.setTitle(getChannelTitle(spaceId));
        feed.setDescription(getChannelTitle(spaceId));

        MainSessionController mainSessionController = util.getMainSessionController(request);
        KmeliaTransversal kmeliaTransversal;
        String preferredLanguage;
        if (mainSessionController != null) {
          kmeliaTransversal = new KmeliaTransversal(mainSessionController);
          preferredLanguage = mainSessionController.getFavoriteLanguage();
        } else {
          kmeliaTransversal = new KmeliaTransversal(userId);
          preferredLanguage = getPersonalization(userId).getLanguage();
        }

        // récupération de la liste des N éléments à remonter dans le flux
        Collection<PublicationDetail> publications = getElements(kmeliaTransversal, spaceId);

        // création d'une liste de ItemIF en fonction de la liste des éléments
        List<SyndEntry> entries = new ArrayList<>(publications.size());
        for (PublicationDetail publication : publications) {
          entries.add(toSyndEntry(publication, serverURL, preferredLanguage));
        }
        feed.setEntries(entries);

        // exportation du feed
        response.setContentType(MimeTypes.RSS_MIME_TYPE);
        Writer writer = response.getWriter();
        SyndFeedOutput feedOutput = new SyndFeedOutput();
        feedOutput.output(feed, writer);
      } else {
        objectNotFound(request, response);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      objectNotFound(request, response);
    }
  }

  public Collection<PublicationDetail> getElements(KmeliaTransversal kmeliaTransversal,
      String spaceId) {
    final int defaultMaxAge = 0;
    final int defaultReturnedNb = 10;
    int maxAge = settings.getInteger("max.age.last.publication", defaultMaxAge);
    int returnedNb = settings.getInteger("max.nb.last.publication", defaultReturnedNb);
    return kmeliaTransversal.getUpdatedPublications(spaceId, maxAge, returnedNb);
  }

  public SyndEntry toSyndEntry(PublicationDetail publication, String serverURL, String lang) throws
      MalformedURLException {
    final int maxUrlLength = 256;
    SyndEntry entry = new SyndEntryImpl();
    entry.setTitle(publication.getTitle());
    StringBuilder url = new StringBuilder(maxUrlLength);
    url.append(serverURL);
    url.append(URLUtil.getSimpleURL(URLUtil.URL_PUBLI, publication.getPK().getId()));
    entry.setLink(url.toString());
    entry.setPublishedDate(publication.getCreationDate());
    entry.setUpdatedDate(publication.getLastUpdateDate());

    SyndContent description = new SyndContentImpl();
    description.setType("text/plan");
    description.setValue(publication.getDescription(lang));
    entry.setDescription(description);

    String creatorId = publication.getUpdaterId();
    if (StringUtil.isDefined(creatorId)) {
      UserDetail creator = adminController.getUserDetail(creatorId);
      if (creator != null) {
        entry.setAuthor(creator.getDisplayedName());
      }
    }
    return entry;
  }

  public String getChannelTitle(String spaceId) {
    SpaceInstLight space = organizationController.getSpaceInstLightById(spaceId);
    if (space != null) {
      return space.getName();
    }
    return "";
  }

  public String getServerURL(UserFull user) {
    Domain defaultDomain = adminController.getDomain(user.getDomainId());
    return defaultDomain.getSilverpeasServerURL();
  }

  public boolean isUserAuthorized(UserFull user, String login, String password, String spaceId) {
    return (user != null) && login.equals(user.getLogin()) && password.equals(user.getPassword())
        && isSpaceAvailable(user.getId(), spaceId);
  }

  public boolean isSpaceAvailable(String userId, String spaceId) {
    return adminController.isSpaceAvailable(userId, spaceId);
  }

  protected void objectNotFound(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    boolean isLoggedIn = util.getMainSessionController(req) != null;
    if (!isLoggedIn) {
      res.sendRedirect(URLUtil.getApplicationURL() + "/admin/jsp/documentNotFound.jsp");
      return;
    }
    res.sendRedirect("/weblib/notFound.html");
  }

  /**
   * Return the personalization service layer
   * @param userId the user identifier
   * @return the UserPreferences of user identified by userId
   */
  public UserPreferences getPersonalization(String userId) {
    return PersonalizationServiceProvider.getPersonalizationService().getUserSettings(userId);
  }
}
