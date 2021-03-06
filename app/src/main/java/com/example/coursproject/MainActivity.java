package com.example.coursproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SearchView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    FloatingActionButton btnAdd;
    SearchView searchView;
    DBHelper dbHelper;
    SQLiteDatabase database;
    NoteAdapter adapter;

    View item;

    @Override
    @SuppressLint({"NewApi", "SetTextI18n"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        String queryload = "SELECT " + DBHelper.KEY_ID + ", "
                + DBHelper.KEY_TITLE + ", " + DBHelper.KEY_NOTE + ", " + DBHelper.KEY_CREATED_AT + " FROM " + DBHelper.TABLE_NOTES;
        @SuppressLint("Recycle")
        Cursor cursor = database.rawQuery(queryload, null);
        CreateRecycleView(cursor);




        item = findViewById(R.id.linearlayout);

        btnAdd = findViewById(R.id.addNoteButton);
        btnAdd.setOnClickListener(this);

        searchView = findViewById(R.id.searchSV);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                callSearch();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String queryfilter =
                        "SELECT "
                                + DBHelper.KEY_ID + ", "
                                + DBHelper.KEY_TITLE + ", "
                                + DBHelper.KEY_NOTE + ", "
                                + DBHelper.KEY_CREATED_AT
                                + " FROM "
                                + DBHelper.TABLE_NOTES
                                + " WHERE ((title like'%"
                                + newText + "%') OR (note like '%" + newText + "%'))";

                @SuppressLint("Recycle")
                Cursor cursor = database.rawQuery(queryfilter, null);
                CreateRecycleView(cursor);
                return true;
            }

            public void callSearch() {

            }

        });
    }



    public void onItemClick(View view, int position) {
        String queryedit =
                "SELECT "
                        + DBHelper.KEY_ID + ", "
                + DBHelper.KEY_TITLE
                        + ", " + DBHelper.KEY_NOTE
                        + ", " + DBHelper.KEY_CREATED_AT
                        + " FROM "
                        + DBHelper.TABLE_NOTES
                + " WHERE _id = "
                + adapter.getItem(position);
        @SuppressLint("Recycle")
        Cursor cursor = database.rawQuery(queryedit, null);
        if (cursor.moveToFirst()) {
            int editidIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
            int editcreated_atIndex = cursor.getColumnIndex(DBHelper.KEY_CREATED_AT);
            int editnoteIndex = cursor.getColumnIndex(DBHelper.KEY_NOTE);
            int edittitleIndex = cursor.getColumnIndex(DBHelper.KEY_TITLE);
            Log.d("mLog",
                    "Edit: ID = " + cursor.getInt(editidIndex)
                            + ", title = " + cursor.getString(edittitleIndex)
                            + ", note = " + cursor.getString(editnoteIndex)
                            + ", date = " + cursor.getString(editcreated_atIndex));
            Memory.EditID = cursor.getInt(editidIndex);
            Memory.EditTitle = cursor.getString(edittitleIndex);
            Memory.EditNote = cursor.getString(editnoteIndex);
            Memory.EditChek = true;
            Intent in = new Intent(MainActivity.this, NoteActivity.class);
            startActivityForResult(in, 200);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Memory.DeleteChek){
            String querydelete = "DELETE " + " FROM " + DBHelper.TABLE_NOTES + " WHERE " + DBHelper.KEY_ID + " = " + (Memory.EditID);
            database.execSQL(querydelete);
            try {
                String idGenerate = "UPDATE " + DBHelper.TABLE_NOTES + " SET  _id =  id - " + 1 + " WHERE _id " + " > " + Memory.EditID;
                @SuppressLint("Recycle") Cursor cursor = database.rawQuery(idGenerate, null);
                if (cursor.moveToFirst()) {
                    int editidIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
                    if (editidIndex != 0){
                        database.execSQL(idGenerate);
                    }
                }
            } catch (Exception exception){
                Log.d("mLog",exception.toString());
            }
            Memory.DeleteChek = false;
        }
        searchView.setQuery("", false);
        String queryload = "SELECT " + DBHelper.KEY_ID + ", "
                + DBHelper.KEY_TITLE + ", " + DBHelper.KEY_NOTE + ", " + DBHelper.KEY_CREATED_AT + " FROM " + DBHelper.TABLE_NOTES;
        @SuppressLint("Recycle")
        Cursor cursor = database.rawQuery(queryload, null);
        CreateRecycleView(cursor);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.addNoteButton) {


            Memory.EditChek = false;
            Memory.EditTitle = "";
            Memory.EditNote = "";
            Intent intent = new Intent(this, NoteActivity.class);
            startActivityForResult(intent, 200);
        }
    }

    public  void CreateRecycleView(Cursor cursor){

        List<Model> mModelList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
            int noteIndex = cursor.getColumnIndex(DBHelper.KEY_NOTE);
            int titleIndex = cursor.getColumnIndex(DBHelper.KEY_TITLE);
            int dateIndex = cursor.getColumnIndex(DBHelper.KEY_CREATED_AT);
            do {
                Log.d("mLog",
                        "ID = " + cursor.getInt(idIndex)
                                + ", title = " + cursor.getString(titleIndex)
                                + ", note = " + cursor.getString(noteIndex)
                                + ", date = " + cursor.getString(dateIndex));

                int IdI = cursor.getInt(idIndex);
                String TitleS;
                String NoteS;
                String DateS;
                char a;
                int checker = 0;
                for (int i = 0; i < cursor.getString(noteIndex).length(); i++){
                    a = cursor.getString(noteIndex).charAt(i);
                    if (a == '\n'){
                        checker=i;
                        break;
                    }
                }

                if (checker != 0)
                    NoteS = cursor.getString(noteIndex).substring(0, checker) + "...";
                else if (cursor.getString(noteIndex).length() > 25){
                    NoteS = cursor.getString(noteIndex).substring(0, 25) + "...";
                } else {
                    NoteS = cursor.getString(noteIndex);
                }

                DateS = cursor.getString(dateIndex);

                if (cursor.getString(titleIndex).length() > 25)
                    TitleS = cursor.getString(titleIndex).substring(0, 25) + "...";
                else
                    TitleS = cursor.getString(titleIndex);
                mModelList.add(new Model(TitleS,NoteS, DateS, IdI));
                Memory.EditID = cursor.getInt(idIndex);

            } while (cursor.moveToNext());
        }

        RecyclerView recyclerView = findViewById(R.id.LayoutForCard);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoteAdapter(mModelList, item );
        adapter.setClickListener(this::onItemClick);
        recyclerView.setAdapter(adapter);
    }


}

