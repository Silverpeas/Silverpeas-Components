/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
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

import java.util.Iterator;

import com.silverpeas.questionReply.model.Question;
import com.silverpeas.util.i18n.AbstractI18NBean;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.webactiv.persistence.IdPK;

/**
 * The questionReply implementation of SilverContentInterface
 */
public final class QuestionHeader extends AbstractI18NBean implements SilverContentInterface {
  private static final long serialVersionUID = 311303663095375317L;
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

  @Override
  public String getName() {
    return label;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getDescription(String language) {
    return getDescription();
  }

  @Override
  public String getName(String language) {
    return getName();
  }

  @Override
  public String getURL() {
    return "ConsultQuestionQuery?questionId=" + id;
  }

  @Override
  public String getId() {
    return (new Long(id)).toString();
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

  public String getIconUrl() {
    return "questionReplySmall.gif";
  }

  public String getCreatorId() {
    return this.creatorId;
  }

  public String getSilverCreationDate() {
    return this.date;
  }

  public Iterator<String> getLanguages() {
    return null;
  }
}