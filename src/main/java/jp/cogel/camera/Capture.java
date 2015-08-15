package jp.cogel.camera;

import java.awt.image.BufferedImage;
import java.io.*;
import java.io.IOException;

import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;


/**
 * Example of how to take single picture.
 *
 * @author Bartosz Firyn (SarXos)
 */
public class Capture extends TimerTask {
	private static final String PROPERTIES_FILENAME = "capture.properties";
	private Properties prop = new Properties();

	public Capture() {
		InputStream in = null;
		try {
			in = new BufferedInputStream( new FileInputStream(PROPERTIES_FILENAME) );
			prop.load(in);

			System.out.println(prop);
		}catch(IOException ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace();
		}finally {
			try {
				if(in != null) {
					in.close();
				}
			}catch(IOException ex2) {
				System.err.println(ex2.getMessage());
				ex2.printStackTrace();
			}
		}
	}

	@Override
	public void run() {

	}

	public static void main(String[] args) throws IOException {
		Capture capture = new Capture();


		// get default webcam and open it
		Webcam webcam = Webcam.getDefault();
		webcam.open();

		// get image
		BufferedImage image = webcam.getImage();

		// save image to PNG file
		ImageIO.write(image, "PNG", new File("test.png"));
	}
}
