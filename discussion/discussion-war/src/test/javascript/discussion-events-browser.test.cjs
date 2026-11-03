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
const assets = path.join(__dirname,'../../main/webapp/discussion/jsp');
const properties=fs.readFileSync(path.join(__dirname,'../../../../discussion-configuration/src/main/config/properties/org/silverpeas/components/discussion/multilang/DiscussionBundle_fr.properties'),'utf8');
const labels=Object.fromEntries(properties.split('\n').filter(line=>line.startsWith('discussion.')&&line.includes('=')).map(line=>{
  const index=line.indexOf('=');return [line.slice(0,index).trim(),line.slice(index+1).trim().replace(/\\u([a-fA-F0-9]{4})/g,(_,digits)=>String.fromCodePoint(Number.parseInt(digits,16)))];
}));
labels['GML.close']='Fermer';labels['GML.cancel']='Annuler';
const render = text => text.replace(/<%[\s\S]*?%>/g,'').replace(/<view:setBundle[^>]*\/>/g,'').replace(/<c:url var="eventBase"[^>]*\/>/g,'')
    .replace(/<fmt:message key=['"]([^'"]+)['"]\s*\/>/g,(_,key)=>labels[key])
    .replace(/<c:url value=['"]([^'"]+)['"]\s*\/>/g,(_,value)=>value)
    .replaceAll('${eventBase}','/Rdiscussion/discussion1/channels/main/');
const dialog=render(fs.readFileSync(path.join(assets,'event-dialog.jsp'),'utf8'));
const source=fs.readFileSync(path.join(assets,'discussion.jsp'),'utf8');
const button=render(source.match(/<button id="discussion-add-event"[\s\S]*?<\/button>/)[0]);
const status=render(source.match(/<p id="discussion-event-status"[\s\S]*?<\/p>/)[0]);
const choices=(empty=false)=>`<div class="discussion-event-options" data-zone="Europe/Paris" data-start="2026-11-04T10:00" data-end="2026-11-04T11:00" data-invite-count="12">${empty?'':'<option value="calendar1">Agenda équipe</option><option value="calendar2">Agenda projet</option>'}</div>`;
async function setup({width=1100,root='',empty=false}={}) {
  const browser=await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
  const page=await browser.newPage({viewport:{width,height:850},timezoneId:'America/New_York'});
  await page.route('http://discussion.test/discussion/jsp/icons/**',route=>route.fulfill({contentType:'image/svg+xml',body:fs.readFileSync(path.join(assets,'icons/discussion-icons.svg'))}));
  await page.setContent(`<base href="http://discussion.test/"><main id="discussion" data-thread-root-id="${root}"><form class="discussion-composer"><textarea id="discussion-body" name="body">Un brouillon conservé</textarea><input type="file" name="files"><div class="discussion-editor-tools"></div>${button}${status}</form>${dialog}</main>`);
  await page.addStyleTag({content:'body{margin:0;font:16px Arial}'+fs.readFileSync(path.join(assets,'css/discussion.css'),'utf8')});
  await page.evaluate(html=>{
    window.created=0;document.addEventListener('discussion:event-created',()=>created++);
    window.calls=[];window.optionsHtml=html;window.failOptions=false;
    window.sp={ajaxRequest:url=>{
      const request={url,params:{},post:false};
      return {byPostMethod(){request.post=true;return this;},withParam(k,v){request.params[k]=v;return this;},send(){
        calls.push(request);
        if(!request.post&&!window.deferOptions){return failOptions?Promise.reject(new Error('offline')):Promise.resolve({responseText:optionsHtml});}
        return new Promise((resolve,reject)=>{request.resolve=html=>resolve({responseText:html});request.reject=()=>reject(new Error('offline'));});
      }};
    }};
  },choices(empty));
  await page.addScriptTag({path:path.join(assets,'javaScript/discussion-events.js')});
  await page.evaluate(()=>document.dispatchEvent(new Event('DOMContentLoaded')));
  return {browser,page};
}
async function open(page) {
  await page.locator('#discussion-add-event').click();
  await page.waitForFunction(()=>!document.getElementById('discussion-event-fields').disabled);
}

test('the actual JSP calendar icon opens the native dialog with authorized Almanachs and server time zone defaults',async()=>{
  const {browser,page}=await setup();
  try {
    assert.equal(await page.locator('.discussion-editor-tools #discussion-add-event').isVisible(),true);
    assert.equal(await page.locator('#discussion-add-event').getAttribute('title'),'Créer un événement');
    await open(page);
    assert.equal(await page.locator('#discussion-event-calendar option').count(),2);
    assert.equal(await page.locator('#discussion-event-zone').textContent(),'Europe/Paris');
    assert.equal(await page.locator('#discussion-event-start').inputValue(),'2026-11-04T10:00');
    assert.equal(await page.locator('#discussion-event-end').inputValue(),'2026-11-04T11:00');
    assert.match(await page.locator('#discussion-event-invites').textContent(),/12 autres membres/);
    assert.equal(await page.locator('#discussion-event-title').evaluate(n=>n===document.activeElement),true);
    await page.keyboard.press('Escape');
    assert.equal(await page.locator('#discussion-event-dialog').evaluate(n=>n.open),false);
    assert.equal(await page.locator('#discussion-add-event').evaluate(n=>n===document.activeElement),true);
  } finally {await browser.close();}
});

test('creation posts once to the current thread, preserves the message and attachment draft and refreshes the discussion',async()=>{
  const {browser,page}=await setup({root:'thread-root'});
  try {
    await page.locator('.discussion-composer input[type=file]').setInputFiles({name:'draft.txt',mimeType:'text/plain',buffer:Buffer.from('draft')});
    await open(page);
    await page.locator('#discussion-event-title').fill('Réunion de projet');
    await page.locator('#discussion-event-calendar').selectOption('calendar2');
    await page.locator('#discussion-event-description').fill('Préparer la prochaine étape');
    await page.locator('#discussion-event-submit').click();
    await page.evaluate(()=>document.getElementById('discussion-event-form').dispatchEvent(new Event('submit',{bubbles:true,cancelable:true})));
    assert.equal(await page.evaluate(()=>calls.filter(c=>c.post).length),1);
    assert.deepEqual(await page.evaluate(()=>calls.find(c=>c.post).params),{calendar:'calendar2',title:'Réunion de projet',start:'2026-11-04T10:00',end:'2026-11-04T11:00',description:'Préparer la prochaine étape',threadRootId:'thread-root'});
    assert.ok((await page.evaluate(()=>calls.find(c=>c.post).url)).endsWith('/channels/main/events'));
    await page.keyboard.press('Escape');
    assert.equal(await page.locator('#discussion-event-dialog').evaluate(n=>n.open),true);
    await page.evaluate(()=>calls.find(c=>c.post).resolve('<span class="discussion-event-created" data-event-id="event1"></span>'));
    await page.waitForFunction(()=>!document.getElementById('discussion-event-dialog').open);
    assert.equal(await page.locator('#discussion-event-status').isVisible(),true);
    assert.equal(await page.evaluate(()=>created),1);
    assert.equal(await page.locator('#discussion-body').inputValue(),'Un brouillon conservé');
    assert.equal(await page.locator('.discussion-composer input[type=file]').evaluate(n=>n.files[0].name),'draft.txt');
  } finally {await browser.close();}
});

test('invalid periods never post and a failed creation retains the event form without an automatic retry',async()=>{
  const {browser,page}=await setup();
  try {
    await open(page);
    await page.locator('#discussion-event-title').fill('Réunion');
    await page.locator('#discussion-event-end').fill('2026-11-04T09:00');
    await page.locator('#discussion-event-submit').click();
    assert.equal(await page.evaluate(()=>calls.filter(c=>c.post).length),0);
    assert.equal(await page.locator('#discussion-event-end').evaluate(n=>n.validationMessage),'La fin doit être postérieure au début.');
    await page.locator('#discussion-event-end').fill('2026-11-04T12:00');
    await page.locator('#discussion-event-submit').click();
    await page.evaluate(()=>calls.find(c=>c.post).reject());
    await page.waitForFunction(()=>!document.getElementById('discussion-event-error').hidden);
    assert.equal(await page.locator('#discussion-event-title').inputValue(),'Réunion');
    assert.equal(await page.locator('#discussion-event-fields').isEnabled(),true);
    assert.equal(await page.evaluate(()=>calls.filter(c=>c.post).length),1);
    assert.equal(await page.evaluate(()=>created),0);
  } finally {await browser.close();}
});

test('missing writable Almanach explains the limitation and option-loading errors can be retried',async()=>{
  const {browser,page}=await setup({empty:true});
  try {
    await page.locator('#discussion-add-event').click();
    await page.waitForFunction(()=>document.getElementById('discussion-event-loading').textContent.includes('Aucun Almanach'));
    assert.equal(await page.locator('#discussion-event-submit').isDisabled(),true);
    await page.locator('.discussion-event-cancel').last().click();
    await page.evaluate(()=>{window.failOptions=true;});
    await page.locator('#discussion-add-event').click();
    await page.waitForFunction(()=>!document.getElementById('discussion-event-error').hidden);
    assert.equal(await page.locator('#discussion-event-retry').isVisible(),true);
    await page.evaluate(html=>{window.failOptions=false;window.optionsHtml=html;},choices());
    await page.locator('#discussion-event-retry').click();
    await page.waitForFunction(()=>!document.getElementById('discussion-event-submit').disabled);
  } finally {await browser.close();}
});

test('late option responses cannot overwrite a reopened dialog and date defaults keep wall-clock days across browser zones',async()=>{
  const {browser,page}=await setup();
  try {
    await page.evaluate(()=>{window.deferOptions=true;});
    await page.locator('#discussion-add-event').click();
    await page.locator('.discussion-event-cancel').last().click();
    await page.locator('#discussion-add-event').click();
    await page.evaluate(html=>calls[1].resolve(html),choices());
    await page.waitForFunction(()=>!document.getElementById('discussion-event-fields').disabled);
    await page.evaluate(()=>calls[0].resolve('<div class="discussion-event-options" data-zone="UTC"></div>'));
    assert.equal(await page.locator('#discussion-event-zone').textContent(),'Europe/Paris');
    await page.locator('#discussion-event-start').fill('2026-11-05T23:30');
    assert.equal(await page.locator('#discussion-event-end').inputValue(),'2026-11-06T00:30');
    for (const width of [1100,768,390,320]) {
      await page.setViewportSize({width,height:850});
      const box=await page.locator('#discussion-event-dialog').boundingBox();
      assert.ok(box.x>=0&&box.x+box.width<=width,'dialog fits '+width);
      assert.equal(await page.locator('#discussion-event-dialog').evaluate(n=>n.scrollWidth<=n.clientWidth),true,'no horizontal scroll '+width);
    }
  } finally {await browser.close();}
});
