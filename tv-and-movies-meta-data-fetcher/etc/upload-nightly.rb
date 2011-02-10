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
                line=line.gsub("@@#{key}@@",value)
                out.puts(line)
            }
        }
    }
    system("osc add #{dest}")
end

def readVersion(projectDir) 
   File.open(projectDir+"/VERSION").each_line { |line| 
      if /^.* (\d+\.\d+)$/ =~ line
        return $1
      end
   }
   return nil
end

################## Main ##################

projectDir=Dir.getwd()+"/.."
date=Time.new.strftime("%Y%m%d%H%M%S")
version=readVersion(projectDir)

params=Hash["version" => version, "release" => date]

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

    copyAndUpdateFile("#{projectDir}/etc/opensuse-nightly.spec","MediaInfoFetcher.spec",params); 
    copyFileToProject("#{projectDir}/dist/MediaInfoFetcher-#{version}-src.zip","MediaInfoFetcher-nightly-#{date}-src.zip"); 

    system("osc commit")
}

exit(0)
