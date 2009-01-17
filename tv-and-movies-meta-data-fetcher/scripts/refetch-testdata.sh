#!/bin/sh

TESTDIR="../tests/org/stanwood/media/testdata"

wget -O $TESTDIR/tagchimp-search-ironman.html "http://www.tagchimp.com/search/index.php?searchterm=iron+man&search.x=0&search.y=0&kind=mo1"
wget -O $TESTDIR/tagchimp-iron-man-17.html "http://www.tagchimp.com/39752/"
