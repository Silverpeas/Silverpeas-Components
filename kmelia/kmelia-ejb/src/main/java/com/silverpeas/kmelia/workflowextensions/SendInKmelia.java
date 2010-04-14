/**
 * Copyright (C) 2000 - 2010 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.kmelia.workflowextensions;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.DataRecordUtil;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.form.XmlForm;
import com.silverpeas.form.record.GenericFieldTemplate;
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
import com.silverpeas.workflow.api.model.State;
import com.silverpeas.workflow.api.model.Trigger;
import com.silverpeas.workflow.external.impl.ExternalActionImpl;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.ejb.VersioningBm;
import com.stratelia.silverpeas.versioning.ejb.VersioningBmHome;
import com.stratelia.silverpeas.versioning.ejb.VersioningRuntimeException;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.model.Worker;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBmHome;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

public class SendInKmelia extends ExternalActionImpl {
  private String targetId = "unknown";
  private String topicId = "unknown";
  private String pubTitle = "unknown";
  private String pubDesc = "unknown";
  private String role = "unknown";
  private String xmlFormName = null;
  private boolean addPDFHistory = true;
  private OrganizationController orga = null;
  private String userId = null;

  public SendInKmelia() {

  }

  @Override
  public void execute() {
    setRole(getEvent().getUserRoleName());
    targetId = getTriggerParameter("targetComponentId").getValue();
    topicId = getTriggerParameter("targetTopicId").getValue();
    pubTitle = getTriggerParameter("pubTitle").getValue();
    if (getTriggerParameter("pubDescription") != null) {
      pubDesc = getTriggerParameter("pubDescription").getValue();
    }
    if (getTriggerParameter("xmlFormName") != null) {
      xmlFormName = getTriggerParameter("xmlFormName").getValue();
      if (StringUtil.isDefined(xmlFormName) && xmlFormName.lastIndexOf(".xml") != -1) {
        xmlFormName = xmlFormName.substring(0, xmlFormName.lastIndexOf(".xml"));
      }
    }
    if (getTriggerParameter("addPDFHistory") != null) {
      addPDFHistory = StringUtil.getBooleanValue(getTriggerParameter("addPDFHistory").getValue());
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
    userId = getEvent().getUser().getUserId();
    PublicationDetail pubDetail =
        new PublicationDetail(pubPK, pubName, desc, now, now, null, userId, 1, null, null, null);

    if (StringUtil.isDefined(xmlFormName)) {
      pubDetail.setInfoId(xmlFormName);
    }

    KmeliaBm kmelia = getKmeliaBm();
    String pubId = null;
    try {
      pubId = kmelia.createPublicationIntoTopic(pubDetail, new NodePK(getTopicId(), getTargetId()));
    } catch (RemoteException e) {
      SilverTrace.error("workflowEngine", "SendInKmelia.execute()", "root.MSG_GEN_ERROR", e);
    }
    pubPK.setId(pubId);

    // 2 - Attach history as pdf file
    if (addPDFHistory) {
      String fileName = "processHistory_" + getProcessInstance().getInstanceId() + ".pdf";
      byte[] pdf = generatePDF(getProcessInstance());
      try {
        kmelia.addAttachmentToPublication(pubPK, userId, fileName, "", pdf);
      } catch (RemoteException e) {
        SilverTrace.error("workflowEngine", "SendInKmelia.execute()", "root.MSG_GEN_ERROR", e);
      }
    }

    // 3 - Copy all instance attached files to publication
    ForeignPK fromPK =
        new ForeignPK(getProcessInstance().getInstanceId(), getProcessInstance().getModelId());
    ForeignPK toPK = new ForeignPK(pubPK);
    pasteFiles(fromPK, toPK);

    // force the update
    try {
      PublicationDetail newPubli = getKmeliaBm().getPublicationDetail(pubPK);
      newPubli.setStatusMustBeChecked(false);
      getKmeliaBm().updatePublication(newPubli);
    } catch (RemoteException e) {
      SilverTrace.error("workflowEngine", "SendInKmelia.execute()",
          "workflowEngine.CANNOT_UPDATE_PUBLICATION", e);
    }

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
      PublicationTemplateImpl pubTemplate =
          (PublicationTemplateImpl) PublicationTemplateManager.getPublicationTemplate(targetId +
          ":" + xmlFormName);
      DataRecord record = pubTemplate.getRecordSet().getEmptyRecord();
      record.setId(pubId);
      for (int i = 0; i < record.getFieldNames().length; i++) {
        record.getField(record.getFieldNames()[i]).setObjectValue(
            currentProcessInstance.getField(
            record.getFieldNames()[i]).getObjectValue());
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

  public Hashtable<String, String> pasteFiles(ForeignPK fromPK, ForeignPK toPK) {
    Hashtable<String, String> fileIds = new Hashtable<String, String>();

    try {
      boolean fromCompoVersion =
          "yes".equals(getOrganizationController().getComponentParameterValue(
          fromPK.getInstanceId(), "versionControl"));
      boolean toCompoVersion =
          "yes".equals(getOrganizationController().getComponentParameterValue(toPK.getInstanceId(),
          "versionControl"));

      if (!fromCompoVersion && !toCompoVersion) {
        // attachments --> attachments
        // paste attachments
        fileIds = AttachmentController.copyAttachmentByCustomerPKAndContext(fromPK, toPK, "Images");
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
    } catch (Exception e) {
      SilverTrace.error("workflowEngine", "SendInKmelia.pasteFiles", "CANNOT_PASTE_FILES", e);
    }

    return fileIds;
  }

  /******************************************************************************************/
  /* KMELIA - Copier/coller des documents versionn�s */
  /******************************************************************************************/
  public void pasteDocuments(ForeignPK fromPK, ForeignPK pubPK) throws RemoteException {
    SilverTrace.info("workflowEngine", "SendInKmelia.pasteDocuments()",
        "root.MSG_GEN_ENTER_METHOD", "pubPKFrom = " + fromPK.toString() + ", pubPK = " + pubPK);

    // paste versioning documents attached to publication
    List<Document> documents = getVersioningBm().getDocuments(new ForeignPK(fromPK));

    SilverTrace.info("workflowEngine", "SendInKmelia.pasteDocuments()", "root.MSG_GEN_PARAM_VALUE",
        documents.size() + " to paste");

    if (documents.size() == 0)
      return;

    VersioningUtil versioningUtil = new VersioningUtil();
    String pathFrom = null; // where the original files are
    String pathTo = null; // where the copied files will be

    // change the list of workers
    ArrayList<Worker> workers = getWorkers(pubPK);

    // paste each document
    Document document = null;
    List<DocumentVersion> versions = null;
    DocumentVersion version = null;
    for (int d = 0; d < documents.size(); d++) {
      document = documents.get(d);

      SilverTrace.info("workflowEngine", "SendInKmelia.pasteDocuments()",
          "root.MSG_GEN_PARAM_VALUE", "document name = " + document.getName());

      // retrieve all versions of the document
      versions = getVersioningBm().getDocumentVersions(document.getPk());

      // retrieve the initial version of the document
      version = versions.get(0);

      if (pathFrom == null)
        pathFrom =
            versioningUtil.createPath(document.getPk().getSpaceId(), document.getPk()
            .getInstanceId(), null);

      // change some data to paste
      document.setPk(new DocumentPK(-1, "useless", pubPK.getInstanceId()));
      document.setForeignKey(pubPK);
      document.setStatus(Document.STATUS_CHECKINED);
      document.setLastCheckOutDate(new Date());
      document.setWorkList(workers);

      if (pathTo == null)
        pathTo = versioningUtil.createPath("useless", pubPK.getInstanceId(), null);

      String newVersionFile = null;
      if (version != null) {
        // paste file on fileserver
        newVersionFile = pasteVersionFile(version.getPhysicalName(), pathFrom, pathTo);
        version.setPhysicalName(newVersionFile);
      }

      // create the document with its first version
      DocumentPK documentPK = getVersioningBm().createDocument(document, version);
      document.setPk(documentPK);

      for (int v = 1; v < versions.size(); v++) {
        version = (DocumentVersion) versions.get(v);
        version.setDocumentPK(documentPK);
        SilverTrace.info("workflowEngine", "SendInKmelia.pasteDocuments()",
            "root.MSG_GEN_PARAM_VALUE", "paste version = " + version.getLogicalName());

        // paste file on fileserver
        newVersionFile = pasteVersionFile(version.getPhysicalName(), pathFrom, pathTo);
        version.setPhysicalName(newVersionFile);

        // paste data
        getVersioningBm().addVersion(version);
      }
    }
  }

  private ArrayList<Worker> getWorkers(ForeignPK pubPK) {
    ArrayList<Worker> workers = new ArrayList<Worker>();

    List<String> workingProfiles = new ArrayList<String>();
    workingProfiles.add("writer");
    workingProfiles.add("publisher");
    workingProfiles.add("admin");
    String[] userIds =
        getOrganizationController().getUsersIdsByRoleNames(pubPK.getInstanceId(), workingProfiles);

    String userId = null;
    Worker worker = null;
    for (int u = 0; u < userIds.length; u++) {
      userId = (String) userIds[u];
      worker =
          new Worker(new Integer(userId).intValue(), -1, u, false, true, pubPK.getInstanceId(),
          "U", false, true, 0);
      workers.add(worker);
    }

    return workers;
  }

  public void pasteDocumentsAsAttachments(ForeignPK fromPK, ForeignPK toPK) throws RemoteException {
    SilverTrace.info("workflowEngine", "SendInKmelia.pasteDocumentsAsAttachments()",
        "root.MSG_GEN_ENTER_METHOD", "pubPKFrom = " + fromPK.toString() + ", toPK = " +
        toPK.toString());

    // paste versioning documents attached to publication
    List<Document> documents = getVersioningBm().getDocuments(new ForeignPK(fromPK));

    SilverTrace.info("workflowEngine", "SendInKmelia.pasteDocumentsAsAttachments()",
        "root.MSG_GEN_PARAM_VALUE", documents.size() + " documents to paste");

    if (documents.size() == 0)
      return;

    VersioningUtil versioningUtil = new VersioningUtil();
    String pathFrom = null; // where the original files are
    String pathTo = null; // where the copied files will be

    // paste each document
    Document document = null;
    DocumentVersion version = null;
    for (int d = 0; d < documents.size(); d++) {
      document = documents.get(d);

      SilverTrace.info("workflowEngine", "SendInKmelia.pasteDocumentsAsAttachments()",
          "root.MSG_GEN_PARAM_VALUE", "document name = " + document.getName());

      // retrieve last public versions of the document
      version = getVersioningBm().getLastPublicDocumentVersion(document.getPk());

      if (pathFrom == null)
        pathFrom =
            versioningUtil.createPath(document.getPk().getSpaceId(), document.getPk()
            .getInstanceId(), null);

      if (pathTo == null)
        pathTo = AttachmentController.createPath(toPK.getInstanceId(), "Images");

      String newVersionFile = null;
      if (version != null) {
        // paste file on fileserver
        newVersionFile = pasteVersionFile(version.getPhysicalName(), pathFrom, pathTo);

        if (newVersionFile != null) {
          // create the attachment in DB
          // Do not index it cause made by the updatePublication call later
          AttachmentDetail attachment =
              new AttachmentDetail(new AttachmentPK("unknown", toPK.getInstanceId()),
              newVersionFile, version.getLogicalName(), "", version.getMimeType(), version
              .getSize(), "Images", new Date(), toPK, document.getName(), document
              .getDescription(), 0);
          AttachmentController.createAttachment(attachment, false);
        }
      }
    }
  }

  public void pasteAttachmentsAsDocuments(ForeignPK fromPK, ForeignPK toPK) throws RemoteException {
    SilverTrace.info("workflowEngine", "SendInKmelia.pasteAttachmentsAsDocuments()",
        "root.MSG_GEN_ENTER_METHOD", "pubPKFrom = " + fromPK.toString() + ", toPK = " + toPK);

    List<AttachmentDetail> attachments =
        AttachmentController.searchAttachmentByPKAndContext(fromPK, "Images");

    SilverTrace.info("workflowEngine", "SendInKmelia.pasteAttachmentsAsDocuments()",
        "root.MSG_GEN_PARAM_VALUE", attachments.size() + " attachments to paste");

    if (attachments.size() == 0)
      return;

    ArrayList<Worker> workers = getWorkers(toPK);

    VersioningUtil versioningUtil = new VersioningUtil();
    String pathFrom = null; // where the original files are
    String pathTo = null; // where the copied files will be

    // paste each attachment
    Document document = null;
    DocumentVersion version = null;
    AttachmentDetail attachment = null;
    for (int d = 0; d < attachments.size(); d++) {
      attachment = attachments.get(d);

      SilverTrace.info("workflowEngine", "SendInKmelia.pasteAttachmentsAsDocuments()",
          "root.MSG_GEN_PARAM_VALUE", "attachment name = " + attachment.getLogicalName());

      if (pathTo == null)
        pathTo = versioningUtil.createPath("useless", toPK.getInstanceId(), null);

      if (pathFrom == null)
        pathFrom = AttachmentController.createPath(fromPK.getInstanceId(), "Images");

      // paste file on fileserver
      String newPhysicalName = pasteVersionFile(attachment.getPhysicalName(), pathFrom, pathTo);

      if (newPhysicalName != null) {
        // Document creation
        document =
            new Document(new DocumentPK(-1, "useless", toPK.getInstanceId()), toPK, attachment
            .getLogicalName(), attachment.getInfo(), 0, Integer.parseInt(userId), new Date(),
            "", toPK.getInstanceId(), workers, new ArrayList(), 0, 0);

        // Version creation
        version =
            new DocumentVersion(null, null, 1, 0, Integer.parseInt(userId), new Date(), "",
            DocumentVersion.TYPE_PUBLIC_VERSION, DocumentVersion.STATUS_VALIDATION_NOT_REQ,
            newPhysicalName, attachment.getLogicalName(), attachment.getType(), new Long(
            attachment.getSize()).intValue(), toPK.getInstanceId());

        getVersioningBm().createDocument(document, version);
      }
    }
  }

  private String pasteVersionFile(String fileNameFrom, String from, String to) {
    SilverTrace.info("workflowEngine", "SendInKmelia.pasteVersionFile()",
        "root.MSG_GEN_ENTER_METHOD", "version = " + fileNameFrom);

    if (!fileNameFrom.equals("dummy")) {
      // we have to rename pasted file (in case the copy/paste append in the same instance)
      String type = FileRepositoryManager.getFileExtension(fileNameFrom);
      String fileNameTo = new Long(new Date().getTime()).toString() + "." + type;

      try {
        // paste file associated to the first version
        FileRepositoryManager.copyFile(from + fileNameFrom, to + fileNameTo);
      } catch (Exception e) {
        SilverTrace.error("workflowEngine", "SendInKmelia.pasteVersionFile()",
            "root.EX_FILE_NOT_FOUND", from + fileNameFrom);
        return null;
        // throw new
        // KmeliaRuntimeException("SendInKmelia.pasteVersionFile()",SilverpeasRuntimeException.ERROR,
        // "root.EX_FILE_NOT_FOUND", e);
      }
      return fileNameTo;
    } else {
      return fileNameFrom;
    }
  }

  private VersioningBm getVersioningBm() {
    try {
      VersioningBmHome vscEjbHome =
          (VersioningBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.VERSIONING_EJBHOME,
          VersioningBmHome.class);
      return vscEjbHome.create();
    } catch (Exception e) {
      throw new VersioningRuntimeException("SendInKmelia.getVersioningBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
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

      String sAction = null;
      try {
        if (step.getAction().equals("#question#"))
          sAction = getString("processManager.question");

        else if (step.getAction().equals("#response#"))
          sAction = getString("processManager.response");

        else if (step.getAction().equals("#reAssign#"))
          sAction = getString("processManager.reAffectation");

        else {
          Action action = step.getProcessInstance().getProcessModel().getAction(step.getAction());
          sAction = action.getLabel(getRole(), getLanguage());
        }
      } catch (WorkflowException we) {
        sAction = "##";
      }

      String actor = step.getUser().getFullName();

      String date = DateUtil.getOutputDate(step.getActionDate(), getLanguage());

      String header = "";
      if (StringUtil.isDefined(activity))
        header += activity + " - ";
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
      Form form = null;
      if (step.getAction().equals("#question#") || step.getAction().equals("#response#")) {
        // form = getQuestionForm(true);
        // TODO
        form = null;
      } else
        form =
            getProcessInstance().getProcessModel().getPresentationForm(step.getAction(), getRole(),
            getLanguage());

      XmlForm xmlForm = (XmlForm) form;
      if (xmlForm != null) {
        DataRecord data = step.getActionRecord();

        // Force simpletext displayers because itext cannot display HTML Form fields (select,
        // radio...)
        float[] colsWidth = { 25, 75 };
        PdfPTable tableContent = new PdfPTable(colsWidth);
        tableContent.setWidthPercentage(100);

        PdfPCell cell = null;
        String fieldLabel;
        String fieldValue;
        Font fontLabel = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font fontValue = new Font(Font.HELVETICA, 10, Font.NORMAL);
        List<FieldTemplate> fieldTemplates = xmlForm.getFieldTemplates();
        for (Iterator<?> iterator = fieldTemplates.iterator(); iterator.hasNext();) {
          GenericFieldTemplate fieldTemplate = (GenericFieldTemplate) iterator.next();
          fieldTemplate.setDisplayerName("simpletext");

          fieldLabel = fieldTemplate.getLabel("fr");

          fieldValue = data.getField(fieldTemplate.getFieldName()).getValue();
          if (fieldTemplate.getTypeName().equals("date")) {
            fieldValue = DateUtil.getOutputDate(fieldValue, "fr");
          }

          cell = new PdfPCell(new Phrase(fieldLabel, fontLabel));
          cell.setBorderWidth(0);
          cell.setPaddingBottom(5);
          tableContent.addCell(cell);

          cell = new PdfPCell(new Phrase(fieldValue, fontValue));
          cell.setBorderWidth(0);
          cell.setPaddingBottom(5);
          tableContent.addCell(cell);
        }

        document.add(tableContent);
      }
    } catch (Exception e) {
      SilverTrace
          .error("workflowEngine", "SendInKmelia.generatePDFStep()", "root.MSG_GEN_ERROR", e);
    }
  }

  private OrganizationController getOrganizationController() {
    if (orga == null)
      orga = new OrganizationController();
    return orga;
  }

  private String getString(String key) {
    // TODO
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
      KmeliaBmHome kscEjbHome =
          (KmeliaBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.KMELIABM_EJBHOME,
          KmeliaBmHome.class);
      return kscEjbHome.create();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("SendInKmelia.getKmeliaBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

}