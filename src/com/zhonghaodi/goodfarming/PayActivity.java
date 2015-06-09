package com.zhonghaodi.goodfarming;

import com.zhonghaodi.customui.GFToast;
import com.zhonghaodi.customui.MyEditText;
import com.zhonghaodi.customui.MyTextButton;
import com.zhonghaodi.model.GFUserDictionary;
import com.zhonghaodi.model.User;
import com.zhonghaodi.networking.GFHandler;
import com.zhonghaodi.networking.HttpUtil;
import com.zhonghaodi.networking.GFHandler.HandMessage;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class PayActivity extends Activity implements HandMessage,OnClickListener {
	private static final int QRCODE = 0;
	private static final int CANCELCODE = 1;
	private static final int CHECKPAY = 2;
	private User user;
	private MyEditText countExt;
	private MyEditText passExt;
	private MyTextButton backBtn;
	private MyTextButton confirmBtn;
	private LinearLayout payLayout;
	private LinearLayout codeLayout;
	private ImageView codeImageView;
	private MyTextButton cancelBtn;
	private MyTextButton okBtn;
	private GFHandler<PayActivity> handler = new GFHandler<PayActivity>(this);
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pay);
		user = (User)getIntent().getSerializableExtra("user");
		countExt = (MyEditText)findViewById(R.id.count_ext);
		passExt = (MyEditText)findViewById(R.id.password_edit);
		payLayout = (LinearLayout)findViewById(R.id.pay_layout);
		codeLayout = (LinearLayout)findViewById(R.id.qrcode_layout);
		codeImageView = (ImageView)findViewById(R.id.qrcode_image);
		backBtn = (MyTextButton)findViewById(R.id.back_button);
		backBtn.setOnClickListener(this);
		confirmBtn = (MyTextButton)findViewById(R.id.confirm_button);
		confirmBtn.setOnClickListener(this);
		cancelBtn = (MyTextButton)findViewById(R.id.cancel_button);
		cancelBtn.setOnClickListener(this);
		okBtn = (MyTextButton)findViewById(R.id.ok_button);
		okBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back_button:
			if(codeLayout.getVisibility()==View.VISIBLE){
				cancelPay();
			}
			else{
				this.finish();
			}
			break;
		case R.id.confirm_button:
			sendPay();
			break;
		case R.id.cancel_button:
			cancelPay();
			break;
		case R.id.ok_button:
			okpay();
			break;

		default:
			break;
		}
		
	}
	
	private void sendPay(){
		
		String count = countExt.getText().toString();
		String pass = passExt.getText().toString();
		if(count.isEmpty()){
			GFToast.show("请输入金额");
			return;
		}
		if(pass.isEmpty()){
			GFToast.show("请输入密码");
			return;
		}
		if(!pass.equals(GFUserDictionary.getPassword())){
			GFToast.show("密码错误");
			return;
		}
		final int price = Integer.parseInt(count);
		if(price>user.getCurrency()){
			GFToast.show("余额不足");
			return;
		}
		final String uid = GFUserDictionary.getUserId();
		new Thread(new Runnable() {

			@Override
			public void run() {
				Bitmap bitmap= HttpUtil.getPayQRCode(uid, price);
				Message msg = handler.obtainMessage();
				msg.what = QRCODE;
				msg.obj = bitmap;
				msg.sendToTarget();
			}
		}).start();
		
	}
	
	private void cancelPay(){
		
		new AlertDialog.Builder(this)
		.setTitle("提示")
		.setMessage("确定取消支付吗？")
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				//确定按钮事件
				setResult(RESULT_OK);
				dialog.dismiss();
				final String mid = GFUserDictionary.getUserId();
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						String jsonString = HttpUtil.cancelPay(mid);
						Message msg = handler.obtainMessage();
						msg.what = CANCELCODE;
						msg.obj = jsonString;
						msg.sendToTarget();

					}
				}).start();
			}
		})
		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				//取消按钮事件
				dialog.dismiss();
			}
		})
		.show();
		
	}
	
	private void okpay(){
		final String mid = GFUserDictionary.getUserId();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String jsonString = HttpUtil.checkPay(mid);
				Message msg = handler.obtainMessage();
				msg.what = CHECKPAY;
				msg.obj = jsonString;
				msg.sendToTarget();

			}
		}).start();
	}
	
	/***
	 * 监听返回按键
	 */
    @Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
    	if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) { 
	        if (event.getAction() == KeyEvent.ACTION_DOWN) { 
	        	if(codeLayout.getVisibility()==View.VISIBLE){
					cancelPay();
				}
				else{
					this.finish();
				}
	        	return true;
	        } 
	    } 
		return super.dispatchKeyEvent(event);

	}
	

	@Override
	public void handleMessage(Message msg, Object object) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case QRCODE:
			if (msg.obj != null) {
				codeLayout.setVisibility(View.VISIBLE);
				payLayout.setVisibility(View.GONE);
				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
			     hideSoftInputFromWindow(passExt.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS); 
				codeImageView.setImageBitmap((Bitmap)msg.obj);
			} else {
				GFToast.show("错误");
			}
			break;
		case CANCELCODE:
			if (msg.obj != null) {
				String jsString = msg.obj.toString();
				if(jsString!=""){
					GFToast.show("取消支付出错！");
				}
				else{
					GFToast.show("支付已取消");
					codeLayout.setVisibility(View.GONE);
					payLayout.setVisibility(View.VISIBLE);
				}
				
			} else {
				
				GFToast.show("取消支付出错！");
			}
			break;
		case CHECKPAY:
			if (msg.obj != null) {
				String okString = msg.obj.toString();
				if(okString.equals("true")){
					this.finish();
				}
				else{
					GFToast.show("支付尚未完成。");
				}
			} else {
				GFToast.show("错误");
			}
			break;

		default:
			break;
		}
	}

}
