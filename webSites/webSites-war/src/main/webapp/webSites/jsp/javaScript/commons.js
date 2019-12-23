/*
 * Copyright (C) 2000 - 2019 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

(function() {
  window.WebSiteManager = function(params) {
    var __context = extendsObject({
      contextUrl : undefined,
      beforeOpenSiteTopicCallback : undefined,
      beforeOpenSiteCallback : undefined
    }, (typeof params === 'string' ? {contextUrl : params} : params));
    /**
     * Go to the application represented by given identifier.
     */
    this.goToApp = function(componentId) {
      spWindow.loadComponent(componentId);
    };
    /**
     * Go to the page given by the parameter into context of current application instance.
     */
    this.goToAppPage = function(page) {
      spWindow.loadContent(page);
    };
    /**
     * Go to the page given by the parameter into context of current application instance.
     */
    this.goToAppTopic = function(id) {
      this.openSiteTopic(id);
    };
    /**
     * Opens a popup displaying all the icons and their definition.
     */
    this.openIconDictionary = function() {
      SP_openWindow("dictionnaireIcones.jsp", "dico", "480", "300", "scrollbars=yes, resizable, alwaysRaised");
    };
    /**
     * Navigating to the topic aimed by the given identifier.
     * @param id identifier of a topic.
     */
    this.openSiteTopic = function(id) {
      if (typeof __context.beforeOpenSiteTopicCallback === "function") {
        __context.beforeOpenSiteTopicCallback();
      }
      return spWindow.loadContent(sp.url.format(__context.contextUrl, {
        'Action' : 'Search',
        'Id' : id
      }));
    };
    /**
     * Load the data of the site represented by the given identifier.
     * @param siteId an identifier of a site.
     * @returns {*} a promise with the JSON data of the aimed site.
     */
    this.loadSite = function(siteId) {
      return sp.ajaxRequest('siteAsJson').withParam('id', siteId).sendAndPromiseJsonResponse();
    };
    /**
     * Navigating to the site according the different given parameters.
     * @param siteId
     * @param options
     */
    this.openSite = function(siteId, options) {
      if (typeof __context.beforeOpenSiteCallback === "function") {
        __context.beforeOpenSiteCallback();
      }
      return this.loadSite(siteId).then(function(site) {
        var sitePath = site.contentPath;
        if (__isLinkSite(site.type)) {
          if (sitePath.indexOf("://") !== -1) {
            site.url = sitePath;
          } else {
            site.url = "http://" + sitePath;
          }
        } else {
          site.url += '/' + sitePath;
        }
        __display(site, options);
      });
    };
    var __isLinkSite = function(siteType) {
      return siteType === WebSiteManager.Constants.LINK_SITE_TYPE
          || siteType === ('' + WebSiteManager.Constants.LINK_SITE_TYPE);
    };
    var __display = function(site, options) {
      var __options = extendsObject({
        popupTarget : '_blank',
        popupWidth : 670,
        popupHeight : 500,
        forceSitePopupOpening : false
      }, __context, options);
      if (!__options.forceSitePopupOpening && !site.popup) {
        sp.navRequest('DisplaySite').withParam('Id', site.id).withParam('SitePage', site.url).go();
      } else {
        var windowParams = "width=" + __options.popupWidth + ",height=" + __options.popupHeight + ", toolbar=yes, scrollbars=yes, resizable, alwaysRaised";
        window.open(site.url, __options.popupTarget, windowParams);
      }
    };
  };
  WebSiteManager.Constants = {
    LINK_SITE_TYPE : 1
  }
})();
