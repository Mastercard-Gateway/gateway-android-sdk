package com.mastercard.gateway.android.sdkold;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class AbstractResourceTest {
    protected static String slurpCondensed( String filename ) throws IOException {
        BufferedReader br = null;

        try {
            br = new BufferedReader( new FileReader( filename ) );
            StringBuilder sb = new StringBuilder();
            String line;

            while ( ( line = br.readLine() ) != null ) {
                sb.append( line.trim() );
            }

            return sb.toString();
        }
        finally {
            try {
                if ( br != null ) {
                    br.close();
                }
            }
            catch ( Exception e ) {
                /* No worries */
            }
        }
    }
}
