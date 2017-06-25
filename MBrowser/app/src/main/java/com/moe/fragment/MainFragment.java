package com.moe.fragment;
import com.moe.database.HomePage;
import com.moe.dialog.AddDialog;
import android.view.ViewGroup;
import android.widget.ViewFlipper;
import android.view.LayoutInflater;
import android.view.View;
import com.moe.Mbrowser.R;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import com.moe.database.Sqlite;
import de.greenrobot.event.EventBus;
import android.support.v4.content.LocalBroadcastManager;
import android.graphics.Color;
import de.greenrobot.event.Subscribe;
import com.moe.bean.WindowEvent;
import de.greenrobot.event.ThreadMode;
import android.os.Message;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.moe.database.SearchHistory;
import android.app.SearchManager;
import android.content.IntentFilter;
import com.moe.internal.Theme;
import com.moe.internal.ToolManager;
import android.content.SharedPreferences;
import com.moe.webkit.WebViewManagerView;
import com.moe.webkit.WebSettings;
import android.webkit.WebView;

public class MainFragment extends Fragment implements FragmentPop.OnHideListener,AddDialog.OnAddListener
{
	private HomePage hp;
	private AddDialog ad;
    private ViewGroup pop;
    private ViewFlipper content;
	private android.widget.ViewFlipper menutool;
	private FragmentPop current,menu=new MenuFragment(),window=new WindowFragment();
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v= inflater.inflate(R.layout.main_view, container, false);
        Theme.registerForeGround(v.findViewById(R.id.mainview_searchbar));
		Theme.registerForeGround(v.findViewById(R.id.mainview_bar));
 		return v;
    }

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		ToolManager.init(view);
		content = (ViewFlipper)getView().findViewById(R.id.main_content);
		menutool = (android.widget.ViewFlipper)getView().findViewById(R.id.mainview_ViewFlipper);
		pop = (ViewGroup)getView().findViewById(R.id.mainview_popwin);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		hp = Sqlite.getInstance(getActivity(), HomePage.class);
		ad = new AddDialog(getActivity());
        EventBus.getDefault().register(this);
        //注册fragment隐藏监听
        menu.setOnHideListener(this);
        window.setOnHideListener(this);
		LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(new TextQuery(), new IntentFilter("com.moe.search"));
		ad.setOnAddLostener(this);

	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
		super.onActivityCreated(savedInstanceState);
		//setPadding(isfull);
		pop.setPadding(0, 0, 0, (int)getResources().getDimension(R.dimen.actionBarSize));
        //pop.setLayoutParams(new CoordinatorLayout.LayoutParams(getActivity().getWindowManager().getDefaultDisplay().getWidth(), getActivity().getWindowManager().getDefaultDisplay().getHeight()));
		menutool.setInAnimation(getActivity(), R.anim.bottom_up);
		menutool.setOutAnimation(getActivity(), R.anim.up_up);
		Theme.updateTheme(Color.parseColor(getResources().getTextArray(R.array.skin_color)[getContext().getSharedPreferences("moe", 0).getInt("color", 0)].toString()));
		openNewWindow();
		if (getArguments() != null)
		{
			String url=getArguments().getString("url");
			if (url == null)url = getArguments().getString(SearchManager.QUERY);
			LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent("com.moe.search").putExtra(SearchManager.QUERY, url));
		}
	}

	@Override
	public void setArguments(Bundle args)
	{
		try{super.setArguments(args);
		}catch(Exception e){
			String url=args.getString("url");
			if (url == null)url = args.getString(SearchManager.QUERY);
		LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent("com.moe.search").putExtra(SearchManager.QUERY, url));
		}
	}

	@Override
	public void onAdd(String url, String title, String dir)
	{
		hp.insertItem(url, title);
		((WebViewManagerView)content.getCurrentView()).reload();
	}


	@Subscribe(threadMode=ThreadMode.MainThread)
	public void onEvent(WindowEvent event)
	{
		switch (event.what)
		{
			case event.WHAT_NEW_WINDOW:
				openNewWindow();
				break;
			case event.WHAT_URL_NEW_WINDOW_BACKGROUND:
				openNewWindowInBackground(event.obj.toString());
				break;
			case event.WHAT_URL_NEW_WINDOW:
				openNewWindow(event.obj.toString());
				break;
			case event.WHAT_JS_NEW_WINDOW:
				((WebView.WebViewTransport)((Message)event.obj).obj).setWebView(openNewWindow().getCurrent());
				((Message)event.obj).sendToTarget();
				break;
			case event.WHAT_TOGGLE_WINDOW:
				openWindow(event.obj);
				break;
			case event.WHAT_URL_WINDOW:
				openUrl(event.obj.toString());
				break;
			case event.WHAT_JS_CLOSE_WINDOW:
				content.removeView((WebViewManagerView)event.obj);
				break;
			case event.WHAT_DATA_NEW_WINDOW:
				WebViewManagerView wv=new WebViewManagerView(getActivity(), ad);
				wv.loadDataWithBaseURL(((WebViewManagerView)content.getCurrentView()).getUrl(), event.obj.toString(), "text/plain", "utf-8", "");
				int index=content.getChildCount();
				content.addView(wv, index);
				content.setDisplayedChild(index);				
				break;
		}
	}
	@Subscribe
	public void close(Integer close)
	{
		switch (close)
		{
			case WindowFragment.CLOSE:
				hidePop();
				break;
			case WindowFragment.OPEN:
				if (!hidePop())
				{
					pop.setVisibility(pop.VISIBLE);
					if (window.isAdded())
						getChildFragmentManager().beginTransaction().setCustomAnimations(R.anim.popin, R.anim.popout).show(window).commit();
					else
						getChildFragmentManager().beginTransaction().setCustomAnimations(R.anim.popin, R.anim.popout).add(R.id.mainview_popwin, window).show(window).commit();
				}
				break;
			case MenuFragment.SHOW:
				//打开菜单
				int time=0;
				if (current != null && !current.isHidden())
					time = 150;
				hidePop();
				pop.postDelayed(new Runnable(){

						@Override
						public void run()
						{
							pop.setVisibility(pop.VISIBLE);
							if (menu.isAdded())
								getChildFragmentManager().beginTransaction().setCustomAnimations(R.anim.popin, R.anim.popout).show(menu).commit();
							else
								getChildFragmentManager().beginTransaction().setCustomAnimations(R.anim.popin, R.anim.popout).add(R.id.mainview_popwin, menu).show(menu).commit();
							
							menutool.setDisplayedChild(1);

						}
					}, time);

				break;
			case MenuFragment.HIDE:
				hidePop();
				break;
			case MenuFragment.SHUTDOWN:
				getChildFragmentManager().beginTransaction().hide(menu).commit();
				menutool.setDisplayedChild(0);
				break;
			case ToolManager.HOME:
				((WebViewManagerView)content.getCurrentView()).goHome();
				break;
		}
	}
    //隐藏弹出视图
    private boolean hidePop()
    {
        if (current != null && !current.isHidden())
        {
            getChildFragmentManager().beginTransaction().setCustomAnimations(R.anim.popin, R.anim.popout).hide(current).commit();
			getChildFragmentManager().popBackStack();
            if (current == menu)
				menutool.setDisplayedChild(0);
			return true;
        }
        return false;
    }


    @Override
    public void hide(FragmentPop f, boolean b)
    {

        if (b)
        {

            current = null;
			pop.setVisibility(pop.INVISIBLE);
        }
        else
            current = f;
    }
    //当前窗口打开url
    public void openUrl(String url)
    {
		((WebViewManagerView)content.getCurrentView()).loadUrl(url.trim());
    }
    //后台打开一个网页窗口
    public void openNewWindowInBackground(String url)
    {
        WebViewManagerView wv=new WebViewManagerView(getActivity(), ad);
		int index=content.getChildCount();
        content.addView(wv, index);
		wv.stopLoading();
		wv.loadUrl(url);
    }
//打开一个网页窗口并跳转过去
    public WebViewManagerView openNewWindow(String url)
    {
        WebViewManagerView wv=new WebViewManagerView(getActivity(), ad);
		int index=content.getChildCount();
        content.addView(wv, index);
		content.setDisplayedChild(index);
		wv.stopLoading();
		wv.loadUrl(url);
		return wv;
    }
//打开一个空白窗口并跳转过去
    public WebViewManagerView openNewWindow()
    {
        WebViewManagerView wv=new WebViewManagerView(getActivity(), ad);
		int index=content.getChildCount();
        content.addView(wv, index);
		content.setDisplayedChild(index);
		return wv;
	}
//跳转窗口
    public boolean openWindow(int position)
    {
        content.setDisplayedChild(position);
        return false;
    }



    @Override
    public boolean onBackPressed()
    {
        if (!hidePop())
            if (((WebViewManagerView)content.getCurrentView()).canGoBack())
            {  ((WebViewManagerView)content.getCurrentView()).goBack();
				return true;
            }
			else if (content.getChildCount() > 1)
			{
				content.removeViewAt(content.getDisplayedChild());
				return true;
			}
			else
				return false;
        return true;
    }




    class TextQuery extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context p1, Intent p2)
        {
			SharedPreferences webview=p1.getSharedPreferences("webview", 0);
            String text=p2.getStringExtra(SearchManager.QUERY);
			if (text.indexOf("://") != -1 || text.indexOf(".") != -1)
			{
				if (text.indexOf("://") == -1)
					text = "http://" + text;
			}
			else
			{
				if (!webview.getBoolean(WebSettings.Setting.PRIVATE, false))
					Sqlite.getInstance(p1, SearchHistory.class).insertSearchHistory(text);
				text = webview.getString("searchValue", p1.getResources().getTextArray(R.array.search_value)[0].toString()).replace("$s", text);
			}
			openUrl(text);
		}


    }
}


