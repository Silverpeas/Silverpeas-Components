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
package com.stratelia.webactiv.kmelia.control;

import com.silverpeas.attachment.importExport.AttachmentImportExport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.ejb.EJBObject;
import javax.ejb.RemoveException;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.fieldDisplayer.WysiwygFCKFieldDisplayer;
import com.silverpeas.form.record.GenericRecordSetManager;
import com.silverpeas.form.record.IdentifiedRecordTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.ZipManager;
import com.silverpeas.util.clipboard.ClipboardSelection;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.versioning.importExport.VersioningImportExport;
import com.stratelia.silverpeas.alertUser.AlertUser;
import com.stratelia.silverpeas.comment.control.CommentController;
import com.stratelia.silverpeas.comment.ejb.CommentBm;
import com.stratelia.silverpeas.comment.ejb.CommentBmHome;
import com.stratelia.silverpeas.comment.ejb.CommentRuntimeException;
import com.stratelia.silverpeas.notificationManager.NotificationManager;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
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
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.beans.admin.ProfileInst;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.kmelia.FileImport;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBmHome;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
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
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.WAAttributeValuePair;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentException;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentRuntimeException;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
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
import com.stratelia.webactiv.util.publication.info.model.InfoLinkDetail;
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
import java.io.FileReader;
import javax.xml.parsers.ParserConfigurationException;


public class KmeliaSessionController extends AbstractComponentSessionController {

	/*EJBs used by sessionController */
    private SearchEngineBm          searchEngineEjb 	= null;
    private CommentBm               commentBm 			= null;
    private VersioningBm			versioningBm 		= null;
    private PdcBm					pdcBm				= null;
    private StatisticBm             statisticBm 		= null;
    private NotificationManager		notificationManager	= null;

    //Session objects
    private TopicDetail             sessionTopic = null;
    private UserCompletePublication sessionPublication = null;
    private UserCompletePublication sessionClone = null;
    private String                  sessionPath = null;			//html link with <a href="">
	private String                  sessionPathString = null;	//html string only
    private TopicDetail             sessionTopicToLink = null;
    private boolean                 sessionOwner = false;
    private	List					sessionPublicationsList = null;
    private	ArrayList				sessionCombination = null;		//Specific Kmax
    private	String					sessionTimeCriteria = null;		//Specific Kmax
    private List					sessionTreeview = null;
    
    private String					sortValue = "2";
    private String					autoRedirectURL = null;

	private int						nbPublicationsOnRoot	= -1;

	private int 					rang	= 0;
	
    private ResourceLocator         publicationSettings = null;

    public final static String TAB_PREVIEW		= "tabpreview";
    public final static String TAB_HEADER		= "tabheader";
    public final static String TAB_CONTENT		= "tabcontent";
    public final static String TAB_COMMENT		= "tabcomments";
    public final static String TAB_ATTACHMENTS	= "tabattachments";
    public final static String TAB_SEE_ALSO		= "tabseealso";
    public final static String TAB_ACCESS_PATHS = "tabaccesspaths";
    public final static String TAB_READER_LIST	= "tabreaderslist";
	public final static String TAB_PDC			= "usepdc";

    // For import files
    public final static String UNITARY_IMPORT_MODE		= "0";
	public final static String MASSIVE_IMPORT_MODE_ONE_PUBLICATION		= "1";
	public final static String MASSIVE_IMPORT_MODE_MULTI_PUBLICATIONS	= "2";
	public final static String FILETYPE_ZIP1 = "application/x-zip-compressed";
	public final static String FILETYPE_ZIP2 = "application/zip";

	//Versioning options
	public final static String VER_USE_WRITERS_AND_READERS = "0";
	public final static String VER_USE_WRITERS = "1";
	public final static String VER_USE_READERS = "2";
	public final static String VER_USE_NONE = "3";
	
	//For Office files direct update
	public final static String NO_UPDATE_MODE = "0";
	public final static String UPDATE_DIRECT_MODE = "1";
	public final static String UPDATE_SHORTCUT_MODE = "2";
		
	//utilisation de userPanel/ userpanelPeas
	String[] idSelectedUser = null;

	//pagination de la liste des publications
	private int indexOfFirstPubToDisplay	= 0;
	private int	nbPublicationsPerPage		= -1;
	
	private List publicationsToLink = new ArrayList();
	
	// Assistant de publication
	private String wizard = "none";
	private String wizardRow = "0";
	private String wizardLast = "0";

	//Specific for Kmax
	private List timeAxis = null;
	private ArrayList currentCombination = null;
	public boolean isKmaxMode = false;
	
	//i18n
	private String currentLanguage = null;
	
	private AdminController m_AdminCtrl = null;
	
	// sauvegarde pour mise à jour à la chaine
	Fields saveFields = new Fields();

	/** Creates new sessionClientController */
    public KmeliaSessionController(MainSessionController mainSessionCtrl, ComponentContext context) {
		super(mainSessionCtrl, context, "com.stratelia.webactiv.kmelia.multilang.kmeliaBundle", "com.stratelia.webactiv.kmelia.settings.kmeliaIcons", "com.stratelia.webactiv.kmelia.settings.kmeliaSettings");
		init();
	}

	private void init() {
		//Remove all data store by this SessionController
		removeSessionObjects();
		
		currentLanguage = getLanguage();
	}

    public KmeliaBm getKmeliaBm() {
    	try {
			KmeliaBmHome kscEjbHome = (KmeliaBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.KMELIABM_EJBHOME, KmeliaBmHome.class);
			return kscEjbHome.create();
		} catch (Exception e) {
			throw new KmeliaRuntimeException("KmeliaSessionController.getKmeliaBm()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT",e);
		}
    }
    
    public CommentBm getCommentBm()
    {
        if (commentBm == null)
        {
            try
            {
              CommentBmHome commentHome = (CommentBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.COMMENT_EJBHOME, CommentBmHome.class);
              commentBm =  commentHome.create();
            }
            catch (Exception e)
            {
                throw new CommentRuntimeException("KmeliaSessionController.getCommentBm()", SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
            }
        }

        return commentBm;
    }
    
    public StatisticBm getStatisticBm()
    {
        if (statisticBm == null)
        {
            try
            {
              StatisticBmHome statisticHome = (StatisticBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.STATISTICBM_EJBHOME, StatisticBmHome.class);
              statisticBm =  statisticHome.create();
            }
            catch (Exception e)
            {
                throw new StatisticRuntimeException("KmeliaSessionController.getStatisticBm()", SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
            }
        }

        return statisticBm;
    }
    
	private VersioningBm getVersioningBm()
	{
		if (versioningBm == null)
		{
			try {
				VersioningBmHome vscEjbHome = (VersioningBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.VERSIONING_EJBHOME, VersioningBmHome.class);
				versioningBm = vscEjbHome.create();
			} catch (Exception e) {
				throw new VersioningRuntimeException("KmeliaSessionController.getVersioningBm()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT",e);
			}
		}
		return versioningBm;
	}

    public ResourceLocator getPublicationSettings() {
          if (publicationSettings == null) {
              publicationSettings = new ResourceLocator("com.stratelia.webactiv.util.publication.publicationSettings", getLanguage());
          }
          return publicationSettings;
    }

    /*public ResourceLocator getSettings() {
          return settings;
    }*/

    public SearchEngineBm getSearchEngine() {
          if (this.searchEngineEjb == null) {
            try {
                  SearchEngineBmHome home = (SearchEngineBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.SEARCHBM_EJBHOME, SearchEngineBmHome.class);
                  this.searchEngineEjb = home.create();
            } catch (Exception e) {
		          throw new KmeliaRuntimeException("KmeliaSessionController.getSearchEngine()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT",e);
            }
          }
          return this.searchEngineEjb;
    }

	public int getNbPublicationsOnRoot() {
		if (nbPublicationsOnRoot == -1) {	
			String parameterValue = getComponentParameterValue("nbPubliOnRoot");
			if (StringUtil.isDefined(parameterValue))
				nbPublicationsOnRoot = new Integer(parameterValue).intValue();
			else {
				if (KmeliaHelper.isToolbox(getComponentId()))
					nbPublicationsOnRoot = 0;
				else
				{
					//lecture du properties
					nbPublicationsOnRoot = SilverpeasSettings.readInt(getSettings(), "HomeNbPublications", 15);
				}
			}
		}
		return nbPublicationsOnRoot;
	}

	public int getNbPublicationsPerPage() {
		if (nbPublicationsPerPage == -1) {
			String parameterValue = this.getComponentParameterValue("nbPubliPerPage");
			if (parameterValue == null || parameterValue.length() <= 0)
				nbPublicationsPerPage = SilverpeasSettings.readInt(getSettings(), "NbPublicationsParPage", 10);
			else {
				try
				{
					nbPublicationsPerPage = new Integer(parameterValue).intValue();	
				}
				catch (Exception e)
				{
					nbPublicationsPerPage = SilverpeasSettings.readInt(getSettings(), "NbPublicationsParPage", 10);
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
    	if (!StringUtil.isDefined(param))
    		return true;
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
	
	public boolean isTreeviewEnabled()
	{
		return "yes".equalsIgnoreCase(getComponentParameterValue("useTreeview"));
	}

	public boolean isImportFileAllowed() {
		String parameterValue = this.getComponentParameterValue("importFiles");
		if (parameterValue == null || parameterValue.length() <= 0)	{
			return false;
		} else {
			if ("1".equalsIgnoreCase(parameterValue) || "3".equalsIgnoreCase(parameterValue))
				return true;
			else
				return false;
		}
    }

	public boolean isImportFilesAllowed() {
		String parameterValue = this.getComponentParameterValue("importFiles");
		if (parameterValue == null || parameterValue.length() <= 0)	{
			return false;
		} else {
			if ("2".equalsIgnoreCase(parameterValue) || "3".equalsIgnoreCase(parameterValue))
				return true;
			else
				return false;
		}
    }
	
	public boolean isExportZipAllowed() {
		String parameterValue = this.getComponentParameterValue("exportComponent");
		if (parameterValue == null || parameterValue.length() <= 0)	{
			return false;
		} else {
			if ("yes".equalsIgnoreCase(parameterValue) || "both".equalsIgnoreCase(parameterValue))
				return true;
			else
				return false;
		}
	}
	
	public boolean isExportPdfAllowed() {
		String parameterValue = this.getComponentParameterValue("exportComponent");
		if (parameterValue == null || parameterValue.length() <= 0)	{
			return false;
		} else {
			if ("pdf".equalsIgnoreCase(parameterValue) || "both".equalsIgnoreCase(parameterValue))
				return true;
			else
				return false;
		}
	}
	

	public boolean isUpdateOfficeFilesUsed() {
		String parameterValue = this.getComponentParameterValue("useModifyOfficeFiles");
		if (parameterValue == null || parameterValue.length() <= 0)	{
			return false;
		} else {
			if ("1".equalsIgnoreCase(parameterValue))
				return true;
			else if ("2".equalsIgnoreCase(parameterValue))
				return true;
			else
				return false;
		}
    }

	public String getUpdateOfficeMode() {
		String parameterValue = this.getComponentParameterValue("useModifyOfficeFiles");
		if (parameterValue == null || parameterValue.length() <= 0)	{
			return NO_UPDATE_MODE;
		} else {
			if (UPDATE_DIRECT_MODE.equalsIgnoreCase(parameterValue))
				return UPDATE_DIRECT_MODE;
			else if (UPDATE_SHORTCUT_MODE.equalsIgnoreCase(parameterValue))
				return UPDATE_SHORTCUT_MODE;
			else
				return NO_UPDATE_MODE;
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
	
	public boolean isRightsOnTopicsEnabled()
	{
		return "yes".equalsIgnoreCase(getComponentParameterValue("rightsOnTopics"));
	}
	
	public boolean isFoldersLinkedEnabled()
	{
		return "yes".equals(getComponentParameterValue("isLink"));
	}
	
	public boolean attachmentsInPubList()
	{
		return "yes".equals(getComponentParameterValue("attachmentsInPubList"));
	}
	
	public boolean isPublicationIdDisplayed()
	{
		return "yes".equals(getComponentParameterValue("codification"));
	}
	
	public boolean isSuppressionOnlyForAdmin()
	{
		return "yes".equals(getComponentParameterValue("suppressionOnlyForAdmin"));
	}
	
	public boolean isContentEnabled()
	{
		String parameterValue = getComponentParameterValue("tabContent");
		if (!StringUtil.isDefined(parameterValue))	{
			return false;
		} else {
			return"yes".equals(parameterValue.toLowerCase());
		}
	}
	
	public boolean isSeeAlsoEnabled()
	{
		String parameterValue = getComponentParameterValue("tabSeeAlso");
		if (!StringUtil.isDefined(parameterValue))	{
			return false;
		} else {
			return"yes".equals(parameterValue.toLowerCase());
		}
	}
	
	public boolean showUserNameInList()
	{
		return SilverpeasSettings.readBoolean(getSettings(), "showUserNameInList", true);
	}

	/**
	 * 
	 * @return
	 */
	public String getVersionningFileRightsMode() {
		String parameterValue = this.getComponentParameterValue("versionUseFileRights");
		if (parameterValue == null || parameterValue.length() <= 0)	{
			return VER_USE_WRITERS_AND_READERS;
		}
		else
			return parameterValue;
    }

    public ArrayList getInvisibleTabs() throws RemoteException {
	    ArrayList invisibleTabs = new ArrayList(0);
	    
	    if (!isPdcUsed())
	    	invisibleTabs.add("usepdc");

		if (!isContentEnabled())
			invisibleTabs.add("tabcontent");
		
		if (isToolbox())
			invisibleTabs.add(KmeliaSessionController.TAB_PREVIEW);

		String parameterValue = this.getComponentParameterValue("tabAttachments");
		if (!isToolbox())
		{
			//attachments tab is always visible with toolbox
			if (!StringUtil.isDefined(parameterValue))	{
				//invisibleTabs.add("tabattachments");
			} else {
				if (!"yes".equals(parameterValue.toLowerCase()))
					invisibleTabs.add("tabattachments");
			}
		}
		
		if (!isSeeAlsoEnabled())
			invisibleTabs.add("tabseealso");

		parameterValue = this.getComponentParameterValue("tabAccessPaths");
		if (parameterValue == null || parameterValue.length() <= 0)	{
			//invisibleTabs.add("tabaccesspaths");
		} else {
			if (!"yes".equals(parameterValue.toLowerCase()))
				invisibleTabs.add("tabaccesspaths");
		}

		parameterValue = this.getComponentParameterValue("tabReadersList");
		if (parameterValue == null || parameterValue.length() <= 0)	{
			//invisibleTabs.add("tabreaderslist");
		} else {
			if (!"yes".equals(parameterValue.toLowerCase()))
				invisibleTabs.add("tabreaderslist");
		}

		parameterValue = this.getComponentParameterValue("tabComments");
		if (parameterValue == null || parameterValue.length() <= 0)	{
			invisibleTabs.add("tabcomments");
		} else {
			if (!"yes".equals(parameterValue.toLowerCase()))
				invisibleTabs.add("tabcomments");
		}

        return invisibleTabs;
	}

    @SuppressWarnings("unchecked")
	public String generatePdf(String pubID)
    {
        SilverTrace.info("kmelia", "KmeliaSessionControl.generatePdf", "root.MSG_ENTRY_METHOD");
        String nameFilePdf = "";
        try
        {
            if (pubID != null)
            {
            	CompletePublication complete = null;
                try
                {
                   complete = getKmeliaBm().getPublicationBm().getCompletePublication(getPublicationPK(pubID));
                }
                catch (Exception e)
                {
                    SilverTrace.info("kmelia", "KmeliaSessionControl.generatePdf", "kmelia.MSG_RETURN_COMPLETE_LIST_OF_PUBLI", "pubId="+pubID);
                }
                TopicDetail topic = getPublicationTopic(pubID);
                if(complete != null && topic != null) {
                	List<SpaceInst> listSpaces = (List<SpaceInst>) this.getOrganizationController().getSpacePath(this.getSpaceId());
                   	for(SpaceInst space : listSpaces) {
                		nameFilePdf += space.getName(getLanguage())+"-";
                	}
                   	nameFilePdf = this.getComponentLabel();
                   	Collection<NodeDetail> path = (Collection<NodeDetail>) topic.getPath();
                   	for(NodeDetail node : path) {
                   		nameFilePdf = nameFilePdf+"-"+ node.getName(getCurrentLanguage());
                   	}
					nameFilePdf = 	nameFilePdf + "-" + complete.getPublicationDetail().getTitle();
					//encodage nom du pdf
					nameFilePdf = nameFilePdf.replaceAll("\\\\", "");
					nameFilePdf = nameFilePdf.replaceAll("/", "");
					nameFilePdf = nameFilePdf.replaceAll(":", "");
					nameFilePdf = nameFilePdf.replaceAll("\\*", "");
					nameFilePdf = nameFilePdf.replaceAll("\\?", "");
					nameFilePdf = nameFilePdf.replaceAll("\"", "");
					nameFilePdf = nameFilePdf.replaceAll("<", "");
					nameFilePdf = nameFilePdf.replaceAll(">", "");
					nameFilePdf = nameFilePdf.replaceAll("&", "");
					nameFilePdf = nameFilePdf.replaceAll("'", "");
					nameFilePdf = nameFilePdf.replaceAll("#", "");
					nameFilePdf = nameFilePdf.replaceAll("%", "");
					nameFilePdf = nameFilePdf.replace(' ', '_');
					nameFilePdf = nameFilePdf.replace('\'', '_');
					nameFilePdf = nameFilePdf.replace('#', '_');
					nameFilePdf = nameFilePdf.replace('%', '_');
					nameFilePdf = nameFilePdf.replace('+', '_');
					nameFilePdf = nameFilePdf.replace('|', '_');
					//path file doit ?tre <= 254 caract?res
					ResourceLocator resourceGeneral = new ResourceLocator("com.stratelia.webactiv.general", "");
		            String arbo = resourceGeneral.getString("tempPath");
		            String pathFile = arbo+nameFilePdf+"-"+pubID+".pdf";
		            if(pathFile.length()>254) {
		            	nameFilePdf = nameFilePdf.substring(0, 254-5-arbo.length()-pubID.length());
		            }
		            nameFilePdf += "-"+pubID+".pdf";
                	PdfGenerator.generate(nameFilePdf, complete, this);
                }
            }
        }
        catch (Exception e)
        {
        	throw new KmeliaRuntimeException("KmeliaSessionController.setKmeliaBm()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT",e);
        }
        return nameFilePdf;
    }


    /************************************************************************************************/
    // Current User operations
    /************************************************************************************************/   
	public String getProfile() throws RemoteException {
		//return KmeliaHelper.getProfile(getUserRoles());
		
		return getUserTopicProfile();
	}
	
	public String getUserTopicProfile() throws RemoteException
	{
		return getUserTopicProfile(null);
	}
	
	public String getUserTopicProfile(String id) throws RemoteException
	{
		if (!isRightsOnTopicsEnabled())
			return KmeliaHelper.getProfile(getUserRoles());
		
		NodeDetail node = null;
		if (StringUtil.isDefined(id))
			node = getKmeliaBm().getNodeHeader(id, getComponentId());
		else if (getSessionTopic() != null)
			node = getSessionTopic().getNodeDetail();
		
		//check if we have to take care of topic's rights
		if (node != null && node.haveRights())
		{
			int rightsDependsOn = node.getRightsDependsOn();
			return KmeliaHelper.getProfile(getOrganizationController().getUserProfiles(getUserId(), getComponentId(), rightsDependsOn, ObjectType.NODE));
		}
		else
		{
			return KmeliaHelper.getProfile(getUserRoles());
		}
	}
	
	public boolean isCurrentTopicAvailable()
	{
		if (isRightsOnTopicsEnabled() && getSessionTopic().getNodeDetail().haveRights())
		{
			int rightsDependsOn = getSessionTopic().getNodeDetail().getRightsDependsOn();
			return getOrganizationController().isObjectAvailable(rightsDependsOn, ObjectType.NODE, getComponentId(), getUserId());
		}
		return true;
	}
	
	public boolean isUserComponentAdmin()
	{
		return "admin".equalsIgnoreCase(KmeliaHelper.getProfile(getUserRoles()));
	}

    /**************************************************************************************/
    /* KMelia - Gestion des thèmes                                                        */
    /**************************************************************************************/
	public synchronized TopicDetail getTopic(String id) throws RemoteException {
    	return getTopic(id, true);
    }
	
    public synchronized TopicDetail getTopic(String id, boolean resetSessionPublication) throws RemoteException {
    	if (resetSessionPublication)
    		setSessionPublication(null);
		if (getSessionTopic() == null || !id.equals(getSessionTopic().getNodeDetail().getNodePK().getId()))
			indexOfFirstPubToDisplay = 0;
		
		TopicDetail currentTopic = null;
		if (isUserComponentAdmin())
			currentTopic = getKmeliaBm().goTo(getNodePK(id), getUserId(), isTreeStructure(), "admin", false);
		else
			currentTopic = getKmeliaBm().goTo(getNodePK(id), getUserId(), isTreeStructure(), getUserTopicProfile(id), isRightsOnTopicsEnabled());
		
		List treeview = null;
		if (isTreeviewEnabled() || displayNbPublis())
		{
			if (isUserComponentAdmin())
				treeview = getKmeliaBm().getTreeview(getNodePK("0"), "admin", isCoWritingEnable(), isDraftVisibleWithCoWriting(), getUserId(), displayNbPublis(), false);
			else
				treeview = getKmeliaBm().getTreeview(getNodePK("0"), getProfile(), isCoWritingEnable(), isDraftVisibleWithCoWriting(), getUserId(), displayNbPublis(), isRightsOnTopicsEnabled());
			setSessionTreeview(treeview);
		}
		if (displayNbPublis())
		{
			List children = (List) currentTopic.getNodeDetail().getChildrenDetails();
			for (int n=0; n<children.size(); n++)
			{
				NodeDetail node = (NodeDetail) children.get(n);
				if (node != null)
				{
					int index = treeview.indexOf(node);
					if (index != -1)
					{
						NodeDetail nodeTreeview = (NodeDetail) treeview.get(index);
						
						if (nodeTreeview != null)
							node.setNbObjects(nodeTreeview.getNbObjects());
					}
				}
			}
		}
		setSessionTopic(currentTopic);
		applyVisibilityFilter();
	    return currentTopic;
    }
    
    public synchronized TopicDetail getPublicationTopic(String pubId) throws RemoteException {
    	TopicDetail currentTopic = getKmeliaBm().getPublicationFather(getPublicationPK(pubId), isTreeStructure(), getUserId(), isRightsOnTopicsEnabled());
    	setSessionTopic(currentTopic);
		applyVisibilityFilter();
	    return currentTopic;
    }
    
    public synchronized List getAllTopics() throws RemoteException
    {
   		return getNodeBm().getSubTree(getNodePK("0"));
    }
    
    public synchronized List getTreeview() throws RemoteException
    {
    	if (isTreeviewEnabled())
    		return getSessionTreeview();//getKmeliaBm().getTreeview(getNodePK("0"), getProfile(), isCoWritingEnable(), isDraftVisibleWithCoWriting(), getUserId(), displayNbPublis());
    	return null;
    }

    public synchronized void flushTrashCan() throws RemoteException {
	    SilverTrace.info("kmelia", "KmeliaSessionControl.flushTrashCan", "root.MSG_ENTRY_METHOD");
	    TopicDetail td = getKmeliaBm().goTo(getNodePK("1"), getUserId(), false, getUserTopicProfile("1"), isRightsOnTopicsEnabled());
        Collection pds = td.getPublicationDetails();
        Iterator ipds = pds.iterator();

        SilverTrace.info("kmelia", "KmeliaSessionControl.flushTrashCan", "root.MSG_PARAM_VALUE", "NbPubli=" + pds.size());
        while (ipds.hasNext())
        {
            String theId = ((UserPublication)ipds.next()).getPublication().getPK().getId();
            SilverTrace.info("kmelia", "KmeliaSessionControl.flushTrashCan", "root.MSG_PARAM_VALUE", "Deleting Publi #" + theId);
            deletePublication(theId);
        }
		indexOfFirstPubToDisplay = 0;
    }

    public synchronized NodePK updateTopicHeader(NodeDetail nd, String alertType) throws RemoteException {
		nd.getNodePK().setSpace(getSpaceId());
		nd.getNodePK().setComponentName(getComponentId());			
		return getKmeliaBm().updateTopic(nd, alertType);
    }

    public synchronized NodeDetail getSubTopicDetail(String subTopicId) throws RemoteException {
		return getKmeliaBm().getSubTopicDetail(getNodePK(subTopicId));
    }

    public synchronized NodePK addSubTopic(NodeDetail nd, String alertType) throws RemoteException {
		nd.getNodePK().setSpace(getSpaceId());
		nd.getNodePK().setComponentName(getComponentId());
		nd.setCreatorId(getUserId());
		//nd.setLanguage(getLanguage());
	    return getKmeliaBm().addSubTopic(getSessionTopic().getNodePK(), nd, alertType);
    }

    public synchronized void deleteTopic(String topicId) throws RemoteException 
    {
    	//First, remove rights on topic and its descendants
    	List treeview = getNodeBm().getSubTree(getNodePK(topicId));
    	NodeDetail node = null;
    	for (int n=0; n<treeview.size(); n++)
    	{
    		node = (NodeDetail) treeview.get(n);
    		
    		deleteTopicRoles(node);
    	}
    	
    	//Then, remove the topic itself
	    getKmeliaBm().deleteTopic(getNodePK(topicId));
    }

	public synchronized void changeSubTopicsOrder(String way, String subTopicId) throws RemoteException {
	    getKmeliaBm().changeSubTopicsOrder(way, getNodePK(subTopicId), getSessionTopic().getNodePK());
    }

	public synchronized void changeTopicStatus(String newStatus, String topicId, boolean recursiveChanges) throws RemoteException {
	    getKmeliaBm().changeTopicStatus(newStatus, getNodePK(topicId), recursiveChanges);
    }

    /**************************************************************************************/
    /* KMelia - Gestion des abonnements                                                   */
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
    /* KMelia - Gestion des publications                                                  */
    /**************************************************************************************/
    public synchronized PublicationDetail getPublicationDetail(String pubId) throws RemoteException {
	    return getKmeliaBm().getPublicationDetail(getPublicationPK(pubId));
    }

    public synchronized Collection getPathList(String pubId) throws RemoteException {
	    return getKmeliaBm().getPathList(getPublicationPK(pubId));
    }

    public synchronized Collection getPublicationFathers(String pubId) throws RemoteException {
    	return getKmeliaBm().getPublicationFathers(getPublicationPK(pubId));
    }
    public synchronized String createPublication(PublicationDetail pubDetail) throws RemoteException {
		pubDetail.getPK().setSpace(getSpaceId());
	    pubDetail.getPK().setComponentName(getComponentId());
	    pubDetail.setCreatorId(getUserId());
	    pubDetail.setCreationDate(new Date());
		    
	    String result = null;
	    if (isKmaxMode)
	    	result = getKmeliaBm().createKmaxPublication(pubDetail);
	    else
	    	result = getKmeliaBm().createPublicationIntoTopic(pubDetail, getSessionTopic().getNodePK());
        SilverTrace.info("kmelia", "KmeliaSessionController.createPublication(pubDetail)", "Kmelia.MSG_ENTRY_METHOD");
        SilverTrace.spy("kmelia", "KmeliaSessionController.createPublication(pubDetail)", getSpaceId(), getComponentId(), result, getUserDetail().getId(), SilverTrace.SPY_ACTION_CREATE);
        return result;
    }

    public synchronized String createPublicationIntoTopic(PublicationDetail pubDetail, String fatherId) throws RemoteException {
    	pubDetail.getPK().setSpace(getSpaceId());
	    pubDetail.getPK().setComponentName(getComponentId());
	    pubDetail.setCreatorId(getUserId());
	    pubDetail.setCreationDate(new Date());
	    
	    String result = getKmeliaBm().createPublicationIntoTopic(pubDetail, getNodePK(fatherId));
        SilverTrace.spy("kmelia", "KmeliaSessionController.createPublicationIntoTopic(pubDetail, fatherId)", getSpaceId(), getComponentId(), result, getUserDetail().getId(), SilverTrace.SPY_ACTION_CREATE);
        return result;
    }

    public synchronized void updatePublication(PublicationDetail pubDetail) throws RemoteException {
		pubDetail.getPK().setSpace(getSpaceId());
	    pubDetail.getPK().setComponentName(getComponentId());
	    pubDetail.setUpdaterId(getUserId());

	    SilverTrace.info("kmelia", "KmeliaSessionController.updatePublication(pubDetail)", "root.MSG_GEN_PARAM_VALUE", "isPublicationAlwaysVisibleEnabled() = "+isPublicationAlwaysVisibleEnabled());
	    SilverTrace.info("kmelia", "KmeliaSessionController.updatePublication(pubDetail)", "root.MSG_GEN_PARAM_VALUE", "'writer'.equals(KmeliaHelper.getProfile(getUserRoles())) = "+"writer".equals(KmeliaHelper.getProfile(getUserRoles())));
	    SilverTrace.info("kmelia", "KmeliaSessionController.updatePublication(pubDetail)", "root.MSG_GEN_PARAM_VALUE", "(getSessionClone() == null) = "+(getSessionClone() == null));
	    if (isCloneNeeded())
	    {
			clonePublication(pubDetail);
	    }
	    else
	    {
		    if (getSessionTopic() == null || getSessionTopic().getNodePK().getId().equals("1"))
		    {
		    	//la publication est dans la corbeille
				pubDetail.setIndexOperation(IndexManager.NONE);
			}
	
			getKmeliaBm().updatePublication(pubDetail);
	    }
        SilverTrace.spy("kmelia", "KmeliaSessionController.updatePublication", getSpaceId(), getComponentId(),	pubDetail.getId(), getUserDetail().getId(), SilverTrace.SPY_ACTION_UPDATE);
    }
    
    public boolean isCloneNeeded()
    {
    	String currentStatus = getSessionPublication().getPublication().getPublicationDetail().getStatus();
    	return (isPublicationAlwaysVisibleEnabled() && "writer".equals(KmeliaHelper.getProfile(getUserRoles())) && (getSessionClone() == null) && PublicationDetail.VALID.equals(currentStatus));
    }
    
    public boolean isCloneNeededWithDraft()
    {
    	return (isPublicationAlwaysVisibleEnabled() && (getSessionClone() == null));
    }
    
    public String clonePublication()
    {
    	return clonePublication(null);
    }
    
    public String clonePublication(PublicationDetail pubDetail)
    {
    	String cloneStatus = PublicationDetail.TO_VALIDATE;
    	if (isDraftEnabled())
    		cloneStatus = PublicationDetail.DRAFT;
    	return clonePublication(pubDetail, cloneStatus);
    }
    
    /**
     * Clone current publication. Create new publication based on pubDetail object if not null or sessionPublication otherwise.
     * Original publication must not be modified (except references to clone : cloneId and cloneStatus).
     * @param pubDetail If not null, attribute values are set to the clone
     * @param nextStatus Draft or Clone
     * @return
     */
    private String clonePublication(PublicationDetail pubDetail, String nextStatus) 
    {
    	String cloneId = null;
		try {
			//récupération de la publi de référence
			CompletePublication refPubComplete = getSessionPublication().getPublication();
			PublicationDetail refPub = refPubComplete.getPublicationDetail();
			
			String fromId = new String(refPub.getPK().getId());
			String fromComponentId = new String(getComponentId());
			
			PublicationDetail clone = getClone(refPub);
				
			String imagesSubDirectory = getPublicationSettings().getString("imagesSubDirectory");
			String absolutePath = FileRepositoryManager.getAbsolutePath(fromComponentId);
			
			//paste vignette
			String vignette = refPub.getImage();
			if (vignette != null) {
				String from = absolutePath + imagesSubDirectory + File.separator + vignette;
				
				String type = vignette.substring(vignette.lastIndexOf(".")+1, vignette.length());
				String newVignette = new Long(new java.util.Date().getTime()).toString() + "." +type;
				
				String to = absolutePath + imagesSubDirectory + File.separator + newVignette;
				FileRepositoryManager.copyFile(from, to);
				
				clone.setImage(newVignette);
				clone.setImageMimeType(refPub.getImageMimeType());
			}
			
			if (pubDetail != null)
			{
				clone.setAuthor(pubDetail.getAuthor());
				clone.setBeginDate(pubDetail.getBeginDate());
				clone.setBeginHour(pubDetail.getBeginHour());
				clone.setDescription(pubDetail.getDescription());
				clone.setEndDate(pubDetail.getEndDate());
				clone.setEndHour(pubDetail.getEndHour());
				clone.setImportance(pubDetail.getImportance());
				clone.setKeywords(pubDetail.getKeywords());
				clone.setName(pubDetail.getName());
				clone.setTargetValidatorId(pubDetail.getTargetValidatorId());
			}
			if (isInteger(refPub.getInfoId()))
			{
				//Case content = DB
				clone.setInfoId(null);
			}
			clone.setStatus(nextStatus);
			clone.setCloneId(fromId);
			clone.setIndexOperation(IndexManager.NONE);

			PublicationPK clonePK = getKmeliaBm().getPublicationBm().createPublication(clone);
			cloneId = clonePK.getId();
			
			//eventually, paste the model content
			if (refPubComplete.getModelDetail() != null && refPubComplete.getInfoDetail() != null) {
				//Paste images of model
				if (refPubComplete.getInfoDetail().getInfoImageList() != null) {
					for (Iterator i = refPubComplete.getInfoDetail().getInfoImageList().iterator(); i.hasNext();) {
						InfoImageDetail attachment = (InfoImageDetail)i.next();
						String from = absolutePath + imagesSubDirectory + File.separator + attachment.getPhysicalName();
						String type = attachment.getPhysicalName().substring(attachment.getPhysicalName().lastIndexOf(".")+1, attachment.getPhysicalName().length());
						String newName = new Long(new java.util.Date().getTime()).toString() + "." +type;
						attachment.setPhysicalName(newName);
						String to = absolutePath + imagesSubDirectory + File.separator + newName;
						FileRepositoryManager.copyFile(from, to);
					}
				}
				
				//Paste model content
				getKmeliaBm().createInfoModelDetail(getPublicationPK(cloneId), refPubComplete.getModelDetail().getId(), refPubComplete.getInfoDetail());
			}
			else
			{
				String infoId = refPub.getInfoId();
				if (infoId != null && !"0".equals(infoId) && !isInteger(infoId))
				{
					//Content = XMLForm
					//register xmlForm to publication
					String xmlFormShortName = infoId;
					PublicationTemplateManager.addDynamicPublicationTemplate(getComponentId()+":"+xmlFormShortName, xmlFormShortName+".xml");
					
					PublicationTemplate pubTemplate = PublicationTemplateManager.getPublicationTemplate(getComponentId()+":"+xmlFormShortName);
					
					RecordSet set = pubTemplate.getRecordSet();
					
					//clone dataRecord
					set.clone(fromId, cloneId);
				}
			}

			//paste wysiwyg
			WysiwygController.copy(null, fromComponentId, fromId, null, fromComponentId, cloneId, clone.getCreatorId());

			//clone attachments
			AttachmentPK pkFrom = new AttachmentPK(fromId, fromComponentId);
			AttachmentPK pkTo	= new AttachmentPK(cloneId, fromComponentId);
			AttachmentController.cloneAttachments(pkFrom, pkTo);

			//paste versioning documents
			//pasteDocuments(pubPKFrom, clonePK.getId());
			
			//affectation de l'id du clone à la publication de référence
			refPub.setCloneId(cloneId);
			refPub.setCloneStatus(nextStatus);
			refPub.setStatusMustBeChecked(false);
			refPub.setUpdateDateMustBeSet(false);
			getKmeliaBm().updatePublication(refPub);
			
			setSessionClone(getUserCompletePublication(cloneId));
		} 
		catch (IOException e) 
		{
			throw new KmeliaRuntimeException("KmeliaSessionController.clonePublication", SilverpeasException.ERROR, "kmelia.CANT_CLONE_PUBLICATION", e);
		} 
		catch (AttachmentException ae)
		{
			throw new KmeliaRuntimeException("KmeliaSessionController.clonePublication", SilverpeasException.ERROR, "kmelia.CANT_CLONE_PUBLICATION_FILES", ae);
		}
		catch (FormException fe)
		{
			throw new KmeliaRuntimeException("KmeliaSessionController.clonePublication", SilverpeasException.ERROR, "kmelia.CANT_CLONE_PUBLICATION_XMLCONTENT", fe);
		}
		catch (PublicationTemplateException pe)
		{
			throw new KmeliaRuntimeException("KmeliaSessionController.clonePublication", SilverpeasException.ERROR, "kmelia.CANT_CLONE_PUBLICATION_XMLCONTENT", pe);
		}
		return cloneId;
    }
    
    private PublicationDetail getClone(PublicationDetail refPub)
    {
    	PublicationDetail clone = new PublicationDetail();
		if (refPub.getAuthor() != null)
			clone.setAuthor(new String(refPub.getAuthor()));
		if (refPub.getBeginDate() != null)
			clone.setBeginDate(new Date(refPub.getBeginDate().getTime()));
		if (refPub.getBeginHour() != null)
			clone.setBeginHour(new String(refPub.getBeginHour()));
		if (refPub.getContent() != null)
			clone.setContent(new String(refPub.getContent()));
		clone.setCreationDate(new Date(refPub.getCreationDate().getTime()));
		clone.setCreatorId(new String(refPub.getCreatorId()));
		if (refPub.getDescription() != null)
			clone.setDescription(new String(refPub.getDescription()));
		if (refPub.getEndDate() != null)
			clone.setEndDate(new Date(refPub.getEndDate().getTime()));
		if (refPub.getEndHour() != null)
			clone.setEndHour(new String(refPub.getEndHour()));
		if (refPub.getImage() != null)
			clone.setImage(new String(refPub.getImage()));
		if (refPub.getImageMimeType() != null)
			clone.setImageMimeType(new String(refPub.getImageMimeType()));
		clone.setImportance(refPub.getImportance());
		if (refPub.getInfoId() != null)
			clone.setInfoId(new String(refPub.getInfoId()));
		if (refPub.getKeywords() != null)
			clone.setKeywords(new String(refPub.getKeywords()));
		if (refPub.getName() != null)
			clone.setName(new String(refPub.getName()));
		clone.setPk(new PublicationPK(new String(refPub.getPK().getId()), getComponentId()));
		if (refPub.getStatus() != null)
			clone.setStatus(new String(refPub.getStatus()));
		if (refPub.getTargetValidatorId() != null)
			clone.setTargetValidatorId(new String(refPub.getTargetValidatorId()));
		if (refPub.getCloneId() != null)
			clone.setCloneId(new String(refPub.getCloneId()));
		if (refPub.getUpdateDate() != null)
			clone.setUpdateDate(new Date(refPub.getUpdateDate().getTime()));
		if (refPub.getUpdaterId() != null)
			clone.setUpdaterId(new String(refPub.getUpdaterId()));
		if (refPub.getValidateDate() != null)
			clone.setValidateDate(new Date(refPub.getValidateDate().getTime()));
		if (refPub.getValidatorId() != null)
			clone.setValidatorId(new String(refPub.getValidatorId()));
		if (refPub.getVersion() != null)
			clone.setVersion(new String(refPub.getVersion()));
		
		return clone;
    }

    public synchronized void deletePublication(String pubId) throws RemoteException {
    	deletePublication(pubId, false);
    }
    public synchronized void deletePublication(String pubId, boolean kmaxMode) throws RemoteException {
		//récupération de la position de la publication pour savoir s'il elle se trouve déjà dans la corbeille node=1
		//s'il elle se trouve déjà au node 1, il est nécessaire de supprimer les fichier joints sinon non
   		String nodeId = "";
    	if (getSessionTopic() != null)
   			nodeId = getSessionTopic().getNodeDetail().getNodePK().getId();
		if("1".equals(nodeId)){
			//la publication sera supprimée définitivement, il faut donc supprimer les fichiers joints
			try {
				WysiwygController.deleteWysiwygAttachments(getSpaceId(), getComponentId(), pubId);
			}
			catch (Exception e) {
				throw new KmeliaRuntimeException("KmeliaSessionController.deletePublication",SilverpeasRuntimeException.ERROR,"root.EX_DELETE_ATTACHMENT_FAILED", e);
			}
			
			removeXMLContentOfPublication(getPublicationPK(pubId));
			getKmeliaBm().deletePublication(getPublicationPK(pubId));
		}
		else
		{
			getKmeliaBm().sendPublicationToBasket(getPublicationPK(pubId), kmaxMode);
		}
        SilverTrace.spy("kmelia", "KmeliaSessionController.deletePublication", getSpaceId(), getComponentId(), pubId, getUserDetail().getId(), SilverTrace.SPY_ACTION_DELETE);
    }

    public synchronized void deleteClone() throws RemoteException {
    	if (getSessionClone() != null)
    	{
    		//supprime le clone
    		String cloneId = getSessionClone().getPublication().getPublicationDetail().getPK().getId();
    		PublicationPK clonePK = getPublicationPK(cloneId);
    		
    		removeXMLContentOfPublication(clonePK);
    		getKmeliaBm().deletePublication(clonePK);
    		
    		setSessionClone(null);
    		refreshSessionPubliAndClone();
    		
    		//supprime les références au clone
    		PublicationDetail pubDetail = getSessionPublication().getPublication().getPublicationDetail();
    		pubDetail.setCloneId(null);
    		pubDetail.setCloneStatus(null);
    		pubDetail.setStatusMustBeChecked(false);
    		pubDetail.setUpdateDateMustBeSet(false);
    		
    		getKmeliaBm().updatePublication(pubDetail);
    		
    		SilverTrace.spy("kmelia", "KmeliaSessionController.deleteClone", getSpaceId(), getComponentId(), cloneId, getUserDetail().getId(), SilverTrace.SPY_ACTION_DELETE);
    	}
    }
    
    private void removeXMLContentOfPublication(PublicationPK pubPK) throws RemoteException
	{
		try {
			PublicationDetail pubDetail = getKmeliaBm().getPublicationDetail(pubPK);
			String infoId = pubDetail.getInfoId();
			if (!isInteger(infoId))
			{
				String xmlFormShortName = infoId;
				
				PublicationTemplate pubTemplate = PublicationTemplateManager.getPublicationTemplate(pubDetail.getPK().getInstanceId()+":"+xmlFormShortName);
				
				RecordSet 	set		= pubTemplate.getRecordSet();
				DataRecord 	data 	= set.getRecord(pubDetail.getPK().getId());
				set.delete(data);
			}
		} catch (PublicationTemplateException e) {
			throw new KmeliaRuntimeException("KmeliaSessionController.removeXMLContentOfPublication()", SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LE_CONTENU_XML", e);
		} catch (FormException e) {
			throw new KmeliaRuntimeException("KmeliaSessionController.removeXMLContentOfPublication()", SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LE_CONTENU_XML", e);
		}
	}
    
    private static boolean isInteger(String id)
	{
		try
		{
			Integer.parseInt(id);
			return true;
		}
		catch (NumberFormatException e)
		{
			return false;
		}
	}

    public synchronized void addPublicationToTopic(String pubId, String fatherId) throws RemoteException {
		getKmeliaBm().addPublicationToTopic(getPublicationPK(pubId), getNodePK(fatherId), false);
    }

    public synchronized void deletePublicationFromAllTopics(String pubId) throws RemoteException {
	    getKmeliaBm().deletePublicationFromAllTopics(getPublicationPK(pubId));
    }
    
    public synchronized Collection getAllModels() throws RemoteException {
	    return getKmeliaBm().getAllModels();
    }

    public synchronized ModelDetail getModelDetail(String modelId) throws RemoteException {
	    return getKmeliaBm().getModelDetail(modelId);
    }

    /*public synchronized void createInfoDetail(String pubId, String modelId, InfoDetail infos) throws RemoteException {
	    getKmeliaBm().createInfoDetail(getPublicationPK(pubId), modelId, infos);
    }*/

    public synchronized void createInfoModelDetail(String pubId, String modelId, InfoDetail infos) throws RemoteException {
    	pubId = getSessionPubliOrClone().getPublication().getPublicationDetail().getPK().getId();
    	if (isCloneNeeded())
	    {
			pubId = clonePublication();
	    }
    	if (getSessionClone() != null)
    	{
			ModelPK modelPK = new ModelPK(modelId, getPublicationPK(pubId));
			getKmeliaBm().getPublicationBm().createInfoModelDetail(getPublicationPK(pubId), modelPK, infos);
	    }
    	else
    	{
    		getKmeliaBm().createInfoModelDetail(getPublicationPK(pubId), modelId, infos);
    	}
	    refreshSessionPubliAndClone();
    }
    
    public void refreshSessionPubliAndClone() throws RemoteException
    {
    	if (getSessionClone() != null)
	    {
	    	//refresh du clone
	    	UserCompletePublication ucp = getUserCompletePublication(getSessionClone().getPublication().getPublicationDetail().getPK().getId());
	    	setSessionClone(ucp);
	    }
	    else
	    {
	    	//refresh de la publi de référence
	    	UserCompletePublication ucp = getUserCompletePublication(getSessionPublication().getPublication().getPublicationDetail().getPK().getId());
	    	setSessionPublication(ucp);
	    }
    }

    public synchronized InfoDetail getInfoDetail(String pubId) throws RemoteException {
	    return getKmeliaBm().getInfoDetail(getPublicationPK(pubId));
    }

    public synchronized void updateInfoDetail(String pubId, InfoDetail infos) throws RemoteException {
    	pubId = getSessionPubliOrClone().getPublication().getPublicationDetail().getPK().getId();
    	if (isCloneNeeded())
	    {
			pubId = clonePublication();
	    }
    	if (getSessionClone() != null)
    	{
			getKmeliaBm().getPublicationBm().updateInfoDetail(getPublicationPK(pubId), infos);
	    }
    	else
    	{
    		getKmeliaBm().updateInfoDetail(getPublicationPK(pubId), infos);
    	}
    	refreshSessionPubliAndClone();
    }
    
    public void deleteInfoLinks(String pubId, List pubIds) throws RemoteException
    {
		getKmeliaBm().deleteInfoLinks(getPublicationPK(pubId), pubIds);
		
		//reset current publication
		UserCompletePublication completPub = getKmeliaBm().getUserCompletePublication(getPublicationPK(pubId), getUserId());
		setSessionPublication(completPub);
    }

    public synchronized UserCompletePublication getUserCompletePublication(String pubId) throws RemoteException {
		resetPublicationsToLink();
		PublicationPK pubPK = getPublicationPK(pubId);
		//get publication
		UserCompletePublication completPub 			= getKmeliaBm().getUserCompletePublication(pubPK, getUserId());
		PublicationDetail 		publicationDetail 	= completPub.getPublication().getPublicationDetail();
		
		ForeignPK foreignPK = new ForeignPK(pubId, getComponentId());
		if (!publicationDetail.getPK().getInstanceId().equals(getComponentId()))
		{
			//it's an alias
			foreignPK.setComponentName(publicationDetail.getPK().getInstanceId());
		}
		
		if (getSessionPublication() != null )
		{
			if (!pubId.equals(getSessionPublication().getId()))
			{
				//memorize the reading of the publication by the user
				getStatisticBm().addStat(getUserId(), foreignPK, 1, "Publication");
			}
		}
		else
		{
			getStatisticBm().addStat(getUserId(), foreignPK, 1, "Publication");
		}
		
		//set nb access
		setNbAccess(publicationDetail);

		// mise à jour du rang de la publication
		//List publis = (List) getSessionTopic().getPublicationDetails();
		UserDetail owner = completPub.getOwner();
		UserPublication pub = new UserPublication(owner, publicationDetail);
		if (getSessionPublicationsList() != null)
			rang = getSessionPublicationsList().indexOf(pub);
		return completPub;
    }

    public synchronized CompletePublication getCompletePublication(String pubId) throws RemoteException {
	    return getKmeliaBm().getCompletePublication(getPublicationPK(pubId));
    }

    public synchronized void orderPubs() throws RemoteException {
    	if (getSortValue() == null)
    		orderPubs(2);
    	orderPubs(Integer.parseInt(getSortValue()));
    }
    
    private void applyVisibilityFilter() throws RemoteException
    {
    	List publications = getSessionPublicationsList();
    	UserPublication		userPub;
		PublicationDetail	pub;
		List				orderedPublications = new ArrayList();
		
		Calendar calendar = Calendar.getInstance();
		
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date today = calendar.getTime();
		
		for (int p=0; p<publications.size(); p++)
		{
			userPub = (UserPublication) publications.get(p);
            pub		= userPub.getPublication();
			if (pub.getStatus() != null)
			{
				if (pub.getStatus().equals("Valid")) {
					Date dBegin = DateUtil.getDate(pub.getBeginDate(), pub.getBeginHour());
					Date dEnd 	= DateUtil.getDate(pub.getEndDate(), pub.getEndHour());
					
					pub.setBeginDateAndHour(dBegin);
					pub.setEndDateAndHour(dEnd);
					
					if (dBegin != null && dBegin.after(today))
					{
						pub.setNotYetVisible(true);
					}
					else if (dEnd != null && dEnd.before(today))
					{
						pub.setNoMoreVisible(true);
					}
					if (pub.isVisible())
						orderedPublications.add(userPub);
					else
					{
						if (getProfile().equals("admin") || getUserId().equals(pub.getUpdaterId()) || (!getProfile().equals("user") && isCoWritingEnable()))
							orderedPublications.add(userPub);
					}
				} else {
					if (pub.getStatus().equals("Draft")) 
					{
						// si le theme est en co-rédaction et si on autorise le mode brouillon visible par tous
						// toutes les publications en mode brouillon sont visibles par tous, sauf les lecteurs
						// sinon, seule les publications brouillon de l'utilisateur sont visibles
						if (getUserId().equals(pub.getUpdaterId()) || ((isCoWritingEnable() && isDraftVisibleWithCoWriting())  && !getProfile().equals("user"))) 
								orderedPublications.add(userPub);
					} 
					else
					{
						// si le thème est en co-rédaction, toutes les publications sont visibles par tous, sauf les lecteurs
						if (getProfile().equals("admin") || getProfile().equals("publisher") || getUserId().equals(pub.getUpdaterId()) || (!getProfile().equals("user") && isCoWritingEnable())) {
							orderedPublications.add(userPub);
						}
					}
				}
			}
		}
		
		setSessionPublicationsList(orderedPublications);
    }

    private synchronized void orderPubs(int sortType) throws RemoteException {
    	
		List publications = (List) sort(getSessionPublicationsList(), sortType);
		
		setSessionPublicationsList(publications);
    }

	public synchronized List orderPubsToValidate(int sortType) throws RemoteException {
		return sort(getKmeliaBm().getPublicationsToValidate(getComponentId()), sortType);
    }
	
	private List sort(Collection publications, int sortType) {
		UserPublication pubs[] = new UserPublication[publications.size()];
	    Iterator iterator = publications.iterator();
        int p = 0;
        while (iterator.hasNext()) {
            pubs[p] = (UserPublication) iterator.next();
            p++;
        }
        UserPublication sortedPubs[];

        switch (sortType) {
            case 0 : sortedPubs = sortByAuthor(pubs);
                     break;
            case 1 : sortedPubs = sortByDateAsc(pubs);
                     break;
            case 2 : sortedPubs = sortByDateDesc(pubs);
                     break;
            case 3 : sortedPubs = sortByImportance(pubs);
                     break;
            case 4 : sortedPubs = sortByTitle(pubs);
                     break;
            default : sortedPubs = sortByDateDesc(pubs);
                     break;
        }

        ArrayList list = new ArrayList();
        for (int i = 0; i<sortedPubs.length; i++) {
              list.add(sortedPubs[i]);
        }
        return list;
    }

    private UserPublication[] sortByTitle(UserPublication[] pubs) {
        for (int i = pubs.length; --i>=0; ) {
		    boolean swapped = false;
		    for (int j = 0; j<i; j++) {
				if (pubs[j].getPublication().getName(getCurrentLanguage()).compareToIgnoreCase(pubs[j+1].getPublication().getName(getCurrentLanguage())) > 0) {
				    UserPublication T = pubs[j];
				    pubs[j] = pubs[j+1];
				    pubs[j+1] = T;
				    swapped = true;
				}
		    }
		    if (!swapped)
		    	break;
		}
        return pubs;
    }

    private UserPublication[] sortByImportance(UserPublication[] pubs) {
        for (int i = pubs.length; --i>=0; ) {
		    boolean swapped = false;
		    for (int j = 0; j<i; j++) {
				if (pubs[j].getPublication().getImportance() < pubs[j+1].getPublication().getImportance()) {
				    UserPublication T = pubs[j];
				    pubs[j] = pubs[j+1];
				    pubs[j+1] = T;
				    swapped = true;
				}
		    }
		    if (!swapped)
		    	break;
		}
        return pubs;
    }

    private UserPublication[] sortByDateAsc(UserPublication[] pubs) {
        for (int i = pubs.length; --i>=0; ) {
		    boolean swapped = false;
		    for (int j = 0; j<i; j++) {
				if (pubs[j].getPublication().getUpdateDate().getTime() > pubs[j+1].getPublication().getUpdateDate().getTime()) {
				    UserPublication T = pubs[j];
				    pubs[j] = pubs[j+1];
				    pubs[j+1] = T;
				    swapped = true;
				}
		    }
		    if (!swapped)
		    	break;
			}
        return pubs;
    }

    private UserPublication[] sortByDateDesc(UserPublication[] pubs) {
        for (int i = pubs.length; --i>=0; ) {
		    boolean swapped = false;
		    for (int j = 0; j<i; j++) {
				if (pubs[j].getPublication().getUpdateDate().getTime() < pubs[j+1].getPublication().getUpdateDate().getTime()) {
				    UserPublication T = pubs[j];
				    pubs[j] = pubs[j+1];
				    pubs[j+1] = T;
				    swapped = true;
				}
		    }
		    if (!swapped)
		    	break;
		}
        return pubs;
    }

    private UserPublication[] sortByAuthor(UserPublication[] pubs) {
        for (int i = pubs.length; --i>=0; ) {
		    boolean swapped = false;
		    for (int j = 0; j<i; j++) {
				if (pubs[j].getOwner().getLastName().compareToIgnoreCase(pubs[j+1].getOwner().getLastName()) > 0) {
				    UserPublication T = pubs[j];
				    pubs[j] = pubs[j+1];
				    pubs[j+1] = T;
				    swapped = true;
				}
		    }
		    if (!swapped)
		    	break;
		}
        return pubs;
    }
    
    public void orderPublications(List sortedPubIds) throws RemoteException
    {
    	getPublicationBm().changePublicationsOrder(sortedPubIds, getSessionTopic().getNodePK());
    }

	public Collection getAllPublications() throws RemoteException {
		return getAllPublications(null);
	}

	/**
	 * Get all publications sorted
	 * @param sortedBy (example: pubName asc)
	 * @return Collection of Publications
	 * @throws RemoteException
	 */
	public Collection getAllPublications(String sortedBy) throws RemoteException {
		String publication_default_sorting = SilverpeasSettings.readString(getSettings(), "publication_defaultsorting", "pubId desc");
		if (StringUtil.isDefined(sortedBy))
			publication_default_sorting = sortedBy;
		return getKmeliaBm().getPublicationBm().getAllPublications(new PublicationPK("useless", getComponentId()), publication_default_sorting);
	}

	public Collection getAllPublicationsByTopic(PublicationPK pubPK, ArrayList fatherIds) throws RemoteException 
	{
		Collection result = getKmeliaBm().getPublicationBm().getDetailsByFatherIdsAndStatus(fatherIds, pubPK, "P.pubUpdateDate desc, P.pubId desc", PublicationDetail.VALID );
		SilverTrace.info("kmelia", "KmeliaSessionController.getAllPublicationsByTopic()", "root.MSG_PARAM_VALUE", "publis=" + result.toString());
		return result;
	}

	/**
	 * Get all visible publications
	 * @return List of WAAtributeValuePair (Id and InstanceId)
	 * @throws RemoteException
	 */
	public List getAllVisiblePublications () throws RemoteException
	{
		List allVisiblesPublications = new ArrayList();
        Collection allPublications = getAllPublications();
        SilverTrace.info("kmelia", "KmeliaSessionController.getAllVisiblePublications()", "root.MSG_PARAM_VALUE", "NbPubli=" + allPublications.size());
        Iterator allPublis = allPublications.iterator();
        while (allPublis.hasNext())
        {
        	PublicationDetail pubDetail = (PublicationDetail) allPublis.next(); 
        	if (pubDetail.getStatus().equals(PublicationDetail.VALID))
        	{
	            SilverTrace.info("kmelia", "KmeliaSessionController.getAllVisiblePublications()", "root.MSG_PARAM_VALUE", "Get pubId" + pubDetail.getId() + "InstanceId="+pubDetail.getInstanceId());
	            allVisiblesPublications.add(new WAAttributeValuePair(pubDetail.getId(), pubDetail.getInstanceId()));
        	}
        }
		return allVisiblesPublications;
	}
	
	public List getAllVisiblePublicationsByTopic (String topicId) throws RemoteException
	{
		List allVisiblesPublications = new ArrayList();
		// récupérer la liste des sous thèmes de topicId
		ArrayList fatherIds = new ArrayList();
		NodePK nodePK = new NodePK(topicId, getComponentId());
		ArrayList nodes = getNodeBm().getSubTree(nodePK);
		Iterator it = nodes.iterator();
		while (it.hasNext())
		{
			NodeDetail node = (NodeDetail) it.next();
			fatherIds.add(Integer.toString(node.getId()));
		}
		// création de pubPK
		PublicationPK pubPK = getPublicationPK("useless");
        SilverTrace.info("kmelia", "KmeliaSessionController.getAllVisiblePublicationsByTopic()", "root.MSG_PARAM_VALUE", "fatherIds =" + fatherIds.toString());
        SilverTrace.info("kmelia", "KmeliaSessionController.getAllVisiblePublicationsByTopic()", "root.MSG_PARAM_VALUE", "pubPK =" + pubPK.toString());
        Collection allPublications = getAllPublicationsByTopic(pubPK, fatherIds);
        SilverTrace.info("kmelia", "KmeliaSessionController.getAllVisiblePublicationsByTopic()", "root.MSG_PARAM_VALUE", "NbPubli=" + allPublications.size());
        Iterator allPublis = allPublications.iterator();
        while (allPublis.hasNext())
        {
        	PublicationDetail pubDetail = (PublicationDetail) allPublis.next(); 
        	if (pubDetail.getStatus().equals(PublicationDetail.VALID))
        	{
	            SilverTrace.info("kmelia", "KmeliaSessionController.getAllVisiblePublicationsByTopic()", "root.MSG_PARAM_VALUE", "Get pubId" + pubDetail.getId() + "InstanceId="+pubDetail.getInstanceId());
	            allVisiblesPublications.add(new WAAttributeValuePair(pubDetail.getId(), pubDetail.getInstanceId()));
        	}
        }
		return allVisiblesPublications;
	}
	
	public List getAllPublicationsIds () throws RemoteException
	{
		List allPublicationsIds = new ArrayList();
        Collection allPublications = getAllPublications("pubName asc");
        SilverTrace.info("kmelia", "KmeliaSessionController.getAllPublicationsIds()", "root.MSG_PARAM_VALUE", "NbPubli=" + allPublications.size());
        Iterator allPublis = allPublications.iterator();
        while (allPublis.hasNext())
        {
        	PublicationDetail pubDetail = (PublicationDetail) allPublis.next(); 
        	if (pubDetail.getStatus().equals(PublicationDetail.VALID))
        	{
	            SilverTrace.info("kmelia", "KmeliaSessionController.getAllPublicationsIds()", "root.MSG_PARAM_VALUE", "Get pubId" + pubDetail.getId() + "InstanceId="+pubDetail.getInstanceId());
	            allPublicationsIds.add(new WAAttributeValuePair(pubDetail.getId(), pubDetail.getInstanceId()));
        	}
        }
		return allPublicationsIds;
	}


	public int getIndexOfFirstPubToDisplay()
	{
		return indexOfFirstPubToDisplay;
	}

	public void setIndexOfFirstPubToDisplay(String index)
	{
		this.indexOfFirstPubToDisplay = new Integer(index).intValue();
	}

    public Vector getAllComments(String id) throws RemoteException
    {
		return getCommentBm().getAllComments(getPublicationPK(id));
    }

	public void processTopicWysiwyg(String topicId) throws RemoteException
    {
		getKmeliaBm().getNodeBm().processWysiwyg(getNodePK(topicId));
    }
    
    /**
     * Si le mode brouillon est activé et que le classement PDC est possible
     * alors une publication ne peut sortir du mode brouillon que si elle est
     * classée sur le PDC
	 * @param pubId - l'identifiant de la publication qui doit sortir du mode brouillon
	 * @return true si le PDC n'est pas utilisé ou si aucun axe n'est utilisé par le composant ou si la publication est classée sur le PDC
	 * @throws RemoteException
	 */
	public boolean isDraftOutAllowed() throws RemoteException
    {
    	if (!isPdcUsed())
    	{
    		//le PDC n'est pas utilisé
			return true;
    	}
    	else
    	{
    		boolean pdcClassifyingMandatory = isPDCClassifyingMandatory();
    		if (!pdcClassifyingMandatory)
    		{
    			//Aucun axe n'est utilisé
				return true;
    		}
    		else
    		{
    			String pubId = getSessionPublication().getPublication().getPublicationDetail().getPK().getId();
				if (isPublicationClassifiedOnPDC(pubId))
				{
					//Au moins un axe est obligatoire et la publication est classée sur le PDC
					return true;
				}
				else
				{
					//La publication n'est pas classée sur le PDC
					return false;
				}
    		}
    	}
	}

    /**************************************************************************************/
    /* KMelia - Gestion des Liens                                                         */
    /**************************************************************************************/
    //return a PublicationDetail collection
    public synchronized Collection getPublications(Collection targetIds) throws RemoteException {
	    return getKmeliaBm().getPublications(targetIds, getComponentId());
    }

    /**************************************************************************************/
    /* KMelia - Gestion des validations                                                   */
    /**************************************************************************************/
    public synchronized List getPublicationsToValidate() throws RemoteException {
	    return getKmeliaBm().getPublicationsToValidate(getComponentId());
    }

    public synchronized boolean validatePublication(String publicationId) throws RemoteException {
	    return getKmeliaBm().validatePublication(getPublicationPK(publicationId), getUserId(), getValidationType(), false);
    }
    
    public synchronized boolean forcePublicationValidation(String publicationId) throws RemoteException {
	    return getKmeliaBm().validatePublication(getPublicationPK(publicationId), getUserId(), getValidationType(), true);
    }

    public synchronized void unvalidatePublication(String publicationId, String refusalMotive) throws RemoteException {
	    getKmeliaBm().unvalidatePublication(getPublicationPK(publicationId), getUserId(), refusalMotive, getValidationType());
    }
    
	public synchronized void suspendPublication(String publicationId, String defermentMotive) throws RemoteException {
		getKmeliaBm().suspendPublication(getPublicationPK(publicationId), defermentMotive, getUserId());
	}
	
	public List getValidationSteps() throws RemoteException
	{
		List steps = getPublicationBm().getValidationSteps(getSessionPubliOrClone().getPublication().getPublicationDetail().getPK());
		
		//Get users who have already validate this publication
		List validators = new ArrayList();
		ValidationStep step = null;
		for (int s=0; s<steps.size(); s++)
		{
			step = (ValidationStep) steps.get(s);
			step.setUserFullName(getOrganizationController().getUserDetail(step.getUserId()).getDisplayedName());
			validators.add(step.getUserId());
		}
			
		List allValidators = getKmeliaBm().getAllValidators(getSessionPubliOrClone().getPublication().getPublicationDetail().getPK(), getValidationType());
		
		for (int v=0; v<allValidators.size(); v++)
		{
			if (!validators.contains(allValidators.get(v)))
			{
				step = new ValidationStep();
				step.setUserFullName(getOrganizationController().getUserDetail((String) allValidators.get(v)).getDisplayedName());
				steps.add(step);
			}
		}
		
		return steps;
	}
	
	public ValidationStep getValidationStep() throws RemoteException
	{
		if (getValidationType() == 2)
			return getPublicationBm().getValidationStepByUser(getSessionPubliOrClone().getPublication().getPublicationDetail().getPK(), getUserId());
		
		return null;
	}

	public synchronized void draftOutPublication() throws RemoteException {
		SilverTrace.info("kmelia", "KmeliaSessionController.draftOutPublication()", "root.MSG_GEN_ENTER_METHOD", "getSessionPublication().getPublication() = "+getSessionPublication().getPublication());
		if (isKmaxMode)
			getKmeliaBm().draftOutPublication(getSessionPublication().getPublication().getPublicationDetail().getPK(), null, getProfile());
		else
			getKmeliaBm().draftOutPublication(getSessionPublication().getPublication().getPublicationDetail().getPK(), getSessionTopic().getNodePK(), getProfile());
		//setSessionClone(null);
		refreshSessionPubliAndClone();
	}

	/**
	* Change publication status from any state to draft
	* @param publicationId the id of the publication
	* @since 3.0
	*/
	public synchronized void draftInPublication() throws RemoteException {
		if (isCloneNeededWithDraft())
	    {
			clonePublication();
			//getKmeliaBm().draftInPublication(getPublicationPK(cloneId));
	    }
		else
		{
			getKmeliaBm().draftInPublication(getSessionPublication().getPublication().getPublicationDetail().getPK());
		}
		refreshSessionPubliAndClone();
	}
	
	public void deleteVignette(String pubId) throws RemoteException
	{
		PublicationDetail pubDetail = getKmeliaBm().getPublicationDetail(getPublicationPK(pubId));
		
		//remove image from filesystem
		File dir = new File(FileRepositoryManager.getAbsolutePath(getComponentId())+getPublicationSettings().getString("imagesSubDirectory")+ File.separator +pubDetail.getImage());
		if (dir.exists())
			dir.delete();
		
		//remove image in DB
		getKmeliaBm().deletePublicationImage(getPublicationPK(pubId));
	}

    /**************************************************************************************/
    /* KMelia - Gestion des Controles de lecture                                          */
    /**************************************************************************************/
    /*public synchronized Collection getReadingStates(String pubId) throws RemoteException {
	    return getKmeliaBm().getReadingStates(getPublicationPK(pubId));
    } */
    
    private void setNbAccess(PublicationDetail pub) throws RemoteException
    {
    	int nbAccess = getStatisticBm().getCount(new ForeignPK(pub.getPK().getId(), pub.getPK().getInstanceId()), 1, "Publication");
    	pub.setNbAccess(nbAccess);
    }

	/*************************************************************/
	/** SCO - 26/12/2002 Integration AlertUser et AlertUserPeas **/
	/*************************************************************/
	private synchronized NotificationMetaData getAlertNotificationMetaData(String pubId) throws RemoteException {
		NotificationMetaData metaData = null;
		if (isKmaxMode)
			metaData = getKmeliaBm().getAlertNotificationMetaData(getPublicationPK(pubId), null, getUserDetail().getDisplayedName());
		else
			metaData = getKmeliaBm().getAlertNotificationMetaData(getPublicationPK(pubId), getSessionTopic().getNodePK(), getUserDetail().getDisplayedName());
		metaData.setSender(getUserId());
		return metaData;
    }
	/*************************************************************/

	/**************************************************************************************/
    /* KMELIA - Reindexation				                                                  */
    /**************************************************************************************/
	public synchronized void indexKmelia() throws RemoteException {
		getKmeliaBm().indexKmelia(getComponentId());
	}
	
	public boolean isIndexable(PublicationDetail pubDetail)
	{
		return KmeliaHelper.isIndexable(pubDetail);
	}
	
	public Hashtable pasteFiles(PublicationPK pubPKFrom, String pubId) throws RemoteException
	{
		Hashtable fileIds = new Hashtable();
		
		boolean fromCompoVersion = "yes".equals(getOrganizationController().getComponentParameterValue(pubPKFrom.getInstanceId(), "versionControl"));
		
		if (!fromCompoVersion && !isVersionControlled())
		{
			//attachments --> attachments
			//paste attachments
			fileIds = AttachmentController.copyAttachmentByCustomerPKAndContext(pubPKFrom, getPublicationPK(pubId), "Images");
		}
		else if (fromCompoVersion && !isVersionControlled())
		{
			//versioning --> attachments
			//Last public versions becomes the new attachment
			pasteDocumentsAsAttachments(pubPKFrom, pubId);
		}
		else if (!fromCompoVersion && isVersionControlled())
		{
			//attachments --> versioning
			//paste versioning documents
			pasteAttachmentsAsDocuments(pubPKFrom, pubId);
			
			SilverTrace.error("kmelia", "KmeliaRequestRouter.processPublicationsPaste", "CANNOT_PASTE_FROM_ATTACHMENTS_TO_VERSIONING");
		}
		else
		{
			//versioning --> versioning
			//paste versioning documents
			pasteDocuments(pubPKFrom, pubId);
		}
		
		return fileIds;
	}

	/******************************************************************************************/
	/* KMELIA - Copier/coller des documents versionnés		                                  */
	/******************************************************************************************/	
	public void pasteDocuments(PublicationPK pubPKFrom, String pubId) throws RemoteException 
	{
		SilverTrace.info("kmelia", "KmeliaSessionController.pasteDocuments()", "root.MSG_GEN_ENTER_METHOD", "pubPKFrom = "+pubPKFrom.toString()+", pubId = "+pubId);
		
		//paste versioning documents attached to publication
		List documents = getVersioningBm().getDocuments(new ForeignPK(pubPKFrom));
		
		SilverTrace.info("kmelia", "KmeliaSessionController.pasteDocuments()", "root.MSG_GEN_PARAM_VALUE", documents.size()+" to paste");
		
		if (documents.size() == 0)
			return;
		
		VersioningUtil versioningUtil = new VersioningUtil();
		String pathFrom = null;	//where the original files are
		String pathTo	= null;	//where the copied files will be
		
		ForeignPK pubPK = new ForeignPK(pubId, getComponentId());
		
		//change the list of workers
		ArrayList workers = getWorkers();
		
		//paste each document
		Document 		document 	= null;
		List 			versions 	= null;
		DocumentVersion version 	= null;
		for (int d=0; d<documents.size(); d++)
		{
			document = (Document) documents.get(d);
			
			SilverTrace.info("kmelia", "KmeliaSessionController.pasteDocuments()", "root.MSG_GEN_PARAM_VALUE", "document name = "+document.getName());
			
			//retrieve all versions of the document
			versions = getVersioningBm().getDocumentVersions(document.getPk());
			
			//retrieve the initial version of the document
			version = (DocumentVersion) versions.get(0);
			
			if (pathFrom == null)
				pathFrom = versioningUtil.createPath(document.getPk().getSpaceId(), document.getPk().getInstanceId(), null);

			//change some data to paste
			document.setPk(new DocumentPK(-1, getSpaceId(), getComponentId()));
			document.setForeignKey(pubPK);
			document.setStatus(Document.STATUS_CHECKINED);
			document.setLastCheckOutDate(new Date());
			document.setWorkList(workers);
			
			if (pathTo == null)
				pathTo = versioningUtil.createPath(getSpaceId(), getComponentId(), null);
			
			String newVersionFile = null;
			if (version != null)
			{
				//paste file on fileserver
				newVersionFile = pasteVersionFile(version.getPhysicalName(), pathFrom, pathTo);
				version.setPhysicalName(newVersionFile);
			}
			
			//create the document with its first version
			DocumentPK documentPK = getVersioningBm().createDocument(document, version);
			document.setPk(documentPK);
			
			for (int v=1; v<versions.size(); v++)
			{
				version = (DocumentVersion) versions.get(v);
				version.setDocumentPK(documentPK);
				SilverTrace.info("kmelia", "KmeliaSessionController.pasteDocuments()", "root.MSG_GEN_PARAM_VALUE", "paste version = "+version.getLogicalName());
				
				//paste file on fileserver
				newVersionFile = pasteVersionFile(version.getPhysicalName(), pathFrom, pathTo);
				version.setPhysicalName(newVersionFile);
			
				//paste data
				getVersioningBm().addVersion(version);
			}
		}
	}
	
	private ArrayList getWorkers()
	{
		ArrayList workers = new ArrayList();
		
		List workingProfiles = new ArrayList();
		workingProfiles.add("writer");
		workingProfiles.add("publisher");
		workingProfiles.add("admin");
		String[] userIds = getOrganizationController().getUsersIdsByRoleNames(getComponentId(), workingProfiles);
		
		String 		userId 	= null;
		Worker		worker	= null;
		for (int u=0; u<userIds.length; u++)
		{
			userId = (String) userIds[u];
  			worker = new Worker(new Integer(userId).intValue(), -1, u, false, true, getComponentId(), "U", false, true, 0);
  			workers.add(worker);
		}
		
		return workers;
	}
	
	public void pasteDocumentsAsAttachments(PublicationPK pubPKFrom, String pubId) throws RemoteException 
	{
		SilverTrace.info("kmelia", "KmeliaSessionController.pasteDocumentsAsAttachments()", "root.MSG_GEN_ENTER_METHOD", "pubPKFrom = "+pubPKFrom.toString()+", pubId = "+pubId);
		
		//paste versioning documents attached to publication
		List documents = getVersioningBm().getDocuments(new ForeignPK(pubPKFrom));
		
		SilverTrace.info("kmelia", "KmeliaSessionController.pasteDocumentsAsAttachments()", "root.MSG_GEN_PARAM_VALUE", documents.size()+" documents to paste");
		
		if (documents.size() == 0)
			return;
		
		VersioningUtil versioningUtil = new VersioningUtil();
		String pathFrom = null;	//where the original files are
		String pathTo	= null;	//where the copied files will be
					
		//paste each document
		Document 		document 	= null;
		DocumentVersion version 	= null;
		for (int d=0; d<documents.size(); d++)
		{
			document = (Document) documents.get(d);
			
			SilverTrace.info("kmelia", "KmeliaSessionController.pasteDocumentsAsAttachments()", "root.MSG_GEN_PARAM_VALUE", "document name = "+document.getName());
			
			//retrieve last public versions of the document
			version = getVersioningBm().getLastPublicDocumentVersion(document.getPk());
			
			if (pathFrom == null)
				pathFrom = versioningUtil.createPath(document.getPk().getSpaceId(), document.getPk().getInstanceId(), null);

			if (pathTo == null)
				pathTo = AttachmentController.createPath(getComponentId(), "Images");
			
			String newVersionFile = null;
			if (version != null)
			{
				//paste file on fileserver
				newVersionFile = pasteVersionFile(version.getPhysicalName(), pathFrom, pathTo);
				
				if (newVersionFile != null)
				{
					//create the attachment in DB
					//Do not index it cause made by the updatePublication call later
					AttachmentDetail attachment = new AttachmentDetail(new AttachmentPK("unknown", getComponentId()), newVersionFile, version.getLogicalName(), "", version.getMimeType(), version.getSize(), "Images", new Date(), getPublicationPK(pubId), document.getName(), document.getDescription(),0);
					AttachmentController.createAttachment(attachment, false);
				}
			}
		}
	}
	
	public void pasteAttachmentsAsDocuments(PublicationPK pubPKFrom, String pubId) throws RemoteException 
	{
		SilverTrace.info("kmelia", "KmeliaSessionController.pasteAttachmentsAsDocuments()", "root.MSG_GEN_ENTER_METHOD", "pubPKFrom = "+pubPKFrom.toString()+", pubId = "+pubId);
		
		List attachments = AttachmentController.searchAttachmentByPKAndContext(pubPKFrom, "Images");
		
		SilverTrace.info("kmelia", "KmeliaSessionController.pasteAttachmentsAsDocuments()", "root.MSG_GEN_PARAM_VALUE", attachments.size()+" attachments to paste");
		
		if (attachments.size() == 0)
			return;
		
		ArrayList workers = getWorkers();
		
		VersioningUtil versioningUtil = new VersioningUtil();
		String pathFrom = null;	//where the original files are
		String pathTo	= null;	//where the copied files will be
					
		//paste each attachment
		Document 			document 	= null;
		DocumentVersion 	version 	= null;
		AttachmentDetail 	attachment 	= null;
		for (int d=0; d<attachments.size(); d++)
		{
			attachment = (AttachmentDetail) attachments.get(d);
			
			SilverTrace.info("kmelia", "KmeliaSessionController.pasteAttachmentsAsDocuments()", "root.MSG_GEN_PARAM_VALUE", "attachment name = "+attachment.getLogicalName());
			
			if (pathTo == null)
				pathTo = versioningUtil.createPath(getSpaceId(), getComponentId(), null);

			if (pathFrom == null)
				pathFrom = AttachmentController.createPath(pubPKFrom.getInstanceId(), "Images");
			
			//paste file on fileserver
			String newPhysicalName = pasteVersionFile(attachment.getPhysicalName(), pathFrom, pathTo);
			
			if (newPhysicalName != null)
			{
				//Document creation
				document = new Document(new DocumentPK(-1, "useless", getComponentId()), getPublicationPK(pubId), attachment.getLogicalName(), attachment.getInfo(), 0, Integer.parseInt(getUserId()), new Date(), "", getComponentId(), workers, new ArrayList(), 0, 0);
				
				//Version creation
				version = new DocumentVersion(null, null, 1, 0, Integer.parseInt(getUserId()), new Date(), "", DocumentVersion.TYPE_PUBLIC_VERSION, DocumentVersion.STATUS_VALIDATION_NOT_REQ, newPhysicalName, attachment.getLogicalName(), attachment.getType(), new Long(attachment.getSize()).intValue(), getComponentId());
	
				getVersioningBm().createDocument(document, version);
			}
		}
	}
	
	private String pasteVersionFile(String fileNameFrom, String from, String to)
	{
		SilverTrace.info("kmelia", "KmeliaSessionController.pasteVersionFile()", "root.MSG_GEN_ENTER_METHOD", "version = "+fileNameFrom);
		
		if (!fileNameFrom.equals("dummy"))
		{
			//we have to rename pasted file (in case the copy/paste append in the same instance)
			String type			= FileRepositoryManager.getFileExtension(fileNameFrom);
			String fileNameTo	= new Long(new Date().getTime()).toString() + "." +type;

			try {
				//paste file associated to the first version
				FileRepositoryManager.copyFile(from+fileNameFrom, to+fileNameTo);
			} catch (Exception e) {
				SilverTrace.error("kmelia", "KmeliaSessionController.pasteVersionFile()", "root.EX_FILE_NOT_FOUND", from+fileNameFrom);
				return null;
				//throw new KmeliaRuntimeException("KmeliaSessionController.pasteVersionFile()",SilverpeasRuntimeException.ERROR, "root.EX_FILE_NOT_FOUND", e);
			}
			return fileNameTo;
		} else {
			return fileNameFrom;
		}
	}
	
	public int addPublicationsToLink(String pubId) throws RemoteException
	{
		InfoLinkDetail infoLinkDetail = null;
        ArrayList infoLinks = new ArrayList();
        String pubIdToLink = null;
        for (int i=0; i<publicationsToLink.size(); i++)
        {
        	pubIdToLink = (String) publicationsToLink.get(i);
        	infoLinkDetail = new InfoLinkDetail(null, "1", null, pubIdToLink);
        	infoLinks.add(infoLinkDetail);
        }
        InfoDetail infos = new InfoDetail(null, null, null, infoLinks, null);
        updateInfoDetail(pubId, infos);
        
        resetPublicationsToLink();
        
        return infoLinks.size();
	}

    /**************************************************************************************/
    /* KMelia - Gestion des objets session                                                */
    /**************************************************************************************/
	public List getPublicationsToLink()
	{
		return publicationsToLink;
	}
	
	public void resetPublicationsToLink()
	{
		publicationsToLink.clear();
	}
	
    public void setSessionTopic(TopicDetail topicDetail) {
        this.sessionTopic = topicDetail;
        if (topicDetail != null)
        {
        	setSessionPublicationsList((List) topicDetail.getPublicationDetails());
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

    public void setSessionPublicationsList(List publications) {
        this.sessionPublicationsList = publications;
    }

    public void setSessionCombination(ArrayList combination) {
        this.sessionCombination = combination;
    }
    public void setSessionTimeCriteria(String timeCriteria) {
        this.sessionTimeCriteria = timeCriteria;
    }

    public String getSortValue() {
        return this.sortValue;
    }
    public void setSortValue(String sort) throws RemoteException {
    	if (isDefined(sort))
    		this.sortValue = sort;
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
    
    public UserCompletePublication getSessionPubliOrClone()
    {
    	if (getSessionClone() != null)
    		return getSessionClone();
    	else
    		return getSessionPublication();
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

	public List getSessionPublicationsList() {
        return this.sessionPublicationsList;
    }

	public ArrayList getSessionCombination() {
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
	
	public String initUPToSelectValidator(String pubId)
	{
		String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
		PairObject hostComponentName = new PairObject(getComponentLabel(), "");
		PairObject[] hostPath = new PairObject[1];
		hostPath[0] = new PairObject(getString("kmelia.SelectValidator"), "");
		String hostUrl 		= m_context+URLManager.getURL("useless", getComponentId())+"SetValidator?PubId="+pubId;
		String cancelUrl 	= m_context+URLManager.getURL("useless", getComponentId())+"SetValidator?PubId="+pubId;
		
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
		if (isTargetMultiValidationEnable())
			sel.setMultiSelect(true);
		else
			sel.setMultiSelect(false);
		sel.setPopupMode(true);
		sel.setSetSelectable(false);
		
		//Add extra params
        SelectionUsersGroups sug = new SelectionUsersGroups();
        sug.setComponentId(getComponentId());
        
        ArrayList profiles = new ArrayList();
        profiles.add("publisher");
        profiles.add("admin");
        
        boolean haveRights = isRightsOnTopicsEnabled() && getSessionTopic().getNodeDetail().haveRights();
        if (haveRights)
        {
        	int rightsDependsOn = getSessionTopic().getNodeDetail().getRightsDependsOn();
        	List profileInsts = getAdmin().getProfilesByObject(Integer.toString(rightsDependsOn), ObjectType.NODE, getComponentId());
        	if (profileInsts != null)
        	{
        		ProfileInst profileInst = null;
        		for (int p=0; p<profileInsts.size(); p++)
        		{
        			profileInst = (ProfileInst) profileInsts.get(p);
        			if (profileInst != null)
        			{
	        			if (profiles.contains(profileInst.getName()))
	        				sug.addProfileId(profileInst.getId());
        			}
        		}
        	}
        }
        else
        {
	        sug.setProfileNames(profiles);
        }
        
        sel.setExtraParams(sug);

		return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
	}
	
	/*************************************************************/
	/** SCO - 26/12/2002 Integration AlertUser et AlertUserPeas **/
	/*************************************************************/
	public String initAlertUser() throws RemoteException {
		String pubId = getSessionPublication().getPublication().getPublicationDetail().getPK().getId();
		
        AlertUser sel = getAlertUser();
		// Initialisation de AlertUser
        sel.resetAll();
		sel.setHostSpaceName(getSpaceLabel()); // set nom de l'espace pour browsebar
		sel.setHostComponentId(getComponentId()); // set id du composant pour appel selectionPeas (extra param permettant de filtrer les users ayant acces au composant)
		PairObject hostComponentName = new PairObject(getComponentLabel(),  null); // set nom du composant pour browsebar (PairObject(nom_composant, lien_vers_composant)) NB : seul le 1er element est actuellement utilisé (alertUserPeas est toujours présenté en popup => pas de lien sur nom du composant)
		sel.setHostComponentName(hostComponentName);
		sel.setNotificationMetaData(getAlertNotificationMetaData(pubId)); // set NotificationMetaData contenant les informations à notifier
		// fin initialisation de AlertUser
		// l'url de nav vers alertUserPeas et demandée à AlertUser et retournée
        return AlertUser.getAlertUserURL(); 
    }
	/*************************************************************/

	public void toRecoverUserId(){
        Selection sel = getSelection();
        idSelectedUser = SelectionUsersGroups.getDistinctUserIds(sel.getSelectedElements(),sel.getSelectedSets());
	}

	public boolean isVersionControlled() {
		String strVersionControlled = this.getComponentParameterValue("versionControl");
		return ((strVersionControlled != null) && !("").equals(strVersionControlled) && !("no").equals(strVersionControlled.toLowerCase()));
	}
	
	public boolean isVersionControlled(String anotherComponentId)
	{
		String strVersionControlled = getOrganizationController().getComponentParameterValue(anotherComponentId, "versionControl");
		return ((strVersionControlled != null) && !("").equals(strVersionControlled) && !("no").equals(strVersionControlled.toLowerCase()));
	}

	/**
	 * 
	 * @param pubId
	 * @return
	 * @throws RemoteException
	 */
	public boolean isWriterApproval(String pubId) throws RemoteException
	{
	    ArrayList documents = getVersioningBm().getDocuments((new ForeignPK(pubId, getComponentId())));
		Iterator documentsIterator = documents.iterator();
		while (documentsIterator.hasNext())
		{
			Document document = (Document) documentsIterator.next();
	        ArrayList writers = document.getWorkList();
	        for (int i=0; i<writers.size(); i++)
	        {
	            Worker user = (Worker) writers.get(i);
	            if (user.getUserId() == new Integer(getUserId()).intValue())
	            	if (user.isApproval())
	            		return true;
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
	
	public boolean isValidationTabVisible() 
	{
		boolean tabVisible = PublicationDetail.TO_VALIDATE.equalsIgnoreCase(getSessionPubliOrClone().getPublication().getPublicationDetail().getStatus());

		return tabVisible && (getValidationType() == KmeliaHelper.VALIDATION_COLLEGIATE || getValidationType() == KmeliaHelper.VALIDATION_TARGET_N);
	}
	
	public int getValidationType()
	{
		if (isTargetValidationEnable())
			return KmeliaHelper.VALIDATION_TARGET_1;
		else if (isTargetMultiValidationEnable())
			return KmeliaHelper.VALIDATION_TARGET_N;
		else if (isCollegiateValidationEnable())
			return KmeliaHelper.VALIDATION_COLLEGIATE;
		else
			return KmeliaHelper.VALIDATION_CLASSIC;
	}
	
	public boolean isCoWritingEnable() {
		return "yes".equalsIgnoreCase(getComponentParameterValue("coWriting"));
	}

	public String[] getSelectedUsers(){
		return idSelectedUser;
	}

	public int getSilverObjectId(String objectId) {
		int silverObjectId = -1;
		try
		{
			silverObjectId = getKmeliaBm().getSilverObjectId(getPublicationPK(objectId));
		} catch (Exception e)
		{
			SilverTrace.error("kmelia", "KmeliaSessionController.getSilverObjectId()", "root.EX_CANT_GET_LANGUAGE_RESOURCE", "objectId=" + objectId, e);
		} 
		return silverObjectId;
	}
	
	public void close()
	{
		removeEJBs(commentBm);
		removeEJBs(searchEngineEjb);
		removeEJBs(versioningBm);
	}
	
	private void removeEJBs(EJBObject ejbBm)
	{
		try
		{
			if (ejbBm != null)
				ejbBm.remove();
		}
		catch (RemoteException e)
		{
			SilverTrace.error("kmelia", "KmeliaSessionController.removeEJBs", "", e);
		}
		catch (RemoveException e)
		{
			SilverTrace.error("kmelia", "KmeliaSessionController.removeEJBs", "", e);
		}
	}
	
	private boolean isPublicationClassifiedOnPDC(String pubId)
	{
		if (pubId != null && pubId.length()>0)
		{
			try
			{
				int silverObjectId = getKmeliaBm().getSilverObjectId(getPublicationPK(pubId));
				List positions = getPdcBm().getPositions(silverObjectId, getComponentId());
				return (positions.size()>0);
			}
			catch (Exception e)
			{
				throw new KmeliaRuntimeException("KmeliaSessionController.isPublicationClassifiedOnPDC()", SilverpeasRuntimeException.ERROR, "kmelia.MSG_ERR_GENERAL", e);
			}
		}
		return false;
	}
	
	public boolean isCurrentPublicationHaveContent() throws WysiwygException
	{
		return (getSessionPublication().getPublication().getModelDetail() != null || StringUtil.isDefined(WysiwygController.load(getComponentId(), getSessionPublication().getId(), getCurrentLanguage())) || !isInteger(getSessionPublication().getPublication().getPublicationDetail().getInfoId())); 
	}
	
	public void pastePdcPositions(PublicationPK fromPK, String toPubId) throws RemoteException, PdcException
	{
		int fromSilverObjectId = getKmeliaBm().getSilverObjectId(fromPK);
		int toSilverObjectId = getKmeliaBm().getSilverObjectId(getPublicationPK(toPubId));
			
		getPdcBm().copyPositions(fromSilverObjectId, fromPK.getInstanceId(), toSilverObjectId, getComponentId());
	}
	
	public boolean isPDCClassifyingMandatory()
	{
		try 
		{
			return getPdcBm().isClassifyingMandatory(getComponentId());
		} catch (Exception e) {
			throw new KmeliaRuntimeException("KmeliaSessionController.isPDCClassifyingPossible()", SilverpeasRuntimeException.ERROR, "kmelia.MSG_ERR_GENERAL", e);
		}
	}

	/**
	 * @return
	 */
	public PdcBm getPdcBm() {
		if (pdcBm == null)
			pdcBm = new PdcBmImpl();
		return pdcBm;
	}
	
	public NodeBm getNodeBm() {
		  NodeBm nodeBm = null;
		  try {
			  NodeBmHome nodeBmHome = (NodeBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
	          nodeBm = nodeBmHome.create();
		  } catch (Exception e) {
			  throw new KmeliaRuntimeException("KmeliaSessionController.getNodeBm()",SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_NODEBM_HOME", e);
		  }
		  return nodeBm;
	  }
	
	public PublicationBm getPublicationBm() {
		PublicationBm pubBm = null;
		try {
			PublicationBmHome pubBmHome = (PublicationBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME, PublicationBmHome.class);
			pubBm = pubBmHome.create();
		} catch (Exception e) {
			throw new KmeliaRuntimeException("KmeliaSessionController.getPublicationBm()",SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_NODEBM_HOME", e);
		}
		return pubBm;
	}

	/**
	 * @return
	 */
	public NotificationManager getNotificationManager() {
		if (notificationManager == null)
			notificationManager = new NotificationManager(null);
		return notificationManager;
	}

	/**
	 * @return
	 */
	public String getAutoRedirectURL() {
		if (autoRedirectURL == null)
			autoRedirectURL = getNotificationManager().getUserAutoRedirectURL(getUserId());
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
	public ArrayList importFile(File fileUploaded, String fileType, String topicId, String importMode, boolean draftMode, int versionType)
	{
        SilverTrace.debug("kmelia","KmeliaSessionController.importFile()","root.MSG_GEN_ENTER_METHOD","fileUploaded = "+fileUploaded.getAbsolutePath()+" fileType="+fileType+" importMode="+importMode+" draftMode="+draftMode+" versionType="+versionType);
		ArrayList publicationDetails = null;
		FileImport fileImport = new FileImport();
		fileImport.setFileUploaded(fileUploaded);
		fileImport.setTopicId(topicId);
		fileImport.setDraftMode(draftMode);
		fileImport.setVersionType(versionType);
		fileImport.setKmeliaScc(this);
		if (importMode.equals(UNITARY_IMPORT_MODE))
			publicationDetails = fileImport.importFile();
		else if (importMode.equals(MASSIVE_IMPORT_MODE_ONE_PUBLICATION) && (fileType.equals(KmeliaSessionController.FILETYPE_ZIP1) || fileType.equals(KmeliaSessionController.FILETYPE_ZIP2)))
			publicationDetails = fileImport.importFiles();
		else if (importMode.equals(MASSIVE_IMPORT_MODE_MULTI_PUBLICATIONS) && (fileType.equals(KmeliaSessionController.FILETYPE_ZIP1) || fileType.equals(KmeliaSessionController.FILETYPE_ZIP2)))
			publicationDetails = fileImport.importFilesMultiPubli();
		return publicationDetails;
	}
	
	private PublicationPK getPublicationPK(String id)
	{
		return new PublicationPK(id, getSpaceId(), getComponentId());
	}
	
	private NodePK getNodePK(String id)
	{
		return new NodePK(id, getSpaceId(), getComponentId());
	}
	
	/**
	 * Return if publication is in the basket
	 * @param pubId
	 * @return true or false
	 */
	public boolean isPublicationDeleted(String pubId)
	{
		boolean isPublicationDeleted = false;
		try {
			Collection pathList = getPathList(pubId);
	        SilverTrace.debug("kmelia","KmeliaSessionController.isPublicationDeleted()","root.MSG_GEN_PARAM_VALUE","pathList = "+pathList);
			if (pathList.size() == 1)
			{
		        Iterator i = pathList.iterator();
			    while (i.hasNext())
			    {
				  Collection path = (Collection) i.next();
				  Iterator j = path.iterator();
				  while (j.hasNext()) {
				       NodeDetail nodeInPath = (NodeDetail) j.next();
				       SilverTrace.debug("kmelia","KmeliaSessionController.isPublicationDeleted()","root.MSG_GEN_PARAM_VALUE","nodeInPath = "+nodeInPath);
				       if (nodeInPath.getNodePK().getId().equals("1"))
				    	   isPublicationDeleted = true;
				  }
	          }
			}
		}
		catch (Exception e)
		{
			throw new KmeliaRuntimeException("KmeliaSessionController.isPublicationDeleted()", SilverpeasRuntimeException.ERROR, "kmelia.MSG_ERR_GENERAL", e);
		}
        SilverTrace.debug("kmelia","KmeliaSessionController.isPublicationDeleted()","root.MSG_GEN_PARAM_VALUE","isPublicationDeleted="+isPublicationDeleted);
		return isPublicationDeleted;
	}
	
	public void addModelUsed(String[] models)
	{
		try {
			getKmeliaBm().addModelUsed(models, getComponentId());
		} catch (RemoteException e) {
			throw new KmeliaRuntimeException("KmeliaSessionController.addModelUsed()", SilverpeasRuntimeException.ERROR, "kmelia.MSG_ERR_GENERAL", e);
		}
	}
	
	public Collection getModelUsed()
	{
		Collection result = null;
		try {
			result = getKmeliaBm().getModelUsed(getComponentId());
		} catch (RemoteException e) {
			throw new KmeliaRuntimeException("KmeliaSessionController.getModelUsed()", SilverpeasRuntimeException.ERROR, "kmelia.MSG_ERR_GENERAL", e);
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
	/**************************************************************************************/
	/**************************************************************************************/
	/*  Specific methods for Kmax */
	/***************************************************************************************/
	/** Parameter of kmax **/
	/** Parameter for time Axis visibility */
	public boolean isTimeAxisUsed() {
		return "yes".equalsIgnoreCase(getComponentParameterValue("timeAxisUsed"));
	}

	/** Parameter for fields visibility of the publication */
	public boolean isFieldDescriptionVisible() {
		String paramValue = getComponentParameterValue("useDescription");
		return "1".equalsIgnoreCase(paramValue) || "2".equalsIgnoreCase(paramValue) || "".equals(paramValue);
    }
	public boolean isFieldDescriptionMandatory() {
		return "2".equalsIgnoreCase(getComponentParameterValue("useDescription"));
    }
	public boolean isFieldKeywordsVisible() {
		String paramValue = getComponentParameterValue("useKeywords");
		return "yes".equalsIgnoreCase(paramValue) || "".equals(paramValue);
    }
	public boolean isFieldImportanceVisible() {
		return "yes".equalsIgnoreCase(getComponentParameterValue("useImportance")) || getSettings().getBoolean("showImportance", true);
    }
	public boolean isFieldVersionVisible() {
		return "yes".equalsIgnoreCase(getComponentParameterValue("useVersion")) || getSettings().getBoolean("showPubVersion", true);
    }
	
	/**************************************************************************************/
	/* Interface - Gestion des axes */                                                       
	/**************************************************************************************/
	
	public List getTimeAxisKeys()
	{
		if (this.timeAxis == null)
		{
			ResourceLocator timeSettings = new ResourceLocator("com.stratelia.webactiv.kmelia.multilang.timeAxisBundle", getLanguage());
			Enumeration keys = timeSettings.getKeys();
			List orderKeys = new ArrayList();
			Integer key = null;
			String keyStr = "";
			while (keys.hasMoreElements())
			{
				keyStr = (String) keys.nextElement();
				key = new Integer(keyStr);
				orderKeys.add(key);
			}
			Collections.sort(orderKeys);
			this.timeAxis = orderKeys;
		}
		return this.timeAxis;
	}

	public synchronized List getAxis() throws RemoteException
	{
		return getKmeliaBm().getAxis(getComponentId());
	}

	public synchronized List getAxisHeaders() throws RemoteException
	{
		return getKmeliaBm().getAxisHeaders(getComponentId());
	}

	public synchronized NodePK addAxis(NodeDetail axis) throws RemoteException
	{
		return getKmeliaBm().addAxis(axis, getComponentId());
	}

	public synchronized NodeDetail getNodeHeader(String id) throws RemoteException
	{
		return getKmeliaBm().getNodeHeader(id, getComponentId());
	}

	public synchronized void updateAxis(NodeDetail axis) throws RemoteException
	{
		getKmeliaBm().updateAxis(axis, getComponentId());
	}

	public synchronized void deleteAxis(String axisId) throws RemoteException
	{
		getKmeliaBm().deleteAxis(axisId, getComponentId());
	}

	public synchronized List search(ArrayList combination) throws RemoteException
	{
		List publis = (List) getKmeliaBm().search(combination, getComponentId());
		setSessionPublicationsList(publis);
		applyVisibilityFilter();
		return getSessionPublicationsList();
	}

	public synchronized List search(ArrayList combination, int nbDays) throws RemoteException
	{
		List publis = (List) getKmeliaBm().search(combination, nbDays, getComponentId());
		setSessionPublicationsList(publis);
		applyVisibilityFilter();
		return getSessionPublicationsList();
	}

	public synchronized List getUnbalancedPublications() throws RemoteException
	{
		return (List) getKmeliaBm().getUnbalancedPublications(getComponentId());
	}

	/**************************************************************************************/
	/* Kmax - Positions under axis				                                                  */
	/**************************************************************************************/
	public synchronized NodePK addPosition(String fatherId, NodeDetail position) throws RemoteException
	{
		SilverTrace.info(
			"kmax",
			"KmeliaSessionController.addPosition()",
			"root.MSG_GEN_PARAM_VALUE",
			"fatherId = " + fatherId + " And position = " + position.toString());
		return getKmeliaBm().addPosition(fatherId, position,  getComponentId(),  getUserId());
	}
	public synchronized void updatePosition(NodeDetail position) throws RemoteException
	{
		getKmeliaBm().updatePosition(position, getComponentId());
	}

	public synchronized void deletePosition(String positionId) throws RemoteException
	{
		getKmeliaBm().deletePosition(positionId, getComponentId());
	}

	/**************************************************************************************/
	/* Kmax - Reindexation				                                                  */
	/**************************************************************************************/
	public synchronized void indexKmax(String componentId) throws RemoteException
	{
		getKmeliaBm().indexKmax(componentId);
	}

	/**************************************************************************************/
	/* Kmax - Publications				                                                  */
	/**************************************************************************************/
	public synchronized UserCompletePublication getKmaxCompletePublication(String pubId) throws RemoteException
	{
		return getKmeliaBm().getKmaxCompletePublication(pubId, getUserId());
	}

    public synchronized Collection getPublicationCoordinates(String pubId) throws RemoteException
	{
		return getKmeliaBm().getPublicationCoordinates(pubId, getComponentId());
	}
	
	public synchronized void addPublicationToCombination(String pubId, ArrayList combination) throws RemoteException
	{
		getKmeliaBm().addPublicationToCombination(pubId, combination, getComponentId());
	}
	
	public synchronized void deletePublicationFromCombination(String pubId, String combinationId) throws RemoteException
	{
		getKmeliaBm().deletePublicationFromCombination(pubId, combinationId, getComponentId());
	}

	/**
	 * Get session publications
	 * @return List of WAAtributeValuePair (Id and InstanceId)
	 * @throws RemoteException
	 */
	public List getCurrentPublicationsList () throws RemoteException
	{
		List currentPublications = new ArrayList();
        Collection allPublications = getSessionPublicationsList();
        SilverTrace.info("kmelia", "KmeliaSessionController.getCurrentPublicationsList()", "root.MSG_PARAM_VALUE", "NbPubli=" + allPublications.size());
        Iterator allPublis = allPublications.iterator();
        while (allPublis.hasNext())
        {
        	UserPublication userPubli = (UserPublication) allPublis.next();
        	PublicationDetail pubDetail = userPubli.getPublication(); 
        	if (pubDetail.getStatus().equals(PublicationDetail.VALID))
        	{
	            SilverTrace.info("kmelia", "KmeliaSessionController.getCurrentPublicationsList()", "root.MSG_PARAM_VALUE", "Get pubId" + pubDetail.getId() + "InstanceId="+pubDetail.getInstanceId());
	            currentPublications.add(new WAAttributeValuePair(pubDetail.getId(), pubDetail.getInstanceId()));
        	}
        }
		return currentPublications;
	}

	/**************************************************************************************/
	/* Kmax - Utils				                                                  */
	/**************************************************************************************/
	public synchronized Collection getPath(String positionId) throws RemoteException
	{
		return getKmeliaBm().getPath(positionId, getComponentId());
	}

	public void setCurrentCombination(ArrayList combination)
	{
		this.currentCombination = combination;
	}

	public ArrayList getCurrentCombination()
	{
		return currentCombination;
	}

    /**
	 * Transform combination axis from String /0/1037,/0/1038
	 * in ArrayList /0/1037 then /0/1038 etc...
	 * @param axisValuesStr
	 * @return Collection of combination
	 * 
	 */
	private ArrayList convertStringCombination2List(String axisValuesStr)
	{
		ArrayList combination = new ArrayList();
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
	public ArrayList getCombination(String axisValuesStr)
	{
        SilverTrace.info("kmelia", "KmeliaSessionController.getCombination(String)", "root.MSG_GEN_PARAM_VALUE","axisValuesStr="+axisValuesStr);
		return convertStringCombination2List(axisValuesStr);
	}
	
	private String getNearPublication(int direction)
	{
		String pubId = "";
		
		// rechercher le rang de la publication précédente
		int rangNext = rang + direction;
		
		UserPublication pub = (UserPublication) getSessionPublicationsList().get(rangNext);
		pubId = pub.getPublication().getId();
		
		// on est sur la précédente, mettre à jour le rang avec la publication courante
		rang = rangNext;
		
		return pubId;
	}
	
	public String getFirst()
	{
		rang = 0;
		UserPublication pub = (UserPublication) getSessionPublicationsList().get(0);
		String pubId = pub.getPublication().getId();

		return pubId;
	}
	
	/**
	 * getPrevious
	 * @return previous publication id
	 */
	public String getPrevious()
	{
		return getNearPublication(-1);
	}
	
	/**
	 * getNext
	 * @return next publication id
	 */
	public String getNext()
	{
		return getNearPublication(1);
	}

	public int getRang()
	{
		return rang;
	}
	
	private boolean isDefined(String param)
	{
		return (param != null && param.length()>0 && !"".equals(param) && !"null".equals(param)); 
	}

	public List getSessionTreeview() {
		return sessionTreeview;
	}

	public void setSessionTreeview(List sessionTreeview) {
		this.sessionTreeview = sessionTreeview;
	}
	
	public synchronized boolean isDragAndDropEnable() throws RemoteException
	{
		try
		{
			return getPersonalization().getDragAndDropStatus() && "yes".equals(getSettings().getString("massiveDragAndDropAllowed")) && isMassiveDragAndDropAllowed();
		}
		catch (NoSuchObjectException nsoe)
		{
			initPersonalization();
			return getPersonalization().getDragAndDropStatus() && "yes".equals(getSettings().getString("massiveDragAndDropAllowed")) && isMassiveDragAndDropAllowed();
		}
	}

	public String getCurrentLanguage() {
		return currentLanguage;
	}

	public void setCurrentLanguage(String currentLanguage) {
		this.currentLanguage = currentLanguage;
	}
	
	public String initUserPanelForTopicProfile(String role, String nodeId) throws RemoteException
	{
		String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
		PairObject[] hostPath = new PairObject[1];
		hostPath[0] = new PairObject(getString("kmelia.SelectValidator"), "");
		
		Selection sel = getSelection();
		sel.resetAll();
		sel.setHostSpaceName(getSpaceLabel());
		sel.setHostComponentName(new PairObject(getComponentLabel(), ""));
		sel.setHostPath(hostPath);
		
		String hostUrl 		= m_context+URLManager.getURL("useless", getComponentId())+"TopicProfileSetUsersAndGroups?Role="+role+"&NodeId="+nodeId;
		String cancelUrl 	= m_context+URLManager.getURL("useless", getComponentId())+"CloseWindow";
		
		sel.setGoBackURL(hostUrl);
		sel.setCancelURL(cancelUrl);
		
		// Contraintes		
		//sel.setMultiSelect(false);
		//sel.setPopupMode(true);
		//sel.setSetSelectable(false);
		
		List profiles = getAdmin().getProfilesByObject(nodeId, ObjectType.NODE, getComponentId());
		ProfileInst topicProfile = getProfile(profiles, role);
		
		SelectionUsersGroups sug = new SelectionUsersGroups();
        sug.setComponentId(getComponentId());
		
        boolean useComponentProfiles = true;
        
		/*NodeDetail node = getNodeHeader(nodeId);
		if (node.haveInheritedRights())
		{
			//The selectable users and groups are ones of the inherited node
			List inheritedProfiles = getAdmin().getProfilesByObject(Integer.toString(node.getRightsDependsOn()), getComponentId());
			
			setProfileIds(sug, inheritedProfiles);
			sel.setExtraParams(sug);
			
			useComponentProfiles = false;
		}
		else if(node.haveLocalRights())
		{
			//The selectable users and groups are ones of n-1 level
			NodePK 		fatherPK 	= node.getFatherPK();
			NodeDetail 	father 		= getNodeBm().getHeader(fatherPK);
			
			if (father.haveRights())
			{
				List inheritedProfiles = getAdmin().getProfilesByObject(Integer.toString(father.getRightsDependsOn()), getComponentId());
				
				setProfileIds(sug, inheritedProfiles);
				sel.setExtraParams(sug);
				
				useComponentProfiles = false;
			}
		}*/
		
		if (useComponentProfiles)
		{
			//The selectable users and groups are component's ones.
			ArrayList profileNames = new ArrayList();
	        profileNames.add("user");
	        profileNames.add("writer");
	        profileNames.add("publisher");
	        profileNames.add("admin");
	        sug.setProfileNames(profileNames);
	        sel.setExtraParams(sug);
		}
		
		if (topicProfile != null)
		{
			sel.setSelectedElements((String[])topicProfile.getAllUsers().toArray(new String[0]));
			sel.setSelectedSets((String[])topicProfile.getAllGroups().toArray(new String[0]));
		}
		
		return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
	}
	
	private void deleteTopicRoles(NodeDetail node) throws RemoteException
	{
		if (node != null && node.haveLocalRights())
		{
			List profiles = getTopicProfiles(node.getNodePK().getId());
			ProfileInst profile = null;
			for (int p=0; profiles!=null && p<profiles.size(); p++)
			{
				profile = (ProfileInst) profiles.get(p);
				if (profile != null && StringUtil.isDefined(profile.getId()))
				{
					deleteTopicRole(profile.getId());
				}
			}
		}
	}
	
	public void deleteTopicRole(String profileId) throws RemoteException
	{
		//Remove the profile
		getAdmin().deleteProfileInst(profileId);
	}
	
	public void updateTopicRole(String role, String nodeId) throws RemoteException
	{
		ProfileInst profile = getTopicProfile(role, nodeId);
		
		//Update the topic
		NodeDetail topic = getNodeHeader(nodeId);
		topic.setRightsDependsOnMe();
		getNodeBm().updateRightsDependency(topic);
		
		if (StringUtil.isDefined(profile.getId()))
		{			
			//Update the profile
			profile.removeAllGroups();
			profile.removeAllUsers();
			
			profile.setGroupsAndUsers(getSelection().getSelectedSets(), getSelection().getSelectedElements());
			
			getAdmin().updateProfileInst(profile);
		}
		else
		{
			//Create the profile
			profile.setObjectId(Integer.parseInt(nodeId));
			profile.setObjectType(ObjectType.NODE);
	        profile.setComponentFatherId(getComponentId());
	        
	        profile.setGroupsAndUsers(getSelection().getSelectedSets(), getSelection().getSelectedElements());
	        
	        getAdmin().addProfileInst(profile);
		}
	}
	
	public void updateTopicDependency(NodeDetail node, boolean enableIt) throws RemoteException
	{
		if (!enableIt)
		{
			NodePK 		fatherPK 	= node.getFatherPK();
			NodeDetail 	father 		= getNodeBm().getHeader(fatherPK);
			
			node.setRightsDependsOn(father.getRightsDependsOn());
			
			//Topic profiles must be removed
			List profiles = getAdmin().getProfilesByObject(node.getNodePK().getId(), ObjectType.NODE, getComponentId());
			for (int p=0; profiles != null && p<profiles.size(); p++)
			{
				ProfileInst profile = (ProfileInst) profiles.get(p);
				if (profile != null)
					getAdmin().deleteProfileInst(profile.getId());
			}
		}
		else
		{
			node.setRightsDependsOnMe();
		}
		
		getNodeBm().updateRightsDependency(node);
	}
	
	public ProfileInst getTopicProfile(String role, String topicId)
	{
		List profiles = getAdmin().getProfilesByObject(topicId, ObjectType.NODE, getComponentId());
		for (int p=0; profiles != null && p<profiles.size(); p++)
		{
			ProfileInst profile = (ProfileInst) profiles.get(p);
			if (profile.getName().equals(role))
				return profile;
		}

		ProfileInst profile = new ProfileInst();
		profile.setName(role);
		return profile;
	}
	
	public ProfileInst getTopicProfile(String role)
	{
		return getTopicProfile(role, getSessionTopic().getNodePK().getId());
	}
	
	public ProfileInst getProfile(String role)
	{
		ComponentInst componentInst = getAdmin().getComponentInst(getComponentId());
		ProfileInst profile 			= componentInst.getProfileInst(role);
		ProfileInst inheritedProfile 	= componentInst.getInheritedProfileInst(role);
		
		if (profile == null && inheritedProfile == null)
		{
			profile = new ProfileInst();
			profile.setName(role);
			
			return profile;
		}
		else if (profile != null && inheritedProfile == null)
		{
			return profile;
		}
		else if (profile == null && inheritedProfile != null)
		{
			return inheritedProfile;
		}
		else
		{
			//merge des profiles
			ProfileInst newProfile = (ProfileInst) profile.clone();
			newProfile.setObjectFatherId(profile.getObjectFatherId());
			newProfile.setObjectType(profile.getObjectType());
			newProfile.setInherited(profile.isInherited());
			
			newProfile.addGroups(inheritedProfile.getAllGroups());
			newProfile.addUsers(inheritedProfile.getAllUsers());
			
			return newProfile;
		}
	}
	
	public List getTopicProfiles()
	{
		return getTopicProfiles(getSessionTopic().getNodePK().getId());
	}
	
	public List getTopicProfiles(String topicId)
    {
    	List alShowProfile = new ArrayList();
    	ProfileInst profile = null;

    	//profils dispo
    	String[] asAvailProfileNames = getAdmin().getAllProfilesNames("kmelia");

        for(int nI=0;  nI < asAvailProfileNames.length; nI++)
        {
        	SilverTrace.info("jobStartPagePeas","JobStartPagePeasSessionController.getAllProfilesNames()","root.MSG_GEN_PARAM_VALUE","asAvailProfileNames = "+asAvailProfileNames[nI]);
        	//boolean bFound = false;
        	
        	profile = getTopicProfile(asAvailProfileNames[nI], topicId);
        	/*if (profile != null)
        	{
        		bFound = true;
				profile.setLabel(getAdmin().getProfileLabelfromName("kmelia", asAvailProfileNames[nI]));
            	alShowProfile.add(profile);
        	}
			
            if (!bFound) {
            	profile = new ProfileInst();
            	profile.setName(asAvailProfileNames[nI]);
            	profile.setLabel(getAdmin().getProfileLabelfromName("kmelia", asAvailProfileNames[nI]));
            	alShowProfile.add(profile);
            }*/
        	profile.setLabel(getAdmin().getProfileLabelfromName("kmelia", asAvailProfileNames[nI]));
        	alShowProfile.add(profile);
        }

        return alShowProfile;

    }
	
	public List groupIds2Groups(List groupIds)
	{
		List res = new ArrayList();
		Group theGroup = null;

    	for(int nI=0; groupIds != null && nI < groupIds.size(); nI++)
        {
        	theGroup = getAdmin().getGroupById((String)groupIds.get(nI));
        	if (theGroup != null)
        		res.add(theGroup);
        }

	    return res;
	}
	
	public List userIds2Users(List userIds)
	{
		List res = new ArrayList();
		UserDetail user = null;

    	for(int nI=0; userIds != null && nI < userIds.size(); nI++)
        {
    		user = getUserDetail((String)userIds.get(nI));
        	if (user != null)
        		res.add(user.getDisplayedName());
        }

	    return res;
	}
	
	private AdminController getAdmin()
	{
		if (m_AdminCtrl == null)
			m_AdminCtrl = new AdminController(getUserId());
		
		return m_AdminCtrl;
	}
	
	private ProfileInst getProfile(List profiles, String role)
	{
		ProfileInst profile = null;
		for (int p=0; p<profiles.size(); p++)
		{
			profile = (ProfileInst) profiles.get(p);
			if (role.equals(profile.getName()))
				return profile;
		}
		return null;
	}
	
	public void copyPublication(String pubId) throws RemoteException
	{
		CompletePublication pub = getCompletePublication(pubId);
		PublicationSelection pubSelect = new PublicationSelection(pub);

		SilverTrace.info("kmelia", "KmeliaSessionController.copyPublication()", "root.MSG_GEN_PARAM_VALUE","clipboard = " + getClipboard().getName() + "' count=" + getClipboard().getCount());
		getClipboard().add((ClipboardSelection) pubSelect);
	}
	
	public void copyPublications(String[] pubIds) throws RemoteException
	{
		for (int i = 0; i < pubIds.length; i++) {
			if (StringUtil.isDefined(pubIds[i])) {
				copyPublication(pubIds[i]);
			}
		}
	}
	
	public void cutPublication(String pubId) throws RemoteException
	{
		CompletePublication pub = getCompletePublication(pubId);
		PublicationSelection pubSelect = new PublicationSelection(pub);
		pubSelect.setCutted(true);

		SilverTrace.info("kmelia", "KmeliaSessionController.cutPublication()", "root.MSG_GEN_PARAM_VALUE","clipboard = " + getClipboard().getName() + "' count=" + getClipboard().getCount());
		getClipboard().add((ClipboardSelection) pubSelect);
	}
	
	public void cutPublications(String[] pubIds) throws RemoteException
	{
		for (int i = 0; i < pubIds.length; i++) {
			if (StringUtil.isDefined(pubIds[i])) {
				cutPublication(pubIds[i]);
			}
		}
	}
	
	public void copyTopic(String id) throws RemoteException
	{
		NodeSelection nodeSelect = new NodeSelection(getNodeHeader(id));

		SilverTrace.info("kmelia", "KmeliaSessionController.copyTopic()", "root.MSG_GEN_PARAM_VALUE","clipboard = " + getClipboard().getName() + "' count=" + getClipboard().getCount());
		getClipboard().add((ClipboardSelection) nodeSelect);
	}
	
	public void cutTopic(String id) throws RemoteException
	{
		NodeSelection nodeSelect = new NodeSelection(getNodeHeader(id));
		nodeSelect.setCutted(true);

		SilverTrace.info("kmelia", "KmeliaSessionController.cutTopic()", "root.MSG_GEN_PARAM_VALUE","clipboard = " + getClipboard().getName() + "' count=" + getClipboard().getCount());
		getClipboard().add((ClipboardSelection) nodeSelect);
	}
	
	public void paste() throws RemoteException
	{
		try {
			SilverTrace.info("kmelia","KmeliaRequestRooter.paste()", "root.MSG_GEN_PARAM_VALUE","clipboard = " + getClipboard().getName() + " count=" + getClipboard().getCount());
			Collection clipObjects = getClipboard().getSelectedObjects();
			Iterator clipObjectIterator = clipObjects.iterator();
			while (clipObjectIterator.hasNext()) {
				ClipboardSelection clipObject = (ClipboardSelection) clipObjectIterator.next();
				if (clipObject != null) {
					if (clipObject.isDataFlavorSupported(PublicationSelection.CompletePublicationFlavor)) {
						CompletePublication pub = (CompletePublication) clipObject.getTransferData(PublicationSelection.CompletePublicationFlavor);

						pastePublication(pub, clipObject.isCutted());
					}
					else if (clipObject.isDataFlavorSupported(NodeSelection.NodeDetailFlavor))
					{
						NodeDetail node = (NodeDetail) clipObject.getTransferData(NodeSelection.NodeDetailFlavor);
						
						//check if current topic is a subTopic of node
						boolean pasteAllowed = true;						
						if (getComponentId().equals(node.getNodePK().getInstanceId()))
						{
							if (node.getNodePK().getId().equals(getSessionTopic().getNodePK().getId()))
							{
								pasteAllowed = false;
							}
							
							String nodePath 	= node.getPath()+node.getId()+"/";
							String currentPath 	= getSessionTopic().getNodeDetail().getPath()+getSessionTopic().getNodePK().getId()+"/";
							SilverTrace.info("kmelia","KmeliaRequestRooter.paste()", "root.MSG_GEN_PARAM_VALUE", "nodePath = " + nodePath + ", currentPath = " + currentPath);
							if (pasteAllowed && currentPath.startsWith(nodePath))
							{
								pasteAllowed = false;
							}
						}
						
						if (pasteAllowed)
							pasteNode(node, getSessionTopic().getNodeDetail(), clipObject.isCutted());
						
						//if (clipObject.isCutted())
						//	CallBackManager.invoke(CallBackManager.ACTION_CUTANDPASTE, Integer.parseInt(getUserId()), getComponentId(), node.getNodePK());
					}
				}
			}
		} catch (Exception e) {
			throw new KmeliaRuntimeException("KmeliaSessionController.paste()", SilverpeasRuntimeException.ERROR, "kmelia.EX_PASTE_ERROR", e);
		}
		//TopicDetail topic = getTopic(nodeId);
		getClipboard().PasteDone();
	}
	
	private void pasteNode(NodeDetail nodeToPaste, NodeDetail father, boolean isCutted) throws RemoteException
	{
		NodePK nodeToPastePK = nodeToPaste.getNodePK();
		
		List treeToPaste = getNodeBm().getSubTree(nodeToPastePK);
		
		if (isCutted)
		{
			//move node and subtree
			getNodeBm().moveNode(nodeToPastePK, father.getNodePK());
			
			NodeDetail fromNode = null;
			NodePK toNodePK = null;
			for (int i=0; i<treeToPaste.size(); i++)
			{
				fromNode = (NodeDetail) treeToPaste.get(i);
				if (fromNode != null)
				{
					toNodePK = getNodePK(fromNode.getNodePK().getId());
					
					//remove rights
					deleteTopicRoles(fromNode);
					
					//move wysiwyg
					try {
						AttachmentController.moveAttachments(new ForeignPK("Node_"+fromNode.getNodePK()), new ForeignPK("Node_"+toNodePK.getId(), getComponentId()), true);  //Change instanceId + move files
					} catch (AttachmentException e) {
						SilverTrace.error("kmelia", "KmeliaSessionController.pastePublication()", "root.MSG_GEN_PARAM_VALUE","kmelia.CANT_MOVE_ATTACHMENTS", e);
					}
					
					//change images path in wysiwyg
					try {
						WysiwygController.wysiwygPlaceHaveChanged(fromNode.getNodePK().getInstanceId(), "Node_"+fromNode.getNodePK().getId(), getComponentId(), "Node_"+toNodePK.getId());
					} catch (WysiwygException e) {
						SilverTrace.error("kmelia", "KmeliaSessionController.pastePublication()", "root.MSG_GEN_PARAM_VALUE", e);
					}
					
					//move publications of topics
					pastePublicationsOfTopic(fromNode.getNodePK(), toNodePK, true, null);
				}
			}
		}
		else
		{
			//paste topic
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
			
			//paste wysiwyg attached to node
			WysiwygController.copy(null, nodeToPastePK.getInstanceId(), "Node_"+nodeToPastePK.getId(), null, getComponentId(), "Node_"+nodePK.getId(), getUserId());
			
			List nodeIdsToPaste = new ArrayList();
			NodeDetail oneNodeToPaste = null;
			for (int i=0; i<treeToPaste.size(); i++)
			{
				oneNodeToPaste = (NodeDetail) treeToPaste.get(i);
				if (oneNodeToPaste != null)
					nodeIdsToPaste.add(oneNodeToPaste.getNodePK());
			}
			
			//paste publications of topics
			pastePublicationsOfTopic(nodeToPastePK, nodePK, false, nodeIdsToPaste);
			
			//paste subtopics
			node = getNodeBm().getHeader(nodePK);
			Collection subtopics = getNodeBm().getDetail(nodeToPastePK).getChildrenDetails();
			Iterator itSubTopics = subtopics.iterator();
			NodeDetail subTopic = null;
			while (itSubTopics != null && itSubTopics.hasNext())
			{
				subTopic = (NodeDetail) itSubTopics.next();
				if (subTopic != null)
					pasteNode(subTopic, node, isCutted);
			}
		}
	}
	
	private void pastePublicationsOfTopic(NodePK fromPK, NodePK toPK, boolean isCutted, List nodePKsToPaste) throws RemoteException
	{
		Collection publications = getPublicationBm().getDetailsByFatherPK(fromPK);
		Iterator itPublis = publications.iterator();
		PublicationDetail publi = null;
		CompletePublication completePubli = null;
		while (itPublis.hasNext())
		{
			publi = (PublicationDetail) itPublis.next();
			completePubli = getPublicationBm().getCompletePublication(publi.getPK());
			
			pastePublication(completePubli, isCutted, toPK, nodePKsToPaste);
		}
	}
	
	private void pastePublication(CompletePublication pub, boolean isCutted)
	{
		pastePublication(pub, isCutted, null, null);
	}
	
	private void pastePublication(CompletePublication completePub, boolean isCutted, NodePK nodePK, List nodePKsToPaste)
	{
		try {
			PublicationDetail publi = completePub.getPublicationDetail();
			
			String 			fromId 			= new String(publi.getPK().getId());
			String 			fromComponentId = new String(publi.getPK().getInstanceId());
			
			ForeignPK 		fromForeignPK	= new ForeignPK(publi.getPK().getId(), fromComponentId);		
			PublicationPK	fromPubPK		= new PublicationPK(publi.getPK().getId(), fromComponentId);
			
			ForeignPK 		toForeignPK		= new ForeignPK(publi.getPK().getId(), getComponentId());		
			PublicationPK	toPubPK			= new PublicationPK(publi.getPK().getId(), getComponentId());
			
			
			String imagesSubDirectory = getPublicationSettings().getString("imagesSubDirectory");
			String toAbsolutePath = FileRepositoryManager.getAbsolutePath(getComponentId());
			String fromAbsolutePath = FileRepositoryManager.getAbsolutePath(fromComponentId);
			
			if (isCutted)
			{
				if (nodePK == null)
				{
					//Ajoute au thème courant
					nodePK = getSessionTopic().getNodePK();
				}
					
				if (fromComponentId.equals(getComponentId()))
				{
					//déplacement de la publication dans le même composant
					//seul le père doit être modifié
					
					//TODO : Quid d'une publi avec plusieurs pères ?
					
					//Supprime tous les pères
					getPublicationBm().removeAllFather(publi.getPK());
					
					getPublicationBm().addFather(publi.getPK(), nodePK);
				}
				else
				{
					//déplacement de la publication dans un autre composant
					//- déplacer entête (instanceId)
					//- déplacer vignette
					//- déplacer contenu
					//		- wysiwyg
					//		- wysiwyg (images)
					//		- xml (images et fichiers)
					//		- DB (images)
					//- déplacer fichiers joints
					//- déplacer versioning
					//- déplacer commentaires
					//- deplacer pdc
					
					boolean indexIt = KmeliaHelper.isIndexable(publi);
					
					getPublicationBm().movePublication(publi.getPK(), nodePK, false);  //Change instanceId and unindex header+content
					
					//move Vignette on disk
					String vignette = publi.getImage();
					if (StringUtil.isDefined(vignette)) {						
						String from = fromAbsolutePath + imagesSubDirectory + File.separator + vignette;
						
						try {
							FileRepositoryManager.createAbsolutePath(getComponentId(), imagesSubDirectory);
						} catch (Exception e) {
							SilverTrace.error("kmelia", "KmeliaSessionController.pastePublication()", "root.MSG_GEN_PARAM_VALUE", "kmelia.CANT_MOVE_ATTACHMENTS", e);
						}
						String to = toAbsolutePath + imagesSubDirectory + File.separator + vignette;
						
						File fromVignette = new File(from);
						File toVignette = new File(to);
						
						boolean moveOK = fromVignette.renameTo(toVignette);
						
						SilverTrace.info("kmelia", "KmeliaSessionController.pastePublication()", "root.MSG_GEN_PARAM_VALUE","vignette move = " +moveOK);
					}
					
					//move attachments first (wysiwyg, wysiwyg images, formXML files and images, attachments)
					//TODO : attachments to versioning
					try {
						AttachmentController.moveAttachments(fromForeignPK, toForeignPK, indexIt);  //Change instanceId + move files
					} catch (AttachmentException e) {
						SilverTrace.error("kmelia", "KmeliaSessionController.pastePublication()", "root.MSG_GEN_PARAM_VALUE","kmelia.CANT_MOVE_ATTACHMENTS", e);
					}
					
					try {
						//change images path in wysiwyg
						WysiwygController.wysiwygPlaceHaveChanged(fromComponentId, publi.getPK().getId(), getComponentId(), publi.getPK().getId());
					} catch (WysiwygException e) {
						SilverTrace.error("kmelia", "KmeliaSessionController.pastePublication()", "root.MSG_GEN_PARAM_VALUE",e);
					}
					
					boolean fromCompoVersion = "yes".equals(getOrganizationController().getComponentParameterValue(fromComponentId, "versionControl"));
					
					if (fromCompoVersion && isVersionControlled())
					{
						//move versioning files
						VersioningUtil versioning = new VersioningUtil();
						versioning.moveDocuments(fromForeignPK, toForeignPK, indexIt);
					}
					else if (fromCompoVersion && !isVersionControlled())
					{
						//versioning --> attachments
						//Last public versions becomes the new attachment
						pasteDocumentsAsAttachments(fromPubPK, publi.getPK().getId());
						
						if (indexIt)
							AttachmentController.attachmentIndexer(toForeignPK);
						
						//remove files
						getVersioningBm().deleteDocumentsByForeignPK(fromForeignPK);
					}
					else if (!fromCompoVersion && isVersionControlled())
					{
						//attachments --> versioning
						//paste versioning documents
						
						//Be careful, attachments have already moved !
						pasteAttachmentsAsDocuments(toPubPK, publi.getPK().getId());
						
						if (indexIt)
						{
							VersioningUtil versioning = new VersioningUtil();
							versioning.indexDocumentsByForeignKey(toForeignPK);
						}							
						
						//remove only files
						AttachmentController.deleteAttachmentsByCustomerPKAndContext(toForeignPK, "Images");
					}
					else
					{
						//already made by moveAttachments
					}
					
					//eventually, paste the model content
					if (completePub.getModelDetail() != null && completePub.getInfoDetail() != null) {
						//Move images of model
						if (completePub.getInfoDetail().getInfoImageList() != null) {
							for (Iterator i = completePub.getInfoDetail().getInfoImageList().iterator(); i.hasNext();) {
								InfoImageDetail attachment = (InfoImageDetail)i.next();
								String from = fromAbsolutePath + imagesSubDirectory + File.separator + attachment.getPhysicalName();
								String to = toAbsolutePath + imagesSubDirectory + File.separator + attachment.getPhysicalName();
								
								File fromImage = new File(from);
								File toImage = new File(to);
								
								boolean moveOK = fromImage.renameTo(toImage);
								
								SilverTrace.info("kmelia", "KmeliaSessionController.pastePublication()", "root.MSG_GEN_PARAM_VALUE","dbImage move = " +moveOK);
							}
						}
					}
					else
					{
						String infoId = publi.getInfoId();
						if (infoId != null && !"0".equals(infoId))
						{
							//register content to component
							PublicationTemplateManager.addDynamicPublicationTemplate(getComponentId()+":"+publi.getInfoId(), publi.getInfoId()+".xml");
						}
					}
					
					//move comments
					CommentController.moveComments(fromForeignPK, toForeignPK, indexIt);
									
					//move pdc positions
					int fromSilverObjectId = getKmeliaBm().getSilverObjectId(fromPubPK);
					int toSilverObjectId = getKmeliaBm().getSilverObjectId(toPubPK);
						
					getPdcBm().copyPositions(fromSilverObjectId, fromComponentId, toSilverObjectId, getComponentId());
					getKmeliaBm().deleteSilverContent(fromPubPK);
					
					if (indexIt)
						getPublicationBm().createIndex(toPubPK);
				}
			}
			else
			{
				//paste vignette
				String vignette = publi.getImage();
				if (vignette != null) {						
					String from = fromAbsolutePath + imagesSubDirectory + File.separator + vignette;
					
					String type = vignette.substring(vignette.indexOf(".")+1, vignette.length());
					String newVignette = new Long(new java.util.Date().getTime()).toString() + "." +type;
					
					String to = toAbsolutePath + imagesSubDirectory + File.separator + newVignette;
					FileRepositoryManager.copyFile(from, to);
					
					publi.setImage(newVignette);
				}
	
				//paste the publicationDetail
				publi.setUpdaterId(getUserId()); //ignore initial parameters
				
				String id = null;
				if (nodePK == null)
				{
					id = createPublication(publi);
				}
				else
				{
					id = createPublicationIntoTopic(publi, nodePK.getId());
					
					List fatherPKs = (List) getPublicationBm().getAllFatherPK(publi.getPK());
					if (nodePKsToPaste != null)
					{
						fatherPKs.removeAll(nodePKsToPaste);
					}
				}
				
				//update id cause new publication is created
				toPubPK.setId(id);
					
				//Paste positions on Pdc
				pastePdcPositions(fromPubPK, id);
				
				//paste wysiwyg
				WysiwygController.copy(null, fromComponentId, fromId, null, getComponentId(), id, getUserId());
				
				//paste files
				Hashtable fileIds = pasteFiles(fromPubPK, id);
										
				//eventually, paste the model content
				if (completePub.getModelDetail() != null && completePub.getInfoDetail() != null) {
					//Paste images of model
					if (completePub.getInfoDetail().getInfoImageList() != null) {
						for (Iterator i = completePub.getInfoDetail().getInfoImageList().iterator(); i.hasNext();) {
							InfoImageDetail attachment = (InfoImageDetail)i.next();
							String from = fromAbsolutePath + imagesSubDirectory + File.separator + attachment.getPhysicalName();
							String type = attachment.getPhysicalName().substring(attachment.getPhysicalName().indexOf(".")+1, attachment.getPhysicalName().length());
							String newName = new Long(new java.util.Date().getTime()).toString() + "." +type;
							attachment.setPhysicalName(newName);
							String to = toAbsolutePath + imagesSubDirectory + File.separator + newName;
							FileRepositoryManager.copyFile(from, to);
						}
					}
					
					//Paste model content
					getKmeliaBm().createInfoModelDetail(toPubPK, completePub.getModelDetail().getId(), completePub.getInfoDetail());
				}
				else
				{
					String infoId = publi.getInfoId();
					if (infoId != null && !"0".equals(infoId))
					{
						//Content = XMLForm
						//register xmlForm to publication
						String xmlFormShortName = infoId;
						PublicationTemplateManager.addDynamicPublicationTemplate(getComponentId()+":"+xmlFormShortName, xmlFormShortName+".xml");
						
						//Paste images
						Hashtable imageIds = AttachmentController.copyAttachmentByCustomerPKAndContext(fromPubPK, fromPubPK, "XMLFormImages");
													
						if (imageIds != null)
							fileIds.putAll(imageIds);
						
						//Paste wysiwyg fields content
						WysiwygFCKFieldDisplayer wysiwygField = new WysiwygFCKFieldDisplayer();
						wysiwygField.cloneContents(fromComponentId, fromId, getComponentId(), id);
						
						//get xmlContent to paste
						PublicationTemplate pubTemplateFrom = PublicationTemplateManager.getPublicationTemplate(fromComponentId+":"+xmlFormShortName);
						IdentifiedRecordTemplate recordTemplateFrom = (IdentifiedRecordTemplate) pubTemplateFrom.getRecordSet().getRecordTemplate();
						
						PublicationTemplate pubTemplate = PublicationTemplateManager.getPublicationTemplate(getComponentId()+":"+xmlFormShortName);
						IdentifiedRecordTemplate recordTemplate = (IdentifiedRecordTemplate) pubTemplate.getRecordSet().getRecordTemplate();
						
						//paste xml content
						GenericRecordSetManager.cloneRecord(recordTemplateFrom, fromId, recordTemplate, id, fileIds);
					}
				}
	
				//force the update
				PublicationDetail newPubli = getPublicationDetail(id);
				newPubli.setStatusMustBeChecked(false);
				getKmeliaBm().updatePublication(newPubli);
				
				/*if(isCutted && mustBeDeleted)
	            {
	                CallBackManager.invoke(CallBackManager.ACTION_CUTANDPASTE, Integer.parseInt(getUserId()), getComponentId(), new PublicationPK(fromId, fromComponentId));
	            }*/
			}
		} catch (AttachmentRuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PdcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PublicationTemplateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UtilException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FormException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * get languages of publication header and attachments
	 * @param pubDetail
	 * @return a List of String (language codes)
	 */
	public List getPublicationLanguages()
	{
		List languages = new ArrayList();
		
		PublicationDetail pubDetail = getSessionPubliOrClone().getPublication().getPublicationDetail();
		
		//get publicationdetail languages
		Iterator itLanguages = pubDetail.getLanguages();
		while (itLanguages.hasNext())
		{
			languages.add((String) itLanguages.next());
		}
		
		if (languages.size() == I18NHelper.getNumberOfLanguages())
		{
			//Publication is translated in all supported languages
			return languages;
		}
		else
		{
			//get attachments languages
			List attLanguages = getAttachmentLanguages();
			String language = null;
			for (int l=0; l<attLanguages.size(); l++)
			{
				language = (String) attLanguages.get(l);
				if (!languages.contains(language))
				{
					languages.add(language);
				}
			}
		}
		
		return languages;
	}
	
	public List getAttachmentLanguages()
	{
		PublicationPK pubPK = getSessionPubliOrClone().getPublication().getPublicationDetail().getPK();
		
		//get attachments languages
		List languages = new ArrayList();
		List attLanguages = AttachmentController.getLanguagesOfAttachments(new ForeignPK(pubPK.getId(), pubPK.getInstanceId()));
		String language = null;
		for (int l=0; l<attLanguages.size(); l++)
		{
			language = (String) attLanguages.get(l);
			if (!languages.contains(language))
			{
				languages.add(language);
			}
		}
		return languages;
	}
	
	public void setAliases(List aliases) throws RemoteException
	{
		getKmeliaBm().setAlias(getSessionPublication().getPublication().getPublicationDetail().getPK(), aliases);
	}
	
	public void setAliases(PublicationPK pubPK, List aliases) throws RemoteException
	{
		getKmeliaBm().setAlias(pubPK, aliases);
	}
	
	public List getAliases() throws RemoteException
	{
		List aliases = (List) getKmeliaBm().getAlias(getSessionPublication().getPublication().getPublicationDetail().getPK());
		
		//add user's displayed name
		for (Iterator iterator = aliases.iterator(); iterator.hasNext();) {
			Alias object = (Alias) iterator.next();
			if (StringUtil.isDefined(object.getUserId()))
				object.setUserName(getUserDetail(object.getUserId()).getDisplayedName());
		}
		
		return aliases;
	}
	
	/**
	 * @return a List of Treeview
	 * @throws RemoteException
	 */
	public List getOtherComponents(List aliases) throws RemoteException
	{
		List instanceIds = new ArrayList();
		List result = new ArrayList();
		SpaceInstLight space = null;
		String instanceId = null;
		List tree = null;
		String path = "";
		Treeview treeview = null;
		NodePK root = new NodePK("0");
		
		if (KmeliaHelper.isToolbox(getComponentId()))
		{
			root.setComponentName(getComponentId());
			tree = getKmeliaBm().getTreeview(root, "useless", false, false, getUserId(), false, "yes".equalsIgnoreCase(getOrganizationController().getComponentParameterValue(instanceId, "rightsOnTopics")));
			
			treeview = new Treeview(getComponentLabel(), tree, getComponentId());
			
			treeview.setNbAliases(getNbAliasesInComponent(aliases, instanceId));
			
			result.add(treeview);
		}
		else
		{
			List spaces = getOrganizationController().getSpaceTreeview(getUserId());
			for (int s=0; s<spaces.size(); s++)
			{
				space = (SpaceInstLight) spaces.get(s);
				path = "";
				String[] componentIds = getOrganizationController().getAvailCompoIdsAtRoot(space.getFullId(), getUserId());
				for (int k=0; k<componentIds.length; k++)
				{
					instanceId = componentIds[k];
					
					if (instanceId.startsWith("kmelia"))
					{
						String[] profiles = getOrganizationController().getUserProfiles(getUserId(), instanceId);
						String bestProfile = KmeliaHelper.getProfile(profiles);
						if (bestProfile.equalsIgnoreCase("admin") || bestProfile.equalsIgnoreCase("publisher"))
						{
							instanceIds.add(instanceId);
							root.setComponentName(instanceId);
							
							if (instanceId.equals(getComponentId()))
							{
								tree = getKmeliaBm().getTreeview(root, "useless", false, false, getUserId(), false, "yes".equalsIgnoreCase(getOrganizationController().getComponentParameterValue(instanceId, "rightsOnTopics")));
							}
							
							if (!StringUtil.isDefined(path))
							{
								List sPath = getOrganizationController().getSpacePath(space.getFullId());
								SpaceInst spaceInPath = null;
								for (int i=0; i<sPath.size(); i++)
								{
									spaceInPath = (SpaceInst) sPath.get(i);
									if (i>0)
										path += " > ";
									path += spaceInPath.getName();
								}
							}
							
							treeview = new Treeview(path+" > "+getOrganizationController().getComponentInstLight(instanceId).getLabel(), tree, instanceId);
						
							treeview.setNbAliases(getNbAliasesInComponent(aliases, instanceId));
							
							if (instanceId.equals(getComponentId()))
								result.add(0, treeview);
							else
								result.add(treeview);
						}
					}
				}
			}
		}
		return result;
	}
	
	public List getAliasTreeview() throws RemoteException
	{
		return getAliasTreeview(getComponentId());
	}
	
	public List getAliasTreeview(String instanceId) throws RemoteException
	{
		String[] profiles = getOrganizationController().getUserProfiles(getUserId(), instanceId);
		String bestProfile = KmeliaHelper.getProfile(profiles);
		List tree = null;
		if (bestProfile.equalsIgnoreCase("admin") || bestProfile.equalsIgnoreCase("publisher"))
		{
			NodePK root = new NodePK("0", instanceId);
			
			tree = getKmeliaBm().getTreeview(root, "useless", false, false, getUserId(), false, "yes".equalsIgnoreCase(getOrganizationController().getComponentParameterValue(instanceId, "rightsOnTopics")));
		}
		return tree;
	}
	
	private int getNbAliasesInComponent(List aliases, String instanceId)
	{
		Alias alias = null;
		int nb = 0;
		for (int a=0; a<aliases.size(); a++)
		{
			alias = (Alias) aliases.get(a);
			if (alias.getInstanceId().equals(instanceId))
				nb++;
		}
		return nb;
	}
	
	private boolean isToolbox()
	{
		return KmeliaHelper.isToolbox(getComponentId());
	}
	
	public String getFirstAttachmentURLOfCurrentPublication() throws RemoteException
	{
		PublicationPK pubPK = getSessionPublication().getPublication().getPublicationDetail().getPK();
		String url = null;
		if (isVersionControlled())
		{
			VersioningUtil versioning = new VersioningUtil();
			List documents = versioning.getDocuments(new ForeignPK(pubPK));
			if (documents.size() != 0)
			{
				Document document = (Document) documents.get(0);
				DocumentVersion documentVersion = versioning.getLastPublicVersion(document.getPk());
				if (documentVersion != null)
					url = versioning.getDocumentVersionURL(document.getInstanceId(), documentVersion.getLogicalName(), document.getPk().getId(), documentVersion.getPk().getId());
			}
		}
		else
		{
			Vector attachments = AttachmentController.searchAttachmentByPKAndContext(pubPK, "Images");
			if (attachments.size() != 0)
			{
				AttachmentDetail attachment = (AttachmentDetail) attachments.get(0);
				url = attachment.getAttachmentURL();
			}
		}
		return url;
	}
	
	public boolean useUpdateChain()
	{
		return "yes".equals(getComponentParameterValue("updateChain"));
	}
	
	public void setFieldUpdateChain(Fields fields)
	{
		this.saveFields = fields;
	}
	
	public Fields getFieldUpdateChain()
	{
		return saveFields;
	}
	
	public void initUpdateChainTopicChoice(String pubId) 
	{
		Collection path;
		try {
			String[] topics = null;
			if (saveFields.getTree() != null)
			{
				// initialisation du premier thème coché
				FieldParameter param = saveFields.getTree().getParams().get(0);
				if (!param.getName().equals("none"))
				{
					String id = param.getValue();
					topics = new String[1];
					NodePK node = getNodeHeader(id).getNodePK();
					topics[0] = id + "," + node.getComponentName();
				}
			}
			else
			{
				path = getPublicationFathers(pubId);
				topics = new String[path.size()];
				Iterator it = path.iterator();
				int i = 0;
				while (it.hasNext())
				{
					NodePK node = (NodePK) it.next();
					topics[i] = node.getId() + "," + node.getComponentName();
					i++;
				}
				
			}
			getFieldUpdateChain().setTopics(topics);
		}
		catch (RemoteException e) {
			getFieldUpdateChain().setTopics(null);
		}
	}
	
	public boolean isTopicHaveUpdateChainDescriptor() 
	{
		boolean haveDescriptor = false;
		// regarder si ce fichier existe
		if (useUpdateChain())
		{
			File descriptorFile = new File(getUpdateChainDescriptorFilename(getSessionTopic().getNodePK().getId()));
			if (descriptorFile.exists())
				haveDescriptor = true;
		}
		return haveDescriptor;
	}
	
	private String getUpdateChainDescriptorFilename(String topicId)
	{
		return getSettings().getString("updateChainRepository") + getComponentId() + "_" + topicId + ".xml";
	}
	
	public synchronized List getSubTopics(String rootId) throws RemoteException
    {
   		return getNodeBm().getSubTree(getNodePK(rootId));
    }
	
	public List<NodeDetail> getUpdateChainTopics() throws RemoteException
	{
		List<NodeDetail> topics = new ArrayList<NodeDetail>();
	    if (getFieldUpdateChain().getTree() != null)
	    {
	    	FieldParameter param = getFieldUpdateChain().getTree().getParams().get(0);
			if (param.getName().equals("rootId"))
			{
				topics = getSubTopics(param.getValue());
			}
			if (param.getName().equals("targetId"))
			{
				topics.add(getNodeHeader(param.getValue()));
			}
	    }
	    else
	    {
	    	topics = getAllTopics();
	    }
	    return topics;
	}
	
	public void initUpdateChainDescriptor() throws IOException, ClassNotFoundException, ParserConfigurationException
	{
		XStream xstream = new XStream(new DomDriver());
		xstream.alias("fieldDescriptor", FieldUpdateChainDescriptor.class);
		xstream.alias("updateChain", UpdateChainDescriptor.class);
		xstream.alias("parameter", FieldParameter.class);
		
		File descriptorFile = new File(getUpdateChainDescriptorFilename(getSessionTopic().getNodePK().getId()));
		UpdateChainDescriptor updateChainDescriptor = (UpdateChainDescriptor) xstream.fromXML(new FileReader(descriptorFile));
		
		String title = updateChainDescriptor.getTitle();
		String libelle = updateChainDescriptor.getLibelle();
		saveFields.setTitle(title);
		saveFields.setLibelle(libelle);
		
		List<FieldUpdateChainDescriptor> fields = updateChainDescriptor.getFields();
		Iterator it = fields.iterator();
		while (it.hasNext())
		{
			FieldUpdateChainDescriptor field = (FieldUpdateChainDescriptor) it.next();
			
			saveFields.setHelper(updateChainDescriptor.getHelper());
			
			if (field.getName().equals("Name"))
			{
				saveFields.setName(field);
			}
			else if (field.getName().equals("Description"))
			{
				saveFields.setDescription(field);
			}
			else if (field.getName().equals("Keywords"))
			{
				saveFields.setKeywords(field);
			}
			else if (field.getName().equals("Topics"))
			{
				saveFields.setTree(field);
			}
		}
		
	}

	public String getXmlFormForFiles()
	{
		return getComponentParameterValue("XmlFormForFiles");
	}
	
	public List<String> exportPublication()
	{
		List<String> result = new ArrayList<String>();
		PublicationPK pubPK = getSessionPublication().getPublication().getPublicationDetail().getPK();
		
		try {
			//get PDF
			String pdf = generatePdf(pubPK.getId());
			String pdfWithoutExtension = pdf.substring(0, pdf.indexOf(".pdf"));
			
			//create subdir to zip
			String subdir 		= "ExportPubli_"+new Date().getTime();
			//String subDirPath 	= FileRepositoryManager.getTemporaryPath()+subdir;
			//FileRepositoryManager.createGlobalTempPath(subdir);
			String subDirPath 	= FileRepositoryManager.getTemporaryPath()+subdir+File.separator+pdfWithoutExtension;
			FileFolderManager.createFolder(subDirPath);
						
			//copy pdf into zip
			String filePath = FileRepositoryManager.getTemporaryPath("useless", getComponentId()) + pdf;
			FileRepositoryManager.copyFile(filePath, subDirPath+File.separator+pdf);
			
			//copy files
			new AttachmentImportExport().getAttachments(pubPK, subDirPath, "useless", null);
			new VersioningImportExport().exportDocuments(pubPK, subDirPath, "useless", null);
			
			String zipFileName = pdfWithoutExtension+".zip";
			
			//zip PDF and files
			long zipSize = ZipManager.compressPathToZip(subDirPath, FileRepositoryManager.getTemporaryPath()+zipFileName);
			
			result.add(zipFileName);
			result.add(String.valueOf(zipSize));
			result.add(FileServerUtils.getUrlToTempDir(zipFileName, zipFileName, "application/zip"));
		} catch (Exception e) {
			throw new KmeliaRuntimeException("KmeliaSessionController.exportPublication()", SilverpeasRuntimeException.ERROR, "kmelia.CANT_EXPORT_PUBLICATION", e);
		}
		
		return result;
	}

	public boolean isNotificationAllowed()
	{
		String parameterValue = getComponentParameterValue("notifications");
		if (!StringUtil.isDefined(parameterValue))	{
			return true;
		} else {
			return "yes".equals(parameterValue.toLowerCase());
		}
	}
	
	public boolean isWysiwygOnTopicsEnabled()
	{
		return "yes".equals(getComponentParameterValue("wysiwygOnTopics").toLowerCase());
	}
	
	public String getWysiwygOnTopic()
	{
		if (isWysiwygOnTopicsEnabled())
		{
			try {
				return WysiwygController.load(getComponentId(), "Node_"+getSessionTopic().getNodePK().getId(), getLanguage());
			} catch (WysiwygException e) {
				return "";
			}
		}
		return "";
	}
	
}