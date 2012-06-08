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
		surfaceHolder.addCallback(this); // SurfaceHolder.Callback�� ���������ν�
											// Surface�� ����/�Ҹ�Ǿ����� �� �� ����
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void Like(View target) { // ���ƿ� ��ư
		camera.takePicture(shutterCallback, rawCallback, jpegCallback);
		mode = 0; // ��带 0(���ƿ�)���� �ٲٰ� ĸ��
	}

	public void Hate(View target) { // �Ⱦ�� ��ư
		camera.takePicture(shutterCallback, rawCallback, jpegCallback);
		mode = 1; // ��带 1(�Ⱦ��)�� �ٲٰ� ĸ��
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) { // ī�޶� Preview�� ǥ���� ��ġ ����
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
			int height) { // ī�޶� Preview ���� 
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
	public void surfaceDestroyed(SurfaceHolder holder) { // ī�޶� �ڿ� �ݳ�
		camera.stopPreview();
		camera.release();
		camera = null;
		previewing = false;
	}

	public boolean Capture(Camera.PictureCallback jpegHandler) { // ���� �̹����� ������ ���� Callback Ŭ���� ���� 
		if (camera != null) {
			camera.takePicture(null, null, jpegHandler); //ī�޶� �����ִ� �̹����� �Կ��� ���� takePicture�޼��带 ȣ�� 
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
						data.length); // bitmap ������ ������ ����
				Intent i = new Intent(LikeCamera.this, PhotoViewer.class);
				startActivityForResult(i, 0); // ĸ�� �� ���� ��� ���
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode == RESULT_OK) {
			if (intent.getExtras().getInt("R") == 1)
				Save(bitmap); // ���� ���� Ȯ���� ������ ���� ����
		}
	}

	private void Save(Bitmap bm) { // ���� ����
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