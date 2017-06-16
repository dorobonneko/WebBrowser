package com.moe.fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.support.v4.view.ViewPager;
import android.widget.LinearLayout;
import android.util.TypedValue;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView;
import com.moe.adapter.ViewPagerAdapter;
import java.util.ArrayList;
import android.support.v7.widget.GridLayoutManager;
import com.moe.bean.MenuItem;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import com.moe.adapter.MenuAdapter;
import com.moe.Mbrowser.R;
import android.support.v7.widget.StaggeredGridLayoutManager;
import com.moe.widget.MenuGridLayoutManager;
import com.moe.widget.MenuViewPager;
import android.widget.Toast;
import android.view.Gravity;
import android.widget.AdapterView.OnItemClickListener;
import de.greenrobot.event.EventBus;
import com.moe.bean.MenuOptions;
import android.content.SharedPreferences;
import com.moe.dialog.ToolboxDialog;
import com.moe.entity.Bookmark;
import com.moe.database.BookMarks;
import com.moe.database.Sqlite;
import com.moe.internal.ToolManager;
import com.moe.webkit.WebViewManagerView;
import com.moe.webkit.WebSettings;

public class MenuFragment extends FragmentPop implements MenuAdapter.OnItemClickListener
{
public final static int SHOW=0XFF0002;
public final static int HIDE=0XFF0003;
public final static int SHUTDOWN=0xff005;
private ViewPagerAdapter vpa;
private ArrayList<View> av=new ArrayList<>();
private int groupSize=0;//计算几组
private SharedPreferences webview,moe;
private final static String xmlns="http://schemas.android.com/apk/res/android";
@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
		ViewPager vp=new MenuViewPager(getActivity().getApplicationContext(),av);
        LinearLayout.LayoutParams ll=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        //ll.setMargins(5,5,5,5);
        vp.setLayoutParams(ll);
        //Theme.registerTheme(vp);
		vp.setAdapter(vpa=new ViewPagerAdapter(av));
		try
		{
			parser(R.menu.menu);
		}
		catch (XmlPullParserException e)
		{}
		catch (IOException e)
		{}
		//vp.setBackgroundResource(R.color.window_background);
        return vp;
    }

	
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
		webview=getContext().getSharedPreferences("webview",0);
		moe=getContext().getSharedPreferences("moe",0);
		updateBlockImage();
		updateFull();
		updateAutoScreen();
		updateNightMode();
		updateDesktopMode();
		updateGps();
		updateScale();
    }

	@Override
	public void onItemClick(View v)
	{
		
		switch(v.getId()){
			case R.id.menu_nightmode:
				EventBus.getDefault().post(MenuOptions.NIGHTMODE);
				EventBus.getDefault().post(HIDE);
				updateNightMode();
				break;
			case R.id.menu_bookmark:
				EventBus.getDefault().post(MenuOptions.BOOKMARKS);
				EventBus.getDefault().post(SHUTDOWN);
				break;
			case R.id.menu_fullscreen:
				EventBus.getDefault().post(MenuOptions.FULLSCREEN);
				EventBus.getDefault().post(HIDE);
				updateFull();
				break;
			case R.id.menu_refresh:
				ToolManager.getInstance().refresh();
				EventBus.getDefault().post(HIDE);
				break;
			case R.id.menu_noimage:
				webview.edit().putBoolean(WebSettings.Setting.BLOCKIMAGES,!webview.getBoolean(WebSettings.Setting.BLOCKIMAGES,false)).commit();
				updateBlockImage();
				EventBus.getDefault().post(HIDE);
				break;
			case R.id.menu_download:
				EventBus.getDefault().post(MenuOptions.DOWNLOAD);
				EventBus.getDefault().post(SHUTDOWN);
				break;
			case R.id.menu_autoscreen:
				webview.edit().putBoolean(WebSettings.Setting.WIDEVIEW,!webview.getBoolean(WebSettings.Setting.WIDEVIEW,true)).commit();
				Toast.makeText(getActivity(),webview.getBoolean(WebSettings.Setting.WIDEVIEW,true)==true?"自适应布局已开启":"自适应布局已关闭",Toast.LENGTH_SHORT).show();
				EventBus.getDefault().post(HIDE);
				updateAutoScreen();
				break;
			case R.id.menu_desktop:
				webview.edit().putBoolean(WebSettings.Setting.DESKTOP,!webview.getBoolean(WebSettings.Setting.DESKTOP,false)).commit();
				Toast.makeText(getActivity(),webview.getBoolean(WebSettings.Setting.DESKTOP,true)==true?"桌面模式已开启":"桌面模式已关闭",Toast.LENGTH_SHORT).show();
				ToolManager.getInstance().refresh();
				EventBus.getDefault().post(HIDE);
				updateDesktopMode();
				break;
			case R.id.menu_toolbox:
				EventBus.getDefault().post(ToolboxDialog.SHOW);
				EventBus.getDefault().post(SHUTDOWN);
				break;
			case R.id.menu_bookmark_plus:
				final WebViewManagerView wv=(WebViewManagerView)ToolManager.getInstance().getContent().getCurrentView();
				Bookmark b=new Bookmark();
				b.setParent(Sqlite.getInstance(getContext(),BookMarks.class).getRoot().getPath());
				b.setTitle(wv.getTitle());
				b.setSummary(wv.getUrl());
				b.setType(BookMarks.Type.BOOKMARK);
				Sqlite.getInstance(getContext(),BookMarks.class).insert(b);
				EventBus.getDefault().post(HIDE);
				break;
			case R.id.menu_gps:
				webview.edit().putBoolean(WebSettings.Setting.GPS,!webview.getBoolean(WebSettings.Setting.GPS,false)).commit();
				updateGps();
				EventBus.getDefault().post(HIDE);
				break;
			case R.id.menu_videofullscreen:
				((WebViewManagerView)ToolManager.getInstance().getContent().getCurrentView()).loadUrl("javascript:var video=document.querySelector('video');if(video)video.webkitRequestFullscreen();else{video=document.querySelector('iframe');if(video)video.webkitRequestFullscreen();};");
				EventBus.getDefault().post(HIDE);
				break;
			case R.id.menu_forceScale:
				webview.edit().putBoolean(WebSettings.Setting.FORCESCALE,!webview.getBoolean(WebSettings.Setting.FORCESCALE,false)).commit();
				updateScale();
				EventBus.getDefault().post(HIDE);
				break;
			default:
				EventBus.getDefault().post(HIDE);
			break;
			
		}
		
	}
private void updateScale(){
	MenuAdapter ma=((MenuAdapter)((RecyclerView)av.get(1)).getAdapter());
	MenuItem mi=ma.get(2);
	if(webview.getBoolean(WebSettings.Setting.FORCESCALE,false)){
		mi.setColor(R.color.accent);
		}else{
		mi.setColor(R.color.textColor);
	}ma.notifyItemChanged(2);
}
private void updateFull(){
	MenuAdapter ma=((MenuAdapter)((RecyclerView)av.get(0)).getAdapter());
	MenuItem mi=ma.get(2);
	if(moe.getBoolean("full",false)){
		mi.setIcon(getResources().getDrawable(R.drawable.menu_fullscreen_exit));
		mi.setColor(R.color.accent);
	}else{
		mi.setIcon(getResources().getDrawable(R.drawable.menu_fullscreen));
		mi.setColor(R.color.textColor);
	}ma.notifyItemChanged(2);
}
private void updateBlockImage(){
	MenuAdapter ma=((MenuAdapter)((RecyclerView)av.get(0)).getAdapter());
	MenuItem mi=ma.get(4);
	if(webview.getBoolean(WebSettings.Setting.BLOCKIMAGES,false)){
		mi.setIcon(getResources().getDrawable(R.drawable.menu_image_broken));
	mi.setColor(R.color.accent);
	}else{
		mi.setIcon(getResources().getDrawable(R.drawable.menu_picture));
		mi.setColor(R.color.textColor);
	}ma.notifyItemChanged(4);
}
private void updateAutoScreen(){
	MenuAdapter ma=((MenuAdapter)((RecyclerView)av.get(0)).getAdapter());
	MenuItem mi=ma.get(6);
	if(webview.getBoolean(WebSettings.Setting.WIDEVIEW,true)){
		mi.setColor(R.color.accent);
	}else{
		mi.setColor(R.color.textColor);
	}ma.notifyItemChanged(6);
}
private void updateDesktopMode(){
	MenuAdapter ma=((MenuAdapter)((RecyclerView)av.get(0)).getAdapter());
	MenuItem mi=ma.get(9);
	if(webview.getBoolean(WebSettings.Setting.DESKTOP,false)){
		mi.setColor(R.color.accent);
	}else{
		mi.setColor(R.color.textColor);
	}ma.notifyItemChanged(9);
}
private void updateNightMode(){
	MenuAdapter ma=((MenuAdapter)((RecyclerView)av.get(0)).getAdapter());
	MenuItem mi=ma.get(7);
	if(moe.getBoolean("night",false)){
		mi.setColor(R.color.accent);
	}else{
		mi.setColor(R.color.textColor);
	}ma.notifyItemChanged(7);
}
	private void updateGps(){
		MenuAdapter ma=((MenuAdapter)((RecyclerView)av.get(1)).getAdapter());
		MenuItem mi=ma.get(0);
		if(webview.getBoolean(WebSettings.Setting.GPS,false)){
			mi.setColor(R.color.accent);
			mi.setIcon(getResources().getDrawable(R.drawable.ic_map_marker));
		}else{
			mi.setColor(R.color.textColor);
			mi.setIcon(getResources().getDrawable(R.drawable.ic_map_marker_off));
		}ma.notifyItemChanged(0);
	}
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
    }

    @Override
    public boolean onBackPressed()
    {
        // TODO: Implement this method
        return false;
    }

    private void parser(int resId) throws XmlPullParserException, IOException{
		XmlPullParser xml=getResources().getXml(resId);
		int type;
		RecyclerView rv=null;
		ArrayList<MenuItem> ami = null;
		MenuAdapter ma = null;
		while((type=xml.next())!=xml.END_DOCUMENT){
			switch(type){
				case xml.START_TAG:
					if(xml.getName().equals("group")){
						groupSize++;
						if(groupSize%2!=0){
							rv=new RecyclerView(getActivity());
							ami=new ArrayList<MenuItem>();
							rv.setNestedScrollingEnabled(false);
							rv.setOverScrollMode(rv.OVER_SCROLL_NEVER);
							ViewPager.LayoutParams vl= new ViewPager.LayoutParams();
							vl.width=vl.MATCH_PARENT;
							vl.height=vl.WRAP_CONTENT;
							rv.setLayoutParams(vl);
							rv.setAdapter(ma=new MenuAdapter(ami));
							GridLayoutManager glm=new GridLayoutManager(getActivity(),5);
							rv.setLayoutManager(glm);
							rv.setHasFixedSize(true);
							glm.setAutoMeasureEnabled(true);
							av.add(rv);
						}
					}
					if(xml.getName().equals("item")){
						MenuItem mi=new MenuItem();
						mi.setId(Integer.parseInt(xml.getAttributeValue(xmlns,"id").substring(1)));
						String icon=xml.getAttributeValue(xmlns,"icon");
						if(icon!=null)
						mi.setIcon(getResources().getDrawable(Integer.parseInt(icon.substring(1))));
						String title=xml.getAttributeValue(xmlns,"title");
						if(title.startsWith("@"))
							title=getResources().getString(Integer.parseInt(title.substring(1)));
						mi.setSummory(title);
						ami.add(mi);
					}
					break;
					case xml.END_TAG:
						if(xml.getName().equals("group")&&groupSize%2!=0){
						ma.notifyDataSetChanged();
						ma.setOnItemClickListener(this);
						}
						break;
			}
		}
		vpa.notifyDataSetChanged();
		
	}
}
