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

import com.silverpeas.look.LookHelper;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;

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
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.indexEngine.model.FieldDescription;

public class ClassifiedsRequestRouter
    extends ComponentRequestRouter {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
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
  @Override
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
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
  @Override
  public String getDestination(String function,
      ComponentSessionController componentSC,
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
        Collection<Category> categories = null;
        Form formUpdate = null;
        DataRecord data = null;
        PublicationTemplate pubTemplate = getPublicationTemplate(classifiedsSC);
        if (pubTemplate != null) {
          String templateFileName = pubTemplate.getFileName();
          String templateName = templateFileName.substring(0, templateFileName.lastIndexOf("."));
          String field = classifiedsSC.getSearchFields1();
          String keys = pubTemplate.getRecordTemplate().getFieldTemplate(field).
              getParameters(classifiedsSC.getLanguage()).get("keys");
          String values = pubTemplate.getRecordTemplate().getFieldTemplate(field).
              getParameters(classifiedsSC.getLanguage()).get("values");
          String label = pubTemplate.getRecordTemplate().getFieldTemplate(field).getFieldName();
          categories = createCategory(templateName, label, keys, values, classifiedsSC);
          formUpdate = pubTemplate.getSearchForm();
          RecordSet recordSet = pubTemplate.getRecordSet();
          data = recordSet.getEmptyRecord();
          request.setAttribute("Categories", categories);
        }
        request.setAttribute("Form", formUpdate);
        request.setAttribute("Data", data);
        request.setAttribute("NbTotal", classifiedsSC.getNbTotalClassifieds());
        request.setAttribute("Validation", classifiedsSC.isValidationEnabled());
        request.setAttribute("AnonymousAccess", isAnonymousAccess(request));
        destination = rootDest + "accueil.jsp";
      } else if (function.equals("ViewClassifiedToValidate")) {
        // récupérer les petites annonces à valider
        Collection<ClassifiedDetail> classifieds = classifiedsSC.getClassifiedsToValidate();
        request.setAttribute("Classifieds", classifieds);
        request.setAttribute("TitlePath", "classifieds.viewClassifiedToValidate");
        request.setAttribute("AnonymousAccess", isAnonymousAccess(request));
        destination = rootDest + "classifieds.jsp";
      } else if (function.equals("ViewMyClassifieds")) {
        // récupérer les petites annonces de l'utilisateur
        Collection<ClassifiedDetail> classifieds = classifiedsSC.getClassifiedsByUser();
        request.setAttribute("Classifieds", classifieds);
        request.setAttribute("TitlePath", "classifieds.myClassifieds");
        request.setAttribute("AnonymousAccess", isAnonymousAccess(request));
        destination = rootDest + "classifieds.jsp";
      } else if (function.equals("SearchClassifieds")) {
        if (FileUploadUtil.isRequestMultipart(request)) {
          List<FileItem> items = FileUploadUtil.parseRequest(request);
          QueryDescription query = new QueryDescription();
          DataRecord data = null;
          XmlSearchForm searchForm = null;
          PublicationTemplateImpl template =
              (PublicationTemplateImpl) getPublicationTemplate(classifiedsSC);
          if (template != null) {
            String templateFileName = template.getFileName();
            String templateName = templateFileName.substring(0, templateFileName.lastIndexOf("."));
            RecordTemplate searchTemplate = template.getSearchTemplate();
            data = searchTemplate.getEmptyRecord();
            PagesContext context = new PagesContext("XMLSearchForm", "2",
                classifiedsSC.getLanguage(), classifiedsSC.getUserId());
            searchForm = (XmlSearchForm) template.getSearchForm();
            searchForm.update(items, data, context);

            String[] fieldNames = searchTemplate.getFieldNames();
            for (int f = 0; f < fieldNames.length; f++) {
              String fieldName = fieldNames[f];
              Field field = data.getField(fieldName);
              String fieldValue = field.getStringValue();
              if (fieldValue != null && fieldValue.trim().length() > 0) {
                String fieldQuery = fieldValue.trim().replaceAll("##", " AND "); // case à cocher
                // multiple
                query.addFieldQuery(new FieldDescription(templateName + "$$" + fieldName,
                    fieldQuery,
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
        }
        destination = rootDest + "classifiedsResult.jsp";
      } else if (function.equals("ViewClassified")) {
        request.setAttribute("IsDraftEnabled", classifiedsSC.isDraftEnabled());
        String classifiedId = request.getParameter("ClassifiedId");
        if (!StringUtil.isDefined(classifiedId)) {
          classifiedId = (String) request.getAttribute("ClassifiedId");
        }
        ResourcesWrapper resources = classifiedsSC.getResources();
        ClassifiedDetail classified = classifiedsSC.getClassified(classifiedId);
        request.setAttribute("Classified", classifiedsSC.getClassified(classifiedId));
        String creationDate = null;
        if (classified.getCreationDate() != null) {
          creationDate = resources.getOutputDateAndHour(classified.getCreationDate());
        } else {
          creationDate = "";
        }
        request.setAttribute("CreationDate", creationDate);
        String updateDate = null;
        if (classified.getUpdateDate() != null) {
          updateDate = resources.getOutputDateAndHour(classified.getUpdateDate());
        } else {
          updateDate = "";
        }
        request.setAttribute("UpdateDate", updateDate);
        String validateDate = null;
        if (classified.getValidateDate() != null) {
          validateDate = resources.getOutputDateAndHour(classified.getValidateDate());
        } else {
          validateDate = "";
        }
        request.setAttribute("ValidateDate", validateDate);
        Form formView = null;
        DataRecord data = null;
        PublicationTemplate pubTemplate = getPublicationTemplate(classifiedsSC);
        if (pubTemplate != null) {
          formView = pubTemplate.getViewForm();
          RecordSet recordSet = pubTemplate.getRecordSet();
          data = recordSet.getRecord(classifiedId);
          if (data != null) {
            PagesContext xmlContext = new PagesContext("myForm", "0", resources.getLanguage(),
                false, classified.getInstanceId(), null);
            xmlContext.setBorderPrinted(false);
            xmlContext.setIgnoreDefaultValues(true);
            request.setAttribute("Form", formView);
            request.setAttribute("Data", data);
            request.setAttribute("Context", xmlContext);
          }
        }

        destination = rootDest + "viewClassified.jsp";
      } else if (function.equals("NewClassified")) {
        // récupération des paramètres
        String fieldKey = request.getParameter("FieldKey");
        // passage des paramètres
        request.setAttribute("Classified", null);
        request.setAttribute("UserName", classifiedsSC.getUserDetail(userId).getDisplayedName());
        request.setAttribute("UserEmail", classifiedsSC.getUserDetail(userId).geteMail());
        Form formUpdate = null;
        DataRecord data = null;
        PublicationTemplate pubTemplate = getPublicationTemplate(classifiedsSC);
        if (pubTemplate != null) {
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
        if (FileUploadUtil.isRequestMultipart(request)) {
          List<FileItem> items = FileUploadUtil.parseRequest(request);
          String title = FileUploadUtil.getParameter(items, "Title");
          ClassifiedDetail classified = new ClassifiedDetail(title);
          String classifiedId = classifiedsSC.createClassified(classified, flag);
          PublicationTemplate pub = getPublicationTemplate(classifiedsSC);
          if (pub != null) {
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
            classifiedsSC.updateClassified(classified, false, false);
          }
        }
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
        PublicationTemplate pubTemplate = getPublicationTemplate(classifiedsSC);
        if (pubTemplate != null) {
          formView = pubTemplate.getUpdateForm();
          RecordSet recordSet = pubTemplate.getRecordSet();
          data = recordSet.getRecord(classifiedId);
          if (data != null) {
            request.setAttribute("Form", formView);
            request.setAttribute("Data", data);
          }
        }
        destination = rootDest + "classifiedManager.jsp";
      } else if (function.equals("UpdateClassified")) {
        if (FileUploadUtil.isRequestMultipart(request)) {
          List<FileItem> items = FileUploadUtil.parseRequest(request);
          String title = FileUploadUtil.getParameter(items, "Title");
          String classifiedId = FileUploadUtil.getParameter(items, "ClassifiedId");
          ClassifiedDetail classified = classifiedsSC.getClassified(classifiedId);
          classified.setTitle(title);
          PublicationTemplate pub = getPublicationTemplate(classifiedsSC);
          if (pub != null) {
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
          classifiedsSC.updateClassified(classified, true, SilverpeasRole.admin.isInRole(flag));
          request.setAttribute("ClassifiedId", classifiedId);
        }
        destination = getDestination("ViewMyClassifieds", classifiedsSC, request);
      } else if (function.equals("DeleteClassified")) {
        String classifiedId = request.getParameter("ClassifiedId");
        classifiedsSC.deleteClassified(classifiedId);
        destination = getDestination("ViewMyClassifieds", classifiedsSC, request);
      } else if (function.equals("AddComment")) {
        // récupération des paramètres
        String message = request.getParameter("Message");
        String classifiedId = request.getParameter("ClassifiedId");
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
        Form formUpdate = null;
        DataRecord data = null;
        PublicationTemplate pubTemplate = getPublicationTemplate(classifiedsSC);
        if (pubTemplate != null) {
          formUpdate = pubTemplate.getSearchForm();
          RecordSet recordSet = pubTemplate.getRecordSet();
          data = recordSet.getEmptyRecord();
        }
        request.setAttribute("Form", formUpdate);
        request.setAttribute("Data", data);
        destination = rootDest + "subscriptionManager.jsp";
      } else if (function.equals("AddSubscription")) {
        if (FileUploadUtil.isRequestMultipart(request)) {
          List<FileItem> items = FileUploadUtil.parseRequest(request);
          String field1 = FileUploadUtil.getParameter(items, classifiedsSC.getSearchFields1());
          String field2 = FileUploadUtil.getParameter(items, classifiedsSC.getSearchFields2());
          if (StringUtil.isDefined(field1) && StringUtil.isDefined(field2)) {
            Subscribe subscribe = new Subscribe(field1, field2);
            classifiedsSC.createSubscribe(subscribe);
          }
        }
        destination = getDestination("ViewMySubscriptions", classifiedsSC, request);
      } else if (function.equals("ViewMySubscriptions")) {
        Collection<Subscribe> subscribes = classifiedsSC.getSubscribesByUser();
        request.setAttribute("Subscribes", subscribes);
        Form formUpdate = null;
        DataRecord data = null;
        PublicationTemplate pubTemplate = getPublicationTemplate(classifiedsSC);
        if (pubTemplate != null) {
          formUpdate = pubTemplate.getSearchForm();
          RecordSet recordSet = pubTemplate.getRecordSet();
          data = recordSet.getEmptyRecord();
        }
        request.setAttribute("Form", formUpdate);
        request.setAttribute("Data", data);
        destination = rootDest + "subscriptions.jsp";
      } else if (function.equals("DeleteSubscription")) {
        String subscribeId = request.getParameter("SubscribeId");
        classifiedsSC.deleteSubscribe(subscribeId);
        destination = getDestination("ViewMySubscriptions", classifiedsSC, request);
      } else if (function.equals("ViewAllClassifiedsByCategory")) {
        String fieldKey = request.getParameter("FieldKey");
        String categoryName = request.getParameter("CategoryName");
        Collection<ClassifiedDetail> classifieds = new ArrayList<ClassifiedDetail>();
        PublicationTemplate pubTemplate = getPublicationTemplate(classifiedsSC);
        if (pubTemplate != null) {
          String templateFileName = pubTemplate.getFileName();
          String templateName = templateFileName.substring(0, templateFileName.lastIndexOf("."));
          String field = classifiedsSC.getSearchFields1();
          String label = pubTemplate.getRecordTemplate().getFieldTemplate(field).getFieldName();
          // Ajout des résultats de la recherche dans la catégorie
          QueryDescription query = new QueryDescription();
          query.addFieldQuery(new FieldDescription(templateName + "$$" + label, fieldKey, null));
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
        request.setAttribute("AnonymousAccess", isAnonymousAccess(request));
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

  private Collection<Category> createCategory(String templateName,
      String label,
      String stringKeys,
      String stringValues,
      ClassifiedsSessionController classifiedsSC) {
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

  /**
   * Gets the template of the publication based on the classified XML form.
   * @param classifiedsSC the session controller.
   * @return the publication template for classifieds.
   * @throws PublicationTemplateException if an error occurs while getting the publication template.
   */
  private PublicationTemplate getPublicationTemplate(
      final ClassifiedsSessionController classifiedsSC) throws PublicationTemplateException {
    PublicationTemplateImpl pubTemplate = null;
    String xmlFormName = classifiedsSC.getXMLFormName();
    if (StringUtil.isDefined(xmlFormName)) {
      String xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf("/") + 1,
          xmlFormName.indexOf("."));
      pubTemplate =
          (PublicationTemplateImpl) getPublicationTemplateManager().getPublicationTemplate(
          classifiedsSC.getComponentId() + ":" + xmlFormShortName,
          xmlFormName);
    }
    return pubTemplate;
  }

  /**
   * Gets an instance of PublicationTemplateManager.
   * @return an instance of PublicationTemplateManager.
   */
  private PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }

  private boolean isAnonymousAccess(HttpServletRequest request) {
    LookHelper lookHelper = (LookHelper) request.getSession().getAttribute("Silverpeas_LookHelper");
    if (lookHelper != null) {
      return lookHelper.isAnonymousAccess();
    }
    return false;
  }
}