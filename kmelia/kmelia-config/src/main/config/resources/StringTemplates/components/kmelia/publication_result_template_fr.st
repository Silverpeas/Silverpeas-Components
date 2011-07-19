$!-------------------------------------------------------------------------------!$
$! STRING TEMPLATE which try to display an Event object result information !$
$! the list of templates attributes are :     !$
$!  "gsr"  the GlobalSilverResult     !$
$!  "pubDetail"  the PublicationDetail object     !$
$!  "name"  the result search name    !$
$!  "description"  the result search description   !$
$!  "url"  the link to access this search result   !$
$!  "downloadUrl"  a download link  !$
$!  "location"  the result search location !$
$!  "creatorName"  the publication creator name !$
$!  "spaceId"  the space identifier !$
$!  "instanceId"  the component identifier !$
$!  "creationDate"  publication creation date !$
$!  "serverName"  only used for external search purpose !$
$!  "pertinenceVisible"  is pertinence visible !$
$!  "pertinence"  HTML representation full and empty stars !$
$!  "activeSelection"  is active selection !$
$!  "exportEnabled"  is export enabled !$
$!  "type"  type of publication !$
$!  "pubAuthor"  publication author  (IF DEFINED)!$
$!  "pubCreatorName"  publication creator name (IF DEFINED)!$
$!  "pubKeywords"  publication keywords (IF DEFINED)!$
$!  "pubContent"  publication content  (IF DEFINED)!$
$!  "wysiwygContent"  publication wysiwyg content  (IF DEFINED)!$
$!  "xmlFormContent"  publication XML Form content  (IF DEFINED)!$
<div class="template">
Date de création : $pubDetail.creationDate; format="dd/MM/yyyy"$
$if(pubKeywords)$<br/>Mots clés: $pubKeywords$$endif$
$if(wysiwygContent)$<div id="publicationContent"><br/>Contenu WYSIWYG = $wysiwygContent$</div>$endif$
$if(xmlFormContent)$<div id="publicationContent"><br/>Contenu WYSIWYG = $xmlFormContent$</div>$endif$
</div>