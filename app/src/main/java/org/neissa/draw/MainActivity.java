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
		HashMap<Integer, Integer> lastColors = new HashMap<Integer, Integer>();
		int indexColor;
		float startX = 0;
		float startY = 0;
		float endX = 0;
		float endY = 0;
		int[] colors = {0xFFFF0000,0xFF00FF00,0xFF0000FF,0xFFFFFF00,0xFFFF00FF,0xFF00FFFF,0xFFFFFFFF};

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
			for(Float[] line : lines)
			{
				paint.setColor(colors[line[5].intValue()]);
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
					indexColor = (indexColor+1)%colors.length;
					lastColors.put(id,indexColor);
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
			lastXs.put(id,x2);
			lastYs.put(id,y2);
			if(Math.abs(x1-x2)+Math.abs(y1-y2) < 0.5)
				return;
			lines.add(new Float[]{(float)id,x1,y1,x2,y2,(float)lastColors.get(id)});
			invalidate();
		}
	}
}
