package org.stanwood.media.source.xbmc;

import java.util.regex.Pattern;

public class XBMCExpression {

	private Pattern pattern;
	private boolean clean = true;
	private boolean clear = false;
	
	public Pattern getPattern() {
		return pattern;
	}

	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	public boolean getClean() {
		return clean;
	}

	public void setClean(boolean clean) {
		this.clean = clean;
	}

	public boolean getClear() {
		return clear;
	}

	public void setClear(boolean clear) {
		this.clear = clear;
	}


		
	
}
