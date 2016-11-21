package geometry.pe.ui;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


import geometry.FileHelper;

public class Application {

	public Application() {
		Frame mf = new Frame();
		mf.setTitle("Base Application");
		final ProjectPanel2 p = new ProjectPanel2();
		mf.add(p);
		
		mf.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}

			public void windowActivated(WindowEvent e) {
				p.windowActivated();
			}

			public void windowDeactivated(WindowEvent e) {
				p.windowDeactivated();
			}
		});
		
		mf.pack();
		mf.setVisible(true);
		p.listeningEvents();
		
//		System.exit(0);
	}
	
	public static void main(String[] args)
	{
		FileHelper.regularInit();
		new Application();
	}
}
