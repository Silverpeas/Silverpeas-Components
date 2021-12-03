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
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.content.ddwe.model.DragAndDropWebEditorStore;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.WysiwygContent;
import org.silverpeas.core.ddwe.DragAndDropEditorContent;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBean;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;
import org.silverpeas.core.util.URLUtil;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.silverpeas.components.infoletter.model.InfoLetterPublicationPdC.TYPE;
import static org.silverpeas.core.util.StringUtil.isNotDefined;

/**
 * @author frageade
 */
public class InfoLetterPublication extends SilverpeasBean implements Comparable<InfoLetter> {
  private static final long serialVersionUID = 2579802983989822400L;
  public static final int PUBLICATION_EN_REDACTION = 1;
  public static final int PUBLICATION_VALIDEE = 2;

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

  private WysiwygContent content;

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

  public ContributionIdentifier getIdentifier() {
    return ContributionIdentifier.from(getInstanceId(), getPK().getId(), TYPE);
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

  @Override
  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

  @Override
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

  @Override
  public String _getTableName() {
    return "SC_IL_Publication";
  }

  public boolean _isValid() {
    return (publicationState == PUBLICATION_VALIDEE);
  }

  public Optional<WysiwygContent> getWysiwygContent() {
    if (this.content == null) {
      this.content = WysiwygController.get(getInstanceId(), getPK().getId(),
          I18NHelper.DEFAULT_LANGUAGE);
    }
    return ofNullable(this.content);
  }

  /**
   * Saves given content.
   * <p>
   *   The given content MAY be directly a WYSIWYG content, in a such case the content has been
   *   edited by a WYSIWYG editor.
   * </p>
   * <p>
   *   The given content is not defined, in a such case the content has been MAYBE edited by a
   *   Drag And Drop Web Editor. Then the temporary content is saved into final one and the
   *   Inlined HTML is saved into WYSIWYG repository.
   * </p>
   * @param manualContent a manual content. The content is specified when it comes directly from
   * a WYSIWYG editing.
   */
  public void saveContent(final String manualContent) {
    final ContributionIdentifier identifier = getIdentifier();
    String wysiwygContent = manualContent;
    if (isNotDefined(manualContent)) {
      // For now looking into Drag & Drop Edition Content
      final DragAndDropWebEditorStore store = new DragAndDropWebEditorStore(identifier);
      wysiwygContent = store.getFile()
          .getContainer()
          .getTmpContent()
          .map(DragAndDropWebEditorStore.Content::getValue)
          .map(c -> {
            store.getFile().getContainer().getOrCreateContent().setValue(c);
            store.save();
            final DragAndDropEditorContent editorContent = new DragAndDropEditorContent(c);
            return editorContent.getSimpleContent().orElseGet(editorContent::getInlinedHtml);
          })
          .orElse(manualContent);
    }
    // Update the Wysiwyg if exists, create one otherwise
    WysiwygController.updateFileAndAttachment(wysiwygContent, identifier.getComponentInstanceId(),
        identifier.getLocalId(), User.getCurrentUser().getId(), I18NHelper.DEFAULT_LANGUAGE);
  }

  /**
   * Deletes contents linked to the publication (WYSIWYG and DDWE ones).
   */
  public void deleteContent() {
    final ContributionIdentifier identifier = getIdentifier();
    WysiwygController.deleteWysiwygAttachments(identifier.getComponentInstanceId(),
        identifier.getLocalId());
    new DragAndDropWebEditorStore(identifier).delete();
  }
}
