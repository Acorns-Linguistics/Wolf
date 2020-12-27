/*
 *   class DictionaryPanels.java
 *
 *   @author  HarveyD
 *   Dan Harvey - Professor of Computer Science
 *   Southern Oregon University, 1250 Siskiyou Blvd., Ashland, OR 97520-5028
 *   harveyd@sou.edu
 *   @version 1.00
 *
 *   Copyright 2010, all rights reserved
 *
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * To receive a copy of the GNU Lesser General Public write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.wolf.application;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.CompoundControl;
import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.Port;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.acorns.editor.OptionPanel;
import org.acorns.visual.ColorScheme;
import org.wolf.lib.Icons;
import org.wolf.system.Environment;

/** Create a panel with more than one dictionary panels. */
public class DictionaryPanels extends RootDictionaryPanel      
{
 	private static final long serialVersionUID = 1L;
	private DictionaryPanel  panel;
    private JCheckBox        volumeMute, microphoneMute;
    private JPanel           thisPanel, center;
    
     /** Panel background color behind buttons */
    public static final int ICON = 30;

    public static final Color BACKGROUND  = new Color(200,200,200);
    /** Panel Dark background in scrollbar viewport */
    public static final Color FOREGROUND  = Color.WHITE;
	
    
    /** Creates a new instance of DictionaryPanels
     *
     * @param env Runtime environment class
     * @param number Number of panels to create (Only one supported at present)
     */
    public DictionaryPanels(Environment env)
    {   super(env);

        // Set the layout of this panel.
        thisPanel = this;
        setLayout(new BorderLayout());
        setBackground(BACKGROUND);

        // Create the "DictionaryListeners" properties change listener
        // Remove previous "DictionaryListeners"
        String listener = "DictionaryListeners";
        PropertyChangeListener[] pcl 
            = Toolkit.getDefaultToolkit().getPropertyChangeListeners(listener);
        for (int i=0; i<pcl.length; i++)
        {   Toolkit.getDefaultToolkit().removePropertyChangeListener
                                                        (listener, pcl[0]); }
        // Add the new one.
        Toolkit.getDefaultToolkit().addPropertyChangeListener(listener, this);
 
        // Create array of dictionary panels and add them to the center.
        center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(BACKGROUND);
        
        Color color = new Color(224,224,224);
        panel = new DictionaryPanel
                    (this, new ColorScheme(color, null), null);
        setButtonPanel(panel);
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createLoweredBevelBorder());
        center.add(panel);
        center.add(Box.createVerticalStrut(10));
        add(center, BorderLayout.CENTER);
        
         // Create the slider that controls speaker volume.
        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.X_AXIS));
        north.setBackground(BACKGROUND);
       
        // Create a check Box to indicate if the speaker is muted.
        volumeMute = new JCheckBox("Mute");
        volumeMute.setBorder(BorderFactory.createEtchedBorder());
        volumeMute.setBackground(BACKGROUND);
        volumeMute.setToolTipText("Turn on or off the Speaker Output");
        volumeMute.setSelected(updateMute(Port.Info.SPEAKER, false, false));
        volumeMute.addActionListener(
                new ActionListener()
                {
                   public void actionPerformed(ActionEvent event)
                   {
                       JCheckBox checkBox = (JCheckBox)event.getSource();
                       boolean  value    = checkBox.isSelected();                       
                       if (!updateMute(Port.Info.SPEAKER, value, true))
                       {
                           checkBox.setSelected(false);
                           JOptionPane.showMessageDialog
                               (thisPanel, "Couldn't access Mute Controls");
                       }
                   }
                });
       
        JSlider volume     = new JSlider();
        volume.setBackground(BACKGROUND);
        volume.setMinimum(0);
        volume.setMaximum(20);
        volume.setToolTipText("Drag to Control Speaker Volume");
        float speakerRatio = updateControls(Port.Info.SPEAKER, -1);
        volume.setValue((int)(20 * speakerRatio));
        volume.addChangeListener(
                new ChangeListener()
                {  public void stateChanged(ChangeEvent ce) 
                   {   JSlider slider = (JSlider)ce.getSource();
                       float   value    = slider.getValue();
                       float   minValue = slider.getMinimum();
                       float   maxValue = slider.getMaximum();
                       float   ratio    = (value-minValue)/(maxValue-minValue);
                       
                       updateControls(Port.Info.SPEAKER, ratio);
                       if (ratio<0)
                       {  JOptionPane.showMessageDialog
                             (thisPanel, "Couldn't Alter the Speaker Controls");
                       }
                   }   // End of StateChanged.
                });
    
        // Create a check Box to indicate if the micropone is muted.
        microphoneMute = new JCheckBox("Boost");
        microphoneMute.setBorder(BorderFactory.createEtchedBorder());
        microphoneMute.setBackground(BACKGROUND);
        microphoneMute.setToolTipText
                ("Turn on or off the Boosting of Microphone Input Signals");
        microphoneMute.setSelected
                (updateMute(Port.Info.MICROPHONE, false, false));
        microphoneMute.addActionListener(
                new ActionListener()
                {
                   public void actionPerformed(ActionEvent event)
                   {
                       JCheckBox checkBox = (JCheckBox)event.getSource();
                       boolean  value    = checkBox.isSelected();                       
                       if (!updateMute(Port.Info.MICROPHONE, value, true))
                       {
                           checkBox.setSelected(false);
                           JOptionPane.showMessageDialog
                               (thisPanel, "Couldn't access Mute Controls");
                       }
                   }
                });
       
        // Create the slider that controls microphone volume.
        JSlider microphone = new JSlider();
        microphone.setBackground(BACKGROUND);
        microphone.setMinimum(0);
        microphone.setMaximum(20);
        microphone.setToolTipText("Drag to Control Microphone Input Volume");

        float microphoneRatio = updateControls(Port.Info.MICROPHONE, -1);
        microphone.setValue((int)(20 * microphoneRatio));
        microphone.addChangeListener(
                new ChangeListener()
                {
                   public void stateChanged(ChangeEvent ce) 
                   {
                       JSlider slider = (JSlider)ce.getSource();
                       float   value    = slider.getValue();
                       float   minValue = slider.getMinimum();
                       float   maxValue = slider.getMaximum();
                       float   ratio    = (value-minValue)/(maxValue-minValue);
                       
                       updateControls(Port.Info.MICROPHONE, ratio);
                       if (ratio<0)
                       {  JOptionPane.showMessageDialog
                            (thisPanel
                                  , "Couldn't Alter the Microphone Controls");
                       }
                   }
                });

        // Create the Help Button.
        JButton helpButton = null;
        ImageIcon icon = Icons.getImageIcon("help.png", 0);
        helpButton = new JButton(icon);
        helpButton.setPreferredSize(new Dimension(ICON+1, ICON+1));
        helpButton.setMinimumSize  (new Dimension(ICON+1, ICON+1));
        helpButton.setMaximumSize  (new Dimension(ICON+1, ICON+1));
        helpButton.setToolTipText("Get Help on How to Manage Dictionaries");
        helpButton.setBackground(BACKGROUND);
        HelpSet helpSet = env.getHelpSet();
        
        if (helpSet!=null)
        {  
        	HelpBroker helpBroker = helpSet.createHelpBroker("Main_Window");
            final ActionListener newListener = new CSH.DisplayHelpFromSource(helpBroker);
            helpButton.addActionListener(e -> {
          		Frame frame = Environment.getRootFrame();
          		Point point = frame.getLocation();
          		helpBroker.setLocation(point);
          		ActionEvent event = (ActionEvent)(e);
          	    newListener.actionPerformed(event);
            });
        }
        else
        {  helpButton.addActionListener(
                new ActionListener()
                {   public void actionPerformed(ActionEvent ae)
                    {  JOptionPane.showMessageDialog
                               (thisPanel, "Sorry, No Help is Available");
                    }
                });
        }
         
        // Create the advanced controls button.
        JButton controlsButton = null;
        icon = Icons.getImageIcon("options.png", 0);
        controlsButton = new JButton(icon);
        controlsButton.setPreferredSize(new Dimension(ICON+1, ICON+1));
        controlsButton.setMinimumSize  (new Dimension(ICON+1, ICON+1));
        controlsButton.setMaximumSize  (new Dimension(ICON+1, ICON+1));
        controlsButton.setToolTipText
                ("Configure Sound Recorder Advanced Controls");
        controlsButton.setBackground(BACKGROUND);
        controlsButton.addActionListener(
           new ActionListener()
           {  public void actionPerformed(ActionEvent ae)
              {
                 // Create option dialog panel.
                 OptionPanel options = new OptionPanel();

                 //options.setBackground(DictionaryDefaults.BACKGROUND);
                 Frame frame = JOptionPane.getRootFrame();
                 Component[] components = frame.getComponents();
                 for (int c=0; c<components.length; c++)
                 {  components[c].setBackground(BACKGROUND);  }
                 frame = JOptionPane.getFrameForComponent(thisPanel);
                 components = frame.getComponents();
                 for (int c=0; c<components.length; c++)
                 {  components[c].setBackground(BACKGROUND);  }
                 components = options.getComponents();
                 for (int c=0; c<components.length; c++)
                 {  components[c].setBackground(BACKGROUND);   }

                 // Display dialog and get user response.
                 String[] dialogOptions = {"Accept", "Cancel"};
                 String title = "ACORNS Dictionary: Advanced Sound Controls";

                 int result  = JOptionPane.showOptionDialog
                     (thisPanel, options, title
                     , JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE
                     , null, dialogOptions, dialogOptions[1]);

                 // If canceled, restore initial values.
                 if (result == 0)  // Index into dialog option array.
                 {    JOptionPane.showMessageDialog
                               (thisPanel, "Some Values were Illegal");
                 }
              }
           });

        // Add the components to the north panel.
        if (helpButton!=null)    north.add(helpButton);
        north.add(Box.createHorizontalGlue());
        if (AudioSystem.isLineSupported(Port.Info.SPEAKER))
        {  north.add(new JLabel("Speakers: "));
           north.add(volumeMute);
           north.add(Box.createRigidArea(new Dimension(15, 40)));
           north.add(new JLabel("Volume"));
           north.add(volume);
           north.add(Box.createRigidArea(new Dimension(30, 40)));
        }
        if (AudioSystem.isLineSupported(Port.Info.MICROPHONE))
        {
           north.add(new JLabel("Microphone: "));
           north.add(microphoneMute);        
           north.add(Box.createRigidArea(new Dimension(15, 40)));
           north.add(new JLabel("Volume"));
           north.add(Box.createRigidArea(new Dimension(5, 40)));
           north.add(microphone);
        }
        north.add(Box.createHorizontalGlue());
        if (controlsButton!=null) north.add(controlsButton);
        
        // Add the north portion to the main panel.
        add(north, BorderLayout.NORTH);
    }    
    
     
    /** Update speaker and microphone mute control.
     *  @param port Either Port.Info.SPEAKER or Port.Info.MICROPHONE
     *  @param on  true to set mute, false otherwise.
     *  @param update true to update, false otherwise.
     *  @return if update is true, return true if success
     *  @return if update is false, return current value
     */
   private boolean updateMute(Port.Info port, boolean on, boolean update)
   {   boolean result = true;
       String  controlName = "Mute";
       if (port.equals(Port.Info.MICROPHONE)) controlName = "Microphone Boost";
       
       if (AudioSystem.isLineSupported(port))
       {   
           Line line = null;
           try
           {
              line = (Port)AudioSystem.getLine(port);
              line.open();
              
             String controlType;
             CompoundControl compound;
             BooleanControl  mute = null;
             
             Control   member;
             Control[] members;
             Control[] controls = line.getControls();
             
             for (int i=0; i<controls.length; i++)
             {   member = controls[i];
                 if (controls[i] instanceof CompoundControl)
                 {  compound = (CompoundControl)controls[i];
                    members = compound.getMemberControls();
                    for (int m=0; m<members.length; m++)
                    {  member = members[m];
                       controlType = members[m].getType().toString();
                       if (controlType.equals("Select"))
                       { ((BooleanControl) member).setValue(true); 
                       }
                       if (controlType.equals(controlName)) { break;  } 
                    }  // End Inner loop.
                 }     // End if.
                
                 if (member.getType().toString().equals(controlName)) 
                 {  mute = (BooleanControl)member; 

                    // Update the mute control.
                    if (update) { mute.setValue(on); result = true; }
                    else        result = mute.getValue();
                 }
             }       // End outer loop.
           }
           catch (Exception e) {  result = false;  }
           finally { try {line.close();} catch (Exception e) {} }
       } 
       return result;
   }
    
    /** Access speaker and microphone volume controls.
     *  @param port Either Port.Info.SPEAKER or Port.Info.MICROPHONE
     *  @param ratio percentage of maximum to set (<0 to skip updating)
     *  @return a value of -1 if the call fails.
     */ 
    private float updateControls(Port.Info port, float ratio)
    { 
       Line line    = null;
       float result = -1;
       

       if (AudioSystem.isLineSupported(port))
       {
          try
          {  line = AudioSystem.getLine(port);
             line.open();
  
             String controlType;
             float max, min, vol, value, newValue;
             
             CompoundControl compound;
             FloatControl    scale = null;
             
             Control   member;
             Control[] members;
             Control[] controls = line.getControls();
             
             for (int i=0; i<controls.length; i++)
             {  member = controls[i];
                if (controls[i] instanceof CompoundControl)
                {  compound = (CompoundControl)controls[i];
                   members = compound.getMemberControls();
                   for (int m=0; m<members.length; m++)
                   {  member = members[m];
                      controlType = members[m].getType().toString();
                      if (controlType.equals("Select"))
                      { ((BooleanControl) member).setValue(true); 
                      }
                      if (controlType.equals("Volume")) { break;  } 
                   }  // End Inner loop.
                }     // End if.
                
                if (member.getType().toString().equals("Volume")) 
                {  scale = (FloatControl)member; 

                   // Update the volume control.
                   max      = scale.getMaximum();
                   min      = scale.getMinimum();
                   vol      = scale.getValue();
                   value    = (vol-min)/(max-min);
                   newValue = min + (max-min)*ratio;
               
                   // Prevent storing an illegal value.
                   if (ratio>0 && newValue>max && newValue<min) 
                   throw new NumberFormatException();

                   if (ratio<0) ratio = value;
                   else         newValue = min + (max-min)*ratio;
                 
                   if (newValue<0)   { result = vol; }
                   else                
                   { ((FloatControl) scale).setValue(newValue);
                     result = newValue;
                   }                 
                }
              }       // End outer loop.
          }
          catch (Exception e) { result = -1;}
          finally { try{line.close();} catch (Exception e) {}}
       } 
       return result;
    }    // End of updateControls.

}   // End of DictionaryPanels class
