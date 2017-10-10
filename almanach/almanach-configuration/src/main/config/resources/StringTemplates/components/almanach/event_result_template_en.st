$!-------------------------------------------------------------------------------!$
$! STRING TEMPLATE which try to display an Event object result information !$
$! the list of templates attributes are :     !$
$!  "evtStartDate"  event start date !$
$!  "evtLocation"  location where the event occurs  (IF DEFINED) !$
$!  "evtEndDate"  event end date (IF DEFINED) !$
$!  "evtPriority"  event priority !$
$!  "evtPeriodicity"  event periodicity (IF DEFINED) !$
$!  "evtURL"  event URL (IF DEFINED) !$
Take place the $evtStartDate; format="dd.MM.yyyy"$$if(location)$ in $location$ $endif$