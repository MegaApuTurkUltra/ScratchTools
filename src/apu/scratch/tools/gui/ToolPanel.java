/**
 * 
 */
package apu.scratch.tools.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import apu.scratch.tools.ParamDef;
import apu.scratch.tools.ToolBase;

/**
 * @author MegaApuTurkUltra
 */
public class ToolPanel extends JPanel {
	private static final long serialVersionUID = 8590898716019244718L;
	List<ToolOption> toolOptions;

	public ToolPanel(ToolBase tool) {
		ParamDef[] params = tool.getParams();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		toolOptions = new LinkedList<ToolOption>();
		for (int i = 0; i < params.length; i++) {
			ToolOption option = ToolOption.getToolOption(params[i]);
			add(option);
			toolOptions.add(option);
		}
	}

	public String[] getAllOptions() {
		List<String> options = new LinkedList<String>();
		for (int i = 0; i < toolOptions.size(); i++) {
			String val = toolOptions.get(i).getValue();
			if (val != null)
				options.add(val);
		}
		return options.toArray(new String[options.size()]);
	}

	static abstract class ToolOption extends JPanel {
		private static final long serialVersionUID = 7216256329107183615L;
		ParamDef param;
		JLabel name;
		JCheckBox useThis;

		public ToolOption(ParamDef param) {
			this.param = param;
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			useThis = new JCheckBox("Use");
			useThis.setBorder(new EmptyBorder(0, 0, 0, 10));
			useThis.setSelected(true);
			useThis.setEnabled(param.isOptional());
			useThis.setToolTipText("Use this option");
			useThis.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					setAllEnabled(useThis.isSelected());
				}
			});
			add(useThis);

			name = new JLabel(param.getName());
			name.setToolTipText(param.getDesc());
			name.setBorder(new EmptyBorder(0, 0, 0, 10));
			add(name);
		}

		protected abstract String getValue0();

		public String getValue() {
			if (!useThis.isSelected())
				return null;
			return getValue0();
		}

		public abstract void setAllEnabled(boolean flag);

		public static ToolOption getToolOption(ParamDef param) {
			switch (param.getType()) {
			case BOOLEAN:
				return new BooleanToolOption(param);
			case FILENAME:
				return new FileSelectToolOption(param);
			case FILE_OUT:
				return new FileSaveToolOption(param);
			case NUMBER:
				return new NumberToolOption(param);
			case STRING:
			default:
				return new StringToolOption(param);
			}
		}
	}

	static class StringToolOption extends ToolOption {
		private static final long serialVersionUID = 8309499283087587645L;
		JTextField text;

		public StringToolOption(ParamDef param) {
			super(param);
			text = new JTextField();
			add(text);
		}

		@Override
		protected String getValue0() {
			return text.getText();
		}

		@Override
		public void setAllEnabled(boolean flag) {
			text.setEnabled(flag);
		}
	}

	static class FileSelectToolOption extends ToolOption {
		private static final long serialVersionUID = 1058459678992739785L;
		JFileChooser chooser;
		File selectedFile = null;
		JButton chooseFile;

		public FileSelectToolOption(ParamDef param) {
			super(param);
			chooser = new JFileChooser(new File("."));
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooseFile = new JButton("Choose File...");
			chooseFile.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							showChooserDialog();
						}
					});
				}
			});
			add(chooseFile);
		}

		public void showChooserDialog() {
			int val = chooser.showOpenDialog(ToolsGui.TOOLS_GUI);
			if (val == JFileChooser.APPROVE_OPTION)
				selectedFile = chooser.getSelectedFile();
		}

		@Override
		protected String getValue0() {
			return selectedFile == null ? null : selectedFile.getAbsolutePath();
		}

		@Override
		public void setAllEnabled(boolean flag) {
			chooseFile.setEnabled(flag);
		}
	}

	static class FileSaveToolOption extends FileSelectToolOption {
		private static final long serialVersionUID = -3502869498787477351L;

		public FileSaveToolOption(ParamDef param) {
			super(param);
		}

		public void showChooserDialog() {
			int val = chooser.showSaveDialog(ToolsGui.TOOLS_GUI);
			if (val == JFileChooser.APPROVE_OPTION)
				selectedFile = chooser.getSelectedFile();
		}
	}

	static class NumberToolOption extends ToolOption {
		private static final long serialVersionUID = 7295082988204063791L;
		JSpinner spinner;

		public NumberToolOption(ParamDef param) {
			super(param);
			spinner = new JSpinner(new SpinnerNumberModel(0, -Double.MIN_VALUE,
					Double.MAX_VALUE, 1));
			add(spinner);
			Dimension size = new Dimension(50, 20);
			setSize(size);
			setPreferredSize(size);
			spinner.setSize(size);
			spinner.setPreferredSize(size);
		}

		@Override
		protected String getValue0() {
			return spinner.getValue().toString();
		}

		@Override
		public void setAllEnabled(boolean flag) {
			spinner.setEnabled(flag);
		}
	}

	static class BooleanToolOption extends ToolOption {
		private static final long serialVersionUID = 5511114988830238547L;
		JCheckBox check;

		public BooleanToolOption(ParamDef param) {
			super(param);
			check = new JCheckBox();
			add(check);
		}

		@Override
		protected String getValue0() {
			return Boolean.toString(check.isSelected());
		}

		@Override
		public void setAllEnabled(boolean flag) {
			check.setEnabled(flag);
		}

	}
}
