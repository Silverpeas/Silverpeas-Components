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
const row = (id, thread=false) => `<article id="message-${id}" class="discussion-message" data-sequence="${Number(id.replace('root',''))||1}"><div class="discussion-message-content"><strong>Auteur</strong><p>Un message de discussion suffisamment long pour vérifier que le changement de largeur conserve la position de lecture.</p>${thread?'':`<a class="discussion-thread-link" href="/Rdiscussion/discussion1/threads/${id}">Répondre</a>`}</div></article>`;
function html(root='',live=false) {
 const rows=root?row(root,true):Array.from({length:35},(_,i)=>row('root'+(i+1))).join('');
 return `<!doctype html><html><head><meta charset="utf-8"><link rel="stylesheet" href="/assets/css/discussion.css"><style>body{margin:0;font:16px Arial}.breadcrumb{height:50px}textarea{box-sizing:border-box;max-width:100%}</style>
 <script>
 if(window===window.top){window.calls=[];window.handlers=new Map();window.spServerEventSource={isEnabled:()=>true,addEventListener:(type,fn,id)=>{if(!id)throw Error('Missing listener id');handlers.set(id,{type,fn});},removeEventListener:(type,fn)=>{for(const [id,v]of handlers){if(v.type===type&&v.fn===fn)handlers.delete(id);}}};window.emit=()=>{for(const v of handlers.values()){if(v.type==='DISCUSSION_CHANGED')v.fn({data:JSON.stringify({instanceId:'discussion1'})});}};}
 window.sp={ajaxRequest:url=>{const params={};const api={withParam:(k,v)=>{params[k]=v;return api;},send:async()=>{window.top.calls.push({scope:'${root||'main'}',params});return {responseText:'<div class="discussion-page" data-sync-sequence="35" data-last-sequence="35" data-unread-count="0"></div>'};}};return api;}};
 </script>
 <script src="/assets/javaScript/discussion-threads.js" defer></script><script src="/assets/javaScript/discussion-layout.js" defer></script>${live?'<script src="/assets/javaScript/discussion-live.js" defer></script>':''}</head><body><div class="breadcrumb">Ressources &gt; Discussion</div><div class="native-frame"><main id="discussion" data-instance-id="discussion1" data-thread-root-id="${root}" data-updates="/updates">
 <header class="discussion-header"><div class="discussion-heading"><h1>Discussion</h1></div><span class="discussion-unread-count">0</span></header>
 <div class="discussion-workspace"><div class="discussion-stream"><div class="discussion-scroll" id="discussion-scroll"><section id="discussion-history"><div class="discussion-page">${rows}</div></section></div><p id="discussion-live-error" hidden>Error</p><form class="discussion-composer"><input type="file" id="attachment"><textarea id="draft">${root?'Brouillon du fil':'Brouillon principal conservé'}</textarea><button type="button">Publier</button></form></div>
 <aside class="discussion-sidebar"><div id="discussion-sidebar-accordions"><details name="sidebar" id="people" open><summary>Participants</summary><p>Alice</p></details><details name="sidebar" id="publications"><summary>Publications</summary><p>Projet</p></details></div>
 <section id="discussion-thread-panel" class="discussion-thread-panel" aria-label="Fil de discussion" hidden><header class="discussion-thread-panel-header"><h2>Fil de discussion</h2><button id="discussion-thread-close" type="button">Fermer le fil</button></header><p id="discussion-thread-status" data-loading="Chargement" data-error="Erreur" hidden></p><a id="discussion-thread-fallback" hidden>Ouvrir le fil</a><iframe id="discussion-thread-frame" class="discussion-thread-frame" title="Fil"></iframe></section>
 </aside></div></main></div></body></html>`;
}
async function setup({width=1280,live=false,bad=false}={}) {
 const browser=await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
 const page=await browser.newPage({viewport:{width,height:900}});
 await page.route('http://discussion.test/**',route=>{
  const url=new URL(route.request().url());
  if(url.pathname.startsWith('/assets/'))return route.fulfill({contentType:url.pathname.endsWith('.js')?'application/javascript':'text/css',body:fs.readFileSync(path.join(assets,url.pathname.slice('/assets/'.length)))});
  const root=url.pathname.includes('/threads/')?url.pathname.split('/').pop():'';
  return route.fulfill({contentType:'text/html',body:root&&bad?'<h1>Session expirée</h1>':html(root,live)});
 });
 await page.goto('http://discussion.test/main');
 return {browser,page};
}
async function openThread(page,id='root12') {
 await page.locator('#message-'+id+' .discussion-thread-link').click();
 await page.waitForFunction(()=>document.getElementById('discussion-thread-frame').getAttribute('aria-busy')==='false');
 return page.frames().find(f=>f.url().includes('/threads/'));
}

test('opening and closing a thread preserves main draft, reading anchor and accordion state',async()=>{
 const {browser,page}=await setup();
 try{
  await page.locator('#publications > summary').click();
  await page.locator('#attachment').setInputFiles({name:'note.txt',mimeType:'text/plain',buffer:Buffer.from('Draft attachment')});
  await page.locator('#message-root12').scrollIntoViewIfNeeded();
  const before=await page.evaluate(()=>{const top=document.querySelector('.discussion-header').getBoundingClientRect().bottom;const n=[...document.querySelectorAll('.discussion-message')].find(n=>n.getBoundingClientRect().bottom>top);return {id:n.id,top:n.getBoundingClientRect().top};});
  const frame=await openThread(page);
  await page.evaluate(()=>new Promise(r=>requestAnimationFrame(()=>requestAnimationFrame(r))));
  assert.equal(page.url(),'http://discussion.test/main');
  assert.equal(await page.locator('#draft').inputValue(),'Brouillon principal conservé');
  assert.equal(await page.locator('#attachment').evaluate(n=>n.files[0].name),'note.txt');
  assert.equal(await page.locator('#discussion-sidebar-accordions').isVisible(),false);
  assert.equal(await page.locator('.discussion-sidebar').evaluate(n=>n.getBoundingClientRect().width)>350,true);
  const after=await page.locator('#'+before.id).boundingBox();
  assert.ok(Math.abs(after.y-before.top)<4,'Main reading anchor preserved: '+before.top+' -> '+after.y);
  assert.equal(await frame.locator('.breadcrumb').isVisible(),false);
  assert.equal(await frame.locator('.discussion-header').isVisible(),false);
  await frame.locator('#draft').fill('Une réponse pas encore envoyée');
  await page.locator('#discussion-thread-close').click();
  assert.equal(await page.locator('#discussion-sidebar-accordions').isVisible(),true);
  assert.equal(await page.locator('#publications').evaluate(n=>n.open),true);
  assert.equal(await page.locator('#people').evaluate(n=>n.open),false);
  const again=await openThread(page);
  assert.equal(await again.locator('#draft').inputValue(),'Une réponse pas encore envoyée');
  assert.equal(await page.locator('#draft').inputValue(),'Brouillon principal conservé');
 }finally{await browser.close();}
});

test('on mobile the thread fills the viewport and main content is inert until closing',async()=>{
 const {browser,page}=await setup({width:390});
 try{
  await openThread(page,'root1');
  const box=await page.locator('.discussion-sidebar').boundingBox();
  assert.ok(box.x>=0&&box.x+box.width<=391&&box.height>=898);
  assert.equal(await page.locator('#discussion-thread-panel').getAttribute('aria-modal'),'true');
  assert.equal(await page.locator('.discussion-stream').evaluate(n=>n.inert),true);
  assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),true);
  await page.locator('#discussion-thread-close').click();
  assert.equal(await page.locator('.discussion-stream').evaluate(n=>n.inert),false);
  assert.equal(await page.locator('#discussion-thread-panel').isVisible(),false);
 }finally{await browser.close();}
});

test('thread loading failure offers its native dedicated view and remains closable',async()=>{
 const {browser,page}=await setup({bad:true});
 try{
  await openThread(page,'root1');
  assert.equal(await page.locator('#discussion-thread-status').textContent(),'Erreur');
  assert.equal(await page.locator('#discussion-thread-fallback').getAttribute('href'),'http://discussion.test/Rdiscussion/discussion1/threads/root1');
  await page.locator('#discussion-thread-close').click();
  assert.equal(await page.locator('#discussion-sidebar-accordions').isVisible(),true);
 }finally{await browser.close();}
});

test('main conversation and thread retain independent listeners on the shared native event source',async()=>{
 const {browser,page}=await setup({live:true});
 try{
  await openThread(page,'root1');
  assert.equal(await page.evaluate(()=>[...handlers.values()].filter(v=>v.type==='DISCUSSION_CHANGED').length),2);
  const before=await page.evaluate(()=>calls.length);
  await page.evaluate(()=>emit());
  await page.waitForFunction(n=>calls.length>=n+2,before);
  assert.ok(await page.evaluate(()=>calls.some(c=>c.scope==='main')&&calls.some(c=>c.scope==='root1')));
  await page.locator('#discussion-thread-close').click();
  const childCalls=await page.evaluate(()=>calls.filter(c=>c.scope==='root1').length);
  await page.evaluate(()=>emit());
  await page.waitForTimeout(80);
  assert.equal(await page.evaluate(()=>calls.filter(c=>c.scope==='root1').length),childCalls,'A closed thread pauses synchronization');
  await openThread(page,'root1');
  await page.waitForFunction(n=>calls.filter(c=>c.scope==='root1').length>n,childCalls);
  assert.equal(await page.evaluate(()=>[...handlers.values()].filter(v=>v.type==='DISCUSSION_CHANGED').length),2);
 }finally{await browser.close();}
});
