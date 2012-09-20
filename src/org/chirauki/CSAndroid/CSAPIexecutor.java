package org.chirauki.CSAndroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

public class CSAPIexecutor {
private transient DefaultHttpClient client = new DefaultHttpClient();
	
	private transient CookieManager cookieManager = new CookieManager();

	@SerializedName("csapiurl") 
	private String csApiUrl;
	@SerializedName("csuserpassword") 
	private String csUserPassword;
	@SerializedName("csuserid") 
	private String csUserId;
	@SerializedName("csusername") 
	private String csUserName;
	@SerializedName("csfirstname") 
	private String csFirstName;
	@SerializedName("cslastname") 
	private String csLastName;
	@SerializedName("csemail") 
	private String csEmail;
	@SerializedName("cscreated") 
	private String csCreated;
	@SerializedName("csstate") 
	private String csState;
	@SerializedName("csaccount") 
	private String csAccount;
	@SerializedName("csaccounttype") 
	private int csAccountType;
	@SerializedName("csdomainid") 
	private String csDomainId;
	@SerializedName("csdomain") 
	private String csDomain;
	@SerializedName("cstimezone") 
	private String csTimeZone;
	@SerializedName("csapikey") 
	private String csApiKey;
	@SerializedName("cssecretkey") 
	private String csSecretKey;
	@SerializedName("csaccountid") 
	private String csAccountId;
	
	@SerializedName("csjsessionid") 
	private String jSessionId;
	@SerializedName("cssessionkey") 
	private String sessionKey;
	
	private transient Context context; 
	
	private static final String JSON = "&response=json";
	private static final String LIST_USERS = "command=ListUsers";
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
	 * Initializes new API Executor with api key and secret key.
	 * 
	 * @param h API endpoint url
	 * @param ak API key
	 * @param ask Secret key
	 */
	public CSAPIexecutor(String h, String ak, String ask, Context ctx) {
		super();
		csApiUrl = h;
		csApiKey = ak;
		csSecretKey = ask;
		try {
			whoAmI();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		context = ctx;
		URL url;
		try {
			url = new URL(h);
			if(url.getHost().equals("plecs.upc.edu")) {
				client = new plecsHttpClient(ctx);
			}
		} catch (MalformedURLException e) {
			Log.e("Creating CSAPIexecutor", "Error: Malformed url.");
			e.printStackTrace();
		}
		CookieHandler.setDefault(cookieManager);
		
		loginUser();
	}
	
	/**
	 * Initializes new API Executor with username and password.
	 * 
	 * @param h API endpoint url
	 * @param user User name
	 * @param pass User password
	 * @param dom User domain
	 * @param ctx Application context
	 */
	public CSAPIexecutor(String h, String user, String password, String domain, Context ctx) {
		super();
		csApiUrl = h;
		csUserName = user;
		csUserPassword = password;
		if (domain == null || domain == "") {
			//Assume domain ROOT
			csDomain = "ROOT";
		} else {
			csDomain = domain;
		}
		context = ctx;
		URL url;
		try {
			url = new URL(h);
			if(url.getHost().equals("plecs.upc.edu")) {
				client = new plecsHttpClient(ctx);
			}
		} catch (MalformedURLException e) {
			Log.e("Creating CSAPIexecutor", "Error: Malformed url.");
			e.printStackTrace();
		}
		CookieHandler.setDefault(cookieManager);
		
		//loginUser();
	}
	
	/**
	 * Initializes new empty API Executor.
	 */
	public CSAPIexecutor() {
		super();

		client = new DefaultHttpClient();
		cookieManager = new CookieManager();
	}
	
	public String listCapacity() {
		String ret = "";
		String req = "command=listCapacity";
		
		try {
			JSONArray jsonArray = new JSONArray(new executeRequest().execute(req).get());
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
		try {
			String response = new executeRequest().execute(LIST_VM + JSON).get();
			JSONArray vms = null;
			JSONObject jObject = null;
		
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
		try {
			String response = new executeRequest().execute(LIST_VM + "&id=" + id + JSON).get();
			JSONObject jObject = null;
		
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
		try {
			String response = new executeRequest().execute(VM_STOP + id + JSON).get();

			JSONObject jObject = null;
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
		try {
			String response = new executeRequest().execute(VM_START + id + JSON).get();
			JSONObject jObject = null;

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
		try {
			String response = new executeRequest().execute(LIST_VOLS + JSON).get();
		
			JSONArray vols = null;
			JSONObject jObject = null;
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
		try {
			String response = new executeRequest().execute(LIST_VOLS + "&id=" + volId + JSON).get();

			JSONArray vols = null;
			JSONObject jObject = null;

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
		try {
			String response = new executeRequest().execute(LIST_SNAP + JSON).get();

			JSONArray snap = null;
			JSONObject jObject = null;
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
		try {
			String response = new executeRequest().execute(LIST_OWNTMPL + JSON).get();

			JSONArray tmpl = null;
			JSONObject jObject = null;

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
		try {
			String response = new executeRequest().execute(LIST_TMPL + JSON).get();

			JSONArray tmpl = null;
			JSONObject jObject = null;

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
		try {
			String response = new executeRequest().execute(LIST_OSTYPES + "&id="+ osId + JSON).get();

			JSONArray ostype = null;
			JSONObject jObject = null;
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
		try {
			String response = new executeRequest().execute(LIST_SVC_OFFERING + JSON).get();

			JSONArray offers = null;
			JSONObject jObject = null;
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
		try {
			String response = new executeRequest().execute(LIST_DISC_OFFERING + JSON).get();

			JSONArray offers = null;
			JSONObject jObject = null;
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
		try {
			String response = new executeRequest().execute(LIST_NETWORKS + "&account=" + csAccountId + 
					"&domainid=" + csDomainId + JSON).get();

			JSONArray nets = null;
			JSONObject jObject = null;
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
		try {
			String response = new executeRequest().execute(LIST_OWNISOS + JSON).get();

			JSONArray isos = null;
			JSONObject jObject = null;

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
		try {
			String response = new executeRequest().execute(LIST_ISOS + JSON).get();
			
			JSONArray isos = null;
			JSONObject jObject = null;
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
		try {
			String response = new executeRequest().execute(LIST_RESOURCE_LIMITS + JSON).get();
			
			JSONArray rlimits = null;
			JSONObject jObject = null;
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
	
	public JSONObject listAccounts() {
		try {
			String response = new executeRequest().execute(LIST_ACCOUNTS + JSON).get();
			JSONArray accounts = null;
			JSONObject jObject = null;
			
			jObject = new JSONObject(response);
			JSONObject tmp = (JSONObject) jObject.get("listaccountsresponse");
			accounts = tmp.getJSONArray("account");
			return accounts.getJSONObject(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
		
	public JSONObject whoAmI() throws InterruptedException, ExecutionException {
		String response = new executeRequest().execute(LIST_ACCOUNTS + JSON).get();
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
				
				/*if (user_apik.equals(apiKey) && user_seck.equals(apiSKey)) {
					ACC_TYPE = jObject.getInt("accounttype");
					ACC_NAME = jObject.getString("account");
					ACC_ID = userObj.getInt("id");
					DOM_ID = jObject.getInt("domainid");
					DOM_NAME = jObject.getString("domain");
					return jObject;
				}*/
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return jArray;
		return new JSONObject();
	}
	
	public int queryAsyncJobResult(int jobId) {
		try {
			String response = new executeRequest().execute(ASYNC_QUERY + jobId + JSON).get();

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
	
	public boolean loginUser() {
		try {
			String loginResponse = new login().execute().get();
			if(!loginResponse.equals("")) {
				JSONObject result = new JSONObject(loginResponse);;

				this.csAccount = result.getString("account");
				this.csAccountId = result.getString("accountid");
				this.csAccountType = result.getInt("accounttype");
				this.csCreated = result.getString("created");
				this.csDomain = result.getString("domain");
				this.csDomainId = result.getString("domainid");
				this.csEmail = result.getString("email");
				this.csFirstName = result.getString("firstname");
				this.csLastName = result.getString("lastname");
				this.csState = result.getString("state");
				this.csTimeZone = result.getString("timezone");
				this.csUserId = result.getString("id");

				if(result.has("apikey")) {
					this.csApiKey = result.getString("apikey");
					this.csSecretKey = result.getString("secretkey");
				}
				
				return true;
			}
		} catch (InterruptedException e) {
			Log.e("LOGIN PROCESS", "Something went wron at login.");
			e.printStackTrace();
		} catch (ExecutionException e) {
			Log.e("LOGIN PROCESS", "Something went wron at login.");
			e.printStackTrace();
		} catch (JSONException e) {
			Log.e("LOGIN PROCESS", "Something went wron at login.");
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean getUserData() {
		try {
			String loginResponse = new executeRequest().execute("listUsers").get();
			if(!loginResponse.equals("")) {
				JSONObject result = new JSONObject(loginResponse);;

				this.csAccount = result.getString("account");
				this.csAccountId = result.getString("accountid");
				this.csAccountType = result.getInt("accounttype");
				this.csCreated = result.getString("created");
				this.csDomain = result.getString("domain");
				this.csDomainId = result.getString("domainid");
				this.csEmail = result.getString("email");
				this.csFirstName = result.getString("firstname");
				this.csLastName = result.getString("lastname");
				this.csState = result.getString("state");
				this.csTimeZone = result.getString("timezone");
				this.csUserId = result.getString("id");

				if(result.has("apikey")) {
					this.csApiKey = result.getString("apikey");
					this.csSecretKey = result.getString("secretkey");
				}
				
				return true;
			}
		} catch (InterruptedException e) {
			Log.e("LOGIN PROCESS", "Something went wron at login.");
			e.printStackTrace();
		} catch (ExecutionException e) {
			Log.e("LOGIN PROCESS", "Something went wron at login.");
			e.printStackTrace();
		} catch (JSONException e) {
			Log.e("LOGIN PROCESS", "Something went wron at login.");
			e.printStackTrace();
		}
		return false;
	}
	
	private class login extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			
			String username = csUserName;
			String password = csUserPassword;
			String domain = csDomain;
			
			StringBuilder builder = null;
			String apiUrl = "command=login&username=" + username + "&password=" + hashPassword(password);
			if (domain != null && !domain.equals("")) {
				if(domain.equals("ROOT")) {
					apiUrl = apiUrl + "&domain=/";
				} else {
					apiUrl = apiUrl + "&domain=" + domain;
				}
			} 
			
			apiUrl = apiUrl + "&response=json";
			
			try {
				List<String> sortedParams = new ArrayList<String>();
				StringTokenizer st = new StringTokenizer(apiUrl, "&");
				while (st.hasMoreTokens()) {
					String paramValue = st.nextToken().toLowerCase();
					String param = paramValue.substring(0, paramValue.indexOf("="));
					String value = URLEncoder.encode(paramValue.substring(paramValue.indexOf("=")+1, paramValue.length()), "UTF-8");
					sortedParams.add(param + "=" + value);
				}
				Collections.sort(sortedParams);
				
				builder = new StringBuilder();
				String finalUrl = csApiUrl + "?" + apiUrl;
				HttpGet httpGet = new HttpGet(finalUrl);
				try {
					String responseLogin = executeHttpRequest(csApiUrl + "?" + apiUrl);
					// Patch for LDAP auth
					// response was empty, it may be due to password encoding
					if (responseLogin.equals("")) {
						apiUrl = "command=login&username=" + username + "&password=" + password;
						if (domain != null && !domain.equals("")) {
							if(domain.equals("ROOT")) {
								apiUrl = apiUrl + "&domain=/";
							} else {
								apiUrl = apiUrl + "&domain=" + domain;
							}
						} 
						
						apiUrl = apiUrl + "&response=json";
						
						sortedParams = new ArrayList<String>();
						st = new StringTokenizer(apiUrl, "&");
						while (st.hasMoreTokens()) {
							String paramValue = st.nextToken().toLowerCase();
							String param = paramValue.substring(0, paramValue.indexOf("="));
							String value = URLEncoder.encode(paramValue.substring(paramValue.indexOf("=")+1, paramValue.length()), "UTF-8");
							sortedParams.add(param + "=" + value);
						}
						Collections.sort(sortedParams);
						
						builder = new StringBuilder();
						finalUrl = csApiUrl + "?" + apiUrl;
						httpGet = new HttpGet(finalUrl);
						responseLogin = executeHttpRequest(csApiUrl + "?" + apiUrl);
					}
					// END patch
					
					JSONArray tmp1 = null;
					JSONObject jObject = null;
					jObject = new JSONObject(responseLogin);
					JSONObject tmp = (JSONObject) jObject.get("loginresponse");
					sessionKey = tmp.getString("sessionkey");
					String csUserId = tmp.getString("userid");
				
					apiUrl = "response=json&command=listUsers&id=" + csUserId +
							"&sessionkey=" + URLEncoder.encode(sessionKey, "UTF-8");
					
					finalUrl = csApiUrl + "?" + apiUrl;
					
					responseLogin = executeHttpRequest(finalUrl);
					jObject = new JSONObject(responseLogin);
					JSONObject tmp2 = (JSONObject) jObject.get("listusersresponse");
					JSONArray acc = tmp2.getJSONArray("user");
					jObject = acc.getJSONObject(0);
					return jObject.toString();
				} catch (IOException e) {
					Log.e("Login call", "Exception " + e.getMessage());
					e.printStackTrace();
					return "";
				}
			} catch (Exception e) {
				Log.e("Login call", "Exception " + e.getMessage());
				e.printStackTrace();
				return "";
			}
		}
	}
	
	private class executeRequest extends AsyncTask<String, Void, String> {
		StringBuilder builder = null;
		
		@Override
		protected String doInBackground(String... params) {
			try {
				String apiUrl = params[0];
				String encodedApiKey = URLEncoder.encode(csApiKey.toLowerCase(), "UTF-8");
				
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

				String sortedUrl = null;
				boolean first = true;
				for (Object elem: sortedParams) {
					String param = (String)elem;
					if (first) {
						sortedUrl = param;
						first = false;
					} else {
						sortedUrl = sortedUrl + "&" + param;
					}
				}
				
				String encodedSignature = signRequest(sortedUrl, csSecretKey);
				
				String finalUrl = csApiUrl + "?" + apiUrl + "&apiKey=" + csApiKey + "&signature=" + encodedSignature;
				return executeHttpRequest(finalUrl);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "";
		}
		
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
			String encodedApiKey = URLEncoder.encode(csApiKey.toLowerCase(), "UTF-8");
			
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
			
			String encodedSignature = signRequest(sortedUrl, csSecretKey);
			
			// Step 4: Construct the final URL we want to send to the CloudStack Management Server
			// Final result should look like:
			// http(s)://://client/api?&apiKey=&signature=
			String finalUrl = csApiUrl + "?" + apiUrl + "&apiKey=" + csApiKey + "&signature=" + encodedSignature;
			return executeHttpRequest(finalUrl);
			// Step 5: Perform a HTTP GET on this URL to execute the command
			/*builder = new StringBuilder();
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
			}*/
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return httpResponse;
		return "";
	}
	
	/**
	 * 1. Signs a string with a secret key using SHA-1
	 * 2. Base64 encode the result
	 * 3. URL encode the final result
	 * 
	 * @param request
	 * @param key
	 * @return HMAC-SHA1 signature
	 */
	public String signRequest(String request, String key) {
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
	
	/**
	 * Returns an MD5 password hash for the password provided
	 * @param password The password to be hashed
	 * @return The md5 password hash for password
	 */
	public String hashPassword(String password) {
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
	
	public String executeHttpRequest(String req) {
		String responseText = "";
		HttpResponse reqResponse = null;
		StatusLine statusLine;
		int statusCode;
		StringBuilder builder = new StringBuilder();
		URL url;
		
		trustEveryone();
		
		try {
			url = new URL(req);
			HttpGet httpGet = new HttpGet(url.toString());
			reqResponse =  client.execute(httpGet);
			statusLine = reqResponse.getStatusLine();
			statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				// IF JSESSIONID is not set, save it
				if(jSessionId == null) {
					Header[] headers = reqResponse.getHeaders("Set-Cookie");
					for(Header header : headers) {
						String cookie = header.getValue();
						String[] garbage = cookie.split(";");
						for(String piece : garbage) {
							if(piece.startsWith("JSESSIONID")) {
								String[] value = piece.split("=");
								jSessionId = value[1];
							}
						}
					}
				}
				HttpEntity entity = reqResponse.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
				String line;
				builder.delete(0, builder.capacity());
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		responseText = builder.toString();
		return responseText;
	}
	
	private void trustEveryone() { 
	    try { 
	    	HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
	    	/*HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){ 
	    		public boolean verify(String hostname, SSLSession session) { 
	    			return true; 
	    		}});*/
	    	HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
	    	SSLContext context = SSLContext.getInstance("TLS"); 
	    	context.init(null, new X509TrustManager[]{new X509TrustManager(){ 
	    		public void checkClientTrusted(X509Certificate[] chain, 
	    				String authType) {} 
	    		public void checkServerTrusted(X509Certificate[] chain, 
	    				String authType) {} 
	    		public X509Certificate[] getAcceptedIssuers() { 
	    			return new X509Certificate[0]; 
	    		}}}, new SecureRandom()); 
	    	HttpsURLConnection.setDefaultSSLSocketFactory( 
	    			context.getSocketFactory()); 
	    } catch (Exception e) { // should never happen 
	    	e.printStackTrace(); 
	    } 
	}

	/*
	 * Getters and setters
	 */
	public String getCsApiUrl() {
		return csApiUrl;
	}

	public String getCsUserPassword() {
		return csUserPassword;
	}

	public String getCsUserId() {
		return csUserId;
	}

	public String getCsUserName() {
		return csUserName;
	}

	public String getCsFirstName() {
		return csFirstName;
	}

	public String getCsLastName() {
		return csLastName;
	}

	public String getCsEmail() {
		return csEmail;
	}

	public String getCsCreated() {
		return csCreated;
	}

	public String getCsState() {
		return csState;
	}

	public String getCsAccount() {
		return csAccount;
	}

	public int getCsAccountType() {
		return csAccountType;
	}

	public String getCsDomainId() {
		return csDomainId;
	}

	public String getCsDomain() {
		return csDomain;
	}

	public String getCsTimeZone() {
		return csTimeZone;
	}

	public String getCsApiKey() {
		return csApiKey;
	}

	public String getCsSecretKey() {
		return csSecretKey;
	}

	public String getCsAccountId() {
		return csAccountId;
	}

	public void setContext(Context context) {
		this.context = context;
		refreshClient();
	}
	
	private void refreshClient() {
		URL url;
		try {
			if (csApiUrl != null && context != null) {
				url = new URL(csApiUrl);
				if(url.getHost().equals("plecs.upc.edu")) {
					client = new plecsHttpClient(context);
				}	
			}
		} catch (MalformedURLException e) {
			Log.e("Creating CSAPIexecutor", "Error: Malformed url.");
			e.printStackTrace();
		}
		CookieHandler.setDefault(cookieManager);
	}
}
