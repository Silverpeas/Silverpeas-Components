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
const labels=Object.fromEntries(props.split('\n').filter(s=>s.startsWith('discussion.format')).map(s=>{const i=s.indexOf('=');return [s.slice(11,i).trim(),s.slice(i+1).trim()];}));
async function setup({body='',draft=''}={}) {
  const browser=await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
  const page=await browser.newPage({viewport:{width:1100,height:850}});
  await page.setContent(`<base href="https://discussion.test/"><main id="discussion" data-profile-base="/Rprofil/jsp/Main" data-contribution-base="/silverpeas/Contribution/" data-view-idea-label="Voir l’idée" data-view-event-label="Voir l’événement" data-view-vote-label="Participer au vote" data-emoji-label="Emoji" data-mention-label="Mention" data-icons="/icons.svg" data-close-image="Fermer"><div class="discussion-body"></div><section id="later"></section><form class="discussion-composer" method="post" enctype="multipart/form-data"><textarea id="discussion-body" name="body" maxlength="2000" required></textarea><input type="file" name="images" id="discussion-images" multiple data-remove="Retirer" data-error="Images invalides"><input type="file" name="files" id="discussion-files" multiple><div id="discussion-image-preview"></div><p id="discussion-image-error" hidden></p><button class="discussion-send" type="submit">Publier</button></form></main>`);
  await page.evaluate(({body,draft,labels})=>{
    const main=document.getElementById('discussion');
    Object.entries(labels).forEach(([key,value])=>{main.dataset[key]=value;});
    document.querySelector('.discussion-body').textContent=body;
    document.getElementById('discussion-body').value=draft;
    window.posts=[];document.addEventListener('submit',event=>{event.preventDefault();posts.push(new FormData(event.target));});
  },{body,draft,labels});
  await page.addStyleTag({path:path.join(assets,'css/discussion.css')});
  for(const file of ['vendor/markdown-it-15.0.3.min.js','discussion-markdown.js','discussion-editor.js','discussion-images.js','discussion-composer.js']) {
    await page.addScriptTag({path:path.join(assets,'javaScript',file)});
  }
  await page.evaluate(()=>document.dispatchEvent(new Event('DOMContentLoaded')));
  return {browser,page};
}
async function select(page,start,end) {
  await page.locator('textarea').focus();
  await page.locator('textarea').evaluate((n,{start,end})=>{n.setSelectionRange(start,end);n.dispatchEvent(new Event('select'));},{start,end});
}
async function paste(page,{count=1,type='image/png',size=20,text=''}={}) {
  return page.evaluate(({count,type,size,text})=>{
    const data=new DataTransfer();
    for(let i=0;i<count;i++)data.items.add(new File([new Uint8Array(size)],'paste-'+i+'.png',{type}));
    if(text)data.setData('text/plain',text);
    const event=new ClipboardEvent('paste',{clipboardData:data,bubbles:true,cancelable:true});
    document.getElementById('discussion-body').dispatchEvent(event);
    return event.defaultPrevented;
  },{count,type,size,text});
}

test('restricted Markdown renders formatting, mentions and native links while code and HTML stay literal',async()=>{
  const body='**Décision** et *italique* ~~barré~~\n\n- Premier\n- Second\n\n1. Étape\n2. Suite\n\n> Citation\n\nBonjour @[Alice](2) 😀 [Documentation](https://example.org)\n\n```java\n<script>alert(1)</script> @[Alice](2)\n```\n\n<img src=x onerror=alert(1)> [danger](javascript:alert(1))';
  const {browser,page}=await setup({body});
  try {
    assert.equal(await page.locator('.discussion-body strong').textContent(),'Décision');
    assert.equal(await page.locator('.discussion-body em').textContent(),'italique');
    assert.equal(await page.locator('.discussion-body s').textContent(),'barré');
    assert.equal(await page.locator('.discussion-body ul li').count(),2);
    assert.equal(await page.locator('.discussion-body ol li').count(),2);
    assert.equal((await page.locator('.discussion-body blockquote').textContent()).trim(),'Citation');
    assert.equal(await page.locator('.discussion-body .discussion-mention').textContent(),'@Alice');
    assert.equal(await page.locator('.discussion-body .discussion-mention').getAttribute('href'),'/Rprofil/jsp/Main?userId=2');
    assert.equal(await page.locator('.discussion-body pre a').count(),0);
    assert.ok((await page.locator('.discussion-body pre').textContent()).includes('@[Alice](2)'));
    assert.equal(await page.locator('.discussion-body img, .discussion-body script, .discussion-body a[href^="javascript:"]').count(),0);
    const doc=page.locator('.discussion-body a',{hasText:'Documentation'});
    assert.equal(await doc.getAttribute('target'),'_blank');
    assert.equal(await doc.getAttribute('rel'),'noopener noreferrer');
  } finally {await browser.close();}
});

test('toolbar and keyboard preserve recipient identifiers and submit raw Markdown without HTML',async()=>{
  const {browser,page}=await setup({draft:'Bonjour @[Alice](2)'});
  try {
    assert.equal(await page.locator('.discussion-format-tools').isVisible(),false);
    await page.locator('.discussion-format-toggle').click();
    assert.equal(await page.locator('.discussion-format-tools').isVisible(),true);
    await select(page,8,14);
    await page.locator('.discussion-format-bold').click();
    assert.equal(await page.locator('textarea').inputValue(),'Bonjour **@Alice**');
    assert.equal(await page.locator('input[name=body]').inputValue(),'Bonjour **@[Alice](2)**');
    await page.keyboard.press('Control+b');
    assert.equal(await page.locator('input[name=body]').inputValue(),'Bonjour @[Alice](2)');
    await page.keyboard.press('Control+b');
    assert.equal(await page.locator('input[name=body]').inputValue(),'Bonjour **@[Alice](2)**');
    await select(page,0,7);
    await page.keyboard.press('Control+i');
    assert.equal(await page.locator('input[name=body]').inputValue(),'*Bonjour* **@[Alice](2)**');
    await page.locator('.discussion-send').click();
    assert.equal(await page.evaluate(()=>posts[0].get('body')),'*Bonjour* **@[Alice](2)**');
    await page.locator('.discussion-format-code').focus();
    await page.keyboard.press('Escape');
    assert.equal(await page.locator('.discussion-format-tools').isVisible(),false);
  } finally {await browser.close();}
});

test('history uses the same Markdown renderer and named native links keep their chosen label',async()=>{
  const {browser,page}=await setup();
  try {
    await page.evaluate(()=>{
      const p=document.createElement('div');p.className='discussion-body';
      p.textContent='**Nouveau** [Mon guide](/silverpeas/Publication/12)';
      document.getElementById('later').append(p);DiscussionEditor.enhance(document.getElementById('later'));DiscussionEditor.enhance(document.getElementById('later'));
    });
    assert.equal(await page.locator('#later strong').count(),1);
    assert.equal(await page.locator('#later a').textContent(),'Mon guide');
    assert.equal(await page.locator('#later a').getAttribute('href'),'https://discussion.test/silverpeas/Publication/12');
    assert.equal(await page.locator('#later a').evaluate(n=>n.classList.contains('sp-link')),true);
    for(const width of [1100,390,320]) {
      await page.setViewportSize({width,height:850});
      await page.locator('.discussion-format-toggle').click();
      assert.equal(await page.locator('.discussion-format-tools').evaluate(n=>n.scrollWidth<=n.clientWidth),true);
    }
  } finally {await browser.close();}
});

test('pasted photos enable image-only multipart messages, accumulate with selected files and can be removed',async()=>{
  const {browser,page}=await setup();
  try {
    assert.equal(await page.locator('.discussion-send').isDisabled(),true);
    assert.equal(await paste(page),true);
    assert.equal(await page.locator('#discussion-images').evaluate(n=>n.files.length),1);
    assert.equal(await page.locator('#discussion-image-preview figure').count(),1);
    assert.equal(await page.locator('.discussion-send').isEnabled(),true);
    await page.locator('#discussion-images').setInputFiles({name:'picked.png',mimeType:'image/png',buffer:Buffer.from('picture')});
    assert.equal(await page.locator('#discussion-images').evaluate(n=>n.files.length),2);
    await page.locator('.discussion-send').click();
    assert.equal(await page.evaluate(()=>posts[0].getAll('images').length),2);
    assert.equal(await page.evaluate(()=>posts[0].get('body')),'');
    await page.locator('#discussion-image-preview button').first().click();
    await page.locator('#discussion-image-preview button').first().click();
    assert.equal(await page.locator('.discussion-send').isDisabled(),true);
  } finally {await browser.close();}
});

test('invalid clipboard images preserve previous attachments and text-only paste keeps native behavior',async()=>{
  const {browser,page}=await setup({draft:'Mon brouillon'});
  try {
    await paste(page);
    for(const data of [{count:4},{size:6*1024*1024},{type:'image/svg+xml'},{size:0}]) {
      await paste(page,data);
      assert.equal(await page.locator('#discussion-image-error').isVisible(),true);
      assert.equal(await page.locator('#discussion-images').evaluate(n=>n.files.length),1);
      assert.equal(await page.locator('textarea').inputValue(),'Mon brouillon');
    }
    assert.equal(await paste(page,{count:0,text:'Texte normal'}),false);
    assert.equal(await paste(page,{text:'Légende'}),false);
    assert.equal(await page.locator('#discussion-images').evaluate(n=>n.files.length),2);
  } finally {await browser.close();}
});

test('formatting never truncates a draft beyond the message length limit',async()=>{
  const {browser,page}=await setup({draft:'x'.repeat(1999)});
  try {
    await select(page,0,1999);
    await page.keyboard.press('Control+b');
    assert.equal((await page.locator('textarea').inputValue()).length,1999);
    assert.equal((await page.locator('input[name=body]').inputValue()).length,1999);
  } finally {await browser.close();}
});
