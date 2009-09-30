package com.silverpeas.questionReply.control;

import java.util.Iterator;

import com.silverpeas.questionReply.model.Question;
import com.silverpeas.util.i18n.AbstractI18NBean;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.webactiv.persistence.IdPK;

/**
 * The questionReply implementation of SilverContentInterface
 */
public final class QuestionHeader extends AbstractI18NBean implements
    SilverContentInterface {
  private long id;
  private String label;
  private String instanceId;
  private String title;
  private String date;
  private String creatorId;
  private String description;

  public void init(long id, Question question) {
    this.id = ((IdPK) question.getPK()).getIdAsLong();
    this.label = question.getTitle();
    this.title = question.getTitle();
    this.date = question.getCreationDate();
    this.description = question.getContent();
  }

  public QuestionHeader(long id, Question question) {
    init(id, question);
  }

  public QuestionHeader(long id, Question question, String instanceId,
      String date, String creatorId) {
    init(id, question);
    this.instanceId = instanceId;
    this.date = date;
    this.creatorId = creatorId;
  }

  public String getName() {
    return label;
  }

  public String getDescription() {
    return description;
  }

  public String getDescription(String language) {
    return getDescription();
  }

  public String getName(String language) {
    return getName();
  }

  public String getURL() {
    return "ConsultQuestionQuery?questionId=" + id;
  }

  public String getId() {
    return (new Long(id)).toString();
  }

  public String getInstanceId() {
    return instanceId;
  }

  public String getTitle() {
    return this.title;
  }

  public String getDate() {
    return this.date;
  }

  public String getIconUrl() {
    return "questionReplySmall.gif";
  }

  public String getCreatorId() {
    return this.creatorId;
  }

  public String getSilverCreationDate() {
    return this.date;
  }

  public Iterator getLanguages() {
    return null;
  }
}