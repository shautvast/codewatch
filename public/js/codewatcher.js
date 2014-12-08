var ws = new WebSocket('ws://localhost:8080/sourcecodeHandler');
var regexLineNr = /(.*)At.*\[line.*? (.+)\]/;

/**
 * close websocket on pageleave
 */
window.onbeforeunload = function(e) {
	ws.close();
};

/**
 * called by websocket middleware
 */
ws.onopen = function() {
	ws.send('connect');
	console.log('connected');
};

/**
 * called by websocket middleware
 */
ws.onclose = function() {
	console.log('Connection is closed...');
};

/**
 * Being called when data is sent on websocket. Incoming (in event.data) is the name of the sourcefile
 * 
 * @param event: MessageEvent
 */
ws.onmessage = function(event) {
	console.log('incoming code update for ' + event.data);
	var sourcefileName = event.data;
	/*lookup the annotated source code*/
	getCode(sourcefileName);
};

/**
 * @param classname: String The filename of the sourcecode to lookup
 */
function getCode(classname) {
	d3.json('/code/' + classname + '.', function(error, data) {
		d3.select('div#editor').remove();
		//SyntaxHighlighter expects a PRE which contains the sourcecode
		var editor = d3.select('pre#editor')
		editor.text(data.code);

		SyntaxHighlighter.highlight();
		
		//when Syntax highlighter is done, the pre is turned into a DIV. Any subsequent code updates will fail to render properly unless a new PRE is available
		d3.select('body')
			.append('pre')
			.attr('id', 'editor')
			.attr('class','brush: java');
		
		annotateCode(data.analysis);
	});
}

/**
 * @param annotations: String This is actually geared towards findbugs output which is lines of plain text for each message
 */
function annotateCode(annotations) {
	var messages = [];
	annotations.split('\n').forEach(
		function(line) {
			var match = regexLineNr.exec(line); // separate the complete message into text (match[1]) and linenumber(range) (match[2])
			if (match) {
				var linenumber = parseInt(match[2].split('-')[0]); //handle linenumber ranges -> use start line
				/*use jquery here because its search is more convenient*/
				var offsettop = $('.container .number' + (linenumber + 1))[0].offsetTop; //convert linenumber to actual position when rendered
				/* insert marker*/
				d3.select('body')
				.append('div')
				.attr('class', 'marker')
				.text(match[1])
				.style('top',offsettop + 'px');
			}
		});
}
