/* $Revision$ $Author$ $Date$
 *
 * Copyright (C) 2008  Gilleain Torrance <gilleain.torrance@gmail.com>
 * Copyright (C) 2008  Stefan Kuhn (undo redo)
 *
 * Contact: cdk-devel@lists.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All I ask is that proper credit is given for my work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.controller;

import static org.openscience.cdk.CDKConstants.STEREO_BOND_DOWN;
import static org.openscience.cdk.CDKConstants.STEREO_BOND_DOWN_INV;
import static org.openscience.cdk.CDKConstants.STEREO_BOND_NONE;
import static org.openscience.cdk.CDKConstants.STEREO_BOND_UNDEFINED;
import static org.openscience.cdk.CDKConstants.STEREO_BOND_UP;
import static org.openscience.cdk.CDKConstants.STEREO_BOND_UP_INV;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point2d;

import org.openscience.cdk.controller.undoredo.IUndoRedoable;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;


/**
 * Alters the chirality of a bond, setting it to be into or outof the plane.
 * 
 * @author Gilleain Torrance
 * @cdk.svnrev $Revision$
 * @cdk.module control
 */
public class AlterBondStereoModule extends ControllerModuleAdapter {
    
    public enum Direction { UP, DOWN };
    private Direction desiredDirection;

	public AlterBondStereoModule(IChemModelRelay chemModelRelay, Direction desiredDirection) {
		super(chemModelRelay);
		this.desiredDirection = desiredDirection;
	}
	
	/**
	 * Change the stereo bond from start->end to start<-end.
	 * 
	 * @param bond the bond to change
	 * @param stereo the current stereo of that bond
	 */
	private void flipDirection(IBond bond, int stereo) {
	    int stereoDir = STEREO_BOND_UNDEFINED;
	    if (stereo == STEREO_BOND_UP) stereoDir=STEREO_BOND_UP_INV;
	    else if (stereo == STEREO_BOND_UP_INV) stereoDir=STEREO_BOND_UP;
	    else if (stereo == STEREO_BOND_DOWN_INV) stereoDir=STEREO_BOND_DOWN;
	    else if (stereo == STEREO_BOND_DOWN) stereoDir=STEREO_BOND_DOWN_INV;
	    chemModelRelay.setWedgeType(bond,stereoDir);
	}
	
	/**
	 * Change the stereo of the bond from UP<->DOWN.
	 * @param bond the bond to change
	 * @param stereo the current stereo of the bond
	 */
	private void flipOrientation(IBond bond, int stereo) {
	    int stereoDir = STEREO_BOND_UNDEFINED;
	    if (stereo == STEREO_BOND_UP) stereoDir=STEREO_BOND_DOWN_INV;
        else if (stereo == STEREO_BOND_UP_INV) stereoDir=STEREO_BOND_DOWN;
        else if (stereo == STEREO_BOND_DOWN_INV) stereoDir=STEREO_BOND_UP;
        else if (stereo == STEREO_BOND_DOWN) stereoDir=STEREO_BOND_UP_INV;
	    chemModelRelay.setWedgeType(bond,stereoDir);
	}
	
	private boolean isUp(int stereo) {
	    return stereo == STEREO_BOND_UP || stereo == STEREO_BOND_UP_INV;
	}
	
    private boolean isDown(int stereo) {
        return stereo == STEREO_BOND_DOWN || stereo == STEREO_BOND_DOWN_INV;
    }
    
    private boolean noStereo(int stereo) {
        return stereo == STEREO_BOND_NONE || stereo == STEREO_BOND_UNDEFINED;
    }
    
    private void makeNewStereoBond(IAtom atom) {
        String atomType = 
            chemModelRelay.getController2DModel().getDrawElement();
        IAtom newAtom = chemModelRelay.addAtom(atomType, atom);
        IAtomContainer undoRedoContainer=chemModelRelay.getIChemModel().getBuilder().newAtomContainer();
        
        // XXX these calls would not be necessary if addAtom returned a bond
        IAtomContainer atomContainer = 
            ChemModelManipulator.getRelevantAtomContainer(
                    chemModelRelay.getIChemModel(), newAtom);
        IBond newBond = atomContainer.getBond(atom, newAtom);

        if (desiredDirection == Direction.UP) {
            chemModelRelay.setWedgeType( newBond, STEREO_BOND_UP);
        } else {
            chemModelRelay.setWedgeType(newBond, STEREO_BOND_DOWN);
        }
        undoRedoContainer.addAtom(newAtom);
        undoRedoContainer.addBond(newBond);
	    if(chemModelRelay.getUndoRedoFactory()!=null && chemModelRelay.getUndoRedoHandler()!=null){
		    IUndoRedoable undoredo = chemModelRelay.getUndoRedoFactory().getAddAtomsAndBondsEdit(chemModelRelay.getIChemModel(), undoRedoContainer, "Add Stereo Bond",chemModelRelay.getController2DModel());
		    chemModelRelay.getUndoRedoHandler().postEdit(undoredo);
	    }
	}
    
    private void makeBondStereo(IBond bond) {
        int stereo = bond.getStereo();
        boolean isUp = isUp(stereo);
        boolean isDown = isDown(stereo);
        boolean noStereo = noStereo(stereo);
        if (isUp && desiredDirection == Direction.UP) {
            flipDirection(bond, stereo);
        } else if (isDown && desiredDirection == Direction.UP) {
            flipOrientation(bond, stereo);
        } else if (isUp && desiredDirection == Direction.DOWN) {
            flipOrientation(bond, stereo);
        } else if (isDown && desiredDirection == Direction.DOWN) {
            flipDirection(bond, stereo);
        } else if (noStereo && desiredDirection == Direction.UP) {
            chemModelRelay.setWedgeType(bond, STEREO_BOND_UP);
        } else if (noStereo && desiredDirection == Direction.DOWN) {
            chemModelRelay.setWedgeType(bond, STEREO_BOND_DOWN);
        }
        Integer[] stereos=new Integer[2];
        stereos[1]=stereo;
        stereos[0]=bond.getStereo();
		Map<IBond, IBond.Order[]> changedBonds = new HashMap<IBond, IBond.Order[]>();
		Map<IBond, Integer[]> changedBondsStereo = new HashMap<IBond, Integer[]>();
		changedBondsStereo.put(bond, stereos);
	    if(chemModelRelay.getUndoRedoFactory()!=null && chemModelRelay.getUndoRedoHandler()!=null){
	    	IUndoRedoable undoredo = chemModelRelay.getUndoRedoFactory().getAdjustBondOrdersEdit(changedBonds, changedBondsStereo, "Adjust Bond Stereo");
		    chemModelRelay.getUndoRedoHandler().postEdit(undoredo);
	    }
    }
    
	public void mouseClickedDown(Point2d worldCoord) {
	    IAtom atom = this.chemModelRelay.getClosestAtom(worldCoord);
	    IBond bond = this.chemModelRelay.getClosestBond(worldCoord);
	    
	    double dH = super.getHighlightDistance();
        double dA = super.distanceToAtom(atom, worldCoord);
        double dB = super.distanceToBond(bond, worldCoord);
        
	    if (super.noSelection(dA, dB, dH)) {
	        // probably don't make empty stereo bonds in blank space..
	        return;
	    } else if (super.isAtomOnlyInHighlightDistance(dA, dB, dH)) {
	        this.makeNewStereoBond(atom);
	    } else if (super.isBondOnlyInHighlightDistance(dA, dB, dH)) {
	        this.makeBondStereo(bond);
	    } else {
	        // if same distance, default to changing the bond
	        this.makeBondStereo(bond);
	    }
		chemModelRelay.updateView();
	}

	public void setChemModelRelay(IChemModelRelay relay) {
		this.chemModelRelay = relay;
	}

	public String getDrawModeString() {
		if (desiredDirection==Direction.UP) {
			return "Add or convert to bond up";
		} else {
			return "Add or convert to bond down";
		}
	}

}
