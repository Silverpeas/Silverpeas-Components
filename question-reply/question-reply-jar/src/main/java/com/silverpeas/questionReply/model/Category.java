package com.silverpeas.questionReply.model;

import com.stratelia.webactiv.util.node.model.NodeDetail;

public class Category extends NodeDetail {
  public Category(NodeDetail node) {
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

}
