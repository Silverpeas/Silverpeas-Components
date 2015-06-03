 update sb_formtemplate_template set templatename='whitePages.xml' where templatename='whitePages/annuaire.xml';
 update st_instance_data set value='whitePages.xml' where name='cardTemplate' and value='whitePages/annuaire.xml';
 update st_instance_data set value='whitePages/view.html' where name='userTemplate' and value='whitePages/annuaire.html';

