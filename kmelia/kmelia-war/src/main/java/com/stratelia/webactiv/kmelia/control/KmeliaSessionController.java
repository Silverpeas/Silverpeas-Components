/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import java.awt.datatransfer.UnsupportedFlavorException;
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
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;

import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.component.kmelia.InstanceParameters;
import org.silverpeas.component.kmelia.KmeliaPublicationHelper;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.importExport.attachment.AttachmentImportExport;
import org.silverpeas.importExport.versioning.VersioningImportExport;
import org.silverpeas.search.SearchEngineFactory;
import org.silverpeas.search.indexEngine.model.IndexManager;
import org.silverpeas.search.searchEngine.model.MatchingIndexEntry;
import org.silverpeas.search.searchEngine.model.QueryDescription;
import org.silverpeas.subscription.SubscriptionContext;
import org.silverpeas.util.GlobalContext;
import org.silverpeas.wysiwyg.WysiwygException;
import org.silverpeas.wysiwyg.control.WysiwygController;

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
import com.silverpeas.importExport.model.ImportExportException;
import com.silverpeas.kmelia.SearchContext;
import com.silverpeas.kmelia.domain.TopicSearch;
import com.silverpeas.kmelia.export.ExportFileNameProducer;
import com.silverpeas.kmelia.search.KmeliaSearchServiceFactory;
import com.silverpeas.pdc.PdcServiceFactory;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.model.PdcPosition;
import com.silverpeas.pdc.service.PdcClassificationService;
import com.silverpeas.pdc.web.PdcClassificationEntity;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.subscribe.service.NodeSubscriptionResource;
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
import com.silverpeas.util.clipboard.ClipboardException;
import com.silverpeas.util.clipboard.ClipboardSelection;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;

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
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.beans.admin.ProfileInst;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.kmelia.FileImport;
import com.stratelia.webactiv.kmelia.KmeliaSecurity;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
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
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.WAAttributeValuePair;
import com.stratelia.webactiv.util.coordinates.model.Coordinate;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.node.model.NodeSelection;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
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
import com.stratelia.webactiv.util.statistic.model.StatisticRuntimeException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import static com.silverpeas.kmelia.export.KmeliaPublicationExporter.*;
import static com.silverpeas.pdc.model.PdcClassification.NONE_CLASSIFICATION;
import static com.silverpeas.pdc.model.PdcClassification.aPdcClassificationOfContent;

public class KmeliaSessionController extends AbstractComponentSessionController implements
    ExportFileNameProducer {

  /**
   * The different export formats the KmeliaPublicationExporter should support.
   */
  private static final String EXPORT_FORMATS = "kmelia.export.formats.active";
  /**
   * All the formats that are available for the export of publications.
   */
  private static final String[] AVAILABLE_EXPORT_FORMATS = {"zip", "pdf", "odt", "doc"};

  /* EJBs used by sessionController */ private ThumbnailService thumbnailService = null;
  private CommentService commentService = null;
  private PdcBm pdcBm = null;
  private StatisticBm statisticBm = null;
  private NotificationManager notificationManager = null;
  // Session objects
  private TopicDetail sessionTopic = null;
  private String currentFolderId = NodePK.ROOT_NODE_ID;
  private KmeliaPublication sessionPublication = null;
  private KmeliaPublication sessionClone = null;
  private String sessionPath = null; // html link with <a href="">
  private String sessionPathString = null; // html string only
  private TopicDetail sessionTopicToLink = null;
  private boolean sessionOwner = false;
  private List<KmeliaPublication> sessionPublicationsList = null;
  private List<String> sessionCombination = null; // Specific Kmax
  private String sessionTimeCriteria = null; // Specific Kmax
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
  // For import files
  public final static String UNITARY_IMPORT_MODE = "0";
  public final static String MASSIVE_IMPORT_MODE_ONE_PUBLICATION = "1";
  public final static String MASSIVE_IMPORT_MODE_MULTI_PUBLICATIONS = "2";
  // Versioning options
  public final static String VER_USE_WRITERS_AND_READERS = "0";
  public final static String VER_USE_WRITERS = "1";
  public final static String VER_USE_READERS = "2";
  public final static String VER_USE_NONE = "3";
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
  private SearchContext searchContext = null;

  /**
   * Creates new sessionClientController
   *
   * @param mainSessionCtrl
   * @param context
   */
  public KmeliaSessionController(MainSessionController mainSessionCtrl, ComponentContext context) {
    super(mainSessionCtrl, context, "org.silverpeas.kmelia.multilang.kmeliaBundle",
        "org.silverpeas.kmelia.settings.kmeliaIcons",
        "org.silverpeas.kmelia.settings.kmeliaSettings");
    init();
  }

  public static List<String> getLanguagesOfAttachments(ForeignPK foreignPK) {
    List<String> languages = new ArrayList<String>();
    for (String availableLanguage : I18NHelper.getAllSupportedLanguages()) {
      List<SimpleDocument> attachments = AttachmentServiceFactory.getAttachmentService()
          .listDocumentsByForeignKeyAndType(foreignPK, DocumentType.attachment, availableLanguage);
      for (SimpleDocument attachment : attachments) {
        if (availableLanguage.equalsIgnoreCase(attachment.getLanguage())) {
          languages.add(availableLanguage);
          break;
        }
      }
    }
    return languages;
  }

  private void init() {
    // Remove all data store by this SessionController
    removeSessionObjects();
    currentLanguage = getLanguage();
    if (StringUtil.getBooleanValue(getSettings().getString("massiveDragAndDropAllowed"))) {
      isDragAndDropEnableByUser = isDragAndDropEnableByUser();
    }
    componentManageable = GeneralPropertiesManager.getBoolean("AdminFromComponentEnable", true);
    if (componentManageable) {
      componentManageable = getOrganisationController().isComponentManageable(getComponentId(),
          getUserId());
    }
    defaultSortValue = getComponentParameterValue("publicationSort");
    if (!StringUtil.isDefined(defaultSortValue)) {
      defaultSortValue = getSettings().getString("publications.sort.default", "2");
    }
    // check if this instance use a specific template of publication
    SilverpeasTemplate template = SilverpeasTemplateFactory.createSilverpeasTemplateOnComponents();
    customPublicationTemplateName = "publication_" + getComponentId();
    customPublicationTemplateUsed = template.isCustomTemplateExists("kmelia",
        customPublicationTemplateName);
    sessionPublicationsList = Collections.emptyList();
  }

  /**
   * Gets a business service of comments.
   *
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
      return EJBUtilitaire.getEJBObjectRef(JNDINames.KMELIABM_EJBHOME, KmeliaBm.class);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.getKmeliaBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public StatisticBm getStatisticBm() {
    if (statisticBm == null) {
      try {
        statisticBm = EJBUtilitaire.getEJBObjectRef(JNDINames.STATISTICBM_EJBHOME,
            StatisticBm.class);
      } catch (Exception e) {
        throw new StatisticRuntimeException("KmeliaSessionController.getStatisticBm()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return statisticBm;
  }

  public ResourceLocator getPublicationSettings() {
    if (publicationSettings == null) {
      publicationSettings = new ResourceLocator(
          "org.silverpeas.util.publication.publicationSettings", getLanguage());
    }
    return publicationSettings;
  }

  public int getNbPublicationsOnRoot() {
    if (nbPublicationsOnRoot == -1) {
      String parameterValue = getComponentParameterValue("nbPubliOnRoot");
      if (StringUtil.isDefined(parameterValue)) {
        nbPublicationsOnRoot = Integer.parseInt(parameterValue);
      } else {
        if (KmeliaHelper.isKmelia(getComponentId())) {
          // lecture du properties
          nbPublicationsOnRoot = getSettings().getInteger("HomeNbPublications", 15);
        } else {
          nbPublicationsOnRoot = 0;
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
    return KmeliaPublicationHelper.isTreeEnabled(getComponentId());
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

  public boolean isExportComponentAllowed() {
    return StringUtil.getBooleanValue(getSettings().getString("exportComponentAllowed"));
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
    return StringUtil.getBooleanValue(getComponentParameterValue(
        InstanceParameters.displayNbItemsOnFolders));
  }

  public boolean isRightsOnTopicsEnabled() {
    return StringUtil.
        getBooleanValue(getComponentParameterValue(InstanceParameters.rightsOnFolders));
  }

  public boolean isFoldersLinkedEnabled() {
    return StringUtil.getBooleanValue(getComponentParameterValue("isLink"));
  }

  public boolean attachmentsInPubList() {
    return StringUtil.getBooleanValue(getComponentParameterValue("attachmentsInPubList"));
  }

  public boolean isPublicationIdDisplayed() {
    return StringUtil.getBooleanValue(getComponentParameterValue("codification"));
  }

  public boolean isSuppressionOnlyForAdmin() {
    return StringUtil.getBooleanValue(getComponentParameterValue("suppressionOnlyForAdmin"));
  }

  public boolean isThumbnailMandatory() {
    return StringUtil.getBooleanValue(getComponentParameterValue("thumbnailMandatory"));
  }

  public boolean isFolderSharingEnabled() {
    return StringUtil.getBooleanValue(getComponentParameterValue("useFolderSharing"));
  }

  public boolean isContentEnabled() {
    String parameterValue = getComponentParameterValue("tabContent");
    if (!StringUtil.isDefined(parameterValue)) {
      return false;
    }
    return StringUtil.getBooleanValue(parameterValue);
  }

  public boolean isSeeAlsoEnabled() {
    String parameterValue = getComponentParameterValue("tabSeeAlso");
    if (!StringUtil.isDefined(parameterValue)) {
      return false;
    }
    return StringUtil.getBooleanValue(parameterValue);
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
        if (!StringUtil.getBooleanValue(parameterValue)) {
          invisibleTabs.add("tabattachments");
        }
      }
    }

    if (!isSeeAlsoEnabled()) {
      invisibleTabs.add("tabseealso");
    }

    parameterValue = this.getComponentParameterValue("tabAccessPaths");
    if (!StringUtil.isDefined(parameterValue)) {
      // invisibleTabs.add("tabaccesspaths");
    } else {
      if (!StringUtil.getBooleanValue(parameterValue)) {
        invisibleTabs.add("tabaccesspaths");
      }
    }

    parameterValue = this.getComponentParameterValue("tabReadersList");
    if (!StringUtil.isDefined(parameterValue)) {
      // invisibleTabs.add("tabreaderslist");
    } else {
      if (!StringUtil.getBooleanValue(parameterValue)) {
        invisibleTabs.add("tabreaderslist");
      }
    }

    parameterValue = this.getComponentParameterValue("tabComments");
    if (!StringUtil.isDefined(parameterValue)) {
      invisibleTabs.add("tabcomments");
    } else {
      if (!StringUtil.getBooleanValue(parameterValue)) {
        invisibleTabs.add("tabcomments");
      }
    }

    return invisibleTabs;
  }

  /**
   * Generates a document in the specified format from the specified publication.
   *
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
        KmeliaPublication publication = KmeliaPublication
            .aKmeliaPublicationWithPk(new PublicationPK(fromPubId, getComponentId()));
        String fileName = getPublicationExportFileName(publication, getLanguage());
        document = new File(FileRepositoryManager.getTemporaryPath() + fileName + "." + inFormat.
            name());
        FileOutputStream output = new FileOutputStream(document);
        ExportDescriptor descriptor = ExportDescriptor.withOutputStream(output).
            withParameter(EXPORT_FOR_USER, getUserDetail()).
            withParameter(EXPORT_LANGUAGE, getLanguage()).
            withParameter(EXPORT_TOPIC, getCurrentFolderId()).
            inFormat(inFormat.name());
        aKmeliaPublicationExporter().export(descriptor, publication);
      } catch (Exception ex) {
        SilverTrace.error("kmelia",
            "KmeliaSessionControl.KmeliaSessionController.generateDocument()",
            "root.EX_CANT_EXPORT_PUBLICATION", ex);
        if (document != null) {
          FileUtils.deleteQuietly(document);
        }
        throw new KmeliaRuntimeException("KmeliaSessionController.generateDocument()",
            SilverpeasRuntimeException.ERROR, "kmelia.EX_CANT_EXPORT_PUBLICATION", ex);
      }
    }
    return document;
  }

  public String getProfile() throws RemoteException {
    return getUserTopicProfile();
  }

  public String getUserTopicProfile() throws RemoteException {
    return getUserTopicProfile(null);
  }

  public String getUserTopicProfile(String id) throws RemoteException {
    String nodeId = id;
    if (!StringUtil.isDefined(id)) {
      nodeId = getCurrentFolderId();
    }
    return getKmeliaBm().getUserTopicProfile(getNodePK(nodeId), getUserId());
  }

  public List<String> getUserIdsOfTopic() throws RemoteException {
    if (!isRightsOnTopicsEnabled()) {
      return null;
    }

    NodeDetail node = getNodeHeader(getCurrentFolderId());

    // check if we have to take care of topic's rights
    if (node != null && node.haveRights()) {
      int rightsDependsOn = node.getRightsDependsOn();
      List<String> profileNames = new ArrayList<String>(4);
      profileNames.add(KmeliaHelper.ROLE_ADMIN);
      profileNames.add(KmeliaHelper.ROLE_PUBLISHER);
      profileNames.add(KmeliaHelper.ROLE_WRITER);
      profileNames.add(KmeliaHelper.ROLE_READER);
      String[] userIds = getOrganisationController().getUsersIdsByRoleNames(getComponentId(),
          Integer.toString(rightsDependsOn), ObjectType.NODE, profileNames);
      return Arrays.asList(userIds);
    } else {
      return null;
    }
  }

  public boolean isCurrentTopicAvailable() throws RemoteException {
    if (isRightsOnTopicsEnabled()) {
      if (KmeliaHelper.isToValidateFolder(getCurrentFolderId())) {
        return true;
      }
      NodeDetail node = getNodeHeader(getCurrentFolderId());
      if (node.haveRights()) {
        int rightsDependsOn = node.getRightsDependsOn();
        return getOrganisationController().isObjectAvailable(rightsDependsOn, ObjectType.NODE,
            getComponentId(), getUserId());
      }
    }
    return true;
  }

  public boolean isUserComponentAdmin() {
    return SilverpeasRole.admin.isInRole(KmeliaHelper.getProfile(getUserRoles()));
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
    if (!id.equals(getCurrentFolderId())) {
      indexOfFirstPubToDisplay = 0;
    }

    TopicDetail currentTopic;
    if (isUserComponentAdmin()) {
      currentTopic = getKmeliaBm().goTo(getNodePK(id), getUserId(), isTreeStructure(), "admin",
          false);
    } else {
      currentTopic = getKmeliaBm().goTo(getNodePK(id), getUserId(), isTreeStructure(),
          getUserTopicProfile(id), isRightsOnTopicsEnabled());
    }

    if (displayNbPublis()) {
      List<NodeDetail> treeview = getTreeview("0");
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

  public List<NodeDetail> getTreeview(String nodeId) throws RemoteException {
    if (isUserComponentAdmin()) {
      return getKmeliaBm().getTreeview(getNodePK(nodeId), "admin", isCoWritingEnable(),
          isDraftVisibleWithCoWriting(), getUserId(), displayNbPublis(), false);
    } else {
      return getKmeliaBm().getTreeview(getNodePK(nodeId), getProfile(), isCoWritingEnable(),
          isDraftVisibleWithCoWriting(), getUserId(), displayNbPublis(),
          isRightsOnTopicsEnabled());
    }
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

  public synchronized void changeTopicStatus(String newStatus, String topicId,
      boolean recursiveChanges) throws RemoteException {
    getKmeliaBm().changeTopicStatus(newStatus, getNodePK(topicId), recursiveChanges);
  }

  /**
   * @return @throws RemoteException
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

  private Collection<Collection<NodeDetail>> getPathList(PublicationPK pk) {
    return getKmeliaBm().getPathList(pk);
  }

  public synchronized Collection<NodePK> getPublicationFathers(String pubId)
      throws RemoteException {
    return getKmeliaBm().getPublicationFathers(getPublicationPK(pubId));
  }

  public NodePK getAllowedPublicationFather(String pubId) throws RemoteException {
    return getKmeliaBm().getPublicationFatherPK(getPublicationPK(pubId), isTreeStructure(),
        getUserId(), isRightsOnTopicsEnabled());
  }

  public synchronized String createPublication(PublicationDetail pubDetail,
      final PdcClassificationEntity classification) throws RemoteException {
    pubDetail.getPK().setSpace(getSpaceId());
    pubDetail.getPK().setComponentName(getComponentId());
    pubDetail.setCreatorId(getUserId());
    pubDetail.setCreationDate(new Date());

    String result;
    if (isKmaxMode) {
      result = getKmeliaBm().createKmaxPublication(pubDetail);
    } else {
      if (classification.isUndefined()) {
        result = getKmeliaBm().createPublicationIntoTopic(pubDetail, getCurrentFolderPK());
      } else {
        List<PdcPosition> pdcPositions = classification.getPdcPositions();
        PdcClassification withClassification = aPdcClassificationOfContent(pubDetail.getId(),
            pubDetail.getComponentInstanceId()).withPositions(pdcPositions);
        result = getKmeliaBm().createPublicationIntoTopic(pubDetail, getCurrentFolderPK(),
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
      if (NodePK.BIN_NODE_ID.equals(getCurrentFolderId())) {
        // publication is in the trash can
        pubDetail.setIndexOperation(IndexManager.NONE);
      }

      if (getSessionClone() != null) {
        if (getSessionClone().getId().equals(pubDetail.getId())) {
          // update the clone, clone stay in same status
          pubDetail.setStatusMustBeChecked(false);
          
          // clone must not be indexed
          pubDetail.setIndexOperation(IndexManager.NONE);

        }
      }
      getKmeliaBm().updatePublication(pubDetail);
    }
    SilverTrace.spy("kmelia", "KmeliaSessionController.updatePublication", getSpaceId(),
        getComponentId(),
        pubDetail.getId(), getUserDetail().getId(), SilverTrace.SPY_ACTION_UPDATE);
  }

  public boolean isCloneNeeded() throws RemoteException {
    String currentStatus = getSessionPublication().getDetail().getStatus();
    return (isPublicationAlwaysVisibleEnabled() && "writer".equals(
        getUserTopicProfile()) && (getSessionClone() == null) && PublicationDetail.VALID
        .equals(currentStatus));
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
   *
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
    String nodeId = getCurrentFolderId();
    if (NodePK.BIN_NODE_ID.equals(nodeId)) {
      // la publication sera supprimée définitivement, il faut donc supprimer les fichiers joints
      try {
        WysiwygController.deleteWysiwygAttachments(getComponentId(), pubId);
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

  public List<String> deleteSelectedPublications() throws RemoteException {
    List<String> removed = getKmeliaBm().deletePublications(getSelectedPublicationIds(),
        getCurrentFolderPK(), getUserId());
    resetSelectedPublicationIds();
    return removed;
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

  private void removeXMLContentOfPublication(PublicationPK pubPK) {
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
          SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LE_CONTENU_XML", e);
    } catch (FormException e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.removeXMLContentOfPublication()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LE_CONTENU_XML", e);
    }
  }

  private static boolean isInteger(String id) {
    return StringUtil.isInteger(id);
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
    String currentPubId = getSessionPubliOrClone().getDetail().getPK().getId();
    if (isCloneNeeded()) {
      currentPubId = clonePublication();
    }
    if (getSessionClone() != null) {
      ModelPK modelPK = new ModelPK(modelId, getPublicationPK(currentPubId));
      getPublicationBm().createInfoModelDetail(getPublicationPK(currentPubId), modelPK, infos);
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
      KmeliaPublication pub = getPublication(getSessionPublication().getDetail().getPK().getId());
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
      getPublicationBm().updateInfoDetail(getPublicationPK(currentPubId), infos);
    } else {
      getKmeliaBm().updateInfoDetail(getPublicationPK(currentPubId), infos);
    }
    refreshSessionPubliAndClone();
  }

  /**
   * removes links between specified publication and other publications contained in links parameter
   *
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
   *
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

  /**
   * Get publications explicitly referenced by current publication. Only valid publications which
   * are not in bin are returned. Rights of user are checked (applications and folders).
   *
   * @return a List of KmeliaPublication
   * @see KmeliaPublication
   * @throws RemoteException
   */
  public List<KmeliaPublication> getLinkedVisiblePublications() throws RemoteException {
    List<ForeignPK> seeAlsoList = getSessionPublication().getCompleteDetail().getLinkList();
    List<ForeignPK> authorizedSeeAlsoList = new ArrayList<ForeignPK>();
    for (ForeignPK curFPK : seeAlsoList) {
      String curComponentId = curFPK.getComponentName();
      // check if user have access to application
      if (curComponentId != null && getOrganisationController().isComponentAvailable(curComponentId,
          getUserId())) {
        authorizedSeeAlsoList.add(curFPK);
      }
    }

    Collection<KmeliaPublication> linkedPublications = getPublications(authorizedSeeAlsoList);
    List<KmeliaPublication> authorizedAndValidSeeAlsoList = new ArrayList<KmeliaPublication>();
    for (KmeliaPublication pub : linkedPublications) {
      // check if publication is valid and not in bin
      if (pub.getDetail().isValid() && !isPublicationDeleted(pub.getDetail().getPK())) {
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
      // getting rank of publication
      KmeliaPublication pub = KmeliaPublication.aKmeliaPublicationFromDetail(publicationDetail);
      if (getSessionPublicationsList() != null) {
        rang = getSessionPublicationsList().indexOf(pub);
        if (rang != -1 && getSearchContext() != null) {
          getSessionPublicationsList().get(rang).read = true;
        }
      }
    }

    return publication;
  }

  public int getNbPublis() {
    if (getSessionPublicationsList() != null) {
      return getSessionPublicationsList().size();
    }
    return 1;
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
                || (isCoWritingEnable() && isDraftVisibleWithCoWriting() && !getProfile().equals(
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
    List<KmeliaPublication> publications = sort(getKmeliaBm().getPublicationsToValidate(
        getComponentId(), getUserId()), sort);
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
      default:        // display publications according to manual order defined by admin

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
    getPublicationBm().changePublicationsOrder(sortedPubIds, getCurrentFolderPK());
  }

  public Collection<PublicationDetail> getAllPublications() throws RemoteException {
    return getAllPublications(null);
  }

  /**
   * Get all publications sorted
   *
   * @param sortedBy (example: pubName asc)
   * @return Collection of Publications
   * @throws RemoteException
   */
  public Collection<PublicationDetail> getAllPublications(String sortedBy) throws RemoteException {
    String publication_default_sorting = getSettings().getString("publication_defaultsorting",
        "pubId desc");
    if (StringUtil.isDefined(sortedBy)) {
      publication_default_sorting = sortedBy;
    }
    return getPublicationBm().getAllPublications(new PublicationPK("useless", getComponentId()),
        publication_default_sorting);
  }

  public Collection<PublicationDetail> getAllPublicationsByTopic(PublicationPK pubPK,
      List<String> fatherIds)
      throws RemoteException {
    Collection<PublicationDetail> result = getPublicationBm().
        getDetailsByFatherIdsAndStatus((ArrayList<String>) fatherIds, pubPK,
        "P.pubUpdateDate desc, P.pubId desc", PublicationDetail.VALID);
    SilverTrace.info("kmelia", "KmeliaSessionController.getAllPublicationsByTopic()",
        "root.MSG_PARAM_VALUE", "publis=" + result.toString());
    return result;
  }

  /**
   * Get all visible publications
   *
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
    getNodeBm().processWysiwyg(getNodePK(topicId));
  }

  /**
   * Si le mode brouillon est activé et que le classement PDC est possible alors une publication ne
   * peut sortir du mode brouillon que si elle est classée sur le PDC
   *
   * @return true si le PDC n'est pas utilisé ou si aucun axe n'est utilisé par le composant ou si
   * la publication est classée sur le PDC
   * @throws RemoteException
   */
  public boolean isPublicationTaxonomyOK() {
    if (!isPdcUsed() || getSessionPublication() == null || !isPDCClassifyingMandatory()) {
      // Classification is not used or mandatory so we don't care about the current classification of the content
      return true;
    }
    String pubId = getSessionPublication().getDetail().getPK().getId();
    return isPublicationClassifiedOnPDC(pubId);
  }

  public boolean isPublicationValidatorsOK() throws RemoteException {
    if (getSessionPublication() != null && SilverpeasRole.writer.isInRole(getUserTopicProfile())
        && (isTargetValidationEnable() || isTargetMultiValidationEnable())) {
      return StringUtil.isDefined(getSessionPublication().getDetail().getTargetValidatorId());
    }
    return true;
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

  public synchronized boolean validatePublication(String publicationId) throws RemoteException {
    boolean validationComplete = getKmeliaBm().validatePublication(getPublicationPK(publicationId),
        getUserId(), false);
    if (validationComplete) {
      setSessionClone(null);
      refreshSessionPubliAndClone();
    }
    return validationComplete;
  }

  public synchronized boolean forcePublicationValidation(String publicationId) throws
      RemoteException {
    return getKmeliaBm().validatePublication(getPublicationPK(publicationId), getUserId(), true);
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
    List<ValidationStep> steps = getPublicationBm().getValidationSteps(
        getSessionPubliOrClone().getDetail().getPK());

    // Get users who have already validate this publication
    List<String> validators = new ArrayList<String>();
    for (ValidationStep step : steps) {
      step.setUserFullName(getOrganisationController().getUserDetail(step.getUserId()).
          getDisplayedName());
      validators.add(step.getUserId());
    }

    List<String> allValidators = getKmeliaBm().getAllValidators(getSessionPubliOrClone().getDetail()
        .getPK());

    for (String allValidator : allValidators) {
      if (!validators.contains(allValidator)) {
        ValidationStep step = new ValidationStep();
        step.setUserFullName(getOrganisationController().getUserDetail(
            allValidator).getDisplayedName());
        steps.add(step);
      }
    }

    return steps;
  }

  public boolean isUserCanValidatePublication() throws RemoteException {
    return getKmeliaBm().isUserCanValidatePublication(getSessionPubliOrClone().getDetail().getPK(),
        getUserId());
  }

  public ValidationStep getValidationStep() throws RemoteException {
    if (getValidationType() == KmeliaHelper.VALIDATION_TARGET_N) {
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
      getKmeliaBm().draftOutPublication(getSessionPublication().getDetail().getPK(), null,
          getProfile());
    } else {
      getKmeliaBm().draftOutPublication(getSessionPublication().getDetail().getPK(),
          getCurrentFolderPK(), getProfile());
    }

    if (!KmeliaHelper.ROLE_WRITER.equals(getUserTopicProfile())) {
      setSessionClone(null); // always reset clone
    }
    refreshSessionPubliAndClone();
  }

  /**
   * Change publication status from any state to draft
   *
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

  private synchronized NotificationMetaData getAlertNotificationMetaData(String pubId) {
    NotificationMetaData metaData = null;
    if (isKmaxMode) {
      metaData = getKmeliaBm().getAlertNotificationMetaData(getPublicationPK(pubId), null,
          getUserDetail().getDisplayedName());
    } else {
      metaData = getKmeliaBm().getAlertNotificationMetaData(getPublicationPK(pubId),
          getCurrentFolderPK(), getUserDetail().getDisplayedName());
    }
    metaData.setSender(getUserId());
    return metaData;
  }

  private synchronized NotificationMetaData getAlertNotificationMetaData(String pubId,
      String attachmentId) {
    NodePK nodePK = null;
    if (!isKmaxMode) {
      nodePK = getCurrentFolderPK();
    }
    SimpleDocumentPK documentPk = new SimpleDocumentPK(attachmentId, getComponentId());
    NotificationMetaData metaData = getKmeliaBm().getAlertNotificationMetaData(
        getPublicationPK(pubId), documentPk, nodePK, getUserDetail().getDisplayedName());
    metaData.setSender(getUserId());
    return metaData;
  }

  public synchronized void indexKmelia() throws RemoteException {
    getKmeliaBm().indexKmelia(getComponentId());
  }

  public boolean isIndexable(PublicationDetail pubDetail) {
    return KmeliaHelper.isIndexable(pubDetail);
  }

  public Map<String, String> pasteFiles(PublicationPK pubPKFrom, String pubId)
      throws IOException {
    Map<String, String> fileIds = new HashMap<String, String>();
    String componentId = pubPKFrom.getInstanceId();
    List<SimpleDocument> origins = AttachmentServiceFactory.getAttachmentService().
        listDocumentsByForeignKeyAndType(pubPKFrom, DocumentType.attachment, getLanguage());
    for (SimpleDocument origin : origins) {
      SimpleDocumentPK copyPk = AttachmentServiceFactory.getAttachmentService().copyDocument(
          origin, new ForeignPK(pubId, getComponentId()));
      fileIds.put(origin.getId(), copyPk.getId());
    }

    return fileIds;
  }

  /**
   * adds links between specified publication and other publications contained in links parameter
   *
   * @param pubId publication which you want removes the external link
   * @param links list of links to remove
   * @return the number of links created
   * @throws RemoteException
   */
  public int addPublicationsToLink(String pubId, HashSet<String> links) throws RemoteException {
    List<ForeignPK> infoLinks = new ArrayList<ForeignPK>();
    for (String link : links) {
      StringTokenizer tokens = new StringTokenizer(link, "/");
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
      setCurrentFolderId(topicDetail.getNodePK().getId(), true);
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
    setSessionPublicationsList(publications, true);
  }

  private void setSessionPublicationsList(List<KmeliaPublication> publications, boolean sort) {
    this.sessionPublicationsList = (publications == null ? null : new ArrayList<KmeliaPublication>(
        publications));
    if (sort) {
      orderPubs();
    }
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

  public String getCurrentFolderId() {
    return currentFolderId;
  }

  public NodePK getCurrentFolderPK() {
    return new NodePK(getCurrentFolderId(), getSpaceId(), getComponentId());
  }

  public NodeDetail getCurrentFolder() throws RemoteException {
    return getNodeHeader(getCurrentFolderId());
  }

  public void setCurrentFolderId(String id, boolean resetSessionPublication) {
    if (!id.equals(currentFolderId) && !KmeliaHelper.SPECIALFOLDER_TOVALIDATE.equalsIgnoreCase(id)) {
      indexOfFirstPubToDisplay = 0;
      resetSelectedPublicationIds();
      setSearchContext(null);
      Collection<NodeDetail> pathColl = getTopicPath(id);
      String linkedPathString = displayPath(pathColl, true, 3);
      String pathString = displayPath(pathColl, false, 3);
      setSessionPath(linkedPathString);
      setSessionPathString(pathString);
    }
    if (resetSessionPublication) {
      setSessionPublication(null);
    }
    currentFolderId = id;
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

  public String initUPToSelectValidator(String pubId) throws RemoteException {
    String m_context = URLManager.getApplicationURL();
    PairObject hostComponentName = new PairObject(getComponentLabel(), "");
    PairObject[] hostPath = new PairObject[1];
    hostPath[0] = new PairObject(getString("kmelia.SelectValidator"), "");
    String hostUrl = m_context + URLManager.getURL("useless", getComponentId())
        + "SetValidator?PubId="
        + pubId;
    String cancelUrl = m_context + URLManager.getURL("useless", getComponentId())
        + "SetValidator?PubId="
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
    profiles.add(SilverpeasRole.publisher.toString());
    profiles.add(SilverpeasRole.admin.toString());

    NodeDetail node = getNodeHeader(getCurrentFolderId());
    boolean haveRights = isRightsOnTopicsEnabled() && node.haveRights();
    if (haveRights) {
      sug.setObjectId(ObjectType.NODE.getCode() + node.getRightsDependsOn());
    }
    sug.setProfileNames((ArrayList<String>) profiles);

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

  public String initAlertUserAttachment(String attachmentOrDocumentId) throws RemoteException {
    initAlertUser();
    AlertUser sel = getAlertUser();
    String pubId = getSessionPublication().getDetail().getPK().getId();
    sel.setNotificationMetaData(getAlertNotificationMetaData(pubId, attachmentOrDocumentId));
    return AlertUser.getAlertUserURL();
  }

  public void toRecoverUserId() {
    Selection sel = getSelection();
    idSelectedUser = SelectionUsersGroups.getDistinctUserIds(sel.getSelectedElements(), sel
        .getSelectedSets());
  }

  public boolean isVersionControlled() {
    return StringUtil.getBooleanValue(getComponentParameterValue("versionControl"));
  }

  public boolean isVersionControlled(String anotherComponentId) {
    String strVersionControlled = getOrganisationController().getComponentParameterValue(
        anotherComponentId, "versionControl");
    return StringUtil.getBooleanValue(strVersionControlled);

  }

  /**
   * @param pubId
   * @return
   * @throws RemoteException
   */
  public boolean isWriterApproval(String pubId) throws RemoteException {
    return false;
  }

  public boolean isTargetValidationEnable() {
    return String.valueOf(KmeliaHelper.VALIDATION_TARGET_1).equalsIgnoreCase(
        getComponentParameterValue(InstanceParameters.validation));
  }

  public boolean isTargetMultiValidationEnable() {
    return String.valueOf(KmeliaHelper.VALIDATION_TARGET_N).equalsIgnoreCase(
        getComponentParameterValue(InstanceParameters.validation));
  }

  public boolean isCollegiateValidationEnable() {
    return String.valueOf(KmeliaHelper.VALIDATION_COLLEGIATE).equalsIgnoreCase(
        getComponentParameterValue(InstanceParameters.validation));
  }

  public boolean isValidationTabVisible() {
    boolean tabVisible = PublicationDetail.TO_VALIDATE.equalsIgnoreCase(getSessionPubliOrClone()
        .getDetail().
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
    return StringUtil.getBooleanValue(getComponentParameterValue(InstanceParameters.coWriting));
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

  private boolean isPublicationClassifiedOnPDC(String pubId) {
    if (pubId != null && pubId.length() > 0) {
      try {
        int silverObjectId = getKmeliaBm().getSilverObjectId(getPublicationPK(pubId));
        List<ClassifyPosition> positions = getPdcBm().getPositions(silverObjectId,
            getComponentId());
        return !positions.isEmpty();
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaSessionController.isPublicationClassifiedOnPDC()",
            SilverpeasRuntimeException.ERROR, "kmelia.MSG_ERR_GENERAL", e);
      }
    }
    return false;
  }

  public boolean isCurrentPublicationHaveContent() throws WysiwygException {
    return (getSessionPublication().getCompleteDetail().getModelDetail() != null
        || StringUtil.isDefined(WysiwygController.load(getComponentId(), getSessionPublication().
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
    try {
      return EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBm.class);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.getNodeBm()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_NODEBM_HOME", e);
    }
  }

  public PublicationBm getPublicationBm() {
    try {
      return EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME, PublicationBm.class);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.getPublicationBm()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_NODEBM_HOME", e);
    }
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
   * @throws ImportExportException
   */
  public List<PublicationDetail> importFile(File fileUploaded, String fileType, String topicId,
      String importMode, boolean draftMode, int versionType) throws ImportExportException {
    SilverTrace.debug("kmelia", "KmeliaSessionController.importFile()",
        "root.MSG_GEN_ENTER_METHOD", "fileUploaded = " + fileUploaded.getAbsolutePath()
        + " fileType=" + fileType + " importMode=" + importMode + " draftMode=" + draftMode
        + " versionType=" + versionType);
    List<PublicationDetail> publicationDetails = null;
    FileImport fileImport = new FileImport(this, fileUploaded);
    boolean draft = draftMode;
    if (isDraftEnabled() && isPDCClassifyingMandatory()) {
      // classifying on PDC is mandatory, set publication in draft mode
      draft = true;
    }
    fileImport.setVersionType(versionType);
    if (UNITARY_IMPORT_MODE.equals(importMode)) {
      publicationDetails = fileImport.importFile(draft);
    } else if (MASSIVE_IMPORT_MODE_ONE_PUBLICATION.equals(importMode)
        && FileUtil.isArchive(fileUploaded.getName())) {
      publicationDetails = fileImport.importFiles(draft);
    } else if (MASSIVE_IMPORT_MODE_MULTI_PUBLICATIONS.equals(importMode)
        && FileUtil.isArchive(fileUploaded.getName())) {
      publicationDetails = fileImport.importFilesMultiPubli(draft);
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
   *
   * @param pk
   * @return true or false
   */
  public boolean isPublicationDeleted(PublicationPK pk) {
    boolean isPublicationDeleted = false;
    try {
      Collection<Collection<NodeDetail>> pathList = getPathList(pk);
      SilverTrace.debug("kmelia", "KmeliaSessionController.isPublicationDeleted()",
          "root.MSG_GEN_PARAM_VALUE", "pathList = " + pathList);
      if (pathList.size() == 1) {
        for (Collection<NodeDetail> path : pathList) {
          for (NodeDetail nodeInPath : path) {
            SilverTrace.debug("kmelia", "KmeliaSessionController.isPublicationDeleted()",
                "root.MSG_GEN_PARAM_VALUE", "nodeInPath = " + nodeInPath);
            if (nodeInPath.getNodePK().isTrash()) {
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
    String objectId = getCurrentFolderId();
    getKmeliaBm().addModelUsed(models, getComponentId(), objectId);
  }

  public Collection<String> getModelUsed() {
    String objectId = getCurrentFolderId();
    return getKmeliaBm().getModelUsed(getComponentId(), objectId);
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
   *
   * @return
   */
  public boolean isTimeAxisUsed() {
    return StringUtil.getBooleanValue(getComponentParameterValue("timeAxisUsed"));
  }

  /**
   * Parameter for fields visibility of the publication
   *
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
      ResourceLocator timeSettings = new ResourceLocator(
          "com.stratelia.webactiv.kmelia.multilang.timeAxisBundle",
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
    this.sessionPublicationsList = new ArrayList<KmeliaPublication>(getKmeliaBm()
        .search(combination, getComponentId()));
    applyVisibilityFilter();
    return getSessionPublicationsList();
  }

  public synchronized List<KmeliaPublication> search(List<String> combination, int nbDays)
      throws RemoteException {
    this.sessionPublicationsList = new ArrayList<KmeliaPublication>(getKmeliaBm()
        .search(combination, nbDays,
        getComponentId()));
    applyVisibilityFilter();
    return getSessionPublicationsList();
  }

  public synchronized List<KmeliaPublication> getUnbalancedPublications() throws RemoteException {
    return (List<KmeliaPublication>) getKmeliaBm().getUnbalancedPublications(getComponentId());
  }

  public synchronized NodePK addPosition(String fatherId, NodeDetail position)
      throws RemoteException {
    SilverTrace.info("kmax", "KmeliaSessionController.addPosition()", "root.MSG_GEN_PARAM_VALUE",
        "fatherId = " + fatherId + " And position = " + position);
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
   *
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
   *
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
   *
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
   *
   * @return previous publication id
   */
  public String getPrevious() {
    return getNearPublication(-1);
  }

  /**
   * getNext
   *
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
    String m_context = URLManager.getApplicationURL();
    PairObject[] hostPath = new PairObject[1];
    hostPath[0] = new PairObject(getString("kmelia.SelectValidator"), "");

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(getSpaceLabel());
    sel.setHostComponentName(new PairObject(getComponentLabel(), ""));
    sel.setHostPath(hostPath);

    String hostUrl = m_context + URLManager.getURL("useless", getComponentId())
        + "TopicProfileSetUsersAndGroups?Role=" + role + "&NodeId=" + nodeId;
    String cancelUrl = m_context + URLManager.getURL("useless", getComponentId()) + "CloseWindow";

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    List<ProfileInst> profiles = getAdmin().getProfilesByObject(nodeId, ObjectType.NODE.getCode(),
        getComponentId());
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
      List<ProfileInst> profiles = getAdmin().getProfilesByObject(node.getNodePK().getId(),
          ObjectType.NODE.getCode(),
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
    List<ProfileInst> profiles = getAdmin().getProfilesByObject(topicId, ObjectType.NODE.getCode(),
        getComponentId());
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
    return getTopicProfile(role, getCurrentFolderId());
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
    return getTopicProfiles(getCurrentFolderId());
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
    return getKmeliaBm().isUserCanValidate(getComponentId(), getUserId());
  }

  public boolean isUserCanWrite() throws RemoteException {
    return getKmeliaBm().isUserCanWrite(getComponentId(), getUserId());
  }

  public void copyPublication(String pubId) throws ClipboardException, RemoteException {
    CompletePublication pub = getCompletePublication(pubId);
    PublicationSelection pubSelect = new PublicationSelection(pub);
    SilverTrace.info("kmelia", "KmeliaSessionController.copyPublication()",
        "root.MSG_GEN_PARAM_VALUE", "clipboard = " + getClipboardName() + "' count="
        + getClipboardCount());
    addClipboardSelection(pubSelect);
  }

  private void copyPublications(List<String> pubIds) throws ClipboardException, RemoteException {
    for (String pubId : pubIds) {
      if (StringUtil.isDefined(pubId)) {
        copyPublication(pubId);
      }
    }
  }

  public void copySelectedPublications() throws ClipboardException, RemoteException {
    copyPublications(getSelectedPublicationIds());
  }

  public void cutPublication(String pubId) throws ClipboardException, RemoteException {
    CompletePublication pub = getCompletePublication(pubId);
    PublicationSelection pubSelect = new PublicationSelection(pub);
    pubSelect.setCutted(true);

    SilverTrace.info("kmelia", "KmeliaSessionController.cutPublication()",
        "root.MSG_GEN_PARAM_VALUE",
        "clipboard = " + getClipboardName() + "' count=" + getClipboardCount());
    addClipboardSelection(pubSelect);
  }

  private void cutPublications(List<String> pubIds) throws ClipboardException, RemoteException {
    for (String pubId : pubIds) {
      if (StringUtil.isDefined(pubId)) {
        cutPublication(pubId);
      }
    }
  }

  public void cutSelectedPublications() throws ClipboardException, RemoteException {
    cutPublications(getSelectedPublicationIds());
  }

  public void copyTopic(String id) throws ClipboardException, RemoteException {
    NodeSelection nodeSelect = new NodeSelection(getNodeHeader(id));

    SilverTrace.info("kmelia", "KmeliaSessionController.copyTopic()", "root.MSG_GEN_PARAM_VALUE",
        "clipboard = " + getClipboardName() + "' count=" + getClipboardCount());
    addClipboardSelection(nodeSelect);
  }

  public void cutTopic(String id) throws ClipboardException, RemoteException {
    NodeSelection nodeSelect = new NodeSelection(getNodeHeader(id));
    nodeSelect.setCutted(true);
    SilverTrace.info("kmelia", "KmeliaSessionController.cutTopic()", "root.MSG_GEN_PARAM_VALUE",
        "clipboard = " + getClipboardName() + "' count=" + getClipboardCount());
    addClipboardSelection(nodeSelect);
  }

  public List<Object> paste() throws ClipboardException, RemoteException {
    return paste(getCurrentFolderId());
  }

  public List<Object> paste(String nodeId) throws ClipboardException, RemoteException {
    resetSelectedPublicationIds();
    return paste(getNodeHeader(nodeId));
  }

  private List<Object> paste(NodeDetail folder) throws ClipboardException {
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
            pastePublication(pub, clipObject.isCutted(), folder.getNodePK(), null);
            pastedItems.add(pub.getPublicationDetail());
          } else if (clipObject.isDataFlavorSupported(NodeSelection.NodeDetailFlavor)) {
            NodeDetail node = (NodeDetail) clipObject.getTransferData(
                NodeSelection.NodeDetailFlavor);
            // check if current topic is a subTopic of node
            boolean pasteAllowed = true;
            if (getComponentId().equals(node.getNodePK().getInstanceId())) {
              if (node.getNodePK().getId().equals(folder.getNodePK().getId())) {
                pasteAllowed = false;
              }
              String nodePath = node.getPath() + node.getId() + "/";
              String currentPath = folder.getPath() + folder.getNodePK().getId() + "/";
              SilverTrace.info("kmelia", "KmeliaRequestRooter.paste()", "root.MSG_GEN_PARAM_VALUE",
                  "nodePath = " + nodePath + ", currentPath = " + currentPath);
              if (pasteAllowed && currentPath.startsWith(nodePath)) {
                pasteAllowed = false;
              }
            }
            if (pasteAllowed) {
              NodeDetail newNode = pasteNode(node, folder, clipObject.isCutted());
              pastedItems.add(newNode);
            }
          }
        }
      }
    } catch (ClipboardException e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.paste()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_PASTE_ERROR", e);
    } catch (UnsupportedFlavorException e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.paste()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_PASTE_ERROR", e);
    } catch (RemoteException e) {
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
          try {
            NodePK fromForeignPK = fromNode.getNodePK();
            List<SimpleDocument> documents = AttachmentServiceFactory.getAttachmentService().
                listAllDocumentsByForeignKey(fromForeignPK, getLanguage());
            ForeignPK toForeignPK = new ForeignPK(toNodePK.getId(), getComponentId());
            for (SimpleDocument document : documents) {
              AttachmentServiceFactory.getAttachmentService().moveDocument(document, toForeignPK);
            }
          } catch (org.silverpeas.attachment.AttachmentException e) {
            SilverTrace.error("kmelia", "KmeliaSessionController.pastePublication()",
                "root.MSG_GEN_PARAM_VALUE", "kmelia.CANT_MOVE_ATTACHMENTS", e);
          }
          // change images path in wysiwyg
          WysiwygController.wysiwygPlaceHaveChanged(fromNode.getNodePK().getInstanceId(),
              "Node_" + fromNode.getNodePK().getId(), getComponentId(), "Node_" + toNodePK.getId());
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
      WysiwygController.copy(nodeToPastePK.getInstanceId(), "Node_" + nodeToPastePK.getId(),
          getComponentId(), "Node_" + nodePK.getId(), getUserId());

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
      List<NodePK> nodePKsToPaste) {
    Collection<PublicationDetail> publications = getPublicationBm().getDetailsByFatherPK(fromPK);
    CompletePublication completePubli = null;
    for (PublicationDetail publi : publications) {
      completePubli = getPublicationBm().getCompletePublication(publi.getPK());
      pastePublication(completePubli, isCutted, toPK, nodePKsToPaste);
    }
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
        currentNodePK = getCurrentFolderPK();
      }

      if (isCutted) {
        if (fromComponentId.equals(getComponentId())) {
          getKmeliaBm().movePublicationInSameApplication(publi, currentNodePK, getUserId());
        } else {
          movePublication(completePub, currentNodePK, publi, fromId, fromComponentId,
              fromForeignPK, fromPubPK, toForeignPK, toPubPK, imagesSubDirectory,
              thumbnailsSubDirectory, toAbsolutePath, fromAbsolutePath);
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
        ThumbnailDetail vignette = ThumbnailController.getCompleteThumbnail(new ThumbnailDetail(
            fromComponentId,
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
        WysiwygController.copy(fromComponentId, fromId, getComponentId(), id, getUserId());
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
            List< SimpleDocument> images = AttachmentServiceFactory.getAttachmentService()
                .listDocumentsByForeignKeyAndType(fromPubPK, DocumentType.form, getLanguage());
            for (SimpleDocument image : images) {
              SimpleDocumentPK copyPk = AttachmentServiceFactory.getAttachmentService()
                  .copyDocument(image, toForeignPK);
              fileIds.put(image.getId(), copyPk.getId());
            }
            // Paste wysiwyg fields content
            WysiwygFCKFieldDisplayer wysiwygField = new WysiwygFCKFieldDisplayer();
            wysiwygField.cloneContents(fromComponentId, fromId, getComponentId(), id);

            // get xmlContent to paste
            PublicationTemplate pubTemplateFrom = getPublicationTemplateManager().
                getPublicationTemplate(fromComponentId + ":" + xmlFormShortName);
            IdentifiedRecordTemplate recordTemplateFrom = (IdentifiedRecordTemplate) pubTemplateFrom
                .getRecordSet().getRecordTemplate();
            PublicationTemplate pubTemplate = getPublicationTemplateManager().
                getPublicationTemplate(getComponentId() + ":" + xmlFormShortName);
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
   * Move a publication to another component. Moving in this order : <ul>
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
   *
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
      String fromAbsolutePath) throws RemoteException, PublicationTemplateException, PdcException,
      IOException {
    boolean indexIt = false;
    // move Vignette on disk
    int[] thumbnailSize = getThumbnailWidthAndHeight();
    String vignette = ThumbnailController.getImage(fromComponentId, Integer.parseInt(fromId),
        ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE, thumbnailSize[0],
        thumbnailSize[1]);
    if (StringUtil.isDefined(vignette)) {
      moveThumbnail(fromAbsolutePath, thumbnailsSubDirectory, vignette, toAbsolutePath);
    }
    movePublicationContent(fromForeignPK, toForeignPK, fromComponentId, publi);
    movePublicationDocuments(fromComponentId, fromForeignPK, toForeignPK, fromPubPK, publi, indexIt,
        toPubPK);

    // eventually, paste the model content
    if (completePub.getModelDetail() != null && completePub.getInfoDetail() != null) {
      // Move images of model
      if (completePub.getInfoDetail().getInfoImageList() != null) {
        for (InfoImageDetail attachment : completePub.getInfoDetail().getInfoImageList()) {
          String from = fromAbsolutePath + imagesSubDirectory + File.separator + attachment.
              getPhysicalName();
          String to = toAbsolutePath + imagesSubDirectory + File.separator + attachment.
              getPhysicalName();
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
        IdentifiedRecordTemplate recordTemplateFrom = (IdentifiedRecordTemplate) pubTemplateFrom
            .getRecordSet().getRecordTemplate();

        PublicationTemplate pubTemplate = getPublicationTemplateManager().getPublicationTemplate(
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
    // Careful! positions must be moved according to taxonomy restrictions of target application
    int fromSilverObjectId = getKmeliaBm().getSilverObjectId(fromPubPK);
    // get positions of cutted publication
    List<ClassifyPosition> positions = getPdcBm().
        getPositions(fromSilverObjectId, fromComponentId);

    // delete taxonomy data relative to cutted publication
    getKmeliaBm().deleteSilverContent(fromPubPK);
    // move statistics
    getStatisticBm().moveStat(toForeignPK, 1, "Publication");
    // move publication itself
    getKmeliaBm().movePublicationInAnotherApplication(publi, nodePK, getUserId());

    if (indexIt) {
      getPublicationBm().createIndex(toPubPK);
    }
    // reference pasted publication on taxonomy service
    int toSilverObjectId = getKmeliaBm().getSilverObjectId(toPubPK);
    // add original positions to pasted publication
    getPdcBm().addPositions(positions, toSilverObjectId, getComponentId());
  }

  /**
   * get languages of publication header and attachments
   *
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
    List< String> attLanguages = getLanguagesOfAttachments(new ForeignPK(pubPK.getId(), pubPK.
        getInstanceId()));
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
    List<Alias> aliases = (List<Alias>) getKmeliaBm().getAlias(
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
  public List<Treeview> getComponents(List<Alias> aliases) throws RemoteException {
    List<String> instanceIds = new ArrayList<String>();
    List<Treeview> result = new ArrayList<Treeview>();
    String instanceId = null;
    List<NodeDetail> tree = null;
    NodePK root = new NodePK(NodePK.ROOT_NODE_ID);

    List<SpaceInstLight> spaces = getOrganisationController().getSpaceTreeview(getUserId());
    for (SpaceInstLight space : spaces) {
      String path = "";
      String[] componentIds = getOrganisationController().getAvailCompoIdsAtRoot(
          space.getFullId(), getUserId());
      for (String componentId : componentIds) {
        instanceId = componentId;

        if (instanceId.startsWith("kmelia")) {
          String[] profiles = getOrganisationController().getUserProfiles(getUserId(), instanceId);
          String bestProfile = KmeliaHelper.getProfile(profiles);
          if (SilverpeasRole.admin.isInRole(bestProfile) || SilverpeasRole.publisher.isInRole(
              bestProfile) || instanceId.equals(getComponentId())) {
            instanceIds.add(instanceId);
            root.setComponentName(instanceId);

            if (instanceId.equals(getComponentId())) {
              tree = getKmeliaBm().getTreeview(root, "useless", false, false, getUserId(),
                  false, StringUtil.getBooleanValue(getOrganisationController().
                  getComponentParameterValue(instanceId, "rightsOnTopics")));
            }

            if (!StringUtil.isDefined(path)) {
              List<SpaceInst> sPath = getOrganisationController().getSpacePath(space.getFullId());
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
                + getOrganisationController().getComponentInstLight(instanceId).getLabel(),
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
    return result;
  }

  public List<NodeDetail> getAliasTreeview() throws RemoteException {
    return getAliasTreeview(getComponentId());
  }

  public List<NodeDetail> getAliasTreeview(String instanceId) throws RemoteException {
    String[] profiles = getOrganisationController().getUserProfiles(getUserId(), instanceId);
    String bestProfile = KmeliaHelper.getProfile(profiles);
    List<NodeDetail> tree = null;
    if ("admin".equalsIgnoreCase(bestProfile) || "publisher".equalsIgnoreCase(bestProfile)) {
      NodePK root = new NodePK(NodePK.ROOT_NODE_ID, instanceId);

      tree = getKmeliaBm().getTreeview(root, "useless", false, false, getUserId(), false,
          StringUtil.getBooleanValue(getOrganisationController().getComponentParameterValue(
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
   *
   * @return the url to the first attached file for the curent publication.
   * @throws RemoteException
   */
  public String getFirstAttachmentURLOfCurrentPublication() throws RemoteException {
    PublicationPK pubPK = getSessionPublication().getDetail().getPK();
    String url = null;
    List< SimpleDocument> attachments = AttachmentServiceFactory.getAttachmentService().
        listDocumentsByForeignKey(pubPK, getLanguage());
    if (!attachments.isEmpty()) {
      url = URLManager.getApplicationURL() + attachments.get(0).getLastPublicVersion().
          getAttachmentURL();
    }
    return url;
  }

  /**
   * Return the url to access the file
   *
   * @param fileId the id of the file (attachment or document id).
   * @return the url to the file.
   * @throws RemoteException
   */
  public String getAttachmentURL(String fileId) throws RemoteException {
    SimpleDocument attachment = AttachmentServiceFactory.getAttachmentService().
        searchDocumentById(new SimpleDocumentPK(fileId), getLanguage());
    return URLManager.getApplicationURL() + attachment.getLastPublicVersion().getAttachmentURL();
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
        currentId = getCurrentFolderId();
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

    File descriptorFile = new File(getUpdateChainDescriptorFilename(getCurrentFolderId()));
    UpdateChainDescriptor updateChainDescriptor = (UpdateChainDescriptor) xstream.fromXML(
        new FileReader(descriptorFile));

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
      String subDirPath = FileRepositoryManager.getTemporaryPath() + subdir + File.separator
          + fileName;
      FileFolderManager.createFolder(subDirPath);
      // generate from the publication a document in PDF
      if (isFormatSupported(DocumentFormat.pdf.name())) {
        pdf = generateDocument(DocumentFormat.pdf, pubPK.getId());
        // copy pdf into zip
        FileRepositoryManager.copyFile(pdf.getPath(), subDirPath + File.separator + pdf.getName());
      }
      // copy files
      new AttachmentImportExport().getAttachments(pubPK, subDirPath, "useless", null);
      new VersioningImportExport(getUserDetail()).
          exportDocuments(pubPK, subDirPath, "useless", null);
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
    }
    return "yes".equals(parameterValue.toLowerCase());
  }

  public boolean isWysiwygOnTopicsEnabled() {
    return "yes".equals(getComponentParameterValue("wysiwygOnTopics").toLowerCase());
  }

  public String getWysiwygOnTopic(String id) {
    String currentId = id;
    if (isWysiwygOnTopicsEnabled()) {
      if (!StringUtil.isDefined(currentId)) {
        currentId = getCurrentFolderId();
      }
      return WysiwygController.load(getComponentId(), "Node_" + currentId, getLanguage());
    }
    return "";
  }

  public String getWysiwygOnTopic() {
    return getWysiwygOnTopic(null);
  }

  public List<NodeDetail> getTopicPath(String topicId) {
    try {
      List<NodeDetail> pathInReverse = (List<NodeDetail>) getNodeBm().getPath(new NodePK(topicId,
          getComponentId()));
      Collections.reverse(pathInReverse);
      return pathInReverse;
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getTopicPath()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DAVOIR_LE_CHEMIN_COURANT", e);
    }
  }

  public int[] getThumbnailWidthAndHeight() {
    int widthInt = getLengthFromXMLDescriptor("thumbnailWidthSize");
    int heightInt = getLengthFromXMLDescriptor("thumbnailHeightSize");

    if (widthInt == -1 && heightInt == -1) {
      // 2ième chance si nécessaire
      widthInt = getLengthFromProperties("vignetteWidth");
      heightInt = getLengthFromProperties("vignetteHeight");
    }

    return new int[]{widthInt, heightInt};
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
   *
   * @return an indentifier of Pdc axis
   */
  public String getAxisIdGlossary() {
    return getComponentParameterValue("axisIdGlossary");
  }

  public List<ComponentInstLight> getGalleries() {
    List<ComponentInstLight> galleries = null;
    OrganisationController orgaController = OrganisationControllerFactory.
        getOrganisationController();
    String[] compoIds = orgaController.getCompoId("gallery");
    for (String compoId : compoIds) {
      if (StringUtil.getBooleanValue(orgaController.getComponentParameterValue("gallery" + compoId,
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
    while (iterator.hasNext()) {
      NodeDetail nodeInPath = iterator.next();
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
    if (linked) {
      return linkedPathString.toString();
    } else {
      return pathString.toString();
    }
  }

  /**
   * Is search in topics enabled
   *
   * @return boolean
   */
  public boolean isSearchOnTopicsEnabled() {
    return StringUtil.getBooleanValue(getComponentParameterValue("searchOnTopics").toLowerCase());
  }

  public boolean isAttachmentsEnabled() {
    return StringUtil.getBooleanValue(getComponentParameterValue(TAB_ATTACHMENTS));
  }

  /**
   * Get publications and aliases of this topic and its subtopics answering to the query
   *
   * @param query
   * @return List of Kmelia publications
   */
  public synchronized List<KmeliaPublication> search(String query) {

    SearchContext previousSearch = getSearchContext();
    boolean newSearch = previousSearch == null || !previousSearch.getQuery().equalsIgnoreCase(query);
    if (!newSearch) {
      // process cached results
      return getSessionPublicationsList();
    }

    // Insert this new search inside persistence layer in order to compute statistics
    TopicSearch newTS = new TopicSearch(getComponentId(), Integer.parseInt(getCurrentFolderId()),
        Integer.parseInt(getUserId()), getLanguage(), query.toLowerCase(), new Date());
    KmeliaSearchServiceFactory.getTopicSearchService().createTopicSearch(newTS);

    List<KmeliaPublication> userPublications = new ArrayList<KmeliaPublication>();
    QueryDescription queryDescription = new QueryDescription(query);
    queryDescription.setSearchingUser(getUserId());
    String[] componentIds = getOrganisationController().getComponentIdsForUser(getUserId(),
        getComponentName());
    for (String componentId : componentIds) {
      queryDescription.addComponent(componentId);
    }

    try {

      List<MatchingIndexEntry> results = SearchEngineFactory.getSearchEngine().search(
          queryDescription).getEntries();
      PublicationDetail pubDetail = new PublicationDetail();
      pubDetail.setPk(new PublicationPK("unknown"));
      KmeliaPublication publication = KmeliaPublication.aKmeliaPublicationFromDetail(pubDetail);

      // get visible publications in the topic and sub-topics
      List<WAAttributeValuePair> pubsInPath = getAllVisiblePublicationsByTopic(getCurrentFolderId());

      // Store all descendant topicIds of this topic
      List<NodePK> nodeIDs = new ArrayList<NodePK>();

      // Get current topic too
      nodeIDs.add(getCurrentFolderPK());
      Collection<NodePK> nodePKs = getNodeBm().getDescendantPKs(getCurrentFolderPK());
      nodeIDs.addAll(nodePKs);

      List<String> pubIds = new ArrayList<String>();
      KmeliaSecurity security = new KmeliaSecurity();
      security.enableCache();
      for (MatchingIndexEntry result : results) {
        try {
          if ("Publication".equals(result.getObjectType())) {
            pubDetail.getPK().setId(result.getObjectId());

            PublicationPK pubPK = new PublicationPK(result.getObjectId(), result.getComponent());
            Collection<Alias> pubAliases = getKmeliaBm().getAlias(pubPK);

            // Add the alias which have a link to the targets topics
            for (Alias alias : pubAliases) {
              if (!alias.getInstanceId().equals(pubPK.getInstanceId())) {
                if (nodeIDs.contains(new NodePK(alias.getId(), alias.getInstanceId()))) {
                  if (!pubIds.contains(pubDetail.getId())) {
                    pubIds.add(pubDetail.getId());
                  }
                }
              }
            }

            // Add the publications
            WAAttributeValuePair pubWAFound = new WAAttributeValuePair(pubDetail.getId(), result
                .getComponent());
            int index = pubsInPath.indexOf(pubWAFound);
            if (index != -1) {
              // Add only if not yet in the returned results
              if (!pubIds.contains(pubDetail.getId())) {
                // return publication if user can consult it only (check rights on folder)
                if (security.isObjectAvailable(getComponentId(), getUserId(), pubDetail.getPK()
                    .getId(), "Publication")) {
                  pubIds.add(pubDetail.getId());
                }
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

    // store "in session" current search context
    SearchContext searchContext = new SearchContext(query);
    setSearchContext(searchContext);

    // store results and keep search results order
    setSessionPublicationsList(userPublications, false);

    return userPublications;
  }

  /**
   * @return the list of SpaceInst from current space identifier (in session) to root space <br/>
   * (all the subspace)
   */
  public List<SpaceInst> getSpacePath() {
    return this.getOrganisationController().getSpacePath(this.getSpaceId());
  }

  /**
   * Gets an instance of PublicationTemplateManager.
   *
   * @return an instance of PublicationTemplateManager.
   */
  private PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }

  public List<PublicationTemplate> getForms() {
    List<PublicationTemplate> templates = new ArrayList<PublicationTemplate>();
    try {
      GlobalContext context = new GlobalContext(getSpaceId(), getComponentId());
      templates = getPublicationTemplateManager().getPublicationTemplates(context);
    } catch (PublicationTemplateException e) {
      SilverTrace.error("kmelia", "KmeliaSessionController.getForms()", "root.CANT_GET_FORMS", e);
    }
    return templates;
  }

  /**
   * Is news manage
   *
   * @return boolean
   */
  public boolean isNewsManage() {
    return StringUtil.getBooleanValue(getComponentParameterValue("isNewsManage"));
  }

  /**
   * Récupère une actualité déléguée dans le composant delegatednews correspondant à la publication
   * passée en paramètre
   *
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
   *
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
    String[] tabInstanceId = getOrganisationController().getCompoId("delegatednews");
    String delegatednewsInstanceId = null;
    for (String aTabInstanceId : tabInstanceId) {
      delegatednewsInstanceId = aTabInstanceId;
      break;
    }
    delegatedNewsService.notifyDelegatedNewsToValidate(pubId, pubDetail.getName(this.getLanguage()),
        this.getUserId(), this.getUserDetail().getDisplayedName(), delegatednewsInstanceId);
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
   *
   * @return a list of export formats Silverpeas supports for export.
   */
  public List<String> getAvailableFormats() {
    return Arrays.asList(AVAILABLE_EXPORT_FORMATS);
  }

  /**
   * Gets the export formats that are supported by the current Kmelia component instance. As some of
   * the export formats can be deactivated in the Kmelia settings file, this method returns all the
   * formats that are currently active.
   *
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
   *
   * @param format a recognized export format.
   * @return true if the specified format is currently supported for the publication export, false
   * otherwise.
   */
  public boolean isFormatSupported(String format) {
    return getSupportedFormats().contains(format);
  }

  /**
   * Is the specified publication classified on the PdC.
   *
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
   *
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

  public List<KmeliaPublication> getLatestPublications() throws RemoteException {
    List<KmeliaPublication> publicationsToDisplay = new ArrayList<KmeliaPublication>();
    List<KmeliaPublication> toCheck = getKmeliaBm().getLatestPublications(getComponentId(),
        getNbPublicationsOnRoot(),
        isRightsOnTopicsEnabled(), getUserId());
    for (KmeliaPublication aPublication : toCheck) {
      if (!isPublicationDeleted(aPublication.getPk())) {
        publicationsToDisplay.add(aPublication);
      }
    }
    return publicationsToDisplay;
  }

  public List<KmeliaPublication> getPublicationsOfCurrentFolder() throws RemoteException {
    List<KmeliaPublication> publications = null;
    if (!KmeliaHelper.SPECIALFOLDER_TOVALIDATE.equalsIgnoreCase(currentFolderId)) {
      publications = getKmeliaBm().getPublicationsOfFolder(new NodePK(currentFolderId,
          getComponentId()),
          getUserTopicProfile(currentFolderId), getUserId(), isTreeStructure(),
          isRightsOnTopicsEnabled());
    } else {
      publications = getKmeliaBm().getPublicationsToValidate(getComponentId(), getUserId());
    }
    setSessionPublicationsList(publications);
    applyVisibilityFilter();
    return getSessionPublicationsList();
  }

  public String getContentLanguage() {
    if (getPublicationLanguages().contains(getCurrentLanguage())) {
      return getCurrentLanguage();
    }
    return I18NHelper.checkLanguage(getSessionPublication().getDetail().getLanguage());
  }

  private void setSearchContext(SearchContext searchContext) {
    this.searchContext = searchContext;
  }

  public SearchContext getSearchContext() {
    return searchContext;
  }

  private void moveThumbnail(String fromAbsolutePath, String thumbnailsSubDirectory, String vignette,
      String toAbsolutePath) {
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

  private void movePublicationContent(ForeignPK fromForeignPK, ForeignPK toForeignPK,
      String fromComponentId, PublicationDetail publi) {
    try {
      // Change instanceId and move files
      List<SimpleDocument> documents = AttachmentServiceFactory.getAttachmentService().
          listDocumentsByForeignKeyAndType(fromForeignPK, DocumentType.form, getLanguage());
      documents.addAll(AttachmentServiceFactory.getAttachmentService().
          listDocumentsByForeignKeyAndType(fromForeignPK, DocumentType.image, getLanguage()));
      documents.addAll(AttachmentServiceFactory.getAttachmentService().
          listDocumentsByForeignKeyAndType(fromForeignPK, DocumentType.wysiwyg, getLanguage()));
      for (SimpleDocument doc : documents) {
        AttachmentServiceFactory.getAttachmentService().moveDocument(doc, toForeignPK);
      }
    } catch (org.silverpeas.attachment.AttachmentException e) {
      SilverTrace.error("kmelia", "KmeliaSessionController.pastePublication()",
          "root.MSG_GEN_PARAM_VALUE", "kmelia.CANT_MOVE_ATTACHMENTS", e);
    }
    // change images path in wysiwyg
    WysiwygController.wysiwygPlaceHaveChanged(fromComponentId, publi.getPK().getId(),
        getComponentId(), publi.getPK().getId());
  }

  private void movePublicationDocuments(String fromComponentId, ForeignPK fromForeignPK,
      ForeignPK toForeignPK, PublicationPK fromPubPK, PublicationDetail publi, boolean indexIt,
      PublicationPK toPubPK) throws IOException {
    List<SimpleDocument> docs = AttachmentServiceFactory.getAttachmentService().
        listDocumentsByForeignKeyAndType(fromForeignPK, DocumentType.attachment, getLanguage());
    for (SimpleDocument doc : docs) {
      AttachmentServiceFactory.getAttachmentService().moveDocument(doc, toForeignPK);
    }
  }

  public String manageSubscriptions() {
    SubscriptionContext subscriptionContext = getSubscriptionContext();
    List<NodeDetail> nodePath = getTopicPath(getCurrentFolderId());
    nodePath.remove(0);
    subscriptionContext
        .initializeFromNode(NodeSubscriptionResource.from(getCurrentFolderPK()), nodePath);
    return subscriptionContext.getDestinationUrl();
  }
}
