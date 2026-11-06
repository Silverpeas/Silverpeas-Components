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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
const { test } = require('node:test');
const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const { runInNewContext } = require('node:vm');
const { resolve } = require('node:path');

const script = readFileSync(resolve(__dirname,
    '../../main/webapp/kmelia/jsp/javaScript/navigation.js'), 'utf8');

/**
 * Loads navigation.js in a sandbox in which the AJAX requests are recorded: the request of the
 * publications is answered only when the test settles it.
 */
function newNavigation() {
  const requests = [];
  let publicationsLoading;
  const $ = function() {
    return {};
  };
  $.get = function(url, params) {
    requests.push(params.Action);
  };
  const context = {
    window: {},
    $: $,
    jQuery: $,
    document: {
      querySelector: () => ({ innerHTML: '', appendChild: () => {} }),
      createElement: () => ({ classList: { add: () => {} } })
    },
    sp: {
      element: { querySelectorAll: () => [] },
      i18n: { get: key => key },
      ajaxRequest: function() {
        return {
          withParams: function() {
            return this;
          },
          send: function() {
            requests.push('GetPublications');
            return new Promise((resolve, reject) => {
              publicationsLoading = { resolve, reject };
            });
          }
        };
      }
    },
    spProgressMessage: { show: () => {}, hide: () => {} },
    setTimeout: () => 0,
    clearTimeout: () => {},
    getWebContext: () => '/silverpeas',
    getComponentId: () => 'kmelia1',
    getPubIdToHighlight: () => '',
    updateHtmlContainingAngularDirectives: () => {},
    activateUserZoom: () => {},
    showPublicationCheckedBoxes: () => {}
  };
  runInNewContext(script, context);
  return {
    context,
    requests,
    publicationsLoaded: () => publicationsLoading.resolve({ responseText: '<ul></ul>' }),
    publicationsFailed: () => publicationsLoading.reject({ responseText: 'error' })
  };
}

test('the operations of a folder are requested only once its publications are loaded',
    async () => {
      const navigation = newNavigation();

      const displaying = navigation.context.displayPublicationsAndOperations('2');

      assert.deepEqual(Array.from(navigation.requests), ['GetPublications']);

      navigation.publicationsLoaded();
      await displaying;

      assert.deepEqual(Array.from(navigation.requests), ['GetPublications', 'GetOperations']);
    });

test('the operations of a folder are requested even if its publications cannot be loaded',
    async () => {
      const navigation = newNavigation();

      const displaying = navigation.context.displayPublicationsAndOperations('2');
      navigation.publicationsFailed();
      await displaying;

      assert.deepEqual(Array.from(navigation.requests), ['GetPublications', 'GetOperations']);
    });
