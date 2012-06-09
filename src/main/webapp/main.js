Event.observe(window, 'load', initMain);

function initMain() {
	initHide();
	initFullscreen();
}

function initHide() {

	$('showHidden').observe('click', function(event) {
		event.preventDefault();
		$('PipelineDashboard').toggleClassName('hide-default-hidden');
		return false;
	});
}

function initFullscreen() {
	$('fullScreen').observe('click', function(event) {
		event.preventDefault();

		var div = $('PipelineDashboard');
		if(div.webkitRequestFullScreen) {
			div.webkitRequestFullScreen(Element.ALLOW_KEYBOARD_INPUT);
		}
		if(div.mozRequestFullScreen) {
			div.mozRequestFullScreen();
		}


		return false;
	});
}