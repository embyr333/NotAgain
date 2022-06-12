/*  Objective: Program to check whether five letter word I type in Wordle web page
on a computer has been a solution already, BEFORE I press enter and potentially
waste the full value of a guess. Previouly, I searched a spreadsheet, but that 
disrupts my flow a little (first world problems). As program has to detect the 
'global' keystrokes i.e. while focus is off this program itself, it uses native
interface JNativeHook library. Runs in a GUI, with second popup window for alert.
Currently using first version JNativeHook (see my Word doc note on that) though
assume I could use a later version, but beware any change in behaviour. (I note 
that the JNativeHook library prints a copyright declaration to the console each run).
Expected limitations inc.: it cannot distinguish between keystrokes made in Wordle
website interface and those in other contexts, so user needs to pause program if 
using their computer for other input between Wordle guesses, or delete extra 
letters on returning.

General notes: 
- None of the "System.out.print..." statements are (now) needed for 
program function (user interacts with GUI), but have been left in place for the
moment in case useful to me for ongoing checking/troubleshooting.
- Changes from last version/iteration are usually flagged by prefixed "--"
to associated comment.

Possible features to add/modify, /alternative projects, inc.:
- Could try to make a modified version of the (HTML/JavaScript) web page that
as an alternative to this program. Advantage: it would only respond to keystrokes
made while Wordle in focus (user interacting with the page). Caveat: I'm wary of
accidentally resetting my Wordle cookies and losing 'stats' to date!

Did here:  
- Cleaned up some comments and disabled lines from previous iteration.   

============================================================================================================================
- 


Version/intermediate:   ====================================================================================================
 */

package embyr333.notagain; 


/* Static imports to avoid needing to prefix WordsAndWindows 
 * static member names with class names when accessing between classes
 * (not worth doing the reverse for KeyLoggerWordChecker class, though) */
import static embyr333.notagain.WordsAndWindows.processingKeystrokes;
import static embyr333.notagain.WordsAndWindows.dTextArea;
import static embyr333.notagain.WordsAndWindows.toAvoidWords;
import static embyr333.notagain.WordsAndWindows.repeatWordTextField;
import static embyr333.notagain.WordsAndWindows.frameForPopup;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.logging.Level;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
//import org.jnativehook.GlobalScreen;
//import org.jnativehook.NativeHookException;
//import org.jnativehook.keyboard.NativeKeyEvent;
//import org.jnativehook.keyboard.NativeKeyListener;
// -- Switching from use of  old version jnativehook.jar (old/first version)
//    dependency to jnativehook-2.2.2.jar (latest version), it seems the above 
//    file names (paths) need to be replaced by...
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class WordsAndWindows 
{
    /* Main GUI components... */
    
    private static JFrame frame = new JFrame("NotAgain"); // Contains all the other main GUI components 
    private static JPanel upperPanel = new JPanel(new BorderLayout()); // For buttons
    private static JPanel lowerPanel = new JPanel(new BorderLayout()); // For text areas   
    
    /* Variables to allow startButton transition to a Pause-Restart toggle
     * where Pause prevents key presses being processed until Restart engaged */
    private static int startButtonState = 0; 
    static boolean processingKeystrokes = true; 
    private static String sayPause = "Pause";    
    private static String sayRestart = "Restart";
    // (Not sure if these String fields contribute significantly to efficiency vs just
    // creating new String object each time where used below, but will keep for now)
    
    private static JButton startButton = new JButton("Start"); // Button to start keylogger-driven code
    
    /* Button and associated variables to allow use to choose whether
     * GUI stays on top of other windows */
    private static boolean whetherStaysOnTop = false; 
    private static String onTopButtonTextYes = "Keep on top of other windows";    
    private static String onTopButtonTextNo = "Allow overlay by other windows";    
    private static JButton onTopButton = new JButton(onTopButtonTextYes);     
         
    /* Text area and associated label to display current value of nascentWord in the logger code */
    private static JLabel dLabel = new JLabel("<html>" + // To explain the dTextArea below
            "If the letters in your Wordle input don't match those displayed below " + "<br>" +
            "(because you typed in another context at some point without pausing), " + "<br>" +
            "just delete and retype as needed while outside of the Wordle page. " + "</html>");
    static JTextArea dTextArea = new JTextArea(2, 20);  
    
    /* Coomponents for 'repeat warningTextField popup' window (previouly used a
     * JOptionPane but that allowed little custonisation of font and window sizes etc.)...  */
    static JTextField repeatWordTextField = new JTextField();
    static JTextField warningTextField = new JTextField("is a former Wordle solution!");
    static JButton okButton = new JButton("<html>" + 
            "<p style=\"font-size:17px; text-align: center;\">" +
            "OK" + "</p><br>" +
            "<p style=\"font-size:11px; text-align: center;\">" +
            "I have been warned" + "</p>" + "</html>"); 
    static JPanel okButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 150));     
    static JFrame frameForPopup = new JFrame();
    
    
    /* For accessing the text file resource containing the listing of Wordle solutions. */
    private static File toAvoidFile = new File("WordleSolutions.txt"); 
    
    
    /* Calculating the solution number/position in the listing... */
    
    /* Today's date (from computer clock) */
    private static LocalDate currentDate = LocalDate.now(); 
    
    /* Date representing the day when the first of the current solutions 
     * list would have appeared if there had been no changes to list */
    private static LocalDate startDate = LocalDate.of(2021, Month.JUNE, 18);
    
    private static long daysSinceStartDate = Duration.between
        (startDate.atStartOfDay(), currentDate.atStartOfDay()).toDays();
    
    
    /* Object into which the words will be read and stored for checking */
    static StringBuilder toAvoidWords = new StringBuilder();           

    
    /** Static block to set up the GUIs when class accessed
     * (...or maybe I could just transfer the contained code to main method
     *  or put it in a separate method */
    static 
    {     
        
        /* Main GUI... */
        
        startButton.addActionListener(
            new ActionListener() 
            { 
                public void actionPerformed(ActionEvent e) 
                {       
                    if (startButtonState == 0) 
                    {
                        KeyLoggerWordChecker.main(); // Start the keylogger to respond to user typing
                        startButton.setText(sayPause);
                        startButtonState = 1; // Next state will be Pause 
                    }
                    else if (startButtonState == 1) // Pause
                    {
                        startButton.setText(sayRestart);
                        processingKeystrokes = false; 
                        startButtonState = 2; // Next state will be Restart   
                        dTextArea.append("\n   ...processing paused...");     
                    }
                    else // Restart
                    {
                        startButton.setText(sayPause);
                        processingKeystrokes = true; 
                        startButtonState = 1; // Next state will be Pause again
                        // Remove the "...processing paused..." message...
                        String fullDisplay = dTextArea.getText();
                        dTextArea.setText(fullDisplay.substring(0, fullDisplay.length() - 27));   
                    } 
                } 
            } 
        );         
        startButton.setMargin(new Insets(30, 150, 30, 150));
        startButton.setFont(new Font("SansSerif", Font.BOLD, 30));         
        upperPanel.add(startButton, BorderLayout.NORTH);                

        onTopButton.addActionListener(  
            new ActionListener() 
            { 
                public void actionPerformed(ActionEvent e) 
                {             
                    whetherStaysOnTop = !whetherStaysOnTop; // Reverse value when button clicked
                    frame.setAlwaysOnTop(whetherStaysOnTop); // Reset GUI behaviour based on new value
                    // Toggle button text to describe what will happen when clicked
                    onTopButton.setText(whetherStaysOnTop ? onTopButtonTextNo : onTopButtonTextYes);
                } 
            } 
        );          
        upperPanel.add(onTopButton, BorderLayout.SOUTH);           

        dLabel.setBorder(new EmptyBorder(10, 10, 10, 0)); 
        dLabel.setFont(new Font("SansSerif", Font.PLAIN, 15)); 
        lowerPanel.add(dLabel, BorderLayout.NORTH);

        dTextArea.setEditable(false);
        dTextArea.setFont(new Font("SansSerif", Font.BOLD, 30)); 
        dTextArea.setLineWrap(true); // (Probably irrelevant, but will leave here for the moment)
        dTextArea.setMargin(new Insets(5, 5, 5, 5)); 
        lowerPanel.add(dTextArea, BorderLayout.SOUTH);

        frame.add(upperPanel, BorderLayout.NORTH); 
        frame.add(lowerPanel, BorderLayout.SOUTH); 
        frame.setResizable(false);
        frame.setVisible(true);       
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);    
        frame.pack(); 

        
        /* 'Repeat warning' popup window... */
                            
        repeatWordTextField.setEditable(false);                            
        repeatWordTextField.setFont(new Font("SansSerif", Font.BOLD, 30)); 
        repeatWordTextField.setHorizontalAlignment(JTextField.CENTER); 
        repeatWordTextField.setBorder(BorderFactory.createEmptyBorder(50, 250, 0, 250)); 
        repeatWordTextField.setBackground(Color.YELLOW); 
        
        warningTextField.setEditable(false);
        warningTextField.setFont(new Font("SansSerif", Font.BOLD, 30)); 
        warningTextField.setBorder(BorderFactory.createEmptyBorder(50, 250, 50, 250)); 
        warningTextField.setBackground(Color.YELLOW); 

        okButton.addActionListener(  
            new ActionListener() 
            { 
                public void actionPerformed(ActionEvent e) 
                {             
                    frameForPopup.setVisible(false);
                } 
            } 
        ); // Of course, user could just dismiss popup by clciking upper-right 'X'
        // but want to mimic usual type dialog box behaviour
                   
        okButtonPanel.add(okButton); // (Putting button in panel allows it to 
        // avoid expanding to full width frame under border layout)        
        okButtonPanel.setBackground(Color.YELLOW); 
    
        frameForPopup.setAlwaysOnTop(true); // Make sure popup is not hidden under other 
        // windows, including the Wordle browser window in which the user is typing        
        frameForPopup.add(repeatWordTextField, BorderLayout.NORTH); 
        frameForPopup.add(warningTextField, BorderLayout.CENTER); 
        frameForPopup.add(okButtonPanel, BorderLayout.SOUTH); 
        frameForPopup.setResizable(false);
        frameForPopup.setVisible(false); // Want NOT visible here, until called from KeyLogger
//        frameForPopup.setUndecorated(true); // (--could add this to remove the title bar,
//        // so that only the 'OK' button can close the indow, but that might orry some uers)
        frameForPopup.pack(); 
        frameForPopup.setLocationRelativeTo(null); // Place at centre of screen
        // (...need to this location statement after pack statement for correct operation)        
        
             
        // Prevents pressing spacebar key from acting as a click on GUI buttons
        InputMap im = (InputMap)UIManager.get("Button.focusInputMap");
        im.put(KeyStroke.getKeyStroke("pressed SPACE"), "none");
        im.put(KeyStroke.getKeyStroke("released SPACE"), "none");   
        // Thanks to https://stackoverflow.com/questions/4472530/disabling-space-bar-triggering-click-for-jbutton
    }
    
    public static void main(String[] args)
    {
        /* Set toAvoidWordsAsString to content of the external text file... */
        try // 
        {
            if(toAvoidFile.exists())     
            {
                BufferedReader bfr = new BufferedReader(new FileReader(toAvoidFile)); 
                int charInput = 0;
                int commaCount = 0; 
                
                // Temporary verification of way of calculating the position 
                // of today's repeatWordTextField in the solution list...
                System.out.println("Back-strapolated Wordle start date: " + startDate); 
                System.out.println("Days since Wordle start date: " + daysSinceStartDate);
                // E.g. output on 8May2022 was 324, as expected, and Wordle #320
                // 4 days before that was TRAIN

//                while ((charInput = bfr.read()) != -1 && commaCount < 315) 
                // Temporarily testing limiting to words up to and including 
                // penultimate repeatWordTextField of April 2022, TRASH: OK 
                    
                while ((charInput = bfr.read()) != -1 && commaCount < daysSinceStartDate - 1) 
                // Decided to leave the first condition in place so that the program 
                // can still use partial solution list text files, if we need to revert
                // to using such with manual update of ongoing solutions if NYT keeps 
                // messing with solutions list. (Have tested that this works. The comma-counting 
                // logic would not then be needed, but program is still likely to remain 
                // fast enough to avoid need for reversion to ‘read all’ configuration.)    
                { 
                    char character = (char) charInput; 
                    
                    if (character == ',') 
                    {
                        ++commaCount; 
                    }

                    toAvoidWords.append(character);
                }
            }  
        } 
        catch (IOException ex) 
        {                      
            ex.printStackTrace();
        }       
        System.out.println("\nTemporary check that the word list has been acquired: \n"
        + toAvoidWords); // Confirmed               
        
        System.out.println("Press Start button on GUI to begin keylogger..."); 
    }
}

/* Keylogger implementation in terms of ability to respond to key presses modified from
 * https://github.com/vakho10/java-keylogger/blob/master/src/main/java/ge/vakho/KeyLogger.java
 * hope OK to do this (can't see a License declaration; thought I saw them quote
   snippet in StackOverflow or similar site but cannot now find that) - thanks to author! 
 * Thanks also to JNativeHook author, whose binary this program uses (dependency) */
class KeyLoggerWordChecker implements NativeKeyListener 
{       
    /* Object To keep track of current letters in the Wordle 5-letter entry field */
    private static StringBuilder nascentWord = new StringBuilder(); 
    
    /* Field to allow delay below in clearing nascentWord after Enter key pressed,
     * in case user needs to Backspace some letters making a guess not in Wordle.
     * May come up with a more concise way to arrange the decision series that
     * uses this field below in the future, but seems to work as intended so far. */
    private static boolean enterWasLastKeyPressed = false;  

    public static void main() 
    {       
        System.out.println("Key logger has been started");
        init();

        try
        {
            GlobalScreen.registerNativeHook();
        } 
        catch (NativeHookException e)
        {
            System.out.println(e);
            System.exit(-1);
        }

        GlobalScreen.addNativeKeyListener(new KeyLoggerWordChecker());
    }

    private static void init()
    {
        // Get the logger for "org.jnativehook" and set the level to warning.
        java.util.logging.Logger logger = java.util.logging.Logger
                .getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);

        // Don't forget to disable the parent handlers.
        logger.setUseParentHandlers(false);
    }

    public void nativeKeyPressed(NativeKeyEvent e) // Runs each time a key pressed
    {
        if (processingKeystrokes) // Condition to allow Pause/Restart
        {
            String keyText = NativeKeyEvent.getKeyText(e.getKeyCode());

            if (keyText.equals("Enter") && nascentWord.length() == 5)
            {
                if (enterWasLastKeyPressed == false) // Restricts to first time 
                { // Enter presed before a reset, in case user preses multile times
                    System.out.println("Wordle guess entered!"); // (Not seen by user)
//                    WordsAndWindows.dTextArea.append("    entered");  
                    dTextArea.append("    entered");   
                }
                enterWasLastKeyPressed = true;   
            }

            else if (keyText.equals("Backspace")) // Backspace key will delete a Wordle letter
            { 
                if (nascentWord.length() > 0) 
                {   // Update nascent repeatWordTextField by...
                    nascentWord.deleteCharAt((nascentWord.length() -1)); // ...deleting last letter                
                    dTextArea.setText(nascentWord.toString()); // Then display to user
                    System.out.println("Previous letter deleted!");                 
                    enterWasLastKeyPressed = false; // Reset
                }
            }      
            // May replace above arrangement with following more concise/readable statement
            // if I consistently observe it to behave the same (had an unexpected 
            // difference in bug severity when working on intermediate 220502_2345)...

            // If a letter key is pressed             
            else if (keyText.length() == 1 && Character.isLetter(keyText.charAt(0)))             
            { 
                if (enterWasLastKeyPressed == true) // (Not in the process of editing
                { // the letters in a rejected Wordle guess via Backspace)
                    nascentWord.setLength(0); // Reset (clear nascentWord)
                    enterWasLastKeyPressed = false; // Reset
                }

                if (nascentWord.length() < 5) // Do not want to add any letters typed 
                { // after 5, as Wordle state would not change (until Enter pressed)
                    System.out.print(keyText); 
                    nascentWord.append(keyText); // Update nascentWord...
                    dTextArea.setText(nascentWord.toString()); // ...and display to user
                    System.out.print("    # letters in Wordle: " + nascentWord.length());
                    System.out.print("    being: " + nascentWord + "\n"); 

                    if (nascentWord.length() == 5) // (Avoid inefficiency of searching before relevant)
                    {   
                        // Alert user if a 5-letter repeatWordTextField is present that has been 
                        // a previous Wordle solution (before they enter it as a guesss)
                        if (toAvoidWords.toString().contains(nascentWord.toString().toLowerCase()))
                        { // (Words in external file happen to be lowercase while keylohgger returns uppercase letters)
                            System.out.println("Alert: this is a previous Wordle solution!");

                            repeatWordTextField.setText(nascentWord.toString());  
                            frameForPopup.setVisible(true);
                        }          
                        else // Confirm to user in main display area that repeatWordTextField is not
                        { // previous Wordle solution when popup alert does not appear
                            dTextArea.append("    :) not a repeat :)");
                        }                                              
                    }     
                } 
            } 
        }
    }

    /* ('Empty' implementations to satisfy NativeKeyListener contract, not needed to do anything here */
    public void nativeKeyReleased(NativeKeyEvent e) { }
    public void nativeKeyTyped(NativeKeyEvent e) { }
}
