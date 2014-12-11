package net.strongdesign.stg.export;

import java.awt.Point;
import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedList;

import org.w3c.dom.*;

import javax.xml.parsers.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import net.strongdesign.desij.Messages;
import net.strongdesign.desij.gui.STGGraphComponent;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.traversal.ConditionFactory;

public class SVGExport {

	static final String dummyStyle = "stroke:none;fill:black;font-family:Serif";//font-style:oblique
	static final String inputStyle = "stroke:none;fill:red";
	static final String outputStyle = "stroke:none;fill:blue;";
	static final String internalStyle = "stroke:none;fill:green;";

	static final String otherStyle = "fill:pink;";

	static final int boxWidth = 50;
	static final int boxHeight = 22;
	static final int placeRadius = 11;

	static int minx;
	static int miny;

	private static Element PNArrowMarker(Document doc) {
		Element marker = doc.createElement("marker");
		marker.setAttribute("id", "PNArrow");
		marker.setAttribute("style", "overflow:visible;fill:black;stroke:none");
		marker.setAttribute("orient", "auto");

		Element path = doc.createElement("path");
		path.setAttribute("d", "M 1,0 l -8,-4 l 2,4 l -2,4 z");
		marker.appendChild(path);

		return marker;
	}

	private static LinkedList<Element> createTransition(Document doc, STG stg,
			Transition t) {
		Point p = stg.getCoordinates(t);

		p.x -= minx;
		p.y -= miny;

		Element text = doc.createElement("text");
		text.setAttribute("x", "" + p.x);
		text.setAttribute("y", "" + (p.y + 6));
		String style;

		Element underline = null;

		switch (stg.getSignature(t.getLabel().getSignal())) {
		case DUMMY:
			style = dummyStyle;
			break;
		case INPUT:
			style = inputStyle;
			underline = doc.createElement("path");
			underline.setAttribute("d", "M " + (p.x - 20) + "," + (p.y + 8)
					+ " l 40,0");
			break;
		case OUTPUT:
			style = outputStyle;
			break;
		case INTERNAL:
			style = internalStyle;
			break;
		default:
			style = otherStyle;
			break;
		}
		
		if (stg.getSignature(t.getLabel().getSignal())==Signature.DUMMY) {
			text.appendChild(doc.createTextNode("\u03bb"));
		} else {
			text.appendChild(doc.createTextNode(t.getString(0)));
		}

		text.setAttribute("style", style);

		Element rect = doc.createElement("rect");

		rect.setAttribute("x", "" + (p.x - boxWidth / 2));
		rect.setAttribute("y", "" + (p.y - boxHeight / 2));
		rect.setAttribute("width", "" + boxWidth);
		rect.setAttribute("height", "" + boxHeight);
		rect.setAttribute("style", "fill:white;");

		LinkedList<Element> result = new LinkedList<Element>();
		result.add(rect);
		result.add(text);

		if (underline != null)
			result.add(underline);

		return result;
	}

	private static Element createPlace(Document doc, STG stg, Place place) {
		Point p = stg.getCoordinates(place);

		if (p == null)
			return null;
		if (STGGraphComponent.isShorthandPlace(place, null))
			return null;

		p.x -= minx;
		p.y -= miny;

		Element tokens = null;

		Element circle = doc.createElement("circle");
		circle.setAttribute("cx", "" + p.x);
		circle.setAttribute("cy", "" + p.y);
		circle.setAttribute("r", "" + 10);
		circle.setAttribute("style", "fill:white;stroke:black;stroke-width:2;");

		if (place.getMarking() == 1) {
			tokens = doc.createElement("circle");

			tokens.setAttribute("cx", "" + p.x);
			tokens.setAttribute("cy", "" + p.y);
			tokens.setAttribute("r", "" + 5);
			tokens.setAttribute("style", "fill:black;stroke:none;");

		}
		if (place.getMarking() > 1) {
			tokens = doc.createElement("text");

			tokens.setAttribute("x", "" + p.x);
			tokens.setAttribute("y", "" + (p.y + 5));
			tokens.setAttribute("style", "fill:black;stroke:none;");
			tokens.appendChild(doc.createTextNode("" + place.getMarking()));
		}

		if (tokens != null) {
			Element result = doc.createElement("g");
			result.appendChild(circle);
			result.appendChild(tokens);
			return result;
		} else {
			return circle;
		}

	}

	private static Element createConnection(Document doc, STG stg,
			net.strongdesign.stg.Node n1, net.strongdesign.stg.Node n2, int w) {
		// find out coordinates
		Point p1 = stg.getCoordinates(n1);
		Point p2 = stg.getCoordinates(n2);

		double dx, dy;
		dx = p2.x - p1.x;
		dy = p2.y - p1.y;

		double d = dx * dx + dy * dy;

		d = Math.sqrt(d);

		if (d < placeRadius)
			return null;

		double x1, y1, x2, y2;

		x1 = p1.x;
		x2 = p2.x;
		y1 = p1.y;
		y2 = p2.y;

		if (n1 instanceof Place) {
			x1 = p1.x + (double) (placeRadius - 1) * dx / d;
			y1 = p1.y + (double) (placeRadius - 1) * dy / d;
		} else {

			double d1;
			double xx = (double) boxWidth / 2;
			double yy = (double) boxHeight / 2;

			if (dx != 0 && Math.abs(dy) / Math.abs(dx) < yy / xx) {
				d1 = Math.abs(d * xx / dx);
			} else {
				d1 = Math.abs(d * yy / dy);
			}

			x1 = p1.x + d1 * dx / d;
			y1 = p1.y + d1 * dy / d;
		}

		if (n2 instanceof Place) {
			x2 = p1.x + (d - (placeRadius + 1)) * dx / d;
			y2 = p1.y + (d - (placeRadius + 1)) * dy / d;

		} else {

			double d1;
			double xx = (double) boxWidth / 2;
			double yy = (double) boxHeight / 2;

			if (dx != 0 && Math.abs(dy) / Math.abs(dx) < yy / xx) {
				d1 = Math.abs(d * xx / dx);
			} else {
				d1 = Math.abs(d * yy / dy);
			}

			d1 += 1.5;

			x2 = p1.x + (d - d1) * dx / d;
			y2 = p1.y + (d - d1) * dy / d;

		}

		// create line component
		Element line = doc.createElement("path");
		line.setAttribute(
				"d",
				"M " + (double) Math.round(x1 * 10) / 10 + ","
						+ (double) Math.round(y1 * 10) / 10 + " L "
						+ (double) Math.round(x2 * 10) / 10 + ","
						+ (double) Math.round(y2 * 10) / 10);

		line.setAttribute("marker-end", "url(#PNArrow)");
		return line;
	}

	private static LinkedList<Element> createConnections(Document doc, STG stg,
			Place place) {
		LinkedList<Element> result = new LinkedList<Element>();

		if (STGGraphComponent.isShorthandPlace(place, null)) {
			net.strongdesign.stg.Node n1 = place.getParents().iterator().next();
			net.strongdesign.stg.Node n2 = place.getChildren().iterator()
					.next();
			result.add(createConnection(doc, stg, n1, n2, 1));

		} else {

			for (net.strongdesign.stg.Node n : place.getParents())
				result.add(createConnection(doc, stg, n, place,
						n.getChildValue(place)));

			for (net.strongdesign.stg.Node n : place.getChildren())
				result.add(createConnection(doc, stg, place, n,
						place.getChildValue(n)));

		}

		return result;
	}

	public static String export(STG stg) {
		minx = 100000;
		miny = 100000;

		// first, find the upper left corner
		for (net.strongdesign.stg.Node n : stg.getNodes()) {
			if (n instanceof Place) {
				if (!STGGraphComponent.isShorthandPlace(n, null)) {
					Point p = stg.getCoordinates(n);
					if (p != null) {
						minx = Math.min(minx, p.x - placeRadius - 2);
						miny = Math.min(miny, p.y - placeRadius - 2);
					}
				}
			} else if (n instanceof Transition) {
				Point p = stg.getCoordinates(n);
				if (p != null) {
					minx = Math.min(minx, p.x - boxWidth / 2 - 1);
					miny = Math.min(miny, p.y - boxHeight / 2 - 1);
				}
			}
		}

		try {

			// create the DOM document
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			// output svg content
			Element svg = doc.createElement("svg");
			doc.appendChild(svg);

			svg.setAttribute("xmlns", "http://www.w3.org/2000/svg");
			svg.setAttribute("version", "1.2");
			svg.setAttribute("text-anchor", "middle");
			svg.setAttribute("font-size", "14");
			svg.setAttribute("font-family", "Arial");
			svg.setAttribute("stroke-linecap", "butt");
			svg.setAttribute("style", "stroke-width:1;stroke:black;fill:none;");

			Element desc = doc.createElement("desc");
			desc.appendChild(doc.createTextNode(Messages
					.getString("STGFile.stg_start_comment") + " " + new Date()));

			svg.appendChild(desc);

			Element defs = doc.createElement("defs");
			defs.appendChild(PNArrowMarker(doc));
			svg.appendChild(defs);

			// add all transitions
			for (Transition t : stg
					.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
				for (Element e : createTransition(doc, stg, t)) {
					svg.appendChild(e);
				}
			}

			// add all places
			for (Place p : stg.getPlaces()) {
				Element pe = createPlace(doc, stg, p);
				if (pe != null) {
					svg.appendChild(pe);
				}
			}

			// add all connections
			for (Place p : stg.getPlaces()) {
				for (Element e : createConnections(doc, stg, p)) {
					if (e != null)
						svg.appendChild(e);
				}
			}

			// create string from xml tree
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);
			
			String ret = sw.toString();
			
			// dirty hack, TODO: find a decent way to output UTF-8 characters...
			ret = ret.replaceAll("\u03bb", "&#x3bb;");
			
			return ret;

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}

		return null;
	}
}
