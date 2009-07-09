package com.silverpeas.silvercrawler.statistic;

import java.io.Serializable;
import java.util.Date;

 
/**
 * Class declaration
 *
 *
 * @author
 */
public class HistoryDetail implements Serializable
{

    private Date        date;
    private String      userId;
    private String		path;

    /**
     * Constructor declaration
     *
     *
     * @param date
     * @param userId
     * @param foreignPK
     *
     * @see
     */
    public HistoryDetail(Date date, String userId, String path)
    {
        this.date = date;
        this.userId = userId;
        this.path = path;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public Date getDate()
    {
        return date;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getUserId()
    {
        return userId;
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

}
