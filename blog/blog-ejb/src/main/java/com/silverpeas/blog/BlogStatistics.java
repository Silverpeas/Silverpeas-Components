package com.silverpeas.blog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.silverpeas.blog.control.ejb.BlogBm;
import com.silverpeas.blog.control.ejb.BlogBmHome;
import com.silverpeas.blog.model.BlogRuntimeException;
import com.silverpeas.blog.model.PostDetail;
import com.stratelia.silverpeas.silverstatistics.control.ComponentStatisticsInterface;
import com.stratelia.silverpeas.silverstatistics.control.UserIdCountVolumeCouple;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class BlogStatistics implements ComponentStatisticsInterface {

  public Collection getVolume(String spaceId, String componentId)
      throws Exception {
    ArrayList myArrayList = new ArrayList();

    Collection posts = getBlogBm().getAllPosts(componentId, 10000);
    Iterator iter = posts.iterator();
    while (iter.hasNext()) {
      PostDetail post = (PostDetail) iter.next();
      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
      myCouple.setUserId(post.getPublication().getCreatorId());
      myCouple.setCountVolume(1);
      myArrayList.add(myCouple);
    }

    return myArrayList;
  }

  private BlogBm getBlogBm() {
    BlogBm blogBm = null;
    try {
      BlogBmHome blogBmHome = (BlogBmHome) EJBUtilitaire.getEJBObjectRef(
          JNDINames.BLOGBM_EJBHOME, BlogBmHome.class);
      blogBm = blogBmHome.create();
    } catch (Exception e) {
      throw new BlogRuntimeException("Blog.BlogStatistics.getBlogBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return blogBm;
  }

}
