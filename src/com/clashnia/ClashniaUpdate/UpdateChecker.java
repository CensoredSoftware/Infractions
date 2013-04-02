package com.clashnia.ClashniaUpdate;

import com.legit2.hqm.Infractions.Util;
import org.bukkit.plugin.PluginDescriptionFile;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class UpdateChecker
{
	private URL filesFeed;

	private String version;
	private String link;
	private String jarLink;

	public UpdateChecker(String url)
	{
		try
		{
			this.filesFeed = new URL(url);
		}
		catch(MalformedURLException e)
		{
			e.printStackTrace();
		}
	}

	public boolean updateNeeded()
	{
		try
		{
			InputStream input = this.filesFeed.openConnection().getInputStream();
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);

			Node latestFile = document.getElementsByTagName("item").item(0);
			NodeList children = latestFile.getChildNodes();

			this.version = children.item(1).getTextContent().replaceAll("[a-zA-Z ]", "");
			try
			{
				this.link = children.item(3).getTextContent();
			}
			catch(Exception e)
			{
				Util.consoleMSG("warning", "Failed to find download page.");
				e.printStackTrace();
			}
			input.close();

			try
			{
				input = (new URL(this.link)).openConnection().getInputStream();
			}
			catch(Exception e)
			{
				Util.consoleMSG("warning", "Failed to open connection with download page.");
				e.printStackTrace();
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			String line;

			while((line = reader.readLine()) != null)
			{
				if(line.trim().startsWith("<li class=\"user-action user-action-download\">"))
				{
					this.jarLink = line.substring(line.indexOf("href=\"") + 6, line.lastIndexOf("\""));
					break;
				}
			}

			reader.close();
			input.close();

			PluginDescriptionFile pdf = Util.getPlugin().getDescription();
			String currentVersion = pdf.getVersion();
			if(!currentVersion.equals(this.version))
			{
				return true;
			}
		}
		catch(Exception e)
		{
			Util.consoleMSG("warning", "Failed to read download page.");
			e.printStackTrace();
		}

		return false;
	}

	public String getVersion()
	{
		return this.version;
	}

	public String getLink()
	{
		return this.link;
	}

	public String getJarLink()
	{
		return this.jarLink;
	}

}
