package app.beedle.pocketreview.Activity;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jaredrummler.materialspinner.MaterialSpinner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import app.beedle.pocketreview.R;
import app.beedle.pocketreview.model.entity.NoteDatabase;
import app.beedle.pocketreview.model.entity.NoteEntity;

public class EditNoteActivity extends AppCompatActivity implements View.OnClickListener {


    private NoteDatabase noteDatabase;
    private RecyclerView.LayoutManager layoutManager;
    private static final int RESULT_UPDATE = 40;
    private String[] name = {"AUD", "BGN", "BRL", "CAD", "CHF", "CNY", "CZK", "DKK", "GBP", "HKD", "HRK", "HUF", "IDR", "ILS", "INR", "JPY", "KRW", "MXN", "MYR", "NOK", "NZD", "PHP", "PLN", "RON", "RUB", "SEK", "SGD", "THB", "TRY", "USD", "ZAR"};
    private boolean statusErr = false;
    private List<NoteEntity> noteEntityList;
    private NoteEntity noteEntity;
    private Intent intent;
    private ImageButton deleteNote, ratingStar;
    private Button doneBtn, shareBtn;
    private TextView totalPrice, tvCurrencyName;
    private EditText titleName, description, detail, value;
    private float amount = 0;
    private int rating;
    private String nCurrency, tempDetail = "", tempName = "", tempDescription = "", tempPrice = "";
    private Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        bindingID();
        setListenerDelete();
        setTempDetailEntity();
        setSpinnerItem();
        setDetail();
    }


    public Bitmap createBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        view.draw(c);
        return bitmap;
    }

    public void saveBitmap(Bitmap bitmap) {
        // save bitmap to cache directory
        try {
            //Context correct?
            File cachePath = new File(getApplicationContext().getCacheDir(), "images");
            cachePath.mkdirs(); // don't forget to make the directory
            FileOutputStream stream = new FileOutputStream(cachePath + "/image.png"); // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void shareNote() {
        bitmap = createBitmapFromView(this.getWindow().getDecorView());
        saveBitmap(bitmap);
        File imagePath = new File(getCacheDir(), "images");
        File newFile = new File(imagePath, "image.png");
        Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "app.beedle.pocketreview.fileprovider", newFile); //get path from package internal
        if (contentUri != null) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
            intent.putExtra(Intent.EXTRA_STREAM, contentUri);
            startActivity(Intent.createChooser(intent, "Share to app"));
        }
    }

    private void bindingID() {
        //Layout Inflate
        layoutManager = new LinearLayoutManager(this);
        titleName = findViewById(R.id.titleNameEditNote);
        description = findViewById(R.id.tvTripDesctiptionEditNote);
        detail = findViewById(R.id.detailEditNote);
        value = findViewById(R.id.valueEditNote);
        totalPrice = findViewById(R.id.totalPriceEdit);
        deleteNote = findViewById(R.id.deleteNote);
        ratingStar = findViewById(R.id.ratingStar);
        tvCurrencyName = findViewById(R.id.tvCurrencyName);
        doneBtn = findViewById(R.id.doneBtnEditNote);
        shareBtn = findViewById(R.id.shareBtn);
    }

    private void setListenerDelete() {
        deleteNote.setOnClickListener(this);
    }

    private void setTempDetailEntity() {
        //Get DB
        noteDatabase = Room.databaseBuilder(this, NoteDatabase.class, "NOTE").build();
        //Initial Entity
        noteEntityList = new ArrayList<>();
        noteEntity = new NoteEntity();
        intent = getIntent();
        //Get intent from Item selected contain NoteEntity Object in it
        noteEntity = intent.getParcelableExtra("NoteInformation");//NoteEntity in this parcelable
        tempName = noteEntity.getName();
        tempDescription = noteEntity.getDesc();
        tempDetail = noteEntity.getDetail();
        tempPrice = noteEntity.getAmount();
        rating = noteEntity.getRating();
        nCurrency = noteEntity.getCurrency();
        tvCurrencyName.setText(noteEntity.getCurrency());
    }

    private void setSpinnerItem() {
        MaterialSpinner spinner = findViewById(R.id.spinner);
        spinner.setItems(name);
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                nCurrency = item;
                tvCurrencyName.setText(item);
                noteEntity.setCurrency(item);
                Snackbar.make(view, "Clicked " + item, Snackbar.LENGTH_SHORT).show();
            }
        });
        //End Spinner
    }

    private void setDetail() {
        //Set Text on screen
        String[] text = tempPrice.split("\n");
        amount = 0;
        try {
            if (text.length <= 1 && (text.length <= 0 || text[0] == "")) {
                noteEntity.setTotal((float) 0);
            } else {
                for (int i = 0; i < text.length; i++) {
                    amount += Float.parseFloat(text[i]);
                }
                noteEntity.setTotal(amount);

            }
        } catch (NumberFormatException e) {
            noteEntity.setAmount("0");
            statusErr = true;
            e.printStackTrace();
        }
        String nCurrency = noteEntity.getCurrency();
        titleName.setText(tempName);
        description.setText(tempDescription);
        detail.setText(tempDetail);
        value.setText(tempPrice);
        amount = noteEntity.getTotal();
        totalPrice.setText("TOTAL: " + amount + " " + nCurrency); //Can add Currency here
        setStar();

    }

    private void addTotalprice(NoteEntity noteEntity) {
        String[] text = noteEntity.getAmount().split("\n");
        float amount = 0;
        try {
            if (text.length <= 1 && text[0] == "") {
                noteEntity.setTotal((float) 0);
            } else {
                for (int i = 0; i < text.length; i++) {
                    amount += Float.parseFloat(text[i]);
                }
                tempPrice = noteEntity.getAmount();
                noteEntity.setTotal(amount);
            }
        } catch (NumberFormatException e) {
            noteEntity.setAmount("0");
            statusErr = true;
            e.printStackTrace();
        }
    }

    private void setStar() {
        int star;
        star = rating;
        ratingStar.setBackgroundColor(Color.parseColor("#FFD700"));
        switch (star) {
            case 1:
                ratingStar.setImageResource(R.drawable.one_star);
                break;
            case 2:
                ratingStar.setImageResource(R.drawable.two_star);
                break;
            case 3:
                ratingStar.setImageResource(R.drawable.three_star);
                break;
            case 4:
                ratingStar.setImageResource(R.drawable.four_star);
                break;
            case 5:
                ratingStar.setImageResource(R.drawable.five_star);
                break;
            default:
                break;

        }
    }

    @SuppressLint("StaticFieldLeak")
    private void deleteNote() {
        new AsyncTask<Void, Void, List<NoteEntity>>() {
            @Override
            protected List<NoteEntity> doInBackground(Void... voids) {
                noteDatabase.noteRoomDAO().deleteNoteRecord(noteEntity);
                return null;
            }

            @Override
            protected void onPostExecute(List<NoteEntity> noteEntityList) {
                Intent intent = new Intent(EditNoteActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }.execute();

    }

    private void alertDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Confirm Delete Note");
        alertDialog.setMessage("Do you want to delete this note?").setCancelable(true).setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteNote();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
            }
        });
        alertDialog.create();
        alertDialog.show();
    }

    @SuppressLint("StaticFieldLeak")
    private void UpdateNote(final NoteEntity noteEntity) {


        new AsyncTask<Void, Void, NoteEntity>() {
            @Override
            protected NoteEntity doInBackground(Void... voids) {
                noteDatabase.noteRoomDAO().update(noteEntity);
                return null;
            }

            @Override
            protected void onPostExecute(NoteEntity noteEntity) {
                Toast.makeText(EditNoteActivity.this, "Note has been Updated", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(EditNoteActivity.this, PocketNoteActivity.class);
                startActivityForResult(intent, RESULT_UPDATE);
            }
        }.execute();
    }

    private NoteEntity saveNoteRecord() {
        noteEntity.setName(titleName.getText().toString());
        noteEntity.setDesc(description.getText().toString());
        noteEntity.setDetail(detail.getText().toString());
        noteEntity.setAmount(value.getText().toString());
        addTotalprice(noteEntity);
        noteEntity.setTotal(noteEntity.getTotal()); // Set total price
        noteEntity.setRating(rating); //Set rating star
        noteEntity.setCurrency(nCurrency);
        return noteEntity;
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.deleteNote) {
            alertDialog();
        }
    }

    public void updateNote(View view) {
        NoteEntity noteEntity = saveNoteRecord();
        if (statusErr == true) {
            statusErr = false;
            Toast.makeText(this, "Wrong number format, Please fill correct number", Toast.LENGTH_LONG).show();
        } else {
            UpdateNote(noteEntity);
        }
    }

    public void setRatingStar(View view) {
        ratingDialog();
    }

    private void ratingDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        final RatingBar ratingBar = new RatingBar(this);
        ratingBar.setNumStars(5);
        ratingBar.setMax(5);
        ratingBar.setRating(0);

        alertDialog.setIcon(android.R.drawable.btn_star_big_on);
        alertDialog.setTitle("Rating Trip!");
        alertDialog.setView(ratingBar);
        alertDialog.setCancelable(true).setNeutralButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ratingBar.setNumStars(5);
                int star = 0;
                star = ratingBar.getProgress();
                ratingStar.setBackgroundColor(Color.parseColor("#FFD700"));
                switch (star) {
                    case 1:
                        ratingStar.setImageResource(R.drawable.one_star);
                        break;
                    case 2:
                        ratingStar.setImageResource(R.drawable.two_star);
                        break;
                    case 3:
                        ratingStar.setImageResource(R.drawable.three_star);
                        break;
                    case 4:
                        ratingStar.setImageResource(R.drawable.four_star);
                        break;
                    case 5:
                        ratingStar.setImageResource(R.drawable.five_star);
                        break;
                    default:
                        break;

                }
                rating = star;
                dialog.dismiss();

            }

        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.create();
        alertDialog.show().getWindow().setLayout(700, 700);
    }


    public void shareClick(View view) {
        shareNote();
    }
}
