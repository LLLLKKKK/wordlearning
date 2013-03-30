package srtp.android.wordlearning;

import android.os.Bundle;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

	private CameraPreview cameraPreview;
	private DrawView drawView;
	private FrameLayout alParentFrameLayout;

	private Camera camera;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onPause() {
    	super.onPause();
    	
    	if (cameraPreview != null) {
    		cameraPreview.onPause();
    		cameraPreview = null;
    	}
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	Load();
    }	
    
    private void Load() {
    	
    	try {
    		camera = Camera.open();
			
			alParentFrameLayout = new FrameLayout(this);
			alParentFrameLayout.setLayoutParams(
					new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
			cameraPreview = new CameraPreview(this, camera);
			alParentFrameLayout.addView(cameraPreview);
		
			drawView = new DrawView(this, camera);
			drawView.setZOrderMediaOverlay(true);
			drawView.getHolder().setFormat(PixelFormat.TRANSPARENT);
			alParentFrameLayout.addView(drawView);

			setContentView(alParentFrameLayout);
		} catch (Exception e) {
			e.printStackTrace();

			Toast toast = Toast.makeText(
					getApplicationContext(), "Something bad happens", Toast.LENGTH_SHORT);
			toast.show();
			finish();
		}
	}
}
