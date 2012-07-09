/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
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

import static com.silverpeas.kmelia.export.KmeliaPublicationExporter.EXPORT_FOR_USER;
import static com.silverpeas.kmelia.export.KmeliaPublicationExporter.EXPORT_LANGUAGE;
import static com.silverpeas.kmelia.export.KmeliaPublicationExporter.EXPORT_TOPIC;
import static com.silverpeas.kmelia.export.KmeliaPublicationExporter.aKmeliaPublicationExporter;
import static com.silverpeas.pdc.model.PdcClassification.NONE_CLASSIFICATION;
import static com.silverpeas.pdc.model.PdcClassification.aPdcClassificationOfContent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJBObject;
import javax.ejb.RemoveException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.silverpeas.search.SearchEngineFactory;

import com.silverpeas.attachment.importExport.AttachmentImportExport;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.comment.service.CommentServiceFactory;
import com.silverpeas.converter.DocumentFormat;
import com.silverpeas.delegatednews.model.DelegatedNews;
import com.silverpeas.delegatednews.service.DelegatedNewsService;
import com.silverpeas.delegatednews.service.ServicesFactory;
import com.silverpeas.export.ExportDescriptor;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.displayers.WysiwygFCKFieldDisplayer;
import com.silverpeas.form.record.GenericRecordSetManager;
import com.silverpeas.form.record.IdentifiedRecordTemplate;
import com.silverpeas.kmelia.export.ExportFileNameProducer;
import com.silverpeas.pdc.PdcServiceFactory;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.model.PdcPosition;
import com.silverpeas.pdc.service.PdcClassificationService;
import com.silverpeas.pdc.web.PdcClassificationEntity;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.thumbnail.ThumbnailException;
import com.silverpeas.thumbnail.control.ThumbnailController;
import com.silverpeas.thumbnail.model.ThumbnailDetail;
import com.silverpeas.thumbnail.service.ThumbnailService;
import com.silverpeas.thumbnail.service.ThumbnailServiceImpl;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.ZipManager;
import com.silverpeas.util.clipboard.ClipboardSelection;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.silverpeas.versioning.importExport.VersioningImportExport;
import com.stratelia.silverpeas.alertUser.AlertUser;
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
import com.stratelia.silverpeas.versioning.ejb.VersioningBm;
import com.stratelia.silverpeas.versioning.ejb.VersioningBmHome;
import com.stratelia.silverpeas.versioning.ejb.VersioningRuntimeException;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.model.Reader;
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
import com.stratelia.webactiv.kmelia.model.KmeliaPublication;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.kmelia.model.PubliAuthorComparatorAsc;
import com.stratelia.webactiv.kmelia.model.PubliCreationDateComparatorAsc;
import com.stratelia.webactiv.kmelia.model.PubliImportanceComparatorDesc;
import com.stratelia.webactiv.kmelia.model.PubliRankComparatorAsc;
import com.stratelia.webactiv.kmelia.model.PubliUpdateDateComparatorAsc;
import com.stratelia.webactiv.kmelia.model.TopicDetail;
import com.stratelia.webactiv.kmelia.model.Treeview;
import com.stratelia.webactiv.kmelia.model.updatechain.FieldParameter;
import com.stratelia.webactiv.kmelia.model.updatechain.FieldUpdateChainDescriptor;
import com.stratelia.webactiv.kmelia.model.updatechain.Fields;
import com.stratelia.webactiv.kmelia.model.updatechain.UpdateChainDescriptor;
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

public class KmeliaSessionController extends AbstractComponentSessionController implements
        ExportFileNameProducer {

  /**
   * The different export formats the KmeliaPublicationExporter should support.
   */
  private static final String EXPORT_FORMATS = "kmelia.export.formats.active";
  /**
   * All the formats that are available for the export of publications.
   */
  private static final String[] AVAILABLE_EXPORT_FORMATS = { "zip", "pdf", "odt", "doc" };

  /* EJBs used by sessionController */
  private ThumbnailService thumbnailService = null;
  private CommentService commentService = null;
  private VersioningBm versioningBm = null;
  private PdcBm pdcBm = null;
  private StatisticBm statisticBm = null;
  private NotificationManager notificationManager = null;
  // Session objects
  private TopicDetail sessionTopic = null;
  private KmeliaPublication sessionPublication = null;
  private KmeliaPublication sessionClone = null;
  private String sessionPath = null; // html link with <a href="">
  private String sessionPathString = null; // html string only
  private TopicDetail sessionTopicToLink = null;
  private boolean sessionOwner = false;
  private List<KmeliaPublication> sessionPublicationsList = null;
  private List<String> sessionCombination = null; // Specific Kmax
  private String sessionTimeCriteria = null; // Specific Kmax
  private List<NodeDetail> sessionTreeview = null;
  private String sortValue = null;
  private String defaultSortValue = "2";
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
  boolean componentManageable = false;

  private List<String> selectedPublicationIds = new ArrayList<String>();
  private boolean customPublicationTemplateUsed = false;
  private String customPublicationTemplateName = null;

  /**
   * Creates new sessionClientController
   * @param mainSessionCtrl
   * @param context
   */
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
    if (StringUtil.getBooleanValue(getSettings().getString("massiveDragAndDropAllowed"))) {
      isDragAndDropEnableByUser = isDragAndDropEnableByUser();
    }
    componentManageable = GeneralPropertiesManager.getGeneralResourceLocator().getBoolean(
            "AdminFromComponentEnable", true);
    if (componentManageable) {
      componentManageable = getOrganizationController().isComponentManageable(getComponentId(),
              getUserId());
    }
    defaultSortValue = getComponentParameterValue("publicationSort");
    if (!StringUtil.isDefined(defaultSortValue)) {
      defaultSortValue = getSettings().getString("publications.sort.default", "2");
    }
    // check if this instance use a specific template of publication
    SilverpeasTemplate template = SilverpeasTemplateFactory.createSilverpeasTemplateOnComponents();
    customPublicationTemplateName = "publication_" + getComponentId();
    customPublicationTemplateUsed =
        template.isCustomTemplateExists("kmelia", customPublicationTemplateName);
  }

  /**
   * Gets a business service of comments.
   * @return a DefaultCommentService instance.
   */
  protected CommentService getCommentService() {
    if (commentService == null) {
      commentService = CommentServiceFactory.getFactory().getCommentService();
    }
    return commentService;
  }

  public KmeliaBm getKmeliaBm() {
    try {
      KmeliaBmHome kscEjbHome = EJBUtilitaire.getEJBObjectRef(JNDINames.KMELIABM_EJBHOME,
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
            EJBUtilitaire.getEJBObjectRef(JNDINames.STATISTICBM_EJBHOME,
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
        VersioningBmHome vscEjbHome = EJBUtilitaire.getEJBObjectRef(JNDINames.VERSIONING_EJBHOME,
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

  public int getNbPublicationsOnRoot() {
    if (nbPublicationsOnRoot == -1) {
      String parameterValue = getComponentParameterValue("nbPubliOnRoot");
      if (StringUtil.isDefined(parameterValue)) {
        nbPublicationsOnRoot = Integer.parseInt(parameterValue);
      } else {
        if (KmeliaHelper.isToolbox(getComponentId())) {
          nbPublicationsOnRoot = 0;
        } else {
          // lecture du properties
          nbPublicationsOnRoot = getSettings().getInteger("HomeNbPublications", 15);
        }
      }
    }
    return nbPublicationsOnRoot;
  }

  public int getNbPublicationsPerPage() {
    if (nbPublicationsPerPage == -1) {
      String parameterValue = this.getComponentParameterValue("nbPubliPerPage");
      if (parameterValue == null || parameterValue.length() <= 0) {
        nbPublicationsPerPage = getSettings().getInteger("NbPublicationsParPage", 10);
      } else {
        try {
          nbPublicationsPerPage = Integer.parseInt(parameterValue);
        } catch (Exception e) {
          nbPublicationsPerPage = getSettings().getInteger("NbPublicationsParPage", 10);
        }
      }
    }
    return nbPublicationsPerPage;
  }

  public boolean isDraftVisibleWithCoWriting() {
    return getSettings().getBoolean("draftVisibleWithCoWriting", false);
  }

  public boolean isTreeStructure() {
    String param = getComponentParameterValue("istree");
    if (!StringUtil.isDefined(param)) {
      return true;
    }
    return "0".equals(param) || "1".equals(param);
  }

  public boolean isTreeviewUsed() {
    String param = getComponentParameterValue("istree");
    if (!StringUtil.isDefined(param)) {
      return true;
    }
    return "0".equals(param);
  }

  public boolean isPdcUsed() {
    return StringUtil.getBooleanValue(getComponentParameterValue("usepdc"));
  }

  public boolean isDraftEnabled() {
    return StringUtil.getBooleanValue(getComponentParameterValue("draft"));
  }

  public boolean isOrientedWebContent() {
    return StringUtil.getBooleanValue(getComponentParameterValue("webContent"));
  }

  public boolean isSortedTopicsEnabled() {
    return StringUtil.getBooleanValue(getComponentParameterValue("sortedTopics"));
  }

  public boolean isTopicManagementDelegated() {
    return StringUtil.getBooleanValue(getComponentParameterValue("delegatedTopicManagement"));
  }

  public boolean isAuthorUsed() {
    return StringUtil.getBooleanValue(getComponentParameterValue("useAuthor"));
  }

  public boolean openSingleAttachmentAutomatically() {
    return StringUtil.getBooleanValue(getComponentParameterValue("openSingleAttachment"));
  }

  public boolean isImportFileAllowed() {
    String parameterValue = getComponentParameterValue("importFiles");
    if (!StringUtil.isDefined(parameterValue)) {
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
      if (StringUtil.getBooleanValue(parameterValue) || "both".equalsIgnoreCase(parameterValue)) {
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
    }
    if (UPDATE_DIRECT_MODE.equalsIgnoreCase(parameterValue)) {
      return UPDATE_DIRECT_MODE;
    }
    if (UPDATE_SHORTCUT_MODE.equalsIgnoreCase(parameterValue)) {
      return UPDATE_SHORTCUT_MODE;
    }
    return NO_UPDATE_MODE;
  }

  public boolean isExportComponentAllowed() {
    return "yes".equals(getSettings().getString("exportComponentAllowed"));
  }

  public boolean isMassiveDragAndDropAllowed() {
    return StringUtil.getBooleanValue(getComponentParameterValue("massiveDragAndDrop"));
  }

  public boolean isPublicationAlwaysVisibleEnabled() {
    return StringUtil.getBooleanValue(getComponentParameterValue("publicationAlwaysVisible"));
  }

  public boolean isWizardEnabled() {
    return StringUtil.getBooleanValue(getComponentParameterValue("wizardEnabled"));
  }

  public boolean displayNbPublis() {
    return StringUtil.getBooleanValue(getComponentParameterValue("displayNB"));
  }

  public boolean isRightsOnTopicsEnabled() {
    return StringUtil.getBooleanValue(getComponentParameterValue("rightsOnTopics"));
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
  
  public boolean isFolderSharingEnabled() {
    return "yes".equals(getComponentParameterValue("useFolderSharing"));
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
    }
    return "yes".equals(parameterValue.toLowerCase());
  }

  public boolean showUserNameInList() {
    return getSettings().getBoolean("showUserNameInList", true);
  }

  /**
   * @return
   */
  public String getVersionningFileRightsMode() {
    String parameterValue = this.getComponentParameterValue("versionUseFileRights");
    if (parameterValue == null || parameterValue.length() <= 0) {
      return VER_USE_WRITERS_AND_READERS;
    }
    return parameterValue;
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

  /**
   * Generates a document in the specified format from the specified publication.
   * @param inFormat the format of the document to generate.
   * @param fromPubId the unique identifier of the publication from which the document will be
   * generated.
   * @return the generated document as a File instance.
   */
  public File generateDocument(final DocumentFormat inFormat, String fromPubId) {
    SilverTrace.info("kmelia", "KmeliaSessionControl.KmeliaSessionController.generateDocument()",
            "root.MSG_ENTRY_METHOD");
    if (!isFormatSupported(inFormat.name())) {
      throw new KmeliaRuntimeException("kmelia", SilverTrace.TRACE_LEVEL_ERROR,
              "kmelia.EX_EXPORT_FORMAT_NOT_SUPPORTED");
    }
    File document = null;
    if (fromPubId != null) {
      try {
        KmeliaPublication publication =
                KmeliaPublication.aKmeliaPublicationWithPk(new PublicationPK(
                    fromPubId, getComponentId()));
        if (isVersionControlled()) {
          publication.versioned();
        }
        String fileName = getPublicationExportFileName(publication, getLanguage());
        document = new File(FileRepositoryManager.getTemporaryPath() + fileName + "." + inFormat.
                name());
        FileOutputStream output = new FileOutputStream(document);
        ExportDescriptor descriptor = ExportDescriptor.withOutputStream(output).
                withParameter(EXPORT_FOR_USER, getUserDetail()).
                withParameter(EXPORT_LANGUAGE, getLanguage()).
                withParameter(EXPORT_TOPIC, getSessionTopic()).
                inFormat(inFormat.name());
        aKmeliaPublicationExporter().export(descriptor, publication);
      } catch (Exception ex) {
        Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE, ex.getMessage(), ex);
        if (document != null) {
          document.delete();
        }
        throw new KmeliaRuntimeException("KmeliaSessionController.generateDocument()",
                SilverpeasRuntimeException.ERROR, "kmelia.EX_CANT_EXPORT_PUBLICATION", ex);
      }
    }
    return document;
  }

  /************************************************************************************************/
  // Current User operations
  /**
   * ********************************************************************************************
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

  /*
   * Topic management
   */
  public NodePK getRootPK() {
    return new NodePK(NodePK.ROOT_NODE_ID, getComponentId());
  }

  public synchronized TopicDetail getTopic(String id) throws RemoteException {
    return getTopic(id, true);
  }

  public synchronized TopicDetail getTopic(String id, boolean resetSessionPublication)
          throws RemoteException {
    if (resetSessionPublication) {
      setSessionPublication(null);
    }
    if (getSessionTopic() == null || !id.equals(
            getSessionTopic().getNodeDetail().getNodePK().getId())) {
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
    if (isTreeviewUsed() || displayNbPublis()) {
      if (isUserComponentAdmin()) {
        treeview = getKmeliaBm().getTreeview(getNodePK("0"), "admin", isCoWritingEnable(),
                isDraftVisibleWithCoWriting(), getUserId(), displayNbPublis(), false);
      } else {
        treeview = getKmeliaBm().getTreeview(getNodePK("0"), getProfile(), isCoWritingEnable(),
                isDraftVisibleWithCoWriting(), getUserId(), displayNbPublis(),
                isRightsOnTopicsEnabled());
      }
      setSessionTreeview(treeview);
    }
    if (displayNbPublis()) {
      // set nb objects of current root
      currentTopic.getNodeDetail().setNbObjects(treeview.get(0).getNbObjects());
      // set nb objects of children
      Collection<NodeDetail> children = currentTopic.getNodeDetail().getChildrenDetails();
      for (NodeDetail node : children) {
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
    TopicDetail currentTopic = getKmeliaBm().getPublicationFather(getPublicationPK(pubId),
            isTreeStructure(), getUserId(), isRightsOnTopicsEnabled());
    setSessionTopic(currentTopic);
    applyVisibilityFilter();
    return currentTopic;
  }

  public synchronized List<NodeDetail> getAllTopics() throws RemoteException {
    return getNodeBm().getSubTree(getNodePK(NodePK.ROOT_NODE_ID));
  }

  public synchronized List<NodeDetail> getTreeview() throws RemoteException {
    if (isTreeviewUsed()) {
      return getSessionTreeview();// getKmeliaBm().getTreeview(getNodePK("0"), getProfile(),
    } // isCoWritingEnable(), isDraftVisibleWithCoWriting(),
    // getUserId(), displayNbPublis());
    return null;
  }

  public synchronized void flushTrashCan() throws RemoteException {
    SilverTrace.info("kmelia", "KmeliaSessionControl.flushTrashCan", "root.MSG_ENTRY_METHOD");
    TopicDetail td = getKmeliaBm().goTo(getNodePK(NodePK.BIN_NODE_ID), getUserId(), false,
            getUserTopicProfile("1"), isRightsOnTopicsEnabled());
    setSessionTopic(td);
    Collection<KmeliaPublication> pds = td.getKmeliaPublications();
    Iterator<KmeliaPublication> ipds = pds.iterator();
    SilverTrace.info("kmelia", "KmeliaSessionControl.flushTrashCan", "root.MSG_PARAM_VALUE",
            "NbPubli=" + pds.size());
    while (ipds.hasNext()) {
      String theId = (ipds.next()).getDetail().getPK().getId();
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
    return getKmeliaBm().addSubTopic(getNodePK(parentId), nd, alertType);
  }

  public synchronized String deleteTopic(String topicId) throws RemoteException {
    if (NodePK.ROOT_NODE_ID.equals(topicId) || NodePK.BIN_NODE_ID.equals(topicId)) {
      return null;
    }
    NodeDetail node = getNodeHeader(topicId);
    // check if user is allowed to delete this topic
    if (SilverpeasRole.admin.isInRole(getUserTopicProfile(topicId))
            || SilverpeasRole.admin.isInRole(getUserTopicProfile(node.getFatherPK().getId()))) {
      // First, remove rights on topic and its descendants
      List<NodeDetail> treeview = getNodeBm().getSubTree(getNodePK(topicId));
      for (NodeDetail nodeToDelete : treeview) {
        deleteTopicRoles(nodeToDelete);
      }
      // Then, remove the topic itself
      getKmeliaBm().deleteTopic(getNodePK(topicId));

      return node.getFatherPK().getId();
    }
    return null;
  }

  public synchronized void changeSubTopicsOrder(String way, String subTopicId)
          throws RemoteException {
    getKmeliaBm().changeSubTopicsOrder(way, getNodePK(subTopicId), getSessionTopic().getNodePK());
  }

  public synchronized void changeTopicStatus(String newStatus, String topicId,
          boolean recursiveChanges) throws RemoteException {
    getKmeliaBm().changeTopicStatus(newStatus, getNodePK(topicId), recursiveChanges);
  }

  /**
   * @return
   * @throws RemoteException
   */
  public synchronized Collection<Collection<NodeDetail>> getSubscriptionList() throws
          RemoteException {
    return getKmeliaBm().getSubscriptionList(getUserId(), getComponentId());
  }

  public synchronized void removeSubscription(String topicId) throws RemoteException {
    getKmeliaBm().removeSubscriptionToCurrentUser(getNodePK(topicId), getUserId());
  }

  public synchronized void addSubscription(String topicId) throws RemoteException {
    getKmeliaBm().addSubscription(getNodePK(topicId), getUserId());
  }

  /**
   * @param pubId
   * @return
   * @throws RemoteException
   */
  public synchronized PublicationDetail getPublicationDetail(String pubId) throws RemoteException {
    return getKmeliaBm().getPublicationDetail(getPublicationPK(pubId));
  }

  public synchronized Collection<Collection<NodeDetail>> getPathList(String pubId)
          throws RemoteException {
    return getKmeliaBm().getPathList(getPublicationPK(pubId));
  }

  public synchronized Collection<NodePK> getPublicationFathers(String pubId)
          throws RemoteException {
    return getKmeliaBm().getPublicationFathers(getPublicationPK(pubId));
  }

  public synchronized String createPublication(PublicationDetail pubDetail,
          final PdcClassificationEntity classification) throws RemoteException {
    pubDetail.getPK().setSpace(getSpaceId());
    pubDetail.getPK().setComponentName(getComponentId());
    pubDetail.setCreatorId(getUserId());
    pubDetail.setCreationDate(new Date());

    String result = null;
    if (isKmaxMode) {
      result = getKmeliaBm().createKmaxPublication(pubDetail);
    } else {
      if (classification.isUndefined()) {
        result = getKmeliaBm().createPublicationIntoTopic(pubDetail, getSessionTopic().getNodePK());
      } else {
        List<PdcPosition> pdcPositions = classification.getPdcPositions();
        PdcClassification withClassification = aPdcClassificationOfContent(pubDetail.getId(),
                pubDetail.getComponentInstanceId()).withPositions(pdcPositions);
        result = getKmeliaBm().createPublicationIntoTopic(pubDetail, getSessionTopic().getNodePK(),
                withClassification);
      }
    }

    SilverTrace.info("kmelia", "KmeliaSessionController.createPublication(pubDetail)",
            "Kmelia.MSG_ENTRY_METHOD");
    SilverTrace.spy("kmelia", "KmeliaSessionController.createPublication(pubDetail)", getSpaceId(),
            getComponentId(), result, getUserDetail().getId(), SilverTrace.SPY_ACTION_CREATE);
    return result;
  }

  private String createPublicationIntoTopic(PublicationDetail pubDetail, String fatherId)
          throws RemoteException {
    pubDetail.getPK().setSpace(getSpaceId());
    pubDetail.getPK().setComponentName(getComponentId());
    pubDetail.setCreatorId(getUserId());
    pubDetail.setCreationDate(new Date());

    if (KmeliaHelper.ROLE_WRITER.equals(getUserTopicProfile(fatherId))) {
      // in case of writers, status of new publication must be processed
      pubDetail.setStatus(null);
    }

    String result = getKmeliaBm().createPublicationIntoTopic(pubDetail, getNodePK(fatherId));
    SilverTrace.spy("kmelia",
            "KmeliaSessionController.createPublicationIntoTopic(pubDetail, fatherId)",
        getSpaceId(),
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
            "root.MSG_GEN_PARAM_VALUE",
            "'writer'.equals(KmeliaHelper.getProfile(getUserRoles())) = "
                + "writer".equals(KmeliaHelper.getProfile(getUserRoles())));
    SilverTrace.info("kmelia", "KmeliaSessionController.updatePublication(pubDetail)",
            "root.MSG_GEN_PARAM_VALUE",
            "(getSessionClone() == null) = " + (getSessionClone() == null));
    if (isCloneNeeded()) {
      clonePublication(pubDetail);
    } else {
      if (getSessionTopic() == null || NodePK.BIN_NODE_ID.equals(
              getSessionTopic().getNodePK().getId())) {
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
            getSessionPublication().getDetail().getStatus();
    return (isPublicationAlwaysVisibleEnabled()
            && "writer".equals(
                getUserTopicProfile()) && (getSessionClone() == null) && PublicationDetail.VALID
        .equals(
            currentStatus));
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
    CompletePublication refPubComplete = getSessionPublication().getCompleteDetail();
    try {
      cloneId = getKmeliaBm().clonePublication(refPubComplete, pubDetail, nextStatus);
      setSessionClone(getPublication(cloneId));
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.clonePublication",
              SilverpeasException.ERROR, "kmelia.CANT_CLONE_PUBLICATION", e);
    }
    return cloneId;
  }

  public synchronized void deletePublication(String pubId) throws RemoteException {
    deletePublication(pubId, false);
  }

  public synchronized void deletePublication(String pubId, boolean kmaxMode)
          throws RemoteException {
    // récupération de la position de la publication pour savoir si elle se trouve déjà dans
    // la corbeille node=1
    // si elle se trouve déjà au node 1, il est nécessaire de supprimer les fichier joints
    // sinon non
    String nodeId = "";
    if (getSessionTopic() != null) {
      nodeId = getSessionTopic().getNodeDetail().getNodePK().getId();
    }
    if (NodePK.BIN_NODE_ID.equals(nodeId)) {
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
      String cloneId = getSessionClone().getDetail().getPK().getId();
      PublicationPK clonePK = getPublicationPK(cloneId);

      removeXMLContentOfPublication(clonePK);
      getKmeliaBm().deletePublication(clonePK);

      setSessionClone(null);
      refreshSessionPubliAndClone();

      // supprime les références au clone
      PublicationDetail pubDetail = getSessionPublication().getDetail();
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
        PublicationTemplate pubTemplate = getPublicationTemplateManager().getPublicationTemplate(
                pubDetail.getPK().getInstanceId() + ":" + xmlFormShortName);

        RecordSet set = pubTemplate.getRecordSet();
        DataRecord data = set.getRecord(pubDetail.getPK().getId());
        set.delete(data);
      }
    } catch (PublicationTemplateException e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.removeXMLContentOfPublication()",
              SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LE_CONTENU_XML",
              e);
    } catch (FormException e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.removeXMLContentOfPublication()",
              SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LE_CONTENU_XML",
              e);
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

  public synchronized void createInfoModelDetail(String pubId, String modelId, InfoDetail infos)
          throws RemoteException {
    String currentPubId = pubId;
    currentPubId = getSessionPubliOrClone().getDetail().getPK().getId();
    if (isCloneNeeded()) {
      currentPubId = clonePublication();
    }
    if (getSessionClone() != null) {
      ModelPK modelPK = new ModelPK(modelId, getPublicationPK(currentPubId));
      getKmeliaBm().getPublicationBm().createInfoModelDetail(getPublicationPK(currentPubId),
              modelPK,
              infos);
    } else {
      getKmeliaBm().createInfoModelDetail(getPublicationPK(currentPubId), modelId, infos);
    }
    refreshSessionPubliAndClone();
  }

  public void refreshSessionPubliAndClone() throws RemoteException {
    if (getSessionClone() != null) {
      // refresh du clone
      KmeliaPublication pub = getPublication(getSessionClone().getDetail().getPK().getId());
      setSessionClone(pub);
    } else {
      // refresh de la publi de référence
      KmeliaPublication pub = getPublication(getSessionPublication().
              getDetail().getPK().getId());
      setSessionPublication(pub);
    }
  }

  public synchronized InfoDetail getInfoDetail(String pubId) throws RemoteException {
    return getKmeliaBm().getInfoDetail(getPublicationPK(pubId));
  }

  public synchronized void updateInfoDetail(String pubId, InfoDetail infos) throws RemoteException {
    String currentPubId = pubId;
    currentPubId = getSessionPubliOrClone().getDetail().getPK().getId();
    if (isCloneNeeded()) {
      currentPubId = clonePublication();
    }
    if (getSessionClone() != null) {
      getKmeliaBm().getPublicationBm().updateInfoDetail(getPublicationPK(currentPubId), infos);
    } else {
      getKmeliaBm().updateInfoDetail(getPublicationPK(currentPubId), infos);
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
    KmeliaPublication completPub = getKmeliaBm().getPublication(getPublicationPK(pubId));
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
    KmeliaPublication completPub = getKmeliaBm().getPublication(getPublicationPK(pubId));
    setSessionPublication(completPub);
  }

  public List<KmeliaPublication> getLinkedVisiblePublications() throws RemoteException {
    List<ForeignPK> seeAlsoList = getSessionPublication().getCompleteDetail().getLinkList();
    List<ForeignPK> authorizedSeeAlsoList = new ArrayList<ForeignPK>();
    List<KmeliaPublication> authorizedAndValidSeeAlsoList = new ArrayList<KmeliaPublication>();
    String curComponentId = null;
    for (ForeignPK curFPK : seeAlsoList) {
      curComponentId = curFPK.getComponentName();
      if (curComponentId != null &&
          getOrganizationController().isComponentAvailable(curComponentId,
              getUserId())) {
        authorizedSeeAlsoList.add(curFPK);
      }
    }

    Collection<KmeliaPublication> linkedPublications = getPublications(authorizedSeeAlsoList);
    for (KmeliaPublication pub : linkedPublications) {
      if (pub.getDetail().isValid()) {
        authorizedAndValidSeeAlsoList.add(pub);
      }
    }
    return authorizedAndValidSeeAlsoList;
  }

  public synchronized KmeliaPublication getPublication(String pubId) throws RemoteException {
    return getPublication(pubId, false);
  }

  public synchronized KmeliaPublication getPublication(String pubId,
          boolean processIndex) throws RemoteException {
    PublicationPK pubPK = getPublicationPK(pubId);
    // get publication
    KmeliaPublication publication = getKmeliaBm().getPublication(pubPK);
    PublicationDetail publicationDetail = publication.getDetail();

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

    if (processIndex) {
      // mise à jour du rang de la publication
      // List publis = (List) getSessionTopic().getPublicationDetails();
      KmeliaPublication pub = KmeliaPublication.aKmeliaPublicationFromDetail(publicationDetail);
      if (getSessionPublicationsList() != null) {
        rang = getSessionPublicationsList().indexOf(pub);
      }
    }
    return publication;
  }

  public synchronized CompletePublication getCompletePublication(String pubId)
          throws RemoteException {
    return getKmeliaBm().getCompletePublication(getPublicationPK(pubId));
  }

  public synchronized void orderPubs() {
    if (!StringUtil.isDefined(getSortValue())) {
      //sortValue = "2";
      orderPubs(-1);
    } else {
      orderPubs(Integer.parseInt(getSortValue()));
    }
  }

  private void applyVisibilityFilter() throws RemoteException {
    List<KmeliaPublication> publications = getSessionPublicationsList();
    List<KmeliaPublication> filteredPublications = new ArrayList<KmeliaPublication>();

    Calendar calendar = Calendar.getInstance();

    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date today = calendar.getTime();

    for (KmeliaPublication userPub : publications) {
      PublicationDetail detail = userPub.getDetail();
      if (detail.getStatus() != null) {
        if (detail.isValid()) {
          Date dBegin = DateUtil.getDate(detail.getBeginDate(), detail.getBeginHour());
          Date dEnd = DateUtil.getDate(detail.getEndDate(), detail.getEndHour());

          detail.setBeginDateAndHour(dBegin);
          detail.setEndDateAndHour(dEnd);

          if (dBegin != null && dBegin.after(today)) {
            detail.setNotYetVisible(true);
          } else if (dEnd != null && dEnd.before(today)) {
            detail.setNoMoreVisible(true);
          }
          if (detail.isVisible()) {
            filteredPublications.add(userPub);
          } else {
            if (getProfile().equals("admin") || getUserId().equals(detail.getUpdaterId())
                    || (!getProfile().equals("user") && isCoWritingEnable())) {
              filteredPublications.add(userPub);
            }
          }
        } else {
          if (detail.isDraft()) {
            // si le theme est en co-rédaction et si on autorise le mode brouillon visible par tous
            // toutes les publications en mode brouillon sont visibles par tous, sauf les lecteurs
            // sinon, seule les publications brouillon de l'utilisateur sont visibles
            if (getUserId().equals(detail.getUpdaterId())
                    ||
                (isCoWritingEnable() && isDraftVisibleWithCoWriting() && !getProfile().equals(
                    "user"))) {
              filteredPublications.add(userPub);
            }
          } else {
            // si le thème est en co-rédaction, toutes les publications sont visibles par tous,
            // sauf les lecteurs
            if (getProfile().equals("admin") || getProfile().equals("publisher")
                    || getUserId().equals(detail.getUpdaterId())
                    || (!getProfile().equals("user") && isCoWritingEnable())) {
              filteredPublications.add(userPub);
            }
          }
        }
      }
    }

    setSessionPublicationsList(filteredPublications);
  }

  private synchronized void orderPubs(int sortType) {
    sessionPublicationsList = sort(getSessionPublicationsList(), sortType);
  }

  public synchronized void orderPubsToValidate(String sortType) throws RemoteException {
    int sort = Integer.parseInt(defaultSortValue);
    if (StringUtil.isDefined(sortType)) {
      sort = Integer.parseInt(sortType);
    }
    List<KmeliaPublication> publications =
            sort(getKmeliaBm().getPublicationsToValidate(getComponentId()), sort);
    sessionPublicationsList = publications;
  }

  private List<KmeliaPublication> sort(Collection<KmeliaPublication> publications, int sortType) {
    if (publications == null) {
      return null;
    }
    List<KmeliaPublication> publicationsToSort = new ArrayList<KmeliaPublication>(publications);
    
    int sort = sortType;
    if (isManualSortingUsed(publicationsToSort) && sort == -1) {
      // display publications according to manual order defined by admin
      sort = 99;
    } else if (sort == -1) {
      // display publications according to default sort defined on application level or instance
      // level
      sort = Integer.parseInt(defaultSortValue);
    }
    
    switch (sort) {
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
        // display publications according to manual order defined by admin
        Collections.sort(publicationsToSort, new PubliRankComparatorAsc());
    }

    return publicationsToSort;
  }
  
  private boolean isManualSortingUsed(List<KmeliaPublication> publications) {
    for (KmeliaPublication publication : publications) {
      if (publication.getDetail().getExplicitRank() > 0) {
        return true;
      }
    }
    return false;
  }

  private List<KmeliaPublication> sortByTitle(List<KmeliaPublication> publications) {
    KmeliaPublication[] pubs = publications.toArray(new KmeliaPublication[publications.size()]);
    for (int i = pubs.length; --i >= 0;) {
      boolean swapped = false;
      for (int j = 0; j < i; j++) {
        if (pubs[j].getDetail().getName(getCurrentLanguage()).compareToIgnoreCase(
                pubs[j + 1].getDetail().getName(getCurrentLanguage())) > 0) {
          KmeliaPublication T = pubs[j];
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

  private List<KmeliaPublication> sortByDescription(List<KmeliaPublication> publications) {
    KmeliaPublication[] pubs = publications.toArray(new KmeliaPublication[publications.size()]);
    for (int i = pubs.length; --i >= 0;) {
      boolean swapped = false;
      for (int j = 0; j < i; j++) {
        String p1 = pubs[j].getDetail().getDescription(getCurrentLanguage());
        if (p1 == null) {
          p1 = "";
        }
        String p2 = pubs[j + 1].getDetail().getDescription(getCurrentLanguage());
        if (p2 == null) {
          p2 = "";
        }
        if (p1.compareToIgnoreCase(p2) > 0) {
          KmeliaPublication T = pubs[j];
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
            getSettings().getString("publication_defaultsorting", "pubId desc");
    if (StringUtil.isDefined(sortedBy)) {
      publication_default_sorting = sortedBy;
    }
    return getKmeliaBm().getPublicationBm().getAllPublications(
            new PublicationPK("useless", getComponentId()), publication_default_sorting);
  }

  public Collection<PublicationDetail> getAllPublicationsByTopic(PublicationPK pubPK,
          List<String> fatherIds)
          throws RemoteException {
    Collection<PublicationDetail> result = getKmeliaBm().getPublicationBm().
            getDetailsByFatherIdsAndStatus((ArrayList<String>) fatherIds, pubPK,
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
    for (PublicationDetail pubDetail : allPublications) {
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
    for (NodeDetail node : nodes) {
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
    for (PublicationDetail pubDetail : allPublications) {
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
    for (PublicationDetail pubDetail : allPublications) {
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
    this.indexOfFirstPubToDisplay = Integer.parseInt(index);
  }

  public List<Comment> getAllComments(String id) throws RemoteException {
    return getCommentService().getAllCommentsOnPublication(PublicationDetail.getResourceType(),
        getPublicationPK(id));
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
                getSessionPublication().getDetail().getPK().getId();
        return isPublicationClassifiedOnPDC(pubId);
      }
    }
  }

  /**
   * @param links
   * @return
   * @throws RemoteException
   */
  public synchronized Collection<KmeliaPublication> getPublications(List<ForeignPK> links)
          throws RemoteException {
    return getKmeliaBm().getPublications(links, getUserId(), true);
  }

  public synchronized List<KmeliaPublication> getPublicationsToValidate() throws RemoteException {
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
                getSessionPubliOrClone().getDetail().getPK());

    // Get users who have already validate this publication
    List<String> validators = new ArrayList<String>();
    for (ValidationStep step : steps) {
      step.setUserFullName(getOrganizationController().getUserDetail(step.getUserId()).
              getDisplayedName());
      validators.add(step.getUserId());
    }

    List<String> allValidators =
            getKmeliaBm().getAllValidators(
                getSessionPubliOrClone().getDetail().getPK(),
                getValidationType());

    for (String allValidator : allValidators) {
      if (!validators.contains(allValidator)) {
        ValidationStep step = new ValidationStep();
        step.setUserFullName(getOrganizationController().getUserDetail(
                allValidator).getDisplayedName());
        steps.add(step);
      }
    }

    return steps;
  }

  public ValidationStep getValidationStep() throws RemoteException {
    if (getValidationType() == 2) {
      return getPublicationBm().getValidationStepByUser(
              getSessionPubliOrClone().getDetail().getPK(), getUserId());
    }

    return null;
  }

  public synchronized void draftOutPublication() throws RemoteException {
    SilverTrace.info("kmelia", "KmeliaSessionController.draftOutPublication()",
            "root.MSG_GEN_ENTER_METHOD", "getSessionPublication().getPublication() = "
                + getSessionPublication().getCompleteDetail());
    if (isKmaxMode) {
      getKmeliaBm().draftOutPublication(
              getSessionPublication().getDetail().getPK(), null,
              getProfile());
    } else {
      getKmeliaBm().draftOutPublication(
              getSessionPublication().getDetail().getPK(),
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
              getSessionPublication().getDetail().getPK(), getUserId());
    }
    refreshSessionPubliAndClone();
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

  private synchronized NotificationMetaData getAlertNotificationMetaData(String pubId,
          String attachmentOrDocumentId, boolean isVersionning)
          throws RemoteException {
    NotificationMetaData metaData = null;
    if (isVersionning) {
      DocumentPK documentPk =
          new DocumentPK(Integer.parseInt(attachmentOrDocumentId), getSpaceId(),
              getComponentId());
      if (isKmaxMode) {
        metaData =
                getKmeliaBm().getAlertNotificationMetaData(getPublicationPK(pubId), documentPk,
                    null,
                    getUserDetail().getDisplayedName());
      } else {
        metaData =
                getKmeliaBm().getAlertNotificationMetaData(getPublicationPK(pubId), documentPk,
                    getSessionTopic().getNodePK(), getUserDetail().getDisplayedName());
      }
    } else {
      AttachmentPK attachmentPk = new AttachmentPK(attachmentOrDocumentId, getSpaceId(),
              getComponentId());
      if (isKmaxMode) {
        metaData =
                getKmeliaBm().getAlertNotificationMetaData(getPublicationPK(pubId), attachmentPk,
                    null,
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
  /**
   * **********************************************************************************
   */
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
  /**
   * **************************************************************************************
   */
  public void pasteDocuments(PublicationPK pubPKFrom, String pubId) throws RemoteException {
    SilverTrace.info("kmelia", "KmeliaSessionController.pasteDocuments()",
            "root.MSG_GEN_ENTER_METHOD",
            "pubPKFrom = " + pubPKFrom.toString() + ", pubId = " + pubId);

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
    for (Document document : documents) {
      SilverTrace.info("kmelia", "KmeliaSessionController.pasteDocuments()",
              "root.MSG_GEN_PARAM_VALUE", "document name = " + document.getName());

      // retrieve all versions of the document (from last version to first version)
      List<DocumentVersion> versions = getVersioningBm().getDocumentVersions(document.getPk());

      // sort versions (from first version to last version)
      Collections.reverse(versions);

      // retrieve the initial version of the document
      DocumentVersion version = versions.get(0);

      if (pathFrom == null) {
        pathFrom = versioningUtil.createPath(document.getPk().getSpaceId(),
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
      worker = new Worker(Integer.parseInt(userId), -1, u, false, true, getComponentId(), "U",
              false, true, 0);
      workers.add(worker);
    }

    return workers;
  }

  public void pasteDocumentsAsAttachments(PublicationPK pubPKFrom, String pubId)
          throws RemoteException {
    SilverTrace.info("kmelia", "KmeliaSessionController.pasteDocumentsAsAttachments()",
            "root.MSG_GEN_ENTER_METHOD",
            "pubPKFrom = " + pubPKFrom.toString() + ", pubId = " + pubId);

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
    for (Document document : documents) {

      SilverTrace.info("kmelia", "KmeliaSessionController.pasteDocumentsAsAttachments()",
              "root.MSG_GEN_PARAM_VALUE", "document name = " + document.getName());

      // retrieve last public versions of the document
      DocumentVersion version = getVersioningBm().getLastPublicDocumentVersion(document.getPk());

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
                  new AttachmentDetail(new AttachmentPK("unknown", getComponentId()),
                      newVersionFile,
                      version.getLogicalName(), "", version.getMimeType(), version.getSize(),
                      "Images",
                      new Date(), getPublicationPK(pubId), document.getName(),
                      document.getDescription(),
                      0);
          AttachmentController.createAttachment(attachment, false);
        }
      }
    }
  }

  public void pasteAttachmentsAsDocuments(PublicationPK pubPKFrom, String pubId)
          throws RemoteException {
    SilverTrace.info("kmelia", "KmeliaSessionController.pasteAttachmentsAsDocuments()",
            "root.MSG_GEN_ENTER_METHOD",
            "pubPKFrom = " + pubPKFrom.toString() + ", pubId = " + pubId);

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
    for (AttachmentDetail attachment : attachments) {

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
        Document document =
                new Document(new DocumentPK(-1, "useless", getComponentId()),
                    getPublicationPK(pubId),
                    attachment.getLogicalName(), attachment.getInfo(), 0,
                    Integer.parseInt(getUserId()), new Date(), "", getComponentId(),
                    (ArrayList<Worker>) workers, new ArrayList<Reader>(), 0, 0);

        // Version creation
        DocumentVersion version =
                new DocumentVersion(null, null, 1, 0, Integer.parseInt(getUserId()), new Date(),
                    "",
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
      String fileNameTo = Long.toString(System.currentTimeMillis()) + "." + type;

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

  /**
   * @param topicDetail
   */
  public void setSessionTopic(TopicDetail topicDetail) {
    this.sessionTopic = topicDetail;
    if (topicDetail != null) {
      Collection<KmeliaPublication> publications = topicDetail.getKmeliaPublications();
      setSessionPublicationsList((List<KmeliaPublication>) publications);
    }
  }

  public void setSessionTopicToLink(TopicDetail topicDetail) {
    this.sessionTopicToLink = topicDetail;
  }

  public void setSessionPublication(KmeliaPublication pubDetail) {
    this.sessionPublication = pubDetail;
    setSessionClone(null);
  }

  public void setSessionClone(KmeliaPublication clone) {
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

  public void setSessionPublicationsList(List<KmeliaPublication> publications) {
    this.sessionPublicationsList = (publications == null ? null
            : new ArrayList<KmeliaPublication>(publications));
    orderPubs();
  }

  public void setSessionCombination(List<String> combination) {
    this.sessionCombination = (combination == null ? null
            : new ArrayList<String>(combination));
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

  public KmeliaPublication getSessionPublication() {
    return this.sessionPublication;
  }

  public KmeliaPublication getSessionClone() {
    return this.sessionClone;
  }

  public KmeliaPublication getSessionPubliOrClone() {
    KmeliaPublication publication = getSessionClone();
    if (publication == null) {
      publication = getSessionPublication();
    }
    return publication;
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

  public List<KmeliaPublication> getSessionPublicationsList() {
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
            m_context + URLManager.getURL("useless", getComponentId()) + "SetValidator?PubId="
                + pubId;
    String cancelUrl =
            m_context + URLManager.getURL("useless", getComponentId()) + "SetValidator?PubId="
                + pubId;

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
              getAdmin().getProfilesByObject(Integer.toString(rightsDependsOn),
                  ObjectType.NODE.getCode(),
                  getComponentId());
      if (profileInsts != null) {
        for (ProfileInst profileInst : profileInsts) {
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
    String pubId = getSessionPublication().getDetail().getPK().getId();

    AlertUser sel = getAlertUser();
    // Initialisation de AlertUser
    sel.resetAll();
    sel.setHostSpaceName(getSpaceLabel()); // set nom de l'espace pour browsebar
    sel.setHostComponentId(getComponentId()); // set id du composant pour appel selectionPeas (extra
    // param permettant de filtrer les users ayant acces
    // au composant)
    PairObject hostComponentName = new PairObject(getComponentLabel(), null); // set nom du
    // composant pour browsebar (PairObject(nom_composant, lien_vers_composant))
    // NB : seul le 1er element est actuellement utilisé (alertUserPeas est toujours présenté
    // en popup => pas de lien sur nom du composant)
    sel.setHostComponentName(hostComponentName);
    sel.setNotificationMetaData(getAlertNotificationMetaData(pubId)); // set NotificationMetaData
    // contenant les informations à notifier fin initialisation de AlertUser
    // l'url de nav vers alertUserPeas et demandée à AlertUser et retournée
    return AlertUser.getAlertUserURL();
  }

  public String initAlertUserAttachment(String attachmentOrDocumentId, boolean isVersionning)
          throws RemoteException {

    initAlertUser();

    AlertUser sel = getAlertUser();
    String pubId = getSessionPublication().getDetail().getPK().getId();
    sel.setNotificationMetaData(getAlertNotificationMetaData(pubId, attachmentOrDocumentId,
            isVersionning));
    return AlertUser.getAlertUserURL();
  }

  public void toRecoverUserId() {
    Selection sel = getSelection();
    idSelectedUser =
            SelectionUsersGroups.getDistinctUserIds(sel.getSelectedElements(), sel
                .getSelectedSets());
  }

  public boolean isVersionControlled() {
    String strVersionControlled = this.getComponentParameterValue("versionControl");
    return ((strVersionControlled != null) && !("").equals(strVersionControlled) && !("no")
        .equals(strVersionControlled.toLowerCase()));
  }

  public boolean isVersionControlled(String anotherComponentId) {
    String strVersionControlled =
            getOrganizationController().getComponentParameterValue(anotherComponentId,
                "versionControl");
    return ((strVersionControlled != null) && !("").equals(strVersionControlled) && !("no")
        .equals(strVersionControlled.toLowerCase()));
  }

  /**
   * @param pubId
   * @return
   * @throws RemoteException
   */
  public boolean isWriterApproval(String pubId) throws RemoteException {
    List<Document> documents =
            getVersioningBm().getDocuments((new ForeignPK(pubId, getComponentId())));
    for (Document document : documents) {
      List<Worker> writers = document.getWorkList();
      for (Worker user : writers) {
        if (user.getUserId() == Integer.parseInt(getUserId())) {
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
            PublicationDetail.TO_VALIDATE.equalsIgnoreCase(getSessionPubliOrClone().getDetail().
                getStatus());

    return tabVisible
            && (getValidationType() == KmeliaHelper.VALIDATION_COLLEGIATE || getValidationType()
            == KmeliaHelper.VALIDATION_TARGET_N);
  }

  public int getValidationType() {
    if (isTargetValidationEnable()) {
      return KmeliaHelper.VALIDATION_TARGET_1;
    }
    if (isTargetMultiValidationEnable()) {
      return KmeliaHelper.VALIDATION_TARGET_N;
    }
    if (isCollegiateValidationEnable()) {
      return KmeliaHelper.VALIDATION_COLLEGIATE;
    }
    return KmeliaHelper.VALIDATION_CLASSIC;
  }

  public boolean isCoWritingEnable() {
    return StringUtil.getBooleanValue(getComponentParameterValue("coWriting"));
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
        List<ClassifyPosition> positions = getPdcBm().getPositions(silverObjectId,
                getComponentId());
        return (positions.size() > 0);
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaSessionController.isPublicationClassifiedOnPDC()",
                SilverpeasRuntimeException.ERROR, "kmelia.MSG_ERR_GENERAL", e);
      }
    }
    return false;
  }

  public boolean isCurrentPublicationHaveContent() throws WysiwygException {
    return (getSessionPublication().getCompleteDetail().getModelDetail() != null
            ||
        StringUtil.isDefined(WysiwygController.load(getComponentId(), getSessionPublication().
            getId(), getCurrentLanguage())) || !isInteger(getSessionPublication()
        .getCompleteDetail().
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
      throw new KmeliaRuntimeException("KmeliaSessionController.isPDCClassifyingMandatory()",
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
              EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
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
              EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
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
   * @param fileUploaded : File uploaded in temp directory
   * @param fileType
   * @param topicId
   * @param importMode
   * @param draftMode
   * @param versionType
   * @return list of publications imported
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
    if (isDraftEnabled() && isPDCClassifyingMandatory()) {
      // classifying on PDC is mandatory, set publication in draft mode
      fileImport.setDraftMode(true);
    } else {
      fileImport.setDraftMode(draftMode);
    }
    fileImport.setVersionType(versionType);
    fileImport.setKmeliaScc(this);
    if (UNITARY_IMPORT_MODE.equals(importMode)) {
      publicationDetails = fileImport.importFile();
    } else if (MASSIVE_IMPORT_MODE_ONE_PUBLICATION.equals(importMode)
            && FileUtil.isArchive(fileUploaded.getName())) {
      publicationDetails = fileImport.importFiles();
    } else if (MASSIVE_IMPORT_MODE_MULTI_PUBLICATIONS.equals(importMode)
            && FileUtil.isArchive(fileUploaded.getName())) {
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
        for (Collection<NodeDetail> path : pathList) {
          for (NodeDetail nodeInPath : path) {
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

  /**
   * Parameter for time Axis visibility
   * @return
   */
  public boolean isTimeAxisUsed() {
    return StringUtil.getBooleanValue(getComponentParameterValue("timeAxisUsed"));
  }

  /**
   * Parameter for fields visibility of the publication
   * @return
   */
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
    return StringUtil.getBooleanValue(paramValue) || "".equals(paramValue);
  }

  public boolean isFieldImportanceVisible() {
    if (isKmaxMode) {
      return StringUtil.getBooleanValue(getComponentParameterValue("useImportance"));
    }
    return getSettings().getBoolean("showImportance", true);
  }

  public boolean isFieldVersionVisible() {
    if (isKmaxMode) {
      return StringUtil.getBooleanValue(getComponentParameterValue("useVersion"));
    }
    return getSettings().getBoolean("showPubVersion", true);
  }

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

  public synchronized List<KmeliaPublication> search(List<String> combination)
          throws RemoteException {
    this.sessionPublicationsList =
            new ArrayList<KmeliaPublication>(getKmeliaBm().search(combination, getComponentId()));
    applyVisibilityFilter();
    return getSessionPublicationsList();
  }

  public synchronized List<KmeliaPublication> search(List<String> combination, int nbDays)
          throws RemoteException {
    this.sessionPublicationsList =
            new ArrayList<KmeliaPublication>(getKmeliaBm().search(combination, nbDays,
                getComponentId()));
    applyVisibilityFilter();
    return getSessionPublicationsList();
  }

  public synchronized List<KmeliaPublication> getUnbalancedPublications() throws RemoteException {
    return (List<KmeliaPublication>) getKmeliaBm().getUnbalancedPublications(getComponentId());
  }

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

  public synchronized void indexKmax(String componentId) throws RemoteException {
    getKmeliaBm().indexKmax(componentId);
  }

  /*
   * /* Kmax - Publications
   */
  /**
   * **********************************************************************************
   */
  public synchronized KmeliaPublication getKmaxCompletePublication(String pubId)
          throws RemoteException {
    return getKmeliaBm().getKmaxPublication(pubId, getUserId());
  }

  public synchronized Collection<Coordinate> getPublicationCoordinates(String pubId)
          throws RemoteException {
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
    Collection<KmeliaPublication> allPublications = getSessionPublicationsList();
    SilverTrace.info("kmelia", "KmeliaSessionController.getCurrentPublicationsList()",
            "root.MSG_PARAM_VALUE", "NbPubli=" + allPublications.size());
    for (KmeliaPublication aPubli : allPublications) {
      PublicationDetail pubDetail = aPubli.getDetail();
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
  /**
   * **********************************************************************************
   */
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

    KmeliaPublication pub = getSessionPublicationsList().get(rangNext);
    pubId = pub.getDetail().getId();

    // on est sur la précédente, mettre à jour le rang avec la publication courante
    rang = rangNext;

    return pubId;
  }

  public String getFirst() {
    rang = 0;
    KmeliaPublication pub = getSessionPublicationsList().get(0);
    String pubId = pub.getDetail().getId();

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

  private synchronized boolean isDragAndDropEnableByUser() {
    return getPersonalization().isDragAndDropEnabled();
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
            getAdmin().getProfilesByObject(nodeId, ObjectType.NODE.getCode(), getComponentId());
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
      List<String> users = topicProfile.getAllUsers();
      sel.setSelectedElements(users.toArray(new String[users.size()]));
      List<String> groups = topicProfile.getAllGroups();
      sel.setSelectedSets(groups.toArray(new String[groups.size()]));
    }

    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  private void deleteTopicRoles(NodeDetail node) throws RemoteException {
    if (node != null && node.haveLocalRights()) {
      List<ProfileInst> profiles = getTopicProfiles(node.getNodePK().getId());
      if (profiles != null) {
        for (ProfileInst profile : profiles) {
          if (profile != null && StringUtil.isDefined(profile.getId())) {
            deleteTopicRole(profile.getId());
          }
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
      profile.setObjectType(ObjectType.NODE.getCode());
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
              getAdmin().getProfilesByObject(node.getNodePK().getId(), ObjectType.NODE.getCode(),
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
            getAdmin().getProfilesByObject(topicId, ObjectType.NODE.getCode(), getComponentId());
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
    String[] asAvailProfileNames = getAdmin().getAllProfilesNames("kmelia");
    for (String asAvailProfileName : asAvailProfileNames) {
      SilverTrace.info("jobStartPagePeas",
              "JobStartPagePeasSessionController.getAllProfilesNames()",
              "root.MSG_GEN_PARAM_VALUE", "asAvailProfileNames = " + asAvailProfileName);
      ProfileInst profile = getTopicProfile(asAvailProfileName, topicId);
      profile.setLabel(getAdmin().getProfileLabelfromName("kmelia", asAvailProfileName,
              getLanguage()));
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
    for (int nI = 0; userIds != null && nI < userIds.size(); nI++) {
      UserDetail user = getUserDetail(userIds.get(nI));
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
    for (ProfileInst profile : profiles) {
      if (role.equals(profile.getName())) {
        return profile;
      }
    }
    return null;
  }

  public boolean isUserCanValidate() throws RemoteException {
    if (KmeliaHelper.isToolbox(getComponentId())) {
      return false;
    }

    String profile = getUserTopicProfile();
    boolean isPublisherOrAdmin =
            SilverpeasRole.admin.isInRole(profile) || SilverpeasRole.publisher.isInRole(profile);

    if (!isPublisherOrAdmin && isRightsOnTopicsEnabled()) {
      // check if current user is publisher or admin on at least one descendant
      Iterator<NodeDetail> descendants = getNodeBm().getDescendantDetails(getRootPK()).iterator();
      while (!isPublisherOrAdmin && descendants.hasNext()) {
        NodeDetail descendant = descendants.next();
        if (descendant.haveLocalRights()) {
          // check if user is admin or publisher on this topic
          String[] profiles =
                  getAdmin().getProfilesByObjectAndUserId(descendant.getId(),
                      ObjectType.NODE.getCode(), getComponentId(), getUserId());
          if (profiles != null && profiles.length > 0) {
            List<String> lProfiles = Arrays.asList(profiles);
            isPublisherOrAdmin =
                    lProfiles.contains(SilverpeasRole.admin.name())
                        || lProfiles.contains(SilverpeasRole.publisher.name());
          }
        }
      }
    }
    return isPublisherOrAdmin;
  }

  public boolean isUserCanWrite() throws RemoteException {
    String profile = getUserTopicProfile();
    boolean userCanWrite =
            SilverpeasRole.admin.isInRole(profile) || SilverpeasRole.publisher.isInRole(profile)
                || SilverpeasRole.writer.isInRole(profile);

    if (!userCanWrite && isRightsOnTopicsEnabled()) {
      // check if current user is publisher or admin on at least one descendant
      Iterator<NodeDetail> descendants = getNodeBm().getDescendantDetails(getRootPK()).iterator();
      while (!userCanWrite && descendants.hasNext()) {
        NodeDetail descendant = descendants.next();
        if (descendant.haveLocalRights()) {
          // check if user is admin, publisher or writer on this topic
          String[] profiles =
                  getAdmin().getProfilesByObjectAndUserId(descendant.getId(),
                      ObjectType.NODE.getCode(), getComponentId(), getUserId());
          if (profiles != null && profiles.length > 0) {
            List<String> lProfiles = Arrays.asList(profiles);
            userCanWrite =
                    lProfiles.contains(SilverpeasRole.admin.name())
                        || lProfiles.contains(SilverpeasRole.publisher.name())
                        || lProfiles.contains(SilverpeasRole.writer.name());
          }
        }
      }
    }
    return userCanWrite;
  }

  public void copyPublication(String pubId) throws RemoteException {
    CompletePublication pub = getCompletePublication(pubId);
    PublicationSelection pubSelect = new PublicationSelection(pub);
    SilverTrace.info("kmelia", "KmeliaSessionController.copyPublication()",
            "root.MSG_GEN_PARAM_VALUE",
            "clipboard = " + getClipboardName() + "' count=" + getClipboardCount());
    addClipboardSelection(pubSelect);
  }

  public void copyPublications(String[] pubIds) throws RemoteException {
    for (String pubId : pubIds) {
      if (StringUtil.isDefined(pubId)) {
        copyPublication(pubId);
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
    addClipboardSelection(pubSelect);
  }

  public void cutPublications(String[] pubIds) throws RemoteException {
    for (String pubId : pubIds) {
      if (StringUtil.isDefined(pubId)) {
        cutPublication(pubId);
      }
    }
  }

  public void copyTopic(String id) throws RemoteException {
    NodeSelection nodeSelect = new NodeSelection(getNodeHeader(id));

    SilverTrace.info("kmelia", "KmeliaSessionController.copyTopic()", "root.MSG_GEN_PARAM_VALUE",
            "clipboard = " + getClipboardName() + "' count=" + getClipboardCount());
    addClipboardSelection(nodeSelect);
  }

  public void cutTopic(String id) throws RemoteException {
    NodeSelection nodeSelect = new NodeSelection(getNodeHeader(id));
    nodeSelect.setCutted(true);
    SilverTrace.info("kmelia", "KmeliaSessionController.cutTopic()", "root.MSG_GEN_PARAM_VALUE",
            "clipboard = " + getClipboardName() + "' count=" + getClipboardCount());
    addClipboardSelection(nodeSelect);
  }

  public List<Object> paste() throws RemoteException {
    List<Object> pastedItems = new ArrayList<Object>();
    try {
      SilverTrace.info("kmelia", "KmeliaRequestRooter.paste()", "root.MSG_GEN_PARAM_VALUE",
              "clipboard = " + getClipboardName() + " count=" + getClipboardCount());
      Collection<ClipboardSelection> clipObjects = getClipboardSelectedObjects();
      for (ClipboardSelection clipObject : clipObjects) {
        if (clipObject != null) {
          if (clipObject.isDataFlavorSupported(PublicationSelection.CompletePublicationFlavor)) {
            CompletePublication pub = (CompletePublication) clipObject.getTransferData(
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
      for (NodeDetail fromNode : treeToPaste) {
        if (fromNode != null) {
          NodePK toNodePK = getNodePK(fromNode.getNodePK().getId());

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
      for (NodeDetail oneNodeToPaste : treeToPaste) {
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
      NodePK currentNodePK = nodePK;
      PublicationDetail publi = completePub.getPublicationDetail();
      if (!isCutted) {
        publi.setCloneId(null);
        publi.setCloneStatus("");
      }
      String fromId = publi.getPK().getId();
      String fromComponentId = publi.getPK().getInstanceId();

      ForeignPK fromForeignPK = new ForeignPK(publi.getPK().getId(), fromComponentId);
      PublicationPK fromPubPK = new PublicationPK(publi.getPK().getId(), fromComponentId);

      ForeignPK toForeignPK = new ForeignPK(publi.getPK().getId(), getComponentId());
      PublicationPK toPubPK = new PublicationPK(publi.getPK().getId(), getComponentId());

      String imagesSubDirectory = getPublicationSettings().getString("imagesSubDirectory");
      String thumbnailsSubDirectory = getPublicationSettings().getString("imagesSubDirectory");
      String toAbsolutePath = FileRepositoryManager.getAbsolutePath(getComponentId());
      String fromAbsolutePath = FileRepositoryManager.getAbsolutePath(fromComponentId);

      if (currentNodePK == null) {
        // Ajoute au thème courant
        currentNodePK = getSessionTopic().getNodePK();
      }

      if (isCutted) {
        if (fromComponentId.equals(getComponentId())) {
          getKmeliaBm().movePublicationInSameApplication(publi, currentNodePK, getUserId());
        } else {
          movePublication(completePub, currentNodePK, publi, fromId, fromComponentId,
              fromForeignPK,
                  fromPubPK, toForeignPK, toPubPK, imagesSubDirectory, thumbnailsSubDirectory,
                  toAbsolutePath, fromAbsolutePath);
        }
      } else {
        // paste the publicationDetail
        publi.setUpdaterId(getUserId()); // ignore initial parameters

        String id = createPublicationIntoTopic(publi, currentNodePK.getId());
        List<NodePK> fatherPKs = (List<NodePK>) getPublicationBm().getAllFatherPK(publi.getPK());
        if (nodePKsToPaste != null) {
          fatherPKs.removeAll(nodePKsToPaste);
        }
        // paste vignette
        ThumbnailDetail vignette =
                ThumbnailController.getCompleteThumbnail(new ThumbnailDetail(fromComponentId,
                    Integer.parseInt(fromId),
                    ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE));
        if (vignette != null) {
          pasteThumbnail(publi, thumbnailsSubDirectory, toAbsolutePath, fromAbsolutePath, id,
                  vignette);
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
            for (InfoImageDetail attachment : completePub.getInfoDetail().getInfoImageList()) {
              String from = fromAbsolutePath + imagesSubDirectory + File.separatorChar
                      + attachment.getPhysicalName();
              String type = FilenameUtils.getExtension(attachment.getPhysicalName());
              String newName = String.valueOf(System.currentTimeMillis()) + "." + type;
              attachment.setPhysicalName(newName);
              String to = toAbsolutePath + imagesSubDirectory + File.separatorChar + newName;
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
            Map<String, String> imageIds =
                    AttachmentController.copyAttachmentByCustomerPKAndContext(fromPubPK, fromPubPK,
                        "XMLFormImages");

            if (imageIds != null) {
              fileIds.putAll(imageIds);
            }

            // Paste wysiwyg fields content
            WysiwygFCKFieldDisplayer wysiwygField = new WysiwygFCKFieldDisplayer();
            wysiwygField.cloneContents(fromComponentId, fromId, getComponentId(), id);

            // get xmlContent to paste
            PublicationTemplate pubTemplateFrom = getPublicationTemplateManager().
                    getPublicationTemplate(fromComponentId + ":" + xmlFormShortName);
            IdentifiedRecordTemplate recordTemplateFrom =
                    (IdentifiedRecordTemplate) pubTemplateFrom.getRecordSet().getRecordTemplate();

            PublicationTemplate pubTemplate =
                    getPublicationTemplateManager().getPublicationTemplate(
                        getComponentId() + ":" + xmlFormShortName);
            IdentifiedRecordTemplate recordTemplate = (IdentifiedRecordTemplate) pubTemplate.
                    getRecordSet().getRecordTemplate();

            // paste xml content
            GenericRecordSetManager.getInstance().cloneRecord(recordTemplateFrom, fromId,
                    recordTemplate, id, fileIds);
          }
        }

        // force the update
        PublicationDetail newPubli = getPublicationDetail(id);
        newPubli.setStatusMustBeChecked(false);
        getKmeliaBm().updatePublication(newPubli);
      }
    } catch (Exception ex) {
      SilverTrace.error("kmelia", getClass().getSimpleName() + ".pastePublication()",
              "root.EX_NO_MESSAGE", ex);
    }
  }

  private void pasteThumbnail(PublicationDetail publi, String thumbnailsSubDirectory,
          String toAbsolutePath, String fromAbsolutePath, String id, ThumbnailDetail vignette)
          throws IOException, ThumbnailException {
    ThumbnailDetail thumbDetail = new ThumbnailDetail(publi.getPK().getInstanceId(),
            Integer.valueOf(
                id), ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE);

    if (vignette.getOriginalFileName().startsWith("/")) {
      thumbDetail.setOriginalFileName(vignette.getOriginalFileName());
      thumbDetail.setMimeType(vignette.getMimeType());
    } else {
      String from = fromAbsolutePath + thumbnailsSubDirectory + File.separatorChar
              + vignette.getOriginalFileName();

      String type = FilenameUtils.getExtension(vignette.getOriginalFileName());
      String newOriginalImage = String.valueOf(System.currentTimeMillis()) + "." + type;

      String to = toAbsolutePath + thumbnailsSubDirectory + File.separatorChar + newOriginalImage;
      FileRepositoryManager.copyFile(from, to);
      thumbDetail.setOriginalFileName(newOriginalImage);

      // then copy thumnbnail image if exists
      if (vignette.getCropFileName() != null) {
        from = fromAbsolutePath + thumbnailsSubDirectory + File.separatorChar + vignette.
                getCropFileName();
        type = FilenameUtils.getExtension(vignette.getCropFileName());
        String newThumbnailImage = String.valueOf(System.currentTimeMillis()) + "." + type;
        to = toAbsolutePath + thumbnailsSubDirectory + File.separatorChar + newThumbnailImage;
        FileRepositoryManager.copyFile(from, to);
        thumbDetail.setCropFileName(newThumbnailImage);
      }
      thumbDetail.setMimeType(type);
      thumbDetail.setXLength(vignette.getXLength());
      thumbDetail.setYLength(vignette.getYLength());
      thumbDetail.setXStart(vignette.getXStart());
      thumbDetail.setYStart(vignette.getYStart());
    }
    getThumbnailService().createThumbnail(thumbDetail);
  }

  /**
   * Move a publication to another component. Moving in tis order :
   * <ul>
   * <li>moving the metadata</li>
   * <li>moving the thumbnail</li>
   * <li>moving the content</li>
   * <li>moving the wysiwyg</li>
   * <li>moving the images linked to the wysiwyg</li>
   * <li>moving the xml form content (files and images)</li>
   * <li>moving the db content and the images</li>
   * <li>moving attachments</li>
   * <li>moving versionned attached files</li>
   * <li>moving the pdc poistion</li>
   * <li>moving the statistics</li>
   * </ul>
   * @param completePub
   * @param nodePK
   * @param publi
   * @param fromId
   * @param fromComponentId
   * @param fromForeignPK
   * @param fromPubPK
   * @param toForeignPK
   * @param toPubPK
   * @param imagesSubDirectory
   * @param thumbnailsSubDirectory
   * @param toAbsolutePath
   * @param fromAbsolutePath
   * @throws RemoteException
   * @throws ThumbnailException
   * @throws PublicationTemplateException
   * @throws PdcException
   */
  private void movePublication(CompletePublication completePub, NodePK nodePK,
          PublicationDetail publi, String fromId, String fromComponentId, ForeignPK fromForeignPK,
          PublicationPK fromPubPK, ForeignPK toForeignPK, PublicationPK toPubPK,
          String imagesSubDirectory, String thumbnailsSubDirectory, String toAbsolutePath,
          String fromAbsolutePath)
          throws RemoteException, ThumbnailException, PublicationTemplateException, PdcException {

    boolean indexIt = false;

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
    try {
      // Change instanceId and move files
      AttachmentController.moveAttachments(fromForeignPK, toForeignPK, indexIt);
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
        for (InfoImageDetail attachment : completePub.getInfoDetail().getInfoImageList()) {
          String from = fromAbsolutePath + imagesSubDirectory + File.separator
                  + attachment.getPhysicalName();
          String to = toAbsolutePath + imagesSubDirectory + File.separator
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

        // get xmlContent to move
        PublicationTemplate pubTemplateFrom = getPublicationTemplateManager().
                getPublicationTemplate(fromComponentId + ":" + publi.getInfoId());
        IdentifiedRecordTemplate recordTemplateFrom =
                (IdentifiedRecordTemplate) pubTemplateFrom.getRecordSet().getRecordTemplate();

        PublicationTemplate pubTemplate =
                getPublicationTemplateManager().getPublicationTemplate(
                    getComponentId() + ":" + publi.getInfoId());
        IdentifiedRecordTemplate recordTemplate = (IdentifiedRecordTemplate) pubTemplate.
                getRecordSet().getRecordTemplate();

        try {
          GenericRecordSetManager.getInstance().moveRecord(recordTemplateFrom, fromId,
                  recordTemplate);
        } catch (FormException e) {
          SilverTrace.error("kmelia", "KmeliaSessionController.movePublication",
                  "kmelia.CANT_MOVE_PUBLICATION_CONTENT", "publication id = " + fromId, e);
        }
      }
    }

    // move comments
    if (indexIt) {
      getCommentService().moveAndReindexComments(PublicationDetail.getResourceType(),
          fromForeignPK, toForeignPK);
    } else {
      getCommentService().moveComments(PublicationDetail.getResourceType(), fromForeignPK,
          toForeignPK);
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

    // move publication itself
    getKmeliaBm().movePublicationInAnotherApplication(publi, nodePK, getUserId());
  }

  /**
   * get languages of publication header and attachments
   * @param pubDetail
   * @return a List of String (language codes)
   */
  public List<String> getPublicationLanguages() {
    List<String> languages = new ArrayList<String>();

    PublicationDetail pubDetail = getSessionPubliOrClone().getDetail();

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
      for (String language : attLanguages) {
        if (!languages.contains(language)) {
          languages.add(language);
        }
      }
    }

    return languages;
  }

  public List<String> getAttachmentLanguages() {
    PublicationPK pubPK = getSessionPubliOrClone().getDetail().getPK();

    // get attachments languages
    List<String> languages = new ArrayList<String>();
    List<String> attLanguages = AttachmentController.getLanguagesOfAttachments(
            new ForeignPK(pubPK.getId(), pubPK.getInstanceId()));
    for (String language : attLanguages) {
      if (!languages.contains(language)) {
        languages.add(language);
      }
    }
    return languages;
  }

  public void setAliases(List<Alias> aliases) throws RemoteException {
    getKmeliaBm().setAlias(getSessionPublication().getDetail().getPK(),
            aliases);
  }

  public void setAliases(PublicationPK pubPK, List<Alias> aliases) throws RemoteException {
    getKmeliaBm().setAlias(pubPK, aliases);
  }

  public List<Alias> getAliases() throws RemoteException {
    List<Alias> aliases =
            (List<Alias>) getKmeliaBm().getAlias(
                getSessionPublication().getDetail().getPK());

    // add user's displayed name
    for (Alias object : aliases) {
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
    String instanceId = null;
    List<NodeDetail> tree = null;
    NodePK root = new NodePK(NodePK.ROOT_NODE_ID);

    if (KmeliaHelper.isToolbox(getComponentId())) {
      root.setComponentName(getComponentId());
      tree = getKmeliaBm().getTreeview(root, "useless", false, false, getUserId(), false,
              StringUtil.getBooleanValue(getOrganizationController().getComponentParameterValue(
                  instanceId, "rightsOnTopics")));

      Treeview treeview = new Treeview(getComponentLabel(), tree, getComponentId());

      treeview.setNbAliases(getNbAliasesInComponent(aliases, instanceId));

      result.add(treeview);
    } else {
      List<SpaceInstLight> spaces = getOrganizationController().getSpaceTreeview(getUserId());
      for (SpaceInstLight space : spaces) {
        String path = "";
        String[] componentIds = getOrganizationController().getAvailCompoIdsAtRoot(
                space.getFullId(), getUserId());
        for (String componentId : componentIds) {
          instanceId = componentId;

          if (instanceId.startsWith("kmelia")) {
            String[] profiles =
                getOrganizationController().getUserProfiles(getUserId(), instanceId);
            String bestProfile = KmeliaHelper.getProfile(profiles);
            if ("admin".equalsIgnoreCase(bestProfile) || "publisher".equalsIgnoreCase(bestProfile)) {
              instanceIds.add(instanceId);
              root.setComponentName(instanceId);

              if (instanceId.equals(getComponentId())) {
                tree = getKmeliaBm().getTreeview(root, "useless", false, false, getUserId(),
                        false, StringUtil.getBooleanValue(getOrganizationController().
                            getComponentParameterValue(instanceId, "rightsOnTopics")));
              }

              if (!StringUtil.isDefined(path)) {
                List<SpaceInst> sPath = getOrganizationController().getSpacePath(space.getFullId());
                boolean first = true;
                for (SpaceInst spaceInPath : sPath) {
                  if (!first) {
                    path += " > ";
                  }
                  path += spaceInPath.getName();
                  first = false;
                }
              }

              Treeview treeview = new Treeview(path + " > "
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
    if ("admin".equalsIgnoreCase(bestProfile) || "publisher".equalsIgnoreCase(bestProfile)) {
      NodePK root = new NodePK(NodePK.ROOT_NODE_ID, instanceId);

      tree = getKmeliaBm().getTreeview(root, "useless", false, false, getUserId(), false,
              StringUtil.getBooleanValue(getOrganizationController().getComponentParameterValue(
                  instanceId, "rightsOnTopics")));
    }
    return tree;
  }

  private int getNbAliasesInComponent(List<Alias> aliases, String instanceId) {
    int nb = 0;
    for (Alias alias : aliases) {
      if (alias.getInstanceId().equals(instanceId)) {
        nb++;
      }
    }
    return nb;
  }

  private boolean isToolbox() {
    return KmeliaHelper.isToolbox(getComponentId());
  }

  /**
   * Return the url to the first attached file for the current publication.
   * @return the url to the first attached file for the curent publication.
   * @throws RemoteException
   */
  public String getFirstAttachmentURLOfCurrentPublication() throws RemoteException {
    PublicationPK pubPK = getSessionPublication().getDetail().getPK();
    String url = null;
    if (isVersionControlled()) {
      VersioningUtil versioning = new VersioningUtil();
      List<Document> documents = versioning.getDocuments(new ForeignPK(pubPK));
      if (!documents.isEmpty()) {
        Document document = documents.get(0);
        DocumentVersion documentVersion = versioning.getLastPublicVersion(document.getPk());
        if (documentVersion != null) {
          url = URLManager.getApplicationURL() + versioning.getDocumentVersionURL(document.
                  getInstanceId(), documentVersion.getLogicalName(), document.getPk().getId(),
                  documentVersion.getPk().getId());
        }
      }
    } else {
      List<AttachmentDetail> attachments = AttachmentController.searchAttachmentByPKAndContext(
              pubPK, "Images");
      if (!attachments.isEmpty()) {
        AttachmentDetail attachment = attachments.get(0);
        url = URLManager.getApplicationURL() + attachment.getAttachmentURL();
      }
    }
    return url;
  }

  /**
   * Return the url to access the file
   * @param fileId the id of the file (attachment or document id).
   * @return the url to the file.
   * @throws RemoteException
   */
  public String getAttachmentURL(String fileId) throws RemoteException {
    String url = null;
    if (isVersionControlled()) {
      VersioningUtil versioningUtil = new VersioningUtil();
      DocumentPK documentPk = new DocumentPK(Integer.parseInt(fileId));
      Document document = versioningUtil.getDocument(documentPk);
      DocumentVersion documentVersion = versioningUtil.getLastPublicVersion(documentPk);
      url = URLManager.getApplicationURL() + versioningUtil.getDocumentVersionURL(document.
              getInstanceId(), documentVersion.getLogicalName(), document.getPk().getId(),
              documentVersion.getPk().getId());
    } else {
      AttachmentDetail attachment = AttachmentController.searchAttachmentByPK(new AttachmentPK(
              fileId));
      url = URLManager.getApplicationURL() + attachment.getAttachmentURL();
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
    String currentId = id;
    boolean haveDescriptor = false;
    // regarder si ce fichier existe
    if (useUpdateChain()) {
      if (!StringUtil.isDefined(currentId)) {
        currentId = getSessionTopic().getNodePK().getId();
      }
      File descriptorFile = new File(getUpdateChainDescriptorFilename(currentId));
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
    for (FieldUpdateChainDescriptor field : fields) {
      saveFields.setHelper(updateChainDescriptor.getHelper());

      if ("Name".equals(field.getName())) {
        saveFields.setName(field);
      } else if ("Description".equals(field.getName())) {
        saveFields.setDescription(field);
      } else if ("Keywords".equals(field.getName())) {
        saveFields.setKeywords(field);
      } else if ("Topics".equals(field.getName())) {
        saveFields.setTree(field);
      }
    }

  }

  public String getXmlFormForFiles() {
    return getComponentParameterValue("XmlFormForFiles");
  }

  public File exportPublication() {
    if (!isFormatSupported("zip")) {
      throw new KmeliaRuntimeException("KmeliaSessionController.exportPublication()",
              SilverpeasRuntimeException.ERROR, "kmelia.EX_EXPORT_FORMAT_NOT_SUPPORTED");
    }
    PublicationPK pubPK = getSessionPublication().getDetail().getPK();
    File pdf = null;
    try {
      // create subdir to zip
      String subdir = "ExportPubli_" + pubPK.getId() + "_" + System.currentTimeMillis();
      String fileName = getPublicationExportFileName(getSessionPublication(), getLanguage());
      String subDirPath =
              FileRepositoryManager.getTemporaryPath() + subdir + File.separator + fileName;
      FileFolderManager.createFolder(subDirPath);
      // generate from the publication a document in PDF
      if (isFormatSupported(DocumentFormat.pdf.name())) {
        pdf = generateDocument(DocumentFormat.pdf, pubPK.getId());
        // copy pdf into zip
        FileRepositoryManager.copyFile(pdf.getPath(), subDirPath + File.separator + pdf.getName());
      }
      // copy files
      new AttachmentImportExport().getAttachments(pubPK, subDirPath, "useless", null);
      new VersioningImportExport().exportDocuments(pubPK, subDirPath, "useless", null);
      String zipFileName = FileRepositoryManager.getTemporaryPath() + fileName + ".zip";
      // zip PDF and files
      ZipManager.compressPathToZip(subDirPath, zipFileName);

      return new File(zipFileName);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.exportPublication()",
              SilverpeasRuntimeException.ERROR, "kmelia.EX_CANT_EXPORT_PUBLICATION", e);
    } finally {
      if (pdf != null) {
        pdf.delete();
      }
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
    String currentId = id;
    if (isWysiwygOnTopicsEnabled()) {
      try {
        if (!StringUtil.isDefined(currentId)) {
          currentId = getSessionTopic().getNodePK().getId();
        }
        return WysiwygController.load(getComponentId(), "Node_" + currentId, getLanguage());
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
              "root.MSG_GEN_PARAM_VALUE", "properties wrong parameter " + name + " = "
                  + lengthFromProperties);
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
                "root.MSG_GEN_PARAM_VALUE", "xml wrong parameter " + name + " = "
                    + lengthFromXml);
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
    for (String compoId : compoIds) {
      if (StringUtil.getBooleanValue(orgaController.getComponentParameterValue(
              "gallery" + compoId,
              "viewInWysiwyg"))) {
        if (galleries == null) {
          galleries = new ArrayList<ComponentInstLight>();
        }

        ComponentInstLight gallery = orgaController.getComponentInstLight("gallery" + compoId);
        galleries.add(gallery);
      }
    }
    return galleries;
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

  public boolean isAttachmentsEnabled() {
    return StringUtil.getBooleanValue(getComponentParameterValue(TAB_ATTACHMENTS));
  }

  /**
   * Get publications and aliases of this topic and its subtopics answering to the query
   * @param query
   * @param sort
   * @return List of Kmelia publications
   */
  public synchronized List<KmeliaPublication> search(String query) {
    List<KmeliaPublication> userPublications = new ArrayList<KmeliaPublication>();
    QueryDescription queryDescription = new QueryDescription(query);
    queryDescription.setSearchingUser(getUserDetail().getId());

    // Search in all spaces and components (to find alias)
    String[] spacesIds = getOrganizationController().getAllSpaceIds(getUserDetail().getId());
    for (String spacesId : spacesIds) {
      String[] componentsIds =
              getOrganizationController().getComponentIdsForUser(getUserDetail().getId(),
                  this.getComponentName());
      for (String componentsId : componentsIds) {
        queryDescription.addSpaceComponentPair(spacesId, componentsId);
      }
    }

    try {
  
      List<MatchingIndexEntry> results = SearchEngineFactory.getSearchEngine().search(
        queryDescription).getEntries();
      PublicationDetail pubDetail = new PublicationDetail();
      pubDetail.setPk(new PublicationPK("unknown"));
      KmeliaPublication publication = KmeliaPublication.aKmeliaPublicationFromDetail(pubDetail);

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

      for (MatchingIndexEntry result : results) {
        try {
          if ("Publication".equals(result.getObjectType())) {
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
        } catch (Exception e) {
          SilverTrace.error("kmelia", "KmeliaSessionController.search",
              "kmelia.ERROR_PROCESSING_POTENTIAL_RESULT", "pubId = " + result.getObjectId());
        }
      }
      for (String pubId : pubIds) {
        publication = KmeliaPublication.aKmeliaPublicationFromDetail(getPublicationDetail(pubId));
        userPublications.add(publication);
      }
    } catch (Exception pe) {
      throw new KmeliaRuntimeException("KmeliaSessionController.search",
              SilverpeasRuntimeException.ERROR, "root.EX_SEARCH_ENGINE_FAILED", pe);
    }
    return userPublications;
  }

  /**
   * @return the list of SpaceInst from current space identifier (in session) to root space <br/>
   * (all the subspace)
   */
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

  /**
   * Is news manage
   * @return boolean
   */
  public boolean isNewsManage() {
    return StringUtil.getBooleanValue(getComponentParameterValue("isNewsManage"));
  }

  /**
   * Récupère une actualité déléguée dans le composant delegatednews correspondant à la publication
   * passée en paramètre
   * @param pubId : l'id de la publication de Theme Tracker
   * @return DelegatedNews : l'objet correspondant à l'actualité déléguée dans le composant
   * delegatednews ou null si elle n'existe pas
   */
  public DelegatedNews getDelegatedNews(String pubId) {

    DelegatedNewsService delegatedNewsService = ServicesFactory.getDelegatedNewsService();
    DelegatedNews delegatedNews = delegatedNewsService.getDelegatedNews(Integer.parseInt(pubId));
    return delegatedNews;
  }

  /**
   * Ajout d'une actualité déléguée dans le composant delegatednews
   * @return String : pubId
   */
  public String addDelegatedNews() {
    // ajoute l'actualité déléguée dans le composant delegatednews
    KmeliaPublication kmeliaPublication = getSessionPublication();
    String pubId = kmeliaPublication.getId();
    PublicationDetail pubDetail = kmeliaPublication.getDetail();
    String instanceId = pubDetail.getInstanceId();
    String contributorId = pubDetail.getUpdaterId();
    Date beginDateAndHour = DateUtil.getDate(pubDetail.getBeginDate(), pubDetail.getBeginHour());
    Date endDateAndHour = DateUtil.getDate(pubDetail.getEndDate(), pubDetail.getEndHour());
    DelegatedNewsService delegatedNewsService = ServicesFactory.getDelegatedNewsService();
    delegatedNewsService.addDelegatedNews(Integer.parseInt(pubId), instanceId, contributorId,
            new Date(), beginDateAndHour, endDateAndHour);

    // alerte l'équipe éditoriale du composant delegatednews
    String[] tabInstanceId = getOrganizationController().getCompoId("delegatednews");
    String delegatednewsInstanceId = null;
    for (String aTabInstanceId : tabInstanceId) {
      delegatednewsInstanceId = aTabInstanceId;
      break;
    }

    delegatedNewsService.notifyDelegatedNewsToValidate(pubId,
        pubDetail.getName(this.getLanguage()), this.getUserId(), this.getUserDetail()
            .getDisplayedName(), delegatednewsInstanceId);

    return pubId;
  }

  @Override
  public String getPublicationExportFileName(KmeliaPublication publication, String language) {
    String lang = getLanguage();
    String pubId = publication.getPk().getId();
    StringBuilder fileName = new StringBuilder(250);

    fileName.append(getUserDetail().getLogin()).append('-');

    // add space path to filename
    List<SpaceInst> listSpaces = getSpacePath();
    for (SpaceInst space : listSpaces) {
      fileName.append(space.getName(lang)).append('-');
    }
    // add component name to filename
    fileName.append(getComponentLabel());

    if (!isKmaxMode) {
      try {
        TopicDetail topic = getPublicationTopic(pubId);
        Collection<NodeDetail> path = topic.getPath();
        for (NodeDetail node : path) {
          fileName.append('-').append(node.getName(lang));
        }
      } catch (RemoteException ex) {
        SilverTrace.error("kmelia", getClass().getSimpleName() + ".getPublicationExportFileName()",
                "root.EX_NO_MESSAGE", ex);
      }
    }

    fileName.append('-').append(publication.getDetail().getTitle()).append('-');
    fileName.append(publication.getPk().getId());
    return StringUtil.toAcceptableFilename(fileName.toString());
  }

  public void removePublicationContent() throws RemoteException {
    KmeliaPublication publication = getSessionPubliOrClone();
    PublicationPK pubPK = publication.getPk();
    getKmeliaBm().removeContentOfPublication(pubPK);
    // reset reference to content
    publication.getDetail().setInfoId("0");
  }

  public boolean isComponentManageable() {
    return componentManageable;
  }

  /**
   * Gets all available export formats.
   * @return a list of export formats Silverpeas supports for export.
   */
  public List<String> getAvailableFormats() {
    return Arrays.asList(AVAILABLE_EXPORT_FORMATS);
  }

  /**
   * Gets the export formats that are supported by the current Kmelia component instance. As some of
   * the export formats can be deactivated in the Kmelia settings file, this method returns all the
   * formats that are currently active.
   * @return a list of export formats.
   */
  public List<String> getSupportedFormats() {
    String exportFormats = getSettings().getString(EXPORT_FORMATS, "");
    List<String> supportedFormats = new ArrayList<String>();
    if (!exportFormats.trim().isEmpty()) {
      List<String> availableFormats = getAvailableFormats();
      for (String exportFormat : exportFormats.trim().split(" ")) {
        if (!availableFormats.contains(exportFormat)) {
          throw new KmeliaRuntimeException("KmeliaSessionController.getSupportedFormats()",
                  SilverTrace.TRACE_LEVEL_ERROR, "kmelia.EX_UNKNOWN_EXPORT_FORMAT");
        }
        supportedFormats.add(exportFormat);
      }
    }
    return supportedFormats;
  }

  /**
   * Is the specified export format is supported by the Kmelia component instance?
   * @param format a recognized export format.
   * @return true if the specified format is currently supported for the publication export, false
   * otherwise.
   */
  public boolean isFormatSupported(String format) {
    return getSupportedFormats().contains(format);
  }

  /**
   * Is the specified publication classified on the PdC.
   * @param publication a publication;
   * @return true if the publication is classified, false otherwise.
   * @throws PdcException if an error occurs while verifying the publication is classified.
   */
  public boolean isClassifiedOnThePdC(final PublicationDetail publication) throws PdcException {
    List<ClassifyPosition> positions = getPdcBm().getPositions(
            Integer.valueOf(publication.getSilverObjectId()),
            publication.getComponentInstanceId());
    return !positions.isEmpty();
  }

  /**
   * Is the default classification on the PdC used to classify the publications published in the
   * specified topic of the specified component instance can be modified during the
   * multi-publications import process? If no default classification is defined for the specified
   * topic (and for any of its parent topics), then false is returned.
   * @param topicId the unique identifier of the topic.
   * @param componentId the unique identifier of the component instance.
   * @return true if the default classification can be modified during the automatical
   * classification of the imported publications. False otherwise.
   */
  public boolean isDefaultClassificationModifiable(String topicId, String componentId) {
    PdcClassificationService classificationService = PdcServiceFactory.getFactory().
            getPdcClassificationService();
    PdcClassification defaultClassification = classificationService.findAPreDefinedClassification(
            topicId, componentId);
    return defaultClassification != NONE_CLASSIFICATION && defaultClassification.isModifiable();
  }

  public void resetSelectedPublicationIds() {
    this.selectedPublicationIds.clear();
  }

  public List<String> processSelectedPublicationIds(String selectedPublicationIds,
      String notSelectedPublicationIds) {
    StringTokenizer tokenizer = null;
    if (selectedPublicationIds != null) {
      tokenizer = new StringTokenizer(selectedPublicationIds, ",");
      while (tokenizer.hasMoreTokens()) {
        this.selectedPublicationIds.add(tokenizer.nextToken());
      }
    }

    if (notSelectedPublicationIds != null) {
      tokenizer = new StringTokenizer(notSelectedPublicationIds, ",");
      while (tokenizer.hasMoreTokens()) {
        this.selectedPublicationIds.remove(tokenizer.nextToken());
      }
    }

    return this.selectedPublicationIds;
  }

  public List<String> getSelectedPublicationIds() {
    return selectedPublicationIds;
  }
  
  public boolean isCustomPublicationTemplateUsed() {
    return customPublicationTemplateUsed;
  }
  
  public String getCustomPublicationTemplateName() {
    return customPublicationTemplateName;
  }

}
