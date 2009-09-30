package com.silverpeas.kmelia.updatechainhelpers;

import com.stratelia.webactiv.util.publication.model.PublicationDetail;

public class DefineUnpromptedCandidacy extends UpdateChainHelperImpl {

  public void execute(UpdateChainHelperContext uchc) {
    // récupération des données
    PublicationDetail pubDetail = uchc.getPubDetail();

    // concaténation de "description" et "mot clé"
    String newDescription = pubDetail.getDescription() + " \n"
        + pubDetail.getKeywords();
    pubDetail.setDescription(newDescription);
    pubDetail.setKeywords("");
    uchc.setPubDetail(pubDetail);
  }

}
