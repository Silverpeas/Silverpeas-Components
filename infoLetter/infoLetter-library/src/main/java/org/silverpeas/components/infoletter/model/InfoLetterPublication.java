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
package org.silverpeas.components.infoletter.model;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBean;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.util.URLUtil;

/**
 * @author frageade
 */
public class InfoLetterPublication extends SilverpeasBean implements Comparable<InfoLetter> {
  private static final long serialVersionUID = 2579802983989822400L;
  public final static int PUBLICATION_EN_REDACTION = 1;
  public final static int PUBLICATION_VALIDEE = 2;

  public static final String TEMPLATE_ID = "template";

  /**
   * instance identifier
   */
  private String instanceId;

  /**
   * publication title
   */
  private String title;

  /**
   * publication description
   */
  private String description;

  /**
   * publish date
   */
  private String parutionDate;

  /**
   * publication state
   */
  private int publicationState;

  /**
   * letter identifier
   */
  private int letterId;

  private String content;

  /**
   * Default constructor
   */
  public InfoLetterPublication() {
    super();
    title = "";
    description = "";
    parutionDate = "";
    publicationState = PUBLICATION_EN_REDACTION;
    letterId = 0;
  }

  /**
   * @param pk
   * @param instanceId
   * @param title
   * @param description
   * @param parutionDate
   * @param publicationState
   * @param letterId
   */
  public InfoLetterPublication(WAPrimaryKey pk, String instanceId, String title, String description,
      String parutionDate, int publicationState, int letterId) {
    super();
    setPK(pk);
    this.instanceId = instanceId;
    this.title = title;
    this.description = description;
    this.parutionDate = parutionDate;
    this.publicationState = publicationState;
    this.letterId = letterId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getParutionDate() {
    return parutionDate;
  }

  public void setParutionDate(String parutionDate) {
    this.parutionDate = parutionDate;
  }

  public int getPublicationState() {
    return publicationState;
  }

  public void setPublicationState(int publicationState) {
    this.publicationState = publicationState;
  }

  public int getLetterId() {
    return letterId;
  }

  public void setLetterId(int letterId) {
    this.letterId = letterId;
  }

  public void setLetterId(String letterId) {
    this.letterId = Integer.parseInt(letterId);
  }

  public String _getPermalink() {
    return URLUtil.getSimpleURL(URLUtil.URL_NEWSLETTER, getPK().getId());
  }

  // Methodes

  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

  public int compareTo(InfoLetter obj) {
    if (obj == null) {
      return 0;
    }
    return (String.valueOf(getPK().getId())).compareTo(String.valueOf(obj.getPK().getId()));
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InfoLetter)) {
      return false;
    }

    final InfoLetter that = (InfoLetter) o;
    return compareTo(that) == 0;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(getPK().getId())
        .append(getPK().getInstanceId())
        .toHashCode();
  }

  public String _getTableName() {
    return "SC_IL_Publication";
  }

  public boolean _isValid() {
    return (publicationState == PUBLICATION_VALIDEE);
  }

  public String _getContent() {
    if (this.content == null) {
      this.content =
          WysiwygController.load(getInstanceId(), getPK().getId(), I18NHelper.defaultLanguage);
    }
    return this.content;
  }
}
