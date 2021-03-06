package com.qardapp.qard.settings.services;

import org.json.JSONArray;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FlickrApi;
import org.scribe.builder.api.Foursquare2Api;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.builder.api.TumblrApi;
import org.scribe.builder.api.TwitterApi;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.qardapp.qard.R;
import com.qardapp.qard.Services;

public class OAuthActivity extends Activity {
	
	OAuthService mService = null;
	public Token mRequestToken;
	public WebView mWebView;
	private SharedPreferences mPrefs;
	Services service;
	int serviceID;
	String data;
	Activity activity;
	String email;
	String firstname;
	String lastname;
	String username;
	String profileURL;
	String userID;
	private boolean uses_oauth2 = false;
	
	private class OAuthTask extends AsyncTask<Void, Void, String> {
		
		ProgressDialog progDialog;
		
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDialog = new ProgressDialog(OAuthActivity.this);
            progDialog.setMessage("Connecting...");
            progDialog.setIndeterminate(true);
            progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDialog.setCancelable(false);
            progDialog.show();
        }
        
		protected String doInBackground(Void... arg0) {

			// Temporary URL
			String authURL = service.authURL;

			try {
				if (uses_oauth2)
				{
				    authURL = mService.getAuthorizationUrl(null);	
				} else {
				    mRequestToken = mService.getRequestToken();
					authURL = mService.getAuthorizationUrl(mRequestToken);
				}
			}
			catch ( OAuthException e ) {
				e.printStackTrace();
				return null;
			}
            Log.d("here", authURL);
			return authURL;
	    }
		
		@Override  
		protected void onPostExecute(String authURL) { 
			
			mWebView = (WebView)findViewById(R.id.webview);
		    mWebView.setWebViewClient(new WebViewClient() {		    	
		        
		        @Override
		        public void onPageFinished(WebView view, String url) {
	        		if (progDialog != null) {
			            progDialog.dismiss();
					}                
		        }
		    		    	
			    @Override
			    public boolean shouldOverrideUrlLoading(WebView view, String url) {	 
			    	Log.d("urls",url);

					super.shouldOverrideUrlLoading(view, url);

					if( url.startsWith("oauth") || url.startsWith("http://localhost") ) {
				        mWebView.setVisibility(WebView.GONE);	
				        
				        // Restart loading animation
			            progDialog = new ProgressDialog(OAuthActivity.this);
			            progDialog.setMessage("Authorizing...");
			            progDialog.setIndeterminate(true);
			            progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			            progDialog.setCancelable(false);
			            progDialog.show();		
			            
					    final String url1 = url;
				   	    Thread t1 = new Thread() {
							public void run() {
						  	    Uri uri = Uri.parse(url1);
			
							    String verifier = uri.getQueryParameter("oauth_verifier");
							    if (uses_oauth2)
							    {
							    	verifier = uri.getQueryParameter("code");
							    	mRequestToken = null;
							    }
							    
							    Verifier v = new Verifier(verifier);
							    Token accessToken = mService.getAccessToken(mRequestToken, v);		

							    // Do a quick query to get user information
							    
							    String urlStr = service.userQuery;
							    if (serviceID == Services.FOURSQUARE.id)
							    {
							    	urlStr += accessToken.getToken() + "&v=20130606";
							    } else if (serviceID == Services.INSTAGRAM.id) {
							    	urlStr += accessToken.getToken();
							    } else if (serviceID == Services.YOUTUBE.id) {
							    	urlStr += accessToken.getToken() + "&alt=json";
							    } else if (serviceID == Services.GOOGLEPLUS.id) {
							    	urlStr += accessToken.getToken() + "&alt=json";
							    } 
							    
							    OAuthRequest request = new OAuthRequest(Verb.GET,urlStr);

							    Token t = new Token(accessToken.getToken(),accessToken.getSecret());
							    if (!(uses_oauth2) || serviceID == Services.BLOGGER.id)
							    {							    
							    	mService.signRequest(t, request);
							    }
							    
							    Response response = null;
							    try {
							        response = request.send();
						    		Log.d("response",String.valueOf(response.getCode()));
							    	if ( response.isSuccessful() )
							    	{	
						                JSONObject mainObject = new JSONObject(response.getBody());
						                if (serviceID == Services.FLICKR.id)
						                {
						                    JSONObject user = mainObject.getJSONObject("user");
						                    data = user.getString(service.idFieldName);
						                    //username = user.getString("url").replaceFirst("http://www.flickr.com/people/","");
						                    // for flickr make 1 more api request to get username
									    	request = new OAuthRequest(Verb.GET,"http://api.flickr.com/services/rest/?method=flickr.people.getInfo&user_id="+data+"&format=json&nojsoncallback=1");
										    try {	
											    t = new Token(accessToken.getToken(),accessToken.getSecret());
						    
											    mService.signRequest(t, request);
                                                response = request.send();
            							    	if ( response.isSuccessful() )
            							    	{	
            						                mainObject = new JSONObject(response.getBody());
            						                Log.d("response",response.getBody());
            						                JSONObject person = mainObject.getJSONObject("person");
            						                JSONObject uname = person.getJSONObject("username");
            						                username = uname.getString("_content");
            						                Log.d("here",username);
            							    	}
										    }
										    catch ( Exception e ) {
										    	e.printStackTrace();
										    }
						                } else if (serviceID == Services.FOURSQUARE.id)
						                {
						                    JSONObject resp = mainObject.getJSONObject("response");
						                    JSONObject user = resp.getJSONObject("user");
						                    data = user.getString(service.idFieldName);
						                    username = user.getString("firstName") + " " + user.getString("lastName");
						                }else if (serviceID == Services.INSTAGRAM.id)
						                {
						                    JSONObject resp = mainObject.getJSONObject("data");
						                    //data = resp.getString(service.idFieldName);
						                    username = resp.getString("username");
						                    data = username;
						                }else if (serviceID == Services.LINKEDIN.id)
						                {

						                    //data = mainObject.getString(service.idFieldName);
						                    firstname = mainObject.getString("firstName");
						                    lastname = mainObject.getString("lastName");
						                    email = mainObject.getString("emailAddress");
						                    JSONObject req = mainObject.getJSONObject("siteStandardProfileRequest");
						                    profileURL = req.getString("url");
						                	data = profileURL;
						                	username = email;
						                } else if (serviceID == Services.TWITTER.id) {
							                data = mainObject.getString("screen_name");	
							                username = mainObject.getString("screen_name");
							                userID = mainObject.getString(service.idFieldName);
						                } else if (serviceID == Services.TUMBLR.id) {
						                    JSONObject resp = mainObject.getJSONObject("response");
						                    JSONObject user = resp.getJSONObject("user");
						                    JSONArray blogs = user.getJSONArray("blogs");
						                    JSONObject pblog = blogs.getJSONObject(0);					                    
							                data = pblog.getString("name");	
							                username = user.getString("name");
							                Log.d("here",data);
							                //userID = mainObject.getString(service.idFieldName);
						                }else if (serviceID == Services.YOUTUBE.id) {
						                    JSONObject resp = mainObject.getJSONObject("entry");
						                    JSONObject user = resp.getJSONObject("yt$username");
						                    data = user.getString("$t");
						                    username = data;
							                Log.d("here",data);
							                //userID = mainObject.getString(service.idFieldName);
						                } else if (serviceID == Services.BLOGGER.id) {
						                    JSONArray items = mainObject.getJSONArray("items");
						                    JSONObject first = items.getJSONObject(0);
						                    data = first.getString("url");
						                    username = data.replace("http://", "");
						                    username = username.replace(".blogspot.com/","");
							                Log.d("here",data);
						                } else if (serviceID == Services.GOOGLEPLUS.id) {
						                    data = mainObject.getString("id");
						                    username = mainObject.getString("displayName");
							                Log.d("here",data);
						                }
						                else {
							                data = mainObject.getString(service.idFieldName);						                	
						                }
						                UpdateDatabase.updateDatabase(data, serviceID, activity);
							    	} else {
							    		// Http request failed so just finish
						        		runOnUiThread(new Runnable() {
						        		    public void run() {
								                Toast.makeText(activity, "Could not add " + service.name + " information!", Toast.LENGTH_LONG).show();
						        		    }
						        		});
				        		
							  		    finish();
							    	}
							    }
							    catch ( Exception e ) {
							    	e.printStackTrace();
							    }
							    
				        		// Store the tokens in preferences for further use
				        		SharedPreferences.Editor editor = mPrefs.edit();
				        		editor.putString(service.name+"_access_token", accessToken.getToken());
				        		editor.putString(service.name+"_access_secret", accessToken.getSecret());
				        		editor.putString(service.name+"_username",username);
				        		editor.putString(service.name+"_data", data);
				        		editor.putString(service.name+"_profileurl",profileURL);
				        		editor.putString(service.name+"_userid",userID);
				        		if (serviceID == Services.LINKEDIN.id)
				        		{
					        		editor.putString(service.name+"_emailAddress", email );
					        		editor.putString(service.name+"_firstName", firstname);
					        		editor.putString(service.name+"_lastName", lastname);
				        		}
				        		editor.commit();    
				        		
				        		runOnUiThread(new Runnable() {
				        		    public void run() {
						                Toast.makeText(activity, "Added " + service.name + " information!", Toast.LENGTH_LONG).show();
				        		    }
				        		});

				        		
					  		    finish();

							}
					    };
					    t1.start();
					
					} else {
						
						// If not oauth link, it is an intermediate page so dismiss loading dialog here				
			            //if (progDialog != null)
			            //    progDialog.dismiss();
					}
	
				    return false;
			    }
		    });

		    mWebView.getSettings().setJavaScriptEnabled(true);
		    if (serviceID == Services.FLICKR.id) {
			    String userAgent = "User-Agent: Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)";
			    mWebView.getSettings().setUserAgentString(userAgent);
		    }
		    mWebView.loadUrl(authURL);
		}
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oauth_layout);
        
        activity = this;
        
        // Determine service
        Bundle extras = getIntent().getExtras();
        serviceID = extras.getInt("serviceID");
        
        // Check shared preferences for token
        mPrefs = getSharedPreferences("tokens", 0);
        Log.d("sharedpref",mPrefs.getString("LinkedIn_access_token","-1"));
        
        if (serviceID == Services.TWITTER.id)
        {
        	service = Services.TWITTER;
            mService = new ServiceBuilder()
    		.provider(TwitterApi.class)
    		.apiKey(service.apiKey)
    		.apiSecret(service.apiSecret)
    		.callback(service.callbackURL)
    		.build();
        } else if (serviceID == Services.LINKEDIN.id)
        {
        	service = Services.LINKEDIN;
            mService = new ServiceBuilder()
    		.provider(LinkedInApi.class)
    		.apiKey(service.apiKey)
    		.apiSecret(service.apiSecret)
    		.callback(service.callbackURL)
                    .scope(service.scope)
    		.build();
        } else if (serviceID == Services.FLICKR.id)
        {
        	service = Services.FLICKR;
            mService = new ServiceBuilder()
    		.provider(FlickrApi.class)
    		.apiKey(service.apiKey)
    		.apiSecret(service.apiSecret)
    		.callback(service.callbackURL)
    		.build();
        } else if (serviceID == Services.FOURSQUARE.id)
        {
        	service = Services.FOURSQUARE;
            mService = new ServiceBuilder()
    		.provider(Foursquare2Api.class)
    		.apiKey(service.apiKey)
    		.apiSecret(service.apiSecret)
    		.callback(service.callbackURL)
    		.build();
            
            uses_oauth2 = true;
        } else if (serviceID == Services.INSTAGRAM.id)
        {
        	service = Services.INSTAGRAM;
            mService = new ServiceBuilder()
    		.provider(InstagramApi.class)
    		.apiKey(service.apiKey)
    		.apiSecret(service.apiSecret)
    		.callback(service.callbackURL)
    		.scope(service.scope)
    		.build();
            
            uses_oauth2 = true;
            
        } else if (serviceID == Services.TUMBLR.id)
        {
        	service = Services.TUMBLR;
            mService = new ServiceBuilder()
    		.provider(TumblrApi.class)
    		.apiKey(service.apiKey)
    		.apiSecret(service.apiSecret)
    		.callback(service.callbackURL)
    		.build();
        } else if (serviceID == Services.YOUTUBE.id)
        {
        	service = Services.YOUTUBE;
            mService = new ServiceBuilder()
    		.provider(YoutubeApi.class)
    		.apiKey(service.apiKey)
    		.apiSecret("9999")
    		.callback(service.callbackURL)
    		.build();
            
            uses_oauth2 = true;
            
        } else if (serviceID == Services.PINTEREST.id)
        {
        	service = Services.PINTEREST;
            mService = new ServiceBuilder()
    		.provider(PinterestApi.class)
    		.apiKey(service.apiKey)
    		.apiSecret("9999")
    		.callback(service.callbackURL)
    		.build();
            
            uses_oauth2 = true;        	
        } else if (serviceID == Services.BLOGGER.id)
        {
        	service = Services.BLOGGER;
            mService = new ServiceBuilder()
    		.provider(BloggerApi.class)
    		.apiKey(service.apiKey)
    		.apiSecret("9999")
    		.callback(service.callbackURL)
    		.build();
            
            uses_oauth2 = true;
            
        } else if (serviceID == Services.GOOGLEPLUS.id)
        {
        	service = Services.GOOGLEPLUS;
            mService = new ServiceBuilder()
    		.provider(GooglePlusApi.class)
    		.apiKey(service.apiKey)
    		.apiSecret("9999")
    		.callback(service.callbackURL)
    		.build();
            
            uses_oauth2 = true;
            
        }
        

        
        new OAuthTask().execute();
    }
        
}
