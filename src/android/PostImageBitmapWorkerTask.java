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

    public PostImageBitmapWorkerTask(ImageView imageView, String TAG, PostImageFeedFragment fragment, List<ListaDeArquivos> galleryList, MyAdapter myAdapter) {
        mImageView = imageView;
        this.TAG = TAG;
        mFragment = fragment;
        this.galleryList = galleryList;
        this.myAdapter = myAdapter;
    }

    @Override
    protected Bitmap doInBackground(Integer... params) {
        arquivoSelecionado = BitmapFactory.decodeFile(galleryList.get(Integer.valueOf(TAG)).getMiniatura());
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
                mImageView.setImageBitmap(Bitmap.createScaledBitmap(arquivoSelecionado, 90, 90, true));
            }
        }
        myAdapter.notifyItemRangeInserted(Integer.valueOf(TAG),0);
    }
}
