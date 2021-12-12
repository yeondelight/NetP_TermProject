package data;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Audio {
	private Clip clip;
	//private boolean isloop;
	
	public Audio(String pathName, boolean isloop) {
		try {
			clip = AudioSystem.getClip();
			File audioFile = new File(pathName);
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);			
			clip.open(audioInputStream);
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void start(boolean isloop) {
		clip.setFramePosition(0);
		clip.start();
		if(isloop)
			clip.loop(Clip.LOOP_CONTINUOUSLY);
	}
	public int stop() {
		clip.stop();
		return clip.getFramePosition();
	}
	public void restart(int startpoint) {
		clip.setFramePosition(startpoint);
		clip.start();
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}
}

