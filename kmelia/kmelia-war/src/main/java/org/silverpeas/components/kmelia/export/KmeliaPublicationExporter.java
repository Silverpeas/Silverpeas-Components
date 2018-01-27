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
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.export;

import org.apache.commons.io.FileUtils;
import org.silverpeas.components.kmelia.model.KmeliaPublication;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.converter.DocumentFormat;
import org.silverpeas.core.contribution.converter.DocumentFormatConverterProvider;
import org.silverpeas.core.contribution.converter.ODTConverter;
import org.silverpeas.core.importexport.ExportDescriptor;
import org.silverpeas.core.importexport.ExportException;
import org.silverpeas.core.importexport.Exporter;
import org.silverpeas.core.util.file.FileRepositoryManager;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.function.Supplier;

import static org.silverpeas.core.contribution.converter.DocumentFormat.inFormat;
import static org.silverpeas.core.contribution.converter.DocumentFormat.odt;

/**
 * An exporter of Kmelia publications into a document in a given format.
 * This exporter waits for the following parameters in the export descriptor:
 * <ul>
 * <li>the name of the document into which the export should be done,</li>
 * <li>the detail of the user for which the export should be done,</li>
 * <li>the language with which the export of the publication has to be done.</li>
 * </ul>
 */
@Singleton
public class KmeliaPublicationExporter implements Exporter<KmeliaPublication> {

  /**
   * Expected export parameter giving the detail about the user that calls the export of a
   * publication.
   */
  public static final String EXPORT_FOR_USER = "kmelia.export.forUser";
  /**
   * Expected export parameter giving the language with which the export of a publication has to be
   * performed.
   */
  public static final String EXPORT_LANGUAGE = "kmelia.export.language";
  /**
   * Optional export parameter giving the topic within which the export of a publication has to be
   * performed.
   */
  public static final String EXPORT_TOPIC = "kmelia.export.topic";

  /**
   * Only the first publication is taken in charge by this exporter.
   * @param descriptor the descriptor providing enough information about the export to perform
   * (document name, user for which the export is, the language in which the export has to be done,
   * ...)
   * @param supplier a supplier of the publication to export. If several publications are passed as
   * parameter, only the first one is taken.
   * @throws ExportException if an error occurs while exporting the publication.
   */
  @Override
  public void exports(ExportDescriptor descriptor, Supplier<KmeliaPublication> supplier) throws
          ExportException {
    OutputStream output = descriptor.getOutputStream();
    UserDetail user = descriptor.getParameter(EXPORT_FOR_USER);
    String language = descriptor.getParameter(EXPORT_LANGUAGE);
    String folderId = descriptor.getParameter(EXPORT_TOPIC);
    DocumentFormat targetFormat = DocumentFormat.inFormat(descriptor.getMimeType());
    KmeliaPublication publication = supplier.get();
    String documentPath = getTemporaryExportFilePathFor(publication);
    File odtDocument = null, exportFile = null;
    try {
      ODTDocumentBuilder builder = ODTDocumentBuilder.anODTDocumentBuilder().forUser(user).inLanguage(language).
              inTopic(folderId);
      odtDocument = builder.buildFrom(publication, ODTDocumentBuilder.anODTAt(documentPath));
      if (targetFormat != odt) {
        ODTConverter converter = DocumentFormatConverterProvider.getODTConverter();
        exportFile = converter.convert(odtDocument, inFormat(targetFormat));
      } else {
        exportFile = odtDocument;
      }
      output.write(FileUtils.readFileToByteArray(exportFile));
      output.flush();
      output.close();
    } catch (IOException ex) {
      throw new ExportException(ex.getMessage(), ex);
    } finally {
      if (odtDocument != null && odtDocument.exists()) {
        odtDocument.delete();
      }
      if (exportFile != null && exportFile.exists()) {
        exportFile.delete();
      }
    }
  }

  private String getTemporaryExportFilePathFor(final KmeliaPublication publication) {
    return FileRepositoryManager.getTemporaryPath() + publication.getPk().getId() + "-"
            + UUID.randomUUID().toString();
  }
}
