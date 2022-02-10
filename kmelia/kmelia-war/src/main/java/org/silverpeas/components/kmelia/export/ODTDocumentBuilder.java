/*
 *  Copyright (C) 2000 - 2022 Silverpeas
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
 *  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.export;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.odftoolkit.odfdom.dom.element.text.TextAElement;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.meta.Meta;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;
import org.odftoolkit.simple.text.Paragraph;
import org.odftoolkit.simple.text.Section;
import org.odftoolkit.simple.text.list.ListItem;
import org.silverpeas.components.kmelia.model.KmeliaPublication;
import org.silverpeas.components.kmelia.service.KmeliaService;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.RenderingContext;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygContentTransformer;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.converter.DocumentFormatConverterProvider;
import org.silverpeas.core.contribution.converter.HTMLConverter;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.ClassifyValue;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.UnitUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.silverpeas.components.kmelia.export.DocumentTemplateParts.*;
import static org.silverpeas.components.kmelia.export.ODTDocumentTextTranslator.aTranslatorWith;
import static org.silverpeas.components.kmelia.export.ODTDocumentsMerging.atSection;
import static org.silverpeas.components.kmelia.export.ODTDocumentsMerging.decorates;
import static org.silverpeas.core.contribution.converter.DocumentFormat.inFormat;
import static org.silverpeas.core.contribution.converter.DocumentFormat.odt;
import static org.silverpeas.core.util.DateUtil.formatDate;
import static org.silverpeas.core.util.DateUtil.getOutputDate;
import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.util.StringUtil.isInteger;

/**
 * A builder of an ODT document based on a given template and from a specified Kmelia publication.
 */
public class ODTDocumentBuilder {

  private static final String DOCUMENT_TEMPLATE = "kmelia.export.template";
  private static final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.kmelia.settings.kmeliaSettings");
  private User user;
  private String language = "";
  private String topicIdToConsider;
  private LocalizationBundle messages;

  /**
   * Gets an instance of a builder of ODT documents.
   * @return an ODTDocumentBuilder instance.
   */
  public static ODTDocumentBuilder anODTDocumentBuilder() {
    return new ODTDocumentBuilder();
  }

  /**
   * Informs this builder the build is for the specified user. If not set, then the builds will be
   * performed for the publication creator. Only information the user is authorized to access will
   * be rendered into the ODT documents.
   * @param user the user for which the build of the documents should be done.
   * @return itself.
   */
  public ODTDocumentBuilder forUser(final User user) {
    this.user = user;
    return this;
  }

  /**
   * Informs this builder the prefered language to use for the content of the documents to build.
   * If
   * the publication doesn't have a content in the specified language, then it is the default
   * publication's text that will be taken (whatever the language in which it is written).
   * @param language the language in which the text should be displayed in the built documents.
   * @return itself.
   */
  public ODTDocumentBuilder inLanguage(String language) {
    this.language = (language == null ? "" : language);
    return this;
  }

  /**
   * Informs explicitly the topic to consider when building a document from publications. This
   * topic
   * can be provided by the caller itself as it was already computed for the publications to export
   * and according to the rights of the user on a such topic. If this topic isn't provided
   * explicitly, then it is computed directly from the publication and according to the rights of
   * the user on the topics the publication belongs to.
   * @param topicId the topic to explicitly consider.
   * @return itself.
   */
  public ODTDocumentBuilder inTopic(final String topicId) {
    this.topicIdToConsider = topicId;
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
   * Builds an ODT document at the specified path and from the specified Kmelia publication. If an
   * error occurs while building the document, a runtime exception DocumentBuildException is
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
    return TextDocument.loadDocument(new File(exportTemplateDir + templateDoc));
  }

  private void translate(final TextDocument odtDocument) {
    ODTDocumentTextTranslator translator = aTranslatorWith(getMessagesBundle());
    translator.translate(odtDocument);
  }

  private void fill(final TextDocument odtDocument, final KmeliaPublication publication)
      throws Exception {
    buildInfoSection(in(odtDocument), with(publication));
    buildContentSection(in(odtDocument), with(publication));
    buildAttachmentsSection(in(odtDocument), with(publication));
    buildSeeAlsoSection(in(odtDocument), with(publication));
    buildPdCSection(in(odtDocument), with(publication));
    buildCommentSection(in(odtDocument), with(publication));
  }

  private void buildInfoSection(final TextDocument odtDocument,
      final KmeliaPublication publication) {
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
        getOutputDate(detail.getLastUpdateDate(), getLanguage()));
    metadata.setUserDefinedDataValue(FIELD_AUTHOR, publication.getCreator().getDisplayedName());
    metadata.setUserDefinedDataValue(FIELD_LAST_MODIFIER, publication.getLastUpdater().
        getDisplayedName());
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
        row.getCellByIndex(0).setStringValue(comment.getCreator().getDisplayedName());
        row.getCellByIndex(1).setStringValue(comment.getMessage());
        row.getCellByIndex(2).setStringValue(formatDate(comment.getCreationDate()));
        row.getCellByIndex(3).setStringValue(formatDate(comment.getLastUpdateDate()));
      }
    }
  }

  private void buildContentSection(final TextDocument odtDocument,
      final KmeliaPublication publication) throws Exception {
    String htmlContent = WysiwygController.load(publication.getComponentInstanceId(),
        publication.getId(), getLanguage());
    if (isDefined(htmlContent)) {
      buildWithHTMLText(htmlContent, in(odtDocument));
    } else {
      buildWithXMLText(publication, in(odtDocument));
    }
  }

  private void buildWithHTMLText(String htmlText, final TextDocument odtDocument) throws Exception {
    Section content = odtDocument.getSectionByName(SECTION_CONTENT);
    if (isDefined(htmlText)) {
      String text = WysiwygContentTransformer.on(htmlText).resolveVariablesDirective().transform();
      String html = "<html><body>" + text + "</body></html>";
      Paragraph p = content.getParagraphByIndex(1, false);
      if (p != null) {
        content.removeParagraph(p);
      }
      File htmlFile = null;
      File odtConvertedHtmlFile = null;
      try {
        htmlFile = new File(
            FileRepositoryManager.getTemporaryPath() + UUID.randomUUID().toString() + ".html");
        FileUtils.writeStringToFile(htmlFile, html, Charsets.UTF_8);
        HTMLConverter converter = DocumentFormatConverterProvider.getHTMLConverter();
        odtConvertedHtmlFile = converter.convert(htmlFile, inFormat(odt));
        TextDocument htmlContent = TextDocument.loadDocument(odtConvertedHtmlFile);
        decorates(odtDocument).merge(htmlContent, atSection(SECTION_CONTENT));
      } finally {
        if (htmlFile != null) {
          FileUtils.deleteQuietly(htmlFile);
        }
        if (odtConvertedHtmlFile != null) {
          FileUtils.deleteQuietly(odtConvertedHtmlFile);
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
      PublicationTemplate template = PublicationTemplateManager.getInstance().
          getPublicationTemplate(publication.getComponentInstanceId() + ":" + templateId);
      Form viewForm = template.getViewForm();
      RecordSet recordSet = template.getRecordSet();
      DataRecord dataRecord = recordSet.getRecord(publication.getId(), getLanguage());
      if (dataRecord == null) {
        dataRecord = recordSet.getEmptyRecord();
        dataRecord.setId(publication.getId());
      }
      PagesContext context = new PagesContext();
      context.setRenderingContext(RenderingContext.EXPORT);
      context.setLanguage(getLanguage());
      context.setComponentId(publication.getComponentInstanceId());
      context.setObjectId(publication.getId());
      context.setBorderPrinted(false);
      context.setContentLanguage(getLanguage());
      context.setUserId(getUser().getId());
      context.setNodeId(getTopicIdOf(publication));
      String htmlText = viewForm.toString(context, dataRecord);
      if (isDefined(htmlText)) {
        //Suppress script tag
        htmlText = Pattern.compile("<script[^>]*>.*?</script>",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.DOTALL)
            .matcher(htmlText)
            .replaceAll("");
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
    List<SimpleDocument> attachments = AttachmentServiceProvider.getAttachmentService()
        .listDocumentsByForeignKey(publication.getIdentifier().toReference(), getLanguage());
    boolean hasNoAttachmentToDisplay = true;
    Table attachmentsTable = odtDocument.getTableByName(LIST_OF_ATTACHMENTS);
    int i = 1;
    for (SimpleDocument document : attachments) {
      SimpleDocument attachment = document.getLastPublicVersion();
      if (attachment != null) {
        hasNoAttachmentToDisplay = false;
        SimpleDocumentHolder attachmentHolder = SimpleDocumentHolder.hold(attachment);
        Row row = attachmentsTable.getRowByIndex(i++);
        row.getCellByIndex(0).setStringValue(attachment.getFilename());
        row.getCellByIndex(1).setStringValue(attachment.getTitle());
        row.getCellByIndex(2).setStringValue(attachment.getDescription());
        row.getCellByIndex(3).setStringValue(UnitUtil.formatMemSize(attachment.getSize()));
        row.getCellByIndex(4).setStringValue(attachmentHolder.getVersionNumber());
        row.getCellByIndex(5).setStringValue(attachmentHolder.getLastModification(getLanguage()));
        row.getCellByIndex(6).setStringValue(attachmentHolder.getAuthorFullName());
      }
    }
    if (hasNoAttachmentToDisplay) {
      Section attachmentsSection = odtDocument.getSectionByName(SECTION_ATTACHMENTS);
      attachmentsSection.remove();
    }
  }

  private void buildSeeAlsoSection(final TextDocument odtDocument,
      final KmeliaPublication publication) {
    try {
      Section seeAlso = odtDocument.getSectionByName(SECTION_SEEALSO);
      List<KmeliaPublication> linkedPublications =
          getKmeliaService().getLinkedPublications(publication, getUser().getId());
      if (linkedPublications.isEmpty()) {
        seeAlso.remove();
      } else {
        Paragraph p = seeAlso.getParagraphByIndex(1, false);
        if (p != null) {
          seeAlso.removeParagraph(p);
        }
        for (KmeliaPublication aLinkedPublication : linkedPublications) {
          PublicationDetail publicationDetail = aLinkedPublication.getDetail();
          if (publicationDetail.isValid() &&
              !publicationDetail.getId().equals(publication.getId())) {
            TextAElement hyperlink = new TextAElement(odtDocument.getContentDom());
            hyperlink.setXlinkHrefAttribute(aLinkedPublication.getURL());
            hyperlink.setXlinkTypeAttribute("simple");
            hyperlink.setTextContent(publicationDetail.getName(getLanguage()));
            org.odftoolkit.simple.text.list.List ul = odtDocument.addList();
            ListItem li = ul.addItem("");
            li.getOdfElement().getFirstChild().appendChild(hyperlink);
            seeAlso.getOdfElement().appendChild(ul.getOdfElement().cloneNode(true));
            ul.remove();
            seeAlso.addParagraph(aLinkedPublication.getLastUpdater().getDisplayedName() + " - " +
                getOutputDate(publicationDetail.getLastUpdateDate(), getLanguage()));
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
        rank = buildPosition(odtDocument, classification, pathMaxLength, pathNodeMinNb,
            pathSeparator, rank, aPosition);
      }
    }
  }

  private int buildPosition(final TextDocument odtDocument, final Section classification,
      final int pathMaxLength, final int pathNodeMinNb, final String pathSeparator, int rank,
      final ClassifyPosition aPosition) {
    classification.addParagraph("Position " + rank++);
    org.odftoolkit.simple.text.list.List ul = odtDocument.addList();
    for (ClassifyValue positionValue : aPosition.getValues()) {
      buildPositionValue(pathMaxLength, pathNodeMinNb, pathSeparator, ul, positionValue);
    }
    classification.getOdfElement().appendChild(ul.getOdfElement().cloneNode(true));
    ul.remove();
    return rank;
  }

  private void buildPositionValue(final int pathMaxLength, final int pathNodeMinNb,
      final String pathSeparator, final org.odftoolkit.simple.text.list.List ul,
      final ClassifyValue positionValue) {
    StringBuilder pathToRender = new StringBuilder();
    List<Value> pathNodes = positionValue.getFullPath();
    int pathLength = pathNodes.size();
    if (pathLength > pathMaxLength) {
      for (int i = 0; i < pathNodeMinNb; i++) {
        pathToRender.append(pathNodes.get(i).getName(getLanguage())).append(pathSeparator);
      }
      pathToRender.append("...").append(pathSeparator);
      for (int i = pathNodeMinNb; i > 0; i--) {
        pathToRender.append(pathNodes.get(pathLength - i).getName(getLanguage()))
            .append(pathSeparator);
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

  private String getTopicIdOf(final KmeliaPublication publication) {
    String theTopicId = this.topicIdToConsider;
    if (theTopicId == null) {
      NodePK pk = getKmeliaService().
          getBestLocationOfPublicationForUser(publication.getPk(), getUser().getId());
      theTopicId = pk.getId();
    }
    return theTopicId;
  }

  private String getLanguage() {
    return this.language;
  }

  private User getUser() {
    return this.user;
  }

  private LocalizationBundle getMessagesBundle() {
    if (this.messages == null) {
      this.messages =
          ResourceLocator.getLocalizationBundle("org.silverpeas.kmelia.multilang.kmeliaExport", getLanguage());
    }
    return this.messages;

  }

  /**
   * Gets the Kmelia service.
   * @return an instance of KmeliaService.
   */
  protected KmeliaService getKmeliaService() {
    return KmeliaService.get();
  }
}
