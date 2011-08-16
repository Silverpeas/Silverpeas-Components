$!-------------------------------------------------------------------------------!$
$! STRING TEMPLATE which try to display an Event object result information !$
$! the list of templates attributes are :     !$
$!  "gsr"  the GlobalSilverResult     !$
$!  "pubDetail"  the PublicationDetail object     !$
$!  "pubAuthor"  publication author  (IF DEFINED)!$
$!  "pubKeywords"  publication keywords (IF DEFINED)!$
$!  "wysiwygContent"  publication wysiwyg content  (IF DEFINED)!$
$!  "xmlFormContent"  publication XML Form content  (IF DEFINED)!$
Creation date : $pubDetail.creationDate; format="MM/dd/yyyy"$
$if(pubKeywords)$<br/>Keywords : $pubKeywords$$endif$
$if(xmlFormContent)$<br/>$xmlFormContent$$endif$