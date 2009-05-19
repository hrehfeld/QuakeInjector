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

public class PackageDatabaseParser implements java.io.Serializable {
	/**
	 * Parse the complete document
	 */
	public List<Package> parse(String databaseUrl) throws IOException, org.xml.sax.SAXException {
		HashMap<String,Package> packages = new HashMap<String,Package>();
		Map<Package,List<String>> unresolvedRequirements = new HashMap<Package,List<String>>();
		List<Package> packageList = new ArrayList<Package>(packages.size());

			Document document = XmlUtils.getDocument(databaseUrl);

			Element files = document.getDocumentElement();
//			System.out.println(files.getTagName());


			for (Node file: XmlUtils.iterate(files.getChildNodes())) {
				if (XmlUtils.isElement(file)) {
					Package currentPackage = parsePackage((Element) file, unresolvedRequirements);
					packages.put(currentPackage.getId(), currentPackage);

					//add to final list
					packageList.add(currentPackage);
				}
				/** @todo 2009-03-29 01:36 hrehfeld    find out why this happens */
				else {
// 					System.out.println("node: " + file.getNodeName());
// 					System.out.println("Whoops, i thought file is an element!");
				}
			}

		resolveRequirements(packageList, unresolvedRequirements, packages);

		return packageList;
	}

	private void resolveRequirements(List<Package> packageList,
									 Map<Package,List<String>> unresolvedRequirements,
									 Map<String, Package> packages) {
		for (Package current: packageList) {
			List<String> reqs = unresolvedRequirements.get(current);

// 			if (reqs.size() > 1) {
// 				System.out.println(current.getId() + " has more than one requirement");
// 			}

			List<String> unavailableRequirements = new ArrayList<String>(1);
			List<Package> resolvedRequirements = new ArrayList<Package>(reqs.size());
			for (String id: reqs) {
				Package resolved = packages.get(id);
				if (resolved != null) { resolvedRequirements.add(resolved); }
				else { unavailableRequirements.add(id); }
			}
			current.setRequirements(resolvedRequirements);
			current.setUnavailableRequirements(unavailableRequirements);
		}
	}


	/**
	 * Parse a single Package/file entry
	 */
	private Package parsePackage(Element file, Map<Package,List<String>> reqResolve) {
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

		Package result = new Package(id,
									 author,
									 title,
									 size,
									 PackageDatabaseParser.parseDate(date),
									 false,
									 relativeBaseDir,
									 cmdline,
									 startmaps,
									 null,
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