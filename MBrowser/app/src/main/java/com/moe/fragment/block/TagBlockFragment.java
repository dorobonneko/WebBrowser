package com.moe.fragment.block;
import com.moe.fragment.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;
import com.moe.Mbrowser.R;
import com.moe.adapter.TagBlockAdapter;
import android.support.v7.widget.LinearLayoutManager;
import java.util.LinkedHashMap;
import java.util.Map;
import com.moe.database.AdBlockDatabase;
import android.support.design.widget.TextInputLayout;
import android.widget.EditText;
import com.moe.database.Sqlite;
import com.moe.utils.DialogUtils;
import android.text.TextUtils;
import android.content.DialogInterface;
import android.widget.TextView;

public class TagBlockFragment extends PreferenceFragment implements View.OnClickListener,TagBlockAdapter.OnClickListener,DialogInterface.OnClickListener,TagBlockAdapter.OnLongClickListener
{
	private AlertDialog add;
	private TagBlockAdapter aba;
	private Map<String,String> mss;
	private AdBlockDatabase abd;
	private TextInputLayout til;
	private EditText msg;
	//标记状态
	private boolean edit=false;
	private int position;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.block_view,container,false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		RecyclerView rv=(RecyclerView)view.findViewById(R.id.block_view_list);
		view.findViewById(R.id.block_view_add).setOnClickListener(this);
		rv.setLayoutManager(new LinearLayoutManager(getActivity()));
		rv.setAdapter(aba = new TagBlockAdapter(mss = new LinkedHashMap<>(), (TextView)view.findViewById(R.id.block_view_title)));
		aba.setOnClickListener(this);
		aba.setOnLongClickListener(this);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{

		super.onActivityCreated(savedInstanceState);
		abd = Sqlite.getInstance(getActivity(), AdBlockDatabase.class);
		mss.putAll(abd.getAllData());
		aba.setType(TagBlockAdapter.Type.HOST, 0);
		aba.notifyDataSetChanged();
	}
	private void createAdd()
	{
		til = new TextInputLayout(getActivity());
		msg = new EditText(getActivity());
		msg.setSingleLine(true);
		til.addView(msg);
		til.setErrorEnabled(true);
		add = new AlertDialog.Builder(getActivity()).setView(til)
			.setNeutralButton("删除", this)
			.setNegativeButton("确定", this)
			.setPositiveButton("取消", this).create();

	}

	@Override
	public void onHiddenChanged(boolean hidden)
	{
		if (!hidden)
		{
			mss.clear();
			mss.putAll(abd.getAllData());
			aba.setType(TagBlockAdapter.Type.HOST, 0);
			aba.notifyDataSetChanged();
		}
		super.onHiddenChanged(hidden);
	}
	@Override
	public void onClick(View p1)
	{
		if (add == null)createAdd();
		if (aba.getType() == TagBlockAdapter.Type.HOST)
			add.setTitle("添加Host");
		else
			add.setTitle("添加标签");
		edit = false;
		til.setError(null);
		add.show();
		DialogUtils.changeState(add, false);
	}
	@Override
	public boolean onBackPressed()
	{
		if (getFragmentManager()!=null&&!isHidden())
		{
			if (aba.getType() == TagBlockAdapter.Type.SELECTOR)
			{
				aba.setType(TagBlockAdapter.Type.HOST, 0);
				aba.notifyDataSetChanged();
			}
			else
			{
				getFragmentManager().beginTransaction().hide(this).detach(this).remove(this).commit();
			}
			return true;
		}
		return false;
	}
	@Override
	public void onClick(DialogInterface p1, int p2)
	{
		switch (p2)
		{
			case AlertDialog.BUTTON_NEUTRAL:
				if (edit == true)
				{
					DialogUtils.changeState(add, true);
					switch (aba.getType())
					{
						case HOST:
							abd.delete(aba.getKey(position));
							mss.clear();
							mss.putAll(abd.getAllData());
							aba.setType(TagBlockAdapter.Type.HOST, 0);
							break;
						case SELECTOR:
							String[] value=aba.getValue();
							value[position] = null;
							abd.update(aba.getKey(aba.getKeyPosition()), value);
							mss.clear();
							mss.putAll(abd.getAllData());
							aba.setType(TagBlockAdapter.Type.SELECTOR, aba.getKeyPosition());
							break;
					}
					aba.notifyDataSetChanged();
				}
				break;
			case AlertDialog.BUTTON_NEGATIVE:
				if (TextUtils.isEmpty(msg.getText().toString().trim()))
				{
					til.setError("内容不能为空");
				}
				else
				{
					switch (aba.getType())
					{
						case HOST:
							if (edit)
							{
								String src=aba.getKey(position);//原记录
								abd.changeHost(src, msg.getText().toString().trim());}
							else
							{
								abd.add(msg.getText().toString().trim(), null);
							}
							mss.clear();
							mss.putAll(abd.getAllData());
							aba.setType(TagBlockAdapter.Type.HOST, 0);
							break;
						case SELECTOR:
							if (edit)
							{
								String[] value=aba.getValue();
								value[position] = msg.getText().toString().trim();
								abd.update(aba.getKey(aba.getKeyPosition()), value);
							}
							else
							{
								abd.add(aba.getKey(aba.getKeyPosition()), msg.getText().toString().trim());
							}
							mss.clear();
							mss.putAll(abd.getAllData());
							aba.setType(TagBlockAdapter.Type.SELECTOR, aba.getKeyPosition());
							break;
					}
					aba.notifyDataSetChanged();
					DialogUtils.changeState(add, true);
				}
				break;
			case AlertDialog.BUTTON_POSITIVE:
				DialogUtils.changeState(add, true);
				break;
		}
	}


	@Override
	public void onClick(TagBlockAdapter.Type type, int position)
	{
		this.position = position;
		switch (type)
		{
			case HOST:
				aba.setType(TagBlockAdapter.Type.SELECTOR, position);
				aba.notifyDataSetChanged();
				break;
			case SELECTOR:
				if (add == null)createAdd();
				add.setTitle("修改");
				edit = true;
				til.setError(null);
				msg.setText(aba.getValue(position));
				add.show();
				break;
		}
	}

	@Override
	public boolean onLongClick(TagBlockAdapter.Type type, int position)
	{
		this.position = position;
		if (add == null)createAdd();
		switch (type)
		{
			case HOST:
				add.setTitle("修改Host");
				msg.setText(aba.getKey(position));
				til.setError(null);
				edit = true;
				add.show();
				break;
			case SELECTOR:
				onClick(type, position);
				break;
		}
		return true;
	}


}
