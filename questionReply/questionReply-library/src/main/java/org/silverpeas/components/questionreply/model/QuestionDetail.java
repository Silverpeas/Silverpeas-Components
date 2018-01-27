/*
 * Copyright (C) 2000 - 2018 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.questionreply.model;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerProvider;
import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.text.ParseException;
import java.util.Date;

public class QuestionDetail implements SilverpeasContent {

  private static final long serialVersionUID = -5411923403335541499L;

  public static final String TYPE = "QuestionReply";

  private Question question;
  // added for PDC integration
  private String silverObjectId;

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
      ContentManager contentManager = ContentManagerProvider.getContentManager();
      try {
        int objectId =
            contentManager.getSilverContentId(this.getId(), this.getComponentInstanceId());
        if (objectId >= 0) {
          this.silverObjectId = String.valueOf(objectId);
        }
      } catch (ContentManagerException ex) {
        SilverLogger.getLogger(this).silent(ex);
        this.silverObjectId = null;
      }
    }
    return this.silverObjectId;
  }

  @Override
  public User getCreator() {
    return User.getById(question.getCreatorId());
  }

  @Override
  public Date getCreationDate() {
    Date creationDate = null;
    try {
      creationDate = DateUtil.parse(question.getCreationDate());
    } catch (ParseException e) {
      SilverLogger.getLogger(this).error(e);
    }
    return creationDate;
  }

  @Override
  public User getLastModifier() {
    return getCreator();
  }

  @Override
  public Date getLastModificationDate() {
    return getCreationDate();
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
}
