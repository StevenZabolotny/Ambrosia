package bitcamp.ambrosia;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Riyadh on 4/8/2017.
 */

public class Message implements Parcelable {
    private boolean sentByApp;
    private String message;

    public Message(boolean sentByApp, String message) {
        this.sentByApp = sentByApp;
        this.message = message;
    }

    protected Message(Parcel in) {
        sentByApp = in.readByte() != 0;
        message = in.readString();
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public boolean getSentByApp() {
        return sentByApp;
    }

    public void setSentByApp(boolean sentByApp) {
        this.sentByApp = sentByApp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (sentByApp ? 1 : 0));
        dest.writeString(message);
    }
}
