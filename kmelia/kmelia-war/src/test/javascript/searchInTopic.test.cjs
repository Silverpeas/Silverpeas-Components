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
const { runInNewContext, createContext } = require('node:vm');
const { resolve } = require('node:path');

function readScript(name) {
  return readFileSync(resolve(__dirname, '../../main/webapp/kmelia/jsp/javaScript/' + name),
      'utf8');
}

/**
 * Loads navigation.js and searchInTopic.js in a sandbox in which the AJAX requests are recorded:
 * the search request is answered only when the test settles it.
 */
function newSearchInTopic() {
  const requests = [];
  let searchAnswer;
  const $ = function() {
    return { serialize: () => '', val: () => 'silverpeas' };
  };
  $.progressMessage = () => {};
  $.get = function(url, params, callback) {
    if (params.SearchRequest) {
      requests.push({ action: 'Search', query: params.Query });
      searchAnswer = callback;
    } else {
      requests.push({ action: params.Action, id: params.Id });
    }
  };
  const context = createContext({
    window: {},
    $: $,
    jQuery: $,
    document: {
      querySelector: () => null,
      getElementById: () => ({})
    },
    spProgressMessage: { show: () => {}, hide: () => {} },
    setTimeout: () => 0,
    clearTimeout: () => {},
    getWebContext: () => '/silverpeas',
    getComponentId: () => 'kmelia1',
    updateHtmlContainingAngularDirectives: () => {},
    activateUserZoom: () => {},
    showPublicationCheckedBoxes: () => {}
  });
  runInNewContext(readScript('navigation.js'), context);
  runInNewContext(readScript('searchInTopic.js'), context);
  context.setCurrentNodeId('0');
  return {
    context,
    requests,
    searchAnswered: () => searchAnswer('<ul><li>publication</li></ul>')
  };
}

test('the operations of the current folder are requested once the search results are displayed',
    () => {
      const search = newSearchInTopic();

      search.context.searchInTopic();

      assert.deepEqual(search.requests.map(r => r.action), ['Search']);

      search.searchAnswered();

      assert.deepEqual(search.requests.map(r => r.action), ['Search', 'GetOperations']);
      assert.equal(search.requests[1].id, '0');
    });
