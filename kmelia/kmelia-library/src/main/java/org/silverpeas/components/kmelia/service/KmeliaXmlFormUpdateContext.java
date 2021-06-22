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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.kmelia.service;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.components.kmelia.model.KmaxRuntimeException;
import org.silverpeas.components.kmelia.model.KmeliaRuntimeException;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.contribution.content.form.field.FileField;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.util.MemoizedSupplier;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileUploadUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Handles the context of an Xml Form update.
 * <p>
 * It permits to manage some useful caches.
 * </p>
 * @author silveryocha
 */
public class KmeliaXmlFormUpdateContext {

  private final Map<String, PublicationTemplate> templateCache = new HashMap<>();
  private final Map<String, Pair<PublicationTemplate, DataRecord>> dataRecordCache =
      new HashMap<>();
  private final List<FileItem> items;
  private final MemoizedSupplier<String> xmlShortName;
  private final boolean forceUpdatePublication;
  private boolean batchProcessing = false;

  public KmeliaXmlFormUpdateContext(final List<FileItem> items,
      final boolean forceUpdatePublication) {
    this.items = items;
    this.forceUpdatePublication = forceUpdatePublication;
    this.xmlShortName = new MemoizedSupplier<>(
        () -> FileUploadUtil.getParameter(items, "KmeliaPubFormName"));
  }

  public KmeliaXmlFormUpdateContext batchProcessing() {
    this.batchProcessing = true;
    return this;
  }

  public List<FileItem> getItems() {
    return items;
  }

  public boolean isForceUpdatePublication() {
    return forceUpdatePublication;
  }

  public boolean isBatchProcessing() {
    return batchProcessing;
  }

  /**
   * Gets from the context the shot name of the Xml Form.
   * @return a name as string.
   */
  public String getXmlFormShortNameFromItems() {
    return xmlShortName.get();
  }

  /**
   * Gets the list of couples of {@link FileItem} and {@link FileField} about the given
   * publication and language.
   * @param pub a publication.
   * @param language a language.
   * @return a list of couple of {@link FileItem} and {@link FileField}.
   */
  public List<Pair<FileItem, FileField>> getPublicationFileFields(final PublicationDetail pub,
      final String language) {
    final Pair<PublicationTemplate, DataRecord> pubData = getOrInitializePublicationDataRecordOf(pub, language);
    try {
      return pubData.getFirst()
          .getUpdateForm()
          .getFieldTemplates()
          .stream()
          .flatMap(ft -> IntStream.range(0, ft.getMaximumNumberOfOccurrences()).mapToObj(i -> {
            final String fieldName = ft.getFieldName();
            final Field field = pubData.getSecond().getField(fieldName, i);
            if (FileField.TYPE.equals(field.getTypeName())) {
              final String inputName = Util.getFieldOccurrenceName(field.getName(), field.getOccurrence());
              final FileItem item = FileUploadUtil.getFile(items, inputName);
              if (item != null && !item.isFormField() && StringUtil.isDefined(item.getName())) {
                return Pair.of(item, (FileField) field);
              } else {
                return Pair.of((FileItem) null, (FileField) field);
              }
            }
            return null;
          })
          .filter(Objects::nonNull))
          .collect(Collectors.toList());
    } catch (PublicationTemplateException e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  /**
   * Gets a {@link Pair} of {@link Form} and existing {@link DataRecord} of the given
   * publication if any or initializes a new {@link DataRecord} otherwise.
   * <p>
   * Searched elements are cached in order to improve the treatment processing.
   * </p>
   * @param pub the aimed publication.
   * @param language the current managed language.
   * @return a {@link Pair} of {@link Form} and {@link DataRecord}.
   * @throws KmeliaRuntimeException in case of publication template service error or in case of
   * form management error.
   */
  public Pair<PublicationTemplate, DataRecord> getOrInitializePublicationDataRecordOf(
      final PublicationDetail pub, final String language) {
    return dataRecordCache.computeIfAbsent(pub.getId() + ":" + language, i -> {
      final String externalId = pub.getInstanceId() + ":" + getXmlFormShortNameFromItems();
      final PublicationTemplate template = getPublicationTemplate(externalId);
      try {
        final RecordSet set = template.getRecordSet();
        final String pubLanguage = pub.getLanguageToDisplay(language);
        DataRecord data = set.getRecord(pub.getId(), pubLanguage);
        if (data == null || (pubLanguage != null && !pubLanguage.equals(data.getLanguage()))) {
          // This publication haven't got any content at all or for requested language
          data = set.getEmptyRecord();
          data.setId(pub.getId());
          data.setLanguage(pubLanguage);
        }
        return Pair.of(template, data);
      } catch (FormException | PublicationTemplateException e) {
        throw new KmeliaRuntimeException(e);
      }
    });
  }

  /**
   * Gets a {@link PublicationTemplate} instance from its external identifier.
   * <p>
   * The instance is cached against its external identifier.
   * </p>
   * @param externalId an external identifier as string.
   * @return a {@link PublicationTemplate} instance.
   */
  private PublicationTemplate getPublicationTemplate(final String externalId) {
    return templateCache.computeIfAbsent(externalId, i -> {
      try {
        return getPublicationTemplateManager().getPublicationTemplate(i);
      } catch (PublicationTemplateException e) {
        throw new KmaxRuntimeException(e);
      }
    });
  }

  /**
   * Gets an instance of PublicationTemplateManager.
   * @return an instance of PublicationTemplateManager.
   */
  private PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }
}
