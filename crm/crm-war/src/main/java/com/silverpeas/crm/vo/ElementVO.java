package com.silverpeas.crm.vo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Vector;

import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.a;
import org.apache.ecs.xhtml.img;

import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;

public abstract class ElementVO {

  protected ResourcesWrapper resources;
  private DateFormat dateFormat = null;
  private DateFormat displayDateFormat = null;
  
  public ElementVO(ResourcesWrapper resources) {
    this.resources = resources;
    dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    displayDateFormat = new SimpleDateFormat(resources.getString("GML.dateFormat"));
  }
  
  protected String getAttachments(Vector<AttachmentDetail> attachments) {
    ElementContainer container = new ElementContainer();
    String context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
    for (AttachmentDetail attachment : attachments) {
      container.addElement(getImageLink(context + attachment.getAttachmentURL(), "CRMWindow",
        resources.getIcon("crm.attachedFiles"), attachment.getLogicalName()));
      container.addElement("&nbsp;");
    }
    return container.toString();
  }
  
  protected String getOperationLinks(String type, String name, String id) {
    ElementContainer container = new ElementContainer();
    container.addElement(getImageLink(
      "javascript:edit" + type + "('" + id + "')", null, resources.getIcon("crm.update"),
      resources.getString("crm.update") + " '" + EncodeHelper.javaStringToHtmlString(name) + "'"));
    container.addElement("&nbsp;&nbsp;&nbsp;");
    container.addElement(getImageLink(
      "javascript:delete" + type + "('" + id + "')", null, resources.getIcon("crm.delete"),
      resources.getString("crm.delete") + " '" + EncodeHelper.javaStringToHtmlString(name) + "'"));
    return container.toString();
  }
  
  protected String getActive(String active) {
    String key = (active.equals("1") ? "crm.actif" : "crm.nonActif");
    img image = new img();
    image.setSrc(resources.getIcon(key));
    image.setAlt(key);
    return image.toString();
  }
  
  protected String getDate(String date) {
    if (StringUtil.isDefined(date)) {
      try {
        return displayDateFormat.format(dateFormat.parse(date));
      } catch (ParseException e) {
        // do nothing
      }
    }
    return "";
  }
  
  private a getImageLink(String href, String target, String src, String alt) {
    a link = new a();
    link.setHref(href);
    if (StringUtil.isDefined(target)) {
      link.setTarget(target);
    }
    img image = new img();
    image.setSrc(src);
    image.setAlt(alt);
    image.setTitle(alt);
    link.addElement(image);
    return link;
  }
  
}
