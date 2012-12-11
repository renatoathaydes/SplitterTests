package splitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class Splitter {
	
	private BlockingDeque<int[]> queue = new LinkedBlockingDeque<int[]>();
	private List<Integer> userChanges = new ArrayList<Integer>();
	Map<Integer, Integer> propagation = new LinkedHashMap<Integer, Integer>();
	
	private List<int[]> doAfterPropagations = new ArrayList<int[]>();
	
	private final int[] vals;
	private final int[] termVals;
	
	public Splitter(int terminals) {
		vals = new int[terminals];
		termVals = new int[terminals];
		
		vals[0] = 100;
		termVals[0] = 100;
		
		Thread t = new Thread() {
			public void run() {
				try {
					
					while (true) {
						int[] item = queue.poll(10, TimeUnit.SECONDS);
						doSet(item[0], item[1]);
					}
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		t.setDaemon(true);
		t.start();
	}
	
	public void setItem(int index, int value) {
		System.out.println("**** Requesting index " + index + " to be " + value + ", history: " + userChanges);
		termVals[index] = value;
		queue.add(new int[] { index, value });
	}
	
	private void doSet(int index, int value) {
		
		if (propagation.containsKey(index) && propagation.get(index) == value) {
			propagation.remove(index);
			System.out.println("This is a propagation, will not go into queue, propagation = " + propagation);
			next();
		} else if (!propagation.isEmpty()) {
			doAfterPropagations.add(new int[] { index, value });
		} else {
			System.out.println("Setting index " + index + " to " + value);
			vals[index] = ensureBounds(value);
			
			List<Integer> forbidden = new ArrayList<Integer>(Arrays.asList(index));
			int valsSum;
			while ((valsSum = sum(vals)) != 100) {
				compensateAfterChange(forbidden, valsSum);
			}
			userChanges.remove( (Integer) index );
			userChanges.add( index );
			
			next();	
		}
		
	}
	
	private void next() {
		if (propagation.isEmpty() && !doAfterPropagations.isEmpty()) {
			System.out.println("%%%%%%%% Doing next thing after propagations");
			int[] todo = doAfterPropagations.remove(0);
			doSet(todo[0], todo[1]);
		}
		if (propagation.isEmpty() && doAfterPropagations.isEmpty()) {
			System.out.println("DONE! termvals = " + Arrays.toString(termVals));
			for (int i = 0; i < termVals.length; i++) {
				if (termVals[i] != vals[i]) {
					setItem(i, vals[i]);
					return;
				}
			}
		}
	}
	
	private void compensateAfterChange(List<Integer> forbiddenIndexes, int valsSum) {
		Integer toChange = findIndexToChange( forbiddenIndexes, valsSum );
		System.out.println("Compensating index " + toChange + ", forbidden = " + forbiddenIndexes + ", Sum = " + valsSum);
		
		forbiddenIndexes.add(toChange);
		
		vals[toChange] = ensureBounds(vals[toChange] - (valsSum - 100));
		propagation.put(toChange, vals[toChange]);
		
		System.out.println("Values: " + Arrays.toString(vals));
		setItem(toChange, vals[toChange]);
	}

	private Integer findIndexToChange( List<Integer> forbiddenIndexes, int valsSum )
	{
		Integer toChange = null;
		// try to find a terminal which has not been changed yet
		for (int i = 0; i < termVals.length; i++) {
			if (!forbiddenIndexes.contains(i) && !userChanges.contains(i) &&
					( valsSum < 100 || ( valsSum > 100 && vals[i] > 0 ) ) ) {
				System.out.println("Found never used index to change to give away %: " + i);
				toChange = i;
				break;
			}	
		}
		
		// if not found before, try to find the knob changed the longest time ago 
		if (toChange == null) {
			for (Integer change : userChanges) {
				if (!forbiddenIndexes.contains(change) &&
						( valsSum < 100 || ( valsSum > 100 && vals[change] > 0 ) )) {
					System.out.println("Found index to change, longest time changed: " + change);
					toChange = change;
					break;
				}
			}	
		}
		
		// if still not found, find the first terminal which is not forbidden
		if (toChange == null) {
			for (int i = 0; i < termVals.length; i++) {
				if (!forbiddenIndexes.contains(i) ) {
					System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^Found index to change, first which has a + value: " + i);
					toChange = i;
					break;
				}	
			}
			
		}
		
		return toChange;
	}

	public int val(int index) {
		return termVals[index];
	}
	
	private int sum(int[] vals) {
		int r = 0;
		for (int i = 0; i < vals.length; i++) {
			r += vals[i];
		}
		return r;
	}
	
	private int ensureBounds(int val) {
		return Math.max(Math.min(100, val), 0);
	}

}
