/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.components.whitepages.control;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.components.whitepages.model.Card;
import org.silverpeas.components.whitepages.record.UserRecord;
import org.silverpeas.components.whitepages.record.UserTemplate;
import org.silverpeas.core.Identifiable;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.core.i18n.AbstractBean;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class CardHeader extends AbstractBean
    implements LocalizedContribution, Identifiable, Comparable<CardHeader> {

  private static final long serialVersionUID = -8781512864589764317L;
  private long id;
  private String instanceId;
  private final String date;
  private final String creatorId;
  private static final SettingBundle whitePagesIcons =
      ResourceLocator.getSettingBundle("org.silverpeas.whitePages.settings.whitePagesIcons");

  public void init(long id, Card card) {
    this.id = id;

    String label;
    try {
      UserRecord user = getUserRecord(card);
      if (user == null) {
        label = "user(" + id + ")";
      } else {
        /*
         * Label value.
         */
        Field firstName = user.getField("FirstName");
        Field name = user.getField("LastName");
        label = name.getValue("") + " " + firstName.getValue("");

        /*
         * Description value.
         */
        String isMailHidden = getParam("isEmailHidden", instanceId);
        if ((isMailHidden != null) && (isMailHidden.equals("yes"))) {
          setDescription(buildMailLink(instanceId, card.getPK().getId()));
        } else {
          setDescription(user.getField("Mail").getValue(""));
        }

      }
    } catch (FormException e) {
      label = "user(" + id + ")";
    }
    setName(label);
    this.instanceId = card.getInstanceId();
  }

  private String buildMailLink(String instanceId, String cardId) {
    StringBuilder buffer = new StringBuilder();

    buffer.append("<a href=\"");
    buffer.append(URLUtil.getApplicationURL());
    buffer.append(URLUtil.getURL(null, instanceId));
    buffer.append("NotifyExpert?cardId=");
    buffer.append(cardId);
    buffer.append("\"><img src=\"");
    buffer.append(URLUtil.getApplicationURL());
    buffer.append(whitePagesIcons.getString("whitePages.notify"));
    buffer.append("\" border=\"0\"></a>");

    return buffer.toString();
  }

  public CardHeader(long id, Card card, String instanceId, String date, String creatorId) {
    this.instanceId = instanceId;
    this.date = date;
    this.creatorId = creatorId;
    init(id, card);
  }

  public String getURL() {
    return "consultIdentity?userCardId=" + id;
  }

  private static UserRecord getUserRecord(Card card) {
    try {
      UserTemplate templateUser = getUserTemplate(card.getInstanceId());
      card.writeUserRecord(templateUser.getRecord(card.getUserId()));
      return card.readUserRecord();
    } catch (Exception e) {
      return null;
    }
  }

  private static UserTemplate getUserTemplate(String instanceId) {
    UserTemplate template = templates.get(instanceId);

    if (template == null) {
      SettingBundle templateSettings =
          ResourceLocator.getSettingBundle("org.silverpeas.whitePages.settings.template");
      String templateDir = templateSettings.getString("templateDir");
      String userTemplate = getParam("userTemplate", instanceId);
      template =
          new UserTemplate(templateDir.replace('\\', '/') + "/" + userTemplate.replace('\\', '/'),
              "");
      templates.put(instanceId, template);
    }
    return template;
  }

  private static String getParam(String paramName, String instanceId) {
    return AdministrationServiceProvider.getAdminService()
        .getComponentParameterValue(instanceId, paramName);
  }

  static Map<String, UserTemplate> templates = new HashMap<>();

  @Override
  public String getId() {
    return String.valueOf(id);
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
    return getName();
  }

  @Override
  public String getContributionType() {
    return "BusinessCard";
  }

  public String getCreatorId() {
    return this.creatorId;
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

  @Override
  public int compareTo(CardHeader other) {
    return this.getName().compareTo(other.getName());
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CardHeader)) {
      return false;
    }

    final CardHeader that = (CardHeader) o;
    return compareTo(that) == 0;

  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getName()).toHashCode();
  }
}