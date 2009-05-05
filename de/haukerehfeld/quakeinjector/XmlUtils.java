package de.haukerehfeld.quakeinjector;

import javax.xml.parsers.*;
import org.w3c.dom.*;

import java.io.*;

import java.lang.RuntimeException;
import java.util.Iterator;

public class XmlUtils {
	/**
	 * get the first element in the parent element that's named name or null if no such element exists
	 */
	public static Element getFirstElement(Element parent, String name) {
		NodeList list = parent.getElementsByTagName(name);
		
		Node node = list.item(0);
		if (node == null) {
			//throw new RuntimeException("Malformed XML: No such node: " + name);
			return null;
		}
		
		if (!isElement(node)) {
			throw new RuntimeException("XML Parsing error: " + name + " is not an Element");
		}

		return (Element) node;
	}

	public static Document getDocument(String file)
		throws java.io.IOException,
		       org.xml.sax.SAXException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setIgnoringElementContentWhitespace(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
	 
			Document doc = db.parse(file);
			return doc;
		}
		catch (javax.xml.parsers.ParserConfigurationException e) {
			return null;
		}
	}

	public static java.lang.Iterable<Node> iterate(NodeList list) {
		return new NodeListIterator(list);
	}

	public static class NodeListIterator implements Iterator<Node>,
		java.lang.Iterable<Node> {
		private NodeList list;
		private int i = 0;

		public NodeListIterator(NodeList list) {
			this.list = list;
		}

		public Node next() {
			i++;

			return list.item(i);
		}

		public boolean hasNext() {
			return i + 1 < list.getLength();
		}

		public void remove() {
			throw new java.lang.UnsupportedOperationException();
		}

		public Iterator<Node> iterator() {
			return this;
		}
	}

	public static boolean isElement(Node n) {
		return (n.getNodeType() == Node.ELEMENT_NODE);
	}
}