package org.geogebra.common.kernel.commands;

import org.geogebra.common.geogebra3D.kernel3D.implicit3D.GeoImplicitSurface;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.Path;
import org.geogebra.common.kernel.algos.AlgoIntersect;
import org.geogebra.common.kernel.algos.AlgoIntersectConics;
import org.geogebra.common.kernel.algos.AlgoIntersectCurveCurve;
import org.geogebra.common.kernel.algos.AlgoIntersectFunctions;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.algos.AlgoIntersectPolynomialLine;
import org.geogebra.common.kernel.algos.AlgoIntersectPolynomials;
import org.geogebra.common.kernel.algos.AlgoIntersectSingle;
import org.geogebra.common.kernel.arithmetic.Command;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoCurveCartesian;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoFunction;
import org.geogebra.common.kernel.geos.GeoFunctionable;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoPolyLine;
import org.geogebra.common.kernel.geos.GeoPolygon;
import org.geogebra.common.kernel.implicit.AlgoIntersectImplicitpolys;
import org.geogebra.common.kernel.implicit.GeoImplicit;
import org.geogebra.common.kernel.implicit.GeoImplicitCurve;
import org.geogebra.common.kernel.kernelND.GeoLineND;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.main.MyError;

/**
 * Intersect[ <GeoLine>, <GeoLine> ] Intersect[ <GeoLine>,
 * <GeoPolygon> ]Intersect[ <GeoPolyLine>, <GeoPolyLine> ] Intersect[ <GeoLine>,
 * <GeoConic> ] Intersect[ <GeoConic>, <GeoLine> ] Intersect[ <GeoConic>,
 * <GeoConic> ] Intersect[ <GeoFunction>, <GeoFunction> ] Intersect[
 * <GeoFunction>, <GeoLine> ] Intersect[ <GeoImplicitPoly>, <GeoImplicitPoly> ]
 * Intersect[ <GeoImplicitPoly>, <GeoLine> ] Intersect[ <GeoImplicitPoly>,
 * <GeoFunction(Polynomial)> ] Intersect[ <GeoFunction>, <GeoFunction>,
 * <NumberValue>, <NumberValue> ] Intersect[ <Path>, <Point> ]
 */
public class CmdIntersect extends CommandProcessor {

	/**
	 * Create new command processor
	 * 
	 * @param kernel
	 *            kernel
	 */
	public CmdIntersect(Kernel kernel) {
		super(kernel);
	}

	@Override
	public GeoElement[] process(Command c) throws MyError {
		int n = c.getArgumentNumber();
		boolean[] ok = new boolean[n];
		GeoElement[] arg;

		// Application.debug(n,1);

		switch (n) {
		case 2:
			arg = resArgs(c);
			// Line - Line
			if ((ok[0] = (arg[0].isGeoLine()))
					&& (ok[1] = (arg[1].isGeoLine()))) {
				GeoElement[] ret = { (GeoElement) getAlgoDispatcher()
						.IntersectLines(c.getLabel(), (GeoLine) arg[0],
								(GeoLine) arg[1]) };
				return ret;
			}
			// Line - Parametric Curve
			else if ((ok[0] = (arg[0].isGeoLine()))
					&& (ok[1] = (arg[1] instanceof GeoCurveCartesian))) {
				GeoElement[] ret = getAlgoDispatcher().IntersectLineCurve(
						c.getLabels(), (GeoLine) arg[0],
						(GeoCurveCartesian) arg[1]);
				return ret;
			} else if ((ok[0] = (arg[0] instanceof GeoCurveCartesian))
					&& (ok[1] = (arg[1].isGeoLine()))) {
				GeoElement[] ret = getAlgoDispatcher().IntersectLineCurve(
						c.getLabels(), (GeoLine) arg[1],
						(GeoCurveCartesian) arg[0]);
				return ret;
			}
			// curve - curve
			else if ((ok[0] = (arg[0] instanceof GeoCurveCartesian))
					&& (ok[1] = (arg[1] instanceof GeoCurveCartesian))) {
				GeoElement[] ret = getAlgoDispatcher().IntersectCurveCurve(
						c.getLabels(), (GeoCurveCartesian) arg[1],
						(GeoCurveCartesian) arg[0]);
				return ret;
			}
			// Line - PolyLine
			else if ((ok[0] = (arg[0].isGeoLine()))
					&& (ok[1] = (arg[1] instanceof GeoPolyLine))) {
				GeoElement[] ret = getAlgoDispatcher().IntersectLinePolyLine(
						c.getLabels(), (GeoLine) arg[0], (GeoPolyLine) arg[1]);
				return ret;
			} else if ((ok[0] = (arg[0] instanceof GeoPolyLine))
					&& (ok[1] = (arg[1].isGeoLine()))) {
				GeoElement[] ret = getAlgoDispatcher().IntersectLinePolyLine(
						c.getLabels(), (GeoLine) arg[1], (GeoPolyLine) arg[0]);
				return ret;
			}

			/**
			 * @author thilina
			 */
			// PolyLine - PolyLine
			else if ((ok[0] = (arg[0] instanceof GeoPolyLine))
					&& (ok[1] = (arg[1] instanceof GeoPolyLine))) {
				GeoElement[] ret = getAlgoDispatcher().IntersectPolyLines(
						c.getLabels(), (GeoPolyLine) arg[1],
						(GeoPolyLine) arg[0]);
				return ret;
			}

			// Line - Polygon(as boudary)
			else if ((ok[0] = (arg[0].isGeoLine()))
					&& (ok[1] = (arg[1].isGeoPolygon()))) {
				GeoElement[] ret = getAlgoDispatcher().IntersectLinePolygon(
						c.getLabels(), (GeoLine) arg[0], (GeoPolygon) arg[1]);
				return ret;
			} else if ((ok[0] = (arg[0].isGeoPolygon()))
					&& (ok[1] = (arg[1].isGeoLine()))) {
				GeoElement[] ret = getAlgoDispatcher().IntersectLinePolygon(
						c.getLabels(), (GeoLine) arg[1], (GeoPolygon) arg[0]);
				return ret;
			}

			// Line - Polygon(as region)
			// ---- see CmdIntersectionPaths

			/**
			 * @author thilina
			 */
			// PolyLine - Polygon(as boundary)
			else if ((ok[0] = arg[0].isGeoPolyLine())
					&& (ok[1] = arg[1].isGeoPolygon())) {
				GeoElement[] ret = getAlgoDispatcher().IntersectPolyLinePolygon(
						c.getLabels(), (GeoPolyLine) arg[0],
						(GeoPolygon) arg[1]);
				return ret;
			} else if ((ok[0] = arg[0].isGeoPolygon())
					&& (ok[1] = arg[1].isGeoPolyLine())) {
				GeoElement[] ret = getAlgoDispatcher().IntersectPolyLinePolygon(
						c.getLabels(), (GeoPolyLine) arg[1],
						(GeoPolygon) arg[0]);
				return ret;
			}

			/**
			 * @author thilina
			 */
			// Polygon(as boundary) - Polygon(as boundary)
			else if ((ok[0] = arg[0].isGeoPolygon())
					&& (ok[1] = arg[1].isGeoPolygon())) {
				GeoElement[] ret = getAlgoDispatcher().IntersectPolygons(
						c.getLabels(), (GeoPolygon) arg[0], (GeoPolygon) arg[1],
						false);
				return ret;
			}

			// polygon(as region) - Polygon(as region)
			// ---- see CmdIntersectionPaths

			// Line - Conic
			else if ((ok[0] = (arg[0].isGeoLine()))
					&& (ok[1] = (arg[1].isGeoConic()))) {
				return (GeoElement[]) getAlgoDispatcher().IntersectLineConic(
						c.getLabels(), (GeoLine) arg[0], (GeoConic) arg[1]);
			} else if ((ok[0] = (arg[0].isGeoConic()))
					&& (ok[1] = (arg[1].isGeoLine()))) {
				return (GeoElement[]) getAlgoDispatcher().IntersectLineConic(
						c.getLabels(), (GeoLine) arg[1], (GeoConic) arg[0]);
			} else if ((ok[0] = (arg[0].isGeoPolyLine()))
					&& (ok[1] = (arg[1].isGeoConic()))) {
				return getAlgoDispatcher().IntersectPolyLineConic(c.getLabels(),
						(GeoPolyLine) arg[0], (GeoConic) arg[1]);
			} else if ((ok[0] = (arg[0].isGeoConic()))
					&& (ok[1] = (arg[1].isGeoPolyLine()))) {
				return getAlgoDispatcher().IntersectPolyLineConic(c.getLabels(),
						(GeoPolyLine) arg[1], (GeoConic) arg[0]);
			} else if ((ok[0] = (arg[0].isGeoPolygon()))
					&& (ok[1] = (arg[1].isGeoConic()))) {
				return getAlgoDispatcher().IntersectPolygonConic(c.getLabels(),
						(GeoPolygon) arg[0], (GeoConic) arg[1], false);
			} else if ((ok[0] = (arg[0].isGeoConic()))
					&& (ok[1] = (arg[1].isGeoPolyLine()))) {
				return getAlgoDispatcher().IntersectPolygonConic(c.getLabels(),
						(GeoPolygon) arg[1], (GeoConic) arg[0], false);
			} else if ((ok[0] = (arg[0].isGeoFunction()))
					&& (ok[1] = (arg[1].isGeoConic()))) {
				return getAlgoDispatcher().IntersectPolynomialConic(
						c.getLabels(), (GeoFunction) arg[0], (GeoConic) arg[1]);
			} else if ((ok[0] = (arg[0].isGeoConic()))
					&& (ok[1] = (arg[1].isGeoFunction()))) {
				return getAlgoDispatcher().IntersectPolynomialConic(
						c.getLabels(), (GeoFunction) arg[1], (GeoConic) arg[0]);
			} else if ((ok[0] = (arg[0].isGeoConic()))
					&& (ok[1] = (arg[1].isGeoConic()))) {
				return (GeoElement[]) getAlgoDispatcher().IntersectConics(
						c.getLabels(), (GeoConic) arg[0], (GeoConic) arg[1]);
			} else if ((ok[0] = (arg[0].isGeoFunctionable()))
					&& (ok[1] = (arg[1].isGeoLine()))) {
				return getAlgoDispatcher().IntersectPolynomialLine(
						c.getLabels(),
						((GeoFunctionable) arg[0]).getGeoFunction(),
						(GeoLine) arg[1], null);
			} else if ((ok[0] = (arg[0].isGeoLine()))
					&& (ok[1] = (arg[1].isGeoFunctionable()))) {
				return getAlgoDispatcher().IntersectPolynomialLine(
						c.getLabels(),
						((GeoFunctionable) arg[1]).getGeoFunction(),
						(GeoLine) arg[0], null);
			} else if ((ok[0] = (arg[0].isGeoFunctionable()))
					&& (ok[1] = (arg[1].isGeoPolyLine()))
					&& ((GeoFunctionable) arg[0]).getGeoFunction()
							.isPolynomialFunction(false)) {

				return getAlgoDispatcher().IntersectPolynomialPolyLine(
						c.getLabels(),
						((GeoFunctionable) arg[0]).getGeoFunction(),
						(GeoPolyLine) arg[1]);

			} else if ((ok[0] = (arg[0].isGeoPolyLine()))
					&& (ok[1] = (arg[1].isGeoFunctionable()))
					&& ((GeoFunctionable) arg[1]).getGeoFunction()
							.isPolynomialFunction(false)) {

				return getAlgoDispatcher().IntersectPolynomialPolyLine(
						c.getLabels(),
						((GeoFunctionable) arg[1]).getGeoFunction(),
						(GeoPolyLine) arg[0]);
			}

			// polynomial-polygon
			else if ((ok[0] = (arg[0].isGeoFunctionable()))
					&& (ok[1] = (arg[1].isGeoPolygon()))
					&& ((GeoFunctionable) arg[0]).getGeoFunction()
							.isPolynomialFunction(false)) {

				return getAlgoDispatcher().IntersectPolynomialPolygon(
						c.getLabels(),
						((GeoFunctionable) arg[0]).getGeoFunction(),
						(GeoPolygon) arg[1]);

			} else if ((ok[0] = (arg[0].isGeoPolygon()))
					&& (ok[1] = (arg[1].isGeoFunctionable()))
					&& ((GeoFunctionable) arg[1]).getGeoFunction()
							.isPolynomialFunction(false)) {

				return getAlgoDispatcher().IntersectPolynomialPolygon(
						c.getLabels(),
						((GeoFunctionable) arg[1]).getGeoFunction(),
						(GeoPolygon) arg[0]);

			}

			else if ( // check after GeoLine as GeoLine is now GeoFunctionable
			(ok[0] = (arg[0].isGeoFunctionable()
					|| arg[0] instanceof GeoFunction))
					&& (ok[1] = (arg[1].isGeoFunctionable()
							|| arg[1] instanceof GeoFunction))) {
				return getAlgoDispatcher().IntersectPolynomials(c.getLabels(),
						((GeoFunctionable) arg[0]).getGeoFunction(),
						((GeoFunctionable) arg[1]).getGeoFunction());
			} else if ((ok[0] = (arg[0].isGeoImplicitPoly()))
					&& (ok[1] = (arg[1].isGeoFunctionable())
							// && (ok[1]=((GeoFunctionable)
							// arg[1]).getGeoFunction().isPolynomialFunction(false))
							&& (ok[1] = !(arg[1].isGeoLine())))) {
				return getAlgoDispatcher().IntersectImplicitpolyPolynomial(
						c.getLabels(), (GeoImplicit) arg[0],
						((GeoFunctionable) arg[1]).getGeoFunction());
			} else if ((ok[0] = arg[0].isGeoFunctionable())
					// && (ok[0]=((GeoFunctionable)
					// arg[0]).getGeoFunction().isPolynomialFunction(false))
					&& (ok[0] = !(arg[0].isGeoLine()))
					&& (ok[1] = arg[1].isGeoImplicitPoly())) {
				return getAlgoDispatcher().IntersectImplicitpolyPolynomial(
						c.getLabels(), (GeoImplicit) arg[1],
						((GeoFunctionable) arg[0]).getGeoFunction());
			} else if ((ok[0] = (arg[0].isGeoImplicitPoly()))
					&& (ok[1] = (arg[1].isGeoLine()))) {
				return getAlgoDispatcher().IntersectImplicitpolyLine(
						c.getLabels(), (GeoImplicit) arg[0], (GeoLine) arg[1]);
			} else if ((ok[1] = (arg[1].isGeoImplicitPoly()))
					&& (ok[0] = (arg[0].isGeoLine()))) {
				return getAlgoDispatcher().IntersectImplicitpolyLine(
						c.getLabels(), (GeoImplicit) arg[1], (GeoLine) arg[0]);
			} else if ((ok[0] = (arg[0].isGeoImplicitPoly()))
					&& (ok[1] = (arg[1].isGeoPolyLine()))) {
				return getAlgoDispatcher().IntersectImplicitpolyPolyLine(
						c.getLabels(), (GeoImplicit) arg[0],
						(GeoPolyLine) arg[1]);
			} else if ((ok[1] = (arg[1].isGeoImplicitPoly()))
					&& (ok[0] = (arg[0].isGeoPolyLine()))) {
				return getAlgoDispatcher().IntersectImplicitpolyPolyLine(
						c.getLabels(), (GeoImplicit) arg[1],
						(GeoPolyLine) arg[0]);
			} else if ((ok[0] = (arg[0].isGeoImplicitPoly()))
					&& (ok[1] = (arg[1].isGeoPolygon()))) {
				return getAlgoDispatcher().IntersectImplicitpolyPolygon(
						c.getLabels(), (GeoImplicit) arg[0],
						(GeoPolygon) arg[1]);
			} else if ((ok[1] = (arg[1].isGeoImplicitPoly()))
					&& (ok[0] = (arg[0].isGeoPolygon()))) {
				return getAlgoDispatcher().IntersectImplicitpolyPolygon(
						c.getLabels(), (GeoImplicit) arg[1],
						(GeoPolygon) arg[0]);
			} else if ((ok[0] = (arg[0].isGeoImplicitPoly()))
					&& (ok[1] = (arg[1].isGeoImplicitPoly()))) {
				return getAlgoDispatcher().IntersectImplicitpolys(c.getLabels(),
						(GeoImplicit) arg[0], (GeoImplicit) arg[1]);
			} else if ((ok[0] = (arg[0].isGeoImplicitPoly()))
					&& (ok[1] = (arg[1].isGeoConic()))) {
				return getAlgoDispatcher().IntersectImplicitpolyConic(
						c.getLabels(), (GeoImplicit) arg[0], (GeoConic) arg[1]);
			} else if ((ok[1] = (arg[1].isGeoImplicitPoly()))
					&& (ok[0] = (arg[0].isGeoConic()))) {
				return getAlgoDispatcher().IntersectImplicitpolyConic(
						c.getLabels(), (GeoImplicit) arg[1], (GeoConic) arg[0]);
			} else if ((ok[0] = arg[0].isGeoImplicitCurve())
					&& (ok[1] = arg[1].isGeoLine())) {
				return getAlgoDispatcher().IntersectImplicitCurveLine(
						c.getLabels(), (GeoImplicitCurve) arg[0],
						(GeoLine) arg[1]);
			} else if ((ok[0] = arg[0].isGeoImplicitCurve())
					&& (ok[1] = arg[1].isGeoConic())) {
				return getAlgoDispatcher().IntersectImplicitCurveConic(
						c.getLabels(), (GeoImplicitCurve) arg[0],
						(GeoConic) arg[1]);
			} else if ((ok[0] = arg[0].isGeoImplicitCurve())
					&& (ok[1] = arg[1].isGeoFunction())) {
				return getAlgoDispatcher().IntersectImplicitCurveFunction(
						c.getLabels(), (GeoImplicitCurve) arg[0],
						(GeoFunction) arg[1]);
			} else if ((ok[0] = arg[0].isGeoImplicitCurve())
					&& (ok[1] = arg[1].isGeoImplicitCurve())) {
				return getAlgoDispatcher().IntersectImplicitCurveImpCurve(
						c.getLabels(), (GeoImplicit) arg[0],
						(GeoImplicit) arg[1]);
			} else if ((ok[0] = arg[0].isGeoLine())
					&& (ok[1] = arg[1].isGeoImplicitCurve())) {
				return getAlgoDispatcher().IntersectImplicitCurveLine(
						c.getLabels(), (GeoImplicitCurve) arg[1],
						(GeoLine) arg[0]);
			} else if ((ok[0] = arg[0].isGeoConic())
					&& (ok[1] = arg[1].isGeoImplicitCurve())) {
				return getAlgoDispatcher().IntersectImplicitCurveConic(
						c.getLabels(), (GeoImplicitCurve) arg[1],
						(GeoConic) arg[0]);
			} else if ((ok[0] = arg[0].isGeoFunction())
					&& (ok[1] = arg[1].isGeoImplicitCurve())) {
				return getAlgoDispatcher().IntersectImplicitCurveFunction(
						c.getLabels(), (GeoImplicitCurve) arg[1],
						(GeoFunction) arg[0]);
			} else if ((ok[0] = arg[0].isGeoLine())
					&& (ok[1] = arg[1].isGeoImplicitSurface())) {
				return getAlgoDispatcher().IntersectImplicitSurfaceLine(
						c.getLabels(), (GeoImplicitSurface) arg[1],
						(GeoLineND) arg[0]);
			} else if ((ok[0] = arg[0].isGeoImplicitSurface())
					&& (ok[1] = arg[1].isGeoLine())) {
				return getAlgoDispatcher().IntersectImplicitSurfaceLine(
						c.getLabels(), (GeoImplicitSurface) arg[0],
						(GeoLineND) arg[1]);
			}
			// intersect path and point
			else if ((ok[0] = arg[0] instanceof Path)
					&& (ok[1] = arg[1].isGeoPoint())) {
				return getAlgoDispatcher().IntersectPathPoint(c.getLabel(),
						(Path) arg[0], (GeoPointND) arg[1]);
			}
			/*
			 * moved to CmdIntersection to allow Intersect[List, List] to
			 * intersect list elements in the future // intersection of two
			 * lists else if (arg[0].isGeoList() && arg[1].isGeoList() ) {
			 * GeoElement[] ret = {
			 * getAlgoDispatcher().Intersection(c.getLabel(), (GeoList) arg[0],
			 * (GeoList)arg[1] ) }; return ret; }
			 */

			else {
				if (!ok[0]) {
					throw argErr(app, c, arg[0]);
				}
				throw argErr(app, c, arg[1]);
			}

		case 3: // only one of the intersection points: the third argument
			// states which one
			arg = resArgs(c);
			// Line - Conic
			if ((ok[0] = (arg[0].isGeoLine()))
					&& (ok[1] = (arg[1].isGeoConic()))
					&& (ok[2] = (arg[2] instanceof GeoNumberValue))) {
				GeoElement[] ret = {
						IntersectLineConicSingle(c.getLabel(), (GeoLine) arg[0],
								(GeoConic) arg[1], (GeoNumberValue) arg[2]) };
				return ret;
			} else if ((ok[0] = (arg[0].isGeoConic()))
					&& (ok[1] = (arg[1].isGeoLine()))
					&& (ok[2] = (arg[2] instanceof GeoNumberValue))) {
				GeoElement[] ret = {
						IntersectLineConicSingle(c.getLabel(), (GeoLine) arg[1],
								(GeoConic) arg[0], (GeoNumberValue) arg[2]) };
				return ret;
			}
			// Line - Conic with startPoint
			else if ((ok[0] = (arg[0].isGeoLine()))
					&& (ok[1] = (arg[1].isGeoConic()))
					&& (ok[2] = (arg[2].isGeoPoint()))) {
				GeoElement[] ret = {
						IntersectLineConicSingle(c.getLabel(), (GeoLine) arg[0],
								(GeoConic) arg[1], (GeoPoint) arg[2]) };
				return ret;
			} else if ((ok[0] = (arg[0].isGeoConic()))
					&& (ok[1] = (arg[1].isGeoLine()))
					&& (ok[2] = (arg[2].isGeoPoint()))) {
				GeoElement[] ret = {
						IntersectLineConicSingle(c.getLabel(), (GeoLine) arg[1],
								(GeoConic) arg[0], (GeoPoint) arg[2]) };
				return ret;
			}
			// Conic - Conic with startPoint
			else if ((ok[0] = (arg[0].isGeoConic()))
					&& (ok[1] = (arg[1].isGeoConic()))
					&& (ok[2] = (arg[2].isGeoPoint()))) {
				GeoElement[] ret = {
						IntersectConicsSingle(c.getLabel(), (GeoConic) arg[0],
								(GeoConic) arg[1], (GeoPoint) arg[2]) };
				return ret;
			}
			// Conic - Conic
			else if ((ok[0] = (arg[0].isGeoConic()))
					&& (ok[1] = (arg[1].isGeoConic()))
					&& (ok[2] = (arg[2] instanceof GeoNumberValue))) {
				GeoElement[] ret = {
						IntersectConicsSingle(c.getLabel(), (GeoConic) arg[0],
								(GeoConic) arg[1], (GeoNumberValue) arg[2]) };
				return ret;
			}
			// Polynomial - Line with index of point
			// check before GeoFunctionable as GeoLine is now GeoFunctionable
			else if ((ok[0] = (arg[0].isGeoFunctionable()))
					&& (ok[1] = (arg[1].isGeoLine()))
					&& (ok[2] = (arg[2] instanceof GeoNumberValue))) {
				GeoPoint ret =

						IntersectPolynomialLineSingle(c.getLabel(),
								((GeoFunctionable) arg[0]).getGeoFunction(),
								(GeoLine) arg[1], (GeoNumberValue) arg[2]);

				if (ret == null) {
					throw argErr(app, c, arg[0]);
				}

				return new GeoElement[] { ret };
			}
			// Line - Polynomial with index of point
			// check before GeoFunctionable as GeoLine is now GeoFunctionable
			else if ((ok[0] = (arg[0].isGeoLine()))
					&& (ok[1] = (arg[1].isGeoFunctionable()))
					&& (ok[2] = (arg[2] instanceof GeoNumberValue))) {
				GeoPoint ret =

						IntersectPolynomialLineSingle(c.getLabel(),
								((GeoFunctionable) arg[1]).getGeoFunction(),
								(GeoLine) arg[0], (GeoNumberValue) arg[2]);

				if (ret == null) {
					throw argErr(app, c, arg[0]);
				}

				return new GeoElement[] { ret };
			}
			// Polynomial - Polynomial with index of point
			else if ((ok[0] = (arg[0].isGeoFunctionable()))
					&& (ok[1] = (arg[1].isGeoFunctionable()))
					&& (ok[2] = (arg[2] instanceof GeoNumberValue))) {
				GeoElement[] ret = { IntersectPolynomialsSingle(c, arg,
						((GeoFunctionable) arg[0]).getGeoFunction(),
						((GeoFunctionable) arg[1]).getGeoFunction(),
						(GeoNumberValue) arg[2]) };
				return ret;
			}
			// Polynomial - Conic with index of point
			else if ((ok[0] = (arg[0].isGeoFunction()))
					&& (ok[1] = (arg[1].isGeoConic()))
					&& (ok[2] = (arg[2] instanceof GeoNumberValue))) {
				return new GeoElement[] { IntersectPolynomialConicSingle(
						c.getLabel(), (GeoFunction) arg[0], (GeoConic) arg[1],
						(GeoNumberValue) arg[2]) };
			} else if ((ok[0] = (arg[0].isGeoConic()))
					&& (ok[1] = (arg[1].isGeoFunction()))
					&& (ok[2] = (arg[2] instanceof GeoNumberValue))) {
				return new GeoElement[] { IntersectPolynomialConicSingle(
						c.getLabel(), (GeoFunction) arg[1], (GeoConic) arg[0],
						(GeoNumberValue) arg[2]) };
			} else if ((ok[0] = (arg[0].isGeoImplicitPoly()))
					&& (ok[1] = (arg[1].isGeoLine()))
					&& (ok[2] = (arg[2] instanceof GeoNumberValue))) {
				return new GeoElement[] { IntersectImplicitpolyLineSingle(
						c.getLabel(), (GeoImplicit) arg[0], (GeoLine) arg[1],
						(GeoNumberValue) arg[2]) };
			} else if ((ok[1] = (arg[1].isGeoImplicitPoly()))
					&& (ok[0] = (arg[0].isGeoLine()))
					&& (ok[2] = (arg[2] instanceof GeoNumberValue))) {
				return new GeoElement[] { IntersectImplicitpolyLineSingle(
						c.getLabel(), (GeoImplicit) arg[1], (GeoLine) arg[0],
						(GeoNumberValue) arg[2]) };
			}
			// ImplicitPoly - Functionable
			// TODO decide polynomial before CAS loaded
			else if ((ok[0] = (arg[0].isGeoImplicitCurve()))
					&& (ok[1] = (arg[1].isGeoFunctionable())
							&& (ok[2] = (arg[2] instanceof GeoNumberValue))
							// this line uses CAS so check last
							&& (ok[1] = ((GeoFunctionable) arg[1])
									.getGeoFunction()
									.isPolynomialFunction(false)))) {

				GeoPoint ret = IntersectImplicitpolyPolynomialSingle(
						c.getLabel(), (GeoImplicit) arg[0],
						((GeoFunctionable) arg[1]).getGeoFunction(),
						(GeoNumberValue) arg[2]);

				if (ret == null) {
					throw argErr(app, c, arg[0]);
				}

				return new GeoElement[] { ret };
			} else if ((ok[0] = arg[0].isGeoFunctionable())
					&& (ok[1] = (arg[1].isGeoImplicitCurve()))
					&& (ok[2] = (arg[2] instanceof GeoNumberValue))
					// this line uses CAS so check last
					&& (ok[0] = ((GeoFunctionable) arg[0]).getGeoFunction()
							.isPolynomialFunction(false))) {

				GeoPoint ret = IntersectImplicitpolyPolynomialSingle(
						c.getLabel(), (GeoImplicit) arg[1],
						((GeoFunctionable) arg[0]).getGeoFunction(),
						(GeoNumberValue) arg[2]);

				if (ret == null) {
					throw argErr(app, c, arg[1]);
				}

				return new GeoElement[] { ret };
			}

			// implicitPoly - implicitPoly
			else if ((ok[0] = (arg[0].isGeoImplicitPoly()))
					&& (ok[1] = (arg[1].isGeoImplicitPoly()))
					&& (ok[2] = arg[2] instanceof GeoNumberValue)) {
				return new GeoElement[] { IntersectImplicitpolysSingle(
						c.getLabel(), (GeoImplicit) arg[0],
						(GeoImplicit) arg[1], (GeoNumberValue) arg[2]) };
			} else if ((ok[0] = (arg[0].isGeoImplicitCurve()))
					&& (ok[1] = (arg[1].isGeoConic()))
					&& (ok[2] = arg[2] instanceof GeoNumberValue)) {
				return new GeoElement[] { IntersectImplicitpolyConicSingle(
						c.getLabel(), (GeoImplicit) arg[0], (GeoConic) arg[1],
						(GeoNumberValue) arg[2]) };
			} else if ((ok[1] = (arg[1].isGeoImplicitPoly()))
					&& (ok[0] = (arg[0].isGeoConic()))
					&& (ok[2] = arg[2] instanceof GeoNumberValue)) {
				return new GeoElement[] { IntersectImplicitpolyConicSingle(
						c.getLabel(), (GeoImplicit) arg[1], (GeoConic) arg[0],
						(GeoNumberValue) arg[2]) };
			} else if ((ok[0] = (arg[0].isGeoFunctionable()))
					&& (ok[1] = (arg[1].isGeoLine()))
					&& (ok[2] = (arg[2].isGeoPoint()))) {
				GeoElement[] ret = {
						getAlgoDispatcher().IntersectFunctionLine(c.getLabel(),
								((GeoFunctionable) arg[0]).getGeoFunction(),
								(GeoLine) arg[1], (GeoPoint) arg[2]) };
				return ret;
			}
			// Function - Function with startPoint
			else if ((ok[0] = (arg[0].isGeoFunctionable()
					|| arg[0] instanceof GeoFunction))
					&& (ok[1] = (arg[1].isGeoFunctionable()
							|| arg[1] instanceof GeoFunction))
					&& (ok[2] = (arg[2].isGeoPoint()))) {
				GeoElement[] ret = {
						getAlgoDispatcher().IntersectFunctions(c.getLabel(),
								((GeoFunctionable) arg[0]).getGeoFunction(),
								((GeoFunctionable) arg[1]).getGeoFunction(),
								(GeoPoint) arg[2]) };
				return ret;
			}
			// Line - Function with startPoint
			else if ((ok[0] = (arg[0].isGeoLine()))
					&& (ok[1] = (arg[1].isGeoFunctionable()))
					&& (ok[2] = (arg[2].isGeoPoint()))) {
				GeoElement[] ret = {
						getAlgoDispatcher().IntersectFunctionLine(c.getLabel(),
								((GeoFunctionable) arg[1]).getGeoFunction(),
								(GeoLine) arg[0], (GeoPoint) arg[2]) };
				return ret;
			}
			// polyLine - NonPolynomialFunction with startPoint
			else if ((ok[0] = (arg[0].isGeoPolyLine()))
					&& (ok[1] = (arg[1].isGeoFunctionable()))
					&& (ok[2] = (arg[2].isGeoPoint()))) {
				GeoElement[] ret = getAlgoDispatcher()
						.IntersectNPFunctionPolyLine(c.getLabels(),
								((GeoFunctionable) arg[1]).getGeoFunction(),
								(GeoPolyLine) arg[0], (GeoPoint) arg[2]);
				return ret;
			} else if ((ok[0] = (arg[0].isGeoFunctionable()))
					&& (ok[1] = (arg[1].isGeoPolyLine()))
					&& (ok[2] = (arg[2].isGeoPoint()))) {
				GeoElement[] ret = getAlgoDispatcher()
						.IntersectNPFunctionPolyLine(c.getLabels(),
								((GeoFunctionable) arg[0]).getGeoFunction(),
								(GeoPolyLine) arg[1], (GeoPoint) arg[2]);
				return ret;
			}
			// polygon - NonPolynomialFunction with startPoint
			else if ((ok[0] = (arg[0].isGeoPolygon()))
					&& (ok[1] = (arg[1].isGeoFunctionable()))
					&& (ok[2] = (arg[2].isGeoPoint()))) {
				GeoElement[] ret = getAlgoDispatcher()
						.IntersectNPFunctionPolygon(c.getLabels(),
								((GeoFunctionable) arg[1]).getGeoFunction(),
								(GeoPolygon) arg[0], (GeoPoint) arg[2]);
				return ret;
			} else if ((ok[0] = (arg[0].isGeoFunctionable()))
					&& (ok[1] = (arg[1].isGeoPolygon()))
					&& (ok[2] = (arg[2].isGeoPoint()))) {
				GeoElement[] ret = getAlgoDispatcher()
						.IntersectNPFunctionPolygon(c.getLabels(),
								((GeoFunctionable) arg[0]).getGeoFunction(),
								(GeoPolygon) arg[1], (GeoPoint) arg[2]);
				return ret;
			}

			// Syntax Error
			else {
				if (!ok[0]) {
					throw argErr(app, c, arg[0]);
				} else if (!ok[1]) {
					throw argErr(app, c, arg[1]);
				} else {
					throw argErr(app, c, arg[2]);
				}
			}

		case 4:
			arg = resArgs(c);
			// Function - Function in interval [a,b]
			// Polynomial - Polynomial with index of point
			if ((ok[0] = (arg[0].isGeoFunction()))
					&& (ok[1] = (arg[1].isGeoFunction()))
					&& (ok[2] = (arg[2] instanceof GeoNumberValue))
					&& (ok[3] = (arg[3] instanceof GeoNumberValue))) {
				GeoElement[] ret = IntersectFunctions(c.getLabels(),
						(GeoFunction) arg[0], (GeoFunction) arg[1],
						(GeoNumberValue) arg[2], (GeoNumberValue) arg[3]);
				return ret;
				// intersection of curves with starting point for iteration
			} else if ((ok[0] = (arg[0] instanceof GeoCurveCartesian))
					&& (ok[1] = (arg[1] instanceof GeoCurveCartesian))
					&& (ok[2] = (arg[2] instanceof GeoNumberValue))
					&& (ok[3] = (arg[3] instanceof GeoNumberValue))

			) {
				AlgoIntersectCurveCurve algo = new AlgoIntersectCurveCurve(cons,
						c.getLabels(), (GeoCurveCartesian) arg[0],
						(GeoCurveCartesian) arg[1], (GeoNumberValue) arg[2],
						(GeoNumberValue) arg[3]);
				return algo.getOutput();
			}
			throw argErr(app, c, getBadArg(ok, arg));

		default:
			throw argNumErr(app, c, n);
		}
	}

	/**
	 * get single intersection points of a implicitPoly and a line
	 * 
	 * @param idx
	 *            index of choosen point
	 */
	final private GeoPoint IntersectImplicitpolyLineSingle(String label,
			GeoImplicit p, GeoLine l, GeoNumberValue idx) {
		AlgoIntersect algo = getAlgoDispatcher().getIntersectionAlgorithm(p, l);
		AlgoIntersectSingle salgo = new AlgoIntersectSingle(label, algo, idx);
		GeoPoint point = salgo.getPoint();
		return point;
	}

	/**
	 * get single intersection points of implicitPoly and conic
	 * 
	 * @param idx
	 *            index of choosen point
	 */
	final private GeoPoint IntersectImplicitpolyConicSingle(String label,
			GeoImplicit p1, GeoConic c1, GeoNumberValue idx) {
		AlgoIntersectImplicitpolys algo = getAlgoDispatcher()
				.getIntersectionAlgorithm(p1, c1);
		AlgoIntersectSingle salgo = new AlgoIntersectSingle(label, algo, idx);
		GeoPoint point = salgo.getPoint();
		return point;
	}

	/**
	 * get single intersection points of two implicitPolys
	 * 
	 * @param idx
	 *            index of choosen point
	 */
	final private GeoPoint IntersectImplicitpolysSingle(String label,
			GeoImplicit p1, GeoImplicit p2, GeoNumberValue idx) {
		AlgoIntersectImplicitpolys algo = getAlgoDispatcher()
				.getIntersectionAlgorithm(p1, p2);
		AlgoIntersectSingle salgo = new AlgoIntersectSingle(label, algo, idx);
		GeoPoint point = salgo.getPoint();
		return point;
	}

	/**
	 * get single intersection points of a implicitPoly and a line
	 * 
	 * @param idx
	 *            index of choosen point
	 */
	final private GeoPoint IntersectImplicitpolyPolynomialSingle(String label,
			GeoImplicit p, GeoFunction f, GeoNumberValue idx) {
		if (!f.getConstruction().isFileLoading()
				&& !f.isPolynomialFunction(false)) {
			return null;
		}
		AlgoIntersect algo = getAlgoDispatcher().getIntersectionAlgorithm(p, f);
		AlgoIntersectSingle salgo = new AlgoIntersectSingle(label, algo, idx);
		GeoPoint point = salgo.getPoint();
		return point;
	}

	final private GeoPoint IntersectPolynomialConicSingle(String label,
			GeoFunction f, GeoConic c, GeoNumberValue idx) {
		AlgoIntersect algo = getAlgoDispatcher().getIntersectionAlgorithm(f, c);
		AlgoIntersectSingle salgo = new AlgoIntersectSingle(label, algo, idx);
		GeoPoint point = salgo.getPoint();
		return point;
	}

	/**
	 * get only one intersection point of two conics
	 */
	final private GeoPoint IntersectConicsSingle(String label, GeoConic a,
			GeoConic b, GeoPoint refPoint) {
		AlgoIntersectConics algo = getAlgoDispatcher()
				.getIntersectionAlgorithm(a, b); // index - 1
		// to start
		// at 0
		AlgoIntersectSingle salgo = new AlgoIntersectSingle(label, algo,
				refPoint);
		GeoPoint point = salgo.getPoint();
		return point;
	}

	/**
	 * get only one intersection point of two conics
	 */
	final private GeoPoint IntersectConicsSingle(String label, GeoConic a,
			GeoConic b, GeoNumberValue index) {
		AlgoIntersectConics algo = getAlgoDispatcher()
				.getIntersectionAlgorithm(a, b); // index - 1
		// to start
		// at 0
		AlgoIntersectSingle salgo = new AlgoIntersectSingle(label, algo, index);
		GeoPoint point = salgo.getPoint();
		return point;
	}

	/**
	 * get only one intersection point of line/Conic near to a given point
	 */
	final private GeoPoint IntersectLineConicSingle(String label, GeoLine a,
			GeoConic b, GeoPoint refPoint) {
		AlgoIntersectLineConic algo = getAlgoDispatcher()
				.getIntersectionAlgorithm(a, b); // index -
		// 1 to
		// start
		// at 0
		AlgoIntersectSingle salgo = new AlgoIntersectSingle(label, algo,
				refPoint);
		GeoPoint point = salgo.getPoint();
		return point;
	}

	/**
	 * get only one intersection point of a line and a conic
	 */
	final private GeoPoint IntersectLineConicSingle(String label, GeoLine g,
			GeoConic c, GeoNumberValue index) {
		AlgoIntersectLineConic algo = getAlgoDispatcher()
				.getIntersectionAlgorithm(g, c); // index -
		// 1 to
		// start
		// at 0
		AlgoIntersectSingle salgo = new AlgoIntersectSingle(label, algo, index);
		GeoPoint point = salgo.getPoint();
		return point;
	}

	/**
	 * get only one intersection point of a line and a function
	 */
	final private GeoPoint IntersectPolynomialLineSingle(String label,
			GeoFunction f, GeoLine l, GeoNumberValue index) {
		if (!f.getConstruction().isFileLoading()
				&& !f.isPolynomialFunction(false)) {
			return null;
		}

		AlgoIntersectPolynomialLine algo = getAlgoDispatcher()
				.getIntersectionAlgorithm(f, l);
		AlgoIntersectSingle salgo = new AlgoIntersectSingle(label, algo, index);
		GeoPoint point = salgo.getPoint();
		return point;
	}

	/**
	 * get only one intersection point of two polynomials a, b with given index
	 */
	/**
	 * get only one intersection point of two polynomials a, b with given index
	 */
	final private GeoPoint IntersectPolynomialsSingle(Command c,
			GeoElement[] arg, GeoFunction a, GeoFunction b,
			GeoNumberValue index) {

		if (!a.getConstruction().isFileLoading()
				&& !a.isPolynomialFunction(false)) {
			throw argErr(app, c, arg[0]);
		}
		if (!b.getConstruction().isFileLoading()
				&& !b.isPolynomialFunction(false)) {
			throw argErr(app, c, arg[1]);
		}

		AlgoIntersectPolynomials algo = getAlgoDispatcher()
				.getIntersectionAlgorithm(a, b); // index
		// - 1
		// to
		// start
		// at 0
		AlgoIntersectSingle salgo = new AlgoIntersectSingle(c.getLabel(), algo,
				index);
		GeoPoint point = salgo.getPoint();
		return point;
	}

	/**
	 * Intersects f and g in interfal [left,right] numerically
	 */
	final private GeoPoint[] IntersectFunctions(String[] labels, GeoFunction f,
			GeoFunction g, GeoNumberValue left, GeoNumberValue right) {
		AlgoIntersectFunctions algo = new AlgoIntersectFunctions(cons, labels,
				f, g, left, right);
		GeoPoint[] S = algo.getIntersectionPoints();
		return S;
	}
}