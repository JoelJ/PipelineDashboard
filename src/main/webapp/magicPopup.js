Event.observe(window, 'load', function() {
	$$(".custom").each(function(it) {
		it.observe("click", openPopup);
		document.body.observe("click", closePopup);
	});
});

var openPopup = (function(e){
	var div;
	if(window.commitTooltip) {
		div = window.commitTooltip;
	} else {
		div = document.createElement("div");
		div.setAttribute('id', 'commitTooltip');
		window.commitTooltip = div;
		document.body.appendChild(div);
	}

	var siblings = e.target.siblings();
	var first = siblings[0];
	var url = first.getAttribute('url') + "api/json";
	showContent(div, url, e.clientX, e.clientY);
});

var closePopup = (function(e) {
	if(e.target != window.commitTooltip && !$(e.target).hasClassName('custom')) {
		window.commitTooltip.style.display = "none";
	}
});

var showContent = (function(element, url, x, y) {
	element = $(element);
	new Ajax.Request(url, {
		method:'get',
		onSuccess:function (transport) {
			var json = transport.response;
			var result = eval('('+json+')');

			var changeSet = result.changeSet.items;
			if(changeSet.length <= 0) {
				console.log("not showing");
				return;
			}
			console.log("showing");

			var html = '<div class="header">Commits</div>';
			changeSet.each(function(it) {
				html += '<hr/>';
				html += '<div class="commit">';
				html += 	'<div>Author: <span class="author">'+it.author.fullName+'</span></div>';
				html += 	'<div>Revision: <span class="revision">'+it.id.substring(0, 7)+'</span></div>';
				html += 	'<div>Comment: "<span class="comment">'+it.comment.trim().replace('\n', '<br/>')+'</span>"</div>';
				html += '</div>';
			});

			element.innerHTML = html;
			element.style.left = x + "px";
			element.style.top = y + "px";
			element.style.display = "block";
		}
	});
});