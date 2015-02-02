package opennlp.ccg.parse;

/**
 * A chart.
 * 
 * WARNING: This is an abstraction of a static data structure. It is not supposed to include methods
 * for applying grammatical rules nor any other kind of ruled construction. 
 * 
 * @author Daniel Couto-Vale
 */
public interface Chart {

	/**
	 * Gets the form starting at the first position and ending at the last position
	 * 
	 * @param first the position of the first atom of the form 
	 * @param last the position of the last atom of the form
	 * @return the forms starting at the first position and ending at the last position
	 */
	Form getForm(int first, int last);

	/**
	 * Sets a form token ending at a end position and with a given hops 
	 * 
	 * @param first the position of the first atom of the form 
	 * @param last the position of the last atom of the form
	 * @param form the form to set
	 */
	void setForm(int first, int last, Form form);

	/**
	 * @return the number of atoms in the chart
	 */
	int size();

}
