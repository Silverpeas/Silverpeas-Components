package com.stratelia.webactiv.webSites.siteManage.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import org.apache.tools.ant.Project;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.webSites.control.WebSitesException;

/**
 * Unzip a file. 
 *
 * @author costin@dnt.ro
 * @author <a href="mailto:stefan.bodewig@megabit.net">Stefan Bodewig</a>
 */

public class Expand  {
    private File dest; // req
    private File source; // req
    
    /**
     * Do the work.
     *
     * @exception IOException Thrown in unrecoverable error.
     */
    // XXX move it to util or tools
    public void execute()  throws WebSitesException {
        if (source == null) {  
            throw new WebSitesException("Expand.execute()",
		                                SilverpeasException.ERROR,
		                                "webSites.EXE_SOURCE_FILE_ATTRIBUTE_MUST_BE_SPECIFIED");
        }
        if (dest == null) {  
            throw new WebSitesException("Expand.execute()",
		                                SilverpeasException.ERROR,
		                                "webSites.EXE_DESTINATION_FILE_ATTRIBUTE_MUST_BE_SPECIFIED");
        }
        expandFile(source, dest);
    }

    /**
    * ExpandFile
    *
    */
    private void expandFile(File srcF, File dir) {
      ZipFile zf = null;
	try {
	    
       zf = new ZipFile(srcF, "Cp437");
	    ZipEntry ze = null;
	    
       Enumeration entries = zf.getEntries();
       while (entries.hasMoreElements()) {
       ze = (ZipEntry) entries.nextElement();
		File f = new File(dir, Project.translatePath(ze.getName()));
		try {
		    // create intermediary directories - sometimes zip don't add them
		    File dirF=new File(f.getParent());
		    dirF.mkdirs();
		    
		    if (ze.isDirectory()) {
			f.mkdirs(); 
		    } else {
			byte[] buffer = new byte[1024];
			int length = 0;
         InputStream zis = zf.getInputStream(ze);
			FileOutputStream fos = new FileOutputStream(f);
			
			while ((length = zis.read(buffer)) >= 0) {
			    fos.write(buffer, 0, length);
			}
			
			fos.close();
		    }


		} catch( FileNotFoundException ex ) {
			 SilverTrace.warn("webSites", "Expand.expandFile()", "root.EX_FILE_NOT_FOUND", "file = "+f.getPath(), ex);
		}
	    }
	} catch (IOException ioe) {
	     SilverTrace.warn("webSites", "Expand.expandFile()", "webSites.EXE_ERROR_WHILE_EXPANDING_FILE", "sourceFile = "+srcF.getPath(), ioe);
	} finally {
       if (zf != null) {
	        try {
            zf.close();
	        }
	        catch (IOException e) {
			   SilverTrace.warn("webSites", "Expand.expandFile()", "webSites.EXE_ERROR_WHILE_CLOSING_ZIPINPUTSTREAM", null, e);
            }
	    }
	}
    }

    /**
     * Set the destination directory. File will be unzipped into the
     * destination directory.
     *
     * @param d Path to the directory.
     */
    public void setDest(File d) {
	this.dest=d;
    }

    /**
     * Set the path to zip-file.
     *
     * @param s Path to zip-file.
     */
    public void setSrc(File s) {
	this.source = s;
    }
}
