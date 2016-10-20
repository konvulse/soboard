package kvl.android.kvl.soboard;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by kvl on 8/19/16.
 */
public class ImageListAdapter extends ArrayAdapter<ImageListItem> {
    boolean editing = false;
    private static final String LOG_TAG = "ImageListAdapter";

    public ImageListAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.image_list_item, null);
        }

        if (!this.isEmpty()) {
            ImageListItem item = this.getItem(position);
            if (item != null) {
                TextView imageName = (TextView) v.findViewById(R.id.textView_imageName);
                imageName.setText(item.getName());
            }
        }

        return v;
    }

    public ArrayList<ImageListItem> getArrayList() {
        ArrayList<ImageListItem> images = new ArrayList<>();
        for (int i = 0; i < this.getCount(); ++i) {
            images.add(this.getItem(i));
        }
        return images;
    }

    public boolean makeEditable(AdapterView<?> parent, View view, int position, long id) {
        boolean alreadyEditing = editing;
        View v = view;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.image_list_item, null);
        }

        if (!this.isEmpty()) {
            final ImageListItem item = this.getItem(position);

            if (item != null) {
                final TextView imageName = (TextView) v.findViewById(R.id.textView_imageName);
                final EditText editName = (EditText) v.findViewById(R.id.editText_imageName);
                if (!editing) {
                    editing = true;
                    editName.setText(item.getName());
                    imageName.setVisibility(View.GONE);
                    editName.setVisibility(View.VISIBLE);

                    //TODO: Set focus to the edit text element and display keyboard
                    if (editName.requestFocus()) {
                        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInputFromWindow(editName.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);

                        Log.d(LOG_TAG, "editName took focus");
                    } else {
                        Log.d(LOG_TAG, "editName did not take focus");
                    }

                    editName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            if (actionId == EditorInfo.IME_NULL || event == null) {
                                item.setName(editName.getText().toString());
                                imageName.setText(editName.getText());
                                imageName.setVisibility(View.VISIBLE);
                                editName.setVisibility(View.GONE);
                                editing = false;
                                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.toggleSoftInputFromWindow(editName.getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                return true;
                            } else {
                                return false;
                            }
                        }
                    });
                } else {
                    Log.d(LOG_TAG, "already editing, selecting all");
                }
                editName.selectAll();
            }
        }
        return !alreadyEditing && editing;
    }
}
