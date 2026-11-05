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
const test = require('node:test');
const assert = require('node:assert/strict');
const path = require('node:path');
const {chromium} = require('playwright');
const script = path.join(__dirname,'../../main/webapp/discussion/jsp/javaScript/discussion-document-viewer.js');
const launch = () => chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
async function setup(page) {
  await page.setContent(`<main id="discussion"><div id="messages"><button id="message-view" class="discussion-view-file" data-attachment-id="native-file-uuid" data-language="fr"><svg width="20" height="20"><circle cx="10" cy="10" r="8"/></svg></button></div><div id="files"></div><a href="/silverpeas/attached_file/file" download="budget.pdf">budget.pdf</a><p id="discussion-viewer-error" hidden>Erreur</p></main>`);
  await page.evaluate(()=>{
    window.views=[];
    window.jQuery=element=>({view:(method,options)=>window.views.push({id:element.id,method,options})});
    window.jQuery.fn={view:()=>{}};
  });
  await page.addScriptTag({path:script});
  await page.evaluate(()=>document.dispatchEvent(new Event('DOMContentLoaded')));
}

test('message and dynamically refreshed sidebar buttons open the native attachment viewer',async()=>{
  const browser=await launch();
  try {
    const page=await browser.newPage();await setup(page);
    await page.locator('#message-view svg').click();
    assert.deepEqual(await page.evaluate(()=>window.views[0]),{id:'message-view',method:'document',options:{documentId:'native-file-uuid',documentType:'attachment',lang:'fr'}});
    await page.evaluate(()=>{document.getElementById('files').innerHTML='<button id="sidebar-view" class="discussion-view-file" data-attachment-id="another-file" data-language="en">Voir</button>';});
    await page.locator('#sidebar-view').click();
    assert.equal(await page.evaluate(()=>window.views[1].options.documentId),'another-file');
    assert.equal(await page.evaluate(()=>window.views[1].options.lang),'en');
    assert.equal(await page.locator('a[download]').getAttribute('href'),'/silverpeas/attached_file/file');
    assert.equal(await page.locator('#discussion-viewer-error').isVisible(),false);
  } finally {await browser.close();}
});

test('a missing viewer plugin gives feedback while the download stays available',async()=>{
  const browser=await launch();
  try {
    const page=await browser.newPage();await setup(page);
    await page.evaluate(()=>{window.jQuery.fn.view=undefined;});
    await page.locator('#message-view').click();
    assert.equal(await page.locator('#discussion-viewer-error').isVisible(),true);
    assert.equal(await page.locator('a[download]').getAttribute('download'),'budget.pdf');
    assert.equal(await page.evaluate(()=>window.views.length),0);
  } finally {await browser.close();}
});


test('message and refreshed sidebar edit actions submit to native WBE in a new tab without reserving the file or altering the draft', async () => {
  const browser = await launch();
  try {
    const page = await browser.newPage();
    await setup(page);
    await page.evaluate(() => {
      window.editions = [];
      window.sp = {formRequest: url => {
        const request = {url, params: {}};
        return {
          byPostMethod() {request.method = 'POST'; return this;},
          withParam(key, value) {request.params[key] = value; return this;},
          toTarget(target) {request.target = target; return this;},
          submit() {editions.push(request);}
        };
      }};
      document.getElementById('messages').insertAdjacentHTML('beforeend', '<button id="message-edit" class="discussion-edit-file" data-edit-url="/silverpeas/Rdiscussion/discussion1/channels/main/files/file1/edit" data-language="fr"><svg width="20" height="20"><circle cx="10" cy="10" r="8"/></svg></button><textarea id="draft">Mon brouillon</textarea>');
    });
    await page.locator('#message-edit svg').click();
    assert.deepEqual(await page.evaluate(() => editions[0]), {
      url: '/silverpeas/Rdiscussion/discussion1/channels/main/files/file1/edit',
      method: 'POST', params: {language: 'fr'}, target: '_blank'
    });
    await page.evaluate(() => {
      document.getElementById('files').innerHTML = '<button id="sidebar-edit" class="discussion-edit-file" data-edit-url="/silverpeas/Rdiscussion/discussion1/files/file2/edit" data-language="en">Modifier</button>';
    });
    await page.locator('#sidebar-edit').click();
    assert.equal(await page.evaluate(() => editions[1].params.language), 'en');
    assert.equal(await page.locator('#draft').inputValue(), 'Mon brouillon');
    assert.equal(await page.evaluate(() => views.length), 0);
  } finally {await browser.close();}
});
