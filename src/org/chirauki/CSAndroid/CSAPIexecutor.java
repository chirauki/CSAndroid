package org.chirauki.CSAndroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class CSAPIexecutor {
	private String host; //en la forma http://host/api
	private String apiKey; // api key del cloud.com
	private String apiSKey; // secret key
	private int ACC_TYPE = 0;
	private String ACC_NAME = "";
	private int ACC_ID;
	private String DOM_NAME = "";
	private int DOM_ID;
	
	private static final String JSON = "&response=json";
	private static final String ASYNC_QUERY = "command=queryAsyncJobResult&jobid=";
	private static final String LIST_VM = "command=listVirtualMachines";
	private static final String LIST_VOLS = "command=listVolumes";
	private static final String LIST_SNAP = "command=listSnapshots";
	private static final String LIST_RESOURCE_LIMITS = "command=listResourceLimits";
	private static final String LIST_ACCOUNTS = "command=listAccounts";
	private static final String LIST_OWNTMPL = "command=listTemplates&templatefilter=self";
	private static final String LIST_TMPL = "command=listTemplates&templatefilter=executable";
	private static final String LIST_OWNISOS = "command=listIsos&isofilter=self";
	private static final String LIST_ISOS = "command=listIsos&isofilter=executable";
	private static final String LIST_OSTYPES = "command=listOsTypes";
	private static final String VM_STOP = "command=stopVirtualMachine&id=";
	private static final String VM_START = "command=startVirtualMachine&id=";
	private static final String LIST_SVC_OFFERING = "command=listServiceOfferings";
	private static final String LIST_NETWORKS = "command=listNetworks";
	private static final String LIST_DISC_OFFERING = "command=listDiskOfferings";
	
	/**
	 * Initializes new API Executor.
	 * 
	 * @param h
	 * @param ak
	 * @param ask
	 */
	public CSAPIexecutor(String h, String ak, String ask) {
		super();
		host = h;
		apiKey = ak;
		apiSKey = ask;
		whoAmI();
	}
	
	public CSAPIexecutor(String h) {
		super();
		host = h;
	}
	
	public int getACC_TYPE() {
		return ACC_TYPE;
	}
	
	public String listCapacity() {
		String ret = "";
		String req = "command=listCapacity";
		
		try {
			JSONArray jsonArray = new JSONArray(executeRequest(req));
			Log.i(CSAPIexecutor.class.getName(),
					"Number of entries " + jsonArray.length());
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				Log.i(CSAPIexecutor.class.getName(), jsonObject.getString("text"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public JSONArray listVirtualMachines() {
		String response = executeRequest(LIST_VM + JSON);
		JSONArray vms = null;
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(response);
			JSONObject tmp = (JSONObject) jObject.get("listvirtualmachinesresponse");
    		vms = tmp.getJSONArray("virtualmachine");
			return vms;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new JSONArray();
	}
	
	public JSONObject listVirtualMachines(int id) {
		String response = executeRequest(LIST_VM + "&id=" + id + JSON);
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(response);
			JSONObject tmp = (JSONObject) jObject.get("listvirtualmachinesresponse");
    		JSONArray vm = tmp.getJSONArray("virtualmachine");
			return vm.getJSONObject(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new JSONObject();
	}
	
	public JSONArray stopVirtualMachine(int id) {
		String response = executeRequest(VM_STOP + id + JSON);
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(response);
			JSONObject tmp = (JSONObject) jObject.get("stopvirtualmachineresponse");
    		int jobid = tmp.getInt("jobid");

    		int jobstatus = 0;
    		while (jobstatus != 1) {
    			jobstatus = queryAsyncJobResult(jobid);
    			Thread.sleep(2000);
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new JSONArray();
	}
	
	public JSONArray startVirtualMachine(int id) {
		String response = executeRequest(VM_START + id + JSON);
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(response);
			JSONObject tmp = (JSONObject) jObject.get("startvirtualmachineresponse");
    		int jobid = tmp.getInt("jobid");

    		int jobstatus = 0;
    		while (jobstatus != 1) {
    			jobstatus = queryAsyncJobResult(jobid);
    			Thread.sleep(2000);
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new JSONArray();
	}
	
	public JSONArray listVolumes() {
		String response = executeRequest(LIST_VOLS + JSON);
		JSONArray vols = null;
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(response);
			JSONObject tmp = (JSONObject) jObject.get("listvolumesresponse");
    		vols = tmp.getJSONArray("volume");
			return vols;
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return jArray;
		return new JSONArray();
	}

	public JSONObject listVolumes(Integer volId) {
		String response = executeRequest(LIST_VOLS + "&id=" + volId + JSON);
		JSONArray vols = null;
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(response);
			JSONObject tmp = (JSONObject) jObject.get("listvolumesresponse");
			vols = tmp.getJSONArray("volume");
			jObject = vols.getJSONObject(0);
			return jObject;
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return jArray;
		return new JSONObject();
	}

	public JSONArray listSnapshots() {
		String response = executeRequest(LIST_SNAP + JSON);
		JSONArray snap = null;
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(response);
			JSONObject tmp = (JSONObject) jObject.get("listsnapshotsresponse");
    		snap = tmp.getJSONArray("snapshot");
			return snap;
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return jArray;
		return new JSONArray();
	}
	
	public JSONArray listOwnTemplates() {
		String response = executeRequest(LIST_OWNTMPL + JSON);
		JSONArray tmpl = null;
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(response);
			JSONObject tmp = (JSONObject) jObject.get("listtemplatesresponse");
    		tmpl = tmp.getJSONArray("template");
			return tmpl;
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return jArray;
		return new JSONArray();
	}
	
	public JSONArray listTemplates() {
		String response = executeRequest(LIST_TMPL + JSON);
		JSONArray tmpl = null;
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(response);
			JSONObject tmp = (JSONObject) jObject.get("listtemplatesresponse");
    		tmpl = tmp.getJSONArray("template");
			return tmpl;
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return jArray;
		return new JSONArray();
	}
	
	public JSONObject listOsTypes(Integer osId) {
		String response = executeRequest(LIST_OSTYPES + "&id="+ osId + JSON);
		JSONArray ostype = null;
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(response);
			JSONObject tmp = (JSONObject) jObject.get("listostypesresponse");
			ostype = tmp.getJSONArray("ostype");
			jObject = ostype.getJSONObject(0);
			return jObject;
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return jArray;
		return new JSONObject();
	}
	
	public JSONArray listServiceOfferings() {
		String response = executeRequest(LIST_SVC_OFFERING + JSON);
		JSONArray offers = null;
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(response);
			JSONObject tmp = (JSONObject) jObject.get("listserviceofferingsresponse");
			offers = tmp.getJSONArray("serviceoffering");
			return offers;
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return jArray;
		return new JSONArray();
	}
	
	public JSONArray listDiskOfferings() {
		String response = executeRequest(LIST_DISC_OFFERING + JSON);
		JSONArray offers = null;
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(response);
			JSONObject tmp = (JSONObject) jObject.get("listdiskofferingsresponse");
			offers = tmp.getJSONArray("diskoffering");
			return offers;
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return jArray;
		return new JSONArray();
	}
	
	public JSONArray listNetworks() {
		String response = executeRequest(LIST_NETWORKS + "&account=" + ACC_ID + "&domainid=" + DOM_ID + JSON);
		JSONArray nets = null;
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(response);
			JSONObject tmp = (JSONObject) jObject.get("listnetworksresponse");
			nets = tmp.getJSONArray("network");
			return nets;
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return jArray;
		return new JSONArray();
	}
	
	public JSONArray listOwnIsos() {
		String response = executeRequest(LIST_OWNISOS + JSON);
		JSONArray isos = null;
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(response);
			JSONObject tmp = (JSONObject) jObject.get("listisosresponse");
			isos = tmp.getJSONArray("iso");
			return isos;
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return jArray;
		return new JSONArray();
	}
	
	public JSONArray listIsos() {
		String response = executeRequest(LIST_ISOS + JSON);
		JSONArray isos = null;
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(response);
			JSONObject tmp = (JSONObject) jObject.get("listisosresponse");
			isos = tmp.getJSONArray("iso");
			return isos;
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return jArray;
		return new JSONArray();
	}
	
	public JSONArray listResourceLimits() {
		String response = executeRequest(LIST_RESOURCE_LIMITS + JSON);
		JSONArray rlimits = null;
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(response);
			JSONObject tmp = (JSONObject) jObject.get("listresourcelimitsresponse");
			rlimits = tmp.getJSONArray("resourcelimit");
			return rlimits;
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return jArray;
		return new JSONArray();
	}
		
	public JSONObject whoAmI() {
		String response = executeRequest(LIST_ACCOUNTS + JSON);
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(response);
			JSONObject tmp = (JSONObject) jObject.get("listaccountsresponse");
			JSONArray account = tmp.getJSONArray("account");
			JSONObject userObj = (JSONObject) account.get(0);
			JSONArray arrayuser = userObj.getJSONArray("user");
			for (int i = 0; i < arrayuser.length(); i++) {
				jObject = (JSONObject) arrayuser.getJSONObject(i);
				String user_apik = jObject.getString("apikey");
				String user_seck = jObject.getString("secretkey");
				
				if (user_apik.equals(apiKey) && user_seck.equals(apiSKey)) {
					ACC_TYPE = jObject.getInt("accounttype");
					ACC_NAME = jObject.getString("account");
					ACC_ID = userObj.getInt("id");
					DOM_ID = jObject.getInt("domainid");
					DOM_NAME = jObject.getString("domain");
					return jObject;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return jArray;
		return new JSONObject();
	}
	
	public int queryAsyncJobResult(int jobId) {
		String response = executeRequest(ASYNC_QUERY + jobId + JSON);
		try {
			JSONObject jObject = new JSONObject(response);
			JSONObject tmp = (JSONObject) jObject.get("queryasyncjobresultresponse");
			return tmp.getInt("jobstatus");
			
		} catch (JSONException jex) {
			return -1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return jArray;
		return -1;
	}
	
	public String executeLogin(String username, String password, String domain)
	{
		StringBuilder builder = null;
		String apiUrl = "command=login&username=" + username + "&password=" + hashPassword(password);
		if (domain != null) {
			apiUrl = apiUrl + "&domain=" + domain;
		}
		apiUrl = apiUrl + "&response=json";
		
		try {
			//System.out.println("Constructing API call to host = '" + host + "' with API command = '" + apiUrl + "' using apiKey = '" + apiKey + "' and secretKey = '" + apiSKey + "'");
			
			// Step 1: Make sure your APIKey is toLowerCased and URL encoded
			//String encodedApiKey = URLEncoder.encode(apiKey.toLowerCase(), "UTF-8");
			
			// Step 2: toLowerCase all the parameters, URL encode each parameter value, and the sort the parameters in alphabetical order
			// Please note that if any parameters with a '&' as a value will cause this test client to fail since we are using '&' to delimit 
			// the string
			List sortedParams = new ArrayList();
			//sortedParams.add("apikey="+encodedApiKey);
			StringTokenizer st = new StringTokenizer(apiUrl, "&");
			while (st.hasMoreTokens()) {
				String paramValue = st.nextToken().toLowerCase();
				String param = paramValue.substring(0, paramValue.indexOf("="));
				String value = URLEncoder.encode(paramValue.substring(paramValue.indexOf("=")+1, paramValue.length()), "UTF-8");
				sortedParams.add(param + "=" + value);
			}
			Collections.sort(sortedParams);
//			System.out.println("Sorted Parameters: " + sortedParams);
			
			builder = new StringBuilder();
			HttpClient client = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(host + "?" + apiUrl);
			try {
				HttpResponse response = client.execute(httpGet);
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(content));
					String line;
					while ((line = reader.readLine()) != null) {
						builder.append(line);
					}
				} else {
					Log.e(CSAPIexecutor.class.toString(), "Failed to download file");
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			String responseLogin = builder.toString();
			JSONArray tmp1 = null;
			JSONObject jObject = null;
			String sesKey = null;
			String acctName = null;
			Integer domId = null;
			try {
				jObject = new JSONObject(responseLogin);
				JSONObject tmp = (JSONObject) jObject.get("loginresponse");
				sesKey = tmp.getString("sessionkey");
				acctName = tmp.getString("account");
				domId = tmp.getInt("domainid");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			//apiUrl = "command=listAccounts&name=" + acctName + "&domainid=" + domId;
			apiUrl = "command=listAccounts";
			
			// Step 1: Make sure your APIKey is toLowerCased and URL encoded
			//String encodedApiKey = URLEncoder.encode(apiKey.toLowerCase(), "UTF-8");
			
			// Step 2: toLowerCase all the parameters, URL encode each parameter value, and the sort the parameters in alphabetical order
			// Please note that if any parameters with a '&' as a value will cause this test client to fail since we are using '&' to delimit 
			// the string
			sortedParams = new ArrayList();
			//sortedParams.add("apikey="+encodedApiKey);
			st = new StringTokenizer(apiUrl, "&");
			while (st.hasMoreTokens()) {
				String paramValue = st.nextToken().toLowerCase();
				String param = paramValue.substring(0, paramValue.indexOf("="));
				String value = URLEncoder.encode(paramValue.substring(paramValue.indexOf("=")+1, paramValue.length()), "UTF-8");
				sortedParams.add(param + "=" + value);
			}
			Collections.sort(sortedParams);
			//System.out.println("Sorted Parameters: " + sortedParams);
			
			// Step 3: Construct the sorted URL and sign and URL encode the sorted URL with your secret key
			String sortedUrl = null;
			boolean first = true;
			//for (String param : sortedParams) {
			for (Object elem: sortedParams) {
				String param = (String)elem;
				if (first) {
					sortedUrl = param;
					first = false;
				} else {
					sortedUrl = sortedUrl + "&" + param;
				}
			}
			
			//String encodedSignature = signRequest(sortedUrl, apiSKey);
			String encodedSignature = signRequest(sortedUrl, sesKey);
			
			// Step 4: Construct the final URL we want to send to the CloudStack Management Server
			// Final result should look like:
			// http(s)://://client/api?&apiKey=&signature=
			//String finalUrl = host + "?" + apiUrl + "&apiKey=" + apiKey + "&signature=" + encodedSignature;
			String finalUrl = host + "?" + apiUrl + "&sessionkey=" + URLEncoder.encode(sesKey, "UTF-8");
;
			
			// Step 5: Perform a HTTP GET on this URL to execute the command
			builder = new StringBuilder();
			client = new DefaultHttpClient();
			httpGet = new HttpGet(finalUrl);
			try {
				HttpResponse response = client.execute(httpGet);
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				//if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(content));
					String line;
					while ((line = reader.readLine()) != null) {
						builder.append(line);
					}
				//} else {
					Log.e(CSAPIexecutor.class.toString(), "Failed to download file");
				//}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return httpResponse;
		return builder.toString();	
	}
	
	/**
	 * Executes apiUrl request and returns the String object containing the response.
	 * 
	 * @param apiUrl
	 * @return
	 */
	public String executeRequest(String apiUrl) {
		StringBuilder builder = null;

		try {
			//System.out.println("Constructing API call to host = '" + host + "' with API command = '" + apiUrl + "' using apiKey = '" + apiKey + "' and secretKey = '" + apiSKey + "'");
			
			// Step 1: Make sure your APIKey is toLowerCased and URL encoded
			String encodedApiKey = URLEncoder.encode(apiKey.toLowerCase(), "UTF-8");
			
			// Step 2: toLowerCase all the parameters, URL encode each parameter value, and the sort the parameters in alphabetical order
			// Please note that if any parameters with a '&' as a value will cause this test client to fail since we are using '&' to delimit 
			// the string
			List sortedParams = new ArrayList();
			sortedParams.add("apikey="+encodedApiKey);
			StringTokenizer st = new StringTokenizer(apiUrl, "&");
			while (st.hasMoreTokens()) {
				String paramValue = st.nextToken().toLowerCase();
				String param = paramValue.substring(0, paramValue.indexOf("="));
				String value = URLEncoder.encode(paramValue.substring(paramValue.indexOf("=")+1, paramValue.length()), "UTF-8");
				sortedParams.add(param + "=" + value);
			}
			Collections.sort(sortedParams);
//			System.out.println("Sorted Parameters: " + sortedParams);
			
			// Step 3: Construct the sorted URL and sign and URL encode the sorted URL with your secret key
			String sortedUrl = null;
			boolean first = true;
			//for (String param : sortedParams) {
			for (Object elem: sortedParams) {
				String param = (String)elem;
				if (first) {
					sortedUrl = param;
					first = false;
				} else {
					sortedUrl = sortedUrl + "&" + param;
				}
			}
			
			String encodedSignature = signRequest(sortedUrl, apiSKey);
			
			// Step 4: Construct the final URL we want to send to the CloudStack Management Server
			// Final result should look like:
			// http(s)://://client/api?&apiKey=&signature=
			String finalUrl = host + "?" + apiUrl + "&apiKey=" + apiKey + "&signature=" + encodedSignature;
			
			// Step 5: Perform a HTTP GET on this URL to execute the command
			builder = new StringBuilder();
			HttpClient client = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(finalUrl);
			try {
				HttpResponse response = client.execute(httpGet);
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(content));
					String line;
					while ((line = reader.readLine()) != null) {
						builder.append(line);
					}
				} else {
					Log.e(CSAPIexecutor.class.toString(), "Failed to download file");
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return httpResponse;
		return builder.toString();
	}
	
	/**
	 * 1. Signs a string with a secret key using SHA-1
	 * 2. Base64 encode the result
	 * 3. URL encode the final result
	 * 
	 * @param request
	 * @param key
	 * @return
	 */
	public static String signRequest(String request, String key) {
		try {
			Mac mac = Mac.getInstance("HmacSHA1");
			SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "HmacSHA1");
			mac.init(keySpec);
			mac.update(request.getBytes());
			byte[] encryptedBytes = mac.doFinal();
			return URLEncoder.encode(Base64.encodeBytes(encryptedBytes), "UTF-8");
		} catch (Exception ex) {
			System.out.println(ex);
		}
		return null;
	}
	
	public static String hashPassword(String password) {
		String hashword = null;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(password.getBytes());
			BigInteger hash = new BigInteger(1, md5.digest());
			hashword = hash.toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return hashword;
	}
}
