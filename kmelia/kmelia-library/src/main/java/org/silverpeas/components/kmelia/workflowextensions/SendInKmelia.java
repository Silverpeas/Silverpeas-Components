/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
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
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.util.DateUtil;
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

import javax.inject.Inject;
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

  @Inject
  private NodeService nodeService;
  @Inject
  private KmeliaService kmeliaService;

  @Override
  public void execute() {
    final String role = getEvent().getUserRoleName();
    final String userId = OperationContext.getFromCache().getUser().getId();
    final Parameter parameter = getTriggerParameter("explorerFieldName");
    final Target target = new Target(role, userId, parameter).invoke();
    final String targetId = target.getTargetId();
    final String topicId = target.getTopicId();
    final String pubTitle = getPublicationTitle();
    final String pubDesc = getPublicationDescription();
    final String xmlFormName = getXmlFormName();
    final boolean formIsUsed = StringUtil.isDefined(xmlFormName);
    final PdfHistory pdfHistory = new PdfHistory().invoke();
    final boolean isHistoryEnable = pdfHistory.isEnable();
    final boolean isHistoryFirstAdding = pdfHistory.isFirstAdding();
    final String pdfHistoryName = pdfHistory.getFileName();

    // 1 - Create publication
    PublicationPK pubPK = new PublicationPK("0", targetId);
    Date now = new Date();
    String pubName = getProcessInstance().getTitle(role, getLanguage());
    pubName = applySubstitution(role, pubTitle, pubName);
    String desc = "";
    desc = applySubstitution(role, pubDesc, desc);
    PublicationDetail pubDetail = PublicationDetail.builder(getLanguage())
        .setPk(pubPK)
        .setNameAndDescription(pubName, desc)
        .created(now, userId)
        .setBeginDateTime(now, null)
        .setImportance(1)
        .build();

    if (formIsUsed) {
      pubDetail.setInfoId(xmlFormName);
    }

    KmeliaService kmelia = getKmeliaService();
    NodePK nodePK = new NodePK(topicId, targetId);
    String pubId = kmelia.createPublicationIntoTopic(pubDetail, nodePK);
    pubPK.setId(pubId);

    // 2 - Attach history as pdf file
    if (isHistoryEnable && isHistoryFirstAdding) {
      addPdfHistory(pdfHistoryName, role, pubPK, userId);
    }

    // 3 - Copy all instance regular files to publication
    ResourceReference fromPK = new ResourceReference(getProcessInstance().getInstanceId(),
        getProcessInstance().getModelId());
    ResourceReference toPK = new ResourceReference(pubPK);
    copyFiles(fromPK, toPK, DocumentType.attachment, DocumentType.attachment);

    if (isHistoryEnable && !isHistoryFirstAdding) {
      addPdfHistory(pdfHistoryName, role, pubPK, userId);
    }

    // process form content
    if (formIsUsed) {
      // target app use form : populate form fields
      populateFields(pubId, fromPK, toPK, xmlFormName);
    } else {
      // target app do not use form : copy files of worflow folder
      copyFiles(fromPK, toPK, DocumentType.form, DocumentType.attachment);
    }

    Parameter draftOutParameter = getTriggerParameter("forceDraftOut");
    if (draftOutParameter != null && StringUtil.getBooleanValue(draftOutParameter.getValue())) {
      getKmeliaService().draftOutPublication(pubPK, nodePK, SilverpeasRole.ADMIN.toString());
    }
  }

  private String getXmlFormName() {
    String xmlFormName = null;
    if (getTriggerParameter("xmlFormName") != null) {
      xmlFormName = getTriggerParameter("xmlFormName").getValue();
      if (StringUtil.isDefined(xmlFormName) && xmlFormName.lastIndexOf(".xml") != -1) {
        xmlFormName = xmlFormName.substring(0, xmlFormName.lastIndexOf(".xml"));
      }
    }
    return xmlFormName;
  }

  private String getPublicationTitle() {
    return getTriggerParameter("pubTitle").getValue();
  }

  private String getPublicationDescription() {
    final String pubDesc;
    final Parameter paramDescription = getTriggerParameter("pubDescription");
    if (paramDescription != null && StringUtil.isDefined(paramDescription.getValue())) {
      pubDesc = paramDescription.getValue();
    } else {
      pubDesc = null;
    }
    return pubDesc;
  }

  private String applySubstitution(final String role, final String text, final String defaultText) {
    String result = defaultText;
    if (StringUtil.isDefined(text)) {
      try {
        result = DataRecordUtil.applySubstitution(text,
            getProcessInstance().getAllDataRecord(role, "fr"), "fr");
      } catch (WorkflowException e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }
    }
    return result;
  }

  public void populateFields(String pubId, ResourceReference fromPK, ResourceReference toPK,
      String xmlFormName) {
    // Get the current instance
    UpdatableProcessInstance currentProcessInstance =
        (UpdatableProcessInstance) getProcessInstance();
    try {
      // register xmlForm of publication
      PublicationTemplateManager.getInstance()
          .addDynamicPublicationTemplate(toPK.getComponentInstanceId() + ":" + xmlFormName,
              xmlFormName + ".xml");

      PublicationTemplateImpl pubTemplate =
          (PublicationTemplateImpl) PublicationTemplateManager.getInstance()
              .getPublicationTemplate(toPK.getComponentInstanceId() + ":" + xmlFormName);
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

  private Object getFieldValue(final ResourceReference fromPK, final ResourceReference toPK,
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
        fieldValue =
            displayer.duplicateContent(fieldTemplate, fromPK, toPK, I18NHelper.DEFAULT_LANGUAGE);
      }
    } catch (WorkflowException e) {
      SilverLogger.getLogger(this).warn(e);
    }
    return fieldValue;
  }

  private String copyFormFile(ResourceReference fromPK, ResourceReference toPK,
      String attachmentId) {
    SimpleDocument attachment;
    if (StringUtil.isDefined(attachmentId)) {
      AttachmentService service = AttachmentServiceProvider.getAttachmentService();
      // Retrieve attachment detail to copy
      attachment =
          service.searchDocumentById(new SimpleDocumentPK(attachmentId, fromPK.getInstanceId()),
              null);
      if (attachment != null) {
        SimpleDocumentPK copyPK = copyFileWithoutDocumentTypeChange(attachment, toPK);
        return copyPK.getId();
      }
    }
    return null;
  }

  private Map<String, String> copyFiles(ResourceReference fromPK, ResourceReference toPK,
      DocumentType fromType, DocumentType toType) {
    Map<String, String> fileIds = new HashMap<>();
    try {
      List<SimpleDocument> origins = AttachmentServiceProvider.getAttachmentService()
          .listDocumentsByForeignKeyAndType(fromPK, fromType, getLanguage());
      for (SimpleDocument origin : origins) {
        SimpleDocumentPK copyPk = copyFile(origin, toPK, toType);
        fileIds.put(origin.getId(), copyPk.getId());
      }

    } catch (AttachmentException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return fileIds;
  }

  private SimpleDocumentPK copyFileWithoutDocumentTypeChange(SimpleDocument file,
      ResourceReference toPK) {
    return copyFile(file, toPK, null);
  }

  private SimpleDocumentPK copyFile(SimpleDocument file, ResourceReference toPK,
      DocumentType type) {
    if (type != null) {
      file.setDocumentType(type);
    }
    return AttachmentServiceProvider.getAttachmentService().copyDocument(file, toPK);
  }

  private byte[] generatePDF(final String role, ProcessInstance instance) {
    com.lowagie.text.Document document = new com.lowagie.text.Document();

    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PdfWriter.getInstance(document, baos);
      document.open();

      HistoryStep[] steps = instance.getHistorySteps();
      for (HistoryStep historyStep : steps) {
        generatePDFStep(role, historyStep, document);
      }

      document.close();

      return baos.toByteArray();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return new byte[0];
  }

  private void generatePDFStep(final String role, HistoryStep step,
      com.lowagie.text.Document document) {
    if (step != null) {
      generatePDFStepHeader(role, step, document);
      generatePDFStepContent(role, step, document);
    }
  }

  private void generatePDFStepHeader(final String role, HistoryStep step,
      com.lowagie.text.Document document) {
    try {
      String activity = "";
      if (step.getResolvedState() != null) {
        State resolvedState =
            step.getProcessInstance().getProcessModel().getState(step.getResolvedState());
        activity = resolvedState.getLabel(role, getLanguage());
      }

      String sAction = getAction(role, step);

      String actor = step.getUser().getFullName();
      String substituteId = step.getSubstituteId();
      if (StringUtil.isDefined(substituteId)) {
        actor = User.getById(substituteId).getDisplayedName() + " >> " + actor;
      }

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

  private String getAction(final String role, final HistoryStep step) {
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
        sAction = action.getLabel(role, getLanguage());
      }
      return sAction;
    } catch (WorkflowException we) {
      return "##";
    }
  }

  private void generatePDFStepContent(final String role, HistoryStep step,
      com.lowagie.text.Document document) {
    try {
      Form form;
      if ("#question#".equals(step.getAction()) || "#response#".equals(step.getAction())) {
        form = null;
      } else {
        form = getProcessInstance().getProcessModel()
            .getPresentationForm(step.getAction(), role, getLanguage());
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
        List<FieldTemplate> fieldTemplates = form.getFieldTemplates();
        for (FieldTemplate fieldTemplate : fieldTemplates) {
          generatePdfFieldContent(step, data, pageContext, tableContent,
              (GenericFieldTemplate) fieldTemplate);
        }
        document.add(tableContent);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
  }

  private void generatePdfFieldContent(final HistoryStep step, final DataRecord data,
      final PagesContext pageContext, final PdfPTable tableContent,
      final GenericFieldTemplate fieldTemplate) {
    try {
      Font fontLabel = new Font(Font.HELVETICA, 10, Font.BOLD);
      Font fontValue = new Font(Font.HELVETICA, 10, Font.NORMAL);
      String fieldLabel = fieldTemplate.getLabel(getLanguage());
      String fieldValue = null;
      Field field = data.getField(fieldTemplate.getFieldName());
      String componentId = step.getProcessInstance().getProcessModel().getModelId();

      // wysiwyg field
      if ("wysiwyg".equals(fieldTemplate.getDisplayerName())) {
        String file =
            WysiwygFCKFieldDisplayer.getFile(componentId, getProcessInstance().getInstanceId(),
                fieldTemplate.getFieldName(), getLanguage());

        // Extract the text content of the html code
        Source source = new Source(new FileInputStream(file));
        fieldValue = source.getTextExtractor().toString();
      } else if (FileField.TYPE.equals(fieldTemplate.getTypeName())) {
        // Field file type
        if (StringUtil.isDefined(field.getValue())) {
          SimpleDocument doc = AttachmentServiceProvider.getAttachmentService()
              .searchDocumentById(new SimpleDocumentPK(field.getValue(), componentId), null);
          if (doc != null) {
            fieldValue = doc.getFilename();
          }
        }
      } else {
        // Other field types
        FieldDisplayer<Field> fieldDisplayer =
            TypeManager.getInstance().getDisplayer(fieldTemplate.getTypeName(), "simpletext");
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

  private String getString(String key) {
    return key;
  }

  private String getLanguage() {
    return I18NHelper.DEFAULT_LANGUAGE;
  }

  private KmeliaService getKmeliaService() {
    return kmeliaService;
  }

  private void addPdfHistory(final String pdfHistoryName, final String role, PublicationPK pubPK,
      String userId) {
    final String fileName;
    if (pdfHistoryName != null && !pdfHistoryName.trim().isEmpty()) {
      if (!pdfHistoryName.endsWith(".pdf")) {
        fileName = pdfHistoryName + ".pdf";
      } else {
        fileName = pdfHistoryName;
      }
    } else {
      fileName = "processHistory_" + getProcessInstance().getInstanceId() + ".pdf";
    }
    byte[] pdf = generatePDF(role, getProcessInstance());
    getKmeliaService().addAttachmentToPublication(pubPK, userId, fileName, "", pdf);
  }

  private class Target {
    private final String role;
    private final String userId;
    private final Parameter parameter;
    private String targetId;
    private String topicId;

    public Target(final String role, final String userId, final Parameter parameter) {
      this.role = role;
      this.userId = userId;
      this.parameter = parameter;
    }

    public String getTargetId() {
      return targetId;
    }

    public String getTopicId() {
      return topicId;
    }

    public Target invoke() {
      if (parameter != null && StringUtil.isDefined(parameter.getValue())) {
        String explorerFieldName = parameter.getValue();
        // getting place to create publication from explorer field
        try {
          ExplorerField explorer = (ExplorerField) getProcessInstance().getField(explorerFieldName);
          ResourceReference pk = (ResourceReference) explorer.getObjectValue();
          targetId = pk.getInstanceId();
          topicId = pk.getId();
        } catch (WorkflowException e) {
          SilverLogger.getLogger(SendInKmelia.this).error(e.getMessage(), e);
          targetId = UNKNOWN;
          topicId = UNKNOWN;
        }
      } else {
        targetId = getTriggerParameter("targetComponentId").getValue();
        Parameter paramTopicPath = getTriggerParameter("targetFolderPath");
        if (paramTopicPath != null && StringUtil.isDefined(paramTopicPath.getValue())) {
          try {
            String path = DataRecordUtil.applySubstitution(paramTopicPath.getValue(),
                getProcessInstance().getAllDataRecord(role, "fr"), "fr");
            topicId = getNodeId(path, targetId, userId);
          } catch (WorkflowException e) {
            SilverLogger.getLogger(SendInKmelia.this).error(e.getMessage(), e);
            topicId = "0";
          }
        } else {
          topicId = getTriggerParameter("targetTopicId").getValue();
        }
      }
      return this;
    }

    /**
     * Creates the identifier of the last node in the specified path. If some of the nodes in the
     * path doesn't exist, then creates them. In case of a node creation, the following requirements
     * are satisfied:
     * <ul>
     *   <li>if the creation fails, the root node is returned and the current transaction isn't
     *   rollbacked so that the publication can be put into the returned node,</li>
     *   <li>the node creation is effectively applied and not just put in the current transaction's
     *   cache so that the node can be get later in the treatment.</li>
     * </ul>
     * @param explicitPath the path of the topic in which the publication will be put.
     * @return the unique identifier of the topic referred by the given path.
     */
    private String getNodeId(String explicitPath, final String targetId, final String userId) {
      return Transaction.performInNew(() -> {
        String[] path = explicitPath.substring(1).split("/");
        NodePK nodePK = new NodePK(UNKNOWN, targetId);
        String parentId = NodePK.ROOT_NODE_ID;
        for (String name : path) {
          NodeDetail existingNode =
              getNodeService().getDetailByNameAndFatherId(nodePK, name, Integer.parseInt(parentId));
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
              newNodePK = getNodeService().createNode(newNode);
            } catch (Exception e) {
              SilverLogger.getLogger(this)
                  .warn("Cannot create node {0} in path {1}: {2}", name, explicitPath,
                      e.getMessage());
              return "0";
            }
            parentId = newNodePK.getId();
          }
        }
        return parentId;
      });
    }

    private NodeService getNodeService() {
      return nodeService;
    }
  }

  private class PdfHistory {
    private boolean addPDFHistory;
    private boolean addPDFHistoryFirst;
    private String pdfHistoryName;

    public boolean isEnable() {
      return addPDFHistory;
    }

    public boolean isFirstAdding() {
      return addPDFHistoryFirst;
    }

    public String getFileName() {
      return pdfHistoryName;
    }

    public PdfHistory invoke() {
      // Add pdf history before instance attachments
      if (getTriggerParameter("addPDFHistory") != null) {
        addPDFHistory = StringUtil.getBooleanValue(getTriggerParameter("addPDFHistory").getValue());
        if (getTriggerParameter("addPDFHistoryFirst") != null) {
          addPDFHistoryFirst =
              StringUtil.getBooleanValue(getTriggerParameter("addPDFHistoryFirst").getValue());
        } else {
          addPDFHistoryFirst = true;
        }
        Parameter paramPDFName = getTriggerParameter("pdfHistoryName");
        if (paramPDFName != null) {
          pdfHistoryName = getTriggerParameter("pdfHistoryName").getValue();
        } else {
          pdfHistoryName = null;
        }
      } else {
        addPDFHistory = true;
        addPDFHistoryFirst = true;
        pdfHistoryName = null;
      }
      return this;
    }
  }
}