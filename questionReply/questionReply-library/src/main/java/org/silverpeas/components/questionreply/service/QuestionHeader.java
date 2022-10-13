/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.questionreply.service;

import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.core.Identifiable;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.core.i18n.AbstractBean;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;

import java.text.ParseException;
import java.util.Date;

/**
 * A question
 */
public final class QuestionHeader extends AbstractBean implements LocalizedContribution,
    Identifiable {

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
  public String getId() {
    return Long.toString(id);
  }

  public String getInstanceId() {
    return instanceId;
  }

  @Override
  public ContributionIdentifier getIdentifier() {
    return ContributionIdentifier.from(getInstanceId(), getId(), getContributionType());
  }

  @Override
  public String getTitle() {
    return this.title;
  }

  @Override
  public String getContributionType() {
    return "Question";
  }

  @Override
  public Date getCreationDate() {
    if (StringUtil.isDefined(this.date)) {
      try {
        return DateUtil.parseDate(this.date);
      } catch (ParseException e) {
        return null;
      }
    }
    return null;
  }

  @Override
  public Date getLastUpdateDate() {
    return getCreationDate();
  }

  @Override
  public User getCreator() {
    return User.getById(getCreatorId());
  }

  @Override
  public User getLastUpdater() {
    return getCreator();
  }

  public String getCreatorId() {
    return this.creatorId;
  }

}