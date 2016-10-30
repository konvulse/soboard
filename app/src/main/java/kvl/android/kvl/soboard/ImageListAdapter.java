package kvl.android.kvl.soboard;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Created by kvl on 8/19/16.
 */
public class ImageListAdapter extends ArrayAdapter<ImageListItem> {
    boolean editing = false;
    int editPosition = -1;
    private static final String LOG_TAG = "ImageListAdapter";

    public ImageListAdapter(Context context, int resource) {
        super(context, resource);
    }

    public boolean isEditing() { return editing; }

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
                ImageView imageThumb = (ImageView) v.findViewById(R.id.imageView_listThumbnail);
                int dim = (int) getContext().getResources().getDimension(R.dimen.thumbsize_length);
                try {
                    imageThumb.setImageBitmap(item.getScaledImageBitmap(dim, dim));
                } catch (FileNotFoundException e) {
                    //imageThumb.setImageDrawable(R.drawable.ic_dialog_alert);
                    Log.e(LOG_TAG, "Image not found.");
                }
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

    private void startEdit(View v, int position) {

        if (this.isEmpty()) {
            return;
        }

        final EditText editName = (EditText) v.findViewById(R.id.editText_imageName);

        if (editing) {
            Log.d(LOG_TAG, "Already editing, selecting all.");
            editName.selectAll();
            return;
        }

        final ImageListItem item = this.getItem(position);
        if (item == null) {
            Log.d(LOG_TAG, "List item was null, cannot edit a null item.");
            return;
        }

        editPosition = position;
        editing = true;
        final TextView imageName = (TextView) v.findViewById(R.id.textView_imageName);
        final LinearLayout itemLayout = (LinearLayout) v.findViewById(R.id.layout_imageListItem);

        editName.setText(item.getName());
        imageName.setVisibility(View.GONE);
        editName.setVisibility(View.VISIBLE);

        if (editName.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInputFromWindow(editName.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);
            editName.selectAll();
            Log.d(LOG_TAG, "editName took focus");
        } else {
            Log.d(LOG_TAG, "editName did not take focus");
        }

        editName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ENTER) {
                    endEdit(item, imageName, editName);
                    return true;
                } else {
                    return false;
                }
            }
        });

        itemLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d(LOG_TAG, "Handling focus change during item edit");
                if (!hasFocus) {
                    Log.d(LOG_TAG, "Item lost focus");
                    endEdit(item, imageName, editName);
                } else {
                    Log.d(LOG_TAG, "Item gained focus");
                }

            }
        });
    }

    private void endEdit(ImageListItem item, TextView imageName, EditText editName) {
        item.setName(editName.getText().toString());
        imageName.setText(item.getName());
        imageName.setVisibility(View.VISIBLE);
        editName.setVisibility(View.GONE);
        editing = false;
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editName.getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void stopEditing(ViewGroup parent) {
        if (editing && !this.isEmpty()) {
            View itemView = this.getView(editPosition, null, null);
            final ImageListItem item = this.getItem(editPosition);
            if (item != null) {
                final TextView imageName = (TextView) itemView.findViewById(R.id.textView_imageName);
                final EditText editName = (EditText) itemView.findViewById(R.id.editText_imageName);
                endEdit(item, imageName, editName);
            }
        }
    }

    public boolean makeEditable(View view, int position) {
        boolean alreadyEditing = editing;

        View v = view;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.image_list_item, null);
        }

        startEdit(v, position);

        return !alreadyEditing && editing;
    }
}
