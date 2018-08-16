package smsgi.com.br.galeriasmview;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ionicframework.siapec3mobile136142.R;

import java.util.List;

/**
 * Created by desenvolvimento10 on 05/07/18.
 */


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private List<ListaDeArquivos> galleryList;
    private Activity activity;
    private GaleriaSmView galeriaSm;
    private View view;
    private PostImageFeedFragment mFragment;
    private ViewHolder viewHolderThread;
    private String TAG;
    private MyAdapter myAdapter;

    public MyAdapter(Activity context, List<ListaDeArquivos> galleryList, GaleriaSmView galeria, PostImageFeedFragment mFragment) {
        this.activity = context;
        this.galleryList = galleryList;
        this.galeriaSm = galeria;
        this.mFragment = mFragment;
        this.myAdapter = this;

    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
//        setContentView(getResources().getIdentifier("cell_layout", "layout", getPackageName()));
        this.view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    public void refreshData(){

    }

    @Override
    public void onBindViewHolder(MyAdapter.ViewHolder viewHolder, int position) {
        viewHolder.img.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        viewHolder.img.setTag(position);
        viewHolder.img.setImageDrawable(null);
        TAG = String.valueOf(position);
        viewHolder.img.setTag(position);

        if (galleryList != null && position < galleryList.size() && galleryList.get(position) != null) {
            if (!galleryList.get(position).getMiniatura().isDirectory()) {
                String formato = galleryList.get(position).getTituloDaImagem().substring(galleryList.get(position).getTituloDaImagem().lastIndexOf(".") + 1);
                formatarThumbnails(viewHolder, formato);
                viewHolderThread = viewHolder;
//                       arquivoSelecionado = BitmapFactory.decodeFile(galleryList.get(position).getMiniatura().getAbsolutePath());
                Bitmap bitmap = mFragment.getBitmapFromMemCache(String.valueOf(TAG));
                if (bitmap == null) {
//                    activity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            PostImageBitmapWorkerTask task = new PostImageBitmapWorkerTask(viewHolderThread.img, TAG, mFragment, galleryList, myAdapter);
//                            task.execute(Integer.valueOf(TAG));
//                        }
//                    });

               } else {
                   formato = galleryList.get(position).getTituloDaImagem().substring(galleryList.get(position).getTituloDaImagem().lastIndexOf(".") + 1);
                   formatarThumbnails(viewHolder, formato);
               }
               viewHolder.title.setText(galleryList.get(position).getTituloDaImagem());
            } else {
                viewHolder.title.setText(galleryList.get(position).getTituloDaImagem());
//                viewHolder.img.setImageDrawable(IconeUtils.resizeImage(context, R.drawable.folder, 60, 60));
                viewHolder.img.setImageResource(R.drawable.folder);

            }
        }
        if (galleryList.get(position).isSelecionado()) {
            viewHolder.caixa.setBackgroundResource(R.drawable.customborder_selecionado);
        } else {
            viewHolder.caixa.setBackgroundResource(R.drawable.customborder);
        }
        viewHolder.img.setOnClickListener(clickSelecao());
        viewHolder.img.setOnLongClickListener(longClickSelecao());
    }

    private void formatarThumbnails(ViewHolder viewHolder, String formato){
        if (formato.equals("doc") || formato.equals("docx")) {
            viewHolder.img.setImageResource(R.drawable.word);
        } else if (formato.equals("xls") || formato.equals("xlsx")) {
            viewHolder.img.setImageResource(R.drawable.excel);
        } else if (formato.equals("pdf")) {
            viewHolder.img.setImageResource(R.drawable.pdf);
        } else if (formato.equals("ppt") || formato.equals("pptx")) {
            viewHolder.img.setImageResource(R.drawable.ppt);
        } else if (formato.equals("jpeg") || formato.equals("jpg")) {
            viewHolder.img.setImageResource(R.drawable.image_area);
        } else {
            viewHolder.img.setImageResource(R.drawable.document);
        }
    }

    @Override
    public void onViewRecycled(ViewHolder viewHolder){
//        if (galeriaSm.processandoDialog != null && galeriaSm.processandoDialog.isShowing()) {
//            galeriaSm.processandoDialog.dismiss();
//            galeriaSm.processandoDialog = null;
//        }
    }

    private OnClickListener clickSelecao() {
        return new OnClickListener() {
            @Override
            public void onClick(View view) {
                galeriaSm.setArquivoSelecionado(galleryList.get(Integer.valueOf(view.getTag().toString())));
                if (galleryList.get(Integer.valueOf(view.getTag().toString())).getMiniatura() != null) {
                    galeriaSm.setArquivoSelecionado(galleryList.get(Integer.valueOf(view.getTag().toString())));
                    galeriaSm.entrarPasta(galleryList.get(Integer.valueOf(view.getTag().toString())).getMiniatura());
                }
            }
        };
    }

    private View.OnLongClickListener longClickSelecao() {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                marcarSelecionado((int) Integer.valueOf(view.getTag().toString()));
                galeriaSm.setArquivoSelecionado(galleryList.get( Integer.valueOf(view.getTag().toString())));
                if (galleryList.get( Integer.valueOf(view.getTag().toString())).getMiniatura().isDirectory()) {
                    galeriaSm.entrarPasta(galleryList.get( Integer.valueOf(view.getTag().toString())).getMiniatura());
                } else {
                    galeriaSm.setArquivoSelecionado(galleryList.get( Integer.valueOf(view.getTag().toString())));
                    notifyDataSetChanged();
                    galeriaSm.changeBotoes(true);
                }
                return true;
            }
        };
    }


    @Override
    public int getItemCount() {
        return galleryList.size();
    }

    private void marcarSelecionado(int indice) {
        for (int i = 0; i < galleryList.size(); i++) {
            galleryList.get(i).setSelecionado(i == indice);
        }
    }



    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView title;
        private ImageView img;
        private LinearLayout caixa;
        public ViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.titulo);
            img = (ImageView) view.findViewById(R.id.imagem);
            caixa = (LinearLayout) view.findViewById(R.id.caixa);
        }
    }
}
