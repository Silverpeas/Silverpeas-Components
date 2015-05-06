<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<view:setBundle basename="org.silverpeas.quickinfo.multilang.quickinfo"/>

<c:set var="listOfNews" value="${sessionScope['Silverpeas_BlockingNews']}"/>
<c:set var="finalURL" value="${sessionScope['Silverpeas_FinalURL']}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title></title>
  <link rel="stylesheet" type="text/css" href="../../quickinfo/jsp/styleSheets/quickinfo.css"/>
  <view:looknfeel/>
  <script type="text/javascript">
    var allNews = [];
    <c:forEach items="${listOfNews}" var="news">
    allNews[allNews.length] = '${news.id}';
    </c:forEach>

    var index = 0;
    var currentNews;

    function displayNews(index) {
      var id = allNews[index];
      $.ajax(webContext + "/services/news/" + id, {
        type : "GET",
        async : false,
        cache : false,
        success : function(news) {
          currentNews = news;
          $(".actuality-title").text(news.title);
          if (typeof news.description === 'string' && news.description.length > 0) {
            $(".quickInfo-description").show();
            $(".quickInfo-description").html(news.description);
          } else {
            $(".quickInfo-description").hide();
          }
          var forNbDays = news.publishedForNbDays;
          var dateLabel = news.publishTime;
          if (forNbDays == 0) {
            dateLabel = "<fmt:message key="quickinfo.blocking.news.today"/> " + dateLabel;
          } else if (forNbDays == 1) {
            dateLabel = "<fmt:message key="quickinfo.blocking.news.yesterday"/> " + dateLabel;
          } else {
            dateLabel = "<fmt:message key="quickinfo.blocking.news.daysAgo"/> " + forNbDays +
                " <fmt:message key="quickinfo.blocking.news.days"/>";
          }
          $(".date").text(dateLabel);
          $("#richContent").html(news.content);
          if (news.thumbnailURL && news.thumbnailURL.length > 0) {
            $("#illustration img").attr("src", news.thumbnailURL);
            $("#illustration img").show();
          } else {
            $("#illustration img").hide();
          }
        }
      });
    }

    function nextNews() {
      acknowledgeCurrentNews().always(function() {
        // go to next news
        index++;
        if (index >= allNews.length) {
          location.href = "${finalURL}";
        } else {
          displayNews(index);
        }
      });
    }

    function acknowledgeCurrentNews() {
      <%-- A promise is created in order to wait for successful
      acknowledge before going to next news or homepage. --%>
      var deferred = new $.Deferred();
      // save news reading by user
      $.ajax(webContext + "/services/news/" + currentNews.id + "/acknowledge", {
        type : "POST",
        cache : false,
        success : function(data, status, jqXHR) {
          deferred.resolve();
        },
        error : function(jqXHR, textStatus, errorThrown) {
          window.console &&
          window.console.log('Silverpeas Blocking News Request - ERROR - ' + errorThrown);
          deferred.reject();
        }
      });
      return deferred.promise();
    }

    $(function() {
      displayNews(0);
    });
  </script>
</head>
<body class="alert-quickInfo">
<div class="page">
  <div id="header"><img src="/silverpeas/images/logo.jpg" class="logo" alt="logo"/></div>
  <div class="cadre">
    <div class="content-alert-quickInfo">
      <h1 class="titre"><fmt:message key="quickinfo.blocking.title"/></h1>

      <div class="rightContent">
        <div id="illustration"><img alt="" src=""/></div>
      </div>
      <div class="principalContent">
        <h2 class="actuality-title"></h2>

        <div class="date"></div>
        <p class="quickInfo-description"></p>

        <div id="richContent"></div>
      </div>
    </div>
  </div>
  <a class="validate-actuality" href="#" onclick="javascript:nextNews()"><span><fmt:message key="quickinfo.blocking.button"/></span></a>
</div>
</body>
</html>