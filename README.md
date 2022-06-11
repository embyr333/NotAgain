<img src="https://repository-images.githubusercontent.com/501008140/0ed3fb4f-c624-4dde-b0fc-471c742e0e24" width="150">

# NotAgain  

Java program to alert Wordle players if they're about to waste a guess on a previous solution  

Dependencies (in addition to JDK 8): https://github.com/kwhat/jnativehook  
Note that I have been using what is probably the first release, JNativeHook.jar (no version number), because I initially failed to find the equivalent file for the the latest version, and I cannot now find the link to download that(!) However I have now checked that the latest release (jnativehook-2.2.2.jar) seems to work after updating import statements, and will switch to that later if I do not encounter any issues after further testing.

Resources: WordleSolutions.txt

NB: This is my first upload to GitHub (made an account late May 2022), and consists of just the latest version of the code for this program (re-uploaded now as I was unsure how the addition of a package structure would be handled by git). I'm still trying to get a better idea of what files should be uploaded, how to indicate dependencies, and how to make 'releases' (I may learn to use a build tool such as Maven later). On my website, embyrne.c1.biz, I link to a zipped folder containing the three items needed for execution as understandable by someone like me (JARs/bytecodes of NotAgain & JNativeHook, WordleSolutions.txt) and also a folder with previous iterations/versions of NotAgain which were made without version control and are probably not worth retrofitting into a repo. There are also other small programs on the site, some of which I may upload to GitHub later.

Back to NotAgain:

Your finger is poised above the Enter key. A nagging sense of familiarity. Yes - definitely a recent one. Why would you waste the full value of a Wordle guess on a previous solution (unless desperate)? Backspace! Over days and months the scenario recurs, but each time you're less certain. It haunts you. So you make a list of previous Wordles (cheaty? no! why should you memorise them? it's not a memory game! :D Check against it each time, by eyeball, then by searching as it lengthens...Ctrl+F, typing, Enter...each doggone time. It breaks the flow :(

So here's a small program, which can run on Java-enabled computers[1], that pops up an alert if you type a five-letter word that has been a previous Wordle solution[2], before you press Enter[3]. Backspace removes letters one-at-a-time just like for the Wordle entry field. After each entry (if there hasn't been a backspace, as might happen if you enter a word rejected by Wordle and need to re-type), it resets and processes the next set of keystrokes.

Footnotes (re limitations, known issues etc.):  
[1] "But I play on a phone!", I hear you protest. That's on my radar, though I need to investigate, a lot (...perhaps a web-based solution could cover all platforms?).    
[2a] The program is using the latest version of the Wordle solutions list that I know about (retrieved on 10 May 2022. However if the New York Times makes yet more tweaks in the future, the file will have to be updated again to guarentee accuracy of NotAgain behaviour, or else we could revert to using a partial list again (might be able to leave file-reading code as-is as it should still stop at end file I think)).  
[2b] Note that if you have left the Wordle browser open across a 'day transition' (through midnight) to continue working on what is now 'yesterday's puzzle', closing and reopening NotAgain will load that solution and warn it's a previous solution...so best to keep the browser page and program in sync!
[3] At least, if you wait for the alternative message, ":) not a repeat :)" to appear in the main display area after the word. This will happen instantly, unless there is a delay in processing, in which case it would be best to wait until one or other message appears.  
[4] Latest version (220610_1257) has some non-performance-related changes to code organisation (java file placed in a non-default package and  some other naming changes applied.  
[5] Since a keylogger is involved, you may get a warning from your system about malware (as I did); there is nothing malicious involved, but I realise this may put off many potential users.  
[6] Note that the program cannot distinguish between keystrokes made in the Wordle website interface and those in any other contexts, or independantly detect which letters are present there. Therefore, if using your computer for other input (e.g. another browser tab, a text document) between Wordle guesses you can get a mismatch between Wordle and NotAgain display. In that case, just backspace and re-type as necessary to realign. Alternatively, use the Pause feature before leaving Wordle, then Restart when returning. (Alao, in the unlikely event that you press backspace after entering a word accepted by Wordle, NotAgain will delete from the existing word in its display rather than starting a new word, so backspace all the way to clear letters to sync with Wordle.) If I find a way around these limitations I will re-code; again, a web-based alternative might help...perhaps making an altered version of the Wordle page that could be played offline-only.
