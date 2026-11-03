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
const script = path.join(__dirname,'../../main/webapp/discussion/jsp/javaScript/discussion-search.js');

async function setup() {
  const browser = await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
  const page = await browser.newPage();
  await page.setContent(`<base href="http://discussion.test/"><form id="discussion-search" action="/silverpeas/Rdiscussion/discussion1/search"><input type="search" id="discussion-search-query"/><button type="submit">Search</button></form><textarea id="draft">Un brouillon</textarea><details id="discussion-search-section" hidden><summary>Results <span id="discussion-search-count"></span></summary><p id="discussion-search-status" data-loading="Loading" data-error="Search failed" role="status"></p><div id="discussion-search-results"></div><button id="discussion-search-more" hidden>More</button><button id="discussion-search-close">Close</button></details>`);
  await page.evaluate(() => {
    window.requests=[];
    window.sp={ajaxRequest:url=>{
      const request={url,params:{}};
      return {withParam(name,value){request.params[name]=value;return this;},send(){
        window.requests.push(request);
        return new Promise((resolve,reject)=>{request.resolve=html=>resolve({responseText:html});request.reject=reject;});
      }};
    }};
  });
  await page.addScriptTag({path:script});
  await page.evaluate(() => document.dispatchEvent(new Event('DOMContentLoaded')));
  return {browser,page};
}
const fragment = (id,next,total,more) => `<div class="discussion-search-page" data-next-offset="${next}" data-total="${total}" data-has-more="${more}"><ul><li><a href="/silverpeas/Rdiscussion/discussion1/messages/${id}#message-${id}">Message ${id} @Alice</a></li></ul></div>`;

test('search queries the entire discussion without replacing history or draft and paginates results', async () => {
  const {browser,page}=await setup();
  try {
    await page.locator('#discussion-search-query').fill('projet');
    await page.locator('button[type=submit]').click();
    assert.equal(await page.locator('#discussion-search-status').textContent(),'Loading');
    const request=await page.evaluate(()=>({url:requests[0].url,params:requests[0].params}));
    assert.ok(request.url.endsWith('/discussion1/search'),JSON.stringify(request));
    assert.deepEqual(request.params,{q:'projet',offset:'0',allChannels:'false'});
    await page.evaluate(html=>requests[0].resolve(html),fragment('old-message',20,21,true));
    await page.waitForFunction(()=>document.getElementById('discussion-search-count').textContent==='21');
    assert.equal(await page.locator('#discussion-search-section').evaluate(n=>n.open),true);
    assert.equal(await page.locator('#draft').inputValue(),'Un brouillon');
    assert.ok((await page.locator('#discussion-search-results a').getAttribute('href')).endsWith('messages/old-message#message-old-message'));
    await page.locator('#discussion-search-more').click();
    assert.equal(await page.evaluate(()=>requests[1].params.offset),'20');
    await page.evaluate(html=>requests[1].resolve(html),fragment('older-message',21,21,false));
    await page.waitForFunction(()=>document.querySelectorAll('#discussion-search-results a').length===2);
    assert.equal(await page.locator('#discussion-search-more').isVisible(),false);
  } finally {await browser.close();}
});

test('a later search wins over stale responses and closing prevents late results reopening', async () => {
  const {browser,page}=await setup();
  try {
    await page.locator('#discussion-search-query').fill('first');
    await page.locator('button[type=submit]').click();
    await page.locator('#discussion-search-query').fill('second');
    await page.locator('button[type=submit]').click();
    await page.evaluate(html=>requests[1].resolve(html),fragment('second',1,1,false));
    await page.evaluate(html=>requests[0].resolve(html),fragment('first',1,1,false));
    assert.match(await page.locator('#discussion-search-results').textContent(),/second/);
    assert.doesNotMatch(await page.locator('#discussion-search-results').textContent(),/first/);
    await page.locator('button[type=submit]').click();
    await page.locator('#discussion-search-close').click();
    await page.evaluate(html=>requests[2].resolve(html),fragment('late',1,1,false));
    assert.equal(await page.locator('#discussion-search-section').isVisible(),false);
    assert.equal(await page.locator('#discussion-search-results').textContent(),'');
    assert.equal(await page.locator('#discussion-search-query').evaluate(n=>n===document.activeElement),true);
  } finally {await browser.close();}
});

test('failure and invalid responses are visible and a cleared query closes results', async () => {
  const {browser,page}=await setup();
  try {
    await page.locator('#discussion-search-query').fill('projet');
    await page.locator('button[type=submit]').click();
    await page.evaluate(()=>requests[0].reject(new Error('offline')));
    await page.waitForFunction(()=>document.getElementById('discussion-search-status').textContent==='Search failed');
    await page.locator('button[type=submit]').click();
    await page.evaluate(()=>requests[1].resolve('<html>login expired</html>'));
    assert.equal(await page.locator('#discussion-search-status').textContent(),'Search failed');
    await page.locator('#discussion-search-query').fill('');
    assert.equal(await page.locator('#discussion-search-section').isVisible(),false);
  } finally {await browser.close();}
});
