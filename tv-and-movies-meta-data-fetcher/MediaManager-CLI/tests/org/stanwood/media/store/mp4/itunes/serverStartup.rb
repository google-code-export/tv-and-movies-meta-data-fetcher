require 'itunesController/config'
require 'itunesController/dummy_itunescontroller'
require 'itunesController/controllserver'
require 'itunesController/version'
require 'itunesController/cachedcontroller'
require 'itunesController/database/backend'
require 'itunesController/controller_creator'

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
                puts "setInt(#{count},#{arg})"
                @stmt.setInt(count,arg)
            elsif (arg.kind_of? String)                                                                             
                puts "setString(#{count},#{arg})"
                @stmt.setString(count,arg)
            else                
                $stderr.puts "ERROR: Unhandled type: #{count},#{arg.class}"
                raise "Unsupported argument type: #{arg.class}"
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
        stmt=@db.createStatement() 
        stmt.executeUpdate("DROP SCHEMA PUBLIC CASCADE")
    end
    
    def prepare(sql)
        sql=translateSQL(sql)       
        return Statement.new(@db,sql)
    end
    
    def execute(sql)                
        stmt=@db.createStatement()        
        sql = translateSQL(sql)
        begin        
            stmt.executeUpdate(sql)
        rescue java.sql.SQLIntegrityConstraintViolationException => e                                
            raise ItunesController::DatabaseConstraintException, e.message
        end
        return nil              
    end
    
    def executeStatement(stmt,*args)
        begin
            return stmt.execute(*args)
        rescue java.sql.SQLIntegrityConstraintViolationException => e                                
            raise ItunesController::DatabaseConstraintException, e.message
        end               
    end
    
    def translateSQL(sql)
        sql = org.stanwood.media.store.mp4.itunes.SQLiteToHSQLTranslater::translateSQL(sql)
        puts(sql)        
        return sql
    end
    
    def dropTables()
        execute("delete from tracks")
        execute("delete from dead_tracks")
        execute("delete from dupe_tracks")
        execute("delete from params")
    end
    
    def close()
        @db.close()
    end    
end

class TestStuff
        
    def self.setServer(server)
        @@server = server
    end
    
    def self.getServer()
        return @@server
    end
    
    def self.setController(controller)
        @@controller = controller
    end
    
    def self.getController()
        return @@controller
    end
    
    def self.setDB(db)
        @@db = db
    end
    
    def self.getDB()
        return @@db
    end
end

class DummyControllerCreator < ItunesController::ControllerCreator

    def initialize(controller)
        @controller = controller
    end

    def createController()
        return @controller
    end
end

def resetDummyServer()
    ItunesController::DummyITunesController::resetCommandLog()
    ItunesController::DummyITunesController::resetTracks()
    TestStuff::getDB().dropTables()
end

def launchServer(configPath,port)
    begin            
        ItunesController::DummyITunesController::resetCommandLog()
        ItunesController::DummyITunesController::resetTracks()
        itunes=ItunesController::DummyITunesController.new()
                
        dbBackend = HSQLBackend.new            
        controller=ItunesController::CachedController.new(itunes,dbBackend)
        TestStuff::setController(controller)
        config=ItunesController::ServerConfig.readConfig(configPath)  
        server=ItunesController::ITunesControlServer.new(config,port,DummyControllerCreator.new(controller))
        TestStuff::setServer(server)        
        TestStuff::setDB(dbBackend)

        return server
    rescue => exc                
        ItunesController::ItunesControllerLogging::error("Unable to execute command",exc)                        
        raise exc.exception(exc.message)
    end
end

