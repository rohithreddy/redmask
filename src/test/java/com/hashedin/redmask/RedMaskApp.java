package com.hashedin.redmask;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class RedMaskApp extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public RedMaskApp( String testName ) {
           super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( RedMaskApp.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp(){
        assertTrue( true );
    }
}
