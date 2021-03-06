/*******************************************************************************
 * Copyright (c) 2009  Arvid Berg <goglepox@users.sourceforge.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.ui.model;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.SDFElement;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.progress.IElementCollector;

public class MoleculesFromSMI implements IMoleculesFromFile {
    Logger logger = Logger.getLogger( MoleculesFromSMI.class );
    IFile file;
    List<SDFElement> molecules;

    public MoleculesFromSMI(IFile file) {
       this.file = file;
       molecules = Collections.synchronizedList( new LinkedList<SDFElement>());
    }

    public ICDKMolecule getMoleculeAt( int index ) {

        if( molecules.size()> index) {
            return (ICDKMolecule) molecules.get( index )
                        .getAdapter( ICDKMolecule.class );
        }
        return null;
    }

    public int getNumberOfMolecules() {

        return molecules.size();
    }

    public void markDirty( int index, ICDKMolecule moleculeToSave ) {

        throw new UnsupportedOperationException("Can't edit SMILES yet.");
    }

    public void fetchDeferredChildren( Object object,
                                       IElementCollector collector,
                                       IProgressMonitor monitor ) {

       ICDKManager manager = Activator.getDefault().getJavaCDKManager();
       try {
        List<ICDKMolecule> mols = manager.loadSMILESFile( file );
        for(int i=0;i<mols.size();i++) {
            ICDKMolecule molecule = mols.get( i );
            SDFElement element = new SDFElement( file,
                                                 molecule.getName(),
                                                 -1,
                                                 i);
            // FIXME : maybe problem when file changes resource listener should
            // take care of it

            collector.add( element, monitor );
            molecules.add(element);
        }


    } catch ( IOException e ) {
        // TODO Auto-generated catch block
       LogUtils.debugTrace( logger, e );
    } catch ( CoreException e ) {
        // TODO Auto-generated catch block
        LogUtils.debugTrace( logger, e );
    } finally {
        monitor.done();
    }

    }

    public ISchedulingRule getRule( Object object ) {

        // TODO Auto-generated method stub
        return null;
    }

    public boolean isContainer() {
        return true;
    }

    public Object[] getChildren( Object o ) {

        return molecules.toArray();
    }

    public ImageDescriptor getImageDescriptor( Object object ) {

   // TODO : Implement for specific SMILES icon and update MoleculeLabelProvider
        return null;
    }

    public String getLabel( Object o ) {
        return "Molecules";
    }

    public Object getParent( Object o ) {

       return file;
    }

     public void save() {

         throw new UnsupportedOperationException("Can't save SMILES yet.");
    }

     public Collection<Object> getAvailableProperties() {
         return Collections.emptySet();
    }

    public <T> void setPropertyFor( int moleculeIndex, String property, T value ) {
        logger.error( "SMI model dose not support changing properties." );
    }

    public void instert( ICDKMolecule... molecules ) {
        throw new UnsupportedOperationException();
    }

    public void delete( int index ) {
        throw new UnsupportedOperationException();
    }
}
