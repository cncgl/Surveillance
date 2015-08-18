package jp.cogel.camera;

import java.awt.image.BufferedImage;
import java.awt.Dimension;
import java.io.*;
import java.io.IOException;

import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import java.text.*;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;
import com.jcraft.jsch.*;


/**
 * Example of how to take single picture.
 *
 * @author Bartosz Firyn (SarXos)
 */
public class Capture extends TimerTask {
	private static final String PROPERTIES_FILENAME = "capture.properties";
	private static final Format FILEDATE_FORMAT = new SimpleDateFormat("YYYYMMddHHmmss");
	private static final Format LOG_FORMAT = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
	public Properties prop = new Properties();

	private static Webcam   webcam  = null;
	private JSch     jsch    = null;
	private Session  session = null;
	private UserInfo info    = null;

	public Capture() throws WebcamException {
		try {
			loadProerties();
			info = new ScpUserInfo(prop);
		} catch(Exception ex) {
			System.out.println(PROPERTIES_FILENAME + " not found.");
		}

		webcam = Webcam.getDefault();
		if( webcam == null) {
			throw new WebcamException("Webcam not ready.");
		} else {
			webcam.setViewSize(new Dimension(640, 480));
			webcam.open();

			jsch = new JSch();
		}
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
			webcam.open();
			// get image
			BufferedImage image = webcam.getImage();

			// save image to PNG file
			String filename = prop.getProperty("image.local.filename");
			int i = filename.indexOf(".");
			String base = filename.substring(0,i);
			String ext  = filename.substring(i+1);
			//System.out.println("base:"+base+",ext:"+ext);
			String type = "";
			if("jpg".equals(ext.toLowerCase()) || "jpeg".equals(ext.toLowerCase())) {
				type = "JPEG";
			} else if("png".equals(ext.toLowerCase())) {
				type = "PNG";
			} else if("bmp".equals(ext.toLowerCase())) {
				type = "BMP";
			} else if("gif".equals(ext.toLowerCase())) {
				type = "GIF";
			} else if("wbmp".equals(ext.toLowerCase())) {
				type = "WBMP";
			} else {
				System.out.println(ext + " is not supported.");
				System.exit(0);
			}
			Date now = Calendar.getInstance().getTime();
			String lfile = base + "-" + FILEDATE_FORMAT.format(now) + "." + ext;
			System.out.printf("filename:%s lfile:%s ext:%s type:%s\n", filename, lfile, ext, type);
			ImageIO.write(image, type, new File(lfile));
			System.out.println("DONE: "+LOG_FORMAT.format(now));

			webcam.close();

			// send to SCP host
			String rfile = prop.getProperty("image.remote.filename");
			sendToScp(lfile, rfile);


		} catch (IOException ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace();
		} catch (JSchException ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace();
		}
	}

	private void sendToScp( String lfile, String rfile ) throws IOException, JSchException {
		if( session == null ) {
			String user = prop.getProperty("ftp.user");
			String host = prop.getProperty("ftp.host");
			int    port = Integer.parseInt(prop.getProperty("ftp.port"));
			session = jsch.getSession(user, host, port);
			session.setUserInfo(info);
		}
		if( !session.isConnected() ) {
			session.connect();
		}
		boolean ptimestamp = true;

		String command="scp " + (ptimestamp ? "-p" :"") +" -t "+rfile;
		Channel channel=session.openChannel("exec");
		((ChannelExec)channel).setCommand(command);

		OutputStream out=channel.getOutputStream();
		InputStream in=channel.getInputStream();

		channel.connect();

		// 死活チェック
		//  System.exit(0);

		File _lfile = new File(lfile);

		if(ptimestamp){
			command="T "+(_lfile.lastModified()/1000)+" 0";
			// The access time should be sent here,
			// but it is not accessible with JavaAPI ;-<
			command+=(" "+(_lfile.lastModified()/1000)+" 0\n");
			out.write(command.getBytes()); out.flush();

			// 死活チェック
			//if(checkAck(in)!=0){
			//   System.exit(0);
			//}
		}

		long filesize=_lfile.length();
		command = "C0644 " + filesize + " ";
		if(lfile.lastIndexOf('/')>0){
			command+=lfile.substring(lfile.lastIndexOf('/')+1);
		} else {
			command+=lfile;
		}
		command += "\n";
		out.write(command.getBytes()); out.flush();

		// 死活チェック
		//if(checkAck(in)!=0){
		//   System.exit(0);
		//}

		FileInputStream fis=new FileInputStream(lfile);
		byte[] buf = new byte[1024];
		while(true){
			int len = fis.read(buf, 0, buf.length);
			if(len<=0) break;
			out.write(buf, 0, len); //out.flush();
		}
		fis.close();
		fis=null;
		// send '\0'
		buf[0]=0; out.write(buf, 0, 1); out.flush();

		// 死活チェック
		//if(checkAck(in)!=0){
		//	System.exit(0);
		//}
		out.close();

		channel.disconnect();
		//session.disconnect();
	}

	public static void main(String[] args) throws IOException {
		Timer t = new Timer();
		try {
			Capture capture = new Capture();
			long interval = Long.parseLong( capture.prop.getProperty("ftp.interval") );
			t.schedule( capture, 0 , interval );
		} catch(WebcamException ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace();
			System.exit(1);
		}
		if( webcam!=null ) {
			webcam.close();
		}

	}
}
