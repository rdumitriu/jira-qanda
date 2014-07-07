/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * ResourceUtils.java
 *
 * Created  2005/03/23 02:16:12
 */

package ro.agrade.jira.qanda.utils;

import java.net.*;

import java.io.*;
import java.util.*;
import java.util.jar.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.w3c.dom.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * The resources utils. Contains utilities to find and parse a file, either
 * XML or a properties one.
 * </p>
 * <p>
 * It also contain utilities for the JAR files. Getting a JAR entry has never
 * been so simple.
 * </p>
 * @author Radu Dumitriu
 */
public final class ResourceUtils {
    
    private static final Log LOG = LogFactory.getLog(ResourceUtils.class);
    
    /** Never try to instantiate this */
    private ResourceUtils() {
    }


    /**
     * Gets a resource as URL. A resource means a simple resource in the
     * classpath or a plain URL
     *
     * @param resFileName the resource name
     * @param loaderClazz the loader class
     * @return the URL, or null if URL verification failed
     */
    public static URL getResourceAsURL(String resFileName,
                                       Class<?> loaderClazz) {
        URL url = null;

        if(resFileName.startsWith("/")) {
            url = loaderClazz.getResource(resFileName);
            if(LOG.isDebugEnabled()) {
                if(url == null) {
                    LOG.debug(String.format("Resource %s is not a resource.",
                                            resFileName));
                }
            }
        } else {
            try {
                url = new URL(resFileName);
            } catch (MalformedURLException ex) {
                LOG.debug(String.format("Resource %s is not a URL resource.",
                                        resFileName));
            }
        }
        return url;
    }

    /**
     * Gets a resource as URL. It uses the current class loader
     *
     * @param resFileName the resource name
     * @return the URL, or null if URL verification failed
     */
    public static URL getResourceAsURL(String resFileName) {
        return getResourceAsURL(resFileName, ResourceUtils.class);
    }

    /**
     * Gets the resource
     *
     * @param resFileName file path, URL, or internal resource denominator.
     * @param loaderClazz the loader class
     * @throws IOException if something goes wrong.
     * @return the configuration as an input stream
     */
    public static InputStream getAsInputStream(String resFileName,
                                               Class<?> loaderClazz)
                                                        throws IOException {
        //first, try to see if this is a file
        File f = new File(resFileName);
        if(f.exists()) {
            return new BufferedInputStream(new FileInputStream(f));
        }
        
        //fallback to URL config
        URL configurationUrl = null;
        if(resFileName.startsWith("/")) {
            configurationUrl = loaderClazz.getResource(resFileName);
            if(configurationUrl == null) {
                String msg = String.format(
                        "The resource >>%s<< is not a valid resource.", 
                        resFileName);
                LOG.error(msg);
                throw new java.io.IOException(msg);
            }
        } else {
            try {
                configurationUrl = new URL(resFileName);
            } catch (MalformedURLException ex) {
                String msg = String.format(
                        "The resource >>%s<< is not a valid URL.", 
                        resFileName);
                LOG.error(msg, ex);
                throw new IOException(msg);
            }
        }
        
        //we have the URL, connect on it ...
        URLConnection conn = configurationUrl.openConnection();
        conn.connect();
        return conn.getInputStream();
    }

    /**
     * Gets the resource
     *
     * @param resFileName file path, URL, or internal resource denominator.
     * @throws IOException if something goes wrong.
     * @return the configuration as an input stream
     */
    public static InputStream getAsInputStream(String resFileName)
                                                        throws IOException {
        return getAsInputStream(resFileName, ResourceUtils.class);
    }

    /**
     * Gets the configuration resource, as properties file
     * @param resFileName file path, URL, or internal resource denominator.
     * @throws IOException if the properties cannot be loaded, for whatever
     * reason
     * @return the properties
     */
    public static Properties getAsProperties(String resFileName)
                                                            throws IOException {
       return getAsProperties(resFileName, ResourceUtils.class); 
    }
    
    /**
     * Gets the configuration resource, as properties file
     * @param resFileName file path, URL, or internal resource denominator.
     * @param loaderClazz the loader class
     * @throws IOException if the properties cannot be loaded, for whatever 
     * reason
     * @return the properties
     */
    public static Properties getAsProperties(String resFileName,
                                             Class<?> loaderClazz)
                                                            throws IOException {
        InputStream is = null;
        
        try {
            is = getAsInputStream(resFileName, loaderClazz);
            Properties p = new Properties();
            p.load(is);
            return p;
        } finally {
            if(is != null) try { is.close(); } catch(IOException ex) {}
        }
    }
    
    /**
     * Gets the configuration resource, as properties file. You should read the
     * corresponding standard documentation, and check the DTD found at:
     * <pre>
     * &lt;!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd"&gt;
     * </pre>
     * 
     * @return the properties
     * @param resFileName file path, URL, or internal resource denominator.
     * @param loaderClazz the loader class
     * @throws java.util.InvalidPropertiesFormatException if the XML format is 
     * invalid
     * @throws IOException if the properties cannot be loaded, for whatever 
     * reason
     */
    public static Properties getAsXMLProperties(String resFileName,
                                                Class<?> loaderClazz)
                throws IOException {
        InputStream is = null;
        
        try {
            is = getAsInputStream(resFileName, loaderClazz);
            Properties p = new Properties();
            p.loadFromXML(is);
            return p;
        } finally {
            if(is != null) try { is.close(); } catch(IOException ex) {}
        }
    }

    /**
     * Gets the configuration resource, as properties file. You should read the
     * corresponding standard documentation, and check the DTD found at:
     * <pre>
     * &lt;!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd"&gt;
     * </pre>
     *
     * @return the properties
     * @param resFileName file path, URL, or internal resource denominator.
     * @throws java.util.InvalidPropertiesFormatException if the XML format is
     * invalid
     * @throws IOException if the properties cannot be loaded, for whatever
     * reason
     */
    public static Properties getAsXMLProperties(String resFileName)
                                                            throws IOException {
        return getAsXMLProperties(resFileName, ResourceUtils.class);
    }
    
    
    
    /**
     * Gets the configuration resource, as XML
     * @return the DOM structure of the document
     * @param resFileName the resource name
     * @param loaderClazz the loader class
     * @throws org.xml.sax.SAXException if parsing failed
     * @throws javax.xml.parsers.ParserConfigurationException if parser is 
     * not correctly configured
     * @throws IOException if the config cannot be loaded, for whatever reason
     */
    public static Document getAsXML(String resFileName, Class<?> loaderClazz)
                throws IOException, SAXException, ParserConfigurationException {
        InputStream is = null;
        
        try {
            is = getAsInputStream(resFileName, loaderClazz);
            DocumentBuilderFactory factory = 
                                        DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(is);
        } finally {
            if(is != null) try { is.close(); } catch(IOException ex) {}
        }
    }

    /**
     * Gets the configuration resource, as XML
     * @return the DOM structure of the document
     * @param resFileName the resource name
     * @throws org.xml.sax.SAXException if parsing failed
     * @throws javax.xml.parsers.ParserConfigurationException if parser is
     * not correctly configured
     * @throws IOException if the config cannot be loaded, for whatever reason
     */
    public static Document getAsXML(String resFileName)
                throws IOException, SAXException, ParserConfigurationException {
        return getAsXML(resFileName, ResourceUtils.class);
    }


    /**
     * Gets an entry in the JAR. It's your responsability to close the input
     * stream, as usually
     * @param jar the jar file
     * @param jarEntryName the jar entry name
     * @return the input stream, or null if no entry is found in the file
     * @throws IOException if the jar is invalid
     */
    public static InputStream getJarInputStream(JarFile jar,
                                                String jarEntryName)
                                                        throws IOException {
        if(LOG.isDebugEnabled()) {
            LOG.debug(String.format("Searching in JAR >>%s<<, entry >>%s<<",
                                    jar.getName(),
                                    jarEntryName));
        }
        JarEntry je = jar.getJarEntry(jarEntryName);
        if(je == null) {
            if(LOG.isDebugEnabled()) {
                LOG.debug(String.format("No such entry [%s] in JAR >>%s<<",
                                        jarEntryName, jar.getName()));
            }
            return null;
        }
        return jar.getInputStream(je);
    }

    /**
     * Gets an entry in the JAR. It's your responsability to close the
     * input stream, as usually
     * @param file a file which is actually the jar file
     * @param jarEntryName the jar entry name
     * @return the input stream, or null if no entry is found in the file
     * @throws IOException if the jar is invalid
     */
    public static InputStream getJarInputStream(File file,
                                                String jarEntryName)
                                                        throws IOException {
        return getJarInputStream(new JarFile(file), jarEntryName);
    }
}

