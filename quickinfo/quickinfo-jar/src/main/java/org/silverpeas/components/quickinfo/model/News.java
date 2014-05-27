package org.silverpeas.components.quickinfo.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.date.Period;

import com.silverpeas.SilverpeasContent;
import com.silverpeas.thumbnail.control.ThumbnailController;
import com.silverpeas.thumbnail.model.ThumbnailDetail;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

public class News implements SilverpeasContent {
  
  private static final long serialVersionUID = 1L;
  private PublicationDetail publication;
  private String content;
  private int[] broadcastModes;
  
  public static final int BROADCAST_CLASSIC = 1;
  public static final int BROADCAST_MAJOR = 2;
  public static final int BROADCAST_TICKER = 3;
  public static final int BROADCAST_BLOCKING = 4;
  
  public News(String name, String description, Period visibilityPeriod, int[] broadcastModes) {
    PublicationDetail publi = new PublicationDetail(name, description, visibilityPeriod, null, null);
    this.publication = publi;
    setBroadcastModes(broadcastModes);
    this.broadcastModes = broadcastModes;
  }
  
  public News(PublicationDetail publication) {
    this.publication = publication;
    this.content = getPublication().getWysiwyg();
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
  
  public void setDescription(String desc) {
    getPublication().setDescription(desc);
  }
  
  public void setCreatorId(String userId) {
    getPublication().setCreatorId(userId);
  }
  
  public String getCreatorId() {
    return getPublication().getCreatorId();
  }
  
  public void setUpdaterId(String userId) {
    getPublication().setUpdaterId(userId);
  }
  
  public String getUpdaterId() {
    return getPublication().getUpdaterId();
  }
  
  public Date getLastUpdateDate() {
    return getPublication().getUpdateDate();
  }
  
  public boolean isVisible() {
    return getVisibilityPeriod().contains(new Date());
  }
  
  public void setVisibilityPeriod(Period period) {
    getPublication().setVisibilityPeriod(period);
  }
  
  public Period getVisibilityPeriod() {
    return getPublication().getVisibilityPeriod();
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getContent() {
    return content;
  }
  
  public List<Integer> getBroadcastModes() {
    List<Integer> modes = new ArrayList<Integer>();
    modes.add(getPublication().getImportance());
    return modes;
  }
  
  public void setBroadcastModes(int[] modes) {
    if (modes != null && modes.length > 0) {
      getPublication().setImportance(modes[0]);
    } else {
      getPublication().setImportance(BROADCAST_CLASSIC);
    }
  }
  
  public void setBroadcastModes(List<Integer> modes) {
    if (modes != null && !modes.isEmpty()) {
      getPublication().setImportance(modes.get(0));
    } else {
      getPublication().setImportance(BROADCAST_CLASSIC);
    }
  }
  
  public ThumbnailDetail getThumbnail() {
    ThumbnailDetail thumbnail =
        new ThumbnailDetail(getComponentInstanceId(), Integer.parseInt(getId()),
            ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE);
    return ThumbnailController.getCompleteThumbnail(thumbnail);
  }

  @Override
  public String getId() {
    return getPublication().getId();
  }

  @Override
  public String getComponentInstanceId() {
    return getPublication().getInstanceId();
  }
  
  public void setComponentInstanceId(String componentId) {
    getPublication().getPK().setComponentName(componentId);
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