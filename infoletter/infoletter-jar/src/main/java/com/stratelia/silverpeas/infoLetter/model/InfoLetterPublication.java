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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.silverpeas.infoLetter.model;

import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.util.WAPrimaryKey;
import org.silverpeas.wysiwyg.control.WysiwygController;

public class InfoLetterPublication extends SilverpeasBean implements Comparable<InfoLetter> {
  private static final long serialVersionUID = 2579802983989822400L;
  public final static int PUBLICATION_EN_REDACTION = 1;
  public final static int PUBLICATION_VALIDEE = 2;

  public static final String TEMPLATE_ID = "template";

  // Membres

  /** id de l'instance */
  private String instanceId;

  /** titre de la publication */
  private String title;

  /** description de la publication */
  private String description;

  /** date de parution */
  private String parutionDate;

  /** etat de la publication */
  private int publicationState;

  /** id de la lettre */
  private int letterId;

  private String content;

  // Constructeurs

  /**
   * Constructeur sans parametres
   * @author frageade
   * @since February 2002
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
   * Constructeur à 7 parametres
   * @param WAPrimaryKey pk
   * @param instanceId
   * @param String title
   * @param String description
   * @param String parutionDate
   * @param int publicationState
   * @param String letterId
   */
  public InfoLetterPublication(WAPrimaryKey pk, String instanceId, String title,
      String description, String parutionDate, int publicationState,
      int letterId) {
    super();
    setPK(pk);
    this.instanceId = instanceId;
    this.title = title;
    this.description = description;
    this.parutionDate = parutionDate;
    this.publicationState = publicationState;
    this.letterId = letterId;
  }

  // Assesseurs

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

  // Methodes

  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

  public int compareTo(InfoLetter obj) {
    if (!(obj instanceof InfoLetter)) {
      return 0;
    }
    return (String.valueOf(getPK().getId())).compareTo(String
        .valueOf(((InfoLetter) obj).getPK().getId()));
  }

  public String _getTableName() {
    return "SC_IL_Publication";
  }

  public boolean _isValid() {
    return (publicationState == PUBLICATION_VALIDEE);
  }

  public String _getContent() {
    if (this.content == null) {
      this.content = WysiwygController
          .load(getInstanceId(), getPK().getId(), I18NHelper.defaultLanguage);
    }
    return this.content;
  }
}
