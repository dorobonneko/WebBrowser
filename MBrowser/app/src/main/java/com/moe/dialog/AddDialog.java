package com.moe.dialog;
import android.content.Context;
import com.moe.Mbrowser.R;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.TextView;
import com.moe.database.BookMarks;
import android.widget.Button;

public class AddDialog extends Dialog implements View.OnClickListener
{
    private TextInputLayout t_name,t_url;
    private TextView name,url;
	private String dir;
	private String b1,b2;
	private Button add,cancel;
	private String title="添加网址导航";
    public AddDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO: Implement this method
        super.onCreate(savedInstanceState);
        //setIcon(R.drawable.ic_launcher);
        setTitle(title);
        setContentView(R.layout.dialog_add_view);
        t_name=(TextInputLayout)findViewById(R.id.dialogaddview_sitename);
        t_url=(TextInputLayout)findViewById(R.id.dialogaddview_siteurl);
        name=(TextView)findViewById(R.id.dialogaddview_sitename_content);
        url=(TextView)findViewById(R.id.dialogaddview_siteurl_content);
		name.setHintTextColor(R.color.accent);
		url.setHintTextColor(R.color.accent);
        add=(Button)findViewById(R.id.dialogaddview_add);
		add.setOnClickListener(this);
        cancel=(Button)findViewById(R.id.dialogaddview_cancel);
		cancel.setOnClickListener(this);
		if(b1!=null)add.setText(b1);
		if(b2!=null)cancel.setText(b2);
    }
	public void setButtonText(int index,CharSequence text){
		switch(index){
			case 0:
				b1=text.toString();
				if(add!=null)add.setText(b1);
				break;
			case 1:
				b2=text.toString();
				if(cancel!=null)cancel.setText(b2);
				break;
		}
	}

	@Override
	public void setTitle(CharSequence title)
	{
		this.title=title.toString();
		super.setTitle(title);
	}
	
    @Override
    public void onClick(View p1)
    {
        switch(p1.getId()){
            case R.id.dialogaddview_add:
        if(validate()){
			if(oal!=null)
				oal.onAdd(url.getText().toString().trim(),name.getText().toString().trim(),dir);
        }else return;
        break;
        }
        dismiss();
    }
private boolean validate(){
    boolean flag = true;
    
    if(name.getText().toString().trim().isEmpty()){
	t_name.setError(getContext().getResources().getString(R.string.site_name_null));
      flag=false;
    }else t_name.setErrorEnabled(false);
    if(url.getText().toString().trim().isEmpty()){
        t_url.setError(getContext().getResources().getString(R.string.site_url_null));
       flag=false;
    }else t_url.setErrorEnabled(false);
    
    
    return flag==true;
}

@Override
public void show()
{
	show(null);
}
public void show(String dir)
{
	this.dir=dir;
    super.show();
		url.setText(null);
		name.setText(null);
		t_url.setErrorEnabled(false);
		t_name.setErrorEnabled(false);
		name.requestFocus();
}

public void setOnAddLostener(OnAddListener o){
	oal=o;
}
private OnAddListener oal;
    public abstract interface OnAddListener{
		void onAdd(String url,String title,String dir);
	}
}
