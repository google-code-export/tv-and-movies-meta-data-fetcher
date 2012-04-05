require 'itunesController/config'
require 'itunesController/dummy_itunescontroller'
require 'itunesController/controllserver'
require 'itunesController/version'
require 'itunesController/cachedcontroller'
require 'itunesController/database/backend'

require 'java'

class ResultSet
    
    def initialize(rs)
        if (rs==nil)
            @rs = nil
        else
            @rs = rs
            @colCount = @rs.getMetaData().getColumnCount()
        end
        
    end
    
    def next
        if (@rs == nil)
            return nil
        end
        if (@rs.next)            
            row = []
            for i in (1..@colCount)
                value=@rs.getObject(i)
                puts value
                row.push(value)    
            end
            
            return row
        end
        return nil
    end
end

class Statement
    
    def initialize(db,sql)        
        @sql = sql
        @stmt = db.prepareStatement(sql)                 
    end
    
    def execute(*args)
        count=1
        args.each do | arg |
            if (arg.kind_of? Integer)
                @stmt.setInt(count,arg)
            elsif (arg.kind_of? String)
                @stmt.setString(count,arg)
            else                
                raise "Unsupported argument type: " + arg.class
            end
            
            count+=1
        end
        if (@sql.start_with?("select"))
            return ResultSet.new(@stmt.executeQuery)            
        else
            @stmt.executeUpdate
            return ResultSet.new(nil)
        end
        
    end
end

class HSQLBackend < ItunesController::DatabaseBackend                
    
    def initialize()  
        @db = java.sql.DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb", "SA", "")      
    end
    
    def prepare(sql)
        sql=translateSQL(sql)       
        return Statement.new(@db,sql)
    end
    
    def execute(sql)                
        stmt=@db.createStatement()        
        sql = translateSQL(sql)        
        stmt.executeUpdate(sql)
        return nil              
    end
    
    def translateSQL(sql)
        sql = org.stanwood.media.store.mp4.itunes.SQLiteToHSQLTranslater::translateSQL(sql)
        puts(sql)        
        return sql
    end
    
    def close()
        @db.close()
    end    
end

def launchServer(configPath,port)
    begin    
        ItunesController::DummyITunesController::resetCommandLog()
        ItunesController::DummyITunesController::resetTracks()
        itunes=ItunesController::DummyITunesController.new()
    
        dbBackend = HSQLBackend.new    
        controller = ItunesController::CachedController.new(itunes,dbBackend)
        config=ItunesController::ServerConfig.readConfig(configPath)
        
        server=ItunesController::ITunesControlServer.new(config,port,controller)        
        return server
    rescue => exc                
        ItunesController::ItunesControllerLogging::error("Unable to execute command",exc)                        
        raise exc.exception(exc.message)
    end
end

