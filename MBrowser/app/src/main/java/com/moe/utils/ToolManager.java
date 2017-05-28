package com.moe.utils;
import android.view.View;
import com.moe.dialog.SearchDialog;
import android.widget.TextView;
import com.moe.widget.ProgressBar;
import android.widget.ImageButton;
import com.moe.widget.ViewFlipper;
import com.moe.Mbrowser.R;
import de.greenrobot.event.EventBus;
import com.moe.fragment.MenuFragment;
import com.moe.fragment.WindowFragment;
import com.moe.widget.WebView;
import android.support.v4.content.LocalBroadcastManager;
import com.moe.bean.Message;
import android.support.design.widget.AppBarLayout;
import com.moe.database.BookMarks;
import com.moe.database.DataBase;
import android.graphics.Bitmap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import android.widget.ImageView;
import com.moe.bean.MenuOptions;
import android.widget.EditText;
import android.text.TextWatcher;
import android.text.Editable;
import java.lang.reflect.Method;
import android.view.inputmethod.InputMethodManager;
import android.view.MotionEvent;
import com.moe.dialog.ToolboxDialog;

public class ToolManager implements View.OnClickListener,ViewFlipper.OnChangeListener,WebView.OnStateListener,TextWatcher,WebView.FindListener,View.OnLongClickListener
{

	private int index=0;
	private SearchDialog search;
	private TextView title;
	private ProgressBar pb;
	private ImageButton back,forward,home,win,menu;
	private ImageView refresh;
	//private ImageView bookmark;
	private View toolbar;
	private ViewFlipper content;
	private static ToolManager tm;
	private AppBarLayout abl;
	private BookMarks bm;
	public static final int HOME = 0xff0007;
	private android.widget.ViewFlipper findToggle;
	private EditText findKey;
	private ImageView findup,finddown;
	private TextView findCount;
	private int active,count;
	private InputMethodManager imm;
	private ToolManager(View v){
		imm=v.getContext().getSystemService(InputMethodManager.class);
		search=new SearchDialog(v.getContext());
		bm=DataBase.getInstance(v.getContext());
		toolbar = v.findViewById(R.id.mainview_searchbar);
        toolbar.setOnClickListener(this);
        title = (TextView)toolbar.findViewById(R.id.mainview_title);
        pb=(ProgressBar)v.findViewById(R.id.mainview_progress);
        v.findViewById(R.id.mainview_bar_home).setOnClickListener(this);
        back=(ImageButton)v.findViewById(R.id.mainview_bar_pre);
        back.setOnClickListener(this);
        forward=(ImageButton)v.findViewById(R.id.mainview_bar_next);
        forward.setOnClickListener(this);
        win=(ImageButton)v.findViewById(R.id.mainview_bar_win);
        win.setOnClickListener(this);
        v.findViewById(R.id.mainview_bar_menu).setOnClickListener(this);
        Theme.registerForeGround(v.findViewById(R.id.mainview_bar));
        refresh=(ImageButton)v.findViewById(R.id.mainview_refresh);
		refresh.setOnClickListener(this);
		home=(ImageButton)v.findViewById(R.id.mainview_bar_home);
		home.setOnClickListener(this);
		menu=(ImageButton)v.findViewById(R.id.mainview_bar_menu);
		menu.setOnClickListener(this);
		menu.setOnLongClickListener(this);
		content=(ViewFlipper)v.findViewById(R.id.main_content);
		content.registerOnChangeListener(this);
		v.findViewById(R.id.mainview_popwin).setOnClickListener(this);
		abl=(AppBarLayout)v.findViewById(R.id.main_view_appbarlayout);
		//bookmark=(ImageView)v.findViewById(R.id.mainview_fav);
		//bookmark.setOnClickListener(this);
		v.findViewById(R.id.main_view_exit).setOnClickListener(this);
		v.findViewById(R.id.main_view_setting).setOnClickListener(this);
		v.findViewById(R.id.main_view_skin).setOnClickListener(this);
		findToggle=(android.widget.ViewFlipper)v.findViewById(R.id.main_view_toolbarToggle);
		findKey=(EditText)v.findViewById(R.id.main_view_findText);
		findup=(ImageView)v.findViewById(R.id.main_view_findup);
		finddown=(ImageView)v.findViewById(R.id.main_view_finddown);
		findCount=(TextView)v.findViewById(R.id.main_view_findCount);
		findKey.addTextChangedListener(this);
		findup.setOnClickListener(this);
		finddown.setOnClickListener(this);
	}

	@Override
	public boolean onLongClick(View p1)
	{
		EventBus.getDefault().post(ToolboxDialog.SHOW);
		return false;
	}


	
	
	public ViewFlipper getContent(){
		return content;
	}
	@Override
	public void onTextChanged(CharSequence p1, int p2, int p3, int p4)
	{
		// TODO: Implement this method
	}

	@Override
	public void afterTextChanged(Editable p1)
	{
		((WebView)content.getCurrentView()).findAllAsync(p1.toString());
	}

	@Override
	public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4)
	{
		// TODO: Implement this method
	}

	@Override
	public void onFindResultReceived(int p1, int p2, boolean p3)
	{
		active=p1;
		count=p2;
		findCount.setText(p1+"/"+p2);
		if(!canForward())
			findup.setImageResource(R.drawable.find_up_none);
			else
			findup.setImageResource(R.drawable.find_up);
		if(!canNext())
			finddown.setImageResource(R.drawable.find_down_none);
			else
			finddown.setImageResource(R.drawable.find_down);
	}

	private boolean canNext(){
		if(count-active>1)
			return true;
			return false;
	}
	private boolean canForward(){
		if(active==0)
			return false;
			return true;
	}
	public boolean isShowFind(){
		return findToggle.getDisplayedChild()==1;
	}
	public void findToggle(boolean toggle){
		if(toggle){
			findToggle.setDisplayedChild(1);
			((WebView)content.getCurrentView()).setFindListener(this);
			findKey.setText(null);
			active=0;
			count=0;
			findKey.requestFocus();
			imm.toggleSoftInput(imm.SHOW_FORCED,imm.HIDE_NOT_ALWAYS);
			}
			else{
			findToggle.setDisplayedChild(0);
				((WebView)content.getCurrentView()).setFindListener(null);
				((WebView)content.getCurrentView()).clearMatches();
				findCount.setText(null);
			}
	}
	
	public void refresh()
	{
		((WebView)content.getCurrentView()).reload();
	}

	public static void destroy()
	{
		getInstance().getContent().unRegisterOnChangeListener(getInstance());
		tm=null;
	}
	public static void init(View v){
			tm=new ToolManager(v);
	}
	public static ToolManager getInstance(){
		if(tm==null)throw new NullPointerException("工具管理器未初始化");
		return tm;
	}
	@Override
    public void onClick(View p1)
    {
        switch (p1.getId())
        {
            case R.id.mainview_searchbar:
				// String url="";
				EventBus.getDefault().post(MenuFragment.HIDE);
				EventBus.getDefault().post(WindowFragment.CLOSE);
				search.show(((WebView)content.getCurrentView()).getUrl());
                //搜索
                break;
            case R.id.mainview_bar_menu:
				checkToggle();
                EventBus.getDefault().post(MenuFragment.SHOW);
				abl.setExpanded(true,true);
                break;
            case R.id.mainview_popwin:
                //关闭popwin
                EventBus.getDefault().post(WindowFragment.CLOSE);
                abl.setExpanded(true,true);
				break;
            case R.id.mainview_bar_home:
				checkToggle();
                EventBus.getDefault().post(WindowFragment.CLOSE);
				EventBus.getDefault().post(HOME);
                //((WebView)content.getCurrentView()).loadUrl();
                break;
            case R.id.mainview_bar_pre:
				checkToggle();
                EventBus.getDefault().post(WindowFragment.CLOSE);
               final  WebView wv=((WebView)content.getCurrentView());
			   if(wv.canGoBack())
				   wv.goBack();
				   else if(content.getChildCount()>1)
				content.removeViewAt(content.getDisplayedChild());
                break;
            case R.id.mainview_bar_next:
				checkToggle();
                EventBus.getDefault().post(WindowFragment.CLOSE);
                ((WebView)content.getCurrentView()).goForward();
                break;
            case R.id.mainview_bar_win:
				checkToggle();
                EventBus.getDefault().post(WindowFragment.OPEN);
                break;
			case R.id.mainview_refresh:
				final WebView wvv=(WebView)content.getCurrentView();
				if(wvv.getState())
					wvv.stopLoading();
					else
					wvv.reload();
				break;
			/**case R.id.mainview_fav:
				String url=((WebView)content.getCurrentView()).getUrl();
				if(bm.isBookmark(url))
				{
					bm.deleteBookmark(url);
					bookmark.setImageResource(R.drawable.ic_star_outline);
				}
				else if(url!=null&&url.length()>0){
					/*ByteArrayOutputStream baos=new ByteArrayOutputStream();
					Bitmap b=((WebView)content.getCurrentView()).getFavicon();
					if(b!=null)b.compress(Bitmap.CompressFormat.PNG,100,baos);
					bm.insertBookmark(url,((WebView)content.getCurrentView()).getTitle(),baos.toByteArray());
					try
					{
						baos.close();
					}
					catch (IOException e)
					{}
					bm.insertBookmark(url,((WebView)content.getCurrentView()).getTitle());
					bookmark.setImageResource(R.drawable.ic_star);
					}
				break;*/
			case R.id.main_view_exit:
				EventBus.getDefault().post(MenuOptions.EXIT);
				break;
			case R.id.main_view_setting:
				EventBus.getDefault().post(MenuOptions.SETTING);
				EventBus.getDefault().post(MenuFragment.SHUTDOWN);
				break;
			case R.id.main_view_skin:
				EventBus.getDefault().post(MenuOptions.SKIN);
				EventBus.getDefault().post(MenuFragment.SHUTDOWN);
				break;
			case R.id.main_view_finddown:
				if(canNext())
				((WebView)content.getCurrentView()).findNext(true);
				break;
			case R.id.main_view_findup:
				if(canForward())
				((WebView)content.getCurrentView()).findNext(false);
				break;
        }
    }
	private void checkToggle(){
		if(findToggle.getDisplayedChild()==1)
			findToggle(false);
	}
	@Override
	public void onAdd(WebView vf, int index)
	{
		win.setImageBitmap(ImageDraw.squareImage(content.getChildCount()));
	}

	@Override
	public void onRemove(int index)
	{
		
		win.setImageBitmap(ImageDraw.squareImage(content.getChildCount()));
		
	}

	@Override
	public void onToggle(int index)
	{
		WebView wv=(WebView)content.getCurrentView();
		if(wv!=null)wv.setOnStateListener(null);
		wv=(WebView)content.getChildAt(index);
		if(wv==null)return;
		title.setText(wv.getTitle());
		if(wv.getState()){
			pb.setVisibility(pb.VISIBLE);
			pb.setProgress(wv.getProgress());
			refresh.setImageResource(R.drawable.ic_close);
		}else{
		pb.setVisibility(pb.GONE);
		refresh.setImageResource(R.drawable.menu_refresh);
		}
		checkButtonState(wv);
		//if(bm.isBookmark(wv.getUrl()))bookmark.setImageResource(R.drawable.ic_star);
		//else bookmark.setImageResource(R.drawable.ic_star_outline);
		wv.setOnStateListener(this);
		this.index=index;
	}
	@Override
	public void onProgress(int p2)
	{
		pb.setProgress(p2);
	}

	@Override
	public void onStart(String url)
	{
		checkToggle();
		pb.setProgress(0);
		pb.setVisibility(pb.VISIBLE);
		refresh.setImageResource(R.drawable.ic_close);
		title.setText(url);
		//if(bm.isBookmark(url))bookmark.setImageResource(R.drawable.ic_star);
		//else bookmark.setImageResource(R.drawable.ic_star_outline);
		EventBus.getDefault().post(Message.obitMessage(0,index));
	}

	@Override
	public void onEnd(String url, String title)
	{
		refresh.setImageResource(R.drawable.menu_refresh);
		this.title.setText(title);
		pb.setVisibility(pb.GONE);
		checkButtonState((WebView)content.getCurrentView());
		EventBus.getDefault().post(Message.obitMessage(0,index));
		
	}

	@Override
	public void onReceiverTitle(String title)
	{
		this.title.setText(title);
		EventBus.getDefault().post(Message.obitMessage(0,index));
		checkButtonState((WebView)content.getCurrentView());
	}
	
	public void checkButtonState(WebView wv){
		if(wv.canGoBack())
			back.setImageResource(R.drawable.bar_back);
			else if(content.getChildCount()>1)
			back.setImageResource(R.drawable.ic_exit_to_app);
				else
			back.setImageResource(R.drawable.bar_back_normal);
		if(wv.canGoForward())
			forward.setImageResource(R.drawable.bar_next);
			else
			forward.setImageResource(R.drawable.bar_next_normal);
		}
}
