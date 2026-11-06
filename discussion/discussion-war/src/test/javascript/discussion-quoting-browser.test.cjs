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
const sharedEditor = require('./shared-editor-assets.cjs');
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
  page.setDefaultTimeout(3000);
  await page.route('https://discussion.test/**', route=>route.fulfill({contentType:'text/html',body:''}));
  await page.goto('https://discussion.test/');
  await page.setContent(`<base href="https://discussion.test/"><main id="discussion" data-profile-base="/Rprofil/jsp/Main" data-contribution-base="/silverpeas/Contribution/" data-view-idea-label="Voir l’idée" data-view-event-label="Voir l’événement" data-view-vote-label="Participer au vote" data-emoji-label="Emoji" data-mention-label="Mention" data-icons="/icons.svg" data-close-image="Fermer" data-length-error="Trop long" data-remove-quote="Retirer la citation" data-message-required="Message requis" data-link-text="Texte" data-link-url="Adresse" data-link-apply="Appliquer" data-link-invalid="Adresse invalide"><template id="discussion-mention-directory"><span data-user-id="2" data-name="Alice Martin"></span><span data-user-id="3" data-name="Bernard"></span></template><div class="discussion-body"></div><section id="later"></section><form class="discussion-composer" method="post" enctype="multipart/form-data"><div class="discussion-compose-box"><textarea id="discussion-body" name="body" maxlength="2000" required></textarea><input hidden type="file" name="images" id="discussion-images" multiple data-remove="Retirer" data-error="Images invalides"><input hidden type="file" name="files" id="discussion-files" multiple><div id="discussion-image-preview"></div><p id="discussion-image-error" hidden></p><div class="discussion-composer-footer"><button class="discussion-send" type="submit">Publier</button></div></div></form></main>`);
  await page.evaluate(({body,draft,labels})=>{
    const main=document.getElementById('discussion');
    Object.entries(labels).forEach(([key,value])=>{main.dataset[key]=value;});
    document.querySelector('.discussion-body').textContent=body;
    document.getElementById('discussion-body').value=draft;
    window.posts=[];document.addEventListener('submit',event=>{event.preventDefault();posts.push(new FormData(event.target));});
  },{body,draft,labels});
  await page.addStyleTag({path:path.join(assets,'css/discussion.css')});
  await sharedEditor.load(page);
  for(const file of ['vendor/markdown-it-15.0.3.min.js','discussion-markdown.js','vendor/tiptap-3.32.2.min.js','discussion-rich-editor.js','discussion-editor.js','discussion-images.js','discussion-composer.js','discussion-quoting.js']) {
    await page.addScriptTag({path:(file.startsWith('vendor/') ? sharedEditor.asset(file) : path.join(assets,'javaScript',file))});
  }
  await page.evaluate(()=>document.dispatchEvent(new Event('DOMContentLoaded')));
  return {browser,page};
}


const visual = page => page.locator('.discussion-composer .tiptap');
async function message(page,{id='m1',author='Alice',text='Bonjour @Bernard',attachment=false}={}) {
  await page.evaluate(({id,author,text,attachment})=>{
    const article=document.createElement('article');article.id='message-'+id;article.className='discussion-message';
    const body=document.createElement('div');body.className='discussion-body';body.textContent=text;article.append(body);
    if(attachment){const files=document.createElement('ul');files.className='discussion-message-files';const link=document.createElement('a');link.download='budget.pdf';link.textContent='budget.pdf';files.append(link);article.append(files);}
    const button=document.createElement('button');button.type='button';button.className='discussion-quote';button.textContent='Citer';button.dataset.quoteAuthor=author;button.dataset.quoteUrl='/silverpeas/Rdiscussion/discussion1/messages/'+id;
    article.append(button);document.getElementById('later').append(article);
  },{id,author,text,attachment});
}

async function quote(page,id='m1') { await page.locator('#message-'+id).hover(); await page.locator('#message-'+id+' .discussion-quote').click(); }

test('quoting preserves the draft, uses visible text and posts a linked Markdown quotation in the current composer',async()=>{
  const {browser,page}=await setup({draft:'**Ma réponse**'});
  try {
    await message(page);
    await quote(page);
    assert.equal(await visual(page).locator('blockquote a').textContent(),'Alice');
    assert.equal(await visual(page).locator('blockquote a').getAttribute('href'),'https://discussion.test/silverpeas/Rdiscussion/discussion1/messages/m1');
    assert.ok((await visual(page).innerText()).includes('Bonjour @Bernard'));
    assert.equal(await visual(page).locator('strong').textContent(),'Ma réponse');
    assert.equal(await page.locator('.discussion-remove-quote').isVisible(),true);
    await page.locator('.discussion-send').click();
    const body=await page.evaluate(()=>posts[0].get('body'));
    assert.ok(body.startsWith('> [Alice]('));
    assert.ok(body.includes('> Bonjour @Bernard'));
    assert.ok(body.endsWith('**Ma réponse**'));
    assert.ok(!body.includes('@[Bernard]'));
    assert.equal(await page.evaluate(()=>posts.length),1);
  } finally {await browser.close();}
});

test('a new citation replaces the previous citation and removing it keeps the answer and attached files',async()=>{
  const {browser,page}=await setup({draft:'Réponse conservée'});
  try {
    await message(page);await message(page,{id:'m2',author:'Bernard',text:'Autre message'});
    await page.locator('#discussion-files').setInputFiles({name:'note.pdf',mimeType:'application/pdf',buffer:Buffer.from('document')});
    await quote(page);
    await quote(page,'m2');
    assert.equal(await visual(page).locator('blockquote').count(),1);
    assert.equal(await visual(page).locator('blockquote a').textContent(),'Bernard');
    await page.locator('.discussion-remove-quote').click();
    assert.equal(await visual(page).locator('blockquote').count(),0);
    assert.equal(await page.locator('.discussion-remove-quote').isVisible(),false);
    assert.equal(await page.locator('input[name=body]').inputValue(),'Réponse conservée');
    assert.equal(await page.locator('#discussion-files').evaluate(n=>n.files[0].name),'note.pdf');
  } finally {await browser.close();}
});

test('attachment-only messages can be cited, quotations survive draft restoration and open the original with native navigation',async()=>{
  const {browser,page}=await setup();
  try {
    await message(page,{text:'',attachment:true});
    await quote(page);
    assert.ok((await visual(page).innerText()).includes('budget.pdf'));
    const stored=await page.locator('input[name=body]').inputValue();
    await page.evaluate(stored=>{
      DiscussionEditor.reset(document.querySelector('textarea'),stored);
      const body=document.createElement('div');body.className='discussion-body';body.textContent=stored;
      document.getElementById('later').append(body);DiscussionEditor.enhance(document.getElementById('later'));
    },stored);
    assert.equal(await page.locator('.discussion-remove-quote').isVisible(),true);
    const link=page.locator('#later .discussion-body blockquote a');
    assert.equal(await link.getAttribute('target'),'_self');
    assert.equal(await link.evaluate(n=>n.classList.contains('sp-link')),true);
  } finally {await browser.close();}
});

test('quoting never injects HTML or silently truncates a nearly full draft',async()=>{
  const {browser,page}=await setup({draft:'a'.repeat(1950)});
  try {
    await message(page,{author:'<script>bad()</script>',text:'<img src=x onerror=bad()> '+ 'x'.repeat(1000)});
    await quote(page);
    assert.equal(await page.locator('input[name=body]').inputValue(),'a'.repeat(1950));
    assert.equal(await page.locator('.discussion-rich-error').isVisible(),true);
    await page.evaluate(()=>DiscussionEditor.reset(document.querySelector('textarea'),''));
    await quote(page);
    assert.equal(await visual(page).locator('script,img').count(),0);
    assert.equal(await visual(page).locator('blockquote a').textContent(),'<script>bad()</script>');
    assert.ok((await visual(page).locator('blockquote').innerText()).length<400);
  } finally {await browser.close();}
});

test('quotations use the configured Unicode character limit', async () => {
  const {browser,page} = await setup({body:'😀'.repeat(25)});
  try {
    await page.evaluate(() => {document.getElementById('discussion').dataset.quoteExcerptLength = '8';});
    await message(page, {text:'😀'.repeat(25)});
    await quote(page);
    const markdown = await page.locator('input[name=body]').inputValue();
    assert.ok(markdown.includes('😀'.repeat(8) + '…'));
    assert.ok(!markdown.includes('😀'.repeat(9)));
  } finally {await browser.close();}
});
