# ScratchTools #
A collection of MegaApuTurkUltra's programs for batch operations involving Scratch.

## GUI Usage ##
To run tools with (a very basic) GUI, run `dist/ScratchTools.jar` with no arguments. Select a tool on the left, set options on the right, and then hit "Run Tool"

## Command Line Usage ##
### Follower List Generator ###
Generates a sprite2 file with avatars and details of a given user's followers.  
Usage:
```bash
java -jar dist/ScratchTools.jar FollowerListGenerator <username> <output sprite2 file>
```

### Visualizer Generator ###
Generates a project with a visualization of a given audio file. Additional instructions for setting up the project are in the generated Scratch project file.  
```bash
java -jar dist/ScratchTools.jar VisualizerGenerator <input audio file> <output sb2 file> <fullSpectrum>
```
fullSpectrum: Can be true or false, if true, the tool will output a large range of frequencies, if false, the tool will only output low frequencies.  
Note: Allows MP3 files as input, but has trouble reading some of them. WAV files are recommended.

The generated projects contains more instructions for setup. Visualizer color, alpha, scale, and low and high data thresholds can be set there.  
Note: You must set the sound length setting in the project if nothing else. Follow the instructions for best results.