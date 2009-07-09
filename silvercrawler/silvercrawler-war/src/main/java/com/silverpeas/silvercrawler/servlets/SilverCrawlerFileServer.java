/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.silverpeas.silvercrawler.servlets;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.silverpeas.silvercrawler.statistic.Statistic;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class SilverCrawlerFileServer extends HttpServlet
{

    //HttpSession session;
    //PrintWriter out;

    public static String getUrl(String logicalName, String physicalName, String mimeType, String userId, String componentId)
    {
        return FileServerUtils.getUrl(logicalName, physicalName, mimeType, userId, componentId);
    }


    public void init(ServletConfig config)
    {
        try
        {
            super.init(config);
        }
        catch (ServletException se)
        {
            SilverTrace.fatal("silverCrawler", "FileServer.init()", "peasUtil.CANNOT_ACCESS_SUPERCLASS");
        }
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        doPost(req, res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        SilverTrace.info("silverCrawler", "FileServer.doPost()", "root.MSG_GEN_ENTER_METHOD");
        String mimeType 	= req.getParameter("MimeType");
        String sourceFile 	= req.getParameter("SourceFile");
        String userId 		= req.getParameter("UserId");
        String componentId 	= req.getParameter("ComponentId");
        String typeUpload 	= req.getParameter("TypeUpload");
        String path 		= req.getParameter("Path");
        
        String filePath = null;

        // paramètres des stats
        String type = "";
        String fileStat = "";
        
        HttpSession session = req.getSession(true);
        MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute("SilverSessionController");
        if (mainSessionCtrl == null || !isUserAllowed(mainSessionCtrl, componentId))
        {
            SilverTrace.warn("silverCrawler", "FileServer.doPost()", "root.MSG_GEN_SESSION_TIMEOUT", "NewSessionId=" + session.getId() + GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL")+GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout"));
            res.sendRedirect(GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL")+GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout"));
        }
        
        String rootPath 	= mainSessionCtrl.getOrganizationController().getComponentParameterValue(componentId, "directory");
        String separator 	= rootPath.substring(0,1);
		if (rootPath.endsWith(separator))
			rootPath = rootPath.substring(rootPath.length()-1, rootPath.length());
        
        //2 cas :
        // - téléchargement d'un zip dans répertoire temporaire
        // - téléchargement d'un fichier depuis le répertoire crawlé
        if ("link".equals(typeUpload))
        {
            filePath = rootPath+separator+sourceFile;
            
            type = Statistic.FILE;
            fileStat = filePath;
            SilverTrace.debug("silverCrawler", "FileServer", "root.MSG_GEN_PARAM_VALUE", "file, type = " + type + " file = " + fileStat);
        }
        else
        {
        	filePath = FileRepositoryManager.getTemporaryPath(null, componentId) + sourceFile;
                
            type = Statistic.DIRECTORY;
            fileStat = rootPath+separator+path;
            SilverTrace.debug("silverCrawler", "FileServer", "root.MSG_GEN_PARAM_VALUE", "directory, type = " + type + " file = " + fileStat);
        }

        res.setContentType(mimeType);

       	outputFile(res, filePath);
        
        //ajout dans la table des téléchargements
        Statistic.addStat(userId, fileStat, componentId, type) ;
        SilverTrace.info("silverCrawler", "FileServer.doPost()", "root.MSG_GEN_ENTER_METHOD", " addStat : fileStat = " + fileStat);
    }

    private boolean isUserAllowed(MainSessionController controller, String componentId) {
        return controller.getOrganizationController().isComponentAvailable(componentId, controller.getUserId());
    }

    private void outputFile(HttpServletResponse res, String filePath) throws IOException
    {
        OutputStream        out2 = res.getOutputStream();
        int                 read;
        BufferedInputStream input = null; // for the html document generated
        SilverTrace.info("silverCrawler", "FileServer.displayHtmlCode()", "root.MSG_GEN_ENTER_METHOD", "filePath = "+filePath);
        try
        {
                input = new BufferedInputStream(new FileInputStream(filePath));
                read = input.read();
				SilverTrace.info("silverCrawler", "FileServer.displayHtmlCode()", "root.MSG_GEN_ENTER_METHOD", " BufferedInputStream read "+read);
                if (read == -1){
                	displayWarningHtmlCode(res);
                } else {
					while (read != -1)
					{
						out2.write(read); // writes bytes into the response
						read = input.read();
					}
				}
        }
        catch (Exception e)
        {
            SilverTrace.warn("silverCrawler", "FileServer.doPost", "root.EX_CANT_READ_FILE", "file name=" + filePath);
            displayWarningHtmlCode(res);
        }
        finally
        {
            SilverTrace.info("silverCrawler", "FileServer.displayHtmlCode()", "", " finally ");
            // we must close the in and out streams
            try
            {
                if (input != null)
                {
                    input.close();
                }
                out2.close();
            }
            catch (Exception e)
            {
                SilverTrace.warn("silverCrawler", "FileServer.displayHtmlCode", "root.EX_CANT_READ_FILE", "close failed");
            }
        }
    }

	
    private void displayWarningHtmlCode(HttpServletResponse res) throws IOException{
        StringReader        sr = null; 
        OutputStream        out2 = res.getOutputStream();
        int                 read;
		ResourceLocator resourceLocator = new ResourceLocator("com.stratelia.webactiv.util.peasUtil.multiLang.fileServerBundle", "");

		sr = new StringReader(resourceLocator.getString("warning"));
		try{
			read = sr.read();
			while (read != -1){
				SilverTrace.info("silverCrawler", "FilServer.displayHtmlCode()", "root.MSG_GEN_ENTER_METHOD", " StringReader read "+read);
				out2.write(read); // writes bytes into the response
				read = sr.read();
			}
		} catch (Exception e){
            SilverTrace.warn("silverCrawler", "FileServer.displayWarningHtmlCode", "root.EX_CANT_READ_FILE", "warning properties");
		} finally {
			try{
                if (sr != null)
                    sr.close();
                out2.close();
			} catch (Exception e){
                SilverTrace.warn("silverCrawler", "FileServer.displayHtmlCode", "root.EX_CANT_READ_FILE", "close failed");
			}
		}
	}
}
