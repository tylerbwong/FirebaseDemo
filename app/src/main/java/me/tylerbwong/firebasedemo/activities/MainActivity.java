package me.tylerbwong.firebasedemo.activities;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tylerbwong.firebasedemo.R;

/**
 * @author Tyler Wong
 */

public class MainActivity extends AppCompatActivity {
   @BindView(R.id.input_text)
   TextView mInputText;
   @BindView(R.id.text_feed)
   ListView mTextFeed;
   @BindView(R.id.image_view)
   ImageView mImageView;

   private DatabaseReference mDatabase;
   private FirebaseAnalytics mAnalytics;
   private StorageReference mImageStorage;
   private List<String> mMessages;
   private ArrayAdapter<String> mAdapter;
   private Uri mUri;

   private final static String MESSAGE_SENDER = "message_sender";
   private final static String STORAGE_URL = "gs://fir-demo-494f4.appspot.com";
   private final static String IMAGE_NAME = "sprites_1.png";
   private final static String[] PERMISSIONS = {"android.permission.READ_EXTERNAL_STORAGE",
         "android.permission.WRITE_EXTERNAL_STORAGE"};

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      ButterKnife.bind(this);

      mImageStorage = FirebaseStorage.getInstance().getReferenceFromUrl(STORAGE_URL);
      requestPermissions(PERMISSIONS, 200);

      mImageStorage.child("images").child(IMAGE_NAME).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
         @Override
         public void onSuccess(Uri uri) {
            mUri = uri;
         }
      });

      mAnalytics = FirebaseAnalytics.getInstance(this);
      logEntryAnalytics();

      mMessages = new ArrayList<>();
      mDatabase = FirebaseDatabase.getInstance().getReference();
      mDatabase.addValueEventListener(new ValueEventListener() {
         @Override
         public void onDataChange(DataSnapshot dataSnapshot) {
            Iterable<DataSnapshot> data = dataSnapshot.getChildren();
            mMessages.clear();

            for (DataSnapshot message : data) {
               mMessages.add(message.getValue().toString());
            }
            mAdapter.notifyDataSetChanged();
         }

         @Override
         public void onCancelled(DatabaseError databaseError) {

         }
      });

      mAdapter = new ArrayAdapter<>(this, R.layout.list_view_item, mMessages);
      mTextFeed.setAdapter(mAdapter);
   }

   @OnClick(R.id.submit_button)
   public void submitText(View view) {
      String message = mInputText.getText().toString();
      DatabaseReference messageReference = mDatabase.push();
      messageReference.setValue(message);
      mInputText.setText("");
      logMessageAnalytics();
   }

   @OnClick(R.id.crash_button)
   public void crashApp(View view) {
      FirebaseCrash.log("Crash button pressed.");
      throw new RuntimeException("Boom.");
   }

   @OnClick(R.id.picture_button)
   public void downloadPicture(View view) {
      String url = mUri.toString();

      Glide.with(this)
            .load(url)
            .override(300, 200)
            .into(mImageView);
   }

   @OnClick(R.id.upload_button)
   public void uploadPicture(View view) {
      File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
      Uri file = Uri.fromFile(new File(path, "250px-004Charmander.png"));
      StorageReference imagesRef = mImageStorage.child("images/" + file.getLastPathSegment());
      imagesRef.putFile(file);
      Toast.makeText(this, "Image Uploaded!", Toast.LENGTH_LONG).show();
   }

   private void logMessageAnalytics() {
      Bundle bundle = new Bundle();
      bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "text");
      mAnalytics.logEvent(FirebaseAnalytics.Event.ADD_TO_WISHLIST, bundle);
      mAnalytics.setUserProperty(MESSAGE_SENDER, "true");
   }

   private void logEntryAnalytics() {
      Bundle bundle = new Bundle();
      bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "state");
      mAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);
   }
}
