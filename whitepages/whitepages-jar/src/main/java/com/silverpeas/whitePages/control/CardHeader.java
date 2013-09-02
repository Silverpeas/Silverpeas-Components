/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.whitePages.control;

import com.silverpeas.form.Field;
import com.silverpeas.form.FormException;
import com.silverpeas.util.i18n.AbstractI18NBean;
import com.silverpeas.whitePages.model.Card;
import com.silverpeas.whitePages.record.UserRecord;
import com.silverpeas.whitePages.record.UserTemplate;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.util.ResourceLocator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The fileboxplus implementation of SilverContentInterface
 */
public final class CardHeader extends AbstractI18NBean implements SilverContentInterface,
    Comparable {
  
  private static final long serialVersionUID = -8781512864589764317L;
  private long id;
  private String label;
  private String instanceId;
  private String date;
  private String creatorId;
  private String description;
  private final static ResourceLocator whitePagesIcons = new ResourceLocator(
      "com.silverpeas.whitePages.settings.whitePagesIcons", "");

  public void init(long id, Card card) {
    this.id = id;

    String label = null;
    try {
      UserRecord user = getUserRecord(card);
      if (user == null)
        label = "user(" + id + ")";
      else {
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
          this.description = buildMailLink(instanceId, card.getPK().getId());
        } else {
          this.description = ((Field) user.getField("Mail")).getValue("");
        }

      }
    } catch (FormException e) {
      label = "user(" + id + ")";
    }
    this.label = label;
    this.instanceId = card.getInstanceId();
  }

  private String buildMailLink(String instanceId, String cardId) {
    StringBuffer buffer = new StringBuffer();

    buffer.append("<a href=\"");
    buffer.append(URLManager.getApplicationURL());
    buffer.append(URLManager.getURL(null, instanceId));
    buffer.append("NotifyExpert?cardId=");
    buffer.append(cardId);
    buffer.append("\"><img src=\"");
    buffer.append(URLManager.getApplicationURL());
    buffer.append(whitePagesIcons.getString("whitePages.notify"));
    buffer.append("\" border=\"0\"></a>");

    return buffer.toString();
  }

  public CardHeader(long id, Card card) {
    init(id, card);
  }

  public CardHeader(long id, Card card, String instanceId, String date,
      String creatorId) {
    this.instanceId = instanceId;
    this.date = date;
    this.creatorId = creatorId;
    init(id, card);
  }

  public String getName() {
    return label;
  }

  public String getDescription() {
    return this.description;
  }

  public String getURL() {
    return "consultIdentity?userCardId=" + id;
  }

  static private UserRecord getUserRecord(Card card) {
    try {
      UserTemplate templateUser = getUserTemplate(card.getInstanceId());
      card.writeUserRecord(templateUser.getRecord(card.getUserId()));
      UserRecord user = card.readUserRecord();

      return user;
    } catch (Exception e) {
      return null;
    }
  }

  static private UserTemplate getUserTemplate(String instanceId) {
    UserTemplate template = (UserTemplate) templates.get(instanceId);

    if (template == null) {
      ResourceLocator templateSettings = new ResourceLocator(
          "com.silverpeas.whitePages.settings.template", "");
      String templateDir = templateSettings.getString("templateDir");
      String userTemplate = getParam("userTemplate", instanceId);
      template = new UserTemplate(templateDir.replace('\\', '/') + "/"
          + userTemplate.replace('\\', '/'), "");

      templates.put(instanceId, template);
    }
    return template;
  }

  static private String getParam(String paramName, String instanceId) {
    return AdminReference.getAdminService().getComponentParameterValue(instanceId, paramName);
  }

  static Map<String, UserTemplate> templates = new HashMap<String, UserTemplate>();

  public String getId() {
    return (new Long(id)).toString();
  }

  public String getInstanceId() {
    return instanceId;
  }

  public String getTitle() {
    return this.label;
  }

  public String getDate() {
    return this.date;
  }

  public String getIconUrl() {
    return "whitePagesSmall.gif";
  }

  public String getCreatorId() {
    return this.creatorId;
  }

  public String getSilverCreationDate() {
    return this.date;
  }

  public String getDescription(String language) {
    return getDescription();
  }

  public String getName(String language) {
    return getName();
  }

  public Iterator getLanguages() {
    return null;
  }

  public int compareTo(Object other) {
    if (other instanceof CardHeader) {
      CardHeader otherCard = (CardHeader) other;
      return this.label.compareTo(otherCard.label);
    } else
      return 1;
  }
}