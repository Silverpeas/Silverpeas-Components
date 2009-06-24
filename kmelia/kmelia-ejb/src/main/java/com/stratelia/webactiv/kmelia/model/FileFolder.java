package com.stratelia.webactiv.kmelia.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class FileFolder extends Object implements java.io.Serializable
{

    /**
     * A File collection representing all items in folder
     */
    private ArrayList children;

    /**
     * A File collection representing files in folder
     */
    private ArrayList files;

    /**
     * A File collection representing folders in folder
     */
    private ArrayList folders;

    /**
     * folder name
     */
    private String    name;

    /**
     * folder path
     */
    private String    path;


    /**
     * Constructor declaration
     *
     *
     * @param path
     *
     * @see
     */
    public FileFolder(String path)
    {
    	this.path = path;
       	files = new ArrayList(0);
       	folders = new ArrayList(0);
       	children = new ArrayList(0);
       	
        try
        {
        	SilverTrace.debug("kmelia","FileFolder.FileFolder()","root.MSG_GEN_PARAM_VALUE","Starting constructor for FileFolder. Path = "+path);
        	File f = new File(path);
        	File fChild;

	        SilverTrace.debug("kmelia","FileFolder.FileFolder()","root.MSG_GEN_PARAM_VALUE","isExists " + f.exists() +" isFile="+ f.isFile());
        	if (f.exists())
        	{
            	this.name = f.getName();
            	String[] children_name = f.list();

            	for (int i = 0; children_name != null && i < children_name.length; i++)
            	{
                	SilverTrace.debug("kmelia","FileFolder.FileFolder()","root.MSG_GEN_PARAM_VALUE","Name = "+children_name[i]);
                	fChild = new File(path + File.separator + children_name[i]);
                	children.add(new FileDetail(fChild.getName(), fChild.getPath(), fChild.length(), fChild.isDirectory()));
                	if (fChild.isDirectory())
                	{
	                    folders.add(new FileDetail(fChild.getName(), fChild.getPath(), fChild.length(), fChild.isDirectory()));
                	}
                	else
                	{
	                    files.add(new FileDetail(fChild.getName(), fChild.getPath(), fChild.length(), fChild.isDirectory()));
                	}
            	}
        	}
        }
        catch (Exception e)
        {
        	throw new KmeliaRuntimeException("FileFolder.FileFolder()", SilverpeasRuntimeException.ERROR, "kmelia.IMPOSSIBLE_DACCEDER_AU_REPERTOIRE", e);	
        }
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public Collection getFiles()
    {
        return files;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public Collection getFolders()
    {
        return folders;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getName()
    {
        return name;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public ArrayList getChildren()
    {
        return children;
    }




}
