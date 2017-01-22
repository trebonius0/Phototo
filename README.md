# Photato

Photato aims to be a self-hosted photo gallery, accessible through a **responsive WebUI**. 

Give to Photato a picture folder and it will index it, using the file system hierarchy. 

## Features
* Folders / pictures gallery
* Responsive design
* Realtime indexing (throught picture ExifMetadata) 
  * Title of the picture
  * Keywords of the picture
  * Persons tagged in a picture
  * Location of the picture

## Screenshots
![Pictures Gallery](https://i.imgur.com/U59HnpX.png)

![FullScreen Picture](https://i.imgur.com/nKBk6im.png)

## Try it
### On Windows
* Download and unzip latest version from [https://github.com/trebonius0/Photato/releases](https://github.com/trebonius0/Photato/releases)
* Download the latest version of Java [here](https://www.java.com/fr/download/).
* Edit the start.cmd file to select the picture folder you want to index 
* Run the start.cmd file
  * At the first start, Photato will start by indexing the picture folder and generate thumbnails (which can take ~20min for 7000 pictures)
  * When it will have finished, you can access it on [http://127.0.0.1:8186](http://127.0.0.1:8186)

### On Ubuntu/Debian
* Download and unzip latest version from [https://github.com/trebonius0/Photato/releases](https://github.com/trebonius0/Photato/releases)
* *sudo apt install libimage-exiftool-perl openjdk-8-jre*
* *java -Xmx1g -jar Photato-Release.jar "[Path to your picture folder]"*
  * At the first start, Photato will start by indexing the picture folder and generate thumbnails (which can take ~20min for 7000 pictures)
  * When it will have finished, you can access it on [http://127.0.0.1:8186](http://127.0.0.1:8186)

## Roadmap
Here are the features we will implement in the future (you can contribute if you want to implement one yourself)
- XMP file support
- Authentication / Access restriction to some folders
- Upload of pictures to the filesystem directly through the gallery
- Edit picture metadata directly through the gallery
- Easier download of a picture / a group of pictures
- Auto-generated "galleries": persons / tags / locations / dates
- Easier configuration mechanism
- File-based Logging
- Automated offline keywords annotations

## Disclaimer
Photato will rename all pictures with 2+ spaces in a row to the same but with one space (otherwise it would make exiftool crash). For instance "my  picture.jpg" will be renamed to "my picture.jpg"