package org.neissa.draw;

import android.app.*;
import android.os.*;
import android.view.*;
import android.graphics.*;
import android.content.*;
import java.util.*;

public class MainActivity extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(new DrawView(this));
    }
	class DrawView extends View{
		ArrayList<Float[]> lines = new ArrayList<Float[]>();
		HashMap<Integer,Float> lastXs = new HashMap<Integer,Float>();
		HashMap<Integer,Float> lastYs = new HashMap<Integer,Float>();
		float startX = 0;
		float startY = 0;
		float endX = 0;
		float endY = 0;
		
		DrawView(Context c)
		{
			super(c);
		}

		@Override
		protected void onDraw(Canvas canvas)
		{
			super.onDraw(canvas);
			int[] colors = {0xFFFF0000,0xFF00FF00,0xFF0000FF,0xFFFFFF00,0xFFFF00FF,0xFF00FFFF,0xFFFFFFFF,
							0xFF880000,0xFF008800,0xFF000088,0xFF888800,0xFF880088,0xFF008888,0xFF888888,
							0xFFCC0000,0xFF00CC00,0xFF0000CC,0xFFCCCC00,0xFFCC00CC,0xFF00CCCC,0xFFCCCCCC,
							0xFF440000,0xFF004400,0xFF000044,0xFF444400,0xFF440044,0xFF004444,0xFF444444};
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(5);
			for(Float[] line : lines)
			{
				paint.setColor(colors[line[0].intValue()%colors.length]);
				canvas.drawLine(line[1],line[2],line[3],line[4],paint);
			}
			while(lines.size() > 500)
				lines.remove(0);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			int id;
			switch (event.getActionMasked()) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_POINTER_DOWN:
					id = event.getPointerId(event.getActionIndex());
					addLine(id, event.getX(event.getActionIndex()),event.getY(event.getActionIndex()),event.getX(event.getActionIndex()),event.getY(event.getActionIndex()));
					break;
				case MotionEvent.ACTION_MOVE:
					for(int i=0; i<event.getPointerCount(); i++)
					{
						id = event.getPointerId(i);
						addLine(id, lastXs.get(id),lastYs.get(id),event.getX(i),event.getY(i));
					}
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					id = event.getPointerId(event.getActionIndex());
					addLine(id, lastXs.get(id),lastYs.get(id),event.getX(event.getActionIndex()),event.getY(event.getActionIndex()));
					break;
			}
			return true;
		}
		public void addLine(int id, float x1, float y1, float x2, float y2)
		{
			lines.add(new Float[]{(float)id,x1,y1,x2,y2});
			lastXs.put(id,x2);
			lastYs.put(id,y2);
			invalidate();
		}
	}
}
