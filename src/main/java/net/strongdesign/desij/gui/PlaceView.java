

package net.strongdesign.desij.gui;


public class PlaceView {//extends VertexView {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4073093934436286765L;
	public static transient PlaceRenderer renderer = new PlaceRenderer();
	
	public PlaceView() {
		super();
	}
	
//	public PlaceView(Object cell) {
//		super(cell);
//	}
	
//	public Point2D getPerimeterPoint(EdgeView edge, Point2D source, Point2D p) {
//		Rectangle2D r = getBounds();
//		
//		double x = r.getX();
//		double y = r.getY();
//		double a = (r.getWidth() + 1) / 2;
//		double b = (r.getHeight() + 1) / 2;
//		
//		// x0,y0 - center of ellipse
//		double x0 = x + a;
//		double y0 = y + b;
//		
//		// x1, y1 - point
//		double x1 = p.getX();
//		double y1 = p.getY();
//		
//		// calculate straight line equation through point and ellipse center
//		// y = d * x + h
//		double dx = x1 - x0;
//		double dy = y1 - y0;
//		
//		if (dx == 0)
//			return new Point((int) x0, (int) (y0 + b * dy / Math.abs(dy)));
//		
//		double d = dy / dx;
//		double h = y0 - d * x0;
//		
//		// calculate intersection
//		double e = a * a * d * d + b * b;
//		double f = -2 * x0 * e;
//		double g = a * a * d * d * x0 * x0 + b * b * x0 * x0 - a * a * b * b;
//		
//		double det = Math.sqrt(f * f - 4 * e * g);
//		
//		// two solutions (perimeter points)
//		double xout1 = (-f + det) / (2 * e);
//		double xout2 = (-f - det) / (2 * e);
//		double yout1 = d * xout1 + h;
//		double yout2 = d * xout2 + h;
//		
//		double dist1Squared = Math.pow((xout1 - x1), 2)	+ Math.pow((yout1 - y1), 2);
//		double dist2Squared = Math.pow((xout2 - x1), 2)	+ Math.pow((yout2 - y1), 2);
//		
//		// correct solution
//		double xout, yout;
//		
//		if (dist1Squared < dist2Squared) {
//			xout = xout1;
//			yout = yout1;
//		} else {
//			xout = xout2;
//			yout = yout2;
//		}
//		
//		return getAttributes().createPoint(xout, yout);
//		
//	}
	
	/**
	 */
//	public CellViewRenderer getRenderer() {
//		return renderer;
//	}
	
	/**
	 */
	public static class PlaceRenderer {//extends mxVertexRenderer {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -1527525921557881012L;

		/**
		 * Return a slightly larger preferred size than for a rectangle.
		 */
//		public Dimension getPreferredSize() {
//			Dimension d = super.getPreferredSize();
//			d.width += d.width / 8;
//			d.height += d.height / 2;
//			return d;
//		}
		
		/**
		 */
//		public void paint(Graphics g) {
//			int b = borderWidth;
//			Graphics2D g2 = (Graphics2D) g;
//			Dimension d = getSize();
//			boolean tmp = selected;
//			if (super.isOpaque()) {
//				g.setColor(super.getBackground());
//				if (gradientColor != null && !preview) {
//					setOpaque(false);
//					g2.setPaint(new GradientPaint(0, 0, getBackground(),
//							getWidth(), getHeight(), gradientColor, true));
//				}
//				g.fillOval(b - 1, b - 1, d.width - b, d.height - b);				
//			}
//			try {
//				setBorder(null);
//				setOpaque(false);
//				selected = false;
//				super.paint(g);
//			} finally {
//				selected = tmp;
//			}
//			if (bordercolor != null) {
//				g.setColor(bordercolor);
//				g2.setStroke(new BasicStroke(b));
//				g.drawOval(b - 1, b - 1, d.width - b, d.height - b);
//			}
//			if (selected) {
//				g2.setStroke(GraphConstants.SELECTION_STROKE);
//				g.setColor(highlightColor);
//				g.drawOval(b - 1, b - 1, d.width - b, d.height - b);
//			}
//		}
		
	}
	
}


