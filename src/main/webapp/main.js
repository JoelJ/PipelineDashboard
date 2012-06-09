Event.observe(window, 'load', initHide);

function updateLabel() {
	if ($('PipelineDashboard').hasClassName('hide-default-hidden')) {
		$('showHidden').innerHTML = "Show Hidden";
	} else {
		$('showHidden').innerHTML = "Hide Hidden";
	}
}

function initHide() {
	updateLabel();

	$('showHidden').observe('click', function(event) {
		event.preventDefault();

		$('PipelineDashboard').toggleClassName('hide-default-hidden');

		updateLabel();

		return false;
	});
}