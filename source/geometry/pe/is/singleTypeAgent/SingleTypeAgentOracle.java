package geometry.pe.is.singleTypeAgent;

import geometry.gap.Gap;
import geometry.pe.is.Event;
import geometry.pe.is.Oracle;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class SingleTypeAgentOracle extends Oracle {

	private int totalAgents;
	
	private Map<Double, SingleTypeAgentEvent> eventMap = new TreeMap<Double, SingleTypeAgentEvent>();
	
	private SingleTypeAgentEvent stae[] = null;
	
	public SingleTypeAgentOracle(int totalAgents) {
		super();
		this.totalAgents = totalAgents;
	}

	public int getTotalAgentNumber() {
		return totalAgents;
	}

	@Override
	public int getNumberOfEvents(){
		return stae.length;
	}
	
	@Override
	public Event getEvent(int i){
		return stae[i]; 
	}
	
	@Override
	public void initialize(Gap[][] gapss) {
		eventMap.clear();
		distributeAgents(gapss);
		stae = eventMap.values().toArray(new SingleTypeAgentEvent[0]);
	}
	
	@Override
	public Event getEventByTime(double t){
		return eventMap.get(new Double(t));
	}

	/**
	 * Distributes agents randomly as gaps evolve
	 * 
	 * @param gapss
	 */
	private void distributeAgents(Gap[][] gapss) {
		
		int previousVisibleAgents = 0;
		
		for (int i = 0; i < gapss.length; i++) {
			Gap[] gaps = gapss[i];
			
			SingleTypeAgentEvent e = new SingleTypeAgentEvent();
			// First initialize first step, initial condition
			if (i == 0) {
				previousVisibleAgents = (int) (Math.random() * 0.5 * totalAgents);
				e.setVisibleAgents(previousVisibleAgents);
				e.setEventType(EVENT_TYPE.INIT);
				int[] b = distributeOneBatch(totalAgents - previousVisibleAgents, gaps.length);
				int toGaps[] = new int[gaps.length];
				e.setToGap(toGaps);
				for (int j = 0; j < gaps.length; j++) {
					gaps[j].setState(new SingleTypeAgentState(b[j]));
					toGaps[j] = gaps[j].getId();
				}
			}
			// Propagate
			else {
				Map<Integer, Gap> gm = Gap.getGapMap(gaps);
				Gap[] pGaps = gapss[i - 1];
				Map<Integer, Gap> pgm = Gap.getGapMap(pGaps);

				// Split or new gap appears
				if (gaps.length > pGaps.length) {
					for (int j = 0; j < pGaps.length; j++) {
						Integer[] toGaps = pGaps[j].getToGapSet().toArray(
								new Integer[0]);
						// Split?
						if (toGaps.length == 2) {
							e.setEventType(EVENT_TYPE.SPLIT);
							int t = ((SingleTypeAgentState) pGaps[j].getState())
									.getNumberOfAgents();
							int[] b = distributeOneBatch(t, 2);
							gm.get(toGaps[0]).setState(
									new SingleTypeAgentState(b[0]));
							gm.get(toGaps[1]).setState(
									new SingleTypeAgentState(b[1]));
							e.setFromGap(new int[]{pGaps[j].getId()});
							e.setToGap(new int[]{toGaps[0].intValue(), toGaps[1].intValue()});
						} else {
							gm.get(pGaps[j].getId()).setState(
									pGaps[j].getState());
						}
						gm.remove(pGaps[j].getId());
					}
					// New gap appear?
					if (gm.size() == 1) {
						e.setEventType(EVENT_TYPE.APPEAR);
						int gn = gm.keySet().toArray(new Integer[0])[0]
								.intValue();
						int[] b = distributeOneBatch(previousVisibleAgents, 2);
						gm.get(gn).setState(new SingleTypeAgentState(b[0]));
						previousVisibleAgents = b[1];
						e.setVisibleAgents(previousVisibleAgents);
						e.setToGap(new int[]{gm.get(gn).getId()});
						e.setMovedAgents(b[0]);
					} else {
						e.setVisibleAgents(previousVisibleAgents);
					}
				}
				// Merge or gap disappears
				else {
					for (int j = 0; j < pGaps.length; j++) {
						Integer[] toGaps = pGaps[j].getToGapSet().toArray(
								new Integer[0]);

						// Merge?
						if (toGaps.length == 1) {
							int t = ((SingleTypeAgentState) pGaps[j].getState())
									.getNumberOfAgents();
							Gap toGap = gm.get(toGaps[0]);
							if (toGap.getState() == null){
								e.setEventType(EVENT_TYPE.MERGE);
								e.setVisibleAgents(previousVisibleAgents);
								toGap.setState(new SingleTypeAgentState(t));
								e.setToGap(new int[]{toGap.getId()});
								e.setFromGap(new int[]{pGaps[j].getId(), 0});
							}
							else{
								toGap.setState(new SingleTypeAgentState(
										((SingleTypeAgentState) pGaps[j]
												.getState())
												.getNumberOfAgents()
												+ t));
								e.getFromGap()[1] = pGaps[j].getId();
							}
						} else {
							Gap toGap = gm.get(pGaps[j].getId());
							// Disappear
							if(toGap == null){
								e.setEventType(EVENT_TYPE.DISAPPEAR);
								int ng = ((SingleTypeAgentState)pGaps[j].getState()).getNumberOfAgents();
								previousVisibleAgents += ng;
								e.setVisibleAgents(previousVisibleAgents);
								e.setFromGap(new int[]{pGaps[j].getId()});
								e.setMovedAgents(ng);
							}
							else{
								toGap.setState(
									pGaps[j].getState());
							}
						}
						pgm.remove(pGaps[j].getId());
					}
				}
			}
			
			e.setEventRelativeTime(gapss[i][0].getRelativeTime());
			eventMap.put(gapss[i][0].getRelativeTime(), e);
			
			// Add other visibility events randomly
//			double curTime = gapss[i][0].getRelativeTime();
//			double nextTime = 1;
//			if(i < gapss.length - 1) nextTime = gapss[i + 1][0].getRelativeTime();
//			previousVisibleAgents = addRandomVisibilityEvents(gapss[i], previousVisibleAgents, curTime, nextTime);
		}
	}
	
	/**
	 * Randomly add other types of events that is consistent with current gap
	 * structure. There are two types of events to be added: agents appear 
	 * from behind of a gap and agents disappear behind a gap. For now we 
	 * randomly assign 0-4 events
	 * 
	 * @param gaps
	 * @param centerAgents
	 * @return
	 */
	protected int addRandomVisibilityEvents(Gap[] gaps, int centerAgents, double curTime, double nextTime){
		int events = (int) (Math.random()*5);
		for(int i = 0; i < events; i ++){
			if(Math.random() < 0.5){
				SingleTypeAgentEvent e = new SingleTypeAgentEvent();
				e.setEventType(EVENT_TYPE.AGENT_APPEAR);
				// Pick a gap
				int iGap = (int) (Math.random() * gaps.length);
				// See whether we have agents in the gap
				SingleTypeAgentState state = (SingleTypeAgentState) gaps[iGap].getState();
				if(state != null && state.getNumberOfAgents() > 0){
					// Subtract random number of agents from the gap
					int agents = state.getNumberOfAgents();
					int deltaA = (int) (Math.random() * (agents / 2)) + 1;
					state.setNumberOfAgents(agents - deltaA);
					centerAgents += deltaA;
					
					// Create event
					e.setMovedAgents(deltaA);
					e.setFromGap(new int[]{iGap});
					e.setVisibleAgents(centerAgents);
					curTime = curTime + (nextTime - curTime) * (i + 1) / events * Math.random();
					e.setEventRelativeTime(curTime);
					eventMap.put(curTime, e);
				}
			}
			else{
				SingleTypeAgentEvent e = new SingleTypeAgentEvent();
				e.setEventType(EVENT_TYPE.AGENT_DISAPPEAR);
				// Pick a gap
				int iGap = (int) (Math.random() * gaps.length);
				// See whether we have agents in visibility region
				SingleTypeAgentState state = (SingleTypeAgentState) gaps[iGap].getState();
				if(centerAgents >0 ){
					// Subtract random number of agents from visibility region
					int deltaA = (int) (Math.random() * (centerAgents / 2)) + 1;
					state.setNumberOfAgents(deltaA);
					centerAgents -= deltaA;
					
					// Create event
					e.setMovedAgents(deltaA);
					e.setToGap(new int[]{iGap});
					e.setVisibleAgents(centerAgents);
					curTime = curTime + (nextTime - curTime) * (i + 1) / events * Math.random();
					e.setEventRelativeTime(curTime);
					eventMap.put(curTime, e);
				}
			}
		}
		
		return centerAgents; 
	}

	/**
	 * Distribute randomly t agents into g gaps. The algorithm take g-1
	 * different random numbers from 0 - t and use those as separation points.
	 * It then picks the pieces in between. For example, t = 10 and g = 3. We
	 * obtain randomly points 3 and 8. So 3 agents goes to first gap, 8-3 = 5
	 * agents will go to second gap and 10 - 8 = 2 agents will go to the last
	 * gap.
	 * 
	 * @param total
	 * @param gaps
	 * @return
	 */
	private int[] distributeOneBatch(int t, int g) {

		// If single gap just return
		if (g == 1)
			return new int[] { t };
		
		// If t is 0 then just return bunch zeros
		if (t == 0){
			return new int[g];
		}

		// More than one gap
		Set<Integer> spSet = new TreeSet<Integer>();
		while (spSet.size() < g - 1) {
			spSet.add(new Integer((int) (Math.random() * t)));
		}
		spSet.add(t);
		Integer[] retI = spSet.toArray(new Integer[0]);
		int[] ret = new int[g];
		ret[0] = retI[0].intValue();
		for (int i = 1; i < ret.length; i++) {
			ret[i] = retI[i] - retI[i - 1];
		}
		return ret;
	}

}
