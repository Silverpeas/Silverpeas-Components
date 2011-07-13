$!-------------------------------------------------------------------------------!$
$! STRING TEMPLATE which try to display an Event object result information !$
$! the list of templates attributes are :     !$
$!  "gsr"  the GlobalSilverResult     !$
$!  "eventDetail"  the EventDetail object     !$
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
<div class="almanach_result">
Event date : $eventDetail.startDate; format="dd/MM/yyyy"$<br/>
Event location : $eventDetail.place$
</div>