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
const fs = require('node:fs');
const path = require('node:path');
const {chromium} = require('playwright');
const assets = path.join(__dirname, '../../main/webapp/discussion/jsp');
const source = fs.readFileSync(path.join(assets,'discussion.jsp'),'utf8');
const properties = fs.readFileSync(path.join(__dirname,'../../../../discussion-configuration/src/main/config/properties/org/silverpeas/components/discussion/multilang/DiscussionBundle_fr.properties'),'utf8');
const labels = Object.fromEntries(properties.split('\n').filter(line=>line.startsWith('discussion.')&&line.includes('=')).map(line=>{
  const separator=line.indexOf('=');
  return [line.slice(0,separator).trim(),line.slice(separator+1).trim().replace(/\\u([a-fA-F0-9]{4})/g,(_,digits)=>String.fromCodePoint(Number.parseInt(digits,16)))];
}));
const render = text => text.replace(/<fmt:message key=['"]([^'"]+)['"]\s*\/>/g,(_,key)=>labels[key])
    .replace(/<c:url value=['"]([^'"]+)['"]\s*\/>/g,(_,value)=>value)
    .replace(/<c:out value="\$\{messagePage.unreadCount}"\s*\/>/g,'0')
    .replaceAll('${base}','/Rdiscussion/discussion1/channels/main/');
const form = render(source.match(/<form id="discussion-search"[\s\S]*?<\/form>/)[0]);
const reading = render(source.match(/<div class="discussion-reading"[\s\S]*?<\/div>/)[0]);
const fragment = (id,next=20,more=true) => `<div class="discussion-search-page" data-next-offset="${next}" data-total="21" data-has-more="${more}"><a href="#${id}">${id}</a></div>`;
async function setup(width=1280) {
  const browser=await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
  const page=await browser.newPage({viewport:{width,height:800}});
  await page.route('http://discussion.test/discussion/jsp/icons/**',route=>route.fulfill({contentType:'image/svg+xml',body:fs.readFileSync(path.join(assets,'icons/discussion-icons.svg'))}));
  await page.setContent(`<base href="http://discussion.test/"><main id="discussion"><header class="discussion-header"><div class="discussion-heading"><h1>Discussion sur l’IA</h1></div>${form}${reading}</header><textarea id="draft">Brouillon conservé</textarea>${render(source.match(/<details id="discussion-search-section"[\s\S]*?<\/details>/)[0])}</main>`);
  await page.addStyleTag({content:'body{margin:0;font:16px Arial}'+fs.readFileSync(path.join(assets,'css/discussion.css'),'utf8')});
  await page.evaluate(()=>{
    window.requests=[];
    window.sp={ajaxRequest:url=>{
      const request={url,params:{}};
      return {withParam(name,value){request.params[name]=value;return this;},send(){
        requests.push(request);
        return new Promise(resolve=>{request.resolve=html=>resolve({responseText:html});});
      }};
    }};
  });
  await page.addScriptTag({path:path.join(assets,'javaScript/discussion-header.js')});
  await page.addScriptTag({path:path.join(assets,'javaScript/discussion-search.js')});
  await page.evaluate(()=>document.dispatchEvent(new Event('DOMContentLoaded')));
  return {browser,page};
}

test('real JSP unread icon appears for live counts and disappears after visible messages are read',async()=>{
  const {browser,page}=await setup();
  try {
    assert.equal(await page.locator('.discussion-reading').isVisible(),false);
    await page.locator('.discussion-unread-count').evaluate(n=>{n.textContent='3';});
    await page.waitForFunction(()=>!document.querySelector('.discussion-reading').hidden);
    assert.equal(await page.locator('.discussion-reading').getAttribute('title'),'3 messages non lus');
    assert.equal(await page.locator('.discussion-reading').getAttribute('aria-label'),'3 messages non lus');
    assert.equal((await page.locator('.discussion-reading').textContent()).trim(),'3');
    assert.equal(await page.locator('.discussion-reading use').getAttribute('href'),'/discussion/jsp/icons/discussion-icons.svg#unread');
    await page.locator('.discussion-unread-count').evaluate(n=>{n.textContent='0';});
    await page.waitForFunction(()=>document.querySelector('.discussion-reading').hidden);
    await page.evaluate(()=>{dispatchEvent(new Event('pagehide'));document.querySelector('.discussion-unread-count').textContent='2';dispatchEvent(new Event('pageshow'));});
    assert.equal(await page.locator('.discussion-reading').isVisible(),true);
    assert.equal(await page.locator('.discussion-reading').getAttribute('title'),'2 messages non lus');
  } finally {await browser.close();}
});

test('scope picker has keyboard accessible radios, a contextual tooltip and closes on Escape or outside click',async()=>{
  const {browser,page}=await setup();
  try {
    const scope=page.locator('#discussion-search-scope');
    const summary=scope.locator('summary');
    assert.equal(await summary.getAttribute('title'),'Rechercher dans : Ce canal');
    await summary.focus();
    await page.keyboard.press('Enter');
    assert.equal(await scope.evaluate(n=>n.open),true);
    await scope.locator('input[value=false]').focus();
    await page.keyboard.press('ArrowDown');
    assert.equal(await scope.locator('input[value=true]').isChecked(),true);
    assert.equal(await scope.evaluate(n=>n.open),false);
    assert.equal(await summary.getAttribute('title'),'Rechercher dans : Tous les canaux');
    assert.equal(await scope.evaluate(n=>n.classList.contains('is-all-channels')),true);
    assert.equal(await summary.evaluate(n=>n===document.activeElement),true);
    await summary.click();
    await page.keyboard.press('Escape');
    assert.equal(await scope.evaluate(n=>n.open),false);
    await summary.click();
    await page.locator('h1').click();
    assert.equal(await scope.evaluate(n=>n.open),false);
    assert.equal(await page.evaluate(()=>requests.length),0);
  } finally {await browser.close();}
});

test('changing scope restarts native search and pagination without mixing stale responses or losing the draft',async()=>{
  const {browser,page}=await setup();
  try {
    await page.locator('#discussion-search-query').fill('projet');
    await page.locator('#discussion-search button[type=submit]').click();
    assert.equal(await page.evaluate(()=>requests[0].params.allChannels),'false');
    await page.locator('#discussion-search-scope > summary').click();
    await page.locator('#discussion-search-scope label').nth(1).click();
    assert.deepEqual(await page.evaluate(()=>requests[1].params),{q:'projet',offset:'0',allChannels:'true'});
    await page.evaluate(html=>requests[1].resolve(html),fragment('tous-les-canaux'));
    await page.evaluate(html=>requests[0].resolve(html),fragment('ancien-canal'));
    await page.waitForFunction(()=>document.getElementById('discussion-search-results').textContent.includes('tous-les-canaux'));
    assert.doesNotMatch(await page.locator('#discussion-search-results').textContent(),/ancien-canal/);
    await page.locator('#discussion-search-more').click();
    assert.deepEqual(await page.evaluate(()=>requests[2].params),{q:'projet',offset:'20',allChannels:'true'});
    await page.locator('#discussion-search-scope > summary').click();
    await page.locator('#discussion-search-scope label').first().click();
    assert.deepEqual(await page.evaluate(()=>requests[3].params),{q:'projet',offset:'0',allChannels:'false'});
    assert.equal(await page.locator('#draft').inputValue(),'Brouillon conservé');
  } finally {await browser.close();}
});

test('real compact header and its open picker fit desktop and narrow iframe widths',async()=>{
  const {browser,page}=await setup();
  try {
    for (const width of [1280,768,390,320]) {
      await page.setViewportSize({width,height:800});
      await page.locator('.discussion-unread-count').evaluate(n=>{n.textContent='15';});
      await page.locator('#discussion-search-scope > summary').click();
      const box=await page.locator('.discussion-search-scope-menu').boundingBox();
      assert.ok(box.x>=0&&box.x+box.width<=width,'picker fits '+width);
      assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),true,'header fits '+width);
      await page.keyboard.press('Escape');
    }
  } finally {await browser.close();}
});


test('real search results have a compact labelled close icon beside the heading',async()=>{
  const {browser,page}=await setup();
  try {
    await page.locator('#discussion-search-query').fill('agent');
    await page.locator('#discussion-search').evaluate(n=>n.requestSubmit());
    await page.evaluate(html=>requests[0].resolve(html),fragment('result'));
    const close=page.locator('#discussion-search-close');
    await close.waitFor({state:'visible'});
    assert.equal(await close.getAttribute('aria-label'),'Fermer la recherche');
    assert.equal(await close.getAttribute('title'),'Fermer la recherche');
    assert.equal((await close.textContent()).trim(),'');
    assert.equal(await close.locator('use').getAttribute('href'),'/discussion/jsp/icons/discussion-icons.svg#close');
    const button=await close.boundingBox();
    const heading=await page.locator('#discussion-search-section > summary').boundingBox();
    assert.ok(button.width<=36&&button.height<=36);
    assert.ok(button.y>=heading.y&&button.y+button.height<=heading.y+heading.height);
    await close.focus();
    await page.keyboard.press('Enter');
    assert.equal(await page.locator('#discussion-search-section').isVisible(),false);
    assert.equal(await page.locator('#draft').inputValue(),'Brouillon conservé');
  } finally {await browser.close();}
});
