package geometry.pe.is.singleTypeAgent;

import geometry.pe.is.Event;
import geometry.pe.is.Oracle.EVENT_TYPE;

/**
 * We can make this more object oriented, but then it can get a bit bloated as
 * well. So we cram all the events into this single one.
 * 
 * @author Jingjin Yu
 * 
 */
public class SingleTypeAgentEvent implements Event {

	private EVENT_TYPE eventType;

	private double eventRelativeTime;

	private int fromGap[], toGap[];

	private int visibleAgents, movedAgents;

	public void setEventRelativeTime(double eventRelativeTime) {
		this.eventRelativeTime = eventRelativeTime;
	}

	public double getEventRelativeTime() {
		return eventRelativeTime;
	}

	public EVENT_TYPE getEventType() {
		return eventType;
	}

	public void setEventType(EVENT_TYPE eventType) {
		this.eventType = eventType;
	}

	public int[] getFromGap() {
		return fromGap;
	}

	public void setFromGap(int[] fromGap) {
		this.fromGap = fromGap;
	}

	public int getMovedAgents() {
		return movedAgents;
	}

	public void setMovedAgents(int movedAgents) {
		this.movedAgents = movedAgents;
	}

	public int[] getToGap() {
		return toGap;
	}

	public void setToGap(int[] toGap) {
		this.toGap = toGap;
	}

	public int getVisibleAgents() {
		return visibleAgents;
	}

	public void setVisibleAgents(int visibleAgents) {
		this.visibleAgents = visibleAgents;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		switch (eventType) {
		case AGENT_APPEAR:
			buf.append("AGENT_APPEAR: ").append(getMovedAgents()).append(
					" appeared from gap ").append(getFromGap()[0]);
			break;
		case AGENT_DISAPPEAR:
			buf.append("AGENT_DISAPPEAR: ").append(getMovedAgents()).append(
					" disappeared from gap ").append(getToGap()[0]);
			break;
		case INIT:
			buf.append("INIT EVENT");
			for (int i = 0; i < toGap.length; i++) {
				buf.append(" ").append(toGap[i]);
			}
			break;
		case APPEAR:
			buf.append("APPEAR EVENT");
			buf.append(" ").append(toGap[0]).append(" <- ").append(
					this.movedAgents);
			break;
		case DISAPPEAR:
			buf.append("DISAPPEAR EVENT");
			buf.append(" ").append(fromGap[0]).append(" -> ").append(
					this.movedAgents);
			break;
		case SPLIT:
			buf.append("SPLIT EVENT");
			buf.append(" ").append(fromGap[0]).append(" -> ").append(toGap[0])
					.append(" ").append(toGap[1]);
			break;
		case MERGE:
			buf.append("MERGE EVENT");
			buf.append(" ").append(fromGap[0]).append(" ").append(fromGap[1])
					.append(" -> ").append(toGap[0]);
			break;
		}
		return buf.toString();
	}
}
