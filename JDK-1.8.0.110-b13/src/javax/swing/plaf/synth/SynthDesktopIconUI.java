/*
 * @(#)SynthDesktopIconUI.java	1.14 05/11/17
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.swing.plaf.synth;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.BasicDesktopIconUI;
import java.beans.*;
import java.io.Serializable;
import sun.swing.plaf.synth.SynthUI;


/**
 * Synth L&F for a minimized window on a desktop.
 *
 * @version 1.14, 11/17/05
 * @author Joshua Outwater
 */
class SynthDesktopIconUI extends BasicDesktopIconUI implements SynthUI,
					ActionListener, PropertyChangeListener {
    private SynthStyle style;

    public static ComponentUI createUI(JComponent c)    {
        return new SynthDesktopIconUI();
    }

    protected void installComponents() {
	if (UIManager.getBoolean("InternalFrame.useTaskBar")) {
	    iconPane = new JToggleButton(frame.getTitle(), frame.getFrameIcon()) {
		public String getToolTipText() {
		    return getText();
		}

		public JPopupMenu getComponentPopupMenu() {
		    return frame.getComponentPopupMenu();
		}
	    };
	    ToolTipManager.sharedInstance().registerComponent(iconPane);
	    iconPane.setFont(desktopIcon.getFont());
	    iconPane.setBackground(desktopIcon.getBackground());
	    iconPane.setForeground(desktopIcon.getForeground());
	} else {
	    iconPane = new SynthInternalFrameTitlePane(frame);
	    iconPane.setName("InternalFrame.northPane");
	}
	desktopIcon.setLayout(new BorderLayout());
	desktopIcon.add(iconPane, BorderLayout.CENTER);
    }

    protected void installListeners() {
        super.installListeners();
        desktopIcon.addPropertyChangeListener(this);

	if (iconPane instanceof JToggleButton) {
	    frame.addPropertyChangeListener(this);
	    ((JToggleButton)iconPane).addActionListener(this);
	}
    }

    protected void uninstallListeners() {
	if (iconPane instanceof JToggleButton) {
	    frame.removePropertyChangeListener(this);	
	}
        desktopIcon.removePropertyChangeListener(this);
        super.uninstallListeners();
    }

    protected void installDefaults() {
        updateStyle(desktopIcon);
    }

    private void updateStyle(JComponent c) {
        SynthContext context = getContext(c, ENABLED);
        style = SynthLookAndFeel.updateStyle(context, this);
        context.dispose();
    }

    protected void uninstallDefaults() {
        SynthContext context = getContext(desktopIcon, ENABLED);
        style.uninstallDefaults(context);
        context.dispose();
        style = null;
    }

    public SynthContext getContext(JComponent c) {
        return getContext(c, getComponentState(c));
    }

    private SynthContext getContext(JComponent c, int state) {
        Region region = getRegion(c);
        return SynthContext.getContext(SynthContext.class, c, region,
                                       style, state);
    }

    private int getComponentState(JComponent c) {
        return SynthLookAndFeel.getComponentState(c);
    }

    Region getRegion(JComponent c) {
        return SynthLookAndFeel.getRegion(c);
    }

    public void update(Graphics g, JComponent c) {
        SynthContext context = getContext(c);

        SynthLookAndFeel.update(context, g);
        context.getPainter().paintDesktopIconBackground(context, g, 0, 0,
                                                  c.getWidth(), c.getHeight());
        paint(context, g);
        context.dispose();
    }

    public void paint(Graphics g, JComponent c) {
        SynthContext context = getContext(c);

        paint(context, g);
        context.dispose();
    }

    protected void paint(SynthContext context, Graphics g) {
    }

    public void paintBorder(SynthContext context, Graphics g, int x,
                            int y, int w, int h) {
        context.getPainter().paintDesktopIconBorder(context, g, x, y, w, h);
    }

    public void actionPerformed(ActionEvent evt) {
	if (evt.getSource() instanceof JToggleButton) {
	    // Either iconify the frame or deiconify and activate it.
	    JToggleButton button = (JToggleButton)evt.getSource();
	    try {
		boolean selected = button.isSelected();
		if (!selected && !frame.isIconifiable()) {
		    button.setSelected(true);
		} else {
		    frame.setIcon(!selected);
		    if (selected) {
			frame.setSelected(true);
		    }
		}
	    } catch (PropertyVetoException e2) {
	    }
	}
    }

    public void propertyChange(PropertyChangeEvent evt) {
	if (evt.getSource() instanceof JInternalFrame.JDesktopIcon) {
	    if (SynthLookAndFeel.shouldUpdateStyle(evt)) {
		updateStyle((JInternalFrame.JDesktopIcon)evt.getSource());
	    }
	} else if (evt.getSource() instanceof JInternalFrame) {
	    JInternalFrame frame = (JInternalFrame)evt.getSource();
	    if (iconPane instanceof JToggleButton) {
		JToggleButton button = (JToggleButton)iconPane;
		String prop = evt.getPropertyName();
		if (prop == "title") {
		    button.setText((String)evt.getNewValue());
		} else if (prop == "frameIcon") {
		    button.setIcon((Icon)evt.getNewValue());
		} else if (prop == JInternalFrame.IS_ICON_PROPERTY ||
			   prop == JInternalFrame.IS_SELECTED_PROPERTY) {
		    button.setSelected(!frame.isIcon() && frame.isSelected());
		}
	    }
	}
    }
}
