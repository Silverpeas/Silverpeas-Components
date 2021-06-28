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
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.processmanager.service;

import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.model.UnlockContext;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.field.DateField;
import org.silverpeas.core.contribution.content.form.field.FileField;
import org.silverpeas.core.contribution.content.form.field.TextField;
import org.silverpeas.core.contribution.content.form.form.XmlForm;
import org.silverpeas.core.contribution.content.form.record.GenericDataRecord;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.MimeTypes;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.workflow.api.Workflow;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.event.TaskDoneEvent;
import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.ProcessModel;
import org.silverpeas.core.workflow.api.task.Task;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.engine.user.UserImpl;
import org.silverpeas.processmanager.ProcessManagerException;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.silverpeas.core.contribution.attachment.AttachmentService.VERSION_MODE;

/**
 * Process manager service which manage processes
 */
@Service
@Singleton
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultProcessManagerService implements ProcessManagerService {

  /**
   * Default role for creating workflow processes.
   */
  public static final String DEFAULT_ROLE = "supervisor";
  private static final String FR_LANG = "fr";
  private static final String FIELD_TYPE = ", field type: ";
  private static final String DEFAULT_PROCESS_MANAGER_SERVICE = "DefaultProcessManagerService";
  private static final String FIELD_NAME = "field name: ";

  /**
   * Create a process instance for a specific workflow component, by a specific user using one role
   * of those defined in a given workflow definition. The contents of a file is passed in as a
   * single parameter. This file is uploaded into the process data and stored in the first field of
   * the file type.
   * @param componentId the ID of the component which defines the workflow (must be a workflow
   * component).
   * @param userId the current user ID.
   * @param fileName the name of the file being pushed during process creation.
   * @param fileContent the full content of the file being pushed during process creation (as an
   * array of bytes).
   * @return the instance ID of the newly started process
   * @throws ProcessManagerException
   */
  @Override
  public String createProcess(String componentId, String userId, String fileName,
      byte[] fileContent) throws ProcessManagerException {
    Map<String, FileContent> metadata = new HashMap<>(1);
    metadata.put(null, new FileContent(fileName, fileContent));
    return createProcess(componentId, userId, DEFAULT_ROLE, metadata);
  }

  /**
   * Create a process instance for a specific workflow component, by a specific user using one role
   * of those defined in a given workflow definition.
   * <p>
   * Some information may be specified that will fill in the creation form of the new process
   * instance. Such data should be placed into a map structure of key-value pairs where keys are
   * the
   * name of the intended fields of the creation form and values are strins (text fields), dates
   * (date fields), colelctions of strings, collections of dates, or a single {@link FileContent}
   * object. </p>
   * <p>
   * {@link FileContent} are used to pass in as an argument a complete file of binary data, loaded
   * into memory. </p>
   * @param componentId the ID of the component which defines the workflow (must be a workflow
   * component).
   * @param userId the current user ID.
   * @param userRole the role of the user while creating the process instance (this role must have
   * been defined in the workflow process definition).
   * @param metadata a map of all input metadata, coming with the file and describing it. The key
   * is
   * expected to be the name of a field in the process form definition (with specification of the
   * type name of the field), and the value must be the value to put into this field (it may be a
   * collection of value if the field is multivaluated, else only the first value is considered).
   * @return the instance ID of the newly started process
   * @throws ProcessManagerException
   */
  @Override
  public String createProcess(String componentId, String userId, String userRole,
      Map<String, ? extends Serializable> metadata) throws ProcessManagerException {
    // Default map for metadata is an empty map
    if (metadata == null) {
      metadata = Collections.emptyMap();
    }
    ProcessModel processModel = getProcessModel(componentId);
    XmlForm form = (XmlForm) getCreationForm(processModel);
    GenericDataRecord data = (GenericDataRecord) getEmptyCreationRecord(processModel, userRole);
    PagesContext pagesContext =
        new PagesContext("creationForm", "0", getLanguage(), true, componentId, userId);
    boolean versioningUsed = StringUtil.getBooleanValue(OrganizationControllerProvider.
        getOrganisationController().getComponentParameterValue(componentId, VERSION_MODE));
    pagesContext.setVersioningUsed(versioningUsed);

    // 1 - Populate form data (save file on disk, populate file field)
    List<String> attachmentIds = new ArrayList<>();

    // Populate file name and file content
    for (Map.Entry<String, ?> entry : metadata.entrySet()) {
      String fieldName = entry.getKey();
      Object fieldValue = entry.getValue();

      String fieldType = retrieveMatchingFieldTypeName(fieldValue);
      Field field = findMatchingField(form, data, fieldName, fieldType);

      if (fieldValue == null) {
        populateSimpleField(field, fieldName, null, fieldType);
      } else if (fieldValue instanceof Collection<?>) {
        populateListField(field, fieldName, (Collection<?>) fieldValue, fieldType);
      } else if (FileField.TYPE.equals(fieldType)) {
        attachmentIds.add(
            populateFileField(form, data, (FileField) field, fieldName, (FileContent) fieldValue,
                pagesContext));
      } else {
        populateSimpleField(field, fieldName, fieldValue, fieldType);
      }

    }

    // 2 - Create process instance
    String instanceId = createProcessInstance(processModel, userId, userRole, data);

    // 3 - Update attachment foreignkey
    // Attachment's foreignkey must be set with the just created instanceId
    for (String attachmentId : attachmentIds) {
      SimpleDocumentPK pk = new SimpleDocumentPK(attachmentId, componentId);
      SimpleDocument document = AttachmentServiceProvider.getAttachmentService().
          searchDocumentById(pk, null);
      document.setForeignId(instanceId);
      AttachmentServiceProvider.getAttachmentService().lock(attachmentId, userId, null);
      AttachmentServiceProvider.getAttachmentService().updateAttachment(document, false, false);
      AttachmentServiceProvider.getAttachmentService()
          .unlock(new UnlockContext(attachmentId, userId, null));
    }

    return instanceId;
  }

  /**
   * Retrieve and return the name of the data type, as expected by form templates in workflow
   * processing, from the Java data type of a given value object.
   * @param value the value object we want to set into a form field
   * @return the corresponding data type of value (or values if the argument is a data collection),
   * or return <code>null</code> if the value is an empty or null value (which means that any type
   * of field may match).
   * @throws ProcessManagerException if no matching data type exists for the given value.
   */
  private String retrieveMatchingFieldTypeName(Object value) throws ProcessManagerException {
    if (value == null) {
      return null;
    } else if (value instanceof String) {
      return TextField.TYPE;
    } else if (value instanceof Date) {
      return DateField.TYPE;
    } else if (value instanceof FileContent) {
      return FileField.TYPE;
    } else if (value instanceof Collection<?>) {
      Collection<?> col = (Collection<?>) value;
      if (col.isEmpty()) {
        return null;
      }
      return retrieveMatchingFieldTypeName(col.iterator().next());
    } else {
      throw new ProcessManagerException(DEFAULT_PROCESS_MANAGER_SERVICE, "processManager.FORM_FIELD_BAD_TYPE",
          "type: " + value.getClass().getName());
    }
  }

  /**
   * Find and return the matching field in the current form, knowing its name and type name. If
   * name
   * is
   * <code>null</code>, then search the first mandatory field of the right type.
   * @param form the form template.
   * @param data the instanciated current form.
   * @param name the searched name or <code>null</code> for looking for the first mandatory field
   * of
   * the given type.
   * @param typeName the searched type name.
   * @return the found field (never return <code>null</code>).
   * @throws ProcessManagerException if no field exists for the given name and type.
   */
  private Field findMatchingField(XmlForm form, GenericDataRecord data, String name,
      String typeName) throws ProcessManagerException {

    for (FieldTemplate fieldTemplate : form.getFieldTemplates()) {
      String fieldType = fieldTemplate.getTypeName();
      // special case : pdc is inserted as a text value
      if ("pdc".equals(fieldType)) {
        fieldType = "text";
      }
      String fieldName = fieldTemplate.getFieldName();

      if (((typeName == null) || fieldType.equals(typeName)) &&
          (fieldName.equals(name) || ((name == null) && fieldTemplate.isMandatory()))) {

        try {
          return data.getField(fieldName);
        } catch (FormException e) {
          throw new ProcessManagerException(DEFAULT_PROCESS_MANAGER_SERVICE,
              "processManager.FORM_FIELD_BAD_TYPE",
              FIELD_NAME + name + FIELD_TYPE + typeName, e);
        }
      }
    }

    throw new ProcessManagerException(DEFAULT_PROCESS_MANAGER_SERVICE, "processManager.FORM_FIELD_NOT_FOUND",
        FIELD_NAME + name + FIELD_TYPE + typeName);
  }

  /**
   * Transform and return a gievn value into a string value suitable for a form field, depending
   * with the given field type name.
   * @param value the value to convert into a string.
   * @param type the field type name.
   * @return the converetd string value.
   */
  private String getSimpleFieldValueString(Object value, String type) {
    if (value != null) {
      if (TextField.TYPE.equals(type)) {
        return value.toString();
      } else if (DateField.TYPE.equals(type)) {
        return (value instanceof Date) ? DateUtil.date2SQLDate((Date) value) : value.toString();
      }
    }
    return null;
  }

  /**
   * Fill a form field in with a given value.
   * @param field the field object to fill in.
   * @param name the name of the field.
   * @param value the value object (to be converted into a string value during the execution of
   * this
   * method).
   * @param type the type name of the field.
   * @throws ProcessManagerException if an error occurs while setting the value of the field
   * object.
   */
  private void populateSimpleField(Field field, String name, Object value, String type)
      throws ProcessManagerException {

    try {
      if (value == null) {
        field.setNull();
      } else {
        field.setStringValue(getSimpleFieldValueString(value, type));
      }

    } catch (FormException e) {
      throw new ProcessManagerException(DEFAULT_PROCESS_MANAGER_SERVICE, "processManager.FORM_FIELD_ERROR",
          FIELD_NAME + name + FIELD_TYPE + type, e);
    }
  }

  private String populateFileField(XmlForm form, GenericDataRecord data, FileField field,
      String name, FileContent content, PagesContext pagesContext) throws ProcessManagerException {

    // If metadata name is null, then look for a null-named text field in
    // order to set it with the file name, which is the default behavior of
    // specifying filename corresponding to a null-named file field.
    // -> Ascending compatibility
    if (name == null) {
      Field fileNameField;
      try {
        fileNameField = findMatchingField(form, data, null, TextField.TYPE);
      } catch (ProcessManagerException e) {
        // Ignore it in this case
        fileNameField = null;
      }
      if (fileNameField != null) {
        populateSimpleField(fileNameField, null, content.getName(), TextField.TYPE);
      }
    }
    // Then store the file content and attach it to the field
    String attachmentId =
        processUploadedFile(content.getContent(), content.getName(), pagesContext);
    field.setAttachmentId(attachmentId);
    return attachmentId;
  }

  private void populateListField(Field field, String name, Collection<?> values, String type)
      throws ProcessManagerException {
    try {
      StringBuilder valuesStr = new StringBuilder(512);
      for (Object value : values) {
        if (valuesStr.length() > 0) {
          valuesStr.append(',');
        }
        String str = getSimpleFieldValueString(value, type);
        if (str == null) {
          throw new ProcessManagerException(DEFAULT_PROCESS_MANAGER_SERVICE,
              "processManager.FORM_FIELD_COLLECTION_NOT_ALLOWED",
              FIELD_NAME + name + FIELD_TYPE + type);
        }
        valuesStr.append(str);
      }
      field.setStringValue(valuesStr.toString());

    } catch (FormException e) {
      throw new ProcessManagerException(DEFAULT_PROCESS_MANAGER_SERVICE, "processManager.FORM_FIELD_ERROR",
          FIELD_NAME + name + FIELD_TYPE + type, e);
    }
  }

  /**
   * Create a new process instance with the filled form.
   */
  private String createProcessInstance(ProcessModel processModel, String userId, String currentRole,
      DataRecord data) throws ProcessManagerException {
    try {
      Action creation = processModel.getCreateAction(currentRole);
      TaskDoneEvent event = getCreationTask(processModel, userId, currentRole).
          buildTaskDoneEvent(creation.getName(), data);
      Workflow.getWorkflowEngine().process(event);
      return event.getProcessInstance().getInstanceId();
    } catch (WorkflowException e) {
      throw new ProcessManagerException("SessionController",
          "processManager.CREATION_PROCESSING_FAILED", e);
    }
  }

  /**
   * Returns the form which starts a new instance.
   */
  private Form getCreationForm(ProcessModel processModel) throws ProcessManagerException {
    try {
      Action creation = processModel.getCreateAction("administrateur");
      return processModel.getPublicationForm(creation.getName(), "administrateur", getLanguage());
    } catch (WorkflowException e) {
      throw new ProcessManagerException("SessionController", "processManager.NO_CREATION_FORM", e);
    }
  }

  /**
   * Returns the an empty creation record which will be filled in with the creation form.
   */
  private DataRecord getEmptyCreationRecord(ProcessModel processModel, String currentRole)
      throws ProcessManagerException {
    try {
      Action creation = processModel.getCreateAction(currentRole);
      return processModel.getNewActionRecord(creation.getName(), currentRole, getLanguage(), null);
    } catch (WorkflowException e) {
      throw new ProcessManagerException(DEFAULT_PROCESS_MANAGER_SERVICE, "processManager.UNKNOWN_ACTION", e);
    }
  }

  /**
   * Returns the creation task.
   */
  private Task getCreationTask(ProcessModel processModel, String userId, String currentRole)
      throws ProcessManagerException {

    try {
      User user = new UserImpl(UserDetail.getById(userId));
      return Workflow.getTaskManager().getCreationTask(user, currentRole, processModel);
    } catch (WorkflowException e) {
      throw new ProcessManagerException(DEFAULT_PROCESS_MANAGER_SERVICE,
          "processManager.CREATION_TASK_UNAVAILABLE", e);
    }
  }

  /**
   * Returns the process model having the given id.
   */
  private ProcessModel getProcessModel(String modelId) throws ProcessManagerException {
    try {
      return Workflow.getProcessModelManager().getProcessModel(modelId);
    } catch (WorkflowException e) {
      throw new ProcessManagerException(DEFAULT_PROCESS_MANAGER_SERVICE,
          "processManager.UNKNOWN_PROCESS_MODEL", modelId, e);
    }
  }

  private String processUploadedFile(byte[] fileContent, String fileName,
      PagesContext pagesContext) {
    String attachmentId = null;
    String foreignId = pagesContext.getObjectId();
    String logicalName = fileName;
    if (StringUtil.isDefined(logicalName)) {
      logicalName = FileUtil.getFilename(fileName);
      String extension = FileRepositoryManager.getFileExtension(logicalName);
      String mimeType = FileUtil.getMimeType(logicalName);
      if (mimeType.equals("application/x-zip-compressed")) {
        if (MimeTypes.JAR_EXTENSION.equalsIgnoreCase(extension) || MimeTypes.WAR_EXTENSION.
            equalsIgnoreCase(extension) || MimeTypes.EAR_EXTENSION.equalsIgnoreCase(extension)) {
          mimeType = MimeTypes.JAVA_ARCHIVE_MIME_TYPE;
        } else if ("3D".equalsIgnoreCase(extension)) {
          mimeType = MimeTypes.SPINFIRE_MIME_TYPE;
        }
      }
      SimpleDocument ad =
          createSimpleDocument(foreignId, pagesContext.getComponentId(), logicalName, mimeType,
              fileContent, pagesContext.getUserId(),
              pagesContext.isVersioningUsed());
      ad.setDocumentType(DocumentType.attachment);
      attachmentId = ad.getId();
    }
    return attachmentId;
  }

  private SimpleDocument createSimpleDocument(String foreignId, String componentId, String fileName,
      String mimeType, byte[] content, String userId, boolean versioned) {

    // create AttachmentPK with spaceId and componentId
    SimpleDocumentPK simpleDocPk = new SimpleDocumentPK(null, componentId);
    SimpleAttachment attachment = SimpleAttachment.builder(getLanguage())
        .setFilename(fileName)
        .setTitle(fileName)
        .setDescription("")
        .setSize(content.length)
        .setContentType(mimeType)
        .setCreationData(userId, new Date())
        .build();
    SimpleDocument doc =
        new SimpleDocument(simpleDocPk, foreignId, 0, versioned, userId, attachment);
    return AttachmentServiceProvider.getAttachmentService()
        .createAttachment(doc, new ByteArrayInputStream(content));
  }

  private String getLanguage() {
    return FR_LANG;
  }
}
