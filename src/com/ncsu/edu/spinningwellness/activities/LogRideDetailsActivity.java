package com.ncsu.edu.spinningwellness.activities;

import java.net.MalformedURLException;
import java.util.Date;

import net.bican.wordpress.Page;
import net.bican.wordpress.User;
import net.bican.wordpress.Wordpress;
import redstone.xmlrpc.XmlRpcFault;

import com.example.spinningwellness.R;
import com.ncsu.edu.spinningwellness.entities.Ride;
import com.ncsu.edu.spinningwellness.managers.UsersManager;
import com.ncsu.edu.spinningwellness.tabpanel.MenuConstants;
import com.ncsu.edu.spinningwellness.tabpanel.MyTabHostProvider;
import com.ncsu.edu.spinningwellness.tabpanel.TabView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LogRideDetailsActivity extends BaseActivity {

	Ride ride;

	long timeOfRide = 0;
	double distanceCovered, averageSpeed = 0.0, cadence, heartRate;

	TextView textViewRideName, textViewDistanceCovered, textViewTimeOfRide, textViewAverageSpeed, textViewHeartRate, textViewCadence, textViewExperience;
	Button btnSubmit;
	
	LinearLayout progressBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Draw menu
		tabProvider = new MyTabHostProvider(LogRideDetailsActivity.this);
		TabView tabView = tabProvider.getTabHost(MenuConstants.JOIN_RIDES);
		tabView.setCurrentView(R.layout.log_ride_details_activity);
		setContentView(tabView.render());

		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		setFormFields();
	}

	@Override
	protected void setTitle() {
		ride = getIntent().getParcelableExtra("Ride");

		final TextView myTitleText = (TextView)findViewById(R.id.myTitle);
		myTitleText.setText(SPINNING_WELLNESS + " " + "Log Ride Details for Ride:" + ride.getName());
	}

	private void validateUserInputAndCallAsyncTask() {

		//Add validation code herer
		boolean isValid = true;
		if(isValid) {
			progressBar.setVisibility(View.VISIBLE);
			LinearLayout createRideForm = (LinearLayout) findViewById(R.id.logRideDetailsForm);
			createRideForm.setVisibility(View.INVISIBLE);

			new LogRideDetailsTask().execute();
		} else {
			//Show the error message to user
			System.out.println("Error in user input");
		}
	}

	private void setFormFields() {
		
		progressBar = (LinearLayout) findViewById(R.id.logRideDetailsSpinner);
		progressBar.setVisibility(View.INVISIBLE);

		//Get information from intent
		//		distanceCovered = getIntent().getParcelableExtra("DistanceCovered");
		//		averageSpeed = getIntent().getParcelableExtra("AverageSpeed");
		//		timeOfRide = getIntent().getParcelableExtra("TimeOfRide");

		//Get the elements on the form
		textViewRideName = (TextView) findViewById(R.id.textViewRideName);
		textViewDistanceCovered = (TextView) findViewById(R.id.textViewDistanceCovered);
		textViewTimeOfRide = (TextView) findViewById(R.id.textViewTimeOfRide);
		textViewAverageSpeed = (TextView) findViewById(R.id.textViewAverageSpeed);
		textViewHeartRate = (TextView) findViewById(R.id.textViewHeartRate);
		textViewCadence = (TextView) findViewById(R.id.textViewCadence);
		textViewExperience = (TextView) findViewById(R.id.textViewExperience);

		//Set the parameters which are recorded automatically
		textViewRideName.setText(ride.getName());
		textViewDistanceCovered.setText(((Double) distanceCovered).toString());
		textViewAverageSpeed.setText(((Double) averageSpeed).toString());
		textViewTimeOfRide.setText(((Long) timeOfRide).toString());

		//Set onClickListener for the submit button
		Button button  = (Button) findViewById(R.id.btnSubmit);
		button.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				validateUserInputAndCallAsyncTask();
			}
		});
	}

	private void moveToJoinRidesPage() {
		
		progressBar.setVisibility(View.INVISIBLE);

		LinearLayout logRideDetailsForm = (LinearLayout) findViewById(R.id.logRideDetailsForm);
		logRideDetailsForm.setVisibility(View.VISIBLE);

		//clear the text boxes
		textViewRideName.setText(ride.getName());
		textViewDistanceCovered.setText(((Double) distanceCovered).toString());
		textViewTimeOfRide.setText(((Long) timeOfRide).toString());
		textViewAverageSpeed.setText(((Double) averageSpeed).toString());
		textViewHeartRate.setText("");
		textViewCadence.setText("");
		textViewExperience.setText("");

		Intent joinRidesIntent = new Intent(LogRideDetailsActivity.this, JoinRidesActivity.class); 
		LogRideDetailsActivity.this.startActivity(joinRidesIntent);
	}

	private class LogRideDetailsTask extends AsyncTask<Void,Void,User> {

		Exception error;

		protected User doInBackground(Void... params) {

			//Log activity to backend
			Date activityDate = new Date();
			cadence = Double.parseDouble(((TextView) findViewById(R.id.textViewCadence)).getText().toString());
			heartRate = Double.parseDouble(((TextView) findViewById(R.id.textViewHeartRate)).getText().toString());
			String result = UsersManager.logActivity(ride.getId(), BaseActivity.username, distanceCovered, cadence, averageSpeed, timeOfRide, heartRate, activityDate);
			if(!result.equalsIgnoreCase("success")) {
				error = new Exception();
			}

			//Post to blog
			System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
			Wordpress wp;
			try {
				wp = new Wordpress(BaseActivity.username, BaseActivity.password, xmlRpcUrl);
				Page recentPost = new Page();
				recentPost.setDescription(((TextView) findViewById(R.id.textViewExperience)).getText().toString());
				wp.newPost(recentPost, true);
			} catch (MalformedURLException e) {
				error = e;
			} catch (XmlRpcFault e) {
				error = e;
			}
			return null;
		}

		protected void onPostExecute(User result) {
			//Redirect to join rides page
			//if(error == null) {
				moveToJoinRidesPage();
			//} else {
				//Display the error to user
			//}
		}
	}
}