package gov.lanl.archive.webapp;

import java.util.HashMap;
import java.util.Map;

public class Args {
	private final String[] args;
	private final Map<String, String> map;
		   
		    public Args( String[] args )
		    {
		        this.args = args;
		        this.map = parseArgs( args );
		    }
		   
		    public Map<String, String> asMap()
		    {
	        return new HashMap<String, String>( this.map );
		    }
		   
		    public String get( String key, String defaultValue )
		    {
		        String value = this.map.get( key );
		        return value != null ? value : defaultValue;
		    }
		   
		    public Number getNumber( String key, Number defaultValue )
		    {
		        String value = this.map.get( key );
		        return value != null ? Double.parseDouble( value ) : defaultValue;
		    }
		   
		    public Boolean getBoolean( String key, Boolean defaultValue )
		    {
		        String value = this.map.get( key );
		        return value != null ? Boolean.parseBoolean( value ) : defaultValue;
		    }
		   
		    private static boolean isOption( String arg )
		    {
		        return arg.startsWith( "-" );
		    }
		   
		    private static String stripOption( String arg )
		    {
		        while ( arg.length() > 0 && arg.charAt( 0 ) == '-' )
		        {
		            arg = arg.substring( 1 );
		        }
		        return arg;
		    }
		
		    private static Map<String, String> parseArgs( String[] args )
		    {
		        Map<String, String> map = new HashMap<String, String>();
		        for ( int i = 0; i < args.length; i++ )
		        {
		            String arg = args[ i ];
		            if ( isOption( arg ) )
		            {
		                arg = stripOption( arg );
		                int equalIndex = arg.indexOf( '=' );
		                if ( equalIndex != -1 )
		                {
		                    String key = arg.substring( 0, equalIndex );
		                    String value = arg.substring( equalIndex + 1 );
		                    map.put( key, value );
		                }
		                else
		                {
		                    String key = arg;
		                    int nextIndex = i + 1;
		                    String value = nextIndex < args.length ?
		                        args[ nextIndex ] : null;
		                    value = value == null || isOption( value ) ? null : value;
		                    map.put( key, value );
		                }
		            }
		        }
		        return map;
		    }
}
