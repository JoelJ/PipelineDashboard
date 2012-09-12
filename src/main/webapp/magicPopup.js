Event.observe(window, 'load', function() {
	$("projectstatus").observe("click", openPopup)
	document.body.observe("click", closePopup);
});

var openPopup = (function(e){
	var target = $(e.target);

	if(!e.target.hasClassName("custom")) {
		var customColumn = target.up(".custom");
		if(customColumn) {
			target = customColumn;
		}
	}

	if(target) {
		var div;
		if(window.commitTooltip) {
			div = window.commitTooltip;
		} else {
			div = document.createElement("div");
			div.setAttribute('id', 'commitTooltip');
			window.commitTooltip = div;
			document.body.appendChild(div);
		}

		var first = target.up().down(".build");
		var url = first.getAttribute('url') + "api/json";
		showContent(div, url, e.clientX + window.scrollX, e.clientY + window.scrollY);
	}
});

var closePopup = (function(e) {
	var parent = e.target;
	while(parent && parent != window.commitTooltip) {
		parent = parent.parentElement;
	}

	if(parent != window.commitTooltip && !$(e.target).hasClassName('custom')) {
		window.commitTooltip.style.display = "none";
	}
});

var showContent = (function(element, url, x, y) {
	element = $(element);
	element.innerHTML = "Loading...";
	element.style.left = x + "px";
	element.style.top = y + "px";
	element.style.display = "block";
	new Ajax.Request(url, {
		method:'get',
		onSuccess:function (transport) {
			var json = transport.responseText;
			var result = eval('('+json+')');

			if(!result || !result.changeSet || !result.changeSet.items) {
				console.log("no changeset");
				return;
			}

			var changeSet = result.changeSet.items;
			if(changeSet.length <= 0) {
				console.log("empty changeset")
				return;
			}

			var html = '<div class="header">Revisions Included in Run</div>';
			changeSet.each(function(it) {
				var hash = it.id.replace('<','&lt;');
				var comment = it.comment.trim().replace('<','&lt;');
				var baseURLForIssue = "https://dot8.attask.com/search?objCode=OPTASK&query=";
				var baseURLForTask = "https://dot8.attask.com/search?objCode=TASK&query=";
				var hashRegex = /\b[0-9a-f]{40}/;
				var issueRegex = /[Ii]ssue[:]? (\d+).*?/;
				var taskRegex = /[Tt]ask[:]? (\d+).*?/;
				if(hashRegex.test(comment)) {
					var newHash = hashRegex.exec(comment)[0];
					comment = comment.replace(hashRegex, newHash+ " (<a target='_blank' href='http://git.ops.ut.us.attask.com/git/?p=attask.git;a=commitdiff;h="+newHash+"'>git</a>|<a target='_blank' href='#row-"+newHash+"'>jenkins</a>)");
				}
				if(issueRegex.test(comment)) {
					var issueNumber = issueRegex.exec(comment)[1];
					comment = comment.replace(issueRegex, "Issue <a target='_blank' href='" + baseURLForIssue + issueNumber+"'>"+issueNumber+"</a>");
				}
				
				if(taskRegex.test(comment)) {
					var taskNumber = taskRegex.exec(comment)[1];
					comment = comment.replace(taskRegex, "Task <a target='_blank' href='" + baseURLForTask + taskNumber+"'>"+taskNumber+"</a>");
				}

				html += '<hr/>';
				html += '<div class="commit">';
				html += '<div>Revision: <span class="revision">'+hash+' (<a target="_blank" href="http://git.ops.ut.us.attask.com/git/?p=attask.git;a=commitdiff;h='+hash+'">git</a>)</span></div>';
				html += '<div>Author: <span class="author">'+it.author.fullName.replace('<','&lt;')+'</span></div>';
				html += '<div>Date: <span class="comment">'+it.date.replace('<','&lt;') +"</span></div>";
				html += '<div>Comment: <span class="comment"><pre>'+comment+'</pre></span></div>';
				html += '</div>';
			});

			element.innerHTML = html;
		}
	});
});






























