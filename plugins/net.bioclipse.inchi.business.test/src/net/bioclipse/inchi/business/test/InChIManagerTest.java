/*******************************************************************************
 * Copyright (c) 2008-2009  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.inchi.business.test;

import junit.framework.Assert;
import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.tests.AbstractManagerTest;
import net.bioclipse.inchi.InChI;
import net.bioclipse.inchi.business.InChIManager;
import net.bioclipse.managers.business.IBioclipseManager;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

public class InChIManagerTest extends AbstractManagerTest {

    InChIManager inchi;

    //Do not use SPRING OSGI for this manager
    //since we are only testing the implementations of the manager methods
    public InChIManagerTest() {
        inchi = new InChIManager();
    }

    public IBioclipseManager getManager() {
        return inchi;
    }

    @Test public void testGenerate() throws Exception {
        CDKManager cdk = new CDKManager();
        IMolecule mol = cdk.fromSMILES("C");
        Assert.assertNotNull("Input structure is unexpectedly null", mol);
        InChI inchiObj = inchi.generate(mol, new NullProgressMonitor());
        Assert.assertNotNull(inchiObj);
        Assert.assertEquals("InChI=1/CH4/h1H4", inchiObj.getValue());
    }

    @Test public void testGenerateNoStereo() throws Exception {
        CDKManager cdk = new CDKManager();
        IMolecule mol = cdk.fromSMILES("ClC(Br)(F)(O)");
        Assert.assertNotNull("Input structure is unexpectedly null", mol);
        InChI inchiStr = inchi.generate(mol, new NullProgressMonitor());
        Assert.assertNotNull(inchiStr);
        Assert.assertEquals(
            "InChI=1/CHBrClFO/c2-1(3,4)5/h5H",
            inchiStr.getValue()
        );
    }

    @Test public void testGenerateKey() throws Exception {
        CDKManager cdk = new CDKManager();
        IMolecule mol = cdk.fromSMILES("C");
        Assert.assertNotNull("Input structure is unexpectedly null", mol);
        InChI key = inchi.generate(mol, new NullProgressMonitor());
        Assert.assertNotNull(key);
        Assert.assertEquals(
            "VNWKTOKETHGBQD-UHFFFAOYAM",
            key.getKey()
        );
    }

}
