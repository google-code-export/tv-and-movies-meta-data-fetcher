#!/usr/bin/ruby -W0

require 'tmpdir'
require 'fileutils'

################# Functions ##############

def checkoutProject()
    system("osc checkout home:sunny007/MediaInfoFetcher-nightlybuild")
end

def copyFileToProject(src,dest)
    FileUtils.cp(src,dest)
    system("osc add #{dest}")
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

def readChangeLog(projectDir)
    changeLog = ""
    File.open(projectDir+"/Changelog").each_line { |line|
        changeLog = changeLog+line
    }
    return changeLog
end

################## Main ##################

## TODO: 
##   * Perform build of a the project
##   * Use location of this script to work out the projectDir

projectDir=Dir.getwd()+"/.."
date=Time.new.strftime("%Y%m%d%H%M%S")
version=readVersion(projectDir)

params=Hash["version" => version, "release" => date,"changelog" => readChangeLog(projectDir)]

if (version==nil)
    $stderr.puts("Unable to read project version")
    exit(1)
else
    puts "Uploading version: #{version}"
end

Dir.mktmpdir("osc") { |dir|
    Dir.chdir(dir)
    checkoutProject()
    Dir.chdir("home:sunny007/MediaInfoFetcher-nightlybuild")
    
    Dir.glob("*.zip").each { |file|
        system("osc delete #{file}")
    }

    copyAndUpdateFile("#{projectDir}/etc/opensuse-nightly.spec","MediaInfoFetcher.spec",params); 
    copyFileToProject("#{projectDir}/dist/MediaInfoFetcher-#{version}-src.zip","MediaInfoFetcher-#{version}-#{date}-src.zip"); 

    system("osc commit -m \"nightly upload #{version}-#{date}\"")
}

exit(0)
