package client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.SoftBevelBorder;

import server.Pair;
import services.UrlService;
import services.XmlService;

class Elem {

	// basic class for JList items

	private String content;
	private String link;

	Elem(String _content, String _link) {
		content = new String(_content);
		link = new String(_link);
	}

	public String getName() {
		return content;
	}
	
	public String getLink() {
		return link;
	}

	@Override
	public String toString() {
		return content;
	}

}

@SuppressWarnings("serial")
public class SearchPanel extends JPanel {

	protected JList jt;
	protected JTextField area;
	protected JLabel found;
	private XmlService db;
	private DownloadPanel downPanel;
	
	
	@SuppressWarnings("unchecked")
	public void query(String s) {
//		System.out.println(s);
		db.writeObject(s);
		List<Pair<String>> results = (List<Pair<String>>) db.readObject();
		int sz;
		if( results == null ) sz = 0;
		else sz = results.size();
		found.setText( "found " + sz + " items" );
		found.setVisible(true);
		if ( sz == 0 ) { jt.setListData(new Object[] {}); return ; }
		Object[] data = new Object[results.size()];
		int q = 0;
		for (Pair<String> p : results) {
			Elem e = new Elem(p.getFirst(), p.getSecond());
			data[q++] = e;
		}
		jt.setListData(data);
	}

	public void requestDownload(Elem e) {
		// TorrentDownloadTask t = new TorrentDownloadTask(e);
		JFileChooser jf;
		File directory;
		setPreferredSize( new Dimension(400, 400) );
		
		jf = new JFileChooser();
		jf.setDialogTitle("Save as");
		jf.setApproveButtonText("Save");
		jf.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
		if ( jf.showOpenDialog(this) == JFileChooser.APPROVE_OPTION ) {
			directory = jf.getSelectedFile();
			System.out.println(directory.getAbsolutePath());
			String tmpDir = System.getProperty("java.io.tmpdir");
			if(!UrlService.copyFile(e.getLink(), tmpDir + "/__" + e.getName() + ".torrent"))
				System.out.println("neshto se omaza s tegleneto na torenta");
			//System.out.println("name " + file + " torrent" + torrent);
			System.out.println("name " + e.getName());
			System.out.println(e.getLink());
			downPanel.addTorrent(new TorrentTask(tmpDir + "/__" + e.getName() + ".torrent", directory.getAbsolutePath() + "/", e.getName(), e));
			//TorrentDownloader td = new TorrentDownloader(tmpDir + "/__tmp123.torrent", directory.getAbsolutePath() + "/", e.getName());
			//td.start();
		}

	}

	public SearchPanel(DownloadPanel downPanel) {
		
		super();
		
		this.downPanel = downPanel;

		ResourceBundle prop = ResourceBundle.getBundle("client");
		
		db = XmlService.createXMLHandler(prop.getString("server_host"), Integer.parseInt(prop.getString("server_port")));
		db.writeObject(new Integer(0));
		
		found = new JLabel();
		
		setBackground(Color.orange);

		JPanel jp0 = new JPanel();
		jp0.setLayout(new BorderLayout());

		JPanel innerUpper = new JPanel(); // innerUpper.setPreferredSize( new
											// Dimension(500, 100) );
		// uncomment upper if you want download button on a new line
		area = new JTextField(30);
		innerUpper.add(area);

		JButton search = new JButton("Search");
		search.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				final String q = area.getText();
				new Thread(new Runnable( ) {
				

					public void run() {
						query(q);
					}
				}).start();
			}
		});

		JButton down = new JButton("Download");
		down.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				Object[] t = jt.getSelectedValues();
				for (Object x : t)
					requestDownload((Elem) x);
			}
		});

		innerUpper.add(search);
		innerUpper.add(down);
		jp0.add(BorderLayout.NORTH, innerUpper);

		JPanel innerLower = new JPanel();
		innerLower.setLayout(new BorderLayout());
		JLabel j1 = new JLabel("Name");
		innerLower.add(BorderLayout.WEST, j1);
		// JLabel j2 = new JLabel("S/L"); innerLower.add(BorderLayout.EAST,j2);
		innerLower.add(BorderLayout.EAST, found);
		found.setVisible(false);
		jp0.add(BorderLayout.CENTER, innerLower);

		jt = new JList();
		jt.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
		JScrollPane sp = new JScrollPane(jt);
		sp.setPreferredSize(new Dimension(500, 200));
		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jp0.add(BorderLayout.SOUTH, sp);

		add(jp0);
		area.addKeyListener(new KeyListener() {
			
			public void keyTyped(KeyEvent e) {
			}


			public void keyReleased(KeyEvent e) {
			}


			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					new Thread(new Runnable( ) {
						

						public void run() {
							// TODO Auto-generated method stub
							query(area.getText());
						}
					}).start();
			}
		});

	}

}
