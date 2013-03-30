package srtp.android.wordlearning;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DrawView extends SurfaceView implements SurfaceHolder.Callback{

	private Paint textPaint = new Paint();
	private boolean isDown = false;
	private ViewThread mViewThread;
	private PictureProcessThread mPictureThread;
	//private Camera camera;

	public Rect rect = new Rect(0, 0, 0, 0);
	
	public DrawView(Context context, Camera camera) {
		super(context);
		getHolder().addCallback(this);
		
		textPaint.setARGB(200, 0, 0, 255);
		setWillNotDraw(false);
		
		mViewThread = new ViewThread(this);
		mPictureThread = new PictureProcessThread(camera, rect, "/sdcard/");
	}
	
	protected void doDraw(Long elapsed, Canvas canvas) {
		if (isDown) {
			Log.d("RECT", String.format("%d %d %d %d", rect.left, rect.top, rect.right, rect.bottom));
			canvas.drawRect(rect, textPaint);
			
			Bitmap capBitmap = mPictureThread.GetCapturedBitmap();
			if (!mPictureThread.isAlive() && capBitmap != null) {
				canvas.drawBitmap(capBitmap, 0, 0, null);
			}
		}
	}
	
	@Override 
	public boolean onTouchEvent(MotionEvent event) {
		
		int action = event.getAction();
		switch (action) {
		
		case MotionEvent.ACTION_DOWN:
			Log.d("RECT", "mouse down");
			rect.left = rect.right = (int)event.getX();
			rect.top = rect.bottom = (int)event.getY();
			isDown = true;
			break;
			
		case MotionEvent.ACTION_MOVE:
			Log.d("RECT", "mouse move");
			if (isDown) {
				rect.right = (int)event.getX();
				rect.bottom = (int)event.getY();
			}
			break;
			
		case MotionEvent.ACTION_UP:
			isDown = false;
			mPictureThread.start();
			break;

		default:
			break;
		}

		return true;
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		if (!mViewThread.isAlive()) {
			mViewThread = new ViewThread(this);
			mViewThread.setRunning(true);
			mViewThread.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		if (!mViewThread.isAlive()) {
			mViewThread.setRunning(false);
		}
	}	
}
