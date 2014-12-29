package zhangls.dlink;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MediaFrameHolder {
    private int maxCount = 5;
    public ArrayBlockingQueue<MediaFrame> _free;
    private ArrayBlockingQueue<MediaFrame> _filled;

    public MediaFrameHolder() {
        _free = new ArrayBlockingQueue<MediaFrame>(maxCount, true);
        _filled = new ArrayBlockingQueue<MediaFrame>(maxCount, true);

        for (int i = 0; i < maxCount; i++) {
            _free.add(new MediaFrame(ByteBuffer.allocate(1024 * 20)));
        }
    }

    public MediaFrameHolder(int count, int frameSize) {
        maxCount = count;
        _free = new ArrayBlockingQueue<MediaFrame>(maxCount, true);
        _filled = new ArrayBlockingQueue<MediaFrame>(maxCount, true);

        for (int i = 0; i < maxCount; i++) {
            _free.add(new MediaFrame(ByteBuffer.allocate(frameSize)));
        }
    }

    public boolean queue_free(MediaFrame frame) {
        frame.clean();
        try {
            return this._free.add(frame);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean queue_filled(MediaFrame frame) {
        try {
            return this._filled.add(frame);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public MediaFrame dequeue_free() {
        try {
            return this._free.poll(5L, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized MediaFrame dequeue_filled() {
        try {
            return this._filled.poll(5L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void clearHolder() {
        _free.clear();
        _filled.clear();
    }

}
