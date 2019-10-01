package sedonac.translate;

/**
 * Translation options for C++
 */
public class CppOptions {
    private String sourceExt = "cpp";
    private String headerExt = "h";
    private String objectExt = "obj";

    /**
     * If true, a "using namespace std;" statement will be inserted in the source file.
     */
    private boolean usingStd = true;

    private boolean generateMethodImpl = true;

    /** If true, the Sedona test will also be translated */
    private boolean generateTests = false;
    /** If true, error check is disabled - USE WITH CARE! */
    private boolean disableErrorCheck = false;

    /**
     * Prefix for class members
     */
    private String memberPrefix = "";
    private String memberPostfix = "_";

    public CppOptions() {
    }

    public String getSourceExt() {
        return sourceExt;
    }

    public void setSourceExt(String sourceExt) {
        this.sourceExt = sourceExt;
    }

    public String getHeaderExt() {
        return headerExt;
    }

    public void setHeaderExt(String headerExt) {
        this.headerExt = headerExt;
    }

    public String getObjectExt() {
        return objectExt;
    }

    public void setObjectExt(String objectExt) {
        this.objectExt = objectExt;
    }

    public boolean requiresNamespaceInSource() {
        return !usingStd;
    }

    public void setUsingStd(boolean usingStd) {
        this.usingStd = usingStd;
    }

    public String getMemberPrefix() {
        return memberPrefix;
    }

    public void setMemberPrefix(String memberPrefix) {
        this.memberPrefix = memberPrefix;
    }

    public String getMemberPostfix() {
        return memberPostfix;
    }

    public void setMemberPostfix(String memberPostfix) {
        this.memberPostfix = memberPostfix;
    }

    public boolean isGenerateMethodImpl() {
        return generateMethodImpl;
    }

    public void setGenerateMethodImpl(boolean generateMethodImpl) {
        this.generateMethodImpl = generateMethodImpl;
    }

    public boolean isGenerateTests() {
        return generateTests;
    }

    public void setGenerateTests(boolean generateTests) {
        this.generateTests = generateTests;
    }

    public boolean isDisableErrorCheck() {
        return disableErrorCheck;
    }

    public void setDisableErrorCheck(boolean disableErrorCheck) {
        this.disableErrorCheck = disableErrorCheck;
    }

    @Override
    public String toString() {
        return "C++ {" +
                "sourceExt='" + sourceExt + '\'' +
                ", headerExt='" + headerExt + '\'' +
                ", objectExt='" + objectExt + '\'' +
                ", usingStd=" + usingStd +
                ", disableErrorCheck=" + disableErrorCheck +
                ", impl=" + generateMethodImpl +
                ", tests=" + generateTests +
                ", prefix='" + memberPrefix + '\'' +
                ", postfix='" + memberPostfix + '\'' +
                '}';
    }
}
