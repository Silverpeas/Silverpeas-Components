<%--
  Copyright (C) 2000 - 2014 Silverpeas
  
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.
  
  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
  
  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/gallery" prefix="gallery" %>

<c:set var="__userLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${__userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<%-- Default values --%>
<c:set var="_formName" value="mediaForm"/>
<c:set var="_formAction" value="EditSelectedMedia"/>
<c:set var="_searchKeyword" value=""/>

<%@ attribute name="formName" required="false"
              type="java.lang.String"
              description="The name of the HTML form TAG ('mediaForm' by default)." %>
<c:if test="${formName != null}">
  <c:set var="_formName" value="${formName}"/>
</c:if>

<%@ attribute name="formAction" required="false"
              type="java.lang.String"
              description="The action of the HTML form TAG ('EditSelectedMedia' by default)." %>
<c:if test="${formAction != null}">
  <c:set var="_formAction" value="${formAction}"/>
</c:if>

<%@ attribute name="currentAlbum" required="true"
              type="com.silverpeas.gallery.model.AlbumDetail"
              description="The current album." %>

<%@ attribute name="mediaList" required="true"
              type="java.util.List"
              description="The album path." %>
<jsp:useBean id="mediaList"
             type="java.util.List<com.silverpeas.gallery.model.Media>"
             scope="page"/>
<%@ attribute name="selectedIds" required="true"
              type="java.util.List"
              description="The media that must be maked as selected." %>
<jsp:useBean id="selectedIds"
             type="java.util.List<java.lang.String>"
             scope="page"/>
<%@ attribute name="isViewMetadata" required="true"
              type="java.lang.Boolean"
              description="Indicates, if true, that metadata of photo must be displayed." %>

<%@ attribute name="mediaResolution" required="true"
              type="com.silverpeas.gallery.constant.MediaResolution"
              description="The album path." %>
<%@ attribute name="isViewList" required="true"
              type="java.lang.Boolean"
              description="Is the display mode of list." %>

<%@ attribute name="nbMediaPerPage" required="true"
              type="java.lang.Integer"
              description="Number of media per page" %>
<%@ attribute name="currentPageIndex" required="true"
              type="java.lang.Integer"
              description="Index of the current page" %>

<%@ attribute name="searchKeyword" required="false"
              type="java.lang.String"
              description="Current keyword search." %>
<c:if test="${searchKeyword != null}">
  <c:set var="_searchKeyword" value="${searchKeyword}"/>
</c:if>

<c:set var="firstMediaIndex" value="${nbMediaPerPage * currentPageIndex}"/>
<c:set var="lastMediaIndex" value="${firstMediaIndex + nbMediaPerPage - 1}"/>

<c:set var="typeAff" value="default"/>
<c:choose>
  <c:when test="${mediaResolution.small}">
    <c:set var="nbMediaPerLine" value="5"/>
    <c:if test="${not empty isViewList && isViewList}">
      <c:set var="typeAff" value="small_list"/>
    </c:if>
  </c:when>
  <c:when test="${mediaResolution.medium}">
    <c:set var="nbMediaPerLine" value="3"/>
    <c:if test="${not empty isViewList && isViewList}">
      <c:set var="typeAff" value="medium_list"/>
      <c:set var="nbMediaPerLine" value="1"/>
    </c:if>
  </c:when>
  <c:otherwise>
    <c:set var="nbMediaPerLine" value="8"/>
  </c:otherwise>
</c:choose>

<script type="text/javascript">

  /**
   * Method that handle the navigation with selection management.
   */
  function doPagination(index) {
    document.${_formName}.SelectedIds.value = getMediaIds(true);
    document.${_formName}.NotSelectedIds.value = getMediaIds(false);
    document.${_formName}.Index.value = index;
    document.${_formName}.action = "Pagination";
    document.${_formName}.submit();
  }

  /**
   * Method that permits to get the media identifiers that are selected.
   * @param selected
   * @returns {string}
   */
  function getMediaIds(selected) {
    var items = "";
    try {
      var boxItems = document.${_formName}.SelectMedia;
      if (boxItems != null) {
        // au moins une checkbox exist
        var nbBox = boxItems.length;
        if ((nbBox == null) && (boxItems.checked == selected)) {
          // only one checkbox
          items += boxItems.value + ",";
        } else {
          // several checkboxes
          for (var i = 0; i < boxItems.length; i++) {
            if (boxItems[i].checked == selected) {
              items += boxItems[i].value + ",";
            }
          }
        }
      }
    } catch (e) {
      //Checkboxes are not displayed
    }
    return items;
  }
</script>

<c:if test="${not empty mediaList}">
  <view:board>
    <form name="${_formName}" action="${_formAction}">
      <input type="hidden" name="AlbumId" value="${currentAlbum.id}"/>
      <input type="hidden" name="SearchKeyWord" value="${_searchKeyword}">
      <input type="hidden" name="Index"/>
      <input type="hidden" name="SelectedIds"/>
      <input type="hidden" name="NotSelectedIds"/>

      <c:set var="textColumnCount" value="${typeAff eq 'medium_list' ? 2 : 0}"/>
      <table width="98%" border="0" cellspacing="0" cellpadding="0" align="center">
        <tr>
          <td colspan="${nbMediaPerLine + textColumnCount}" align="center">
            <gallery:albumListHeader currentMediaResolution="${mediaResolution}"
                                     nbMediaPerPage="${nbMediaPerPage}"
                                     currentPageIndex="${currentPageIndex}"
                                     mediaList="${mediaList}"/>
          </td>
        </tr>
      </table>
      <table class="listing-media">
        <c:set var="cellWidth" value="${100 / nbMediaPerLine}"/>
        <c:forEach var="media" items="${mediaList}" begin="${firstMediaIndex}" end="${lastMediaIndex}" varStatus="loop">
          <c:set var="isNewLine" value="${loop.index % nbMediaPerLine == 0}"/>
          <c:set var="isEndLine"
                 value="${loop.last or loop.index % nbMediaPerLine == (nbMediaPerLine-1)}"/>
          <c:if test="${isNewLine}">
            <tr>
              <td colspan="${nbMediaPerLine + textColumnCount}">&nbsp;</td>
            </tr>
            <tr>
          </c:if>
          <c:set var="mediaBackgroundClass" value="${media.visible ? 'fondPhoto' : 'fondPhotoNotVisible'}"/>
          <c:set var="mediaChecked" value="${selectedIds.contains(media.id) ? 'checked' : ''}"/>
          <c:choose>
            <c:when test="${typeAff eq 'default' or typeAff eq 'small_list'}">
              <td class="a-media ${typeAff eq 'small_list' ? 'aff-small': 'aff-tiny'}" width="${cellWidth}%">
                <div class="${mediaBackgroundClass}">
                  <div class="cadrePhoto">
                    <a href="MediaView?MediaId=${media.id}">
                      <img src="${media.getApplicationThumbnailUrl(mediaResolution)}" border="0" alt="<c:out value='${media.title}'/>" title="<c:out value='${media.title}'/>"/>
                    </a>
                  </div>
                  <div>
                    <input type="checkbox" name="SelectMedia" value="${media.id}" ${mediaChecked}/>
                  </div>
                  <div class="txtlibform"><c:out value="${media.title}"/></div>
                  <c:if test="${typeAff eq 'small_list' and not empty media.description}">
                    <div class="media-description"><c:out value="${media.description}"/></div>
                  </c:if>
                </div>
              </td>
            </c:when>
            <c:otherwise>
              <td class="a-media aff-list checkbox">
                <input type="checkbox" name="SelectMedia" value="${media.id}" ${mediaChecked}/>
              </td>
              <td class="a-media aff-list vignette">
                <div class="${mediaBackgroundClass}">
                  <div class="cadrePhoto">
                    <a href="MediaView?MediaId=${media.id}">
                      <img src="${media.getApplicationThumbnailUrl(mediaResolution)}" border="0" alt="<c:out value='${media.title}'/>" title="<c:out value='${media.title}'/>"/>
                    </a>
                  </div>
                </div>
              </td>
              <td class="a-media aff-list details forms" >
              <ul class="fields ui-sortable">
                <li class="field field_category media-name">
                  <label class="txtlibform"><fmt:message key="GML.title"/> :</label>

                  <div class="fieldInput"><c:out value="${media.title}"/></div>
                </li>
                <c:if test="${not empty media.description}">
                  <li class="field field_category media-name">
                    <label class="txtlibform"><fmt:message key="GML.description"/> :</label>

                    <div class="fieldInput"><c:out value="${media.description}"/></div>
                  </li>
                </c:if>
                <c:if test="${not empty media.author}">
                  <li class="field field_category media-name">
                    <label class="txtlibform"><fmt:message key="GML.author"/> :</label>

                    <div class="fieldInput"><c:out value="${media.author}"/></div>
                  </li>
                </c:if>
                <c:if test="${isViewMetadata and media.type.photo}">
                  <c:set var="photoMedia" value="${media.photo}"/>
                  <c:forEach var="metaDataKey" items="${photoMedia.metaDataProperties}">
                    <li class="field field_category media-name">
                      <c:set var="metaData" value="${photoMedia.getMetaData(metaDataKey)}"/>
                      <jsp:useBean id="metaData" type="com.silverpeas.gallery.model.MetaData"/>
                      <label class="txtlibform"><c:out value="${metaData.label}"/> :</label>

                      <div class="fieldInput">
                        <c:out value="${metaData.date ? silfn:formatDateAndHour(metaData.dateValue, __userLanguage) : metaData.value}"/>
                      </div>
                    </li>
                  </c:forEach>
                </c:if>
                <c:if test="${not empty media.keyWord}">
                  <li class="field field_category media-name">
                    <label class="txtlibform"><fmt:message key="gallery.keyword"/> :</label>

                    <div class="fieldInput">
                      <c:set var="delims" value=" "/>
                      <c:forEach var="keyword" items="${silfn:splitOnWhitespace(media.keyWord)}">
                        <a href="SearchKeyWord?SearchKeyWord=${keyword}"><c:out value="${keyword}"/></a>
                      </c:forEach>
                    </div>
                  </li>
                </c:if>
              </ul>
            </c:otherwise>
          </c:choose>
          <c:if test="${isEndLine}">
            </tr>
          </c:if>
        </c:forEach>
        <c:if test="${fn:length(mediaList) > nbMediaPerPage}">
          <tr>
            <td colspan="${nbMediaPerLine + textColumnCount}">&nbsp;</td>
          </tr>
          <tr class="intfdcolor4 pagination">
            <td colspan="${nbMediaPerLine + textColumnCount}">
              <view:pagination action="doPagination" actionIsJsFunction="${true}"
                               currentPage="${currentPageIndex}"
                               nbItemsPerPage="${nbMediaPerPage}" totalNumberOfItems="${fn:length(mediaList)}"/>
            </td>
          </tr>
        </c:if>
      </table>
    </form>
  </view:board>
</c:if>
