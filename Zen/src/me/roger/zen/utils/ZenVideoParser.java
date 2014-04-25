package me.roger.zen.utils;

public class ZenVideoParser {
	private static final String ZenYoukuURL = "http://v.youku.com/v_show/id_%s.html";
	private static final String ZenTudouURL = "http://www.tudou.com/programs/view/%s";
	private static final String Zen56URL = "http://www.56.com/u/ipad-%s.html";
	private static final String ZenKu6URL = "http://v.ku6.com/show/%s.html";
	private static final String ZenTecentURL = "http://play.v.qq.com/play?vid=%s";
	private static final String ZenPPSURL = "http://v.pps.tv/play_%s.html";
	private static final String ZenQiYiURL = "http://www.iqiyi.com/v_%s.html";
	
	public static String parser(String url) {
		try {
			if(url == null || !(url instanceof String)) {
				return null;
			} 
			
			if(url.contains("tudou")) {
				String vid = ZenVideoParser.sub("v/", "/", url);
				if (vid == null) {
					vid = ZenVideoParser.sub("v/", null, url);
				}
				if (vid != null) {
					return String.format(ZenTudouURL, vid);
				}
			}
			else if(url.contains("youku")) {
				String vid = ZenVideoParser.sub("VideoIDS=", "&", url);
				if (vid == null) {
					vid = ZenVideoParser.sub("sid/", "/", url);
				}
				if (vid != null) {
					return String.format(ZenYoukuURL, vid);
				}
			}
			else if(url.contains("56.com")){
				String vid = ZenVideoParser.sub("_", ".swf", url);
				if (vid != null) {
					return String.format(Zen56URL, vid);
				}
			}
			else if(url.contains("ku6.com")){
				String vid = ZenVideoParser.sub("refer/", "/", url);
				if (vid != null) {
					return String.format(ZenKu6URL, vid);
				}
			}
			else if(url.contains("qq.com")){
				String vid = ZenVideoParser.sub("vid=", null, url);
				if (vid != null) {
					return String.format(ZenTecentURL, vid);
				}
			}
			else if(url.contains(".pps")){
				String vid = ZenVideoParser.sub("sid=", "/", url);
				if (vid != null && vid.length() > 6) {
					vid = vid.substring(0, 6);
					return String.format(ZenPPSURL, vid);
				}
			}
			else if(url.contains("qiyi")){
				String vid = ZenVideoParser.sub("v_", ".swf", url);
				if (vid != null) {
					return String.format(ZenQiYiURL, vid);
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	} 
	
	// utils
	private static String sub(String begin, String end, String src) throws Exception {
		int indexBegin = src.indexOf(begin);
		if (indexBegin != -1) {
			String sub = src.substring(indexBegin + begin.length());
			if (end == null) {
				return sub;
			}
			int indexEnd = sub.indexOf(end);
			if (indexEnd != -1) {
				return sub.substring(0, indexEnd);
			}
		}
		return null;
	}
}
