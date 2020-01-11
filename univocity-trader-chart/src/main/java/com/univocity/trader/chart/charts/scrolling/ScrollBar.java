package com.univocity.trader.chart.charts.scrolling;

import com.univocity.trader.chart.gui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class ScrollBar extends MouseAdapter {

	private static final Color glassBlue = new Color(0, 0, 255, 128);
	private static final Color barGray = new Color(128, 128, 128, 64);
	private final Point gradientStart = new Point(0, 0);
	private final Point gradientEnd = new Point(0, 0);

	final NullLayoutPanel parent;

	int height = 10;
	boolean scrolling;
	private boolean scrollRequired;
	private boolean dragging;
	private int dragStart;
	private double visibleProportion;
	private double scrollStep;

	final ScrollHandle scrollHandle = new ScrollHandle(this);

	public ScrollBar(NullLayoutPanel parent) {
		this.parent = parent;
		parent.addMouseMotionListener(this);

		Timer timer = new Timer(500, (e) -> {
			if (!dragging) {
				Point p = MouseInfo.getPointerInfo().getLocation();
				p = new Point(p.x - parent.getLocation().x, p.y - parent.getLocation().y);
				updateHighlight(p);
			}
		}
		);
		timer.start();
	}

	public boolean isScrollingView(){
		return scrollRequired;
	}

	public void draw(Graphics2D g) {
		double required = parent.requiredWidth();
		double available = parent.getWidth();
		scrollRequired = required > available;

		gradientStart.x = parent.getWidth() / 2;
		gradientStart.y = -50;
		gradientEnd.x = parent.getWidth() / 2;
		gradientEnd.y = height + 50;

		g.setPaint(new GradientPaint(gradientStart, glassBlue, gradientEnd, barGray));
		g.fillRect(0, parent.getHeight() - height, parent.getWidth(), height);

		if (scrollRequired) {
			double scrollingArea = available < ScrollHandle.MIN_WIDTH ? ScrollHandle.MIN_WIDTH : available;
			visibleProportion = available / required;
			double handleWidth = scrollingArea * visibleProportion;
			scrollStep = (required - available) / (available - handleWidth);

			scrollHandle.setWidth((int) handleWidth);
			scrollHandle.draw(g, parent);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (scrollRequired) {
			if (scrolling) {
				dragging = true;

				int pixelsToMove = e.getX() - dragStart;

				pixelsToMove = scrollHandle.getMovablePixels(pixelsToMove);
				if (pixelsToMove != 0) {
					scrollHandle.move(pixelsToMove);
					dragStart = e.getX();
				}
			}
		}
	}

	public int getBoundaryRight() {
		return (int) Math.round((scrollHandle.getPosition() + scrollHandle.getWidth()) * scrollStep);
	}

	public int getBoundaryLeft() {
		return (int) Math.round(scrollHandle.getPosition() * scrollStep);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		dragging = false;
	}

	@Override
	public void mouseExited(MouseEvent e) {
		updateHighlight(e.getPoint());
	}

	public double getVisibleProportion(){
		return visibleProportion;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		dragStart = e.getX();
		updateHighlight(e.getPoint());
	}

	private void updateHighlight(Point cursor) {
		boolean prev = scrolling;
		if (scrollRequired) {
			scrolling = scrollHandle.isCursorOver(cursor, parent);
		} else {
			scrolling = false;
		}

		if (prev != scrolling && !dragging) {
			SwingUtilities.invokeLater(parent::repaint);
		}

		if (!scrollRequired) {
			scrollHandle.setPosition(0);
		}
	}
}