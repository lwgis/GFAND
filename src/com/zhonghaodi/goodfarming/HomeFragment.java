package com.zhonghaodi.goodfarming;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.umeng.analytics.MobclickAgent;
import com.zhonghaodi.adapter.QuestionAdpter;
import com.zhonghaodi.api.DiseaseListView;
import com.zhonghaodi.api.ContentListView;
import com.zhonghaodi.api.ShareContainer;
import com.zhonghaodi.customui.DiseasePopupWindow;
import com.zhonghaodi.customui.DpTransform;
import com.zhonghaodi.customui.GFToast;
import com.zhonghaodi.customui.ContentPopupWindow;
import com.zhonghaodi.model.City;
import com.zhonghaodi.model.GFAreaUtil;
import com.zhonghaodi.model.GFUserDictionary;
import com.zhonghaodi.model.NetResponse;
import com.zhonghaodi.model.PostResponse;
import com.zhonghaodi.model.Question;
import com.zhonghaodi.model.SpinnerDto;
import com.zhonghaodi.model.User;
import com.zhonghaodi.networking.GFHandler;
import com.zhonghaodi.networking.GFHandler.HandMessage;
import com.zhonghaodi.utils.PublicHelper;
import com.zhonghaodi.networking.HttpUtil;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class HomeFragment extends Fragment implements HandMessage,
			OnClickListener,OnCreateContextMenuListener,DiseaseListView {
	private PullToRefreshListView pullToRefreshListView;
	
	private QuestionAdpter adapter;
	private GFHandler<HomeFragment> handler = new GFHandler<HomeFragment>(this);
	private TextView titleView;
	private TextView diseaseTextView;
	private TextView plantTextView;
	private TextView gossipTextView;
	private View messageView;
	private TextView countTv;
	private Question selectQuestion;
	private PopupWindow popupWindow;
	private View popView;
	private DiseasePopupWindow diseasePopupWindow;
	private ShareContainer shareContainer;
	private City area1;
	private String zonestr;
	

	public ShareContainer getShareContainer() {
		return shareContainer;
	}

	public void setShareContainer(ShareContainer shareContainer) {
		this.shareContainer = shareContainer;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_home, container, false);
		ImageView questionButton = (ImageView) view
				.findViewById(R.id.question_button);
		questionButton.setOnClickListener(this);
		popView = inflater.inflate(R.layout.popupwindow_question, container,
				false);
		popupWindow = new PopupWindow(popView, DpTransform.dip2px(
				getActivity(), 180), DpTransform.dip2px(getActivity(), 150));
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		popupWindow.setFocusable(true);
		Button newQueBtn = (Button)popView.findViewById(R.id.btn_question);
		newQueBtn.setOnClickListener(this);
		Button newGossipBtn = (Button)popView.findViewById(R.id.btn_gossip);
		newGossipBtn.setOnClickListener(this);
		Button newPlantBtn = (Button)popView.findViewById(R.id.btn_plant);
		newPlantBtn.setOnClickListener(this);
		titleView = (TextView)view.findViewById(R.id.title_txt);
		Drawable drawable = getResources().getDrawable(R.drawable.location_white);
		drawable.setBounds(0, 0, PublicHelper.dip2px(getActivity(), 18), PublicHelper.dip2px(getActivity(), 18));
		titleView.setCompoundDrawables(drawable, null, null, null);
		titleView.setOnClickListener(this);
		diseaseTextView = (TextView)view.findViewById(R.id.disease_text);
		diseaseTextView.setOnClickListener(this);
		plantTextView = (TextView)view.findViewById(R.id.plant_text);
		plantTextView.setOnClickListener(this);
		gossipTextView = (TextView)view.findViewById(R.id.gossip_text);
		gossipTextView.setOnClickListener(this);
		messageView = view.findViewById(R.id.message_layout);
		messageView.setOnClickListener(this);
		countTv = (TextView) view.findViewById(R.id.count_text);
		
		pullToRefreshListView = (PullToRefreshListView) view
				.findViewById(R.id.pull_refresh_list);
		pullToRefreshListView.setMode(Mode.BOTH);
		pullToRefreshListView
				.setOnRefreshListener(new OnRefreshListener2<ListView>() {

					@Override
					public void onPullDownToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						if(UILApplication.displayStatus==0)
							loadNewQuestion();
						else if(UILApplication.displayStatus==1){
							loadNewGossips();
						}
						else if(UILApplication.displayStatus==2){
							loadNewPlant();
						}
					}

					@Override
					public void onPullUpToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						if (getAllQuestions().size() == 0) {
							return;
						}
						Question question = getAllQuestions().get(getAllQuestions().size() - 1);
						if(UILApplication.displayStatus==0)
							loadMoreQuestion(question.getId());
						else if(UILApplication.displayStatus==1){
							loadMoreGossips(question.getId());
						}
						else if(UILApplication.displayStatus==2){
							loadMorePlant(question.getId());
						}
					}

				});
		adapter = new QuestionAdpter(getAllQuestions(),getActivity(),HomeFragment.this,UILApplication.displayStatus);
		HomeFragment.this.pullToRefreshListView.getRefreshableView()
				.setAdapter(adapter);
		
		initArea();		
		this.pullToRefreshListView
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// TODO Auto-generated method stub
						if (GFUserDictionary.getUserId(getActivity().getApplicationContext()) != null){
							Intent it = new Intent(getActivity(),
									QuestionActivity.class);
							it.putExtra("questionId", getAllQuestions().get(position - 1).getId());
							if(UILApplication.displayStatus==1 || UILApplication.displayStatus==2){
								it.putExtra("status", UILApplication.displayStatus);
							}
							getActivity().startActivity(it);
						}
						else{
							Intent it = new Intent(getActivity(),
									LoginActivity.class);
							getActivity().startActivity(it);
						}
						
					}
				});
		this.pullToRefreshListView.getRefreshableView().setOnCreateContextMenuListener(this);
		diseasePopupWindow = new DiseasePopupWindow(this, UILApplication.diseaseStatus, getActivity());
		return view;
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onPageStart("主页Fragment");
		setUnreadMessageCount();
		
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPageEnd("主页Fragment");
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		String uid = GFUserDictionary.getUserId(getActivity().getApplicationContext());
		if(uid!=null){
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
			Question question = getAllQuestions().get(info.position-1);
			if(question.getWriter().getId().equals(uid)){
				menu.add(0, 0, 0, "删除");
			} 
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item 
                .getMenuInfo(); 
		selectQuestion = getAllQuestions().get(info.position-1);
		final Dialog dialog = new Dialog(getActivity(), R.style.MyDialog);
        //设置它的ContentView
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog, null);
        dialog.setContentView(layout);
        TextView contentView = (TextView)layout.findViewById(R.id.contentTxt);
        TextView titleView = (TextView)layout.findViewById(R.id.dialog_title);
        Button okBtn = (Button)layout.findViewById(R.id.dialog_button_ok);
        okBtn.setText("确定");
        okBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				delete(selectQuestion.getId());
			}
		});
        Button cancelButton = (Button)layout.findViewById(R.id.dialog_button_cancel);
        cancelButton.setText("取消");
        cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
        titleView.setText("提示");
        contentView.setText("确定要删除选中的提问吗？");
        dialog.show();
		return super.onContextItemSelected(item);
	}
	
	@Override
	public void changeDiseaseStatus(SpinnerDto spinnerDto) {
		// TODO Auto-generated method stub
		if(spinnerDto.getId()==1){
			String uid = GFUserDictionary.getUserId(getActivity().getApplicationContext());
			if(uid==null){
				diseasePopupWindow.reset();
				Intent intent = new Intent(getActivity(),LoginActivity.class);
				getActivity().startActivity(intent);
				return;
			}
			String croids = GFUserDictionary.getCroids(getActivity());
			if(TextUtils.isEmpty(croids)){
				diseasePopupWindow.reset();
				GFToast.show(getActivity(), "请设置关注的作物");
				Intent it = new Intent(getActivity(), InformationActivity.class);
				Bundle bundle = new Bundle();
				User user = getUser();
				bundle.putSerializable("user", user);
				it.putExtras(bundle);
				getActivity().startActivity(it);
				return;
			}
		}
		UILApplication.diseaseStatus = spinnerDto.getId();
		diseaseTextView.setText(spinnerDto.getName());		
		loadNewQuestion();		
	}
	
	
	public void resetArea(){
		zonestr="";
		int cityid = GFAreaUtil.getCity(getActivity());
		String name = GFAreaUtil.getCityName(getActivity());
		if(cityid!=0){
			zonestr+=cityid;
			
		}
		if(!TextUtils.isEmpty(name)){
			titleView.setText(name);
		}
		if(UILApplication.displayStatus==1){
			loadNewGossips();
		}	
	}
	
	public void initArea(){
		zonestr="";
		int cityid = GFAreaUtil.getCity(getActivity());
		String name = GFAreaUtil.getCityName(getActivity());
		if(cityid!=0){
			zonestr+=cityid;
		}
		if(!TextUtils.isEmpty(name)){
			titleView.setText(name);
		}
		if(UILApplication.displayStatus==0){
			if(getAllQuestions().size()==0){
				loadNewQuestion();
			}
			selectTextView(diseaseTextView);
			if(UILApplication.diseaseStatus==0){
				diseaseTextView.setText("病害问题");
			}
			else{
				diseaseTextView.setText("我的作物");
			}
		}			
		else if(UILApplication.displayStatus==1){
			if(getAllQuestions().size()==0){
				loadNewGossips();
			}
			
			selectTextView(gossipTextView);
		}
		else if(UILApplication.displayStatus==2){
			if(getAllQuestions().size()==0){
				loadNewPlant();
			}
			
			selectTextView(plantTextView);
		}

	}

	public void loadNewQuestion() {
		if(UILApplication.diseaseStatus==0){
			new Thread(new Runnable() {

				@Override
				public void run() {
					String jsonString;
					jsonString = HttpUtil.getQuestionsString("");
					Message msg = handler.obtainMessage();
					msg.what = 0;
					msg.obj = jsonString;
					msg.sendToTarget();
				}
			}).start();
		}
		else{
			
			new Thread(new Runnable() {

				@Override
				public void run() {
					String jsonString;
					int zone=0;
					if(area1!=null){
						zone = area1.getId();
					}
					String uid = GFUserDictionary.getUserId(getActivity());
					jsonString = HttpUtil.getAscQuestionsString(uid,zone);
					Message msg = handler.obtainMessage();
					msg.what = 0;
					msg.obj = jsonString;
					msg.sendToTarget();
				}
			}).start();
		}
		
	}

	private void loadMoreQuestion(final int qid) {
		if(UILApplication.diseaseStatus==0){
			new Thread(new Runnable() {

				@Override
				public void run() {
					String jsonString;
					int zone=0;
					if(area1!=null){
						zone = area1.getId();
					}
					jsonString = HttpUtil.getQuestionsString(qid,"");
					Message msg = handler.obtainMessage();
					msg.what = 1;
					msg.obj = jsonString;
					msg.sendToTarget();
				}
			}).start();
		}
		else{
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					String jsonString;
					int zone=0;
					if(area1!=null){
						zone = area1.getId();
					}
					String uid = GFUserDictionary.getUserId(getActivity());
					jsonString = HttpUtil.getAscQuestionsString(uid,qid,zone);
					Message msg = handler.obtainMessage();
					msg.what = 1;
					msg.obj = jsonString;
					msg.sendToTarget();
				}
			}).start();
		}
	}

	private void loadNewGossips() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				
				String jsonString = HttpUtil.getGossipsString(zonestr);
				Message msg = handler.obtainMessage();
				msg.what = 0;
				msg.obj = jsonString;
				msg.sendToTarget();
			}
		}).start();
	}

	private void loadMoreGossips(final int qid) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				
				String jsonString = HttpUtil.getGossipsString(qid,zonestr);
				Message msg = handler.obtainMessage();
				msg.what = 1;
				msg.obj = jsonString;
				msg.sendToTarget();
			}
		}).start();
	}
	
	private void loadNewPlant() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				int zone=0;
				String jsonString = HttpUtil.getPlant(zonestr);
				Message msg = handler.obtainMessage();
				msg.what = 0;
				msg.obj = jsonString;
				msg.sendToTarget();
			}
		}).start();
	}

	private void loadMorePlant(final int qid) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				int zone=0;
				String jsonString = HttpUtil.getMorePlant(qid,zonestr);
				Message msg = handler.obtainMessage();
				msg.what = 1;
				msg.obj = jsonString;
				msg.sendToTarget();
			}
		}).start();
	}
	
	/**
	 * 显示未读信息数
	 * @param count
	 */
	public void setUnreadMessageCount() {
		int count=0;
		// 获取所有会话，包括陌生人
		Hashtable<String, EMConversation> conversations = EMChatManager
				.getInstance().getAllConversations();
		for (EMConversation conversation : conversations.values()) {
			if (conversation.getAllMessages().size() != 0) {
				count+=conversation.getUnreadMsgCount();
			}
		}
		if (count == 0) {
			countTv.setVisibility(View.GONE);
		} else {
			countTv.setVisibility(View.VISIBLE);
			countTv.setText(String.valueOf(count));
		}
	}
	/**
	 * 显示未读信息数
	 * @param count
	 */
	public void setUnreadMessageCount1() {
		int count=1;

		if (count == 0) {
			countTv.setVisibility(View.GONE);
		} else {
			countTv.setVisibility(View.VISIBLE);
			countTv.setText(String.valueOf(count));
		}
	}
	
	private void delete(final int qid){
		new Thread(new Runnable() {

			@Override
			public void run() {
				String jsonString;
				if(UILApplication.displayStatus==0){
					jsonString = HttpUtil.deleteQuestion(qid);
				}
				else if(UILApplication.displayStatus==1){
					jsonString = HttpUtil.deleteGossip(qid);
				}
				else{
					jsonString = HttpUtil.deletePlant(qid);
				}
				Message msg = handler.obtainMessage();
				msg.what = 3;
				msg.obj = jsonString;
				msg.sendToTarget();
			}
		}).start();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.title_txt:
//			Intent cityIntent = new Intent(getActivity(), CityActivity.class);
//			getActivity().startActivityForResult(cityIntent, PublicHelper.CITY_REQUEST_CODE);
			Intent cityIntent = new Intent(getActivity(), ProvinceActivity.class);
			getActivity().startActivityForResult(cityIntent, PublicHelper.CITY_REQUEST_CODE);
			break;
		case R.id.question_button:
			if (popupWindow.isShowing()) {
				popupWindow.dismiss();
			} else {
				popupWindow.showAsDropDown(v,
						-DpTransform.dip2px(getActivity(), -50),
						DpTransform.dip2px(getActivity(), 0));
			}
			break;
		case R.id.message_layout:
			Intent it1 = new Intent();
			if (GFUserDictionary.getUserId(getActivity().getApplicationContext())==null) {
				it1.setClass(getActivity(), LoginActivity.class);
				
			}
			else {
				it1.setClass(getActivity(), MessagesActivity.class);			
			}
			getActivity().startActivity(it1);
			break;
			
		case R.id.disease_text:
			if(UILApplication.displayStatus!=0){
				selectTextView(v);
				loadNewQuestion();
				UILApplication.displayStatus = 0;
			}
			else{
				if(diseasePopupWindow.isShowing()){
					diseasePopupWindow.dismiss();
				}
				else{

					diseasePopupWindow.showAsDropDown((View)v.getParent(), 0, 0);
				}
			}
			break;
		case R.id.gossip_text:
			if(UILApplication.displayStatus!=1){
				String uid = GFUserDictionary.getUserId(getActivity().getApplicationContext());
				if(uid==null){
					Intent intent = new Intent(getActivity(),LoginActivity.class);
					getActivity().startActivity(intent);
				}
				else{
					selectTextView(v);
					loadNewGossips();					
					UILApplication.displayStatus = 1;
				}
			}
			break;
		case R.id.plant_text:
			if(UILApplication.displayStatus!=2){
				String uid1 = GFUserDictionary.getUserId(getActivity().getApplicationContext());
				if(uid1==null){
					Intent intent = new Intent(getActivity(),LoginActivity.class);
					getActivity().startActivity(intent);
				}
				else{
					selectTextView(v);
					loadNewPlant();
					UILApplication.displayStatus = 2;
				}
			}
			
			break;

		case R.id.btn_question:
			popupWindow.dismiss();
			Intent it = new Intent();
			if (GFUserDictionary.getUserId(getActivity().getApplicationContext())==null) {
				it.setClass(getActivity(), LoginActivity.class);
			}
			else {
				it.setClass(getActivity(), CreateQuestionActivity.class);
			}
			getActivity().startActivity(it);
			break;
		case R.id.btn_gossip:
			popupWindow.dismiss();
			Intent intent = new Intent();
			if (GFUserDictionary.getUserId(getActivity().getApplicationContext())==null) {
				intent.setClass(getActivity(), LoginActivity.class);
			}
			else {
				intent.setClass(getActivity(), CreateQuestionActivity.class);
				intent.putExtra("status", 1);
			}
			getActivity().startActivity(intent);
			break;
		case R.id.btn_plant:
			popupWindow.dismiss();
			Intent intent1 = new Intent();
			if (GFUserDictionary.getUserId(getActivity().getApplicationContext())==null) {
				intent1.setClass(getActivity(), LoginActivity.class);
			}
			else {
				intent1.setClass(getActivity(), CreatePlantActivity.class);
			}
			getActivity().startActivity(intent1);
			break;
		case R.id.plantzan_layout:
			selectQuestion = (Question)v.getTag();
			final String uid2 = GFUserDictionary.getUserId(getActivity().getApplicationContext());
			if(selectQuestion.getWriter().getId().equals(uid2)){
				GFToast.show(getActivity(),"不能给自己的分享点赞。");
				return;
			}
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					NetResponse netResponse=HttpUtil.agreePlant(selectQuestion.getId(),uid2);
					Message msg = handler.obtainMessage();
					if(netResponse.getStatus()==1){
						msg.what = 9;
						msg.obj = netResponse.getResult();
					}
					else{
						msg.what = 0;
						msg.obj = netResponse.getMessage();
					}
					msg.sendToTarget();
				}
			}).start();
			break;
		case R.id.forward_layout:
			
			if (GFUserDictionary.getUserId(getActivity().getApplicationContext())==null) {
				Intent intent2 = new Intent();
				intent2.setClass(getActivity(), LoginActivity.class);
				getActivity().startActivity(intent2);
				
			}
			else {				
				Question q = (Question)v.getTag();
				String folder;
				if(UILApplication.displayStatus==0){
					folder="question";
				}else if(UILApplication.displayStatus==1){
					folder="gossip";
				}else{
					folder="plantinfo";
				}
				shareContainer.shareQuestionWindow(q, folder);			
			}
			
			break;
		
		default:
			break;
		}
	}
	
	public void selectTextView(View view){
		Drawable drawable = getResources().getDrawable(R.drawable.dropdown);
		drawable.setBounds(0, 0, PublicHelper.dip2px(getActivity(), 15), PublicHelper.dip2px(getActivity(), 15)); 
		diseaseTextView.setTextColor(Color.rgb(128, 128, 128));
		diseaseTextView.setBackgroundDrawable(getResources().getDrawable(R.drawable.topbar_null));
		diseaseTextView.setCompoundDrawables(null, null, drawable, null);
		plantTextView.setTextColor(Color.rgb(128, 128, 128));
		plantTextView.setBackgroundDrawable(getResources().getDrawable(R.drawable.topbar_null));
		gossipTextView.setTextColor(Color.rgb(128, 128, 128));
		gossipTextView.setBackgroundDrawable(getResources().getDrawable(R.drawable.topbar_null));
		
		TextView selectTextView = (TextView)view;
		selectTextView.setTextColor(Color.rgb(56, 190, 153));
		if(selectTextView.getId()==R.id.disease_text){
			Drawable drawable1 = getResources().getDrawable(R.drawable.dropdown_s);
			drawable1.setBounds(0, 0, PublicHelper.dip2px(getActivity(), 15), PublicHelper.dip2px(getActivity(), 15)); 
			selectTextView.setCompoundDrawables(null, null, drawable1, null);
		}	
	}
	
	private List<Question> getAllQuestions(){
		return ((MainActivity)getActivity()).allQuestions;
	}
	
	private User getUser(){
		return UILApplication.user;
	}

	@Override
	public void handleMessage(Message msg,Object object) {
			if(getActivity()==null){
				return;
			}
			if(msg.what==0||msg.what==1){
				if (msg.obj != null) {
					Gson gson = new Gson();
					List<Question> questions = gson.fromJson(msg.obj.toString(),
							new TypeToken<List<Question>>() {
							}.getType());
					if (msg.what == 0) {
						getAllQuestions().clear();
					}

					for (Question question : questions) {
						if(question.getWriter()!=null){
							getAllQuestions().add(question);
						}						
					}
					if(UILApplication.displayStatus==0 || UILApplication.displayStatus==3){
						adapter.setStatus(0);
					}
					else{
						adapter.setStatus(UILApplication.displayStatus);
					}
					adapter.notifyDataSetChanged();
					if (msg.what == 0) {
						pullToRefreshListView.getRefreshableView().setSelection(0);
					}
				} else {
//					GFToast.show(getActivity(), "连接服务器失败,请稍候再试!");
				}
				pullToRefreshListView.onRefreshComplete();
			}
			else if(msg.what==3){
				
				String str = msg.obj.toString();
				if(!str.isEmpty()){
					GFToast.show(getActivity().getApplicationContext(),str);
				}
				else{
					getAllQuestions().remove(selectQuestion);
					adapter.notifyDataSetChanged();
				}
				
			}
			else if(msg.what==9){
				if(msg.obj!=null){
					Gson gson2 = new Gson();
					String jString = (String) msg.obj;
					PostResponse reportResponse = gson2.fromJson(jString, PostResponse.class);
					if(reportResponse == null){
						GFToast.show(getActivity(),"点赞操作错误");
					}
					else{
						if(reportResponse.isResult()){
							selectQuestion.setAgree(selectQuestion.getAgree()+1);
							adapter.notifyDataSetChanged();
						}
						else{
							GFToast.show(getActivity(),reportResponse.getMessage());
						}
					}
				}
				else{
					GFToast.show(getActivity(),"点赞操作错误");
				}
			}
			else if(msg.what==-1){
				if(msg.obj!=null){
					GFToast.show(getActivity(),msg.obj.toString());
				}
			}
	
	}
}
