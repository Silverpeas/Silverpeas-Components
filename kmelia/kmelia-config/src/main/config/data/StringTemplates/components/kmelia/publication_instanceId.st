<div class="line1">
	<span class="bullet">&#8226;</span>
		<font color="$statusColor$"><a href="$link$"><b class="$highlightClass$">$if(showRef)$[$pub.id$] $endif$$name$</b></a></font>&nbsp;
		$if(status)$
			<span>($status$)</span>
		$elseif(showImportance)$
			<span class="importance"><nobr>$importance$</nobr></span>
		$endif$
</div>
<div class="line2">
	<font color="$statusColor$">
		$if(path)$$path$$endif$
		$if(showUsername)$<span class="user">$username$ - </span>$endif$
		$if(showDate)$<span class="date">$date$</span>$endif$
		$if(showPermalink)$<span class="permalink">$permalink$</span>$endif$
	</font>
</div>
$if(showDescription)$
	<div class="line3">
		<font color="$statusColor$"><span class="description">$description$</span></font><br/>
	</div>
$endif$
$files$