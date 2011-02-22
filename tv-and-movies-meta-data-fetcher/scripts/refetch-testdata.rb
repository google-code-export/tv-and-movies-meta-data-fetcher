#!/usr/bin/ruby -W0

require 'tmpdir'
require 'net/http'

################# Functions ##############

def downloadFile(url,dest)        
    url2 = URI.parse('http://www.example.com/index.html')
    
    Net::HTTP.start(url2.host,url2.port) { |http|
        resp = http.get(url2.path)
        open(dest, "wb") { |file|
            file.write(resp.body)
        }
    }
    puts "Downloaded #{url}"
end

################## Main ##################

projectDir=File.expand_path(File.dirname(__FILE__))+"/.."   
testsDir=projectDir+"/tests"
testDataDir=testsDir+"/org/stanwood/media/testdata"
    
downloadFile("http://www.thetvdb.com/api/GetSeries.php?seriesname=Heroes&language=en",testDataDir+"/tvdb-search-heroes.html")
downloadFile("http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip",testDataDir+"/tvdb-series-79501.zip" )
downloadFile("http://api.themoviedb.org/2.1/Movie.search/en/xml/57983e31fb435df4df77afb854740ea9/Iron+Man",testDataDir+"/themoviedb-search-iron-man.html")
downloadFile("http://api.themoviedb.org/2.1/Movie.getInfo/en/xml/57983e31fb435df4df77afb854740ea9/1726",testDataDir+"/themoviedb-film-1726.html")
downloadFile("http://akas.imdb.com/title/tt0371746/",testDataDir+"/imdb-tt0371746.html")
downloadFile("http://api.themoviedb.org/2.1/Movie.getImages/en/xml/57983e31fb435df4df77afb854740ea9/1726",testDataDir+"/themoviedb-images-1726.html")
downloadFile("http://api.themoviedb.org/2.1/Movie.imdbLookup/en/xml/57983e31fb435df4df77afb854740ea9/tt0371746",testDataDir+"/themoviedb-imdbLookup-tt0371746.html")

Dir.mktmpdir("updates") { |dir|
    Dir.chdir(dir)
    Dir.mkdir("addons")
    
    downloadFile("http://mirrors.xbmc.org/addons/dharma/addons.xml","addons/addons.xml" )
    downloadFile("http://mirrors.xbmc.org/addons/dharma/addons.xml.md5","addons/addons.xml.md5")
    downloadFile("http://mirrors.xbmc.org/addons/dharma/metadata.themoviedb.org/metadata.themoviedb.org-1.2.4.zip","addons/metadata.themoviedb.org/metadata.themoviedb.org-1.2.4.zip")
    downloadFile("http://mirrors.xbmc.org/addons/dharma/metadata.common.hdtrailers.net/metadata.common.hdtrailers.net-1.0.6.zip","addons/metadata.common.hdtrailers.net/metadata.common.hdtrailers.net-1.0.6.zip")
    downloadFile("http://mirrors.xbmc.org/addons/dharma/metadata.common.imdb.com/metadata.common.imdb.com-2.0.7.zip","addons/metadata.common.imdb.com/metadata.common.imdb.com-2.0.7.zip")
    downloadFile("http://mirrors.xbmc.org/addons/dharma/metadata.tvdb.com/metadata.tvdb.com-1.1.0.zip","addons/metadata.tvdb.com/metadata.tvdb.com-1.1.0.zip")
    downloadFile("http://mirrors.xbmc.org/addons/dharma/metadata.common.themoviedb.org/metadata.common.themoviedb.org-1.0.7.zip","addons/metadata.common.themoviedb.org/metadata.common.themoviedb.org-1.0.7.zip")
   
    Zip::ZipFile.open(testsDir+"/org/stanwood/media/source/xbmc/updates.zip", Zip::ZipFile::CREATE) do |zipfile|
        Find.find(dir) do |path|
            puts "adding to zip: " + path 
            Find.prune if File.basename(path)[0] == ?.            
            #zipfile.add(dest[1],path) if dest
        end 
    end
}