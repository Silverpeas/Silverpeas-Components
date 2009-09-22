package com.silverpeas.kmelia.updatechainhelpers;

import java.util.List;

import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.kmelia.model.updatechain.FieldUpdateChainDescriptor;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

public class UpdateChainHelperContext 
{
 
	private PublicationDetail pubDetail;
	private String[] topics;
	private List<NodeDetail> allTopics;
	private FieldUpdateChainDescriptor descriptor;
	private KmeliaSessionController kmeliaScc;

	public KmeliaSessionController getKmeliaScc() {
		return kmeliaScc;
	}

	public UpdateChainHelperContext() {
		
	}
	public UpdateChainHelperContext(PublicationDetail pubDetail, KmeliaSessionController kmeliaScc) {
		this.pubDetail = pubDetail;
		this.kmeliaScc = kmeliaScc;
	}
	
	public UpdateChainHelperContext(PublicationDetail pubDetail) {
		this.pubDetail = pubDetail;
	}

	public PublicationDetail getPubDetail() {
		return pubDetail;
	}

	public void setPubDetail(PublicationDetail pubDetail) {
		this.pubDetail = pubDetail;
	}

	public String[] getTopics() {
		return topics;
	}

	public void setTopics(String[] topics) {
		this.topics = topics;
	}

	public FieldUpdateChainDescriptor getDescriptor() {
		return descriptor;
	}

	public void setDescriptor(FieldUpdateChainDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	public List<NodeDetail> getAllTopics() {
		return allTopics;
	}

	public void setAllTopics(List<NodeDetail> allTopics) {
		this.allTopics = allTopics;
	}
	
}