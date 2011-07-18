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
$!  "evtStartDate"  event start date !$
$!  "evtLocation"  location where the event occurs  (IF DEFINED) !$
$!  "evtEndDate"  event end date (IF DEFINED) !$
$!  "evtStartHour"  event start hour (IF DEFINED) !$
$!  "evtEndHour"  event end hour (IF DEFINED) !$
$!  "evtPriority"  event priority !$
$!  "evtPeriodicity"  event periodicity (IF DEFINED) !$
$!  "evtURL"  event URL (IF DEFINED) !$
<div class="template">
$if(evtLocation)$A $evtLocation$ le $else$Le $endif$$eventDetail.startDate; format="dd/MM/yyyy"$<br/>
</div>