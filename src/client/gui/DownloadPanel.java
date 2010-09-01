package client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("serial")
public class DownloadPanel extends JPanel {

//	private ArrayList<TorrentTask> downloads;
	JList jt;
	JButton delete, pause;
	
	public void addTorrent (TorrentTask task) {
		ListModel lt = jt.getModel();
		Object[] data = new Object[ lt.getSize()+1 ];
		for (int i = 0; i < lt.getSize(); ++i)
			data[i] = lt.getElementAt(i);
		data[lt.getSize()] = task;
		jt.setListData(data);
	}
	
	protected void removeElem (int ind) {
		ListModel g = jt.getModel();
		TorrentTask [] o = new TorrentTask[g.getSize()-1];
		int c=0;
		for (int i = 0; i < g.getSize(); ++i)
			if ( ind != i )
				o[c++] = (TorrentTask) g.getElementAt(i);
		((TorrentTask) jt.getSelectedValue()).pause();
/*		if ( o.length == 0 ) {
			pause.setEnabled(false);
			delete.setEnabled(false);					
		}
*/
		jt.setListData(o);		
	}
	
	public DownloadPanel() {
		super();
//		downloads = new ArrayList<TorrentTask>();
		JLabel j = new JLabel("Downloads: ");
		add(j);

		setBackground(Color.orange);

		JPanel jp0 = new JPanel();
		jp0.setLayout(new BorderLayout());

		JPanel innerUpper = new JPanel();
		pause = new JButton("Pause");

		pause.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				TorrentTask t = (TorrentTask) jt.getSelectedValue();
				if ( t == null ) return ;
				if (pause.getText() == "Pause") {
					pause.setText("Resume");
					t.pause();
				} else {
					pause.setText("Pause");
					t.resume();
				}
			}
		});

		delete = new JButton("Delete");
		delete.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {			
				int ind = jt.getSelectedIndex();
				if ( ind == -1 ) return;
				removeElem(ind);
			}
		});

		innerUpper.add(pause);
		innerUpper.add(delete);
		jp0.add(BorderLayout.NORTH, innerUpper);

		JPanel innerLower = new JPanel();
		innerLower.setLayout(new BorderLayout());
		JLabel j1 = new JLabel("Name");
		innerLower.add(BorderLayout.WEST, j1);
		jp0.add(BorderLayout.CENTER, innerLower);

		jt = new JList( new Object[] {} );
		
		jt.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
		jt.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jt.addListSelectionListener(new ListSelectionListener() {
			
			public void valueChanged(ListSelectionEvent e) {
				Object[] tt = jt.getSelectedValues();
				if (tt.length == 0)
					return;
				boolean f = ((TorrentTask) tt[0]).isPaused();
				if (f)
					pause.setText("Resume");
				else
					pause.setText("Pause");
			}
		});

		JScrollPane sp = new JScrollPane(jt);
		sp.setPreferredSize(new Dimension(500, 200));
		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jp0.add(BorderLayout.SOUTH, sp);
		add(jp0);
		new Thread(new Runnable() {
			
			public void run() {
				while (true) {					
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						System.out.println(":( chance again");
					}					
					jt.repaint();
				}
			}
		}).start();
	}

}
