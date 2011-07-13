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
<tr class="lineResult $spaceId$ $instanceId$">
$if(pertinenceVisible)$ <td class="pertinence">$pertinence$&nbsp;</td> $endif$
  <td class="content">
    <table cellspacing="0" cellpadding="0">
      <tr>
        <td>
	  $if(activeSelection)$ <span id="readSpanId_$gsr.resultId$" class="textePetitBold">$name$</span>
	  $else$
	    $if (gsr.hasRead)$
	    <a href="$url$" class="markedkAsReadDisableVisited"><span id="readSpanId_$gsr.resultId$" class="markedkAsRead">$name$</span></a>
	    $else$
	    <a href="$url$" class=""><span id="readSpanId_$gsr.resultId$" class="textePetitBold">$name$</span></a>
	    $endif$
	  $endif$
	  $if(downloadUrl)$<a href="$downloadUrl$" target="_blank">$downloadUrl$</a>$endif$
	  $if(creatorName)$<span class="creatorName"> - $creatorName$</span>$endif$
	  $if(creationDate)$<span class="creationDate"> ($creationDate$) </span>$endif$
	  $if(description)$<span class="description"><br/><i> $description$</i></span>$endif$
	  $if(location)$<span class="location"><br/>$location$</span>$endif$
	  <br/>Lieu de l'événement : <b>$eventDetail.place$</b> <br/>
	  Date de l'événement : $eventDetail.startDate; format="dd/MM/yyyy"$
	</td>
      </tr>
    </table>
</tr>