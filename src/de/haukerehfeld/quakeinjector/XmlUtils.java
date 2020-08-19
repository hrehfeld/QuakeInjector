package de.haukerehfeld.quakeinjector;

import java.io.InputStream;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

	public static Document getDocument(InputStream xml)
		throws java.io.IOException,
		org.xml.sax.SAXException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringElementContentWhitespace(true);
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		}
		catch (javax.xml.parsers.ParserConfigurationException e) {
			throw new RuntimeException("Parser misconfiguration - " + e.getMessage());
		}

		return db.parse(xml);
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

/**
 * Gets the String value of the node. 
 * If the node does not contain text then an empty String is returned
 * @param node the node of interest
 * @return the value of that node
 */
	public static String getTextForNode(Node node)
		{
			NodeList children = node.getChildNodes();
			if (children == null)
				return "";

			for (int i = 0; i < children.getLength(); i++) {
				Node childNode = children.item(i);
				if ((childNode.getNodeType() == Node.TEXT_NODE)
				    || (childNode.getNodeType() == Node.CDATA_SECTION_NODE)) {
					return childNode.getNodeValue();
				}
			}

			return "";
		}	
}