package com.stratelia.webactiv.kmelia.model;

import java.util.List;

import com.stratelia.webactiv.util.publication.model.CompletePublication;

/**
 * This object contains elements which are displayed in a kmelia Topic
 * 
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class FullPublication extends Object implements java.io.Serializable {

  private List attachments;
  private CompletePublication publication;
  private List pdcPositions;

  public FullPublication() {
    init(null, null, null);
  }

  public FullPublication(CompletePublication publication, List attachments,
      List pdcPositions) {
    init(publication, attachments, pdcPositions);
  }

  private void init(CompletePublication publication, List attachments,
      List pdcPositions) {
    this.attachments = attachments;
    this.publication = publication;
    this.pdcPositions = pdcPositions;
  }

  public List getAttachments() {
    return this.attachments;
  }

  public CompletePublication getPublication() {
    return this.publication;
  }

  public void setAttachments(List attachments) {
    this.attachments = attachments;
  }

  public void setPublication(CompletePublication pub) {
    this.publication = pub;
  }

  public List getPdcPositions() {
    return pdcPositions;
  }

  public void setPdcPositions(List pdcPositions) {
    this.pdcPositions = pdcPositions;
  }
}