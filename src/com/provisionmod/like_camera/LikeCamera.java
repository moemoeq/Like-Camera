/*
 * Copyright (C) 2012 The Provision Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.provisionmod.like_camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class LikeCamera extends Activity implements SurfaceHolder.Callback {

	Camera camera;
	SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	boolean previewing = false;
	LayoutInflater controlInflater = null;
	static Bitmap bitmap;
	static int mode;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this); // SurfaceHolder.Callback을 설정함으로써
											// Surface가 생성/소멸되었음을 알 수 있음
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void Like(View target) { // 좋아요 버튼
		camera.takePicture(shutterCallback, rawCallback, jpegCallback);
		mode = 0; // 모드를 0(좋아요)으로 바꾸고 캡쳐
	}

	public void Hate(View target) { // 싫어요 버튼
		camera.takePicture(shutterCallback, rawCallback, jpegCallback);
		mode = 1; // 모드를 1(싫어요)로 바꾸고 캡쳐
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) { // 카메라 Preview를 표시할 위치 설정
		camera = Camera.open();
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException exception) {
			camera.release();
			camera = null;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) { // 카메라 Preview 시작 
		if (previewing) {
			camera.stopPreview();
		}

		try {
			Camera.Parameters p = camera.getParameters();
			p.setPreviewSize(width, height);
			camera.setParameters(p);
			camera.setPreviewDisplay(surfaceHolder);
			camera.startPreview();
			previewing = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) { // 카메라 자원 반납
		camera.stopPreview();
		camera.release();
		camera = null;
		previewing = false;
	}

	public boolean Capture(Camera.PictureCallback jpegHandler) { // 찍은 이미지를 저장을 위해 Callback 클래스 구현 
		if (camera != null) {
			camera.takePicture(null, null, jpegHandler); //카메라가 보고있는 이미지를 촬영을 위해 takePicture메서드를 호출 
			return true;
		} else {
			return false;
		}
	}

	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
		}
	};

	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
		}
	};
	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			if (data != null) {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 2;
				bitmap = BitmapFactory.decodeByteArray(data, 0,
						data.length); // bitmap 변수에 사진을 저장
				Intent i = new Intent(LikeCamera.this, PhotoViewer.class);
				startActivityForResult(i, 0); // 캡쳐 후 사진 뷰어 띄움
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode == RESULT_OK) {
			if (intent.getExtras().getInt("R") == 1)
				Save(bitmap); // 사진 뷰어에서 확인을 누르면 사진 저장
		}
	}

	private void Save(Bitmap bm) { // 사진 저장
		try {
			File path = new File("/sdcard/LikeCamera");

			if (!path.isDirectory()) {
				path.mkdirs();
			}

			FileOutputStream out = new FileOutputStream("/sdcard/LikeCamera/"
					+ System.currentTimeMillis() + ".jpeg");
			bm.compress(Bitmap.CompressFormat.JPEG, 75, out);
		} catch (FileNotFoundException e) {
		}
	}
}