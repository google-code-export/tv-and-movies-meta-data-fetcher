#!/usr/bin/ruby -W0

require 'tmpdir'
require 'fileutils'

################# Functions ##############

def executeCmd(cmd)
    system(cmd)
    if ($?.exitstatus!=0)
        $stderr.puts("Unable to execute command #{cmd}")
        exit(2)
    end
end

def checkoutProject()
    executeCmd("osc --no-keyring checkout home:sunny007/MediaInfoFetcher-nightlybuild")
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
            out.puts(line)
        }
    }    
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
     
    executeCmd("ant dist")
end

################## Main ##################

projectDir=File.expand_path(File.dirname(__FILE__))+"/.."
date=Time.new.strftime("%Y%m%d%H%M%S")

if (ARGV[0]==nil && ARGV[0]!="--skip-build")
    doBuild(projectDir)
end

version=readVersion(projectDir)

params=Hash[
  "version" => version, 
  "release" => date,
  "changelog" => readFile(projectDir,"Changelog"),
  "sourcefile" => "MediaManager-#{version}-#{date}-src.zip",
  "description" => readFile(projectDir,"Description")
]

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
        File.delete(file)
    }

    copyAndUpdateFile("#{projectDir}/build/specs/opensuse-nightly.spec","MediaManager.spec",params) 
    copyFileToProject("#{projectDir}/dist/MediaManager-#{version}-notests-src.zip","MediaManager-#{version}-#{date}-src.zip") 

    executeCmd("osc --no-keyring addremove")
    executeCmd("osc --no-keyring commit -m \"nightly upload #{version}-#{date}\"")
    executeCmd("osc --no-keyring rebuild home:sunny007/MediaInfoFetcher-nightlybuild")
}

exit(0)
