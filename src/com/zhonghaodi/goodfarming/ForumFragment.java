package com.zhonghaodi.goodfarming;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.Type;
import com.easemob.chat.TextMessageBody;
import com.easemob.exceptions.EaseMobException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhonghaodi.customui.GFToast;
import com.zhonghaodi.customui.HoldMessage;
import com.zhonghaodi.goodfarming.AgrotechnicalActivity.AgroAdapter;
import com.zhonghaodi.goodfarming.AgrotechnicalActivity.AgroHolder;
import com.zhonghaodi.model.Agrotechnical;
import com.zhonghaodi.model.Category_disease;
import com.zhonghaodi.model.ComparatorSort;
import com.zhonghaodi.model.Disease;
import com.zhonghaodi.model.GFMessage;
import com.zhonghaodi.model.GFUserDictionary;
import com.zhonghaodi.model.User;
import com.zhonghaodi.networking.GFDate;
import com.zhonghaodi.networking.GFHandler;
import com.zhonghaodi.networking.HttpUtil;
import com.zhonghaodi.networking.ImageOptions;
import com.zhonghaodi.networking.GFHandler.HandMessage;
import com.zhonghaodi.utils.PublicHelper;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ForumFragment extends Fragment implements OnClickListener,HandMessage {
	
	private PullToRefreshListView pullToRefreshListView;
	private LinearLayout tabLayout;
	private List<Agrotechnical> agrotechnicals;
	private List<Category_disease> categorys;
	private AgroAdapter adapter;
	private GFHandler<ForumFragment> handler = new GFHandler<ForumFragment>(this);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_forum, container,
				false);
		tabLayout = (LinearLayout)view.findViewById(R.id.tabhost);
		pullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.pull_refresh_list);
		
		pullToRefreshListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
//				Agrotechnical agrotechnical = agrotechnicals.get(position-1);
//				Intent intent = new Intent(AgrotechnicalActivity.this, AgroActivity.class);
//				intent.putExtra("aid", agrotechnical.getId());
//				AgrotechnicalActivity.this.startActivity(intent);
			}
		});		
		pullToRefreshListView.setMode(Mode.PULL_FROM_END);
		pullToRefreshListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

					@Override
					public void onPullDownToRefresh(
							PullToRefreshBase<ListView> refreshView) {

					}

					@Override
					public void onPullUpToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						if (agrotechnicals.size() == 0) {
							return;
						}
						loadMoreData(agrotechnicals.get(agrotechnicals.size()-1).getId());
					}

				});
		
		agrotechnicals = new ArrayList<Agrotechnical>();
		adapter = new AgroAdapter();
		pullToRefreshListView.getRefreshableView().setAdapter(adapter);	
		loadCategory();
		return view;
	}
	
	public void loadCategory(){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String jsonString = HttpUtil.getAgrotechnicalCates();
				Message msg = handler.obtainMessage();
				msg.what = 2;
				msg.obj = jsonString;
				msg.sendToTarget();				
			}
		}).start();
	}
	
	public void loadData(){
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String jsonString = HttpUtil.getAgrotechnical();
				Message msg = handler.obtainMessage();
				msg.what = 0;
				msg.obj = jsonString;
				msg.sendToTarget();				
			}
		}).start();
		
	}
	
	private void loadMoreData(final int fromid){
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String jsonString = HttpUtil.getMoreAgrotechnical(fromid);
				Message msg = handler.obtainMessage();
				msg.what = 1;
				msg.obj = jsonString;
				msg.sendToTarget();				
			}
		}).start();
		
	}
	
	public void createTabViews(){
		
//		Comparator comp = new ComparatorSort();  
//        Collections.sort(categorys,comp);
//		
//		tabLayout.removeAllViews();
//		for(int i=0;i<categorys.size();i++){
//			
//			TextView tabView = new TextView(getActivity());
//			int height = PublicHelper.dip2px(getActivity(), 34);
//			LayoutParams layoutParams = new LinearLayout.LayoutParams(0, height, 1);
//			tabView.setGravity(Gravity.CENTER);
//			tabView.setText(cDiseases.get(i).getName());
//			tabView.setBackgroundResource(R.drawable.topbar);
//			int pix = PublicHelper.dip2px(this, 8);
//			tabView.setPadding(pix, pix, pix, pix);
//			tabView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
//			tabView.setTag(cDiseases.get(i).getId());
//			tabLayout.addView(tabView, layoutParams);
//			tabView.setOnClickListener(this);
//		}
//		selectTextView(tabLayout.getChildAt(0));
		
	}
	
	class AgroHolder{
		public ImageView agroIv;
		public TextView titleTv;
		public TextView timeTv;
		 public AgroHolder(View view){
			 agroIv=(ImageView)view.findViewById(R.id.head_image);
			 titleTv=(TextView)view.findViewById(R.id.title_text);
			 timeTv=(TextView)view.findViewById(R.id.time_text);
		 }
	}
	
	class AgroAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return agrotechnicals.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return agrotechnicals.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			AgroHolder agroholder;;
			if(convertView==null){
				convertView = LayoutInflater.from(getActivity())
						.inflate(R.layout.cell_agrotechnical, parent, false);
				agroholder = new AgroHolder(convertView);
				convertView.setTag(agroholder);
			}
			
			agroholder=(AgroHolder)convertView.getTag();
			Agrotechnical agrotechnical = agrotechnicals.get(position);
			if (agrotechnical.getThumbnail()!=null) {
				ImageLoader.getInstance().displayImage(HttpUtil.ImageUrl+"agrotechnicals/small/"+agrotechnical.getThumbnail(), agroholder.agroIv, ImageOptions.optionsNoPlaceholder);
			}
			agroholder.titleTv.setText(agrotechnical.getTitle());
			agroholder.timeTv.setText(agrotechnical.getTime());
			return convertView;
		}
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleMessage(Message msg, Object object) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case 0:
		case 1:
			if (msg.obj != null) {
				Gson gson = new Gson();
				List<Agrotechnical> agrs = gson.fromJson(msg.obj.toString(),
						new TypeToken<List<Agrotechnical>>() {
						}.getType());
				if (msg.what == 0) {
					agrotechnicals.clear();
				}
				for (Agrotechnical agrotechnical: agrs) {
					agrotechnicals.add(agrotechnical);
				}
				adapter.notifyDataSetChanged();
				
			} else {
				GFToast.show("连接服务器失败,请稍候再试!");
			}
			pullToRefreshListView.onRefreshComplete();
			break;
		case 2:
			if(msg.obj!=null){
				Gson gson = new Gson();
				categorys = gson.fromJson(msg.obj.toString(),
						new TypeToken<List<Category_disease>>() {
						}.getType());
				
			}
			else{
				
			}
			break;

		default:
			break;
		}
		
	}
	
}