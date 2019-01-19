package com.typicalgeek.therapy;

import android.animation.AnimatorInflater;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements RefreshInterface{
    DatabaseHelper databaseHelper = new DatabaseHelper(this);
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    TextView tvDateToday;
    AppBarLayout appBar;
    Button btnToday;
    private static final int SELF = 0, BOT = 1;

    public MainActivity() throws ParseException {
    }

    static int getSelf() { return SELF; }
    static int getBot() { return BOT; }
    String [] greetingMessage = {"Hi", "Hello", "Hey", "Hey", getTimeBasedGreetingMessage()},
            greetingFollowUp = {"What's up?", "How are you today?", "How are you doing?", "...", getTimeBasedGreetingFollowUp()},
            listeningMessage = {"Okay...", "Go on...", "I'm listening", "..."},
            byeMessage = {"Okay, bye", "Okay, later", "Later then", "I'll be here", getTimeBasedByeMessage()};
    private String MSG;

    Intent intent;

    private String getTimeBasedGreetingMessage() throws ParseException {
        Date time = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse(getDate(0));
        Date midnight = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse("00:00");
        Date noon = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse("12:00");
        Date evening = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse("16:00");
        String msg;
        if(time.after(midnight)&&time.before(noon)) msg = "Good morning";
        else if(time.after(noon)&&time.before(evening)) msg = "Good afternoon";
        else msg = "Good evening";
        return msg;
    }

    private String getTimeBasedGreetingFollowUp() throws ParseException {
        Date time = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse(getDate(0));
        Date midnight = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse("00:00");
        Date noon = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse("12:00");
        Date evening = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse("16:00");
        String msg;
        if(time.after(midnight)&&time.before(noon)) msg = "How's your morning?";
        else if(time.after(noon)&&time.before(evening)) msg = "Having a good day so far?";
        else msg = "Enjoying your evening?";
        return msg;
    }

    private String getTimeBasedByeMessage() throws ParseException {
        Date time = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse(getDate(0));
        Date midnight = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse("00:00");
        Date noon = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse("12:00");
        Date evening = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse("16:00");
        Date night = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse("20:00");
        String msg;
        if(time.after(midnight)&&time.before(noon)) msg = "Have a nice day";
        else if(time.after(noon)&&time.before(evening)) msg = "Enjoy your afternoon";
        else if(time.after(evening)&&time.before(night)) msg = "Enjoy your evening";
        else msg = "Good night";
        return msg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        appBar = findViewById(R.id.appBar);
        if (databaseHelper.count(getDate(1)) <= 0){
            MSG = greetingMessage[new Random().nextInt(greetingMessage.length)];
            databaseHelper.insertMessage(buildMessage(MSG, BOT));
        }
        final ImageButton btnSend = findViewById(R.id.btnSend);
        final EditText etMessage = findViewById(R.id.etMessage);
        btnToday = findViewById(R.id.btnToday);
        recyclerView = findViewById(R.id.rvChats);
        layoutManager = new LinearLayoutManager(this);
        tvDateToday = findViewById(R.id.tvDateToday);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dbInsert(buildMessage(etMessage.getText().toString().trim(), SELF))) {
                    etMessage.setText("");
                } else {
                    Toast.makeText(MainActivity.this, "Therapist is offline", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) btnSend.setVisibility(View.GONE);
                else btnSend.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        tvDateToday.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(getDate(1))){
                    findViewById(R.id.layout_etMessage).setVisibility(View.GONE);
                    btnSend.setVisibility(View.GONE);
                    btnToday.setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.layout_etMessage).setVisibility(View.VISIBLE);
                    if (!etMessage.getText().toString().trim().isEmpty()) btnSend.setVisibility(View.VISIBLE);
                    else btnSend.setVisibility(View.GONE);
                    btnToday.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (layoutManager.findFirstCompletelyVisibleItemPosition() != 0){
                    appBar.setStateListAnimator(AnimatorInflater
                            .loadStateListAnimator(getApplicationContext(), R.animator.appbar_elevator));
                } else {
                    appBar.setStateListAnimator(AnimatorInflater
                            .loadStateListAnimator(getApplicationContext(), R.animator.appbar_deelevator));
                }
            }
        });
        refresh();
    }

    public void refresh() {
        refresh(getDate(1));
    }

    public void refresh(String date){
        tvDateToday.setText(date);
        recyclerView.setHasFixedSize(true);
        ChatAdapter adapter = new ChatAdapter(this, dbGetItems(date));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
        if (adapter.getItemCount() > 0) recyclerView
                .smoothScrollToPosition(adapter.getItemCount() - 1);
    }

    private Message buildMessage(String content, int sender) {
        return new Message(content, sender, getDate(0), getDate(1));
    }

    private boolean dbInsert(Message message) {
        boolean success = databaseHelper.insertMessage(message);
        String date = getDate(1);
        final String dateRegex = "\\d{2}/\\d{2}/\\d{4}";
        String s = message.getMessageContent().trim().toLowerCase();
        if (s.endsWith("should i?")){
            MSG = new Random().nextBoolean()?"Yes":"No";
        } else if (databaseHelper.count(getDate(1)) < 3) {
            MSG = greetingFollowUp[new Random().nextInt(greetingFollowUp.length)];
        } else if (s.startsWith("show me")){
            if (date != null && date.matches(dateRegex)) {
                if (databaseHelper.count(date) > 0) {
                    MSG = String.format(Locale.getDefault(), getString(R.string.showing), date);
                } else {
                    MSG = String.format(Locale.getDefault(), getString(R.string.no_conversations_found), date);
                }
            }
        } else if (s.equals("play tetris")){
            final String packageName = "com.oggtechnologies.oskar.impossibletetris";
            intent = getPackageManager().getLaunchIntentForPackage(packageName);
            if(intent == null){
                MSG = "Tetris is not installed on your system";
                new AlertDialog.Builder(this)
                        .setTitle("Get Tetris")
                        .setMessage("You will be redirected to the play store.")
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("market://details?id="+packageName));
                            }
                        })
                        .setNeutralButton("Cancel", null)
                        .create().show();
            } else MSG = "Launching Tetris";
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (s.endsWith("that's it for now") || s.endsWith("that's it for today")) {
            MSG = byeMessage[new Random().nextInt(byeMessage.length)];
            databaseHelper.insertMessage(buildMessage(MSG, BOT));
        } else if (s.equals("contact ice")) {
            // TODO: 05/10/2018 Contact ICE
        } else {
            MSG = listeningMessage[new Random().nextInt(listeningMessage.length)];
        }
        databaseHelper.insertMessage(buildMessage(MSG, BOT));
        if (databaseHelper.count(date)>0) refresh(date); else refresh(getDate(1));
        return success;
    }

    private Message[] dbGetItems(String date) {
        Cursor res = databaseHelper.getAllData(date);
        Message[] messages = new Message[res.getCount()];
        if (res.getCount() > 0) {
            int i = 0;
            while (res.moveToNext()) {
                messages[i] = new Message(
                        res.getInt(0),
                        res.getString(1),
                        res.getInt(2),
                        res.getString(3),
                        res.getString(4));
                i++;
            }
        }
        return messages;
    }

    String getDate(int format){
        switch (format){
            case 0:
                return new SimpleDateFormat("HH:mm",
                        Locale.getDefault()).format(new Date());
            case 1:
                return new SimpleDateFormat("dd/MM/yyyy",
                        Locale.getDefault()).format(new Date());
            default:
                Toast.makeText(this, "Error in getDate()", Toast.LENGTH_SHORT).show();
                return null;
        }
    }
}

interface RefreshInterface{
    void refresh(String date);
}

class Message{
    private int messageID;
    private String messageContent;
    private int messageSender;
    private String messageTime;
    private String messageDate;

    Message(int messageID, String messageContent, int messageSender, String messageTime, String messageDate) {
        this.messageID = messageID;
        this.messageContent = messageContent;
        this.messageSender = messageSender;
        this.messageTime = messageTime;
        this.messageDate = messageDate;
    }

    Message(String messageContent, int messageSender, String messageTime, String messageDate) {
        this.messageContent = messageContent;
        this.messageSender = messageSender;
        this.messageTime = messageTime;
        this.messageDate = messageDate;
    }

    public int getMessageID() {
        return messageID;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public int getMessageSender() {
        return messageSender;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public String getMessageDate() {
        return messageDate;
    }
}

class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder>{
    private RefreshInterface mListener;
    private Message[] mMessages;
    private final static int SELF = MainActivity.getSelf(), BOT = MainActivity.getBot();
    ChatAdapter(RefreshInterface refreshInterface, Message[] messages){
        mListener = refreshInterface;
        mMessages = messages;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView tvMessage, tvDate;
        CardView cardView;
        LinearLayout layoutItemView;
        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvDate = itemView.findViewById(R.id.tvDate);
            layoutItemView = itemView.findViewById(R.id.layoutItemView);
            cardView.setOnClickListener(this);
            cardView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (tvDate.getVisibility() == View.VISIBLE){
                tvDate.setVisibility(View.GONE);
                cardView.setCardElevation(0.0f);
            } else {
                tvDate.setVisibility(View.VISIBLE);
                cardView.setCardElevation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                        v.getContext().getResources().getDisplayMetrics()));
            }
        }

        @Override
        public boolean onLongClick(final View v) {
            final int pos = getAdapterPosition();
            final PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.context_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.ctxt_copy:
                            ClipboardManager clipboardManager = (ClipboardManager)
                                    v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            String clipTxt = mMessages[pos].getMessageContent();
                            ClipData clipData = ClipData.newPlainText("copypasta", clipTxt);
                            assert clipboardManager != null;
                            clipboardManager.setPrimaryClip(clipData);
                            Toast.makeText(v.getContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.ctxt_delete:
                            new DatabaseHelper(v.getContext())
                                    .deleteMessage(mMessages[pos].getMessageID());
                            try {
                                mListener.refresh(new MainActivity().getDate(1));
                            } catch (ParseException e) {
                                Toast.makeText(v.getContext(), "Error in onLongClick():\n" + e.toString(), Toast.LENGTH_SHORT).show();
                            }
                    }
                    return true;
                }
            });
            popupMenu.show();
            return true;
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.layout_chat_item, viewGroup, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {
        Message m = mMessages[i];
        final int dp = 32;
        int margin = (int) (dp * (holder.itemView.getContext().getResources().getDisplayMetrics().density));
        final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)
                holder.layoutItemView.getLayoutParams();
        holder.tvMessage.setText(m.getMessageContent());
        holder.tvDate.setText(m.getMessageTime());
        if (m.getMessageSender() == SELF){
            holder.cardView.setCardBackgroundColor(Color.DKGRAY);
            holder.tvMessage.setTextColor(Color.LTGRAY);
            params.gravity = GravityCompat.END;
            params.setMarginStart(margin);
            params.setMarginEnd(0);
        } else {
            holder.cardView.setCardBackgroundColor(Color.LTGRAY);
            holder.tvMessage.setTextColor(Color.DKGRAY);
            params.gravity = GravityCompat.START;
            params.setMarginStart(0);
            params.setMarginEnd(margin);
        }
        holder.layoutItemView.setLayoutParams(params);
        holder.tvDate.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return mMessages.length;
    }
}

class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "MessagesDB";
    private static final int DATABASE_VERSION = 1;
    private static final String MESSAGES_TABLE_NAME = "messages_table";
    private static final String MESSAGES_COL_0 = "ID";
    private static final String MESSAGES_COL_1 = "CONTENT";
    private static final String MESSAGES_COL_2 = "SENDER";
    private static final String MESSAGES_COL_3 = "TIME";
    private static final String MESSAGES_COL_4 = "DATE";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + MESSAGES_TABLE_NAME + "(" + MESSAGES_COL_0 + " INTEGER PRIMARY KEY, "
                + MESSAGES_COL_1 + " TEXT, " + MESSAGES_COL_2 + " INTEGER, " + MESSAGES_COL_3 + " TEXT, " +
                MESSAGES_COL_4 + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        refreshDB();
    }

    boolean insertMessage(Message message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MESSAGES_COL_1, message.getMessageContent());
        contentValues.put(MESSAGES_COL_2, message.getMessageSender());
        contentValues.put(MESSAGES_COL_3, message.getMessageTime());
        contentValues.put(MESSAGES_COL_4, message.getMessageDate());
        long result = db.insert(MESSAGES_TABLE_NAME, null, contentValues);
        db.close();
        return result != -1;
    }

    int count(String date){
        return getAllData(date).getCount();
    }

    Cursor getAllData(String DATE) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + MESSAGES_TABLE_NAME + " WHERE " + MESSAGES_COL_4
                + " = \'" + DATE + "\'" , null);
    }

    void deleteMessage(int ID){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + MESSAGES_TABLE_NAME + " WHERE " + MESSAGES_COL_0 + " = " + ID);
    }

    private void refreshDB() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + MESSAGES_TABLE_NAME);
        onCreate(db);
    }

}


