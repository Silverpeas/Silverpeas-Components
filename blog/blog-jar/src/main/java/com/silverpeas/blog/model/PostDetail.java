/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.blog.model;

import com.silverpeas.SilverpeasContent;
import com.silverpeas.accesscontrol.AccessController;
import com.silverpeas.accesscontrol.AccessControllerProvider;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import org.silverpeas.accesscontrol.ComponentAccessControl;
import org.silverpeas.wysiwyg.control.WysiwygController;

import java.util.Date;

public final class PostDetail implements SilverpeasContent {

  private static final long serialVersionUID = -1703768097976820443L;
  private String content;
  private PublicationDetail publication;
  private Category category;
  private String categoryId;
  private int nbComments;
  private String creatorName;
  private Date dateEvent;
  
  private static final String TYPE = PublicationDetail.TYPE;

  public PostDetail(PublicationDetail publication, String categoryId) {
    setPublication(publication);
    setCategoryId(categoryId);
  }

  public PostDetail(PublicationDetail publication, String categoryId,
      Date dateEvent) {
    setPublication(publication);
    setCategoryId(categoryId);
    setDateEvent(dateEvent);
  }

  public PostDetail(PublicationDetail publication, Category category,
      int nbComments) {
    setPublication(publication);
    setCategory(category);
    setNbComments(nbComments);
  }

  public PostDetail(PublicationDetail publication, Category category,
      int nbComments, Date dateEvent) {
    setPublication(publication);
    setCategory(category);
    setNbComments(nbComments);
    setDateEvent(dateEvent);
  }

  public String getContent() {
    if (this.content == null) {
      this.content = WysiwygController.load(getPublication().getPK().getComponentName(), getPublication().getPK().getId(), getPublication().getLanguage());
    }
    return this.content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public int getNbComments() {
    return nbComments;
  }

  public void setNbComments(int nbComments) {
    this.nbComments = nbComments;
  }

  public PublicationDetail getPublication() {
    return publication;
  }

  public void setPublication(PublicationDetail publication) {
    this.publication = publication;
  }

  public String getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(String categoryId) {
    this.categoryId = categoryId;
  }

  public String getCreatorName() {
    return creatorName;
  }

  public void setCreatorName(String creatorName) {
    this.creatorName = creatorName;
  }

  public Date getDateEvent() {
    return dateEvent;
  }

  public void setDateEvent(Date dateEvent) {
    this.dateEvent = dateEvent;
  }

  public String getPermalink() {
    if (URLManager.displayUniversalLinks())
      return URLManager.getApplicationURL() + "/Post/"
          + publication.getPK().getId();

    return null;
  }

  @Override
  public String getId() {
    return publication.getId();
  }

  @Override
  public String getComponentInstanceId() {
    return publication.getPK().getInstanceId();
  }

  @Override
  public UserDetail getCreator() {
    return publication.getCreator();
  }

  @Override
  public Date getCreationDate() {
    return publication.getCreationDate();
  }

  @Override
  public String getTitle() {
    return publication.getTitle();
  }
  @Override
  public String getDescription() {
    return publication.getDescription();
  }

  @Override
  public String getContributionType() {
    return TYPE;
  }

  /**
   * Is the specified user can access this post?
   * <p/>
   * A user can access a post if it has enough rights to access the blog instance in
   * which is managed this post.
   * @param user a user in Silverpeas.
   * @return true if the user can access this post, false otherwise.
   */
  @Override
  public boolean canBeAccessedBy(final UserDetail user) {
    AccessController<String> accessController =
        AccessControllerProvider.getAccessController(ComponentAccessControl.class);
    return accessController.isUserAuthorized(user.getId(), getComponentInstanceId());
  }

  /**
   * The type of this resource
   * @return the same value returned by getContributionType()
   */
  public static String getResourceType() {
    return TYPE;
  }

  @Override
  public String getSilverpeasContentId() {
    return publication.getSilverpeasContentId();
  }
}
