package opennlp.ccg.parse;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The implementation of a sparse chart as a hash map of a hash map. This implementation has cost
 * O(1) for set and get operations.
 * 
 * This chart is more efficient in space and less efficient in time than a dense chart. It is to be
 * preferred in cases where a large text is to be analyzed.
 * 
 * @author Daniel Couto-Vale
 */
public class SparseChart implements Chart, Serializable {

	/**
	 * Generated serial version
	 */
	private static final long serialVersionUID = -55772431506718482L;

	/**
	 * The data structure.
	 */
	private final Map<Integer, Map<Integer, Cell>> formMapMap;

	/**
	 * The size of the chart.
	 */
	private final int size;

	/**
	 * Constructor
	 * 
	 * @param size the size of the table
	 */
	public SparseChart(int size) {
		this.size = size;
		this.formMapMap = new HashMap<Integer, Map<Integer, Cell>>();
	}

	/**
	 * Constructor
	 * 
	 * @param chartFile the chart file
	 * @throws IOException when there is a problem with reading the file
	 * @throws FileNotFoundException when the file is not found
	 */
	@SuppressWarnings("unchecked")
	public SparseChart(File chartFile) throws FileNotFoundException , IOException {
		FileInputStream fis = new FileInputStream(chartFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ObjectInputStream ois = new ObjectInputStream(bis);
		try {
			this.formMapMap = (HashMap<Integer, Map<Integer, Cell>>) ois.readObject();
			int maxIndex = 0;
			for (Integer index : formMapMap.keySet()) {
				if (index > maxIndex) {
					maxIndex = index;
				}
			}
			this.size = maxIndex + 1;
		} catch (ClassNotFoundException e) {
			throw (RuntimeException) new RuntimeException().initCause(e);
		} finally {
			ois.close();
			bis.close();
			fis.close();
		}
	}

	@Override
	public final Cell getForm(int end, int hops) {
		if (end - hops < 0 || end >= size) {
			throw new ArrayIndexOutOfBoundsException();
		}
		Map<Integer, Cell> formMap = formMapMap.get(end);
		if (formMap == null) {
			return null;
		}
		return formMap.get(hops);
	}

	@Override
	public final void setForm(int end, int hops, Cell form) {
		if (end - hops < 0 || end >= size) {
			throw new ArrayIndexOutOfBoundsException();
		}
		Map<Integer, Cell> formMap = formMapMap.get(end);
		if (formMap == null) {
			formMap = new HashMap<Integer, Cell>();
			formMapMap.put(end, formMap);
		}
		formMap.put(hops, form);
	}

	@Override
	public final int getSize() {
		return size;
	}

}
