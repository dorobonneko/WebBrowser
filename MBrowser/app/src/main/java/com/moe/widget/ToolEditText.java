package com.moe.widget;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.text.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import com.moe.Mbrowser.*;


public class ToolEditText extends EditText implements TextWatcher
{
	private boolean canClear=false;
	private int leftWidth,rightWidth;
	private Dialog dialog;
	private Drawable back,clear;
	private OnTextChangedListener otcl;
	public void setOnTextChangedListener(OnTextChangedListener o){
		this.otcl=o;
	}
    @Override
    public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4)
    {
        
    }

	@Override
	public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
	{
		// TODO: Implement this method
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
	}

	

    @Override
    public void afterTextChanged(Editable p1)
    {
        if (p1.length() == 0)
		{
            setCompoundDrawablesWithIntrinsicBounds(back, null, null, null);
            canClear = false;}
		else
		{
            setCompoundDrawablesWithIntrinsicBounds(back, null, clear, null);
            canClear = true;

		}
		if(otcl!=null)
			otcl.OnTextChanged(p1.toString());
		}


    public ToolEditText(Context context, Dialog d)
	{
        super(context);
		setCursorVisible(true);
        this.dialog = d;
        back = context.getResources().getDrawable(R.drawable.bar_back);
        clear = context.getResources().getDrawable(R.drawable.ic_close);
        leftWidth = back.getIntrinsicWidth();
        rightWidth = clear.getIntrinsicWidth();
        addTextChangedListener(this);
		setCompoundDrawablesWithIntrinsicBounds(back, null, null, null);
		//setBackgroundColor(0xffff0000);
		setSingleLine(true);
        setImeOptions(EditorInfo.IME_ACTION_GO);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (event.getAction() == event.ACTION_UP)
		{
            Rect rectLeft=new Rect();
            getGlobalVisibleRect(rectLeft);
            rectLeft.right = rectLeft.left + leftWidth;
            if (rectLeft.contains((int)event.getRawX(), (int)event.getRawY()))
                dialog.dismiss();
			if (canClear)
			{
				Rect rectRight=new Rect();
				getGlobalVisibleRect(rectRight);
				rectRight.left = rectRight.right - rightWidth;
				if (rectRight.contains((int)event.getRawX(), (int)event.getRawY()))
					setText("");
			}
        }
        return super.onTouchEvent(event);
    }

	public abstract interface OnTextChangedListener{
		void OnTextChanged(String text);
	}
	

}
