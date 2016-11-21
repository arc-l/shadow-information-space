package geometry.pe.is;

/**
 * We want to encode the event using a standard interface to capture some 
 * common traits such as where the event happens in space/time. When we 
 * store event timing for a robot path, we might use robot relative 
 * on path from the starting point since it's essentially the same as time
 * in this case. 
 * 
 * @author Jingjin Yu
 *
 */

public interface Event {

	public double getEventRelativeTime();
	
}
