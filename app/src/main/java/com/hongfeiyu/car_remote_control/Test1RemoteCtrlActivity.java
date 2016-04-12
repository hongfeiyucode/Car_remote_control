package com.hongfeiyu.car_remote_control;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class Test1RemoteCtrlActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);


//		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.test1_remote_ctrl);

//		setResult(Activity.RESULT_CANCELED);
	};  

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	public void onClick_Up(View view)
	{

	}
	
}