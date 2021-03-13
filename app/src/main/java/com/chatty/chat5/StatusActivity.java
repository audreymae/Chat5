package com.chatty.chat5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText mStatus;
    private Button mSaveBtn;

    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    //Progress
    //private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUID = mCurrentUser.getUid();

        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUID);

        mToolbar = findViewById(R.id.toolbar_status_activity);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get current status as a string
        String statusValue = getIntent().getStringExtra("status_value");

        mStatus = findViewById(R.id.status_text_input);
        mSaveBtn = findViewById(R.id.status_save_changes_btn);

        //Put current status in TextView
        mStatus.setText(statusValue);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Progress
                //mProgress = new ProgressDialog(getApplicationContext());
                //mProgress.setTitle("Saving");
                //mProgress.setMessage("Please wait...");
                //mProgress.show();

                String status = mStatus.getText().toString();
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(getApplicationContext(), "Good",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
//                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()){
//                            //mProgress.dismiss();
//                        } else {
//                            Toast.makeText(getApplicationContext(), "Error",Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
            }
        });


    }
}