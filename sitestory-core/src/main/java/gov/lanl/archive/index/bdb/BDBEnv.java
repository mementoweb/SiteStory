package gov.lanl.archive.index.bdb;
import gov.lanl.archive.ArchiveConfig;

import java.io.File;
import com.sleepycat.je.CheckpointConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryDatabase;


public class BDBEnv {
	
	  protected static final String RESOURCE_DATABASE = "resource_record";
	//  public static final String DB_PATH = System.getProperty("ta.storage.basedir", "target/db" ); 
	  public static final String DB_PATH = ArchiveConfig.prop.get("ta.storage.basedir");
	 // protected String databaseDirectory = System.getProperty("ta.index.basedir",  DB_PATH  + File.separator+"bdbindex" );
	 
	  protected String databaseDirectory ;
	 // protected String databaseDirectory = System.getProperty("ta.index.basedir", "/Users/ludab/projects/bdbindextest" );
	   // protected static Logger log = Logger.getLogger(BDBEnv.class.getName());
	    protected Environment env;
	 //   protected Database rangeDb = null;
	    protected Database recDb = null;
	   // protected Database digestDb = null;
	  //  protected Database lastdateDb = null;
	    protected String dbDir;
	  //  protected SecondaryDatabase DigestIndexDb =null;
	    protected SecondaryDatabase DateIndexDb = null;
	    protected SecondaryDatabase UrlIndexDb = null;
	    protected SecondaryDatabase IdIndexDb = null;
	    protected  Database urlIndexPrime = null;
	    protected Database  headersBlob = null;
	    protected Database archiveStats = null;
	   // private StoredClassCatalog classCatalog;
	  //  private Database classCatalogDb = null;
	//    protected static TupleBinding resourceBinding = new ResourceBinding();
	    
	    public BDBEnv(String dbDir, boolean readOnly) {
		//  System.out.println("r100_" +dbDir);
	          
	    	if (ArchiveConfig.prop.containsKey("ta.index.basedir")) {
	    		databaseDirectory= ArchiveConfig.prop.get("ta.index.basedir");
	    	}
	    	else {
	    		databaseDirectory = DB_PATH  + File.separator + "bdbindex";
	    	}
	        openEnv(dbDir, readOnly);
	        openDatabases(readOnly);
	    }

	    public void openEnv(String dbDir, boolean readOnly) {
	        try {
	            //System.out.println("r10_" );
	            if (!readOnly && !System.getProperty("os.name").contains("Win") && new File(dbDir).exists()) {
	            	try {  //System.out.println("r10__" );
						Runtime.getRuntime().exec("chmod -R 755 " + dbDir).waitFor();
					} catch (Exception e) {
						e.printStackTrace();
					}
	            	//log.debug("Changed permissions for 755 for: " + dbDir);
	            }
	          //  System.out.println("r10" );
	        	this.dbDir = dbDir;
				File dir = new File(dbDir);
				if (!readOnly) {
					if (!dir.exists()) {
						if (readOnly) { // Don't create database if read-only
						   // System.out.println("r12");
							//ErrorHandler.error("Database directory " + dbDir
								//	+ " does not exist;\nit will not be created"
									//+ " because the read-only flag is set");
						}
						if (!dir.mkdirs()) {
						    //System.out.println("r13");
							//ErrorHandler.error("Database directory " + dbDir
								//	+ " does not exist;\nan attempt to create"
									//+ " it failed");
						}
						 //System.out.println("r14");
					}
				}
				long s = System.currentTimeMillis();
				 // System.out.println("r11" +s);
				EnvironmentConfig config = new EnvironmentConfig();
				config.setAllowCreate(!readOnly);
				config.setTransactional(!readOnly);
				config.setReadOnly(readOnly);
				//config.setConfigParam("java.util.logging.FileHandler.on", "false");
				//config.setConfigParam("java.util.logging.ConsoleHandler.on", "true");
				//config.setConfigParam("java.util.logging.DbLogHandler.on", "false");
				/*
				if (readOnly) {
					System.out.println("read only config");
					//config.setConfigParam("je.env.runEvictor", "false");
					config.setConfigParam("je.env.runINCompressor", "false");
					config.setConfigParam("je.env.runCheckpointer", "false");
					//config.setConfigParam("je.env.runCleaner", "false");
					config.setConfigParam("je.log.checksumRead", "false");
					config.setConfigParam("je.env.sharedLatches", "false");
					config.setLocking(false);
				} else {
					//config.setConfigParam("je.cleaner.minUtilization", "90");
					config.setConfigParam("je.env.sharedLatches", "false");
					
				}
				*/
				env = new Environment(dir, config);
				//log.info("openEnv(): " +  dbDir + " | "  + (System.currentTimeMillis() - s));
			} catch (DatabaseException dbe) {
			    dbe.printStackTrace();
				//ErrorHandler.error("openEnvironment", dbe);
				//dbe.printStackTrace();
			} catch (Exception e) {
			    e.printStackTrace();
				//ErrorHandler.error("openEnvironment", e);
				//e.printStackTrace();
			}
	    }

	    protected void openDatabases(boolean readOnly) {
			long s = System.currentTimeMillis();
			/*
			if (rangeDb == null) {
				 DatabaseConfig config = new DatabaseConfig();
			        config.setAllowCreate(!readOnly);
			        config.setTransactional(!readOnly);
			        config.setReadOnly(readOnly);
			      	config.setSortedDuplicates(true);
			        rangeDb = env.openDatabase(null, "RangeDB", config);	
	                //rangeDb = openDatabase(readOnly,"RangeDB");
			    log.debug("openDatabases(): idDb "  + (System.currentTimeMillis() - s));
			    s = System.currentTimeMillis();
			}
			*/
			if (recDb == null) {
				 DatabaseConfig config = new DatabaseConfig();
			        config.setAllowCreate(!readOnly);
			        config.setTransactional(!readOnly);
			        config.setReadOnly(readOnly);
			      	config.setSortedDuplicates(false);
			        recDb = env.openDatabase(null, "RecDB", config);
	            //recDb = openDatabase(readOnly,"RecDB");
			  //  log.debug("openDatabases(): idDb "  + (System.currentTimeMillis() - s));
			    s = System.currentTimeMillis();
			   
			   // String classcatalogdb = databaseDirectory + "/" + "ClassCatalogDB.db";
	        //    classCatalogDb = new Database(classcatalogdb, null,config);
	            
	          //  classCatalog = new StoredClassCatalog(classCatalogDb);
			    
			    DateKeyCreator fnkc = new DateKeyCreator();
	            SecondaryConfig mySecConfig = new SecondaryConfig();
	           // mySecConfig.setAllowPopulate(true);
	            mySecConfig.setAllowCreate(true);
	            mySecConfig.setKeyCreator(fnkc);
	            mySecConfig.setSortedDuplicates(true);
	            mySecConfig.setTransactional(true);
	            DateIndexDb =  env.openSecondaryDatabase(null,"acessdateindex", recDb,  mySecConfig);
	            
	            
	            
	          // DateIndexDb = new SecondaryDatabase(databaseDirectory + "/dateindex",null,recDb,mySecConfig);
	         //   DigestKeyCreator dkc = new DigestKeyCreator(new ResourceBinding());
	          //  SecondaryConfig mySecDConfig = new SecondaryConfig();
	           // mySecDConfig.setAllowPopulate(true);
	            //mySecDConfig.setAllowCreate(true);
	            //mySecDConfig.setKeyCreator(dkc);
	            //mySecDConfig.setSortedDuplicates(true);
	            //mySecDConfig.setTransactional(true);
	            //DigestIndexDb =  env.openSecondaryDatabase(null, "digestindex", recDb,  mySecDConfig);
	          
	            UrlKeyCreator ukc = new UrlKeyCreator(new ResourceBinding());
	            SecondaryConfig mySecUConfig = new SecondaryConfig();
	            mySecUConfig.setAllowPopulate(true);
	            mySecUConfig.setAllowCreate(true);
	            mySecUConfig.setKeyCreator(ukc);
	            mySecUConfig.setSortedDuplicates(true);
	            mySecUConfig.setTransactional(true);
	            UrlIndexDb =  env.openSecondaryDatabase(null, "urlindex", recDb,  mySecUConfig);
	          
	            IdKeyCreator ikc = new IdKeyCreator(new ResourceBinding());
	            SecondaryConfig mySecIConfig = new SecondaryConfig();
	            mySecIConfig.setAllowPopulate(true);
	            mySecIConfig.setAllowCreate(true);
	            mySecIConfig.setKeyCreator(ikc);
	            mySecIConfig.setSortedDuplicates(true);
	            mySecIConfig.setTransactional(true);
	            IdIndexDb =  env.openSecondaryDatabase(null, "idindex", recDb,  mySecIConfig);
	            
	            
			}
			
		/*	if (digestDb == null) {
				
				 DatabaseConfig config = new DatabaseConfig();
			        config.setAllowCreate(!readOnly);
			        config.setTransactional(!readOnly);
			        config.setReadOnly(readOnly);
			      	config.setSortedDuplicates(false);
			         digestDb = env.openDatabase(null, "DigestDB", config);
	            //recDb = openDatabase(readOnly,"RecDB");
			    log.debug("openDatabases(): idDb "  + (System.currentTimeMillis() - s));
			    s = System.currentTimeMillis();
			}
			*/

			if (urlIndexPrime == null) {
			
				 DatabaseConfig config = new DatabaseConfig();
			        config.setAllowCreate(false);
			        config.setTransactional(true);
			        config.setReadOnly(readOnly);
			        config.setSortedDuplicates(true);
			        urlIndexPrime =  env.openDatabase(null, "urlindex", config);       
			}
			
			if ( headersBlob == null) {
				 DatabaseConfig config = new DatabaseConfig();
			        config.setAllowCreate(true);
			        config.setTransactional(true);
			        config.setSortedDuplicates(false);
			        System.out.println("open blob db");
			        headersBlob =  env.openDatabase(null, "headersblob", config);
			}
			
			if (archiveStats==null) {
				 DatabaseConfig config = new DatabaseConfig();
			        config.setAllowCreate(true);
			        config.setTransactional(true);
			        config.setSortedDuplicates(false);
			        archiveStats  =  env.openDatabase(null, "archivestats", config);
			}
			
	/*	if (lastdateDb == null) {
				
				 DatabaseConfig config = new DatabaseConfig();
			        config.setAllowCreate(!readOnly);
			        config.setTransactional(!readOnly);
			        config.setReadOnly(readOnly);
			      	config.setSortedDuplicates(false);
			         lastdateDb = env.openDatabase(null, "LastDateDB", config);
	            //recDb = openDatabase(readOnly,"RecDB");
			  //  log.debug("openDatabases(): idDb "  + (System.currentTimeMillis() - s));
			   // s = System.currentTimeMillis();
			}
			
		*/	
	        // BDB Stats
	        //StatsConfig config = new StatsConfig();
	        //config.setClear(true);
	        //try {
			//	System.err.println(dbEnvironment.getStats(config));
			//} catch (DatabaseException e) {
			//	e.printStackTrace();
			//}
	    }

	  public  Database openDatabase(boolean readOnly, String databaseName) {
	        DatabaseConfig config = new DatabaseConfig();
	        config.setAllowCreate(!readOnly);
	       // config.setTransactional(!readOnly);
	        config.setTransactional(true);
	        config.setReadOnly(readOnly);
	      
	        config.setSortedDuplicates(true);
	        Database db = null;
	        try {
	        // If readOnly is false, then the database config specifies that we
	        // can use transactions. When opening the database, the first argument
	        // is an optional transaction. If that argument is null and readOnly
	        // is false (transactional is true), then autocommit is in effect for
	        // this operation.
	        db = env.openDatabase(null, databaseName, config);
	        }
	        catch (DatabaseException dbe) {
	     //   ErrorHandler.error("openDatabase", dbe, false);
	        dbe.printStackTrace();
	        }
	        return db;
	    }
	                               
	    public Environment getEnv() { return env; }
	    public Database getResourceRecordDb() { return recDb; }
	  //  public Database getRangeRecordDb() { return rangeDb; }
	   // public Database getDigestDb() { return digestDb; }
	    //for serving feeds 
	   // public Database getlastdateDb() { return lastdateDb; }
	    public SecondaryDatabase getIndexDateDb() { return DateIndexDb; }
	    //public SecondaryDatabase getIndexDigestDb() { return DigestIndexDb; }
	    public SecondaryDatabase getIndexUrlDb() { return UrlIndexDb; }
	    public SecondaryDatabase getIndexIdDb() { return IdIndexDb; }
	    public Database getPrimaryUrlIndex(){return urlIndexPrime;}
	    public Database getHeadersBlob(){return headersBlob;}
	    public Database getArchiveStats(){return archiveStats;}
	    public void closeDatabases() throws Exception {
	        try {
	        	if (recDb != null){
	        		//DigestIndexDb.close();
	        		  DateIndexDb.close();
	        		  UrlIndexDb.close();
	        		  IdIndexDb.close();
	        	    recDb.close();
	        	    recDb = null;
	            }
	           // if (rangeDb != null) {
	        	//    rangeDb.close();
	            //    rangeDb = null;
	           // }
	            
	            if ( urlIndexPrime != null) {
	            	 urlIndexPrime.close();
	            	 urlIndexPrime = null;
	            }
	            if ( headersBlob != null) {
	            	 headersBlob.close();
	            	 headersBlob = null;
	            }
	            if ( archiveStats != null) {
	            	 archiveStats.close();
	            	 archiveStats = null;
	            }
	         //   if (digestDb != null) {
	        //	    digestDb.close();
	               // digestDb = null;
	          //  }
	         //   if (lastdateDb != null) {
	        	//    lastdateDb.close();
	              //  lastdateDb = null;
	            //}
	        } catch (DatabaseException ex) {
	        	//ErrorHandler.error("closeDatabases", ex, false);
	        	ex.printStackTrace();
	        }
	    }
	    
	    public void shutDown()  {
	        try {
	        	boolean readOnly = env.getConfig().getReadOnly();
	        	closeDatabases();
	        	if (!readOnly) {
	        		boolean changes = false;
	     	        while (env.cleanLog() > 0) {changes = true;}
	     	        if (changes) {
						CheckpointConfig force = new CheckpointConfig();
						force.setForce(true);
						env.checkpoint(force);
					}
	        	}
	            env.close();
	            env = null;
	            if (!readOnly && !System.getProperty("os.name").contains("Win")) {
	            	Runtime.getRuntime().exec("rm -f " + dbDir + "/je.lck");
	            	Runtime.getRuntime().exec("chmod -R 555 " + dbDir);
	            }
	        } catch (Exception ex) {
	        	//ErrorHandler.error("shutDown", ex, false);
	        	ex.printStackTrace();
	            //throw new IndexException("identifier db close error", ex);
	        } 
	    }

}
