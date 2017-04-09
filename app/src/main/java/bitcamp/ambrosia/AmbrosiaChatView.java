package bitcamp.ambrosia;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;

import android.graphics.drawable.shapes.RoundRectShape;
import android.view.View;

public class AmbrosiaChatView extends View {
    private ShapeDrawable AmbrosiaChatMessage;
    private ShapeDrawable UserChatMessage;
    private final int MessageLength;

    public AmbrosiaChatView(Context context) {
        super(context);

        MessageLength = (int) (0.75 * MainActivity.maxWidth);
        float[] outerRad = new float[] {10, 10, 10, 10, 10, 10, 10, 10};
        AmbrosiaChatMessage = new ShapeDrawable(new RoundRectShape(outerRad, null, null));
        AmbrosiaChatMessage.getPaint().setColor(0xb3b3cc);
    }

    public void setNextMessage(int x, int y, int height) {
        float[] outerRad = new float[] {10, 10, 10, 10, 10, 10, 10, 10};
        AmbrosiaChatMessage = new ShapeDrawable(new RoundRectShape(outerRad, null, null));
    }

    protected void onDraw(Canvas canvas) {
        AmbrosiaChatMessage.draw(canvas);
    }
}