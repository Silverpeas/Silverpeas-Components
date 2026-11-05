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
/* Activate the native Silverpeas user card on initial and dynamically inserted names. */
(() => {
  'use strict';
  document.addEventListener('DOMContentLoaded', () => {
    const discussion = document.getElementById('discussion');
    if (!discussion) { return; }
    whenSilverpeasReady(() => {
      const activate = container => {
        if (!(container instanceof Element)) { return; }
        const names = [...container.querySelectorAll('.userToZoom')];
        if (container.matches('.userToZoom')) { names.unshift(container); }
        names.forEach(name => {
          const target = jQuery(name);
          if (!target.data('userZoom')) { target.userZoom({id:name.getAttribute('rel')}); }
        });
      };
      activate(discussion);
      const observer = new MutationObserver(changes => {
        changes.forEach(change => change.addedNodes.forEach(activate));
      });
      const observe = () => observer.observe(discussion, {childList:true, subtree:true});
      observe();
      window.addEventListener('pagehide', () => observer.disconnect());
      window.addEventListener('pageshow', event => {
        if (event.persisted) { activate(discussion); observe(); }
      });
    });
  });
})();
