package com.silverpeas.blog;

import com.silverpeas.blog.control.ejb.BlogBm;
import com.silverpeas.blog.control.ejb.BlogBmHome;
import com.silverpeas.blog.model.BlogRuntimeException;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class BlogIndexer implements ComponentIndexerInterface {

  public void index(MainSessionController mainSessionCtrl,
      ComponentContext context) throws Exception {
    getBlogBm().indexBlog(context.getCurrentComponentId());
  }

  private BlogBm getBlogBm() {
    BlogBm blogBm = null;
    try {
      BlogBmHome blogBmHome = (BlogBmHome) EJBUtilitaire.getEJBObjectRef(
          JNDINames.BLOGBM_EJBHOME, BlogBmHome.class);
      blogBm = blogBmHome.create();
    } catch (Exception e) {
      throw new BlogRuntimeException("BlogSessionController.getBlogBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return blogBm;
  }
}
