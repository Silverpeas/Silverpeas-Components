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
/* Measure the available viewport in this document, without changing the parent navigation. */
(() => {
  'use strict';
  document.addEventListener('DOMContentLoaded', () => {
    const discussion = document.getElementById('discussion');
    if (!discussion) {
      return;
    }
    const scroller = document.getElementById('discussion-scroll');
    const ancestors = [];
    for (let node = discussion.parentElement; node; node = node.parentElement) {
      ancestors.push(node);
    }
    let pending;
    const fit = () => {
      const atBottom = scroller.scrollHeight - scroller.scrollTop - scroller.clientHeight < 2;
      const viewport = window.visualViewport;
      const bottom = viewport ? viewport.height + viewport.offsetTop : window.innerHeight;
      const top = discussion.getBoundingClientRect().top + window.scrollY;
      // Reserve the real frame padding, borders and margins below the component.
      const inset = ancestors.reduce((sum, node) => {
        const style = window.getComputedStyle(node);
        return sum + ['paddingBottom', 'borderBottomWidth', 'marginBottom']
            .reduce((value, property) => value + (Number.parseFloat(style[property]) || 0), 0);
      }, 0);
      const height = Math.max(0, Math.floor(bottom - top - inset - 1)) + 'px';
      if (discussion.style.getPropertyValue('--discussion-available-height') !== height) {
        discussion.style.setProperty('--discussion-available-height', height);
      }
      if (atBottom) {
        scroller.scrollTop = scroller.scrollHeight;
      }
    };
    const schedule = () => {
      window.cancelAnimationFrame(pending);
      pending = window.requestAnimationFrame(fit);
    };
    const observer = new ResizeObserver(schedule);
    ancestors.forEach(node => observer.observe(node));
    window.addEventListener('resize', schedule);
    window.visualViewport?.addEventListener('resize', schedule);
    window.addEventListener('pageshow', () => {
      ancestors.forEach(node => observer.observe(node));
      schedule();
    });
    window.addEventListener('pagehide', () => {
      observer.disconnect();
      window.cancelAnimationFrame(pending);
    });
    fit();
  });
})();
