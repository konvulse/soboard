package kvl.android.kvl.soboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by kvl on 8/19/16.
 */
public class ImageListAdapter extends ArrayAdapter<ImageListItem> {
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

        if(!this.isEmpty()) {
            ImageListItem item = this.getItem(position);
            if(item != null) {
                TextView imageName = (TextView) v.findViewById(R.id.textView_imageName);
                imageName.setText(item.getName());
            }
        }

        return v;
    }

    public ArrayList<ImageListItem> getArrayList() {
        ArrayList<ImageListItem> images = new ArrayList<>();
        for(int i = 0; i < this.getCount(); ++i) {
            images.add(this.getItem(i));
        }
        return images;
    }
}
