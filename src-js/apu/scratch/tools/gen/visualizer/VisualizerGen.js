var player = document.getElementById("audioPlayer");
var inputFile = document.getElementById("inputFile");
var progress = document.getElementById("progressBar");
progress.style.display = "none";

var pts = [];

var analyser;
var audioCtx = new (window.AudioContext || window.webkitAudioContext);

analyser = audioCtx.createAnalyser();
analyser.fftSize = 256 * 64;
analyser.smoothingTimeConstant = 0.1;

var source = audioCtx.createMediaElementSource(player);

source.connect(analyser);
// analyser.connect(audioCtx.destination);

streamData = new Uint8Array(128 * 64);

var shakeThreshold = 7000;
var shakeDelay = 1000;
var lastShakeTime = new Date().getTime();

var totalVol;

var startTime;

var lastCalledTime = new Date().getTime();

var iterations = 0;

function sampleAudioStream() {
	if (player.paused) {
		generateProject();
		return;
	}

	var val = player.currentTime * 100 / player.duration;
	if(isFinite(val)) progress.value = val;

	var timeNow = new Date().getTime();
	var delta = timeNow - lastCalledTime;
	lastCalledTime = timeNow;

	analyser.getByteFrequencyData(streamData);
	totalVol = 0;
	for (var i = 0; i < 80; i++) {
		totalVol += Math.pow(streamData[i], 2.72) / 20000;
	}

	var frame = Math.floor((timeNow - startTime) / 100); // 10 fps

	for (var i = 0; i < 80; i++) {
		var data = Math.pow(streamData[i], 2.72) / 20000;
		if (data < 100)
			data = 100;

		data = data * (180) / 250;
		pts[frame * 80 + i] = data;
	}
	iterations++;
	setTimeout(sampleAudioStream, 10);
};

function startGen() {
	document.getElementById("status").innerText = "Getting data...";
	pts = [];
	player.play();
	progress.style.display = "block";
	inputFile.style.display = "none";
	iterations = 0;
	startTime = new Date().getTime();
	sampleAudioStream();
}

var _debug;

function generateProject() {
	progress.style.display = "none";
	inputFile.style.display = "block";
	document.getElementById("status").innerText = "Downloading template...";

	JSZipUtils.getBinaryContent('VisualizerBase.sb2', function(err, data) {
		if (err) {
			document.getElementById("status").innerText = "Failed to download template!";
			return;
		}

		try {
			var zip = new JSZip(data);
			_debug = zip;
			document.getElementById("status").innerText = "Generating project data...";
			
			var projectJson = JSON.parse(zip.file("project.json").asText());
			projectJson.children[0].lists[0].contents = pts;
			
			console.log(projectJson.children[0].lists[0].contents);
			
			zip.file("project.json", JSON.stringify(projectJson));
			
			document.getElementById("status").innerText = "Generating project...";
			var blob = zip.generate({type:"blob"});
	        saveAs(blob, "Visualizer.sb2");
	        document.getElementById("status").innerText = "Done. Check the project, there's still some setup to do. Enjoy!";
		} catch (e) {
			document.getElementById("status").innerText = "Failed to read template!";
		}
	});

}

inputFile.onchange = function() {
	var files = this.files;
	var file = URL.createObjectURL(files[0]);
	player.src = file;
	startGen();
};