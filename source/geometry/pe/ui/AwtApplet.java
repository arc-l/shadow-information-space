package geometry.pe.ui;

import java.applet.Applet;

import geometry.FileHelper;

public class AwtApplet extends Applet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1290636242422686146L;

	/**
	 * Override default init to:
	 *  
	 *  -	Copy the required jars and dlls for SWT execution
	 *  -	Obtain code base for loading any other resources over the network
	 * 
	 */
	public void init()
	{
		/** Set up remote path. This supports both local and remote testing. */
		FileHelper.AppletInit(this);
		String remotePath = FileHelper.getBaseUrl();
		System.out.println("Using " + remotePath + " as remote base URL");

		appInit();
	}
	
	/**
	 * Additional application init code
	 *
	 */
	public void appInit(){
		
	}
}
