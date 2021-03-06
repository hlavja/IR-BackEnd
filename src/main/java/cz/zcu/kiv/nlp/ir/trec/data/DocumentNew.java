package cz.zcu.kiv.nlp.ir.trec.data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Tigi on 8.1.2015.
 *
 * Ukázka implementace rozhraní {@link Document}
 *
 * Tuto třídu si můžete libovolně upravovat pokud vám nevyhovuje nebo můžete vytvořit vlastní třídu, která
 * implementuje rozhraní {@link Document}.
 *
 */
public class DocumentNew implements Document, Serializable {
    private static final long serialVersionUID = -5097715898427114007L;
    String text;
    String id;
    String title;
    Date date;

    public DocumentNew() {
    }

    public DocumentNew(String text, String id, Date date) {
        this.text = text;
        this.id = id;
        this.date = date;
    }

    public DocumentNew(String text, String id, String title, Date date) {
        this.text = text;
        this.id = id;
        this.title = title;
        this.date = date;
    }

    @Override
    public String toString() {
        return "DocumentNew{" +
                ", id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", date=" + date +
                "text='" + text + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
