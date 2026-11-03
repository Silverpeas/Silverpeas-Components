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
const labels=Object.fromEntries(properties.split('\n').filter(line=>line.startsWith('discussion.')&&line.includes('=')).map(line=>{const i=line.indexOf('=');return [line.slice(0,i).trim(),line.slice(i+1).trim()];}));
labels['GML.close']='Fermer';labels['GML.cancel']='Annuler';
const render=text=>text.replace(/<%[\s\S]*?%>/g,'').replace(/<view:setBundle[^>]*\/>/g,'').replace(/<c:url var="voteBase"[^>]*\/>/g,'')
    .replace(/<fmt:message key=['"]([^'"]+)['"]\s*\/>/g,(_,key)=>labels[key])
    .replace(/<c:url value=['"]([^'"]+)['"]\s*\/>/g,(_,value)=>value).replaceAll('${voteBase}','/Rdiscussion/discussion1/channels/main/');
const dialog=render(fs.readFileSync(path.join(assets,'vote-dialog.jsp'),'utf8'));
const source=fs.readFileSync(path.join(assets,'discussion.jsp'),'utf8');
const button=render(source.match(/<button id="discussion-add-vote"[\s\S]*?<\/button>/)[0]);
const options=(empty=false)=>`<div class="discussion-vote-options">${empty?'':'<option value="pollingStation1" data-anonymous="false">Vote équipe</option><option value="pollingStation2" data-anonymous="true">Vote anonyme</option>'}</div>`;
async function setup({root='',empty=false}={}) {
  const browser=await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
  const page=await browser.newPage({viewport:{width:1100,height:850}});
  await page.route('http://discussion.test/discussion/jsp/icons/**',route=>route.fulfill({contentType:'image/svg+xml',body:fs.readFileSync(path.join(assets,'icons/discussion-icons.svg'))}));
  await page.setContent(`<base href="http://discussion.test/"><main id="discussion" data-thread-root-id="${root}"><form class="discussion-composer"><textarea id="draft">Mon brouillon</textarea><input type="file" id="files"><div class="discussion-editor-tools"></div>${button}</form>${dialog}</main>`);
  await page.addStyleTag({content:'body{margin:0;font:16px Arial}'+fs.readFileSync(path.join(assets,'css/discussion.css'),'utf8')});
  await page.evaluate(html=>{
    window.calls=[];window.optionsHtml=html;window.failOptions=false;window.created=0;
    document.addEventListener('discussion:vote-created',()=>created++);
    window.sp={ajaxRequest:url=>{const request={url,params:{},post:false};return {
      byPostMethod(){request.post=true;return this;},withParam(k,v){request.params[k]=v;return this;},send(){
        calls.push(request);
        if(!request.post&&!window.deferOptions)return failOptions?Promise.reject(new Error('offline')):Promise.resolve({responseText:optionsHtml});
        return new Promise((resolve,reject)=>{request.resolve=html=>resolve({responseText:html});request.reject=()=>reject(new Error('offline'));});
      }
    };}};
  },options(empty));
  await page.addScriptTag({path:path.join(assets,'javaScript/discussion-votes.js')});
  await page.evaluate(()=>document.dispatchEvent(new Event('DOMContentLoaded')));
  return {browser,page};
}
async function open(page,selector='#discussion-add-vote') {
  await page.locator(selector).click();
  await page.waitForFunction(()=>!document.getElementById('discussion-vote-submit').disabled);
}
async function fill(page) {
  await page.locator('#discussion-vote-question').fill('Quel jour ?');
  await page.locator('#discussion-vote-answers').fill('Mardi\nJeudi');
}

test('native vote dialog offers one question and forces anonymity when the target requires it',async()=>{
  const {browser,page}=await setup();
  try {
    await open(page);
    assert.equal(await page.locator('.discussion-editor-tools #discussion-add-vote').count(),1);
    await page.locator('#discussion-vote-box').selectOption('pollingStation2');
    assert.equal(await page.locator('input[name=anonymous]').isChecked(),true);
    assert.equal(await page.locator('input[name=anonymous]').isDisabled(),true);
    await page.locator('#discussion-vote-box').selectOption('pollingStation1');
    assert.equal(await page.locator('input[name=anonymous]').isEnabled(),true);
    for(const width of [1100,390,320]) {
      await page.setViewportSize({width,height:850});
      const box=await page.locator('#discussion-vote-dialog').boundingBox();
      assert.ok(box.x>=0&&box.x+box.width<=width);
      assert.equal(await page.locator('#discussion-vote-dialog').evaluate(n=>n.scrollWidth<=n.clientWidth),true);
    }
    await page.locator('.discussion-vote-cancel').first().click();
    assert.equal(await page.evaluate(()=>calls.filter(c=>c.post).length),0);
  } finally {await browser.close();}
});

test('native creation posts once in the current thread and preserves draft and files',async()=>{
  const {browser,page}=await setup({root:'root'});
  try {
    await page.locator('#files').setInputFiles({name:'draft.txt',mimeType:'text/plain',buffer:Buffer.from('draft')});
    await open(page);
    await fill(page);
    await page.locator('input[name=multiple]').check();
    await page.locator('#discussion-vote-submit').click();
    await page.locator('#discussion-vote-form').evaluate(n=>n.dispatchEvent(new Event('submit',{cancelable:true,bubbles:true})));
    assert.equal(await page.evaluate(()=>calls.filter(c=>c.post).length),1);
    assert.deepEqual(await page.evaluate(()=>calls.find(c=>c.post).params),{vote:'pollingStation1',question:'Quel jour ?',answers:'Mardi\nJeudi',multiple:'true',anonymous:'false',threadRootId:'root'});
    await page.evaluate(()=>calls.find(c=>c.post).resolve('<span class="discussion-vote-created" data-vote-id="123"></span>'));
    await page.waitForFunction(()=>!document.getElementById('discussion-vote-dialog').open);
    assert.equal(await page.locator('#discussion-vote-status').textContent(),labels['discussion.voteCreated']);
    assert.equal(await page.locator('#draft').inputValue(),'Mon brouillon');
    assert.equal(await page.locator('#files').evaluate(n=>n.files.length),1);
    assert.equal(await page.evaluate(()=>created),1);
  } finally {await browser.close();}
});

test('invalid choices never post and native creation failures keep the form without retrying automatically',async()=>{
  const {browser,page}=await setup();
  try {
    await open(page);
    await page.locator('#discussion-vote-question').fill('Question');
    for(const text of ['Oui','Oui\noui','Oui\n',Array(11).fill('Réponse').join('\n')]) {
      await page.locator('#discussion-vote-answers').fill(text);
      await page.locator('#discussion-vote-form').evaluate(n=>n.dispatchEvent(new Event('submit',{cancelable:true,bubbles:true})));
      assert.equal(await page.evaluate(()=>calls.filter(c=>c.post).length),0);
    }
    await fill(page);
    await page.locator('#discussion-vote-submit').click();
    await page.evaluate(()=>calls.find(c=>c.post).reject());
    await page.locator('#discussion-vote-error').waitFor({state:'visible'});
    assert.equal(await page.locator('#discussion-vote-question').inputValue(),'Quel jour ?');
    assert.equal(await page.locator('#discussion-vote-answers').inputValue(),'Mardi\nJeudi');
    assert.equal(await page.evaluate(()=>calls.filter(c=>c.post).length),1);
  } finally {await browser.close();}
});

test('missing authorized Vote blocks creation and stale option responses cannot overwrite a reopened dialog',async()=>{
  const {browser,page}=await setup({empty:true});
  try {
    await page.locator('#discussion-add-vote').click();
    await page.waitForFunction(()=>document.getElementById('discussion-vote-loading').textContent.includes('Aucune'));
    assert.equal(await page.locator('#discussion-vote-submit').isDisabled(),true);
    await page.keyboard.press('Escape');
    await page.evaluate(()=>{deferOptions=true;});
    await page.locator('#discussion-add-vote').click();
    await page.keyboard.press('Escape');
    await page.locator('#discussion-add-vote').click();
    await page.evaluate(html=>calls[2].resolve(html),options());
    await page.waitForFunction(()=>!document.getElementById('discussion-vote-submit').disabled);
    await fill(page);
    await page.evaluate(()=>calls[1].resolve('<div class="discussion-vote-options"></div>'));
    assert.equal(await page.locator('#discussion-vote-box option').count(),2);
    assert.equal(await page.locator('#discussion-vote-question').inputValue(),'Quel jour ?');
  } finally {await browser.close();}
});
