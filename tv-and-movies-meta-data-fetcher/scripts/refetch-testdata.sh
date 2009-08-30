#!/bin/sh

TESTDIR="../tests/org/stanwood/media/testdata"

wget -O $TESTDIR/tagchimp-search-ironman.html "http://www.tagchimp.com/search/index.php?searchterm=iron+man&search.x=0&search.y=0&kind=mo1"
wget -O $TESTDIR/tagchimp-iron-man-17.html "http://www.tagchimp.com/tc/39752/"

wget -O $TESTDIR/filmposters/movieposter-ironman.html "http://eu.movieposter.com/cgi-bin/mpw8/search.pl?ti=Iron+Man&pl=action&th=y&rs=12&size=any"

wget -O $TESTDIR/eureka-search.html "http://www.tv.com/search.php?type=Search&stype=ajax_search&search_type=program&qs=eureka"
wget -O $TESTDIR/17552-summary.html "http://www.tv.com/heroes/show/17552/summary.html"
wget -O $TESTDIR/17552-episode_guide-printable=1.html "http://www.tv.com/heroes/show/17552/episode_guide.html?printable=1"
wget -O $TESTDIR/17552-episode_listings-season=3.html "http://www.tv.com/heroes/show/17552/episode.html?season=3"
wget -O $TESTDIR/58448-summary.html "http://www.tv.com/heroes/eureka/show/58448/summary.html"
wget -O $TESTDIR/58448-episode_guide-printable=1.html "http://www.tv.com/eureka/show/58448/episode_guide.html?printable=1"
wget -O $TESTDIR/58448-episode_listings-season=1.html "http://www.tv.com/eureka/show/58448/episode.html?season=1"
wget -O $TESTDIR/58448-episode_listings-season=2.html "http://www.tv.com/eureka/show/58448/episode.html?season=2"
wget -O $TESTDIR/58448-episode_listings-season=3.html "http://www.tv.com/eureka/show/58448/episode.html?season=3"
wget -O $TESTDIR/58448-episode_listings-season=4.html "http://www.tv.com/eureka/show/58448/episode.html?season=4"

wget -O $TESTDIR/film-0371746.html "http://www.imdb.com/title/tt0371746"
