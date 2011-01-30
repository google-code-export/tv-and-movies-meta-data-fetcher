#!/bin/sh

TESTDIR="../tests/org/stanwood/media/testdata"

#wget -O $TESTDIR/tagchimp-search-ironman.html "http://www.tagchimp.com/search/index.php?s=iron+man&search.x=0&search.y=0&kind=a"
#wget -O $TESTDIR/tagchimp-iron-man-17.html "http://www.tagchimp.com/tc/39752/"

#wget -O $TESTDIR/filmposters/movieposter-ironman.html "http://eu.movieposter.com/cgi-bin/mpw8/search.pl?ti=Iron+Man&pl=action&th=y&rs=12&size=any"
#wget -O $TESTDIR/xbmc-eureka-search.html "http://www.tv.com/search.php?type=Search&stype=ajax_search&qs=Eureka&search_type=program&pg_results=0&sort="
#wget -O $TESTDIR/eureka-search.html "http://www.tv.com/search.php?type=Search&stype=ajax_search&search_type=program&qs=eureka"
#wget -O $TESTDIR/17552-summary.html "http://www.tv.com/heroes/show/17552/summary.html"
#wget -O $TESTDIR/17552-episode_guide-printable=1.html "http://www.tv.com/heroes/show/17552/episode_guide.html?printable=1"
#wget -O $TESTDIR/17552-episode_listings-season=3.html "http://www.tv.com/heroes/show/17552/episode.html?season=3"
#wget -O $TESTDIR/58448-summary.html "http://www.tv.com/heroes/eureka/show/58448/summary.html"
#wget -O $TESTDIR/58448-episode_guide-printable=1.html "http://www.tv.com/eureka/show/58448/episode_guide.html?printable=1"
#wget -O $TESTDIR/58448-episode_listings-season=1.html "http://www.tv.com/eureka/show/58448/episode.html?season=1"
#wget -O $TESTDIR/58448-episode_listings-season=2.html "http://www.tv.com/eureka/show/58448/episode.html?season=2"
#wget -O $TESTDIR/58448-episode_listings-season=3.html "http://www.tv.com/eureka/show/58448/episode.html?season=3"
#wget -O $TESTDIR/58448-episode_listings-season=4.html "http://www.tv.com/eureka/show/58448/episode.html?season=4"
#wget -O $TESTDIR/58448-episode_listings-season=5.html "http://www.tv.com/eureka/show/58448/episode.html?season=5"

#wget -O $TESTDIR/film-0371746.html "http://www.imdb.com/title/tt0371746"

wget -O "$TESTDIR/tvdb-search-heroes.html" "http://www.thetvdb.com/api/GetSeries.php?seriesname=Heroes&language=en"
wget -O "$TESTDIR/tvdb-series-79501.zip" "http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip"
wget -O "$TESTDIR/themoviedb-search-iron-man.html" "http://api.themoviedb.org/2.1/Movie.search/en/xml/57983e31fb435df4df77afb854740ea9/Iron+Man"
wget -O "$TESTDIR/themoviedb-film-1726.html" "http://api.themoviedb.org/2.1/Movie.getInfo/en/xml/57983e31fb435df4df77afb854740ea9/1726"
wget -O "$TESTDIR/imdb-tt0371746.html" "http://akas.imdb.com/title/tt0371746/"
wget -O "$TESTDIR/themoviedb-images-1726.html"  "http://api.themoviedb.org/2.1/Movie.getImages/en/xml/57983e31fb435df4df77afb854740ea9/1726"

#TEMP_DIR=`mktemp -d`
#git clone git://github.com/xbmc/xbmc.git $TEMP_DIR
#cd $TEMP_DIR
#git checkout addons
#zip -r9 xbmc-addons.zip addons
#cd -
#mv $TEMP_DIR/xbmc-addons.zip ../tests/org/stanwood/media/source/xbmc/
#rm -rf $TEMP_DIR
