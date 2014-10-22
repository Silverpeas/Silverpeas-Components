package org.silverpeas.components.quickinfo;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.authentication.UserAuthenticationListener;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.model.QuickInfoService;
import org.silverpeas.components.quickinfo.model.QuickInfoServiceFactory;

public class QuickInfoUserAuthenticationListener implements UserAuthenticationListener {

  @Override
  public String firstHomepageAccessAfterAuthentication(HttpServletRequest request, UserDetail user,
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
    return QuickInfoServiceFactory.getQuickInfoService();
  }

}
