package geometry.pe.is;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Basic integer equation
 * 
 * @author Jingjin Yu
 * 
 */
public class Equation {
	private SortedMap<Integer, Integer> coeffMap = new TreeMap<Integer, Integer>();

	private int constant;

	public Equation() {
		super();
	}

	public SortedMap<Integer, Integer> getCoeffMap() {
		return coeffMap;
	}

	public void setCoeffMap(SortedMap<Integer, Integer> coeffMap) {
		this.coeffMap = coeffMap;
	}

	public void addTerm(int unknown, int coeff) {
		coeffMap.put(unknown, coeff);
	}

	public int getConstant() {
		return constant;
	}

	public void setConstant(int constant) {
		this.constant = constant;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		Integer[] nums = coeffMap.keySet().toArray(new Integer[0]);
		Integer[] coeffs = coeffMap.values().toArray(new Integer[0]);
		for (int i = 0; i < coeffs.length; i++) {
			buf.append("" + coeffs[i].intValue() + " x" + (nums[i]));
			if (i != coeffs.length - 1) {
				if( coeffs[i + 1] >= 0 )
					buf.append(" + ");
				else
					buf.append(" ");
			}
		}
		buf.append(" = " + constant);
		return buf.toString();
	}

	public static void main(String[] argv) {
		Equation e = new Equation();
		e.addTerm(3, 5);
		e.addTerm(4, 8);
		e.setConstant(120);
		System.out.println(e.toString());
	}
}
