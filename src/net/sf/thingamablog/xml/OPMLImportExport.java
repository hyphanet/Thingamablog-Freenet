/*
 * Created on Jul 11, 2004
 *
 * This file is part of Thingamablog. ( http://thingamablog.sf.net )
 *
 * Copyright (c) 2004, Bob Tantlinger All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 * USA.
 * 
 * 
 **************************************************************************
 * Some of the code in this class comes from RSS OWL @ http://rssowl.sf.net
 ************************************************************************** 
*/


package net.sf.thingamablog.xml;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.thingamablog.feed.Feed;
import net.sf.thingamablog.feed.FeedBackend;
import net.sf.thingamablog.feed.FeedFolder;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;



/**
 * Static methods for Importing and Exporting FeedFolders to/from OPML files
 */
public class OPMLImportExport
{
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
	

	
	/**
	 * Import Feeds from the given OPML file
	 * 
	 * @param folder The folder to import into
	 * @param filePath The path of the OPML file
	 * @param backend The backend to connect the imported feeds to
	 * 
	 * @throws JDOMException If a JDOM error occurs
	 * @throws IOException If an IO error occurs
	 */
	public static void importFromOPML(FeedFolder folder, String filePath, FeedBackend backend) 
	throws JDOMException, IOException 
	{
		SAXBuilder builder = new SAXBuilder(false);
		builder.setEntityResolver(new TBEntityResolver());

		Document document = null;

		//if (new File(filePath).exists())
		document = builder.build(filePath);
		//else
		//	return;

		// Root element
		Element root = document.getRootElement();

		// Get body 
		Element body = root.getChild("body");

		if(body != null) 
		{
			// Check for favorits that have no category
			boolean favWithoutCat = false;			
			
			List elements = body.getChildren();
			Iterator elementsIt = elements.iterator();
			while(elementsIt.hasNext()) 
			{
				Element element = (Element) elementsIt.next();
				/** This outline has no childs, so it is a favorite */
				if(element.getChildren().size() == 0) 
				{
					favWithoutCat = true;
					break;
				}
			}

			// If there is a fav w/o cat, create a new cat for it */
			if(favWithoutCat) 
			{
				FeedFolder newFolder = new FeedFolder("No description");
				//newFolder.setParent(rssOwlCategory);
				folder.addFolder(newFolder);

				//RSSOwlCategory.checkCatExists(rssOwlCategory, newCategory);
				importFromOPML(root.getChild("body"), newFolder, backend);
			}
			// Proceed with the given rssOwlCategory */
			else 
			{
				importFromOPML(root.getChild("body"), folder, backend);
			}
		}
	}
	

	private static void importFromOPML(Element element, FeedFolder folder, FeedBackend backend) 
	{
		List outlines = element.getChildren();
		Iterator outIt = outlines.iterator();

		/** Foreach Outline Element */
		while (outIt.hasNext()) 
		{
			Element outline = (Element) outIt.next();

			/** This outline is a category */
			if (outline.getAttributeValue("xmlUrl") == null && 
			outline.getAttributeValue("xmlurl") == null) 
			{
				FeedFolder newFolder = new FeedFolder("No name");
				//newFolder.setParent(folder);
				folder.addFolder(newFolder);

				if (outline.getAttributeValue("title") != null) 
				{
					newFolder.setName(outline.getAttributeValue("title"));
				}

				/** Check if a category with this name already exists */
				//RSSOwlCategory.checkCatExists(rssOwlCategory, newCategory);

				/** Recursivly add childs */
				importFromOPML(outline, newFolder, backend);
			}

			/** This outline is a favorite. Add it to the category */
			else 
			{
				String url = outline.getAttributeValue("xmlUrl");

				/** Attribute xmlUrl may be written in small letters */
				if (url == null)
					url = outline.getAttributeValue("xmlurl");

				String title = url;

				if (outline.getAttributeValue("title") != null)
					title = outline.getAttributeValue("title");

				Feed feed = new Feed(url);
				feed.setTitle(title);
				feed.setBackend(backend);
				//rssOwlFavorite.setCreationDate(System.currentTimeMillis());
				folder.addFeed(feed);
			}
		}
	}
	
	/**
	 * Exports a FeedFolder to an OPML file
	 * 
	 * @param folder The folder to export
	 * @param path The path of the OPML file
	 * @throws JDOMException If a JDOM error occurs
	 * @throws IOException If an IO error occurs
	 */
	public static void exportFolderToOPML(FeedFolder folder, String path)
	throws JDOMException, IOException
	{
		File f = new File(path);
		Document document = XMLUtils.initDocument("opml version=\"1.1\"", "opml", f);

		/** Could not init the document, return */
		if(document == null)
			return;

		/** Setup default template for XML Document */
		Element body = prepareOPMLDocument(document);

		Element rootOutline = new Element("outline");
		rootOutline.setAttribute("title", folder.getName());
		body.addContent(rootOutline);

		exportFolderToOPML(folder, rootOutline);

		/** Write the new DOM into temp File */
		XMLUtils.writeXML(document, path, true);    
	}
	
	private static void exportFolderToOPML(FeedFolder folder, Element element) 
	{
		/** Save Sub - Categorys */
		//TreeSet subCats = rssOwlCategory.getSortedSubCatTitles();
		FeedFolder subFolders[] = folder.getFolders();
		for(int i = 0; i < subFolders.length; i++)
		{
			FeedFolder subFolder = subFolders[i];

			Element outline = new Element("outline");
			outline.setAttribute("title", subFolder.getName());
			element.addContent(outline);

			exportFolderToOPML(subFolder, outline);
			
		}
		
		Feed feeds[] = folder.getFeeds();
		for(int i = 0; i < feeds.length; i++)
		{
			Element outline = new Element("outline");
			outline.setAttribute("text", feeds[i].getTitle());
			outline.setAttribute("title", feeds[i].getTitle());
			outline.setAttribute("xmlUrl", feeds[i].getURL());
			element.addContent(outline);
			
		}
	}
	
	private static Element prepareOPMLDocument(Document document) 
	{
		Element root = document.getRootElement();

		/** Setup head */
		Element head = new Element("head");
		root.addContent(head);

		/** Title */
		Element title = new Element("title");
		title.setText("OPML generated by Thingamablog (http://thingamablog.sf.net)");
		head.addContent(title);

		/** Date */
		Element dateCreated = new Element("dateCreated");
		dateCreated.setText(df.format(new Date()));
		head.addContent(dateCreated);

		/** Setup body */
		Element body = new Element("body");
		root.addContent(body);
		return body;
	}
}
