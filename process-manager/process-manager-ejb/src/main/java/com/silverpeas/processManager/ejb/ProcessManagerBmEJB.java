/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
package com.silverpeas.processManager.ejb;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.fieldType.FileField;
import com.silverpeas.form.fieldType.TextField;
import com.silverpeas.form.form.XmlForm;
import com.silverpeas.form.record.GenericDataRecord;
import com.silverpeas.processManager.ProcessManagerException;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.workflow.api.Workflow;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.event.TaskDoneEvent;
import com.silverpeas.workflow.api.model.Action;
import com.silverpeas.workflow.api.model.ProcessModel;
import com.silverpeas.workflow.api.task.Task;
import com.silverpeas.workflow.api.user.User;
import com.silverpeas.workflow.engine.user.UserImpl;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.ejb.VersioningBm;
import com.stratelia.silverpeas.versioning.ejb.VersioningBmHome;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;

public class ProcessManagerBmEJB implements SessionBean {

  String currentRole = "supervisor";
  ProcessModel processModel = null;
  String userId = null;

  public String createProcess(String componentId, String userId, String fileName, byte[] fileContent)
	{
		String instanceId = "unknown";
		try {
			processModel = getProcessModel(componentId);
			this.userId = userId;
			XmlForm form = (XmlForm) getCreationForm();
			GenericDataRecord data = (GenericDataRecord) getEmptyCreationRecord();

			PagesContext pagesContext = new PagesContext("creationForm", "0", getLanguage(), true, componentId, userId);

			//versioning used ?
			OrganizationController controller = new OrganizationController();
			String paramVersion = controller.getComponentParameterValue(componentId, "versionControl");
			boolean versioningUsed = (StringUtil.isDefined(paramVersion) && !("no").equals(paramVersion.toLowerCase()));
			pagesContext.setVersioningUsed(versioningUsed);

			//1 - Populate form data (save file on disk, populate file field)
			FieldTemplate fieldTemplate;
			Field field;
			String attachmentId = null;
			boolean fileNameInserted = false;
			boolean fileInserted = false;
			for (int f=0; f<form.getFieldTemplates().size(); f++)
			{
				fieldTemplate = (FieldTemplate) form.getFieldTemplates().get(f);
				if (!fileNameInserted && fieldTemplate.getTypeName().equals(TextField.TYPE) && fieldTemplate.isMandatory())
				{
					field = data.getField(fieldTemplate.getFieldName());
					field.setValue(fileName);
					fileNameInserted = true;
				}
				else
				{
					if (!fileInserted && fieldTemplate.getTypeName().equals(FileField.TYPE))
					{
						field = data.getField(fieldTemplate.getFieldName());
						FileField fileField = (FileField) field;

						attachmentId = processUploadedFile(fileContent, fileName, pagesContext);

						fileField.setAttachmentId(attachmentId);

						fileInserted = true;
					}
				}
			}

			//2 - Create process instance
			instanceId = createProcessInstance(data);

			//3 - Update attachment foreignkey
			//Attachment's foreignkey must be set with the just created instanceId
			AttachmentPK 	attachmentPK 	= null;
			DocumentPK		documentPK		= null;
			VersioningUtil	versioningUtil	= null;
			List<String> attachmentIds = Arrays.asList(attachmentId);
			for (int a=0; a<attachmentIds.size(); a++)
			{
				attachmentId = (String) attachmentIds.get(a);

				if (versioningUsed)
				{
					if (versioningUtil == null)
						versioningUtil = new VersioningUtil();

					documentPK = new DocumentPK(Integer.parseInt(attachmentId), "useless", componentId);
					versioningUtil.updateDocumentForeignKey(documentPK, instanceId);
				}
				else
				{
					attachmentPK = new AttachmentPK(attachmentId, "useless", componentId);
					AttachmentController.updateAttachmentForeignKey(attachmentPK, instanceId);
				}
			}
		} catch (Exception e) {
			SilverTrace.error("processManager", "ProcessManagerBmEJB.createProcess", "root.MSG_GEN_ERROR", e);
		}

		return instanceId;
	}

  /**
   * Create a new process instance with the filled form.
   */
  private String createProcessInstance(DataRecord data)
      throws ProcessManagerException {
    try {
      Action creation = processModel.getCreateAction();
      TaskDoneEvent event = getCreationTask().buildTaskDoneEvent(
          creation.getName(), data);
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
  private Form getCreationForm() throws ProcessManagerException {
    try {
      Action creation = processModel.getCreateAction();
      return processModel.getPublicationForm(creation.getName(),
          "administrateur", getLanguage());
    } catch (WorkflowException e) {
      throw new ProcessManagerException("SessionController",
          "processManager.ERR_NO_CREATION_FORM", e);
    }
  }

  /**
   * Returns the an empty creation record which will be filled with the creation
   * form.
   */
  private DataRecord getEmptyCreationRecord() throws ProcessManagerException {
    try {
      Action creation = processModel.getCreateAction();
      return processModel.getNewActionRecord(creation.getName(), currentRole,
          getLanguage(), null);
    } catch (WorkflowException e) {
      throw new ProcessManagerException("SessionController",
          "processManager.UNKNOWN_ACTION", e);
    }
  }

  /**
   * Returns the creation task.
   */
  private Task getCreationTask() throws ProcessManagerException {
    try {
      OrganizationController controller = new OrganizationController();
      User user = new UserImpl(controller.getUserDetail(userId), null);
      Task creationTask = Workflow.getTaskManager().getCreationTask(user,
          currentRole, processModel);

      return creationTask;
    } catch (WorkflowException e) {
      throw new ProcessManagerException("SessionController",
          "processManager.CREATION_TASK_UNAVAILABLE", e);
    }
  }

  /**
   * Returns the process model having the given id.
   */
  private ProcessModel getProcessModel(String modelId)
      throws ProcessManagerException {
    try {
      return Workflow.getProcessModelManager().getProcessModel(modelId);
    } catch (WorkflowException e) {
      throw new ProcessManagerException("ProcessManagerSessionControler",
          "processManager.UNKNOWN_PROCESS_MODEL", modelId, e);
    }
  }

  private String processUploadedFile(byte[] fileContent, String fileName,
      PagesContext pagesContext) throws Exception {
    String attachmentId = null;

    String componentId = pagesContext.getComponentId();
    String userId = pagesContext.getUserId();
    String objectId = pagesContext.getObjectId();
    String logicalName = fileName;
    String physicalName = null;
    String mimeType = null;
    String context = "Images";
    File dir = null;
    long size = 0;
    VersioningUtil versioningUtil = new VersioningUtil();
    if (StringUtil.isDefined(logicalName)) {
      logicalName = logicalName.substring(logicalName
          .lastIndexOf(File.separator) + 1, logicalName.length());
      String type = FileRepositoryManager.getFileExtension(logicalName);
      mimeType = FileUtil.getMimeType(logicalName);
      if (mimeType.equals("application/x-zip-compressed")) {
        if (type.equalsIgnoreCase("jar") || type.equalsIgnoreCase("ear")
            || type.equalsIgnoreCase("war"))
          mimeType = "application/java-archive";
        else if (type.equalsIgnoreCase("3D"))
          mimeType = "application/xview3d-3d";
      }
      physicalName = new Long(new Date().getTime()).toString() + "." + type;

      String path = "";
      if (pagesContext.isVersioningUsed())
        path = versioningUtil.createPath("useless", componentId, "useless");
      else
        path = AttachmentController.createPath(componentId, context);
      dir = new File(path + physicalName);

      java.io.FileOutputStream fos = new java.io.FileOutputStream(dir);

      if (fileContent != null && fileContent.length > 0) {
        fos.write(fileContent);
        fos.close();

        size = dir.length();

        AttachmentDetail ad = createAttachmentDetail(objectId, componentId,
            physicalName, logicalName, mimeType, size, context, userId);

        if (pagesContext.isVersioningUsed()) {
          // mode versioning
          attachmentId = createDocument(objectId, ad);
        } else {
          // mode classique
          ad = AttachmentController.createAttachment(ad, true);
          attachmentId = ad.getPK().getId();
        }
      }
    }
    return attachmentId;
  }

  private AttachmentDetail createAttachmentDetail(String objectId,
      String componentId, String physicalName, String logicalName,
      String mimeType, long size, String context, String userId) {
    // create AttachmentPK with spaceId and componentId
    AttachmentPK atPK = new AttachmentPK(null, "useless", componentId);

    // create foreignKey with spaceId, componentId and id
    // use AttachmentPK to build the foreign key of customer object.
    AttachmentPK foreignKey = new AttachmentPK("-1", "useless", componentId);
    if (objectId != null)
      foreignKey.setId(objectId);

    // create AttachmentDetail Object
    AttachmentDetail ad = new AttachmentDetail(atPK, physicalName, logicalName,
        null, mimeType, size, context, new Date(), foreignKey);
    ad.setAuthor(userId);

    return ad;
  }

  private String createDocument(String objectId, AttachmentDetail attachment)
      throws RemoteException {
    String componentId = attachment.getPK().getInstanceId();
    int userId = Integer.parseInt(attachment.getAuthor());
    ForeignPK pubPK = new ForeignPK("-1", componentId);
    if (objectId != null)
      pubPK.setId(objectId);

    // Création d'un nouveau document
    DocumentPK docPK = new DocumentPK(-1, "useless", componentId);
    Document document = new Document(docPK, pubPK, attachment.getLogicalName(),
        "", -1, userId, new Date(), null, null, null, null, 0, 0);

    // document.setWorkList(getWorkers(componentId, userId));

    DocumentVersion version = new DocumentVersion(attachment);
    version.setAuthorId(userId);

    // et on y ajoute la première version
    version.setMajorNumber(1);
    version.setMinorNumber(0);
    version.setType(DocumentVersion.TYPE_PUBLIC_VERSION);
    version.setStatus(DocumentVersion.STATUS_VALIDATION_NOT_REQ);
    version.setCreationDate(new Date());

    docPK = getVersioningBm().createDocument(document, version);
    document.setPk(docPK);

    return docPK.getId();
  }

  private VersioningBm getVersioningBm() {
    VersioningBm versioningBm = null;
    try {
      VersioningBmHome vscEjbHome = (VersioningBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.VERSIONING_EJBHOME, VersioningBmHome.class);
      versioningBm = vscEjbHome.create();
    } catch (Exception e) {
      // NEED
      // throw new
      // ...RuntimeException("VersioningSessionController.initEJB()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT",e);
    }
    return versioningBm;
  }

  private String getLanguage() {
    return "fr";
  }

  public void ejbCreate() {
  }

  public void setSessionContext(SessionContext arg0) throws EJBException,
      RemoteException {
  }

  public void ejbRemove() throws EJBException, RemoteException {
  }

  public void ejbActivate() throws EJBException, RemoteException {
  }

  public void ejbPassivate() throws EJBException, RemoteException {
  }

}