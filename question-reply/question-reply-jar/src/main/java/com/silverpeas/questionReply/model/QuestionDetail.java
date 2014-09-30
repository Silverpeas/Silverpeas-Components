package com.silverpeas.questionReply.model;

import com.silverpeas.SilverpeasContent;
import com.silverpeas.accesscontrol.AccessController;
import com.silverpeas.accesscontrol.AccessControllerProvider;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.contentManager.ContentManagerFactory;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.accesscontrol.ComponentAccessControl;
import org.silverpeas.util.DateUtil;

import javax.enterprise.util.AnnotationLiteral;
import java.text.ParseException;
import java.util.Date;

public class QuestionDetail implements SilverpeasContent {

  private static final long serialVersionUID = -5411923403335541499L;

  public static final String TYPE = "QuestionReply";

  private Question question;
  private String silverObjectId; // added for PDC integration

  /**
   * @param question
   */
  public QuestionDetail(Question question) {
    super();
    this.question = question;
  }

  /**
   * @return the question
   */
  public Question getQuestion() {
    return question;
  }

  /**
   * @param question the question to set
   */
  public void setQuestion(Question question) {
    this.question = question;
  }

  @Override
  public String getId() {
    return question.getPK().getId();
  }

  @Override
  public String getComponentInstanceId() {
    return question.getInstanceId();
  }

  @Override
  public String getSilverpeasContentId() {
    if (this.silverObjectId == null) {
      ContentManager contentManager = ContentManagerFactory.getFactory().getContentManager();
      try {
        int objectId =
            contentManager.getSilverContentId(this.getId(), this.getComponentInstanceId());
        if (objectId >= 0) {
          this.silverObjectId = String.valueOf(objectId);
        }
      } catch (ContentManagerException ex) {
        this.silverObjectId = null;
      }
    }
    return this.silverObjectId;
  }

  @Override
  public UserDetail getCreator() {
    return UserDetail.getById(question.getCreatorId());
  }

  @Override
  public Date getCreationDate() {
    Date creationDate = null;
    try {
      creationDate = DateUtil.parse(question.getCreationDate());
    } catch (ParseException e) {
      SilverTrace.error("questionReply", "QuestionDetail.getCreationDate", "Problem to parse date",
          e);
    }
    return creationDate;
  }

  @Override
  public String getTitle() {
    return question.getTitle();
  }

  @Override
  public String getDescription() {
    return question.getContent();
  }

  @Override
  public String getContributionType() {
    return TYPE;
  }

  /**
   * Is the specified user can access this question?
   * <p/>
   * A user can access a question if it has enough rights to access the QuestionReply instance in
   * which is managed this question.
   * @param user a user in Silverpeas.
   * @return true if the user can access this question, false otherwise.
   */
  @Override
  public boolean canBeAccessedBy(final UserDetail user) {
    AccessController<String> accessController = AccessControllerProvider
        .getAccessController(new AnnotationLiteral<ComponentAccessControl>() {});
    return accessController.isUserAuthorized(user.getId(), getComponentInstanceId());
  }

}
