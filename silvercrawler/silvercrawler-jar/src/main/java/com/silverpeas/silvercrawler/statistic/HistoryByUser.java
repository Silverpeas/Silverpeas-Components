/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.silverpeas.silvercrawler.statistic;

import java.io.Serializable;
import java.util.Date;

import com.stratelia.webactiv.beans.admin.UserDetail;

/**
 * Class declaration
 *
 *
 * @author
 */
public class HistoryByUser implements Serializable
{
	private UserDetail  user;
    private Date        lastDownload;
    private int			nbDownload;	

    public HistoryByUser(UserDetail user, Date lastDownload, int nbDownload)
    {
        this.lastDownload = lastDownload;
        this.user = user;
        this.nbDownload = nbDownload;
    }

    public Date getLastDownload()
    {
        return lastDownload;
    }

    public UserDetail getUser()
    {
        return user;
    }

     public int getNbDownload()
    {
        return nbDownload;
    }

	public void setLastDownload(Date lastDownload)
	{
		this.lastDownload = lastDownload;
	}

	public void setNbDownload(int nbDownload)
	{
		this.nbDownload = nbDownload;
	}

	public void setUser(UserDetail user)
	{
		this.user = user;
	}

}
