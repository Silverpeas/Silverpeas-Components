package org.silverpeas.components.quickinfo.model;

import java.util.Date;

import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.date.Period;

import com.silverpeas.SilverpeasContent;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

public class News implements SilverpeasContent {
  
  private static final long serialVersionUID = 1L;
  private PublicationDetail publication;
  private String content;
  
  public News(String name, String description, Period visibilityPeriod, String positions, String userId, String componentId) {
    PublicationDetail publi = new PublicationDetail(name, description, visibilityPeriod, userId, componentId);
    this.publication = publi;
  }
  
  public News(PublicationDetail publication) {
    this.publication = publication;
  }
  
  public PublicationDetail getPublication() {
    return publication;
  }
  
  public String getTitle() {
    return getPublication().getName();
  }
  
  public void setTitle(String title) {
    getPublication().setName(title);
  }
  
  public String getDescription() {
    return getPublication().getDescription();
  }
  
  public void setUpdaterId(String userId) {
    getPublication().setUpdaterId(userId);
  }
  
  public String getUpdaterId() {
    return getPublication().getUpdaterId();
  }
  
  public boolean isVisible() {
    return getPublication().getVisibilityPeriod().contains(new Date());
  }
  
  public void setVisibilityPeriod(Period period) {
    getPublication().setVisibilityPeriod(period);
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getContent() {
    return content;
  }

  @Override
  public String getId() {
    return getPublication().getId();
  }

  @Override
  public String getComponentInstanceId() {
    return getPublication().getInstanceId();
  }

  @Override
  public String getSilverpeasContentId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public UserDetail getCreator() {
    return UserDetail.getById(getPublication().getCreatorId());
  }

  @Override
  public Date getCreationDate() {
    return getPublication().getCreationDate();
  }

  @Override
  public String getContributionType() {
    return "QuickInfo";
  }

  @Override
  public boolean canBeAccessedBy(UserDetail user) {
    return OrganisationControllerFactory.getOrganisationController().isComponentAvailable(
        getComponentInstanceId(), user.getId());
  }
  
}