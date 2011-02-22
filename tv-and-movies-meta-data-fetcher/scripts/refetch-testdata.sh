#!/bin/sh

TESTDIR="../tests/org/stanwood/media/testdata"

#wget -O $TESTDIR/tagchimp-search-ironman.html "http://www.tagchimp.com/search/index.php?s=iron+man&search.x=0&search.y=0&kind=a"
#wget -O $TESTDIR/tagchimp-iron-man-17.html "http://www.tagchimp.com/tc/39752/"

#wget -O $TESTDIR/filmposters/movieposter-ironman.html "http://eu.movieposter.com/cgi-bin/mpw8/search.pl?ti=Iron+Man&pl=action&th=y&rs=12&size=any"
#wget -O $TESTDIR/film-0371746.html "http://www.imdb.com/title/tt0371746"

wget -O "$TESTDIR/tvdb-search-heroes.html" "http://www.thetvdb.com/api/GetSeries.php?seriesname=Heroes&language=en"
wget -O "$TESTDIR/tvdb-series-79501.zip" "http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip"
wget -O "$TESTDIR/themoviedb-search-iron-man.html" "http://api.themoviedb.org/2.1/Movie.search/en/xml/57983e31fb435df4df77afb854740ea9/Iron+Man"
wget -O "$TESTDIR/themoviedb-film-1726.html" "http://api.themoviedb.org/2.1/Movie.getInfo/en/xml/57983e31fb435df4df77afb854740ea9/1726"
wget -O "$TESTDIR/imdb-tt0371746.html" "http://akas.imdb.com/title/tt0371746/"
wget -O "$TESTDIR/themoviedb-images-1726.html"  "http://api.themoviedb.org/2.1/Movie.getImages/en/xml/57983e31fb435df4df77afb854740ea9/1726"
wget -O "$TESTDIR/themoviedb-imdbLookup-tt0371746.html" "http://api.themoviedb.org/2.1/Movie.imdbLookup/en/xml/57983e31fb435df4df77afb854740ea9/tt0371746"

TEMP_DIR=`mktemp -d`
cd $TEMP_DIR
mkdir addons
wget -O "$TEMP_DIR/addons/addons.xm" "http://mirrors.xbmc.org/addons/dharma/addons.xml"
wget -O "$TEMP_DIR/addons/addons.xml.md5" "http://mirrors.xbmc.org/addons/dharma/addons.xml.md5"
wget -O "$TEMP_DIR/addons/metadata.themoviedb.org/metadata.themoviedb.org-1.2.4.zip" "http://mirrors.xbmc.org/addons/dharma/metadata.themoviedb.org/metadata.themoviedb.org-1.2.4.zip"
wget -O "$TEMP_DIR/addons/metadata.common.hdtrailers.net/metadata.common.hdtrailers.net-1.0.6.zip" "http://mirrors.xbmc.org/addons/dharma/metadata.common.hdtrailers.net/metadata.common.hdtrailers.net-1.0.6.zip"
wget -O "$TEMP_DIR/addons/metadata.common.imdb.com/metadata.common.imdb.com-2.0.7.zip" "http://mirrors.xbmc.org/addons/dharma/metadata.common.imdb.com/metadata.common.imdb.com-2.0.7.zip"
wget -O "$TEMP_DIR/addons/metadata.tvdb.com/metadata.tvdb.com-1.1.0.zip" "http://mirrors.xbmc.org/addons/dharma/metadata.tvdb.com/metadata.tvdb.com-1.1.0.zip"
wget -O "$TEMP_DIR/addons/metadata.common.themoviedb.org/metadata.common.themoviedb.org-1.0.7.zip" "http://mirrors.xbmc.org/addons/dharma/metadata.common.themoviedb.org/metadata.common.themoviedb.org-1.0.7.zip"
zip -r9 "../tests/org/stanwood/media/source/xbmc/updates.zip" addons
rm -rf $TEMP_DIR

#TEMP_DIR=`mktemp -d`
#git clone git://github.com/xbmc/xbmc.git $TEMP_DIR
#cd $TEMP_DIR
#git checkout addons
#zip -r9 xbmc-addons.zip addons
#cd -
#mv $TEMP_DIR/xbmc-addons.zip ../tests/org/stanwood/media/source/xbmc/
#rm -rf $TEMP_DIR
