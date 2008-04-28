/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth
 *     
 ******************************************************************************/
package net.bioclipse.cdk.ui.editors.sdf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.HashMap;

import net.bioclipse.cdk.ui.widgets.JChemPaintWidget;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.Renderer2D;
import org.openscience.cdk.renderer.Renderer2DModel;

/**
 * A class that represents an item in a StructureTable. It consists of an 
 * AtomContainer and a String[] of properties.
 * @author ola
 *
 */
public class StructureTableEntry {

	private Renderer2D renderer;
	private IAtomContainer molecule;
	private HashMap coordinates = new HashMap();

	private final static int compactSize = 200;

	//Properties in table
	String[] columns;

	//The cached image
	Image image;

	public StructureTableEntry(IAtomContainer molecule, String[] columns) {
		this.molecule=molecule;
		this.columns=columns;
	}


	/**
	 * Draw based on object in event
	 * @param event
	 */
	public void draw(Event event) {

		if (event.index==0){
			if (image!=null)
				event.gc.drawImage(image, event.x, event.y);
			else{
				image=computeStructureImage(event);
				if (image==null){
					System.out.println("Could not create structure image.");
					event.gc.drawText("Error", event.x, event.y);
					return;
				}
				event.gc.drawImage(image, event.x, event.y);
			}
		}
		else{
			drawProperty(event);
		}


	}

	/**
	 * Render all except structure as text
	 * @param event
	 */
	private void drawProperty(Event event) {
		String text=columns[event.index];
		event.gc.drawText(text, event.x, event.y);
	}


	/**
	 * Render structure using JCPWidget as Image
	 * @param event
	 */
	private Image computeStructureImage(Event event) {

		renderer = new Renderer2D(new Renderer2DModel());
		Dimension screenSize = new Dimension(200, event.height);
		renderer.getRenderer2DModel().setBackgroundDimension(screenSize);
		renderer.getRenderer2DModel().setDrawNumbers(false);
		setCompactedNess(screenSize);

		int xsize = 200;
		int ysize = event.height;

		BufferedImage bufImage = new BufferedImage(
				xsize, ysize, BufferedImage.TYPE_INT_RGB );

		Graphics graphics = bufImage.createGraphics();
		graphics.setColor( Color.WHITE );
		graphics.fillRect( 0, 0, xsize, ysize );

		GeometryTools.translateAllPositive(molecule);
		GeometryTools.scaleMolecule(molecule, screenSize, 0.8);          
		GeometryTools.center(molecule, screenSize);

		renderer.getRenderer2DModel().setRenderingCoordinates(coordinates);
		renderer.getRenderer2DModel().setBackgroundDimension(screenSize);
		renderer.paintMolecule(
				molecule, 
				(Graphics2D)graphics,
				false, true
		);

		Image swtimage = new Image(
				Display.getDefault(),
				convertToSWT(bufImage)
		);

		return swtimage;
	}


	private void setCompactedNess(Dimension dimensions) {
		if (dimensions.height < compactSize ||
				dimensions.width < compactSize) {
			renderer.getRenderer2DModel().setIsCompact(true);
		} else {
			renderer.getRenderer2DModel().setIsCompact(false);
		}
	}

	private static ImageData convertToSWT( BufferedImage bufferedImage ) {

		ColorModel colorModel = bufferedImage.getColorModel();
		if ( colorModel instanceof DirectColorModel )
			return convertDirectColorImageToSWT( bufferedImage );

		if (bufferedImage.getColorModel() instanceof IndexColorModel) {
			return convertIndexColorModelToSWT( bufferedImage );
		}

		throw new IllegalArgumentException("Unrecognized color model " +
				bufferedImage.getColorModel());
	}

	private static ImageData convertDirectColorImageToSWT(
			BufferedImage bufferedImage ) {

		DirectColorModel colorModel = (DirectColorModel)
		bufferedImage.getColorModel();

		PaletteData palette = new PaletteData(
				colorModel.getRedMask(),
				colorModel.getGreenMask(),
				colorModel.getBlueMask() );

		ImageData data = new ImageData(
				bufferedImage.getWidth(),
				bufferedImage.getHeight(),
				colorModel.getPixelSize(),
				palette);

		WritableRaster raster = bufferedImage.getRaster();

		int[] pixelArray = new int[3];
		for (int y = 0; y < data.height; y++) {
			for (int x = 0; x < data.width; x++) {
				raster.getPixel(x, y, pixelArray);
				int pixel = palette.getPixel(
						new RGB( pixelArray[0],
								pixelArray[1],
								pixelArray[2]) );
				data.setPixel(x, y, pixel);
			}
		}

		return data;
	}

	private static ImageData convertIndexColorModelToSWT(
			BufferedImage bufferedImage ) {

		IndexColorModel colorModel = (IndexColorModel)
		bufferedImage.getColorModel();

		int size = colorModel.getMapSize();

		byte[] reds = new byte[size];
		byte[] greens = new byte[size];
		byte[] blues = new byte[size];

		colorModel.getReds(reds);
		colorModel.getGreens(greens);
		colorModel.getBlues(blues);

		RGB[] rgbs = new RGB[size];

		for (int i = 0; i < rgbs.length; i++)
			rgbs[i] = new RGB(
					reds[i] & 0xFF,
					greens[i] & 0xFF,
					blues[i] & 0xFF );

		PaletteData palette = new PaletteData(rgbs);
		ImageData data = new ImageData(
				bufferedImage.getWidth(),
				bufferedImage.getHeight(),
				colorModel.getPixelSize(),
				palette );

		data.transparentPixel = colorModel.getTransparentPixel();
		WritableRaster raster = bufferedImage.getRaster();

		int[] pixelArray = new int[1];
		for (int y = 0; y < data.height; y++) {
			for (int x = 0; x < data.width; x++) {
				raster.getPixel(x, y, pixelArray);
				data.setPixel(x, y, pixelArray[0]);
			}
		}
		return data;
	}



}
