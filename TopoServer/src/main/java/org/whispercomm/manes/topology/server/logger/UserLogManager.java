package org.whispercomm.manes.topology.server.logger;

import java.io.IOException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class UserLogManager {
	int user_id;
	Logger user;
	PatternLayout layout;
	FileAppender  user_file;
	final static Logger logger = Logger.getLogger(UserLogManager.class);

	public UserLogManager(int user_id, String s) {
		this.user_id = user_id;
		user = Logger.getLogger("user-traces." + user_id + s);
	}

	public UserLogManager(int user_id, String logname, String appname,
			String layoutstr, boolean additivity) {
		this.user_id = user_id;
		user = Logger.getLogger("user-traces." + user_id + logname);
		setLayout(layoutstr);
		addAppender("user-traces/" + user_id + appname);
		setAdditivity(additivity);
	}

	public void addAppender(String s) {
		if (user.getAppender(s) == null) {
			try {
				user_file = new FileAppender(layout, s);
				user.addAppender(user_file);
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	public void setLayout(String s) {
		layout = new PatternLayout();
		layout.setConversionPattern(s);
	}

	public void removeAppender(String s) {
		user.removeAppender(s);
	}

	public void info(Object s) {
		user.info(s);
	}

	public void debug(Object s) {
		user.debug(s);
	}

	public void warn(Object s) {
		user.warn(s);
	}

	public void error(Object s) {
		user.error(s);
	}

	public void setAdditivity(boolean b) {
		user.setAdditivity(b);
	}
	
	public void close(){
		user.removeAppender(user_file);
	}
}
