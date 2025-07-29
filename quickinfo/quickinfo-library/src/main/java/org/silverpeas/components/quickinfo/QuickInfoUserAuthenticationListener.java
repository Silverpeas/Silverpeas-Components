/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.quickinfo;

import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.model.QuickInfoService;
import org.silverpeas.components.quickinfo.model.QuickInfoServiceProvider;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.security.authentication.UserAuthenticationListener;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * This listener handles the blocking news about Quick Info application.<br>
 * Just after a successful user authentication, blocking news are displayed to the user if any.
 */
@Service
@Singleton
public class QuickInfoUserAuthenticationListener implements UserAuthenticationListener {

  @Override
  public String firstHomepageAccessAfterAuthentication(HttpServletRequest request, User user,
      String finalURL) {
    return handle(request, user, finalURL);
  }

  @Override
  public String homepageAccessFromLoginWhenUserSessionAlreadyOpened(
      final HttpServletRequest request, final User user, final String finalURL) {
    return handle(request, user, finalURL);
  }

  private String handle(HttpServletRequest request, User user,
      String finalURL) {
    String redirectURL = null;
    if (user != null && !user.isAnonymous()) {
      HttpSession session = request.getSession();
      session.removeAttribute("Silverpeas_BlockingNews");
      session.removeAttribute("Silverpeas_FinalURL");
      List<News> news = getService().getUnreadBlockingNews(user.getId());
      if (!news.isEmpty()) {
        session.setAttribute("Silverpeas_BlockingNews", news);
        session.setAttribute("Silverpeas_FinalURL", finalURL);
        redirectURL = "/quickinfo/jsp/blockingNews.jsp";
      }
    }
    return redirectURL;
  }

  private QuickInfoService getService() {
    return QuickInfoServiceProvider.getQuickInfoService();
  }
}
