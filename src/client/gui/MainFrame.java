package client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {

	public MainFrame() {
		super("JTorrent");
		setPreferredSize( new Dimension(800, 400) );
		setLayout( new BorderLayout() );
		
		JTabbedPane tabbedPane = new JTabbedPane();		
		DownloadPanel jp2 = new DownloadPanel();
		final SearchPanel jp1 = new SearchPanel(jp2);
		tabbedPane.addTab("Search", jp1);
		tabbedPane.addTab("Downloads", jp2);

		add( BorderLayout.CENTER, tabbedPane );		
		pack();
		setVisible( true );
		addWindowListener(new WindowAdapter() {
		      public void windowClosing(WindowEvent e) {
		    	  jp1.closeConnections();
		    	  System.exit(0);
		      }
		});
		setDefaultCloseOperation( EXIT_ON_CLOSE );
		//setResizable(false);
	}
	
	public static void main(String[] args) {
		new MainFrame ();
	}
	
}
