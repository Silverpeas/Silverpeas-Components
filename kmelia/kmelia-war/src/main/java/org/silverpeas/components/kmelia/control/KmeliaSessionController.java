/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.components.kmelia.control;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.owasp.encoder.Encode;
import org.silverpeas.components.kmelia.FileImport;
import org.silverpeas.components.kmelia.InstanceParameters;
import org.silverpeas.components.kmelia.KmeliaCopyDetail;
import org.silverpeas.components.kmelia.KmeliaPasteDetail;
import org.silverpeas.components.kmelia.KmeliaPublicationHelper;
import org.silverpeas.components.kmelia.SearchContext;
import org.silverpeas.components.kmelia.Sort;
import org.silverpeas.components.kmelia.export.ExportFileNameProducer;
import org.silverpeas.components.kmelia.export.KmeliaPublicationExporter;
import org.silverpeas.components.kmelia.model.*;
import org.silverpeas.components.kmelia.search.KmeliaSearchServiceProvider;
import org.silverpeas.components.kmelia.service.KmeliaHelper;
import org.silverpeas.components.kmelia.service.KmeliaNodeSimulationElementLister;
import org.silverpeas.components.kmelia.service.KmeliaPublicationBatchSimulationElementLister;
import org.silverpeas.components.kmelia.service.KmeliaPublicationSimulationElementLister;
import org.silverpeas.components.kmelia.service.KmeliaService;
import org.silverpeas.components.kmelia.service.KmeliaXmlFormUpdateContext;
import org.silverpeas.core.ActionType;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.ProfiledObjectType;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.GlobalContext;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.clipboard.ClipboardException;
import org.silverpeas.core.clipboard.ClipboardSelection;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.Attachments;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.converter.DocumentFormat;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.core.contribution.publication.datereminder.PublicationNoteReference;
import org.silverpeas.core.contribution.publication.model.CompletePublication;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.model.PublicationSelection;
import org.silverpeas.core.contribution.publication.model.ValidationStep;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.contribution.publication.subscription.PublicationAliasSubscriptionResource;
import org.silverpeas.core.contribution.publication.subscription.PublicationSubscriptionResource;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateImpl;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.datereminder.exception.DateReminderException;
import org.silverpeas.core.datereminder.persistence.DateReminderDetail;
import org.silverpeas.core.datereminder.persistence.PersistentResourceDateReminder;
import org.silverpeas.core.datereminder.persistence.service.DateReminderServiceProvider;
import org.silverpeas.core.datereminder.persistence.service.PersistentDateReminderService;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.importexport.ExportDescriptor;
import org.silverpeas.core.importexport.attachment.AttachmentImportExport;
import org.silverpeas.core.importexport.model.ImportExportException;
import org.silverpeas.core.importexport.report.ComponentReport;
import org.silverpeas.core.importexport.report.ImportReport;
import org.silverpeas.core.importexport.report.MassiveReport;
import org.silverpeas.core.importexport.report.UnitReport;
import org.silverpeas.core.index.indexing.model.FieldDescription;
import org.silverpeas.core.index.indexing.model.IndexManager;
import org.silverpeas.core.index.search.model.MatchingIndexEntry;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.io.media.image.thumbnail.ThumbnailSettings;
import org.silverpeas.core.io.upload.UploadedFile;
import org.silverpeas.core.node.coordinates.model.Coordinate;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.model.NodeSelection;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;
import org.silverpeas.core.pdc.pdc.service.PdcClassificationService;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.process.annotation.SimulationActionProcessProcessor;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.NodeAccessControl;
import org.silverpeas.core.security.authorization.PublicationAccessControl;
import org.silverpeas.core.silverstatistics.access.model.HistoryObjectDetail;
import org.silverpeas.core.silverstatistics.access.service.StatisticService;
import org.silverpeas.core.subscription.SubscriptionResource;
import org.silverpeas.core.subscription.service.ComponentSubscriptionResource;
import org.silverpeas.core.subscription.service.NodeSubscriptionResource;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.core.util.*;
import org.silverpeas.core.util.error.SilverpeasTransverseErrorUtil;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.selection.SelectionUsersGroups;
import org.silverpeas.core.web.subscription.SubscriptionContext;
import org.silverpeas.core.web.subscription.SubscriptionResourcePath;
import org.silverpeas.core.webapi.pdc.PdcClassificationEntity;

import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.silverpeas.components.kmelia.control.KmeliaSessionController.CLIPBOARD_STATE.*;
import static org.silverpeas.components.kmelia.export.KmeliaPublicationExporter.*;
import static org.silverpeas.core.admin.component.model.ComponentInst.getComponentLocalId;
import static org.silverpeas.core.cache.service.CacheServiceProvider.getSessionCacheService;
import static org.silverpeas.core.cache.service.VolatileIdentifierProvider.newVolatileIntegerIdentifierOn;
import static org.silverpeas.core.contribution.attachment.AttachmentService.VERSION_MODE;
import static org.silverpeas.core.index.search.SearchEngineProvider.getSearchEngine;
import static org.silverpeas.core.pdc.pdc.model.PdcClassification.NONE_CLASSIFICATION;
import static org.silverpeas.core.pdc.pdc.model.PdcClassification.aPdcClassificationOfContent;

public class KmeliaSessionController extends AbstractComponentSessionController
    implements ExportFileNameProducer {
  private static final long serialVersionUID = -1986747239669483105L;

  /**
   * The different export formats the KmeliaPublicationExporter should support.
   */
  private static final String EXPORT_FORMATS = "kmelia.export.formats.active";
  /**
   * All the formats that are available for the export of publications.
   */
  private static final String[] AVAILABLE_EXPORT_FORMATS = {"zip", "pdf", "odt", "doc"};
  private static final int DEFAULT_NBPUBLIS_PER_PAGE = 10;
  private static final String INACCESSIBLE_ATTACHMENT = "liaisonInaccessible";
  private static final String KMELIA = "kmelia";
  private static final String PUBLICATION = "Publication";
  private static final String USELESS = "useless";

  // Session objects
  private TopicDetail sessionTopic = null;
  private String currentFolderId = NodePK.ROOT_NODE_ID;
  private KmeliaPublication sessionPublication = null;
  private KmeliaPublication sessionClone = null;
  // sessionPath html link with <a href="">
  private String sessionPath = null;
  // sessionPathString html string only
  private String sessionPathString = null;
  private TopicDetail sessionTopicToLink = null;
  private boolean sessionOwner = false;
  // Specific Kmax
  private List<String> sessionCombination = null;
  // Specific Kmax
  private String sessionTimeCriteria = null;
  private Sort sortValue;
  private int rang = 0;
  public static final String TAB_PREVIEW = "tabpreview";
  public static final String TAB_HEADER = "tabheader";
  public static final String TAB_CONTENT = "tabcontent";
  public static final String TAB_COMMENT = "tabcomments";
  public static final String TAB_ATTACHMENTS = "tabattachments";
  public static final String TAB_SEE_ALSO = "tabseealso";
  public static final String TAB_ACCESS_PATHS = "tabaccesspaths";
  public static final String TAB_READER_LIST = "tabreaderslist";
  // For import files
  public static final String UNITARY_IMPORT_MODE = "0";
  public static final String MASSIVE_IMPORT_MODE_ONE_PUBLICATION = "1";
  public static final String MASSIVE_IMPORT_MODE_MULTI_PUBLICATIONS = "2";
  // Versioning options
  public static final String VER_USE_WRITERS_AND_READERS = "0";
  // utilisation de userPanel/ userpanelPeas
  String[] idSelectedUser = null;
  // pagination de la liste des publications
  private int indexOfFirstPubToDisplay = 0;
  private int nbPublicationsPerPage = DEFAULT_NBPUBLIS_PER_PAGE;
  // Specific for Kmax
  private List<Integer> timeAxis = null;
  private List<String> currentCombination = null;
  private boolean isKmaxMode = false;
  // i18n
  private String currentLanguage = null;
  boolean isDragAndDropEnableByUser = false;
  boolean componentManageable = false;
  private List<PublicationPK> selectedPublicationPKs = new ArrayList<>();
  private boolean customPublicationTemplateUsed = false;
  private String customPublicationTemplateName = null;
  private SearchContext searchContext = null;

  // select/deselect all
  private boolean allPublicationsListSelected = false;

  /**
   * Creates new sessionClientController
   * @param mainSessionCtrl
   * @param context
   */
  public KmeliaSessionController(MainSessionController mainSessionCtrl, ComponentContext context) {
    super(mainSessionCtrl, context, "org.silverpeas.kmelia.multilang.kmeliaBundle",
        "org.silverpeas.kmelia.settings.kmeliaIcons",
        "org.silverpeas.kmelia.settings.kmeliaSettings");
    init();
  }

  public static List<String> getLanguagesOfAttachments(ResourceReference resourceReference) {
    List<String> languages = new ArrayList<>();
    for (String availableLanguage : I18NHelper.getAllSupportedLanguages()) {
      List<SimpleDocument> attachments = AttachmentServiceProvider.getAttachmentService()
          .listDocumentsByForeignKeyAndType(resourceReference, DocumentType.attachment, availableLanguage);
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
    sortValue = new Sort();
    sortValue.setCurrentSort(getDefaultSortValue());

    // Remove all data store by this SessionController
    removeSessionObjects();
    currentLanguage = getLanguage();
    if (StringUtil.getBooleanValue(getSettings().getString("massiveDragAndDropAllowed"))) {
      isDragAndDropEnableByUser = isDragAndDropEnableByUser();
    }
    componentManageable = ResourceLocator.getGeneralSettingBundle().getBoolean(
        "AdminFromComponentEnable", true);
    if (componentManageable) {
      componentManageable =
          getOrganisationController().isComponentManageable(getComponentId(), getUserId());
    }
    // check if this instance use a specific template of publication
    SilverpeasTemplate template = SilverpeasTemplateFactory.createSilverpeasTemplateOnComponents();
    customPublicationTemplateName = "publication_" + getComponentId();
    customPublicationTemplateUsed =
        template.isCustomTemplateExists(KMELIA, customPublicationTemplateName);
    cacheDirectlyPublicationsListInSession(Collections.emptyList());

    nbPublicationsPerPage = getSettings().getInteger("NbPublicationsParPage", 10);
    String parameterValue = getComponentParameterValue("nbPubliPerPage");
    if (StringUtil.isInteger(parameterValue)) {
      nbPublicationsPerPage = Integer.parseInt(parameterValue);
    }
  }

  private int getDefaultSortValue() {
    String defaultSortValue = getComponentParameterValue("publicationSort");
    if (!StringUtil.isDefined(defaultSortValue)) {
      defaultSortValue = getSettings().getString("publications.sort.default", "2");
    }
    return Integer.parseInt(defaultSortValue);
  }

  public boolean isKmaxMode() {
    return isKmaxMode;
  }

  public KmeliaSessionController setKmaxMode(final boolean kmaxMode) {
    isKmaxMode = kmaxMode;
    return this;
  }

  public KmeliaService getKmeliaService() {
    return KmeliaService.get();
  }

  public StatisticService getStatisticService() {
    return StatisticService.get();
  }

  /**
  * Gets a business service of dateReminder.
  *
  * @return a DefaultDateReminderService instance.
  */
  protected PersistentDateReminderService getDateReminderService() {
    return DateReminderServiceProvider.getDateReminderService();
  }

  public int getNbPublicationsOnRoot() {
    int nbPublicationsOnRoot = 0;
    String parameterValue = getComponentParameterValue("nbPubliOnRoot");
    if (StringUtil.isDefined(parameterValue)) {
      nbPublicationsOnRoot = Integer.parseInt(parameterValue);
    } else {
      if (KmeliaHelper.isKmelia(getComponentId())) {
        // lecture du properties
        nbPublicationsOnRoot = getSettings().getInteger("HomeNbPublications", 15);
      }
    }
    return nbPublicationsOnRoot;
  }

  public int getNbPublicationsPerPage() {
    return nbPublicationsPerPage;
  }

  public void setNbPublicationsPerPage(int nb) {
    nbPublicationsPerPage = nb;
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

  public boolean isReminderUsed() {
    return StringUtil.getBooleanValue(getComponentParameterValue("useReminder"));
  }

  public boolean isStatisticAllowed() {
    boolean statisticEnabled = getSettings().getBoolean("kmelia.stats.enable", false);
    return statisticEnabled && !isToolbox() &&
        (getHighestSilverpeasUserRole().isGreaterThanOrEquals(SilverpeasRole.PUBLISHER) ||
        getSilverpeasUserRoles().contains(SilverpeasRole.SUPERVISOR));
  }

  public boolean openSingleAttachmentAutomatically() {
    return StringUtil.getBooleanValue(getComponentParameterValue("openSingleAttachment"));
  }

  public boolean isImportFileAllowed() {
    String parameterValue = getComponentParameterValue("importFiles");
    if (!StringUtil.isDefined(parameterValue)) {
      return false;
    } else {
      return "1".equalsIgnoreCase(parameterValue) || "3".equalsIgnoreCase(parameterValue);
    }
  }

  public boolean isImportFilesAllowed() {
    String parameterValue = this.getComponentParameterValue("importFiles");
    if (parameterValue == null || parameterValue.length() <= 0) {
      return false;
    } else {
      return "2".equalsIgnoreCase(parameterValue) || "3".equalsIgnoreCase(parameterValue);
    }
  }

  public boolean isExportZipAllowed() {
    String parameterValue = this.getComponentParameterValue("exportComponent");
    if (parameterValue == null || parameterValue.length() <= 0) {
      return false;
    } else {
      return StringUtil.getBooleanValue(parameterValue) || "both".equalsIgnoreCase(parameterValue);
    }
  }

  public boolean isExportPdfAllowed() {
    String parameterValue = this.getComponentParameterValue("exportComponent");
    if (parameterValue == null || parameterValue.length() <= 0) {
      return false;
    } else {
      return "pdf".equalsIgnoreCase(parameterValue) || "both".equalsIgnoreCase(parameterValue);
    }
  }

  public boolean isExportComponentAllowed() {
    return StringUtil.getBooleanValue(getSettings().getString("exportComponentAllowed"));
  }

  public boolean isExportAllowedToUsers() {
    return getSettings().getBoolean("export.allowed.users", false);
  }

  public boolean isMassiveDragAndDropAllowed() {
    return StringUtil.getBooleanValue(getComponentParameterValue("massiveDragAndDrop"));
  }

  public boolean isPublicationAlwaysVisibleEnabled() {
    return StringUtil.getBooleanValue(getComponentParameterValue("publicationAlwaysVisible"));
  }

  public boolean displayNbPublis() {
    return StringUtil
        .getBooleanValue(getComponentParameterValue(InstanceParameters.displayNbItemsOnFolders));
  }

  public boolean isRightsOnTopicsEnabled() {
    return StringUtil.
        getBooleanValue(getComponentParameterValue(InstanceParameters.rightsOnFolders));
  }

  private boolean isRightsOnTopicsEnabled(String componentId) {
    return StringUtil.getBooleanValue(getOrganisationController()
        .getComponentParameterValue(componentId, InstanceParameters.rightsOnFolders));
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

  public boolean isContentEnabled() {
    String parameterValue = getComponentParameterValue("tabContent");
    if (!StringUtil.isDefined(parameterValue)) {
      return false;
    }
    return StringUtil.getBooleanValue(parameterValue);
  }

  public boolean isSeeAlsoEnabled() {
    return StringUtil.getBooleanValue(getComponentParameterValue("tabSeeAlso"));
  }

  public boolean isPublicationRatingAllowed() {
    return StringUtil.getBooleanValue(getComponentParameterValue("publicationRating"));
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

  public boolean isLastVisitorsEnabled() {
    String parameterValue = getComponentParameterValue("tabLastVisitors");
    if (!StringUtil.isDefined(parameterValue)) {
      return true;
    }
    return StringUtil.getBooleanValue(parameterValue);
  }

  public List<String> getInvisibleTabs() {
    List<String> invisibleTabs = new ArrayList<>(0);

    if (!isContentEnabled()) {
      invisibleTabs.add(TAB_CONTENT);
    }

    if (isToolbox()) {
      invisibleTabs.add(TAB_PREVIEW);
    }

    String parameterValue = this.getComponentParameterValue("tabAttachments");
    if (!isToolbox() && StringUtil.isDefined(parameterValue) &&
        !StringUtil.getBooleanValue(parameterValue)) {
      // attachments tab is always visible with toolbox
      invisibleTabs.add(TAB_ATTACHMENTS);
    }

    if (!isSeeAlsoEnabled()) {
      invisibleTabs.add(TAB_SEE_ALSO);
    }

    parameterValue = this.getComponentParameterValue("tabAccessPaths");
    if (StringUtil.isDefined(parameterValue) &&
        !StringUtil.getBooleanValue(parameterValue)) {
      invisibleTabs.add(TAB_ACCESS_PATHS);
    }

    parameterValue = this.getComponentParameterValue("tabReadersList");
    if (StringUtil.isDefined(parameterValue) &&
        !StringUtil.getBooleanValue(parameterValue)) {
      invisibleTabs.add(TAB_READER_LIST);
    }

    parameterValue = this.getComponentParameterValue("tabComments");
    if (!StringUtil.isDefined(parameterValue)) {
      invisibleTabs.add(TAB_COMMENT);
    } else {
      if (!StringUtil.getBooleanValue(parameterValue)) {
        invisibleTabs.add(TAB_COMMENT);
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

    if (!isFormatSupported(inFormat.name())) {
      throw new KmeliaRuntimeException("Document format " + inFormat.name() + " not yet supported");
    }
    File document = null;
    if (fromPubId != null) {
      try {
        KmeliaPublication publication = KmeliaPublication.withPK(
            new PublicationPK(fromPubId, getComponentId()), getCurrentFolderPK());
        String fileName = getPublicationExportFileName(publication, getLanguage());
        document = new File(FileRepositoryManager.getTemporaryPath() + fileName + "." + inFormat.
            name());
        FileOutputStream output = new FileOutputStream(document);
        ExportDescriptor descriptor = ExportDescriptor.withOutputStream(output).
            withParameter(EXPORT_FOR_USER, getUserDetail()).
            withParameter(EXPORT_LANGUAGE, getLanguage()).
            withParameter(EXPORT_TOPIC, getCurrentFolderId()).
            inMimeType(inFormat.name());
        aKmeliaPublicationExporter().exports(descriptor, () -> publication);
      } catch (Exception ex) {
        SilverLogger.getLogger(this).error("Publication export failure", ex);
        if (document != null) {
          FileUtils.deleteQuietly(document);
        }
        throw new KmeliaRuntimeException(ex);
      }
    }
    return document;
  }

  /**
   * Gets a new exporter of Kmelia publications.
   * @return a KmeliaPublicationExporter instance.
   */
  public static KmeliaPublicationExporter aKmeliaPublicationExporter() {
    return ServiceProvider.getSingleton(KmeliaPublicationExporter.class);
  }

  @Override
  public SilverpeasRole getHighestSilverpeasUserRole() {
    SilverpeasRole userRole = SilverpeasRole.fromString(getProfile());
    return userRole != null ? userRole : super.getHighestSilverpeasUserRole();
  }

  public String getProfile() {
    return getUserTopicProfile();
  }

  public String getUserTopicProfile() {
    return getUserTopicProfile(null);
  }

  public String getUserTopicProfile(String id) {
    String nodeId = id;
    if (!StringUtil.isDefined(id)) {
      nodeId = getCurrentFolderId();
      if (!KmeliaHelper.isSpecialFolder(nodeId)) {
        try {
          // check that current node still exists
          getKmeliaService().getNodeHeader(nodeId, getComponentId());
        } catch (Exception e) {
          SilverLogger.getLogger(this).warn(e);
          setCurrentFolderId(NodePK.ROOT_NODE_ID, true);
          nodeId = NodePK.ROOT_NODE_ID;
        }
      }
    }
    return getKmeliaService().getUserTopicProfile(getNodePK(nodeId), getUserId());
  }

  public List<String> getUserIdsOfTopic() {
    return getKmeliaService().getUserIdsOfFolder(getCurrentFolderPK());
  }

  public boolean isCurrentTopicAvailable() {
    if (isRightsOnTopicsEnabled()) {
      if (KmeliaHelper.isSpecialFolder(getCurrentFolderId())) {
        return true;
      }
      return NodeAccessControl.get().isUserAuthorized(getUserId(), getCurrentFolderPK());
    }
    return true;
  }

  public boolean isUserComponentAdmin() {
    return SilverpeasRole.ADMIN.isInRole(KmeliaHelper.getSilverpeasRole(getSilverpeasUserRoles()));
  }

  /*
   * Topic management
   */
  public synchronized TopicDetail getTopic(String id) {
    return getTopic(id, true);
  }

  public synchronized TopicDetail getTopic(String id, boolean resetSessionItems) {
    if (resetSessionItems) {
      setSessionPublication(null);
    }
    if (!id.equals(getCurrentFolderId())) {
      indexOfFirstPubToDisplay = 0;
    }

    TopicDetail currentTopic;
    if (isUserComponentAdmin()) {
      currentTopic =
          getKmeliaService().goTo(getNodePK(id), getUserId(), isTreeStructure(), "admin", false);
    } else {
      currentTopic = getKmeliaService()
          .goTo(getNodePK(id), getUserId(), isTreeStructure(), getUserTopicProfile(id),
              isRightsOnTopicsEnabled());
    }

    if (displayNbPublis()) {
      prepareForPublicationNbDisplaying(currentTopic);
    }
    if (resetSessionItems) {
      setSessionTopic(currentTopic);
    }
    applyVisibilityFilter();
    return currentTopic;
  }

  private void prepareForPublicationNbDisplaying(final TopicDetail currentTopic) {
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

  public List<NodeDetail> getTreeview(String nodeId) {
    if (isUserComponentAdmin()) {
      return getKmeliaService().getTreeview(getNodePK(nodeId), "admin", isCoWritingEnable(),
          isDraftVisibleWithCoWriting(), getUserId(), displayNbPublis(), false);
    } else {
      return getKmeliaService().getTreeview(getNodePK(nodeId), getProfile(), isCoWritingEnable(),
          isDraftVisibleWithCoWriting(), getUserId(), displayNbPublis(), isRightsOnTopicsEnabled());
    }
  }

  public synchronized TopicDetail getBestTopicDetailsOfPublication(String pubId) {
    final TopicDetail currentTopic = getKmeliaService()
        .getBestTopicDetailOfPublicationForUser(getPublicationPK(pubId), isTreeStructure(), getUserId());
    setSessionTopic(currentTopic);
    applyVisibilityFilter();
    return currentTopic;
  }

  public synchronized List<NodeDetail> getAllTopics() {
    return getNodeService().getSubTree(getNodePK(NodePK.ROOT_NODE_ID));
  }

  public synchronized void flushTrashCan() {

    TopicDetail td = getKmeliaService()
        .goTo(getNodePK(NodePK.BIN_NODE_ID), getUserId(), false, getUserTopicProfile("1"),
            isRightsOnTopicsEnabled());
    setSessionTopic(td);
    Collection<KmeliaPublication> pds = td.getKmeliaPublications();
    Iterator<KmeliaPublication> ipds = pds.iterator();

    while (ipds.hasNext()) {
      String theId = (ipds.next()).getDetail().getPK().getId();

      deletePublication(theId);
    }
    indexOfFirstPubToDisplay = 0;
  }

  public synchronized NodePK updateTopicHeader(NodeDetail nd, String alertType) {
    nd.getNodePK().setSpace(getSpaceId());
    nd.getNodePK().setComponentName(getComponentId());
    if (isTopicAdmin(nd.getNodePK().getId())) {
      nd.setCreatorId(getUserId());
      nd.setCreationDate(new Date());
      final NodePK updatedNodePK = getKmeliaService().updateTopic(nd, alertType);
      if (updatedNodePK.getId().equals(getCurrentFolderId())) {
        processBreadcrumb(getCurrentFolderId());
      }
      return updatedNodePK;
    }
    SilverLogger.getLogger(this).warn("Security alert from {0}", getUserId());
    return null;
  }

  public synchronized NodeDetail getSubTopicDetail(String subTopicId) {
    return getKmeliaService().getSubTopicDetail(getNodePK(subTopicId));
  }

  public synchronized NodePK addSubTopic(NodeDetail nd, String alertType, String parentId) {
    nd.getNodePK().setSpace(getSpaceId());
    nd.getNodePK().setComponentName(getComponentId());
    nd.setCreatorId(getUserId());
    NodePK pk = getKmeliaService().addSubTopic(getNodePK(parentId), nd, alertType);

    // by default, setting father's admin rights
    // to preventing no more access
    if (nd.haveRights()) {
      ProfileInst parentProfile;
      String profileAdmin = SilverpeasRole.ADMIN.getName();
      if (NodePK.ROOT_NODE_ID.equals(parentId)) {
        parentProfile = getProfile(profileAdmin);
      } else {
        parentProfile = getTopicProfile(profileAdmin, parentId);
      }
      if (parentProfile != null) {
        updateTopicRole(profileAdmin, pk.getId(),
            parentProfile.getAllGroups().toArray(new String[0]),
            parentProfile.getAllUsers().toArray(new String[0]));
      }
    }

    return pk;
  }

  public synchronized String deleteTopic(String topicId) {
    if (NodePK.ROOT_NODE_ID.equals(topicId) || NodePK.BIN_NODE_ID.equals(topicId)) {
      return null;
    }
    NodeDetail node = getNodeHeader(topicId);
    // check if user is allowed to delete this topic
    if (SilverpeasRole.ADMIN.isInRole(getUserTopicProfile(topicId)) ||
        SilverpeasRole.ADMIN.isInRole(getUserTopicProfile(NodePK.ROOT_NODE_ID)) ||
        SilverpeasRole.ADMIN.isInRole(getUserTopicProfile(node.getFatherPK().getId()))) {
      // First, remove rights on topic and its descendants
      List<NodeDetail> treeview = getNodeService().getSubTree(getNodePK(topicId));
      for (NodeDetail nodeToDelete : treeview) {
        deleteTopicRoles(nodeToDelete);
      }
      // Then, remove the topic itself
      getKmeliaService().deleteTopic(getNodePK(topicId));

      return node.getFatherPK().getId();
    }
    return null;
  }

  public synchronized void changeTopicStatus(String newStatus, String topicId,
      boolean recursiveChanges) {
    getKmeliaService().changeTopicStatus(newStatus, getNodePK(topicId), recursiveChanges);
  }

  /**
   * @param pubId a publication identifier
   * @return
   */
  public synchronized PublicationDetail getPublicationDetail(String pubId) {
    return getKmeliaService().getPublicationDetail(getPublicationPK(pubId));
  }

  private Collection<Collection<NodeDetail>> getPathList(PublicationPK pk) {
    return getKmeliaService().getPathList(pk);
  }

  public NodePK getBestAllowedPublicationFather(String pubId) {
    return getKmeliaService().getBestLocationOfPublicationForUser(getPublicationPK(pubId), getUserId());
  }

  public synchronized String createPublication(PublicationDetail pubDetail,
      final PdcClassificationEntity classification) {
    pubDetail.setCreatorId(getUserId());
    pubDetail.setCreationDate(new Date());

    setCurrentLanguage(pubDetail.getLanguage());

    String result;
    if (isKmaxMode) {
      result = getKmeliaService().createKmaxPublication(pubDetail);
    } else {
      if (classification.isUndefined()) {
        result = getKmeliaService().createPublicationIntoTopic(pubDetail, getCurrentFolderPK());
      } else {
        List<PdcPosition> pdcPositions = classification.getPdcPositions();
        PdcClassification withClassification =
            aPdcClassificationOfContent(pubDetail).withPositions(pdcPositions);
        result = getKmeliaService()
            .createPublicationIntoTopic(pubDetail, getCurrentFolderPK(), withClassification);
      }
    }
    return result;
  }

  /**
   * attach uploaded files to the specified publication
   *
   * @param uploadedFiles list of uploaded files
   * @param pubDetail publication on which you want to attach files
   */
  public synchronized void addUploadedFilesToPublication(Collection<UploadedFile> uploadedFiles,
      PublicationDetail pubDetail) {
    Attachments.from(uploadedFiles).attachTo(LocalizedContribution.from(pubDetail,
        pubDetail.getLanguage()));
  }

  public synchronized void updatePublication(PublicationDetail pubDetail) {
    pubDetail.getPK().setSpace(getSpaceId());
    pubDetail.getPK().setComponentName(getComponentId());
    pubDetail.setUpdaterId(getUserId());

    setCurrentLanguage(pubDetail.getLanguage());

    if (isCloneNeeded()) {
      clonePublication(pubDetail);
    } else {
      if (NodePK.BIN_NODE_ID.equals(getCurrentFolderId())) {
        // publication is in the trash can
        pubDetail.setIndexOperation(IndexManager.NONE);
      }

      if (getSessionClone() != null && getSessionClone().getId().equals(pubDetail.getId())) {
        // update the clone, clone stay in same status
        pubDetail.setStatusMustBeChecked(false);

        // clone must not be indexed
        pubDetail.setIndexOperation(IndexManager.NONE);
      }
      getKmeliaService().updatePublication(pubDetail);
    }
  }

  public boolean isCloneNeeded() {
    if (getSessionPublication() == null) {
      return false;
    }
    String currentStatus = getSessionPublication().getDetail().getStatus();
    return (isPublicationAlwaysVisibleEnabled() && "writer".equals(getUserTopicProfile()) &&
        (getSessionClone() == null) && PublicationDetail.VALID_STATUS.equals(currentStatus));
  }

  public boolean isCloneNeededWithDraft() {
    return (isPublicationAlwaysVisibleEnabled() && (getSessionClone() == null));
  }

  public String clonePublication() {
    return clonePublication(null);
  }

  public String clonePublication(PublicationDetail pubDetail) {
    String cloneStatus = PublicationDetail.TO_VALIDATE_STATUS;
    if (isDraftEnabled()) {
      cloneStatus = PublicationDetail.DRAFT_STATUS;
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
      cloneId = getKmeliaService().clonePublication(refPubComplete, pubDetail, nextStatus);
      setSessionClone(getPublication(cloneId));
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
    return cloneId;
  }

  public synchronized void deletePublication(String pubId) {
    getKmeliaService()
        .deletePublications(Collections.singletonList(pubId), getCurrentFolderPK(), getUserId());
  }

  public List<String> deleteSelectedPublications() {
    List<String> removed = getKmeliaService()
        .deletePublications(getLocalSelectedPublicationIds(), getCurrentFolderPK(), getUserId());
    resetSelectedPublicationPKs();
    return removed;
  }

  private List<String> getLocalSelectedPublicationIds() {
    List<String> ids = new ArrayList<>();
    for (PublicationPK pubPK : getSelectedPublicationPKs()) {
      if (pubPK != null && pubPK.getInstanceId().equals(getComponentId())) {
        ids.add(pubPK.getId());
      }
    }
    return ids;
  }

  public synchronized void deleteClone() {
    if (getSessionClone() != null) {
      // delete clone
      getKmeliaService().deleteClone(getSessionClone().getPk());

      setSessionClone(null);
      refreshSessionPubliAndClone();
    }
  }

  private static boolean isInteger(String id) {
    return StringUtil.isInteger(id);
  }

  private void refreshSessionPubliAndClone() {
    final KmeliaPublication sessionPubClone = getSessionClone();
    // Master refresh
    final String pubId = getSessionPublication().getDetail().getPK().getId();
    setSessionPublication(getPublication(pubId));
    if (sessionPubClone != null) {
      // Clone refresh
      final String cloneId = sessionPubClone.getDetail().getPK().getId();
      setSessionClone(getPublication(cloneId));
    }
  }

  /**
   * adds links between specified publication and other publications contained in links parameter
   * @param pubId publication which you want removes the external link
   * @param links list of links to remove
   */
  private void addInfoLinks(String pubId, List<ResourceReference> links) {
    PublicationPK pubPK = getPublicationPK(pubId);
    getKmeliaService().addInfoLinks(pubPK, links);

    // reset current publication and its location
    NodePK nodePK = getKmeliaService().getPublicationFatherPK(pubPK);
    setCurrentFolderId(nodePK.getId(), true);

    KmeliaPublication completPub =
        getKmeliaService().getPublication(pubPK, getCurrentFolderPK());
    setSessionPublication(completPub);
  }

  public synchronized KmeliaPublication getPublication(String pubId) {
    return getPublication(pubId, false);
  }

  public synchronized KmeliaPublication getPublication(String pubId, boolean processIndex) {
    PublicationPK pubPK = getPublicationPK(pubId);
    // get publication
    KmeliaPublication publication = getKmeliaService().getPublication(pubPK, getCurrentFolderPK());
    PublicationDetail publicationDetail = publication.getDetail();

    ResourceReference resourceReference = new ResourceReference(pubId, getComponentId());
    if (!publicationDetail.getPK().getInstanceId().equals(getComponentId())) {
      // it's an alias
      resourceReference.setComponentName(publicationDetail.getPK().getInstanceId());
    }

    if (getSessionPublication() != null) {
      if (!pubId.equals(getSessionPublication().getId())) {
        // memorize the reading of the publication by the user
        getStatisticService().addStat(getUserId(), resourceReference, 1, PUBLICATION);
      }
    } else {
      getStatisticService().addStat(getUserId(), resourceReference, 1, PUBLICATION);
    }

    if (processIndex) {
      // getting rank of publication
      KmeliaPublication pub =
          KmeliaPublication.fromDetail(publicationDetail, getCurrentFolderPK());
      if (getSessionPublicationsList() != null) {
        rang = getSessionPublicationsList().indexOf(pub);
        if (rang != -1 && getSearchContext() != null) {
          getSessionPublicationsList().get(rang).setAsRead();
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

  public synchronized CompletePublication getCompletePublication(String pubId) {
    return getKmeliaService().getCompletePublication(getPublicationPK(pubId));
  }

  public synchronized void orderPubs() {
    orderPubs(getSortValue());
  }

  private void applyVisibilityFilter() {
    final List<KmeliaPublication> publications = getSessionPublicationsList();
    setSessionPublicationsList(getKmeliaService()
        .filterPublications(publications, getComponentId(), SilverpeasRole.fromString(getProfile()),
            getUserId()));
  }

  private synchronized void orderPubs(int sortType) {
    cacheDirectlyPublicationsListInSession(sort(getSessionPublicationsList(), sortType));
  }

  private List<KmeliaPublication> sort(Collection<KmeliaPublication> publications, int sortType) {
    if (publications == null) {
      return Collections.emptyList();
    }
    List<KmeliaPublication> publicationsToSort = new ArrayList<>(publications);

    int sort = sortType;
    boolean manualSortingUsed = isManualSortingUsed(publicationsToSort);

    if (manualSortingUsed && !getSort().isExplicitSort()) {
      // publications are sorted manually and another sort is not explicitly chosen by the user
      // so publications are displayed according to manual order defined by admin
      sort = Sort.SORT_MANUAL;
    } else if (getSortValue() == Sort.SORT_MANUAL) {
      // Current sort is manual but publications are not sorted manually
      // so publications are displayed according to default sort defined on application level
      // or instance level
      sort = getDefaultSortValue();
    }

    // store current sort
    this.sortValue.setCurrentSort(sort);
    this.sortValue.setManualSortEnable(manualSortingUsed);
    this.sortValue.setExplicitSort(false);

    switch (sort) {
      case Sort.SORT_CREATOR_ASC:
        Collections.sort(publicationsToSort, new PubliAuthorComparatorAsc());
        break;
      case Sort.SORT_UPDATE_ASC:
        Collections.sort(publicationsToSort, new PubliUpdateDateComparatorAsc());
        break;
      case Sort.SORT_UPDATE_DESC:
        Collections.sort(publicationsToSort, new PubliUpdateDateComparatorAsc());
        Collections.reverse(publicationsToSort);
        break;
      case Sort.SORT_IMPORTANCE_ASC:
        Collections.sort(publicationsToSort, new PubliImportanceComparatorDesc());
        break;
      case Sort.SORT_TITLE_ASC:
        publicationsToSort = sortByTitle(publicationsToSort);
        break;
      case Sort.SORT_CREATION_ASC:
        Collections.sort(publicationsToSort, new PubliCreationDateComparatorAsc());
        break;
      case Sort.SORT_CREATION_DESC:
        Collections.sort(publicationsToSort, new PubliCreationDateComparatorAsc());
        Collections.reverse(publicationsToSort);
        break;
      case Sort.SORT_DESCRIPTION_ASC:
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
    for (int i = pubs.length; --i >= 0; ) {
      boolean swapped = false;
      for (int j = 0; j < i; j++) {
        if (pubs[j].getDetail().getName(getCurrentLanguage())
            .compareToIgnoreCase(pubs[j + 1].getDetail().getName(getCurrentLanguage())) > 0) {
          KmeliaPublication pub = pubs[j];
          pubs[j] = pubs[j + 1];
          pubs[j + 1] = pub;
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
    for (int i = pubs.length; --i >= 0; ) {
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
          KmeliaPublication pub = pubs[j];
          pubs[j] = pubs[j + 1];
          pubs[j + 1] = pub;
          swapped = true;
        }
      }
      if (!swapped) {
        break;
      }
    }
    return Arrays.asList(pubs);
  }

  public void orderPublications(List<String> sortedPubIds) {
    getPublicationService().changePublicationsOrder(sortedPubIds, getCurrentFolderPK());
  }

  public void resetPublicationsOrder() {
    // reset order in database
    getPublicationService().resetPublicationsOrder(getCurrentFolderPK());
    // reload session cache
    getPublicationsOfCurrentFolder();
    // order publications according to default sort
    this.sortValue.setCurrentSort(getDefaultSortValue());
    orderPubs();
  }

  public Collection<PublicationDetail> getAllPublications() {
    return getAllPublications(null);
  }

  /**
   * Get all publications sorted
   * @param sortedBy (example: pubName asc)
   * @return Collection of Publications
   */
  public Collection<PublicationDetail> getAllPublications(String sortedBy) {
    String publicationDefaultSorting =
        getSettings().getString("publication_defaultsorting", "pubId desc");
    if (StringUtil.isDefined(sortedBy)) {
      publicationDefaultSorting = sortedBy;
    }
    return getPublicationService().getAllPublications(getComponentId(), publicationDefaultSorting);
  }

  public Collection<PublicationDetail> getAllPublicationsByTopic(String instanceId,
      List<String> fatherIds) {
    return getPublicationService().
        getDetailsByFatherIdsAndStatus(fatherIds, instanceId, "P.pubUpdateDate desc, P.pubId desc",
            PublicationDetail.VALID_STATUS);
  }

  /**
   * Get all visible publications
   * @return List of WAAtributeValuePair (Id and InstanceId)
   */
  public List<WAAttributeValuePair> getAllVisiblePublications() {
    List<WAAttributeValuePair> allVisiblesPublications = new ArrayList<>();
    Collection<PublicationDetail> allPublications = getAllPublications();

    for (PublicationDetail pubDetail : allPublications) {
      if (pubDetail.getStatus().equals(PublicationDetail.VALID_STATUS)) {
        allVisiblesPublications.add(new WAAttributeValuePair(pubDetail.getId(), pubDetail.
            getInstanceId()));
      }
    }
    return allVisiblesPublications;
  }

  public List<WAAttributeValuePair> getAllVisiblePublicationsByTopic(String topicId) {
    List<WAAttributeValuePair> allVisiblesPublications = new ArrayList<>();
    // récupérer la liste des sous thèmes de topicId
    List<String> fatherIds = new ArrayList<>();
    NodePK nodePK = new NodePK(topicId, getComponentId());
    List<NodeDetail> nodes = getNodeService().getSubTree(nodePK);
    for (NodeDetail node : nodes) {
      fatherIds.add(node.getId());
    }
    // création de pubPK
    Collection<PublicationDetail> allPublications =
        getAllPublicationsByTopic(getComponentId(), fatherIds);

    for (PublicationDetail pubDetail : allPublications) {
      if (pubDetail.getStatus().equals(PublicationDetail.VALID_STATUS)) {
        allVisiblesPublications.add(new WAAttributeValuePair(pubDetail.getId(), pubDetail.
            getInstanceId()));
      }
    }
    return allVisiblesPublications;
  }

  public List<WAAttributeValuePair> getAllPublicationsIds() {
    List<WAAttributeValuePair> allPublicationsIds = new ArrayList<>();
    Collection<PublicationDetail> allPublications = getAllPublications("pubName asc");
    for (PublicationDetail pubDetail : allPublications) {
      if (pubDetail.getStatus().equals(PublicationDetail.VALID_STATUS)) {
        allPublicationsIds
            .add(new WAAttributeValuePair(pubDetail.getId(), pubDetail.getInstanceId()));
      }
    }
    return allPublicationsIds;
  }

  public int getIndexOfFirstPubToDisplay() {
    if (getSearchContext() != null) {
      return getSearchContext().getPaginationIndex();
    }
    return indexOfFirstPubToDisplay;
  }

  public void setIndexOfFirstPubToDisplay(String index) {
    this.indexOfFirstPubToDisplay = Integer.parseInt(index);
  }

  public void processTopicWysiwyg(String topicId) {
    getNodeService().processWysiwyg(getNodePK(topicId));
  }

  /**
   * Si le mode brouillon est activé et que le classement PDC est possible alors une publication ne
   * peut sortir du mode brouillon que si elle est classée sur le PDC
   * @return true si le PDC n'est pas utilisé ou si aucun axe n'est utilisé par le composant ou si
   * la publication est classée sur le PDC
   */
  public boolean isPublicationTaxonomyOK() {
    if (!isPdcUsed() || getSessionPublication() == null || !isPDCClassifyingMandatory()) {
      // Classification is not used or mandatory so we don't care about the current
      // classification of the content
      return true;
    }
    String pubId = getSessionPublication().getDetail().getPK().getId();
    return isPublicationClassifiedOnPDC(pubId);
  }

  public boolean isPublicationValidatorsOK() {
    if (getSessionPubliOrClone() != null && SilverpeasRole.WRITER.isInRole(getUserTopicProfile()) &&
        (isTargetValidationEnable() || isTargetMultiValidationEnable())) {
      return CollectionUtil
          .isNotEmpty(getKmeliaService().getActiveValidatorIds(getSessionPubliOrClone().getPk()));
    }
    return true;
  }

  /**
   * Gets the authorized publications from given references into a context of read.
   * @param references the list of {@link ResourceReference} instance.
   * @return a collection of {@link KmeliaPublication} instance.
   */
  public synchronized <T extends ResourceReference> Collection<KmeliaPublication> getPublications(
      List<T> references) {
    return getKmeliaService().getPublications(references, getUserId(), getCurrentFolderPK(), true);
  }

  /**
   * Gets the authorized publications from given references into context of modification.
   * <p>
   *   Clones are handled.
   * </p>
   * @param references the list of {@link ResourceReference} instance.
   * @return a collection of pair of {@link KmeliaPublication} instances. On left the
   * publication, on right the optional clone.
   */
  public synchronized <T extends ResourceReference> Collection<Pair<KmeliaPublication,
      KmeliaPublication>> getPublicationsForModification(
      final List<T> references) {
    return getKmeliaService().getPublicationsForModification(references, getUserId());
  }

  public synchronized boolean validatePublication(String publicationId) {
    boolean validationComplete = getKmeliaService()
        .validatePublication(getPublicationPK(publicationId), getUserId(), false, false);
    if (validationComplete) {
      setSessionClone(null);
      refreshSessionPubliAndClone();
    }
    return validationComplete;
  }

  public synchronized boolean forcePublicationValidation(String publicationId) {
    return getKmeliaService()
        .validatePublication(getPublicationPK(publicationId), getUserId(), true, false);
  }

  public synchronized void unvalidatePublication(String publicationId, String refusalMotive) {
    getKmeliaService().unvalidatePublication(getPublicationPK(publicationId), getUserId(),
        refusalMotive,
        getValidationType());
    refreshSessionPubliAndClone();
  }

  public synchronized void suspendPublication(String publicationId, String defermentMotive) {
    getKmeliaService().suspendPublication(getPublicationPK(publicationId), defermentMotive,
        getUserId());
  }

  public List<ValidationStep> getValidationSteps() {
    List<ValidationStep> steps =
        getPublicationService().getValidationSteps(getSessionPubliOrClone().getDetail().getPK());

    // Get users who have already validate this publication
    List<String> validators = new ArrayList<>();
    for (ValidationStep step : steps) {
      step.setUserFullName(getOrganisationController().getUserDetail(step.getUserId()).
          getDisplayedName());
      validators.add(step.getUserId());
    }

    List<String> allValidators =
        getKmeliaService().getAllValidators(getSessionPubliOrClone().getDetail().getPK());

    for (String allValidator : allValidators) {
      if (!validators.contains(allValidator)) {
        ValidationStep step = new ValidationStep();
        step.setUserFullName(
            getOrganisationController().getUserDetail(allValidator).getDisplayedName());
        steps.add(step);
      }
    }

    return steps;
  }

  public boolean isUserCanValidatePublication() {
    return getKmeliaService()
        .isUserCanValidatePublication(getSessionPubliOrClone().getDetail().getPK(), getUserId());
  }

  public ValidationStep getValidationStep() {
    if (getValidationType() == KmeliaHelper.VALIDATION_TARGET_N) {
      return getPublicationService()
          .getValidationStepByUser(getSessionPubliOrClone().getDetail().getPK(), getUserId());
    }

    return null;
  }

  public synchronized void draftOutPublication() {

    NodePK currentFolderPK = getCurrentFolderPK();
    if (isKmaxMode) {
      currentFolderPK = null;
    }
    getKmeliaService().draftOutPublication(getSessionPublication().getDetail().getPK(),
        currentFolderPK,
        getProfile());

    if (!KmeliaHelper.ROLE_WRITER.equals(getUserTopicProfile())) {
      // always reset clone
      setSessionClone(null);
    }
    refreshSessionPubliAndClone();
  }

  /**
   * Change publication status from any state to draft
   * @since 3.0
   */
  public synchronized void draftInPublication() {
    if (isCloneNeededWithDraft()) {
      clonePublication();
    } else {
      getKmeliaService().draftInPublication(getSessionPubliOrClone().getDetail().getPK(),
          getUserId());
    }
    refreshSessionPubliAndClone();
  }

  public boolean isIndexable(PublicationDetail pubDetail) {
    return KmeliaHelper.isIndexable(pubDetail);
  }

  /**
   * adds links between specified publication and other publications contained in links parameter
   * @param pubId publication which you want removes the external link
   * @param links list of links to remove
   * @return the number of links created
   */
  public int addPublicationsToLink(String pubId, Set<String> links) {
    List<ResourceReference> infoLinks = new ArrayList<>();
    for (String link : links) {
      StringTokenizer tokens = new StringTokenizer(link, "-");
      infoLinks.add(new ResourceReference(tokens.nextToken(), tokens.nextToken()));
    }
    addInfoLinks(pubId, infoLinks);
    return infoLinks.size();
  }

  /**
   * @param topicDetail the topic detail
   */
  public void setSessionTopic(TopicDetail topicDetail) {
    this.sessionTopic = topicDetail;
    if (topicDetail != null) {
      setCurrentFolderId(topicDetail.getNodePK().getId(), true);
      Collection<KmeliaPublication> publications = topicDetail.getKmeliaPublications();
      setSessionPublicationsList((List<KmeliaPublication>) publications);
    } else {
      setCurrentFolderId(NodePK.ROOT_NODE_ID, true);
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
    cacheDirectlyPublicationsListInSession(publications == null
        ? null
        : new ArrayList<>(publications));
    if (sort) {
      orderPubs();
    }
  }

  private void cacheDirectlyPublicationsListInSession(final List<KmeliaPublication> publications) {
    getSessionCacheService().getCache().put(getPublicationListSessionCacheKey(), publications);
  }

  public void setSessionCombination(List<String> combination) {
    this.sessionCombination = (combination == null ? null : new ArrayList<>(combination));
  }

  public void setSessionTimeCriteria(String timeCriteria) {
    this.sessionTimeCriteria = timeCriteria;
  }

  public int getSortValue() {
    return this.sortValue.getCurrentSort();
  }

  public void setSortValue(String sort) {
    this.sortValue.setCurrentSort(Integer.parseInt(sort));
    this.sortValue.setExplicitSort(true);
    orderPubs();
  }

  public Sort getSort() {
    return this.sortValue;
  }

  public TopicDetail getSessionTopic() {
    return this.sessionTopic;
  }

  public String getCurrentFolderId() {
    return currentFolderId;
  }

  public NodePK getCurrentFolderPK() {
    return new NodePK(getCurrentFolderId(), getComponentId());
  }

  public NodeDetail getCurrentFolder() {
    return getNodeHeader(getCurrentFolderId());
  }

  public void setCurrentFolderId(String id, boolean resetSessionPublication) {
    if (!id.equals(currentFolderId)) {
      indexOfFirstPubToDisplay = 0;
      resetSelectedPublicationPKs();
      setAllPublicationsListSelected(false);
      if (!KmeliaHelper.isSpecialFolder(id)) {
        processBreadcrumb(id);
      }
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

  @SuppressWarnings("unchecked")
  public List<KmeliaPublication> getSessionPublicationsList() {
    return (List) getSessionCacheService().getCache().get(getPublicationListSessionCacheKey());
  }

  public List<String> getSessionCombination() {
    return this.sessionCombination;
  }

  public String getSessionTimeCriteria() {
    return this.sessionTimeCriteria;
  }

  private void removeSessionObjects() {
    setSessionTopic(null);
    setSessionTopicToLink(null);
    setSessionPublication(null);
    setSessionClone(null);
    setSessionPath(null);
    setSessionPathString(null);
    setSessionOwner(false);
    setSessionPublicationsList(null);
  }

  public String initUPToSelectValidator(String formElementName, String formElementId, String folderId) {
    String mContext = URLUtil.getApplicationURL();
    Pair<String, String> hostComponentName = new Pair<>(getComponentLabel(), "");
    Pair<String, String>[] hostPath = new Pair[1];
    hostPath[0] = new Pair<>(getString("kmelia.SelectValidator"), "");
    String hostUrl = mContext + URLUtil.getURL(USELESS, getComponentId()) + "SetValidator";
    String cancelUrl = mContext + URLUtil.getURL(USELESS, getComponentId()) + "SetValidator";

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(getSpaceLabel());
    sel.setHostComponentName(hostComponentName);
    sel.setHostPath(hostPath);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    sel.setHtmlFormName("pubForm");
    if (!StringUtil.isDefined(formElementName)) {
      formElementName = "Valideur";
    }
    sel.setHtmlFormElementName(formElementName);
    if (!StringUtil.isDefined(formElementId)) {
      formElementId = "ValideurId";
    }
    sel.setHtmlFormElementId(formElementId);
    sel.setMultiSelect(isTargetMultiValidationEnable());
    sel.setPopupMode(true);
    sel.setSetSelectable(false);

    if (getSessionPubliOrClone() != null) {
      String[] userIds = getKmeliaService().getActiveValidatorIds(getSessionPubliOrClone().getPk())
          .toArray(new String[0]);
      sel.setSelectedElements(userIds);
    }

    // Add extra params
    SelectionUsersGroups sug = new SelectionUsersGroups();
    sug.setComponentId(getComponentId());

    List<String> profiles = new ArrayList<>();
    profiles.add(SilverpeasRole.PUBLISHER.toString());
    profiles.add(SilverpeasRole.ADMIN.toString());

    NodeDetail node = getNodeHeader(getCurrentFolderId());
    if (StringUtil.isDefined(folderId) && !getCurrentFolderId().equals(folderId)) {
      node = getNodeHeader(folderId);
    }
    boolean haveRights = isRightsOnTopicsEnabled() && node.haveRights();
    if (haveRights) {
      sug.setObjectId(ProfiledObjectType.NODE.getCode() + node.getRightsDependsOn());
    }
    sug.setProfileNames(profiles);

    sel.setExtraParams(sug);

    return Selection.getSelectionURL();
  }

  public boolean isVersionControlled() {
    if (isPublicationAlwaysVisibleEnabled()) {
      // This mode is not compatible, for now, with the possibility to manage versioned
      // attachments.
      return false;
    }
    return StringUtil.getBooleanValue(getComponentParameterValue(VERSION_MODE));
  }

  public boolean isVersionControlled(String anotherComponentId) {
    String strPublicationAlwaysVisible = getOrganisationController()
        .getComponentParameterValue(anotherComponentId, "publicationAlwaysVisible");
    if (StringUtil.getBooleanValue(strPublicationAlwaysVisible)) {
      // This mode is not compatible, for now, with the possibility to manage versioned
      // attachments.
      return false;
    }
    String strVersionControlled = getOrganisationController().getComponentParameterValue(
        anotherComponentId, VERSION_MODE);
    return StringUtil.getBooleanValue(strVersionControlled);

  }

  public boolean isWriterApproval() {
    return false;
  }

  public boolean isTargetValidationEnable() {
    return String.valueOf(KmeliaHelper.VALIDATION_TARGET_1)
        .equalsIgnoreCase(getComponentParameterValue(InstanceParameters.validation));
  }

  public boolean isTargetMultiValidationEnable() {
    return String.valueOf(KmeliaHelper.VALIDATION_TARGET_N)
        .equalsIgnoreCase(getComponentParameterValue(InstanceParameters.validation));
  }

  public boolean isCollegiateValidationEnable() {
    return String.valueOf(KmeliaHelper.VALIDATION_COLLEGIATE)
        .equalsIgnoreCase(getComponentParameterValue(InstanceParameters.validation));
  }

  public boolean isValidationTabVisible() {
    boolean tabVisible =
        PublicationDetail.TO_VALIDATE_STATUS.equalsIgnoreCase(getSessionPubliOrClone().getDetail().
            getStatus());

    return tabVisible && (getValidationType() == KmeliaHelper.VALIDATION_COLLEGIATE ||
        getValidationType() == KmeliaHelper.VALIDATION_TARGET_N);
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
      silverObjectId = getKmeliaService().getSilverObjectId(getPublicationPK(objectId));
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return silverObjectId;
  }

  private boolean isPublicationClassifiedOnPDC(String pubId) {
    if (pubId != null && pubId.length() > 0) {
      try {
        int silverObjectId = getKmeliaService().getSilverObjectId(getPublicationPK(pubId));
        List<ClassifyPosition> positions =
            getPdcManager().getPositions(silverObjectId, getComponentId());
        return !positions.isEmpty();
      } catch (Exception e) {
        throw new KmeliaRuntimeException(e);
      }
    }
    return false;
  }

  public boolean isCurrentPublicationHaveContent() {
    return (StringUtil.isDefined(WysiwygController.load(getComponentId(), getSessionPublication().
        getId(), getCurrentLanguage())) ||
        !isInteger(getSessionPublication().getCompleteDetail().getPublicationDetail().getInfoId()));
  }

  public boolean isPDCClassifyingMandatory() {
    try {
      return getPdcManager().isClassifyingMandatory(getComponentId());
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  /**
   * @return
   */
  public PdcManager getPdcManager() {
    return PdcManager.get();
  }

  public NodeService getNodeService() {
    return NodeService.get();
  }

  public PublicationService getPublicationService() {
    return PublicationService.get();
  }

  /**
   * @param fileUploaded : File uploaded in temp directory
   * @param importMode
   * @param draftMode
   * @param versionType
   * @return a report of the import
   * @throws ImportExportException
   */
  public ImportReport importFile(File fileUploaded, String importMode,
      boolean draftMode, int versionType) throws ImportExportException {
    ImportReport importReport = null;
    FileImport fileImport = new FileImport(this, fileUploaded);
    boolean draft = draftMode;
    if (isDraftEnabled() && isPDCClassifyingMandatory()) {
      // classifying on PDC is mandatory, set publication in draft mode
      draft = true;
    }
    fileImport.setVersionType(versionType);
    if (UNITARY_IMPORT_MODE.equals(importMode)) {
      importReport = fileImport.importFile(draft);
    } else if (MASSIVE_IMPORT_MODE_ONE_PUBLICATION.equals(importMode) &&
        FileUtil.isArchive(fileUploaded.getName())) {
      importReport = fileImport.importFiles(draft);
    } else if (MASSIVE_IMPORT_MODE_MULTI_PUBLICATIONS.equals(importMode) &&
        FileUtil.isArchive(fileUploaded.getName())) {
      importReport = fileImport.importFilesMultiPubli(draft);
    }

    //print a log file of the report
    LocalizationBundle multilang = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.importExportPeas.multilang.importExportPeasBundle");
    MultiSilverpeasBundle resource = new MultiSilverpeasBundle(multilang, "fr");
    fileImport.writeImportToLog(importReport, resource);

    return importReport;
  }

  private PublicationPK getPublicationPK(String id) {
    return new PublicationPK(id, getSpaceId(), getComponentId());
  }

  private NodePK getNodePK(String id) {
    return new NodePK(id, getSpaceId(), getComponentId());
  }

  /**
   * Return if publication is deleted
   * @param pk publication identifier
   * @return true or false
   */
  public boolean isPublicationDeleted(PublicationPK pk) {
    boolean isPublicationDeleted = false;
    try {
      Collection<Collection<NodeDetail>> pathList = getPathList(pk);
      if (pathList.size() == 1) {
        for (Collection<NodeDetail> path : pathList) {
          for (NodeDetail nodeInPath : path) {
            if (nodeInPath.getNodePK().isTrash()) {
              isPublicationDeleted = true;
            }
          }
        }
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
    return isPublicationDeleted;
  }

  public void setModelUsed(String[] models) {
    String objectId = getCurrentFolderId();
    getKmeliaService().setModelUsed(models, getComponentId(), objectId);
  }

  public List<String> getModelUsed() {
    List<String> models = new ArrayList<>();
    String formNameAppLevel = getXMLFormNameForPublications();
    if (StringUtil.isDefined(formNameAppLevel)) {
      models.add(formNameAppLevel);
    } else {
      String objectId = getCurrentFolderId();
      models.addAll(getKmeliaService().getModelUsed(getComponentId(), objectId));
    }
    return models;
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
    return "1".equalsIgnoreCase(paramValue) || "2".equalsIgnoreCase(paramValue) ||
        "".equals(paramValue);
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
      LocalizationBundle timeSettings =
          ResourceLocator.getLocalizationBundle("org.silverpeas.kmelia.multilang.timeAxisBundle",
              getLanguage());
      Enumeration<String> keys = timeSettings.getSpecificKeys();
      List<Integer> orderKeys = new ArrayList<>();
      while (keys.hasMoreElements()) {
        String keyStr = keys.nextElement();
        Integer key = Integer.parseInt(keyStr);
        orderKeys.add(key);
      }
      Collections.sort(orderKeys);
      this.timeAxis = orderKeys;
    }
    return this.timeAxis;
  }

  public synchronized List<NodeDetail> getAxis() {
    return getKmeliaService().getAxis(getComponentId());
  }

  public synchronized List<NodeDetail> getAxisHeaders() {
    return getKmeliaService().getAxisHeaders(getComponentId());
  }

  public synchronized NodePK addAxis(NodeDetail axis) {
    return getKmeliaService().addAxis(axis, getComponentId());
  }

  public synchronized NodeDetail getNodeHeader(String id) {
    return getKmeliaService().getNodeHeader(id, getComponentId());
  }

  public synchronized void updateAxis(NodeDetail axis) {
    getKmeliaService().updateAxis(axis, getComponentId());
  }

  public synchronized void deleteAxis(String axisId) {
    getKmeliaService().deleteAxis(axisId, getComponentId());
  }

  public synchronized List<KmeliaPublication> search(List<String> combination) {
    cacheDirectlyPublicationsListInSession(
        new ArrayList<>(getKmeliaService().search(combination, getComponentId())));
    applyVisibilityFilter();
    return getSessionPublicationsList();
  }

  public synchronized List<KmeliaPublication> search(List<String> combination, int nbDays) {
    cacheDirectlyPublicationsListInSession(
        new ArrayList<>(getKmeliaService().search(combination, nbDays, getComponentId())));
    applyVisibilityFilter();
    return getSessionPublicationsList();
  }

  public synchronized List<KmeliaPublication> getUnbalancedPublications() {
    return (List<KmeliaPublication>) getKmeliaService().getUnbalancedPublications(getComponentId());
  }

  public synchronized NodePK addPosition(String fatherId, NodeDetail position) {

    return getKmeliaService().addPosition(fatherId, position, getComponentId(), getUserId());
  }

  public synchronized void updatePosition(NodeDetail position) {
    getKmeliaService().updatePosition(position, getComponentId());
  }

  public synchronized void deletePosition(String positionId) {
    getKmeliaService().deletePosition(positionId, getComponentId());
  }

  /*
   * /* Kmax - Publications
   */

  /**
   * **********************************************************************************
   */
  public synchronized KmeliaPublication getKmaxCompletePublication(String pubId) {
    return getKmeliaService().getKmaxPublication(pubId, getUserId());
  }

  public synchronized Collection<Coordinate> getPublicationCoordinates(String pubId) {
    return getKmeliaService().getPublicationCoordinates(pubId, getComponentId());
  }

  public synchronized void addPublicationToCombination(String pubId, List<String> combination) {
    getKmeliaService().addPublicationToCombination(pubId, combination, getComponentId());
  }

  public synchronized void deletePublicationFromCombination(String pubId, String combinationId) {
    getKmeliaService().deletePublicationFromCombination(pubId, combinationId, getComponentId());
  }

  /**
   * Get session publications
   * @return List of WAAtributeValuePair (Id and InstanceId)
   */
  public List<WAAttributeValuePair> getCurrentPublicationsList() {
    List<WAAttributeValuePair> currentPublications = new ArrayList<>();
    Collection<KmeliaPublication> allPublications = getSessionPublicationsList();

    for (KmeliaPublication aPubli : allPublications) {
      PublicationDetail pubDetail = aPubli.getDetail();
      if (pubDetail.getStatus().equals(PublicationDetail.VALID_STATUS)) {
        currentPublications
            .add(new WAAttributeValuePair(pubDetail.getId(), pubDetail.getInstanceId()));
      }
    }
    return currentPublications;
  }

  public synchronized Collection<NodeDetail> getPath(String positionId) {
    return getKmeliaService().getPath(positionId, getComponentId());
  }

  public void setCurrentCombination(List<String> combination) {
    this.currentCombination = new ArrayList<>(combination);
  }

  public List<String> getCurrentCombination() {
    return currentCombination;
  }

  /**
   * Transform combination axis from String /0/1037,/0/1038 in ArrayList /0/1037 then /0/1038
   * etc...
   * @param axisValuesStr
   * @return Collection of combination
   */
  private List<String> convertStringCombination2List(String axisValuesStr) {
    List<String> combination = new ArrayList<>();
    StringTokenizer st = new StringTokenizer(axisValuesStr, ",");
    while (st.hasMoreTokens()) {
      String axisValue = st.nextToken();
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

    return convertStringCombination2List(axisValuesStr);
  }

  private String getNearPublication(int direction) {
    // rechercher le rang de la publication précédente
    int rangNext = rang + direction;

    KmeliaPublication pub = getSessionPublicationsList().get(rangNext);
    String pubId = pub.getDetail().getId();

    // on est sur la précédente, mettre à jour le rang avec la publication courante
    rang = rangNext;

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

  private synchronized boolean isDragAndDropEnableByUser() {
    return getPersonalization().isDragAndDropEnabled();
  }

  public boolean isDragAndDropEnable() {
    return isDragAndDropEnableByUser && isMassiveDragAndDropAllowed();
  }

  public String getCurrentLanguage() {
    return currentLanguage;
  }

  public void setCurrentLanguage(String currentLanguage) {
    this.currentLanguage = currentLanguage;
    processBreadcrumb(getCurrentFolderId());
  }

  public String initUserPanelForTopicProfile(String role, String nodeId, String[] groupIds,
      String[] userIds) {
    String context = URLUtil.getApplicationURL();
    Pair<String, String>[] hostPath = new Pair[1];
    hostPath[0] = new Pair<>(getString("kmelia.SelectValidator"), "");

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(getSpaceLabel());
    sel.setHostComponentName(new Pair<>(getComponentLabel(), ""));
    sel.setHostPath(hostPath);

    String hostUrl = context + URLUtil.getURL(USELESS, getComponentId()) +
        "TopicProfileSetUsersAndGroups?Role=" + role + "&NodeId=" + nodeId;
    String cancelUrl = context + URLUtil.getURL(USELESS, getComponentId()) + "CloseWindow";

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);
    sel.setPopupMode(true);
    sel.setHtmlFormElementId("roleItems");
    sel.setHtmlFormName("dummy");

    List<ProfileInst> profiles =
        getAdmin().getProfilesByObject(ProfiledObjectId.fromNode(nodeId), getComponentId());
    ProfileInst topicProfile = getProfile(profiles, role);

    SelectionUsersGroups sug = new SelectionUsersGroups();
    sug.setComponentId(getComponentId());
    sel.setExtraParams(sug);

    if (topicProfile != null) {
      sel.setSelectedElements(userIds);
      sel.setSelectedSets(groupIds);
    }

    return Selection.getSelectionURL();
  }

  private void deleteTopicRoles(NodeDetail node) {
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

  private void deleteTopicRole(String profileId) {
    // Remove the profile
    getAdmin().deleteProfileInst(profileId);
  }

  public void updateTopicRole(String role, String nodeId, String[] groupIds, String[] userIds) {
    ProfileInst profile = getTopicProfile(role, nodeId);

    // Update the topic
    NodeDetail topic = getNodeHeader(nodeId);
    topic.setRightsDependsOnMe();
    getNodeService().updateRightsDependency(topic);

    profile.setUsers(Arrays.asList(userIds));
    profile.setGroups(Arrays.asList(groupIds));

    if (StringUtil.isDefined(profile.getId())) {
      if (profile.isEmpty()) {
        deleteTopicRole(profile.getId());
      } else {
        // Update the profile
        getAdmin().updateProfileInst(profile);
      }
    } else {
      profile.setObjectId(new ProfiledObjectId(ProfiledObjectType.NODE, nodeId));
      profile.setComponentFatherId(getComponentLocalId(getComponentId()));
      // Create the profile
      getAdmin().addProfileInst(profile);
    }
  }

  public ProfileInst getTopicProfile(String role, String topicId) {
    List<ProfileInst> profiles =
        getAdmin().getProfilesByObject(ProfiledObjectId.fromNode(topicId), getComponentId());
    for (ProfileInst profile: profiles) {
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
    } else if (profile == null) {
      return inheritedProfile;
    } else {
      // merge des profiles
      ProfileInst newProfile = new ProfileInst(profile);
      newProfile.addGroups(inheritedProfile.getAllGroups());
      newProfile.addUsers(inheritedProfile.getAllUsers());

      return newProfile;
    }
  }

  public List<ProfileInst> getTopicProfiles() {
    return getTopicProfiles(getCurrentFolderId());
  }

  public List<ProfileInst> getTopicProfiles(String topicId) {
    List<ProfileInst> alShowProfile = new ArrayList<>();
    String[] asAvailProfileNames = getAdmin().getAllProfilesNames(KMELIA);
    for (String asAvailProfileName : asAvailProfileNames) {
      ProfileInst profile = getTopicProfile(asAvailProfileName, topicId);
      profile.setLabel(
          getAdmin().getProfileLabelfromName(KMELIA, asAvailProfileName, getLanguage()));
      alShowProfile.add(profile);
    }

    return alShowProfile;

  }

  public List<Group> groupIds2Groups(List<String> groupIds) {
    List<Group> res = new ArrayList<>();
    for (int nI = 0; groupIds != null && nI < groupIds.size(); nI++) {
      Group theGroup = getAdmin().getGroupById(groupIds.get(nI));
      if (theGroup != null) {
        res.add(theGroup);
      }
    }

    return res;
  }

  public List<UserDetail> userIds2Users(List<String> userIds) {
    List<UserDetail> res = new ArrayList<>();
    if (userIds != null) {
      for (String userId : userIds) {
        UserDetail user = UserDetail.getById(userId);
        if (user != null) {
          res.add(user);
        }
      }
    }
    return res;
  }

  private AdminController getAdmin() {
    return ServiceProvider.getService(AdminController.class);
  }

  private ProfileInst getProfile(List<ProfileInst> profiles, String role) {
    for (ProfileInst profile : profiles) {
      if (role.equals(profile.getName())) {
        return profile;
      }
    }
    return null;
  }

  public boolean isUserCanValidate() {
    return getKmeliaService().isUserCanValidate(getComponentId(), getUserId());
  }

  public boolean isUserCanWrite() {
    return getKmeliaService().isUserCanWrite(getComponentId(), getUserId());
  }

  public void copyPublication(String pubId) throws ClipboardException {
    PublicationDetail pub = getPublicationDetail(pubId);
    // Can only copy user accessed publication
    PublicationAccessControl publicationAccessController = PublicationAccessControl.get();
    if (publicationAccessController.isUserAuthorized(getUserId(), pub.getPK())) {
      PublicationSelection pubSelect = new PublicationSelection(pub, getCurrentFolderPK());
      addClipboardSelection(pubSelect);
    } else {
      SilverLogger.getLogger(this)
          .warn("Security alert from user {0} trying to copy publication {1}", getUserId(), pubId);
      throw new ClipboardException("Security purpose, access to publication is forbidden");
    }
  }

  private void copyPublications(List<PublicationPK> pubPKs) throws ClipboardException {
    for (PublicationPK pubPK : pubPKs) {
      if (pubPK != null) {
        copyPublication(pubPK.getId());
      }
    }
  }

  public void copySelectedPublications() throws ClipboardException {
    copyPublications(getSelectedPublicationPKs());
  }

  public void cutPublication(String pubId) throws ClipboardException {
    PublicationDetail pub = getPublicationDetail(pubId);
    // Can only copy user accessed publication
    PublicationAccessControl publicationAccessController = PublicationAccessControl.get();
    if (publicationAccessController.isUserAuthorized(getUserId(), pub.getPK())) {
      PublicationSelection pubSelect = new PublicationSelection(pub, getCurrentFolderPK());
      pubSelect.setCutted(true);

      addClipboardSelection(pubSelect);
    } else {
      SilverLogger.getLogger(this)
          .warn("Security alert from user {0} trying to cut publication {1}", getUserId(), pubId);
      throw new ClipboardException("Security purpose, access to publication is forbidden");
    }
  }

  private void cutPublications(List<PublicationPK> pubPKs) throws ClipboardException {
    for (PublicationPK pubPK : pubPKs) {
      if (pubPK != null && pubPK.getInstanceId().equals(getComponentId())) {
        cutPublication(pubPK.getId());
      }
    }
  }

  public void cutSelectedPublications() throws ClipboardException {
    cutPublications(getSelectedPublicationPKs());
  }

  public void copyTopic(String id) throws ClipboardException {
    NodeDetail nodeDetail = getNodeHeader(id);
    if (nodeDetail.canBeModifiedBy(getUserDetail())) {
      NodeSelection nodeSelect = new NodeSelection(getNodeHeader(id));

      addClipboardSelection(nodeSelect);
    } else {
      SilverLogger.getLogger(this)
          .warn("Security alert from user {0} trying to copy topic {1}", getUserId(), id);
      throw new ClipboardException("Security purpose : access to node is forbidden");
    }
  }

  public void cutTopic(String id) throws ClipboardException {
    NodeDetail nodeDetail = getNodeHeader(id);
    if (nodeDetail.canBeModifiedBy(getUserDetail())) {
      NodeSelection nodeSelect = new NodeSelection(getNodeHeader(id));
      nodeSelect.setCutted(true);

      addClipboardSelection(nodeSelect);
    } else {
      SilverLogger.getLogger(this)
          .warn("Security alert from user {0} trying to cut topic {1}", getUserId(), id);
      throw new ClipboardException("Security purpose : access to node is forbidden");
    }
  }

  public List<Object> paste(KmeliaPasteDetail pasteDetail) throws ClipboardException {
    resetSelectedPublicationPKs();
    pasteDetail.setUserId(getUserId());
    List<Object> pastedItems = new ArrayList<>();
    try {
      final NodeDetail targetNode = getNodeHeader(pasteDetail.getToPK().getId());
      final Collection<ClipboardSelection> selectedObjects = getClipboardSelectedObjects();
      final List<PublicationPK> pubPkMoved = new ArrayList<>();
      final List<PublicationPK> publicationPkCopied = new ArrayList<>();
      final List<NodePK> nodePkMoved = new ArrayList<>();
      final List<NodePK> nodePkCopied = new ArrayList<>();
      for (ClipboardSelection selection : selectedObjects) {
        if (selection == null) {
          continue;
        }
        if (selection.isDataFlavorSupported(PublicationSelection.PublicationDetailFlavor)) {
          PublicationSelection.TransferData data = (PublicationSelection.TransferData) selection.getTransferData(
              PublicationSelection.PublicationDetailFlavor);
          final PublicationDetail pub = data.getPublicationDetail();
          Optional.of(pub)
              .filter(p -> selection.isCutted())
              .ifPresentOrElse(p -> pubPkMoved.add(pub.getPK()),
                  () -> publicationPkCopied.add(pub.getPK()));
        } else if (selection.isDataFlavorSupported(NodeSelection.NodeDetailFlavor)) {
          final NodeDetail node = (NodeDetail) selection.getTransferData(NodeSelection.NodeDetailFlavor);
          // check if current topic is a subTopic of node
          Optional.of(node)
              .filter(n -> !n.equals(targetNode) && !n.isFatherOf(targetNode))
              .ifPresent(n -> Optional.of(n)
                  .filter(nd -> selection.isCutted())
                  .ifPresentOrElse(nd -> nodePkMoved.add(node.getNodePK()),
                      () -> nodePkCopied.add(node.getNodePK())));
        }
      }
      SimulationActionProcessProcessor.get()
          .withContext(s -> s.getSourcePKs().addAll(pubPkMoved))
            .listElementsWith(KmeliaPublicationSimulationElementLister::new)
            .byAction(() -> ActionType.MOVE)
          .andWithContext(s -> s.getSourcePKs().addAll(publicationPkCopied))
            .listElementsWith(KmeliaPublicationSimulationElementLister::new)
            .byAction(() -> ActionType.COPY)
          .andWithContext(s -> s.getSourcePKs().addAll(nodePkMoved))
            .listElementsWith(KmeliaNodeSimulationElementLister::new)
            .byAction(() -> ActionType.MOVE)
          .andWithContext(s -> s.getSourcePKs().addAll(nodePkCopied))
            .listElementsWith(KmeliaNodeSimulationElementLister::new)
            .byAction(() -> ActionType.COPY)
          .toTargets(t -> t.getTargetPKs().add(targetNode.getNodePK()))
          .setLanguage(() -> currentLanguage)
          .execute(() -> {
            selectedObjects.stream()
                .filter(Objects::nonNull)
                .map(s -> {
                  try {
                    return pasteClipboardSelection(s, pasteDetail, targetNode);
                  } catch (UnsupportedFlavorException e) {
                    throw new KmeliaRuntimeException(e);
                  }
                })
                .filter(Objects::nonNull)
                .forEach(pastedItems::add);
            return null;
          });

    } catch (ClipboardException | UnsupportedFlavorException e) {
      throw new KmeliaRuntimeException(e);
    }
    clipboardPasteDone();
    return pastedItems;
  }

  private Object pasteClipboardSelection(ClipboardSelection selection,
      KmeliaPasteDetail pasteDetail, final NodeDetail targetNode) throws UnsupportedFlavorException {
    if (selection.isDataFlavorSupported(PublicationSelection.PublicationDetailFlavor)) {
      PublicationSelection.TransferData data = (PublicationSelection.TransferData) selection.getTransferData(
          PublicationSelection.PublicationDetailFlavor);
      PublicationDetail pub = data.getPublicationDetail();
      if (selection.isCutted()) {
        pasteDetail.setFromPK(data.getFatherPK());
        movePublication(pub.getPK(), targetNode.getNodePK(), pasteDetail);
      } else {
        KmeliaCopyDetail copyDetail = KmeliaCopyDetail.fromPasteDetail(pasteDetail);
        copyDetail.setFromNodePK(data.getFatherPK());
        getKmeliaService().copyPublication(pub, copyDetail);
      }
      return pub;
    } else if (selection.isDataFlavorSupported(NodeSelection.NodeDetailFlavor)) {
      NodeDetail node = (NodeDetail) selection.getTransferData(NodeSelection.NodeDetailFlavor);
      // check if current topic is a subTopic of node
      boolean pasteAllowed = !node.equals(targetNode) && !node.isFatherOf(targetNode);
      if (pasteAllowed) {
        if (selection.isCutted()) {
          // move node
          getKmeliaService().moveNode(node.getNodePK(), targetNode.getNodePK(), pasteDetail);
        } else {
          // copy node
          KmeliaCopyDetail copyDetail = KmeliaCopyDetail.fromPasteDetail(pasteDetail);
          copyDetail.setFromNodePK(node.getNodePK());
          getKmeliaService().copyNode(copyDetail);
        }
        return node;
      }
    }
    return null;
  }

  public CLIPBOARD_STATE getClipboardState() {
    try {
      final Set<CLIPBOARD_STATE> states = getClipboardSelectedObjects().stream()
          .filter(Objects::nonNull)
          .filter(c -> c.isDataFlavorSupported(PublicationSelection.PublicationDetailFlavor)
                    || c.isDataFlavorSupported(NodeSelection.NodeDetailFlavor))
          .map(c -> c.isCutted() ? HAS_CUTS : HAS_COPIES)
          .collect(toSet());
      final CLIPBOARD_STATE state;
      if (states.size() == 1) {
        state = states.iterator().next();
      } else if (states.size() > 1) {
        state = HAS_COPIES_AND_CUTS;
      } else {
        state = IS_EMPTY;
      }
      return state;
    } catch (ClipboardException e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  private void movePublication(PublicationPK pubPK, NodePK nodePK, KmeliaPasteDetail pasteContext) {
    try {
      NodePK currentNodePK = nodePK;
      if (currentNodePK == null) {
        // Ajoute au thème courant
        currentNodePK = getCurrentFolderPK();
      }

      getKmeliaService().movePublication(pubPK, currentNodePK, pasteContext);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
  }

  /**
   * get languages of publication header and attachments
   * @return a List of String (language codes)
   */
  public List<String> getPublicationLanguages() {
    List<String> languages = new ArrayList<>();
    PublicationDetail pubDetail = getSessionPubliOrClone().getDetail();
    // get publicationdetail languages
    Collection<String> pubLanguages = pubDetail.getLanguages();
    for (String language : pubLanguages) {
      languages.add(language);
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
    List<String> languages = new ArrayList<>();
    List<String> attLanguages = getLanguagesOfAttachments(new ResourceReference(pubPK.getId(), pubPK.
        getInstanceId()));
    for (String language : attLanguages) {
      if (!languages.contains(language)) {
        languages.add(language);
      }
    }
    return languages;
  }

  public Collection<Location> getPublicationLocations() {
    return getKmeliaService().getLocations(getSessionPublication().getDetail().getPK());
  }

  public List<NodeDetail> getAliasTreeview(String instanceId) {
    List<NodeDetail> tree = null;
    if (getKmeliaService().isUserCanPublish(instanceId, getUserId())) {
      NodePK root = new NodePK(NodePK.ROOT_NODE_ID, instanceId);
      tree = getKmeliaService().getTreeview(root, null, false, false, getUserId(), false,
          isRightsOnTopicsEnabled(instanceId));
    }
    return tree;
  }

  private boolean isToolbox() {
    return KmeliaHelper.isToolbox(getComponentId());
  }

  /**
   * Returns URL of single attached file for the current publication.
   * If publication contains more than one file, null is returned
   * @param fromAlias true if getting document version from an alias.
   * @return URL of single attached file for the current publication. Null if publication
   * contains more than one file.
   */
  public String getSingleAttachmentURLOfCurrentPublication(final boolean fromAlias) {
    PublicationPK pubPK = getSessionPublication().getDetail().getPK();
    List<SimpleDocument> attachments = AttachmentServiceProvider.getAttachmentService().
        listDocumentsByForeignKey(pubPK.toResourceReference(), getLanguage());
    if (attachments.size() == 1) {
      SimpleDocument document = attachments.get(0);
      return getDocumentVersionURL(document, fromAlias);
    }
    return null;
  }

  /**
   * Return the url to access the file
   * @param fileId the id of the file (attachment or document id).
   * @param fromAlias true if getting document version from an alias.
   * @return the url to the file.
   */
  public String getAttachmentURL(final String fileId, final boolean fromAlias) {
    SimpleDocument attachment = AttachmentServiceProvider.getAttachmentService().
        searchDocumentById(new SimpleDocumentPK(fileId), getLanguage());
    return getDocumentVersionURL(attachment, fromAlias);
  }

  /**
   * Returns URL of the right version of the given document according to current folder rights
   * if user is a reader, returns last public version (null if it does not exist)
   * if user is not a reader, returns last version (public or working one)
   * @param document a loaded {@link SimpleDocument} instance.
   * @param fromAlias true if getting document version from an alias.
   * @return the URL of right version or null
   */
  private String getDocumentVersionURL(final SimpleDocument document, final boolean fromAlias) {
    return ofNullable(document.getLastPublicVersion())
        .map(v -> {
          if (!fromAlias && v.getVersionMaster().canBeAccessedBy(getUserDetail())) {
            return v.getVersionMaster();
          }
          return v;
        })
        .map(v -> URLUtil.getApplicationURL() + v.getAttachmentURL())
        .orElse(null);
  }

  public synchronized List<NodeDetail> getSubTopics(String rootId) {
    return getNodeService().getSubTree(getNodePK(rootId));
  }

  public String getXmlFormForFiles() {
    return getComponentParameterValue("XmlFormForFiles");
  }

  public File exportPublication() {
    if (!isFormatSupported("zip")) {
      throw new KmeliaRuntimeException("Export format not yet supported");
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
      new AttachmentImportExport(getUserDetail()).getAttachments(pubPK.toResourceReference(),
          subDirPath, USELESS, null);

      String zipFileName = FileRepositoryManager.getTemporaryPath() + fileName + ".zip";
      // zip PDF and files
      ZipUtil.compressPathToZip(subDirPath, zipFileName);

      return new File(zipFileName);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    } finally {
      if (pdf != null) {
        try {
          Files.delete(pdf.toPath());
        } catch (IOException e) {
          SilverLogger.getLogger(this).warn(e);
        }
      }
    }
  }

  public boolean isNotificationAllowed() {
    String parameterValue = getComponentParameterValue("notifications");
    if (!StringUtil.isDefined(parameterValue)) {
      return true;
    }
    return StringUtil.getBooleanValue(parameterValue);
  }

  public boolean isWysiwygOnTopicsEnabled() {
    return StringUtil.getBooleanValue(getComponentParameterValue("wysiwygOnTopics"));
  }

  public String getWysiwygOnTopic(String id) {
    String currentId = id;
    if (isWysiwygOnTopicsEnabled()) {
      if (!StringUtil.isDefined(currentId)) {
        currentId = getCurrentFolderId();
      }
      return WysiwygController.load(getComponentId(), "Node_" + currentId, getCurrentLanguage());
    }
    return "";
  }

  public String getWysiwygOnTopic() {
    return getWysiwygOnTopic(null);
  }

  public List<NodeDetail> getTopicPath(String topicId) {
    try {
      List<NodeDetail> pathInReverse = getNodeService().getPath(new NodePK(topicId, getComponentId()));
      Collections.reverse(pathInReverse);
      return pathInReverse;
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  public ThumbnailSettings getThumbnailSettings() {
    int width = getSettings().getInteger("vignetteWidth", -1);
    int height = getSettings().getInteger("vignetteHeight", -1);
    return ThumbnailSettings.getInstance(getComponentId(), width, height);
  }

  /**
   * return the value of component parameter "axisIdGlossary". This paramater indicate the axis of
   * pdc to use to highlight word in publication content
   * @return an indentifier of Pdc axis
   */
  public String getAxisIdGlossary() {
    return getComponentParameterValue("axisIdGlossary");
  }

  public String getRole() {
    return getProfile();
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
        if (!nodeInPath.getNodePK().isRoot()) {
          setPathNode(linkedPathString, pathString, iterator, nodeInPath);
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

  private void setPathNode(final StringBuilder linkedPathString, final StringBuilder pathString,
      final Iterator<NodeDetail> iterator, final NodeDetail nodeInPath) {
    String nodeName;
    if (nodeInPath.getNodePK().isTrash()) {
      nodeName = getString("kmelia.basket");
    } else {
      if (getCurrentLanguage() != null) {
        nodeName = nodeInPath.getName(getCurrentLanguage());
      } else {
        nodeName = nodeInPath.getName();
      }
    }
    linkedPathString.append("<a href=\"javascript:onClick=topicGoTo('")
        .append(nodeInPath.getNodePK().getId())
        .append("')\">")
        .append(Encode.forHtml(nodeName))
        .append("</a>");
    pathString.append(nodeName);
    if (iterator.hasNext()) {
      linkedPathString.append(" > ");
      pathString.append(" > ");
    }
  }

  /**
   * Is search in topics enabled
   * @return boolean
   */
  public boolean isSearchOnTopicsEnabled() {
    return StringUtil.getBooleanValue(getComponentParameterValue("searchOnTopics").toLowerCase());
  }

  public boolean isAttachmentsEnabled() {
    return StringUtil.getBooleanValue(getComponentParameterValue("tabAttachments"));
  }

  /**
   * Get publications and aliases of this topic and its subtopics answering to the query
   * @param queryDescription the query
   * @return List of Kmelia publications
   */
  public synchronized List<KmeliaPublication> search(QueryDescription queryDescription,
      PagesContext formContext) {

    String query = queryDescription.getQuery();

    // Insert this new search inside persistence layer in order to compute statistics
    final NodeDetail currentFolder = getCurrentFolder();
    final String nodePathSep = "/";
    final String safeCurrentFolderPath = getCurrentFolder().getFullPath().replaceFirst("[/]+$", "") + nodePathSep;
    TopicSearch newTS = new TopicSearch(getComponentId(), currentFolder.getId(),
        getUserId(), getLanguage(), query.toLowerCase(), new Date());
    KmeliaSearchServiceProvider.getTopicSearchService().createTopicSearch(newTS);

    List<KmeliaPublication> userPublications = new ArrayList<>();
    queryDescription.setSearchingUser(getUserId());
    queryDescription.setRequestedFolder(getCurrentFolder().getFullPath());
    queryDescription.addComponent(getComponentId());
    queryDescription.setRequestedLanguage("*");

    try {
      List<MatchingIndexEntry> results = getSearchEngine().search(queryDescription).getEntries();
      results = results.stream()
          .filter(i -> PUBLICATION.equals(i.getObjectType()))
          .collect(Collectors.toList());
      final Map<PublicationPK, PublicationDetail> indexedUserPubs = new HashMap<>(results.size());
      getKmeliaService().getPublicationDetails(results.stream()
                .map(i -> new ResourceReference(i.getObjectId(), i.getComponent()))
                .collect(Collectors.toList()))
          .forEach(p -> indexedUserPubs.put(p.getPK(), p));
      userPublications = results.stream()
          .map(i -> {
            final PublicationPK pubPk = new PublicationPK(i.getObjectId(), i.getComponent());
            final PublicationDetail pub = indexedUserPubs.get(pubPk);
            if (pub == null) {
              return null;
            }
            final KmeliaPublication kPub = KmeliaPublication.fromDetail(pub);
            if (i.isAlias()
                || (i.getPaths() != null
                    && !i.getPaths().isEmpty()
                    && !(i.getPaths().get(0) + nodePathSep).startsWith(safeCurrentFolderPath))) {
              kPub.getDetail().setAlias(true);
            }
            return kPub;
          })
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
    } catch (Exception pe) {
      throw new KmeliaRuntimeException(pe);
    }

    // store "in session" current search context
    formContext.setLanguage(getLanguage());
    SearchContext aSearchContext = new SearchContext(queryDescription, formContext);
    aSearchContext.setNode(currentFolder);
    aSearchContext.setResults(userPublications);
    setSearchContext(aSearchContext);

    // store results and keep search results order
    setSessionPublicationsList(userPublications, false);

    return userPublications;
  }

  /**
   * @return the list of SpaceInst from current space identifier (in session) to root space <br>
   * (all the subspace)
   */
  public List<SpaceInstLight> getSpacePath() {
    return this.getOrganisationController().getPathToSpace(this.getSpaceId());
  }

  /**
   * Gets an instance of PublicationTemplateManager.
   * @return an instance of PublicationTemplateManager.
   */
  private PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }

  public List<PublicationTemplate> getForms() {
    List<PublicationTemplate> templates = new ArrayList<>();
    try {
      GlobalContext aContext = new GlobalContext(getSpaceId(), getComponentId());
      templates = getPublicationTemplateManager().getPublicationTemplates(aContext);
    } catch (PublicationTemplateException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return templates;
  }

  @Override
  public String getPublicationExportFileName(KmeliaPublication publication, String language) {
    String lang = getLanguage();
    StringBuilder fileName = new StringBuilder(250);

    fileName.append(getUserDetail().getLogin()).append('-');

    // add space path to filename
    List<SpaceInstLight> listSpaces = getSpacePath();
    for (SpaceInstLight space : listSpaces) {
      fileName.append(space.getName(lang)).append('-');
    }
    // add component name to filename
    fileName.append(getComponentLabel());

    if (!isKmaxMode) {
      List<NodeDetail> path = getNodeService().getPath(getCurrentFolder().getNodePK());
      Collections.reverse(path);
      path.remove(0); // remove root folder
      for (NodeDetail node : path) {
        fileName.append('-').append(node.getName(lang));
      }
    }

    fileName.append('-').append(publication.getDetail().getTitle()).append('-');
    fileName.append(publication.getPk().getId());
    return StringUtil.toAcceptableFilename(fileName.toString());
  }

  public void removePublicationContent() {
    KmeliaPublication publication = getSessionPubliOrClone();
    PublicationPK pubPK = publication.getPk();
    getKmeliaService().removeContentOfPublication(pubPK);
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
   * Gets the export formats that are supported by the current Kmelia component instance. As some
   * of
   * the export formats can be deactivated in the Kmelia settings file, this method returns all the
   * formats that are currently active.
   * @return a list of export formats.
   */
  public List<String> getSupportedFormats() {
    String exportFormats = getSettings().getString(EXPORT_FORMATS, "");
    List<String> supportedFormats = new ArrayList<>();
    if (!exportFormats.trim().isEmpty()) {
      List<String> availableFormats = getAvailableFormats();
      for (String exportFormat : exportFormats.trim().split(" ")) {
        if (!availableFormats.contains(exportFormat)) {
          throw new KmeliaRuntimeException("Unknown format " + exportFormat);
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
    List<ClassifyPosition> positions = getPdcManager()
        .getPositions(Integer.valueOf(publication.getSilverObjectId()),
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
    PdcClassificationService classificationService = PdcClassificationService.get();
    PdcClassification defaultClassification =
        classificationService.findAPreDefinedClassification(topicId, componentId);
    return defaultClassification != NONE_CLASSIFICATION && defaultClassification.isModifiable();
  }

  public void resetSelectedPublicationPKs() {
    this.selectedPublicationPKs.clear();
  }

  public List<PublicationPK> processSelectedPublicationIds(String selectedPublicationIds,
      String notSelectedPublicationIds) {
    StringTokenizer tokenizer;
    if (selectedPublicationIds != null) {
      tokenizer = new StringTokenizer(selectedPublicationIds, ",");
      while (tokenizer.hasMoreTokens()) {
        String[] str = StringUtil.splitByWholeSeparator(tokenizer.nextToken(), "-");
        PublicationPK pk = new PublicationPK(str[0], str[1]);
        PublicationAccessControl publicationAccessController = PublicationAccessControl.get();
        if (!this.selectedPublicationPKs.contains(pk) &&
            publicationAccessController.isUserAuthorized(getUserId(), pk,
                AccessControlContext.init())) {
            this.selectedPublicationPKs.add(pk);
        }
      }
    }

    if (notSelectedPublicationIds != null) {
      tokenizer = new StringTokenizer(notSelectedPublicationIds, ",");
      while (tokenizer.hasMoreTokens()) {
        String[] str = StringUtil.splitByWholeSeparator(tokenizer.nextToken(), "-");
        PublicationPK pk = new PublicationPK(str[0], str[1]);
        this.selectedPublicationPKs.remove(pk);
      }
    }

    return this.selectedPublicationPKs;
  }

  public List<PublicationPK> getSelectedPublicationPKs() {
    return selectedPublicationPKs;
  }

  public boolean isCustomPublicationTemplateUsed() {
    return customPublicationTemplateUsed;
  }

  public String getCustomPublicationTemplateName() {
    return customPublicationTemplateName;
  }

  public List<KmeliaPublication> getLatestPublications() {
    List<KmeliaPublication> publicationsToDisplay = new ArrayList<>();
    List<KmeliaPublication> toCheck = getKmeliaService()
        .getLatestAuthorizedPublications(getComponentId(), getUserId(), getNbPublicationsOnRoot());
    for (KmeliaPublication aPublication : toCheck) {
      if (!isPublicationDeleted(aPublication.getPk())) {
        publicationsToDisplay.add(aPublication);
      }
    }
    return publicationsToDisplay;
  }

  public List<KmeliaPublication> getPublicationsOfCurrentFolder() {
    if (KmeliaHelper.isToValidateFolder(currentFolderId)){
      setSessionPublicationsList(
          getKmeliaService().getPublicationsToValidate(getComponentId(), getUserId()));
    } else if (KmeliaHelper.isNonVisiblePubsFolder(currentFolderId)) {
      setSessionPublicationsList(
          getKmeliaService().getNonVisiblePublications(getComponentId(), getUserId()));
    } else {
      setSessionPublicationsList(getKmeliaService()
          .getPublicationsOfFolder(new NodePK(currentFolderId, getComponentId()),
              getUserTopicProfile(currentFolderId), getUserId(), isTreeStructure()));
      applyVisibilityFilter();
    }
    return getSessionPublicationsList();
  }

  public String getContentLanguage() {
    if (getSessionPubliOrClone() != null) {
      if (getPublicationLanguages().contains(getCurrentLanguage())) {
        return getCurrentLanguage();
      }
      return I18NHelper.checkLanguage(getSessionPublication().getDetail().getLanguage());
    }
    return getCurrentLanguage();
  }

  public void setSearchContext(SearchContext searchContext) {
    this.searchContext = searchContext;
  }

  public SearchContext getSearchContext() {
    return searchContext;
  }

  public String manageSubscriptions() {
    final SubscriptionContext subscriptionContext = getSubscriptionContext();
    if (getSessionPublication() != null) {
      PublicationDetail publication = getSessionPublication().getDetail();
      List<NodeDetail> nodePath = getTopicPath(getCurrentFolderId());
      nodePath.remove(0);
      final SubscriptionResource resource;
      if (publication.isAlias()) {
        publication = publication.copy();
        final Location location = new Location(getCurrentFolderId(), getComponentId());
        location.setAsAlias(getUserId());
        publication.setAuthorizedLocation(location);
        resource = PublicationAliasSubscriptionResource.from(new PublicationPK(publication.getId(), getComponentId()));
      } else {
        resource = PublicationSubscriptionResource.from(publication);
      }
      subscriptionContext.initialize(resource)
          .atLocation(new Location(getCurrentFolderId(), getComponentId()))
          .withPath(Stream.concat(nodePath.stream()
                                    .map(p -> new SubscriptionResourcePath(p.getName(getLanguage()), p.getLink())),
                    Stream.of(new SubscriptionResourcePath(publication.getName(getLanguage()), publication.getPermalink())))
              .collect(Collectors.toList()));
    } else if (getCurrentFolder().getNodePK().isRoot()) {
      subscriptionContext.initialize(ComponentSubscriptionResource.from(getComponentId()));
    } else {
      List<NodeDetail> nodePath = getTopicPath(getCurrentFolderId());
      nodePath.remove(0);
      subscriptionContext.initialize(NodeSubscriptionResource.from(getCurrentFolderPK()))
          .atLocation(new Location(getCurrentFolderId(), getComponentId()))
          .withNodePath(nodePath);
    }
    return subscriptionContext.getDestinationUrl();
  }

  /**
   * @param importReport
   * @return the number of publications created
   */
  public int getNbPublicationImported(ImportReport importReport) {
    int nbPublication = 0;
    List<ComponentReport> listComponentReport = importReport.getListComponentReport();
    for (ComponentReport componentRpt : listComponentReport) {
      nbPublication += componentRpt.getNbPublicationsCreated();
    }
    return nbPublication;
  }

  /**
   * @param importReport
   * @param importMode
   * @return an error message or null
   */
  public String getErrorMessageImportation(ImportReport importReport, String importMode) {
    String message = null;
    LocalizationBundle attachmentResourceLocator =
        ResourceLocator.getLocalizationBundle("org.silverpeas.util.attachment.multilang.attachment",
            this.getLanguage());
    ComponentReport componentRpt = importReport.getListComponentReport().get(0);

    if (UNITARY_IMPORT_MODE.equals(importMode)) {
      //Unitary mode
      message = getReportOnOnePublicationImport(attachmentResourceLocator, componentRpt);
    } else if (MASSIVE_IMPORT_MODE_ONE_PUBLICATION.equals(importMode)) {
      //Massive mode, one publication
      message = getReportOnOnePublicationImport(attachmentResourceLocator, componentRpt);
    } else if (MASSIVE_IMPORT_MODE_MULTI_PUBLICATIONS.equals(importMode)) {
      //Massive mode, several publications
      message =
          getReportOnSeveralPublicationImport(message, attachmentResourceLocator, componentRpt);
    }
    return message;
  }

  private String getReportOnSeveralPublicationImport(String message,
      final LocalizationBundle attachmentResourceLocator, final ComponentReport componentRpt) {
    MassiveReport massiveReport = componentRpt.getListMassiveReports().get(0);
    for (UnitReport unitReport : massiveReport.getListUnitReports()) {
      if (unitReport.getError() == UnitReport.ERROR_FILE_SIZE_EXCEEDS_LIMIT) {
        message = getMaxSizeErrorMessage(attachmentResourceLocator);
      } else if (unitReport.getError() != UnitReport.ERROR_NO_ERROR) {
        message = attachmentResourceLocator.getString(INACCESSIBLE_ATTACHMENT);
        break;
      }
    }
    return message;
  }

  private String getReportOnOnePublicationImport(final LocalizationBundle attachmentResourceLocator,
      final ComponentReport componentRpt) {
    final String message;
    MassiveReport massiveReport = componentRpt.getListMassiveReports().get(0);
    UnitReport unitReport = massiveReport.getListUnitReports().get(0);
    if (unitReport.getError() == UnitReport.ERROR_NO_ERROR) {
      return null;
    } else if (unitReport.getError() == UnitReport.ERROR_FILE_SIZE_EXCEEDS_LIMIT) {
      message = getMaxSizeErrorMessage(attachmentResourceLocator);
    } else {
      message = attachmentResourceLocator.getString(INACCESSIBLE_ATTACHMENT);
    }
    return message;
  }

  /**
   * @param importReport
   * @param importMode
   * @return a list of publication
   */
  public List<PublicationDetail> getListPublicationImported(ImportReport importReport,
      String importMode) {
    List<PublicationDetail> listPublicationDetail = new ArrayList<>();
    ComponentReport componentRpt = importReport.getListComponentReport().get(0);

    //Unitary mode
    if (UNITARY_IMPORT_MODE.equals(importMode)) {
      MassiveReport massiveReport = componentRpt.getListMassiveReports().get(0);
      UnitReport unitReport = massiveReport.getListUnitReports().get(0);
      if (unitReport.getStatus() == UnitReport.STATUS_PUBLICATION_CREATED) {
        String idPubli = unitReport.getLabel();
        PublicationDetail publicationDetail = this.getPublicationDetail(idPubli);
        listPublicationDetail.add(publicationDetail);
      }
    } //Massive mode, one publication
    else if (MASSIVE_IMPORT_MODE_ONE_PUBLICATION.equals(importMode)) {
      UnitReport unitReport = componentRpt.getListUnitReports().get(0);
      if (unitReport.getStatus() == UnitReport.STATUS_PUBLICATION_CREATED) {
        String idPubli = unitReport.getLabel();
        PublicationDetail publicationDetail = this.getPublicationDetail(idPubli);
        listPublicationDetail.add(publicationDetail);
      }

      //Massive mode, several publications
    } else if (MASSIVE_IMPORT_MODE_MULTI_PUBLICATIONS.equals(importMode)) {
      MassiveReport massiveReport = componentRpt.getListMassiveReports().get(0);
      for (UnitReport unitReport : massiveReport.getListUnitReports()) {
        if (unitReport.getStatus() == UnitReport.STATUS_PUBLICATION_CREATED) {
          String idPubli = unitReport.getLabel();
          PublicationDetail publicationDetail = this.getPublicationDetail(idPubli);
          listPublicationDetail.add(publicationDetail);
        }
      }
    }
    return listPublicationDetail;
  }

  private String getMaxSizeErrorMessage(LocalizationBundle messages) {
    long maximumFileSize = FileRepositoryManager.getUploadMaximumFileSize();
    String maximumFileSizeMo = UnitUtil.formatMemSize(maximumFileSize);
    return messages.getString("attachment.dialog.errorAtLeastOneFileSize") + " " + messages.
        getString("attachment.dialog.maximumFileSize") + " (" + maximumFileSizeMo + ")";
  }

  public List<HistoryObjectDetail> getLastAccess(PublicationPK pk) {
    return getKmeliaService().getLastAccess(pk, getCurrentFolderPK(), getUserId(), 4);
  }

  public void setPublicationValidator(String userIds) {
    getKmeliaService().setValidators(getSessionPubliOrClone().getDetail().getPK(), userIds);
    refreshSessionPubliAndClone();
  }

  /**
   * Check user access right on folder
   * @param nodeId the topic/folder identifier to check
   * @return true if current user has admin access on topic given in parameter
   */
  public boolean isTopicAdmin(final String nodeId) {
    String profile = getUserTopicProfile(nodeId);
    if (profile != null) {
      return SilverpeasRole.getHighestFrom(SilverpeasRole.fromString(profile))
          .isGreaterThanOrEquals(SilverpeasRole.ADMIN);
    }
    return isUserComponentAdmin();
  }

  /*
   * Persist the date reminder for the given publication
   * @param pubDetail
   * @param dateReminderDate
   * @param messageReminder
   * @throws DateReminderException
   */
  private void createResourceDateReminder(final PublicationDetail pubDetail,
      final Date dateReminderDate, final String messageReminder) throws DateReminderException {

    if(dateReminderDate != null) {
      DateReminderDetail dateReminderDetail =
          new DateReminderDetail(dateReminderDate, messageReminder,
              DateReminderDetail.REMINDER_NOT_PROCESSED, pubDetail.getUpdaterId(), pubDetail.getUpdaterId());
      PublicationNoteReference publicationNoteReference = PublicationNoteReference.fromPublicationDetail(pubDetail);
      getDateReminderService().create(publicationNoteReference, dateReminderDetail);
    }
  }

  /**
   * Create the date reminder for the given publication
   * @param pubDetail
   * @param parameters
   * @throws DateReminderException
   */
  public void addPublicationReminder(PublicationDetail pubDetail, List<FileItem> parameters)
      throws DateReminderException {
    String dateReminder = FileUploadUtil.getParameter(parameters, "DateReminder");
    String messageReminder = FileUploadUtil.getParameter(parameters, "MessageReminder");

    Date dateReminderDate = null;

    try {
      if (StringUtil.isDefined(dateReminder)) {
        dateReminderDate = DateUtil.stringToDate(dateReminder, this.getLanguage());
      }
    } catch (ParseException e) {
      dateReminderDate = null;
    }

    //Create date reminder
    createResourceDateReminder(pubDetail, dateReminderDate, messageReminder);
  }

  /**
   * Save Or remove the date reminder for the given publication
   * @param pubId
   * @param parameters
   * @throws DateReminderException
   */
  public void updatePublicationReminder(String pubId, List<FileItem> parameters)
      throws DateReminderException {
    String dateReminder = FileUploadUtil.getParameter(parameters, "DateReminder");
    String messageReminder = FileUploadUtil.getParameter(parameters, "MessageReminder");

    Date dateReminderDate = null;

    try {
      if (StringUtil.isDefined(dateReminder)) {
        dateReminderDate = DateUtil.stringToDate(dateReminder, this.getLanguage());
      }
    } catch (ParseException e) {
      dateReminderDate = null;
    }

    PublicationDetail pubDetail = getPublicationDetail(pubId);
    PublicationNoteReference publicationNoteReference = PublicationNoteReference.fromPublicationDetail(pubDetail);
    PersistentResourceDateReminder actualResourceDateReminder = getDateReminderService().get(publicationNoteReference);

    if(actualResourceDateReminder.isDefined()) {

      if(dateReminderDate != null) {//Update reminder

        DateReminderDetail actualDateReminderDetail = actualResourceDateReminder.getDateReminder();
        String actualDateReminder = DateUtil.dateToString(
            actualDateReminderDetail.getDateReminder(), this.getLanguage());
        int processStatus;
        if(! actualDateReminder.equals(dateReminder)) {// the date reminder has been updated
          // the date reminder must be processed by the scheduler
          processStatus =  DateReminderDetail.REMINDER_NOT_PROCESSED;
        } else {// the date reminder has not been updated
          // keep the same process status
          processStatus = actualDateReminderDetail.getProcessStatus();
        }

        DateReminderDetail dateReminderDetail =
            new DateReminderDetail(dateReminderDate, messageReminder, processStatus,
                actualDateReminderDetail.getCreatorId(), pubDetail.getUpdaterId());
        getDateReminderService().set(publicationNoteReference, dateReminderDetail);

      } else {//Delete reminder
        getDateReminderService().remove(publicationNoteReference);
      }
    } else {//Create reminder
      createResourceDateReminder(pubDetail, dateReminderDate, messageReminder);
    }
  }

  public boolean isTemplatesSelectionEnabledForRole(SilverpeasRole role) {
    return !isXMLFormEnabledForPublications() && isContentEnabled() &&
        SilverpeasRole.ADMIN.equals(role);
  }

  private String getXMLFormNameForPublications() {
    return getComponentParameterValue("XmlFormForPublis");
  }

  private boolean isXMLFormEnabledForPublications() {
    return StringUtil.isDefined(getXMLFormNameForPublications());
  }

  public Form getXmlFormForPublications() {
    Form formUpdate = null;
    try {
      PublicationTemplateImpl pubTemplate = (PublicationTemplateImpl) getTemplateForPublications();
      if (pubTemplate != null) {
        formUpdate = pubTemplate.getUpdateForm();
        RecordSet recordSet = pubTemplate.getRecordSet();

        DataRecord record = recordSet.getEmptyRecord();

        formUpdate.setData(record);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    return formUpdate;
  }

  public Form getXmlFormSearchForPublications() {
    Form form = null;
    try {
      PublicationTemplateImpl pubTemplate = (PublicationTemplateImpl) getTemplateForPublications();
      if (pubTemplate != null) {
        form = pubTemplate.getSearchForm();
        DataRecord emptyRecord = pubTemplate.getRecordSet().getEmptyRecord();
        form.setData(emptyRecord);
        setSearchCriteria(emptyRecord);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    return form;
  }

  private void setSearchCriteria(DataRecord record) {
    if (getSearchContext() != null) {
      QueryDescription queryDescription = getSearchContext().getQueryDescription();
      if (queryDescription != null && queryDescription.getMultiFieldQuery() != null) {
        for (FieldDescription fieldDescription : queryDescription.getMultiFieldQuery()) {
          try {
            Field field = record.getField(extractFieldName(fieldDescription.getFieldName()));
            if (field != null) {
              field.setValue(
                  fieldDescription.getContent().replace(" OR ", "##").replace(" AND ", "##"));
            }
          } catch (Exception e) {
            SilverLogger.getLogger(this).error(e);
          }
        }
      }
    }
  }

  private String extractFieldName(String completeFieldName) {
    String[] fieldNameParts = StringUtil.split(completeFieldName, "$$");
    if (fieldNameParts.length ==  2) {
      return fieldNameParts[1];
    }
    return "";
  }

  private PublicationTemplate getTemplateForPublications() {
    PublicationTemplate template = null;
    List<String> forms = getModelUsed();
    if (forms.size() == 1 && !"WYSIWYG".equals(forms.get(0))) {
      String form = forms.get(0);
      String formShort = form.substring(0, form.indexOf('.'));
      try {
        // register xmlForm to publication
        getPublicationTemplateManager()
            .addDynamicPublicationTemplate(getComponentId() + ":" + formShort, form);

        template = getPublicationTemplateManager()
            .getPublicationTemplate(getComponentId() + ':' + formShort, form);
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
    return template;
  }

  public void saveXMLFormToPublication(PublicationDetail pubDetail, List<FileItem> items,
      boolean forceUpdatePublication) throws org.silverpeas.core.SilverpeasException {
    saveXMLFormToPublication(pubDetail, new KmeliaXmlFormUpdateContext(items, forceUpdatePublication));
  }

  public boolean saveXMLFormToPublication(PublicationDetail pubDetail,
      final KmeliaXmlFormUpdateContext updateContext) throws org.silverpeas.core.SilverpeasException {
    final String xmlFormShortName;
    // Is it the creation of the content or an update ?
    final String infoId = pubDetail.getInfoId();
    if (infoId == null || "0".equals(infoId)) {
      xmlFormShortName = updateContext.getXmlFormShortNameFromItems();

      // The publication have no content
      // We have to register xmlForm to publication
      pubDetail.setInfoId(xmlFormShortName);
      updatePublication(pubDetail);
    } else {
      xmlFormShortName = pubDetail.getInfoId();
      if (!xmlFormShortName.equals(updateContext.getXmlFormShortNameFromItems())) {
        SilverLogger.getLogger(this)
            .error("{0} and {1} mismatch, no update performed for pub {2}", xmlFormShortName,
                updateContext.getXmlFormShortNameFromItems(), pubDetail.getPK());
        return false;
      }
    }
    final String pubId = pubDetail.getPK().getId();
    try {
      final Pair<PublicationTemplate, DataRecord> pubData =
          updateContext.getOrInitializePublicationDataRecordOf(
          pubDetail, getCurrentLanguage());
      final Form form = pubData.getFirst().getUpdateForm();
      final RecordSet set = pubData.getFirst().getRecordSet();
      final DataRecord data = pubData.getSecond();
      final PagesContext context = new PagesContext();
      context.setLanguage(getLanguage());
      context.setComponentId(getComponentId());
      context.setUserId(getUserId());
      context.setEncoding(Charsets.UTF_8.name());
      if (!isKmaxMode) {
        context.setNodeId(getCurrentFolderId());
      }
      context.setObjectId(pubId);
      if (updateContext.isForceUpdatePublication()) {
        // case of a modification of the publication
        context.setContentLanguage(getContentLanguage());
      } else {
        // the publication is just created
        context.setContentLanguage(pubDetail.getLanguage());
      }
      if (updateContext.isBatchProcessing()) {
        context.setUpdatePolicy(PagesContext.ON_UPDATE_IGNORE_EMPTY_VALUES);
      }
      form.update(updateContext.getItems(), data, context);
      set.save(data);
    } catch (Exception e) {
      throw new org.silverpeas.core.SilverpeasException("Can't save XML form of publication", e);
    }
    ofNullable(FileUploadUtil.getParameter(updateContext.getItems(), "VolatileId"))
        .filter(StringUtil::isDefined)
        // Attaching all documents linked to volatile publication to the persisted one
        .map(v -> Pair.of(v, AttachmentServiceProvider.getAttachmentService()
            .moveAllDocuments(getPublicationPK(v).toResourceReference(),
                pubDetail.getPK().toResourceReference())))
        .filter(p -> !p.getSecond().isEmpty())
        // Change images path in wysiwyg
        .ifPresent(p -> WysiwygController.wysiwygPlaceHaveChanged(getComponentId(), p.getFirst(),
            getComponentId(), pubId));
    if (updateContext.isForceUpdatePublication()) {
      // update publication to change updateDate and updaterId
      updatePublication(pubDetail);
    }
    return true;
  }

  public void saveXMLForm(List<FileItem> items, boolean forceUpdatePublication)
      throws org.silverpeas.core.SilverpeasException {
    if (isCloneNeeded()) {
      clonePublication();
    }

    final String currentLanguageCopy = getCurrentLanguage();
    PublicationDetail pubDetail = getSessionPubliOrClone().getDetail();
    saveXMLFormToPublication(pubDetail, items, forceUpdatePublication);
    setCurrentLanguage(currentLanguageCopy);
  }

  public PublicationDetail prepareNewPublication() {
    String volatileId = newVolatileIntegerIdentifierOn(getComponentId());
    return PublicationDetail.builder()
        .setPk(getPublicationPK(volatileId))
        .build();
  }

  public enum CLIPBOARD_STATE {
    IS_EMPTY, HAS_COPIES, HAS_CUTS, HAS_COPIES_AND_CUTS
  }

  private void processBreadcrumb(String id) {
    Collection<NodeDetail> pathColl = getTopicPath(id);
    String linkedPathString = displayPath(pathColl, true, 3);
    String pathString = displayPath(pathColl, false, 3);
    setSessionPath(linkedPathString);
    setSessionPathString(pathString);
  }

  private String getPublicationListSessionCacheKey() {
    return KmeliaSessionController.class.getSimpleName() + "#" + getComponentId() +
        "#publicationList";
  }

  public void saveXMLFormOfSelectedPublications(List<FileItem> items) {
    final WebMessager messager = WebMessager.getInstance();
    final Map<PublicationPK, Pair<KmeliaPublication, KmeliaPublication>> authorizedPublicationsOrClonesByPubOrClonePks =
        getPublicationsForModification(selectedPublicationPKs)
            .stream()
            .filter(p -> Objects.equals(p.getFirst().getComponentInstanceId(), getComponentId()))
            .collect(toMap(p -> p.getSecond() != null ? p.getSecond().getPk() : p.getFirst().getPk(), p -> p));
    if (authorizedPublicationsOrClonesByPubOrClonePks.isEmpty()) {
      messager.addSuccess(getMultilang().getString("kmelia.publications.batch.update.none"));
      return;
    }
    final Map<PublicationPK, PublicationDetail> publicationOrCloneCache = authorizedPublicationsOrClonesByPubOrClonePks
        .entrySet()
        .stream()
        .map(e -> e.getValue().getSecond() != null ? e.getValue().getSecond() : e.getValue().getFirst())
        .collect(toMap(KmeliaPublication::getPk, KmeliaPublication::getDetail));
    final KmeliaXmlFormUpdateContext updateContext = new KmeliaXmlFormUpdateContext(items, true).batchProcessing();
    try {
      SimulationActionProcessProcessor.get()
          .withContext(s -> s.getSourcePKs().addAll(publicationOrCloneCache.keySet()))
          .listElementsWith(() -> new KmeliaPublicationBatchSimulationElementLister(publicationOrCloneCache,
              updateContext, getUserDetail()))
          .byAction(() -> ActionType.UPDATE)
          .toTargets(t -> t.getTargetPKs().add(getCurrentFolderPK()))
          .setLanguage(this::getCurrentLanguage)
          .execute(() -> {
            final List<PublicationPK> success = new ArrayList<>();
            final List<PublicationPK> fail = new ArrayList<>();
            publicationOrCloneCache.forEach((k, p) -> {
              final PublicationPK selectedPk = authorizedPublicationsOrClonesByPubOrClonePks.get(k).getFirst().getPk();
              try {
                Optional.of(saveXMLFormToPublication(p, updateContext))
                    .filter(Boolean.TRUE::equals)
                    .ifPresentOrElse(t -> success.add(selectedPk), () -> fail.add(selectedPk));
              } catch (Exception e) {
                SilverLogger.getLogger(this).error("Can't save content of publication #" + p.getId(), e);
                fail.add(selectedPk);
              }
            });
            if (fail.isEmpty()) {
              messager.addSuccess(getMultilang().getStringWithParams("kmelia.publications.batch.update.success",
                  success.size()));
            } else {
              messager.addError(
                  getMultilang().getStringWithParams("kmelia.publications.batch.update.fail", fail.size()));
            }
            selectedPublicationPKs.removeAll(success);
            return null;
          });
    } catch (Exception e) {
      SilverpeasTransverseErrorUtil.stopTransverseErrorIfAny(new SilverpeasRuntimeException(e));
    }
  }

  public boolean isAllPublicationsListSelected() {
    return allPublicationsListSelected;
  }

  public void setAllPublicationsListSelected(final boolean allPublicationsListSelected) {
    this.allPublicationsListSelected = allPublicationsListSelected;
  }

  public void selectAllPublications(boolean selected) {
    resetSelectedPublicationPKs();
    setAllPublicationsListSelected(selected);
    if (!selected) {
      return;
    }

    List<KmeliaPublication> publications = getSessionPublicationsList();
    for (KmeliaPublication publication : publications) {
      selectedPublicationPKs.add(publication.getPk());
    }
  }
}
