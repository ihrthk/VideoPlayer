package zhangls.dlink;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;

public class VideoSurfaceView extends SurfaceView {
    private int mMode = 0;
    private int mWidth, mHeight;

    public VideoSurfaceView(Context context) {
        super(context);
    }

    public VideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(mWidth, widthMeasureSpec);
        int height = getDefaultSize(mHeight, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    public void setViewSize(int viewWidth, int viewHeight) {
        LayoutParams lp = getLayoutParams();
        lp.height = viewWidth;
        lp.width = viewHeight;
        setLayoutParams(lp);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
