package com.healthplus;

import java.util.ArrayList;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

public class MainActivityMap extends FragmentActivity implements LocationListener {

	private GoogleMap googleMap;
	private LocationManager locationManager;
	private Location loc;
	private Location currentLoc;
	private ArrayList<Marker> markerList = new ArrayList<Marker>();
	Criteria criteria;
	String provider;
	boolean checkinflag = false;
	ArrayList<LatLng> hospitalList = new ArrayList<LatLng>();
	ArrayList<LatLng> doctors = new ArrayList<LatLng>();
	ArrayList<LatLng> medshops = new ArrayList<LatLng>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_activity_map);
		initializeData();
		try {
			initializeMap();
			criteria = new Criteria();
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

			if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
					|| !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				// Build the alert dialog
				showLocationAlert();
			}

			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
			provider = locationManager.getBestProvider(criteria, false);
			currentLoc = locationManager.getLastKnownLocation(provider);
			googleMap.addMarker(
					new MarkerOptions().position(new LatLng(currentLoc.getLatitude(), currentLoc.getLongitude()))
					.title("My Current Position"));
			animateCameraLocation(currentLoc);
			ImageButton hospitals = (ImageButton) findViewById(R.id.hospital);
			hospitals.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					int check = getHospitalData();
					CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(hospitalList.get(check), 17.0f);
					googleMap.animateCamera(cameraUpdate);
				}
			});
			ImageButton doctor = (ImageButton) findViewById(R.id.doctors);
			doctor.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					int check = getDoctorData();
					CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(doctors.get(check), 17.0f);
					googleMap.animateCamera(cameraUpdate);
				}
			});
			ImageButton medshop = (ImageButton) findViewById(R.id.medshop);
			medshop.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					int check = getMedShopData();
					CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(medshops.get(check), 17.0f);
					googleMap.animateCamera(cameraUpdate);
				}
			});
			ImageButton ambulance = (ImageButton) findViewById(R.id.ambulance);
			ambulance.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					CallPeople();
				}
			});
			ImageButton person = (ImageButton) findViewById(R.id.person);
			person.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					CallPeople();
				}
			});
			googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

				@Override
				public void onInfoWindowClick(Marker arg0) {
					// TODO Auto-generated method stub
					if (arg0.getTitle().contains("GreenView")) {
						createHospitalInfo(arg0.getTitle());
					} else if (arg0.getTitle().contains("Dr.")) {
						createDoctorInfo(arg0.getTitle());
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void createHospitalInfo(String title) {
		// TODO Auto-generated method stub
		final Dialog dialog = new Dialog(MainActivityMap.this);
		dialog.setContentView(R.layout.hospitalinfo);
		dialog.setTitle(title);
		Button now = (Button) dialog.findViewById(R.id.now);
		now.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), "Appointment Fixed", Toast.LENGTH_LONG).show();
				String msg = "Your appointment has been fixed at priority level with Dr. Shashank Dhariwal at GreenView Medical Hospital.";
				sendSMS(msg);
				dialog.dismiss();
			}
		});
		Button later = (Button) dialog.findViewById(R.id.later);
		later.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showDateTimedialog();
				dialog.dismiss();
			}
		});
		// set the custom dialog components - text, image and button
		dialog.show();
	}

	protected void showDateTimedialog() {
		// TODO Auto-generated method stub
		final Dialog dialog1 = new Dialog(MainActivityMap.this);
		dialog1.setContentView(R.layout.datetimelayout);
		dialog1.setTitle("Set Date and Time");
		final DatePicker datepicker = (DatePicker) dialog1.findViewById(R.id.datePicker);
		final TimePicker timepicker = (TimePicker) dialog1.findViewById(R.id.timePicker);
		Button confirm = (Button) dialog1.findViewById(R.id.confirm);
		confirm.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String msg = "Appointment Fixed for " + (datepicker.getMonth() + 1) + "/" + datepicker.getDayOfMonth()
				+ "/" + datepicker.getYear() + " at " + timepicker.getCurrentHour() + ":"
				+ timepicker.getCurrentMinute() + "with Mr. Shashank Dhariwal at GreenView Medical Hospital";
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
				sendSMS(msg);
				dialog1.dismiss();
			}
		});
		dialog1.show();

	}

	protected void sendSMS(String msg) {

		try {
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage("0888409157", null, msg, null, null);
			Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
		}

		catch (Exception e) {
			Toast.makeText(getApplicationContext(), "SMS faild, please try again.", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}

	protected void createDoctorInfo(String title) {
		// TODO Auto-generated method stub
		final Dialog dialog = new Dialog(MainActivityMap.this);
		dialog.setContentView(R.layout.doctorinfo);
		dialog.setTitle(title);
		ImageButton phone = (ImageButton) dialog.findViewById(R.id.phone);
		phone.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				CallPeople();
			}

		});
		// set the custom dialog components - text, image and button
		dialog.show();
	}

	public void CallPeople() {
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		callIntent.setData(Uri.parse("tel:08884091157"));
		startActivity(callIntent);
	}

	protected double calculatedistance(double latitude, double longitude) {
		// TODO Auto-generated method stub
		Location selected_location = new Location("My Location");
		selected_location.setLatitude(currentLoc.getLatitude());
		selected_location.setLongitude(currentLoc.getLongitude());
		Location near_locations = new Location("Hospital");
		near_locations.setLatitude(latitude);
		near_locations.setLongitude(longitude);

		double distance = selected_location.distanceTo(near_locations);
		return distance;

	}

	protected int getHospitalData() {
		// TODO Auto-generated method stub
		double dis = 0;
		int marker = 0;
		removeMarker();
		for (int i = 0; i <= hospitalList.size() - 1; i++) {
			String titleHospital = null;
			switch (i) {
			case 0:
				titleHospital = "Sakra World Hospital";
				break;
			case 1:
				titleHospital = "St. Johns Hospital";
				break;
			case 2:
				titleHospital = "GreenView Hospital";
				break;
			}
			Marker locMarker = googleMap.addMarker(new MarkerOptions()
					.position(new LatLng(hospitalList.get(i).latitude, hospitalList.get(i).longitude))
					.title(titleHospital));
			markerList.add(locMarker);
			double disnew = calculatedistance(hospitalList.get(i).latitude, hospitalList.get(i).longitude);
			if (i == 0)
				dis = disnew;
			if (disnew < dis) {
				dis = disnew;
				marker = i;
			}

		}
		return marker;
	}

	private void removeMarker() {
		// TODO Auto-generated method stub
		for(Marker m : markerList)
		{
			m.remove();
		}
		markerList.clear();
	}

	protected int getDoctorData() {
		// TODO Auto-generated method stub
		double dis = 0;
		int marker = 0;
		removeMarker();
		for (int i = 0; i <= doctors.size() - 1; i++) {
			String titleHospital = null;
			switch (i) {
			case 0:
				titleHospital = "Dr. Shankar";
				break;
			case 1:
				titleHospital = "Dr. Vanita Mathew";
				break;
			case 2:
				titleHospital = "Dr. Nanda Rajaneesh";
				break;
			}
			Marker locMarker = googleMap.addMarker(new MarkerOptions()
					.position(new LatLng(doctors.get(i).latitude, doctors.get(i).longitude)).title(titleHospital));
			markerList.add(locMarker);
			double disnew = calculatedistance(doctors.get(i).latitude, doctors.get(i).longitude);
			if (i == 0)
				dis = disnew;
			if (disnew < dis) {
				dis = disnew;
				marker = i;
			}

		}
		return marker;
	}

	protected int getMedShopData() {
		// TODO Auto-generated method stub
		double dis = 0;
		int marker = 0;
		removeMarker();
		for (int i = 0; i <= medshops.size() - 1; i++) {
			String titleHospital = null;
			switch (i) {
			case 0:
				titleHospital = "Medplus";
				break;
			case 1:
				titleHospital = "Nideeshwaram Ayurvedic Bhavan";
				break;
			case 2:
				titleHospital = "Religare Chemist";
				break;
			}
			Marker locMarker = googleMap.addMarker(new MarkerOptions()
					.position(new LatLng(medshops.get(i).latitude, medshops.get(i).longitude)).title(titleHospital));
			markerList.add(locMarker);
			double disnew = calculatedistance(medshops.get(i).latitude, medshops.get(i).longitude);
			if (i == 0)
				dis = disnew;
			if (disnew < dis) {
				dis = disnew;
				marker = i;
			}

		}
		return marker;
	}

	private void initializeData() {
		// TODO Auto-generated method stub
		hospitalList.add(new LatLng(12.932094, 77.685176));
		hospitalList.add(new LatLng(12.928887, 77.613158));
		hospitalList.add(new LatLng(12.919143, 77.638172));
		doctors.add(new LatLng(12.946653, 77.622825));
		doctors.add(new LatLng(12.934609, 77.625030));
		doctors.add(new LatLng(12.933704, 77.621432));
		medshops.add(new LatLng(12.946517, 77.496417));
		medshops.add(new LatLng(15.317278, 75.713888));
		medshops.add(new LatLng(12.956632, 77.528984));

	}

	public void animateCameraLocation(Location loc) {
		LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17.0f);
		googleMap.animateCamera(cameraUpdate);
	}

	public void showLocationAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Location Services Not Active");
		builder.setMessage("Please enable Location Services and GPS");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				// Show location settings when the user
				// acknowledges the alert dialog
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
			}
		});
		Dialog alertDialog = builder.create();
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.show();
	}

	private void initializeMap() {
		// TODO Auto-generated method stub
		googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

		googleMap.setBuildingsEnabled(true);
		googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		googleMap.setMyLocationEnabled(true);
		googleMap.setTrafficEnabled(true);

	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		// animateCameraLocation(location);

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

}
