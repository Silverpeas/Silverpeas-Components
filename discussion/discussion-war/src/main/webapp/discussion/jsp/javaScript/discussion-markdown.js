/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/* Visual editing only; canonical Markdown remains in the existing native form. */
(function(root) {
  'use strict';
  const shared = typeof module !== 'undefined' && module.exports
      ? require('../../../../../../target/core-editor-resources/util/javaScript/richEditor/silverpeas-markdown.js')
      : root.SilverpeasMarkdown;
  const api = {...shared,
    renderHtml:text => shared.renderHtml(text)?.replaceAll('data-sp-mention', 'data-discussion-mention'),
    render:text => {
      const content = shared.render(text);
      content?.querySelectorAll('[data-sp-mention]').forEach(node => {
        node.dataset.discussionMention = node.dataset.spMention;
      });
      return content;
    },
    enhanceEditor:(textarea, tools, insert) => {
      shared.enhanceEditor(textarea, tools, insert, document.getElementById('discussion').dataset);
      textarea.parentElement.querySelectorAll('[class]').forEach(node => {
        [...node.classList].filter(name => name.startsWith('sp-')).forEach(name => node.classList.add('discussion' + name.slice(2)));
      });
    }
  };
  if (typeof module !== 'undefined' && module.exports) { module.exports = api; }
  else { root.DiscussionMarkdown = api; }
})(globalThis);
