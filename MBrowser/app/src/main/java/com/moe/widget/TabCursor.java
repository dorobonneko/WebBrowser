package com.moe.widget;
import android.view.*;
import android.content.*;
import android.graphics.*;
import android.support.v4.view.*;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import com.moe.Mbrowser.R;

public class TabCursor extends View implements ViewPager.OnPageChangeListener
{
	private ViewPager vp;
	private Observer server;
	private int smallwidth=25,smallheight=15,cellspace=12;
	private Bitmap background;
	private Rect rect=new Rect();
	private Paint paint=new Paint();
	public TabCursor(Context context,AttributeSet attrs){
		super(context,attrs);
		setWillNotDraw(false);
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setColor(context.getResources().getColor(R.color.accent));
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
		super.onMeasure(widthMeasureSpec,heightMeasureSpec);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.save();
		super.onDraw(canvas);
		canvas.restore();
		if(background!=null){
		canvas.drawBitmap(background,0,0,null);
		rect.left=vp.getCurrentItem()*smallwidth+vp.getCurrentItem()*cellspace+1;
		rect.top=1;
		rect.bottom=getHeight()-1;
		rect.right=rect.left+smallwidth-2;
		canvas.drawRect(rect,paint);
		}
	}
	private void drawBackground(){
		if(background!=null)background.recycle();
		int count=vp.getAdapter().getCount();
		int width=count*smallwidth+(count-1)*cellspace;
		//重新调整布局大小
		ViewGroup.LayoutParams vl=getLayoutParams();
		vl.width=width;
		vl.height=smallheight;
		//onMeasure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(smallheight,View.MeasureSpec.EXACTLY));
		background=Bitmap.createBitmap(width,smallheight,Bitmap.Config.ARGB_4444);
		Canvas canvas=new Canvas(background);
		Paint paint=new Paint();
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setStyle(Paint.Style.STROKE);//空心
		paint.setStrokeWidth(1f);//画笔宽度
		for(int i=0;i<count;i++){
			Rect rect=new Rect();
			rect.left=i*smallwidth+i*cellspace;
			rect.top=0;
			rect.right=rect.left+smallwidth;
			rect.bottom=smallheight;
			canvas.drawRect(rect,paint);
		}
	}
	public void setUpViewPager(ViewPager vp){
		this.vp=vp;
		vp.setOnPageChangeListener(this);
		vp.getAdapter().registerDataSetObserver(server=new Observer());
		drawBackground();
	}

	@Override
	protected void onDetachedFromWindow()
	{
		vp.getAdapter().unregisterDataSetObserver(server);
		vp=null;
		server=null;
		super.onDetachedFromWindow();
	}

	@Override
	public void onPageScrolled(int p1, float p2, int p3)
	{
		invalidate();
	}

	@Override
	public void onPageSelected(int p1)
	{
		
	}

	@Override
	public void onPageScrollStateChanged(int p1)
	{
		
	}
	
	class Observer extends DataSetObserver
	{

		@Override
		public void onChanged()
		{
			drawBackground();
			invalidate();
		}

		@Override
		public void onInvalidated()
		{
			drawBackground();
			invalidate();
		}
		
	}
}
