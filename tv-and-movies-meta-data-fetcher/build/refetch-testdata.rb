#!/usr/bin/ruby -rubygems

gem 'rubyzip'
require 'tmpdir'
require 'net/http'
require 'fileutils'
require 'zip/zip'
require 'find'
#require 'zip/zipfilesystem'

################# Functions ##############

def error(msg)
    puts msg
    exit(1)
end

def doDownload(url,dest)
    url2 = URI.parse(url)    
    path = url2.path
    if url2.query != nil && url2.query != "" 
        path = path + "?"+url2.query
    end    
    
    res = Net::HTTP.start(url2.host,url2.port) { |http|
        resp = http.get(path)
        case resp
        when Net::HTTPSuccess
            open(dest, "wb") { |file|
                file.write(resp.body)
            }
        when Net::HTTPRedirection
            location = resp.header['location']
            doDownload(location,dest)
        else
            error("Failed to download #{url} retured error code #{resp.code}")
        end
    }
end

def downloadFile(url,dest)    
    dir = File.dirname(dest)
    FileUtils.mkdir_p dir

    doDownload(url,dest)            
    
    size = File.size(dest)
    puts "Downloaded #{url} at #{dest} with size #{size}"
end

def zip(archive)
    level = Zlib::BEST_COMPRESSION
    Zip::ZipOutputStream.open(archive) do |zip|
        Dir.glob("**/*") do |filename|
            puts "Zipping "+filename
            entry = Zip::ZipEntry.new("", filename)
            entry.gather_fileinfo_from_srcpath(filename)
            zip.put_next_entry(entry, nil, nil, Zip::ZipEntry::DEFLATED, level)
            entry.get_input_stream { |is| IOExtras.copy_stream(zip, is) }
        end
    end
    puts "Wrote zip file: "+archive
end

def zipWebsite(archive,urlBase,zipDirPrefix,files)
    Dir.mktmpdir("aboutToZip") { |dir|
        Dir.chdir(dir)
        
        files.each { |filename|
            downloadFile(urlBase+"/"+filename,zipDirPrefix+"/"+filename)
        }
        
        zip(archive)
    }
end

################## Main ##################

projectDir=File.expand_path(File.dirname(__FILE__))+"/.."   
testsDir=projectDir+"/tests"
testDataDir=testsDir+"/org/stanwood/media/testdata"
    
downloadFile("http://www.tagchimp.com/ape/search.php?token=11151451274D8F94339E891&type=search&title=Iron%20Man&totalChapters=X",testDataDir+"/tagchimp-search-iron-man.html");
downloadFile("http://www.tagchimp.com/ape/search.php?token=11151451274D8F94339E891&type=lookup&id=39752",testDataDir+"/tagchimp-film-39752.html");
downloadFile("http://www.thetvdb.com/api/GetSeries.php?seriesname=Heroes&language=en",testDataDir+"/tvdb-search-heroes.html")
downloadFile("http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip",testDataDir+"/tvdb-series-79501.zip" )
downloadFile("http://api.themoviedb.org/2.1/Movie.search/en/xml/57983e31fb435df4df77afb854740ea9/Iron+Man",testDataDir+"/themoviedb-search-iron-man.html")
downloadFile("http://api.themoviedb.org/2.1/Movie.getInfo/en/xml/57983e31fb435df4df77afb854740ea9/1726",testDataDir+"/themoviedb-film-1726.html")
downloadFile("http://akas.imdb.com/title/tt0371746/",testDataDir+"/imdb-tt0371746.html")
downloadFile("http://api.themoviedb.org/2.1/Movie.getImages/en/xml/57983e31fb435df4df77afb854740ea9/1726",testDataDir+"/themoviedb-images-1726.html")
downloadFile("http://api.themoviedb.org/2.1/Movie.imdbLookup/en/xml/57983e31fb435df4df77afb854740ea9/tt0371746",testDataDir+"/themoviedb-imdbLookup-tt0371746.html")

# Create a ziped update site of XBMC plugins
files=[
    "addons.xml",
    "addons.xml.md5",
    "metadata.themoviedb.org/metadata.themoviedb.org-1.2.4.zip",
    "metadata.common.hdtrailers.net/metadata.common.hdtrailers.net-1.0.6.zip",
    "metadata.common.imdb.com/metadata.common.imdb.com-2.0.7.zip",
    "metadata.tvdb.com/metadata.tvdb.com-1.1.0.zip",
    "metadata.common.themoviedb.org/metadata.common.themoviedb.org-1.0.7.zip"
]
zipWebsite(testsDir+"/org/stanwood/media/source/xbmc/updates.zip","http://mirrors.xbmc.org/addons/dharma","addons",files)
