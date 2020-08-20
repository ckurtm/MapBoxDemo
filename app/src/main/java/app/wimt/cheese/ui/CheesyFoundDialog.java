package app.wimt.cheese.ui;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.tes.R;

import app.wimt.cheese.meta.CheesyTreasure;

/**
 * We need some way to add a Cheezy Note. We will also require some way to show the user the cheezy note..
 * Feel free to just use AlertDialog if time is an issue.
 */
public class CheesyFoundDialog extends Dialog implements View.OnClickListener {

    public Activity context;
    public View pickButton, exitButton;
    public TextView noteText;
    public INoteDialogListener listener;
    private CheesyTreasure treasure;

    public CheesyFoundDialog(Activity context, CheesyTreasure treasure,INoteDialogListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        this.treasure = treasure;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.found_note);

        noteText = findViewById(R.id.found_note);
        pickButton = findViewById(R.id.found_pick);
        exitButton =  findViewById(R.id.found_close);
        pickButton.setOnClickListener(this);
        exitButton.setOnClickListener(this);
        noteText.setText(treasure.getNote());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.found_pick:
                listener.onNoteRemoved(treasure.getNote());
                dismiss();
                break;
            case R.id.found_close:
                dismiss();
                break;
            default:
                break;
        }
    }

    public interface INoteDialogListener {
         void onNoteRemoved(String note);
    }
}
