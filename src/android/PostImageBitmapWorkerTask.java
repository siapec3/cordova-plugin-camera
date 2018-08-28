package smsgi.com.br.galeriasmview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Created by desenvolvimento10 on 13/08/18.
 */

public class PostImageBitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
    private ImageView mImageView;
    private String TAG;
    private PostImageFeedFragment mFragment;
    private Bitmap arquivoSelecionado;
    private List<ListaDeArquivos> galleryList;
    private MyAdapter myAdapter;
    private BitmapFactory.Options options;

    public PostImageBitmapWorkerTask(ImageView imageView, String TAG, PostImageFeedFragment fragment, List<ListaDeArquivos> galleryList, MyAdapter myAdapter) {
        mImageView = imageView;
        this.TAG = TAG;
        mFragment = fragment;
        this.galleryList = galleryList;
        this.myAdapter = myAdapter;
        options = new BitmapFactory.Options();
        options.inMutable = true;
    }

    @Override
    protected Bitmap doInBackground(Integer... params) {
        // otimizar em 50% a imagem em memória
        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateInSampleSize(options, 120, 120);
        arquivoSelecionado = BitmapFactory.decodeFile(galleryList.get(Integer.valueOf(TAG)).getMiniatura(), options);
//        Bitmap bitmap = mFragment.getBitmapFromMemCache(params[0]);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        if (arquivoSelecionado != null) {
            arquivoSelecionado.compress(Bitmap.CompressFormat.JPEG, 50, outStream);
        }
//        mFragment.addBitmapToCache(String.valueOf(TAG),arquivoSelecionado);
        return arquivoSelecionado;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if(mImageView.getTag().toString().equals(TAG)) {
            if (arquivoSelecionado != null) {
                mImageView.setImageBitmap(Bitmap.createScaledBitmap(arquivoSelecionado, 120, 120, false));
            }
        }
        myAdapter.notifyItemRangeInserted(Integer.valueOf(TAG),0);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
