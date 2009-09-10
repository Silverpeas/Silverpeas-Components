/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.silverpeas.gallery.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;

import com.silverpeas.gallery.ImageHelper;
import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.control.ejb.GalleryBmHome;
import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;
import com.silverpeas.util.ZipManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class GalleryDragAndDrop extends HttpServlet
{
	HttpSession session;

	PrintWriter out;

	public void init(ServletConfig config)
	{
		try
		{
			super.init(config);
		}
		catch (ServletException se)
		{
			SilverTrace.fatal("importExportPeas", "ImportDragAndDrop.init", "peasUtil.CANNOT_ACCESS_SUPERCLASS");
		}
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		doPost(req, res);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse res) throws ServletException, IOException
	{
		SilverTrace.info("gallery", "GalleryDragAndDrop.doPost", "root.MSG_GEN_ENTER_METHOD");

		ResourceLocator settings = new ResourceLocator("com.stratelia.webactiv.util.attachment.Attachment", "");
		boolean runOnUnix = settings.getBoolean("runOnSolaris", false);
		SilverTrace.info("gallery", "GalleryDragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE", "runOnUnix = " + runOnUnix);

		try
		{
			String componentId = request.getParameter("ComponentId");
			String albumId = request.getParameter("AlbumId");
			String userId = request.getParameter("UserId");
			String ignoreFolders = request.getParameter("IgnoreFolders");

			SilverTrace.info("gallery", "GalleryDragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE", "componentId = " + componentId + " albumId = " + albumId + " userId = " + userId + " ignoreFolders = " + ignoreFolders);

			DiskFileUpload dfu = new DiskFileUpload();
			// maximum size that will be stored in memory
			dfu.setSizeThreshold(4096);

			String savePath = FileRepositoryManager.getTemporaryPath() + File.separator + userId + new Long(new Date().getTime()).toString() + File.separator;

			List items = dfu.parseRequest(request);

			String parentPath = getParameterValue(items, "userfile_parent");
			SilverTrace.info("gallery", "GalleryDragAndDrop.doPost.doPost", "root.MSG_GEN_PARAM_VALUE", "parentPath = " + parentPath);

			String fullFileName = null;
			SilverTrace.info("gallery", "GalleryDragAndDrop.doPost.doPost", "root.MSG_GEN_PARAM_VALUE", "debut de la boucle");

			for (int i = 0; i < items.size(); i++)
			{
				FileItem item = (FileItem) items.get(i);
				fullFileName = item.getName();
				SilverTrace.info("gallery", "GalleryDragAndDrop.doPost.doPost", "root.MSG_GEN_PARAM_VALUE", "item #" + i + " = " + item.getFieldName() + " - " + fullFileName);

				String fileName = null;

				if (fullFileName != null && parentPath != null && !parentPath.equals(""))
				{
					SilverTrace.info("gallery", "GalleryDragAndDrop.doPost.doPost", "root.MSG_GEN_PARAM_VALUE", "item.getName().indexOf(parentPath)" + fullFileName.indexOf(parentPath) + 1);
					fileName = fullFileName.substring(fullFileName.indexOf(parentPath) + parentPath.length());
					SilverTrace.info("gallery", "GalleryDragAndDrop.doPost.doPost", "root.MSG_GEN_PARAM_VALUE", "fileName = " + fileName);
					if (fileName != null && runOnUnix)
					{
						fileName = fileName.replace('\\', File.separatorChar);
						SilverTrace.info("gallery", "GalleryDragAndDrop.doPost.doPost", "root.MSG_GEN_PARAM_VALUE", "fileName on Unix = " + fileName);
					}

					// Création du fichier (et de l'arborescence) sur le serveur
					if (!savePath.equals(""))
					{
						// modifier le nom avant de l'écrire
						String extension 	= FileRepositoryManager.getFileExtension(fileName);
						String name 		= fileName.substring(1, fileName.lastIndexOf("."));
						//String newName 		= ImageHelper.replaceSpecialChars(name);
						String newFileName 	= File.separator.concat(name).concat(".").concat(extension);
						File f = new File(savePath + newFileName);
						File parent = f.getParentFile();
						if (!parent.exists())
						{
							parent.mkdirs();
						}
						
						item.write(f);

						// Cas du zip
						if ("zip".equalsIgnoreCase(extension))
						{
							ZipManager.extract(f, parent);
						}
					}
				}
			}

			importRepository(new File(savePath), userId, componentId, albumId, new ResourceLocator("com.silverpeas.gallery.settings.gallerySettings", "fr"), new ResourceLocator("com.silverpeas.gallery.settings.metadataSettings", "fr"));

			FileFolderManager.deleteFolder(savePath);
		}
		catch (Exception e)
		{
			SilverTrace.debug("gallery", "GalleryDragAndDrop.doPost.doPost", "root.MSG_GEN_PARAM_VALUE", e);
		}
	}

	private void importRepository(File dir, String userId, String componentId, String albumId, ResourceLocator settings, ResourceLocator metadataSettings) throws Exception
	{
		OrganizationController orga = new OrganizationController();
		boolean watermark = "yes".equalsIgnoreCase(orga.getComponentParameterValue(componentId, "watermark"));
		boolean download = true;
		if ("no".equalsIgnoreCase(orga.getComponentParameterValue(componentId, "download")))
				download = false;
		String watermarkHD = orga.getComponentParameterValue(componentId, "WatermarkHD");
		String watermarkOther = orga.getComponentParameterValue(componentId, "WatermarkOther");
		
		importRepository(dir, userId, componentId, albumId, watermark, watermarkHD, watermarkOther, download, settings, metadataSettings);
	}

	private void importRepository(File dir, String userId, String componentId, String albumId, boolean watermark, 
			String watermarkHD, String watermarkOther, boolean download, ResourceLocator settings, ResourceLocator metadataSettings) throws Exception
	{		
		Iterator itPathContent = getPathContent(dir);
		while (itPathContent.hasNext())
		{
			File file = (File) itPathContent.next();
			if (file.isFile())
			{
				if (ImageHelper.isImage(file.getName()))
					try
					{
						createPhoto(file.getName(), userId, componentId, albumId, file, watermark, watermarkHD, watermarkOther, download, metadataSettings);
					}
					catch (Exception e)
					{
						SilverTrace.info("gallery", "GalleryDragAndDrop.importRepository", "gallery.MSG_NOT_ADD_METADATA", "photo =  " + file.getName());
					}
			}
			else if (file.isDirectory())
			{
				String newAlbumId = createAlbum(file.getName(), userId, componentId, albumId);

				// Traitement récursif spécifique
				importRepository(file.getAbsoluteFile(), userId, componentId, newAlbumId, watermark, watermarkHD, watermarkOther, download, settings, metadataSettings);
			}
		}
	}

	private Iterator getPathContent(File path)
	{
		// Récupération du contenu du dossier
		List listFile = new ArrayList();

		String[] listFileName = path.list();
		for (int i = 0; i < listFileName.length; i++)
		{
			listFile.add(new File(path + File.separator + listFileName[i]));
		}

		return listFile.iterator();
	}

	private String createAlbum(String name, String userId, String componentId, String fatherId) throws Exception
	{
		SilverTrace.info("gallery", "GalleryDragAndDrop.createAlbum", "root.MSG_GEN_ENTER_METHOD", "name = " + name + ", fatherId = " + fatherId);

		// création de l'album (avec le nom du répertoire) une seule fois
		AlbumDetail album = null;

		NodeDetail node = new NodeDetail("unknown", name, null, null, null, null, "0", "unknown");
		album = new AlbumDetail(node);
		album.setCreationDate(DateUtil.date2SQLDate(new Date()));
		album.setCreatorId(userId);
		album.getNodePK().setComponentName(componentId);
		NodePK nodePK = new NodePK(fatherId, componentId);
		NodePK newNodePK = getGalleryBm().createAlbum(album, nodePK);
		String newAlbumId = newNodePK.getId();

		return newAlbumId;
	}

	private String createPhoto(String name, String userId, String componentId, String albumId, File file, boolean watermark, String watermarkHD, String watermarkOther, 
			boolean download, ResourceLocator settings) throws Exception
	{
		SilverTrace.info("gallery", "GalleryDragAndDrop.createPhoto", "root.MSG_GEN_ENTER_METHOD", "name = " + name + ", fatherId = " + albumId);

		// création de la photo
		PhotoDetail newPhoto = new PhotoDetail(name, null, new Date(), null, null, null, download, false);

		newPhoto.setAlbumId(albumId);
		newPhoto.setCreatorId(userId);
		PhotoPK pk = new PhotoPK("unknown", componentId);
		newPhoto.setPhotoPK(pk);

		String photoId = getGalleryBm().createPhoto(newPhoto, albumId);
		newPhoto.getPhotoPK().setId(photoId);

		// Création de la preview et des vignettes sur disque
		ImageHelper.processImage(newPhoto, file, watermark, watermarkHD, watermarkOther);
		try
		{
			ImageHelper.setMetaData(newPhoto, settings);
		}
		catch (Exception e)
		{
			SilverTrace.info("gallery", "GalleryDragAndDrop.createPhoto", "gallery.MSG_NOT_ADD_METADATA", "photoId =  " + photoId);
		}

		// Modification de la photo pour mise à jour dimension
		getGalleryBm().updatePhoto(newPhoto);

		return photoId;
	}

	private String getParameterValue(List items, String parameterName)
	{
		Iterator iter = items.iterator();
		while (iter.hasNext())
		{
			FileItem item = (FileItem) iter.next();
			if (item.isFormField() && parameterName.equals(item.getFieldName()))
			{
				return item.getString();
			}
		}
		return null;
	}

	private GalleryBm getGalleryBm()
	{
		GalleryBm galleryBm = null;
		try
		{
			GalleryBmHome galleryBmHome = (GalleryBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.GALLERYBM_EJBHOME, GalleryBmHome.class);
			galleryBm = galleryBmHome.create();
		}
		catch (Exception e)
		{
			throw new GalleryRuntimeException("GallerySessionController.getGalleryBm()", SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
		}
		return galleryBm;
	}
}