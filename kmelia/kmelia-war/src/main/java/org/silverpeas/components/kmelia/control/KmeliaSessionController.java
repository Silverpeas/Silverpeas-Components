/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharEncoding;
import org.owasp.encoder.Encode;
import org.silverpeas.components.kmelia.FileImport;
import org.silverpeas.components.kmelia.InstanceParameters;
import org.silverpeas.components.kmelia.KmeliaAuthorization;
import org.silverpeas.components.kmelia.KmeliaCopyDetail;
import org.silverpeas.components.kmelia.KmeliaPasteDetail;
import org.silverpeas.components.kmelia.KmeliaPublicationHelper;
import org.silverpeas.components.kmelia.SearchContext;
import org.silverpeas.components.kmelia.export.ExportFileNameProducer;
import org.silverpeas.components.kmelia.export.KmeliaPublicationExporter;
import org.silverpeas.components.kmelia.model.*;
import org.silverpeas.components.kmelia.model.updatechain.FieldParameter;
import org.silverpeas.components.kmelia.model.updatechain.FieldUpdateChainDescriptor;
import org.silverpeas.components.kmelia.model.updatechain.Fields;
import org.silverpeas.components.kmelia.model.updatechain.UpdateChainDescriptor;
import org.silverpeas.components.kmelia.search.KmeliaSearchServiceProvider;
import org.silverpeas.components.kmelia.service.KmeliaHelper;
import org.silverpeas.components.kmelia.service.KmeliaService;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.admin.ObjectType;
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
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.service.CommentService;
import org.silverpeas.core.comment.service.CommentServiceProvider;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.Attachments;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.wysiwyg.WysiwygException;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.converter.DocumentFormat;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.core.contribution.publication.datereminder.PublicationNoteReference;
import org.silverpeas.core.contribution.publication.model.Alias;
import org.silverpeas.core.contribution.publication.model.CompletePublication;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.model.PublicationSelection;
import org.silverpeas.core.contribution.publication.model.ValidationStep;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateImpl;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.datereminder.exception.DateReminderException;
import org.silverpeas.core.datereminder.persistence.DateReminderDetail;
import org.silverpeas.core.datereminder.persistence.PersistentResourceDateReminder;
import org.silverpeas.core.datereminder.persistence.service.DateReminderServiceProvider;
import org.silverpeas.core.datereminder.persistence.service.PersistentDateReminderService;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.importexport.ExportDescriptor;
import org.silverpeas.core.importexport.attachment.AttachmentImportExport;
import org.silverpeas.core.importexport.model.ImportExportException;
import org.silverpeas.core.importexport.report.ComponentReport;
import org.silverpeas.core.importexport.report.ImportReport;
import org.silverpeas.core.importexport.report.MassiveReport;
import org.silverpeas.core.importexport.report.UnitReport;
import org.silverpeas.core.index.indexing.model.IndexManager;
import org.silverpeas.core.index.search.SearchEngineProvider;
import org.silverpeas.core.index.search.model.MatchingIndexEntry;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.io.media.image.thumbnail.ThumbnailSettings;
import org.silverpeas.core.io.upload.UploadedFile;
import org.silverpeas.core.node.coordinates.model.Coordinate;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.model.NodeSelection;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.notification.user.client.NotificationManager;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;
import org.silverpeas.core.pdc.pdc.service.GlobalPdcManager;
import org.silverpeas.core.pdc.pdc.service.PdcClassificationService;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.NodeAccessController;
import org.silverpeas.core.security.authorization.PublicationAccessController;
import org.silverpeas.core.silverstatistics.access.model.HistoryObjectDetail;
import org.silverpeas.core.silverstatistics.access.model.StatisticRuntimeException;
import org.silverpeas.core.silverstatistics.access.service.StatisticService;
import org.silverpeas.core.subscription.service.NodeSubscriptionResource;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.core.util.*;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.util.AlertUser;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.selection.SelectionUsersGroups;
import org.silverpeas.core.web.subscription.SubscriptionContext;
import org.silverpeas.core.webapi.pdc.PdcClassificationEntity;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static org.silverpeas.components.kmelia.export.KmeliaPublicationExporter.*;
import static org.silverpeas.core.cache.service.VolatileIdentifierProvider.newVolatileIntegerIdentifierOn;
import static org.silverpeas.core.contribution.attachment.AttachmentService.VERSION_MODE;
import static org.silverpeas.core.pdc.pdc.model.PdcClassification.NONE_CLASSIFICATION;
import static org.silverpeas.core.pdc.pdc.model.PdcClassification.aPdcClassificationOfContent;

public class KmeliaSessionController extends AbstractComponentSessionController
    implements ExportFileNameProducer {

  /**
   * The different export formats the KmeliaPublicationExporter should support.
   */
  private static final String EXPORT_FORMATS = "kmelia.export.formats.active";
  /**
   * All the formats that are available for the export of publications.
   */
  private static final String[] AVAILABLE_EXPORT_FORMATS = {"zip", "pdf", "odt", "doc"};
  private static final int DEFAULT_NBPUBLIS_PER_PAGE = 10;

  /* Services used by sessionController */
  private CommentService commentService = null;
  private StatisticService statisticService = null;
  private PdcManager pdcManager = null;
  private NotificationManager notificationManager = null;
  private PersistentDateReminderService dateReminderService = null;
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
  private List<KmeliaPublication> sessionPublicationsList = null;
  // Specific Kmax
  private List<String> sessionCombination = null;
  // Specific Kmax
  private String sessionTimeCriteria = null;
  private String sortValue = null;
  private String defaultSortValue = "2";
  private int rang = 0;
  private SettingBundle publicationSettings = null;
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
  // sauvegarde pour mise à jour à la chaine
  Fields saveFields = new Fields();
  boolean isDragAndDropEnableByUser = false;
  boolean componentManageable = false;
  private List<PublicationPK> selectedPublicationPKs = new ArrayList<>();
  private boolean customPublicationTemplateUsed = false;
  private String customPublicationTemplateName = null;
  private SearchContext searchContext = null;

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

  public static List<String> getLanguagesOfAttachments(ForeignPK foreignPK) {
    List<String> languages = new ArrayList<>();
    for (String availableLanguage : I18NHelper.getAllSupportedLanguages()) {
      List<SimpleDocument> attachments = AttachmentServiceProvider.getAttachmentService()
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
    componentManageable = ResourceLocator.getGeneralSettingBundle().getBoolean(
        "AdminFromComponentEnable", true);
    if (componentManageable) {
      componentManageable =
          getOrganisationController().isComponentManageable(getComponentId(), getUserId());
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
    sessionPublicationsList = Collections.emptyList();

    nbPublicationsPerPage = getSettings().getInteger("NbPublicationsParPage", 10);
    String parameterValue = getComponentParameterValue("nbPubliPerPage");
    if (StringUtil.isInteger(parameterValue)) {
      nbPublicationsPerPage = Integer.parseInt(parameterValue);
    }
  }

  /**
   * Gets a business service of comments.
   * @return a DefaultCommentService instance.
   */
  protected CommentService getCommentService() {
    if (commentService == null) {
      commentService = CommentServiceProvider.getCommentService();
    }
    return commentService;
  }

  public KmeliaService getKmeliaBm() {
    try {
      return ServiceProvider.getService(KmeliaService.class);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.getKmeliaService()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public StatisticService getStatisticService() {
    if (statisticService == null) {
      try {
        statisticService = ServiceProvider.getService(StatisticService.class);
      } catch (Exception e) {
        throw new StatisticRuntimeException("KmeliaSessionController.getStatisticService()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return statisticService;
  }

  /**
  * Gets a business service of dateReminder.
  *
  * @return a DefaultDateReminderService instance.
  */
  protected PersistentDateReminderService getDateReminderService() {
    if (dateReminderService == null) {
      dateReminderService = DateReminderServiceProvider.getDateReminderService();
    }
    return dateReminderService;
  }

  public SettingBundle getPublicationSettings() {
    if (publicationSettings == null) {
      publicationSettings =
          ResourceLocator.getSettingBundle("org.silverpeas.publication.publicationSettings");
    }
    return publicationSettings;
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
        (getHighestSilverpeasUserRole().isGreaterThanOrEquals(SilverpeasRole.publisher) ||
        getSilverpeasUserRoles().contains(SilverpeasRole.supervisor));
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

  public boolean isWizardEnabled() {
    return StringUtil.getBooleanValue(getComponentParameterValue("wizardEnabled"));
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
    String parameterValue = getComponentParameterValue("tabSeeAlso");
    if (!StringUtil.isDefined(parameterValue)) {
      return false;
    }
    return StringUtil.getBooleanValue(parameterValue);
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
      invisibleTabs.add(this.TAB_CONTENT);
    }

    if (isToolbox()) {
      invisibleTabs.add(this.TAB_PREVIEW);
    }

    String parameterValue = this.getComponentParameterValue("tabAttachments");
    if (!isToolbox()) {
      // attachments tab is always visible with toolbox
      if (StringUtil.isDefined(parameterValue) &&
          !StringUtil.getBooleanValue(parameterValue)) {
          invisibleTabs.add(this.TAB_ATTACHMENTS);
      }
    }

    if (!isSeeAlsoEnabled()) {
      invisibleTabs.add(this.TAB_SEE_ALSO);
    }

    parameterValue = this.getComponentParameterValue("tabAccessPaths");
    if (StringUtil.isDefined(parameterValue) &&
        !StringUtil.getBooleanValue(parameterValue)) {
        invisibleTabs.add(this.TAB_ACCESS_PATHS);
    }

    parameterValue = this.getComponentParameterValue("tabReadersList");
    if (StringUtil.isDefined(parameterValue) &&
        !StringUtil.getBooleanValue(parameterValue)) {
        invisibleTabs.add(this.TAB_READER_LIST);
    }

    parameterValue = this.getComponentParameterValue("tabComments");
    if (!StringUtil.isDefined(parameterValue)) {
      invisibleTabs.add(this.TAB_COMMENT);
    } else {
      if (!StringUtil.getBooleanValue(parameterValue)) {
        invisibleTabs.add(this.TAB_COMMENT);
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
      throw new KmeliaRuntimeException("kmelia", SilverpeasException.ERROR,
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
            inMimeType(inFormat.name());
        aKmeliaPublicationExporter().exports(descriptor, () -> publication);
      } catch (Exception ex) {
        SilverLogger.getLogger(this).error("Publication export failure", ex);
        if (document != null) {
          FileUtils.deleteQuietly(document);
        }
        throw new KmeliaRuntimeException("KmeliaSessionController.generateDocument()",
            SilverpeasRuntimeException.ERROR, "kmelia.EX_CANT_EXPORT_PUBLICATION", ex);
      }
    }
    return document;
  }

  /**
   * Gets a new exporter of Kmelia publications.
   * @return a KmeliaPublicationExporter instance.
   */
  public static KmeliaPublicationExporter aKmeliaPublicationExporter() {
    return ServiceProvider.getService(KmeliaPublicationExporter.class);
  }

  @Override
  public SilverpeasRole getHighestSilverpeasUserRole() {
    SilverpeasRole userRole = SilverpeasRole.from(getProfile());
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
    }
    return getKmeliaBm().getUserTopicProfile(getNodePK(nodeId), getUserId());
  }

  public List<String> getUserIdsOfTopic() {
    return getKmeliaBm().getUserIdsOfFolder(getCurrentFolderPK());
  }

  public boolean isCurrentTopicAvailable() {
    if (isRightsOnTopicsEnabled()) {
      if (KmeliaHelper.isToValidateFolder(getCurrentFolderId())) {
        return true;
      }
      NodeDetail node = getNodeHeader(getCurrentFolderId());
      if (node.haveRights()) {
        int rightsDependsOn = node.getRightsDependsOn();
        return getOrganisationController()
            .isObjectAvailable(rightsDependsOn, ObjectType.NODE, getComponentId(), getUserId());
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

  public synchronized TopicDetail getTopic(String id) {
    return getTopic(id, true);
  }

  public synchronized TopicDetail getTopic(String id, boolean resetSessionPublication) {
    if (resetSessionPublication) {
      setSessionPublication(null);
    }
    if (!id.equals(getCurrentFolderId())) {
      indexOfFirstPubToDisplay = 0;
    }

    TopicDetail currentTopic;
    if (isUserComponentAdmin()) {
      currentTopic =
          getKmeliaBm().goTo(getNodePK(id), getUserId(), isTreeStructure(), "admin", false);
    } else {
      currentTopic = getKmeliaBm()
          .goTo(getNodePK(id), getUserId(), isTreeStructure(), getUserTopicProfile(id),
              isRightsOnTopicsEnabled());
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

  public List<NodeDetail> getTreeview(String nodeId) {
    if (isUserComponentAdmin()) {
      return getKmeliaBm().getTreeview(getNodePK(nodeId), "admin", isCoWritingEnable(),
          isDraftVisibleWithCoWriting(), getUserId(), displayNbPublis(), false);
    } else {
      return getKmeliaBm().getTreeview(getNodePK(nodeId), getProfile(), isCoWritingEnable(),
          isDraftVisibleWithCoWriting(), getUserId(), displayNbPublis(), isRightsOnTopicsEnabled());
    }
  }

  public synchronized TopicDetail getPublicationTopic(String pubId) {
    TopicDetail currentTopic = getKmeliaBm()
        .getPublicationFather(getPublicationPK(pubId), isTreeStructure(), getUserId(),
            isRightsOnTopicsEnabled());
    setSessionTopic(currentTopic);
    applyVisibilityFilter();
    return currentTopic;
  }

  public synchronized List<NodeDetail> getAllTopics() {
    return getNodeBm().getSubTree(getNodePK(NodePK.ROOT_NODE_ID));
  }

  public synchronized void flushTrashCan() {

    TopicDetail td = getKmeliaBm()
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
      return getKmeliaBm().updateTopic(nd, alertType);
    }
    SilverLogger.getLogger(this).warn("Security alert from {0}", getUserId());
    return null;
  }

  public synchronized NodeDetail getSubTopicDetail(String subTopicId) {
    return getKmeliaBm().getSubTopicDetail(getNodePK(subTopicId));
  }

  public synchronized NodePK addSubTopic(NodeDetail nd, String alertType, String parentId) {
    nd.getNodePK().setSpace(getSpaceId());
    nd.getNodePK().setComponentName(getComponentId());
    nd.setCreatorId(getUserId());
    return getKmeliaBm().addSubTopic(getNodePK(parentId), nd, alertType);
  }

  public synchronized String deleteTopic(String topicId) {
    if (NodePK.ROOT_NODE_ID.equals(topicId) || NodePK.BIN_NODE_ID.equals(topicId)) {
      return null;
    }
    NodeDetail node = getNodeHeader(topicId);
    // check if user is allowed to delete this topic
    if (SilverpeasRole.admin.isInRole(getUserTopicProfile(topicId)) ||
        SilverpeasRole.admin.isInRole(getUserTopicProfile(NodePK.ROOT_NODE_ID)) ||
        SilverpeasRole.admin.isInRole(getUserTopicProfile(node.getFatherPK().getId()))) {
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
      boolean recursiveChanges) {
    getKmeliaBm().changeTopicStatus(newStatus, getNodePK(topicId), recursiveChanges);
  }

  public synchronized Collection<Collection<NodeDetail>> getSubscriptionList() {
    return getKmeliaBm().getSubscriptionList(getUserId(), getComponentId());
  }

  public synchronized void removeSubscription(String topicId) {
    getKmeliaBm().removeSubscriptionToCurrentUser(getNodePK(topicId), getUserId());
  }

  public synchronized void addSubscription(String topicId) {
    getKmeliaBm().addSubscription(getNodePK(topicId), getUserId());
  }

  /**
   * @param pubId a publication identifier
   * @return
   */
  public synchronized PublicationDetail getPublicationDetail(String pubId) {
    return getKmeliaBm().getPublicationDetail(getPublicationPK(pubId));
  }

  private Collection<Collection<NodeDetail>> getPathList(PublicationPK pk) {
    return getKmeliaBm().getPathList(pk);
  }

  public synchronized Collection<NodePK> getPublicationFathers(String pubId) {
    return getKmeliaBm().getPublicationFathers(getPublicationPK(pubId));
  }

  public NodePK getAllowedPublicationFather(String pubId) {
    return getKmeliaBm()
        .getPublicationFatherPK(getPublicationPK(pubId), isTreeStructure(), getUserId(),
            isRightsOnTopicsEnabled());
  }

  public synchronized String createPublication(PublicationDetail pubDetail,
      final PdcClassificationEntity classification) {
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
        PdcClassification withClassification =
            aPdcClassificationOfContent(pubDetail).withPositions(pdcPositions);
        result = getKmeliaBm()
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
      getKmeliaBm().updatePublication(pubDetail);
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
      cloneId = getKmeliaBm().clonePublication(refPubComplete, pubDetail, nextStatus);
      setSessionClone(getPublication(cloneId));
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.clonePublication",
          SilverpeasException.ERROR, "kmelia.CANT_CLONE_PUBLICATION", e);
    }
    return cloneId;
  }

  public synchronized void deletePublication(String pubId) {
    deletePublication(pubId, false);
  }

  public synchronized void deletePublication(String pubId, boolean kmaxMode) {
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
  }

  public List<String> deleteSelectedPublications() {
    List<String> removed = getKmeliaBm()
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
      String cloneId = getSessionClone().getDetail().getPK().getId();
      PublicationPK clonePK = getPublicationPK(cloneId);

      removeXMLContentOfPublication(clonePK);
      getKmeliaBm().deletePublication(clonePK);

      setSessionClone(null);
      refreshSessionPubliAndClone();

      // delete references on clone
      PublicationDetail pubDetail = getSessionPublication().getDetail();
      pubDetail.setCloneId(null);
      pubDetail.setCloneStatus(null);
      pubDetail.setStatusMustBeChecked(false);
      pubDetail.setUpdateDateMustBeSet(false);

      getKmeliaBm().updatePublication(pubDetail);

    }
  }

  private void removeXMLContentOfPublication(PublicationPK pubPK) {
    try {
      PublicationDetail pubDetail = getKmeliaBm().getPublicationDetail(pubPK);
      String infoId = pubDetail.getInfoId();
      if (!isInteger(infoId)) {
        PublicationTemplate pubTemplate = getPublicationTemplateManager()
            .getPublicationTemplate(pubDetail.getPK().getInstanceId() + ":" + infoId);

        RecordSet set = pubTemplate.getRecordSet();
        DataRecord data = set.getRecord(pubDetail.getPK().getId());
        set.delete(data);
      }
    } catch (PublicationTemplateException | FormException e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.removeXMLContentOfPublication()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LE_CONTENU_XML", e);
    }
  }

  private static boolean isInteger(String id) {
    return StringUtil.isInteger(id);
  }

  public synchronized void addPublicationToTopic(String pubId, String fatherId) {
    getKmeliaBm().addPublicationToTopic(getPublicationPK(pubId), getNodePK(fatherId), false);
  }

  public synchronized void deletePublicationFromAllTopics(String pubId) {
    getKmeliaBm().deletePublicationFromAllTopics(getPublicationPK(pubId));
  }

  public void refreshSessionPubliAndClone() {
    if (getSessionClone() != null) {
      // Clone refresh
      KmeliaPublication pub = getPublication(getSessionClone().getDetail().getPK().getId());
      setSessionClone(pub);
    } else {
      // refresh publication master
      KmeliaPublication pub = getPublication(getSessionPublication().getDetail().getPK().getId());
      setSessionPublication(pub);
    }
  }

  /**
   * removes links between specified publication and other publications contained in links
   * parameter
   * @param pubId publication which you want removes the external link
   * @param links list of links to remove
   */
  public void deleteInfoLinks(String pubId, List<ForeignPK> links) {
    getKmeliaBm().deleteInfoLinks(getPublicationPK(pubId), links);

    // reset current publication
    KmeliaPublication completPub = getKmeliaBm().getPublication(getPublicationPK(pubId));
    setSessionPublication(completPub);
  }

  /**
   * adds links between specified publication and other publications contained in links parameter
   * @param pubId publication which you want removes the external link
   * @param links list of links to remove
   */
  public void addInfoLinks(String pubId, List<ForeignPK> links) {
    getKmeliaBm().addInfoLinks(getPublicationPK(pubId), links);

    // reset current publication
    KmeliaPublication completPub = getKmeliaBm().getPublication(getPublicationPK(pubId));
    setSessionPublication(completPub);
  }

  /**
   * Get publications explicitly referenced by current publication. Only valid publications which
   * are not in bin are returned. Rights of user are checked (applications and folders).
   * @return a List of KmeliaPublication
   * @see KmeliaPublication
   */
  public List<KmeliaPublication> getLinkedVisiblePublications() {
    List<ForeignPK> seeAlsoList = getSessionPublication().getCompleteDetail().getLinkList();
    List<ForeignPK> authorizedSeeAlsoList = new ArrayList<ForeignPK>();
    for (ForeignPK curFPK : seeAlsoList) {
      String curComponentId = curFPK.getComponentName();
      // check if user have access to application
      if (curComponentId != null &&
          getOrganisationController().isComponentAvailable(curComponentId, getUserId())) {
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

  public synchronized KmeliaPublication getPublication(String pubId) {
    return getPublication(pubId, false);
  }

  public synchronized KmeliaPublication getPublication(String pubId, boolean processIndex) {
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
        getStatisticService().addStat(getUserId(), foreignPK, 1, "Publication");
      }
    } else {
      getStatisticService().addStat(getUserId(), foreignPK, 1, "Publication");
    }

    if (processIndex) {
      // getting rank of publication
      KmeliaPublication pub = KmeliaPublication.aKmeliaPublicationFromDetail(publicationDetail);
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

  private void applyVisibilityFilter() {
    List<KmeliaPublication> publications = getSessionPublicationsList();

    setSessionPublicationsList(getKmeliaBm()
        .filterPublications(publications, getComponentId(), SilverpeasRole.from(getProfile()),
            getUserId()));
  }

  private synchronized void orderPubs(int sortType) {
    sessionPublicationsList = sort(getSessionPublicationsList(), sortType);
  }

  public synchronized void orderPubsToValidate(String sortType) {
    int sort = Integer.parseInt(defaultSortValue);
    if (StringUtil.isDefined(sortType)) {
      sort = Integer.parseInt(sortType);
    }
    List<KmeliaPublication> publications =
        sort(getKmeliaBm().getPublicationsToValidate(getComponentId(), getUserId()), sort);
    sessionPublicationsList = publications;
  }

  private List<KmeliaPublication> sort(Collection<KmeliaPublication> publications, int sortType) {
    if (publications == null) {
      return null;
    }
    List<KmeliaPublication> publicationsToSort = new ArrayList<>(publications);

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
    getPublicationBm().changePublicationsOrder(sortedPubIds, getCurrentFolderPK());
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
    return getPublicationBm().getAllPublications(new PublicationPK("useless", getComponentId()),
        publicationDefaultSorting);
  }

  public Collection<PublicationDetail> getAllPublicationsByTopic(PublicationPK pubPK,
      List<String> fatherIds) {
    Collection<PublicationDetail> result = getPublicationBm().
        getDetailsByFatherIdsAndStatus(fatherIds, pubPK, "P.pubUpdateDate desc, P.pubId desc",
            PublicationDetail.VALID_STATUS);

    return result;
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
    List<NodeDetail> nodes = getNodeBm().getSubTree(nodePK);
    for (NodeDetail node : nodes) {
      fatherIds.add(Integer.toString(node.getId()));
    }
    // création de pubPK
    PublicationPK pubPK = getPublicationPK("useless");


    Collection<PublicationDetail> allPublications = getAllPublicationsByTopic(pubPK, fatherIds);

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
    return indexOfFirstPubToDisplay;
  }

  public void setIndexOfFirstPubToDisplay(String index) {
    this.indexOfFirstPubToDisplay = Integer.parseInt(index);
  }

  public List<Comment> getAllComments(String id) {
    return getCommentService()
        .getAllCommentsOnPublication(PublicationDetail.getResourceType(), getPublicationPK(id));
  }

  public void processTopicWysiwyg(String topicId) {
    getNodeBm().processWysiwyg(getNodePK(topicId));
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
    if (getSessionPubliOrClone() != null && SilverpeasRole.writer.isInRole(getUserTopicProfile()) &&
        (isTargetValidationEnable() || isTargetMultiValidationEnable())) {
      return StringUtil.isDefined(getSessionPubliOrClone().getDetail().getTargetValidatorId());
    }
    return true;
  }

  /**
   * @param links
   * @return
   */
  public synchronized Collection<KmeliaPublication> getPublications(List<ForeignPK> links) {
    return getKmeliaBm().getPublications(links, getUserId(), true);
  }

  public synchronized boolean validatePublication(String publicationId) {
    boolean validationComplete = getKmeliaBm()
        .validatePublication(getPublicationPK(publicationId), getUserId(), false, false);
    if (validationComplete) {
      setSessionClone(null);
      refreshSessionPubliAndClone();
    }
    return validationComplete;
  }

  public synchronized boolean forcePublicationValidation(String publicationId) {
    return getKmeliaBm()
        .validatePublication(getPublicationPK(publicationId), getUserId(), true, false);
  }

  public synchronized void unvalidatePublication(String publicationId, String refusalMotive) {
    getKmeliaBm().unvalidatePublication(getPublicationPK(publicationId), getUserId(), refusalMotive,
        getValidationType());
    refreshSessionPubliAndClone();
  }

  public synchronized void suspendPublication(String publicationId, String defermentMotive) {
    getKmeliaBm().suspendPublication(getPublicationPK(publicationId), defermentMotive, getUserId());
  }

  public List<ValidationStep> getValidationSteps() {
    List<ValidationStep> steps =
        getPublicationBm().getValidationSteps(getSessionPubliOrClone().getDetail().getPK());

    // Get users who have already validate this publication
    List<String> validators = new ArrayList<>();
    for (ValidationStep step : steps) {
      step.setUserFullName(getOrganisationController().getUserDetail(step.getUserId()).
          getDisplayedName());
      validators.add(step.getUserId());
    }

    List<String> allValidators =
        getKmeliaBm().getAllValidators(getSessionPubliOrClone().getDetail().getPK());

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
    return getKmeliaBm()
        .isUserCanValidatePublication(getSessionPubliOrClone().getDetail().getPK(), getUserId());
  }

  public ValidationStep getValidationStep() {
    if (getValidationType() == KmeliaHelper.VALIDATION_TARGET_N) {
      return getPublicationBm()
          .getValidationStepByUser(getSessionPubliOrClone().getDetail().getPK(), getUserId());
    }

    return null;
  }

  public synchronized void draftOutPublication() {

    NodePK currentFolderPK = getCurrentFolderPK();
    if (isKmaxMode) {
      currentFolderPK = null;
    }
    getKmeliaBm().draftOutPublication(getSessionPublication().getDetail().getPK(), currentFolderPK,
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
      getKmeliaBm().draftInPublication(getSessionPubliOrClone().getDetail().getPK(), getUserId());
    }
    refreshSessionPubliAndClone();
  }

  private synchronized NotificationMetaData getAlertNotificationMetaData(String pubId) {
    NotificationMetaData metaData;
    if (isKmaxMode) {
      metaData = getKmeliaBm().getAlertNotificationMetaData(getPublicationPK(pubId), null,
          getUserDetail().getDisplayedName());
    } else {
      metaData = getKmeliaBm()
          .getAlertNotificationMetaData(getPublicationPK(pubId), getCurrentFolderPK(),
              getUserDetail().getDisplayedName());
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
    NotificationMetaData metaData = getKmeliaBm()
        .getAlertNotificationMetaData(getPublicationPK(pubId), documentPk, nodePK,
            getUserDetail().getDisplayedName());
    metaData.setSender(getUserId());
    return metaData;
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
    List<ForeignPK> infoLinks = new ArrayList<>();
    for (String link : links) {
      StringTokenizer tokens = new StringTokenizer(link, "-");
      infoLinks.add(new ForeignPK(tokens.nextToken(), tokens.nextToken()));
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
    this.sessionPublicationsList = (publications == null ? null : new ArrayList<>(publications));
    if (sort) {
      orderPubs();
    }
  }

  public void setSessionCombination(List<String> combination) {
    this.sessionCombination = (combination == null ? null : new ArrayList<>(combination));
  }

  public void setSessionTimeCriteria(String timeCriteria) {
    this.sessionTimeCriteria = timeCriteria;
  }

  public String getSortValue() {
    return this.sortValue;
  }

  public void setSortValue(String sort) {
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
    return new NodePK(getCurrentFolderId(), getComponentId());
  }

  public NodeDetail getCurrentFolder() {
    return getNodeHeader(getCurrentFolderId());
  }

  public void setCurrentFolderId(String id, boolean resetSessionPublication) {
    if (!id.equals(currentFolderId)) {
      indexOfFirstPubToDisplay = 0;
      resetSelectedPublicationPKs();
      setSearchContext(null);
      if (!KmeliaHelper.SPECIALFOLDER_TOVALIDATE.equalsIgnoreCase(id)) {
        Collection<NodeDetail> pathColl = getTopicPath(id);
        String linkedPathString = displayPath(pathColl, true, 3);
        String pathString = displayPath(pathColl, false, 3);
        setSessionPath(linkedPathString);
        setSessionPathString(pathString);
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

  public List<KmeliaPublication> getSessionPublicationsList() {
    return this.sessionPublicationsList;
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

  public String initUPToSelectValidator(String formElementName, String formElementId) {
    String mContext = URLUtil.getApplicationURL();
    Pair<String, String> hostComponentName = new Pair<>(getComponentLabel(), "");
    Pair<String, String>[] hostPath = new Pair[1];
    hostPath[0] = new Pair<>(getString("kmelia.SelectValidator"), "");
    String hostUrl = mContext + URLUtil.getURL("useless", getComponentId()) + "SetValidator";
    String cancelUrl = mContext + URLUtil.getURL("useless", getComponentId()) + "SetValidator";

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

    // Contraintes
    if (isTargetMultiValidationEnable()) {
      sel.setMultiSelect(true);
    } else {
      sel.setMultiSelect(false);
    }
    sel.setPopupMode(true);
    sel.setSetSelectable(false);

    if (getSessionPubliOrClone() != null) {
      String[] userIds = getSessionPubliOrClone().getDetail().getTargetValidatorIds();
      sel.setSelectedElements(userIds);
    }

    // Add extra params
    SelectionUsersGroups sug = new SelectionUsersGroups();
    sug.setComponentId(getComponentId());

    List<String> profiles = new ArrayList<>();
    profiles.add(SilverpeasRole.publisher.toString());
    profiles.add(SilverpeasRole.admin.toString());

    NodeDetail node = getNodeHeader(getCurrentFolderId());
    boolean haveRights = isRightsOnTopicsEnabled() && node.haveRights();
    if (haveRights) {
      sug.setObjectId(ObjectType.NODE.getCode() + node.getRightsDependsOn());
    }
    sug.setProfileNames(profiles);

    sel.setExtraParams(sug);

    return Selection.getSelectionURL();
  }

  public String initAlertUser() {
    String pubId = getSessionPublication().getDetail().getPK().getId();

    AlertUser sel = getAlertUser();
    sel.resetAll();
    // Set space name inside browsebar
    sel.setHostSpaceName(getSpaceLabel());
    // Set componentId for selectionPeas call (filter user who can access component)
    sel.setHostComponentId(getComponentId());
    // Initialize PairObject link (component, link-to-component), only first element is used by
    // alertUserPeas
    Pair<String, String> hostComponentName = new Pair<>(getComponentLabel(), null);
    sel.setHostComponentName(hostComponentName);
    // set notification metadata to notify user
    sel.setNotificationMetaData(getAlertNotificationMetaData(pubId));
    SelectionUsersGroups sug = new SelectionUsersGroups();
    sug.setComponentId(getComponentId());
    if (!isKmaxMode && isRightsOnTopicsEnabled()) {
      NodeDetail node = getNodeHeader(getCurrentFolderId());
      if (node.haveRights()) {
        sug.setObjectId(ObjectType.NODE.getCode() + node.getRightsDependsOn());
      }
    }
    sel.setSelectionUsersGroups(sug);

    return AlertUser.getAlertUserURL();
  }

  public String initAlertUserAttachment(String attachmentOrDocumentId) {
    initAlertUser();
    AlertUser sel = getAlertUser();
    String pubId = getSessionPublication().getDetail().getPK().getId();
    sel.setNotificationMetaData(getAlertNotificationMetaData(pubId, attachmentOrDocumentId));
    return AlertUser.getAlertUserURL();
  }

  public void toRecoverUserId() {
    Selection sel = getSelection();
    idSelectedUser =
        SelectionUsersGroups.getDistinctUserIds(sel.getSelectedElements(), sel.getSelectedSets());
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

  public boolean isWriterApproval(String pubId) {
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
      silverObjectId = getKmeliaBm().getSilverObjectId(getPublicationPK(objectId));
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return silverObjectId;
  }

  private boolean isPublicationClassifiedOnPDC(String pubId) {
    if (pubId != null && pubId.length() > 0) {
      try {
        int silverObjectId = getKmeliaBm().getSilverObjectId(getPublicationPK(pubId));
        List<ClassifyPosition> positions =
            getPdcManager().getPositions(silverObjectId, getComponentId());
        return !positions.isEmpty();
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaSessionController.isPublicationClassifiedOnPDC()",
            SilverpeasRuntimeException.ERROR, "kmelia.MSG_ERR_GENERAL", e);
      }
    }
    return false;
  }

  public boolean isCurrentPublicationHaveContent() throws WysiwygException {
    return (StringUtil.isDefined(WysiwygController.load(getComponentId(), getSessionPublication().
        getId(), getCurrentLanguage())) ||
        !isInteger(getSessionPublication().getCompleteDetail().getPublicationDetail().getInfoId()));
  }

  public boolean isPDCClassifyingMandatory() {
    try {
      return getPdcManager().isClassifyingMandatory(getComponentId());
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.isPDCClassifyingMandatory()",
          SilverpeasRuntimeException.ERROR, "kmelia.MSG_ERR_GENERAL", e);
    }
  }

  /**
   * @return
   */
  public PdcManager getPdcManager() {
    if (pdcManager == null) {
      pdcManager = new GlobalPdcManager();
    }
    return pdcManager;
  }

  public NodeService getNodeBm() {
    try {
      return NodeService.get();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.getNodeService()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_NODEBM_HOME", e);
    }
  }

  public PublicationService getPublicationBm() {
    return ServiceProvider.getService(PublicationService.class);
  }

  /**
   * @return
   */
  public NotificationManager getNotificationManager() {
    if (notificationManager == null) {
      notificationManager = NotificationManager.get();
    }
    return notificationManager;
  }

  /**
   * @param fileUploaded : File uploaded in temp directory
   * @param fileType
   * @param importMode
   * @param draftMode
   * @param versionType
   * @return a report of the import
   * @throws ImportExportException
   */
  public ImportReport importFile(File fileUploaded, String fileType, String importMode,
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
      throw new KmeliaRuntimeException("KmeliaSessionController.isPublicationDeleted()",
          SilverpeasRuntimeException.ERROR, "kmelia.MSG_ERR_GENERAL", e);
    }
    return isPublicationDeleted;
  }

  public void setModelUsed(String[] models) {
    String objectId = getCurrentFolderId();
    getKmeliaBm().setModelUsed(models, getComponentId(), objectId);
  }

  public List<String> getModelUsed() {
    List models = new ArrayList();
    String formNameAppLevel = getXMLFormNameForPublications();
    if (StringUtil.isDefined(formNameAppLevel)) {
      models.add(formNameAppLevel);
    } else {
      String objectId = getCurrentFolderId();
      models.addAll(getKmeliaBm().getModelUsed(getComponentId(), objectId));
    }
    return models;
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
    return getKmeliaBm().getAxis(getComponentId());
  }

  public synchronized List<NodeDetail> getAxisHeaders() {
    return getKmeliaBm().getAxisHeaders(getComponentId());
  }

  public synchronized NodePK addAxis(NodeDetail axis) {
    return getKmeliaBm().addAxis(axis, getComponentId());
  }

  public synchronized NodeDetail getNodeHeader(String id) {
    return getKmeliaBm().getNodeHeader(id, getComponentId());
  }

  public synchronized void updateAxis(NodeDetail axis) {
    getKmeliaBm().updateAxis(axis, getComponentId());
  }

  public synchronized void deleteAxis(String axisId) {
    getKmeliaBm().deleteAxis(axisId, getComponentId());
  }

  public synchronized List<KmeliaPublication> search(List<String> combination) {
    this.sessionPublicationsList =
        new ArrayList<>(getKmeliaBm().search(combination, getComponentId()));
    applyVisibilityFilter();
    return getSessionPublicationsList();
  }

  public synchronized List<KmeliaPublication> search(List<String> combination, int nbDays) {
    this.sessionPublicationsList =
        new ArrayList<>(getKmeliaBm().search(combination, nbDays, getComponentId()));
    applyVisibilityFilter();
    return getSessionPublicationsList();
  }

  public synchronized List<KmeliaPublication> getUnbalancedPublications() {
    return (List<KmeliaPublication>) getKmeliaBm().getUnbalancedPublications(getComponentId());
  }

  public synchronized NodePK addPosition(String fatherId, NodeDetail position) {

    return getKmeliaBm().addPosition(fatherId, position, getComponentId(), getUserId());
  }

  public synchronized void updatePosition(NodeDetail position) {
    getKmeliaBm().updatePosition(position, getComponentId());
  }

  public synchronized void deletePosition(String positionId) {
    getKmeliaBm().deletePosition(positionId, getComponentId());
  }

  /*
   * /* Kmax - Publications
   */

  /**
   * **********************************************************************************
   */
  public synchronized KmeliaPublication getKmaxCompletePublication(String pubId) {
    return getKmeliaBm().getKmaxPublication(pubId, getUserId());
  }

  public synchronized Collection<Coordinate> getPublicationCoordinates(String pubId) {
    return getKmeliaBm().getPublicationCoordinates(pubId, getComponentId());
  }

  public synchronized void addPublicationToCombination(String pubId, List<String> combination) {
    getKmeliaBm().addPublicationToCombination(pubId, combination, getComponentId());
  }

  public synchronized void deletePublicationFromCombination(String pubId, String combinationId) {
    getKmeliaBm().deletePublicationFromCombination(pubId, combinationId, getComponentId());
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
    return getKmeliaBm().getPath(positionId, getComponentId());
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
  }

  public String initUserPanelForTopicProfile(String role, String nodeId, String[] groupIds,
      String[] userIds) {
    String m_context = URLUtil.getApplicationURL();
    Pair<String, String>[] hostPath = new Pair[1];
    hostPath[0] = new Pair<>(getString("kmelia.SelectValidator"), "");

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(getSpaceLabel());
    sel.setHostComponentName(new Pair<>(getComponentLabel(), ""));
    sel.setHostPath(hostPath);

    String hostUrl = m_context + URLUtil.getURL("useless", getComponentId()) +
        "TopicProfileSetUsersAndGroups?Role=" + role + "&NodeId=" + nodeId;
    String cancelUrl = m_context + URLUtil.getURL("useless", getComponentId()) + "CloseWindow";

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);
    sel.setPopupMode(true);
    sel.setHtmlFormElementId("roleItems");
    sel.setHtmlFormName("dummy");

    List<ProfileInst> profiles =
        getAdmin().getProfilesByObject(nodeId, ObjectType.NODE.getCode(), getComponentId());
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
    getNodeBm().updateRightsDependency(topic);

    profile.removeAllGroups();
    profile.removeAllUsers();
    profile.setGroupsAndUsers(groupIds, userIds);

    if (StringUtil.isDefined(profile.getId())) {
      if (profile.isEmpty()) {
        deleteTopicRole(profile.getId());
      } else {
        // Update the profile
        getAdmin().updateProfileInst(profile);
      }
    } else {
      profile.setObjectId(Integer.parseInt(nodeId));
      profile.setObjectType(ObjectType.NODE.getCode());
      profile.setComponentFatherId(getComponentId());
      // Create the profile
      getAdmin().addProfileInst(profile);
    }
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
    List<ProfileInst> alShowProfile = new ArrayList<>();
    String[] asAvailProfileNames = getAdmin().getAllProfilesNames("kmelia");
    for (String asAvailProfileName : asAvailProfileNames) {
      ProfileInst profile = getTopicProfile(asAvailProfileName, topicId);
      profile.setLabel(
          getAdmin().getProfileLabelfromName("kmelia", asAvailProfileName, getLanguage()));
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
    List<UserDetail> res = new ArrayList<UserDetail>();
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
    return getKmeliaBm().isUserCanValidate(getComponentId(), getUserId());
  }

  public boolean isUserCanWrite() {
    return getKmeliaBm().isUserCanWrite(getComponentId(), getUserId());
  }

  public void copyPublication(String pubId) throws ClipboardException {
    PublicationDetail pub = getPublicationDetail(pubId);
    // Can only copy user accessed publication
    PublicationAccessController publicationAccessController =
        ServiceProvider.getService(PublicationAccessController.class);
    if (publicationAccessController.isUserAuthorized(getUserId(), pub.getPK())) {
      PublicationSelection pubSelect = new PublicationSelection(pub);
      addClipboardSelection(pubSelect);
    } else {
      SilverLogger.getLogger(this)
          .warn("Security alert from user {0} trying to copy publication {1}", getUserId(), pubId);
      throw new ClipboardException("kmelia", SilverpeasException.WARNING,
          "Security purpose, access to publication is forbidden");
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
    PublicationAccessController publicationAccessController =
        ServiceProvider.getService(PublicationAccessController.class);
    if (publicationAccessController.isUserAuthorized(getUserId(), pub.getPK())) {
      PublicationSelection pubSelect = new PublicationSelection(pub);
      pubSelect.setCutted(true);

      addClipboardSelection(pubSelect);
    } else {
      SilverLogger.getLogger(this)
          .warn("Security alert from user {0} trying to cut publication {1}", getUserId(), pubId);
      throw new ClipboardException("kmelia", SilverpeasException.WARNING,
          "Security purpose, access to publication is forbidden");
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
    NodeAccessController nodeAccessController =
        ServiceProvider.getService(NodeAccessController.class);
    if (nodeAccessController.isUserAuthorized(getUserId(), nodeDetail.getNodePK())) {
      NodeSelection nodeSelect = new NodeSelection(getNodeHeader(id));

      addClipboardSelection(nodeSelect);
    } else {
      SilverLogger.getLogger(this)
          .warn("Security alert from user {0} trying to copy topic {1}", getUserId(), id);
      throw new ClipboardException("kmelia", SilverpeasException.WARNING,
          "Security purpose : access to node is forbidden");
    }
  }

  public void cutTopic(String id) throws ClipboardException {
    NodeDetail nodeDetail = getNodeHeader(id);
    NodeAccessController nodeAccessController =
        ServiceProvider.getService(NodeAccessController.class);
    if (nodeAccessController.isUserAuthorized(getUserId(), nodeDetail.getNodePK())) {
      NodeSelection nodeSelect = new NodeSelection(getNodeHeader(id));
      nodeSelect.setCutted(true);

      addClipboardSelection(nodeSelect);
    } else {
      SilverLogger.getLogger(this)
          .warn("Security alert from user {0} trying to cut topic {1}", getUserId(), id);
      throw new ClipboardException("kmelia", SilverpeasException.WARNING,
          "Security purpose : access to node is forbidden");
    }
  }

  public List<Object> paste(KmeliaPasteDetail pasteDetail) throws ClipboardException {
    resetSelectedPublicationPKs();
    pasteDetail.setUserId(getUserId());
    List<Object> pastedItems = new ArrayList<Object>();
    try {
      Collection<ClipboardSelection> selectedObjects = getClipboardSelectedObjects();
      for (ClipboardSelection selection : selectedObjects) {
        if (selection == null) {
          continue;
        }
        Object pastedItem = pasteClipboardSelection(selection, pasteDetail);
        if (pastedItem != null) {
          pastedItems.add(pastedItem);
        }
      }
    } catch (ClipboardException | UnsupportedFlavorException e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.paste()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_PASTE_ERROR", e);
    }
    clipboardPasteDone();
    return pastedItems;
  }

  private Object pasteClipboardSelection(ClipboardSelection selection,
      KmeliaPasteDetail pasteDetail) throws UnsupportedFlavorException {
    NodeDetail folder = getNodeHeader(pasteDetail.getToPK().getId());
    if (selection.isDataFlavorSupported(PublicationSelection.PublicationDetailFlavor)) {
      PublicationDetail pub = (PublicationDetail) selection.getTransferData(
          PublicationSelection.PublicationDetailFlavor);
      if (selection.isCutted()) {
        movePublication(pub.getPK(), folder.getNodePK());
      } else {
        KmeliaCopyDetail copyDetail = KmeliaCopyDetail.fromPasteDetail(pasteDetail);
        getKmeliaBm().copyPublication(pub, copyDetail);
      }
      return pub;
    } else if (selection.isDataFlavorSupported(NodeSelection.NodeDetailFlavor)) {
      NodeDetail node = (NodeDetail) selection.getTransferData(NodeSelection.NodeDetailFlavor);
      // check if current topic is a subTopic of node
      boolean pasteAllowed = !node.equals(folder) && !node.isFatherOf(folder);
      if (pasteAllowed) {
        if (selection.isCutted()) {
          // move node
          getKmeliaBm().moveNode(node.getNodePK(), folder.getNodePK(), getUserId());
        } else {
          // copy node
          KmeliaCopyDetail copyDetail = KmeliaCopyDetail.fromPasteDetail(pasteDetail);
          copyDetail.setFromNodePK(node.getNodePK());
          getKmeliaBm().copyNode(copyDetail);
        }
        return node;
      }
    }
    return null;
  }

  public boolean isClipboardContainsSomeCopiedItems() {
    try {
      Collection<ClipboardSelection> clipObjects = getClipboardSelectedObjects();
      for (ClipboardSelection clipObject : clipObjects) {
        if (clipObject == null) {
          continue;
        }
        if (clipObject.isDataFlavorSupported(PublicationSelection.PublicationDetailFlavor) &&
            !clipObject.isCutted()) {
          return true;
        } else if (clipObject.isDataFlavorSupported(NodeSelection.NodeDetailFlavor) &&
            !clipObject.isCutted()) {
          return true;
          }
        }
    } catch (ClipboardException e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.paste()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_PASTE_ERROR", e);
    }
    return false;
  }

  private void movePublication(PublicationPK pubPK, NodePK nodePK) {
    try {
      NodePK currentNodePK = nodePK;
      if (currentNodePK == null) {
        // Ajoute au thème courant
        currentNodePK = getCurrentFolderPK();
      }

      getKmeliaBm().movePublication(pubPK, currentNodePK, getUserId());
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
  }

  /**
   * get languages of publication header and attachments
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
    List<String> languages = new ArrayList<>();
    List<String> attLanguages = getLanguagesOfAttachments(new ForeignPK(pubPK.getId(), pubPK.
        getInstanceId()));
    for (String language : attLanguages) {
      if (!languages.contains(language)) {
        languages.add(language);
      }
    }
    return languages;
  }

  public void setAliases(List<Alias> aliases) {
    getKmeliaBm().setAlias(getSessionPublication().getDetail().getPK(), aliases);
  }

  public void setAliases(PublicationPK pubPK, List<Alias> aliases) {
    getKmeliaBm().setAlias(pubPK, aliases);
  }

  public List<Alias> getAliases() {
    List<Alias> aliases =
        (List<Alias>) getKmeliaBm().getAlias(getSessionPublication().getDetail().getPK());

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
   */
  public List<Treeview> getComponents(List<Alias> aliases) {
    List<Treeview> result = new ArrayList<>();
    List<NodeDetail> tree = null;
    NodePK root = new NodePK(NodePK.ROOT_NODE_ID);

    List<SpaceInstLight> spaces = getOrganisationController().getSpaceTreeview(getUserId());
    for (SpaceInstLight space : spaces) {
      StringBuilder path = new StringBuilder();
      String[] componentIds =
          getOrganisationController().getAvailCompoIdsAtRoot(space.getId(), getUserId());
      for (String componentId : componentIds) {
        String instanceId = componentId;

        if (instanceId.startsWith("kmelia") &&
            (getKmeliaBm().isUserCanPublish(instanceId, getUserId()) ||
                instanceId.equals(getComponentId()))) {
          root.setComponentName(instanceId);

          if (instanceId.equals(getComponentId())) {
            tree = getKmeliaBm().getTreeview(root, null, false, false, getUserId(), false,
                isRightsOnTopicsEnabled());
          }

          if (path.length() == 0) {
            List<SpaceInstLight> sPath = getOrganisationController().getPathToSpace(space.getId());
            boolean first = true;
            for (SpaceInstLight spaceInPath : sPath) {
              if (!first) {
                path.append(" > ");
              }
              path.append(spaceInPath.getName());
              first = false;
            }
          }

          Treeview treeview = new Treeview(path.toString() + " > " +
              getOrganisationController().getComponentInstLight(instanceId).getLabel(), tree,
              instanceId);

          treeview.setNbAliases(getNbAliasesInComponent(aliases, instanceId));

          if (instanceId.equals(getComponentId())) {
            result.add(0, treeview);
          } else {
            result.add(treeview);
          }
        }
      }
    }
    return result;
  }

  public List<NodeDetail> getAliasTreeview() {
    return getAliasTreeview(getComponentId());
  }

  public List<NodeDetail> getAliasTreeview(String instanceId) {
    List<NodeDetail> tree = null;
    if (getKmeliaBm().isUserCanPublish(instanceId, getUserId())) {
      NodePK root = new NodePK(NodePK.ROOT_NODE_ID, instanceId);
      tree = getKmeliaBm().getTreeview(root, null, false, false, getUserId(), false,
          isRightsOnTopicsEnabled(instanceId));
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
   * Returns URL of single attached file for the current publication.
   * If publication contains more than one file, null is returned
   *
   * @return URL of single attached file for the current publication. Null if publication
   * contains more than one file.
   */
  public String getSingleAttachmentURLOfCurrentPublication(boolean alias) {
    PublicationPK pubPK = getSessionPublication().getDetail().getPK();
    List<SimpleDocument> attachments = AttachmentServiceProvider.getAttachmentService().
        listDocumentsByForeignKey(pubPK, getLanguage());
    if (attachments.size() == 1) {
      SimpleDocument document = attachments.get(0);
      return getDocumentVersionURL(document, alias);
    }
    return null;
  }

  /**
   * Return the url to access the file
   * @param fileId the id of the file (attachment or document id).
   * @return the url to the file.
   */
  public String getAttachmentURL(String fileId, boolean alias) {
    SimpleDocument attachment = AttachmentServiceProvider.getAttachmentService().
        searchDocumentById(new SimpleDocumentPK(fileId), getLanguage());
    return getDocumentVersionURL(attachment, alias);
  }

  /**
   * Returns URL of the right version of the given document according to current folder rights
   * if user is a reader, returns last public version (null if it does not exist)
   * if user is not a reader, returns last version (public or working one)
   * @param document
   * @return the URL of right version or null
   */
  private String getDocumentVersionURL(SimpleDocument document, boolean alias) {
    SimpleDocument version = document.getLastPublicVersion();
    if (document.getVersionMaster().canBeAccessedBy(getUserDetail())) {
      version = document.getVersionMaster();
    }
    if (version != null) {
      if (alias) {
        return version.getAliasURL();
      }
      return URLUtil.getApplicationURL() + version.getAttachmentURL();
    }
    return null;
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
  }

  public boolean isTopicHaveUpdateChainDescriptor() {
    return isTopicHaveUpdateChainDescriptor(null);
  }

  public boolean isTopicHaveUpdateChainDescriptor(String id) {
    String currentId = id;
    boolean haveDescriptor = false;
    // control if file exists
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
    return getSettings().getString("updateChainRepository") + getComponentId() + "_" + topicId +
        ".xml";
  }

  public synchronized List<NodeDetail> getSubTopics(String rootId) {
    return getNodeBm().getSubTree(getNodePK(rootId));
  }

  public List<NodeDetail> getUpdateChainTopics() {
    List<NodeDetail> topics = new ArrayList<>();
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

  public void initUpdateChainDescriptor()
      throws IOException, ClassNotFoundException, ParserConfigurationException {
    XStream xstream = new XStream(new DomDriver());
    xstream.alias("fieldDescriptor", FieldUpdateChainDescriptor.class);
    xstream.alias("updateChain", UpdateChainDescriptor.class);
    xstream.alias("parameter", FieldParameter.class);

    File descriptorFile = new File(getUpdateChainDescriptorFilename(getCurrentFolderId()));
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
      new AttachmentImportExport(getUserDetail())
          .getAttachments(pubPK, subDirPath, "useless", null);

      String zipFileName = FileRepositoryManager.getTemporaryPath() + fileName + ".zip";
      // zip PDF and files
      ZipUtil.compressPathToZip(subDirPath, zipFileName);

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
      return WysiwygController.load(getComponentId(), "Node_" + currentId, getCurrentLanguage());
    }
    return "";
  }

  public String getWysiwygOnTopic() {
    return getWysiwygOnTopic(null);
  }

  public List<NodeDetail> getTopicPath(String topicId) {
    try {
      List<NodeDetail> pathInReverse =
          (List<NodeDetail>) getNodeBm().getPath(new NodePK(topicId, getComponentId()));
      Collections.reverse(pathInReverse);
      return pathInReverse;
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.getTopicPath()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DAVOIR_LE_CHEMIN_COURANT", e);
    }
  }

  public ThumbnailSettings getThumbnailSettings() {
    int width = getSettings().getInteger("vignetteWidth", -1);
    int height = getSettings().getInteger("vignetteHeight", -1);
    ThumbnailSettings settings = ThumbnailSettings.getInstance(getComponentId(), width, height);
    return settings;
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
   * @param query the query
   * @return List of Kmelia publications
   */
  public synchronized List<KmeliaPublication> search(String query) {

    SearchContext previousSearch = getSearchContext();
    boolean newSearch =
        previousSearch == null || !previousSearch.getQuery().equalsIgnoreCase(query);
    if (!newSearch) {
      // process cached results
      return getSessionPublicationsList();
    }

    // Insert this new search inside persistence layer in order to compute statistics
    TopicSearch newTS = new TopicSearch(getComponentId(), Integer.parseInt(getCurrentFolderId()),
        Integer.parseInt(getUserId()), getLanguage(), query.toLowerCase(), new Date());
    KmeliaSearchServiceProvider.getTopicSearchService().createTopicSearch(newTS);

    List<KmeliaPublication> userPublications = new ArrayList<>();
    QueryDescription queryDescription = new QueryDescription(query);
    queryDescription.setSearchingUser(getUserId());
    queryDescription.setRequestedFolder(getCurrentFolder().getFullPath());
    queryDescription.addComponent(getComponentId());

    try {

      List<MatchingIndexEntry> results =
          SearchEngineProvider.getSearchEngine().search(queryDescription).getEntries();

      List<String> pubIds = new ArrayList<>();
      KmeliaAuthorization security = new KmeliaAuthorization();
      security.enableCache();
      for (MatchingIndexEntry result : results) {
        if ("Publication".equals(result.getObjectType())) {
          // Add the publications
          // return publication if user can consult it only (check rights on folder)
          if (security.isObjectAvailable(getComponentId(), getUserId(), result.getObjectId(),
              "Publication")) {
            pubIds.add(result.getObjectId());
          }
        }
      }
      for (String pubId : pubIds) {
        KmeliaPublication publication =
            KmeliaPublication.aKmeliaPublicationFromDetail(getPublicationDetail(pubId));
        userPublications.add(publication);
      }
    } catch (Exception pe) {
      throw new KmeliaRuntimeException("KmeliaSessionController.search",
          SilverpeasRuntimeException.ERROR, "root.EX_SEARCH_ENGINE_FAILED", pe);
    }

    // store "in session" current search context
    SearchContext aSearchContext = new SearchContext(query);
    setSearchContext(aSearchContext);

    // store results and keep search results order
    setSessionPublicationsList(userPublications, false);

    return userPublications;
  }

  /**
   * @return the list of SpaceInst from current space identifier (in session) to root space <br/>
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
      List<NodeDetail> path =
          (List<NodeDetail>) getNodeBm().getPath(getCurrentFolder().getNodePK());
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
          throw new KmeliaRuntimeException("KmeliaSessionController.getSupportedFormats()",
              SilverpeasException.ERROR, "kmelia.EX_UNKNOWN_EXPORT_FORMAT");
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
        PublicationAccessController publicationAccessController =
            ServiceProvider.getService(PublicationAccessController.class);
        if (publicationAccessController.isUserAuthorized(getUserId(), pk,
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
    List<KmeliaPublication> toCheck = getKmeliaBm()
        .getLatestPublications(getComponentId(), getNbPublicationsOnRoot(),
            isRightsOnTopicsEnabled(), getUserId());
    for (KmeliaPublication aPublication : toCheck) {
      if (!isPublicationDeleted(aPublication.getPk())) {
        publicationsToDisplay.add(aPublication);
      }
    }
    return publicationsToDisplay;
  }

  public List<KmeliaPublication> getPublicationsOfCurrentFolder() {
    List<KmeliaPublication> publications;
    if (!KmeliaHelper.SPECIALFOLDER_TOVALIDATE.equalsIgnoreCase(currentFolderId)) {
      publications = getKmeliaBm()
          .getPublicationsOfFolder(new NodePK(currentFolderId, getComponentId()),
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

  public String manageSubscriptions() {
    SubscriptionContext subscriptionContext = getSubscriptionContext();
    List<NodeDetail> nodePath = getTopicPath(getCurrentFolderId());
    nodePath.remove(0);
    subscriptionContext
        .initializeFromNode(NodeSubscriptionResource.from(getCurrentFolderPK()), nodePath);
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

    //Unitary mode
    if (UNITARY_IMPORT_MODE.equals(importMode)) {
      MassiveReport massiveReport = componentRpt.getListMassiveReports().get(0);
      UnitReport unitReport = massiveReport.getListUnitReports().get(0);
      if (unitReport.getError() == UnitReport.ERROR_NO_ERROR) {
        return null;
      } else if (unitReport.getError() == UnitReport.ERROR_FILE_SIZE_EXCEEDS_LIMIT) {
        message = getMaxSizeErrorMessage(attachmentResourceLocator);
      } else {
        message = attachmentResourceLocator.getString("liaisonInaccessible");
      }
    } //Massive mode, one publication
    else if (MASSIVE_IMPORT_MODE_ONE_PUBLICATION.equals(importMode)) {
      MassiveReport massiveReport = componentRpt.getListMassiveReports().get(0);
      UnitReport unitReport = massiveReport.getListUnitReports().get(0);
      if (unitReport.getError() == UnitReport.ERROR_NO_ERROR) {
        return null;
      } else if (unitReport.getError() == UnitReport.ERROR_FILE_SIZE_EXCEEDS_LIMIT) {
        message = getMaxSizeErrorMessage(attachmentResourceLocator);
      } else {
        message = attachmentResourceLocator.getString("liaisonInaccessible");
      }

      //Massive mode, several publications
    } else if (MASSIVE_IMPORT_MODE_MULTI_PUBLICATIONS.equals(importMode)) {
      MassiveReport massiveReport = componentRpt.getListMassiveReports().get(0);
      for (UnitReport unitReport : massiveReport.getListUnitReports()) {
        if (unitReport.getError() == UnitReport.ERROR_FILE_SIZE_EXCEEDS_LIMIT) {
          message = getMaxSizeErrorMessage(attachmentResourceLocator);
        } else if (unitReport.getError() != UnitReport.ERROR_NO_ERROR) {
          return attachmentResourceLocator.getString("liaisonInaccessible");
        }
      }
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
    List<PublicationDetail> listPublicationDetail = new ArrayList<PublicationDetail>();
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
    return getKmeliaBm().getLastAccess(pk, getCurrentFolderPK(), getUserId());
  }

  public void setPublicationValidator(String userIds) {
    getKmeliaBm().setValidators(getSessionPubliOrClone().getDetail().getPK(), userIds);
    refreshSessionPubliAndClone();
  }

  /**
   * Check user access right on folder
   * @param nodeId the topic/folder identifier to check
   * @return true if current user has admin access on topic given in parameter
   */
  public boolean isTopicAdmin(final String nodeId) {
    return SilverpeasRole.getHighestFrom(SilverpeasRole.from(getUserTopicProfile(nodeId)))
        .isGreaterThanOrEquals(SilverpeasRole.admin);
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
      PersistentResourceDateReminder savedDateReminder = getDateReminderService().create(publicationNoteReference, dateReminderDetail);
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
        PersistentResourceDateReminder savedDateReminder = getDateReminderService().set(publicationNoteReference, dateReminderDetail);

      } else {//Delete reminder
        getDateReminderService().remove(publicationNoteReference);
      }
    } else {//Create reminder
      createResourceDateReminder(pubDetail, dateReminderDate, messageReminder);
    }
  }

  public boolean isTemplatesSelectionEnabledForRole(SilverpeasRole role) {
    return !isXMLFormEnabledForPublications() && isContentEnabled() &&
        SilverpeasRole.admin.equals(role);
  }

  private String getXMLFormNameForPublications() {
    return getComponentParameterValue("XmlFormForPublis");
  }

  private boolean isXMLFormEnabledForPublications() {
    return StringUtil.isDefined(getXMLFormNameForPublications());
  }

  public Form getXmlFormForPublications() {
    Form formUpdate = null;
    List<String> forms = getModelUsed();
    if (forms.size() == 1 && !"WYSIWYG".equals(forms.get(0))) {
      String form = forms.get(0);
      String formShort = form.substring(0, form.indexOf('.'));
      try {
        // register xmlForm to publication
        getPublicationTemplateManager()
            .addDynamicPublicationTemplate(getComponentId() + ":" + formShort, form);

        PublicationTemplateImpl pubTemplate = (PublicationTemplateImpl) getPublicationTemplateManager()
            .getPublicationTemplate(getComponentId() + ':' + formShort, form);
        formUpdate = pubTemplate.getUpdateForm();
        RecordSet recordSet = pubTemplate.getRecordSet();

        DataRecord record = recordSet.getEmptyRecord();

        formUpdate.setData(record);
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
    return formUpdate;
  }

  public void saveXMLFormToPublication(PublicationDetail pubDetail, List<FileItem> items,
      boolean forceUpdatePublication) throws org.silverpeas.core.SilverpeasException {
    String xmlFormShortName;

    // Is it the creation of the content or an update ?
    String infoId = pubDetail.getInfoId();
    if (infoId == null || "0".equals(infoId)) {
      xmlFormShortName = FileUploadUtil.getParameter(items, "FormName");

      // The publication have no content
      // We have to register xmlForm to publication
      pubDetail.setInfoId(xmlFormShortName);
      updatePublication(pubDetail);
    } else {
      xmlFormShortName = pubDetail.getInfoId();
    }
    String pubId = pubDetail.getPK().getId();

    try {
      PublicationTemplate pub = getPublicationTemplateManager()
          .getPublicationTemplate(getComponentId() + ":" + xmlFormShortName);
      RecordSet set = pub.getRecordSet();
      Form form = pub.getUpdateForm();
      String language = pubDetail.getLanguageToDisplay(getCurrentLanguage());
      DataRecord data = set.getRecord(pubId, language);
      if (data == null || (language != null && !language.equals(data.getLanguage()))) {
        // This publication haven't got any content at all or for requested language
        data = set.getEmptyRecord();
        data.setId(pubId);
        data.setLanguage(language);
      }
      PagesContext context = new PagesContext();
      context.setLanguage(getLanguage());
      context.setComponentId(getComponentId());
      context.setUserId(getUserId());
      context.setEncoding(CharEncoding.UTF_8);
      if (!isKmaxMode) {
        context.setNodeId(getCurrentFolderId());
      }
      context.setObjectId(pubId);
      context.setContentLanguage(getCurrentLanguage());

      form.update(items, data, context);
      set.save(data);
    } catch (Exception e) {
      throw new org.silverpeas.core.SilverpeasException("Can't save XML form of publication", e);
    }

    String volatileId = FileUploadUtil.getParameter(items, "VolatileId");
    if (StringUtil.isDefined(volatileId)) {
      // Attaching all documents linked to volatile publication to the persisted one
      List<SimpleDocumentPK> movedDocumentPks = AttachmentServiceProvider.getAttachmentService()
          .moveAllDocuments(getPublicationPK(volatileId), pubDetail.getPK());
      if (!movedDocumentPks.isEmpty()) {
        // Change images path in wysiwyg
        WysiwygController.wysiwygPlaceHaveChanged(getComponentId(), volatileId,
            getComponentId(), pubId);
      }
    }

    if (forceUpdatePublication) {
      // update publication to change updateDate and updaterId
      updatePublication(pubDetail);
    }
  }

  public void saveXMLForm(List<FileItem> items, boolean forceUpdatePublication)
      throws org.silverpeas.core.SilverpeasException {
    if (isCloneNeeded()) {
      clonePublication();
    }

    PublicationDetail pubDetail = getSessionPubliOrClone().getDetail();
    saveXMLFormToPublication(pubDetail, items, forceUpdatePublication);
  }

  public PublicationDetail prepareNewPublication() {
    String volatileId = newVolatileIntegerIdentifierOn(getComponentId());
    PublicationDetail publication = new PublicationDetail();
    publication.setPk(getPublicationPK(volatileId));
    return publication;
  }
}
