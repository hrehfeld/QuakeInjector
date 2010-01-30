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

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;

import java.util.Collections;

import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.Map;


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
	private final static String ROOTNODE = "maps";

	public void write(OutputStream out, Iterable<? extends Requirement> list)
		throws java.io.IOException {
		Map<String,Iterable<FileInfo>> files = new HashMap<String,Iterable<FileInfo>>();

		for (Requirement r: list) {
			if (!r.isInstalled()) {
				continue;
			}
			
			Iterable<FileInfo> l = r.getFileList();
			if (l == null) {
				l = Collections.emptyList();
			}

			files.put(r.getId(), l);
		}
		if (files.isEmpty()) {
			System.out.println("WARNING: writing empty maplist");
		}
		write(out, files);
	}

	public void write(OutputStream out, Map<String,Iterable<FileInfo>> files)
		throws java.io.IOException {

		//try {
			DocumentBuilder docBuilder = null;
			try {
				docBuilder
				    = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			}
			catch (javax.xml.parsers.ParserConfigurationException e) {
				System.err.println("Couldn't instantiate Document Builder: " + e);
				e.printStackTrace();
			}
			Document doc = docBuilder.newDocument();

			//create the root element and add it to the document
			Element root = doc.createElement(ROOTNODE);
			doc.appendChild(root);

			for (String id: files.keySet()) {
				Element mapNode = doc.createElement("map");
				mapNode.setAttribute("id", id);
				root.appendChild(mapNode);

				for (FileInfo file: files.get(id)) {
					Element fileNode = doc.createElement("file");
					//System.out.println("adding node " + file.getName());
					fileNode.setAttribute("name", file.getName());
					long crc = file.getChecksum();
					if (crc != 0) {
						fileNode.setAttribute("crc", Long.toString(crc));
					}

					if (!file.getEssential()) {
						fileNode.setAttribute("essential", Boolean.toString(false));
					}
					
					mapNode.appendChild(fileNode);
				}
			}

			//Output the XML

			//set up a transformer
			Transformer trans = null;
			try {
				trans = TransformerFactory.newInstance().newTransformer();
			}
			catch (javax.xml.transform.TransformerConfigurationException e) {
				System.err.println(e);
				e.printStackTrace();
			}
			
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			//create string from xml tree
			StreamResult result = new StreamResult(out);
			DOMSource source = new DOMSource(doc);
			try {
				trans.transform(source, result);
			}
			catch (javax.xml.transform.TransformerException e) {
			}
	}

	public List<PackageFileList> read(InputStream in) throws java.io.IOException {
		Document document;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
	 
			document = db.parse(in);
		}
		catch (javax.xml.parsers.ParserConfigurationException e) {
			throw new java.io.IOException("Couldn't parse installed package list: "
			                              + e.getMessage());
		}
		catch (org.xml.sax.SAXException e) {
			throw new java.io.IOException("Couldn't parse Installed Package List: "
			                              + e.getMessage());
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
				Element e = (Element) file;
				String name = e.getAttribute("name");

				long crc = 0;
				if (e.hasAttribute("crc")) {
					crc = Long.parseLong(e.getAttribute("crc"));
				}
				fileList.add(new FileInfo(name, crc));
			}
		}

		return fileList;

	}
}
	
