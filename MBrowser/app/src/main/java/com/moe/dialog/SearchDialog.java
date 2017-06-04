package com.moe.dialog;
import android.content.Context;
import com.moe.Mbrowser.R;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.view.ViewGroup;
import android.view.Gravity;
import com.moe.widget.ToolEditText;
import android.util.TypedValue;
import com.moe.utils.Theme;
import android.view.View;
import android.view.WindowManager;
import android.view.KeyEvent;
import android.widget.Toast;
import android.widget.TextView;
import android.view.inputmethod.EditorInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.content.Intent;
import android.widget.*;
import android.support.v7.widget.*;
import com.moe.database.*;
import com.moe.adapter.*;
import com.moe.utils.*;
import android.view.ViewAnimationUtils;
import android.animation.Animator;
import android.support.design.widget.TextInputLayout;
import android.hardware.input.InputManager;
import android.view.inputmethod.InputMethodManager;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.app.SearchManager;

public class SearchDialog extends android.app.Dialog implements ToolEditText.OnEditorActionListener,ToolEditText.OnTextChangedListener,Dialog.OnDismissListener
{
	private LinearLayout ll;
    private ToolEditText tet;
	private SearchHistoryAdapter sha;
	//private SearchHistory sh;
	private InputMethodManager imm;
    public SearchDialog(Context context)
	{
        super(context, R.style.searchDialog);
		tet = new ToolEditText(context, this);
		imm = context.getSystemService(InputMethodManager.class);
		//sh=DataBase.getInstance(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        getWindow().setGravity(Gravity.TOP);
		getWindow().setWindowAnimations(R.style.PopupWindowAnim);
        super.onCreate(savedInstanceState);
        ll = new LinearLayout(getContext());
        ll.setPadding((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getContext().getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getContext().getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getContext().getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getContext().getResources().getDisplayMetrics()));
        ll.setOrientation(ll.VERTICAL);
        ll.setGravity(Gravity.TOP | Gravity.CENTER);
        LinearLayout child=new LinearLayout(getContext());
        ll.addView(child, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        child.setBackgroundResource(R.color.window_background);
        child.setOrientation(child.VERTICAL);
        child.setGravity(Gravity.TOP | Gravity.CENTER);
        //Theme.registerTheme(ll);
        setContentView(ll, new ViewGroup.LayoutParams(getWindow().getWindowManager().getDefaultDisplay().getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT));
		TextInputLayout til=new TextInputLayout(getContext());
		til.addView(tet);
        child.addView(til, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        //View border=new View(getContext());
        //border.setBackgroundColor(getContext().getResources().getColor(R.color.border));
        //child.addView(border, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,2,getContext().getResources().getDisplayMetrics())));
		tet.setOnEditorActionListener(this);
		RecyclerView rv=new RecyclerView(getContext());
		LinearLayoutManager llm=new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
		rv.setLayoutManager(llm);
		llm.setAutoMeasureEnabled(true);
		child.addView(rv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		rv.setItemAnimator(new DefaultItemAnimator());
		rv.setAdapter(sha = new SearchHistoryAdapter(getContext(), tet, this));
		tet.setOnTextChangedListener(this);
		rv.setPadding(5, 0, 10, 0);
		rv.addItemDecoration(new CustomDecoration((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getContext().getResources().getDisplayMetrics())));
		tet.setFocusable(true);
		tet.setFocusableInTouchMode(true);
		setOnDismissListener(this);
    }

    @Override
    public boolean onEditorAction(TextView p1, int p2, KeyEvent p3)
    {
        if (p2 == EditorInfo.IME_ACTION_GO)
		{
			String text=tet.getText().toString().trim();
			LocalBroadcastManager.getInstance(getContext().getApplicationContext()).sendBroadcast(new Intent("com.moe.search").putExtra(SearchManager.QUERY, text));
            dismiss();
        }
        return false;
    }

	@Override
	public void OnTextChanged(String text)
	{
		sha.query(text);
	}

	@Override
	public void onDismiss(DialogInterface p1)
	{
		getContext().getSystemService(InputMethodManager.class).toggleSoftInputFromWindow(tet.getApplicationWindowToken(), imm.SHOW_IMPLICIT, imm.HIDE_NOT_ALWAYS);
	}








    public void show(final String url)
	{
		super.show();
		tet.setText(url);

		tet.postDelayed(new Runnable(){

				@Override
				public void run()
				{
					imm.toggleSoftInput(0, 0);
					if (!TextUtils.isEmpty(url))
						tet.selectAll();
				}
			}, 200);

	}
}
