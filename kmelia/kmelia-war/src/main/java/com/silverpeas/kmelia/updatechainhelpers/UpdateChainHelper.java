package com.silverpeas.kmelia.updatechainhelpers;

import com.stratelia.webactiv.util.publication.model.PublicationDetail;

public interface UpdateChainHelper {

	public void execute(UpdateChainHelperContext uchc);
	public PublicationDetail setPublicationDetail();
}
