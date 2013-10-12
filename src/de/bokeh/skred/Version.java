package de.bokeh.skred;

import java.util.Properties;


/**
 * Access to the program's version numbers.
 */
public final class Version {

    private static Properties ps = null;
    
    private static synchronized void getVersionProperties() {
        if (ps == null) {
            ps = new Properties();
            java.io.InputStream is = Version.class.getResourceAsStream("version.properties");
            if (is != null) {
                try {
                    ps.load(is);
                } catch (java.io.IOException ex) {
                    // ignore, properties remain empty
                }
            }
        }
    }

    private static int getVersion(String level) {
        getVersionProperties();
        String v = ps.getProperty("version." + level);
        if (v == null)
            return -1;
        return Integer.parseInt(v);
    }
    
    public static boolean isVersionAvailable() {
        getVersionProperties();
        return ps.containsKey("version.major");
    }
    
    public static int getMajorVersion() {
        return getVersion("major");
    }
    
    public static int getMinorVersion() {
        return getVersion("minor");
    }
    
    public static int getPatchVersion() {
        return getVersion("patch");
    }
    
    public static int getBuildNumber() {
        return getVersion("build");
    }
    
    public static String getVersionString() {
        if (!isVersionAvailable())
            return null;
        return getMajorVersion() + "." + getMinorVersion() + "." + getPatchVersion() + "." + getBuildNumber();
    }

    public static String getLongVersionString() {
        return (getVersionString() + " (" + ps.getProperty("build.user") + "@" + ps.getProperty("build.host") +
                " " + ps.getProperty("build.timestamp") + ")");
    }
}
