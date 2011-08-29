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

import com.silverpeas.comment.model.Comment;
import com.silverpeas.converter.DocumentFormatConverterFactory;
import com.silverpeas.converter.HTMLConverter;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.RenderingContext;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.ClassifyValue;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBmHome;
import com.stratelia.webactiv.kmelia.model.KmeliaPublication;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.kmelia.model.TopicDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import org.odftoolkit.odfdom.dom.element.text.TextAElement;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.meta.Meta;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;
import org.odftoolkit.simple.text.Paragraph;
import org.odftoolkit.simple.text.Section;
import org.odftoolkit.simple.text.list.ListItem;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static com.silverpeas.converter.DocumentFormat.inFormat;
import static com.silverpeas.converter.DocumentFormat.odt;
import static com.silverpeas.kmelia.export.DocumentTemplateParts.*;
import static com.silverpeas.kmelia.export.ODTDocumentTextTranslator.aTranslatorWith;
import static com.silverpeas.kmelia.export.ODTDocumentsMerging.atSection;
import static com.silverpeas.kmelia.export.ODTDocumentsMerging.decorates;
import static com.silverpeas.kmelia.export.VersionedAttachmentHolder.hold;
import static com.silverpeas.util.StringUtil.isDefined;
import static com.silverpeas.util.StringUtil.isInteger;
import static com.stratelia.webactiv.util.DateUtil.dateToString;
import static com.stratelia.webactiv.util.DateUtil.getOutputDate;

/**
 * A builder of an ODT document based on a given template and from a specified
 * Kmelia publication.
 */
public class ODTDocumentBuilder {

  private static final String DOCUMENT_TEMPLATE = "kmelia.export.template";
  private static final ResourceLocator settings = new ResourceLocator(
      "com.stratelia.webactiv.kmelia.settings.kmeliaSettings", "");
  private UserDetail user;
  private String language = "";
  private TopicDetail topicToConsider;
  private ResourceLocator messages;

  /**
   * Gets an instance of a builder of ODT documents.
   * @return an ODTDocumentBuilder instance.
   */
  public static ODTDocumentBuilder anODTDocumentBuilder() {
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
   * Informs this builder the prefered language to use for the content of the documents to build.
   * If the publication doesn't have a content in the specified language, then it is the default
   * publication's text that will be taken (whatever the language in which it is written).
   * @param language the language in which the text should be displayed in the built documents.
   * @return itself.
   */
  public ODTDocumentBuilder inLanguage(String language) {
    this.language = (language == null ? "" : language);
    return this;
  }

  /**
   * Informs explicitly the topic to consider when building a document from publications.
   * This topic can be provided by the caller itself as it was already computed for the publications
   * to export and according to the rights of the user on a such topic.
   * If this topic isn't provided explicitly, then it is computed directly from the publication and
   * according to the rights of the user on the topics the publication belongs to.
   * @param topic the topic to explicitly consider.
   * @return itself.
   */
  public ODTDocumentBuilder inTopic(final TopicDetail topic) {
    this.topicToConsider = topic;
    return this;
  }

  /**
   * A convenient method to improve the readability in the call of the method
   * buildFromPublication(). It can be uses as:
   * <code>File odt = builder.buildFrom(mypublication, anODTAt("/tmp/foo.odt"));</code>
   * @param documentPath the path of the document to build.
   * @return the document path as passed as parameter.
   */
  public static String anODTAt(String documentPath) {
    return documentPath;
  }

  /**
   * Builds an ODT document at the specified path and from the specified Kmelia publication.
   * If an error occurs while building the document, a runtime exception DocumentBuildException is
   * thrown.
   * @param publication the publication from which an ODT document is built.
   * @param documentPath the path of the ODT document to build.
   * @return the file corresponding to the ODT document built from the publication.
   */
  public File buildFrom(final KmeliaPublication publication, String documentPath) {
    boolean isUserSet = true;
    try {
      String odtFilePath = documentPath;
      String extension = FilenameUtils.getExtension(odtFilePath);
      if (!isDefined(extension) || !"odt".equalsIgnoreCase(extension)) {
        odtFilePath += ".odt";
      }
      TextDocument odtDocument = loadTemplate();
      if (getUser() == null) {
        forUser(publication.getCreator());
        isUserSet = false;
      }
      translate(odtDocument);
      fill(odtDocument, with(publication));
      File odtFile = new File(odtFilePath);
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

  private void translate(final TextDocument odtDocument) {
    ODTDocumentTextTranslator translator = aTranslatorWith(getMessagesBundle());
    translator.translate(odtDocument);
  }

  private void fill(final TextDocument odtDocument, final KmeliaPublication publication) throws
      Exception {
    buildInfoSection(in(odtDocument), with(publication));
    buildContentSection(in(odtDocument), with(publication));
    buildAttachmentsSection(in(odtDocument), with(publication));
    buildSeeAlsoSection(in(odtDocument), with(publication));
    buildPdCSection(in(odtDocument), with(publication));
    buildCommentSection(in(odtDocument), with(publication));
  }

  private void buildInfoSection(final TextDocument odtDocument, final KmeliaPublication publication) {
    PublicationDetail detail = publication.getDetail();
    Meta metadata = odtDocument.getOfficeMetadata();
    metadata.setCreator(detail.getCreatorName());
    metadata.setCreationDate(Calendar.getInstance());
    metadata.setSubject(detail.getDescription(getLanguage()));
    metadata.setDcdate(Calendar.getInstance());
    metadata.setTitle(detail.getName(getLanguage()));
    metadata.setUserDefinedDataValue(FIELD_CREATION_DATE,
        getOutputDate(detail.getCreationDate(), getLanguage()));
    metadata.setUserDefinedDataValue(FIELD_MODIFICATION_DATE,
        getOutputDate(detail.getUpdateDate(), getLanguage()));
    metadata.setUserDefinedDataValue(FIELD_AUTHOR, publication.getCreator().getDisplayedName());
    metadata.setUserDefinedDataValue(FIELD_LAST_MODIFIER, publication.getLastModifier().getDisplayedName());
    metadata.setUserDefinedDataValue(FIELD_URL, publication.getURL());
    metadata.setUserDefinedDataValue(FIELD_VERSION, detail.getVersion());
  }

  private void buildCommentSection(final TextDocument odtDocument,
      final KmeliaPublication publication) {
    List<Comment> comments = publication.getComments();
    if (comments.isEmpty()) {
      Section commentsSection = odtDocument.getSectionByName(SECTION_COMMENTS);
      commentsSection.remove();
    } else {
      Table commentsTable = odtDocument.getTableByName(LIST_OF_COMMENTS);
      int i = 1;
      for (Comment comment : comments) {
        Row row = commentsTable.getRowByIndex(i++);
        row.getCellByIndex(0).setStringValue(comment.getOwnerDetail().getDisplayedName());
        row.getCellByIndex(1).setStringValue(comment.getMessage());
        row.getCellByIndex(2).setStringValue(comment.getCreationDate());
        row.getCellByIndex(3).setStringValue(comment.getModificationDate());
      }
    }
  }

  private void buildContentSection(final TextDocument odtDocument,
      final KmeliaPublication publication) throws Exception {
    String htmlContent = WysiwygController.load(publication.getPk().getInstanceId(), publication.
        getPk().getId(), getLanguage());
    if (isDefined(htmlContent)) {
      buildWithHTMLText(htmlContent, in(odtDocument));
    } else {
      buildWithXMLText(publication, in(odtDocument));
    }
  }

  private void buildWithHTMLText(String htmlText, final TextDocument odtDocument) throws Exception {
    Section content = odtDocument.getSectionByName(SECTION_CONTENT);
    if (isDefined(htmlText)) {
      String html = "<html><body>" + htmlText + "</body></html>";
      Paragraph p = content.getParagraphByIndex(1, false);
      if (p != null) {
        content.removeParagraph(p);
      }
      File htmlFile = null, odtConvertedHtmlFile = null;
      try {
        htmlFile = new File(
            FileRepositoryManager.getTemporaryPath() + UUID.randomUUID().toString() + ".html");
        // warning: the content of HTML text is actually in ISO-8859-1!
        FileUtils.writeStringToFile(htmlFile, html, "ISO-8859-1");
        HTMLConverter converter = DocumentFormatConverterFactory.getFactory().getHTMLConverter();
        odtConvertedHtmlFile = converter.convert(htmlFile, inFormat(odt));
        TextDocument htmlContent = TextDocument.loadDocument(odtConvertedHtmlFile);
        decorates(odtDocument).merge(htmlContent, atSection(SECTION_CONTENT));
      } finally {
        if (htmlFile != null) {
          htmlFile.delete();
        }
        if (odtConvertedHtmlFile != null) {
          odtConvertedHtmlFile.delete();
        }
      }
    } else {
      content.remove();
    }
  }

  private void buildWithXMLText(final KmeliaPublication publication, final TextDocument odtDocument)
      throws Exception {
    boolean removeSection = true;
    String templateId = publication.getDetail().getInfoId();
    if (!isInteger(templateId)) {
      PublicationTemplate template = PublicationTemplateManager.getInstance().getPublicationTemplate(
          publication.getPk().getInstanceId() + ":" + templateId);
      Form viewForm = template.getViewForm();
      RecordSet recordSet = template.getRecordSet();
      DataRecord dataRecord = recordSet.getRecord(publication.getPk().getId(), getLanguage());
      if (dataRecord == null) {
        dataRecord = recordSet.getEmptyRecord();
        dataRecord.setId(publication.getPk().getId());
      }
      PagesContext context = new PagesContext();
      context.setRenderingContext(RenderingContext.EXPORT);
      context.setLanguage(getLanguage());
      context.setComponentId(publication.getPk().getInstanceId());
      context.setObjectId(publication.getPk().getId());
      context.setBorderPrinted(false);
      context.setContentLanguage(getLanguage());
      context.setUserId(getUser().getId());
      context.setNodeId(getTopicOf(publication).getNodeDetail().getNodePK().getId());
      String htmlText = viewForm.toString(context, dataRecord);
      if (isDefined(htmlText)) {
        buildWithHTMLText(htmlText, in(odtDocument));
        removeSection = false;
      }
    }
    if (removeSection) {
      Section contentSection = odtDocument.getSectionByName(SECTION_CONTENT);
      contentSection.remove();
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
    if (versionedAttachments.isEmpty()) {
      Section attachmentsSection = odtDocument.getSectionByName(SECTION_ATTACHMENTS);
      attachmentsSection.remove();
    } else {
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
  }

  private void buildWithAttachments(final List<AttachmentDetail> attachments,
      final TextDocument odtDocument) {
    if (attachments.isEmpty()) {
      Section attachmentsSection = odtDocument.getSectionByName(SECTION_ATTACHMENTS);
      attachmentsSection.remove();
    } else {
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
  }

  private void buildSeeAlsoSection(final TextDocument odtDocument,
      final KmeliaPublication publication) {
    try {
      Section seeAlso = odtDocument.getSectionByName(SECTION_SEEALSO);
      List<KmeliaPublication> linkedPublications = getKmeliaService().getLinkedPublications(
          publication, getUser().getId());
      if (linkedPublications.isEmpty()) {
        seeAlso.remove();
      } else {
        Paragraph p = seeAlso.getParagraphByIndex(1, false);
        if (p != null) {
          seeAlso.removeParagraph(p);
        }
        for (KmeliaPublication aLinkedPublication : linkedPublications) {
          PublicationDetail publicationDetail = aLinkedPublication.getDetail();
          if (publicationDetail.getStatus() != null
              && "Valid".equals(publicationDetail.getStatus())
              && !publicationDetail.getPK().getId().equals(publication.getPk().getId())) {
            TextAElement hyperlink = new TextAElement(odtDocument.getContentDom());
            hyperlink.setXlinkHrefAttribute(aLinkedPublication.getURL());
            hyperlink.setXlinkTypeAttribute("simple");
            hyperlink.setTextContent(publicationDetail.getName(getLanguage()));
            org.odftoolkit.simple.text.list.List ul = odtDocument.addList();
            ListItem li = ul.addItem("");
            li.getOdfElement().getFirstChild().appendChild(hyperlink);
            seeAlso.getOdfElement().appendChild(ul.getOdfElement().cloneNode(true));
            ul.remove();
            seeAlso.addParagraph(aLinkedPublication.getLastModifier().getDisplayedName()
                + " - " + getOutputDate(publicationDetail.getUpdateDate(), getLanguage()));
            seeAlso.addParagraph(publicationDetail.getDescription(getLanguage()));
          }
        }
      }
    } catch (Exception ex) {
      throw new DocumentBuildException(ex.getMessage(), ex);
    }
  }

  private void buildPdCSection(TextDocument odtDocument, KmeliaPublication publication) {
    Section classification = odtDocument.getSectionByName(SECTION_CLASSIFICATION);
    List<ClassifyPosition> positions = publication.getPDCPositions();
    int pathMaxLength = 5;
    int pathNodeMinNb = 2;
    String pathSeparator = "/";
    int rank = 1;
    if (positions.isEmpty()) {
      classification.remove();
    } else {
      Paragraph p = classification.getParagraphByIndex(1, false);
      if (p != null) {
        classification.removeParagraph(p);
      }
      for (ClassifyPosition aPosition : positions) {
        classification.addParagraph("Position " + rank++);
        org.odftoolkit.simple.text.list.List ul = odtDocument.addList();
        for (ClassifyValue positionValue : aPosition.getValues()) {
          StringBuilder pathToRender = new StringBuilder();
          List<Value> pathNodes = positionValue.getFullPath();
          int pathLength = pathNodes.size();
          if (pathLength > pathMaxLength) {
            for (int i = 0; i < pathNodeMinNb; i++) {
              pathToRender.append(pathNodes.get(i).getName(getLanguage())).append(pathSeparator);
            }
            pathToRender.append("...").append(pathSeparator);
            for (int i = pathNodeMinNb; i > 0; i--) {
              pathToRender.append(pathNodes.get(pathLength - i).getName(getLanguage())).append(
                  pathSeparator);
            }
          } else {
            for (Value pathNode : pathNodes) {
              pathToRender.append(pathNode.getName(getLanguage())).append(pathSeparator);
            }
          }
          if (!pathSeparator.equals(pathToRender.toString()) && pathToRender.length() > 0) {
            ul.addItem(pathToRender.substring(0, pathToRender.length() - pathSeparator.length()));
          }
        }
        classification.getOdfElement().appendChild(ul.getOdfElement().cloneNode(true));
        ul.remove();
      }
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

  private boolean isRightsOnTopicsEnabled(final String componentInstanceId) {
    return Boolean.valueOf(getOrganizationService().getComponentParameterValue(componentInstanceId,
        "rightsOnTopics"));
  }

  private boolean isTree(final String componentInstanceId) {
    String isTree = getOrganizationService().getComponentParameterValue(componentInstanceId,
        "istree");
    if (!isDefined(isTree)) {
      return true;
    }
    return "0".equals(isTree) || "1".equals(isTree);
  }

  private TopicDetail getTopicOf(final KmeliaPublication publication) throws Exception {
    TopicDetail theTopic = this.topicToConsider;
    if (theTopic == null) {
      String componentId = publication.getPk().getInstanceId();
      theTopic = getKmeliaService().getPublicationFather(publication.getPk(), isTree(componentId),
          getUser().getId(), isRightsOnTopicsEnabled(componentId));
    }
    return theTopic;
  }

  private String getLanguage() {
    return this.language;
  }

  private UserDetail getUser() {
    return this.user;
  }

  private ResourceLocator getMessagesBundle() {
    if (this.messages == null) {
      this.messages = new ResourceLocator(
          "com.stratelia.webactiv.kmelia.multilang.kmeliaExport", getLanguage());
    }
    return this.messages;
  }

  /**
   * Gets the Kmelia service.
   * @return an instance of KmeliaBm.
   */
  protected KmeliaBm getKmeliaService() {
    try {
      KmeliaBmHome kscEjbHome =
          EJBUtilitaire.getEJBObjectRef(JNDINames.KMELIABM_EJBHOME,
          KmeliaBmHome.class);
      return kscEjbHome.create();
    } catch (Exception e) {
      throw new KmeliaRuntimeException(getClass().getSimpleName() + ".getKmeliaService()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * Gets the organization controller.
   * @return an instance of OrganizationController.
   */
  protected OrganizationController getOrganizationService() {
    return new OrganizationController();
  }
}
