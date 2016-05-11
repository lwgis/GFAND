package com.zhonghaodi.goodfarming;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONObject;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.umeng.analytics.MobclickAgent;
import com.zhonghaodi.customui.GFToast;
import com.zhonghaodi.customui.SharePopupwindow;
import com.zhonghaodi.networking.HttpUtil;
import com.zhonghaodi.networking.ImageOptions;
import com.zhonghaodi.utils.PublicHelper;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class AgrotechnicalActivity extends Activity implements OnClickListener {
	int id;
	String image;
	String title;
	String content;
	public IWXAPI wxApi;
	public Tencent mTencent;
	SharePopupwindow sharePopupwindow;
	ImageView agroImageView;
	Bitmap bitmap;
	byte[] data;
	WebView webview;
	private FrameLayout mFullscreenContainer;  
    private LinearLayout mContentView;  
    private View mCustomView = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_agrotechnical);
		MobclickAgent.openActivityDurationTrack(false);
		agroImageView = (ImageView)findViewById(R.id.agro_image);
		Button cancelBtn = (Button) findViewById(R.id.cancel_button);
		cancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		Button shareButton = (Button)findViewById(R.id.share_button);
		shareButton.setOnClickListener(this);
		wxApi=WXAPIFactory.createWXAPI(this,HttpUtil.WX_APP_ID, true);
		wxApi.registerApp(HttpUtil.WX_APP_ID);
		mTencent = Tencent.createInstance(HttpUtil.QQ_APP_ID, this.getApplicationContext());
		id = getIntent().getIntExtra("id", 0);
		image = getIntent().getStringExtra("image");
		title = getIntent().getStringExtra("title");
		content = getIntent().getStringExtra("content");
		if(content.length()>50){
			content = content.substring(0, 49)+"……";
		}
		
		initView();  
        initWebView();  
  
        if (PublicHelper.getPhoneAndroidSDK() >= 14) {// 4.0 需打开硬件加速  
            getWindow().setFlags(0x1000000, 0x1000000);  
        }  
		webview.loadUrl(HttpUtil.ViewUrl+"agrotechnical/detail?id="+id+"&f=1");
		loadImage();
	}
	
	private void initView(){
		mFullscreenContainer = (FrameLayout) findViewById(R.id.fullscreen_custom_content);  
        mContentView = (LinearLayout) findViewById(R.id.main_content);  
        webview  = (WebView)findViewById(R.id.webView);
	}
	
	private void initWebView(){
		WebSettings settings = webview.getSettings();  
        settings.setJavaScriptEnabled(true);  
        settings.setJavaScriptCanOpenWindowsAutomatically(true);  
        settings.setPluginState(PluginState.ON);  
        // settings.setPluginsEnabled(true);  
        settings.setAllowFileAccess(true);  
        settings.setLoadWithOverviewMode(true);  
  
        webview.setWebChromeClient(new MyWebChromeClient());  
        webview.setWebViewClient(new MyWebViewClient());
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		webview.onResume();
		MobclickAgent.onPageStart("田间地头内容");
		MobclickAgent.onResume(this);
	}



	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		webview.onPause();
		MobclickAgent.onPageEnd("田间地头内容");
		MobclickAgent.onPause(this);
	}



	public void loadImage(){
		ImageLoader.getInstance().displayImage(HttpUtil.ImageUrl+"agrotechnicals/small/"+image, agroImageView, ImageOptions.optionsNoPlaceholder);
		agroImageView.setDrawingCacheEnabled(true);
	}
	
	public void popwindow(){
    	sharePopupwindow = new SharePopupwindow(this,this);
    	sharePopupwindow.setFocusable(true);
    	sharePopupwindow.setOutsideTouchable(true);
    	sharePopupwindow.update();
    	ColorDrawable dw = new ColorDrawable(0xb0000000);
    	sharePopupwindow.setBackgroundDrawable(dw);
		sharePopupwindow.showAtLocation(findViewById(R.id.main), 
				Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
    }
    
    private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}
    
    class MyWebChromeClient extends WebChromeClient {  
    	  
        private CustomViewCallback mCustomViewCallback;  
        private int mOriginalOrientation = 1;  
  
        @Override  
        public void onShowCustomView(View view, CustomViewCallback callback) {  
            // TODO Auto-generated method stub  
            onShowCustomView(view, mOriginalOrientation, callback);  
            super.onShowCustomView(view, callback);  
  
        }  
  
        public void onShowCustomView(View view, int requestedOrientation,  
                WebChromeClient.CustomViewCallback callback) {  
            if (mCustomView != null) {  
                callback.onCustomViewHidden();  
                return;  
            }  
            if (PublicHelper.getPhoneAndroidSDK() >= 14) {  
                mFullscreenContainer.addView(view);  
                mCustomView = view;  
                mCustomViewCallback = callback;  
                mOriginalOrientation = getRequestedOrientation();  
                mContentView.setVisibility(View.GONE);  
                mFullscreenContainer.setVisibility(View.VISIBLE);  
                mFullscreenContainer.bringToFront();  
  
                setRequestedOrientation(mOriginalOrientation);  
            }  
  
        }  
  
        public void onHideCustomView() {  
            mContentView.setVisibility(View.VISIBLE);  
            if (mCustomView == null) {  
                return;  
            }  
            mCustomView.setVisibility(View.GONE);  
            mFullscreenContainer.removeView(mCustomView);  
            mCustomView = null;  
            mFullscreenContainer.setVisibility(View.GONE);  
            try {  
                mCustomViewCallback.onCustomViewHidden();  
            } catch (Exception e) {  
            }  
            // Show the content view.  
  
            setRequestedOrientation(mOriginalOrientation);  
        }  
  
    }  
  
    class MyWebViewClient extends WebViewClient {  
  
        @Override  
        public boolean shouldOverrideUrlLoading(WebView view, String url) {  
            // TODO Auto-generated method stub  
            view.loadUrl(url);  
            return super.shouldOverrideUrlLoading(view, url);  
        }  
  
    }
    
    class BaseUiListener implements IUiListener {
		
		protected void doComplete(JSONObject values) {
			
		}
		@Override
		public void onError(UiError e) {
		}
		@Override
		public void onCancel() {
		}
		@Override
		public void onComplete(Object arg0) {
			// TODO Auto-generated method stub
		}
		
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (bitmap==null) {
			Bitmap b = agroImageView.getDrawingCache();
			int WX_THUMB_SIZE = 60;			 			             
			bitmap = Bitmap.createScaledBitmap(b, WX_THUMB_SIZE, WX_THUMB_SIZE, true);
			data = PublicHelper.bmpToByteArray(bitmap, true);
			bitmap.recycle();
		}
		switch (v.getId()) {
		case R.id.share_button:
			popwindow();
			break;
		case R.id.img_share_weixin:
			if(!wxApi.isWXAppInstalled()){
				GFToast.show(getApplicationContext(),"您还未安装微信客户端");
				return;
			}
			WXWebpageObject webpage = new WXWebpageObject();
			webpage.webpageUrl = HttpUtil.ViewUrl+"agrotechnical/detail?id="+id;
			WXMediaMessage msg = new WXMediaMessage(webpage);
			msg.title = title;
			msg.description = content;
			msg.thumbData = data;
			
			SendMessageToWX.Req req = new SendMessageToWX.Req();
			req.transaction = buildTransaction("webpage");
			req.message = msg;
			req.scene=SendMessageToWX.Req.WXSceneSession;
			wxApi.sendReq(req);
			sharePopupwindow.dismiss();
			break;
		case R.id.img_share_circlefriends:
			if(!wxApi.isWXAppInstalled()){
				GFToast.show(getApplicationContext(),"您还未安装微信客户端");
				return;
			}
			WXWebpageObject webpage1 = new WXWebpageObject();
			webpage1.webpageUrl = HttpUtil.ViewUrl+"agrotechnical/detail?id="+id;
			WXMediaMessage msg1 = new WXMediaMessage(webpage1);
			msg1.title = title;
			msg1.description = content;
			msg1.thumbData = data;
			
			SendMessageToWX.Req req1 = new SendMessageToWX.Req();
			req1.transaction = buildTransaction("webpage");
			req1.message = msg1;
			req1.scene=SendMessageToWX.Req.WXSceneTimeline;
			wxApi.sendReq(req1);
			sharePopupwindow.dismiss();
			break;
		case R.id.img_share_qq:
			Bundle params = new Bundle();
		    params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
		    params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
		    params.putString(QQShare.SHARE_TO_QQ_SUMMARY,  content);
		    params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,  HttpUtil.ViewUrl+"agrotechnical/detail?id="+id);
		    params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL,HttpUtil.ImageUrl+"agrotechnicals/small/"+image);
		    params.putString(QQShare.SHARE_TO_QQ_APP_NAME,  "种好地");
		    mTencent.shareToQQ(this, params, new BaseUiListener());
		    sharePopupwindow.dismiss();
			
			break;
		case R.id.img_share_qzone:
			Bundle params1 = new Bundle();
			params1.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE,QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT );
		    params1.putString(QzoneShare.SHARE_TO_QQ_TITLE, title);//必填
		    params1.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, content);//选填
		    params1.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, HttpUtil.ViewUrl+"agrotechnical/detail?id="+id);//必填
		    params1.putString(QzoneShare.SHARE_TO_QQ_IMAGE_URL, HttpUtil.ImageUrl+"agrotechnicals/small/"+image);
		    ArrayList<String> urlsList = new ArrayList<String>();
		    urlsList.add(HttpUtil.ImageUrl+"agrotechnicals/small/"+image);
		    params1.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, urlsList);
		    mTencent.shareToQzone(this, params1, new BaseUiListener());
		    sharePopupwindow.dismiss();
			break;
		default:
			break;
		}
	}

}
