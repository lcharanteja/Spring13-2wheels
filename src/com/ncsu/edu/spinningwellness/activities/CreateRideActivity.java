package com.ncsu.edu.spinningwellness.activities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.example.spinningwellness.R;
import com.ncsu.edu.spinningwellness.Utils.EmailDispatcher;
import com.ncsu.edu.spinningwellness.Utils.EventsCalendar;
import com.ncsu.edu.spinningwellness.Utils.UIUtils;
import com.ncsu.edu.spinningwellness.Utils.Utils;
import com.ncsu.edu.spinningwellness.entities.Ride;
import com.ncsu.edu.spinningwellness.entities.User;
import com.ncsu.edu.spinningwellness.managers.RidesManager;
import com.ncsu.edu.spinningwellness.managers.UsersManager;
import com.ncsu.edu.spinningwellness.tabpanel.MenuConstants;
import com.ncsu.edu.spinningwellness.tabpanel.MyTabHostProvider;
import com.ncsu.edu.spinningwellness.tabpanel.TabView;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class CreateRideActivity extends BaseActivity {

	LinearLayout progressBar;
	TextView textViewCreateError;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Draw menu
		tabProvider = new MyTabHostProvider(CreateRideActivity.this);
		TabView tabView = tabProvider.getTabHost(MenuConstants.CREATE_RIDE);
		tabView.setCurrentView(R.layout.create_new_ride_activity);
		setContentView(tabView.render());			

		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		addListenerForCreateRideButton();

		progressBar = (LinearLayout) findViewById(R.id.createRideSpinner);
		progressBar.setVisibility(View.INVISIBLE);

		textViewCreateError = (TextView) findViewById(R.id.txtViewCreateError);
		textViewCreateError.setVisibility(View.INVISIBLE);

		setAsteriskLabels();
	}

	@Override
	protected void setTitle() {
		final TextView myTitleText = (TextView)findViewById(R.id.myTitle);
		myTitleText.setText("Create Ride");		
	}

	private void addListenerForCreateRideButton() {

		//Listener for create ride button
		Button button  = (Button) findViewById(R.id.btnCreateRide);
		button.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				validateUserInputAndCallAsyncTask();
			}
		});
	}

	private void validateUserInputAndCallAsyncTask() {

		boolean isValid = true;


		//Add validation code here
		String rideName = ((TextView) findViewById(R.id.textViewCreateRideRideName)).getText().toString().trim();
		String source = ((TextView) findViewById(R.id.textViewCreateRideSource)).getText().toString().trim();
		String destination = ((TextView) findViewById(R.id.textViewCreateRideDestination)).getText().toString().trim();

		//		if(rideName.equals(""))
		//			missing= missing + "Ride Name, ";
		//		if(source.equals(""))
		//			missing= missing + "Source, ";
		//		if(destination.equals(""))
		//			missing= missing + "Destination! ";
		//		
		if(rideName.equals("") || source.equals("")||destination.equals("")){
			
			//Show the error message to user

			//			textViewCreateError.append("\nEnter "+missing+"\n");
			isValid = false;
			textViewCreateError.setVisibility(View.VISIBLE);
			Context context = getApplicationContext();
			CharSequence text = "Enter all fields!";
			Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
			TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
			v.setTextColor(getResources().getColor(R.color.red));
			toast.show();
		} else if(dateInPast()){
			isValid = false;
			textViewCreateError.setVisibility(View.VISIBLE);
			Context context = getApplicationContext();
			CharSequence text = "Start date and time is past!";
			Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
			TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
			v.setTextColor(getResources().getColor(R.color.red));
			toast.show();
		}
		
		if(isValid) {
			progressBar.setVisibility(View.VISIBLE);
			LinearLayout createRideForm = (LinearLayout) findViewById(R.id.createRideForm);
			createRideForm.setVisibility(View.INVISIBLE);

			new CreateRideTask().execute();
		}
	}
	
	private boolean dateInPast(){
		int day = ((DatePicker) findViewById(R.id.createRideDatePicker)).getDayOfMonth();
		int month = ((DatePicker) findViewById(R.id.createRideDatePicker)).getMonth();
		int year = ((DatePicker) findViewById(R.id.createRideDatePicker)).getYear();

		int hour = ((TimePicker) findViewById(R.id.createRideTimePicker)).getCurrentHour();
		int minute = ((TimePicker) findViewById(R.id.createRideTimePicker)).getCurrentMinute();

		Calendar calendar = Calendar.getInstance();
		System.out.println(day + " " + month + " " + year + " " + hour + " : " + minute);  //6 4 2013
		calendar.set(year, month, day, hour, minute);   
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDate = formatter.format(calendar.getTime());
		System.out.println("entered:" + formattedDate);
				
		Calendar now = Calendar.getInstance();
		formattedDate = formatter.format(now.getTime());
		System.out.println("now:" + formattedDate);
		
		if(calendar.before(now)){
			return true;
		}else{
			return false;
		}
	}

	private void moveToJoinRidesPage() {

		progressBar.setVisibility(View.INVISIBLE);

		LinearLayout createRideForm = (LinearLayout) findViewById(R.id.createRideForm);
		createRideForm.setVisibility(View.VISIBLE);

		//clear the text boxes
		TextView textViewRideName = (TextView) findViewById(R.id.textViewCreateRideRideName);
		textViewRideName.setText("");

		TextView textViewSource = (TextView) findViewById(R.id.textViewCreateRideSource);
		textViewSource.setText("");

		TextView textViewDestination = (TextView) findViewById(R.id.textViewCreateRideDestination);
		textViewDestination.setText("");

		//Date picker and time picker have to be reset

		Intent joinRidesIntent = new Intent(CreateRideActivity.this, JoinRidesActivity.class); 
		CreateRideActivity.this.startActivity(joinRidesIntent);
	}

	private class CreateRideTask extends AsyncTask<Void, Void, Ride> {

		Exception error;

		protected Ride doInBackground(Void... params) {

			String rideName = ((TextView) findViewById(R.id.textViewCreateRideRideName)).getText().toString();
			String source = ((TextView) findViewById(R.id.textViewCreateRideSource)).getText().toString();
			String destination = ((TextView) findViewById(R.id.textViewCreateRideDestination)).getText().toString();

			int day = ((DatePicker) findViewById(R.id.createRideDatePicker)).getDayOfMonth();
			int month = ((DatePicker) findViewById(R.id.createRideDatePicker)).getMonth();
			int year = ((DatePicker) findViewById(R.id.createRideDatePicker)).getYear();

			int hour = ((TimePicker) findViewById(R.id.createRideTimePicker)).getCurrentHour();
			int minute = ((TimePicker) findViewById(R.id.createRideTimePicker)).getCurrentMinute();

			Calendar calendar = Calendar.getInstance();
			calendar.set(year, month, day, hour, minute);
			Date startDate = calendar.getTime();
			Ride ride = null;
			String result = RidesManager.createRide(rideName, source, destination, startDate, username);
			if(!result.equalsIgnoreCase("success")) {
				textViewCreateError.setVisibility(View.VISIBLE);
				error = new Exception();
			}else{
				ride = new Ride("1",rideName, source, destination, startDate.getTime(), username);
			}

			return ride;
		}

		protected void onPostExecute(Ride result) {

			//Redirect to join rides page
			if(error == null) {
				new GetAllUsersTask(result).execute();
				moveToJoinRidesPage();
			} else {
				//Display the error to user
				textViewCreateError.setVisibility(View.VISIBLE);
			}
		}
	}

	private class GetAllUsersTask extends AsyncTask<Void, Void, List<User>> {

		Exception error;
		Ride r;
		//		List<User> list = new ArrayList<User>();

		GetAllUsersTask(Ride ride){
			r = ride;
		}

		protected List<User> doInBackground(Void... params) {
			//TODO: get from the webservice 
			List<User> list = UsersManager.getAllUsers();
			return list;
		}

		protected void onPostExecute(List<User> result) {
			//Redirect to join rides page
			if(error == null) {
				new SendEmailTask(r,result).execute();
				//fidn this user.... 
				for(User u:result){
					if(u.getName().equalsIgnoreCase(username)){
						new AddToCalendarTask(r,u).execute();
					}
				}

			}
		}
	}

	private class SendEmailTask extends AsyncTask<Void, Void, Void> {
		Exception error;
		Ride r;
		List<User> list;


		SendEmailTask(Ride ride,List<User> list){
			r = ride;
			this.list = list;
		}

		protected Void doInBackground(Void... params) {

			//TODO: get from the webservice 
			new EmailDispatcher().sendEmailToAll(list, r);
			return null;
			//send email

		}

		protected void onPostExecute(Ride result) {
		}
	}

	private class AddToCalendarTask extends AsyncTask<Void, Void, Void> {
		User user;
		Ride ride;
		AddToCalendarTask(Ride ride, User user){
			this.user = user;
			this.ride = ride;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			try{
				EventsCalendar.pushAppointmentsToCalender(CreateRideActivity.this,ride, 0, true, true,user.getName(),user.getEmail());
				//				Toast.makeText(getApplicationContext(), "Event added to Calendar.", Toast.LENGTH_SHORT).show();
			}catch(Exception e){
				//				Toast.makeText(getApplicationContext(), "Please configure your Calendar to get ride notifications.", Toast.LENGTH_SHORT).show();
			}
			return null;
		}

		protected void onPostExecute(Void result) {
		}

	}

	private void setAsteriskLabels() {

		TextView textViewCreateRideNameLabel = (TextView) findViewById(R.id.textViewCreateRideNameLabel);
		textViewCreateRideNameLabel.setText(
				UIUtils.buildSpannableStringWithAsterisk(getResources().getString(R.string.lbl_ride_name))
				);

		TextView textViewCreateSourceLabel = (TextView) findViewById(R.id.textViewCreateSourceLabel);
		textViewCreateSourceLabel.setText(
				UIUtils.buildSpannableStringWithAsterisk(getResources().getString(R.string.lbl_source))
				);

		TextView textViewCreateDestinationLabel = (TextView) findViewById(R.id.textViewCreateDestinationLabel);
		textViewCreateDestinationLabel.setText(
				UIUtils.buildSpannableStringWithAsterisk(getResources().getString(R.string.lbl_destination))
				);

		TextView textViewCreateDateOfRideLabel = (TextView) findViewById(R.id.textViewCreateDateOfRideLabel);
		textViewCreateDateOfRideLabel.setText(
				UIUtils.buildSpannableStringWithAsterisk(getResources().getString(R.string.lbl_date_of_ride))
				);

		TextView textViewCreateStartTimeOfRideLabel = (TextView) findViewById(R.id.textViewCreateStartTimeOfRideLabel);
		textViewCreateStartTimeOfRideLabel.setText(
				UIUtils.buildSpannableStringWithAsterisk(getResources().getString(R.string.lbl_start_time_of_ride))
				);

	}
}