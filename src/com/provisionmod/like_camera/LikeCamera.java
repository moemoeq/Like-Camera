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
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class LikeCamera extends Activity implements SurfaceHolder.Callback {

	Camera camera;
	SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	RelativeLayout info_layout;
	boolean previewing = false, focus_complete = false, click_able = true;
	LayoutInflater controlInflater = null;
	static Bitmap bitmap;
	static int mode = -1;
	ImageButton like_btn, hate_btn, more;
	LinearLayout menu_bar, info;
	DisplayMetrics displayMetrics;

	/** Called when the activity is first created. */
	AutoFocusCallback mAutoFocus = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			if (success) {
				focus_complete = true;
				if (mode != -1) {
					camera.takePicture(shutterCallback, rawCallback,
							jpegCallback);
					// 좋아요 또는 싫어요 버튼을 누른 후 포커싱이 완료되면 캡쳐
				}
			} else {
				camera.cancelAutoFocus();
				click_able = true;
			}
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		displayMetrics = getResources().getDisplayMetrics();
		menu_bar = (LinearLayout) findViewById(R.id.menu_bar);
		menu_bar.setVisibility(View.GONE);
		more = (ImageButton) findViewById(R.id.more);
		info_layout = (RelativeLayout) findViewById(R.id.info_layout);
		info_layout.setVisibility(View.GONE);
		info = (LinearLayout) findViewById(R.id.info);
		like_btn = (ImageButton) findViewById(R.id.like_btn);
		hate_btn = (ImageButton) findViewById(R.id.hate_btn);
		OnTouchListener btnTouch = new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (!click_able)
					return false;
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					focus_complete = false;
					camera.autoFocus(mAutoFocus);
					// 좋아요 또는 싫어요 버튼이 눌리면 포커스 잡기
				} else if (event.getAction() == MotionEvent.ACTION_MOVE
						&& !v.isPressed()) {
					camera.cancelAutoFocus();
					// 버튼 밖으로 손이 나갈 경우 포커싱 취소
				}
				return false;
			}
		};
		like_btn.setOnTouchListener(btnTouch);
		hate_btn.setOnTouchListener(btnTouch);

		LayoutParams lp = menu_bar.getLayoutParams();
		lp.width = (int) ((double) (displayMetrics.heightPixels) / 480 * 640);
		lp.height = LayoutParams.WRAP_CONTENT;
		menu_bar.setLayoutParams(lp);

		lp = info.getLayoutParams();
		lp.width = (int) ((double) (displayMetrics.heightPixels) / 480 * 640);
		lp.height = displayMetrics.heightPixels;
		info.setLayoutParams(lp);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
		lp = surfaceView.getLayoutParams();
		lp.width = (int) ((double) (displayMetrics.heightPixels) / 480 * 640);
		lp.height = displayMetrics.heightPixels;
		surfaceView.setLayoutParams(lp); // 프리뷰 크기 설정
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this); // SurfaceHolder.Callback을 설정함으로써
											// Surface가 생성/소멸되었음을 알 수 있음
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void Info(View target) {
		info_layout
				.setVisibility(info_layout.getVisibility() == View.GONE ? View.VISIBLE
						: View.GONE); // 정보 보여주기
	}

	public void Like(View target) { // 좋아요 버튼
		if (!click_able)
			return;
		click_able = false;
		mode = 0; // 모드를 0(좋아요)으로 바꿈
		if (focus_complete) {
			camera.takePicture(shutterCallback, rawCallback, jpegCallback);
			focus_complete = false;
		}
	}

	public void Hate(View target) { // 싫어요 버튼
		if (!click_able)
			return;
		click_able = false;
		mode = 1; // 모드를 1(싫어요)로 바꿈
		if (focus_complete) {
			camera.takePicture(shutterCallback, rawCallback, jpegCallback);
			focus_complete = false;
		}
	}

	public void CancelInfo(View target) {
		info_layout.setVisibility(View.GONE);
	}

	public void showGallery(View target) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, 1);
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

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& keyCode == KeyEvent.KEYCODE_BACK
				&& info_layout.getVisibility() == View.VISIBLE) {
			info_layout.setVisibility(View.GONE); // Back키 누르면 정보 닫기
			return true;
		} else if (event.getAction() == KeyEvent.ACTION_DOWN
				&& keyCode == KeyEvent.KEYCODE_BACK
				&& menu_bar.getVisibility() == View.VISIBLE) {
			more.setBackgroundDrawable(getResources()
					.getDrawable(R.color.trans));
			menu_bar.setVisibility(View.GONE); // Back키 누르면 메뉴 닫기
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) { // 카메라 Preview 시작
		if (previewing) {
			camera.stopPreview();
		}

		try {
			// TODO
			Camera.Parameters p = camera.getParameters();
			p.setPreviewSize(
					(int) ((double) (displayMetrics.heightPixels) / 480 * 640),
					displayMetrics.heightPixels);
			// surfaceView의 크기에 맞게 Preview 크기 설정
			camera.setParameters(p);
			camera.setPreviewDisplay(surfaceHolder);
			camera.startPreview();
			// Preview 보여주기
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

	public boolean Capture(Camera.PictureCallback jpegHandler) { // 찍은 이미지를 저장을
																	// 위해
																	// Callback
																	// 클래스 구현
		if (camera != null) {
			camera.takePicture(null, null, jpegHandler); // 카메라가 보고있는 이미지를 촬영을
															// 위해
															// takePicture메서드를
															// 호출
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
				bitmap = BitmapFactory.decodeByteArray(data, 0, data.length); // bitmap
																				// 변수에
																				// 사진을
																				// 저장
				Intent i = new Intent(LikeCamera.this, PhotoViewer.class);
				startActivityForResult(i, 0); // 캡쳐 후 사진 뷰어 띄움
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				if (intent.getExtras().getInt("R") == 1) {
					Save(bitmap); // 사진 뷰어에서 확인을 누르면 사진 저장
				}
			}
			bitmap.recycle(); // OOM 방지
			click_able = true;
		} else if (requestCode == 1) {
			try {
				Uri imgUri = intent.getData();
				Bitmap selPhoto = Images.Media.getBitmap(getContentResolver(),
						imgUri);
			} catch (Exception e) {
				e.printStackTrace();
			}
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
			bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
		} catch (Exception e) {
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) { // 메뉴키로 메뉴 열고 닫기
		if (menu_bar.getVisibility() == View.GONE) {
			more.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.btn_pressed));
			menu_bar.setVisibility(View.VISIBLE);
		} else {
			more.setBackgroundDrawable(getResources()
					.getDrawable(R.color.trans));
			menu_bar.setVisibility(View.GONE);
		}
		return false;
	}

	public void showMenu(View target) { // 더보기 버튼으로 메뉴 열고 닫기
		if (menu_bar.getVisibility() == View.GONE) {
			more.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.btn_pressed));
			menu_bar.setVisibility(View.VISIBLE);
		} else {
			more.setBackgroundDrawable(getResources()
					.getDrawable(R.color.trans));
			menu_bar.setVisibility(View.GONE);
		}
	}
}