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
/* Use the iframe document as the single main scroll surface. */
(() => {
  'use strict';
  document.addEventListener('DOMContentLoaded', () => {
    const discussion = document.getElementById('discussion');
    if (!discussion) {
      return;
    }
    const header = discussion.querySelector('.discussion-header');
    const stream = discussion.querySelector('.discussion-stream');
    const composer = discussion.querySelector('.discussion-composer');
    const sidebar = discussion.querySelector('.discussion-sidebar');
    const ancestors = [];
    for (let node = discussion.parentElement; node; node = node.parentElement) {
      ancestors.push(node);
    }
    discussion.classList.add('discussion-document-scroll');
    document.documentElement.classList.add('discussion-document');
    // History pagination and live updates share the same document scroll coordinates.
    window.DiscussionScroll = {
      get scrollTop() { return document.scrollingElement.scrollTop; },
      set scrollTop(value) { document.scrollingElement.scrollTop = value; },
      get scrollHeight() { return document.scrollingElement.scrollHeight; },
      get clientHeight() { return window.innerHeight; },
      getBoundingClientRect: () => ({top: header.getBoundingClientRect().bottom}),
      addEventListener: (...args) => window.addEventListener(...args),
      observerRoot: null
    };
    let pending;
    const property = (name, value) => {
      if (discussion.style.getPropertyValue(name) !== value) {
        discussion.style.setProperty(name, value);
      }
    };
    const fit = () => {
      const viewport = window.visualViewport;
      const height = viewport ? viewport.height : window.innerHeight;
      const box = discussion.getBoundingClientRect();
      const column = stream.getBoundingClientRect();
      const inset = ancestors.reduce((sum, node) => {
        const style = window.getComputedStyle(node);
        return sum + ['paddingBottom', 'borderBottomWidth', 'marginBottom']
            .reduce((value, key) => value + (Number.parseFloat(style[key]) || 0), 0);
      }, 0);
      property('--discussion-available-height', Math.max(0, height - box.top - window.scrollY - inset - 1) + 'px');
      property('--discussion-header-height', header.getBoundingClientRect().height + 'px');
      property('--discussion-sidebar-top', header.getBoundingClientRect().bottom + 'px');
      property('--discussion-left', column.left + 'px');
      property('--discussion-width', column.width + 'px');
      property('--discussion-right', Math.max(0, document.documentElement.clientWidth - box.right + 1) + 'px');
      property('--discussion-sidebar-width', Math.max(0, box.right - column.right - 1) + 'px');
      property('--discussion-composer-height', (composer?.getBoundingClientRect().height || 0) + 'px');
      property('--discussion-authors-height', window.innerWidth <= 760 ? sidebar.getBoundingClientRect().height + 'px' : '0px');
    };
    const schedule = () => {
      window.cancelAnimationFrame(pending);
      pending = window.requestAnimationFrame(fit);
    };
    const observer = new ResizeObserver(schedule);
    const observe = () => {
      [...ancestors, header, stream, sidebar, composer].filter(Boolean).forEach(node => observer.observe(node));
    };
    observe();
    window.addEventListener('resize', schedule);
    window.addEventListener('scroll', schedule, {passive: true});
    window.visualViewport?.addEventListener('resize', schedule);
    window.addEventListener('pageshow', () => { observe(); schedule(); });
    window.addEventListener('pagehide', () => {
      observer.disconnect();
      window.cancelAnimationFrame(pending);
    });
    fit();
  });
})();
