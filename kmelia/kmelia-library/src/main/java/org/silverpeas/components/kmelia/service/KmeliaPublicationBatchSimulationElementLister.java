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
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.components.kmelia.service;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.process.SimpleDocumentSimulationElement;
import org.silverpeas.core.contribution.content.form.field.FileField;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileUtil;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.silverpeas.core.contribution.attachment.AttachmentServiceProvider.getAttachmentService;

/**
 * This lister is used into context of update of a batch of publications.
 */
public class KmeliaPublicationBatchSimulationElementLister
    extends AbstractKmeliaSimulationElementLister {

  private final Map<PublicationPK, PublicationDetail> publicationCache;
  private final KmeliaXmlFormUpdateContext xmlFormUpdateContext;
  private final User currentUser;

  public KmeliaPublicationBatchSimulationElementLister(
      final Map<PublicationPK, PublicationDetail> publicationCache,
      final KmeliaXmlFormUpdateContext xmlFormUpdateContext, final User currentUser) {
    super();
    this.publicationCache = publicationCache;
    this.xmlFormUpdateContext = xmlFormUpdateContext;
    this.currentUser = currentUser;
  }

  @Override
  public void listElements(final ResourceReference sourcePK, final String language) {
    if (sourcePK instanceof PublicationPK && xmlFormUpdateContext != null) {
      listXmlFormDocumentsOfPublication(getPublicationByPk((PublicationPK) sourcePK), language);
    } else {
      throw new IllegalArgumentException(
          "The reference " + sourcePK.getClass().getSimpleName() + " isn't taken in charge");
    }
  }

  private void listXmlFormDocumentsOfPublication(final PublicationDetail publication,
      final String language) {
    final List<Pair<FileItem, FileField>> data = xmlFormUpdateContext.getPublicationFileFields(
        publication, language);
    final Set<String> existingAttachmentIdToBeRemoved = data.stream()
        .filter(d -> Objects.nonNull(d.getFirst()))
        .map(Pair::getSecond)
        .map(FileField::getAttachmentId)
        .filter(StringUtil::isDefined)
        .collect(Collectors.toSet());
    if (!existingAttachmentIdToBeRemoved.isEmpty()) {
      getAttachmentService().listDocumentsByForeignKeyAndType(publication.getPK(), DocumentType.form, language)
          .stream()
          .filter(d -> existingAttachmentIdToBeRemoved.contains(d.getId()))
          .forEach(d -> addElement(new SimpleDocumentSimulationElement(d).setOld()));
    }
    data.stream()
        .map(Pair::getFirst)
        .filter(Objects::nonNull)
        .map(i -> createDummySimpleDocument(publication, i, i.getName()))
        .forEach(d -> addElement(new SimpleDocumentSimulationElement(d)));
  }

  private PublicationDetail getPublicationByPk(final PublicationPK pk) {
    return publicationCache.computeIfAbsent(pk, k -> getPublicationService().getDetail(k));
  }

  private SimpleDocument createDummySimpleDocument(PublicationDetail pub, FileItem item, String fileName) {
    // the UUID make same document non unique in order to count rightly the amount of bytes written on the server
    final SimpleAttachment attachment = SimpleAttachment.builder()
        .setFilename(fileName + UUID.randomUUID())
        .setSize(item.getSize())
        .setContentType(FileUtil.getMimeType(fileName))
        .setCreationData(currentUser.getId(), new Date())
        .build();
    final SimpleDocumentPK documentPk = new SimpleDocumentPK(null, pub.getInstanceId());
    final SimpleDocument document = new SimpleDocument(documentPk, pub.getId(), 0, false, null, attachment);
    document.setDocumentType(DocumentType.form);
    document.setSize(item.getSize());
    return document;
  }
}
