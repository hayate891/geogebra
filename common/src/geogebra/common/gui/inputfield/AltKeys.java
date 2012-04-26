package geogebra.common.gui.inputfield;

import geogebra.common.kernel.arithmetic.ExpressionNodeConstants;
import geogebra.common.util.Unicode;

import java.util.HashMap;

public class AltKeys {
	
	public static HashMap<Character, String> LookupLower = null, LookupUpper = null;

	static {
		LookupLower = new HashMap<Character, String>();
		LookupUpper = new HashMap<Character, String>();
		
		LookupLower.put('A', Unicode.alpha);
		LookupUpper.put('A', Unicode.Alpha);
		LookupLower.put('B', Unicode.beta);
		LookupUpper.put('B', Unicode.Beta);
		LookupLower.put('D', Unicode.delta);
		LookupUpper.put('D', Unicode.Delta);
		LookupLower.put('E', Unicode.EULER_STRING);
		LookupUpper.put('E', Unicode.EULER_STRING);
		LookupLower.put('F', Unicode.phi);
		LookupUpper.put('F', Unicode.Phi);
		LookupLower.put('G', Unicode.gamma);
		LookupUpper.put('G', Unicode.Gamma);
		LookupLower.put('I', Unicode.IMAGINARY);
		LookupUpper.put('I', Unicode.IMAGINARY);
		LookupLower.put('L', Unicode.lambda);
		LookupUpper.put('L', Unicode.Lambda);
		LookupLower.put('M', Unicode.mu);
		LookupUpper.put('M', Unicode.Mu);
		LookupLower.put('O', Unicode.degree);
		LookupUpper.put('O', Unicode.degree);
		LookupLower.put('P', Unicode.pi);
		LookupUpper.put('P', Unicode.Pi);
		LookupLower.put('S', Unicode.sigma);
		LookupUpper.put('S', Unicode.Sigma);
		LookupLower.put('T', Unicode.theta);
		LookupUpper.put('T', Unicode.Theta);
		LookupLower.put('U', Unicode.Infinity+"");
		LookupUpper.put('U', Unicode.Infinity+"");
		LookupLower.put('W', Unicode.omega);
		LookupUpper.put('W', Unicode.Omega);
		
		LookupLower.put('0', Unicode.Superscript_0+"");
		LookupLower.put('1', Unicode.Superscript_1+"");
		LookupLower.put('2', Unicode.Superscript_2+"");
		LookupLower.put('3', Unicode.Superscript_3+"");
		LookupLower.put('4', Unicode.Superscript_4+"");
		LookupLower.put('5', Unicode.Superscript_5+"");
		LookupLower.put('6', Unicode.Superscript_6+"");
		LookupLower.put('7', Unicode.Superscript_7+"");
		LookupLower.put('8', Unicode.Superscript_8+"");
		LookupLower.put('9', Unicode.Superscript_9+"");
		
		
		
		LookupUpper.put('*', ExpressionNodeConstants.strVECTORPRODUCT);
		LookupLower.put('*', ExpressionNodeConstants.strVECTORPRODUCT);
		
		LookupUpper.put('+', Unicode.PLUSMINUS);
		LookupLower.put('+', Unicode.PLUSMINUS);
		
		LookupUpper.put(Unicode.eGrave, "{"); // Italian keyboards
		LookupLower.put(Unicode.eGrave, "["); // Italian keyboards
		
		LookupUpper.put(Unicode.eAcute, "{"); // Italian keyboards
		LookupLower.put(Unicode.eAcute, "["); // Italian keyboards
		
		LookupUpper.put('=', Unicode.NOTEQUAL);
		LookupLower.put('=', Unicode.NOTEQUAL);
		
		LookupUpper.put('-', Unicode.Superscript_Minus+"");
		LookupLower.put('-', Unicode.Superscript_Minus+"");
		
		LookupUpper.put(',', Unicode.LESS_EQUAL+"");
		LookupLower.put(',', Unicode.LESS_EQUAL+"");
		
		LookupUpper.put('<', Unicode.LESS_EQUAL+"");
		LookupLower.put('<', Unicode.LESS_EQUAL+"");
		
		LookupUpper.put('.', Unicode.GREATER_EQUAL+"");
		LookupLower.put('.', Unicode.GREATER_EQUAL+"");
		
		LookupUpper.put('>', Unicode.GREATER_EQUAL+"");
		LookupLower.put('>', Unicode.GREATER_EQUAL+"");
		
		LookupUpper.put('+', Unicode.PLUSMINUS);
		LookupLower.put('+', Unicode.PLUSMINUS);
		
		LookupUpper.put('+', Unicode.PLUSMINUS);
		LookupLower.put('+', Unicode.PLUSMINUS);
		
		LookupUpper.put('+', Unicode.PLUSMINUS);
		LookupLower.put('+', Unicode.PLUSMINUS);
		
		LookupUpper.put('+', Unicode.PLUSMINUS);
		LookupLower.put('+', Unicode.PLUSMINUS);
		
		LookupUpper.put('+', Unicode.PLUSMINUS);
		LookupLower.put('+', Unicode.PLUSMINUS);
		
		LookupUpper.put('+', Unicode.PLUSMINUS);
		LookupLower.put('+', Unicode.PLUSMINUS);
		
		LookupUpper.put('+', Unicode.PLUSMINUS);
		LookupLower.put('+', Unicode.PLUSMINUS);
		
		LookupUpper.put('+', Unicode.PLUSMINUS);
		LookupLower.put('+', Unicode.PLUSMINUS);
		
		
		
	}

}
