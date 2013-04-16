/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.kmelia.workflowextensions;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silverpeas.attachment.AttachmentException;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import com.silverpeas.form.*;
import com.silverpeas.form.displayers.WysiwygFCKFieldDisplayer;
import com.silverpeas.form.fieldType.ExplorerField;
import com.silverpeas.form.fieldType.FileField;
import com.silverpeas.form.form.XmlForm;
import com.silverpeas.form.record.GenericFieldTemplate;
import com.silverpeas.kmelia.control.KmeliaServiceFactory;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.instance.HistoryStep;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.instance.UpdatableProcessInstance;
import com.silverpeas.workflow.api.model.Action;
import com.silverpeas.workflow.api.model.Parameter;
import com.silverpeas.workflow.api.model.State;
import com.silverpeas.workflow.external.impl.ExternalActionImpl;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

import au.id.jericho.lib.html.Source;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class SendInKmelia extends ExternalActionImpl {

  private String targetId = "unknown";
  private String topicId = "unknown";
  private String pubTitle = "unknown";
  private String pubDesc = null;
  private String role = "unknown";
  private String xmlFormName = null;
  private boolean addPDFHistory = true;
  // Add pdf history before instance attachments
  private boolean addPDFHistoryFirst = true;
  private String pdfHistoryName = null;
  private OrganizationController orga = null;
  private String userId = null;
  private final String ADMIN_ID = "0";

  public SendInKmelia() {
  }

  @Override
  public void execute() {
    setRole(getEvent().getUserRoleName());

    Parameter parameter = getTriggerParameter("explorerFieldName");
    if (parameter != null && StringUtil.isDefined(parameter.getValue())) {
      String explorerFieldName = parameter.getValue();
      // getting place to create publication from explorer field
      ExplorerField explorer = null;
      try {
        explorer = (ExplorerField) getProcessInstance().getField(explorerFieldName);
      } catch (WorkflowException e1) {
        SilverTrace.error("processManager", "SendInKmelia.execute", "err.CANT_GET_TOPIC", e1);
      }
      ForeignPK pk = (ForeignPK) explorer.getObjectValue();

      targetId = pk.getInstanceId();
      topicId = pk.getId();
    } else {
      targetId = getTriggerParameter("targetComponentId").getValue();
      topicId = getTriggerParameter("targetTopicId").getValue();
    }
    pubTitle = getTriggerParameter("pubTitle").getValue();
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
        pubName =
            DataRecordUtil.applySubstitution(pubTitle, getProcessInstance().getAllDataRecord(role,
            "fr"), "fr");
      } catch (WorkflowException e) {
        SilverTrace.error("workflowEngine", "SendInKmelia.execute()", "root.MSG_GEN_ERROR", e);
      }
    }
    String desc = "";
    if (StringUtil.isDefined(pubDesc)) {
      try {
        desc =
            DataRecordUtil.applySubstitution(pubDesc, getProcessInstance().getAllDataRecord(role,
            "fr"), "fr");
      } catch (WorkflowException e) {
        SilverTrace.error("workflowEngine", "SendInKmelia.execute()", "root.MSG_GEN_ERROR", e);
      }
    }
    userId = getBestUserDetail().getId();
    PublicationDetail pubDetail =
        new PublicationDetail(pubPK, pubName, desc, now, now, null, userId, 1, null, null, null);

    if (StringUtil.isDefined(xmlFormName)) {
      pubDetail.setInfoId(xmlFormName);
    }

    KmeliaBm kmelia = getKmeliaBm();
    String pubId = kmelia.createPublicationIntoTopic(pubDetail, new NodePK(getTopicId(),
        getTargetId()));
    pubPK.setId(pubId);

    // 2 - Attach history as pdf file
    if (addPDFHistory && addPDFHistoryFirst) {
      addPdfHistory(pubPK, userId);
    }

    // 3 - Copy all instance attached files to publication
    ForeignPK fromPK =
        new ForeignPK(getProcessInstance().getInstanceId(), getProcessInstance().getModelId());
    ForeignPK toPK = new ForeignPK(pubPK);
    pasteFiles(fromPK, toPK);

    if (addPDFHistory && !addPDFHistoryFirst) {
      addPdfHistory(pubPK, userId);
    }

    // force the update
    PublicationDetail newPubli = getKmeliaBm().getPublicationDetail(pubPK);
    newPubli.setStatusMustBeChecked(false);
    getKmeliaBm().updatePublication(newPubli);

    // Populate the fields
    if (StringUtil.isDefined(xmlFormName)) {
      populateFields(pubId);
    }
    orga = null;
  }

  public void populateFields(String pubId) {
    // Get the current instance
    UpdatableProcessInstance currentProcessInstance =
        (UpdatableProcessInstance) getProcessInstance();
    try {
      PublicationTemplateImpl pubTemplate = (PublicationTemplateImpl) PublicationTemplateManager
          .getInstance().getPublicationTemplate(targetId + ":" + xmlFormName);
      DataRecord record = pubTemplate.getRecordSet().getEmptyRecord();
      record.setId(pubId);
      for (String fieldName : record.getFieldNames()) {
        record.getField(fieldName).setObjectValue(
            currentProcessInstance.getField(fieldName).getObjectValue());
      }
      // Update
      pubTemplate.getRecordSet().save(record);

    } catch (PublicationTemplateException e) {
      SilverTrace.error("workflowEngine",
          "SendInKmelia.populateFields()",
          "workflowEngine.CANNOT_UPDATE_PUBLICATION", e);
    } catch (FormException e) {
      SilverTrace.error("workflowEngine",
          "SendInKmelia.populateFields()",
          "workflowEngine.CANNOT_UPDATE_PUBLICATION", e);
    } catch (WorkflowException e) {
      SilverTrace.error("workflowEngine",
          "SendInKmelia.populateFields()",
          "workflowEngine.CANNOT_UPDATE_PUBLICATION", e);
    }

  }

  public Map<String, String> pasteFiles(ForeignPK fromPK, ForeignPK toPK) {
    Map<String, String> fileIds = new HashMap<String, String>();
    try {
      boolean fromCompoVersion = StringUtil.getBooleanValue(getOrganizationController().
          getComponentParameterValue(fromPK.getInstanceId(), "versionControl"));
      boolean toCompoVersion = StringUtil.getBooleanValue(getOrganizationController().
          getComponentParameterValue(toPK.getInstanceId(), "versionControl"));

      if (!fromCompoVersion && !toCompoVersion) {
        // attachments --> attachments
        // paste attachments
        List<SimpleDocument> origins = AttachmentServiceFactory.getAttachmentService().
            listDocumentsByForeignKeyAndType(fromPK, DocumentType.attachment, getLanguage());
        for (SimpleDocument origin : origins) {
          SimpleDocumentPK copyPk = AttachmentServiceFactory.getAttachmentService().copyDocument(
              origin, toPK);
          fileIds.put(origin.getId(), copyPk.getId());
        }
      } else if (fromCompoVersion && !toCompoVersion) {
        // versioning --> attachments
        // Last public versions becomes the new attachment
        pasteDocumentsAsAttachments(fromPK, toPK);
      } else if (!fromCompoVersion && toCompoVersion) {
        // attachments --> versioning
        // paste versioning documents
        pasteAttachmentsAsDocuments(fromPK, toPK);
      } else {
        // versioning --> versioning
        // paste versioning documents
        pasteDocuments(fromPK, toPK);
      }
    } catch (AttachmentException e) {
      SilverTrace.error("workflowEngine", "SendInKmelia.pasteFiles", "CANNOT_PASTE_FILES", e);
    }

    return fileIds;
  }

  /**
   * ***************************************************************************************
   */
  /* KMELIA - Copier/coller des documents versionnï¿½s */
  /**
   * **************************************************************************************
   */
  public void pasteDocuments(ForeignPK fromPK, ForeignPK pubPK) {
    SilverTrace.info("workflowEngine", "SendInKmelia.pasteDocuments()",
        "root.MSG_GEN_ENTER_METHOD", "pubPKFrom = " + fromPK.toString() + ", pubPK = " + pubPK);
    List<SimpleDocument> originals = AttachmentServiceFactory.getAttachmentService().
        listDocumentsByForeignKey(fromPK, getLanguage());
    for (SimpleDocument origin : originals) {
      AttachmentServiceFactory.getAttachmentService().copyDocument(origin, pubPK);
    }
  }

  public void pasteDocumentsAsAttachments(ForeignPK fromPK, ForeignPK toPK) {
    SilverTrace.info("workflowEngine", "SendInKmelia.pasteDocumentsAsAttachments()",
        "root.MSG_GEN_ENTER_METHOD", "pubPKFrom = " + fromPK.toString() + ", toPK = " + toPK.
        toString());
    List<SimpleDocument> originals = AttachmentServiceFactory.getAttachmentService().
        listDocumentsByForeignKey(fromPK, getLanguage());
    for (SimpleDocument origin : originals) {
      AttachmentServiceFactory.getAttachmentService().copyDocument(origin.getLastPublicVersion(),
          toPK);
    }
  }

  public void pasteAttachmentsAsDocuments(ForeignPK fromPK, ForeignPK toPK) {
    SilverTrace.info("workflowEngine", "SendInKmelia.pasteAttachmentsAsDocuments()",
        "root.MSG_GEN_ENTER_METHOD", "pubPKFrom = " + fromPK.toString() + ", toPK = " + toPK);
    KmeliaServiceFactory.getFactory().getKmeliaService().pasteAttachmentsAsDocuments(fromPK, toPK,
        getLanguage());
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
        State resolvedState = step.getProcessInstance().getProcessModel().getState(
            step.getResolvedState());
        activity = resolvedState.getLabel(getRole(), getLanguage());
      }

      String sAction = null;
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
        form = getProcessInstance().getProcessModel().getPresentationForm(step.getAction(),
            getRole(), getLanguage());
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
              String file = WysiwygFCKFieldDisplayer.getFile(componentId,
                  getProcessInstance().getInstanceId(), fieldTemplate.getFieldName(), getLanguage());

              // Extract the text content of the html code
              Source source = new Source(new FileInputStream(file));
              if (source != null) {
                fieldValue = source.getTextExtractor().toString();
              }
            } // Field file type
            else if (FileField.TYPE.equals(fieldTemplate.getDisplayerName()) && StringUtil.
                isDefined(field.getValue())) {
              SimpleDocument doc = AttachmentServiceFactory.getAttachmentService().
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
      SilverTrace.error("workflowEngine", "SendInKmelia.generatePDFStep()", "root.MSG_GEN_ERROR", e);
    }
  }

  private OrganizationController getOrganizationController() {
    if (orga == null) {
      orga = new OrganizationController();
    }
    return orga;
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
      return EJBUtilitaire.getEJBObjectRef(JNDINames.KMELIABM_EJBHOME, KmeliaBm.class);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("SendInKmelia.getKmeliaBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * Get actor if exist, admin otherwise
   *
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
}