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
    private String miniatura;
    private boolean selecionado;
    private boolean diretorio;

    public ListaDeArquivos(String tituloDaImagem, String miniatura, boolean diretorio) {
        this.tituloDaImagem = tituloDaImagem;
        this.miniatura = miniatura;
        this.selecionado = false;
        this.diretorio = diretorio;
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

    public String getMiniatura() {
        return miniatura;
    }

    public void setMiniatura(String miniatura) {
        this.miniatura = miniatura;
    }

    public boolean isDiretorio() {
        return diretorio;
    }

    public void setDiretorio(boolean diretorio) {
        this.diretorio = diretorio;
    }

    @Override
    public int compareTo(@NonNull Object obj) {
        ListaDeArquivos arq = (ListaDeArquivos) obj;
        if (this.isDiretorio() && !arq.isDiretorio()) {
            return -1;
        } else if (!this.isDiretorio() && arq.isDiretorio()) {
            return 1;
        } else {
            return this.getTituloDaImagem().compareTo(arq.getTituloDaImagem());
        }
    }
}