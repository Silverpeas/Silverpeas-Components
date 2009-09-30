package com.silverpeas.gallery.model;

import java.util.Collection;

import com.stratelia.webactiv.util.node.model.NodeDetail;

public class AlbumDetail extends NodeDetail {
  private Collection photos;
  private String permalink = null;

  public AlbumDetail(NodeDetail node) {
    setNodePK(node.getNodePK());
    setName(node.getName());
    setDescription(node.getDescription());
    setCreationDate(node.getCreationDate());
    setCreatorId(node.getCreatorId());
    setPath(node.getPath());
    setLevel(node.getLevel());
    setStatus(node.getStatus());
    setFatherPK(node.getFatherPK());
    setChildrenDetails(node.getChildrenDetails());
    setType(node.getType());
    setOrder(node.getOrder());
  }

  public Collection getPhotos() {
    return photos;
  }

  public void setPhotos(Collection photos) {
    this.photos = photos;
  }

  public String getPermalink() {
    return permalink;
  }

  public void setPermalink(String permalink) {
    this.permalink = permalink;
  }
}
