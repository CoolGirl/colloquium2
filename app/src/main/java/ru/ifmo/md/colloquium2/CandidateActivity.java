package ru.ifmo.md.colloquium2;

/**
 * Created by Евгения on 10.11.2014.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CandidateActivity extends ActionBarActivity implements View.OnClickListener {
    EditText edtSubjectName;
    Button btnAddSubject;

    long id = -1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_subject_activity_layout);
        edtSubjectName = (EditText)findViewById(R.id.edtSubjectName);
        btnAddSubject = (Button)findViewById(R.id.btnSaveCandidate);
        btnAddSubject.setOnClickListener(this);

        id = getIntent().getLongExtra(MainActivity.EXTRA_CANDIDATE_ID, -1);
        if (id != -1) {
            edtSubjectName.setText(getIntent().getStringExtra(EXTRA_SUBJECT_NAME));
            setTitle(getString(R.string.edtCandidate));
            edtSubjectName.setText(getIntent().getStringExtra(EXTRA_SUBJECT_NAME));
            edtSubjectName.setSelection(edtSubjectName.length());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (id != -1) {
            getMenuInflater().inflate(R.menu.menu_delete, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    public static final String EXTRA_DELETE = "delete";

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.mnuDelete) {
            Intent result = new Intent();
            result.putExtra(EXTRA_DELETE, true);
            result.putExtra(MainActivity.EXTRA_CANDIDATE_ID, id);
            setResult(RESULT_OK, result);
            finish();
        }
        return true;
    }

    public static final String EXTRA_SUBJECT_NAME = "subject name";

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSaveCandidate) {
            Intent result = new Intent();
            result.putExtra(EXTRA_SUBJECT_NAME, edtSubjectName.getText().toString());
            result.putExtra(MainActivity.EXTRA_CANDIDATE_ID, id);
            setResult(RESULT_OK, result);
            finish();
        }
    }
}