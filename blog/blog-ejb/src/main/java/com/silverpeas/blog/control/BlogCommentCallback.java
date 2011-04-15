/*
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
 * "http://www.silverpeas.org/legal/licensing"
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
import com.silverpeas.blog.model.PostDetail;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.service.CallBackOnCommentAction;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import java.rmi.RemoteException;
import javax.annotation.PostConstruct;
import javax.inject.Named;

/**
 * A callback invoked when a comment is added to a blog's post.
 * The callback sends a notification to all users that are subscribed to the commented blog post
 * when a new comment is added.
 */
@Named
public class BlogCommentCallback extends CallBackOnCommentAction {

  private BlogBm blogBm = null;

  @Override
  @PostConstruct
  public void subscribe() {
    subscribeForCommentAdding();
  }

  @Override
  public void commentAdded(int publicationId, String componentInstanceId, Comment addedComment) {
    if (componentInstanceId.startsWith("blog")) {
      try {
        PublicationPK pk = new PublicationPK(String.valueOf(publicationId), componentInstanceId);
        PostDetail post = getBlogBm().getPost(pk);
        PublicationDetail pub = post.getPublication();
        NodePK father = new NodePK("0", pub.getPK().getSpaceId(), pub.getPK().getInstanceId());
        getBlogBm().sendSubscriptionsNotification(father, post, addedComment, "commentCreate",
            Integer.toString(addedComment.getOwnerId()));
      } catch (RemoteException ex) {
        SilverTrace.error("blog", getClass().getSimpleName() + ".commentAdded()",
            "root.EX_NO_MESSAGE", ex);
      }
    }
  }

  @Override
  public void commentRemoved(int publicationId, String componentInstanceId, Comment removedComment) {
    SilverTrace.warn("blog", getClass().getSimpleName() + ".commentRemoved()",
        "blog.MSG_WARN_BAD_CALLBACK_INVOCATION");
  }

  private BlogBm getBlogBm() {
    if (blogBm == null) {
      try {
        BlogBmHome blogBmHome =
            EJBUtilitaire.getEJBObjectRef(JNDINames.BLOGBM_EJBHOME, BlogBmHome.class);
        blogBm = blogBmHome.create();
      } catch (Exception e) {
        throw new BlogRuntimeException(getClass().getSimpleName() + ".getBlogBm()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return blogBm;
  }
}
