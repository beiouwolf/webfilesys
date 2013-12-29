package de.webfilesys.gui.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;

/**
 * @author Frank Hoehnel
 */
public class MultiDownloadRequestHandler extends UserRequestHandler
{
	protected HttpServletResponse resp = null;

	public MultiDownloadRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);

	    this.resp = resp;
	}

	protected void process()
	{
		Vector selectedFiles = (Vector) session.getAttribute("selectedFiles");
		
		if ((selectedFiles == null) || (selectedFiles.size() == 0))
		{
			Logger.getLogger(getClass()).error("MultiDownloadRequestHandler: no files selected");
			
			return;
		}
		
		String actPath = getParameter("actPath");
		
		if (actPath == null)
		{
			Logger.getLogger(getClass()).error("MultiDownloadRequestHandler: actPath is null");
			
			return;
		}
		
		if (isMobile()) 
		{
		    actPath = this.getAbsolutePath(actPath);
		}
		
		if (!checkAccess(actPath)) 
		{
		    return;
		}
		
		File tempFile = null;
		
		try
		{
			tempFile = File.createTempFile("fmweb",null);
			
			ZipOutputStream zip_out=null;

			zip_out = new ZipOutputStream(new FileOutputStream(tempFile));

			int count=0;

			FileInputStream fin = null;

			byte buffer[] = new byte[16192];

			for (int i=0;i<selectedFiles.size();i++)
			{
				try
				{
					zip_out.putNextEntry(new ZipEntry((String) selectedFiles.elementAt(i)));

					fin = new FileInputStream(new File(actPath, (String) selectedFiles.elementAt(i)));

					count=0;

					while (( count = fin.read(buffer))>=0 )
					{
						zip_out.write(buffer,0,count);
					}

					fin.close();
				}
				catch (Exception zioe)
				{
					Logger.getLogger(getClass()).warn(zioe);
					return;
				}
			}
		
			zip_out.close();
			
			resp.setContentType("application/zip");

			resp.setHeader("Content-Disposition", "attachment; filename=fmwebDownload.zip");
			
			resp.setContentLength((int) tempFile.length());

			OutputStream byteOut = resp.getOutputStream();

			fin = new FileInputStream(tempFile);

			while ((count = fin.read(buffer)) >= 0)
			{
				byteOut.write(buffer, 0, count);
			}

			fin.close();

			byteOut.flush();

			buffer = null;
			
			tempFile.delete();
			
			if (WebFileSys.getInstance().isDownloadStatistics())
			{
				for (int i = 0; i < selectedFiles.size(); i++)
				{
                    String fullPath = null;
                     
                    if (actPath.endsWith(File.separator))
                    {
					    fullPath = actPath + selectedFiles.elementAt(i);
                    }
                    else
                    {
					    fullPath = actPath + File.separator + selectedFiles.elementAt(i);
                    }
				
				    MetaInfManager.getInstance().incrementDownloads(fullPath);
				}
			}
		}
        catch (IOException ioex)
        {
        	Logger.getLogger(getClass()).error(ioex);
        	return;
        }
        
        session.removeAttribute("selectedFiles");
	}
}
