/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

 */

package geogebra.common.kernel.statistics;

import geogebra.common.kernel.Construction;
import geogebra.common.kernel.commands.Commands;
import geogebra.common.kernel.geos.GeoList;

/**
 * Sxx of a list of Points
 * 
 * @author Michael Borcherds
 * @version 2008-02-23
 */

public class AlgoListSXX extends AlgoStats2D {

	public AlgoListSXX(Construction cons, String label, GeoList geoListx) {
		super(cons, label, geoListx, AlgoStats2D.STATS_SXX);
	}

	public AlgoListSXX(Construction cons, GeoList geoListx) {
		super(cons, geoListx, AlgoStats2D.STATS_SXX);
	}

	@Override
	public Commands getClassName() {
		return Commands.SXX;
	}
}
