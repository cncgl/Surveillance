package jp.cogel.camera;

import java.awt.image.BufferedImage;
import java.io.*;
import java.io.IOException;

import java.util.Calendar;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import java.text.*;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;


/**
 * Example of how to take single picture.
 *
 * @author Bartosz Firyn (SarXos)
 */
public class Capture extends TimerTask {
	private static final String PROPERTIES_FILENAME = "capture.properties";
	private static final Format FILEDATE_FORMAT = new SimpleDateFormat("YYYYMMddHHmmss");
	public Properties prop = new Properties();

	private Webcam webcam = Webcam.getDefault();

	public Capture() {
		try {
			loadProerties();
		} catch(Exception ex) {
			System.out.println(PROPERTIES_FILENAME + " not found.");
		}

		webcam.open();
	}

	private void loadProerties() throws Exception {
		InputStream in = null;
		try {
			in = new BufferedInputStream( new FileInputStream(PROPERTIES_FILENAME) );
			prop.load(in);

			System.out.println(prop);
		} catch(IOException ex) {
			throw new Exception(ex.getMessage());
		} finally {
			try {
				if(in != null) {
					in.close();
				}
			} catch(IOException ex2) {
				System.err.println(ex2.getMessage());
				ex2.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		try {
			// get image
			BufferedImage image = webcam.getImage();

			// save image to PNG file
			String filename = prop.getProperty("image.local.filename");
			int i = filename.indexOf(".");
			String base = filename.substring(0,i);
			String ext  = filename.substring(i+1);
			System.out.println("base:"+base+",ext:"+ext);
			ImageIO.write(image, "JPEG", new File( base+"-"+FILEDATE_FORMAT.format(Calendar.getInstance().getTime())+"."+ext ));
		} catch (IOException ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		Timer t = new Timer();
		Capture capture = new Capture();
		long interval = Long.parseLong( capture.prop.getProperty("ftp.interval") );
		t.schedule( capture, 0 , interval );
	}
}
