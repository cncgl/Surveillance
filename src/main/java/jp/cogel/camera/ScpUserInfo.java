package jp.cogel.camera;

import java.util.Properties;
import com.jcraft.jsch.UserInfo;


public class ScpUserInfo implements UserInfo {
  private Properties prop;

  // Constructor
  //------------------------------------------------
  public ScpUserInfo(Properties prop) {
    this.prop = prop;
  }

  // Overrude
  //------------------------------------------------
  @Override
  public String getPassword() {
    return prop.getProperty("ftp.password");
  }
  @Override
  public boolean promptYesNo(String message) {
    System.out.println("promptYesNo:"+message);
    return true;
  }
  @Override
  public String getPassphrase() {
    return prop.getProperty("ftp.passphase");
  }
  @Override
  public boolean promptPassphrase(String message) {
    System.out.println("promptPassphrase:"+message);
    return true;
  }
  @Override
  public boolean promptPassword(String message) {
    System.out.println("promptPassword:"+message);
    return true;
  }
  @Override
  public void showMessage(String message) {
    System.out.println("showMessage:"+message);
  }
}
