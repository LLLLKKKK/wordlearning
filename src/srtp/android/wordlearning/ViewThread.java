package srtp.android.wordlearning;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

public class ViewThread extends Thread{
	
	private DrawView mView;
	private SurfaceHolder mHolder;
	private boolean mRun = false;
	private long mStartTime;
	private long mElapsed;
	
	public ViewThread(DrawView view) {
		mView = view;
		mHolder = mView.getHolder();
	}
	
	public void setRunning(boolean run) {
		mRun = run;
	}
	
	@Override
	public void run() {
		Log.d(getName(), "running");
		Canvas canvas = null;
		mStartTime = System.currentTimeMillis();
		while (mRun) {
			canvas = mHolder.lockCanvas();
			if (canvas != null) {
				mView.doDraw(mElapsed, canvas);
				mElapsed = System.currentTimeMillis() - mStartTime;
				mHolder.unlockCanvasAndPost(canvas);
			}
			mStartTime = System.currentTimeMillis();
		}
	}
}
