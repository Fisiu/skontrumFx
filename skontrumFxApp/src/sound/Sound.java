package sound;

import javafx.scene.media.AudioClip;

public class Sound {

	private static Sound singleton = null;
	private static final String CATYELL = "catyell.wav";

	/**
	 * Private constructor which doesn't allow to create Sound object by other classes.
	 */
	private Sound() {
	}

	/**
	 * Returns singleton object
	 * 
	 * @return Sound class instance
	 */
	public static Sound getInstance() {
		if (singleton == null) {
			singleton = new Sound();
		}
		return singleton;
	}

	/**
	 * Get cat yelling sound
	 * 
	 * @return AudioClip ready to play
	 */
	public AudioClip getCatYelling() {
		AudioClip audioClip = new AudioClip(getClass().getResource(CATYELL).toString());
		return audioClip;
	}
}
