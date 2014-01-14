$!-------------------------------------------------------------------------------!$
$! STRING TEMPLATE which try to display an Event object result information !$
$! the list of templates attributes are :     !$
$!  "gsr"  the GlobalSilverResult     !$
$!  "eventDetail"  the EventDetail object     !$
$!  "evtStartDate"  event start date !$
$!  "evtLocation"  location where the event occurs  (IF DEFINED) !$
$!  "evtEndDate"  event end date (IF DEFINED) !$
$!  "evtStartHour"  event start hour (IF DEFINED) !$
$!  "evtEndHour"  event end hour (IF DEFINED) !$
$!  "evtPriority"  event priority !$
$!  "evtPeriodicity"  event periodicity (IF DEFINED) !$
$!  "evtURL"  event URL (IF DEFINED) !$
Take place the $eventDetail.startDate; format="MM/dd/yyyy"$$if(location)$ in $location$ $endif$