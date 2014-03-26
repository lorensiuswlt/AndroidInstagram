package net.londatiga.android.instagram;

import net.londatiga.android.instagram.R;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.content.Context;

import android.view.Display;
import android.view.ViewGroup;
import android.view.Window;

import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Authentication and authorization dialog.
 * 
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 *
 */
@SuppressLint({ "NewApi", "SetJavaScriptEnabled" })
public class InstagramDialog extends Dialog {
	private ProgressDialog mSpinner;
	private WebView mWebView;
	private LinearLayout mContent;
	private TextView mTitle;
	
	private String mAuthUrl;
	private String mRedirectUri;
	
	private InstagramDialogListener mListener;
	
	static final FrameLayout.LayoutParams FILL = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.MATCH_PARENT);
	
	static final int MARGIN = 8;
	static final int PADDING = 2;
	
	static final String TAG = "Instagram-Android";
	
	public InstagramDialog(Context context, String authUrl, String redirectUri, InstagramDialogListener listener) {
		super(context);
		
		mAuthUrl		= authUrl;
		mListener		= listener;
		mRedirectUri	= redirectUri;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSpinner = new ProgressDialog(getContext());
	        
		mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mSpinner.setMessage("Loading...");

		mContent = new LinearLayout(getContext());
	        
		mContent.setOrientation(LinearLayout.VERTICAL);
	        
		setUpTitle();
		
		setUpWebView();
	        
		Display display 	= getWindow().getWindowManager().getDefaultDisplay();
		Point outSize		= new Point();
		
		int width			= 0;
		int height			= 0;
		
		double[] dimensions = new double[2];
		        
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			display.getSize(outSize);
			
			width	= outSize.x;
			height	= outSize.y;
		} else {
			width	= display.getWidth();
			height	= display.getHeight();
		}
		
		if (width < height) {
			dimensions[0]	= 0.87 * width;
	        dimensions[1]	= 0.82 * height;
		} else {
			dimensions[0]	= 0.75 * width;
			dimensions[1]	= 0.75 * height;	        
		}
	        
		addContentView(mContent, new FrameLayout.LayoutParams((int) dimensions[0], (int) dimensions[1]));
	}

	private void setUpTitle() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	        
		Drawable icon = getContext().getResources().getDrawable(R.drawable.icon);
	        
		mTitle = new TextView(getContext());
	        
		mTitle.setText("Instagram");
		mTitle.setTextColor(Color.WHITE);
		mTitle.setTypeface(Typeface.DEFAULT_BOLD);
		mTitle.setBackgroundColor(0xFF163753);
		mTitle.setPadding(MARGIN + PADDING, MARGIN, MARGIN, MARGIN);
		mTitle.setCompoundDrawablePadding(MARGIN + PADDING);
		mTitle.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
	        
		mContent.addView(mTitle);
	}

	private void setUpWebView() {
		mWebView = new WebView(getContext());
	        
		mWebView.setVerticalScrollBarEnabled(false);
		mWebView.setHorizontalScrollBarEnabled(false);
		mWebView.setWebViewClient(new InstagramWebViewClient());
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.loadUrl(mAuthUrl);
		mWebView.setLayoutParams(FILL);
	        
		WebSettings webSettings = mWebView.getSettings();
		
		webSettings.setSavePassword(false);
		webSettings.setSaveFormData(false);
		
		mContent.addView(mWebView);
	}

	public void clearCache() {
		mWebView.clearCache(true);
		mWebView.clearHistory();
		mWebView.clearFormData();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		mListener.onCancel();
		
	}
	private class InstagramWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.d(TAG, "Redirecting URL " + url);
	        	
			if (url.startsWith(mRedirectUri)) {
				if (url.contains("code")) {
					String temp[] = url.split("=");
					
					mListener.onSuccess(temp[1]);
				} else if (url.contains("error")) {
					String temp[] = url.split("=");
					
					mListener.onError(temp[temp.length-1]);
				}
	        		
	        	InstagramDialog.this.dismiss();
	        		
	        	return true;
			}
			
			return false;
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {	
			super.onReceivedError(view, errorCode, description, failingUrl);
	      
			mListener.onError(description);
	            
			InstagramDialog.this.dismiss();
			
			Log.d(TAG, "Page error: " + description);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			
			mSpinner.show();
			
			Log.d(TAG, "Loading URL: " + url);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			
			String title = mWebView.getTitle();
			
			if (title != null && title.length() > 0) {
				mTitle.setText(title);
			}
			
			mSpinner.dismiss();
		}
	}
	
	public interface InstagramDialogListener {
		public abstract void onSuccess(String code);
		public abstract void onCancel();
		public abstract void onError(String error);
	}
}