package com.example.navigtion_app.frams;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.navigtion_app.Adapters.BarberAdapter;
import com.example.navigtion_app.Adapters.DateAdapter;
import com.example.navigtion_app.Adapters.TimeAdapter;
import com.example.navigtion_app.R;
import com.example.navigtion_app.activity.MainActivity;
import com.example.navigtion_app.models.Appointment;
import com.example.navigtion_app.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class new_apointment extends Fragment {

    private RecyclerView dateRecyclerView;
    private RecyclerView timeRecyclerView;
    private DateAdapter dateAdapter;
    private TimeAdapter timeAdapter;
    private List<String> dateList;
    private List<String> timeList;
    private String selectedDate;
    private String selectedTime;
    private Button bookAppointmentButton;
    private RecyclerView barberRecyclerView;
    private BarberAdapter barberAdapter;
    private List<User> barberList;
    private DatabaseReference usersRef;
    private String selectedBarberId;
    private String hairType;
    private TextView nameText;
    private TextView typeText;
    private TextView phoneText;
    private TextView mailText;
    private TextView barberNameCircle;

    public new_apointment() {
        // Required empty public constructor
    }

    public static new_apointment newInstance(String param1, String param2) {
        new_apointment fragment = new new_apointment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments() != null) {
            hairType = getArguments().getString("hairType");
        }

        View view = inflater.inflate(R.layout.fragment_new_apointment, container, false);
        nameText = view.findViewById(R.id.name);
        typeText = view.findViewById(R.id.type);
        phoneText = view.findViewById(R.id.Phone);
        mailText = view.findViewById(R.id.Mail);
        barberNameCircle = view.findViewById(R.id.barberNameCircle);

        ImageView backBtn = view.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_new_apointment_to_gender);
            }
        });
        // Initialize RecyclerViews
        dateRecyclerView = view.findViewById(R.id.DateSelect);
        dateRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        timeRecyclerView = view.findViewById(R.id.TimeSelect);
        timeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Initialize lists
        dateList = new ArrayList<>();
        timeList = new ArrayList<>();

        // Set adapters with empty lists initially
        dateAdapter = new DateAdapter(dateList, date -> {
            selectedDate = date;
            selectedTime = null; // איפוס לוגי של בחירת השעה
            if(timeAdapter != null) {
                timeAdapter.resetSelection(); // איפוס עיצובי
            }
        });
        timeAdapter = new TimeAdapter(timeList, time -> selectedTime = time);

        dateRecyclerView.setAdapter(dateAdapter);
        timeRecyclerView.setAdapter(timeAdapter);

        // Book Appointment Button
        bookAppointmentButton = view.findViewById(R.id.button);
        bookAppointmentButton.setOnClickListener(v -> bookAppointment());

        barberRecyclerView = view.findViewById(R.id.barberList);
        barberRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Initialize Firebase Reference
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Fetch barbers with "Long Hair" or "Short Hair"
        loadBarbers();

        return view;
    }

    private void loadBarbers() {
        barberList = new ArrayList<>();

        usersRef.orderByChild("type").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                barberList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        if ("longHair".equals(hairType) && "Long Hair".equals(user.getType())) {
                            barberList.add(user);
                        } else if ("shortHair".equals(hairType) && "Short Hair".equals(user.getType())) {
                            barberList.add(user);
                        }
                    }
                }

                // Set adapter after fetching data
                barberAdapter = new BarberAdapter(barberList, barber -> {
                    selectedBarberId = barber.getId();
                    // Update the TextViews with the selected barber's data
                    nameText.setText(barber.getFullName());
                    typeText.setText(barber.getType());
                    phoneText.setText(barber.getPhone());
                    mailText.setText(barber.getEmail());
                    barberNameCircle.setText(barber.getFullName());
                    selectedBarberId = barber.getId();
                    if (selectedBarberId != null) {
                        Toast.makeText(getContext(), "Selected: " + barber.getFullName(), Toast.LENGTH_SHORT).show();
                        resetDateAndTimeSelection();
                    } else {
                        Toast.makeText(getContext(), "Failed to get Barber ID.", Toast.LENGTH_SHORT).show();
                    }
                });
                barberRecyclerView.setAdapter(barberAdapter);

                // אם הרשימה לא ריקה, בחר אוטומטית את הברבר הראשון
                if (!barberList.isEmpty()) {
                    selectedBarberId = barberList.get(0).getId();
                    nameText.setText(barberList.get(0).getFullName());
                    typeText.setText(barberList.get(0).getType());
                    phoneText.setText(barberList.get(0).getPhone());
                    mailText.setText(barberList.get(0).getEmail());
                    barberNameCircle.setText(barberList.get(0).getFullName());

                    Toast.makeText(getContext(), "Selected: " + barberList.get(0).getFullName(), Toast.LENGTH_SHORT).show();
                    barberAdapter.setSelectedPosition(0); // עדכון הבחירה העיצובית במתאם
                    resetDateAndTimeSelection();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load barbers.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetDateAndTimeSelection() {
        selectedDate = null;
        selectedTime = null;
        dateList.clear();
        timeList.clear();

        dateAdapter.resetSelection();
        timeAdapter.resetSelection();

        if (selectedBarberId != null) {
            dateList.addAll(getNextTwoWeeksDates());
            timeList.addAll(generateTimeSlots());
            dateAdapter.notifyDataSetChanged();
            timeAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getContext(), "Please select a barber first.", Toast.LENGTH_SHORT).show();
        }
    }


    private List<String> getNextTwoWeeksDates() {
        List<String> dates = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE\ndd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 14; i++) {
            dates.add(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return dates;
    }

    private List<String> generateTimeSlots() {
        List<String> times = new ArrayList<>();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, 17);
        endCalendar.set(Calendar.MINUTE, 0);

        while (calendar.before(endCalendar)) {
            times.add(timeFormat.format(calendar.getTime()));
            calendar.add(Calendar.MINUTE, 30);
        }
        return times;
    }

    private void bookAppointment() {
        if (getActivity() == null || !(getActivity() instanceof MainActivity)) {
            Toast.makeText(getContext(), "Error: Invalid activity context.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedBarberId == null) {
            Toast.makeText(getContext(), "Please select a barber first.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate == null || selectedTime == null) {
            Toast.makeText(getContext(), "Please select both date and time.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String clientId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (clientId != null) {
            ((MainActivity) getActivity()).AddAppointmentWithAutoID(clientId, selectedBarberId, selectedDate, selectedTime, success -> {
                if (success) {
                    // במקרה של הצלחה, ננווט לדף הראשי
                    Navigation.findNavController(getView()).navigate(R.id.action_new_apointment_to_fragment_main);
                }
            });
        } else {
            Toast.makeText(getContext(), "User not authenticated.", Toast.LENGTH_SHORT).show();
        }
    }


}
