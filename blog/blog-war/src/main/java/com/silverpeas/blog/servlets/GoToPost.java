package com.silverpeas.blog.servlets;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.blog.control.ejb.BlogBm;
import com.silverpeas.blog.control.ejb.BlogBmHome;
import com.silverpeas.blog.model.PostDetail;
import com.silverpeas.peasUtil.GoTo;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

public class GoToPost extends GoTo {
  public String getDestination(String objectId, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    PublicationPK pubPK = new PublicationPK(objectId);
    PostDetail post = getBlogBm().getPost(pubPK);

    String gotoURL = URLManager.getURL(null, post.getPublication()
        .getInstanceId())
        + post.getPublication().getURL();

    return "goto=" + URLEncoder.encode(gotoURL, "UTF-8");
  }

  private BlogBm getBlogBm() {
    BlogBm currentBlogBm = null;
    try {
      BlogBmHome blogBmHome = (BlogBmHome) EJBUtilitaire.getEJBObjectRef(
          JNDINames.BLOGBM_EJBHOME, BlogBmHome.class);
      currentBlogBm = blogBmHome.create();
    } catch (Exception e) {
      displayError(null);
    }
    return currentBlogBm;
  }
}