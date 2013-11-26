/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.dmerkushov.httphelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import ru.dmerkushov.loghelper.LoggerWrapper;

/**
 *
 * @author Dmitriy Merkushov
 */
public class HttpHelper {
	
	/**
	 * Length of a portion to get the HTTP document
	 */
	public static final int PORTION_LENGTH = 2048;
	
	/**
	 * User-Agent field to be used by HttpHelper
	 */
	public static final String USER_AGENT = "HttpHelper/1.0";
//	"Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7"
//	"Microsoft Internet Explorer/4.0b1 (Windows 95)"
	
	static LoggerWrapper loggerWrapper = LoggerWrapper.getLoggerWrapper ("HttpHelper");
	
	/**
	 * Get a document denoted by its URI as a byte array
	 * @param uri
	 * @return
	 * @throws HttpHelperException
	 */
	public static byte[] getHttpDocument (String uri) throws HttpHelperException {
		Object[] methodParams = {uri};
		loggerWrapper.entering (methodParams);

		InputStream instream = null;
		byte[] bytes = null;

		HttpClient httpClient = new DefaultHttpClient ();

		HttpGet httpGet = new HttpGet (uri);
		httpGet.setHeader ("User-Agent", HttpHelper.USER_AGENT);
		httpGet.setHeader ("Connection", "keep-alive");
		HttpResponse httpResponse = null;
		try {
			httpResponse = httpClient.execute (httpGet);
		} catch (IOException ioE) {
			throw new HttpHelperException ("Received an IOexception when trying to execute a request to " + uri + ".", ioE);
		}
		HttpEntity entity = httpResponse.getEntity ();
		if (entity != null) {
			long contentLength = entity.getContentLength ();

			loggerWrapper.fine ("Content length: " + String.valueOf (contentLength));

			if (contentLength > Integer.MAX_VALUE) {
				throw new HttpHelperException ("Content for " + uri + " is longer than Integer.MAX_VALUE=" + String.valueOf (Integer.MAX_VALUE));
			} else {
				bytes = new byte[(int) contentLength];
			}

			int currentRead = 0;
			int currentOffset = 0;

			try {
				instream = entity.getContent ();
			} catch (IOException ioE) {
				throw new HttpHelperException ("Received an IOException when trying to get entity content from a request to " + uri + ".", ioE);
			}
			loggerWrapper.fine ("Reading bytes...");
			while (currentOffset < contentLength) {
				try {
					currentRead = instream.read (bytes, currentOffset, HttpHelper.PORTION_LENGTH);
				} catch (IOException ioE) {
					throw new HttpHelperException ("Received an IOException when trying to get InputStream data from a request to " + uri + ".", ioE);
				}
				currentOffset += currentRead;

				//System.out.print (" " + String.valueOf (currentRead));
			}
			loggerWrapper.fine ("Total bytes read: " + String.valueOf (bytes.length));
		}

		loggerWrapper.exiting (bytes);

		return bytes;
	}

	/**
	 * Get a text document denoted by its URI as a String
	 * @param uri
	 * @return
	 * @throws HttpHelperException
	 */
	public static String getTextHttpDocument (String uri) throws HttpHelperException {
		Object[] methodParams = {uri};
		loggerWrapper.entering (methodParams);

		String textHttpDocument = new String (getHttpDocument (uri));

		loggerWrapper.exiting (textHttpDocument);
		return textHttpDocument;
	}
	
	/**
	 * Get a full URL from a base URL for the resource and a map of parameters
	 * @param base
	 * @param params parameters, <b>name->value</b>
	 * @return
	 * @throws HttpHelperException 
	 */
	public static String getURL (String base, Map<String, String> params) throws HttpHelperException {
		loggerWrapper.entering (base, params);
		
		StringBuilder urlBuilder = new StringBuilder ();
		if (base != null) {
			urlBuilder.append (base);
		}
		
		if ((params != null) && (!params.isEmpty ())) {
			urlBuilder.append ("?");
			for (String paramName: params.keySet ()) {
				String paramNameUrl;
				try {
					paramNameUrl = java.net.URLEncoder.encode (paramName, "UTF-8");
				} catch (UnsupportedEncodingException ex) {
					throw new HttpHelperException (ex);
				}
				urlBuilder.append (paramNameUrl);

				String paramValue = params.get (paramName);
				
				String paramValueUrl;
				try {
					paramValueUrl = URLEncoder.encode (paramValue, "UTF-8");
				} catch (UnsupportedEncodingException ex) {
					throw new HttpHelperException (ex);
				}
				urlBuilder.append("=").append (paramValueUrl);
				
				urlBuilder.append ("&");
			}
			urlBuilder.setLength (urlBuilder.length () - 1); // to remove the last &
		}
		
		
		String url = urlBuilder.toString ();
		loggerWrapper.exiting (url);
		return url;
	}
	
	public static void main (String[] args) throws Exception {
		Map<String, String> params = new java.util.HashMap<String, String> ();
		
		//params.put ("q", "альфа бета гамма");
		
		String base = "http://google.com";
		
		System.out.println (getURL (base, params));
	}

}

