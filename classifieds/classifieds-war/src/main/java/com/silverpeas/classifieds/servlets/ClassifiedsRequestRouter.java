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
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.classifieds.servlets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.model.Category;
import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.classifieds.model.Subscribe;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.Form;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.form.XmlSearchForm;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.util.indexEngine.model.FieldDescription;

public class ClassifiedsRequestRouter extends ComponentRequestRouter {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "classifieds";
  }

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new ClassifiedsSessionController(mainSessionCtrl, componentContext);
  }

  // recherche du profile de l'utilisateur
  public String getFlag(String[] profiles) {
    String flag = "publisher";
    for (int i = 0; i < profiles.length; i++) {
      if (profiles[i].equals("admin")) {
        return profiles[i];
      }
    }
    return flag;
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param componentSC The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function, ComponentSessionController componentSC,
      HttpServletRequest request) {
    String destination = "";
    String rootDest = "/classifieds/jsp/";

    ClassifiedsSessionController classifiedsSC = (ClassifiedsSessionController) componentSC;
    SilverTrace.info("classifieds", "classifiedsRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + componentSC.getUserId() + " Function=" + function);

    // création des paramètres généraux
    String flag = getFlag(classifiedsSC.getUserRoles());
    String userId = classifiedsSC.getUserId();

    request.setAttribute("Profile", flag);
    request.setAttribute("UserId", userId);
    request.setAttribute("InstanceId", classifiedsSC.getComponentId());
    request.setAttribute("Language", classifiedsSC.getLanguage());

    SilverTrace.debug("classifieds", "classifiedsRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Profile=" + flag);

    try {
      if (function.startsWith("Main")) {
        String xmlFormName = classifiedsSC.getXMLFormName();
        String xmlFormShortName = null;
        Form formUpdate = null;
        DataRecord data = null;
        Collection<Category> categories = null;
        if (StringUtil.isDefined(xmlFormName)) {
          xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName
              .indexOf("."));
          PublicationTemplateImpl pubTemplate =
              (PublicationTemplateImpl) PublicationTemplateManager
                  .getPublicationTemplate(classifiedsSC.getComponentId() + ":" + xmlFormShortName,
                      xmlFormName);
          String templateFileName = pubTemplate.getFileName();
          String templateName = templateFileName.substring(0, templateFileName.lastIndexOf("."));
          formUpdate = pubTemplate.getSearchForm();
          RecordSet recordSet = pubTemplate.getRecordSet();
          data = recordSet.getEmptyRecord();
          // récupération des valeurs de la première liste déroulante pour affichage en page
          // d'accueil
          String field = classifiedsSC.getSearchFields1();
          String keys = (String) pubTemplate.getRecordTemplate().getFieldTemplate(field)
              .getParameters(classifiedsSC.getLanguage()).get("keys");
          String values = (String) pubTemplate.getRecordTemplate().getFieldTemplate(field)
              .getParameters(classifiedsSC.getLanguage()).get("values");
          String label = pubTemplate.getRecordTemplate().getFieldTemplate(field).getFieldName();
          categories = createCategory(templateName, label, keys, values, classifiedsSC);
        }
        request.setAttribute("Form", formUpdate);
        request.setAttribute("Data", data);
        request.setAttribute("Categories", categories);
        request.setAttribute("NbTotal", (classifiedsSC.getNbTotalClassifieds()));
        destination = rootDest + "accueil.jsp";
      } else if (function.equals("ViewClassifiedToValidate")) {
        // récupérer les petites annonces à valider
        Collection<ClassifiedDetail> classifieds = classifiedsSC.getClassifiedsToValidate();
        request.setAttribute("Classifieds", classifieds);
        request.setAttribute("TitlePath", "classifieds.viewClassifiedToValidate");
        destination = rootDest + "classifieds.jsp";
      } else if (function.equals("ViewMyClassifieds")) {
        // récupérer les petites annonces de l'utilisateur
        Collection<ClassifiedDetail> classifieds = classifiedsSC.getClassifiedsByUser();
        request.setAttribute("Classifieds", classifieds);
        request.setAttribute("TitlePath", "classifieds.myClassifieds");
        destination = rootDest + "classifieds.jsp";
      } else if (function.equals("SearchClassifieds")) {
        List<FileItem> items = getRequestItems(request);
        QueryDescription query = new QueryDescription();
        String xmlFormName = classifiedsSC.getXMLFormName();
        DataRecord data = null;
        String xmlFormShortName = null;
        XmlSearchForm searchForm = null;
        if (StringUtil.isDefined(xmlFormName)) {
          xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName
              .indexOf("."));
          PublicationTemplateImpl template = (PublicationTemplateImpl) PublicationTemplateManager
              .getPublicationTemplate(classifiedsSC.getComponentId() + ":" + xmlFormShortName);
          String templateFileName = template.getFileName();
          String templateName = templateFileName.substring(0, templateFileName.lastIndexOf("."));
          RecordTemplate searchTemplate = template.getSearchTemplate();
          data = searchTemplate.getEmptyRecord();
          PagesContext context = new PagesContext("XMLSearchForm", "2",
              classifiedsSC.getLanguage(), classifiedsSC.getUserId());
          searchForm = (XmlSearchForm) template.getSearchForm();
          searchForm.update(items, data, context);

          String[] fieldNames = searchTemplate.getFieldNames();
          String fieldValue = "";
          String fieldName = "";
          String fieldQuery = "";
          Field field = null;
          for (int f = 0; f < fieldNames.length; f++) {
            fieldName = fieldNames[f];
            field = data.getField(fieldName);
            fieldValue = field.getStringValue();
            if (fieldValue != null && fieldValue.trim().length() > 0) {
              fieldQuery = fieldValue.trim().replaceAll("##", " AND "); // case � cocher multiple
              query.addFieldQuery(new FieldDescription(templateName + "$$" + fieldName, fieldQuery,
                  null));
            }
          }
        }
        // Lancement de la recherche
        Collection<ClassifiedDetail> classifieds;
        try {
          classifieds = classifiedsSC.search(query);
        } catch (Exception e) {
          classifieds = new ArrayList<ClassifiedDetail>();
        }
        request.setAttribute("Form", searchForm);
        request.setAttribute("Data", data);
        request.setAttribute("NbTotal", (classifiedsSC.getNbTotalClassifieds()));
        request.setAttribute("Classifieds", classifieds);

        destination = rootDest + "classifiedsResult.jsp";
      } else if (function.equals("ViewClassified")) {
        request.setAttribute("IsDraftEnabled", classifiedsSC.isDraftEnabled());
        String classifiedId = request.getParameter("ClassifiedId");
        if (!StringUtil.isDefined(classifiedId)) {
          classifiedId = (String) request.getAttribute("ClassifiedId");
        }
        request.setAttribute("Classified", classifiedsSC.getClassified(classifiedId));
        Form formView = null;
        DataRecord data = null;
        String xmlFormName = classifiedsSC.getXMLFormName();
        if (StringUtil.isDefined(xmlFormName)) {
          String xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName
              .indexOf("."));
          PublicationTemplateImpl pubTemplate =
              (PublicationTemplateImpl) PublicationTemplateManager
                  .getPublicationTemplate(classifiedsSC.getComponentId() + ":" + xmlFormShortName,
                      xmlFormName);
          if (pubTemplate != null) {
            formView = pubTemplate.getViewForm();
            RecordSet recordSet = pubTemplate.getRecordSet();
            data = recordSet.getRecord(classifiedId);
            if (data != null) {
              request.setAttribute("Form", formView);
              request.setAttribute("Data", data);
            }
          }
        }
        // les commentaires sur cette annonce
        request.setAttribute("AllComments", classifiedsSC.getAllComments(classifiedId));
        destination = rootDest + "viewClassified.jsp";
      } else if (function.equals("NewClassified")) {
        // récupération des paramètres
        String fieldKey = request.getParameter("FieldKey");
        // passage des paramètres
        request.setAttribute("Classified", null);
        request.setAttribute("UserName", classifiedsSC.getUserDetail(userId).getDisplayedName());
        request.setAttribute("UserEmail", classifiedsSC.getUserDetail(userId).geteMail());
        String xmlFormName = classifiedsSC.getXMLFormName();
        String xmlFormShortName = null;
        Form formUpdate = null;
        DataRecord data = null;
        if (StringUtil.isDefined(xmlFormName)) {
          xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName
              .indexOf("."));
          PublicationTemplateImpl pubTemplate =
              (PublicationTemplateImpl) PublicationTemplateManager
                  .getPublicationTemplate(classifiedsSC.getComponentId() + ":" + xmlFormShortName,
                      xmlFormName);
          formUpdate = pubTemplate.getUpdateForm();
          RecordSet recordSet = pubTemplate.getRecordSet();
          data = recordSet.getEmptyRecord();
        }
        request.setAttribute("Form", formUpdate);
        request.setAttribute("Data", data);
        request.setAttribute("FieldKey", fieldKey);
        request.setAttribute("FieldName", classifiedsSC.getSearchFields1());
        destination = rootDest + "classifiedManager.jsp";
      } else if (function.equals("CreateClassified")) {
        List<FileItem> items = getRequestItems(request);
        String title = getParameterValue(items, "Title");
        ClassifiedDetail classified = new ClassifiedDetail(title);
        String classifiedId = classifiedsSC.createClassified(classified, flag);
        String xmlFormName = classifiedsSC.getXMLFormName();
        if (StringUtil.isDefined(xmlFormName)) {
          String xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName
              .indexOf("."));

          PublicationTemplate pub = PublicationTemplateManager.getPublicationTemplate(classifiedsSC
              .getComponentId()
              + ":" + xmlFormShortName);
          RecordSet set = pub.getRecordSet();
          Form form = pub.getUpdateForm();
          DataRecord data = set.getRecord(classifiedId);
          if (data == null) {
            data = set.getEmptyRecord();
            data.setId(classifiedId);
          }

          PagesContext context = new PagesContext("myForm", "0", classifiedsSC.getLanguage(),
              false, classifiedsSC.getComponentId(), classifiedsSC.getUserId());
          context.setObjectId(classifiedId);

          // mise à jour des données saisies
          form.update(items, data, context);
          set.save(data);
        }
        classifiedsSC.updateClassified(classified, false, false);
        destination = getDestination("ViewMyClassifieds", classifiedsSC, request);
      } else if (function.equals("EditClassified")) {
        String classifiedId = request.getParameter("ClassifiedId");
        ClassifiedDetail classified = classifiedsSC.getClassified(classifiedId);
        // passage des paramètres
        request.setAttribute("Classified", classified);
        request.setAttribute("UserName", classifiedsSC.getUserDetail(userId).getDisplayedName());
        request.setAttribute("UserEmail", classifiedsSC.getUserDetail(userId).geteMail());
        Form formView = null;
        DataRecord data = null;
        String xmlFormName = classifiedsSC.getXMLFormName();
        if (StringUtil.isDefined(xmlFormName)) {
          String xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName
              .indexOf("."));
          PublicationTemplateImpl pubTemplate =
              (PublicationTemplateImpl) PublicationTemplateManager
                  .getPublicationTemplate(classifiedsSC.getComponentId() + ":" + xmlFormShortName,
                      xmlFormName);
          if (pubTemplate != null) {
            formView = pubTemplate.getUpdateForm();
            RecordSet recordSet = pubTemplate.getRecordSet();
            data = recordSet.getRecord(classifiedId);
            if (data != null) {
              request.setAttribute("Form", formView);
              request.setAttribute("Data", data);
            }
          }
        }
        destination = rootDest + "classifiedManager.jsp";
      } else if (function.equals("UpdateClassified")) {
        List<FileItem> items = getRequestItems(request);
        String title = getParameterValue(items, "Title");
        String classifiedId = getParameterValue(items, "ClassifiedId");
        ClassifiedDetail classified = classifiedsSC.getClassified(classifiedId);
        classified.setTitle(title);

        String xmlFormName = classifiedsSC.getXMLFormName();
        if (StringUtil.isDefined(xmlFormName)) {
          String xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName
              .indexOf("."));

          PublicationTemplate pub = PublicationTemplateManager.getPublicationTemplate(classifiedsSC
              .getComponentId()
              + ":" + xmlFormShortName);
          RecordSet set = pub.getRecordSet();
          Form form = pub.getUpdateForm();
          DataRecord data = set.getRecord(classifiedId);
          if (data == null) {
            data = set.getEmptyRecord();
            data.setId(classifiedId);
          }

          PagesContext context = new PagesContext("myForm", "0", classifiedsSC.getLanguage(),
              false, classifiedsSC.getComponentId(), classifiedsSC.getUserId());
          context.setObjectId(classifiedId);

          // mise à jour des données saisies
          form.update(items, data, context);
          set.save(data);
        }
        classifiedsSC.updateClassified(classified, true, flag.equals("admin"));

        request.setAttribute("ClassifiedId", classifiedId);
        destination = getDestination("ViewMyClassifieds", classifiedsSC, request);
      } else if (function.equals("DeleteClassified")) {
        String classifiedId = request.getParameter("ClassifiedId");
        classifiedsSC.deleteClassified(classifiedId);
        destination = getDestination("ViewMyClassifieds", classifiedsSC, request);
      } else if (function.equals("AddComment")) {
        // récupération des paramètres
        String message = request.getParameter("Message");
        String classifiedId = (String) request.getParameter("ClassifiedId");
        // ajout du commentaire
        classifiedsSC.addComment(classifiedId, message);
        // retour à la page de visualisation de la petite annonce
        destination = getDestination("ViewClassified", classifiedsSC, request);
      } else if (function.equals("DeleteComment")) {
        String id = request.getParameter("CommentId");
        classifiedsSC.deleteComment(id);
        destination = getDestination("ViewClassified", componentSC, request);
      } else if (function.equals("UpdateComment")) {
        String classifiedId = request.getParameter("ClassifiedId");
        request.setAttribute("ClassifiedId", classifiedId);
        destination = getDestination("ViewClassified", componentSC, request);
      } else if (function.equals("searchResult")) {
        // traitement des recherches
        String id = request.getParameter("Id");
        String type = request.getParameter("Type");
        try {
          if (type.equals("Classified")) {
            // traitement des petites annonces
            request.setAttribute("ClassifiedId", id);
            destination = getDestination("ViewClassified", classifiedsSC, request);
          } else if (type.startsWith("Comment")) {
            // traitement des commentaires
            request.setAttribute("ClassifiedId", id);
            destination = getDestination("Comments", classifiedsSC, request);
          } else {
            destination = getDestination("Main", classifiedsSC, request);
          }
        } catch (Exception e) {
          request.setAttribute("ComponentId", classifiedsSC.getComponentId());
          return "/admin/jsp/documentNotFound.jsp";
        }
      } else if (function.equals("DraftIn")) {
        String classifiedId = request.getParameter("ClassifiedId");
        classifiedsSC.draftInClassified(classifiedId);
        destination = getDestination("ViewClassified", classifiedsSC, request);
      } else if (function.equals("DraftOut")) {
        String classifiedId = request.getParameter("ClassifiedId");
        classifiedsSC.draftOutClassified(classifiedId, flag);
        destination = getDestination("ViewClassified", classifiedsSC, request);
      } else if (function.equals("ValidateClassified")) {
        String classifiedId = request.getParameter("ClassifiedId");
        classifiedsSC.validateClassified(classifiedId);
        destination = getDestination("ViewClassified", classifiedsSC, request);
      } else if (function.equals("WantToRefuseClassified")) {
        String classifiedId = request.getParameter("ClassifiedId");
        ClassifiedDetail classified = classifiedsSC.getClassified(classifiedId);
        request.setAttribute("ClassifiedToRefuse", classified);
        destination = rootDest + "refusalMotive.jsp";
      } else if (function.equals("RefusedClassified")) {
        String motive = request.getParameter("Motive");
        String classifiedId = request.getParameter("ClassifiedId");
        classifiedsSC.refusedClassified(classifiedId, motive);
        destination = getDestination("ViewClassified", classifiedsSC, request);
      } else if (function.equals("NewSubscription")) {
        String xmlFormName = classifiedsSC.getXMLFormName();
        String xmlFormShortName = null;
        Form formUpdate = null;
        DataRecord data = null;
        if (StringUtil.isDefined(xmlFormName)) {
          xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName
              .indexOf("."));
          PublicationTemplateImpl pubTemplate =
              (PublicationTemplateImpl) PublicationTemplateManager
                  .getPublicationTemplate(classifiedsSC.getComponentId() + ":" + xmlFormShortName,
                      xmlFormName);
          formUpdate = pubTemplate.getSearchForm();
          RecordSet recordSet = pubTemplate.getRecordSet();
          data = recordSet.getEmptyRecord();
        }
        request.setAttribute("Form", formUpdate);
        request.setAttribute("Data", data);
        destination = rootDest + "subscriptionManager.jsp";
      } else if (function.equals("AddSubscription")) {
        List<FileItem> items = getRequestItems(request);
        String field1 = getParameterValue(items, classifiedsSC.getSearchFields1());
        String field2 = getParameterValue(items, classifiedsSC.getSearchFields2());
        if (StringUtil.isDefined(field1) && StringUtil.isDefined(field2)) {
          Subscribe subscribe = new Subscribe(field1, field2);
          classifiedsSC.createSubscribe(subscribe);
        }
        destination = getDestination("ViewMySubscriptions", classifiedsSC, request);
      } else if (function.equals("ViewMySubscriptions")) {
        Collection<Subscribe> subscribes = classifiedsSC.getSubscribesByUser();
        request.setAttribute("Subscribes", subscribes);
        destination = rootDest + "subscriptions.jsp";
      } else if (function.equals("DeleteSubscription")) {
        String subscribeId = request.getParameter("SubscribeId");
        classifiedsSC.deleteSubscribe(subscribeId);
        destination = getDestination("ViewMySubscriptions", classifiedsSC, request);
      } else if (function.equals("ViewAllClassifiedsByCategory")) {
        String fieldKey = request.getParameter("FieldKey");
        String categoryName = request.getParameter("CategoryName");
        String xmlFormName = classifiedsSC.getXMLFormName();
        Collection<ClassifiedDetail> classifieds = new ArrayList<ClassifiedDetail>();
        String xmlFormShortName = null;
        if (StringUtil.isDefined(xmlFormName)) {
          xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName
              .indexOf("."));
          PublicationTemplateImpl pubTemplate =
              (PublicationTemplateImpl) PublicationTemplateManager
                  .getPublicationTemplate(classifiedsSC.getComponentId() + ":" + xmlFormShortName,
                      xmlFormName);
          String templateFileName = pubTemplate.getFileName();
          String templateName = templateFileName.substring(0, templateFileName.lastIndexOf("."));
          String field = classifiedsSC.getSearchFields1();
          String label = pubTemplate.getRecordTemplate().getFieldTemplate(field).getFieldName();
          // Ajout des résultats de la recherche dans la catégorie
          QueryDescription query = new QueryDescription();
          query.addFieldQuery(new FieldDescription(templateName + "$$" + label, fieldKey, null));
          String values = (String) pubTemplate.getRecordTemplate().getFieldTemplate(field)
          .getParameters(classifiedsSC.getLanguage()).get("values");
          try {
            classifieds = classifiedsSC.search(query);
          } catch (Exception e) {
            classifieds = new ArrayList<ClassifiedDetail>();
          }
        } else {
          classifieds = new ArrayList<ClassifiedDetail>();
        }
        request.setAttribute("Classifieds", classifieds);
        request.setAttribute("TitlePath", "classifieds.viewByCategorie");
        request.setAttribute("Extra", categoryName);
        destination = rootDest + "classifieds.jsp";

      } else {
        destination = rootDest + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace.info("classifieds", "classifiedsRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }

   private List<FileItem> getRequestItems(HttpServletRequest request) throws FileUploadException {
    DiskFileItemFactory factory = new DiskFileItemFactory();
    ServletFileUpload upload = new ServletFileUpload(factory);
    List<FileItem> items = upload.parseRequest(request);
    return items;
  }

  private String getParameterValue(List<FileItem> items, String parameterName) {
    Iterator<FileItem> iter = items.iterator();
    while (iter.hasNext()) {
      FileItem item = (FileItem) iter.next();
      if (item.isFormField() && parameterName.equals(item.getFieldName()))
        return item.getString();
    }
    return null;
  }

  private Collection<Category> createCategory(String templateName, String label, String stringKeys,
      String stringValues, ClassifiedsSessionController classifiedsSC) {
    Collection<Category> categories = new ArrayList<Category>();
    String[] keys = stringKeys.split("##");
    String[] values = stringValues.split("##");
    for (int i = 0; i < keys.length; i++) {
      Category category = new Category(keys[i], values[i]);
      // Ajout des résultats de la recherche dans la catégorie
      QueryDescription query = new QueryDescription();
      query.addFieldQuery(new FieldDescription(templateName + "$$" + label, keys[i], null));
      Collection<ClassifiedDetail> classifieds = new ArrayList<ClassifiedDetail>();
      try {
        classifieds = classifiedsSC.search(query);
      } catch (Exception e) {
        classifieds = new ArrayList<ClassifiedDetail>();
      }
      category.setClassifieds(classifieds);
      categories.add(category);
    }
    return categories;
  }

}
