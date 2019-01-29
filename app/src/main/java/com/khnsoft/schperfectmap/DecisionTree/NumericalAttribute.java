/* jaDTi package - v0.6.1 */

/*
 *  Copyright (c) 2004, Jean-Marc Francois.
 *
 *  This file is part of jaDTi.
 *  jaDTi is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  jaDTi is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jahmm; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 */

package com.khnsoft.schperfectmap.DecisionTree;


/**
 * This class implements a numerical attribute.  The values of such an attribute
 * represented by a double precision number.
 **/
public class NumericalAttribute 
    extends Attribute {
    
    /**
     * Creates a new numerical attribute.
     *
     * @param name The attribute's name.  This argument be <code>null</code>.
     **/
    public NumericalAttribute(String name) {
	super(name);
    }
    
    public String getExpression()
    {
    	String oRet = "1\t" + this.name() + "\n";
    	
    	return oRet;
    }
    
    public NumericalAttribute(String oExp, Object o) 
    {
    	super(null);
    	String [] aoSplit = oExp.split( "\t" );
    	this.setName( aoSplit[ 1 ].trim() );
    }

    public Attribute copy(String name) {
	return new NumericalAttribute(name);
    }
}
