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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.delegatednews.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.publication.model.PublicationRuntimeException;

@Entity
@Table(name = "sc_delegatednews_news")
public class DelegatedNews implements java.io.Serializable {
	
	private static final long serialVersionUID = 9192830552642027995L;

	@Id
	private int pubId;
	@Column(name = "instanceId")
	private String instanceId;
	@Column(name = "status")
	private String status;
	@Column(name = "contributorId")
	private String contributorId;
	@Column(name = "validatorId")
	private String validatorId;
	@Column(name = "validationDate", columnDefinition = "TIMESTAMP")
  private Date validationDate;
	@Column(name = "beginDate", columnDefinition = "TIMESTAMP")
	private Date beginDate;
	@Column(name = "endDate", columnDefinition = "TIMESTAMP")
	private Date endDate;
	@Column(name = "newsOrder")
  private int newsOrder = 0;
	
	public static final String NEWS_TO_VALIDATE = "ToValidate";
	public static final String NEWS_VALID = "Valid";
	public static final String NEWS_REFUSED = "Refused";
	  
	public DelegatedNews() {
    
	}

	public DelegatedNews(int pubId, String instanceId, 
			String contributorId, Date validationDate, Date beginDate, Date endDate) {
		super();
		this.pubId = pubId;
		this.instanceId = instanceId;
		this.status = NEWS_TO_VALIDATE;
		this.contributorId = contributorId;
		if(validationDate != null) {
		  this.validationDate = new Date(validationDate.getTime());
		}
		if(beginDate != null) {
		  this.beginDate = new Date(beginDate.getTime());
		}
		if(endDate != null) {
		  this.endDate = new Date(endDate.getTime());
		}
	}

	public int getPubId() {
		return pubId;
	}

	public void setPubId(int pubId) {
		this.pubId = pubId;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getContributorId() {
		return contributorId;
	}

	public void setContributorId(String contributorId) {
		this.contributorId = contributorId;
	}

	public String getValidatorId() {
		return validatorId;
	}

	public void setValidatorId(String validatorId) {
		this.validatorId = validatorId;
	}
	
	public Date getValidationDate() {
    return validationDate;
  }

  public void setValidationDate(Date validationDate) {
    this.validationDate = validationDate;
  }

	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public int getNewsOrder() {
    return newsOrder;
  }

  public void setNewsOrder(int newsOrder) {
    this.newsOrder = newsOrder;
  }
  
  public boolean isValidated() {
    return DelegatedNews.NEWS_VALID.equals(getStatus());
  }
  
  public boolean isDenied() {
    return DelegatedNews.NEWS_REFUSED.equals(getStatus());
  }
  
  public boolean isWaitingForValidation() {
    return DelegatedNews.NEWS_TO_VALIDATE.equals(getStatus());
  }
	
	public PublicationDetail getPublicationDetail() {
	  try {
      PublicationBm publicationBm =EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
              PublicationBm.class);
      PublicationPK pubPk = new PublicationPK(Integer.toString(this.pubId), this.instanceId);
      return publicationBm.getDetail(pubPk);
    } catch (Exception e) {
      throw new PublicationRuntimeException("DelegatedNews.getPublicationDetail()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", "pubId = "+this.pubId, e);
    }
  }

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
	    if (getClass() != obj.getClass()) {
	      return false;
	    }
	    final DelegatedNews other = (DelegatedNews) obj;
	    if (this.pubId != other.pubId) {
	        return false;
	      }
	    if ((this.instanceId == null) ? (other.instanceId != null) : !this.instanceId.equals(other.instanceId)) {
	    	return false;
	    }
	    if ((this.status == null) ? (other.status != null) : !this.status.equals(other.status)) {
	    	return false;
	    }
	    if ((this.contributorId == null) ? (other.contributorId != null) : !this.contributorId.equals(other.contributorId)) {
	    	return false;
	    }
	    if ((this.validatorId == null) ? (other.validatorId != null) : !this.validatorId.equals(other.validatorId)) {
	    	return false;
	    }
	    if ((this.validationDate == null) ? (other.validationDate != null) : !this.validationDate.equals(other.validationDate)) {
        return false;
      }
	    if ((this.beginDate == null) ? (other.beginDate != null) : !this.beginDate.equals(other.beginDate)) {
	    	return false;
	    }
	    if ((this.endDate == null) ? (other.endDate != null) : !this.endDate.equals(other.endDate)) {
	    	return false;
	    }
	    if ((this.newsOrder == -1) ? (other.newsOrder != -1) : this.newsOrder != other.newsOrder) {
        return false;
      }
	    return true;
	  }


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + pubId;
		result = prime * result
        + ((instanceId == null) ? 0 : instanceId.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result
        + ((contributorId == null) ? 0 : contributorId.hashCode());
		result = prime * result
        + ((validatorId == null) ? 0 : validatorId.hashCode());
		result = prime * result
        + ((validationDate == null) ? 0 : validationDate.hashCode());
		result = prime * result
				+ ((beginDate == null) ? 0 : beginDate.hashCode());
		result = prime * result 
		    + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result + newsOrder;
		return result;
	}


	  @Override
	  public String toString() {
		  return "DelegatedNews {" + "pubId=" + pubId + ", instanceId=" + instanceId + ", status="
	        + status + ", contributorId=" + contributorId + ", validatorId="
	        + validatorId + ", validationDate=" + validationDate + ", beginDate=" + beginDate +
	        ", endDate=" + endDate + ", newsOrder=" + newsOrder +'}';
  }

}
