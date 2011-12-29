package de.webfilesys.gui.xsl;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslMultiDownloadPromptHandler extends XslRequestHandlerBase
{
	boolean authFailed = false;
	
	public XslMultiDownloadPromptHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
	}
	  
	protected void process()
	{
		String filePath = getParameter("actpath");

		if (filePath == null)
		{
			filePath = (String) session.getAttribute("cwd");
			
			if (filePath == null)
			{
				Logger.getLogger(getClass()).warn("current path cannot be determined");
				return;
			}
		}

		Element downloadElement = doc.createElement("download");
			
		doc.appendChild(downloadElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/multiDownload.xsl\"");

		doc.insertBefore(xslRef, downloadElement);

		XmlUtil.setChildText(downloadElement, "css", userMgr.getCSS(uid), false);
		XmlUtil.setChildText(downloadElement, "path", filePath, false);
		XmlUtil.setChildText(downloadElement, "shortPath", CommonUtils.shortName(getHeadlinePath(filePath), 40), false);
		
		addMsgResource("label.download.link", getResource("label.download.link","Click here to start the Download"));
		addMsgResource("label.download.text3", getResource("label.download.text3","of the selected files in"));
		addMsgResource("label.download.text2", getResource("label.download.text2","Close this window after the download has finished."));
		addMsgResource("button.closewin", getResource("button.closewin","Close Window"));

		this.processResponse("multiDownload.xsl", true);
    }
}