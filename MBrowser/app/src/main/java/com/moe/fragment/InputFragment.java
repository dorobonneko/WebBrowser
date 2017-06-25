package com.moe.fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.moe.widget.ToolEditText;
import com.moe.adapter.SearchHistoryAdapter;
import android.view.inputmethod.InputMethodManager;
import android.app.Service;
import android.view.inputmethod.EditorInfo;
import android.content.Context;
import android.widget.TextView;
import android.view.KeyEvent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import com.moe.Mbrowser.R;
import com.moe.internal.CustomDecoration;
import android.util.TypedValue;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Handler;

public class InputFragment extends Fragment implements ToolEditText.OnTextChangedListener,ToolEditText.OnEditorActionListener,ToolEditText.OnCloseListener
{
    private ToolEditText tet;
	private SearchHistoryAdapter sha;
	private InputMethodManager imm;
	private String text;
	private Handler handler=new Handler();
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.input_view,container,false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		tet=(ToolEditText)view.findViewById(R.id.input_view_input);
		RecyclerView rv=(RecyclerView)view.findViewById(R.id.input_view_history);
		LinearLayoutManager llm=new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
		rv.setLayoutManager(llm);
		llm.setAutoMeasureEnabled(true);
		rv.setAdapter(sha = new SearchHistoryAdapter(getActivity(), tet, this));
		tet.setOnTextChangedListener(this);
		rv.setPadding(5, 0, 10, 0);
		rv.addItemDecoration(new CustomDecoration((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getContext().getResources().getDisplayMetrics())));
		tet.setOnCloseListener(this);
		tet.setOnEditorActionListener(this);
		tet.setText(text);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		imm = (InputMethodManager)getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
		super.onActivityCreated(savedInstanceState);
		onHiddenChanged(false);
	}

	
	public void setArguments(String text)
	{
		this.text=text;
		if(tet!=null)tet.setText(text);
	}
	
	@Override
	public boolean onBackPressed()
	{
//		if(!isHidden()){
//			getFragmentManager().beginTransaction().hide(this).commit();
//			return true;
//		}
		return false;
	}
	@Override
    public boolean onEditorAction(TextView p1, int p2, KeyEvent p3)
    {
        if (p2 == EditorInfo.IME_ACTION_GO)
		{
			String text=tet.getText().toString().trim();
			LocalBroadcastManager.getInstance(getContext().getApplicationContext()).sendBroadcast(new Intent("com.moe.search").putExtra(SearchManager.QUERY, text));
            getActivity().onBackPressed();
        }
        return false;
    }

	@Override
	public void OnTextChanged(String text)
	{
		sha.query(text);
	}

	@Override
	public void onHiddenChanged(boolean hidden)
	{
		if(!hidden){
			handler.postDelayed(new Runnable(){

					@Override
					public void run()
					{
						tet.requestFocus();
						
						imm.toggleSoftInput(0,0);
							if (!TextUtils.isEmpty(text))
							tet.selectAll();
					}
				}, 500);
			
		}
		
	}

	@Override
	public void onClose()
	{
		getActivity().onBackPressed();
	}

	
	
}
