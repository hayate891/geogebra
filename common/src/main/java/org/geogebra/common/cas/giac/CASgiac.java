package org.geogebra.common.cas.giac;

import java.math.BigInteger;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.geogebra.common.cas.CASparser;
import org.geogebra.common.kernel.AsynchronousCommand;
import org.geogebra.common.kernel.CASException;
import org.geogebra.common.kernel.CASGenericInterface;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.arithmetic.AssignmentType;
import org.geogebra.common.kernel.arithmetic.Command;
import org.geogebra.common.kernel.arithmetic.Equation;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.ExpressionValue;
import org.geogebra.common.kernel.arithmetic.FunctionNVar;
import org.geogebra.common.kernel.arithmetic.MyArbitraryConstant;
import org.geogebra.common.kernel.arithmetic.MyList;
import org.geogebra.common.kernel.arithmetic.MyVecNDNode;
import org.geogebra.common.kernel.arithmetic.Traversing;
import org.geogebra.common.kernel.arithmetic.Traversing.ArbconstReplacer;
import org.geogebra.common.kernel.arithmetic.Traversing.DiffReplacer;
import org.geogebra.common.kernel.arithmetic.Traversing.PowerRootReplacer;
import org.geogebra.common.kernel.arithmetic.Traversing.PrefixRemover;
import org.geogebra.common.kernel.arithmetic.Traversing.VariableReplacer;
import org.geogebra.common.kernel.arithmetic.ValidExpression;
import org.geogebra.common.kernel.arithmetic.Variable;
import org.geogebra.common.kernel.geos.GeoCasCell;
import org.geogebra.common.kernel.geos.GeoDummyVariable;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.prover.polynomial.PPolynomial;
import org.geogebra.common.kernel.prover.polynomial.PVariable;
import org.geogebra.common.main.settings.AbstractSettings;
import org.geogebra.common.main.settings.CASSettings;
import org.geogebra.common.plugin.Operation;
import org.geogebra.common.util.StringUtil;
import org.geogebra.common.util.debug.Log;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * Platform (Java / GWT) independent part of giac CAS
 */
public abstract class CASgiac implements CASGenericInterface {
	/** parser tools */

	/**
	 * Random number generator
	 */
	protected static final Random rand = new Random();

	/**
	 * string to put Giac into GeoGebra mode (not affected by 'restart')
	 * 
	 */
	public final static String initString = "caseval(\"init geogebra\")";
	/**
	 * In web we need to skip caseval because of emcscripten
	 */
	public final static String initStringWeb = "init geogebra";

	/**
	 * string to put Giac off GeoGebra mode
	 */
	// public final static String closeString = "caseval(\"close geogebra\")";
	// public final static String closeStringWeb = "close geogebra";

	public static enum CustomFunctions {
		/**
		 * 
		 */
		RESTART(null, "restart"),

		/**
		 * suppress warning about = vs :=
		 */
		DISABLE_WARNING(null, "warn_equal_in_prog(0)"),

		/**
		 * 
		 */
		PROBA_EPSILON(null, "proba_epsilon:=0;"),
		/**
		 * used for sorting output of Solve/Solutions/NSolve/NSolutions sort()
		 * doesn't work for list of lists null -> always sent (used by other
		 * definitions)
		 * 
		 * now implemented natively in Giac. This is just for reference /
		 * testing
		 */
		// SORT(null,
		// "ggbsort(x):=when(length(x)==0,{},when(type(x[0])==DOM_LIST,x,sort(x)))"),

		/**
		 * these both give 3 // @size(point(1,2,3)[1]) gives 3
		 * // @size(point((-(5))+(ggbtmpvark),(-(5))+(ggbtmpvark))[1]) gives 3
		 * // so need to check subtype(x[1])==20 to distinguish 2D and 3D
		 * 
		 * null -> always sent (used by other definitions)
		 */
		// IS_3D_POINT(null,
		// "is_3dpoint(x):=when(size(x[1])==3 &&
		// subtype(x[1])==20,true,false)"),
		/**
		 * check whether a is polynomial special cases like y^2=1 also handled
		 * now implemented natively in Giac. This is just for reference /
		 * testing
		 */
		// IS_POLYNOMIAL("ispolynomial",
		// "ispolynomial(a):=when(a[0] == '=' ,is_polynomial(a[1]) &&
		// is_polynomial(a[2]),"
		// + "when (is_polynomial(a) == 1, true, false ) )"),

		/**
		 * 
		 */
		// IS_POLYNOMIAL2("ispolynomial2",
		// "ispolynomial2(a,b):= is_polynomial(a) && is_polynomial(b)"),

		/**
		 * NOTE: works for max 2 variable
		 */
		GGBIS_POLYNOMIAL("ggbisPolynomial", "ggbisPolynomial(a):= when (size(lname(a)) == 1, is_polynomial(a,lname(a)[0])," + "when (size(lname(a)) == 2, is_polynomial(a,lname(a)[0]) && is_polynomial(a,lname(a)[1]), ?))"),

		/**
		 * now implemented natively in Giac. This is just for reference /
		 * testing
		 */
		// GGBALT("ggbalt", "ggbalt(x):=when(type(x)==DOM_IDENT,altsymb(x),"
		// +
		// "when(x[0]=='pnt',when(is_3dpoint(x),atan2(x[1][2],sqrt(x[1][0]^2+x[1][1]^2)),0),?))"),

		/**
		 * xcoordsymb(A) converted back to x(A) in CommandDispatcherGiac
		 * 
		 * check for type(evalf(a)) needed as type(exact(-2.24)+i*exact(-1.54))
		 * gives DOM_RAT
		 * 
		 */
		XCOORD("xcoord", "xcoord(a):=when(type(evalf(a))==DOM_COMPLEX, real(a), when(type(a)==DOM_IDENT,xcoordsymb(a),when(a[0]=='pnt',when(is_3dpoint(a),a[1][0],real(a[1])),when(a[0]=='=',coeff(a[1]-a[2],x,1),a[0]))))"),
		/**
		 * altsymb(P) converted back to alt(P) in CommandDispatcherGiac
		 */
		YCOORD("ycoord", "ycoord(a):=when(type(evalf(a))==DOM_COMPLEX, im(a), when(type(a)==DOM_IDENT,ycoordsymb(a),when(a[0]=='pnt',when(is_3dpoint(a),a[1][1],im(a[1])),when(a[0]=='=',coeff(a[1]-a[2],y,1),a[1]))))"),

		/**
		 * make sure z((1,2)) = 0
		 */
		ZCOORD("zcoord", "zcoord(a):=when(type(a)==DOM_IDENT,zcoordsymb(a),when(a[0]=='pnt',when(is_3dpoint(a),a[1][2],0),when(length(a)<3 && a[0] != '=',0,when(a[0]=='=',coeff(a[1]-a[2],z,1),a[2]))))"),

		/**
		 * unicode0176u passes unaltered through Giac then gets decoded to
		 * degree sign in GeoGebra needed for
		 * "return angle from inverse trig function" see ExpressionNode.degFix()
		 */
		DEG_ASIN("asind", "asind(x):=normal(asin(x)/pi*180)*unicode0176u"),

		/**
		 * unicode0176u passes unaltered through Giac then gets decoded to
		 * degree sign in GeoGebra needed for
		 * "return angle from inverse trig function" see ExpressionNode.degFix()
		 */
		DEG_ACOS("acosd", "acosd(x):=normal(acos(x)/pi*180)*unicode0176u"),

		/**
		 * unicode0176u passes unaltered through Giac then gets decoded to
		 * degree sign in GeoGebra needed for
		 * "return angle from inverse trig function" see ExpressionNode.degFix()
		 */
		DEG_ATAN("atand", "atand(x):=normal(atan(x)/pi*180)*unicode0176u"),

		/**
		 * unicode0176u passes unaltered through Giac then gets decoded to
		 * degree sign in GeoGebra needed for
		 * "return angle from inverse trig function" see ExpressionNode.degFix()
		 */
		DEG_ATAN2("atan2d", "atan2d(y,x):=normal(arg(x+i*y)/pi*180)*unicode0176u"),

		/**
		 * subtype 27 is ggbvect[]
		 * 
		 * abs() in Giac now works for vectors so this isn't needed
		 * 
		 */
		// ABS("ggbabs",
		// "ggbabs(x):=when(x[0]=='pnt' || (type(x)==DOM_LIST &&
		// subtype(x)==27),l2norm(x),abs(x))"),
		/**
		 * check list before equation to avoid out of bounds. flatten helps for
		 * {} and {{{0}}}
		 * 
		 * used for EQUAL_BOOLEAN in ExpressionNode
		 * sb.append("when(ggbIsZero(simplify(");
		 * 
		 * eg sin(x)^2+cos(x)^2==1
		 */
		IS_ZERO("ggbIsZero", "ggbIsZero(x):=when(x==0,true,when(type(x)=='DOM_LIST',max(flatten({x,0}))==min(flatten({x,0}))&&min(flatten({x,0}))==0,when(x[0]=='=',lhs(x)==0&&rhs(x)==0,x[0]=='pnt' && x[1] == ggbvect[0,0,0])))"),
		/**
		 * Convert the polys into primitive polys in the input list (contains
		 * temporary fix for primpart also):
		 */
		PRIM_POLY("primpoly", "primpoly(x):=begin local pps,ii; if (x==[0]) return [0]; pps:=[]; for ii from 0 to size(x)-1 do pps[ii]:=primpart(x[ii],lvar(x[ii])); od return pps end"),
		/**
		 * Compute squarefree factorization of the input poly p. Strange why
		 * sommet(-x)!='-' (so we do an ugly hack here, FIXME)
		 */
		FACTOR_SQR_FREE("factorsqrfree", "factorsqrfree(p):=begin local pf,r,ii; pf:=factor(p); if (sommet(pf)!='*') begin if (sommet(pf)=='^') return op(pf)[0]; else begin if (sommet(pf)!=sommet(-x)) return pf; else return factorsqrfree(-pf); end; end; opf:=op(pf); r:=1; for ii from 0 to size(opf)-1 do r:=r*factorsqrfree(opf[ii]); od return r end"),
		/**
		 * Remove zeroes or linear dependencies from a list (workaround for
		 * buggy eliminate). Probably obsolete and can be removed and
		 * substituted by eliminate().
		 */
		ELIMINATE2("eliminate2", "eliminate2(x,y):=eliminate(eliminate(x,y),y);"),
		/**
		 * Eliminate variables from a polynomial ideal. If the result is a set
		 * of discrete points, then convert the linear polynomials to a product
		 * of circle definitions with zero radius.
		 */
		GEOM_ELIM("geomElim", "geomElim(polys,elimvars,precision):=begin local ee, ll, ff, gg, ii; ee:=eliminate(polys,revlist(elimvars)); ll:=lvar(ee); if (size(ee)>1) begin ff:=round(fsolve(ee,ll)*precision)/precision; gg:=1; for ii from 0 to size(ff)-1 do gg:=gg*(((ll[0]-ff[ii,0])^2+(ll[1]-ff[ii,1])^2)); od; ee:=[expand(lcm(denom(coeff(gg)))*gg)]; end; if (size(ee)==0) return 0; else return primpoly(ee)[0]; end;"),
		/**
		 * Help simplifying the input when computing the Jacobian matrix in the
		 * Envelope command. Input: a list of polynomials and a list of
		 * variables which will not be used as derivatives. Output: another list
		 * of polynomials (a shorter list) which does not contain the linear
		 * ones and equivalent with the input. Note that
		 * op(solve(polys[ii]=0,linvar)[0])[1] is required in GeoGebra mode, in
		 * standard Giac here solve(polys[ii],linvar)[0] should be written.
		 * 
		 * The algorithm finds the polys which have one variable and it is
		 * linear. After solving such a poly=0 equation, the solution will be
		 * substituted into all other polys. After doing this for all one
		 * variable linear polys recursively, the resulted polys will be used in
		 * the Jacobian matrix in jacobiDet().
		 * 
		 * Used internally.
		 */
		JACOBI_PREPARE("jacobiPrepare", "jacobiPrepare(polys,excludevars):=begin local ii, degrees, pos, vars, linvar; vars:=lvar(polys); ii:=0; while (ii<size(polys)-1) do degrees:=degree(polys[ii],vars); if (sum(degrees)=1) begin pos:=find(1,degrees); linvar:=vars[pos[0]]; if (!is_element(linvar,excludevars)) begin substval:=op(solve(polys[ii]=0,linvar)[0])[1]; polys:=remove(0,expand(subs(polys,[linvar],[substval]))); /* print(polys); */ ii:=-1; end; end; ii:=ii+1; od; return polys; end"),
		/**
		 * Compute the Jacobian determinant of the polys with respect to
		 * excludevars. Used internally.
		 */
		JACOBI_DET("jacobiDet", "jacobiDet(polys,excludevars):=begin local J, ii, vars, s, j, k; vars:=lvar(polys); for ii from 0 to size(excludevars)-1 do vars:=remove(excludevars[ii], vars); od; s:=size(vars); J:=matrix(s,s,(j,k)->diff(polys[j],vars[k])); return det_minor(J); end"),
		/**
		 * Compute the Jacobian determinant of the polys with respect to
		 * excludevars, but first some geometrical preparations are performed to
		 * simplify the result. Used internally.
		 */
		GEOM_JACOBI_DET("geomJacobiDet",
				"geomJacobiDet(polys,excludevars):=begin local J; J:=jacobiPrepare(polys,excludevars); return jacobiDet(J,excludevars); end"),
		/**
		 * Compute the coefficients of the envelope equation for the input
		 * polys, elimvars with given precision for the curve variables x and y.
		 * Used publicly.
		 */
		ENVELOPE_EQU("envelopeEqu",
				"envelopeEqu(polys,elimvars,precision,curvevarx,curvevary):=begin local D; D:=geomJacobiDet(polys,[curvevarx,curvevary]); polys:=append(polys,D); return locusEqu(polys,elimvars,precision,curvevarx,curvevary); end"),
		/**
		 * Compute the coefficients of the locus equation for the input polys,
		 * elimvars with given precision for the curve variables x and y. Used
		 * publicly.
		 */
		LOCUS_EQU("locusEqu", "locusEqu(polys,elimvars,precision,curvevarx,curvevary):=implicitCurveCoeffs(subst(geomElim(polys,elimvars,precision),[curvevarx=x,curvevary=y]))"),
		/**
		 * Compute coefficient matrix of the input polynomial. The output is a
		 * flattened variant of the matrix: the elements are returned row by
		 * row, starting with the sizes of the matrix: height and width. Used
		 * internally.
		 */
		COEFF_MATRIX("coeffMatrix", "coeffMatrix(aa):=begin local bb, sx, sy, ii, jj, ee, cc, kk; bb:=coeffs(aa,x); sx:=size(bb); sy:=size(coeffs(aa,y)); cc:=[sx,sy]; for ii from sx-1 to 0 by -1 do dd:=coeff(bb[ii],y); sd:=size(dd); for jj from sd-1 to 0 by -1 do ee:=dd[jj]; cc:=append(cc,ee); od; for kk from sd to sy-1 do ee:=0; cc:=append(cc,ee); od; od; return cc; end"),
		/**
		 * Compute the coefficient matrices for the factors of the input
		 * polynomial. The first number in the flattened output is the number of
		 * the coefficient matrices, then each coefficient matrix is added. Used
		 * internally.
		 */
		COEFF_MATRICES("coeffMatrices", "coeffMatrices(aa):=begin local ff, bb, ccf, ll, aaf; ff:=factors(aa); ccf:=[size(ff)/2]; for ll from 0 to size(ff)-1 by 2 do aaf:=ff[ll]; bb:=coeffMatrix(aaf); ccf:=append(ccf,bb); od; return flatten(ccf); end"),
		/**
		 * Compute the flattened coefficient matrix as it is directly used when
		 * the algebraic curve is plotted as an implicit poly. Used publicly.
		 */
		IMPLICIT_CURVE_COEFFS("implicitCurveCoeffs", "implicitCurveCoeffs(aa):=begin local bb; bb:=factorsqrfree(aa); return [coeffMatrix(bb),coeffMatrices(bb)]; end");
		/** function name */
		final public String functionName;
		/** definition string */
		final public String definitionString;

		CustomFunctions(String functionName, String definitionString) {
			this.functionName = functionName;
			this.definitionString = definitionString;
		}

		@Override
		public String toString() {
			return functionName;
		}

		private static List<Entry<CustomFunctions, CustomFunctions>> CustomFunctionsDependencies;

		private static void setDependency(CustomFunctions cf1,
				CustomFunctions cf2) {
			Entry<CustomFunctions, CustomFunctions> pair = new SimpleEntry<CustomFunctions, CustomFunctions>(
					cf1, cf2);
			CustomFunctionsDependencies.add(pair);
		}

		/**
		 * Create dependencies between two CAS custom functions. This is
		 * required to ensure that all dependencies will be loaded when a custom
		 * function is loaded.
		 */
		public static void setDependencies() {
			CustomFunctionsDependencies = new ArrayList<Entry<CustomFunctions, CustomFunctions>>();
			setDependency(IMPLICIT_CURVE_COEFFS, COEFF_MATRIX);
			setDependency(IMPLICIT_CURVE_COEFFS, COEFF_MATRICES);
			setDependency(IMPLICIT_CURVE_COEFFS, FACTOR_SQR_FREE);
			setDependency(GEOM_ELIM, PRIM_POLY);
			setDependency(LOCUS_EQU, IMPLICIT_CURVE_COEFFS);
			setDependency(LOCUS_EQU, GEOM_ELIM);
			setDependency(ENVELOPE_EQU, LOCUS_EQU);
			setDependency(ENVELOPE_EQU, GEOM_JACOBI_DET);
			setDependency(GEOM_JACOBI_DET, JACOBI_PREPARE);
			setDependency(GEOM_JACOBI_DET, JACOBI_DET);
		}

		/**
		 * Create the list of prerequisites of a custom command. TODO: Currently
		 * we don't have a complex tree of dependencies. Later we may add a more
		 * sophisticated algorithm here to remove duplicates, be faster etc.
		 * 
		 * @param cf
		 *            the custom command
		 * @return the prerequisites
		 */
		public static ArrayList<CustomFunctions> prereqs(
				CustomFunctions cf) {
			ArrayList<CustomFunctions> list = new ArrayList<CustomFunctions>();
			for (Entry<CustomFunctions, CustomFunctions> pair : CustomFunctionsDependencies) {
				CustomFunctions key = pair.getKey();
				CustomFunctions value = pair.getValue();
				if (key.equals(cf)) {
					list.add(0, value);
					list.addAll(0, prereqs(value));
				}
			}
			return list;
		}
	}

	/** CAS parser */
	public CASparser casParser;

	private static int nrOfReplacedConst = 0;

	/**
	 * Creates new Giac CAS
	 * 
	 * @param casParser
	 *            parser
	 */
	public CASgiac(CASparser casParser) {
		this.casParser = casParser;
	}

	/**
	 * @param exp
	 *            Giac command
	 * @return value returned from CAS
	 */
	@Override
	public abstract String evaluateCAS(String exp);

	@Override
	final public String evaluateRaw(final String input) throws Throwable {

		String exp = input;

		String result = evaluate(exp, getTimeoutMilliseconds());

		// FIXME: This check is too heuristic: in giac.js we can get results
		// starting with \"
		// and they are still correct (e.g. from eliminateFactorized).
		// TODO: Find a better way for checking, now we assume that \"[ start is
		// OK (or \"\").

		Log.debug("input = " + input);

		String rtrimmed = result.trim();
		if (rtrimmed.startsWith("\"") && rtrimmed.endsWith("\"")) {
				result = result.substring(1, result.length() - 1); // removing
		}

		Log.debug("result = " + result);

		return result;
	}

	/**
	 * @param exp
	 *            expression string
	 * @param timeoutMilliseconds
	 *            timeout in milliseconds
	 * @return result in Giac syntax
	 * @throws Throwable
	 *             for CAS error
	 */
	protected abstract String evaluate(String exp, long timeoutMilliseconds)
			throws Throwable;

	@Override
	final public synchronized String evaluateGeoGebraCAS(
			final ValidExpression inputExpression, MyArbitraryConstant arbconst,
			StringTemplate tpl, GeoCasCell cell, Kernel kernel)
			throws CASException {

		ValidExpression casInput = inputExpression;
		Command cmd = casInput.getTopLevelCommand();
		boolean keepInput = (cell != null && cell.isKeepInputUsed())
				|| (cmd != null && "KeepInput".equals(cmd.getName()));
		String plainResult = getPlainResult(casInput, kernel);

		if (keepInput) {
			// remove KeepInput[] command and take argument
			if (cmd != null && cmd.getName().equals("KeepInput")) {
				// use argument of KeepInput as casInput
				if (cmd.getArgumentNumber() > 0) {
					casInput = cmd.getArgument(0);
				}
			}
		}

		// convert result back into GeoGebra syntax
		if (casInput instanceof FunctionNVar) {
			// delayed function definition f(x)::= Derivative[x^2] should return
			// Derivative[x^2]
			if (cell != null
					&& cell.getAssignmentType() == AssignmentType.DELAYED) {
				return casInput.toString(StringTemplate.numericNoLocal);
			}
			// function definition f(x) := x^2 should return x^2
			// f(x):=Derivative[x^2] should return 2x
			// do not return directly, must check keepinput
			/*
			 * plainResult = evaluateMPReduce(plainResult + "(" +
			 * ((FunctionNVar)
			 * casInput).getVarString(StringTemplate.casTemplate) + ")");
			 */
		}

		String result = plainResult;

		if (keepInput && (cell == null || !cell.isSubstitute())) {
			// assume keepinput was not treated in CAS
			return casParser.toGeoGebraString(casInput, tpl);
		}

		if (keepInput && (cell != null && cell.isSubstitute())) {
			// assume keepinput was not treated in CAS
			ExpressionValue substList = casInput.getTopLevelCommand()
					.getArgument(1).unwrap();
			ExpressionValue substArg = casInput.getTopLevelCommand()
					.getArgument(0);
			if (substList instanceof MyList) {
				for (int i = 0; i < ((MyList) substList).size(); i++) {
					substArg = subst(substArg, ((MyList) substList).getItem(i),
							cell.getKernel());
				}
			} else {
				substArg = subst(substArg, substList, cell.getKernel());
			}
			if (substArg != null) {
				return casParser.toGeoGebraString(substArg, tpl);
			}
		}

		// standard case
		if (result == null || result.isEmpty()) {
			return null;
		}
		return toGeoGebraString(result, arbconst, tpl, kernel);

	}

	private static ExpressionValue subst(ExpressionValue substArg,
			ExpressionValue item0, Kernel kernel) {
		ExpressionValue item = item0.unwrap();
		if (item instanceof Equation) {
			ExpressionValue lhs = ((Equation) item).getLHS().unwrap();
			if (lhs instanceof GeoDummyVariable || lhs instanceof Variable) {
				ExpressionValue rhs = ((Equation) item).getRHS();
				ExpressionValue copy = substArg.deepCopy(kernel);
				copy.traverse(VariableReplacer.getReplacer(
						lhs.toString(StringTemplate.defaultTemplate), rhs,
						kernel));
				return copy;
			}
		}
		return null;
	}

	@Override
	final public synchronized ExpressionValue evaluateToExpression(
			final ValidExpression inputExpression, MyArbitraryConstant arbconst,
			Kernel kernel) throws CASException {
		String result = getPlainResult(inputExpression, kernel);
		// standard case
		if ("".equals(result)) {
			return null;
		}
		return replaceRoots(casParser.parseGiac(result), arbconst, kernel);

	}

	private String getPlainResult(ValidExpression casInput, Kernel kernel) {
		// KeepInput[] command should set flag keepinput!!:=1
		// so that commands like Substitute can work accordingly
		Command cmd = casInput.getTopLevelCommand();

		if (cmd != null && "Delete".equals(cmd.getName())) {
			ExpressionValue toDelete = cmd.getArgument(0).unwrap();
			if (toDelete.isExpressionNode() && (((ExpressionNode) toDelete)
					.getOperation() == Operation.FUNCTION
					|| ((ExpressionNode) toDelete)
							.getOperation() == Operation.FUNCTION_NVAR)) {
				toDelete = ((ExpressionNode) toDelete).getLeft();
			}
			String label = toDelete.toString(StringTemplate.defaultTemplate);
			GeoElement geo = kernel.lookupLabel(label);
			if (geo == null) {
				geo = kernel.lookupCasCellLabel(label);
			}
			if (geo != null) {
				geo.remove();
			}
			return "true";
		}

		// convert parsed input to Giac string
		String giacInput = casParser.translateToCAS(casInput,
				StringTemplate.giacTemplate, this);

		// evaluate in Giac
		String plainResult = evaluateCAS(giacInput);
		// get initial nr of vars
		int nrOfVars = casParser.getNrOfVars();
		StringBuilder newPlainResult = new StringBuilder();
		// case we need to process the result
		if (nrOfVars > 0) {
			// get array of potential results
			String[] partsOfResult = plainResult.split("},");
			for (int i = 0; i < partsOfResult.length; i++) {
				// get array of solutions
				String[] partsOfCurrSol = partsOfResult[i].split(",");
				// append only asked solutions
				for (int j = 0; j < nrOfVars; j++) {
					if (j == nrOfVars - 1) {
						newPlainResult.append(partsOfCurrSol[j]);
						newPlainResult.append("},");
					} else {
						newPlainResult.append(partsOfCurrSol[j]);
						newPlainResult.append(",");
					}
				}
			}
			newPlainResult.setLength(newPlainResult.length() - 1);
			newPlainResult.append("}");
			// reset nrOfVars
			casParser.setNrOfVars(0);
			return newPlainResult.toString();
		}
		return plainResult;
	}

	/**
	 * Tries to parse a given Giac string and returns a String in GeoGebra
	 * syntax.
	 * 
	 * @param giacString
	 *            String in Giac syntax
	 * @param arbconst
	 *            arbitrary constant handler
	 * @param tpl
	 *            template that should be used for serialization. Should be
	 *            casCellTemplate for CAS and defaultTemplate for input bar
	 * @param kernel
	 *            kernel
	 * @return String in Geogebra syntax.
	 * @throws CASException
	 *             Throws if the underlying CAS produces an error
	 */
	final public synchronized String toGeoGebraString(String giacString,
			MyArbitraryConstant arbconst, StringTemplate tpl,
			final Kernel kernel)
			throws CASException {
		ExpressionValue ve = replaceRoots(casParser.parseGiac(giacString),
				arbconst, kernel);
		// replace rational exponents by roots or vice versa

		ve = ve.traverse(new Traversing() {

			public ExpressionValue process(ExpressionValue ev) {
				if (ev instanceof MyVecNDNode
						&& ((MyVecNDNode) ev).isCASVector()) {
					return new ExpressionNode(kernel,
							new Variable(kernel, "ggbvect"), Operation.FUNCTION,
							ev);
				}
				return ev;
			}
		});
		return casParser.toGeoGebraString(ve, tpl);

	}

	private static ExpressionValue replaceRoots(ExpressionValue ve0,
			MyArbitraryConstant arbconst, Kernel kernel) {
		ExpressionValue ve = ve0;
		if (ve != null) {
			boolean toRoot = kernel.getApplication().getSettings()
					.getCasSettings().getShowExpAsRoots();
			ve = ve.traverse(DiffReplacer.INSTANCE);
			ve.traverse(PowerRootReplacer.getReplacer(toRoot));
			if (arbconst != null) {
				arbconst.reset();
				ve.traverse(ArbconstReplacer.getReplacer(arbconst));
			}
			PrefixRemover pr = PrefixRemover.getCollector();
			ve.traverse(pr);
		}
		return ve;
	}

	/**
	 * Timeout for CAS in milliseconds. This can be changed in the CAS options.
	 */
	public long timeoutMillis = 5000;

	/**
	 * @return CAS timeout in seconds
	 */
	protected long getTimeoutMilliseconds() {
		return timeoutMillis;
	}

	@Override
	public void settingsChanged(AbstractSettings settings) {
		CASSettings s = (CASSettings) settings;
		timeoutMillis = s.getTimeoutMilliseconds();
	}

	@Override
	public String translateAssignment(final String label, final String body) {
		return label + " := " + body;
	}

	/**
	 * This method is called when asynchronous CAS call is finished. It tells
	 * the calling algo to update itself and adds the result to cache if
	 * suitable.
	 * 
	 * @param exp
	 *            parsed CAS output
	 * @param result2
	 *            output as string (for caching)
	 * @param exception
	 *            exception which stopped the computation (null if there wasn't
	 *            one)
	 * @param c
	 *            command that called the CAS asynchronously
	 * @param input
	 *            input string (for caching)
	 * @param cell
	 *            cas cell
	 */
	public void CASAsyncFinished(ValidExpression exp, String result2,
			Throwable exception, AsynchronousCommand c, String input,
			GeoCasCell cell) {
		String result = result2;
		// pass on exception
		if (exception != null) {
			c.handleException(exception, input.hashCode());
			return;
		}
		// check if keep input command was successful
		// e.g. for KeepInput[Substitute[...]]
		// otherwise return input
		if (cell.isKeepInputUsed() && ("?".equals(result))) {
			// return original input
			c.handleCASoutput(exp.toString(StringTemplate.maxPrecision),
					input.hashCode());
		}

		// success
		if (result2 != null) {
			c.getKernel();
			// get names of escaped global variables right
			// e.g. "ggbcasvar1a" needs to be changed to "a"
			// e.g. "ggbtmpvara" needs to be changed to "a"
			result = Kernel.removeCASVariablePrefix(result, " ");
		}

		c.handleCASoutput(result, input.hashCode());
		if (c.useCacheing()) {
			c.getKernel().putToCasCache(input, result);
		}
	}

	@Override
	public void appendListStart(StringBuilder sbCASCommand) {
		sbCASCommand.append("[");
	}

	@Override
	public void appendListEnd(StringBuilder sbCASCommand) {
		sbCASCommand.append("]");
	}

	@Override
	public String createEliminateFactorizedScript(String polys,
			String elimVars) {
		/*
		 * Some examples to understand the code below:
		 * 
		 * [[aa:=eliminate([-1*v1,-1*v11*v10+v12*v9+v11*v8+-1*v9*v8+-1*v12*v7+
		 * v10 *v7,v13*v8+-1*v14*v7,-1*v13+v13*v10+v9+-1*v14*v9,
		 * -1*v15*v10+v16*v9+v15
		 * *v2+-1*v9*v2+-1*v16*v1+v10*v1,v15*v12+-1*v16*v11,
		 * v17+-1*v17*v12+-1*v11+v18*v11,
		 * -1*v17*v8+v18*v7+v17*v2+-1*v7*v2+-1*v18
		 * *v1+v8*v1,-1+-1*v19*v17*v16+v19
		 * *v18*v15+v19*v17*v14+-1*v19*v15*v14+-1*v19*v18*v13+v19*v16*v13],
		 * [v17,v16,v19,v1,v18,v8,v13,v14,v15])],[bb:=size(aa)],[for ii from 0
		 * to bb-1 do cc[ii]:=factors(aa[ii]); od], cc][3]
		 * 
		 * table( 1 = [v2-1,1,v7,1,v12-1,1], 2 = [v2,1,v9,1,v12,1], 3 =
		 * [v7,1,v10-1,1], 4 = [v12,1,v12-1,1,-1,1,v2,1,v10-1,1,v10-v2,1], 5 =
		 * [-v2+1,1,v7,1,v11,1], 6 = [v2,1,v9,1,v11,1], 7 =
		 * [-v11*v10+v11*v2+v12*v9,1], 0 = [v7,1,v9,1] )
		 * 
		 * But we need the same output as Singular does, so we use this code
		 * instead:
		 * 
		 * [[aa:=eliminate([-1*v1,-1*v11*v10+v12*v9+v11*v8+-1*v9*v8+-1*v12*v7+
		 * v10 *v7,v13*v8+-1*v14*v7,-1*v13+v13*v10+v9+-1*v14*v9,
		 * -1*v15*v10+v16*v9+v15
		 * *v2+-1*v9*v2+-1*v16*v1+v10*v1,v15*v12+-1*v16*v11,
		 * v17+-1*v17*v12+-1*v11+v18*v11,
		 * -1*v17*v8+v18*v7+v17*v2+-1*v7*v2+-1*v18
		 * *v1+v8*v1,-1+-1*v19*v17*v16+v19
		 * *v18*v15+v19*v17*v14+-1*v19*v15*v14+-1*v19*v18*v13+v19*v16*v13],
		 * [v17,v16,v19,v2,v18,v8,v13,v14,v15])],[bb:=size(aa)],[for ii from 0
		 * to bb-1 do print("["+(ii+1)+"]:");print(" [1]:");print("  _[1]=1"
		 * );cc: =factors(aa[ii]);dd:=size(cc); for jj from 0 to dd-1 by 2 do
		 * print("  _["+(jj/2+2)+"]="+(cc[jj])); od; print(" [2]:"); print("  "
		 * +cc[1]);for kk from 1 to dd-1 by 2 do print("   ,"
		 * +cc[kk]);od;od],0][3]
		 * 
		 * which gives
		 * 
		 * [1]: [1]: _[1]=1 _[2]=v7 _[3]=v9 [2]: 1 ,1 ,1 [2]: [1]: _[1]=1
		 * _[2]=v7 _[3]=v10-1 [2]: 1 ,1 ,1 [3]: [1]: _[1]=1 _[2]=v9 _[3]=-1
		 * _[4]=v11*v10-v9*v12 [2]: 1 ,1 ,1 ,1 [4]: [1]: _[1]=1 _[2]=v1 [2]: 1
		 * ,1 0
		 * 
		 * in giac with CoCoA support on command line and runs forever in
		 * giac.js.
		 */
		StringBuilder script = new StringBuilder();

		/*
		 * return script.append("[[aa:=eliminate(["). append(polys).
		 * append("],["). append(elimVars). append(
		 * "])],[bb:=size(aa)],[for ii from 0 to bb-1 do print(\"[\"+(ii+1)+\"]:\");print(\" [1]:\");"
		 * ). append("print(\"  _[1]=1\");cc:=factors(aa[ii]);dd:=size(cc);").
		 * append(
		 * "for jj from 0 to dd-1 by 2 do print(\"  _[\"+(jj/2+2)+\"]=\"+(cc[jj])); od; print(\" [2]:\");"
		 * ). append(
		 * "print(\"  \"+cc[1]);for kk from 1 to dd-1 by 2 do print(\"   ,\"+cc[kk]);od;od],0][3]"
		 * )
		 * 
		 * .toString();
		 */

		String eliminateCommand = CustomFunctions.ELIMINATE2 + "([" + polys
				+ "],revlist(["
				+ elimVars + "]))";

		return script.append("[" + "[ff:=\"\"],[aa:=").append(eliminateCommand)
				.append("],")
				.append("[bb:=size(aa)],[for ii from 0 to bb-1 do ff+=(\"[\"+(ii+1)+\"]: [1]: ")
				.append(" _[1]=1\");cc:=factors(aa[ii]);dd:=size(cc);")
				.append("for jj from 0 to dd-1 by 2 do ff+=(\"  _[\"+(jj/2+2)+\"]=\"+cc[jj]); od; ff+=(\" [2]: ")
				.append("\"+cc[1]);for kk from 1 to dd-1 by 2 do ff+=(\",\"+cc[kk]);od;od],[if(ff==\"\") begin ff:=[0] end],ff][5]")

				.toString();

		// We return text from the CAS here.

	}


	/**
	 * Create a script which eliminates variables from a set of polynomials.
	 * 
	 * @param polys
	 *            the input polynomials
	 * @param elimVars
	 *            the variables to be eliminated
	 * @param oneCurve
	 *            if the output consists of more polynomials, consider the
	 *            intersections of them as points with real coordinates and
	 *            convert them to a single product
	 * @return the Giac program which creates the output ideal
	 */
	@Override
	public String createEliminateScript(String polys, String elimVars,
			boolean oneCurve, Long precision) {
		if (!oneCurve) {
			return CustomFunctions.PRIM_POLY + "(" + CustomFunctions.ELIMINATE2
					+ "([" + polys
					+ "],revlist([" + elimVars + "])))";
		}

		String PRECISION = Long.toString(precision);
		Log.debug("PRECISION = " + PRECISION);
		String retval;
		/*
		 * Exact approach. This will not work if there are irrationals since
		 * sqrt(...) cannot be directly converted to a number.
		 */
		/*
		 * retval = "primpoly([[ee:=eliminate([" + polys + "],revlist([" +
		 * elimVars +
		 * "]))],[ll:=lvar(ee)],[if(size(ee)>1) begin ff:=solve(ee,ll);" +
		 * "gg:=1;for ii from 0 to size(ff)-1 do gg:=gg*right(((ll[0]-ff[ii,0])^2+(ll[1]-ff[ii,1])^2)-"
		 * + FAKE_NULL + ");" +
		 * "od ee:=[lcm(denom(coeff(gg)))*gg]; end],ee][3])";
		 */
		/*
		 * Rounded approach. This works in general, but we should check how
		 * fsolve is implemented. The best would be to use symbolical
		 * computation during solve(...) and then do the numerical
		 * approximation. TODO: Check how giac implements fsolve and use a
		 * different method if needed (and available).
		 */
		retval = CustomFunctions.PRIM_POLY + "([[ee:="
				+ CustomFunctions.ELIMINATE2 + "([" + polys
				+ "],revlist([" + elimVars
				+ "]))],[ll:=lvar(ee)],[if(size(ee)>1) begin ff:=round(fsolve(ee,ll)*"
				+ PRECISION + ")/" + PRECISION + ";"
				+ "gg:=1;for ii from 0 to size(ff)-1 do gg:=gg*(((ll[0]-ff[ii,0])^2+(ll[1]-ff[ii,1])^2));"
				+ "od ee:=[expand(lcm(denom(coeff(gg)))*gg)]; end],ee][3])";
		return retval;
	}

	@Override
	public String createGroebnerSolvableScript(
			HashMap<PVariable, BigInteger> substitutions, String polys,
			String freeVars, String dependantVars, boolean transcext) {
		/*
		 * Example syntax (from Groebner basis tester; but in GeoGebra v1, v2,
		 * ... are used for variables):
		 * 
		 * [[ii:=gbasis(subst([2*d1-b1-c1, 2*d2-b2-c2,2*e1-a1-c1,
		 * 2*e2-a2-c2,2*f1-a1-b1, 2*f2-a2-b2 ,
		 * (d1-o1)*(b1-c1)+(d2-o2)*(b2-c2),(e1-o1)*(c1-a1)+(e2-o2)*(c2-a2),
		 * s1*d2
		 * +a1*(s2-d2)-d1*s2-a2*(s1-d1),s1*e2+b1*(s2-e2)-e1*s2-b2*(s1-e1),(a1
		 * -m1)*(b1-c1)+(a2-m2)*(b2-c2),
		 * (b1-m1)*(c1-a1)+(b2-m2)*(c2-a2),z1*(b1*c2
		 * +a1*(b2-c2)-c1*b2-a2*(b1-c1))-1, z2
		 * *(s1*m2+o1*(s2-m2)-m1*s2-o2*(s1-m1
		 * ))-1],[d1=0,b1=3]),[a1,a2,b1,b2,c1,c2,d1,d2,e1,e2,f1,f2,o1,
		 * o2,s1,s2,m1,m2,z1,z2],revlex)],(degree(ii[0])!=0)||(ii[0]==0)][1]
		 * 
		 * In the last part we check if the Groebner basis is a constant neq 0,
		 * i.e. its degree is 0 but it is not 0. If yes, there is no solution.
		 * 
		 * The giac implementation does not handle the case for request for
		 * polynomial ring with coefficients from a transcendental extension. We
		 * silently use a polynomial ring instead.
		 */

		String idealVar = "ii";

		String ret = "[[" + idealVar + ":=gbasis(";

		if (substitutions != null) {
			ret += "subst(";
		}

		ret += "[" + polys + "]";

		if (substitutions != null) {
			String substParams = substitutionsString(substitutions);
			ret += ",[" + substParams + "])";
		}

		String vars = freeVars + PPolynomial.addLeadingComma(dependantVars);

		// ret += ",[" + vars + "],revlex)],(degree(" +
		// idealVar + "[0])!=0)||(" + idealVar + "[0]==0)][2]";
		ret += ",[" + vars + "],revlex)],(" + idealVar + "[0]!=1)&&(" + idealVar
				+ "[0]!=-1)][1]";

		return ret;
	}

	/**
	 * Converts substitutions to giac strings
	 * 
	 * @param substitutions
	 *            input as a HashMap
	 * @return the parameters for giac (e.g. "v1=0,v2=0,v3=0,v4=1")
	 * 
	 *         Taken from prover.Polynomial, one character difference. Maybe
	 *         commonize.
	 */
	static String substitutionsString(
			HashMap<PVariable, BigInteger> substitutions) {
		StringBuilder ret = new StringBuilder();
		Iterator<Entry<PVariable, BigInteger>> it = substitutions.entrySet()
				.iterator();
		while (it.hasNext()) {
			Entry<PVariable, BigInteger> v = it.next();
			ret.append(",");
			ret.append(v.getKey().toString());
			ret.append("=");
			ret.append(v.getValue());
		}
		if (ret.length() > 0) {
			return ret.substring(1);
		}
		return "";
	}

	/**
	 * Combine non-factorized and factorized results as a 3 dimensional array.
	 * 
	 * The input is like this example:
	 * {{6,5,-184,304,-160,28,-2,52,-136,96,-14,1,-2,-12,-12,0,0,27,2,2,0,0,-10,
	 * 0,0,0,0,1,0,0,0,0},{2,2,1,-2,1,5,5,92,-152,80,-14,1,20,-8,-8,0,0,11,2,2,0
	 * ,0,-8,0,0,0,0,1,0,0,0,0}} describes a Steiner deltoid
	 * (http://tube.geogebra.org/m/xWCP09dk) by its expanded polynomial
	 * x^5+x*y^4+2*x^3*y^2-10*x^4-2*y^4-14*x*y^3-12*x^2*y^2+2*x^3*y+27*x^3+28*y^
	 * 3+96*x*y^2-12*x^2*y-2*x^2-160*y^2-136*x*y+52*x+304*y-184 which is the
	 * first list {6,5,...} and its two factors: x-2=0 and
	 * y^4-14*y^3+2*y^2*x^2-8*y^2*x+80*y^2+2*y*x^2-8*y*x-152*y+x^4-8*x^3+11*x^2+
	 * 20*x+92=0. In the second list {2,2,...} the first element tells the
	 * number of factors (2), then the coefficients are listed (2,1,-2,1 for
	 * x-2=0, here the first two elements describe the size of the coefficient
	 * matrix) and the rest for the other one.
	 * 
	 * In the output the 0. element in the 1. dimension contains the
	 * non-factorized values, the next elements contain the factorized ones.
	 * 
	 * @param rawResult
	 *            input string of coefficients of a polynomial and its factors
	 *            in a custom format
	 * @return the output array of coefficients in GeoImplicitCurve's format
	 */
	@Override
	public double[][][] getBivarPolyCoefficientsAll(String rawResult) {
		double[][] coeff = getBivarPolyCoefficients(rawResult);
		double[][][] coeffSquarefree = getBivarPolySquarefreeCoefficients(
				rawResult);
		double[][][] retval = new double[coeffSquarefree.length + 1][][];
		retval[0] = coeff;
		for (int i = 0; i < coeffSquarefree.length; ++i) {
			retval[i + 1] = coeffSquarefree[i];
		}
		return retval;
	}

	private static double[][][] getBivarPolySquarefreeCoefficients(
			String rawResult) {

		int firstClosingBracket = rawResult.indexOf('}');
		String numbers = rawResult.substring(firstClosingBracket + 3,
				rawResult.length() - 2);
		String[] flatData = numbers.split(",");
		int factors = Integer.parseInt(flatData[0]);
		double[][][] result = new double[factors][][];
		int counter = 1;

		for (int factor = 0; factor < factors; ++factor) {

			int xLength = Integer.parseInt(flatData[counter++]);
			int yLength = Integer.parseInt(flatData[counter++]);
			result[factor] = new double[xLength][yLength];

			for (int x = 0; x < xLength; x++) {
				for (int y = 0; y < yLength; y++) {
					result[factor][x][y] = Double
							.parseDouble(flatData[counter]);
					Log.trace("[LocusEqu] result[" + factor + "][" + x + "," + y
							+ "]=" + result[factor][x][y]);
					++counter;
				}
			}
		}

		return result;

	}

	private static double[][] getBivarPolyCoefficients(String rawResult) {
		int firstClosingBracket = rawResult.indexOf('}');
		String numbers = rawResult.substring(2, firstClosingBracket);
		String[] flatData = numbers.split(",");
		int xLength = Integer.parseInt(flatData[0]);
		int yLength = Integer.parseInt(flatData[1]);
		double[][] result = new double[xLength][yLength];

		int counter = 2;
		for (int x = 0; x < xLength; x++) {
			for (int y = 0; y < yLength; y++) {
				result[x][y] = Double.parseDouble(flatData[counter]);
				Log.trace("[LocusEqu] result[" + x + "," + y + "]="
						+ result[x][y]);
				++counter;
			}
		}

		return result;
	}

	// eg {(ggbtmpvarx>(-sqrt(110)/5)) && ((sqrt(110)/5)>ggbtmpvarx)}
	// eg {(ggbtmpvarx>=(-sqrt(110)/5)) && ((sqrt(110)/5)>=ggbtmpvarx)}
	// eg (ggbtmpvarx>3) && (4>ggbtmpvarx)
	// private final static RegExp inequality =
	// RegExp.compile("(.*)\\((ggbtmpvar[^,}\\(\\)]+)>(=*)(.+)\\) &&
	// \\((.+)>(=*)(ggbtmpvar[^,}\\(\\)]+)\\)(.*)");
	// works only for variables in form [A-Za-z]+
	/** expression with at most 3 levels of brackets */
	public final static String expression = "(([^\\(\\)]|\\([^\\(\\)]+\\)|\\(([^\\(\\)]|\\([^\\(\\)]+\\))+\\))+)";
	/**
	 * inequality a >=? ex1 && ex1 >=? b where a,b are literals and ex1, ex2 are
	 * expressions with at most 3 brackets
	 */
	public final static RegExp inequality = RegExp
			.compile("^(.*)\\(([A-Za-z]+)>(=*)" + expression + "\\) && \\("
					+ expression + ">(=*)([A-Za-z]+)\\)(.*)$", "");

	// eg 3.7 > ggbtmpvarx
	// eg (37/10) > ggbtmpvarx
	// eg 333 > ggbtmpvarx
	// eg (-33) > ggbtmpvarx
	// private final static RegExp inequalitySimple =
	// RegExp.compile("([-0-9.E/\\(\\)]+)>(=*)(ggbtmpvar.+)");
	// works only for variables in form [A-Za-z]+
	private final static RegExp inequalitySimple = RegExp
			.compile("^([-0-9.E/\\(\\)]+)>(=*)([A-Za-z]+)$");

	// eg {3, 3>ggbtmpvarx, x^2}
	// eg {3, 3>ggbtmpvarx}
	// eg {3>ggbtmpvarx, x^2}
	// eg {3>ggbtmpvarx}
	// works only for variables in form [A-Za-z]+ and if it's a simple number
	private final static RegExp inequalitySimpleInList = RegExp.compile(
			"(.*)([,{])(\\(*)?([-0-9.E]+)(\\)*)?>(=*)([A-Za-z]+)([,}\\)])(.*)");

	// old version, causes problems with eg Solve[exp(x)<2]
	// private final static RegExp inequalitySimpleInList =
	// RegExp.compile("(.*)([,{\\(])([-0-9.E/\\(\\)]+)>(=*)([A-Za-z]+)([,}\\)])(.*)");

	/**
	 * convert x>3 && x<7 into 3<x<7 convert 3>x into x<3 convert {3>x} into
	 * {x<3} eg output from Solve[x (x-1)(x-2)(x-3)(x-4)(x-5) < 0]
	 * 
	 * Giac's normal command converts inequalities to > or >= so we don't need
	 * to check <, <=
	 * 
	 * @param exp
	 *            expression
	 * @return converted expression if changed
	 */
	protected String checkInequalityInterval(String exp) {

		String ret = exp;

		MatchResult matcher = inequalitySimple.exec(ret);

		// swap 3>x into x<3
		if (matcher != null && exp.startsWith(matcher.getGroup(1))) {
			// Log.debug(matcher.getGroup(1));
			// Log.debug(matcher.getGroup(2));
			// Log.debug(matcher.getGroup(3));
			// Log.debug(matcher.getGroup(4));
			ret = matcher.getGroup(3) + "<" + matcher.getGroup(2)
					+ matcher.getGroup(1);
			Log.debug("giac output (with simple inequality converted): " + ret);
			return ret;
		}

		// swap 5 > x && x > 3 into 3<x<5
		while ((matcher = inequality.exec(ret)) != null &&
		// TODO: check not x<3 && x<4

		// check variable the same
		// ie not x>5 && y<4
				matcher.getGroup(2).equals(matcher.getGroup(11))) {

			ret = matcher.getGroup(1) + matcher.getGroup(4) + "<"
					+ matcher.getGroup(3) + matcher.getGroup(2) + "<"
					+ matcher.getGroup(10) + matcher.getGroup(7)
					+ matcher.getGroup(12);
		}

		// swap {3>x, 6>y} into {x<3, y<6}
		while ((matcher = inequalitySimpleInList.exec(ret)) != null) {

			// matcher.getGroup(6) is either "" or "="

			// Log.debug("1 "+matcher.getGroup(1));
			// Log.debug("2 "+matcher.getGroup(2));
			// Log.debug("3XX"+matcher.getGroup(3)+"XX");
			// Log.debug(""+matcher.getGroup(3).equals("undefined"));
			// Log.debug("4 "+matcher.getGroup(4));
			// Log.debug("5XX"+matcher.getGroup(5)+"XX");
			// Log.debug("6 "+matcher.getGroup(6));
			// Log.debug("7 "+matcher.getGroup(7));
			// Log.debug("8 "+matcher.getGroup(8));
			// Log.debug("9 "+matcher.getGroup(9));

			String g3 = matcher.getGroup(3);
			String g5 = matcher.getGroup(5);

			// GWT regex bug? eg Solve[(2exp(x)-4)/(exp(x)-1) > 1],
			// Solve[(x^2-x-2)/(-x^3+7x^2-14x+8)<2]
			// #4710 GWT 2.7 beta 1 & RC1 needs the +"" as well
			if ((g3 + "").equals("undefined")) {
				g3 = "";
			}
			if ((g5 + "").equals("undefined")) {
				g5 = "";
			}

			// Log.debug("g3= "+g3);
			// Log.debug("g5= "+g5);

			// eg "(" + "-2" + ")"
			String g345 = g3 + matcher.getGroup(4) + g5;
			String g7 = matcher.getGroup(7);

			ret = matcher.getGroup(1) + matcher.getGroup(2) + g7 + "<"
					+ matcher.getGroup(6) + g345 + matcher.getGroup(8)
					+ matcher.getGroup(9);
		}

		if (!exp.equals(ret)) {
			Log.debug("giac output (with inequality converted): " + ret);
		}

		return ret;

	}

	/**
	 * various improvements and hack for Giac's output
	 * 
	 * @param s
	 *            output from Giac
	 * @return result that GeoGebra can parse
	 */
	protected String postProcess(String s) {

		// #5099 / TRAC-3566
		// catch "dotvecteur Error: Invalid dimension"
		// and "GIAC_ERROR:"
		// (small bug in giac.js, not all errors have GIAC_ERROR:
		if (StringUtil.toLowerCase(s).indexOf("error:") > -1) {
			// GIAC_ERROR: canonical_form(3*ggbtmpvarx^4+ggbtmpvarx^2) Error:
			// Bad Argument Value
			Log.debug("error from Giac: " + s);

			return "?";
		}

		String ret = s.trim();
		// output from ifactor can be wrapped to stop simplification
		// eg js giac output:-('3*5')

		int primeOpen = ret.indexOf('\'');
		while (primeOpen >= 0) {
			int primeClose = ret.indexOf('\'', primeOpen + 1);
			if (primeClose < 0) {
				break;
			}
			// ((a')') -- delete brackets
			if (primeClose == primeOpen + 2
					&& ret.charAt(primeOpen + 1) == ')') {
				int bracketOpen = ret.lastIndexOf('(', primeOpen);

				if (bracketOpen >= 0) {
					StringBuilder sb = new StringBuilder(ret);
					sb = sb.replace(primeOpen + 1, primeOpen + 2, "");
					sb = sb.replace(bracketOpen, bracketOpen + 1, "");
					ret = sb.toString();
				}
				// primeOpen = primeClose;
			} else {
				int check = StringUtil.checkBracketsBackward(
						ret.substring(primeOpen, primeClose));
				// -('3*5') will have check = -1
				if (check < 0) {
					StringBuilder sb = new StringBuilder(ret);
					sb = sb.replace(primeOpen, primeOpen + 1, "");
					sb = sb.replace(primeClose - 1, primeClose, "");
					ret = sb.toString();
					primeOpen = ret.indexOf('\'', primeClose);
				} else {
					primeOpen = primeClose;
				}
			}
		}

		// #5099 / TRAC-3566 GIAC_ERROR: string missing
		// if (ret.indexOf("Unable to solve differential equation") > 0) {
		// return "?";
		// }

		if (ret.indexOf("integrate(") > -1) {
			// eg Integral[sqrt(sin(x))]
			return "?";
		}

		if (ret.indexOf("c_") > -1) {
			nrOfReplacedConst += ret.length() / 3; // upper bound on number of
													// constants in result
			Log.debug("replacing arbitrary constants in " + ret);
			ret = ret.replaceAll("c_([0-9]*)",
					"arbconst($1+" + nrOfReplacedConst + ")");
		}

		if (ret.indexOf("n_") > -1) {
			Log.debug("replacing arbitrary integers in " + ret);
			ret = ret.replaceAll("n_([0-9]*)", "arbint($1)");
		}

		// convert Giac's scientific notation from e.g. 3.24e-4 to
		// 3.2E-4
		// not needed, Giac now outputs E
		// ret = parserTools.convertScientificFloatNotation(ret);

		ret = casParser.insertSpecialChars(ret); // undo special character
													// handling

		// don't do check for long strings eg 7^99999
		if (ret.length() < 200) {

			// convert x>3 && x<7 into 3<x<7
			ret = checkInequalityInterval(ret);
		}

		return ret;
	}

	final private static String EVALFA = "evalfa(";
	private StringBuilder expSB = new StringBuilder(EVALFA);

	/**
	 * evalfa makes sure rootof() converted to decimal
	 * eg @rootof({{-4,10,-440,2025},{1,0,10,-200,375}})
	 * 
	 * @param s
	 *            input
	 * @return "evalfa(" + s + ")"
	 */
	protected String wrapInevalfa(String s) {
		expSB.setLength(EVALFA.length());
		expSB.append(s);
		expSB.append(")");

		return expSB.toString();
	}

	/**
	 * Test if Giac is up and running. Overridden in CASGiacW
	 * 
	 * @return true if Giac is already loaded
	 */
	@Override
	public boolean isLoaded() {
		return true;
	}

}
