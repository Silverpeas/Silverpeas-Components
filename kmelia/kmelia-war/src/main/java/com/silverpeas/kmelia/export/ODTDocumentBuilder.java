/*
 *  Copyright (C) 2000 - 2011 Silverpeas
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  As a special exception to the terms and conditions of version 3.0 of
 *  the GPL, you may redistribute this Program in connection with Free/Libre
 *  Open Source Software ("FLOSS") applications as described in Silverpeas's
 *  FLOSS exception.  You should have recieved a copy of the text describing
 *  the FLOSS exception, and it is also available here:
 *  "http://www.silverpeas.org/legal/licensing"
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.kmelia.export;

import java.io.Writer;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import java.util.List;
import org.w3c.dom.NodeList;
import org.odftoolkit.simple.table.Row;
import com.silverpeas.comment.model.Comment;
import org.odftoolkit.simple.table.Table;
import com.silverpeas.converter.DocumentFormatConverterFactory;
import com.silverpeas.converter.ODTConverter;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.kmelia.model.KmeliaPublication;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import org.apache.commons.io.FilenameUtils;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.meta.Meta;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.text.Paragraph;
import org.odftoolkit.simple.text.Section;
import org.w3c.dom.Node;
import static com.stratelia.webactiv.util.DateUtil.*;
import static com.silverpeas.converter.DocumentFormat.*;
import static com.silverpeas.kmelia.export.DocumentTemplateParts.*;
import static com.silverpeas.kmelia.export.VersionedAttachmentHolder.*;
import static com.silverpeas.util.StringUtil.*;

/**
 * A builder of an ODT document based on a given template and from a specified
 * Kmelia publication.
 * @TODO translate the text in the doc to build from the template
 */
public class ODTDocumentBuilder {

  private static final String DOCUMENT_TEMPLATE = "kmelia.export.template";
  private static final ResourceLocator settings = new ResourceLocator(
          "com.stratelia.webactiv.kmelia.settings.kmeliaSettings", "");
  private UserDetail user;
  private String language = "";

  /**
   * Gets an instance of a builder of ODT documents.
   * @return an ODTDocumentBuilder instance.
   */
  public static ODTDocumentBuilder getODTDocumentBuilder() {
    return new ODTDocumentBuilder();
  }

  /**
   * Informs this builder the build is for the specified user. If not set, then the builds will be
   * performed for the publication creator.
   * Only information the user is authorized to access will be rendered into the ODT documents.
   * @param user the user for which the build of the documents should be done.
   * @return itself.
   */
  public ODTDocumentBuilder forUser(final UserDetail user) {
    this.user = user;
    return this;
  }

  /**
   * Informs this builder the prefered language in which the documents to build will be.
   * @param language the language in which the text should be displayed in the built documents.
   * @return itself.
   */
  public ODTDocumentBuilder inLanguage(String language) {
    this.language = (language == null? "":language);
    return this;
  }
  
  /**
   * A convenient method to improve the readability in the call of the method
   * buildFromPublication(). It can be uses as:
   * <code>File odt = builder.buildFrom(mypublication, anODTNamed("foo"));</code>
   * @param documentName
   * @return 
   */
  public static String anODTNamed(String documentName) {
    return documentName;
  }

  /**
   * Builds an ODT document from the specified Kmelia publication.
   * If an error occurs while building the document, a runtime exception DocumentBuildException is
   * thrown.
   * @param publication the publication from which an ODT document is built.
   * @param documentName the name of the ODT document to build.
   * @return the file corresponding to the ODT document built from the publication.
   */
  public File buildFrom(final KmeliaPublication publication, String documentName) {
    boolean isUserSet = true;
    try {
      String odtFileName = documentName;
      if (!isDefined(FilenameUtils.getExtension(documentName))) {
       odtFileName += ".odt"; 
      }
      TextDocument odtDocument = loadTemplate();
      if (getUser() == null) {
        forUser(publication.getCreator());
        isUserSet = false;
      }
      fill(odtDocument, with(publication));
      File odtFile = new File(FileRepositoryManager.getTemporaryPath() + odtFileName);
      Writer writer = null;
      odtDocument.save(odtFile);
      return odtFile;
    } catch (Exception ex) {
      throw new DocumentBuildException(ex.getMessage(), ex);
    } finally {
      if (!isUserSet) {
        forUser(null);
      }
    }
  }

  private static KmeliaPublication with(final KmeliaPublication publication) {
    return publication;
  }

  private static TextDocument in(final TextDocument document) {
    return document;
  }

  /**
   * Loads the template to use in the build of ODT documents.
   * @return an ODT document corresponding to the loaded template.
   * @throws Exception if the template loading fails.
   */
  private TextDocument loadTemplate() throws Exception {
    String exportTemplateDir = FileRepositoryManager.getExportTemplateRepository();
    String templateDoc = settings.getString(DOCUMENT_TEMPLATE);
    TextDocument template = TextDocument.loadDocument(new File(exportTemplateDir + templateDoc));
    return template;
  }

  private void fill(final TextDocument odtDocument, final KmeliaPublication publication) throws
          Exception {
    buildInfoSection(in(odtDocument), with(publication));
    buildAttachmentsSection(in(odtDocument), with(publication));
    buildCommentSection(in(odtDocument), with(publication));
    //buildWysiwygContentSection(in(odtDocument), new File(getClass().getResource("/" + WYSIWYG_CONTENT).toURI()));
  }

  private void buildInfoSection(final TextDocument odtDocument, final KmeliaPublication publication) {
    PublicationDetail detail = publication.getDetail();
    Meta metadata = odtDocument.getOfficeMetadata();
    metadata.setCreationDate(Calendar.getInstance());
    metadata.setCreator(detail.getCreatorName());
    metadata.setSubject(detail.getDescription(getLanguage()));
    metadata.setDcdate(Calendar.getInstance());
    metadata.setTitle(detail.getName(getLanguage()));
    metadata.setUserDefinedDataValue(FIELD_MODIFICATION_DATE,
            getOutputDateAndHour(detail.getUpdateDate(), getLanguage()));
    metadata.setUserDefinedDataValue(FIELD_AUTHOR, detail.getCreatorName());
  }

  private void buildCommentSection(final TextDocument odtDocument,
          final KmeliaPublication publication) {
    Table commentsTable = odtDocument.getTableByName(LIST_OF_COMMENTS);
    int i = 1;
    for (Comment comment : publication.getComments()) {
      Row row = commentsTable.getRowByIndex(i++);
      row.getCellByIndex(0).setStringValue(comment.getOwnerDetail().getDisplayedName());
      row.getCellByIndex(1).setStringValue(comment.getMessage());
      row.getCellByIndex(2).setStringValue(comment.getCreationDate());
      row.getCellByIndex(3).setStringValue(comment.getModificationDate());
    }
  }

  private void buildWysiwygContentSection(final TextDocument odtDocument, final File wysiwyg) throws
          Exception {
    Section content = odtDocument.getSectionByName(SECTION_CONTENT);
    Paragraph p = content.getParagraphByIndex(1, false);
    if (p != null) {
      content.removeParagraph(p);
    }
    ODTConverter converter = DocumentFormatConverterFactory.getFactory().getODTConverter();
    File wysiwygInOdt = converter.convert(wysiwyg, inFormat(odt));
    TextDocument odt = TextDocument.loadDocument(wysiwygInOdt);
    Node wysiwygNode = odtDocument.getContentDom().importNode(odt.getContentDom().getFirstChild(),
            true);
    Node textNode = wysiwygNode.getLastChild().getFirstChild();
    NodeList children = textNode.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      content.getOdfElement().appendChild(children.item(i).cloneNode(true));
    }
  }

  private void buildAttachmentsSection(final TextDocument odtDocument,
          final KmeliaPublication publication) {
    if (publication.isVersioned()) {
      buildWithVersionedAttachments(publication.getVersionedAttachments(), in(odtDocument));
    } else {
      buildWithAttachments(publication.getAttachments(), in(odtDocument));
    }
  }

  private void buildWithVersionedAttachments(final List<Document> versionedAttachments,
          final TextDocument odtDocument) {
    Table attachmentsTable = odtDocument.getTableByName(LIST_OF_ATTACHMENTS);
    updateTableForVersionedAttachments(attachmentsTable);
    int i = 1;
    for (Document versionedAttachment : versionedAttachments) {
      VersionedAttachmentHolder attachmentHolder = hold(versionedAttachment);
      if (attachmentHolder.isUserAuthorized(getUser())) {
        DocumentVersion lastVersion = attachmentHolder.getLastVersionAccessibleBy(getUser());
        if (lastVersion != null) {
          String creatorOrValidators = attachmentHolder.getCreatorOrValidatorsDisplayedName(
                  lastVersion);
          String version = attachmentHolder.getVersionNumber(lastVersion);
          String creationDate = dateToString(versionedAttachment.getLastCheckOutDate(),
                  getLanguage());

          Row row = attachmentsTable.getRowByIndex(i++);
          row.getCellByIndex(0).setStringValue(lastVersion.getLogicalName());
          row.getCellByIndex(1).setStringValue(versionedAttachment.getName());
          row.getCellByIndex(2).setStringValue(versionedAttachment.getDescription());
          row.getCellByIndex(3).setStringValue(version);
          row.getCellByIndex(4).setStringValue(creationDate);
          row.getCellByIndex(5).setStringValue(creatorOrValidators);
        }
      }
    }
  }

  private void buildWithAttachments(final List<AttachmentDetail> attachments,
          final TextDocument odtDocument) {
    Table attachmentsTable = odtDocument.getTableByName(LIST_OF_ATTACHMENTS);
    int i = 1;
    for (AttachmentDetail attachment : attachments) {
      Row row = attachmentsTable.getRowByIndex(i++);
      row.getCellByIndex(0).setStringValue(attachment.getLogicalName(getLanguage()));
      row.getCellByIndex(1).setStringValue(attachment.getTitle(getLanguage()));
      row.getCellByIndex(2).setStringValue(attachment.getInfo(getLanguage()));
      row.getCellByIndex(3).setStringValue(attachment.getAttachmentFileSize(getLanguage()));
      row.getCellByIndex(4).setStringValue(getOutputDate(attachment.getCreationDate(getLanguage()),
              getLanguage()));
    }
  }

  private void updateTableForVersionedAttachments(final Table attachmentsTable) {
    Cell defaultCell = attachmentsTable.getCellByPosition(0, 0);
    String cellStyle = defaultCell.getCellStyleName();
    String textStyle = defaultCell.getParagraphByIndex(0, true).getStyleName();

    attachmentsTable.getCellByPosition(3, 0).setStringValue("Version");
    Cell newCell = attachmentsTable.appendColumn().getCellByIndex(0);
    newCell.getOdfElement().setStyleName(cellStyle);
    newCell.addParagraph("Validateur").getOdfElement().setStyleName(textStyle);
  }

  private String getLanguage() {
    return this.language;
  }

  private UserDetail getUser() {
    return this.user;
  }

}
