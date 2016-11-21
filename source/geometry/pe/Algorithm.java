package geometry.pe;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import geometry.gap.Gap;
import geometry.pe.is.basic.NSEState;

/**
 * 
 * PE related algorithms
 * 
 * @author Jingjin Yu
 * 
 */
public class Algorithm {

	public static enum SCENARIO {
		NEVER_SEE_EVADER
		// The pursuer never sees evader
	}

	/**
	 * Based on scenarios, process gap history
	 * 
	 * @param gapss
	 * @param scenario
	 * @param additionalData
	 */
	public static void processGapHistoryInformation(Gap[][] gapss,
			SCENARIO scenario, Object additionalData) {
		switch (scenario) {
		case NEVER_SEE_EVADER:
			pursuerNeverSeeEvader(gapss);
			break;
		default:
		}
	}

	/**
	 * This method takes care of the case where pursuer never sees an evader.
	 * The gap region is either clear, which means that there's no evader, or
	 * contaminated, which means there might be evaders. However, as a gap
	 * disappears, no evader is ever found; the gap is simply gone.
	 * 
	 * This is probably the simplest case. Here,
	 * 
	 * @param gapss
	 */
	public static void pursuerNeverSeeEvader(Gap[][] gapss) {
		// At least we need to have one set of gaps
		if (gapss.length == 0 || gapss[0].length == 0)
			return;

		// Assign all gaps to be contaminated for first set of gaps
		for (int i = 0; i < gapss[0].length; i++) {
			gapss[0][i].setState(NSEState.CONTAMINATED);
		}

		// Process gaps, set by set
		for (int i = 0; i < gapss.length; i++) {
			Gap[] gaps = gapss[i];

			// Add next set of gaps into a map for lookup, if there are
			Map<Integer, Gap> nextGapMap = new HashMap<Integer, Gap>();
			if (i != gapss.length - 1) {
				Gap[] nextGaps = gapss[i + 1];
				for (int j = 0; j < nextGaps.length; j++) {
					nextGapMap.put(nextGaps[j].getId(), nextGaps[j]);
				}
			}

			// Process gaps
			for (int j = 0; j < gaps.length; j++) {
				Gap gap = gaps[j];
				Set<Integer> toGapSet = gap.getToGapSet();

				// New gaps will not have a state set; set it to clear
				if (gap.getState() == null) {
					gap.setState(NSEState.CLEAR);
				}
				// If two map is empty, then the gap either disappears or
				// continues to exist. Propagate state is gap continues to exist
				else if (toGapSet.size() == 0) {
					Gap nextGap = nextGapMap.get(gap.getId());
					if (nextGap != null) {
						nextGap.setState(gap.getState());
					}
				}
				// Gap merges. If current gap is not clear set the descendent
				// gap to be contaminated as well
				else if (toGapSet.size() == 1) {
					if (!((NSEState) gap.getState()).isClear()) {
						nextGapMap.get(toGapSet.toArray(new Integer[0])[0])
								.setState(NSEState.CONTAMINATED);
					}
				}
				// Gap splits into two. Propagate gap state into descendents
				else if (toGapSet.size() == 2) {
					Integer[] gapis = toGapSet.toArray(new Integer[0]);
					nextGapMap.get(gapis[0]).setState(gap.getState());
					nextGapMap.get(gapis[1]).setState(gap.getState());
				}
			}
		}
	}
	 
}
