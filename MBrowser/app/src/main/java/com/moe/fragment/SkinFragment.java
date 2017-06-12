package com.moe.fragment;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import com.moe.Mbrowser.R;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.GridLayoutManager;
import com.moe.adapter.SkinAdapter;
import android.content.SharedPreferences;
import com.moe.internal.Theme;


public class SkinFragment extends Fragment implements SkinAdapter.OnItemSelectedListener
{
	private RecyclerView rv;
	private SkinAdapter sa;
	private SharedPreferences shared;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// TODO: Implement this method
		return inflater.inflate(R.layout.skin_view,container,false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		//Theme.registerBackground(view);
		rv=(RecyclerView)view;
		GridLayoutManager glm=new GridLayoutManager(getActivity(),5);
		glm.setAutoMeasureEnabled(true);
		rv.setLayoutManager(glm);
		rv.setAdapter(sa=new SkinAdapter(this));
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onActivityCreated(savedInstanceState);
		sa.setOnItemSelectedListener(this);
		shared=getContext().getSharedPreferences("moe",0);
	}

	@Override
	public void onItemSelected(int position)
	{
		int index=shared.getInt("color",0);
		shared.edit().putInt("color",position).commit();
		Theme.updateTheme(sa.getColor(position));
		sa.notifyItemChanged(index);
		sa.notifyItemChanged(position);
		//sa.notifyDataSetChanged();
	}


	@Override
	public boolean onBackPressed()
	{
		// TODO: Implement this method
		return false;
	}

	
}
