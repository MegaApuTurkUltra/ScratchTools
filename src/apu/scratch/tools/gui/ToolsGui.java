/**
 * 
 */
package apu.scratch.tools.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import apu.scratch.tools.ToolBase;

/**
 * @author MegaApuTurkUltra
 *
 */
public class ToolsGui extends JFrame {
	private static final long serialVersionUID = 240442966508049423L;
	private JPanel contentPane;

	public static ToolsGui TOOLS_GUI = null;
	private JList<ToolBase> toolsList;
	private JTextArea console;
	private JButton btnRunTool;
	private JPanel optionPanel;

	private ToolBase currentSel = null;
	private ToolPanel currentPanel = null;

	public static void launch() {
		TOOLS_GUI = new ToolsGui();
		TOOLS_GUI.setVisible(true);
	}

	public ToolsGui() {
		setTitle("ScratchTools");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(500, 500);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		toolsList = new JList<>();
		toolsList.setListData(ToolBase.tools
				.toArray(new ToolBase[ToolBase.tools.size()]));
		toolsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		toolsList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				ToolBase sel = toolsList.getSelectedValue();
				if (sel == null)
					return;
				currentSel = sel;
				optionPanel.removeAll();
				currentPanel = new ToolPanel(sel);
				optionPanel.add(currentPanel);
				
				ToolsGui.this.revalidate();
				ToolsGui.this.repaint();
			}
		});

		contentPane.add(toolsList, BorderLayout.WEST);

		JScrollPane consoleScrollPane = new JScrollPane();
		contentPane.add(consoleScrollPane, BorderLayout.SOUTH);

		console = new JTextArea();
		console.setEditable(false);
		console.setWrapStyleWord(true);
		console.setLineWrap(true);
		console.setRows(10);
		consoleScrollPane.setViewportView(console);

		JScrollPane optionScrollPane = new JScrollPane();
		contentPane.add(optionScrollPane, BorderLayout.CENTER);

		optionPanel = new JPanel();
		optionScrollPane.setViewportView(optionPanel);

		btnRunTool = new JButton("Run Tool");
		btnRunTool.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runTool();
			}
		});
		optionScrollPane.setColumnHeaderView(btnRunTool);
	}

	public void runTool() {
		if (currentSel == null)
			return;
		btnRunTool.setEnabled(false);
		toolsList.setEnabled(false);
		new ToolRunner(currentSel, currentPanel.getAllOptions(), new Runnable() {
			@Override
			public void run() {
				btnRunTool.setEnabled(true);
				toolsList.setEnabled(true);
			}
		});
	}

	public synchronized void putToConsole(char character){
		console.append("" + character);
		console.setCaretPosition(console.getText().length());
	}
}
