#!/usr/bin/ruby -W0

require 'tmpdir'
require 'fileutils'

################# Functions ##############

def checkoutProject()
    system("osc checkout home:sunny007/MediaInfoFetcher-nightlybuild")
end

def copyFileToProject(src,dest)
    FileUtils.cp(src,dest)
end

def copyAndUpdateFile(src,dest,params)
    File.open(dest, 'w') { |out|  
        File.open(src).each_line { |line|
            params.each { |key, value|
                line=line.gsub("%%#{key}%%",value)
            }
            puts(line)
            out.puts(line)
        }
    }
    system("osc add #{dest}")
end

def readVersion(projectDir) 
    File.open(projectDir+"/VERSION").each_line { |line| 
        if /^.* (.*)$/ =~ line
            return $1
        end
    }
    return nil
end

def readFile(projectDir,filename)
    changeLog = ""
    File.open(projectDir+"/"+filename).each_line { |line|
        changeLog = changeLog+line
    }
    return changeLog
end

def doBuild(projectDir)
     Dir.chdir(projectDir)
     
     system("ant dist")
end

################## Main ##################


projectDir=File.expand_path(File.dirname(__FILE__))+"/.."
date=Time.new.strftime("%Y%m%d%H%M%S")
version=readVersion(projectDir)

params=Hash[
  "version" => version, 
  "release" => date,
  "changelog" => readFile(projectDir,"Changelog"),
  "sourcefile" => "MediaInfoFetcher-#{version}-#{date}-src.zip",
  "description" => readFile(projectDir,"Description")
]

if (version==nil)
    $stderr.puts("Unable to read project version")
    exit(1)
else
    puts "Uploading version: #{version}"
end

doBuild(projectDir)

Dir.mktmpdir("osc") { |dir|
    Dir.chdir(dir)
    checkoutProject()
    Dir.chdir("home:sunny007/MediaInfoFetcher-nightlybuild")
    
    Dir.glob("*.zip").each { |file|
        File.delete(file)
    }

    copyAndUpdateFile("#{projectDir}/etc/opensuse-nightly.spec","MediaInfoFetcher.spec",params) 
    copyFileToProject("#{projectDir}/dist/MediaInfoFetcher-#{version}-src.zip","MediaInfoFetcher-#{version}-#{date}-src.zip") 

    system("osc addremove")
    system("osc commit -m \"nightly upload #{version}-#{date}\"")
    system("osc rebuild home:sunny007/MediaInfoFetcher-nightlybuild")
}

exit(0)
