package org.neissa.draw;

import android.app.*;
import android.os.*;
import android.view.*;
import android.graphics.*;
import android.content.*;
import java.util.*;
import android.widget.*;
import android.net.*;
import android.util.*;
import android.graphics.drawable.*;
import java.io.*;
import android.view.MotionEvent.*;

public class MainActivity extends Activity 
{
	public static int indexColor = 0;
	public static final int[] colors = {0xFFFF0000,0xFF00FF00,0xFF0000FF,0xFFFFFF00,0xFFFF00FF,0xFF00FFFF,0xFFFFFFFF};
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		FrameLayout layout = new FrameLayout(this);
		
		Uri imageUri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
		//imageUri = Uri.parse("content://media/external/images/media/149792");
		if(imageUri != null)
		{
			InputStream inputStream = null;
			try
			{
				inputStream = getContentResolver().openInputStream(imageUri);
			}
			catch (FileNotFoundException e)
			{
				inputStream = null;
			}

			Bitmap largeBitmap = inputStream == null ? null : BitmapFactory.decodeStream(inputStream);
			if(largeBitmap != null)
			{
				int nh = (int) ( largeBitmap.getHeight() * (1024.0 / largeBitmap.getWidth()) );
				Bitmap fitBitmap = Bitmap.createScaledBitmap(largeBitmap, 1024, nh, true);
				ImageView background = new ImageView(this);
				background.setImageBitmap(fitBitmap);
				layout.addView(background,-1,-1);
			}
		}
		
		DrawView draw = new DrawView(this);
		layout.addView(draw,-1,-1);
        setContentView(layout);
    }
	
	public class DrawView extends View{
		
		public ArrayList<DrawPath> paths = new ArrayList<DrawPath>();
		public HashMap<Integer,DrawPath> lastPaths = new HashMap<Integer,DrawPath>();
		
		DrawView(Context c)
		{
			super(c);
		}

		@Override
		protected void onDraw(Canvas canvas)
		{
			super.onDraw(canvas);
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(5);
			for(DrawPath path : paths)
			{
				paint.setColor(path.color);
				path.compute(false);
				canvas.drawPath(path,paint);
			}
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			PointerCoords point = new PointerCoords();
			switch (event.getActionMasked()) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_POINTER_DOWN:
					event.getPointerCoords(event.getActionIndex(),point);
					addPath(event.getPointerId(event.getActionIndex()), true, point);
					break;
				case MotionEvent.ACTION_MOVE:
					for(int i=0; i<event.getPointerCount(); i++)
					{
						event.getPointerCoords(i,point);
						addPath(event.getPointerId(i), false, point);
					}
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					event.getPointerCoords(event.getActionIndex(),point);
					addPath(event.getPointerId(event.getActionIndex()), false, point);
					break;
			}
			return true;
		}
		public void addPath(int id, boolean creation, PointerCoords point)
		{
			if(creation)
				createDrawPath(id,new DrawPointerCoords(point));
			else
				updateDrawPath(id,new DrawPointerCoords(point));
			invalidate();
		}
		public void createDrawPath(int id, DrawPointerCoords point)
		{
			DrawPath path = lastPaths.containsKey(id) && lastPaths.get(id).points.size() <= 1 ? lastPaths.get(id) : new DrawPath(id);
			indexColor = (indexColor+1)%colors.length;
			path.color = colors[indexColor];
			path.startAt(point);
			if(paths.contains(path))
				return;
			if(lastPaths.containsKey(id))
				lastPaths.get(id).compute(true);
			lastPaths.put(id,path);
			paths.add(path);
			while(paths.size() > 500)
				paths.remove(0);
		}
		public void updateDrawPath(int id, DrawPointerCoords point)
		{
			lastPaths.get(id).drawTo(point);
		}

		public class DrawPath extends Path
		{
			public int id;
			public int color;
			public ArrayList<DrawPointerCoords> points = new ArrayList<DrawPointerCoords>();
			public boolean finalized = false;

			public DrawPath(int currentId)
			{
				super();
				id = currentId;
			}

			public void startAt(DrawPointerCoords point)
			{
				points.clear();
				points.add(point);
			}

			public void drawTo(DrawPointerCoords point)
			{
				DrawPointerCoords lastPoint = points.get(points.size()-1);
				if(Math.abs(lastPoint.x-point.x) + Math.abs(lastPoint.y-point.y) < 0.5)
					return;
				points.add(point);
			}

			public void compute(boolean finalize)
			{
				if(finalized)
					return;

				if(points.size() > 1)
				{
					float z = 5;
					for(int i = points.size() - 2; i < points.size(); i++)
					{
						if(i >= 0)
						{
							DrawPointerCoords point = points.get(i);
							if(i == 0)
							{
								DrawPointerCoords next = points.get(i + 1);
								point.dx = (next.x - point.x) / z;
								point.dy = (next.y - point.y) / z;
							}
							else if(i == points.size() - 1)
							{
								DrawPointerCoords prev = points.get(i - 1);
								point.dx = (point.x - prev.x) / z;
								point.dy = (point.y - prev.y) / z;
							}
							else
							{
								DrawPointerCoords next = points.get(i + 1);
								DrawPointerCoords prev = points.get(i - 1);
								point.dx = (next.x - prev.x) / z;
								point.dy = (next.y - prev.y) / z;
							}
						}
					}
				}
				
				reset();
				boolean first = true;
				for(int i = 0; i < points.size(); i++)
				{
					DrawPointerCoords point = points.get(i);
					if(first)
					{
						first = false;
						moveTo(point.x, point.y);
					}
					else
					{
						DrawPointerCoords prev = points.get(i - 1);
						cubicTo(prev.x + prev.dx, prev.y + prev.dy, point.x - point.dx, point.y - point.dy, point.x, point.y);
					}
				}

				if(finalize)
					finalized = true;
			}

		}
		public class DrawPointerCoords
		{
			public float dx = 0;
			public float dy = 0;
			public float x = 0;
			public float y = 0;
			DrawPointerCoords(PointerCoords point)
			{
				x = point.x;
				y = point.y;
			}
		}
	}

}
