package geometry.pe.is;

import geometry.gap.Gap;

/**
 * This oracle does distribution of agents inside an environment; it decides how
 * many agents are in the visibility polygon and behind gaps
 * 
 * @author Jingjin Yu
 * 
 */
public abstract class Oracle {

	public static enum EVENT_TYPE {INIT, SPLIT, MERGE, APPEAR, DISAPPEAR, AGENT_APPEAR, AGENT_DISAPPEAR}
	
	/**
	 * Initialize gaps so that oracle knows what the environment will be
	 * 
	 * @param gapss
	 */
	public abstract void initialize(Gap[][] gapss);

	/**
	 * Total number of agents in the environment
	 * 
	 * @return
	 */
	public abstract int getTotalAgentNumber();
	
	/**
	 * Get number of event
	 * 
	 * @return
	 */
	public abstract int getNumberOfEvents();

	/**
	 * Get first event
	 * 
	 * @return
	 */
	public abstract Event getEvent(int i);
	
	public abstract Event getEventByTime(double time);
}
