# MediaManager Information

MediaManager? previously known as "MediaInfoFetcher?" is a application and a API which can be used to retrieve TV show and movie meta data from Internet sources. This information is then stored locally and can be used to rename media files with the correct title.

MediaManager? is written in Java and requires JRE version >= 1.6.

## Features

- Provides a CLI tools that perform actions on media files.
- Fetches TV Show and Film meta data from the Internet via sources such as the XBMC scrapers.
- Find film posters on the Internet for each film.
- Allows multiple sources and stores.
- Caches information in stores such as iTunes MP4 files and XML stores.
- Able to notify iTunes of media file changes via a remote server running on the iTunes box.
- Support for High Definition iTunes video atoms.
- Runs on any platform that supports Java 1.6.
- Provides action for executing system commands on media files.
- Provides action to rename files based on complex patterns.
- Provides a API for accessing the information.
- Provides a store that writes Sapphire XML files.
- Provides a store that saves metadata into MP4/M4V files that iTunes can read.
- Configurable logging via log4j.
- Able to watch folders for new media.

# Status

# Tue 29 May 2012

Media Manager v2.1 has been released. This is a major update that focuses on iTunes integration. Here is a list of the major changes:

- Seen files can be ignored
- Directories can be watched for new media, which is then moved to the correct media directory.
- MP4 Files metadata now handled by fork of atomic parsley.
- Much improved MP4/M4V metadata.
- Support for High Def media.
- New Store for notifying iTunes of media file changes via a remote server running on the iTunes box.
- Improved windows support.
- Improved TV Show/Film searching.
- Fixed issues with launch scripts.
- Debian/Ubuntu install packages.
