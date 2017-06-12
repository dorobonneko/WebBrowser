package com.moe.fragment.preference;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import com.moe.Mbrowser.R;
import com.moe.fragment.block.TagBlockFragment;
import com.moe.fragment.block.UrlBlockFragment;
import android.content.Intent;

public class AdBkockFragment extends PreferenceFragment implements View.OnClickListener
{
	private PreferenceFragment tag,url,current;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.adblock_view, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		view.findViewById(R.id.adblock_view_url).setOnClickListener(this);
		view.findViewById(R.id.adblock_view_tag).setOnClickListener(this);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if(current!=null)current.onActivityResult(requestCode,resultCode,data);
	}

	

	@Override
	public boolean onBackPressed()
	{
		if(current!=null&&current.onBackPressed())
			return true;
		return false;
	}

	@Override
	public void onClick(View p1)
	{
		switch(p1.getId()){
			case R.id.adblock_view_tag:
				if(tag==null)tag=new TagBlockFragment();
				if(tag.isAdded())
					getFragmentManager().beginTransaction().show(tag).commit();
					else
					getFragmentManager().beginTransaction().add(R.id.adblock_view_float,tag).commit();
				current=tag;
				break;
			case R.id.adblock_view_url:
				if(url==null)url=new UrlBlockFragment();
				if(url.isAdded())
					getFragmentManager().beginTransaction().show(url).commit();
				else
					getFragmentManager().beginTransaction().add(R.id.adblock_view_float,url).commit();
				current=url;
				break;
		}
	}


	

	


}
