#!/usr/bin/ruby -W0

require 'tmpdir'
require 'fileutils'

VERSION="2.0"

################# Functions ##############

def executeCmd(cmd)
    system(cmd)
    if ($?.exitstatus!=0)
        $stderr.puts("Unable to execute command #{cmd}")
        exit(2)
    end
end

def checkoutProject()
    executeCmd("osc checkout home:sunny007/MediaManager")
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
     
    executeCmd("ant dist -Dproject.version=#{VERSION}")
end

################## Main ##################


projectDir=File.expand_path(File.dirname(__FILE__))+"/.."
date=Time.new.strftime("%Y%m%d%H%M%S")
version=readVersion(projectDir)

params=Hash[
  "version" => version, 
  "release" => date,
  "changelog" => readFile(projectDir,"Changelog"),
  "sourcefile" => "MediaManager-#{version}-src.zip",
  "description" => readFile(projectDir,"Description")
]

if (version==nil)
    $stderr.puts("Unable to read project version")
    exit(1)
else
    puts "Uploading version: #{VERSION}"
end

doBuild(projectDir)

Dir.mktmpdir("osc") { |dir|
    Dir.chdir(dir)
    checkoutProject()
    Dir.chdir("home:sunny007/MediaManager")
    
    Dir.glob("*.zip").each { |file|
        File.delete(file)
    }

    copyAndUpdateFile("#{projectDir}/build/specs/opensuse.spec","MediaManager.spec",params) 
    copyFileToProject("#{projectDir}/dist/MediaManager-#{version}-src.zip","MediaManager-#{version}-src.zip") 

    executeCmd("osc addremove")
    executeCmd("osc commit -m \"release upload #{version}\"")
    executeCmd("osc rebuild home:sunny007/MediaManager")
}

exit(0)
