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
const test=require('node:test');
const assert=require('node:assert/strict');
const path=require('node:path');
const fs=require('node:fs');
const {chromium}=require('playwright');
const assets=path.join(__dirname,'../../main/webapp/discussion/jsp');
const props=fs.readFileSync(path.join(__dirname,'../../../../discussion-configuration/src/main/config/properties/org/silverpeas/components/discussion/multilang/DiscussionBundle_fr.properties'),'utf8');
const labels=Object.fromEntries(props.split('\n').filter(line=>line.startsWith('discussion.')&&line.includes('=')).map(line=>{const i=line.indexOf('=');return [line.slice(0,i).trim(),line.slice(i+1).trim()];}));
labels['GML.cancel']='Annuler';
const dialog=fs.readFileSync(path.join(assets,'event-delete-dialog.jsp'),'utf8').replace(/<%[\s\S]*?%>/g,'').replace(/<view:setBundle[^>]*\/>/g,'').replace(/<fmt:message key=['"]([^'"]+)['"]\s*\/>/g,(_,key)=>labels[key]);
const row=(text='Original')=>`<article id="message-1" class="discussion-message" data-sequence="1"><p class="discussion-body">${text}</p><form class="discussion-delete" action="/channels/main/messages/m1/delete" data-deletion-info="/channels/main/messages/m1/deletion"><input name="version" type="hidden" value="3"><input name="deleteEvent" type="hidden" value="false"><button type="submit">Supprimer</button></form></article>`;
const state=({linked=true,allowed=true,recurrent=false}={})=>`<span class="discussion-event-deletion" data-linked="${linked}" data-can-delete-event="${allowed}" data-recurrent="${recurrent}" data-title="Réunion &lt;img src=x&gt;"></span>`;
async function setup({linked=true,allowed=true,recurrent=false,live=false}={}) {
  const browser=await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
  const page=await browser.newPage({viewport:{width:1100,height:850}});
  await page.setContent(`<base href="http://discussion.test/"><main id="discussion" data-instance-id="discussion1" ${live?'data-updates="/updates"':''}><div id="discussion-scroll"><div id="discussion-history"><div class="discussion-page">${row()}</div></div></div><span class="discussion-unread-count">0</span><p id="discussion-live-error" hidden>Erreur</p>${dialog}</main>`);
  await page.addStyleTag({content:'body{margin:0;font:16px Arial}'+fs.readFileSync(path.join(assets,'css/discussion.css'),'utf8')});
  await page.evaluate(({html,updated})=>{
    window.infoHtml=html;window.updatedRow=updated;window.calls=[];window.posts=[];window.handlers=new Map();window.change=false;
    window.spServerEventSource={isEnabled:()=>true,addEventListener:(name,fn,id)=>handlers.set(id,{name,fn}),removeEventListener:()=>{}};
    document.addEventListener('submit',event=>{if(event.target.matches('.discussion-delete')&&!event.defaultPrevented){event.preventDefault();posts.push(Object.fromEntries(new FormData(event.target)));}});
    window.sp={ajaxRequest:url=>{
      const call={url,params:{}};
      return {withParam(k,v){call.params[k]=v;return this;},send(){
        if(url==='/updates'){window.updates=(window.updates||0)+1;return Promise.resolve({responseText:`<div class="discussion-page" data-unread-count="0" data-sync-sequence="1">${window.change?updatedRow:''}</div>`});}
        calls.push(call);
        if(infoHtml===null)return Promise.reject(new Error('offline'));
        return Promise.resolve({responseText:infoHtml});
      }};
    }};
  },{html:state({linked,allowed,recurrent}),updated:row('Updated')});
  await page.addScriptTag({path:path.join(assets,'javaScript/discussion-deletion.js')});
  if(live)await page.addScriptTag({path:path.join(assets,'javaScript/discussion-live.js')});
  await page.evaluate(()=>document.dispatchEvent(new Event('DOMContentLoaded')));
  return {browser,page};
}
async function open(page) {
  await page.locator('.discussion-delete button').click();
  await page.waitForFunction(()=>document.getElementById('discussion-delete-event-dialog').open);
}

test('linked event deletion offers cancellation, message only and both without deleting on opening',async()=>{
  const {browser,page}=await setup();
  try {
    await open(page);
    assert.equal(await page.evaluate(()=>posts.length),0);
    assert.equal(await page.locator('#discussion-delete-event-title').textContent(),'Réunion <img src=x>');
    assert.equal(await page.locator('#discussion-delete-event-dialog img').count(),0);
    assert.equal(await page.evaluate(()=>calls[0].params.version),'3');
    await page.locator('#discussion-delete-event-cancel').click();
    await page.waitForFunction(()=>!document.querySelector('.discussion-delete button').disabled);
    assert.equal(await page.evaluate(()=>posts.length),0);
    await open(page);
    await page.locator('#discussion-delete-message-only').click();
    await page.waitForFunction(()=>posts.length===1);
    assert.deepEqual(await page.evaluate(()=>posts),[{version:'3',deleteEvent:'false'}]);
  } finally {await browser.close();}
});

test('delete both sends an explicit choice while recurring occurrences are clearly warned about',async()=>{
  const {browser,page}=await setup({recurrent:true});
  try {
    await open(page);
    assert.equal(await page.locator('#discussion-delete-event-recurrent').isVisible(),true);
    await page.locator('#discussion-delete-message-and-event').click();
    await page.waitForFunction(()=>posts.length===1);
    assert.deepEqual(await page.evaluate(()=>posts),[{version:'3',deleteEvent:'true'}]);
    assert.equal(await page.locator('#discussion-delete-event-dialog').evaluate(n=>n.open),false);
  } finally {await browser.close();}
});

test('read-only event rights hide the combined deletion choice and ordinary messages keep their native delete action',async()=>{
  const {browser,page}=await setup({allowed:false});
  try {
    await open(page);
    assert.equal(await page.locator('#discussion-delete-message-and-event').isVisible(),false);
    assert.equal(await page.locator('#discussion-delete-event-denied').isVisible(),true);
    await page.locator('#discussion-delete-message-only').click();
    await page.waitForFunction(()=>posts.length===1);
    assert.equal(await page.evaluate(()=>posts[0].deleteEvent),'false');
  } finally {await browser.close();}
  const second=await setup({linked:false});
  try {
    await second.page.locator('.discussion-delete button').click();
    await second.page.waitForFunction(()=>posts.length===1);
    assert.equal(await second.page.locator('#discussion-delete-event-dialog').evaluate(n=>n.open),false);
    assert.equal(await second.page.evaluate(()=>posts[0].deleteEvent),'false');
  } finally {await second.browser.close();}
});

test('failed checks never delete anything and Escape cancels without submitting',async()=>{
  const {browser,page}=await setup();
  try {
    await page.evaluate(()=>{window.infoHtml=null;});
    await page.locator('.discussion-delete button').click();
    await page.locator('.discussion-delete-error').waitFor();
    assert.equal(await page.evaluate(()=>posts.length),0);
    assert.equal(await page.locator('.discussion-delete button').isEnabled(),true);
    await page.evaluate(html=>{window.infoHtml=html;},state());
    await open(page);
    await page.keyboard.press('Escape');
    await page.waitForFunction(()=>!document.querySelector('.discussion-delete button').disabled);
    assert.equal(await page.evaluate(()=>posts.length),0);
    assert.equal(await page.locator('.discussion-delete button').evaluate(n=>n===document.activeElement),true);
  } finally {await browser.close();}
});

test('live updates preserve the message until the confirmation is dismissed and the dialog fits a narrow iframe',async()=>{
  const {browser,page}=await setup({live:true});
  try {
    await open(page);
    await page.evaluate(()=>{window.change=true;for(const h of handlers.values()){if(h.name==='DISCUSSION_CHANGED')h.fn({data:JSON.stringify({instanceId:'discussion1'})});}});
    await page.evaluate(()=>new Promise(resolve=>setTimeout(resolve,50)));
    assert.equal(await page.locator('.discussion-body').textContent(),'Original');
    for(const width of [1100,390,320]) {
      await page.setViewportSize({width,height:850});
      const box=await page.locator('#discussion-delete-event-dialog').boundingBox();
      assert.ok(box.x>=0&&box.x+box.width<=width);
      assert.equal(await page.locator('#discussion-delete-event-dialog').evaluate(n=>n.scrollWidth<=n.clientWidth),true);
    }
    await page.locator('#discussion-delete-event-cancel').click();
    await page.waitForFunction(()=>!document.querySelector('.discussion-delete button').disabled);
    await page.evaluate(()=>{document.activeElement.blur();window.getSelection().removeAllRanges();});
    await page.waitForFunction(()=>document.querySelector('.discussion-body').textContent==='Updated');
    assert.equal(await page.evaluate(()=>posts.length),0);
  } finally {await browser.close();}
});
