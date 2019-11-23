# AragoJ Plugin - Bioformats

This plugin adds basic support for importing images with [OME Bioformats](https://www.openmicroscopy.org/bio-formats/).

Note that this is a fairly simple plugin that simply extracts the first image plane it finds (it does not support extracting more than 1). It also attemps to convert the image to a 24-bit RGB image.

Currently it does not do any post-process work (e.g. RAW files that can be read won't have any white balance applied, etc.).

## Release
You can check the latest release [here](https://github.com/franciscoaleixo/AragoJ-Bioformats/releases).

## License
Because Bioformats uses GPL, which requires copyleft, this plugin also has the same GPL license. Support their amazing work [here](https://github.com/ome/bioformats).
