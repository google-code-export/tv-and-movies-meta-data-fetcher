#
# Regular cron jobs for the mediamanager package
#
0 4	* * *	root	[ -x /usr/bin/mediamanager_maintenance ] && /usr/bin/mediamanager_maintenance
