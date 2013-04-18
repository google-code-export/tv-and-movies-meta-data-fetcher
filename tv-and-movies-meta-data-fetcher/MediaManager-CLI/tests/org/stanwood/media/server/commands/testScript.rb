
def onEventPreMediaImport(watchDirectory)
    $log.info("onEventPreMediaImport(#{watchDirectory})")
end

def onEventPostMediaImport(watchDirectory)
    $log.info("onEventPostMediaImport(#{watchDirectory})")
end

def onEventPreManageMedia(mediaDirectory)
    $log.info("onEventPreManageMedia(#{mediaDirectory})")
end

def onEventPostManageMedia(mediaDirectory)
    $log.info("onEventPostManageMedia(#{mediaDirectory})")
end

def getVideoName(video)
    $log.info("getVideoName(#{video.getTitle()})")
    return Nil
end
