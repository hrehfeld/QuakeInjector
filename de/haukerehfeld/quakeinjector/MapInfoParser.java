package de.haukerehfeld.quakeinjector;

import javax.xml.parsers.*;
import org.w3c.dom.*;

import java.io.*;

import java.lang.RuntimeException;

import java.util.GregorianCalendar;
import java.util.Date;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class MapInfoParser implements java.io.Serializable {
	/**
	 * Parse the complete document
	 */
	public List<MapInfo> parse() {
		HashMap<String,MapInfo> mapinfos = new HashMap<String,MapInfo>();
		Map<MapInfo,List<String>> requirements = new HashMap<MapInfo,List<String>>();
		List<MapInfo> mapList = new ArrayList<MapInfo>(mapinfos.size());

		try {
			Document document = XmlUtils.getDocument(
				"http://www.quaddicted.com/reviews/quaddicted_database.xml");

			Element files = document.getDocumentElement();
//			System.out.println(files.getTagName());


			for (Node file: XmlUtils.iterate(files.getChildNodes())) {
				if (XmlUtils.isElement(file)) {
					MapInfo mapinfo = parseMapInfo((Element) file, requirements);
					mapinfos.put(mapinfo.getId(), mapinfo);

					//add to final list
					mapList.add(mapinfo);
				}
				/** @todo 2009-03-29 01:36 hrehfeld    find out why this happens */
				else {
// 					System.out.println("node: " + file.getNodeName());
// 					System.out.println("Whoops, i thought file is an element!");
				}
			}
		}
		catch (IOException e) {
			System.out.println("Couldn't open xml file!");
		}
		catch (org.xml.sax.SAXException e) {
			System.out.println("Couldn't parse xml!");
		}

		// we still need to resolve requirements
		for (MapInfo currentMap: mapList) {
			List<String> reqs = requirements.get(currentMap);

			List<MapInfo> resolvedRequirements = new ArrayList<MapInfo>(reqs.size());
			for (String s: reqs) {
				MapInfo requiredMapInfo = mapinfos.get(s);
				if (requiredMapInfo == null) {
					System.out.println("Warning: requirement " + s
									   + " couldn't be found in the package list. "
									   + currentMap.getId() + " requires it and may not work correctly");
					continue;
				}
				resolvedRequirements.add(requiredMapInfo);
			}
			currentMap.setRequirements(resolvedRequirements);
		}

		return mapList;
	}

	/**
	 * Parse a single MapInfo/file entry
	 */
	private MapInfo parseMapInfo(Element file, Map<MapInfo,List<String>> reqResolve) {
		String id = file.getAttribute("id");

		String title = XmlUtils.getFirstElement(file, "title").getTextContent().trim();
		String author = XmlUtils.getFirstElement(file, "author").getTextContent().trim();
		int size;
		try {
			size = Integer.parseInt(XmlUtils.getFirstElement(file, "size").getTextContent().trim());
		}
		catch (java.lang.NumberFormatException e) {
			System.out.println("XML Parsing Error: malformed <size> tag on record \"" + id + "\"");
			size = 0;
		}

		String date = XmlUtils.getFirstElement(file, "date").getTextContent().trim();


		String relativeBaseDir = null;
		String cmdline = null;
		ArrayList<String> startmaps = new ArrayList<String>();

		ArrayList<String> requirements = new ArrayList<String>();

		// parse techinfo stuff
		Element techinfo = XmlUtils.getFirstElement(file, "techinfo");
		if (techinfo != null) {
			for (Node node: XmlUtils.iterate(techinfo.getChildNodes())) {
				if (!XmlUtils.isElement(node)) {
					continue;
				}
				Element info = (Element) node;

				if (info.getTagName().equals("zipbasedir")) {
					relativeBaseDir = info.getTextContent();
				}
				else if (info.getTagName().equals("commandline")) {
					cmdline = info.getTextContent();
				}
				else if (info.getTagName().equals("startmap")) {
					startmaps.add(info.getTextContent());
				}
				else if (info.getTagName().equals("requirements")) {
					for (Node reqFile: XmlUtils.iterate(info.getChildNodes())) {
						if (XmlUtils.isElement(reqFile)) {
							String r = ((Element) reqFile).getAttribute("id");
							requirements.add(r);
						}
					}
				}
			}
		}

// 		System.out.println("id: " + id
// 						   + ", title: " + title
// 						   + ", size: " + size);

		//if there's no startmap tag, use the id
		if (startmaps.isEmpty()) {
			startmaps.add(id);
		}

		MapInfo result = new MapInfo(id,
									 author,
									 title,
									 size,
									 MapInfoParser.parseDate(date),
									 false,
									 relativeBaseDir,
									 cmdline,
									 startmaps,
									 null);
		reqResolve.put(result, requirements);
		return result;
	}

	/**
	 * Parses the date that's in dd.mm.yy format
	 */
	public static Date parseDate(String date) {
		String[] components = date.split("\\.");
		if (components.length < 3) {
			throw new RuntimeException("Xml Parsing error: date malformed");
		}
		int day = Integer.parseInt(components[0]);
		int month = Integer.parseInt(components[1]);
		int year = Integer.parseInt(components[2]);
		if (year < 60) {
			year += 2000;
		}
		else {
			year += 1900;
		}
		return new GregorianCalendar(year, month, day).getTime();
	}
}