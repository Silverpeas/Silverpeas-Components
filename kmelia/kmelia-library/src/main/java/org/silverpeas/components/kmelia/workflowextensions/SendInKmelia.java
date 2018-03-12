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
package org.silverpeas.components.kmelia.workflowextensions;

import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import net.htmlparser.jericho.Source;
import org.silverpeas.components.kmelia.service.KmeliaService;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.attachment.AttachmentException;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.content.form.*;
import org.silverpeas.core.contribution.content.form.displayers.WysiwygFCKFieldDisplayer;
import org.silverpeas.core.contribution.content.form.field.ExplorerField;
import org.silverpeas.core.contribution.content.form.field.FileField;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateImpl;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.instance.HistoryStep;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.instance.UpdatableProcessInstance;
import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.Parameter;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.external.impl.ExternalActionImpl;

import javax.inject.Named;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named("SendInKmeliaHandler")
public class SendInKmelia extends ExternalActionImpl {

  private static final String UNKNOWN = "unknown";
  private String targetId = UNKNOWN;
  private String topicId = UNKNOWN;
  private String pubDesc = null;
  private String role = UNKNOWN;
  private String xmlFormName = null;
  private boolean addPDFHistory = true;
  // Add pdf history before instance attachments
  private boolean addPDFHistoryFirst = true;
  private String pdfHistoryName = null;
  private String userId = null;
  private static final String ADMIN_ID = "0";

  @Override
  public void execute() {
    setRole(getEvent().getUserRoleName());

    Parameter parameter = getTriggerParameter("explorerFieldName");
    if (parameter != null && StringUtil.isDefined(parameter.getValue())) {
      String explorerFieldName = parameter.getValue();
      // getting place to create publication from explorer field
      try {
        ExplorerField explorer = (ExplorerField) getProcessInstance().getField(explorerFieldName);
        ForeignPK pk = (ForeignPK) explorer.getObjectValue();
        targetId = pk.getInstanceId();
        topicId = pk.getId();
      } catch (WorkflowException e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }
    } else {
      targetId = getTriggerParameter("targetComponentId").getValue();
      Parameter paramTopicPath = getTriggerParameter("targetFolderPath");
      if (paramTopicPath != null && StringUtil.isDefined(paramTopicPath.getValue())) {
        try {
          String path = DataRecordUtil.applySubstitution(paramTopicPath.getValue(),
              getProcessInstance().getAllDataRecord(role, "fr"), "fr");
          topicId = getNodeId(path);
        } catch (WorkflowException e) {
          SilverLogger.getLogger(this).error(e.getMessage(), e);
          topicId = "0";
        }
      } else {
        topicId = getTriggerParameter("targetTopicId").getValue();
      }
    }
    final String pubTitle = getTriggerParameter("pubTitle").getValue();
    Parameter paramDescription = getTriggerParameter("pubDescription");
    if (paramDescription != null && StringUtil.isDefined(paramDescription.getValue())) {
      pubDesc = paramDescription.getValue();
    }
    if (getTriggerParameter("xmlFormName") != null) {
      xmlFormName = getTriggerParameter("xmlFormName").getValue();
      if (StringUtil.isDefined(xmlFormName) && xmlFormName.lastIndexOf(".xml") != -1) {
        xmlFormName = xmlFormName.substring(0, xmlFormName.lastIndexOf(".xml"));
      }
    }
    boolean formIsUsed = StringUtil.isDefined(xmlFormName);
    if (getTriggerParameter("addPDFHistory") != null) {
      addPDFHistory = StringUtil.getBooleanValue(getTriggerParameter("addPDFHistory").getValue());
      if (getTriggerParameter("addPDFHistoryFirst") != null) {
        addPDFHistoryFirst = StringUtil.getBooleanValue(getTriggerParameter("addPDFHistoryFirst").getValue());
      }
      Parameter paramPDFName = getTriggerParameter("pdfHistoryName");
      if (paramPDFName != null) {
        pdfHistoryName = getTriggerParameter("pdfHistoryName").getValue();
      }
    }

    // 1 - Create publication
    PublicationPK pubPK = new PublicationPK("0", getTargetId());
    Date now = new Date();
    String pubName = getProcessInstance().getTitle(getRole(), getLanguage());
    if (StringUtil.isDefined(pubTitle)) {
      try {
        pubName = DataRecordUtil.applySubstitution(pubTitle, getProcessInstance().getAllDataRecord(role, "fr"), "fr");
      } catch (WorkflowException e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }
    }
    String desc = "";
    if (StringUtil.isDefined(pubDesc)) {
      try {
        desc = DataRecordUtil.applySubstitution(pubDesc, getProcessInstance().getAllDataRecord(role, "fr"), "fr");
      } catch (WorkflowException e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }
    }
    userId = getBestUserDetail().getId();
    PublicationDetail pubDetail = new PublicationDetail(pubPK, pubName, desc, now, now, null, userId, 1, null, null, null);

    if (formIsUsed) {
      pubDetail.setInfoId(xmlFormName);
    }

    KmeliaService kmelia = getKmeliaService();
    NodePK nodePK = new NodePK(getTopicId(), getTargetId());
    String pubId = kmelia.createPublicationIntoTopic(pubDetail, nodePK);
    pubPK.setId(pubId);

    // 2 - Attach history as pdf file
    if (addPDFHistory && addPDFHistoryFirst) {
      addPdfHistory(pubPK, userId);
    }

    // 3 - Copy all instance regular files to publication
    ForeignPK fromPK = new ForeignPK(getProcessInstance().getInstanceId(), getProcessInstance().getModelId());
    ForeignPK toPK = new ForeignPK(pubPK);
    copyFiles(fromPK, toPK, DocumentType.attachment, DocumentType.attachment);

    if (addPDFHistory && !addPDFHistoryFirst) {
      addPdfHistory(pubPK, userId);
    }

    // process form content
    if (formIsUsed) {
      // target app use form : populate form fields
      populateFields(pubId, fromPK, toPK);
    } else {
      // target app do not use form : copy files of worflow folder
      copyFiles(fromPK, toPK, DocumentType.form, DocumentType.attachment);
    }

    Parameter draftOutParameter = getTriggerParameter("forceDraftOut");
    if (draftOutParameter != null && StringUtil.getBooleanValue(draftOutParameter.getValue())) {
      getKmeliaService().draftOutPublication(pubPK, nodePK, SilverpeasRole.admin.toString());
    }
  }

  public void populateFields(String pubId, ForeignPK fromPK, ForeignPK toPK) {
    // Get the current instance
    UpdatableProcessInstance currentProcessInstance = (UpdatableProcessInstance) getProcessInstance();
    try {
      // register xmlForm of publication
      PublicationTemplateManager.getInstance().addDynamicPublicationTemplate(targetId + ":" + xmlFormName, xmlFormName + ".xml");

      PublicationTemplateImpl pubTemplate =
          (PublicationTemplateImpl) PublicationTemplateManager.getInstance().getPublicationTemplate(targetId + ":" + xmlFormName);
      DataRecord record = pubTemplate.getRecordSet().getEmptyRecord();
      record.setId(pubId);
      for (String fieldName : record.getFieldNames()) {
        Object fieldValue =
            getFieldValue(fromPK, toPK, currentProcessInstance, pubTemplate, fieldName);
        record.getField(fieldName).setObjectValue(fieldValue);
      }
      // Update
      pubTemplate.getRecordSet().save(record);

    } catch (PublicationTemplateException | FormException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
  }

  private Object getFieldValue(final ForeignPK fromPK, final ForeignPK toPK,
      final UpdatableProcessInstance currentProcessInstance,
      final PublicationTemplateImpl pubTemplate, final String fieldName)
      throws FormException, PublicationTemplateException {
    Object fieldValue = null;
    try {
      Field fieldOfFolder = currentProcessInstance.getField(fieldName);
      FieldTemplate fieldTemplate = pubTemplate.getRecordTemplate().getFieldTemplate(fieldName);
      fieldValue = fieldOfFolder.getObjectValue();
      // Check file attachment in order to put them inside form
      if (fieldOfFolder instanceof FileField) {

        fieldValue = copyFormFile(fromPK, toPK, ((FileField) fieldOfFolder).getAttachmentId());
      } else if ("wysiwyg".equals(fieldTemplate.getDisplayerName())) {
        WysiwygFCKFieldDisplayer displayer = new WysiwygFCKFieldDisplayer();
        fieldValue = displayer.duplicateContent(fieldOfFolder, fieldTemplate, fromPK, toPK,
            I18NHelper.defaultLanguage);
      }
    } catch (WorkflowException e) {
      SilverLogger.getLogger(this).warn(e);
    }
    return fieldValue;
  }

  private String copyFormFile(ForeignPK fromPK, ForeignPK toPK, String attachmentId) {
    SimpleDocument attachment;
    if (StringUtil.isDefined(attachmentId)) {
      AttachmentService service = AttachmentServiceProvider.getAttachmentService();
      // Retrieve attachment detail to copy
      attachment = service.searchDocumentById(new SimpleDocumentPK(attachmentId, fromPK.getInstanceId()), null);
      if (attachment != null) {
        SimpleDocumentPK copyPK = copyFileWithoutDocumentTypeChange(attachment, toPK);
        return copyPK.getId();
      }
    }
    return null;
  }

  private Map<String, String> copyFiles(ForeignPK fromPK, ForeignPK toPK, DocumentType fromType,
      DocumentType toType) {
    Map<String, String> fileIds = new HashMap<>();
    try {
      List<SimpleDocument> origins = AttachmentServiceProvider.getAttachmentService().
          listDocumentsByForeignKeyAndType(fromPK, fromType, getLanguage());
      for (SimpleDocument origin : origins) {
        SimpleDocumentPK copyPk = copyFile(origin, toPK, toType);
        fileIds.put(origin.getId(), copyPk.getId());
      }

    } catch (AttachmentException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return fileIds;
  }

  private SimpleDocumentPK copyFileWithoutDocumentTypeChange(SimpleDocument file, ForeignPK toPK) {
    return copyFile(file, toPK, null);
  }

  private SimpleDocumentPK copyFile(SimpleDocument file, ForeignPK toPK, DocumentType type) {
    if (type != null) {
      file.setDocumentType(type);
    }
    return AttachmentServiceProvider.getAttachmentService().copyDocument(file, toPK);
  }

  private byte[] generatePDF(ProcessInstance instance) {
    com.lowagie.text.Document document = new com.lowagie.text.Document();

    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PdfWriter.getInstance(document, baos);
      document.open();

      HistoryStep[] steps = instance.getHistorySteps();
      for (HistoryStep historyStep : steps) {
        generatePDFStep(historyStep, document);
      }

      document.close();

      return baos.toByteArray();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return new byte[0];
  }

  private void generatePDFStep(HistoryStep step, com.lowagie.text.Document document) {
    if (step != null) {
      generatePDFStepHeader(step, document);
      generatePDFStepContent(step, document);
    }
  }

  private void generatePDFStepHeader(HistoryStep step, com.lowagie.text.Document document) {
    try {
      String activity = "";
      if (step.getResolvedState() != null) {
        State resolvedState =
            step.getProcessInstance().getProcessModel().getState(step.getResolvedState());
        activity = resolvedState.getLabel(getRole(), getLanguage());
      }

      String sAction = getAction(step);

      String actor = step.getUser().getFullName();

      String date = DateUtil.getOutputDateAndHour(step.getActionDate(), getLanguage());

      String header = "";
      if (StringUtil.isDefined(activity)) {
        header += activity + " - ";
      }
      header += sAction + " (" + actor + " - " + date + ")";

      Font fontHeader = new Font(Font.HELVETICA, 12, Font.NORMAL);

      PdfPCell pCell = new PdfPCell(new Phrase(header, fontHeader));
      pCell.setFixedHeight(28);
      pCell.setBackgroundColor(new Color(239, 239, 239));
      pCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

      PdfPTable pTable = new PdfPTable(1);
      pTable.setWidthPercentage(100);
      pTable.addCell(pCell);

      document.add(pTable);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
  }

  private String getAction(final HistoryStep step) {
    try {
      final String sAction;
      if ("#question#".equals(step.getAction())) {
        sAction = getString("processManager.question");
      } else if ("#response#".equals(step.getAction())) {
        sAction = getString("processManager.response");
      } else if ("#reAssign#".equals(step.getAction())) {
        sAction = getString("processManager.reAffectation");
      } else {
        Action action = step.getProcessInstance().getProcessModel().getAction(step.getAction());
        sAction = action.getLabel(getRole(), getLanguage());
      }
      return sAction;
    } catch (WorkflowException we) {
      return "##";
    }
  }

  private void generatePDFStepContent(HistoryStep step, com.lowagie.text.Document document) {
    try {
      Form form;
      if ("#question#".equals(step.getAction()) || "#response#".equals(step.getAction())) {
        // TODO
        form = null;
      } else {
        form = getProcessInstance().getProcessModel().getPresentationForm(step.getAction(), getRole(), getLanguage());
      }

      if (form != null && step.getActionRecord() != null) {
        DataRecord data = step.getActionRecord();
        PagesContext pageContext = new PagesContext();
        pageContext.setLanguage(getLanguage());

        // Force simpletext displayers because itext cannot display HTML Form fields (select,
        // radio...)
        float[] colsWidth = {25, 75};
        PdfPTable tableContent = new PdfPTable(colsWidth);
        tableContent.setWidthPercentage(100);
        String fieldValue = "";
        Font fontLabel = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font fontValue = new Font(Font.HELVETICA, 10, Font.NORMAL);
        List<FieldTemplate> fieldTemplates = form.getFieldTemplates();
        for (FieldTemplate fieldTemplate1 : fieldTemplates) {
          try {
            GenericFieldTemplate fieldTemplate = (GenericFieldTemplate) fieldTemplate1;

            String fieldLabel = fieldTemplate.getLabel("fr");
            Field field = data.getField(fieldTemplate.getFieldName());
            String componentId = step.getProcessInstance().getProcessModel().getModelId();

            // wysiwyg field
            if ("wysiwyg".equals(fieldTemplate.getDisplayerName())) {
              String file = WysiwygFCKFieldDisplayer
                  .getFile(componentId, getProcessInstance().getInstanceId(), fieldTemplate.getFieldName(), getLanguage());

              // Extract the text content of the html code
              Source source = new Source(new FileInputStream(file));
              fieldValue = source.getTextExtractor().toString();
            } // Field file type
            else if (FileField.TYPE.equals(fieldTemplate.getDisplayerName()) && StringUtil.
                isDefined(field.getValue())) {
              SimpleDocument doc = AttachmentServiceProvider.getAttachmentService().
                  searchDocumentById(new SimpleDocumentPK(field.getValue(), componentId), null);
              if (doc != null) {
                fieldValue = doc.getFilename();
              }
            } else {
              // Other field types
              FieldDisplayer fieldDisplayer = TypeManager.getInstance().getDisplayer(fieldTemplate.getTypeName(), "simpletext");
              StringWriter sw = new StringWriter();
              PrintWriter out = new PrintWriter(sw);
              fieldDisplayer.display(out, field, fieldTemplate, pageContext);
              fieldValue = sw.toString();
            }

            boolean displayField = true;
            if (!Util.isEmptyFieldsDisplayed() && !StringUtil.isDefined(fieldValue)) {
              displayField = false;
            }

            if (displayField) {
              PdfPCell cell = new PdfPCell(new Phrase(fieldLabel, fontLabel));
              cell.setBorderWidth(0);
              cell.setPaddingBottom(5);
              tableContent.addCell(cell);

              cell = new PdfPCell(new Phrase(fieldValue, fontValue));
              cell.setBorderWidth(0);
              cell.setPaddingBottom(5);
              tableContent.addCell(cell);
            }
          } catch (Exception e) {
            SilverLogger.getLogger(this).error(e.getMessage(), e);
          }
        }
        document.add(tableContent);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
  }

  private OrganizationController getOrganizationController() {
    return OrganizationControllerProvider.getOrganisationController();
  }

  private String getString(String key) {
    return key;
  }

  private String getTargetId() {
    return targetId;
  }

  private String getTopicId() {
    return topicId;
  }

  private String getLanguage() {
    return "fr";
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  private KmeliaService getKmeliaService() {
    return ServiceProvider.getService(KmeliaService.class);
  }

  /**
   * Get actor if exist, admin otherwise
   * @return UserDetail
   */
  private UserDetail getBestUserDetail() {
    String currentUserId = ADMIN_ID;
    // For a manual action (event)
    if (getEvent().getUser() != null) {
      currentUserId = getEvent().getUser().getUserId();
    }
    return getOrganizationController().getUserDetail(currentUserId);
  }

  private void addPdfHistory(PublicationPK pubPK, String userId) {
    String fileName = "processHistory_" + getProcessInstance().getInstanceId() + ".pdf";
    if (StringUtil.isDefined(pdfHistoryName)) {
      fileName = pdfHistoryName;
      if (!fileName.endsWith(".pdf")) {
        fileName += ".pdf";
      }
    }
    byte[] pdf = generatePDF(getProcessInstance());
    getKmeliaService().addAttachmentToPublication(pubPK, userId, fileName, "", pdf);
  }

  private String getNodeId(String explicitPath) {
    String[] path = explicitPath.substring(1).split("/");
    NodePK nodePK = new NodePK(UNKNOWN, targetId);
    String parentId = NodePK.ROOT_NODE_ID;
    for (String name : path) {
      NodeDetail existingNode = null;
      try {
        existingNode = getNodeBm().getDetailByNameAndFatherId(nodePK, name, Integer.parseInt(parentId));
      } catch (Exception e) {
        SilverLogger.getLogger(this).warn("Node named {0} in path {1} doesn't exist", name,
            explicitPath);
      }
      if (existingNode != null) {
        // topic exists
        parentId = existingNode.getNodePK().getId();
      } else {
        // topic does not exists, creating it
        NodeDetail newNode = new NodeDetail();
        newNode.setName(name);
        newNode.setNodePK(new NodePK(UNKNOWN, targetId));
        newNode.setFatherPK(new NodePK(parentId, targetId));
        newNode.setCreatorId(userId);
        NodePK newNodePK;
        try {
          newNodePK = getNodeBm().createNode(newNode);
        } catch (Exception e) {
          SilverLogger.getLogger(this).error("Cannot create node {0} in path {1}", new String[] {name, explicitPath}, e);
          return "-1";
        }
        parentId = newNodePK.getId();
      }
    }
    return parentId;
  }

  protected NodeService getNodeBm() {
    return ServiceProvider.getService(NodeService.class);
  }
}