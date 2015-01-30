/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
package com.silverpeas.kmelia.workflowextensions;

import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.DataRecordUtil;
import com.silverpeas.form.Field;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.displayers.WysiwygFCKFieldDisplayer;
import com.silverpeas.form.fieldType.ExplorerField;
import com.silverpeas.form.fieldType.FileField;
import com.silverpeas.form.form.XmlForm;
import com.silverpeas.form.record.GenericFieldTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.instance.HistoryStep;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.instance.UpdatableProcessInstance;
import com.silverpeas.workflow.api.model.Action;
import com.silverpeas.workflow.api.model.Parameter;
import com.silverpeas.workflow.api.model.State;
import com.silverpeas.workflow.external.impl.ExternalActionImpl;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.node.control.NodeService;
import com.stratelia.webactiv.node.model.NodeDetail;
import com.stratelia.webactiv.node.model.NodePK;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import com.stratelia.webactiv.publication.model.PublicationPK;
import net.htmlparser.jericho.Source;
import org.silverpeas.attachment.AttachmentException;
import org.silverpeas.attachment.AttachmentService;
import org.silverpeas.attachment.AttachmentServiceProvider;
import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.admin.OrganizationController;
import org.silverpeas.core.admin.OrganizationControllerProvider;
import org.silverpeas.util.DateUtil;
import org.silverpeas.util.ForeignPK;
import org.silverpeas.util.ServiceProvider;
import org.silverpeas.util.StringUtil;
import org.silverpeas.util.exception.SilverpeasRuntimeException;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendInKmelia extends ExternalActionImpl {

  private String targetId = "unknown";
  private String topicId = "unknown";
  private String pubDesc = null;
  private String role = "unknown";
  private String xmlFormName = null;
  private boolean addPDFHistory = true;
  // Add pdf history before instance attachments
  private boolean addPDFHistoryFirst = true;
  private String pdfHistoryName = null;
  private String userId = null;
  private static final String ADMIN_ID = "0";

  public SendInKmelia() {
  }

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
      } catch (WorkflowException e1) {
        SilverTrace.error("processManager", "SendInKmelia.execute", "err.CANT_GET_TOPIC", e1);
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
          SilverTrace.error("workflowEngine", "SendInKmelia.execute()", "root.MSG_GEN_ERROR", e);
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
        addPDFHistoryFirst =
            StringUtil.getBooleanValue(getTriggerParameter("addPDFHistoryFirst").getValue());
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
        pubName = DataRecordUtil
            .applySubstitution(pubTitle, getProcessInstance().getAllDataRecord(role, "fr"), "fr");
      } catch (WorkflowException e) {
        SilverTrace.error("workflowEngine", "SendInKmelia.execute()", "root.MSG_GEN_ERROR", e);
      }
    }
    String desc = "";
    if (StringUtil.isDefined(pubDesc)) {
      try {
        desc = DataRecordUtil
            .applySubstitution(pubDesc, getProcessInstance().getAllDataRecord(role, "fr"), "fr");
      } catch (WorkflowException e) {
        SilverTrace.error("workflowEngine", "SendInKmelia.execute()", "root.MSG_GEN_ERROR", e);
      }
    }
    userId = getBestUserDetail().getId();
    PublicationDetail pubDetail =
        new PublicationDetail(pubPK, pubName, desc, now, now, null, userId, 1, null, null, null);

    if (formIsUsed) {
      pubDetail.setInfoId(xmlFormName);
    }

    KmeliaBm kmelia = getKmeliaBm();
    String pubId =
        kmelia.createPublicationIntoTopic(pubDetail, new NodePK(getTopicId(), getTargetId()));
    pubPK.setId(pubId);

    // 2 - Attach history as pdf file
    if (addPDFHistory && addPDFHistoryFirst) {
      addPdfHistory(pubPK, userId);
    }

    // 3 - Copy all instance regular files to publication
    ForeignPK fromPK =
        new ForeignPK(getProcessInstance().getInstanceId(), getProcessInstance().getModelId());
    ForeignPK toPK = new ForeignPK(pubPK);
    copyFiles(fromPK, toPK, DocumentType.attachment, DocumentType.attachment);

    if (addPDFHistory && !addPDFHistoryFirst) {
      addPdfHistory(pubPK, userId);
    }

    // force the update
    PublicationDetail newPubli = getKmeliaBm().getPublicationDetail(pubPK);
    newPubli.setStatusMustBeChecked(false);
    getKmeliaBm().updatePublication(newPubli);

    // process form content
    if (formIsUsed) {
      // target app use form : populate form fields
      populateFields(pubId, fromPK, toPK);
    } else {
      // target app do not use form : copy files of worflow folder
      copyFiles(fromPK, toPK, DocumentType.form, DocumentType.attachment);
    }
  }

  public void populateFields(String pubId, ForeignPK fromPK, ForeignPK toPK) {
    // Get the current instance
    UpdatableProcessInstance currentProcessInstance =
        (UpdatableProcessInstance) getProcessInstance();
    try {
      // register xmlForm of publication
      PublicationTemplateManager.getInstance()
          .addDynamicPublicationTemplate(targetId + ":" + xmlFormName, xmlFormName + ".xml");

      PublicationTemplateImpl pubTemplate =
          (PublicationTemplateImpl) PublicationTemplateManager.getInstance()
              .getPublicationTemplate(targetId + ":" + xmlFormName);
      DataRecord record = pubTemplate.getRecordSet().getEmptyRecord();
      record.setId(pubId);
      for (String fieldName : record.getFieldNames()) {
        SilverTrace.debug("workflowEngine", "SendInKmelia.populateFields",
            "Process fieldName =" + fieldName);
        Object fieldValue = null;
        try {
          Field fieldOfFolder = currentProcessInstance.getField(fieldName);
          fieldValue = fieldOfFolder.getObjectValue();
          // Check file attachment in order to put them inside form
          if (fieldOfFolder instanceof FileField) {
            SilverTrace.info("workflowEngine", "SendInKmelia.populateFields", "Process file copy");
            fieldValue = copyFormFile(fromPK, toPK, ((FileField) fieldOfFolder).getAttachmentId());
          }
        } catch (WorkflowException e) {
          SilverTrace
              .debug("workflowEngine", "SendInKmelia.populateFields", "fill fieldname=" + fieldName,
                  e);
        }
        SilverTrace.debug("workflowEngine", "SendInKmelia.populateFields", "fill fieldname=" +
            fieldName + " with value " + fieldValue);
        record.getField(fieldName).setObjectValue(fieldValue);
      }
      // Update
      pubTemplate.getRecordSet().save(record);

    } catch (PublicationTemplateException | FormException e) {
      SilverTrace.error("workflowEngine", "SendInKmelia.populateFields()",
          "workflowEngine.CANNOT_UPDATE_PUBLICATION", e);
    }
  }

  private String copyFormFile(ForeignPK fromPK, ForeignPK toPK, String attachmentId) {
    SimpleDocument attachment;
    if (StringUtil.isDefined(attachmentId)) {
      AttachmentService service = AttachmentServiceProvider.getAttachmentService();
      // Retrieve attachment detail to copy
      attachment = service
          .searchDocumentById(new SimpleDocumentPK(attachmentId, fromPK.getInstanceId()), null);
      if (attachment != null) {
        SimpleDocumentPK copyPK = copyFile(attachment, toPK, DocumentType.attachment);
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
      SilverTrace.error("workflowEngine", "SendInKmelia.copyFiles", "CANNOT_PASTE_FILES", e);
    }
    return fileIds;
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
      SilverTrace.error("workflowEngine", "SendInKmelia.generatePDF()", "root.MSG_GEN_ERROR", e);
    }
    return null;
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

      String sAction;
      try {
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
      } catch (WorkflowException we) {
        sAction = "##";
      }

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
      SilverTrace
          .error("workflowEngine", "SendInKmelia.generatePDFStep()", "root.MSG_GEN_ERROR", e);
    }
  }

  private void generatePDFStepContent(HistoryStep step, com.lowagie.text.Document document) {
    try {
      Form form;
      if ("#question#".equals(step.getAction()) || "#response#".equals(step.getAction())) {
        // TODO
        form = null;
      } else {
        form = getProcessInstance().getProcessModel()
            .getPresentationForm(step.getAction(), getRole(), getLanguage());
      }

      XmlForm xmlForm = (XmlForm) form;
      if (xmlForm != null && step.getActionRecord() != null) {
        DataRecord data = step.getActionRecord();

        // Force simpletext displayers because itext cannot display HTML Form fields (select,
        // radio...)
        float[] colsWidth = {25, 75};
        PdfPTable tableContent = new PdfPTable(colsWidth);
        tableContent.setWidthPercentage(100);
        String fieldValue = "";
        Font fontLabel = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font fontValue = new Font(Font.HELVETICA, 10, Font.NORMAL);
        List<FieldTemplate> fieldTemplates = xmlForm.getFieldTemplates();
        for (FieldTemplate fieldTemplate1 : fieldTemplates) {
          try {
            GenericFieldTemplate fieldTemplate = (GenericFieldTemplate) fieldTemplate1;

            String fieldLabel = fieldTemplate.getLabel("fr");
            Field field = data.getField(fieldTemplate.getFieldName());
            String componentId = step.getProcessInstance().getProcessModel().getModelId();

            // wysiwyg field
            if ("wysiwyg".equals(fieldTemplate.getDisplayerName())) {
              String file = WysiwygFCKFieldDisplayer
                  .getFile(componentId, getProcessInstance().getInstanceId(),
                      fieldTemplate.getFieldName(), getLanguage());

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
            } // Field date type
            else if ("date".equals(fieldTemplate.getTypeName())) {
              fieldValue = DateUtil.getOutputDate(field.getValue(), "fr");
            } // Others fields type
            else {
              fieldTemplate.setDisplayerName("simpletext");
              fieldValue = field.getValue(getLanguage());
            }

            PdfPCell cell = new PdfPCell(new Phrase(fieldLabel, fontLabel));
            cell.setBorderWidth(0);
            cell.setPaddingBottom(5);
            tableContent.addCell(cell);

            cell = new PdfPCell(new Phrase(fieldValue, fontValue));
            cell.setBorderWidth(0);
            cell.setPaddingBottom(5);
            tableContent.addCell(cell);
          } catch (Exception e) {
            SilverTrace.warn("workflowEngine", "SendInKmelia.generatePDFStep()",
                "CANT_DISPLAY_DATA_OF_STEP", e);
          }
        }
        document.add(tableContent);
      }
    } catch (Exception e) {
      SilverTrace
          .error("workflowEngine", "SendInKmelia.generatePDFStep()", "root.MSG_GEN_ERROR", e);
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

  private KmeliaBm getKmeliaBm() {
    try {
      return ServiceProvider.getService(KmeliaBm.class);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("SendInKmelia.getKmeliaBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
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
    getKmeliaBm().addAttachmentToPublication(pubPK, userId, fileName, "", pdf);
  }

  private String getNodeId(String explicitPath) {
    String[] path = explicitPath.substring(1).split("/");
    NodePK nodePK = new NodePK("unknown", targetId);
    String parentId = NodePK.ROOT_NODE_ID;
    for (String name : path) {
      NodeDetail existingNode = null;
      try {
        existingNode =
            getNodeBm().getDetailByNameAndFatherId(nodePK, name, Integer.parseInt(parentId));
      } catch (Exception e) {
        SilverTrace.info("workflowEngine", "SendInKmelia.getNodeId()", "root.MSG_GEN_PARAM_VALUE",
            "node named '" + name + "' in path '" +
                explicitPath + "' does not exist");
      }
      if (existingNode != null) {
        // topic exists
        parentId = existingNode.getNodePK().getId();
      } else {
        // topic does not exists, creating it
        NodeDetail newNode = new NodeDetail();
        newNode.setName(name);
        newNode.setNodePK(new NodePK("unknown", targetId));
        newNode.setFatherPK(new NodePK(parentId, targetId));
        newNode.setCreatorId(userId);
        NodePK newNodePK;
        try {
          newNodePK = getNodeBm().createNode(newNode);
        } catch (Exception e) {
          SilverTrace
              .error("workflowEngine", "SendInKmelia.getNodeId()", "root.MSG_GEN_PARAM_VALUE",
                  "Can't create node named '" + name + "' in path '" +
                      explicitPath + "'", e);
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