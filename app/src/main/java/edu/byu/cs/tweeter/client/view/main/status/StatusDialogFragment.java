package edu.byu.cs.tweeter.client.view.main.status;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.squareup.picasso.Picasso;

import java.util.Locale;

import edu.byu.cs.client.R;
import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.presenter.StatusDialogPresenter;

/**
 * Implements the pop-up dialog for sending a new status.
 */
public class StatusDialogFragment extends AppCompatDialogFragment implements StatusDialogPresenter.StatusDialogView {
    private TextView fullName;
    private TextView alias;
    private ImageView image;
    private EditText post;
    private TextView wordCount;
    private StatusDialogPresenter presenter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.status_dialog, null);

        builder.setView(view)
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("POST STATUS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //service
                        presenter.sendNewStatus(post.getText().toString());
                    }
                });

        fullName = view.findViewById(R.id.dialogFullName);
        fullName.setText(Cache.getInstance().getCurrUser().getName());
        fullName.setTextSize(17);
        alias = view.findViewById(R.id.dialogAlias);
        alias.setText(Cache.getInstance().getCurrUser().getAlias());
        alias.setTextSize(15);
        alias.setTextColor(getResources().getColor(R.color.lightGray));
        image = view.findViewById(R.id.dialogImage);
        Picasso.get().load(Cache.getInstance().getCurrUser().getImageUrl()).into(image);
        post = view.findViewById(R.id.dialogPost);

        post.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                presenter.updateWordCount(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        wordCount = view.findViewById(R.id.wordCount);
        wordCount.setText(String.format(Locale.US, "%d / %d", 0, 250));

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        presenter = new StatusDialogPresenter(this);
        super.onAttach(context);
        presenter.addStatusObserver(context);
    }

    @Override
    public void displayFullName(String name) {

    }

    @Override
    public void displayAlias(String alias) {

    }

    @Override
    public void displayImage(Uri image) {

    }

    @Override
    public void displayPost(String postText) {

    }

    @Override
    public void fetchPostText() {

    }

    @Override
    public void updateWordCount(int numWords) {
        wordCount.setText(Integer.toString(numWords));
    }
}
