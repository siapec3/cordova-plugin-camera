package smsgi.com.br.galeriasmview;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ionicframework.siapec3mobile136142.R;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Marcelio De Oliveira
 * @version 1.0
 * @since Jul 7, 2018, 2:35:39 PM
 */
public class GaleriaSmView extends GaleriaImagensInterface {

    protected FrameLayout previewLayout;
    private RecyclerView recyclerView;
    private Dialog dialog;
    private Dialog previewDialog;
    private Dialog processandoDialog;
    private ProgressDialog progress;
    private ListaDeArquivos arquivoSelecionado = null;
    private String diretorioAtivo = null;
    private File arquivoExibicao;
    private BitmapFactory.Options options;
    private String root = null ;
    private GaleriaSmView galeriaContext;
    private PostImageFeedFragment mFragment;

    public GaleriaSmView(CordovaInterface cordovaInterface, View viewGet, CordovaWebView viewWeb, GaleriaWorker worker, CallbackContext callbackContext) {
        super(cordovaInterface, viewGet, viewWeb, worker, callbackContext);
        onCreate();
        options = new BitmapFactory.Options();
        options.inMutable = true;
    }

    public void onCreate() {
        galeriaContext = this;
        mFragment = new PostImageFeedFragment();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initialize(GaleriaImagensInterface.webView.getContext());
//        super.onCreate(savedInstanceState);
//        dialog.setContentView(getResources().getIdentifier("activity_galeria_sm_view", "layout", getPackageName()));
                dialog.setContentView(R.layout.activity_galeria_sm_view);
                dialog.show();
                //        layoutPrincipal.setContentView(Meta.getResId(activity, "layout", "cordova_camera_plugin"));
                recyclerView = (RecyclerView) dialog.findViewById(R.id.imagegallery);
//        recyclerView.setHasFixedSize(true);
                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(dialog.getContext(), 3);
                layoutManager.setAutoMeasureEnabled(true);
                recyclerView.setLayoutManager(layoutManager);
                MyAdapter adapter = new MyAdapter(activity, criarArvore(null), galeriaContext, mFragment);
                recyclerView.setAdapter(adapter);recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        //At this point the layout is complete and the
                        //dimensions of recyclerView and any child views are known.
                        if (processandoDialog != null && processandoDialog.isShowing()) {
                            processandoDialog.dismiss();
                            processandoDialog = null;
                            progress = null;
                        }
                    }
                });

            }
        });
    }

    @Override
    protected void initialize(Context context) {

        dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            @Override
            public void onBackPressed() {
                super.onBackPressed();
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                    dialog = null;
                    PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
                    r.setKeepCallback(true);
                    callbackContext.sendPluginResult(r);
                    callbackErrorPluginCordova();
                    getView.setVisibility(View.VISIBLE);
                }
            }
        };

        dialog.setCancelable(false);

//        layoutPrincipal.setContentView(Meta.getResId(activity, "layout", "cordova_camera_plugin"));
    }

    private void processando() {

        processandoDialog = new Dialog(dialog.getContext(), android.R.style.Theme_WithActionBar) {
            @Override
            public void onBackPressed() {
                super.onBackPressed();
                if (processandoDialog != null && processandoDialog.isShowing()) {
                    processandoDialog.dismiss();
                    processandoDialog = null;
                }
            }
        };
        progress = new ProgressDialog(processandoDialog.getContext());
        progress.setMessage("Processando!");
        processandoDialog.setCancelable(false);
        processandoDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (diretorioAtivo != null && !(diretorioAtivo.equals("/storage") || diretorioAtivo.equals("/storage/emulated"))) {
            mostrarPastasDoDiretorio(this.criarArvore(diretorioAtivo.substring(0, diretorioAtivo.lastIndexOf(File.separator))));
        } else {
            super.onBackPressed();
        }
    }
    private long mLastClickTime = 0;

    Integer pageNumber = 1;
    Bitmap imagemAEnviar;

    private void previsualizacao(Context context, String file) {
        this.arquivoExibicao = new File(file);
        if (arquivoExibicao != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    options.inJustDecodeBounds = true;
                    options.inSampleSize = 3;
                    imagemAEnviar = BitmapFactory.decodeFile(arquivoExibicao.getAbsolutePath(),options);

                    // mis-clicking prevention, using threshold of 1000 ms
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 2000){
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    previewDialog = new Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
                        @Override
                        public void onBackPressed() {
//                            super.onBackPressed();
                            if (getFile() != null) { getFile().delete(); }
                            if (previewDialog != null && previewDialog.isShowing()) {
                                previewDialog.dismiss();
                                previewDialog = null;
                            }
                        }
                    };

                    String fileName = arquivoExibicao.getName();
                    String extensao = null;
                    if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0){
                        extensao = fileName.substring(fileName.lastIndexOf(".")+1);
                    }

                    if (extensao.equalsIgnoreCase("png") || extensao.equalsIgnoreCase("gif") ||
                            extensao.equalsIgnoreCase("jpg") || extensao.equalsIgnoreCase("jpeg") || extensao.equalsIgnoreCase("pdf")){
//                        changeBotoes(true);
                        ImageView imagemPreview = null;
                        imagemPreview = new ImageView(activity);
                        options.inJustDecodeBounds = false;
                        options.inSampleSize = calculateInSampleSize(options, 840, 840);
                        imagemAEnviar = BitmapFactory.decodeFile(arquivoExibicao.getAbsolutePath(), options);
                        imagemPreview.setImageBitmap(imagemAEnviar);
                        previewLayout = new FrameLayout(activity);
                        previewDialog.setContentView(previewLayout);
                        previewDialog.setCancelable(false);
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        previewLayout.setLayoutParams(layoutParams);
                        previewLayout.addView(imagemPreview);
                        enviarFoto();
                        previewDialog.show();
                    } else if (extensao.equalsIgnoreCase("pdf")){
                        changeBotoes(true);
                    } else {
                        dialogDeMensagem("Este tipo de arquivo "+ extensao +" não é suportado");
                    }
                }
            });
        }
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

    private void enviarFoto() {
        ImageButton enviarFotoButton = new ImageButton(activity);
        enviarFotoButton.setImageResource(R.drawable.paperfly_send);

        enviarFotoButton.setBackground(null);
        LinearLayout linearLayout = new LinearLayout(activity);
        LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lllp.gravity = Gravity.BOTTOM;
        lllp.setMargins(0,0,20,60);
        enviarFotoButton.setLayoutParams(lllp);
        linearLayout.addView(enviarFotoButton);
        enviarFotoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dialogDeComandos();
            }
        });
        previewLayout.addView(linearLayout);
    }

    protected void dialogDeComandos(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Deseja utilizar esta imagem?")
                .setCancelable(false)
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int id) {
                    if (previewDialog != null && previewDialog.isShowing()) {
                        previewDialog.dismiss();
                        previewDialog = null;
                    }
                    if (dialog != null && dialog.isShowing()) {

                        File cache = new File(arquivoSelecionado.getMiniatura()+"_tmp");

                        try {

                            FileOutputStream fos = new FileOutputStream(cache);
                            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                            if (imagemAEnviar != null) {
                                imagemAEnviar.compress(Bitmap.CompressFormat.JPEG, 70, outStream);
                            }
                            byte[] bitmapdata = outStream.toByteArray();
                            fos.write(bitmapdata);
                            fos.flush();
                            fos.close();
                        } catch (FileNotFoundException e) {
                            LOG.e(getClass().getSimpleName(), "Error writing bitmap", e);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        setFile(cache); //arquivo compactado
                        dialog.dismiss();
                        dialog = null;
                        getView.setVisibility(View.VISIBLE);
                        worker.mCallBack.onSuccess(getFile());
                        getFile().delete();
                    }
                    }
                })
                .setNegativeButton("Voltar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int id) {
                        if (getFile() != null) { getFile().delete(); }
                        if (previewDialog != null && previewDialog.isShowing()) {
                            previewDialog.dismiss();
                            previewDialog = null;
                        }
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    protected void dialogDeMensagem(String mensagem){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(mensagem)
                .setCancelable(false)
                .setNegativeButton("Voltar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int id) {
                        if (getFile() != null) { getFile().delete(); }
                        if (previewDialog != null && previewDialog.isShowing()) {
                            previewDialog.dismiss();
                            previewDialog = null;
                        }
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    private List<ListaDeArquivos> arquivosSdCard() {
        List<ListaDeArquivos> arquivos = new ArrayList();
        try {
            File[] externalDirs = getExternalFilesDirs(null);

            for (File file : externalDirs) {
                String path = file.getPath().split("/Android")[0];
                boolean addPath = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    addPath = Environment.isExternalStorageRemovable(file);
                } else {
                    addPath = Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(file));
                }
                if (addPath) {
                    arquivos.add(new ListaDeArquivos("Cartão de Memória", path, true));
                }
            }
        }catch (Exception iox){

        }
        return arquivos;
    }

    private List<ListaDeArquivos> criarArvore(String caminho) {
        List<ListaDeArquivos> arquivos = new ArrayList();
        this.diretorioAtivo = caminho;
        if (caminho == null || caminho.equals("/storage") || caminho.equals("/storage/emulated")) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && arquivosSdCard().size() > 0) {
                arquivos.add(new ListaDeArquivos("Armazenamento Interno", Environment.getExternalStorageDirectory().getAbsolutePath(), new File(Environment.getExternalStorageDirectory().getAbsolutePath()).isDirectory()));
                arquivos.addAll(arquivosSdCard());
                return arquivos;
            } else {
                this.root = Environment.getExternalStorageDirectory().getAbsolutePath();
                return criarArvore(Environment.getExternalStorageDirectory().getAbsolutePath());
            }
        } else if (root == null || !root.equals(caminho)){
            arquivos.add(new ListaDeArquivos("..", caminho.substring(0, caminho.lastIndexOf(File.separator)),  true));
        }
        File[] arquivosInternos = new File(caminho).listFiles();
        for (int i = 0; i < arquivosInternos.length; i++) {
            ListaDeArquivos lista = new ListaDeArquivos(arquivosInternos[i].getName(), arquivosInternos[i].getAbsolutePath(), arquivosInternos[i].isDirectory() );
            arquivos.add(lista);
        }
        Collections.sort(arquivos);
        return arquivos;
    }


    public void entrarPasta(String arquivo, boolean diretorio) {
        if (diretorio) {
            changeBotoes(false);
            mostrarPastasDoDiretorio(criarArvore(arquivo));
        } else {

            previsualizacao(activity, arquivo);
        }
    }

    public void mostrarPastasDoDiretorio(List<ListaDeArquivos> arquivos) {
        processando();
        MyAdapter adapter = new MyAdapter(activity, arquivos, this, mFragment);
        recyclerView.setAdapter(adapter);
        recyclerView.setAdapter(adapter);recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //At this point the layout is complete and the
                //dimensions of recyclerView and any child views are known.
                if (processandoDialog != null && processandoDialog.isShowing()) {
                    processandoDialog.dismiss();
                    processandoDialog = null;
                    progress = null;
                }
            }
        });
    }

    public void setArquivoSelecionado(ListaDeArquivos arquivoSelecionado) {
        this.arquivoSelecionado = arquivoSelecionado;
    }

    public void changeBotoes(boolean exibir) {
        LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.botoes);
        if (exibir) {
            layout.setVisibility(View.VISIBLE);
//
            Button selecionar = (Button) dialog.findViewById(R.id.selecionar);
            selecionar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /**
                     * Colocar aqui a parte para retornar para o ionic
                     * a variavel do arquivo é arquivoSelecionado.getMiniatura()
                     */
                    setFile(new File(arquivoSelecionado.getMiniatura()));
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                        dialog = null;
                        getView.setVisibility(View.VISIBLE);
                        worker.mCallBack.onSuccess(getFile());
                    }
                }
            });
        } else {
            layout.setVisibility(View.INVISIBLE);
        }

    }


}
