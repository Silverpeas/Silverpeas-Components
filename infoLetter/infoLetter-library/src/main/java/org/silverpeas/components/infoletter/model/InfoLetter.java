/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.infoletter.model;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.content.ddwe.model.DragAndDropWebEditorStore;
import org.silverpeas.core.contribution.content.renderer.ContributionContentRenderer;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.model.ContributionContent;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.WysiwygContent;
import org.silverpeas.core.ddwe.DragAndDropEditorContent;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.io.file.SilverpeasFile;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBean;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;
import org.silverpeas.core.util.StringUtil;

import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Optional.ofNullable;
import static org.silverpeas.core.i18n.I18NHelper.DEFAULT_LANGUAGE;
import static org.silverpeas.core.util.StringUtil.isNotDefined;

public class InfoLetter extends SilverpeasBean implements Comparable<InfoLetter> {

  private static final long serialVersionUID = -4798869204934629386L;

  public static final String TYPE = "Lettre";
  public static final String TEMPLATE_ID = "template";

  /** InfoLetter instance identifier */
  private String instanceId;

  /** InfoLetter name */
  private String name;

  /** Info Letter description */
  private String description;

  /** InfoLetter frequency */
  private String periode;

  private WysiwygContent templateContent;

  /**
   * Default constructor
   */
  public InfoLetter() {
    super();
    instanceId = "";
    name = "";
    description = "";
    periode = "";
  }

  /**
   * @param pk the info letter identifier
   * @param name the name
   * @param instanceId the component instance identifier
   * @param description the description
   * @param periode the frequency
   */
  public InfoLetter(WAPrimaryKey pk, String instanceId, String name, String description,
      String periode) {
    super();
    setPK(pk);
    this.instanceId = instanceId;
    this.name = name;
    this.description = description;
    this.periode = periode;
  }

  // Assesseurs

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getName() {
    return name;
  }

  public void setName(String n) {
    name = n;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getPeriode() {
    return periode;
  }

  public void setPeriode(String periode) {
    this.periode = periode;
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
    return "SC_IL_Letter";
  }

  public boolean existsTemplateContent() {
    return Optional.of(new DragAndDropWebEditorStore(getTemplateIdentifier()))
        .map(DragAndDropWebEditorStore::getFile)
        .map(SilverpeasFile::exists)
        .filter(Boolean.TRUE::equals)
        .orElseGet(() -> getTemplateWysiwygContent()
            .map(ContributionContent::getRenderer)
            .map(ContributionContentRenderer::renderEdition)
            .filter(StringUtil::isDefined)
            .filter(Predicate.not("<body></body>"::equalsIgnoreCase))
            .isPresent());
  }

  public Optional<WysiwygContent> getTemplateWysiwygContent() {
    if (this.templateContent == null) {
      final ContributionIdentifier templateId = getTemplateIdentifier();
      this.templateContent = WysiwygController.get(templateId.getComponentInstanceId(),
          templateId.getLocalId(), DEFAULT_LANGUAGE);
    }
    return ofNullable(this.templateContent);
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
  public void saveTemplateContent(final String manualContent) {
    this.templateContent = null;
    final ContributionIdentifier templateId = getTemplateIdentifier();
    String wysiwygContent = manualContent;
    if (isNotDefined(manualContent)) {
      // For now looking into Drag & Drop Edition Content
      final DragAndDropWebEditorStore store = new DragAndDropWebEditorStore(templateId);
      wysiwygContent = store.getFile()
          .getContainer()
          .getTmpContent()
          .map(DragAndDropWebEditorStore.Content::getValue)
          .map(c -> {
            store.getFile().getContainer().getOrCreateContent().setValue(c);
            store.save();
            return new DragAndDropEditorContent(c).getInlinedHtml();
          })
          .orElse(manualContent);
    }
    // Update the Wysiwyg if exists, create one otherwise
    WysiwygController.updateFileAndAttachment(wysiwygContent, templateId.getComponentInstanceId(),
        templateId.getLocalId(), User.getCurrentUser().getId(), I18NHelper.DEFAULT_LANGUAGE);
  }

  public ContributionIdentifier getTemplateIdentifier() {
    return ContributionIdentifier.from(getInstanceId(), TEMPLATE_ID + getPK().getId(), TYPE);
  }
}
