package de.webfilesys.viewhandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;

import de.webfilesys.ViewHandlerConfig;
import de.webfilesys.util.ISO8601DateParser;

/**
 * GPS track file viewer.
 * @author Frank Hoehnel
 */
public class GeoTrackViewHandler implements ViewHandler
{
    private static final String STYLESHEET_REF = "<?xml-stylesheet type=\"text/xsl\" href=\"/webfilesys/xsl/gpxProfile.xsl\"?>";
    
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"yes\"?>";
    
    public void process(String filePath, ViewHandlerConfig viewHandlerConfig, HttpServletRequest req, HttpServletResponse resp)
    {
        try 
        {
            resp.setContentType("text/xml");

            PrintWriter xmlOut = resp.getWriter();
            
            BufferedReader gpxReader = new BufferedReader(new FileReader(filePath));
                	
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader parser = factory.createXMLStreamReader(gpxReader);
            
            boolean lastWasText = false;
            
            double prevLat = Double.MIN_VALUE;
            double prevLon = Double.MIN_VALUE;
            
            double startPointLat = Double.MIN_VALUE;
            double startPointLon = Double.MIN_VALUE;

            long prevTime = 0L;
            
            String currentElementName = null;
            
            double dist = Double.MIN_VALUE;
            double totalDist = 0.0f;
            double distFromStart = Double.MIN_VALUE;
            double speed = Double.MIN_VALUE;
            double prevSpeed = Double.MIN_VALUE;
            
            String tagName = null;
            
            boolean documentEnd = false;
            boolean isCDATA = false;
            
            // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            
            while (!documentEnd) 
            {
                int event = parser.next();

                switch (event)
                {
                    case XMLStreamConstants.END_DOCUMENT:
                        parser.close();
                        documentEnd = true;
                        break;
                
                    case XMLStreamConstants.START_DOCUMENT:
                        /*
                        xmlOut.println(XML_HEADER);
                        xmlOut.println(STYLESHEET_REF);
                        */
                        break;
    
                    case XMLStreamConstants.START_ELEMENT:
                        tagName = parser.getLocalName();
                        
                        currentElementName = tagName;
                        
                        if (tagName.equals("gpx"))
                        {
                            xmlOut.println(XML_HEADER);
                            xmlOut.println(STYLESHEET_REF);
                        }
                        
                        xmlOut.print("\n<"+ tagName);

                        if (tagName.equals("gpx")) 
                        {                        
                            xmlOut.print(" xmlns=\"http://www.topografix.com/GPX/1/0\"");
                        }
                        
                        if (tagName.equals("trk")) {
                            prevLat = Double.MIN_VALUE;
                            prevLon = Double.MIN_VALUE;
                            startPointLat = Double.MIN_VALUE;
                            startPointLon = Double.MIN_VALUE;
                            prevTime = 0L;
                            dist = Double.MIN_VALUE;
                            totalDist = 0.0f;
                            distFromStart = Double.MIN_VALUE;
                            speed = Double.MIN_VALUE;
                            prevSpeed = Double.MIN_VALUE;
                        }
                        
                        if (tagName.equals("trkpt") || tagName.equals("wpt"))
                        {
                            speed = Double.MIN_VALUE;
                        
                            String lat = parser.getAttributeValue(null, "lat");
                            String lon = parser.getAttributeValue(null, "lon");
                            
                            if ((lat != null) && (lon != null)) 
                            {
                                xmlOut.print(" lat=\"" + lat + "\"");
                                xmlOut.print(" lon=\"" + lon + "\"");
                            
                                try 
                                {
                                    double latitude = Double.parseDouble(lat);
                                    double longitude = Double.parseDouble(lon);
                                    
                                    if (prevLat == Double.MIN_VALUE) {
                                        dist = 0.0f;
                                    } else {
                                        dist = calculateDistance(prevLat, prevLon, latitude, longitude);
                                    }
                                    
                                    totalDist += dist;
                                    
                                    prevLat = latitude;
                                    prevLon = longitude;
                                    
                                    if ((startPointLat == Double.MIN_VALUE) || (startPointLon == Double.MIN_VALUE)) 
                                    {
                                        distFromStart = 0.0f;
                                        startPointLat = latitude;
                                        startPointLon = longitude;
                                    }
                                    else
                                    {
                                        distFromStart = calculateDistance(startPointLat, startPointLon, latitude, longitude);
                                    }
                                }
                                catch (NumberFormatException numEx)
                                {
                                    Logger.getLogger(getClass()).error(numEx);
                                }
                            }
                        }
                        
                        xmlOut.print(">");

                        if (tagName.equals("trkpt") || tagName.equals("wpt"))
                        {
                            if (dist != Double.MIN_VALUE) 
                            {
                                xmlOut.print("\n<dist>");
                                if (dist < 0.001f) {
                                    // prevent exponential format
                                    xmlOut.print("0.0");
                                } else {
                                    xmlOut.print(dist);
                                }
                                xmlOut.print("</dist>");
                            }

                            xmlOut.print("\n<totalDist>");
                            if (totalDist < 0.001f) {
                                // prevent exponential format
                                xmlOut.print("0.0");
                            } else {
                                xmlOut.print(totalDist);
                            }
                            xmlOut.print("</totalDist>");

                            if (distFromStart != Double.MIN_VALUE) 
                            {
                                xmlOut.print("\n<distFromStart>");
                                if (distFromStart < 0.001f) {
                                    // prevent exponential format
                                    xmlOut.print("0.0");
                                } else {
                                    xmlOut.print(distFromStart);
                                }
                                xmlOut.print("</distFromStart>");
                            }
                        }

                        isCDATA = (tagName.equals("name") || tagName.equals("desc"));

                        break;

                    case XMLStreamConstants.END_ELEMENT:

                        if (!lastWasText) 
                        {
                            xmlOut.println();
                        }
                        xmlOut.print("</"+ parser.getLocalName() + ">");

                        tagName = parser.getLocalName();
                        if (tagName.equals("time"))
                        {
                            if (speed != Double.MIN_VALUE)
                            {
                                double correctedSpeed = speed;

                                if (prevSpeed != Double.MIN_VALUE) 
                                {
                                    correctedSpeed = (prevSpeed * 4 + speed) / 5;
                                }
                            
                                if (correctedSpeed < 0.001f) {
                                    // prevent exponential format
                                    correctedSpeed = 0.0f;
                                }
                                
                                xmlOut.print("\n<speed>" + correctedSpeed + "</speed>");
                                
                                prevSpeed = correctedSpeed;
                            }
                        }

                        lastWasText = false;
                        break;
                        
                    case XMLStreamConstants.CHARACTERS:
                        String elementText = parser.getText().trim();
                        
                        if (currentElementName.equals("time"))
                        {
                            if (elementText.length() > 0)
                            {
                                try 
                                {
                                    long trackPointTime = ISO8601DateParser.parse(elementText).getTime();
                                    
                                    if ((prevTime > 0L) && (dist != Double.MIN_VALUE)) 
                                    {
                                        double duration = trackPointTime - prevTime;
                          
                                        if (duration == 0l)
                                        {
                                            speed = 0.0f;
                                        }
                                        else 
                                        {
                                            speed = dist / (duration / 1000f);
                                        }
                                    }
                                
                                    prevTime = trackPointTime;
                                } 
                                catch (Exception ex)
                                {
                                    Logger.getLogger(getClass()).error(ex);
                                    
                                    prevTime = 0L;
                                }
                            }
                        }

                        if (isCDATA)
                        {
                            xmlOut.print("<![CDATA[");
                        }
                        
                        xmlOut.print(elementText);

                        if (isCDATA)
                        {
                            xmlOut.print("]]>");
                            isCDATA = false;
                        }

                        if (elementText.length() > 0) 
                        {
                            lastWasText = true;
                        }
                        break;
                    default:
                        // System.out.println("unhandled event: " + event);
                }   
            }            

            xmlOut.flush();
            gpxReader.close();
        } 
        catch (IOException e) 
        {
            Logger.getLogger(getClass()).error("failed to read target file" + e);
        }
        catch (XMLStreamException xmlEx) {
            Logger.getLogger(getClass()).error("error parsing XML stream", xmlEx);
        }
    }

    /**
     * Berechnet die Entfernung zwischen zwei Koordinaten in Metern.
     *
     * @param ax Breite der ersten Koordinate in Dezimalgrad
     * @param ay Laenge der ersten Koordinate in Dezimalgrad
     * @param bx Breite der zweiten Koordinate in Dezimalgrad
     * @param by Laenge der zweiten Koordinate in Dezimalgrad
     * @return Distanz in Metern 
     */
    double calculateDistance(double ax, double ay, double bx, double by) {
    
        if ((ax == bx) && (ay == by)) {
            return 0.0f;
        }
    
        double x = 1.0f / 298.257223563f;  // Abplattung der Erde
        
        double a = 6378137.0f / 1000.0f;  // Aequatorradius der Erde in km
        
        double f = (ax + bx) / 2.0f;
        
        double g = (ax - bx) / 2.0f;
        
        double l = (ay - by) / 2.0f;
        
        // auf Bogenmass bringen
        
        f = (Math.PI / 180.0f) * f;
        
        g = (Math.PI / 180.0f) * g;
        
        l = (Math.PI / 180.0f) * l;
        
        double s = Math.pow(Math.sin(g), 2) * Math.pow(Math.cos(l), 2) + Math.pow(Math.cos(f), 2) * Math.pow(Math.sin(l), 2);
        
        double c = Math.pow(Math.cos(g), 2) * Math.pow(Math.cos(l), 2) + Math.pow(Math.sin(f), 2) * Math.pow(Math.sin(l), 2);
        
        double w = Math.atan(Math.sqrt(s / c));

        double d = 2.0f * w * a;  
        
        double r = Math.sqrt(s * c) / w;
        
        double h1 = (3.0f * r - 1.0f) / (2.0f * c); 
        
        double h2 = (3.0f * r + 1.0f) / (2.0 * s); 
        
        return(1000.0f * d * (1.0f + x * h1 * Math.pow(Math.sin(f), 2) * Math.pow(Math.cos(g), 2) - x * h2 * Math.pow(Math.cos(f), 2) * Math.pow(Math.sin(g), 2))); 
    }
    
    /**
     * Create the HTML response for viewing the given file contained in a ZIP archive..
     * 
     * @param zipFilePath path of the ZIP entry
     * @param zipIn the InputStream for the file extracted from a ZIP archive
     * @param req the servlet request
     * @param resp the servlet response
     */
    public void processZipContent(String zipFilePath, InputStream zipIn, ViewHandlerConfig viewHandlerConfig, HttpServletRequest req, HttpServletResponse resp)
    {
        // not yet supported
        Logger.getLogger(getClass()).warn("reading from ZIP archive not supported by ViewHandler " + this.getClass().getName());
    }
    
    /**
     * Does this ViewHandler support reading the file from an input stream of a ZIP archive?
     * @return true if reading from ZIP archive is supported, otherwise false
     */
    public boolean supportsZipContent()
    {
        return false;
    }
}
