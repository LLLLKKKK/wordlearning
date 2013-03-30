package srtp.android.wordlearning;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.googlecode.tesseract.android.TessBaseAPI;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Environment;
import android.util.Log;

public class PictureProcessThread extends Thread {
	
	private Camera camera;
	private Rect rect;
	private Bitmap capturedBitmap;
	private Bitmap showBitmap;
	private String wordString;
	private URI picUri;
	
	private final String queryUrl = "http://222.205.48.158/api/translate/?q=";
	private String path;

	public Bitmap GetCapturedBitmap() {
		return this.capturedBitmap;
	}
	
	public String getWordString() {
		return this.wordString;
	}
	
	public PictureProcessThread(Camera camera, Rect rect, String path) {
		this.camera = camera;
		this.rect = rect;
		this.path = path;
	}
	
	@Override
	public void run() {
		camera.takePicture(null, null, jpegCallback);
		
		FileOutputStream outStream;
		try {
			Log.d("path", Environment.getExternalStorageDirectory().toString());
			outStream = new FileOutputStream(String.format(
					Environment.getExternalStorageDirectory() + "/%d.jpg", System.currentTimeMillis()));
			capturedBitmap.compress(CompressFormat.JPEG, 100, outStream);
			outStream.close();
			
			try {
				HttpGet httpGet = new HttpGet(queryUrl + wordString);
				httpGet.setHeader("Content-Type", "application/json");
				
				HttpClient httpClient = new DefaultHttpClient();
				HttpResponse httpResponse = httpClient.execute(httpGet);
				
				if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String reString = convertStreamToString(httpResponse.getEntity().getContent());
					
					Gson gson = new Gson();
					JsonParser parser = new JsonParser();
					JsonArray array = parser.parse(reString).getAsJsonArray();
					JsonDataObject data = gson.fromJson(array.get(1), JsonDataObject.class);
					this.wordString = data.result;
					this.picUri = new URI(data.img_url);
					
					HttpGet downloadGet = new HttpGet(picUri);
					HttpResponse response = httpClient.execute(downloadGet);
					HttpEntity entity = response.getEntity();
					String filePath = path + wordString + ".jpg";
					entity.writeTo(new FileOutputStream(filePath));
					
					showBitmap = BitmapFactory.decodeStream(getClass().getResourceAsStream(filePath));
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	PictureCallback jpegCallback = new PictureCallback() {
		
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			capturedBitmap = Bitmap.createBitmap(BitmapFactory.decodeByteArray(data, 0, data.length), 
					rect.left, rect.top, rect.width(), rect.height());
			TessBaseAPI baseApi = new TessBaseAPI();

			baseApi.init("file:///android_asset/", "eng"); baseApi.setImage(capturedBitmap);
			wordString = baseApi.getUTF8Text();
			baseApi.end();
		}
	};
	
	private static String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}	
}
