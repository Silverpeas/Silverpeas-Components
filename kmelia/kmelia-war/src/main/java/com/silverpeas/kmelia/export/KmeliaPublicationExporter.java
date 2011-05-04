/*
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.kmelia.export;

import com.silverpeas.converter.DocumentFormat;
import com.silverpeas.converter.ODTConverter;
import com.silverpeas.converter.DocumentFormatConverterFactory;
import java.io.File;
import com.silverpeas.export.ExportDescriptor;
import com.silverpeas.export.ExportException;
import com.silverpeas.export.Exporter;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.kmelia.model.KmeliaPublication;
import java.util.Arrays;
import java.util.List;
import static com.silverpeas.kmelia.export.ODTDocumentBuilder.*;
import static com.silverpeas.converter.DocumentFormat.*;

/**
 * An exporter of Kmelia publications into a document in a given format.
 * This exporter waits for the following parameters in the export descriptor:
 * <ul>
 * <li>the name of the document into which the export should be done,</li>
 * <li>the detail of the user for which the export should be done,</li>
 * <li>the language with which the export of the publication has to be done.</li>
 * </ul>
 */
public class KmeliaPublicationExporter implements Exporter<KmeliaPublication> {

  /**
   * Expected export parameter giving the name of the document into which the publication has
   * to be performed.
   */
  public static final String EXPORT_DOCUMENT_NAME = "kmelia.export.documentName";
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
   * The parameter giving the File object that corresponds to the document into which the
   * publication is exported. This parameter is set in the descriptor by the export process itself.
   */
  public static final String EXPORT_DOCUMENT = "kmelia.export.document";

  /**
   * Only the first publication is taken in charge by this exporter.
   * @param descriptor the descriptor providing enough information about the export to perform
   * (document name, user for which the export is, the language in which the export has to be done,
   * ...)
   * @param publications the publications to export. If several publications are passed as parameter,
   * only the first one is taken.
   * @throws ExportException if an error occurs while exporting the publication.
   */
  @Override
  public void export(ExportDescriptor descriptor, KmeliaPublication... publications) throws
          ExportException {
    export(descriptor, Arrays.asList(publications));
  }

  /**
   * Only the first publication is taken in charge by this exporter.
   * @param descriptor the descriptor providing enough information about the export to perform
   * (document name, user for which the export is, the language in which the export has to be done,
   * ...)
   * @param publications the publications to export. If several publications are passed as parameter,
   * only the first one is taken. If no publications are passed as parameter, nothing is done.
   * @throws ExportException if an error occurs while exporting the publication.
   */
  @Override
  public void export(ExportDescriptor descriptor,
          List<KmeliaPublication> publications) throws ExportException {
    if (!publications.isEmpty()) {
      KmeliaPublication publication = publications.get(0);
      String fileName = descriptor.getParameter(EXPORT_DOCUMENT_NAME);
      UserDetail user = descriptor.getParameter(EXPORT_FOR_USER);
      String language = descriptor.getParameter(EXPORT_LANGUAGE);
      DocumentFormat format   = DocumentFormat.inFormat(descriptor.getFormat());
      ODTDocumentBuilder builder = getODTDocumentBuilder().forUser(user).inLanguage(language);
      File odtDocument = builder.buildFrom(publication, anODTNamed(fileName));
      ODTConverter converter = DocumentFormatConverterFactory.getFactory().getODTConverter();
      File exportFile = converter.convert(odtDocument, inFormat(format));
      descriptor.setParameter(EXPORT_DOCUMENT, exportFile);
    }
  }
}
