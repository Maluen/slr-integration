package parsers.xml;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import parsers.Parser;

public class XMLParser extends Parser {

	DocumentBuilderFactory factory;
	DocumentBuilder builder;
	XPath xpath;
	
	public XMLParser() {
	    this.factory = DocumentBuilderFactory.newInstance();
	    try {
			this.builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.builder = null; // not needed, but we make it explicit
		}
	    
	    this.xpath = XPathFactory.newInstance().newXPath();
	}
	
	public Document parse(String content) {
	    InputSource is = new InputSource(new StringReader(content));
	    try {
			return builder.parse(is);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	
	// Creates a new Document and import the (deeply cloned) element inside it
	public Document createDocumentFromElement(Element element) {
		Document doc = this.builder.newDocument();
		
		Node node = (Node) element;
		Node importedNode = doc.importNode(node, true);
		doc.appendChild(importedNode);
		
		return doc;
	}
	
	public static List<Element> getChildElements(Element parent) {
		List<Element> childElementList = new ArrayList<Element>();
		
		NodeList childNodeList = parent.getChildNodes();
        for (int i=0; i<childNodeList.getLength(); i++) {
            Node childNode = childNodeList.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
            	Element childElement = (Element) childNode;
            	childElementList.add(childElement);
            }
        }
		
		return childElementList;
	}
	
	public static Element getChildElementByTagName(Element parent, String tagName) {
		List<Element> childElementList = XMLParser.getChildElements(parent);
		for (Element childElement : childElementList) {
			if (childElement.getTagName().equals(tagName)) {
				return childElement;
			}
		}
        
        return null;
	}

	// uses XPATH
	public List<Element> select(String selector, Element fromContentEl) throws XPathExpressionException {
		NodeList nodeList = (NodeList) this.xpath.evaluate(selector, fromContentEl, XPathConstants.NODESET);
		
		// convert to List<Element>
		List<Element> elementList = new ArrayList<Element>();
		for (int i=0; i<nodeList.getLength(); i++) {
			elementList.add( (Element) nodeList.item(i) );
		}
		
		return elementList;
	}
	
}
