package test.sedonac;

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import sedonac.Main;

public class TestCompile {

    private static final String SEDONA_HOME_ENV = "SEDONA_HOME";
    private static final String SEDONA_HOME_PROP = "sedona.home";
    private static final String KIT_XML_BASE_DIR = "src/test/java";
    private static final String TRANSLATE_SUFFIX = ".translate.xml";

    private static final String[] TEST_CASES_SPECIAL = new String[] { "cctest" };

    @BeforeSuite
    public void checkSedonaHome() {
        String sedonaHome = System.getenv(SEDONA_HOME_ENV);
        Assert.assertNotNull (sedonaHome);
        System.setProperty(SEDONA_HOME_PROP, sedonaHome);
    }

    @Test
    public void testSys() {
        Assert.assertEquals(0, Main.doMain(new String[]{KIT_XML_BASE_DIR + "/" + "sys" + TRANSLATE_SUFFIX, "-v"}));
    }
	
	@Test
    public void testHelloWorld() {
        Assert.assertEquals(0, 0);
    }

}
