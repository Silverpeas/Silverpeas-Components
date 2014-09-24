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
 * FLOSS exception.  You should have received a copy of the text describing
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
package com.silverpeas.questionReply.control;

import com.silverpeas.questionReply.model.Question;
import org.silverpeas.util.i18n.AbstractBean;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.webactiv.persistence.IdPK;

/**
 * The questionReply implementation of SilverContentInterface
 */
public final class QuestionHeader extends AbstractBean implements SilverContentInterface {

  private static final long serialVersionUID = 311303663095375317L;
  private long id;
  private String instanceId;
  private String title;
  private String date;
  private String creatorId;

  public void init(Question question) {
    this.id = ((IdPK) question.getPK()).getIdAsLong();
    setName(question.getTitle());
    setDescription(question.getContent());
    this.title = question.getTitle();
    this.date = question.getCreationDate();
  }

  public QuestionHeader(Question question) {
    init(question);
  }

  public QuestionHeader(Question question, String instanceId, String date, String creatorId) {
    init(question);
    this.instanceId = instanceId;
    this.date = date;
    this.creatorId = creatorId;
  }

  @Override
  public String getURL() {
    return "ConsultQuestionQuery?questionId=" + id;
  }

  @Override
  public String getId() {
    return Long.toString(id);
  }

  @Override
  public String getInstanceId() {
    return instanceId;
  }

  public String getTitle() {
    return this.title;
  }

  @Override
  public String getDate() {
    return this.date;
  }

  @Override
  public String getIconUrl() {
    return "questionReplySmall.gif";
  }

  @Override
  public String getCreatorId() {
    return this.creatorId;
  }

  @Override
  public String getSilverCreationDate() {
    return this.date;
  }
}