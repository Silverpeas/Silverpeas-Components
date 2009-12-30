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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.blog.control;

import com.silverpeas.blog.control.ejb.BlogBm;
import com.silverpeas.blog.control.ejb.BlogBmHome;
import com.silverpeas.blog.model.BlogRuntimeException;
import com.stratelia.silverpeas.silverpeasinitialize.CallBack;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

public class BlogCallBack extends CallBack {

  private BlogBmHome blogHome = null;

  public BlogCallBack() {
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#doInvoke(int, int,
   * java.lang.String, java.lang.Object)
   */
  public void doInvoke(int action, int iParam, String componentId,
      Object extraParam) {
    SilverTrace.info("blog", "BlogCallback.doInvoke()",
        "root.MSG_GEN_ENTER_METHOD", "action = " + action + ", iParam = "
        + iParam + ", componentId = " + componentId + ", extraParam = "
        + extraParam.toString());

    if (iParam == -1) {
      SilverTrace.info("blog", "BlogCallback.doInvoke()",
          "root.MSG_GEN_PARAM_VALUE",
          "userId is null. Callback stopped ! action = " + action
          + ", componentId = " + componentId + ", extraParam = "
          + extraParam.toString());
      return;
    }

    if (componentId != null && (componentId.startsWith("blog"))) {
      try {
        // extraction userId
        String sUserId = Integer.toString(iParam);

        String pubId = (String) extraParam;

        if (isPublicationModified(pubId, action)) {
          getBlogBm().externalElementsOfPublicationHaveChanged(
              new PublicationPK(pubId, componentId), sUserId);
        }

      } catch (Exception e) {
        throw new BlogRuntimeException("BlogCallback.doInvoke()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
            e);
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#subscribe()
   */
  public void subscribe() {
    CallBackManager.subscribeAction(CallBackManager.ACTION_ON_WYSIWYG, this);
  }

  private boolean isPublicationModified(String pubId, int action) {
    if (!pubId.startsWith("Node")
        && action == CallBackManager.ACTION_ON_WYSIWYG)
      return true;
    else
      return false;
  }

  private BlogBm getBlogBm() {
    BlogBm blogBm = null;
    try {
      blogBm = getBlogHome().create();
    } catch (Exception e) {
      throw new BlogRuntimeException("BlogCallBack.setBlogBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return blogBm;
  }

  private BlogBmHome getBlogHome() {
    if (blogHome == null) {
      try {
        blogHome = (BlogBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.BLOGBM_EJBHOME, BlogBmHome.class);
      } catch (Exception e) {
        throw new BlogRuntimeException("BlogCallback.getBlogHome()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
            e);
      }
    }
    return blogHome;
  }
}