/**
 * 
 */
package apu.scratch.tools.gen.followers;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import apu.scratch.tools.ParamDef;
import apu.scratch.tools.ParamType;
import apu.scratch.tools.ToolBase;
import at.dhyan.open_imaging.GifDecoder;
import at.dhyan.open_imaging.GifDecoder.GifImage;

/**
 * @author MegaApuTurkUltra
 * 
 */
public class FollowerListGen extends ToolBase {

	public static class NullOutputStream extends OutputStream {
		@Override
		public void write(int b) throws IOException {
		}
	}

	static class Follower {
		public String name;
		public String about;
		public String location;
		public BufferedImage avatar;
		public File cache;
		public String avatarURL;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((about == null) ? 0 : about.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof Follower))
				return false;
			Follower other = (Follower) obj;
			if (about == null) {
				if (other.about != null)
					return false;
			} else if (!about.equals(other.about))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
	}

	// I'm actually not sure why this is required. If it's not there,
	// Scratch gives an IP ban page :O
	public static final String REQUIRED_HEADERS = "Host: cdn2.scratch.mit.edu\n"
			+ "Connection: keep-alive\n"
			+ "Cache-Control: max-age=0\n"
			+ "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\n"
			+ "User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.93 Safari/537.36\n"
			+ "Accept-Encoding: gzip, deflate, sdch\n"
			+ "Accept-Language: en-US,en;q=0.8,hu;q=0.6";

	static Set<Follower> all = Collections
			.synchronizedSet(new HashSet<Follower>());
	static Set<Follower> complete = Collections
			.synchronizedSet(new HashSet<Follower>());

	static Executor pool = Executors.newFixedThreadPool(8);
	static String urlTemplate1 = "https://scratch.mit.edu/users/";
	static String urlTemplate2 = "/followers/?page=";
	static String[] urls;
	static String userPage = "https://scratch.mit.edu/users/";

	static boolean FORCE_NO_CACHE = false;

	static RequestConfig globalConfig = RequestConfig.custom()
			.setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY)
			.setSocketTimeout(0).setConnectionRequestTimeout(0)
			.setConnectTimeout(0).build();

	private static void listFollowers(String url) throws Exception {
		Document doc = Jsoup.connect(url).get();
		Elements followers = doc.getElementsByClass("user");
		System.err.println("Got " + followers.size() + " followers");
		for (int i = 0; i < followers.size(); i++) {
			Element follower = followers.get(i);
			Follower item = new Follower();
			all.add(item);
			String name = follower.getElementsByTag("a").get(0).attr("href");
			item.name = name.substring(7, name.length() - 1);
		}
	}

	private static void getFollowerInfo(Follower follower) throws Exception {
		System.out.println("Getting info on: " + follower.name);
		String url = userPage + follower.name;

		File infoCache = new File("cache/" + follower.name + ".cache");
		if (!infoCache.exists() && !FORCE_NO_CACHE) {
			Document doc = Jsoup.connect(url).get();
			follower.location = doc.getElementsByClass("location").get(0)
					.text();
			follower.about = doc.getElementsByClass("overview").get(0).text();
			follower.avatarURL = "http:"
					+ doc.getElementsByClass("avatar").get(0)
							.getElementsByTag("img").get(0).attr("src")
							.replace("60x60", "200x200");
			JSONObject info = new JSONObject();
			info.put("location", follower.location);
			info.put("about", follower.about);
			info.put(
					"avatarURL",
					follower.avatarURL.substring(0,
							follower.avatarURL.lastIndexOf("?")));
			FileOutputStream infoOut = new FileOutputStream(infoCache);
			infoOut.write(info.toString().getBytes());
			infoOut.close();
		} else {
			StringBuilder infoStr = new StringBuilder();
			FileReader infoIn = new FileReader(infoCache);
			char[] c = new char[1024];
			int len;
			while ((len = infoIn.read(c)) != -1) {
				infoStr.append(c, 0, len);
			}
			infoIn.close();
			JSONObject info = new JSONObject(infoStr.toString());
			follower.location = info.getString("location");
			follower.about = info.getString("about");
			follower.avatarURL = info.getString("avatarURL");
		}

		complete.add(follower);

		System.out.println("\tGetting avatar: " + follower.name);
		RequestBuilder avatarBuilder = RequestBuilder.get().setUri(
				follower.avatarURL);
		for (String header : REQUIRED_HEADERS.split("\n")) {
			String[] split = header.split(": ");
			avatarBuilder.addHeader(split[0], split[1]);
		}
		CloseableHttpClient httpClient = HttpClients
				.custom()
				.setDefaultRequestConfig(globalConfig)
				.setUserAgent(
						"Mozilla/5.0 (Windows NT 6.1; WOW64)"
								+ " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.111 Safari/"
								+ "537.36").build();

		File cached = new File("cache/" + follower.name + ".avatar.cache.png");

		if (!cached.exists() && !FORCE_NO_CACHE) {
			FileOutputStream cacheOut = new FileOutputStream(cached);

			CloseableHttpResponse resp = httpClient.execute(avatarBuilder
					.build());

			byte[] b = new byte[1024 * 16]; // 16K
			int len;
			InputStream in = resp.getEntity().getContent();
			while ((len = in.read(b)) != -1) {
				cacheOut.write(b, 0, len);
			}
			in.close();
			resp.close();
			cacheOut.close();
		}
		follower.cache = cached;
	}

	private static void decodeAndResizeImage(Follower follower)
			throws Exception {
		File cached = follower.cache;

		try (FileInputStream fIn0 = new FileInputStream(cached)) {
			BufferedImage image = ImageIO.read(fIn0);
			follower.avatar = image;
			if (follower.avatar == null)
				throw new Exception();
		} catch (Exception e) {
			System.err.println("Using custom GIF decoder: " + follower.name);
			GifImage image = null;
			try (FileInputStream fIn = new FileInputStream(cached)) {
				image = GifDecoder.read(fIn);
				follower.avatar = image.getFrame(0);
			}
		}
		if (follower.avatar == null) {
			System.err.println("null - " + follower.name);
			follower.avatar = createDummyAvatar();
		} else {
			BufferedImage target = new BufferedImage(200, 200,
					follower.avatar.getType());
			Graphics g = target.getGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, 200, 200);
			g.drawImage(follower.avatar, 0, 0, 200, 200, 0, 0,
					follower.avatar.getWidth(), follower.avatar.getHeight(),
					null);
			follower.avatar = target;
		}
	}

	private static BufferedImage createDummyAvatar() {
		// try-with-resources autocloses the inputstream, saving me some lines
		// of code :P
		try (InputStream in = FollowerListGen.class
				.getResourceAsStream("/res/dummy.png")) {
			return ImageIO.read(in);
		} catch (Exception e) {
			return null;
		}
	}

	static void getFollowerUrls(String username) throws Exception {
		System.out.println("Looking for follower pages...");
		String url = urlTemplate1 + username + urlTemplate2 + "1";
		Document doc = Jsoup.connect(url).get();
		Elements pageLinks = doc.getElementsByClass("page-current");
		Element last = pageLinks.last();

		if (last == null) {
			urls = new String[1];
			urls[0] = url;
			return;
		}

		String text = last.text();
		System.out.println("Found " + text + " pages of followers");
		int numPages = Integer.parseInt(text);
		urls = new String[numPages];
		for (int i = 0; i < numPages; i++) {
			urls[i] = urlTemplate1 + username + urlTemplate2 + (i + 1);
		}
	}

	public void toolMain(String[] args) throws Exception {
		String username = args[0], filename = args[1];

		new File("cache/").mkdirs();

		getFollowerUrls(username);

		try {
			for (String url : urls) {
				System.out.println("Getting page " + url);
				listFollowers(url);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Getting follower list FAILED");
		}

		int completed = 0;
		int total = all.size();
		long startTime = System.currentTimeMillis();
		long startTime0 = System.currentTimeMillis();

		for (Follower follower : all) {
			int tries = 0;
			boolean passed = false;
			do {
				try {
					getFollowerInfo(follower);
					passed = true;
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("Failed try " + tries);
				}
			} while (!passed && (++tries <= 10));
			if (!passed) {
				System.err.println("Max tries exceeded, moving on");
			}

			completed++;
			if (System.currentTimeMillis() - startTime > 10000) {
				float followersPerSecond = ((float) completed)
						/ ((System.currentTimeMillis() - startTime0) / 1000);
				float timeLeft = (total - completed) * followersPerSecond;
				int mins = (int) Math.floor(timeLeft / 60);
				int secs = (int) Math.floor(timeLeft % 60);
				System.out.println("ETA: " + mins + ":" + secs + " // "
						+ completed + "/" + total);
				startTime = System.currentTimeMillis();
			}
		}

		System.out
				.println("All " + all.size() + " Complete " + complete.size());

		for (Follower follower : complete) {
			try {
				decodeAndResizeImage(follower);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Failed to decode: " + follower.name
						+ "; Moving on");
				follower.avatar = createDummyAvatar();
			}
		}

		System.out.println("Done loading; writing output...");
		ZipOutputStream out = new ZipOutputStream(
				new FileOutputStream(filename));
		String jsonBase = "{\n"
				+ "	\"objName\": \"followers\",\n"
				+ "\"scriptComments\": [[14, 21, 399, 100, true, -1, \"Generated by MegaApuTurkUltra's Follower List Generator\\r\\rhttps:\\/\\/github.com\\/MegaApuTurkUltra\\/ScratchTools\"]],"
				+ "	\"lists\": [{\n"
				+ "			\"listName\": \"followers\",\n"
				+ "			\"contents\": [],\n"
				+ "			\"isPersistent\": false,\n"
				+ "			\"x\": 0,\n"
				+ "			\"y\": 0,\n"
				+ "			\"width\": 102,\n"
				+ "			\"height\": 202,\n"
				+ "			\"visible\": false\n"
				+ "		}],\n"
				+ "	\"sounds\": [],\n"
				+ "	\"costumes\": [],\n"
				+ "	\"currentCostumeIndex\": 0,\n"
				+ "	\"scratchX\": 0,\n"
				+ "	\"scratchY\": 0,\n"
				+ "	\"scale\": 1,\n"
				+ "	\"direction\": 90,\n"
				+ "	\"rotationStyle\": \"normal\",\n"
				+ "	\"isDraggable\": false,\n"
				+ "	\"indexInLibrary\": 100000,\n"
				+ "	\"visible\": true,\n"
				+ "	\"spriteInfo\": { \"X-Swaggified-By\": \"MegaApuTurkUltra\" }\n"
				+ "}";
		JSONObject base = new JSONObject(jsonBase);
		JSONArray list = base.getJSONArray("lists").getJSONObject(0)
				.getJSONArray("contents");
		JSONArray costumes = base.getJSONArray("costumes");
		String costumeStr = "{\n" + "\"costumeName\": \"\",\n"
				+ "\"baseLayerID\": 0,\n" + "\"baseLayerMD5\": \"\",\n"
				+ "\"bitmapResolution\": 1,\n" + "\"rotationCenterX\": 100,\n"
				+ "\"rotationCenterY\": 100\n" + "}";
		int index = 0;
		Follower[] followerArr = complete
				.toArray(new Follower[complete.size()]);

		int percent = 0;

		for (Follower follower : followerArr) {
			String data = "Name: " + follower.name + "\nLocation: "
					+ follower.location + "\nAbout: " + follower.about;
			list.put(data);
			JSONObject costume = new JSONObject(costumeStr);
			costume.put("costumeName", (index + 1) + "");
			costume.put("baseLayerID", index);

			BufferedImage target = follower.avatar;

			MessageDigest digest = MessageDigest.getInstance("MD5");
			DigestOutputStream md5Calc = new DigestOutputStream(
					new NullOutputStream(), digest);
			ImageIO.write(target, "png", md5Calc);
			costume.put("baseLayerMD5", bytesToHex(digest.digest()) + ".png");
			costumes.put(costume);

			out.putNextEntry(new ZipEntry(index + ".png"));
			ImageIO.write(target, "png", out);
			out.closeEntry();
			index++;

			if (index * 10 / all.size() > percent) {
				percent++;
				System.out.println(percent * 10 + "%");
			}
		}
		out.putNextEntry(new ZipEntry("sprite.json"));
		out.write(base.toString().getBytes());
		out.closeEntry();
		out.close();
		System.out.println("Done writing");
		if(ToolBase.EXIT_ON_USAGE) System.exit(0);
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	@Override
	public ParamDef[] getParams() {
		return new ParamDef[] {
				new ParamDef("username", "The username to get followers from",
						false, ParamType.STRING),
				new ParamDef("outputFile", "The sprite2 file to write to",
						false, ParamType.FILE_OUT) };
	}

	@Override
	public void call(String[] args) {
		try {
			toolMain(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getName() {
		return "FollowerListGenerator";
	}

}
