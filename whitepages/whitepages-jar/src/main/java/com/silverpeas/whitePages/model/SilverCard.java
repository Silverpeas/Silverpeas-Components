package com.silverpeas.whitePages.model;

import java.text.ParseException;
import java.util.Date;

import com.silverpeas.SilverpeasContent;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;

public class SilverCard implements SilverpeasContent {

  private static final long serialVersionUID = 28853110916688897L;
  private String id;
  private String instanceId;
  private String creatorId;
  private Date creationDate;
  private String silverpeasContentId;
  
  public SilverCard(Card card, int silverContentId) {
    id = card.getPK().getId();
    instanceId = card.getInstanceId();
    creatorId = Integer.toString(card.getCreatorId());
    try {
      creationDate = DateUtil.parse(card.getCreationDate());
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    silverpeasContentId = Integer.toString(silverContentId);
  }
  
  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getComponentInstanceId() {
    return instanceId;
  }

  @Override
  public String getSilverpeasContentId() {
    return silverpeasContentId;
  }

  @Override
  public UserDetail getCreator() {
    return UserDetail.getById(creatorId);
  }

  @Override
  public Date getCreationDate() {
    return creationDate;
  }

  @Override
  public String getTitle() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDescription() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getContributionType() {
    // TODO Auto-generated method stub
    return null;
  }
}
