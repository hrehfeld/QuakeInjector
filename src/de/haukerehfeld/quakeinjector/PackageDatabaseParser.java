/*
Copyright 2009 Hauke Rehfeld


This file is part of QuakeInjector.

QuakeInjector is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

QuakeInjector is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with QuakeInjector.  If not, see <http://www.gnu.org/licenses/>.
*/
package de.haukerehfeld.quakeinjector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.haukerehfeld.quakeinjector.Package.Rating;

public class PackageDatabaseParser implements java.io.Serializable {
	private final static Rating[] ratingTable = { Rating.Unrated,
	                                              Rating.Crap,
	                                              Rating.Poor,
	                                              Rating.Average,
	                                              Rating.Nice,
	                                              Rating.Excellent
	};
	                                              

	/**
	 * Parse the complete document
	 */
	public List<Requirement> parse(Document document) throws IOException,
		org.xml.sax.SAXException {
		HashMap<String, Requirement> packages = new HashMap<String,Requirement>();
		Map<Package,List<String>> unresolvedRequirements = new HashMap<Package,List<String>>();

		Element files = document.getDocumentElement();

		for (Node file: XmlUtils.iterate(files.getChildNodes())) {
			if (XmlUtils.isElement(file)) {
				Package currentPackage = parsePackage((Element) file, unresolvedRequirements);
				packages.put(currentPackage.getId(), currentPackage);
			}
			/** @todo 2009-03-29 01:36 hrehfeld    find out why this happens */
			else {
// 					System.out.println("node: " + file.getNodeName());
// 					System.out.println("Whoops, i thought file is an element!");
			}
		}

		resolveRequirements(unresolvedRequirements, packages);

		List<Requirement> packageList = new ArrayList<Requirement>(packages.values());
		return packageList;
	}

	private void resolveRequirements(Map<Package,List<String>> unresolvedRequirements,
									 Map<String, Requirement> packages) {
		for (Map.Entry<Package,List<String>> entry: unresolvedRequirements.entrySet()) {
			Package current = entry.getKey();
			List<String> reqs = entry.getValue();

// 			if (reqs.size() > 1) {
// 				System.out.println(current.getId() + " has more than one requirement");
// 			}

			List<Requirement> resolvedRequirements = new ArrayList<Requirement>(reqs.size());
			for (String id: reqs) {
				Requirement resolved = packages.get(id);
				if (resolved == null) {
					resolved = new UnavailableRequirement(id);
					packages.put(id, resolved);
				}
				resolvedRequirements.add(resolved);
			}
			current.setRequirements(resolvedRequirements);
		}
	}


	/**
	 * Parse a single Package/file entry
	 */
	private Package parsePackage(Element file, Map<Package,List<String>> reqResolve) {
		String id = file.getAttribute("id");
		Rating rating;
		{
			String ratingString = file.getAttribute("rating");
			int ratingNumber = 0;
			if (!ratingString.equals("")) {
				try {
					ratingNumber = Integer.parseInt(ratingString);

					if (ratingNumber < 0 || ratingNumber > 5) {
						System.out.println("Rating of " + id + " is broken");
						ratingNumber = 0;
					}
				}
				catch (java.lang.NumberFormatException e) {
					System.out.println("Rating of " + id + " is broken");
				}
			}
			rating = ratingTable[ratingNumber];
		}

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

		String description = XmlUtils.getTextForNode(XmlUtils.getFirstElement(file, "description"))
		    .trim();


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

				switch (info.getTagName()) {
					case "zipbasedir":
						relativeBaseDir = info.getTextContent();
						break;
					case "commandline":
						cmdline = info.getTextContent();
						break;
					case "startmap":
						startmaps.add(info.getTextContent());
						break;
					case "requirements":
						for (Node reqFile : XmlUtils.iterate(info.getChildNodes())) {
							if (XmlUtils.isElement(reqFile)) {
								String r = ((Element) reqFile).getAttribute("id");
								requirements.add(r);
							}
						}
						break;
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

		Package result = new Package(id,
									 author,
									 title,
									 size,
									 PackageDatabaseParser.parseDate(date),
									 false,
		                             rating,
		                             description,
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
		int month = Integer.parseInt(components[1]) - 1;
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