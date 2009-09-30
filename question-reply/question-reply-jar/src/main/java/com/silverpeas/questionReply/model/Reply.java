package com.silverpeas.questionReply.model;

import com.stratelia.webactiv.persistence.*;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.beans.admin.*;
import java.util.Date;

public class Reply extends SilverpeasBean {
  private long questionId;
  private String title;
  private String content;
  private String creatorId;
  private String creationDate;
  private int publicReply = 0;
  private int privateReply = 1;
  private static OrganizationController organizationController = new OrganizationController();

  public Reply() {
  }

  public Reply(String creatorId) {
    setCreatorId(creatorId);
    setCreationDate();
  }

  public Reply(long questionId, String creatorId) {
    setQuestionId(questionId);
    setCreatorId(creatorId);
    setCreationDate();
  }

  public long getQuestionId() {
    return questionId;
  }

  public String getTitle() {
    return title;
  }

  public String getContent() {
    return content;
  }

  public String getCreatorId() {
    return creatorId;
  }

  public String getCreationDate() {
    return creationDate;
  }

  public int getPublicReply() {
    return publicReply;
  }

  public int getPrivateReply() {
    return privateReply;
  }

  public void setQuestionId(long questionId) {
    this.questionId = questionId;
  }

  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  public void setCreationDate() {
    this.creationDate = DateUtil.date2SQLDate(new Date());
  }

  public void setCreationDate(String creationDate) {
    this.creationDate = creationDate;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public void setPublicReply(int publicReply) {
    this.publicReply = publicReply;
  }

  public void setPrivateReply(int privateReply) {
    this.privateReply = privateReply;
  }

  public String readCreatorName() {
    String creatorName = null;
    UserDetail userDetail = organizationController.getUserDetail(new Integer(
        getCreatorId()).toString());
    if (userDetail != null)
      creatorName = userDetail.getDisplayedName();
    return creatorName;
  }

  public String _getTableName() {
    return "SC_QuestionReply_Reply";
  }

  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

}
