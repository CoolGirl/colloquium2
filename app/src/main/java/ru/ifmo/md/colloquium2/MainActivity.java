package ru.ifmo.md.colloquium2;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;


public class MainActivity extends ActionBarActivity {

    private ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = (ListView) findViewById(R.id.list);
        setCurrState(State.BEFORE_VOTING);
        updateList();
        invalidateOptionsMenu();
    }

    enum State {BEFORE_VOTING, VOTING, AFTER_VOTING}

    State currState = State.BEFORE_VOTING;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item;
        item = menu.findItem(R.id.miAdd);
        item.setVisible(currState.equals(State.BEFORE_VOTING));
        item = menu.findItem(R.id.miStartVoting);
        item.setVisible(currState.equals(State.BEFORE_VOTING));

        item = menu.findItem(R.id.miEndVoting);
        item.setVisible(currState.equals(State.VOTING));

        item = menu.findItem(R.id.miReset);
        item.setVisible(currState.equals(State.AFTER_VOTING));

        return super.onPrepareOptionsMenu(menu);
    }

    private void setCurrState(State state) {
        currState = state;
        invalidateOptionsMenu();
        updateList();
    }

    private void startEditCandidate(long id) {
        Intent i = new Intent(this, CandidateActivity.class);
        if (id != -1)
            i.putExtra(EXTRA_CANDIDATE_ID, id);
        startActivityForResult(i, id == -1 ? REQUEST_ADD_CANDIDATE : REQUEST_EDIT_CANDIDATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;
        String newName = data.getStringExtra(CandidateActivity.EXTRA_SUBJECT_NAME);
        VotesDBAdapter db = new VotesDBAdapter(this).open();
        if (newName == null) return;
        if (requestCode == REQUEST_ADD_CANDIDATE) {
            db.createCandidate(newName);
        } else if (requestCode == REQUEST_EDIT_CANDIDATE) {
            long id = data.getLongExtra(EXTRA_CANDIDATE_ID, -1);
            if (id != -1)
                if (data.hasExtra(CandidateActivity.EXTRA_DELETE))
                    db.deleteCandidate(id);
                else
                    db.updateCandidate(id, newName);
        }
        updateList();
    }

    public static final int REQUEST_ADD_CANDIDATE = 7;
    public static final int REQUEST_EDIT_CANDIDATE = 8;

    public static final String EXTRA_CANDIDATE_ID = "c_id";

    private void updateList() {
        VotesDBAdapter db = new VotesDBAdapter(this).open();

        ListAdapter newAdapter = null;
        if (currState.equals(State.BEFORE_VOTING) || currState.equals(State.VOTING)) {
            newAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, db.fetchCandidates(false),
                    new String[]{VotesDBAdapter.KEY_CANDIDATE}, new int[]{android.R.id.text1}, 0);
        } else if (currState.equals(State.AFTER_VOTING)) {
            final int totalVotes = db.totalVotesCount();
            newAdapter = new SimpleCursorAdapter(this, R.layout.lst_left_right_item, db.fetchCandidates(true),
                    new String[]{VotesDBAdapter.KEY_CANDIDATE, VotesDBAdapter.KEY_VOTES}, new int[]{android.R.id.text1,
                    android.R.id.text2}, 0) {
                @Override
                public void bindView(View view, Context context, Cursor cursor) {
                    if (cursor.getPosition() == 0) {
                        view.setBackgroundColor(Color.rgb(255, 180, 180));
                    } else
                        view.setBackgroundColor(Color.rgb(255, 255, 255));
                    super.bindView(view, context, cursor);
                    TextView v = ((TextView) view.findViewById(android.R.id.text2));
                    v.append(" (" + cursor.getInt(cursor.getColumnIndex(VotesDBAdapter.KEY_VOTES))*100 / totalVotes + "%)");
                }
            } ;

        }
        list.setAdapter(newAdapter);

        if (currState.equals(State.BEFORE_VOTING)) {
            list.setOnItemClickListener(null);
            list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    startEditCandidate(l);
                    return true;
                }
            });
        } else if (currState.equals(State.VOTING)) {
            list.setOnLongClickListener(null);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    VotesDBAdapter db = new VotesDBAdapter(MainActivity.this).open();
                    db.addVote(l);
                }
            });
        } else if (currState.equals(State.AFTER_VOTING)) {
            list.setOnItemClickListener(null);
            list.setOnItemLongClickListener(null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.miAdd) {
            startEditCandidate(-1);
        } else if (id == R.id.miStartVoting) {
            setCurrState(State.VOTING);
        } else if (id == R.id.miEndVoting) {
            setCurrState(State.AFTER_VOTING);
        } else if (id == R.id.miReset) {
            VotesDBAdapter db = new VotesDBAdapter(this).open();
            db.resetVotes();
            setCurrState(State.BEFORE_VOTING);
        }

        return super.onOptionsItemSelected(item);
    }
}
