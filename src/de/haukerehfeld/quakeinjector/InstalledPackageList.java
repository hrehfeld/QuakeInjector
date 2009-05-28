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

import java.io.File;
import java.lang.Iterable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class InstalledPackageList {
	private final static String filename = "installedMaps.xml";
	private final File file = new File(filename);
	
	public void write(Iterable<Requirement> list) throws java.io.IOException {
		System.out.println("Writing " + filename);
		
		if (!file.exists()) {
			file.createNewFile();
		}
		if (!file.canWrite()) {
			throw new RuntimeException("Can't write installed maps file!");
		}

		try {
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			//create the root element and add it to the document
			Element root = doc.createElement("installedmaps");
			doc.appendChild(root);

			for (Requirement r: list) {
				if (!r.isInstalled()) {
					continue;
				}
				Element mapNode = doc.createElement("map");
				mapNode.setAttribute("id", r.getId());
				root.appendChild(mapNode);

				PackageFileList l = r.getFileList();
				if (l == null) {
					continue;
				}
				for (String filename: l) {
					Element fileNode = doc.createElement("file");
					fileNode.setAttribute("name", filename);
					mapNode.appendChild(fileNode);
				}
			}

			//Output the XML

			//set up a transformer
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			//create string from xml tree
			StreamResult result = new StreamResult(file);
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);
		}
		catch (Exception e) {
            System.out.println(e);
        }
	}

	public List<PackageFileList> read() throws java.io.IOException {
		System.out.println("Reading " + filename);

		Document document;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
	 
			document = db.parse(file);
		}
		catch (javax.xml.parsers.ParserConfigurationException e) {
			throw new java.io.IOException("Couldn't parse installedMaps xml: " + e.getMessage());
		}
		catch (org.xml.sax.SAXException e) {
			throw new java.io.IOException("Couldn't parse installedMaps xml: " + e.getMessage());
		}

		Element root = document.getDocumentElement();

		NodeList installedMaps = root.getChildNodes();


		List<PackageFileList> files = new ArrayList<PackageFileList>(installedMaps.getLength());

		for (int i = 0; i < installedMaps.getLength(); ++i) {
			Node map = installedMaps.item(i);

			if (map.getNodeType() == Node.ELEMENT_NODE) {
				PackageFileList l = parseMapFileList((Element) map);
				files.add(l);
			}
			/** @todo 2009-03-29 01:36 hrehfeld    find out why this happens */
			else {
// 					System.out.println("node: " + file.getNodeName());
// 					System.out.println("Whoops, i thought file is an element!");
			}
		}

		return files;
	}

	private PackageFileList parseMapFileList(Element map) {
		String id = map.getAttribute("id");
		PackageFileList fileList = new PackageFileList(id);

		NodeList files = map.getChildNodes();

		for (int i = 0; i < files.getLength(); ++i) {
			Node file = files.item(i);
			if (file.getNodeType() == Node.ELEMENT_NODE) {
				fileList.add(((Element) file).getAttribute("name"));
			}
		}

		return fileList;

	}
}