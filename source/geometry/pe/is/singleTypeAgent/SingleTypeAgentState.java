package geometry.pe.is.singleTypeAgent;

import geometry.gap.GapState;

public class SingleTypeAgentState implements GapState {
	
	private int numberOfAgents;

	public SingleTypeAgentState(int numberOfAgents) {
		super();
		this.numberOfAgents = numberOfAgents;
	}

	public int getNumberOfAgents() {
		return numberOfAgents;
	}

	public void setNumberOfAgents(int numberOfAgents) {
		this.numberOfAgents = numberOfAgents;
	}

}
