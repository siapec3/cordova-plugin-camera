package smsgi.com.br.galeriasmview;

import android.support.annotation.NonNull;

import java.io.File;

/**
 * Created by desenvolvimento10 on 05/07/18.
 * @author Marcelio De Oliveira
 * @version 1.0
 * @since Jul 5, 2018, 2:35:39 PM
 */
public class ListaDeArquivos implements Comparable {

    private String tituloDaImagem;
    private File miniatura;
    private boolean selecionado;

    public ListaDeArquivos(String tituloDaImagem, File miniatura) {
        this.tituloDaImagem = tituloDaImagem;
        this.miniatura = miniatura;
        this.selecionado = false;
    }

    public boolean isSelecionado() {
        return selecionado;
    }

    public void setSelecionado(boolean selecionado) {
        this.selecionado = selecionado;
    }

    public String getTituloDaImagem() {
        return tituloDaImagem;
    }

    public void setTituloDaImagem(String tituloDaImagem) {
        this.tituloDaImagem = tituloDaImagem;
    }

    public File getMiniatura() {
        return miniatura;
    }

    public void setMiniatura(File miniatura) {
        this.miniatura = miniatura;
    }


    @Override
    public int compareTo(@NonNull Object obj) {
        ListaDeArquivos arq = (ListaDeArquivos) obj;
        if (this.getMiniatura().isDirectory() && !arq.getMiniatura().isDirectory()) {
            return -1;
        } else if (!this.getMiniatura().isDirectory() && arq.getMiniatura().isDirectory()) {
            return 1;
        } else {
            return this.getTituloDaImagem().compareTo(arq.getTituloDaImagem());
        }
    }
}