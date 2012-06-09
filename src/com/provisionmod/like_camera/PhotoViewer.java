package com.provisionmod.like_camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class PhotoViewer extends Activity {
	ImageView photo;
	TextView question;
	DisplayMetrics displayMetrics;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewer);
		displayMetrics = getResources().getDisplayMetrics();
		photo = (ImageView) findViewById(R.id.photo);
		LayoutParams lp = photo.getLayoutParams();
		lp.width = (int) ((double) (displayMetrics.heightPixels) / 480 * 640);
		lp.height = displayMetrics.heightPixels;
		photo.setImageBitmap(LikeCamera.bitmap); //ĸ���� ���� ������ 
		photo.setScaleType(ScaleType.FIT_XY);
		String qStr = "������ " + (LikeCamera.mode == 0 ? "'���ƿ�'" : "'�Ⱦ��'")
				+ "�� ���ε��ұ��?";
		question=(TextView)findViewById(R.id.question);
		question.setText(qStr);
		question.setVisibility(TextView.VISIBLE);
	}

	public void Upload(View target) { //Ȯ�� ��ư 
		Intent intent = new Intent();
		intent.putExtra("R", 1);
		this.setResult(RESULT_OK, intent);
		finish();
	}

	public void Cancel(View target) { //��� ��ư 
		Intent intent = new Intent();
		intent.putExtra("R", 0);
		this.setResult(RESULT_OK, intent);
		finish();
	}
}
