package bitcamp.ambrosia;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Riyadh on 4/8/2017.
 */

public class MessagesListAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private ArrayList<Message> messages;

    public MessagesListAdapter(Context context, ArrayList<Message> messages) {
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.messages = messages;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void add(Message message) {
        messages.add(message);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageViewHolder msgHolder;
        View v = convertView;
        if(convertView == null) {
            msgHolder = new MessageViewHolder();
            v = layoutInflater.inflate(R.layout.item_messages, null);
            msgHolder.name = (TextView) v.findViewById(R.id.name_sender);
            msgHolder.message = (TextView) v.findViewById(R.id.message);
            msgHolder.bg = (LinearLayout) v.findViewById(R.id.msgbg);
            v.setTag(msgHolder);
        } else {
            msgHolder = (MessageViewHolder) v.getTag();
        }

        Message message = (Message) getItem(position);
        if(message.getSentByApp()) {
            msgHolder.name.setText("Ambrosia");
            msgHolder.bg.setBackgroundResource(R.drawable.ambrosiachat);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.LEFT;
            msgHolder.bg.setLayoutParams(params);
        } else {
            msgHolder.name.setText("You");
            msgHolder.bg.setBackgroundResource(R.drawable.userchat);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.RIGHT;
            msgHolder.bg.setLayoutParams(params);
        }

        msgHolder.message.setText(message.getMessage());

        return v;
    }

    class MessageViewHolder {
        private TextView name;
        private TextView message;
        private LinearLayout bg;
    }
}
