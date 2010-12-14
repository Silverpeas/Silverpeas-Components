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
package com.stratelia.webactiv.kmelia.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.ejb.EJBObject;
import javax.ejb.RemoveException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;

import com.google.common.io.Closeables;
import com.silverpeas.attachment.importExport.AttachmentImportExport;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.fieldDisplayer.WysiwygFCKFieldDisplayer;
import com.silverpeas.form.record.GenericRecordSetManager;
import com.silverpeas.form.record.IdentifiedRecordTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.thumbnail.control.ThumbnailController;
import com.silverpeas.thumbnail.model.ThumbnailDetail;
import com.silverpeas.thumbnail.service.ThumbnailService;
import com.silverpeas.thumbnail.service.ThumbnailServiceImpl;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.ZipManager;
import com.silverpeas.util.clipboard.ClipboardSelection;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.versioning.importExport.VersioningImportExport;
import com.stratelia.silverpeas.alertUser.AlertUser;
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.service.CommentServiceFactory;
import com.stratelia.silverpeas.notificationManager.NotificationManager;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.silverpeas.util.SilverpeasSettings;
import com.stratelia.silverpeas.versioning.ejb.VersioningBm;
import com.stratelia.silverpeas.versioning.ejb.VersioningBmHome;
import com.stratelia.silverpeas.versioning.ejb.VersioningRuntimeException;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.model.Worker;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.silverpeas.wysiwyg.WysiwygException;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.ProfileInst;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.kmelia.FileImport;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBmHome;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.kmelia.model.PubliAuthorComparatorAsc;
import com.stratelia.webactiv.kmelia.model.PubliCreationDateComparatorAsc;
import com.stratelia.webactiv.kmelia.model.PubliImportanceComparatorDesc;
import com.stratelia.webactiv.kmelia.model.PubliUpdateDateComparatorAsc;
import com.stratelia.webactiv.kmelia.model.TopicDetail;
import com.stratelia.webactiv.kmelia.model.Treeview;
import com.stratelia.webactiv.kmelia.model.UserCompletePublication;
import com.stratelia.webactiv.kmelia.model.UserPublication;
import com.stratelia.webactiv.kmelia.model.updatechain.FieldParameter;
import com.stratelia.webactiv.kmelia.model.updatechain.FieldUpdateChainDescriptor;
import com.stratelia.webactiv.kmelia.model.updatechain.Fields;
import com.stratelia.webactiv.kmelia.model.updatechain.UpdateChainDescriptor;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBm;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBmHome;
import com.stratelia.webactiv.searchEngine.model.MatchingIndexEntry;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.WAAttributeValuePair;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentException;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.coordinates.model.Coordinate;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import com.stratelia.webactiv.util.indexEngine.model.IndexManager;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.node.model.NodeSelection;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.info.model.InfoDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoImageDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelPK;
import com.stratelia.webactiv.util.publication.model.Alias;
import com.stratelia.webactiv.util.publication.model.CompletePublication;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.publication.model.PublicationSelection;
import com.stratelia.webactiv.util.publication.model.ValidationStep;
import com.stratelia.webactiv.util.statistic.control.StatisticBm;
import com.stratelia.webactiv.util.statistic.control.StatisticBmHome;
import com.stratelia.webactiv.util.statistic.model.StatisticRuntimeException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.util.HashMap;
import java.util.Map;

public class KmeliaSessionController extends AbstractComponentSessionController {

  /* EJBs used by sessionController */
  private ThumbnailService thumbnailService = null;
  private SearchEngineBm searchEngineEjb = null;
  private CommentService commentService = null;
  private VersioningBm versioningBm = null;
  private PdcBm pdcBm = null;
  private StatisticBm statisticBm = null;
  private NotificationManager notificationManager = null;
  // Session objects
  private TopicDetail sessionTopic = null;
  private UserCompletePublication sessionPublication = null;
  private UserCompletePublication sessionClone = null;
  private String sessionPath = null; // html link with <a href="">
  private String sessionPathString = null; // html string only
  private TopicDetail sessionTopicToLink = null;
  private boolean sessionOwner = false;
  private List<UserPublication> sessionPublicationsList = null;
  private List<String> sessionCombination = null; // Specific Kmax
  private String sessionTimeCriteria = null; // Specific Kmax
  private List<NodeDetail> sessionTreeview = null;
  private String sortValue = "2";
  private String autoRedirectURL = null;
  private int nbPublicationsOnRoot = -1;
  private int rang = 0;
  private ResourceLocator publicationSettings = null;
  public final static String TAB_PREVIEW = "tabpreview";
  public final static String TAB_HEADER = "tabheader";
  public final static String TAB_CONTENT = "tabcontent";
  public final static String TAB_COMMENT = "tabcomments";
  public final static String TAB_ATTACHMENTS = "tabattachments";
  public final static String TAB_SEE_ALSO = "tabseealso";
  public final static String TAB_ACCESS_PATHS = "tabaccesspaths";
  public final static String TAB_READER_LIST = "tabreaderslist";
  public final static String TAB_PDC = "usepdc";
  // For import files
  public final static String UNITARY_IMPORT_MODE = "0";
  public final static String MASSIVE_IMPORT_MODE_ONE_PUBLICATION = "1";
  public final static String MASSIVE_IMPORT_MODE_MULTI_PUBLICATIONS = "2";
  public final static String FILETYPE_ZIP1 = "application/x-zip-compressed";
  public final static String FILETYPE_ZIP2 = "application/zip";
  // Versioning options
  public final static String VER_USE_WRITERS_AND_READERS = "0";
  public final static String VER_USE_WRITERS = "1";
  public final static String VER_USE_READERS = "2";
  public final static String VER_USE_NONE = "3";
  // For Office files direct update
  public final static String NO_UPDATE_MODE = "0";
  public final static String UPDATE_DIRECT_MODE = "1";
  public final static String UPDATE_SHORTCUT_MODE = "2";
  // utilisation de userPanel/ userpanelPeas
  String[] idSelectedUser = null;
  // pagination de la liste des publications
  private int indexOfFirstPubToDisplay = 0;
  private int nbPublicationsPerPage = -1;
  // Assistant de publication
  private String wizard = "none";
  private String wizardRow = "0";
  private String wizardLast = "0";
  // Specific for Kmax
  private List<Integer> timeAxis = null;
  private List<String> currentCombination = null;
  public boolean isKmaxMode = false;
  // i18n
  private String currentLanguage = null;
  private AdminController m_AdminCtrl = null;
  // sauvegarde pour mise à jour à la chaine
  Fields saveFields = new Fields();
  boolean isDragAndDropEnableByUser = false;

  /** Creates new sessionClientController */
  public KmeliaSessionController(MainSessionController mainSessionCtrl, ComponentContext context) {
    super(mainSessionCtrl, context, "com.stratelia.webactiv.kmelia.multilang.kmeliaBundle",
        "com.stratelia.webactiv.kmelia.settings.kmeliaIcons",
        "com.stratelia.webactiv.kmelia.settings.kmeliaSettings");
    init();
  }

  private void init() {
    // Remove all data store by this SessionController
    removeSessionObjects();

    currentLanguage = getLanguage();
    if ("yes".equals(getSettings().getString("massiveDragAndDropAllowed"))) {
      try {
        isDragAndDropEnableByUser = isDragAndDropEnableByUser();
      } catch (RemoteException e) {
        isDragAndDropEnableByUser = false;
      }
    }
  }

  /**
   * Gets a business service of comments.
   * @return a CommentService instance.
   */
  protected CommentService getCommentService() {
    if (commentService == null) {
      commentService = CommentServiceFactory.getFactory().getCommentService();
    }
    return commentService;
  }

  public KmeliaBm getKmeliaBm() {
    try {
      KmeliaBmHome kscEjbHome =
          (KmeliaBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.KMELIABM_EJBHOME,
              KmeliaBmHome.class);
      return kscEjbHome.create();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.getKmeliaBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public StatisticBm getStatisticBm() {
    if (statisticBm == null) {
      try {
        StatisticBmHome statisticHome =
            (StatisticBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.STATISTICBM_EJBHOME,
                StatisticBmHome.class);
        statisticBm = statisticHome.create();
      } catch (Exception e) {
        throw new StatisticRuntimeException("KmeliaSessionController.getStatisticBm()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }

    return statisticBm;
  }

  private VersioningBm getVersioningBm() {
    if (versioningBm == null) {
      try {
        VersioningBmHome vscEjbHome =
            (VersioningBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.VERSIONING_EJBHOME,
                VersioningBmHome.class);
        versioningBm = vscEjbHome.create();
      } catch (Exception e) {
        throw new VersioningRuntimeException("KmeliaSessionController.getVersioningBm()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return versioningBm;
  }

  public ResourceLocator getPublicationSettings() {
    if (publicationSettings == null) {
      publicationSettings =
          new ResourceLocator("com.stratelia.webactiv.util.publication.publicationSettings",
              getLanguage());
    }
    return publicationSettings;
  }

  /*
   * public ResourceLocator getSettings() { return settings; }
   */
  public SearchEngineBm getSearchEngine() {
    if (this.searchEngineEjb == null) {
      try {
        SearchEngineBmHome home =
            (SearchEngineBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.SEARCHBM_EJBHOME,
                SearchEngineBmHome.class);
        this.searchEngineEjb = home.create();
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaSessionController.getSearchEngine()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return this.searchEngineEjb;
  }

  public int getNbPublicationsOnRoot() {
    if (nbPublicationsOnRoot == -1) {
      String parameterValue = getComponentParameterValue("nbPubliOnRoot");
      if (StringUtil.isDefined(parameterValue)) {
        nbPublicationsOnRoot = new Integer(parameterValue).intValue();
      } else {
        if (KmeliaHelper.isToolbox(getComponentId())) {
          nbPublicationsOnRoot = 0;
        } else {
          // lecture du properties
          nbPublicationsOnRoot =
              SilverpeasSettings.readInt(getSettings(), "HomeNbPublications", 15);
        }
      }
    }
    return nbPublicationsOnRoot;
  }

  public int getNbPublicationsPerPage() {
    if (nbPublicationsPerPage == -1) {
      String parameterValue = this.getComponentParameterValue("nbPubliPerPage");
      if (parameterValue == null || parameterValue.length() <= 0) {
        nbPublicationsPerPage =
            SilverpeasSettings.readInt(getSettings(), "NbPublicationsParPage", 10);
      } else {
        try {
          nbPublicationsPerPage = new Integer(parameterValue).intValue();
        } catch (Exception e) {
          nbPublicationsPerPage =
              SilverpeasSettings.readInt(getSettings(), "NbPublicationsParPage", 10);
        }
      }
    }
    return nbPublicationsPerPage;
  }

  public boolean isDraftVisibleWithCoWriting() {
    return SilverpeasSettings.readBoolean(getSettings(), "draftVisibleWithCoWriting", false);
  }

  public boolean isTreeStructure() {
    String param = getComponentParameterValue("istree");
    if (!StringUtil.isDefined(param)) {
      return true;
    }
    return "yes".equalsIgnoreCase(param);
  }

  public boolean isPdcUsed() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("usepdc"));
  }

  public boolean isDraftEnabled() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("draft"));
  }

  public boolean isOrientedWebContent() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("webContent"));
  }

  public boolean isSortedTopicsEnabled() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("sortedTopics"));
  }

  public boolean isTopicManagementDelegated() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("delegatedTopicManagement"));
  }

  public boolean isAuthorUsed() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("useAuthor"));
  }

  public boolean openSingleAttachmentAutomatically() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("openSingleAttachment"));
  }

  public boolean isTreeviewEnabled() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("useTreeview"));
  }

  public boolean isImportFileAllowed() {
    String parameterValue = this.getComponentParameterValue("importFiles");
    if (parameterValue == null || parameterValue.length() <= 0) {
      return false;
    } else {
      if ("1".equalsIgnoreCase(parameterValue) || "3".equalsIgnoreCase(parameterValue)) {
        return true;
      } else {
        return false;
      }
    }
  }

  public boolean isImportFilesAllowed() {
    String parameterValue = this.getComponentParameterValue("importFiles");
    if (parameterValue == null || parameterValue.length() <= 0) {
      return false;
    } else {
      if ("2".equalsIgnoreCase(parameterValue) || "3".equalsIgnoreCase(parameterValue)) {
        return true;
      } else {
        return false;
      }
    }
  }

  public boolean isExportZipAllowed() {
    String parameterValue = this.getComponentParameterValue("exportComponent");
    if (parameterValue == null || parameterValue.length() <= 0) {
      return false;
    } else {
      if ("yes".equalsIgnoreCase(parameterValue) || "both".equalsIgnoreCase(parameterValue)) {
        return true;
      } else {
        return false;
      }
    }
  }

  public boolean isExportPdfAllowed() {
    String parameterValue = this.getComponentParameterValue("exportComponent");
    if (parameterValue == null || parameterValue.length() <= 0) {
      return false;
    } else {
      if ("pdf".equalsIgnoreCase(parameterValue) || "both".equalsIgnoreCase(parameterValue)) {
        return true;
      } else {
        return false;
      }
    }
  }

  public boolean isUpdateOfficeFilesUsed() {
    String parameterValue = this.getComponentParameterValue("useModifyOfficeFiles");
    if (parameterValue == null || parameterValue.length() <= 0) {
      return false;
    } else {
      if ("1".equalsIgnoreCase(parameterValue)) {
        return true;
      } else if ("2".equalsIgnoreCase(parameterValue)) {
        return true;
      } else {
        return false;
      }
    }
  }

  public String getUpdateOfficeMode() {
    String parameterValue = this.getComponentParameterValue("useModifyOfficeFiles");
    if (parameterValue == null || parameterValue.length() <= 0) {
      return NO_UPDATE_MODE;
    } else {
      if (UPDATE_DIRECT_MODE.equalsIgnoreCase(parameterValue)) {
        return UPDATE_DIRECT_MODE;
      } else if (UPDATE_SHORTCUT_MODE.equalsIgnoreCase(parameterValue)) {
        return UPDATE_SHORTCUT_MODE;
      } else {
        return NO_UPDATE_MODE;
      }
    }
  }

  public boolean isExportComponentAllowed() {
    return "yes".equals(getSettings().getString("exportComponentAllowed"));
  }

  public boolean isMassiveDragAndDropAllowed() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("massiveDragAndDrop"));
  }

  public boolean isPublicationAlwaysVisibleEnabled() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("publicationAlwaysVisible"));
  }

  public boolean isWizardEnabled() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("wizardEnabled"));
  }

  public boolean displayNbPublis() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("displayNB"));
  }

  public boolean isRightsOnTopicsEnabled() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("rightsOnTopics"));
  }

  public boolean isFoldersLinkedEnabled() {
    return "yes".equals(getComponentParameterValue("isLink"));
  }

  public boolean attachmentsInPubList() {
    return "yes".equals(getComponentParameterValue("attachmentsInPubList"));
  }

  public boolean isPublicationIdDisplayed() {
    return "yes".equals(getComponentParameterValue("codification"));
  }

  public boolean isSuppressionOnlyForAdmin() {
    return "yes".equals(getComponentParameterValue("suppressionOnlyForAdmin"));
  }

  public boolean isThumbnailMandatory() {
    return "yes".equals(getComponentParameterValue("thumbnailMandatory"));
  }

  public boolean isContentEnabled() {
    String parameterValue = getComponentParameterValue("tabContent");
    if (!StringUtil.isDefined(parameterValue)) {
      return false;
    } else {
      return "yes".equals(parameterValue.toLowerCase());
    }
  }

  public boolean isSeeAlsoEnabled() {
    String parameterValue = getComponentParameterValue("tabSeeAlso");
    if (!StringUtil.isDefined(parameterValue)) {
      return false;
    } else {
      return "yes".equals(parameterValue.toLowerCase());
    }
  }

  public boolean showUserNameInList() {
    return SilverpeasSettings.readBoolean(getSettings(), "showUserNameInList", true);
  }

  /**
   * @return
   */
  public String getVersionningFileRightsMode() {
    String parameterValue = this.getComponentParameterValue("versionUseFileRights");
    if (parameterValue == null || parameterValue.length() <= 0) {
      return VER_USE_WRITERS_AND_READERS;
    } else {
      return parameterValue;
    }
  }

  public List<String> getInvisibleTabs() throws RemoteException {
    List<String> invisibleTabs = new ArrayList<String>(0);

    if (!isPdcUsed()) {
      invisibleTabs.add("usepdc");
    }

    if (!isContentEnabled()) {
      invisibleTabs.add("tabcontent");
    }

    if (isToolbox()) {
      invisibleTabs.add(KmeliaSessionController.TAB_PREVIEW);
    }

    String parameterValue = this.getComponentParameterValue("tabAttachments");
    if (!isToolbox()) {
      // attachments tab is always visible with toolbox
      if (!StringUtil.isDefined(parameterValue)) {
        // invisibleTabs.add("tabattachments");
      } else {
        if (!"yes".equals(parameterValue.toLowerCase())) {
          invisibleTabs.add("tabattachments");
        }
      }
    }

    if (!isSeeAlsoEnabled()) {
      invisibleTabs.add("tabseealso");
    }

    parameterValue = this.getComponentParameterValue("tabAccessPaths");
    if (parameterValue == null || parameterValue.length() <= 0) {
      // invisibleTabs.add("tabaccesspaths");
    } else {
      if (!"yes".equals(parameterValue.toLowerCase())) {
        invisibleTabs.add("tabaccesspaths");
      }
    }

    parameterValue = this.getComponentParameterValue("tabReadersList");
    if (parameterValue == null || parameterValue.length() <= 0) {
      // invisibleTabs.add("tabreaderslist");
    } else {
      if (!"yes".equals(parameterValue.toLowerCase())) {
        invisibleTabs.add("tabreaderslist");
      }
    }

    parameterValue = this.getComponentParameterValue("tabComments");
    if (parameterValue == null || parameterValue.length() <= 0) {
      invisibleTabs.add("tabcomments");
    } else {
      if (!"yes".equals(parameterValue.toLowerCase())) {
        invisibleTabs.add("tabcomments");
      }
    }

    return invisibleTabs;
  }

  public String generatePdf(String pubID) {
    SilverTrace.info("kmelia", "KmeliaSessionControl.generatePdf", "root.MSG_ENTRY_METHOD");
    String nameFilePdf = "";
    if (pubID != null) {
      CompletePublication complete = null;
      try {
        complete = getKmeliaBm().getPublicationBm().getCompletePublication(getPublicationPK(pubID));
        nameFilePdf = StringUtil.toAcceptableFilename(getPublicationPdfName(pubID));
      } catch (Exception e) {
        SilverTrace.info("kmelia", "KmeliaSessionControl.generatePdf",
            "kmelia.MSG_RETURN_COMPLETE_LIST_OF_PUBLI", "pubId=" + pubID);
      }
      ResourceLocator resourceGeneral = new ResourceLocator("com.stratelia.webactiv.general", "");
      String tempDir = resourceGeneral.getString("tempPath");
      FileOutputStream out = null;
      try {
        out = new FileOutputStream(new File(tempDir, nameFilePdf));
        PdfGenerator pdfGenerator = new PdfGenerator();
        pdfGenerator.generate(out, complete, this);
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaSessionController.setKmeliaBm()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      } finally {
        Closeables.closeQuietly(out);
      }
    }
    return nameFilePdf;
  }

  /************************************************************************************************/
  // Current User operations
  /***********************************************************************************************
   * /**
   * @return
   * @throws RemoteException
   */
  public String getProfile() throws RemoteException {
    return getUserTopicProfile();
  }

  public String getUserTopicProfile() throws RemoteException {
    return getUserTopicProfile(null);
  }

  public String getUserTopicProfile(String id) throws RemoteException {
    if (!isRightsOnTopicsEnabled()) {
      return KmeliaHelper.getProfile(getUserRoles());
    }

    NodeDetail node = null;
    if (StringUtil.isDefined(id)) {
      node = getKmeliaBm().getNodeHeader(id, getComponentId());
    } else if (getSessionTopic() != null) {
      node = getSessionTopic().getNodeDetail();
    }

    // check if we have to take care of topic's rights
    if (node != null && node.haveRights()) {
      int rightsDependsOn = node.getRightsDependsOn();
      return KmeliaHelper.getProfile(getOrganizationController().getUserProfiles(getUserId(),
          getComponentId(), rightsDependsOn, ObjectType.NODE));
    } else {
      return KmeliaHelper.getProfile(getUserRoles());
    }
  }

  public List<String> getUserIdsOfTopic() {
    if (!isRightsOnTopicsEnabled()) {
      return null;
    }

    NodeDetail node = null;
    if (getSessionTopic() != null) {
      node = getSessionTopic().getNodeDetail();
    }

    // check if we have to take care of topic's rights
    if (node != null && node.haveRights()) {
      int rightsDependsOn = node.getRightsDependsOn();
      List<String> profileNames = new ArrayList<String>();
      profileNames.add(KmeliaHelper.ROLE_ADMIN);
      profileNames.add(KmeliaHelper.ROLE_PUBLISHER);
      profileNames.add(KmeliaHelper.ROLE_WRITER);
      profileNames.add(KmeliaHelper.ROLE_READER);
      String[] userIds =
          getOrganizationController().getUsersIdsByRoleNames(getComponentId(),
              Integer.toString(rightsDependsOn), ObjectType.NODE, profileNames);
      return Arrays.asList(userIds);
    } else {
      return null;
    }
  }

  public boolean isCurrentTopicAvailable() {
    if (isRightsOnTopicsEnabled() && getSessionTopic().getNodeDetail().haveRights()) {
      int rightsDependsOn = getSessionTopic().getNodeDetail().getRightsDependsOn();
      return getOrganizationController().isObjectAvailable(rightsDependsOn, ObjectType.NODE,
          getComponentId(), getUserId());
    }
    return true;
  }

  public boolean isUserComponentAdmin() {
    return "admin".equalsIgnoreCase(KmeliaHelper.getProfile(getUserRoles()));
  }

  /**************************************************************************************/
  /* KMelia - Gestion des thèmes */
  /**************************************************************************************/
  public synchronized TopicDetail getTopic(String id) throws RemoteException {
    return getTopic(id, true);
  }

  public synchronized TopicDetail getTopic(String id, boolean resetSessionPublication)
      throws RemoteException {
    if (resetSessionPublication) {
      setSessionPublication(null);
    }
    if (getSessionTopic() == null
        || !id.equals(getSessionTopic().getNodeDetail().getNodePK().getId())) {
      indexOfFirstPubToDisplay = 0;
    }

    TopicDetail currentTopic = null;
    if (isUserComponentAdmin()) {
      currentTopic =
          getKmeliaBm().goTo(getNodePK(id), getUserId(), isTreeStructure(), "admin", false);
    } else {
      currentTopic =
          getKmeliaBm().goTo(getNodePK(id), getUserId(), isTreeStructure(),
              getUserTopicProfile(id), isRightsOnTopicsEnabled());
    }

    List<NodeDetail> treeview = null;
    if (isTreeviewEnabled() || displayNbPublis()) {
      if (isUserComponentAdmin()) {
        treeview =
            getKmeliaBm().getTreeview(getNodePK("0"), "admin", isCoWritingEnable(),
                isDraftVisibleWithCoWriting(), getUserId(), displayNbPublis(), false);
      } else {
        treeview =
            getKmeliaBm().getTreeview(getNodePK("0"), getProfile(), isCoWritingEnable(),
                isDraftVisibleWithCoWriting(), getUserId(), displayNbPublis(),
                isRightsOnTopicsEnabled());
      }
      setSessionTreeview(treeview);
    }
    if (displayNbPublis()) {
      List<NodeDetail> children =
          (List<NodeDetail>) currentTopic.getNodeDetail().getChildrenDetails();
      for (int n = 0; n < children.size(); n++) {
        NodeDetail node = children.get(n);
        if (node != null) {
          int index = treeview.indexOf(node);
          if (index != -1) {
            NodeDetail nodeTreeview = treeview.get(index);

            if (nodeTreeview != null) {
              node.setNbObjects(nodeTreeview.getNbObjects());
            }
          }
        }
      }
    }
    setSessionTopic(currentTopic);
    applyVisibilityFilter();
    return currentTopic;
  }

  public synchronized TopicDetail getPublicationTopic(String pubId) throws RemoteException {
    TopicDetail currentTopic =
        getKmeliaBm().getPublicationFather(getPublicationPK(pubId), isTreeStructure(), getUserId(),
            isRightsOnTopicsEnabled());
    setSessionTopic(currentTopic);
    applyVisibilityFilter();
    return currentTopic;
  }

  public synchronized List<NodeDetail> getAllTopics() throws RemoteException {
    return getNodeBm().getSubTree(getNodePK("0"));
  }

  public synchronized List<NodeDetail> getTreeview() throws RemoteException {
    if (isTreeviewEnabled()) {
      return getSessionTreeview();// getKmeliaBm().getTreeview(getNodePK("0"), getProfile(),
    } // isCoWritingEnable(), isDraftVisibleWithCoWriting(),
    // getUserId(), displayNbPublis());
    return null;
  }

  public synchronized void flushTrashCan() throws RemoteException {
    SilverTrace.info("kmelia", "KmeliaSessionControl.flushTrashCan", "root.MSG_ENTRY_METHOD");
    TopicDetail td =
        getKmeliaBm().goTo(getNodePK("1"), getUserId(), false, getUserTopicProfile("1"),
            isRightsOnTopicsEnabled());
    Collection<UserPublication> pds = td.getPublicationDetails();
    Iterator<UserPublication> ipds = pds.iterator();

    SilverTrace.info("kmelia", "KmeliaSessionControl.flushTrashCan", "root.MSG_PARAM_VALUE",
        "NbPubli=" + pds.size());
    while (ipds.hasNext()) {
      String theId = (ipds.next()).getPublication().getPK().getId();
      SilverTrace.info("kmelia", "KmeliaSessionControl.flushTrashCan", "root.MSG_PARAM_VALUE",
          "Deleting Publi #" + theId);
      deletePublication(theId);
    }
    indexOfFirstPubToDisplay = 0;
  }

  public synchronized NodePK updateTopicHeader(NodeDetail nd, String alertType)
      throws RemoteException {
    nd.getNodePK().setSpace(getSpaceId());
    nd.getNodePK().setComponentName(getComponentId());
    return getKmeliaBm().updateTopic(nd, alertType);
  }

  public synchronized NodeDetail getSubTopicDetail(String subTopicId) throws RemoteException {
    return getKmeliaBm().getSubTopicDetail(getNodePK(subTopicId));
  }

  public synchronized NodePK addSubTopic(NodeDetail nd, String alertType, String parentId)
      throws RemoteException {
    nd.getNodePK().setSpace(getSpaceId());
    nd.getNodePK().setComponentName(getComponentId());
    nd.setCreatorId(getUserId());
    // nd.setLanguage(getLanguage());
    return getKmeliaBm().addSubTopic(getNodePK(parentId), nd, alertType);
  }

  public synchronized void deleteTopic(String topicId) throws RemoteException {
    NodeDetail node = getNodeHeader(topicId);
    // check if user is allowed to delete this topic
    if (SilverpeasRole.admin.isInRole(getUserTopicProfile(topicId)) ||
        SilverpeasRole.admin.isInRole(getUserTopicProfile(node.getFatherPK().getId()))) {
      // First, remove rights on topic and its descendants
      List<NodeDetail> treeview = getNodeBm().getSubTree(getNodePK(topicId));
      for (int n = 0; n < treeview.size(); n++) {
        node = treeview.get(n);

        deleteTopicRoles(node);
      }

      // Then, remove the topic itself
      getKmeliaBm().deleteTopic(getNodePK(topicId));
    }
  }

  public synchronized void changeSubTopicsOrder(String way, String subTopicId)
      throws RemoteException {
    getKmeliaBm().changeSubTopicsOrder(way, getNodePK(subTopicId), getSessionTopic().getNodePK());
  }

  public synchronized void changeTopicStatus(String newStatus, String topicId,
      boolean recursiveChanges) throws RemoteException {
    getKmeliaBm().changeTopicStatus(newStatus, getNodePK(topicId), recursiveChanges);
  }

  /**************************************************************************************/
  /* KMelia - Gestion des abonnements */
  /**************************************************************************************/
  public synchronized Collection getSubscriptionList() throws RemoteException {
    return getKmeliaBm().getSubscriptionList(getUserId(), getComponentId());
  }

  public synchronized void removeSubscription(String topicId) throws RemoteException {
    getKmeliaBm().removeSubscriptionToCurrentUser(getNodePK(topicId), getUserId());
  }

  public synchronized void addSubscription(String topicId) throws RemoteException {
    getKmeliaBm().addSubscription(getNodePK(topicId), getUserId());
  }

  /**************************************************************************************/
  /* KMelia - Gestion des publications */
  /**************************************************************************************/
  public synchronized PublicationDetail getPublicationDetail(String pubId) throws RemoteException {
    return getKmeliaBm().getPublicationDetail(getPublicationPK(pubId));
  }

  public synchronized Collection<Collection<NodeDetail>> getPathList(String pubId)
      throws RemoteException {
    return getKmeliaBm().getPathList(getPublicationPK(pubId));
  }

  public synchronized Collection<NodePK> getPublicationFathers(String pubId) throws RemoteException {
    return getKmeliaBm().getPublicationFathers(getPublicationPK(pubId));
  }

  public synchronized String createPublication(PublicationDetail pubDetail) throws RemoteException {
    pubDetail.getPK().setSpace(getSpaceId());
    pubDetail.getPK().setComponentName(getComponentId());
    pubDetail.setCreatorId(getUserId());
    pubDetail.setCreationDate(new Date());

    String result = null;
    if (isKmaxMode) {
      result = getKmeliaBm().createKmaxPublication(pubDetail);
    } else {
      result = getKmeliaBm().createPublicationIntoTopic(pubDetail, getSessionTopic().getNodePK());
    }
    SilverTrace.info("kmelia", "KmeliaSessionController.createPublication(pubDetail)",
        "Kmelia.MSG_ENTRY_METHOD");
    SilverTrace.spy("kmelia", "KmeliaSessionController.createPublication(pubDetail)", getSpaceId(),
        getComponentId(), result, getUserDetail().getId(), SilverTrace.SPY_ACTION_CREATE);
    return result;
  }

  public synchronized String createPublicationIntoTopic(PublicationDetail pubDetail, String fatherId)
      throws RemoteException {
    pubDetail.getPK().setSpace(getSpaceId());
    pubDetail.getPK().setComponentName(getComponentId());
    pubDetail.setCreatorId(getUserId());
    pubDetail.setCreationDate(new Date());

    String result = getKmeliaBm().createPublicationIntoTopic(pubDetail, getNodePK(fatherId));
    SilverTrace.spy("kmelia",
        "KmeliaSessionController.createPublicationIntoTopic(pubDetail, fatherId)", getSpaceId(),
        getComponentId(), result, getUserDetail().getId(), SilverTrace.SPY_ACTION_CREATE);
    return result;
  }

  public synchronized void updatePublication(PublicationDetail pubDetail) throws RemoteException {
    pubDetail.getPK().setSpace(getSpaceId());
    pubDetail.getPK().setComponentName(getComponentId());
    pubDetail.setUpdaterId(getUserId());

    SilverTrace.info("kmelia", "KmeliaSessionController.updatePublication(pubDetail)",
        "root.MSG_GEN_PARAM_VALUE", "isPublicationAlwaysVisibleEnabled() = "
            + isPublicationAlwaysVisibleEnabled());
    SilverTrace.info("kmelia", "KmeliaSessionController.updatePublication(pubDetail)",
        "root.MSG_GEN_PARAM_VALUE", "'writer'.equals(KmeliaHelper.getProfile(getUserRoles())) = "
            + "writer".equals(KmeliaHelper.getProfile(getUserRoles())));
    SilverTrace.info("kmelia", "KmeliaSessionController.updatePublication(pubDetail)",
        "root.MSG_GEN_PARAM_VALUE", "(getSessionClone() == null) = " + (getSessionClone() == null));
    if (isCloneNeeded()) {
      clonePublication(pubDetail);
    } else {
      if (getSessionTopic() == null || getSessionTopic().getNodePK().getId().equals("1")) {
        // la publication est dans la corbeille
        pubDetail.setIndexOperation(IndexManager.NONE);
      }

      if (getSessionClone() != null) {
        if (getSessionClone().getId().equals(pubDetail.getId())) {
          // update the clone, clone stay in same status
          pubDetail.setStatusMustBeChecked(false);
        }
      }
      getKmeliaBm().updatePublication(pubDetail);
    }
    SilverTrace.spy("kmelia", "KmeliaSessionController.updatePublication", getSpaceId(),
        getComponentId(),
        pubDetail.getId(), getUserDetail().getId(), SilverTrace.SPY_ACTION_UPDATE);
  }

  public boolean isCloneNeeded() throws RemoteException {
    String currentStatus =
        getSessionPublication().getPublication().getPublicationDetail().getStatus();
    return (isPublicationAlwaysVisibleEnabled()
        && "writer".equals(getUserTopicProfile()) && (getSessionClone() == null) && PublicationDetail.VALID
        .
        equals(currentStatus));
  }

  public boolean isCloneNeededWithDraft() {
    return (isPublicationAlwaysVisibleEnabled() && (getSessionClone() == null));
  }

  public String clonePublication() {
    return clonePublication(null);
  }

  public String clonePublication(PublicationDetail pubDetail) {
    String cloneStatus = PublicationDetail.TO_VALIDATE;
    if (isDraftEnabled()) {
      cloneStatus = PublicationDetail.DRAFT;
    }
    return clonePublication(pubDetail, cloneStatus);
  }

  /**
   * Clone current publication. Create new publication based on pubDetail object if not null or
   * sessionPublication otherwise. Original publication must not be modified (except references to
   * clone : cloneId and cloneStatus).
   * @param pubDetail If not null, attribute values are set to the clone
   * @param nextStatus Draft or ToValidate
   * @return
   */
  private String clonePublication(PublicationDetail pubDetail, String nextStatus) {
    String cloneId = null;
    // récupération de la publi de référence
    CompletePublication refPubComplete = getSessionPublication().getPublication();
    try {
      cloneId = getKmeliaBm().clonePublication(refPubComplete, pubDetail, nextStatus);
      setSessionClone(getUserCompletePublication(cloneId));
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.clonePublication",
          SilverpeasException.ERROR, "kmelia.CANT_CLONE_PUBLICATION", e);
    }
    return cloneId;
  }

  public synchronized void deletePublication(String pubId) throws RemoteException {
    deletePublication(pubId, false);
  }

  public synchronized void deletePublication(String pubId, boolean kmaxMode) throws RemoteException {
    // récupération de la position de la publication pour savoir s'il elle se trouve déjà dans
    // la corbeille node=1
    // s'il elle se trouve déjà au node 1, il est nécessaire de supprimer les fichier joints
    // sinon non
    String nodeId = "";
    if (getSessionTopic() != null) {
      nodeId = getSessionTopic().getNodeDetail().getNodePK().getId();
    }
    if ("1".equals(nodeId)) {
      // la publication sera supprimée définitivement, il faut donc supprimer les fichiers joints
      try {
        WysiwygController.deleteWysiwygAttachments(getSpaceId(), getComponentId(), pubId);
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaSessionController.deletePublication",
            SilverpeasRuntimeException.ERROR, "root.EX_DELETE_ATTACHMENT_FAILED", e);
      }

      removeXMLContentOfPublication(getPublicationPK(pubId));
      getKmeliaBm().deletePublication(getPublicationPK(pubId));
    } else {
      getKmeliaBm().sendPublicationToBasket(getPublicationPK(pubId), kmaxMode);
    }
    SilverTrace.spy("kmelia", "KmeliaSessionController.deletePublication", getSpaceId(),
        getComponentId(), pubId, getUserDetail().getId(), SilverTrace.SPY_ACTION_DELETE);
  }

  public synchronized void deleteClone() throws RemoteException {
    if (getSessionClone() != null) {
      // supprime le clone
      String cloneId = getSessionClone().getPublication().getPublicationDetail().getPK().getId();
      PublicationPK clonePK = getPublicationPK(cloneId);

      removeXMLContentOfPublication(clonePK);
      getKmeliaBm().deletePublication(clonePK);

      setSessionClone(null);
      refreshSessionPubliAndClone();

      // supprime les références au clone
      PublicationDetail pubDetail = getSessionPublication().getPublication().getPublicationDetail();
      pubDetail.setCloneId(null);
      pubDetail.setCloneStatus(null);
      pubDetail.setStatusMustBeChecked(false);
      pubDetail.setUpdateDateMustBeSet(false);

      getKmeliaBm().updatePublication(pubDetail);

      SilverTrace.spy("kmelia", "KmeliaSessionController.deleteClone", getSpaceId(),
          getComponentId(), cloneId, getUserDetail().getId(), SilverTrace.SPY_ACTION_DELETE);
    }
  }

  private void removeXMLContentOfPublication(PublicationPK pubPK) throws RemoteException {
    try {
      PublicationDetail pubDetail = getKmeliaBm().getPublicationDetail(pubPK);
      String infoId = pubDetail.getInfoId();
      if (!isInteger(infoId)) {
        String xmlFormShortName = infoId;

        PublicationTemplate pubTemplate =
            getPublicationTemplateManager().getPublicationTemplate(
                pubDetail.getPK().getInstanceId()
                    + ":" + xmlFormShortName);

        RecordSet set = pubTemplate.getRecordSet();
        DataRecord data = set.getRecord(pubDetail.getPK().getId());
        set.delete(data);
      }
    } catch (PublicationTemplateException e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.removeXMLContentOfPublication()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LE_CONTENU_XML", e);
    } catch (FormException e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.removeXMLContentOfPublication()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LE_CONTENU_XML", e);
    }
  }

  private static boolean isInteger(String id) {
    try {
      Integer.parseInt(id);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public synchronized void addPublicationToTopic(String pubId, String fatherId)
      throws RemoteException {
    getKmeliaBm().addPublicationToTopic(getPublicationPK(pubId), getNodePK(fatherId), false);
  }

  public synchronized void deletePublicationFromAllTopics(String pubId) throws RemoteException {
    getKmeliaBm().deletePublicationFromAllTopics(getPublicationPK(pubId));
  }

  public synchronized Collection<ModelDetail> getAllModels() throws RemoteException {
    return getKmeliaBm().getAllModels();
  }

  public synchronized ModelDetail getModelDetail(String modelId) throws RemoteException {
    return getKmeliaBm().getModelDetail(modelId);
  }

  /*
   * public synchronized void createInfoDetail(String pubId, String modelId, InfoDetail infos)
   * throws RemoteException { getKmeliaBm().createInfoDetail(getPublicationPK(pubId), modelId,
   * infos); }
   */
  public synchronized void createInfoModelDetail(String pubId, String modelId, InfoDetail infos)
      throws RemoteException {
    pubId = getSessionPubliOrClone().getPublication().getPublicationDetail().getPK().getId();
    if (isCloneNeeded()) {
      pubId = clonePublication();
    }
    if (getSessionClone() != null) {
      ModelPK modelPK = new ModelPK(modelId, getPublicationPK(pubId));
      getKmeliaBm().getPublicationBm().createInfoModelDetail(getPublicationPK(pubId), modelPK,
          infos);
    } else {
      getKmeliaBm().createInfoModelDetail(getPublicationPK(pubId), modelId, infos);
    }
    refreshSessionPubliAndClone();
  }

  public void refreshSessionPubliAndClone() throws RemoteException {
    if (getSessionClone() != null) {
      // refresh du clone
      UserCompletePublication ucp =
          getUserCompletePublication(getSessionClone().getPublication().getPublicationDetail()
              .getPK().
              getId());
      setSessionClone(ucp);
    } else {
      // refresh de la publi de référence
      UserCompletePublication ucp =
          getUserCompletePublication(getSessionPublication().getPublication()
              .getPublicationDetail().
              getPK().getId());
      setSessionPublication(ucp);
    }
  }

  public synchronized InfoDetail getInfoDetail(String pubId) throws RemoteException {
    return getKmeliaBm().getInfoDetail(getPublicationPK(pubId));
  }

  public synchronized void updateInfoDetail(String pubId, InfoDetail infos) throws RemoteException {
    pubId = getSessionPubliOrClone().getPublication().getPublicationDetail().getPK().getId();
    if (isCloneNeeded()) {
      pubId = clonePublication();
    }
    if (getSessionClone() != null) {
      getKmeliaBm().getPublicationBm().updateInfoDetail(getPublicationPK(pubId), infos);
    } else {
      getKmeliaBm().updateInfoDetail(getPublicationPK(pubId), infos);
    }
    refreshSessionPubliAndClone();
  }

  /**
   * removes links between specified publication and other publications contained in links parameter
   * @param pubId publication which you want removes the external link
   * @param links list of links to remove
   * @throws RemoteException
   */
  public void deleteInfoLinks(String pubId, List<ForeignPK> links) throws RemoteException {
    getKmeliaBm().deleteInfoLinks(getPublicationPK(pubId), links);

    // reset current publication
    UserCompletePublication completPub =
        getKmeliaBm().getUserCompletePublication(getPublicationPK(pubId), getUserId());
    setSessionPublication(completPub);
  }

  /**
   * adds links between specified publication and other publications contained in links parameter
   * @param pubId publication which you want removes the external link
   * @param links list of links to remove
   * @throws RemoteException
   */
  public void addInfoLinks(String pubId, List<ForeignPK> links) throws RemoteException {
    getKmeliaBm().addInfoLinks(getPublicationPK(pubId), links);

    // reset current publication
    UserCompletePublication completPub =
        getKmeliaBm().getUserCompletePublication(getPublicationPK(pubId), getUserId());
    setSessionPublication(completPub);
  }

  public synchronized UserCompletePublication getUserCompletePublication(String pubId)
      throws RemoteException {
    return getUserCompletePublication(pubId, false);
  }

  public synchronized UserCompletePublication getUserCompletePublication(String pubId,
      boolean processIndex) throws RemoteException {
    PublicationPK pubPK = getPublicationPK(pubId);
    // get publication
    UserCompletePublication completPub =
        getKmeliaBm().getUserCompletePublication(pubPK, getUserId());
    PublicationDetail publicationDetail = completPub.getPublication().getPublicationDetail();

    ForeignPK foreignPK = new ForeignPK(pubId, getComponentId());
    if (!publicationDetail.getPK().getInstanceId().equals(getComponentId())) {
      // it's an alias
      foreignPK.setComponentName(publicationDetail.getPK().getInstanceId());
    }

    if (getSessionPublication() != null) {
      if (!pubId.equals(getSessionPublication().getId())) {
        // memorize the reading of the publication by the user
        getStatisticBm().addStat(getUserId(), foreignPK, 1, "Publication");
      }
    } else {
      getStatisticBm().addStat(getUserId(), foreignPK, 1, "Publication");
    }

    // set nb access
    setNbAccess(publicationDetail);

    if (processIndex) {
      // mise à jour du rang de la publication
      // List publis = (List) getSessionTopic().getPublicationDetails();
      UserDetail owner = completPub.getOwner();
      UserPublication pub = new UserPublication(owner, publicationDetail);
      if (getSessionPublicationsList() != null) {
        rang = getSessionPublicationsList().indexOf(pub);
      }
    }
    return completPub;
  }

  public synchronized CompletePublication getCompletePublication(String pubId)
      throws RemoteException {
    return getKmeliaBm().getCompletePublication(getPublicationPK(pubId));
  }

  public synchronized void orderPubs() throws RemoteException {
    if (!StringUtil.isDefined(getSortValue())) {
      sortValue = "2";
    }
    orderPubs(Integer.parseInt(getSortValue()));
  }

  private void applyVisibilityFilter() throws RemoteException {
    List<UserPublication> publications = getSessionPublicationsList();
    UserPublication userPub;
    PublicationDetail pub;
    List<UserPublication> orderedPublications = new ArrayList<UserPublication>();

    Calendar calendar = Calendar.getInstance();

    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date today = calendar.getTime();

    for (int p = 0; p < publications.size(); p++) {
      userPub = publications.get(p);
      pub = userPub.getPublication();
      if (pub.getStatus() != null) {
        if (pub.getStatus().equals("Valid")) {
          Date dBegin = DateUtil.getDate(pub.getBeginDate(), pub.getBeginHour());
          Date dEnd = DateUtil.getDate(pub.getEndDate(), pub.getEndHour());

          pub.setBeginDateAndHour(dBegin);
          pub.setEndDateAndHour(dEnd);

          if (dBegin != null && dBegin.after(today)) {
            pub.setNotYetVisible(true);
          } else if (dEnd != null && dEnd.before(today)) {
            pub.setNoMoreVisible(true);
          }
          if (pub.isVisible()) {
            orderedPublications.add(userPub);
          } else {
            if (getProfile().equals("admin") || getUserId().equals(pub.getUpdaterId())
                || (!getProfile().equals("user") && isCoWritingEnable())) {
              orderedPublications.add(userPub);
            }
          }
        } else {
          if (pub.getStatus().equals("Draft")) {
            // si le theme est en co-rédaction et si on autorise le mode brouillon visible par tous
            // toutes les publications en mode brouillon sont visibles par tous, sauf les lecteurs
            // sinon, seule les publications brouillon de l'utilisateur sont visibles
            if (getUserId().equals(pub.getUpdaterId())
                || (isCoWritingEnable() && isDraftVisibleWithCoWriting() && !getProfile().equals(
                    "user"))) {
              orderedPublications.add(userPub);
            }
          } else {
            // si le thème est en co-rédaction, toutes les publications sont visibles par tous,
            // sauf les lecteurs
            if (getProfile().equals("admin") || getProfile().equals("publisher")
                || getUserId().equals(pub.getUpdaterId())
                || (!getProfile().equals("user") && isCoWritingEnable())) {
              orderedPublications.add(userPub);
            }
          }
        }
      }
    }

    setSessionPublicationsList(orderedPublications);
  }

  private synchronized void orderPubs(int sortType) throws RemoteException {

    List<UserPublication> publications = sort(getSessionPublicationsList(), sortType);

    setSessionPublicationsList(publications);
  }

  public synchronized void orderPubsToValidate(int sortType)
      throws RemoteException {
    List<UserPublication> publications =
        sort(getKmeliaBm().getPublicationsToValidate(getComponentId()), sortType);
    setSessionPublicationsList(publications);
  }

  private List<UserPublication> sort(Collection<UserPublication> publications, int sortType) {
    List<UserPublication> publicationsToSort = new ArrayList<UserPublication>(publications);
    switch (sortType) {
      case 0:
        Collections.sort(publicationsToSort, new PubliAuthorComparatorAsc());
        break;
      case 1:
        Collections.sort(publicationsToSort, new PubliUpdateDateComparatorAsc());
        break;
      case 2:
        Collections.sort(publicationsToSort, new PubliUpdateDateComparatorAsc());
        Collections.reverse(publicationsToSort);
        break;
      case 3:
        Collections.sort(publicationsToSort, new PubliImportanceComparatorDesc());
        break;
      case 4:
        publicationsToSort = sortByTitle(publicationsToSort);
        break;
      case 5:
        Collections.sort(publicationsToSort, new PubliCreationDateComparatorAsc());
        break;
      case 6:
        Collections.sort(publicationsToSort, new PubliCreationDateComparatorAsc());
        Collections.reverse(publicationsToSort);
        break;
      case 7:
        publicationsToSort = sortByDescription(publicationsToSort);
        break;
      default:
        Collections.sort(publicationsToSort, new PubliUpdateDateComparatorAsc());
        Collections.reverse(publicationsToSort);
        break;
    }

    return publicationsToSort;
  }

  private List<UserPublication> sortByTitle(List<UserPublication> publications) {
    UserPublication[] pubs = publications.toArray(new UserPublication[publications.size()]);
    for (int i = pubs.length; --i >= 0;) {
      boolean swapped = false;
      for (int j = 0; j < i; j++) {
        if (pubs[j].getPublication().getName(getCurrentLanguage()).compareToIgnoreCase(
            pubs[j + 1].getPublication().getName(getCurrentLanguage())) > 0) {
          UserPublication T = pubs[j];
          pubs[j] = pubs[j + 1];
          pubs[j + 1] = T;
          swapped = true;
        }
      }
      if (!swapped) {
        break;
      }
    }
    return Arrays.asList(pubs);
  }

  private List<UserPublication> sortByDescription(List<UserPublication> publications) {
    UserPublication[] pubs = publications.toArray(new UserPublication[publications.size()]);
    for (int i = pubs.length; --i >= 0;) {
      boolean swapped = false;
      for (int j = 0; j < i; j++) {
        String p1 = pubs[j].getPublication().getDescription(getCurrentLanguage());
        if (p1 == null) {
          p1 = "";
        }
        String p2 = pubs[j + 1].getPublication().getDescription(getCurrentLanguage());
        if (p2 == null) {
          p2 = "";
        }
        if (p1.compareToIgnoreCase(p2) > 0) {
          UserPublication T = pubs[j];
          pubs[j] = pubs[j + 1];
          pubs[j + 1] = T;
          swapped = true;
        }
      }
      if (!swapped) {
        break;
      }
    }
    return Arrays.asList(pubs);
  }

  public void orderPublications(List<String> sortedPubIds) throws RemoteException {
    getPublicationBm().changePublicationsOrder(sortedPubIds, getSessionTopic().getNodePK());
  }

  public Collection<PublicationDetail> getAllPublications() throws RemoteException {
    return getAllPublications(null);
  }

  /**
   * Get all publications sorted
   * @param sortedBy (example: pubName asc)
   * @return Collection of Publications
   * @throws RemoteException
   */
  public Collection<PublicationDetail> getAllPublications(String sortedBy) throws RemoteException {
    String publication_default_sorting =
        SilverpeasSettings.readString(getSettings(), "publication_defaultsorting", "pubId desc");
    if (StringUtil.isDefined(sortedBy)) {
      publication_default_sorting = sortedBy;
    }
    return getKmeliaBm().getPublicationBm().getAllPublications(
        new PublicationPK("useless", getComponentId()), publication_default_sorting);
  }

  public Collection<PublicationDetail> getAllPublicationsByTopic(PublicationPK pubPK, List fatherIds)
      throws RemoteException {
    Collection<PublicationDetail> result =
        getKmeliaBm().getPublicationBm().getDetailsByFatherIdsAndStatus((ArrayList) fatherIds,
            pubPK,
            "P.pubUpdateDate desc, P.pubId desc", PublicationDetail.VALID);
    SilverTrace.info("kmelia", "KmeliaSessionController.getAllPublicationsByTopic()",
        "root.MSG_PARAM_VALUE", "publis=" + result.toString());
    return result;
  }

  /**
   * Get all visible publications
   * @return List of WAAtributeValuePair (Id and InstanceId)
   * @throws RemoteException
   */
  public List<WAAttributeValuePair> getAllVisiblePublications() throws RemoteException {
    List<WAAttributeValuePair> allVisiblesPublications = new ArrayList<WAAttributeValuePair>();
    Collection<PublicationDetail> allPublications = getAllPublications();
    SilverTrace.info("kmelia", "KmeliaSessionController.getAllVisiblePublications()",
        "root.MSG_PARAM_VALUE", "NbPubli=" + allPublications.size());
    Iterator<PublicationDetail> allPublis = allPublications.iterator();
    while (allPublis.hasNext()) {
      PublicationDetail pubDetail = allPublis.next();
      if (pubDetail.getStatus().equals(PublicationDetail.VALID)) {
        SilverTrace.info("kmelia", "KmeliaSessionController.getAllVisiblePublications()",
            "root.MSG_PARAM_VALUE", "Get pubId" + pubDetail.getId() + "InstanceId="
                + pubDetail.getInstanceId());
        allVisiblesPublications.add(new WAAttributeValuePair(pubDetail.getId(), pubDetail.
            getInstanceId()));
      }
    }
    return allVisiblesPublications;
  }

  public List<WAAttributeValuePair> getAllVisiblePublicationsByTopic(String topicId)
      throws RemoteException {
    List<WAAttributeValuePair> allVisiblesPublications = new ArrayList<WAAttributeValuePair>();
    // récupérer la liste des sous thèmes de topicId
    List<String> fatherIds = new ArrayList<String>();
    NodePK nodePK = new NodePK(topicId, getComponentId());
    List<NodeDetail> nodes = getNodeBm().getSubTree(nodePK);
    Iterator<NodeDetail> it = nodes.iterator();
    while (it.hasNext()) {
      NodeDetail node = it.next();
      fatherIds.add(Integer.toString(node.getId()));
    }
    // création de pubPK
    PublicationPK pubPK = getPublicationPK("useless");
    SilverTrace.info("kmelia", "KmeliaSessionController.getAllVisiblePublicationsByTopic()",
        "root.MSG_PARAM_VALUE", "fatherIds =" + fatherIds.toString());
    SilverTrace.info("kmelia", "KmeliaSessionController.getAllVisiblePublicationsByTopic()",
        "root.MSG_PARAM_VALUE", "pubPK =" + pubPK.toString());
    Collection<PublicationDetail> allPublications = getAllPublicationsByTopic(pubPK, fatherIds);
    SilverTrace.info("kmelia", "KmeliaSessionController.getAllVisiblePublicationsByTopic()",
        "root.MSG_PARAM_VALUE", "NbPubli=" + allPublications.size());
    Iterator<PublicationDetail> allPublis = allPublications.iterator();
    while (allPublis.hasNext()) {
      PublicationDetail pubDetail = allPublis.next();
      if (pubDetail.getStatus().equals(PublicationDetail.VALID)) {
        SilverTrace.info("kmelia", "KmeliaSessionController.getAllVisiblePublicationsByTopic()",
            "root.MSG_PARAM_VALUE", "Get pubId" + pubDetail.getId() + "InstanceId="
                + pubDetail.getInstanceId());
        allVisiblesPublications.add(new WAAttributeValuePair(pubDetail.getId(), pubDetail.
            getInstanceId()));
      }
    }
    return allVisiblesPublications;
  }

  public List<WAAttributeValuePair> getAllPublicationsIds() throws RemoteException {
    List<WAAttributeValuePair> allPublicationsIds = new ArrayList<WAAttributeValuePair>();
    Collection<PublicationDetail> allPublications = getAllPublications("pubName asc");
    SilverTrace.info("kmelia", "KmeliaSessionController.getAllPublicationsIds()",
        "root.MSG_PARAM_VALUE", "NbPubli=" + allPublications.size());
    Iterator<PublicationDetail> allPublis = allPublications.iterator();
    while (allPublis.hasNext()) {
      PublicationDetail pubDetail = allPublis.next();
      if (pubDetail.getStatus().equals(PublicationDetail.VALID)) {
        SilverTrace.info("kmelia", "KmeliaSessionController.getAllPublicationsIds()",
            "root.MSG_PARAM_VALUE", "Get pubId" + pubDetail.getId() + "InstanceId="
                + pubDetail.getInstanceId());
        allPublicationsIds.add(
            new WAAttributeValuePair(pubDetail.getId(), pubDetail.getInstanceId()));
      }
    }
    return allPublicationsIds;
  }

  public int getIndexOfFirstPubToDisplay() {
    return indexOfFirstPubToDisplay;
  }

  public void setIndexOfFirstPubToDisplay(String index) {
    this.indexOfFirstPubToDisplay = new Integer(index).intValue();
  }

  public List<Comment> getAllComments(String id) throws RemoteException {
    return getCommentService().getAllCommentsOnPublication(getPublicationPK(id));
  }

  public void processTopicWysiwyg(String topicId) throws RemoteException {
    getKmeliaBm().getNodeBm().processWysiwyg(getNodePK(topicId));
  }

  /**
   * Si le mode brouillon est activé et que le classement PDC est possible alors une publication ne
   * peut sortir du mode brouillon que si elle est classée sur le PDC
   * @return true si le PDC n'est pas utilisé ou si aucun axe n'est utilisé par le composant ou si
   * la publication est classée sur le PDC
   * @throws RemoteException
   */
  public boolean isDraftOutAllowed() throws RemoteException {
    if (!isPdcUsed()) {
      // le PDC n'est pas utilisé
      return true;
    } else {
      boolean pdcClassifyingMandatory = isPDCClassifyingMandatory();
      if (!pdcClassifyingMandatory) {
        // Aucun axe n'est utilisé
        return true;
      } else {
        String pubId =
            getSessionPublication().getPublication().getPublicationDetail().getPK().getId();
        if (isPublicationClassifiedOnPDC(pubId)) {
          // Au moins un axe est obligatoire et la publication est classée sur le PDC
          return true;
        } else {
          // La publication n'est pas classée sur le PDC
          return false;
        }
      }
    }
  }

  /**************************************************************************************/
  /* KMelia - Gestion des Liens */
  /**************************************************************************************/
  // return a PublicationDetail collection
  public synchronized Collection<UserPublication> getPublications(List<ForeignPK> links)
      throws RemoteException {
    return getKmeliaBm().getPublications(links, getUserId(),
        true);
  }

  /**************************************************************************************/
  /* KMelia - Gestion des validations */
  /**************************************************************************************/
  public synchronized List<UserPublication> getPublicationsToValidate() throws RemoteException {
    return getKmeliaBm().getPublicationsToValidate(getComponentId());
  }

  public synchronized boolean validatePublication(String publicationId) throws RemoteException {
    return getKmeliaBm().validatePublication(getPublicationPK(publicationId), getUserId(),
        getValidationType(), false);
  }

  public synchronized boolean forcePublicationValidation(String publicationId)
      throws RemoteException {
    return getKmeliaBm().validatePublication(getPublicationPK(publicationId), getUserId(),
        getValidationType(), true);
  }

  public synchronized void unvalidatePublication(String publicationId, String refusalMotive)
      throws RemoteException {
    getKmeliaBm().unvalidatePublication(getPublicationPK(publicationId), getUserId(),
        refusalMotive, getValidationType());
  }

  public synchronized void suspendPublication(String publicationId, String defermentMotive)
      throws RemoteException {
    getKmeliaBm().suspendPublication(getPublicationPK(publicationId), defermentMotive, getUserId());
  }

  public List<ValidationStep> getValidationSteps() throws RemoteException {
    List<ValidationStep> steps =
        getPublicationBm().getValidationSteps(
            getSessionPubliOrClone().getPublication().getPublicationDetail().getPK());

    // Get users who have already validate this publication
    List<String> validators = new ArrayList<String>();
    ValidationStep step = null;
    for (int s = 0; s < steps.size(); s++) {
      step = steps.get(s);
      step.setUserFullName(getOrganizationController().getUserDetail(step.getUserId()).
          getDisplayedName());
      validators.add(step.getUserId());
    }

    List<String> allValidators =
        getKmeliaBm().getAllValidators(
            getSessionPubliOrClone().getPublication().getPublicationDetail().getPK(),
            getValidationType());

    for (int v = 0; v < allValidators.size(); v++) {
      if (!validators.contains(allValidators.get(v))) {
        step = new ValidationStep();
        step.setUserFullName(getOrganizationController().getUserDetail(
            allValidators.get(v)).getDisplayedName());
        steps.add(step);
      }
    }

    return steps;
  }

  public ValidationStep getValidationStep() throws RemoteException {
    if (getValidationType() == 2) {
      return getPublicationBm().getValidationStepByUser(
          getSessionPubliOrClone().getPublication().getPublicationDetail().getPK(), getUserId());
    }

    return null;
  }

  public synchronized void draftOutPublication() throws RemoteException {
    SilverTrace.info("kmelia", "KmeliaSessionController.draftOutPublication()",
        "root.MSG_GEN_ENTER_METHOD", "getSessionPublication().getPublication() = "
            + getSessionPublication().getPublication());
    if (isKmaxMode) {
      getKmeliaBm().draftOutPublication(
          getSessionPublication().getPublication().getPublicationDetail().getPK(), null,
          getProfile());
    } else {
      getKmeliaBm().draftOutPublication(
          getSessionPublication().getPublication().getPublicationDetail().getPK(),
          getSessionTopic().getNodePK(), getProfile());
    }

    if (!KmeliaHelper.ROLE_WRITER.equals(getUserTopicProfile())) {
      setSessionClone(null); // always reset clone
    }
    refreshSessionPubliAndClone();
  }

  /**
   * Change publication status from any state to draft
   * @since 3.0
   */
  public synchronized void draftInPublication() throws RemoteException {
    if (isCloneNeededWithDraft()) {
      clonePublication();
      // getKmeliaBm().draftInPublication(getPublicationPK(cloneId));
    } else {
      getKmeliaBm().draftInPublication(
          getSessionPublication().getPublication().getPublicationDetail().getPK(), getUserId());
    }
    refreshSessionPubliAndClone();
  }

  /**************************************************************************************/
  /* KMelia - Gestion des Controles de lecture */
  /**************************************************************************************/
  /*
   * public synchronized Collection getReadingStates(String pubId) throws RemoteException { return
   * getKmeliaBm().getReadingStates(getPublicationPK(pubId)); }
   */
  private void setNbAccess(PublicationDetail pub) throws RemoteException {
    int nbAccess =
        getStatisticBm().getCount(new ForeignPK(pub.getPK().getId(), pub.getPK().getInstanceId()),
            1, "Publication");
    pub.setNbAccess(nbAccess);
  }
 
  private synchronized NotificationMetaData getAlertNotificationMetaData(String pubId)
      throws RemoteException {
    NotificationMetaData metaData = null;
    if (isKmaxMode) {
      metaData =
          getKmeliaBm().getAlertNotificationMetaData(getPublicationPK(pubId), null,
              getUserDetail().getDisplayedName());
    } else {
      metaData =
          getKmeliaBm().getAlertNotificationMetaData(getPublicationPK(pubId),
              getSessionTopic().getNodePK(), getUserDetail().getDisplayedName());
    }
    metaData.setSender(getUserId());
    return metaData;
  }

  private synchronized NotificationMetaData getAlertNotificationMetaData(String pubId, String attachmentOrDocumentId, boolean isVersionning)
  	throws RemoteException {
	  NotificationMetaData metaData = null;
	  if(isVersionning) {
		  DocumentPK documentPk = new DocumentPK(Integer.parseInt(attachmentOrDocumentId), getSpaceId(), getComponentId());
		  if (isKmaxMode) {
			  metaData =
			      getKmeliaBm().getAlertNotificationMetaData(getPublicationPK(pubId), documentPk, null,
			          getUserDetail().getDisplayedName());
			} else {
			  metaData =
			      getKmeliaBm().getAlertNotificationMetaData(getPublicationPK(pubId), documentPk, 
			          getSessionTopic().getNodePK(), getUserDetail().getDisplayedName());
			}
	  } else {
		  AttachmentPK attachmentPk = new AttachmentPK(attachmentOrDocumentId, getSpaceId(), getComponentId());
		  if (isKmaxMode) {
			  metaData =
			      getKmeliaBm().getAlertNotificationMetaData(getPublicationPK(pubId), attachmentPk, null,
			          getUserDetail().getDisplayedName());
			} else {
			  metaData =
			      getKmeliaBm().getAlertNotificationMetaData(getPublicationPK(pubId), attachmentPk, 
			          getSessionTopic().getNodePK(), getUserDetail().getDisplayedName());
			}
	  }
	  metaData.setSender(getUserId());
	  return metaData;
	}

  
  /**************************************************************************************/
  /* KMELIA - Reindexation */
  /**************************************************************************************/
  public synchronized void indexKmelia() throws RemoteException {
    getKmeliaBm().indexKmelia(getComponentId());
  }

  public boolean isIndexable(PublicationDetail pubDetail) {
    return KmeliaHelper.isIndexable(pubDetail);
  }

  public Map<String, String> pasteFiles(PublicationPK pubPKFrom, String pubId)
      throws RemoteException {
    Map<String, String> fileIds = new HashMap<String, String>();

    boolean fromCompoVersion =
        "yes".equals(getOrganizationController().getComponentParameterValue(
            pubPKFrom.getInstanceId(), "versionControl"));

    if (!fromCompoVersion && !isVersionControlled()) {
      // attachments --> attachments
      // paste attachments
      fileIds =
          AttachmentController.copyAttachmentByCustomerPKAndContext(pubPKFrom,
              getPublicationPK(pubId), "Images");
    } else if (fromCompoVersion && !isVersionControlled()) {
      // versioning --> attachments
      // Last public versions becomes the new attachment
      pasteDocumentsAsAttachments(pubPKFrom, pubId);
    } else if (!fromCompoVersion && isVersionControlled()) {
      // attachments --> versioning
      // paste versioning documents
      pasteAttachmentsAsDocuments(pubPKFrom, pubId);

      SilverTrace.error("kmelia", "KmeliaRequestRouter.processPublicationsPaste",
          "CANNOT_PASTE_FROM_ATTACHMENTS_TO_VERSIONING");
    } else {
      // versioning --> versioning
      // paste versioning documents
      pasteDocuments(pubPKFrom, pubId);
    }

    return fileIds;
  }

  /******************************************************************************************/
  /* KMELIA - Copier/coller des documents versionnés */
  /******************************************************************************************/
  public void pasteDocuments(PublicationPK pubPKFrom, String pubId) throws RemoteException {
    SilverTrace.info("kmelia", "KmeliaSessionController.pasteDocuments()",
        "root.MSG_GEN_ENTER_METHOD", "pubPKFrom = " + pubPKFrom.toString() + ", pubId = " + pubId);

    // paste versioning documents attached to publication
    List<Document> documents = getVersioningBm().getDocuments(new ForeignPK(pubPKFrom));

    SilverTrace.info("kmelia", "KmeliaSessionController.pasteDocuments()",
        "root.MSG_GEN_PARAM_VALUE", documents.size() + " to paste");

    if (documents.isEmpty()) {
      return;
    }

    VersioningUtil versioningUtil = new VersioningUtil();
    String pathFrom = null; // where the original files are
    String pathTo = null; // where the copied files will be

    ForeignPK pubPK = new ForeignPK(pubId, getComponentId());

    // change the list of workers
    List<Worker> workers = getWorkers();

    // paste each document
    Document document = null;
    List<DocumentVersion> versions = null;
    DocumentVersion version = null;
    for (int d = 0; d < documents.size(); d++) {
      document = documents.get(d);

      SilverTrace.info("kmelia", "KmeliaSessionController.pasteDocuments()",
          "root.MSG_GEN_PARAM_VALUE", "document name = " + document.getName());

      // retrieve all versions of the document (from last version to first version)
      versions = getVersioningBm().getDocumentVersions(document.getPk());

      // sort versions (from first version to last version)
      Collections.reverse(versions);

      // retrieve the initial version of the document
      version = versions.get(0);

      if (pathFrom == null) {
        pathFrom =
            versioningUtil.createPath(document.getPk().getSpaceId(),
                document.getPk().getInstanceId(), null);
      }

      // change some data to paste
      document.setPk(new DocumentPK(-1, getSpaceId(), getComponentId()));
      document.setForeignKey(pubPK);
      document.setStatus(Document.STATUS_CHECKINED);
      document.setLastCheckOutDate(new Date());
      document.setWorkList((ArrayList<Worker>) workers);

      if (pathTo == null) {
        pathTo = versioningUtil.createPath(getSpaceId(), getComponentId(), null);
      }

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
        version = versions.get(v);
        version.setDocumentPK(documentPK);
        SilverTrace.info("kmelia", "KmeliaSessionController.pasteDocuments()",
            "root.MSG_GEN_PARAM_VALUE", "paste version = " + version.getLogicalName());

        // paste file on fileserver
        newVersionFile = pasteVersionFile(version.getPhysicalName(), pathFrom, pathTo);
        version.setPhysicalName(newVersionFile);

        // paste data
        getVersioningBm().addVersion(version);
      }
    }
  }

  private List<Worker> getWorkers() {
    List<Worker> workers = new ArrayList<Worker>();

    List<String> workingProfiles = new ArrayList<String>();
    workingProfiles.add("writer");
    workingProfiles.add("publisher");
    workingProfiles.add("admin");
    String[] userIds =
        getOrganizationController().getUsersIdsByRoleNames(getComponentId(), workingProfiles);

    String userId = null;
    Worker worker = null;
    for (int u = 0; u < userIds.length; u++) {
      userId = userIds[u];
      worker =
          new Worker(new Integer(userId).intValue(), -1, u, false, true, getComponentId(), "U",
              false, true, 0);
      workers.add(worker);
    }

    return workers;
  }

  public void pasteDocumentsAsAttachments(PublicationPK pubPKFrom, String pubId)
      throws RemoteException {
    SilverTrace.info("kmelia", "KmeliaSessionController.pasteDocumentsAsAttachments()",
        "root.MSG_GEN_ENTER_METHOD", "pubPKFrom = " + pubPKFrom.toString() + ", pubId = " + pubId);

    // paste versioning documents attached to publication
    List<Document> documents = getVersioningBm().getDocuments(new ForeignPK(pubPKFrom));

    SilverTrace.info("kmelia", "KmeliaSessionController.pasteDocumentsAsAttachments()",
        "root.MSG_GEN_PARAM_VALUE", documents.size() + " documents to paste");

    if (documents.isEmpty()) {
      return;
    }

    VersioningUtil versioningUtil = new VersioningUtil();
    String pathFrom = null; // where the original files are
    String pathTo = null; // where the copied files will be

    // paste each document
    Document document = null;
    DocumentVersion version = null;
    for (int d = 0; d < documents.size(); d++) {
      document = documents.get(d);

      SilverTrace.info("kmelia", "KmeliaSessionController.pasteDocumentsAsAttachments()",
          "root.MSG_GEN_PARAM_VALUE", "document name = " + document.getName());

      // retrieve last public versions of the document
      version = getVersioningBm().getLastPublicDocumentVersion(document.getPk());

      if (pathFrom == null) {
        pathFrom =
            versioningUtil.createPath(document.getPk().getSpaceId(),
                document.getPk().getInstanceId(), null);
      }

      if (pathTo == null) {
        pathTo = AttachmentController.createPath(getComponentId(), "Images");
      }

      String newVersionFile = null;
      if (version != null) {
        // paste file on fileserver
        newVersionFile = pasteVersionFile(version.getPhysicalName(), pathFrom, pathTo);

        if (newVersionFile != null) {
          // create the attachment in DB
          // Do not index it cause made by the updatePublication call later
          AttachmentDetail attachment =
              new AttachmentDetail(new AttachmentPK("unknown", getComponentId()), newVersionFile,
                  version.getLogicalName(), "", version.getMimeType(), version.getSize(), "Images",
                  new Date(), getPublicationPK(pubId), document.getName(), document
                      .getDescription(), 0);
          AttachmentController.createAttachment(attachment, false);
        }
      }
    }
  }

  public void pasteAttachmentsAsDocuments(PublicationPK pubPKFrom, String pubId)
      throws RemoteException {
    SilverTrace.info("kmelia", "KmeliaSessionController.pasteAttachmentsAsDocuments()",
        "root.MSG_GEN_ENTER_METHOD", "pubPKFrom = " + pubPKFrom.toString() + ", pubId = " + pubId);

    List<AttachmentDetail> attachments =
        AttachmentController.searchAttachmentByPKAndContext(pubPKFrom, "Images");

    SilverTrace.info("kmelia", "KmeliaSessionController.pasteAttachmentsAsDocuments()",
        "root.MSG_GEN_PARAM_VALUE", attachments.size() + " attachments to paste");

    if (attachments.isEmpty()) {
      return;
    }

    List<Worker> workers = getWorkers();

    VersioningUtil versioningUtil = new VersioningUtil();
    String pathFrom = null; // where the original files are
    String pathTo = null; // where the copied files will be

    // paste each attachment
    Document document = null;
    DocumentVersion version = null;
    AttachmentDetail attachment = null;
    for (int d = 0; d < attachments.size(); d++) {
      attachment = attachments.get(d);

      SilverTrace.info("kmelia", "KmeliaSessionController.pasteAttachmentsAsDocuments()",
          "root.MSG_GEN_PARAM_VALUE", "attachment name = " + attachment.getLogicalName());

      if (pathTo == null) {
        pathTo = versioningUtil.createPath(getSpaceId(), getComponentId(), null);
      }

      if (pathFrom == null) {
        pathFrom = AttachmentController.createPath(pubPKFrom.getInstanceId(), "Images");
      }

      // paste file on fileserver
      String newPhysicalName = pasteVersionFile(attachment.getPhysicalName(), pathFrom, pathTo);

      if (newPhysicalName != null) {
        // Document creation
        document =
            new Document(new DocumentPK(-1, "useless", getComponentId()), getPublicationPK(pubId),
                attachment.getLogicalName(), attachment.getInfo(), 0,
                Integer.parseInt(getUserId()), new Date(), "", getComponentId(),
                (ArrayList<Worker>) workers,
                new ArrayList(), 0, 0);

        // Version creation
        version =
            new DocumentVersion(null, null, 1, 0, Integer.parseInt(getUserId()), new Date(), "",
                DocumentVersion.TYPE_PUBLIC_VERSION, DocumentVersion.STATUS_VALIDATION_NOT_REQ,
                newPhysicalName, attachment.getLogicalName(), attachment.getType(), new Long(
                    attachment.getSize()).intValue(), getComponentId());

        getVersioningBm().createDocument(document, version);
      }
    }
  }

  private String pasteVersionFile(String fileNameFrom, String from, String to) {
    SilverTrace.info("kmelia", "KmeliaSessionController.pasteVersionFile()",
        "root.MSG_GEN_ENTER_METHOD", "version = " + fileNameFrom);

    if (!fileNameFrom.equals("dummy")) {
      // we have to rename pasted file (in case the copy/paste append in the same instance)
      String type = FileRepositoryManager.getFileExtension(fileNameFrom);
      String fileNameTo = new Long(new Date().getTime()).toString() + "." + type;

      try {
        // paste file associated to the first version
        FileRepositoryManager.copyFile(from + fileNameFrom, to + fileNameTo);
      } catch (Exception e) {
        SilverTrace.error("kmelia", "KmeliaSessionController.pasteVersionFile()",
            "root.EX_FILE_NOT_FOUND", from + fileNameFrom);
        return null;
      }
      return fileNameTo;
    } else {
      return fileNameFrom;
    }
  }

  /**
   * adds links between specified publication and other publications contained in links parameter
   * @param pubId publication which you want removes the external link
   * @param links list of links to remove
   * @return the number of links created
   * @throws RemoteException
   */
  public int addPublicationsToLink(String pubId, HashSet<String> links) throws RemoteException {
    StringTokenizer tokens = null;

    List<ForeignPK> infoLinks = new ArrayList<ForeignPK>();
    for (String link : links) {
      tokens = new StringTokenizer(link, "/");
      infoLinks.add(new ForeignPK(tokens.nextToken(), tokens.nextToken()));
    }
    addInfoLinks(pubId, infoLinks);
    return infoLinks.size();
  }

  /**************************************************************************************/
  /* KMelia - Gestion des objets session */
  /**************************************************************************************/
  public void setSessionTopic(TopicDetail topicDetail) {
    this.sessionTopic = topicDetail;
    if (topicDetail != null) {
      setSessionPublicationsList((List<UserPublication>) topicDetail.getPublicationDetails());
    }
  }

  public void setSessionTopicToLink(TopicDetail topicDetail) {
    this.sessionTopicToLink = topicDetail;
  }

  public void setSessionPublication(UserCompletePublication pubDetail) {
    this.sessionPublication = pubDetail;
    setSessionClone(null);
  }

  public void setSessionClone(UserCompletePublication clone) {
    this.sessionClone = clone;
  }

  public void setSessionPath(String path) {
    this.sessionPath = path;
  }

  public void setSessionPathString(String path) {
    this.sessionPathString = path;
  }

  public void setSessionOwner(boolean owner) {
    this.sessionOwner = owner;
  }

  public void setSessionPublicationsList(List<UserPublication> publications) {
    this.sessionPublicationsList = (publications == null ? null:
        new ArrayList<UserPublication>(publications));
  }

  public void setSessionCombination(List<String> combination) {
    this.sessionCombination = (combination == null ? null :
        new ArrayList<String>(combination));
  }

  public void setSessionTimeCriteria(String timeCriteria) {
    this.sessionTimeCriteria = timeCriteria;
  }

  public String getSortValue() {
    return this.sortValue;
  }

  public void setSortValue(String sort) throws RemoteException {
    if (isDefined(sort)) {
      this.sortValue = sort;
    }
    orderPubs();
  }

  public TopicDetail getSessionTopic() {
    return this.sessionTopic;
  }

  public TopicDetail getSessionTopicToLink() {
    return this.sessionTopicToLink;
  }

  public UserCompletePublication getSessionPublication() {
    return this.sessionPublication;
  }

  public UserCompletePublication getSessionClone() {
    return this.sessionClone;
  }

  public UserCompletePublication getSessionPubliOrClone() {
    if (getSessionClone() != null) {
      return getSessionClone();
    } else {
      return getSessionPublication();
    }
  }

  public String getSessionPath() {
    return this.sessionPath;
  }

  public String getSessionPathString() {
    return this.sessionPathString;
  }

  public boolean getSessionOwner() {
    return this.sessionOwner;
  }

  public List<UserPublication> getSessionPublicationsList() {
    return this.sessionPublicationsList;
  }

  public List<String> getSessionCombination() {
    return this.sessionCombination;
  }

  public String getSessionTimeCriteria() {
    return this.sessionTimeCriteria;
  }

  public void removeSessionObjects() {
    setSessionTopic(null);
    setSessionTopicToLink(null);
    setSessionPublication(null);
    setSessionClone(null);
    setSessionPath(null);
    setSessionPathString(null);
    setSessionOwner(false);
    setSessionPublicationsList(null);
  }

  public String initUPToSelectValidator(String pubId) {
    String m_context =
        GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
    PairObject hostComponentName = new PairObject(getComponentLabel(), "");
    PairObject[] hostPath = new PairObject[1];
    hostPath[0] = new PairObject(getString("kmelia.SelectValidator"), "");
    String hostUrl =
        m_context + URLManager.getURL("useless", getComponentId()) + "SetValidator?PubId=" + pubId;
    String cancelUrl =
        m_context + URLManager.getURL("useless", getComponentId()) + "SetValidator?PubId=" + pubId;

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(getSpaceLabel());
    sel.setHostComponentName(hostComponentName);
    sel.setHostPath(hostPath);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    sel.setHtmlFormName("pubForm");
    sel.setHtmlFormElementName("Valideur");
    sel.setHtmlFormElementId("ValideurId");

    // Contraintes
    if (isTargetMultiValidationEnable()) {
      sel.setMultiSelect(true);
    } else {
      sel.setMultiSelect(false);
    }
    sel.setPopupMode(true);
    sel.setSetSelectable(false);

    // Add extra params
    SelectionUsersGroups sug = new SelectionUsersGroups();
    sug.setComponentId(getComponentId());

    List<String> profiles = new ArrayList<String>();
    profiles.add("publisher");
    profiles.add("admin");

    boolean haveRights =
        isRightsOnTopicsEnabled() && getSessionTopic().getNodeDetail().haveRights();
    if (haveRights) {
      int rightsDependsOn = getSessionTopic().getNodeDetail().getRightsDependsOn();
      List<ProfileInst> profileInsts =
          getAdmin().getProfilesByObject(Integer.toString(rightsDependsOn), ObjectType.NODE,
              getComponentId());
      if (profileInsts != null) {
        ProfileInst profileInst = null;
        for (int p = 0; p < profileInsts.size(); p++) {
          profileInst = profileInsts.get(p);
          if (profileInst != null) {
            if (profiles.contains(profileInst.getName())) {
              sug.addProfileId(profileInst.getId());
            }
          }
        }
      }
    } else {
      sug.setProfileNames((ArrayList<String>) profiles);
    }

    sel.setExtraParams(sug);

    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  public String initAlertUser() throws RemoteException {
    String pubId = getSessionPublication().getPublication().getPublicationDetail().getPK().getId();

    AlertUser sel = getAlertUser();
    // Initialisation de AlertUser
    sel.resetAll();
    sel.setHostSpaceName(getSpaceLabel()); // set nom de l'espace pour browsebar
    sel.setHostComponentId(getComponentId()); // set id du composant pour appel selectionPeas (extra
    // param permettant de filtrer les users ayant acces
    // au composant)
    PairObject hostComponentName = new PairObject(getComponentLabel(), null); // set nom du
    // composant pour
    // browsebar
    // (PairObject(nom_composant,
    // lien_vers_composant))
    // NB : seul le 1er
    // element est
    // actuellement
    // utilisé
    // (alertUserPeas est
    // toujours présenté
    // en popup => pas de
    // lien sur nom du
    // composant)
    sel.setHostComponentName(hostComponentName);
    sel.setNotificationMetaData(getAlertNotificationMetaData(pubId)); // set NotificationMetaData
    // contenant les informations
    // à notifier
    // fin initialisation de AlertUser
    // l'url de nav vers alertUserPeas et demandée à AlertUser et retournée
    return AlertUser.getAlertUserURL();
  }
  
  public String initAlertUserAttachment(String attachmentOrDocumentId, boolean isVersionning) throws RemoteException {
	    
	  initAlertUser();
	  
	  AlertUser sel = getAlertUser();
	  String pubId = getSessionPublication().getPublication().getPublicationDetail().getPK().getId();
	  sel.setNotificationMetaData(getAlertNotificationMetaData(pubId, attachmentOrDocumentId, isVersionning)); // set NotificationMetaData
	  return AlertUser.getAlertUserURL();
  }

  public void toRecoverUserId() {
    Selection sel = getSelection();
    idSelectedUser =
        SelectionUsersGroups.getDistinctUserIds(sel.getSelectedElements(), sel.getSelectedSets());
  }

  public boolean isVersionControlled() {
    String strVersionControlled = this.getComponentParameterValue("versionControl");
    return ((strVersionControlled != null) && !("").equals(strVersionControlled) && !("no")
        .equals(strVersionControlled.
            toLowerCase()));
  }

  public boolean isVersionControlled(String anotherComponentId) {
    String strVersionControlled =
        getOrganizationController()
            .getComponentParameterValue(anotherComponentId, "versionControl");
    return ((strVersionControlled != null) && !("").equals(strVersionControlled) && !("no")
        .equals(strVersionControlled.
            toLowerCase()));
  }

  /**
   * @param pubId
   * @return
   * @throws RemoteException
   */
  public boolean isWriterApproval(String pubId) throws RemoteException {
    List<Document> documents =
        getVersioningBm().getDocuments((new ForeignPK(pubId, getComponentId())));
    Iterator<Document> documentsIterator = documents.iterator();
    while (documentsIterator.hasNext()) {
      Document document = documentsIterator.next();
      List<Worker> writers = document.getWorkList();
      for (int i = 0; i < writers.size(); i++) {
        Worker user = writers.get(i);
        if (user.getUserId() == new Integer(getUserId()).intValue()) {
          if (user.isApproval()) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public boolean isTargetValidationEnable() {
    return "1".equalsIgnoreCase(getComponentParameterValue("targetValidation"));
  }

  public boolean isTargetMultiValidationEnable() {
    return "2".equalsIgnoreCase(getComponentParameterValue("targetValidation"));
  }

  public boolean isCollegiateValidationEnable() {
    return "3".equalsIgnoreCase(getComponentParameterValue("targetValidation"));
  }

  public boolean isValidationTabVisible() {
    boolean tabVisible =
        PublicationDetail.TO_VALIDATE.equalsIgnoreCase(getSessionPubliOrClone().getPublication().
            getPublicationDetail().getStatus());

    return tabVisible
        &&
        (getValidationType() == KmeliaHelper.VALIDATION_COLLEGIATE || getValidationType() == KmeliaHelper.VALIDATION_TARGET_N);
  }

  public int getValidationType() {
    if (isTargetValidationEnable()) {
      return KmeliaHelper.VALIDATION_TARGET_1;
    } else if (isTargetMultiValidationEnable()) {
      return KmeliaHelper.VALIDATION_TARGET_N;
    } else if (isCollegiateValidationEnable()) {
      return KmeliaHelper.VALIDATION_COLLEGIATE;
    } else {
      return KmeliaHelper.VALIDATION_CLASSIC;
    }
  }

  public boolean isCoWritingEnable() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("coWriting"));
  }

  public String[] getSelectedUsers() {
    return idSelectedUser;
  }

  public int getSilverObjectId(String objectId) {
    int silverObjectId = -1;
    try {
      silverObjectId = getKmeliaBm().getSilverObjectId(getPublicationPK(objectId));
    } catch (Exception e) {
      SilverTrace.error("kmelia", "KmeliaSessionController.getSilverObjectId()",
          "root.EX_CANT_GET_LANGUAGE_RESOURCE", "objectId=" + objectId, e);
    }
    return silverObjectId;
  }

  @Override
  public void close() {
    removeEJBs(searchEngineEjb);
    removeEJBs(versioningBm);
  }

  private void removeEJBs(EJBObject ejbBm) {
    try {
      if (ejbBm != null) {
        ejbBm.remove();
      }
    } catch (RemoteException e) {
      SilverTrace.error("kmelia", "KmeliaSessionController.removeEJBs", "", e);
    } catch (RemoveException e) {
      SilverTrace.error("kmelia", "KmeliaSessionController.removeEJBs", "", e);
    }
  }

  private boolean isPublicationClassifiedOnPDC(String pubId) {
    if (pubId != null && pubId.length() > 0) {
      try {
        int silverObjectId = getKmeliaBm().getSilverObjectId(getPublicationPK(pubId));
        List<ClassifyPosition> positions = getPdcBm().getPositions(silverObjectId, getComponentId());
        return (positions.size() > 0);
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaSessionController.isPublicationClassifiedOnPDC()",
            SilverpeasRuntimeException.ERROR, "kmelia.MSG_ERR_GENERAL", e);
      }
    }
    return false;
  }

  public boolean isCurrentPublicationHaveContent() throws WysiwygException {
    return (getSessionPublication().getPublication().getModelDetail() != null
        || StringUtil.isDefined(WysiwygController.load(getComponentId(), getSessionPublication().
            getId(), getCurrentLanguage())) || !isInteger(getSessionPublication().getPublication().
        getPublicationDetail().getInfoId()));
  }

  public void pastePdcPositions(PublicationPK fromPK, String toPubId) throws RemoteException,
      PdcException {
    int fromSilverObjectId = getKmeliaBm().getSilverObjectId(fromPK);
    int toSilverObjectId = getKmeliaBm().getSilverObjectId(getPublicationPK(toPubId));

    getPdcBm().copyPositions(fromSilverObjectId, fromPK.getInstanceId(), toSilverObjectId,
        getComponentId());
  }

  public boolean isPDCClassifyingMandatory() {
    try {
      return getPdcBm().isClassifyingMandatory(getComponentId());
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.isPDCClassifyingPossible()",
          SilverpeasRuntimeException.ERROR, "kmelia.MSG_ERR_GENERAL", e);
    }
  }

  /**
   * @return
   */
  public PdcBm getPdcBm() {
    if (pdcBm == null) {
      pdcBm = new PdcBmImpl();
    }
    return pdcBm;
  }

  public NodeBm getNodeBm() {
    NodeBm nodeBm = null;
    try {
      NodeBmHome nodeBmHome =
          (NodeBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
      nodeBm = nodeBmHome.create();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.getNodeBm()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_NODEBM_HOME", e);
    }
    return nodeBm;
  }

  public PublicationBm getPublicationBm() {
    PublicationBm pubBm = null;
    try {
      PublicationBmHome pubBmHome =
          (PublicationBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
              PublicationBmHome.class);
      pubBm = pubBmHome.create();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.getPublicationBm()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_NODEBM_HOME", e);
    }
    return pubBm;
  }

  public ThumbnailService getThumbnailService() {
    if (thumbnailService == null) {
      thumbnailService = new ThumbnailServiceImpl();
    }
    return thumbnailService;
  }

  /**
   * @return
   */
  public NotificationManager getNotificationManager() {
    if (notificationManager == null) {
      notificationManager = new NotificationManager(null);
    }
    return notificationManager;
  }

  /**
   * @return
   */
  public String getAutoRedirectURL() {
    if (autoRedirectURL == null) {
      autoRedirectURL = getNotificationManager().getUserAutoRedirectURL(getUserId());
    }
    return autoRedirectURL;
  }

  /**
   * @param File - File uploaded in temp directory
   * @param String - fileType
   * @param String - topicId
   * @param String - importMode
   * @param boolean - draftMode
   * @param int - versionType
   * @return boolean - Return nb of publications imported
   */
  public List<PublicationDetail> importFile(File fileUploaded, String fileType, String topicId,
      String importMode, boolean draftMode, int versionType) {
    SilverTrace.debug("kmelia", "KmeliaSessionController.importFile()",
        "root.MSG_GEN_ENTER_METHOD", "fileUploaded = " + fileUploaded.getAbsolutePath()
            + " fileType=" + fileType + " importMode=" + importMode + " draftMode=" + draftMode
            + " versionType=" + versionType);
    List<PublicationDetail> publicationDetails = null;
    FileImport fileImport = new FileImport();
    fileImport.setFileUploaded(fileUploaded);
    fileImport.setTopicId(topicId);
    fileImport.setDraftMode(draftMode);
    fileImport.setVersionType(versionType);
    fileImport.setKmeliaScc(this);
    if (importMode.equals(UNITARY_IMPORT_MODE)) {
      publicationDetails = fileImport.importFile();
    } else if (importMode.equals(MASSIVE_IMPORT_MODE_ONE_PUBLICATION)
        && (fileType.equals(KmeliaSessionController.FILETYPE_ZIP1) || fileType.equals(
            KmeliaSessionController.FILETYPE_ZIP2))) {
      publicationDetails = fileImport.importFiles();
    } else if (importMode.equals(MASSIVE_IMPORT_MODE_MULTI_PUBLICATIONS)
        && (fileType.equals(KmeliaSessionController.FILETYPE_ZIP1) || fileType.equals(
            KmeliaSessionController.FILETYPE_ZIP2))) {
      publicationDetails = fileImport.importFilesMultiPubli();
    }
    return publicationDetails;
  }

  private PublicationPK getPublicationPK(String id) {
    return new PublicationPK(id, getSpaceId(), getComponentId());
  }

  private NodePK getNodePK(String id) {
    return new NodePK(id, getSpaceId(), getComponentId());
  }

  /**
   * Return if publication is in the basket
   * @param pubId
   * @return true or false
   */
  public boolean isPublicationDeleted(String pubId) {
    boolean isPublicationDeleted = false;
    try {
      Collection<Collection<NodeDetail>> pathList = getPathList(pubId);
      SilverTrace.debug("kmelia", "KmeliaSessionController.isPublicationDeleted()",
          "root.MSG_GEN_PARAM_VALUE", "pathList = " + pathList);
      if (pathList.size() == 1) {
        Iterator<Collection<NodeDetail>> i = pathList.iterator();
        while (i.hasNext()) {
          Collection<NodeDetail> path = i.next();
          Iterator<NodeDetail> j = path.iterator();
          while (j.hasNext()) {
            NodeDetail nodeInPath = j.next();
            SilverTrace.debug("kmelia", "KmeliaSessionController.isPublicationDeleted()",
                "root.MSG_GEN_PARAM_VALUE", "nodeInPath = " + nodeInPath);
            if (nodeInPath.getNodePK().getId().equals("1")) {
              isPublicationDeleted = true;
            }
          }
        }
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.isPublicationDeleted()",
          SilverpeasRuntimeException.ERROR, "kmelia.MSG_ERR_GENERAL", e);
    }
    SilverTrace.debug("kmelia", "KmeliaSessionController.isPublicationDeleted()",
        "root.MSG_GEN_PARAM_VALUE", "isPublicationDeleted=" + isPublicationDeleted);
    return isPublicationDeleted;
  }

  public void addModelUsed(String[] models) {
    try {
      String objectId = "0"; // kmax case
      if (getSessionTopic() != null) {
        objectId = getSessionTopic().getNodePK().getId();
      }
      getKmeliaBm().addModelUsed(models, getComponentId(), objectId);
    } catch (RemoteException e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.addModelUsed()",
          SilverpeasRuntimeException.ERROR, "kmelia.MSG_ERR_GENERAL", e);
    }
  }

  public Collection<String> getModelUsed() {
    Collection<String> result = null;
    try {
      String objectId = "0"; // kmax case
      if (getSessionTopic() != null) {
        objectId = getSessionTopic().getNodePK().getId();
      }
      result = getKmeliaBm().getModelUsed(getComponentId(), objectId);
    } catch (RemoteException e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.getModelUsed()",
          SilverpeasRuntimeException.ERROR, "kmelia.MSG_ERR_GENERAL", e);
    }
    return result;
  }

  public String getWizard() {
    return wizard;
  }

  public void setWizard(String wizard) {
    this.wizard = wizard;
  }

  public String getWizardRow() {
    return wizardRow;
  }

  public void setWizardRow(String wizardRow) {
    this.wizardRow = wizardRow;
  }

  public String getWizardLast() {
    return wizardLast;
  }

  public void setWizardLast(String wizardLast) {
    this.wizardLast = wizardLast;
  }

  /**************************************************************************************
   * /
   **************************************************************************************/
  /**************************************************************************************/
  /* Specific methods for Kmax */
  /***************************************************************************************/
  /** Parameter of kmax **/
  /** Parameter for time Axis visibility */
  public boolean isTimeAxisUsed() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("timeAxisUsed"));
  }

  /** Parameter for fields visibility of the publication */
  public boolean isFieldDescriptionVisible() {
    String paramValue = getComponentParameterValue("useDescription");
    return "1".equalsIgnoreCase(paramValue) || "2".equalsIgnoreCase(paramValue)
        || "".equals(paramValue);
  }

  public boolean isFieldDescriptionMandatory() {
    return "2".equalsIgnoreCase(getComponentParameterValue("useDescription"));
  }

  public boolean isFieldKeywordsVisible() {
    String paramValue = getComponentParameterValue("useKeywords");
    return "yes".equalsIgnoreCase(paramValue) || "".equals(paramValue);
  }

  public boolean isFieldImportanceVisible() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("useImportance"))
        || getSettings().getBoolean("showImportance", true);
  }

  public boolean isFieldVersionVisible() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("useVersion"))
        || getSettings().getBoolean("showPubVersion", true);
  }

  /**************************************************************************************/
  /* Interface - Gestion des axes */
  /**************************************************************************************/
  public List<Integer> getTimeAxisKeys() {
    if (this.timeAxis == null) {
      ResourceLocator timeSettings =
          new ResourceLocator("com.stratelia.webactiv.kmelia.multilang.timeAxisBundle",
              getLanguage());
      Enumeration<String> keys = timeSettings.getKeys();
      List<Integer> orderKeys = new ArrayList<Integer>();
      Integer key = null;
      String keyStr = "";
      while (keys.hasMoreElements()) {
        keyStr = keys.nextElement();
        key = new Integer(keyStr);
        orderKeys.add(key);
      }
      Collections.sort(orderKeys);
      this.timeAxis = orderKeys;
    }
    return this.timeAxis;
  }

  public synchronized List<NodeDetail> getAxis() throws RemoteException {
    return getKmeliaBm().getAxis(getComponentId());
  }

  public synchronized List<NodeDetail> getAxisHeaders() throws RemoteException {
    return getKmeliaBm().getAxisHeaders(getComponentId());
  }

  public synchronized NodePK addAxis(NodeDetail axis) throws RemoteException {
    return getKmeliaBm().addAxis(axis, getComponentId());
  }

  public synchronized NodeDetail getNodeHeader(String id) throws RemoteException {
    return getKmeliaBm().getNodeHeader(id, getComponentId());
  }

  public synchronized void updateAxis(NodeDetail axis) throws RemoteException {
    getKmeliaBm().updateAxis(axis, getComponentId());
  }

  public synchronized void deleteAxis(String axisId) throws RemoteException {
    getKmeliaBm().deleteAxis(axisId, getComponentId());
  }

  public synchronized List<UserPublication> search(List<String> combination) throws RemoteException {
    this.sessionPublicationsList =
        new ArrayList<UserPublication>(getKmeliaBm().search(combination, getComponentId()));
    applyVisibilityFilter();
    return getSessionPublicationsList();
  }

  public synchronized List<UserPublication> search(List<String> combination, int nbDays)
      throws RemoteException {
    this.sessionPublicationsList =
        new ArrayList<UserPublication>(getKmeliaBm().search(combination, nbDays, getComponentId()));
    applyVisibilityFilter();
    return getSessionPublicationsList();
  }

  public synchronized List<UserPublication> getUnbalancedPublications() throws RemoteException {
    return (List<UserPublication>) getKmeliaBm().getUnbalancedPublications(getComponentId());
  }

  /*
  /* Kmax - Positions under axis */
  /**************************************************************************************/

  public synchronized NodePK addPosition(String fatherId, NodeDetail position)
      throws RemoteException {
    SilverTrace.info(
        "kmax",
        "KmeliaSessionController.addPosition()",
        "root.MSG_GEN_PARAM_VALUE",
        "fatherId = " + fatherId + " And position = " + position.toString());
    return getKmeliaBm().addPosition(fatherId, position, getComponentId(), getUserId());
  }

  public synchronized void updatePosition(NodeDetail position) throws RemoteException {
    getKmeliaBm().updatePosition(position, getComponentId());
  }

  public synchronized void deletePosition(String positionId) throws RemoteException {
    getKmeliaBm().deletePosition(positionId, getComponentId());
  }

  /*
  /* Kmax - Reindexation */
  /**************************************************************************************/

  public synchronized void indexKmax(String componentId) throws RemoteException {
    getKmeliaBm().indexKmax(componentId);
  }

  /*
  /* Kmax - Publications */
  /**************************************************************************************/

  public synchronized UserCompletePublication getKmaxCompletePublication(String pubId)
      throws RemoteException {
    return getKmeliaBm().getKmaxCompletePublication(pubId, getUserId());
  }

  public synchronized Collection<Coordinate> getPublicationCoordinates(String pubId) throws RemoteException {
    return getKmeliaBm().getPublicationCoordinates(pubId, getComponentId());
  }

  public synchronized void addPublicationToCombination(String pubId, List<String> combination)
      throws RemoteException {
    getKmeliaBm().addPublicationToCombination(pubId, combination, getComponentId());
  }

  public synchronized void deletePublicationFromCombination(String pubId, String combinationId)
      throws RemoteException {
    getKmeliaBm().deletePublicationFromCombination(pubId, combinationId, getComponentId());
  }

  /**
   * Get session publications
   * @return List of WAAtributeValuePair (Id and InstanceId)
   * @throws RemoteException
   */
  public List<WAAttributeValuePair> getCurrentPublicationsList() throws RemoteException {
    List<WAAttributeValuePair> currentPublications = new ArrayList<WAAttributeValuePair>();
    Collection<UserPublication> allPublications = getSessionPublicationsList();
    SilverTrace.info("kmelia", "KmeliaSessionController.getCurrentPublicationsList()",
        "root.MSG_PARAM_VALUE", "NbPubli=" + allPublications.size());
    Iterator<UserPublication> allPublis = allPublications.iterator();
    while (allPublis.hasNext()) {
      UserPublication userPubli = allPublis.next();
      PublicationDetail pubDetail = userPubli.getPublication();
      if (pubDetail.getStatus().equals(PublicationDetail.VALID)) {
        SilverTrace.info("kmelia", "KmeliaSessionController.getCurrentPublicationsList()",
            "root.MSG_PARAM_VALUE", "Get pubId" + pubDetail.getId() + "InstanceId="
                + pubDetail.getInstanceId());
        currentPublications.add(new WAAttributeValuePair(pubDetail.getId(),
            pubDetail.getInstanceId()));
      }
    }
    return currentPublications;
  }

  /**************************************************************************************/
  /* Kmax - Utils */
  /**************************************************************************************/
  public synchronized Collection<NodeDetail> getPath(String positionId) throws RemoteException {
    return getKmeliaBm().getPath(positionId, getComponentId());
  }

  public void setCurrentCombination(List<String> combination) {
    this.currentCombination = new ArrayList<String>(combination);
  }

  public List<String> getCurrentCombination() {
    return currentCombination;
  }

  /**
   * Transform combination axis from String /0/1037,/0/1038 in ArrayList /0/1037 then /0/1038 etc...
   * @param axisValuesStr
   * @return Collection of combination
   */
  private List<String> convertStringCombination2List(String axisValuesStr) {
    List<String> combination = new ArrayList<String>();
    String axisValue = "";
    StringTokenizer st = new StringTokenizer(axisValuesStr, ",");
    while (st.hasMoreTokens()) {
      axisValue = st.nextToken();
      combination.add(axisValue);
    }
    return combination;
  }

  /**
   * Get combination Axis (ie: /0/1037)
   * @param axisValuesStr
   * @return Collection of combination
   */
  public List<String> getCombination(String axisValuesStr) {
    SilverTrace.info("kmelia", "KmeliaSessionController.getCombination(String)",
        "root.MSG_GEN_PARAM_VALUE", "axisValuesStr=" + axisValuesStr);
    return convertStringCombination2List(axisValuesStr);
  }

  private String getNearPublication(int direction) {
    String pubId = "";

    // rechercher le rang de la publication précédente
    int rangNext = rang + direction;

    UserPublication pub = getSessionPublicationsList().get(rangNext);
    pubId = pub.getPublication().getId();

    // on est sur la précédente, mettre à jour le rang avec la publication courante
    rang = rangNext;

    return pubId;
  }

  public String getFirst() {
    rang = 0;
    UserPublication pub = getSessionPublicationsList().get(0);
    String pubId = pub.getPublication().getId();

    return pubId;
  }

  /**
   * getPrevious
   * @return previous publication id
   */
  public String getPrevious() {
    return getNearPublication(-1);
  }

  /**
   * getNext
   * @return next publication id
   */
  public String getNext() {
    return getNearPublication(1);
  }

  public int getRang() {
    return rang;
  }

  private boolean isDefined(String param) {
    return (param != null && param.length() > 0 && !"".equals(param) && !"null".equals(param));
  }

  public List<NodeDetail> getSessionTreeview() {
    return sessionTreeview;
  }

  public void setSessionTreeview(List<NodeDetail> sessionTreeview) {
    this.sessionTreeview = new ArrayList<NodeDetail>(sessionTreeview);
  }

  private synchronized boolean isDragAndDropEnableByUser() throws RemoteException {
    try {
      return getPersonalization().getDragAndDropStatus();
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      return getPersonalization().getDragAndDropStatus();
    }
  }

  public boolean isDragAndDropEnable() throws RemoteException {
    return isDragAndDropEnableByUser && isMassiveDragAndDropAllowed();
  }

  public String getCurrentLanguage() {
    return currentLanguage;
  }

  public void setCurrentLanguage(String currentLanguage) {
    this.currentLanguage = currentLanguage;
  }

  public String initUserPanelForTopicProfile(String role, String nodeId) throws RemoteException {
    String m_context =
        GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
    PairObject[] hostPath = new PairObject[1];
    hostPath[0] = new PairObject(getString("kmelia.SelectValidator"), "");

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(getSpaceLabel());
    sel.setHostComponentName(new PairObject(getComponentLabel(), ""));
    sel.setHostPath(hostPath);

    String hostUrl =
        m_context + URLManager.getURL("useless", getComponentId())
            + "TopicProfileSetUsersAndGroups?Role=" + role + "&NodeId=" + nodeId;
    String cancelUrl = m_context + URLManager.getURL("useless", getComponentId()) + "CloseWindow";

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    List<ProfileInst> profiles =
        getAdmin().getProfilesByObject(nodeId, ObjectType.NODE, getComponentId());
    ProfileInst topicProfile = getProfile(profiles, role);

    SelectionUsersGroups sug = new SelectionUsersGroups();
    sug.setComponentId(getComponentId());

    boolean useComponentProfiles = true;

    if (useComponentProfiles) {
      // The selectable users and groups are component's ones.
      List<String> profileNames = new ArrayList<String>();
      profileNames.add("user");
      profileNames.add("writer");
      profileNames.add("publisher");
      profileNames.add("admin");
      sug.setProfileNames((ArrayList<String>) profileNames);
      sel.setExtraParams(sug);
    }

    if (topicProfile != null) {
      sel.setSelectedElements(topicProfile.getAllUsers().toArray(new String[0]));
      sel.setSelectedSets(topicProfile.getAllGroups().toArray(new String[0]));
    }

    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  private void deleteTopicRoles(NodeDetail node) throws RemoteException {
    if (node != null && node.haveLocalRights()) {
      List<ProfileInst> profiles = getTopicProfiles(node.getNodePK().getId());
      ProfileInst profile = null;
      for (int p = 0; profiles != null && p < profiles.size(); p++) {
        profile = profiles.get(p);
        if (profile != null && StringUtil.isDefined(profile.getId())) {
          deleteTopicRole(profile.getId());
        }
      }
    }
  }

  public void deleteTopicRole(String profileId) throws RemoteException {
    // Remove the profile
    getAdmin().deleteProfileInst(profileId);
  }

  public void updateTopicRole(String role, String nodeId) throws RemoteException {
    ProfileInst profile = getTopicProfile(role, nodeId);

    // Update the topic
    NodeDetail topic = getNodeHeader(nodeId);
    topic.setRightsDependsOnMe();
    getNodeBm().updateRightsDependency(topic);

    if (StringUtil.isDefined(profile.getId())) {
      // Update the profile
      profile.removeAllGroups();
      profile.removeAllUsers();

      profile.setGroupsAndUsers(getSelection().getSelectedSets(),
          getSelection().getSelectedElements());

      getAdmin().updateProfileInst(profile);
    } else {
      // Create the profile
      profile.setObjectId(Integer.parseInt(nodeId));
      profile.setObjectType(ObjectType.NODE);
      profile.setComponentFatherId(getComponentId());

      profile.setGroupsAndUsers(getSelection().getSelectedSets(),
          getSelection().getSelectedElements());

      getAdmin().addProfileInst(profile);
    }
  }

  public void updateTopicDependency(NodeDetail node, boolean enableIt) throws RemoteException {
    if (!enableIt) {
      NodePK fatherPK = node.getFatherPK();
      NodeDetail father = getNodeBm().getHeader(fatherPK);

      node.setRightsDependsOn(father.getRightsDependsOn());

      // Topic profiles must be removed
      List<ProfileInst> profiles =
          getAdmin().getProfilesByObject(node.getNodePK().getId(), ObjectType.NODE,
              getComponentId());
      for (int p = 0; profiles != null && p < profiles.size(); p++) {
        ProfileInst profile = profiles.get(p);
        if (profile != null) {
          getAdmin().deleteProfileInst(profile.getId());
        }
      }
    } else {
      node.setRightsDependsOnMe();
    }

    getNodeBm().updateRightsDependency(node);
  }

  public ProfileInst getTopicProfile(String role, String topicId) {
    List<ProfileInst> profiles =
        getAdmin().getProfilesByObject(topicId, ObjectType.NODE, getComponentId());
    for (int p = 0; profiles != null && p < profiles.size(); p++) {
      ProfileInst profile = profiles.get(p);
      if (profile.getName().equals(role)) {
        return profile;
      }
    }

    ProfileInst profile = new ProfileInst();
    profile.setName(role);
    return profile;
  }

  public ProfileInst getTopicProfile(String role) {
    return getTopicProfile(role, getSessionTopic().getNodePK().getId());
  }

  public ProfileInst getProfile(String role) {
    ComponentInst componentInst = getAdmin().getComponentInst(getComponentId());
    ProfileInst profile = componentInst.getProfileInst(role);
    ProfileInst inheritedProfile = componentInst.getInheritedProfileInst(role);

    if (profile == null && inheritedProfile == null) {
      profile = new ProfileInst();
      profile.setName(role);

      return profile;
    } else if (profile != null && inheritedProfile == null) {
      return profile;
    } else if (profile == null && inheritedProfile != null) {
      return inheritedProfile;
    } else {
      // merge des profiles
      ProfileInst newProfile = (ProfileInst) profile.clone();
      newProfile.setObjectFatherId(profile.getObjectFatherId());
      newProfile.setObjectType(profile.getObjectType());
      newProfile.setInherited(profile.isInherited());

      newProfile.addGroups(inheritedProfile.getAllGroups());
      newProfile.addUsers(inheritedProfile.getAllUsers());

      return newProfile;
    }
  }

  public List<ProfileInst> getTopicProfiles() {
    return getTopicProfiles(getSessionTopic().getNodePK().getId());
  }

  public List<ProfileInst> getTopicProfiles(String topicId) {
    List<ProfileInst> alShowProfile = new ArrayList<ProfileInst>();
    ProfileInst profile = null;

    // profils dispo
    String[] asAvailProfileNames = getAdmin().getAllProfilesNames("kmelia");

    for (int nI = 0; nI < asAvailProfileNames.length; nI++) {
      SilverTrace.info("jobStartPagePeas",
          "JobStartPagePeasSessionController.getAllProfilesNames()", "root.MSG_GEN_PARAM_VALUE",
          "asAvailProfileNames = " + asAvailProfileNames[nI]);
      // boolean bFound = false;

      profile = getTopicProfile(asAvailProfileNames[nI], topicId);
      /*
       * if (profile != null) { bFound = true;
       * profile.setLabel(getAdmin().getProfileLabelfromName("kmelia", asAvailProfileNames[nI]));
       * alShowProfile.add(profile); } if (!bFound) { profile = new ProfileInst();
       * profile.setName(asAvailProfileNames[nI]);
       * profile.setLabel(getAdmin().getProfileLabelfromName("kmelia", asAvailProfileNames[nI]));
       * alShowProfile.add(profile); }
       */
      profile.setLabel(getAdmin().getProfileLabelfromName("kmelia", asAvailProfileNames[nI]));
      alShowProfile.add(profile);
    }

    return alShowProfile;

  }

  public List<Group> groupIds2Groups(List<String> groupIds) {
    List<Group> res = new ArrayList<Group>();
    Group theGroup = null;

    for (int nI = 0; groupIds != null && nI < groupIds.size(); nI++) {
      theGroup = getAdmin().getGroupById(groupIds.get(nI));
      if (theGroup != null) {
        res.add(theGroup);
      }
    }

    return res;
  }

  public List<String> userIds2Users(List<String> userIds) {
    List<String> res = new ArrayList<String>();
    UserDetail user = null;

    for (int nI = 0; userIds != null && nI < userIds.size(); nI++) {
      user = getUserDetail(userIds.get(nI));
      if (user != null) {
        res.add(user.getDisplayedName());
      }
    }

    return res;
  }

  private AdminController getAdmin() {
    if (m_AdminCtrl == null) {
      m_AdminCtrl = new AdminController(getUserId());
    }

    return m_AdminCtrl;
  }

  private ProfileInst getProfile(List<ProfileInst> profiles, String role) {
    ProfileInst profile = null;
    for (int p = 0; p < profiles.size(); p++) {
      profile = profiles.get(p);
      if (role.equals(profile.getName())) {
        return profile;
      }
    }
    return null;
  }

  public void copyPublication(String pubId) throws RemoteException {
    CompletePublication pub = getCompletePublication(pubId);
    PublicationSelection pubSelect = new PublicationSelection(pub);

    SilverTrace.info("kmelia", "KmeliaSessionController.copyPublication()",
        "root.MSG_GEN_PARAM_VALUE",
        "clipboard = " + getClipboardName() + "' count=" + getClipboardCount());
    addClipboardSelection((ClipboardSelection) pubSelect);
  }

  public void copyPublications(String[] pubIds) throws RemoteException {
    for (int i = 0; i < pubIds.length; i++) {
      if (StringUtil.isDefined(pubIds[i])) {
        copyPublication(pubIds[i]);
      }
    }
  }

  public void cutPublication(String pubId) throws RemoteException {
    CompletePublication pub = getCompletePublication(pubId);
    PublicationSelection pubSelect = new PublicationSelection(pub);
    pubSelect.setCutted(true);

    SilverTrace.info("kmelia", "KmeliaSessionController.cutPublication()",
        "root.MSG_GEN_PARAM_VALUE",
        "clipboard = " + getClipboardName() + "' count=" + getClipboardCount());
    addClipboardSelection((ClipboardSelection) pubSelect);
  }

  public void cutPublications(String[] pubIds) throws RemoteException {
    for (int i = 0; i < pubIds.length; i++) {
      if (StringUtil.isDefined(pubIds[i])) {
        cutPublication(pubIds[i]);
      }
    }
  }

  public void copyTopic(String id) throws RemoteException {
    NodeSelection nodeSelect = new NodeSelection(getNodeHeader(id));

    SilverTrace.info("kmelia", "KmeliaSessionController.copyTopic()", "root.MSG_GEN_PARAM_VALUE",
        "clipboard = " + getClipboardName() + "' count=" + getClipboardCount());
    addClipboardSelection((ClipboardSelection) nodeSelect);
  }

  public void cutTopic(String id) throws RemoteException {
    NodeSelection nodeSelect = new NodeSelection(getNodeHeader(id));
    nodeSelect.setCutted(true);
    SilverTrace.info("kmelia", "KmeliaSessionController.cutTopic()", "root.MSG_GEN_PARAM_VALUE",
        "clipboard = " + getClipboardName() + "' count=" + getClipboardCount());
    addClipboardSelection((ClipboardSelection) nodeSelect);
  }

  public List<Object> paste() throws RemoteException {
    List<Object> pastedItems = new ArrayList<Object>();
    try {
      SilverTrace.info("kmelia", "KmeliaRequestRooter.paste()", "root.MSG_GEN_PARAM_VALUE",
          "clipboard = " + getClipboardName() + " count=" + getClipboardCount());
      Collection<ClipboardSelection> clipObjects = getClipboardSelectedObjects();
      Iterator<ClipboardSelection> clipObjectIterator = clipObjects.iterator();
      while (clipObjectIterator.hasNext()) {
        ClipboardSelection clipObject = clipObjectIterator.next();
        if (clipObject != null) {
          if (clipObject.isDataFlavorSupported(PublicationSelection.CompletePublicationFlavor)) {
            CompletePublication pub =
                (CompletePublication) clipObject.getTransferData(
                    PublicationSelection.CompletePublicationFlavor);
            pastePublication(pub, clipObject.isCutted());
            pastedItems.add(pub.getPublicationDetail());
          } else if (clipObject.isDataFlavorSupported(NodeSelection.NodeDetailFlavor)) {
            NodeDetail node =
                (NodeDetail) clipObject.getTransferData(NodeSelection.NodeDetailFlavor);

            // check if current topic is a subTopic of node
            boolean pasteAllowed = true;
            if (getComponentId().equals(node.getNodePK().getInstanceId())) {
              if (node.getNodePK().getId().equals(getSessionTopic().getNodePK().getId())) {
                pasteAllowed = false;
              }

              String nodePath = node.getPath() + node.getId() + "/";
              String currentPath =
                  getSessionTopic().getNodeDetail().getPath()
                      + getSessionTopic().getNodePK().getId() + "/";
              SilverTrace.info("kmelia", "KmeliaRequestRooter.paste()", "root.MSG_GEN_PARAM_VALUE",
                  "nodePath = " + nodePath + ", currentPath = " + currentPath);
              if (pasteAllowed && currentPath.startsWith(nodePath)) {
                pasteAllowed = false;
              }
            }

            if (pasteAllowed) {
              NodeDetail newNode =
                  pasteNode(node, getSessionTopic().getNodeDetail(), clipObject.isCutted());
              pastedItems.add(newNode);
            }
          }
        }
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.paste()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_PASTE_ERROR", e);
    }
    clipboardPasteDone();
    return pastedItems;
  }

  private NodeDetail pasteNode(NodeDetail nodeToPaste, NodeDetail father, boolean isCutted)
      throws RemoteException {
    NodePK nodeToPastePK = nodeToPaste.getNodePK();

    List<NodeDetail> treeToPaste = getNodeBm().getSubTree(nodeToPastePK);

    if (isCutted) {
      // move node and subtree
      getNodeBm().moveNode(nodeToPastePK, father.getNodePK());

      NodeDetail fromNode = null;
      NodePK toNodePK = null;
      for (int i = 0; i < treeToPaste.size(); i++) {
        fromNode = treeToPaste.get(i);
        if (fromNode != null) {
          toNodePK = getNodePK(fromNode.getNodePK().getId());

          // remove rights
          deleteTopicRoles(fromNode);

          // move wysiwyg
          try {
            AttachmentController.moveAttachments(new ForeignPK("Node_" + fromNode.getNodePK()),
                new ForeignPK("Node_" + toNodePK.getId(), getComponentId()), true); // Change
            // instanceId +
            // move files
          } catch (AttachmentException e) {
            SilverTrace.error("kmelia", "KmeliaSessionController.pastePublication()",
                "root.MSG_GEN_PARAM_VALUE", "kmelia.CANT_MOVE_ATTACHMENTS", e);
          }

          // change images path in wysiwyg
          try {
            WysiwygController.wysiwygPlaceHaveChanged(fromNode.getNodePK().getInstanceId(),
                "Node_" + fromNode.getNodePK().getId(), getComponentId(), "Node_"
                    + toNodePK.getId());
          } catch (WysiwygException e) {
            SilverTrace.error("kmelia", "KmeliaSessionController.pastePublication()",
                "root.MSG_GEN_PARAM_VALUE", e);
          }

          // move publications of topics
          pastePublicationsOfTopic(fromNode.getNodePK(), toNodePK, true, null);
        }
      }
      return nodeToPaste;
    } else {
      // paste topic
      NodePK nodePK = new NodePK("unknown", getComponentId());
      NodeDetail node = new NodeDetail();
      node.setNodePK(nodePK);
      node.setCreatorId(getUserId());
      node.setName(nodeToPaste.getName());
      node.setDescription(nodeToPaste.getDescription());
      node.setTranslations(nodeToPaste.getTranslations());
      node.setRightsDependsOn(father.getRightsDependsOn());
      node.setCreationDate(DateUtil.today2SQLDate());
      node.setStatus(nodeToPaste.getStatus());
      nodePK = getNodeBm().createNode(node, father);

      // paste wysiwyg attached to node
      WysiwygController.copy(null, nodeToPastePK.getInstanceId(), "Node_" + nodeToPastePK.getId(),
          null, getComponentId(), "Node_" + nodePK.getId(), getUserId());

      List<NodePK> nodeIdsToPaste = new ArrayList<NodePK>();
      NodeDetail oneNodeToPaste = null;
      for (int i = 0; i < treeToPaste.size(); i++) {
        oneNodeToPaste = treeToPaste.get(i);
        if (oneNodeToPaste != null) {
          nodeIdsToPaste.add(oneNodeToPaste.getNodePK());
        }
      }

      // paste publications of topics
      pastePublicationsOfTopic(nodeToPastePK, nodePK, false, nodeIdsToPaste);

      // paste subtopics
      node = getNodeBm().getHeader(nodePK);
      Collection<NodeDetail> subtopics = getNodeBm().getDetail(nodeToPastePK).getChildrenDetails();
      Iterator<NodeDetail> itSubTopics = subtopics.iterator();
      NodeDetail subTopic = null;
      while (itSubTopics != null && itSubTopics.hasNext()) {
        subTopic = itSubTopics.next();
        if (subTopic != null) {
          pasteNode(subTopic, node, isCutted);
        }
      }
      return node;
    }
  }

  private void pastePublicationsOfTopic(NodePK fromPK, NodePK toPK, boolean isCutted,
      List<NodePK> nodePKsToPaste) throws RemoteException {
    Collection<PublicationDetail> publications = getPublicationBm().getDetailsByFatherPK(fromPK);
    Iterator<PublicationDetail> itPublis = publications.iterator();
    PublicationDetail publi = null;
    CompletePublication completePubli = null;
    while (itPublis.hasNext()) {
      publi = itPublis.next();
      completePubli = getPublicationBm().getCompletePublication(publi.getPK());

      pastePublication(completePubli, isCutted, toPK, nodePKsToPaste);
    }
  }

  private void pastePublication(CompletePublication pub, boolean isCutted) {
    pastePublication(pub, isCutted, null, null);
  }

  private void pastePublication(CompletePublication completePub, boolean isCutted, NodePK nodePK,
      List<NodePK> nodePKsToPaste) {
    try {
      PublicationDetail publi = completePub.getPublicationDetail();

      String fromId = publi.getPK().getId();
      String fromComponentId = publi.getPK().getInstanceId();

      ForeignPK fromForeignPK = new ForeignPK(publi.getPK().getId(), fromComponentId);
      PublicationPK fromPubPK = new PublicationPK(publi.getPK().getId(), fromComponentId);

      ForeignPK toForeignPK = new ForeignPK(publi.getPK().getId(), getComponentId());
      PublicationPK toPubPK = new PublicationPK(publi.getPK().getId(), getComponentId());

      String imagesSubDirectory = getPublicationSettings().getString(
          "imagesSubDirectory");
      String thumbnailsSubDirectory = getPublicationSettings().getString("imagesSubDirectory");
      String toAbsolutePath = FileRepositoryManager
          .getAbsolutePath(getComponentId());
      String fromAbsolutePath = FileRepositoryManager
          .getAbsolutePath(fromComponentId);

      if (isCutted) {
        if (nodePK == null) {
          // Ajoute au thème courant
          nodePK = getSessionTopic().getNodePK();
        }

        if (fromComponentId.equals(getComponentId())) {
          // déplacement de la publication dans le même composant
          // seul le père doit être modifié

          // TODO : Quid d'une publi avec plusieurs pères ?

          // Supprime tous les pères
          getPublicationBm().removeAllFather(publi.getPK());

          getPublicationBm().addFather(publi.getPK(), nodePK);
        } else {
          // déplacement de la publication dans un autre composant
          // - déplacer entête (instanceId)
          // - déplacer vignette
          // - déplacer contenu
          // - wysiwyg
          // - wysiwyg (images)
          // - xml (images et fichiers)
          // - DB (images)
          // - déplacer fichiers joints
          // - déplacer versioning
          // - déplacer commentaires
          // - deplacer pdc
          // - déplacer les statistiques

          boolean indexIt = KmeliaHelper.isIndexable(publi);

          getPublicationBm().movePublication(publi.getPK(), nodePK, false); // Change instanceId and
          // unindex
          // header+content

          // move Vignette on disk
          int[] thumbnailSize = getThumbnailWidthAndHeight();

          String vignette = ThumbnailController.getImage(fromComponentId,
              Integer.parseInt(fromId),
              ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE,
              thumbnailSize[0],
              thumbnailSize[1]);
          if (StringUtil.isDefined(vignette)) {
            String from = fromAbsolutePath + thumbnailsSubDirectory + File.separator + vignette;

            try {
              FileRepositoryManager.createAbsolutePath(getComponentId(), thumbnailsSubDirectory);
            } catch (Exception e) {
              SilverTrace.error("kmelia", "KmeliaSessionController.pastePublication()",
                  "root.MSG_GEN_PARAM_VALUE", "kmelia.CANT_MOVE_ATTACHMENTS", e);
            }
            String to = toAbsolutePath + thumbnailsSubDirectory + File.separator + vignette;

            File fromVignette = new File(from);
            File toVignette = new File(to);

            boolean moveOK = fromVignette.renameTo(toVignette);

            SilverTrace.info("kmelia", "KmeliaSessionController.pastePublication()",
                "root.MSG_GEN_PARAM_VALUE", "vignette move = " + moveOK);
          }

          // move attachments first (wysiwyg, wysiwyg images, formXML files and images, attachments)
          // TODO : attachments to versioning
          try {
            AttachmentController.moveAttachments(fromForeignPK, toForeignPK, indexIt); // Change
            // instanceId
            // + move
            // files
          } catch (AttachmentException e) {
            SilverTrace.error("kmelia", "KmeliaSessionController.pastePublication()",
                "root.MSG_GEN_PARAM_VALUE", "kmelia.CANT_MOVE_ATTACHMENTS", e);
          }

          try {
            // change images path in wysiwyg
            WysiwygController.wysiwygPlaceHaveChanged(fromComponentId, publi.getPK().getId(),
                getComponentId(), publi.getPK().getId());
          } catch (WysiwygException e) {
            SilverTrace.error("kmelia", "KmeliaSessionController.pastePublication()",
                "root.MSG_GEN_PARAM_VALUE", e);
          }

          boolean fromCompoVersion =
              "yes".equals(getOrganizationController().getComponentParameterValue(fromComponentId,
                  "versionControl"));

          if (fromCompoVersion && isVersionControlled()) {
            // move versioning files
            VersioningUtil versioning = new VersioningUtil();
            versioning.moveDocuments(fromForeignPK, toForeignPK, indexIt);
          } else if (fromCompoVersion && !isVersionControlled()) {
            // versioning --> attachments
            // Last public versions becomes the new attachment
            pasteDocumentsAsAttachments(fromPubPK, publi.getPK().getId());

            if (indexIt) {
              AttachmentController.attachmentIndexer(toForeignPK);
            }

            // remove files
            getVersioningBm().deleteDocumentsByForeignPK(fromForeignPK);
          } else if (!fromCompoVersion && isVersionControlled()) {
            // attachments --> versioning
            // paste versioning documents

            // Be careful, attachments have already moved !
            pasteAttachmentsAsDocuments(toPubPK, publi.getPK().getId());

            if (indexIt) {
              VersioningUtil versioning = new VersioningUtil();
              versioning.indexDocumentsByForeignKey(toForeignPK);
            }

            // remove only files
            AttachmentController.deleteAttachmentsByCustomerPKAndContext(toForeignPK, "Images");
          } else {
            // already made by moveAttachments
          }

          // eventually, paste the model content
          if (completePub.getModelDetail() != null && completePub.getInfoDetail() != null) {
            // Move images of model
            if (completePub.getInfoDetail().getInfoImageList() != null) {
              for (Iterator<InfoImageDetail> i =
                  completePub.getInfoDetail().getInfoImageList().iterator(); i.hasNext();) {
                InfoImageDetail attachment = i.next();
                String from =
                    fromAbsolutePath + imagesSubDirectory + File.separator
                        + attachment.getPhysicalName();
                String to =
                    toAbsolutePath + imagesSubDirectory + File.separator
                        + attachment.getPhysicalName();

                File fromImage = new File(from);
                File toImage = new File(to);

                boolean moveOK = fromImage.renameTo(toImage);

                SilverTrace.info("kmelia", "KmeliaSessionController.pastePublication()",
                    "root.MSG_GEN_PARAM_VALUE", "dbImage move = " + moveOK);
              }
            }
          } else {
            String infoId = publi.getInfoId();
            if (infoId != null && !"0".equals(infoId)) {
              // register content to component
              getPublicationTemplateManager().addDynamicPublicationTemplate(getComponentId() + ":"
                  + publi.getInfoId(), publi.getInfoId() + ".xml");
            }
          }

          // move comments
          if (indexIt) {
            getCommentService().moveAndReindexComments(fromForeignPK, toForeignPK);
          } else {
            getCommentService().moveComments(fromForeignPK, toForeignPK);
          }

          // move pdc positions
          int fromSilverObjectId = getKmeliaBm().getSilverObjectId(fromPubPK);
          int toSilverObjectId = getKmeliaBm().getSilverObjectId(toPubPK);

          getPdcBm().copyPositions(fromSilverObjectId, fromComponentId, toSilverObjectId,
              getComponentId());
          getKmeliaBm().deleteSilverContent(fromPubPK);

          if (indexIt) {
            getPublicationBm().createIndex(toPubPK);
          }

          // move statistics
          getStatisticBm().moveStat(toForeignPK, 1, "Publication");

        }
      } else {
        // paste the publicationDetail
        publi.setUpdaterId(getUserId()); // ignore initial parameters

        String id = null;
        if (nodePK == null) {
          id = createPublication(publi);
        } else {
          id = createPublicationIntoTopic(publi, nodePK.getId());

          List<NodePK> fatherPKs = (List<NodePK>) getPublicationBm().getAllFatherPK(publi.getPK());
          if (nodePKsToPaste != null) {
            fatherPKs.removeAll(nodePKsToPaste);
          }
        }

        // paste vignette
        ThumbnailDetail vignette =
            ThumbnailController.getCompleteThumbnail(new ThumbnailDetail(fromComponentId, Integer
                .parseInt(fromId), ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE));
        if (vignette != null) {
          ThumbnailDetail thumbDetail =
              new ThumbnailDetail(publi.getPK().getInstanceId(), Integer.valueOf(id),
                  ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE);
          String from =
              fromAbsolutePath + thumbnailsSubDirectory + File.separator +
                  vignette.getOriginalFileName();

          String type =
              vignette.getOriginalFileName().substring(
                  vignette.getOriginalFileName().indexOf(".") + 1,
                  vignette.getOriginalFileName().length());
          String newOriginalImage =
              new Long(new java.util.Date().getTime()).toString() + "." + type;

          String to = toAbsolutePath + thumbnailsSubDirectory + File.separator + newOriginalImage;
          FileRepositoryManager.copyFile(from, to);
          thumbDetail.setOriginalFileName(newOriginalImage);

          // then copy thumnbnail image if exists
          if (vignette.getCropFileName() != null) {
            from =
                fromAbsolutePath + thumbnailsSubDirectory + File.separator +
                    vignette.getCropFileName();
            type =
                vignette.getCropFileName().substring(vignette.getCropFileName().indexOf(".") + 1,
                    vignette.getCropFileName().length());
            String newThumbnailImage =
                new Long(new java.util.Date().getTime()).toString() + "." + type;
            to = toAbsolutePath + thumbnailsSubDirectory + File.separator + newThumbnailImage;
            FileRepositoryManager.copyFile(from, to);
            thumbDetail.setCropFileName(newThumbnailImage);
          }
          thumbDetail.setMimeType(type);
          thumbDetail.setXLength(vignette.getXLength());
          thumbDetail.setYLength(vignette.getYLength());
          thumbDetail.setXStart(vignette.getXStart());
          thumbDetail.setYStart(vignette.getYStart());
          getThumbnailService().createThumbnail(thumbDetail);

          // publi.setImage(newVignette);
        }

        // update id cause new publication is created
        toPubPK.setId(id);

        // Paste positions on Pdc
        pastePdcPositions(fromPubPK, id);

        // paste wysiwyg
        WysiwygController.copy(null, fromComponentId, fromId, null, getComponentId(), id,
            getUserId());

        // paste files
        Map<String, String> fileIds = pasteFiles(fromPubPK, id);

        // eventually, paste the model content
        if (completePub.getModelDetail() != null && completePub.getInfoDetail() != null) {
          // Paste images of model
          if (completePub.getInfoDetail().getInfoImageList() != null) {
            for (Iterator<InfoImageDetail> i =
                completePub.getInfoDetail().getInfoImageList().iterator(); i.hasNext();) {
              InfoImageDetail attachment = i.next();
              String from =
                  fromAbsolutePath + imagesSubDirectory + File.separator
                      + attachment.getPhysicalName();
              String type =
                  attachment.getPhysicalName().substring(
                      attachment.getPhysicalName().indexOf(".") + 1,
                      attachment.getPhysicalName().length());
              String newName = new Long(new java.util.Date().getTime()).toString() + "." + type;
              attachment.setPhysicalName(newName);
              String to = toAbsolutePath + imagesSubDirectory + File.separator + newName;
              FileRepositoryManager.copyFile(from, to);
            }
          }

          // Paste model content
          getKmeliaBm().createInfoModelDetail(toPubPK, completePub.getModelDetail().getId(),
              completePub.getInfoDetail());
        } else {
          String infoId = publi.getInfoId();
          if (infoId != null && !"0".equals(infoId)) {
            // Content = XMLForm
            // register xmlForm to publication
            String xmlFormShortName = infoId;
            getPublicationTemplateManager().addDynamicPublicationTemplate(getComponentId() + ":"
                + xmlFormShortName, xmlFormShortName + ".xml");

            // Paste images
            Hashtable<String, String> imageIds =
                AttachmentController.copyAttachmentByCustomerPKAndContext(fromPubPK, fromPubPK,
                    "XMLFormImages");

            if (imageIds != null) {
              fileIds.putAll(imageIds);
            }

            // Paste wysiwyg fields content
            WysiwygFCKFieldDisplayer wysiwygField = new WysiwygFCKFieldDisplayer();
            wysiwygField.cloneContents(fromComponentId, fromId, getComponentId(), id);

            // get xmlContent to paste
            PublicationTemplate pubTemplateFrom =
                getPublicationTemplateManager().getPublicationTemplate(fromComponentId + ":"
                    + xmlFormShortName);
            IdentifiedRecordTemplate recordTemplateFrom =
                (IdentifiedRecordTemplate) pubTemplateFrom.getRecordSet().getRecordTemplate();

            PublicationTemplate pubTemplate =
                getPublicationTemplateManager().getPublicationTemplate(getComponentId() + ":"
                    + xmlFormShortName);
            IdentifiedRecordTemplate recordTemplate =
                (IdentifiedRecordTemplate) pubTemplate.getRecordSet().getRecordTemplate();

            // paste xml content
            GenericRecordSetManager.getInstance().cloneRecord(recordTemplateFrom, fromId,
                recordTemplate, id, fileIds);
          }
        }

        // force the update
        PublicationDetail newPubli = getPublicationDetail(id);
        newPubli.setStatusMustBeChecked(false);
        getKmeliaBm().updatePublication(newPubli);

        /*
         * if(isCutted && mustBeDeleted) {
         * CallBackManager.invoke(CallBackManager.ACTION_CUTANDPASTE, Integer.parseInt(getUserId()),
         * getComponentId(), new PublicationPK(fromId, fromComponentId)); }
         */
      }
    } catch (Exception ex) {
      SilverTrace.error("kmelia", getClass().getSimpleName() + ".pastePublication()",
          "root.EX_NO_MESSAGE", ex);
    }
  }

  /**
   * get languages of publication header and attachments
   * @param pubDetail
   * @return a List of String (language codes)
   */
  public List<String> getPublicationLanguages() {
    List<String> languages = new ArrayList<String>();

    PublicationDetail pubDetail = getSessionPubliOrClone().getPublication().getPublicationDetail();

    // get publicationdetail languages
    Iterator<String> itLanguages = pubDetail.getLanguages();
    while (itLanguages.hasNext()) {
      languages.add(itLanguages.next());
    }

    if (languages.size() == I18NHelper.getNumberOfLanguages()) {
      // Publication is translated in all supported languages
      return languages;
    } else {
      // get attachments languages
      List<String> attLanguages = getAttachmentLanguages();
      String language = null;
      for (int l = 0; l < attLanguages.size(); l++) {
        language = attLanguages.get(l);
        if (!languages.contains(language)) {
          languages.add(language);
        }
      }
    }

    return languages;
  }

  public List<String> getAttachmentLanguages() {
    PublicationPK pubPK = getSessionPubliOrClone().getPublication().getPublicationDetail().getPK();

    // get attachments languages
    List<String> languages = new ArrayList<String>();
    List<String> attLanguages =
        AttachmentController.getLanguagesOfAttachments(new ForeignPK(pubPK.getId(), pubPK.
            getInstanceId()));
    String language = null;
    for (int l = 0; l < attLanguages.size(); l++) {
      language = attLanguages.get(l);
      if (!languages.contains(language)) {
        languages.add(language);
      }
    }
    return languages;
  }

  public void setAliases(List<Alias> aliases) throws RemoteException {
    getKmeliaBm().setAlias(getSessionPublication().getPublication().getPublicationDetail().getPK(),
        aliases);
  }

  public void setAliases(PublicationPK pubPK, List<Alias> aliases) throws RemoteException {
    getKmeliaBm().setAlias(pubPK, aliases);
  }

  public List<Alias> getAliases() throws RemoteException {
    List<Alias> aliases =
        (List<Alias>) getKmeliaBm().getAlias(
            getSessionPublication().getPublication().getPublicationDetail().getPK());

    // add user's displayed name
    for (Iterator<Alias> iterator = aliases.iterator(); iterator.hasNext();) {
      Alias object = iterator.next();
      if (StringUtil.isDefined(object.getUserId())) {
        object.setUserName(getUserDetail(object.getUserId()).getDisplayedName());
      }
    }

    return aliases;
  }

  /**
   * @return a List of Treeview
   * @throws RemoteException
   */
  public List<Treeview> getOtherComponents(List<Alias> aliases) throws RemoteException {
    List<String> instanceIds = new ArrayList<String>();
    List<Treeview> result = new ArrayList<Treeview>();
    SpaceInstLight space = null;
    String instanceId = null;
    List<NodeDetail> tree = null;
    String path = "";
    Treeview treeview = null;
    NodePK root = new NodePK("0");

    if (KmeliaHelper.isToolbox(getComponentId())) {
      root.setComponentName(getComponentId());
      tree =
          getKmeliaBm().getTreeview(
              root,
              "useless",
              false,
              false,
              getUserId(),
              false,
              "yes".equalsIgnoreCase(getOrganizationController().getComponentParameterValue(
                  instanceId, "rightsOnTopics")));

      treeview = new Treeview(getComponentLabel(), tree, getComponentId());

      treeview.setNbAliases(getNbAliasesInComponent(aliases, instanceId));

      result.add(treeview);
    } else {
      List<SpaceInstLight> spaces = getOrganizationController().getSpaceTreeview(getUserId());
      for (int s = 0; s < spaces.size(); s++) {
        space = spaces.get(s);
        path = "";
        String[] componentIds =
            getOrganizationController().getAvailCompoIdsAtRoot(space.getFullId(), getUserId());
        for (int k = 0; k < componentIds.length; k++) {
          instanceId = componentIds[k];

          if (instanceId.startsWith("kmelia")) {
            String[] profiles =
                getOrganizationController().getUserProfiles(getUserId(), instanceId);
            String bestProfile = KmeliaHelper.getProfile(profiles);
            if (bestProfile.equalsIgnoreCase("admin") || bestProfile.equalsIgnoreCase("publisher")) {
              instanceIds.add(instanceId);
              root.setComponentName(instanceId);

              if (instanceId.equals(getComponentId())) {
                tree =
                    getKmeliaBm().getTreeview(
                        root,
                        "useless",
                        false,
                        false,
                        getUserId(),
                        false,
                        "yes".equalsIgnoreCase(getOrganizationController()
                            .getComponentParameterValue(
                                instanceId, "rightsOnTopics")));
              }

              if (!StringUtil.isDefined(path)) {
                List<SpaceInst> sPath = getOrganizationController().getSpacePath(space.getFullId());
                SpaceInst spaceInPath = null;
                for (int i = 0; i < sPath.size(); i++) {
                  spaceInPath = sPath.get(i);
                  if (i > 0) {
                    path += " > ";
                  }
                  path += spaceInPath.getName();
                }
              }

              treeview =
                  new Treeview(path + " > "
                      + getOrganizationController().getComponentInstLight(instanceId).getLabel(),
                      tree, instanceId);

              treeview.setNbAliases(getNbAliasesInComponent(aliases, instanceId));

              if (instanceId.equals(getComponentId())) {
                result.add(0, treeview);
              } else {
                result.add(treeview);
              }
            }
          }
        }
      }
    }
    return result;
  }

  public List<NodeDetail> getAliasTreeview() throws RemoteException {
    return getAliasTreeview(getComponentId());
  }

  public List<NodeDetail> getAliasTreeview(String instanceId) throws RemoteException {
    String[] profiles = getOrganizationController().getUserProfiles(getUserId(), instanceId);
    String bestProfile = KmeliaHelper.getProfile(profiles);
    List<NodeDetail> tree = null;
    if (bestProfile.equalsIgnoreCase("admin") || bestProfile.equalsIgnoreCase("publisher")) {
      NodePK root = new NodePK("0", instanceId);

      tree =
          getKmeliaBm().getTreeview(
              root,
              "useless",
              false,
              false,
              getUserId(),
              false,
              "yes".equalsIgnoreCase(getOrganizationController().getComponentParameterValue(
                  instanceId, "rightsOnTopics")));
    }
    return tree;
  }

  private int getNbAliasesInComponent(List<Alias> aliases, String instanceId) {
    Alias alias = null;
    int nb = 0;
    for (int a = 0; a < aliases.size(); a++) {
      alias = aliases.get(a);
      if (alias.getInstanceId().equals(instanceId)) {
        nb++;
      }
    }
    return nb;
  }

  private boolean isToolbox() {
    return KmeliaHelper.isToolbox(getComponentId());
  }

  public String getFirstAttachmentURLOfCurrentPublication(String webContext) throws RemoteException {
    PublicationPK pubPK = getSessionPublication().getPublication().getPublicationDetail().getPK();
    String url = null;
    if (isVersionControlled()) {
      VersioningUtil versioning = new VersioningUtil();
      List<Document> documents = versioning.getDocuments(new ForeignPK(pubPK));
      if (documents.isEmpty()) {
        Document document = documents.get(0);
        DocumentVersion documentVersion = versioning.getLastPublicVersion(document.getPk());
        if (documentVersion != null) {
          url =
              versioning.getDocumentVersionURL(document.getInstanceId(), documentVersion.
                  getLogicalName(), document.getPk().getId(), documentVersion.getPk().getId());
        }
      }
    } else {
      Vector<AttachmentDetail> attachments =
          AttachmentController.searchAttachmentByPKAndContext(pubPK, "Images");
      if (attachments.isEmpty()) {
        AttachmentDetail attachment = attachments.get(0);
        url = webContext + attachment.getAttachmentURL();
      }
    }
    return url;
  }
  
  public String getAttachmentURL(String webContext, String attachmentOrDocumentId) throws RemoteException {
	    String url = null;
	    if (isVersionControlled()) {
	      VersioningUtil versioningUtil = new VersioningUtil();
	      Document document = versioningUtil.getDocument(new DocumentPK(Integer.parseInt(attachmentOrDocumentId)));
	      DocumentVersion documentVersion = versioningUtil.getLastPublicVersion(new DocumentPK(Integer.parseInt(attachmentOrDocumentId)));
	      url = webContext + 
	    	  versioningUtil.getDocumentVersionURL(document.getInstanceId(), documentVersion.
                  getLogicalName(), document.getPk().getId(), documentVersion.getPk().getId());
	    } else {
	    	AttachmentDetail attachment = AttachmentController.searchAttachmentByPK(new AttachmentPK(attachmentOrDocumentId));
	    	url = webContext + attachment.getAttachmentURL();
	    }
	    return url;
  }

  public boolean useUpdateChain() {
    return "yes".equals(getComponentParameterValue("updateChain"));
  }

  public void setFieldUpdateChain(Fields fields) {
    this.saveFields = fields;
  }

  public Fields getFieldUpdateChain() {
    return saveFields;
  }

  public void initUpdateChainTopicChoice(String pubId) {
    Collection<NodePK> path;
    try {
      String[] topics = null;
      if (saveFields.getTree() != null) {
        // initialisation du premier thème coché
        FieldParameter param = saveFields.getTree().getParams().get(0);
        if (!param.getName().equals("none")) {
          String id = param.getValue();
          topics = new String[1];
          NodePK node = getNodeHeader(id).getNodePK();
          topics[0] = id + "," + node.getComponentName();
        }
      } else {
        path = getPublicationFathers(pubId);
        topics = new String[path.size()];
        Iterator<NodePK> it = path.iterator();
        int i = 0;
        while (it.hasNext()) {
          NodePK node = it.next();
          topics[i] = node.getId() + "," + node.getComponentName();
          i++;
        }

      }
      getFieldUpdateChain().setTopics(topics);
    } catch (RemoteException e) {
      getFieldUpdateChain().setTopics(null);
    }
  }

  public boolean isTopicHaveUpdateChainDescriptor() {
    return isTopicHaveUpdateChainDescriptor(null);
  }

  public boolean isTopicHaveUpdateChainDescriptor(String id) {
    boolean haveDescriptor = false;
    // regarder si ce fichier existe
    if (useUpdateChain()) {
      if (!StringUtil.isDefined(id)) {
        id = getSessionTopic().getNodePK().getId();
      }
      File descriptorFile = new File(getUpdateChainDescriptorFilename(id));
      if (descriptorFile.exists()) {
        haveDescriptor = true;
      }
    }
    return haveDescriptor;
  }

  private String getUpdateChainDescriptorFilename(String topicId) {
    return getSettings().getString("updateChainRepository") + getComponentId() + "_" + topicId
        + ".xml";
  }

  public synchronized List<NodeDetail> getSubTopics(String rootId) throws RemoteException {
    return getNodeBm().getSubTree(getNodePK(rootId));
  }

  public List<NodeDetail> getUpdateChainTopics() throws RemoteException {
    List<NodeDetail> topics = new ArrayList<NodeDetail>();
    if (getFieldUpdateChain().getTree() != null) {
      FieldParameter param = getFieldUpdateChain().getTree().getParams().get(0);
      if (param.getName().equals("rootId")) {
        topics = getSubTopics(param.getValue());
      }
      if (param.getName().equals("targetId")) {
        topics.add(getNodeHeader(param.getValue()));
      }
    } else {
      topics = getAllTopics();
    }
    return topics;
  }

  public void initUpdateChainDescriptor() throws IOException, ClassNotFoundException,
      ParserConfigurationException {
    XStream xstream = new XStream(new DomDriver());
    xstream.alias("fieldDescriptor", FieldUpdateChainDescriptor.class);
    xstream.alias("updateChain", UpdateChainDescriptor.class);
    xstream.alias("parameter", FieldParameter.class);

    File descriptorFile =
        new File(getUpdateChainDescriptorFilename(getSessionTopic().getNodePK().getId()));
    UpdateChainDescriptor updateChainDescriptor =
        (UpdateChainDescriptor) xstream.fromXML(new FileReader(descriptorFile));

    String title = updateChainDescriptor.getTitle();
    String libelle = updateChainDescriptor.getLibelle();
    saveFields.setTitle(title);
    saveFields.setLibelle(libelle);

    List<FieldUpdateChainDescriptor> fields = updateChainDescriptor.getFields();
    Iterator<FieldUpdateChainDescriptor> it = fields.iterator();
    while (it.hasNext()) {
      FieldUpdateChainDescriptor field = it.next();

      saveFields.setHelper(updateChainDescriptor.getHelper());

      if (field.getName().equals("Name")) {
        saveFields.setName(field);
      } else if (field.getName().equals("Description")) {
        saveFields.setDescription(field);
      } else if (field.getName().equals("Keywords")) {
        saveFields.setKeywords(field);
      } else if (field.getName().equals("Topics")) {
        saveFields.setTree(field);
      }
    }

  }

  public String getXmlFormForFiles() {
    return getComponentParameterValue("XmlFormForFiles");
  }

  public File exportPublication() {
    PublicationPK pubPK = getSessionPublication().getPublication().getPublicationDetail().getPK();
    try {
      // get PDF
      String pdf = generatePdf(pubPK.getId());
      String pdfWithoutExtension = FilenameUtils.removeExtension(pdf);
      // create subdir to zip
      String subdir = "ExportPubli_" + pubPK.getId() + "_" + new Date().getTime();
      String subDirPath =
          FileRepositoryManager.getTemporaryPath() + subdir + File.separator + pdfWithoutExtension;
      FileFolderManager.createFolder(subDirPath);
      // copy pdf into zip
      String filePath = FileRepositoryManager.getTemporaryPath("useless", getComponentId()) + pdf;
      FileRepositoryManager.copyFile(filePath, subDirPath + File.separator + pdf);
      // copy files
      new AttachmentImportExport().getAttachments(pubPK, subDirPath, "useless", null);
      new VersioningImportExport().exportDocuments(pubPK, subDirPath, "useless", null);
      String zipFileName = FileRepositoryManager.getTemporaryPath() + pdfWithoutExtension + ".zip";
      // zip PDF and files
      ZipManager.compressPathToZip(subDirPath, zipFileName);

      return new File(zipFileName);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.exportPublication()",
          SilverpeasRuntimeException.ERROR, "kmelia.CANT_EXPORT_PUBLICATION", e);
    }
  }

  public boolean isNotificationAllowed() {
    String parameterValue = getComponentParameterValue("notifications");
    if (!StringUtil.isDefined(parameterValue)) {
      return true;
    } else {
      return "yes".equals(parameterValue.toLowerCase());
    }
  }

  public boolean isWysiwygOnTopicsEnabled() {
    return "yes".equals(getComponentParameterValue("wysiwygOnTopics").toLowerCase());
  }

  public String getWysiwygOnTopic(String id) {
    if (isWysiwygOnTopicsEnabled()) {
      try {
        if (!StringUtil.isDefined(id)) {
          id = getSessionTopic().getNodePK().getId();
        }
        return WysiwygController.load(getComponentId(), "Node_" + id, getLanguage());
      } catch (WysiwygException e) {
        return "";
      }
    }
    return "";
  }

  public String getWysiwygOnTopic() {
    return getWysiwygOnTopic(null);
  }

  public List<NodeDetail> getTopicPath(String topicId) {
    List<NodeDetail> newPath = new ArrayList<NodeDetail>();
    try {
      List<NodeDetail> pathInReverse =
          (List<NodeDetail>) getNodeBm().getPath(new NodePK(topicId, getComponentId()));
      // reverse the path from root to leaf
      for (int i = pathInReverse.size() - 1; i >= 0; i--) {
        newPath.add(pathInReverse.get(i));
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getTopicPath()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DAVOIR_LE_CHEMIN_COURANT", e);
    }
    return newPath;
  }

  public int[] getThumbnailWidthAndHeight() {
    int widthInt = getLengthFromXMLDescriptor("thumbnailWidthSize");
    int heightInt = getLengthFromXMLDescriptor("thumbnailHeightSize");

    if (widthInt == -1 && heightInt == -1) {
      // 2ième chance si nécessaire
      widthInt = getLengthFromProperties("vignetteWidth");
      heightInt = getLengthFromProperties("vignetteHeight");
    }

    return new int[] { widthInt, heightInt };
  }

  private int getLengthFromProperties(String name) {
    int length = -1;
    String lengthFromProperties = getSettings().getString(name);
    try {
      length = Integer.parseInt(lengthFromProperties);
    } catch (NumberFormatException e) {
      SilverTrace.info("kmelia", "KmeliaSessionController.getLengthFromProperties()",
          "root.MSG_GEN_PARAM_VALUE", "properties wrong parameter " + name + " = " +
              lengthFromProperties);
    }
    return length;
  }

  private int getLengthFromXMLDescriptor(String name) {
    int length = -1;
    String lengthFromXml = getComponentParameterValue(name);
    if (StringUtil.isDefined(lengthFromXml)) {
      try {
        length = Integer.parseInt(lengthFromXml);
      } catch (NumberFormatException e) {
        SilverTrace.info("kmelia", "KmeliaSessionController.getLengthFromXMLDescriptor()",
            "root.MSG_GEN_PARAM_VALUE", "xml wrong parameter " + name + " = " +
                lengthFromXml);
      }
    }
    return length;
  }

  /**
   * return the value of component parameter "axisIdGlossary". This paramater indicate the axis of
   * pdc to use to highlight word in publication content
   * @return an indentifier of Pdc axis
   */
  public String getAxisIdGlossary() {
    return getComponentParameterValue("axisIdGlossary");
  }

  public List<ComponentInstLight> getGalleries() {
    List<ComponentInstLight> galleries = null;
    OrganizationController orgaController = new OrganizationController();
    String[] compoIds = orgaController.getCompoId("gallery");
    for (int c = 0; c < compoIds.length; c++) {
      if ("yes".equalsIgnoreCase(orgaController.getComponentParameterValue("gallery" + compoIds[c],
          "viewInWysiwyg"))) {
        if (galleries == null) {
          galleries = new ArrayList<ComponentInstLight>();
        }

        ComponentInstLight gallery = orgaController.getComponentInstLight("gallery" + compoIds[c]);
        galleries.add(gallery);
      }
    }
    return galleries;
  }

  public boolean isAdmin() {
    return SilverpeasRole.admin.isInRole(getRole());
  }

  public boolean isWriter() {
    return SilverpeasRole.writer.isInRole(getRole());
  }

  public boolean isPublisher() {
    return SilverpeasRole.publisher.isInRole(getRole());
  }

  public boolean isUser() {
    return SilverpeasRole.user.isInRole(getRole());
  }

  public String getRole() {
    try {
      return getProfile();
    } catch (RemoteException ex) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getRole()",
          SilverpeasRuntimeException.ERROR, "kmelia.MSG_ERR_GENERAL", ex);
    }
  }

  public String displayPath(Collection<NodeDetail> path, boolean linked, int beforeAfter) {
    StringBuilder linkedPathString = new StringBuilder();
    StringBuilder pathString = new StringBuilder();
    int nbItemInPath = path.size();
    Iterator<NodeDetail> iterator = path.iterator();
    boolean alreadyCut = false;
    int i = 0;
    NodeDetail nodeInPath = null;
    while (iterator.hasNext()) {
      nodeInPath = iterator.next();
      if ((i <= beforeAfter) || (i + beforeAfter >= nbItemInPath - 1)) {
        if (!nodeInPath.getNodePK().getId().equals("0")) {
          String nodeName;
          if (getCurrentLanguage() != null) {
            nodeName = nodeInPath.getName(getCurrentLanguage());
          } else {
            nodeName = nodeInPath.getName();
          }
          linkedPathString.append("<a href=\"javascript:onClick=topicGoTo('").append(
              nodeInPath.getNodePK().getId()).append("')\">").append(
              EncodeHelper.javaStringToHtmlString(nodeName)).append("</a>");
          pathString.append(EncodeHelper.javaStringToHtmlString(nodeName));
          if (iterator.hasNext()) {
            linkedPathString.append(" > ");
            pathString.append(" > ");
          }
        }
      } else {
        if (!alreadyCut) {
          linkedPathString.append(" ... > ");
          pathString.append(" ... > ");
          alreadyCut = true;
        }
      }
      i++;
    }
    nodeInPath = null;
    if (linked) {
      return linkedPathString.toString();
    } else {
      return pathString.toString();
    }
  }

  /**
   * Is search in topics enabled
   * @return boolean
   */
  public boolean isSearchOnTopicsEnabled() {
    return "yes".equals(getComponentParameterValue("searchOnTopics").toLowerCase());
  }

  /**
   * Get publications and aliases of this topic and its subtopics answering to the query
   * @param query
   * @param sort
   * @return List of UserPublication
   */
  public synchronized List<UserPublication> search(String query, int sort) {
    LinkedHashSet<UserPublication> userPublications = new LinkedHashSet<UserPublication>();
    QueryDescription queryDescription = new QueryDescription(query);
    queryDescription.setSearchingUser(getUserDetail().getId());

    // Search in all spaces and components (to find alias)
    String[] spacesIds = getOrganizationController().getAllSpaceIds(getUserDetail().getId());
    for (int s = 0; s < spacesIds.length; s++) {
      String[] componentsIds =
          getOrganizationController().getAvailCompoIds(spacesIds[s], getUserDetail().getId());
      for (int c = 0; c < componentsIds.length; c++) {
        queryDescription.addSpaceComponentPair(spacesIds[s], componentsIds[c]);
      }
    }

    MatchingIndexEntry[] results = null;
    try {
      try {
        getSearchEngine().search(queryDescription);
      } catch (NoSuchObjectException nsoe) {
        // reference to EJB Session statefull is expired
        // getting a new one...
        searchEngineEjb = null;
        // re-launching the search
        getSearchEngine().search(queryDescription);
      }
      results = getSearchEngine().getRange(0, getSearchEngine().getResultLength());
      MatchingIndexEntry result = null;
      PublicationDetail pubDetail = new PublicationDetail();
      pubDetail.setPk(new PublicationPK("unknown"));
      UserPublication publication = new UserPublication();
      publication.setPublication(pubDetail);

      // get visible publications in the topic and sub-topics
      List<WAAttributeValuePair> pubsInPath =
          getAllVisiblePublicationsByTopic(getSessionTopic().getNodePK().getId());

      // Store all descendant topicIds of this topic
      List<String> nodeIDs = new ArrayList<String>();

      // Get current topic too
      nodeIDs.add(getSessionTopic().getNodePK().getId());
      Collection<NodePK> nodePKs = getNodeBm().getDescendantPKs(getSessionTopic().getNodePK());
      for (NodePK nodePK : nodePKs) {
        nodeIDs.add(nodePK.getId());
      }

      List<String> pubIds = new ArrayList<String>();

      for (int r = 0; r < results.length; r++) {
        result = results[r];

        if ("Publication".equals(result.getObjectType())
            || result.getObjectType().startsWith("Attachment")
            || result.getObjectType().startsWith("Versioning")) {
          pubDetail.getPK().setId(result.getObjectId());

          PublicationPK pubPK = new PublicationPK(result.getObjectId(), result.getComponent());
          Collection<Alias> pubAliases = getKmeliaBm().getAlias(pubPK);

          // Add the alias which have a link to the targets topics
          for (Alias alias : pubAliases) {
            if (nodeIDs.contains(alias.getId())) {
              if (!pubIds.contains(pubDetail.getId())) {
                pubIds.add(pubDetail.getId());
              }
            }
          }

          // Add the publications
          WAAttributeValuePair pubWAFound =
              new WAAttributeValuePair(pubDetail.getId(), result.getComponent());
          int index = pubsInPath.indexOf(pubWAFound);
          if (index != -1) {
            // Add only if not yet in the returned results
            if (!pubIds.contains(pubDetail.getId())) {
              pubIds.add(pubDetail.getId());
            }
          }
        }
      }
      for (String pubId : pubIds) {
        publication = new UserPublication();
        publication.setPublication(getPublicationDetail(pubId));
        publication.setOwner(getUserDetail(pubDetail.getCreatorId()));
        userPublications.add(publication);
      }
    } catch (Exception pe) {
      throw new KmeliaRuntimeException("KmeliaSessionController.search",
          SilverpeasRuntimeException.ERROR, "root.EX_SEARCH_ENGINE_FAILED", pe);
    }
    return sort(userPublications, sort);
  }

  public String getPublicationPdfName(String pubId) throws RemoteException {
    String lang = getLanguage();
    StringBuilder pdfName = new StringBuilder(250);
    TopicDetail topic = getPublicationTopic(pubId);
    List<SpaceInst> listSpaces = getSpacePath();
    for (SpaceInst space : listSpaces) {
      pdfName.append(space.getName(lang)).append('-');
    }
    pdfName.append(getComponentLabel());
    Collection<NodeDetail> path = topic.getPath();
    for (NodeDetail node : path) {
      pdfName.append('-').append(node.getName(lang));
    }
    CompletePublication complete = getCompletePublication(pubId);
    pdfName.append('-').append(complete.getPublicationDetail().getTitle()).append('-');
    pdfName.append(pubId).append(".pdf");
    return pdfName.toString();
  }

  public List<SpaceInst> getSpacePath() {
    return this.getOrganizationController().getSpacePath(this.getSpaceId());
  }

  /**
   * Gets an instance of PublicationTemplateManager.
   * @return an instance of PublicationTemplateManager.
   */
  private PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }
}
