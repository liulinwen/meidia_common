package cn.nubia.media.photos;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestOptions;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import cn.nubia.media.photos.workthread.ThreadPool;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("cn.nubia.media.photos.test", appContext.getPackageName());
    }

    @Test
    public void testClusering() {
        AppContext app = new AppContxtImp(InstrumentationRegistry.getTargetContext());
        Clustering clustering = new Clustering(app);
        clustering.run();
        int count = clustering.getTotalCount();
        assertNotEquals(0, count);
    }

    @Test
    public void testCluseringWithMediaSet() {
        AppContext app = new AppContxtImp(InstrumentationRegistry.getTargetContext());
        Clustering clustering = new Clustering(app);
        clustering.run();
        MediaSet set = new MediaSet(app);
        set.reload();
        assertEquals(set.getMediaItemCount(), clustering.getTotalCount());
    }

    @Test
    public void tesetDataAdapterProxy() throws InterruptedException {
        AppContext app = new AppContxtImp(InstrumentationRegistry.getTargetContext().getApplicationContext());
        DataAdapterProxy proxy = new DataAdapterProxy(app, null);
        proxy.onResume();

        Clustering clustering = new Clustering(app);
        clustering.run();
        Map<Integer, Clustering.Cluster> maps = clustering.getClusters();
        SystemClock.sleep(3000);
        //count
        assertEquals(proxy.size(), maps.size() + clustering.getTotalCount());

        Object text = proxy.get(0);
        int type = proxy.getType(0);
        assertTrue(text instanceof String);
        assertEquals(type, 0);

        Object bitmap = proxy.get(1);
        int type_1 = proxy.getType(1);
        assertTrue(bitmap instanceof Bitmap);
        assertEquals(type_1, 2);

        Object text_5 = proxy.get(5);
        int type_5 = proxy.getType(5);
        MediaItem item_5 = proxy.getMediaItem(5);
        assertTrue(text_5 instanceof String);
        assertEquals(type_5, 0);
        assertEquals(item_5, null);

        MediaItem item_6 = proxy.getMediaItem(6);
        assertNotNull(item_6);

    }

    @Test
    public void testGlide(){
        AppContext app = new AppContxtImp(InstrumentationRegistry.getTargetContext().getApplicationContext());
        DataAdapterProxy proxy = new DataAdapterProxy(app, null);
        proxy.onResume();
        SystemClock.sleep(5000);

        MediaItem item_1 = proxy.getMediaItem(1);

        Bitmap bitmapGilde = loadBitmapWithGlide(app.getAndroidContext(), item_1.getUrl());

        Bitmap bitmap1 = (Bitmap)proxy.get(1);

        proxy.onPause();
    }

    @Nullable
    private Bitmap loadBitmapWithGlide(Context context, String filePath) {
        FutureTarget<Bitmap> futureTarget =
                Glide.with(context)
                        .asBitmap()
                        .load(filePath)
                        .apply(RequestOptions.overrideOf(200).centerCrop())
                        .submit();

        try {
            return futureTarget.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
